rd "\%ROLENAME%"
mklink /D "\%ROLENAME%" "%ROLEROOT%\approot"
cd /d "\%ROLENAME%"
set SERVER_APPS_LOCATION=\%ROLENAME%

${Variables}

${Components}

${UserStartup}

@ECHO OFF
set ERRLEV=%ERRORLEVEL%
if %ERRLEV%==0 (set _MSG="Startup completed successfully.") else (_MSG="*** Windows Azure startup failed - exiting...")
choice /d y /t 5 /c Y /N /M %_MSG%
exit %ERRLEV%