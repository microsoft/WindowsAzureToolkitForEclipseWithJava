@ECHO OFF
powershell -executionpolicy unrestricted -inputformat none -file %0\..\.wash.ps1 %*
exit 0