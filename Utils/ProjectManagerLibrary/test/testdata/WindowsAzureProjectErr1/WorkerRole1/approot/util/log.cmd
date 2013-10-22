@REM *************************************************************
@REM Copyright 2013 Microsoft Corp
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
@REM '*************************************************************

set _LOGFILE="%CD%\log.txt"

@rem Log the command line
if not exist %_LOGFILE% @echo %time%:		================= LOG CREATED ==================== >%_LOGFILE%
@echo.>>%_LOGFILE%
@echo %time%:		Running command "%*" >>%_LOGFILE%
@echo %*

@rem Execute the command line
call %*>>%_LOGFILE%
if not %errorlevel%==0 @echo %time%:		Command failed! >>%_LOGFILE%
