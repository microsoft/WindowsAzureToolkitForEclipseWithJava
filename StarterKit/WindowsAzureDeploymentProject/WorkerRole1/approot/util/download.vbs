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

' To download a file using WinHTTP.WinHttpRequest and save using ADODB.Stream

set args = WScript.Arguments 

'Read download URL from arguments
If args.Length >= 1 Then
    downloadURL = args(0)
Else
    WScript.Echo "Invalid arguments. Usage: download.vbs <downloadURL> [<destinationPath>]"
    WScript.Quit(1)
End If

'Read destination path
If args.Length >= 2 Then
    'If provided as argument then read it
    destPath = args(1)
Else 
    'Otherwise, determine file name from URL
    destPath = StrReverse(Split(StrReverse(downloadURL), "/")(0))
End If

'Create WinHttpRequest to download file
Set httpReq = CreateObject("WinHttp.WinHttpRequest.5.1")
httpReq.Open "GET", downloadURL, False
httpReq.Send

'Create FileSystemObject for deleting the file
Set fso= CreateObject("Scripting.FileSystemObject")
If fso.FileExists(destPath) Then
   fso.DeleteFile destPath
End If
Set fso = Nothing

' Save the file using ADODB stream
Set stream = CreateObject("ADODB.Stream")  
stream.Open  
stream.Type = 1  
stream.Write httpReq.ResponseBody
stream.Position  = 0
stream.SaveToFile destPath
stream.Close

set stream=Nothing
set httpReq=Nothing