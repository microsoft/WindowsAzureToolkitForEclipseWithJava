@ECHO OFF

:: Require elevation
if "%_ELEVATED%"=="" (goto:Elevate) else (goto:Shutdown)
:Elevate
SET _ELEVATED=1
start /wait /min cscript /NoLogo "%~dp0.elevate.vbs" "%~f0"
SET _ELEVATED=
exit

:: Deploy the package to emulator
:Shutdown
SET _ELEVATED=
"${EmulatorDir}\csrun.exe" /removeAll /devfabric:clean 2>nul
taskkill /im WAStorageEmulator.exe /f
taskkill /im DFService.exe /f
taskkill /im DFUI.exe /f
