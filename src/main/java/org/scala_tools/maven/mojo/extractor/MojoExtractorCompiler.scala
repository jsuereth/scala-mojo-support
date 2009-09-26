package org.scala_tools.maven.mojo.extractor

import scala.tools.nsc._
import scala.tools.nsc.reporters._
import org.apache.maven.project.MavenProject;
import org.apache.maven.plugin.descriptor.InvalidPluginDescriptorException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.Parameter;
import org.apache.maven.plugin.descriptor.PluginDescriptor;

import mojo.util.MavenProjectTools

class MojoExtractorCompiler(project : MavenProject) extends MavenProjectTools with MojoExtractionInfo {
  //Method to extract mojo description from a source file.
  def extract(sourceFiles : String*) : Seq[MojoDescriptor] = {
      //helper method to initialize settings
	  def initialize : (Settings, Reporter) = {
	    val settings = new Settings();
            //TODO - Set settings
            settings.stop.tryToSet(List("typer"))
            val reporter = new ConsoleReporter(settings);
	      (settings,reporter)
	    }
   
      
	  //helper method to execute presentation compiler
	  def execute(settings : Settings, reporter : Reporter) = {
		  val compiler = new Global(settings, reporter) with MojoAnnotationExtractor {
		    //override def onlyPresentation = true
		  }
	      val run = new compiler.Run
	      run.compile(sourceFiles.toList)       
          //Extract mojo description
          def extractMojos(unit : compiler.CompilationUnit) = {
            import compiler._
            for(info <- compiler.parseCompilationUnitBody(unit.body)) yield {
              Console.println(info)
              extractMojoDescriptor(info)
            }
          }
          for(unit <- run.units if !unit.isJava) yield {
            extractMojos(unit)
          }
	  }
      
    val (settings, reporter) = initialize    
    val classpath = getCompileClasspathString(project)
    System.err.println("Classpath = " + classpath)
    settings.classpath.tryToSet("-classpath" :: classpath :: Nil)    
    execute(settings, reporter).toList.flatMap(x=>x)
  } 
}


import scala.tools.nsc.ast._
import scala.tools.nsc.symtab.{Flags, SymbolTable}
import org.scala_tools.maven.mojo.annotations._
trait MojoAnnotationExtractor extends CompilationUnits {
  self: Global =>
    
  /** Pulls all mojo classes out of the body of source code. */
  def parseCompilationUnitBody(body : Tree) = {
    /** Pulls the name of the parent class */
    def pullParentClassName(parent : Tree) = {
      parent match {
        case Select(qualifier,selector) =>
          parent.toString
        case t @ TypeTree() =>  t.tpe.safeToString
        case _ => //TODO - error out? 
           ""
      }
    }
    /** Parses a list of annotations into a list of MojoAnnotationInfo classes */
    def parseAnnotations(annotations : List[Annotation]) = {
      for {
        annotation <- annotations 
        Annotation(constr, _) <- annotation
      } yield {
        // TODO - In the future the second arg to Annotation may be needed... (as it's what's inside the
        //anonymous instantiation of an annotatioin, i.e. @xyz {}
        //TODO - Do we need to match on constructor?
        val argVals = for { 
          Apply(_, args) <- constr
          arg <- args
          argVal <- extractStaticValue(arg)
        } yield argVal
        new MojoAnnotationInfo(annotation.tpe.safeToString, argVals.toList)
      }
    }
    /** Pulls out information about all injectable variables based on mojo annotaitons. */
    def parseMojoInjectedVars(classImpl : Tree) = {
      for { node @ DefDef(mods,name,tparams,vparams,tpt,_) <- classImpl
            if name.toString.endsWith("_$eq") //TODO - only var like setters? for now this is fine...
            annotation <- mods.annotations
            if annotation.tpe.safeToString == classOf[parameter].getName
            argument @ ValDef(_,_,tpt,_) :: Nil <- vparams //setter should only have ONE argument!
      } yield {
        val argName = name.toString.slice(0, name.length - "_$eq".length)
        //DO a real extraction of the type!
        val argType = tpt.toString
        val argAnnotations = parseAnnotations(mods.annotations)
        new MojoInjectedVarInfo(argName,argType,argAnnotations)
      }
    }    
    /** Pulls out mojo information froma mojo class */
    def parseMojoClass(pkgName : String, mojoClass : ClassDef) : MojoClassInfo = {
      //Pull out *FULL* Name 
      val mojoClassname = pkgName + mojoClass.name.toString
      //Rip annotations from the class and add to MojoClassInfo
      val mojoAnnotations = parseAnnotations(mojoClass.mods.annotations)
      //Rip out annotated Var methods
      val mojoArgs = parseMojoInjectedVars(mojoClass.impl)
      //Parse Parent classes injectable arguments
      //TODO - Make sure this works!  We're not sure if a parentclass is a ClassDef (most likely it's just a typeDef...)
      for(parent <- mojoClass.impl.parents) {        
        val parentClassName = pullParentClassName(parent)
        Console.println(mojoClassname + " has parent " + parentClassName)
        ()
      }  
        
      val parentArgs = for { 
        parentClass @ ClassDef(_,_,_,_) <- mojoClass.impl.parents        
      } yield {
        parseMojoInjectedVars(parentClass.impl)
      }
      //Combine all mojo injectable variables...
      val finalArgs = parentArgs.foldLeft(mojoArgs)(_ ++ _)
      new MojoClassInfo(mojoClassname, mojoAnnotations, finalArgs)
    }
    
    var mojoInfos : List[MojoClassInfo] = List()
    /** Parses down into packages... */
    def parsePackage(pkgName : String, pkgDef : PackageDef) {
      pkgDef.stats foreach { _ match {
          case subPkgDef : PackageDef =>
            parsePackage(pkgName + pkgDef.name + ".", subPkgDef)
          //Find mojo classes
          case classDef : ClassDef => for { 
            annotation <- classDef.mods.annotations
            if annotation.tpe.safeToString == classOf[goal].getName
          } {
            mojoInfos = parseMojoClass(pkgName + pkgDef.name + ".", classDef) :: mojoInfos
          }
          case _ => //Ignore
        }
      }
    }
    //TODO - What do we do if top-level tree item is *NOT* a package?
    body match {
      case pkg : PackageDef => parsePackage("", pkg)
      case classDef : ClassDef => parseMojoClass("", classDef)
      case _ => Console.println("Error! Unexpected source file format for Scala Mojo Extractor");
    }
    
    mojoInfos
  }
  
  /** Attempts to pull a static value from the given tree.
   * 
   * @returns
   *         Some(value) if a value is found, None otherwise
   */
  def extractStaticValue(tree : Tree) = tree match {
    case Literal(c) => Some(extractConstantValue(c)) 
    case _ => None
  }
  /**
   * Extracts a constant variable's runtime value
   */
  def extractConstantValue(c : Constant) = {
    //TODO - handle other values
    c.stringValue
  }
}
