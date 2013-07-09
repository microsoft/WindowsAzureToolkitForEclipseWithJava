"${EmulatorDir}\csrun.exe" /devfabric:shutdown /devfabric:clean
"${EmulatorDir}\csrun.exe" /devstore:shutdown
@ECHO OFF
SET DEPLOYROOT=
REG DELETE HKCU\Environment /V DEPLOYROOT /F 2>nul
exit 0