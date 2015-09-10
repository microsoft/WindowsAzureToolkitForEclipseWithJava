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

' To unzip a file using shell32.dll APIs

Dim args
set args = WScript.Arguments 

Dim zipFileName, folderName, pathFilter
If (args.Length = 2) Then
    zipFileName = args(0)
    folderName = args(1)
    pathFilter = ""
Else 
	WScript.Echo "Invalid arguments. Usage: unzip.vbs <zipFileName> <folderName> [pathFilter]"
	WScript.Quit(1)
End If

' Create FileSystemObject for file related operations
Dim fso
Set fso= CreateObject("Scripting.FileSystemObject")

' Create output folder if does not exist
If Not fso.FolderExists(folderName) Then 
    fso.CreateFolder(folderName)
End If

' Create shell32 application instance
Dim oShell
set oShell = CreateObject("Shell.Application")

' Get absolute path names for files and folders
Dim zipFileAbsolutePathName, folderAbsolutePathName
zipFileAbsolutePathName = fso.GetAbsolutePathName(zipFileName)
folderAbsolutePathName = fso.GetAbsolutePathName(folderName)

' Get zip file handle
Dim zip
Set zip = oShell.NameSpace(zipFileAbsolutePathName)

' Get output folder handle
Dim ex
Set ex = oShell.NameSpace(folderAbsolutePathName)

' Unzip all files, without showing any UI/popup
ex.CopyHere zip.items, 20

Set ex = Nothing
Set fso = Nothing
Set zip = Nothing
Set oShell = Nothing

WScript.Echo "Successfully extracted the zip file."