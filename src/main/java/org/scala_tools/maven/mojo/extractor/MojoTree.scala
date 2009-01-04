package org.scala_tools.maven.mojo.extractor

sealed trait MojoTree {

}

case class MojoAnnotation(name : String, children : Seq[MojoAttributeParent]) extends MojoTree {
  
}

trait MojoAttributeParent extends MojoTree {
  def isExpression 
}

case class MojoExpression(expr : String) extends MojoAttributeParent {
  def isExpression = true
}
case class MojoAttribute(attr : String) extends MojoAttributeParent {
  def isExpression = false
}


object MojoTree {
  def expr(expr:String) = new MojoExpression(expr)
  def attr(attr:String) = new MojoAttribute(attr)
  def annnotation(name : String, children : MojoAttributeParent*) = new MojoAnnotation(name,children.toSeq)
  def annot(name : String, children : Seq[MojoAttributeParent]) = new MojoAnnotation(name,children)
}