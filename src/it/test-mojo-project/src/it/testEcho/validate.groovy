def echoString = "HAI"
def fileNames = ["echo.txt", "child.txt"]

fileNames.each({ fileName ->
    def found = false
    def logFile = new File(basedir, "target/${fileName}")
    logFile.eachLine({ line ->
        if(line.startsWith(echoString)) {
            found = true
        }
    });
    assert found
})

true
