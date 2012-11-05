@rem check if running in emulator , if yes then ignore session affinity settings silently
if "%EMULATED%"=="true" goto Exit

@rem creating path variable for session affinity folder to refer in multiple places
set  WEBPI_PATH="%ROLEROOT%\approot\webpi"
rmdir /S /Q %WEBPI_PATH%
mkdir %ROLEROOT%\approot\webpi

@rem Downloading webpicmdline
cscript "%ROLEROOT%\approot\util\download.vbs" "http://go.microsoft.com/?linkid=9752821" "%WEBPI_PATH%\webpicmd.zip"


@rem unzipping the contents of zip file
cscript %ROLEROOT%\approot\util\unzip.vbs "%WEBPI_PATH%\webpicmd.zip" "%WEBPI_PATH%\"

@rem Installing ARR
%WEBPI_PATH%\webpicmdline /accepteula /Products:ARR

@rem removing wepi folder
rmdir /S /Q %WEBPI_PATH%

@rem calling the 
start %ROLEROOT%\approot\.sessionaffinity\SessionAffinityAgent.exe %1 %2
%ROLEROOT%\approot\.sessionaffinity\SessionAffinityAgent.exe -blockstartup

:Exit
exit 0