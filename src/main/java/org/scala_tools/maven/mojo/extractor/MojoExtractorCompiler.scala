package org.scala_tools.maven.mojo.extractor

import scala.tools.nsc._
import scala.tools.nsc.reporters._
import org.apache.maven.project.MavenProject
import org.scala_tools.maven.mojo.annotations.goal;

import org.apache.maven.plugin.descriptor.MojoDescriptor;

import scala.collection.JavaConversions._

import org.scala_tools.maven.mojo.util.MavenProjectTools

class MojoExtractorCompiler(project: MavenProject) extends MavenProjectTools with MojoExtractionInfo {
  //Method to extract mojo description from a source file.
  def extract(sourceFiles: String*): Seq[MojoDescriptor] = {
    //helper method to initialize settings
    def initialize: (Settings, Reporter) = {
      val settings = new Settings();
      //TODO - Set settings
      settings.classpath.value = getCompileClasspathString(project)
      settings.stop.tryToSetColon(List("superaccessors"))
      settings.sourcepath.tryToSet(project.getCompileSourceRoots().asInstanceOf[java.util.List[String]].toList)
      val reporter = new ConsoleReporter(settings);
      (settings, reporter)
    }


    //helper method to execute presentation compiler
    def execute(settings: Settings, reporter: Reporter) = {
      val compiler = new Global(settings, reporter) with MojoAnnotationExtractor {
        //override def onlyPresentation = true
      }
      //Extract mojo description
      def extractMojos(unit: compiler.CompilationUnit) = {
        for (info <- compiler.parseCompilationUnitBody(unit.body)) yield {
          extractMojoDescriptor(info)
        }
      }

      val run = new compiler.Run
      println("TRACER : compiling: " + sourceFiles.toList)
      run.compile(sourceFiles.toList)

      for (unit <- run.units if !unit.isJava) yield {
        extractMojos(unit)
      }
    }

    val (settings, reporter) = initialize
    execute(settings, reporter).toList.flatMap(x => x)
  }
}


import scala.tools.nsc.ast._
import scala.tools.nsc.symtab.{Flags, SymbolTable}
import org.scala_tools.maven.mojo.annotations._

trait MojoAnnotationExtractor extends CompilationUnits {
  self: Global =>

  /**Pulls all mojo classes out of the body of source code. */
  def parseCompilationUnitBody(body: Tree) = {

    /**Slow method to go look for the definition of a parent class */
    def pullParentClass(symbol: Symbol) = {
      currentRun.units.toList.flatMap {
        unit =>
          val x = unit.body.filter(_.isInstanceOf[ClassDef]).find(_.symbol == symbol).map(_.asInstanceOf[ClassDef])
          x
      }
    }

    /**Pulls the name of the parent class */
    def pullParentClassSymbol(parent: Tree) = {
      parent match {
        case t@TypeTree() => Some(t.symbol)
        case _ => None
      }
    }

    object string {
      private val stringClass = classOf[String].getCanonicalName

      def unapply(tree: Tree) = {
        println("TRACER: extracting strning: " + tree.tpe.widen + " === " + stringClass)
        tree.tpe.widen.toString match {
          case `stringClass` => Some(tree.toString)
          case _ => None 
        }
      }
    }

    object boolean {
      private val TRUE = classOf[String].getCanonicalName + "(true)"
      private val FALSE = classOf[String].getCanonicalName + "(false)"

      def unapply(tree: Tree) = {
        println("TRACER: extracting strning: " + tree.tpe.toString + " === " + TRUE)
        tree.tpe.toString match {
          case TRUE => Some(true)
          case FALSE => Some(false)
          case _ => None
        }
      }
    }

    /**Parses a list of annotations into a list of MojoAnnotationInfo classes */
    def parseAnnotations(annotations: List[AnnotationInfo]) = {
      for(annotation <- annotations) yield {
        // TODO - In the future the second arg to Annotation may be needed... (as it's what's inside the
        // anonymous instantiation of an annotatioin, i.e. @xyz {}
        //TODO - Do we need to match on constructor?
        annotation.args match {
          case Nil => MavenAnnotation(annotation.atp.safeToString)
//          case value1 :: Nil => MavenAnnotation(annotation.atp.safeToString, value1)
//          case value1 :: value2 :: Nil => MavenAnnotation(annotation.atp.safeToString, value1, value2)
//          case _ => new IllegalArgumentException("Found unsupported annotation (%s)".format(annotation.atp.safeToString))
            case string(value) :: Nil => MavenAnnotation(annotation.atp.safeToString, value)
            case boolean(value) :: Nil => MavenAnnotation(annotation.atp.safeToString, value)
            case string(value1) :: string(value2) :: Nil => MavenAnnotation(annotation.atp.safeToString, value1, value2)
            case x =>
              println("TRACER: Arguments: " + x)
              MavenAnnotation(annotation.atp.safeToString)
        }
      }
    }

    /**Pulls out information about all injectable variables based on mojo annotaitons. */
    def parseMojoInjectedVars(classImpl: Tree) = {
      for{node@ValDef(_, name, tpt, _) <- classImpl.children
//          if name.toString.endsWith("_$eq") //TODO - only var like setters? for now this is fine...
          annotation <- node.symbol.annotations
          if annotation.atp.safeToString == classOf[parameter].getName
//          argument@ValDef(_, _, tpt, _) :: Nil <- vparams //setter should only have ONE argument!
      } yield {
        val varInfo = new MojoInjectedVarInfo(name.toString, tpt.toString, parseAnnotations(node.symbol.annotations))
        varInfo
      }
    }

    object isGoal {
      def unapply(classDef: ClassDef) = classDef.symbol.annotations.exists(_.toString.contains("org.scala_tools.maven.mojo.annotations.goal"))
    }

    object Goal {
      def unapply(classDef: ClassDef): Option[(String, List[MavenAnnotation], List[MojoInjectedVarInfo])] = Some((classDef.symbol.tpe.safeToString, parseAnnotations(classDef.symbol.annotations), parseMojoInjectedVars(classDef.impl)))
    }

    /**Pulls out mojo information froma mojo class */
    def parseMojoClass(mojoClass: ClassDef): MojoClassInfo = mojoClass match {
      case Goal(name, annotations, args) =>
        val parentSymbols = mojoClass.impl.parents.toList.flatMap(pullParentClassSymbol)
        val parentClasses = parentSymbols.flatMap(pullParentClass)

        val parentArgs = parentClasses.flatMap(x => parseMojoInjectedVars(x.impl))

        //Combine all mojo injectable variables...
        val finalArgs = args ++ parentArgs
        val mojoInfo = new MojoClassInfo(name, annotations, finalArgs)
        println("TRACER: Extracted: " + mojoInfo)
        mojoInfo
    }

    var mojoInfos: List[MojoClassInfo] = List()


    body.foreach {
      x =>
        x match {
          case c@isGoal() =>
              mojoInfos = parseMojoClass(c) :: mojoInfos
          case c: Tree => Unit
        }
    }

//    println(mojoInfos)
    mojoInfos
  }

  /**Attempts to pull a static value from the given tree.
   *
   * @returns
   * Some ( value ) if a value is found, None otherwise
   */
  def extractStaticValue(tree: Tree) = tree match {
    case Literal(c) => Some(extractConstantValue(c))
    case _ => None
  }

  /**
   * Extracts a constant variable's runtime value
   */
  def extractConstantValue(c: Constant) = {
    //TODO - handle other values
    c.stringValue
  }
}
