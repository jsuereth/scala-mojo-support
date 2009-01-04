package org.scala_tools.maven.mojo.extractor

import scala.util.parsing.combinator._
import scala.util.parsing.combinator.lexical._
import scala.util.parsing.syntax._
import scala.util.parsing.input.CharArrayReader.EofCh

/** This class defines parse tokens */
trait MojoTokens extends Tokens {
  case class Annotation(chars : String) extends Token {
    override def toString = "@" + chars
  }
  
  case class Expression(chars : String) extends Token {
    override def toString = "expression=" + chars
  }
  
  case class Attribute(chars : String) extends Token {
    override def toString = ":" + chars + ":"
  }
}

/** Pulls information from javadoc-like annotations*/
class JavaDocAnnotationLexer extends Lexical with MojoTokens with Scanners {
 override def whitespace: Parser[Any] = rep(whitespaceChar)
 override def token: Parser[Token] = 
   ( '@' ~ rep(not(whitespaceChar)) ^^ { case '@' ~ rest => processAnnotation(rest mkString "") }
     | rep(not(whitespaceChar)) ^^ { case attrOrExpr => processExprOrAttr(attrOrExpr mkString "")}
     | EofCh ^^^ EOF
   )
 
 
 def processAnnotation(name : String) = Annotation(name)
 /** Distinguishes between expresson=x and x attribtues */
 def processExprOrAttr(expr : String) : Token = {
   val twoSides = expr.split("=")
   if(twoSides.length == 2 && "expression".equals(twoSides(0))) {
     Expression(twoSides(1))
   } else {
     Attribute(expr)
   }
 }
 
 

}

/** This class defines how to parse tokens into an MojoDescription Tree */
import scala.util.parsing.combinator.syntactical._
class Parser extends TokenParsers {  
  type Tokens = JavaDocAnnotationLexer
  val lexical = new Tokens
  
  def root = annotations 
  def annotations = annotation*  
  def annotation = annotationName ~ attributes ^^ { x =>   MojoTree.annot(x._1, x._2) }
  def annotationName = accept("annotation", { case lexical.Annotation(n) => n  })
  def attributes = (expression | value)*
  def expression = accept("expression", { case lexical.Expression(n) => MojoTree.expr(n) })
  def value = accept("attribute", { case lexical.Attribute(n) => MojoTree.attr(n)} )
}

/** Driver class that performs parsing/translation */
object MojoAnnotationParser extends Parser {
  /** Parses a given input into a MojoTree*/
  def parse(input : String) = {    
    phrase(root)(new lexical.Scanner(input.replaceAll("/*","").replaceAll("*/","").replaceAll("*",""))) match {
	      case Success(result, _) => Some(result)
	      case _ => None
	}
  }
}