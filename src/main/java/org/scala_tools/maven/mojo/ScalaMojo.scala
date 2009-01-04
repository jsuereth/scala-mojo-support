package org.scala_tools.maven.mojo

import org.apache.maven.project.MavenProject
import org.apache.maven.plugin.AbstractMojo
import annotations._

@requiresProject(true)
trait ScalaProjectMojo  extends AbstractMojo {
  //TODO - Make sure this works with injection and initial usage of the project val
  private[this] var mavenProject_ : MavenProject = _
  
  @parameter @expression("${project}")
  def mavenProject_=(project : MavenProject) {
    mavenProject_ = project
  }
  lazy val project = mavenProject_
}


@goal("test")
class TestMojo(@parameter @alias("args") args : Array[String]) extends ScalaProjectMojo {
  
  def execute() {
    getLog debug """Testing plugin
with multiline
log statement
"""
  }
}