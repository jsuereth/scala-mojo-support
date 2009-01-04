package org.scala_tools.maven.mojo.extractor

import scala.tools.nsc._
import scala.tools.nsc.reporters._

class MojoExtractorCompiler {
  //Method to extract mojo description from a source file.
  def extract(sourceFiles : String*) = {
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
            
            for(info <- compiler.parseCompilationUnitBody(unit.body)) {
              Console.println(info)
            }
          }
          for(unit <- run.units if !unit.isJava) {
            extractMojos(unit)
          }
	  }
      
    val (settings, reporter) = initialize
    settings.classpath.tryToSet("-classpath" :: "" :: Nil)
    execute(settings, reporter)
  } 
}

import scala.tools.nsc.ast._
import scala.tools.nsc.symtab.{Flags, SymbolTable}
trait MojoAnnotationExtractor extends CompilationUnits {
  self: Global =>
    
  
  def parseCompilationUnitBody(body : Tree) = {
    
    
    def parseClass(tree : ClassDef, pkgName : String) = {
      val info = new MojoClassInfo(tree.name.toString)
      
      for(annotation <- tree.mods.annotations) {
        info.annotation(annotation.symbol.toString)
      }
      
      info
    }
    import scala.collection.mutable
    def parsePackages(body : Tree, pkgName : String, infos : mutable.ListBuffer[MojoClassInfo]) : Unit = {
      body match {
         case tree : PackageDef =>
           val pkgName = tree.symbol.name.toString
           for(stat <- tree.stats) {
             parsePackages(stat, pkgName, infos)
           }
         case tree : ClassDef =>
           infos.append(parseClass(tree, pkgName))
         case _ => //IGnore
      }
    }
    
    //Pull out mojo class information
    val classInfos = new mutable.ListBuffer[MojoClassInfo]    
    for(node <- body) {
      parsePackages(node, "", classInfos)
    }
    classInfos
  }
  
}

//Class information string
class MojoClassInfo(val name : String) {
  private[this] var annotations : List[String] = Nil
  def annotation(name : String) {
    annotations = name :: annotations
  }
  
  def getAnnotations() = annotations
  
  override def toString = name + " - " + annotations.mkString("(",",",")")
}
