package org.scala_tools.maven.plexus.converters

import org.scala_tools.maven.mojo.annotations._

@goal("dummy") @phase("compile") @requiresDependencyResolution("compile")
class DummyScalaMojo {
  @parameter @expression("${project.build.finalName}")
  var dummyVar: String = _
  @parameter
  var otherVar: Int = _

  @parameter
  var aBoolean: Boolean = _
  @parameter
  var aShort: Short = _
  @parameter
  var aLong: Long = _
  @parameter
  var aDouble: Double = _
  @parameter
  var aChar: Char = _
  @parameter
  var aByte: Byte = _

}
