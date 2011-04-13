package org.scala_tools.mojo

import org.apache.maven.plugin._
import java.io.{File,PrintStream,FileOutputStream}
import org.scala_tools.maven.mojo.annotations._
import org.apache.maven.project.MavenProject

/**
 * Goal which echos "HAI"
 */
@goal("echo")
@phase("process-sources")
@requiresProject
class TestMojo extends AbstractMojo {
  /**
   * Location of the file.
   * @parameter expression="${project.build.directory}"
   * @required
   */
  @parameter
  @expression("${project.build.directory}")
  var outputDirectory : File = _;
  
  @parameter
  @expression("${project}")
  @readOnly
  var project : MavenProject = _;
  
  
  @throws(classOf[MojoExecutionException])
  override def execute() {
    if(!outputDirectory.exists) {
       outputDirectory.mkdirs()
    }
    val file = new File(outputDirectory, "echo.txt")
    val output = new PrintStream(new FileOutputStream(file))
    
    output.println("HAI")
    output.close()
    
  }
}
