@rem check if running in emulator , if yes then ignore session affinity settings silently
if "%EMULATED%"=="true" goto Exit

@rem below command is to handle url rewrite module installation error. To be removed later

msiexec /x {C498EF0D-85FF-46C5-9DDA-7543C7818754} /q /passive

@rem Installing ARR
md "%~dp0appdata"
reg add "hku\.default\software\microsoft\windows\currentversion\explorer\user shell folders" /v "Local AppData" /t REG_EXPAND_SZ /d "%~dp0appdata" /f
%ROLEROOT%\plugins\WebDeploy\WebpiCmd.exe /Install /accepteula /Products:ARR
reg add "hku\.default\software\microsoft\windows\currentversion\explorer\user shell folders" /v "Local AppData" /t REG_EXPAND_SZ /d %%USERPROFILE%%\AppData\Local /f

@rem Settings W3SVC startup mode to auto
SC Config W3SVC Start= Auto

@rem calling session affinity agent 
start %ROLEROOT%\approot\.arrconfig\ARRAgent.exe %*
%ROLEROOT%\approot\.arrconfig\ARRAgent.exe -blockstartup

:Exit
exit 0
