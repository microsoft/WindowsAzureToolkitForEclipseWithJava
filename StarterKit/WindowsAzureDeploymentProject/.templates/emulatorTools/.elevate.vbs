'*************************************************************
' Copyright 2013 Microsoft Open Technologies, Inc.
'
' Licensed under the Apache License, Version 2.0 (the "License");
' you may not use this file except in compliance with the License.
' You may obtain a copy of the License at
' 
' http://www.apache.org/licenses/LICENSE-2.0
' 
' Unless required by applicable law or agreed to in writing, software
' distributed under the License is distributed on an "AS IS" BASIS,
' WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
' See the License for the specific language governing permissions and
' limitations under the License.
'*************************************************************

Set objShell = CreateObject("Shell.Application")
Set wshShell = CreateObject("WScript.Shell")

If WScript.Arguments.length >= 1 Then appFilePath = WScript.Arguments(0)
If WScript.Arguments.length >= 2 Then appDir = WScript.Arguments(1)
If WScript.Arguments.length >= 3 Then appArgs = WScript.Arguments(2)

objShell.ShellExecute appFilePath, appArgs, appDir, "runas", 1
