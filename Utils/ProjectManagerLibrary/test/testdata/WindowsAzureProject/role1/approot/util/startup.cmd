@REM Set approot as the current directory for convenience
cd /d "%ROLEROOT%\approot"

@REM This is a sample startup step unzipping a file in the approot
cscript "util\unzip.vbs" "HelloWorld.zip" "%ROLEROOT%\approot"

@REM If instead of embedding certain large or rarely changing files in the deployment 
@REM package itself, you'd rather download them from a public URL location such as 
@REM Windows Azure blob storage, then use the download.vbs utility script, like this:
@REM cscript "util\download.vbs" "http://www.interopbridges.com/HelloWorld.zip" "HelloWorld.zip"

@REM Prevent this script from exiting, otherwise the role instance will be recycled
echo Starting a sample heart beat script for role instance %RdRoleId%
cscript "util\heartbeat.vbs" 