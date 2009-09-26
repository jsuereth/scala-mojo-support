def echoString = "HAI"
def logFile = new File(basedir, "target/echo.txt")
//Look for echo string
def found = false;

System.out.println("Logfile = " + logFile)

logFile.eachLine({ line ->
   System.out.println("Checking line: " + line)
   if(line.startsWith(echoString)) {
   	 found = true;
   }
});

assert found
true