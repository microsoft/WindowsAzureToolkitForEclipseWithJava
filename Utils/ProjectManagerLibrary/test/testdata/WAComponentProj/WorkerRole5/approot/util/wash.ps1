# Windows Azure Shell (wash), v0.1
# Copyright 2012 by Microsoft Open Technologies, Inc.
# Author: Martin Sawicki
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

$global:sub = @{Name = $null; Id = $null; Certificate = $null; ManagementURL = $null}
$global:store = @{Name = $null; Key = $null; SelectedContainer = $null; Connection = $null; SelectedBlob = $null}
$global:containerName = $null

function get_blob_client($connection) {
    Add-Type -Path ($myDir + '\Microsoft.WindowsAzure.StorageClient.dll')
    $storageAccount = [Microsoft.WindowsAzure.CloudStorageAccount]::Parse($connection)
    New-Object Microsoft.WindowsAzure.StorageClient.CloudBlobClient($storageAccount.BlobEndpoint, $storageAccount.Credentials)
}

function list_blob_containers($connection) {
    $blobClient = get_blob_client $connection
    $blobClient.ListContainers()
}

function list_blob_containerNames($connection) {
    $blobContainers = list_blob_containers($connection)
    foreach($blobContainer in $blobContainers) {
        if([String]::IsNullOrEmpty($blobContainer.Name) -eq $False) {
            "# " + $blobContainer.Name
        }
    }
}

function get_container($name, $connection) {
    $blobClient = get_blob_client $connection
    $blobClient.GetContainerReference($name)
}

function create_container($name, $connection) {
    $container = get_container $name $connection
    $container.CreateIfNotExist()
}

function delete_container($name, $connection) {
    $container = get_container $name $connection
    $container.Delete()
}

function list_blobs($blobContainer) {
    return $blobContainer.ListBlobs()
}

function list_blobNames($blobContainer) {
    $blobs = list_blobs $blobContainer
    foreach($blob in $blobs) {
        if([String]::IsNullOrEmpty($blob.Name) -eq $False) {
            "# " + $blob.Name
        }
    }
}

function get_blob($name, $blobContainer) {
    $blobContainer.GetBlobReference($name)
}

function download_blob($blob, $filepath) {
    if(Test-Path $filepath) {
        Remove-Item $filepath
    }
    try {
        $blob.DownloadToFile($filepath)
        $TRUE
    } catch {
        $FALSE
    }
}

function upload_blob($blob, $filepath) {
    if(Test-Path $filepath) {
        try {
            $blob.UploadFile($filepath)
            $TRUE
        } catch {
            $FALSE
        }
    }
}

function read_cert($certBase64) {
    $certBytes = [System.Convert]::FromBase64String($certBase64)
    $cert = New-Object System.Security.Cryptography.X509Certificates.X509Certificate2
    $cert.Import($certBytes)
    return $cert
}

function read_cert_stdin() {
    $certBase64 = [Console]::In.ReadLine()
    $cert = read_cert $certBase64
    return $cert
}

function create_web_request($cloudURL, $subscriptionID, $operation, $cert, $method, [xml]$body) {
    $uri = $cloudURL + "/" + $subscriptionID 
    if($operation -ne $null) {
        $uri += "/" + $operation
    }
    
    $request = [System.Net.WebRequest]::Create($uri)
    $request.Headers.Add("x-ms-version", "2012-03-01")
    $request.Method = $method
    $request.ContentType = "application/xml"
    [void]$request.ClientCertificates.Add($cert)
    if($body -ne $null) {
        $stream = $request.GetRequestStream()
        $body.Save($stream)
        $stream.Close()
    }
    return $request
}

function get_response_xml($request) {
    try {
        $response = $request.GetResponse()
    } catch [System.Exception] {
        show_error $_.Exception.Message
    }

    if($response -eq $null) {
        return $null
    }
    
    if($null -eq ($stream = $response.GetResponseStream())) {
        return $null
    } elseif($null -eq ($reader = New-Object System.IO.StreamReader($stream))) {
        return $null
    } else {
        $responseContent = $reader.ReadToEnd()
        $response.Close()
        $stream.Close()
        $reader.Close()
        if($responseContent -ne "") {
            return [xml]$responseContent
        } else {
            return $null
        }
    }
}

function call_REST($cloudURL, $subscriptionID, $operation, $method, $cert, [xml]$body) {
    $request = create_web_request $cloudURL $subscriptionID $operation $cert $method $body
    get_response_xml $request
}

function list_servicesXML($cloudURL, $subscriptionID, $cert) {
    call_REST $cloudURL $subscriptionID "services/hostedservices" "GET" $cert
}

function get_subscriptionXML($cloudURL, $subscriptionID, $cert) {
    call_REST $cloudURL $subscriptionID $null "GET" $cert
}

function delete_storage($name, $cloudURL, $subscriptionID, $cert) {
    $operation = "services/storageServices/" + $name
    call_REST $cloudURL $subscriptionID $operation "DELETE" $cert
}

function toBase64([string]$text) {
    $bytes  = [System.Text.Encoding]::UTF8.GetBytes($text);
    [System.Convert]::ToBase64String($bytes);
}

function create_storage([string]$name, [string]$location, $cloudURL, $subscriptionID, $cert) {
    Write-Host "NAME:" $name
    Write-Host "LOC:" $location
    $operation = "services/storageServices"
    $body = [xml]"<CreateStorageServiceInput xmlns=""http://schemas.microsoft.com/windowsazure"">
                    <ServiceName/>
                    <Label/>
                    <Location/>
                </CreateStorageServiceInput>"
    $body.CreateStorageServiceInput.ServiceName = $name
    $body.CreateStorageServiceInput.Location = $location
    $body.CreateStorageServiceInput.Label = toBase64 $name 
    
    $body.Save([Console]::Out)
    Write-Host
    call_REST $cloudURL $subscriptionID $operation "POST" $cert $body
}

function get_storageKeysXML($name, $cloudURL, $subscriptionID, $cert) {
    $operation = "services/storageServices/" + $name + "/keys"
    call_REST $cloudURL $subscriptionID $operation "GET" $cert    
}

function get_storageKey($cloudURL, $subscriptionID, $cert, $storageName) {
    $storeKeysXML = get_storageKeysXML $storageName  $cloudURL $subscriptionID $cert
    $storeKeysXML.StorageService.StorageServiceKeys.Primary            
}

function get_storageXML($cloudURL, $subscriptionID, $cert) {
    call_REST $cloudURL $subscriptionID "services/storageservices" "GET" $cert
}

function list_storageNames($cloudURL, $subID, $cert) {
    $storageXML = get_storageXML $cloudURL $subID $cert
    if($null -eq $storageXML) {
        return $null
    }
    $storageXML.StorageServices.StorageService | % {Write-Host "#" $_.ServiceName }
}

function select_store($name, $key) {
    $global:store.Name = $name
    $global:store.Key = $key
    $global:store.SelectedContainer = $null
    $global:store.SelectedBlob = $null
    if(($name -eq $null) -or ($key -eq $null)) {
        $global:store.Connection = $null
    } else {
        $global:store.Connection = connection_string $name $key
    }
}

function list_locationsXML($cloudURL, $subscriptionID, $cert) {
    call_REST $cloudURL $subscriptionID "locations" "GET" $cert
}

function list_locationsStore($cloudURL, $subscriptionID, $cert) {
    $locationsXML = list_locationsXML $cloudURL $subscriptionID $cert
    if($null -eq $locationsXML) {
        return $null
    }
    $locationsXML.Locations.Location | %{Write-Host $_.Name }
}

function list_serviceNames($cloudURL, $subID, $cert) {
    $servicesXML = list_servicesXML $cloudURL $subID $cert
    if($null -eq $servicesXML) {
        return $null
    }
    $servicesXML.HostedServices.HostedService | % {Write-Host $_.ServiceName }
}

function connection_string($accountName, $accountKey) {
    'DefaultEndpointsProtocol=https;AccountName=' + $accountName + ';AccountKey=' + $accountKey
}

function cmdtokens_quoted([string[]]$cmdTokens) {
    [string[]]$cmdTokens2 = @()
    [int]$cmdTokenCount = -1
    $quoteStart = $null
    foreach($cmdToken in $cmdTokens) {
        if($quoteStart -eq $null) {
            $cmdTokens2 += $cmdToken
            $cmdTokenCount++
        } else {
            $cmdTokens2[$cmdTokenCount] += " " + $cmdToken
        }
            
        if(($cmdToken.StartsWith("""") -eq $True) -or ($cmdToken.StartsWith("'") -eq $True)) {
            if($quoteStart -eq $null) {
                $quoteStart = $cmdToken[0];
            }
        }
        
        if(($cmdToken.EndsWith("""") -eq $True) -or ($cmdToken.EndsWith("'") -eq $True)) {
            if($quoteStart -eq $cmdToken[-1]) {
                $cmdTokens2[$cmdTokenCount] = $cmdTokens2[$cmdTokenCount].TrimStart($quoteStart)
                $cmdTokens2[$cmdTokenCount] = $cmdTokens2[$cmdTokenCount].TrimEnd($quoteStart)                
                $quoteStart = $null
            }
        }
    }
    
    return $cmdTokens2
}

function process_commands()
{
    $promptBase = "wash"
    [Console]::Out.Write($promptBase + ">")
    while($null -ne ($commandLine = [Console]::In.ReadLine())) {
        if($commandLine -eq "exit") {
            break
        }
        $cmdTokens = $commandLine.Split(" ")
        $cmdTokens = cmdtokens_quoted $cmdTokens
        
        process_command $cmdTokens
        $prompt = $promptBase
        if($global:sub.Id -ne $null) {
            $prompt += "/" + $global:sub.Name
        }
        
        if($global:store.Name -ne $null) {
            $prompt += "/" + "store:" + $global:store.Name
            if($global:store.SelectedContainer -ne $null) {
                $prompt += "/" + $global:store.SelectedContainer
                if($global:store.SelectedBlob -ne $null) {
                    $prompt += "/" + $global:store.SelectedBlob
                }
            }
        }
        
        $prompt += ">"
        [Console]::Out.Write($prompt)
    }
    [Console]::Out.WriteLine()
    
    
}

function context_containerName($containerName) {
    if($containerName -eq $null) {
        $containerName = $global:store.SelectedContainer
    }
    $containerName
}

function context_blobName($blobName) {
    if($blobName -eq $null) {
        $blobName = $global:store.SelectedBlob
    }
    $blobName
}

function context_connection($accountName, $accountKey) {
    if(($accountName -eq $null) -or ($accountKey -eq $null)) {
        $global:store.Connection
    } else {
        connection_string $accountName $accountKey
    }
}

function context_storeName($storeName) {
    if($storeName -eq $null) {
        $storeName = $global:store.Name
    }
    $storeName
}

function process_command([string[]]$cmdTokens)
{
    if($null -eq $cmdTokens) {
        show_usage $null
        return
    } elseif($null -eq ($command = $cmdTokens[0])) {
        show_usage $null
        return
    } elseif($command.StartsWith("#")) {
        return
    } elseif($null -eq ($parameters = $commands.Get_Item($command))) {
        show_usage $null
        return
    }

    switch([string]$command) {
        "cert" {
            $certfile = $cmdTokens[1]
            if($certfile -ne $null) {
                $certBase64 = Get-Content $certfile
                $global:sub.Certificate = read_cert $certBase64
            } 
            
            if($global:sub.Certificate -eq $null) {
                show_usage "cert"
            } else {
                Out-Host -inputObject $global:sub.Certificate
            }
        }
        
        "sub" {
            $mgmtURL = $cmdTokens[1]
            $subID = $cmdTokens[2]
            if(($mgmtURL -ne $null) -and ($subID -ne $null)) {
                $global:sub.ManagementURL = $mgmtURL.TrimEnd("/")
                $xml = get_subscriptionXML $global:sub.ManagementURL $subID $global:sub.Certificate
                $global:sub.Name = $xml.Subscription.SubscriptionName
                $global:sub.Id = $xml.Subscription.SubscriptionID
            }
            
            if($null -eq $global:sub.Id) {
                show_usage "sub"
            } else {
                Out-Host -inputObject $global:sub | Format-Table -AutoSize -Wrap
            }
        }
        
        "services" {
            if($global:sub.Id -eq $null) {
                show_error "Subscription not selected."
            } else {
                list_serviceNames $global:sub.ManagementURL $global:sub.Id $global:sub.Certificate
            }
        }
        
        "stores" {
            if($global:sub.Id -eq $null) {
                show_error "Subscription not selected."
            } else {
                list_storageNames $global:sub.ManagementURL $global:sub.Id $global:sub.Certificate
            }
        }
        
        "store" {
            $name = context_storeName $cmdTokens[1]
            $key  = $cmdTokens[2]
            
            if(($name -ne $null) -and ($key -ne $null)) {
                select_store $name $key
            } elseif($global:sub.Id -eq $null) {
                show_error "# Subscription not selected."
                return
            } elseif(($name -eq $null) -and ($key -eq $null)) {
                Out-Host -inputObject $global:store | Format-Table -AutoSize -Wrap
                return
            } elseif($null -ne $name) {
                $storeKey = get_storageKey $global:sub.ManagementURL $global:sub.Id $global:sub.Certificate $name
                select_store $name $storeKey
            }
            
            if(($null -eq $global:store.Name) -or ($null -eq $global:store.Key)) {
                show_usage "store"
            }
        }
        
        "store-key" {
            $name = context_storeName $cmdTokens[1]
            
            if($name -eq $null) {
                show_usage "store-delete"
            } elseif ($name -eq $global:store.Name) {
                $global:store.Key
            } elseif ($global:sub.Id -eq $null) {
                show_error "# Subscription not selected."
            } else {
                get_storageKey $global:sub.ManagementURL $global:sub.Id $global:sub.Certificate $name 
            }
        }
        
        "store-delete" {
            $accountName = context_storeName $cmdTokens[1]
            
            if($accountName -eq $null) {
                show_usage "store-delete"
            } elseif($global:sub.Id -eq $null) {
                show_error "# Subscription not selected."
            } else {
                delete_storage $accountName $global:sub.ManagementURL $global:sub.Id $global:sub.Certificate
                select_store $null $null
            }
        }
        
        "store-create" {
            $accountName = $cmdTokens[1]
            $location = $cmdTokens[2]
            
            if(($accountName -eq $null) -or ($location -eq $null)) {
                show_usage "store-delete"
            } elseif($global:sub.Id -eq $null) {
                show_error "# Subscription not selected."
            } elseif($True -eq (create_storage $accountName $location $global:sub.ManagementURL $global:sub.Id $global:sub.Certificate)) {
                select_store $accountName
            } else {
                show_error("# Failed to create storage account")   
            }
        }
        
        "store-locations" {
            if($global:sub.Id -eq $null) {
                show_error "# Subscription not selected."
            } else {
                list_locationsStore $global:sub.ManagementURL $global:sub.Id $global:sub.Certificate
            }
   
        }

        
        "containers" {
            $accountName = $cmdTokens[1]
            $accountKey = $cmdTokens[2]
            $connection = context_connection $accountName $accountKey
            
            if($connection -eq $null) {
                show_usage "containers"
            } else {
                list_blob_containerNames $connection
            }
        }
        
        "container" {
            $name = context_containerName $cmdTokens[1]
            $accountName = $cmdTokens[2]
            $accountKey = $cmdTokens[3]
            $connection = context_connection $accountName $accountKey
            
            if($null -eq $name) {
                show_usage "container"
            } elseif($connection -eq $null) {
                show_usage "container"
            } else {
                $global:store.SelectedContainer = $name
                $global:store.AccountName = $accountName
                $global:store.AccountKey = $accountKey
                $global:store.Connection = $connection
                $container = get_container $name $connection
            } 
        }
                
        "sub-load" {
            $filepath = $cmdTokens[1]
            if($null -eq $filepath) {
                show_usage "sub-load"
                return
            } elseif((Test-Path $filepath) -eq $False) {
                if($filepath.Contains(".")) {
                    show_error "# File not found"
                    return
                } elseif((Test-Path ($filename + ".publishsettings")) -eq $False) {
                    show_error "# File not found"
                    return
                } else {
                    $filepath += ".publishsettings"
                }
            }
            
            $xml = [xml](Get-Content $filepath)
            $global:sub.ManagementURL = $xml.PublishData.PublishProfile.Url.TrimEnd("/")
            $global:sub.Name = $xml.PublishData.PublishProfile.Subscription.Name
            $global:sub.Id = $xml.PublishData.PublishProfile.Subscription.Id
            $certBase64 = $xml.PublishData.PublishProfile.ManagementCertificate
            $global:sub.Certificate = read_cert $certBase64
        }
        
        "container-delete" {
            $containerName = context_containerName $cmdTokens[1]
            $accountName = $cmdTokens[2]
            $accountKey = $cmdTokens[3]
            $connection = context_connection $accountName $accountKey

            if($null -eq $containerName) {
                show_usage "container-delete"
            } elseif($connection -eq $null) {
                show_usage "container-delete"
            } else {
                delete_container $containerName $connection
                if($containerName -eq $global:store.SelectedContainer) {
                    $global:store.SelectedContainer = $null
                    $global:store.SelectedBlob = $null
                }
            }
        }
        
        "container-create" {
            $containerName = $cmdTokens[1]
            $accountName = $cmdTokens[2]
            $accountKey = $cmdTokens[3]
            $connection = context_connection $accountName $accountKey
            
            if($null -eq $containerName) {
                show_usage "container-create"
            } elseif($connection -eq $null) {
                show_usage "container-create"
            } else {
                create_container $containerName $connection
            }
        }
        
        "blobs" {
            $containerName = context_containerName $cmdTokens[1]
            $accountName = $cmdTokens[2]
            $accountKey = $cmdTokens[3]
            $connection = context_connection $accountName $accountKey
            
            if($containerName -eq $null) {
                show_usage "blobs"
            } elseif($connection -eq $null) {
                show_usage "blobs"
            } else {
                $container = get_container $containerName $connection
                list_blobNames $container
            }
        }
        
        "blob" {
            $blobName = context_blobName $cmdTokens[1]
            $containerName = context_containerName $cmdTokens[2]
            $accountName = $cmdTokens[3]
            $accountKey = $cmdTokens[4]
            $connection = context_connection $accountName $accountKey
            
            if($blobName -eq $null) {
                show_usage "blob"
            } elseif($containerName -eq $null) {
                show_usage "blob"
            } elseif($connection -eq $null) {
                show_error "Storage account not selected"
            } else {
                $global:store.SelectedBlob = $blobName
                $global:store.SelectedContainer = $containerName
                $global:store.AccountName = $accountName
                $global:store.AccountKey = $accountKey
                $global:store.Connection = $connection
                $container = get_container $containerName $connection
                get_blob $blobName $container
            }
        }

        "blob-delete" {
            $blobName = context_blobName $cmdTokens[1]
            $containerName = context_containerName $cmdTokens[2]
            $accountName = $cmdTokens[3]
            $accountKey = $cmdTokens[4]
            $connection = context_connection $accountName $accountKey
            
            if($blobName -eq $null) {
                show_usage "blob-delete"
            } elseif($containerName -eq $null) {
                show_usage "blob-delete"
            } elseif($connection -eq $null) {
                show_usage "blob-delete"
            } else {
                $container = get_container $containerName $connection
                $blob = get_blob $blobName $container
                $blob.Delete()
                if($blobName -eq $global:store.SelectedBlob) {
                    $global:store.SelectedBlob = $null
                }
            }
        }
        
        "blob-download" {
            $filepath = $cmdTokens[1]
            $blobName = context_blobName $cmdTokens[2]
            $containerName = context_containerName $cmdTokens[3]
            $accountName = $cmdTokens[4]
            $accountKey = $cmdTokens[5]
            $connection = context_connection $accountName $accountKey
            
            if($null -eq $filepath) {
                $filepath = $blobName
            } elseif($null -eq $blobName) {
                $blobName = Split-Path $filepath -Leaf
            }
            
            if($null -eq $filepath) {
                show_usage "blob-download"
            } elseif($blobName -eq $null) {
                show_usage "blob-download"
            } elseif($null -eq $containerName) {
                show_usage "blob-download"
            } elseif($null -eq $connection) {
                show_usage "blob-download"
            } else {     
                $container = get_container $containerName $connection
                $blob = get_blob $blobName $container
                $result = download_blob $blob $filepath
                if($result -eq $False) {
                    show_error "Blob download failed"
                }
            }
        }
        
        "blob-upload" {
            $filepath = $cmdTokens[1]
            $blobName = $cmdTokens[2]
            if(($filepath -ne $null) -and ($blobName -eq $null)) {
                $blobName = Split-Path $filepath -Leaf
            }

            $containerName = context_containerName $cmdTokens[3]
            $accountName = $cmdTokens[4]
            $accountKey = $cmdTokens[5]
            $connection = context_connection $accountName $accountKey

            if(($filepath -eq $null) -or ($blobName -eq $null)) {
                show_usage "blob-upload"
            } elseif((Test-Path $filepath) -eq $False) {
                show_error "# File not found"
            } elseif($null -eq $containerName) {
                show_usage "blob-upload"
            } elseif($null -eq $connection) {
                show_usage "blob-upload"
            } else {
                $container = get_container $containerName $connection
                $blob = get_blob $blobName $container
                $result = upload_blob $blob $filepath
                if($result -eq $False) {
                    show_error "Upload failed"
                }
            }
        }
    }
}

function show_error($text) {
    if($text -ne $null) {
        $previousColor = [Console]::ForegroundColor
        [Console]::ForegroundColor = [System.ConsoleColor]::Red
        [Console]::Error.WriteLine("#ERROR " + $text)
        [Console]::ForegroundColor = $previousColor
        return
    }        
}

function show_usage($command) {
    $format = @{Expression={$_.Name};Label="Command"}, @{Expression={$_.Value};Label="Arguments"}
    if($null -eq $command) {
        "USAGE: <command> [<argument>*]"
        $commands.GetEnumerator() | Sort-Object Name | Format-Table -AutoSize -Wrap $format | out-string
    } else {
        $help = $commands.GetEnumerator() | Where-Object {$_.Name -eq $command} | Format-Table -AutoSize -Wrap $format | out-string
        show_error $help
    }
}


$commands = @{
    "containers" = @("[<account-name>]", "[<account-key>]");
    "container" = @("<container-name>", "[<account-name>]", "[<account-key>]");
    "container-create" = @("<container-name>", "[<account-name>]", "[<account-key>]");
    "container-delete" = @("[<container-name>]", "[<account-name>]", "[<account-key>]");
    "blobs" = @("[<container-name>]", "[<account-name>]", "[<account-key>]");
    "blob" = @("[<blob-name>]", "[<container-name>]", "[<account-name>]", "[<account-key>]");
    "blob-delete" = @("[<blob-name>]", "[<container-name>]", "[<account-name>]", "[<account-key>]");
    "blob-upload" = @("<local-path>", "[<blob-name>]", "[<container-name>]", "[<account-name>]", "[""<account-key>""]");
    "blob-download" = @("<local-path>", "[<blob-name>]", "[<container-name>]", "[<accountName>]", "[<accountKey>]");
    "stores" = @();
    "services" = @();
    "sub" = @("<mgmt-url>", "<subscription-id>");
    "cert" = @("<local-path>");
    "store" = @("<name> [<key>]");
    "store-delete" = @("[<name>]");
    "store-create" = @("###NOT FINISHED###", "<name>", "<location>");
    "store-locations" = @();
    "store-key" = @("[<name>]");
    "sub-load" = @("<publish-settings-file>")
}

    
[string[]]$cmdTokens = @()
foreach($arg in $args) {
    $cmdTokens += $arg
}

$myDir = Split-Path -Parent $MyInvocation.MyCommand.Path

if($cmdTokens.Length -gt 0) {
    process_command $cmdTokens
} else {
    process_commands
}