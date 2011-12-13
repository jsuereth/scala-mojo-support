package org.scala_tools.maven.mojo.util

import java.io._
object BufferedReaderHelper {
  def lines(reader: BufferedReader): Traversable[String] =
    new Traversable[String] {
      override def foreach[U](f: String => U): Unit = {
        def read(): Unit = reader.readLine match {
          case null => ()
          case line => f(line); read()
        }
        read()
      }      
      override def toString = "BufferedReaderLines("+reader+")"
    }
}