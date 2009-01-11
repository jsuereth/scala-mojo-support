package org.scala_tools.maven.mojo.extractor

import scala.tools.nsc._
import scala.tools.nsc.reporters._
import org.apache.maven.project.MavenProject;
import org.apache.maven.plugin.descriptor.InvalidPluginDescriptorException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.Parameter;
import org.apache.maven.plugin.descriptor.PluginDescriptor;

import mojo.util.MavenProjectTools

class MojoExtractorCompiler(project : MavenProject) extends MavenProjectTools{
  //Method to extract mojo description from a source file.
  def extract(sourceFiles : String*) : Seq[MojoDescriptor] = {
      //helper method to initialize settings
	  def initialize : (Settings, Reporter) = {
	    val settings = new Settings();
        //TODO - Set settings
        val reporter = new ConsoleReporter(settings);
	    (settings,reporter)
	  }
   
      
	  //helper method to execute presentation compiler
	  def execute(settings : Settings, reporter : Reporter) = {
		  val compiler = new Global(settings, reporter) with MojoAnnotationExtractor {
		    override def onlyPresentation = true
		  }
	      val run = new compiler.Run
	      run.compile(sourceFiles.toList)       
          //Extract mojo description
          def extractMojos(unit : compiler.CompilationUnit) {
            import compiler._
            for(info <- compiler.parseCompilationUnitBody(unit.body)) {
              Console.println(info)
            }
          }
          for(unit <- run.units if !unit.isJava) {
            extractMojos(unit)
          }
          Nil
	  }
      
    val (settings, reporter) = initialize    
    val classpath = getCompileClasspathString(project)
    System.err.println("Classpath = " + classpath)
    settings.classpath.tryToSet("-classpath" :: classpath :: Nil)
    execute(settings, reporter)
  } 
}

import scala.tools.nsc.ast._
import scala.tools.nsc.symtab.{Flags, SymbolTable}
import org.scala_tools.maven.mojo.annotations._
trait MojoAnnotationExtractor extends CompilationUnits {
  self: Global =>
    
  /** Pulls all mojo classes out of the body of source code. */
  def parseCompilationUnitBody(body : Tree) = {
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
    
    /** Pulls out mojo information froma mojo class */
    def parseMojoClass(mojoClass : ClassDef) = {
      
      Console.println(" Found mojo class: " + mojoClass.name.toString)
      //TODO - Pull out *FULL* Name
      val info = new MojoClassInfo(mojoClass.name.toString)
      
      //TODO - Rip annotations from the class and add to MojoClassInfo
      parseAnnotations(mojoClass.mods.annotations).foreach(info.annotation(_))
      //TODO - Rip out annotated Var methods
      for { node <- mojoClass.impl } {
        
      }
      //TODO - Parse Parent classes
      info
    }
    //Find mojo classes
    for { classDef @ ClassDef(mods,name,params, impl) <- body
          annotation <- mods.annotations
          if annotation.tpe.safeToString == classOf[goal].getName
    } yield parseMojoClass(classDef)    
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

//Class information string
class MojoClassInfo(val name : String) {
  private[this] var annotations : List[MojoAnnotationInfo] = Nil
  def annotation(name : MojoAnnotationInfo) {
    annotations = name :: annotations
  }
  
  def getAnnotations() = annotations
  
  override def toString = name + " - " + annotations.mkString("(",",",")")
}

class MojoAnnotationInfo(val name : String, val args : List[String]) {
  override def toString = name + args.mkString("(",",",")")
}