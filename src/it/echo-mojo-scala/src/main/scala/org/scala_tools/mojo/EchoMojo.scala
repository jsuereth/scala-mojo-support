package org.scala_tools.mojo

/*
 * Echo mojo in java for testing the passng of properties from the command line
 */

import org.apache.maven.plugin._
import org.scala_tools.maven.mojo.annotations._
import org.apache.maven.project.MavenProject

@goal("echo")
@requiresProject(false)
class EchoMojo extends AbstractMojo
{
    @parameter
    @expression("${echo.message}")
    // Not yet implemented @default("Hello World...")
    var message: String = "Hello World..."

    @throws(classOf[MojoExecutionException])
    override def execute() 
    {
        getLog().info( message.toString )
    }
}