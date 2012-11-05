@REM This is a sample startup step unzipping a file in the approot
@REM It will log the output of the command into log.txt.

util\log.cmd cscript "util\unzip.vbs" "HelloWorld.zip" "%ROLEROOT%\approot"

@REM If you want the output to show in a console window instead, comment out 
@REM the above and un-comment this:
@REM start cscript "util\unzip.vbs" "HelloWorld.zip" "%ROLEROOT%\approot"

@REM If instead of embedding certain large or rarely changing files in the deployment 
@REM package itself, you'd rather download them from some public URL location, then 
@REM use the download.vbs utility script, like this:
@REM util\log.cmd cscript "util\download.vbs" "http://www.interopbridges.com/HelloWorld.zip" "HelloWorld.zip"