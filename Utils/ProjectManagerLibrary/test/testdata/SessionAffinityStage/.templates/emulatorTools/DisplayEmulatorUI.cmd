@ECHO OFF
cd /d "${SDKDir}\bin"
start dfui.exe
echo Windows Azure Compute Emulator is starting...
choice /d y /t 5 /c Y /N
