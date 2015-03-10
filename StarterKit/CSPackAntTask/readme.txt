Because Azure SDK cannot be installed on Linux machines, following files from SDK need to be placed under 'sdkKit' folder:

Microsoft.WindowsAzure.StorageClient.dll

base\Cloud.uar.csman

base\base\Microsoft.WindowsAzure.ServiceRuntime.dll

base\base\x64\*
base\base\x64\WaHostBootstrapper.exe.config - note: somehow it is not present in my sdk installation, had to copy from x86 folder

base\base\x86\*

base\diagnostics\x64\monitor\*

base\storage\cloud\x64\mswacdmi.dll


plugins\RemoteAccess\*

plugins\RemoteForwarder\*

plugins\Caching\*

plugins\WebDeploy\*
