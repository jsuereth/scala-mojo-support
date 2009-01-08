package org.scala_tools.maven.mojo.util

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;

import jline.ClassNameCompletor;
import jline.ConsoleReader;
import jline.FileNameCompletor;
import jline.SimpleCompletor;

import org.codehaus.plexus.util.IOUtil;
/**
 * Use Jline to manage input stream, to provide arrow support (history and line navigation).
 *
 * @author David Bernard (dwayne)
 * @created 2007
 */
class ConsolePiper(p: Process) extends Thread {
  val console = new ConsoleReader()
  //TODO - Pull history into helper class?
  val histoFile = new File(System.getProperty("user.home"), ".m2/scala-console.histo");
  histoFile.getParentFile().mkdirs();
  console.getHistory().setHistoryFile(histoFile);
  console.addCompletor(new FileNameCompletor());
  console.addCompletor(new ClassNameCompletor());
  console.addCompletor(new SimpleCompletor(Array[String](
                "abstract", "case", "catch", "class", "def",
                "do", "else", "extends", "false", "final",
                "finally", "for", "if", "implicit", "import", "lazy",
                "match", "new", "null", "object", "override",
                "package", "private", "protected", "requires", "return",
                "sealed", "super", "this", "throw", "trait",
                "try", "true", "type", "val", "var",
                "while", "with", "yield"))
        );
  val processInput = new PrintWriter(p.getOutputStream());
  
  
  override def run() {
        try {
            while (true) {
//                // wait for prompt from process
//                do {
//                    bytes_read = processOutput_.read(buffer);
//                    if (bytes_read != -1) {
//                        System.out.write(buffer, 0, bytes_read);
//                        System.out.flush();
//                    }
//                } while (processOutput_.available() > 0);
//                if ((bytes_read > 1) && new String(buffer, 0, bytes_read).startsWith("scala>") && (firstInput_ != null)) {
//                    console_.putString(firstInput_);
//                    console_.printNewline();
//                    firstInput_ = null;
//                }
                processInput.println(console.readLine());
                processInput.flush();
                Thread.`yield`
                Thread.sleep(500l);
            }
        } catch {
          case exc : InterruptedException =>
            System.err.print("stop by interrupt");
            return;
          case exc : Exception =>
            System.err.print("!!!! exc !!!");
            exc.printStackTrace();
            throw new RuntimeException("wrap: " + exc.getMessage(), exc);
        } finally {
            IOUtil.close(processInput);
        }
    }

}
