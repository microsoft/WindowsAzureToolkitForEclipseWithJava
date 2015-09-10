@ECHO OFF
set COMPLUS_ApplicationMigrationRuntimeActivationConfigPath=%~dp0
powershell -executionpolicy unrestricted -inputformat none -file %0\..\.wash.ps1 %*
exit 0