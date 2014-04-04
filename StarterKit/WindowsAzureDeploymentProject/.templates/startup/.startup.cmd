rd "\%ROLENAME%"

if defined DEPLOYROOT_PATH set DEPLOYROOT=%DEPLOYROOT_PATH%
if defined DEPLOYROOT (
	mklink /J "\%ROLENAME%" "%DEPLOYROOT%"
) else (
	mklink /J "\%ROLENAME%" "%ROLEROOT%\approot"
)

set DEPLOYROOT=\%ROLENAME%
set SERVER_APPS_LOCATION=%DEPLOYROOT%

${Variables}

${Components}

${UserStartup}

@ECHO OFF
set ERRLEV=%ERRORLEVEL%
if %ERRLEV%==0 (set _MSG="Startup completed successfully.") else (set _MSG="*** Azure startup failed [%ERRLEV%]- exiting...")
choice /d y /t 5 /c Y /N /M %_MSG%
exit %ERRLEV%