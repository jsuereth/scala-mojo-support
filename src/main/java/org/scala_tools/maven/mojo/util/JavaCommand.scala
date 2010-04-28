package org.scala_tools.maven.mojo.util

import org.codehaus.plexus.util._
import java.io.File
import java.net.URLClassLoader
import java.net.URL
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoFailureException
import scala.collection.JavaConversions._

/**
 * Class used to start a java process.
 */
class JavaCommand(requester : AbstractMojo, mainClassName : String, classpath : String, var jvmArgs : Seq[String], var args : Seq[String]) {
  var logOnly = false;
  val env = for(key <- System.getenv().keySet) yield {
    key + "=" + System.getenv(key)
  }
  val javaHome = {
    val tmp = System.getProperty("java.home")
    if(tmp == null) {
      System.getProperty("JAVA_HOME")
    } else {
      tmp
    }
  }
  if(javaHome == null) {
    throw new IllegalStateException("Couldn't locate java, try setting JAVA_HOME environment variable")
  }
  val javaExec = javaHome + File.separator + "bin" + File.separator + "java"
  jvmArgs = "-classpath" :: classpath :: jvmArgs.toList 
  
  /** Builds the sequence used to create a process builder */
  private def buildCommand = {
    javaExec :: jvmArgs.toList ::: (mainClassName :: args.toList)
  }
  
  //Methods
  @throws(classOf[Exception])
  def run(displayCmd : Boolean, throwFailure : Boolean) {
    val process = spawn(displayCmd)
    if(logOnly) {
            new StreamLogger(process.getErrorStream(), requester.getLog(), true).start();
            new StreamLogger(process.getInputStream(), requester.getLog(), false).start();
     } else {
            new StreamPiper(process.getInputStream(), System.out).start();
            //new StreamPiper(System.in, p.getOutputStream()).start();
            new ConsolePiper(process).start();
        }
    //TODO - Figure out how to dump output into log or console
    val retVal = process.waitFor()
    if(throwFailure && (retVal != 0)) {
      throw new MojoFailureException("command line returned non-zero value: " +retVal)
    }
    ()
  }
  @throws(classOf[Exception])
  def spawn(displayCmd : Boolean) = {
    val cmd = buildCommand
    //TODO - refactor this logging stuff...
    if(displayCmd) {
      requester.getLog.info("cmd : " + cmd.mkString(" "))
    } else if(requester.getLog.isDebugEnabled) {
      requester.getLog.debug("cmd : " + cmd.mkString(" "))
    }
    val pb = new ProcessBuilder(cmd.toArray : _*)    
    pb.start()
  }
  
}

/**
 * Helper methods for attempting to execute a java process.
 */
object JavaCommand {
  /** Takes a sequence of paths and returns a string usable on the command line*/
  def toMultiPath(paths : Seq[String]) = 
    StringUtils.join(paths.toArray.asInstanceOf[Array[AnyRef]], File.pathSeparator)
  
  /** Finds a set of files matching a given pattern */
  def findFiles(dir : File, pattern : String) = {
    val scanner = new DirectoryScanner
    scanner.setBasedir(dir)
    scanner.setIncludes(Array(pattern))
    scanner.addDefaultExcludes()
    scanner.scan()
    scanner.getIncludedFiles
  }
  
  /** Pulls the classpath from a URL Classloader*/
  def toClasspathString(cl : ClassLoader) = {
    var realCL = if(cl == null) Thread.currentThread.getContextClassLoader else cl
    val back = new StringBuilder
    while(realCL != null) {
      if(realCL.isInstanceOf[URLClassLoader]) {
        val ucl = realCL.asInstanceOf[URLClassLoader]
        for(url <- ucl.getURLs) {
          if(back.length != 0) {
            back.append(File.pathSeparator)
          }
          back.append(url.getFile)
       } 
      }
    }
    back.toString
  }
}
