def echoString = "OUT: [ERROR] HAI"
def outputDirectoryIsBadString = "OUT: [ERROR] outputDirectory = null"
def logFile = new File(basedir, "build.log")
//Look for echo string
def found = false;
def outputDirectoryIsBad = false;
logFile.eachLine({ line ->
   if(line.startsWith(echoString)) {
   	 found = true;
   }
   if(line.startsWith(outputDirectoryIsBadString)) {
   	outputDirectoryIsBad = true;
   }
});

assert found
assert !outputDirectoryIsBad