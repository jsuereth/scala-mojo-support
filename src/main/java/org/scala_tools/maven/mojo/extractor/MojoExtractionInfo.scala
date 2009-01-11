package org.scala_tools.maven.mojo.extractor

//Class information string
class MojoClassInfo(val name : String, val annotations : List[MojoAnnotationInfo], val parameters : List[MojoInjectedVarInfo]) {
  override def toString = name + " - " + annotations.mkString("(",",",")") + "\nwithArgs " + parameters.mkString("\n","\n","")
}

class MojoInjectedVarInfo(val name : String, val typeClass : String, val annotations : List[MojoAnnotationInfo]) {
  override def toString = name + " : " + typeClass + " - " + annotations.mkString("(",",",")")
}

class MojoAnnotationInfo(val name : String, val args : List[String]) {
  override def toString = name + args.mkString("(",",",")")
}