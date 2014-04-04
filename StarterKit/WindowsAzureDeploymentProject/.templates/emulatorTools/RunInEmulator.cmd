@ECHO OFF

:: Require elevation
if "%_ELEVATED%"=="" (goto:Elevate) else (goto:Deploy)
:Elevate
SET _ELEVATED=1
start /min cscript /NoLogo "%~dp0.elevate.vbs" "%~f0"
SET _ELEVATED=
exit

:: Deploy the package to emulator
:Deploy
SET _ELEVATED=
"${StorageEmulatorDir}\WAStorageEmulator.exe" start
"${EmulatorDir}\csrun.exe" "${PackageDir}\${PackageFileName}" "${PackageDir}\${ConfigurationFileName}"

:: Ensure that emulator UI is running
for /f %%G in ('tasklist ^| find /I /C "dfui.exe"') do set _PROCCOUNT=%%G
if NOT %_PROCCOUNT%==0 goto:Bye
cd /d "${EmulatorDir}"
start dfui.exe -singleInstance

:Bye
choice /d y /t 5 /c Y /N
exit
