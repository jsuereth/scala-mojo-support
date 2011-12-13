package org.scala_tools.maven.mojo.util

import resource._
import java.io._

/** Pipes form one string to another */
class StreamPiper(in: InputStream, out: OutputStream) extends Thread {
  override def run(): Unit = 
    for(input <- managed(in); ouput <- managed(out)) {
     val buffer = new Array[Byte](64*1024)
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
