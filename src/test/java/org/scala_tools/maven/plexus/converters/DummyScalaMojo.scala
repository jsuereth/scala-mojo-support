package org.scala_tools.maven.plexus.converters

import org.scala_tools.maven.mojo.annotations._

@goal("dummy") @phase("compile") @requiresDependencyResolution("compile")
class DummyScalaMojo {
  @parameter @expression("${project.build.finalName}")
  var dummyVar : String = _
  @parameter
  var otherVar : Int = _
  
  
}
