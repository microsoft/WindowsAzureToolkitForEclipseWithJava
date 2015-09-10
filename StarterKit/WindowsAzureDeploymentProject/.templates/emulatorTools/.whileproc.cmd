@REM *************************************************************
@REM Copyright (c) Microsoft Corporation
@REM 
@REM All rights reserved. 
@REM 
@REM MIT License
@REM 
@REM Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files 
@REM (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, 
@REM publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
@REM subject to the following conditions:
@REM 
@REM The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
@REM 
@REM THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
@REM MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
@REM ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH 
@REM THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
@REM *************************************************************

@REM Keeps running while at least one instance of a given process is running.
@REM If no process name is provided, it will never exit, serving as a permanent heartbeat.
@REM If a specified process has not yet been started when this script is called, it will wait for it

@ECHO OFF

set _SLEEPLENGTH=15000

@REM Create a temporary sleep script in VBScript
echo WScript.sleep(%_SLEEPLENGTH%) > %Temp%\_sleep.vbs

@REM Check if process is already running
set _PROCNAME=%1
set _PROCSTARTED=0
set _PROCCOUNT=0

if "%_PROCNAME%"=="" goto:Sleep
for /f %%G in ('tasklist /NH ^| find /I /C "%_PROCNAME%"') do set _PROCCOUNT=%%G
if %_PROCCOUNT%==0 (set _PROCSTARTED=0) else (set _PROCSTARTED=1)

@REM Loop while process is running or while it has not yet been started
:Loop
if "%_PROCNAME%"=="" goto:Sleep
for /f %%G in ('tasklist /NH ^| find /I /C "%_PROCNAME%"') do set _PROCCOUNT=%%G
if %_PROCCOUNT%==0 (goto:NotFound) else (goto:Found)

:Found
set _PROCSTARTED=1
goto:Sleep

:NotFound
if %_PROCSTARTED%==1 (goto:Failure) else (goto:Sleep)

:Sleep
if "%_PROCNAME%"=="" (echo Running...) else (if %_PROCSTARTED%==1 (echo %_PROCNAME% is running...) else (if "%2"=="/nowait" (goto:Failure) else ( echo %_PROCNAME% has not started - waiting...)))
cscript /Nologo %Temp%\_sleep.vbs
goto:Loop

:Failure
echo %_PROCNAME% stopped running - exiting...
del %Temp%\_sleep.vbs
exit 1
