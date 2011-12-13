package org.scala_tools.maven.mojo.util

import java.io._
import org.apache.maven.plugin.logging.Log
import org.codehaus.plexus.util.IOUtil
import org.codehaus.plexus.util.StringUtils

import resource._

class StreamLogger(in : InputStream, log : Log, isErr : Boolean) extends Thread { 
  override def run() {
    val reader = managed(new BufferedReader(new InputStreamReader(in)))
    val lines = reader map BufferedReaderHelper.lines toTraversable    
    def logLine(line: String): Unit = 
      (isErr, StreamLogger.emacsMode) match {    
        case (true, true)  => log.warn(StreamLogger.LS + line)
        case (true, false) => log.warn(line)
        case _             => log.info(line)
      }
    lines foreach logLine
  }
}

object StreamLogger {
  val LS = System.getProperty("line.separator")
  val emacsMode = StringUtils.isNotEmpty(System.getProperty("emacsMode"))
}



