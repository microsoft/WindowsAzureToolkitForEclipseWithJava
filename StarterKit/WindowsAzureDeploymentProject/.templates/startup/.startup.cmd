rd "\%ROLENAME%"

start "Azure Environment Monitor" "util\wash.cmd" environment watch change.cmd

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
if %ERRLEV%==0 (echo Startup completed successfully.) else (echo *** Azure startup failed [%ERRLEV%]- exiting...)
timeout 5
exit %ERRLEV%