package org.scala_tools.maven.mojo.util

import org.apache.maven.project.MavenProject;
import collection.JavaConversions._
import java.io.File

/**
 * Helper methods to extract info from a maven project
 */
trait MavenProjectTools {

  def getCompileClasspathString(p : MavenProject) = {
    p.getCompileClasspathElements.mkString("",File.pathSeparator,"")
  }
}
