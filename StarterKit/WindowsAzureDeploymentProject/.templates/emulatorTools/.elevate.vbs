'*************************************************************
' Copyright (c) Microsoft Corporation
'
' All rights reserved. 
'
' MIT License
'
' Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files 
' (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, 
' publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
' subject to the following conditions:
'
' The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
'
' THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
' MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
' ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH 
' THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
'*************************************************************

Set objShell = CreateObject("Shell.Application")
Set wshShell = CreateObject("WScript.Shell")

If WScript.Arguments.length >= 1 Then appFilePath = WScript.Arguments(0)
If WScript.Arguments.length >= 2 Then appDir = WScript.Arguments(1)
If WScript.Arguments.length >= 3 Then appArgs = WScript.Arguments(2)

objShell.ShellExecute appFilePath, appArgs, appDir, "runas", 1
