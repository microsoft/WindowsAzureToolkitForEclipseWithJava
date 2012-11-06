@echo off

set __FAIL=FALSE

@rem Cleanup
call :Cleanup

@rem Create
call :EnsureCreate ..\bin\debug\encutil.exe -create -alias myCert -pwd Password1 -pfx cert.pfx -cert cert.cer
call :Cleanup

call :EnsureCreate ..\bin\debug\encutil.exe -create -alias myCert -pwd Password1 -pfx cert.pfx -cert cert.cer -exp 1/1/2020
call :Cleanup

call :EnsureCreate ..\bin\debug\encutil.exe -create -alias myCert -pwd Password1 -pfx cert.pfx -cert cert.cer -exp "1/1/2020 5:20:34pm"
call :Cleanup

call :EnsureCreateNegative ..\bin\debug\encutil.exe -create
call :Cleanup

call :EnsureCreateNegative ..\bin\debug\encutil.exe -create -alias myCert -pwd Password1 -cert cert.cer -exp 1/1/2020
call :Cleanup

call :EnsureCreateNegative ..\bin\debug\encutil.exe -create -alias myCert -foo foo -cert cert.cer -exp 1/1/2020
call :Cleanup

@rem Encrypt
@rem create cert first
..\bin\debug\encutil.exe -create -alias myCert -pwd Password1 -pfx cert.pfx -cert cert.cer
call :Ensure ..\bin\debug\encutil.exe -encrypt -text MyTextToEnctype -cert cert.cer
call :Ensure ..\bin\debug\encutil.exe -encrypt -text "MyTextToEnctypt foo bar" -cert cert.cer
call :EnsureNegative ..\bin\debug\encutil.exe -encrypt
call :EnsureNegative ..\bin\debug\encutil.exe -encrypt -cert cert.cer
call :EnsureNegative ..\bin\debug\encutil.exe -encrypt -cert cert.cer -bla sfod

@rem thumbprint
call :Ensure ..\bin\debug\encutil.exe -thumbprint -cert cert.cer
call :EnsureNegative ..\bin\debug\encutil.exe -thumbprint
call :EnsureNegative ..\bin\debug\encutil.exe -thumbprint fdsdfl

call :Cleanup

if [%__FAIL%]==[TRUE] @echo At least one test failed!&& set errorlevel=100&& goto :EOF
@echo ===========================
@echo     ALL TESTS PASSED
@echo ===========================
goto :EOF

:Cleanup
if exist cert.cer del /q cert.cer
if exist cert.pfx del /q cert.pfx
goto :EOF

:EnsureCreate
@echo.
@echo Running '%*'
set errorlevel=
%* >NUL
if not %errorlevel%==0 @echo FAIL&&set __FAIL=TRUE&& goto :EOF
if not exist cert.cer @echo FAIL&&set __FAIL=TRUE&& goto :EOF
if not exist cert.pfx @echo FAIL&&set __FAIL=TRUE&& goto :EOF
@echo PASS
goto :EOF

:EnsureCreateNegative
@echo.
@echo Running '%*'
set errorlevel=
%* >NUL
@echo errorlevel is %errorlevel%
if not %errorlevel%==100 @echo FAIL&&set __FAIL=TRUE&& goto :EOF
if exist cert.cer @echo FAIL&&set __FAIL=TRUE&& goto :EOF
if exist cert.pfx @echo FAIL&&set __FAIL=TRUE&& goto :EOF
@echo PASS
goto :EOF

:Ensure
@echo.
@echo Running '%*'
set errorlevel=
%* >NUL
if not %errorlevel%==0 @echo FAIL&&set __FAIL=TRUE&& goto :EOF
@echo PASS
goto :EOF

:EnsureNegative
@echo.
@echo Running '%*'
set errorlevel=
%* >NUL
if not %errorlevel%==100 @echo FAIL&&set __FAIL=TRUE&& goto :EOF
@echo PASS
goto :EOF
