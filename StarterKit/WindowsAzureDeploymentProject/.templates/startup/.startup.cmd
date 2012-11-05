rd "\%ROLENAME%"
mklink /D "\%ROLENAME%" "%ROLEROOT%\approot"
set SERVER_APPS_LOCATION=\%ROLENAME%

${Variables}

netsh advfirewall firewall add rule name="allowjava" dir=in action=allow program="%JAVA_HOME%\bin\java.exe"

${Components}

${UserStartup}

@ECHO OFF
set ERRLEV=%ERRORLEVEL%
if %ERRLEV%==0 (set _MSG="Startup completed successfully.") else (_MSG="*** Windows Azure startup failed - exiting...")
choice /d y /t 5 /c Y /N /M %_MSG%
exit %ERRLEV%