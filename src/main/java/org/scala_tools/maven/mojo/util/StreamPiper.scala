package org.scala_tools.maven.mojo.util

import Resource._

import java.io._
/** Pipes form one string to another */
class StreamPiper(in : InputStream, out : OutputStream) extends Thread {
  override def run() {
    in use {
      input =>
        out use {
          output =>
             val buffer = new Array[Byte](512)
             while(true) {
               val numread = in.read(buffer)
               if(numread != -1) {
                 out.write(buffer, 0, numread)
                 out.flush()
               }
               
               if(numread < 1) {
                 Thread.`yield`
                 Thread.sleep(500l)
               }
               
             }
             
        }
    }
  }

}
