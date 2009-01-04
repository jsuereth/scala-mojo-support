package org.scala_tools.maven.mojo.util

import java.io._
import org.apache.maven.plugin.logging.Log
import org.codehaus.plexus.util.IOUtil
import org.codehaus.plexus.util.StringUtils

import Resource._
import RichBufferedReader._


class StreamLogger(in : InputStream, log : Log, isErr : Boolean) extends Thread {
 
  override def run() {
    def logLine(line : String) {
      if(isErr) {
             if(!StreamLogger.emacsMode) {
               log.warn(line)
             } else {
               log.warn(StreamLogger.LS + line)
             }
           } else {
             log.info(line)
           }
    }
    
    new BufferedReader(new InputStreamReader(in)) use {
      reader =>
         for(line <- reader) {
           logLine(line)
         }
      
    }
  }
}

object StreamLogger {
  val LS = System.getProperty("line.separator")
  val emacsMode = StringUtils.isNotEmpty(System.getProperty("emacsMode"))
}



