package org.scala_tools.mojo

import org.apache.maven.plugin._
import java.io.File
import org.scala_tools.maven.mojo.annotations._
/**
 * Goal which echos "HAI"
 *
 * @goal echo
 * 
 * @phase process-sources
 */
@goal("echo")
@phase("process-sources")
class TestMojo extends AbstractMojo {
  /**
   * Location of the file.
   * @parameter expression="${project.build.directory}"
   * @required
   */
  @parameter
  @expression("${project.build.directory}")
  var outputDirectory : File = _;
  
  
  
  @throws(classOf[MojoExecutionException])
  override def execute() {
    getLog.error("HAI")
    getLog.error("outputDirectory = " + outputDirectory);//.getAbsolutePath);
  }
}
