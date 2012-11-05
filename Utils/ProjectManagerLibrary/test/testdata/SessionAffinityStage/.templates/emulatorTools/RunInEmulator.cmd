@ECHO OFF

@REM Deploy the package to emulator
"${EmulatorDir}\csrun.exe" "${PackageDir}\${PackageFileName}" "${PackageDir}\${ConfigurationFileName}"

@REM Ensure that emulator UI is running
for /f %%G in ('tasklist ^| find /I /C "dfui.exe"') do set _PROCCOUNT=%%G
if NOT %_PROCCOUNT%==0 goto:Bye
cd /d "${EmulatorDir}"
start dfui.exe

:Bye
choice /d y /t 5 /c Y /N