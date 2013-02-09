@REM *************************************************************
@REM Copyright 2013 Microsoft Open Technologies, Inc.
@REM 
@REM Licensed under the Apache License, Version 2.0 (the "License");
@REM you may not use this file except in compliance with the License.
@REM You may obtain a copy of the License at
@REM 
@REM http://www.apache.org/licenses/LICENSE-2.0
@REM 
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.
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
