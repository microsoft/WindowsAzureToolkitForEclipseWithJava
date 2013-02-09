# wash v0.1.0
# Copyright 2013 by Microsoft Open Technologies, Inc.
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

[xml]$global:publishDataXML
$global:sub = @{Name = $null; Id = $null; Certificate = $null; ManagementURL = $null}
$global:store = @{Name = $null; Key = $null; SelectedContainer = $null; Connection = $null; SelectedBlob = $null}
$global:service = @{Name = $null; URL = $null; Location = $null; SelectedDeployment = $null}

$entities = @{
    "subs" = @{
        "load" = @("<publish-settings-file>");
    }
        
    "sub" = @{
        "use" = @("<subscription-id> | <subscription-name>", "<mgmt-url>");
        "cert" = @("<cert-file>");
        "stores" = @("<>")
        "services" = @("<>");
    }

    "store" = @{
        "use" = @("<name> [<key>]");
        "create" = @("<name>", "<location>");
        "delete" = @("[<name>]");
        "locations" = @("<>");
        "key" = @("[<name>]", "[--notepad]");
        "containers" = @("<>");
        "reset" = @("<>");
    }
    
    "container" = @{
        "use" =    @("<container-name>", "[<account-name>]", "[<account-key>]");
        "create" = @("<container-name>", "[<account-name>]", "[<account-key>]");
        "delete" = @("[<container-name>]", "[<account-name>]", "[<account-key>]");
        "blobs" = @("<>");
        "reset" = @("<>");
        "access" = @("container | blob | off", "[<container-name>]", "[<account-name>]", "[<account-key>]");
    }
    
    "blob" = @{
        "use" = @("[<blob-name>]", "[<container-name>]", "[<account-name>]", "[<account-key>]");
        "delete" = @("[<blob-name>]", "[<container-name>]", "[<account-name>]", "[<account-key>]");
        "upload" = @("<local-path>", "[<blob-name>]", "[<container-name>]", "[<account-name>]", "[""<account-key>""]");
        "download" = @("<local-path>", "[<blob-name>]", "[<container-name>]", "[<accountName>]", "[<accountKey>]");
        "uri" = @("[<blob-name>]", "[<container-name>]", "[<account-name>]", "[<account-key>]", "[--notepad]blob ");
        "reset" = @("<>");
    }
    
    "service" = @{
        "use" = @("<service-name>");
        "delete" = @("[<service-name>]");
        "location" = @("[<service-name>]");
        "status" = @("[<service-name>]");
        "deployments" = @("<service-name>");
        "reset" = @("<>");
    }
    
    "deployment" = @{
        "use" = @("<name>", "[<service-name>]");
        "url" = @("[<name>]", "[<service-name>]", "[--notepad]");
        "reset" = @("<>");
    }
        
    "file" = @{
        "download" = @("<URL>", "<local-path>");
        "zip" = @("<src-path>", "<zip-path>");
        "unzip" = @("<zip-path>", "<dest-path>");
        "exists" = @("<url>")
    }
    
    "dir" = @{
        "files" = @();
        "use" = @("<directory-path>");
    }
    
    "cd" = @{
        "[<path>]" = @()
    }
}

###############################
### Command implementations ###
###############################

### Entity: subs ###
function cmd_subs_load([string[]]$cmdTokens) {
    $filepath = $cmdTokens[2]
    if($null -eq $filepath) {
        show_usage $cmdTokens
        return
    } elseif((Test-Path $filepath) -eq $False) {
        if($filepath.Contains(".")) {
            show_error "File not found"
            return
        } elseif((Test-Path ($filename + ".publishsettings")) -eq $False) {
            show_error "File not found"
            return
        } else {
            $filepath += ".publishsettings"
        }
    }
    
    $global:publishDataXML = [xml](Get-Content $filepath)
    $global:sub.ManagementURL = $global:publishDataXML.PublishData.PublishProfile.Url.TrimEnd("/")
    $certBase64 = $global:publishDataXML.PublishData.PublishProfile.ManagementCertificate
    $global:sub.Certificate = read_cert $certBase64
}


function cmd_subs([string[]]$cmdTokens) {
    if($global:publishDataXML -eq $null) {
        show_error "Publish settings file not loaded"
    } else {
        $global:publishDataXML.PublishData.PublishProfile.Subscription | foreach {$_}
    }
}


### Entity: sub ###

function cmd_sub_use([string[]]$cmdTokens) {
    $subID = $cmdTokens[2]
    $mgmtURL = context_managementURL $cmdTokens[3]
    
    if(($mgmtURL -eq $null) -or ($subID -eq $null)) {
        show_usage $cmdTokens
        return
    }
    
    # Discover sub ID if known
    if($global:publishDataXML -ne $null) {
        $subs = ($global:publishDataXML.PublishData.PublishProfile.Subscription | Where-Object {($_.Name -like $subID) -or ($_.Id -like $subID)})
        if($subs.Length -ge 2) {
            show_error "Failed to select a subscription because multiple subscriptions exists with this name. Use the id instead"
            return
        } else {
            $subId = $subs.Id
        }
    }
    
    $global:sub.ManagementURL = $mgmtURL.TrimEnd("/")
    $xml = get_subscriptionXML $global:sub.ManagementURL $subID $global:sub.Certificate
    $global:sub.Name = $xml.Subscription.SubscriptionName
    $global:sub.Id = $xml.Subscription.SubscriptionID
}


function cmd_sub([string[]]$cmdTokens) {
    if($null -eq $global:sub.Id) {
        show_usage $cmdTokens
    } elseif($cmdTokens.length -gt 1) {
        show_usage $cmdTokens
    } else {
        Out-Host -inputObject $global:sub | Format-Table -AutoSize -Wrap
    } 
}


function cmd_sub_stores([string[]]$cmdTokens) {
    if(-not (check_sub)) {
        return
    } elseif($null -eq ($storeNames = (list_storeNames $global:sub.ManagementURL $global:sub.Id $global:sub.Certificate))) {
        show_error "Failed to get the list of storage accounts"
    } else {
        $storeNames
    }
}


function cmd_sub_services([string[]]$cmdTokens) {
    if(-not (check_sub)) {
        return
    } elseif($null -eq ($serviceNames = (list_services $global:sub.ManagementURL $global:sub.Id $global:sub.Certificate))) {
        show_error "Failed to get the cloud services list"
    } else {
        $serviceNames
    }
}


function cmd_sub_cert([string[]]$cmdTokens) {
    if($null -ne ($certfile = $cmdTokens[2])) {
        $certBase64 = Get-Content $certfile
        $global:sub.Certificate = read_cert $certBase64
    } 
            
    if($global:sub.Certificate -eq $null) {
        show_usage $cmdTokens
    } else {
        Out-Host -inputObject $global:sub.Certificate
    }
}


### Entity: store ###

function cmd_store([string[]]$cmdTokens)
{
    if(($null -eq $global:store.Name) -or ($null -eq $global:store.Key)) {
        show_usage $cmdTokens
    } elseif($cmdTokens.length -gt 1) {
        show_usage $cmdTokens
    } else  {
        Out-Host -inputObject $global:store | Format-Table -AutoSize -Wrap
    }
}


function cmd_store_use([string[]]$cmdTokens)
{
    $name = context_storeName $cmdTokens[2]
    $key  = $cmdTokens[3]
    
    if($name -eq $null) {
        show_usage $cmdTokens
    } elseif($key -ne $null) {
        select_store $name $key
    } elseif(!(check_sub)) {
        return
    } elseif($null -eq ($name = resolve_storeName $name)) {
        show_error "Failed to find this storage account"
    } else {
        $key = get_storageKey $global:sub.ManagementURL $global:sub.Id $global:sub.Certificate $name
        select_store $name $key
    }
}


function cmd_store_reset([string[]]$cmdTokens) {
    select_store $null $null
}


function cmd_store_create([string[]]$cmdTokens) {
    $accountName = $cmdTokens[2]
    $location = $cmdTokens[3]
            
    if(($accountName -eq $null) -or ($location -eq $null)) {
        show_usage $cmdTokens
    } elseif(!(check_sub)) {
        return
    } elseif($True -eq (create_storage $accountName $location $global:sub.ManagementURL $global:sub.Id $global:sub.Certificate)) {
        select_store $accountName
    } else {
        show_error("Failed to create storage account")   
    }
}


function cmd_store_delete([string[]]$cmdTokens) {
    $accountName = context_storeName $cmdTokens[2]
            
    if($accountName -eq $null) {
        show_usage $cmdTokens
    } elseif(check_sub) {
        $operation = "services/storageServices/" + $accountName
        try {
            call_REST $global:sub.ManagementURL $global:sub.Id $operation "DELETE" $global:sub.Certificate
            select_store $null $null
        } catch {
            show_error "Failed to delete storage account"
        }
    }
}


function cmd_store_locations([string[]]$cmdTokens)
{
    if(-not (check_sub)) {
        return
    } elseif($null -eq ($locationsXML = (list_locationsXML $global:sub.ManagementURL $global:sub.Id $global:sub.Certificate))) {
        show_error "Failed to get the storage locations list"
    } else {
        $locationsXML.Locations.Location | foreach { $_.Name }
    }
}


function cmd_store_key([string[]]$cmdTokens, [string[]]$cmdOptions)
{
    $name = context_storeName $cmdTokens[2]
            
    if($name -eq $null) {
        show_usage $cmdTokens
    } elseif($null -eq ($name = resolve_storeName $name)) {
        show_error "Failed to find this storage account"
    } elseif (-not(check_sub)) {
        return
    } elseif($null -eq ($key = get_storageKey $global:sub.ManagementURL $global:sub.Id $global:sub.Certificate $name)) {
        show_error "Failed to get the key for this storage account"
    } else {
        if($name -eq $global:store.Name) {
            $global:store.Key = $key
        }

        if($cmdOptions -contains "--notepad") {
            out-notepad $key
        } else {
            $key
        }
    }
}


function cmd_store_containers([string[]]$cmdTokens)
{
    $accountName = $global:store.Name
    $accountKey = $global:store.Key
    $connection = context_connection $accountName $accountKey
    
    if($null -eq ($containers = list_containers $connection)) {
        show_usage $cmdTokens
    } else {
        $containers
    }
}


### Entity: container ###

function cmd_container_use([string[]]$cmdTokens)
{
    $name = $cmdTokens[2]
    $accountName = $cmdTokens[3]
    $accountKey = $cmdTokens[4]
    $connection = context_connection $accountName $accountKey
            
    if(($null -eq $name) -or ($connection -eq $null)) {
        show_usage $cmdTokens
    } elseif($null -eq ($name = resolve_containerName $name $connection)) {
        show_error "Failed to find this container"        
    } else {
        if($global:store.SelectedContainer -ne $name) {
            $global:store.SelectedBlob = $null
        }
        
        $global:store.SelectedContainer = $name
        
        if($accountName -ne $null) {
            $global:store.Name = $accountName
        }
        
        if($accountKey -ne $null) {
            $global:store.Key = $accountKey
        }
        
        $global:store.Connection = $connection
    } 
}


function cmd_container_reset([string[]]$cmdTokens) {
    $global:store.SelectedBlob = $null
    $global:store.SelectedContainer = $null
}


function cmd_container_create([string[]]$cmdTokens) {
    $containerName = $cmdTokens[2]
    $accountName = $cmdTokens[3]
    $accountKey = $cmdTokens[4]
    $connection = context_connection $accountName $accountKey
            
    if($null -eq $containerName) {
        show_usage $cmdTokens
    } elseif($connection -eq $null) {
        show_usage $cmdTokens
    } elseif($null -eq ($container = get_container $containerName $connection)) {
        show_error "Container could not be created"
    } else {
        try {
            [void]$container.CreateIfNotExist()
        } catch {
            show_error "Container could not be created"
        }
    }
}


function cmd_container_access([string[]]$cmdTokens) {
    $access = $cmdTokens[2]
    $containerName = context_containerName $cmdTokens[3]
    $accountName = $cmdTokens[4]
    $accountKey = $cmdTokens[5]
    $connection = context_connection $accountName $accountKey

    if(($null -eq $containerName) -or ($null -eq $connection)) {
        show_usage $cmdTokens
    } elseif((-not ([String]::IsNullOrEmpty($access))) -and (@("container", "off", "blob") -notcontains $access)) {
        show_usage $cmdTokens
    } elseif($null -eq ($containerName = resolve_containerName $containerName $connection)) {
        show_error "Failed to find this container"        
    } elseif($null -eq  ($container = get_container $containerName $connection)) {
        show_error "Container could not be accessed"
    } elseif(-not([String]::IsNullOrEmpty($access))) {
        $perms = New-Object Microsoft.WindowsAzure.StorageClient.BlobContainerPermissions
        
        try {
            $perms.PublicAccess = $access
            $container.SetPermissions($perms)
        } catch {
            show_error "Failed to set container access permissions"
        }
    } elseif($null -eq ($perms = $container.GetPermissions())) {
        show_error "Failed to read container access permissions"
    } else {
        ([string]($perms.PublicAccess)).ToLower()
    }
}


function cmd_container_delete([string[]]$cmdTokens)
{
    $containerName = context_containerName $cmdTokens[2]
    $accountName = $cmdTokens[3]
    $accountKey = $cmdTokens[4]
    $connection = context_connection $accountName $accountKey

    if($null -eq $containerName) {
        show_usage $cmdTokens
    } elseif($connection -eq $null) {
        show_usage $cmdTokens
    } elseif($null -eq  ($container = get_container $containerName $connection)) {
        show_error "Container could not be accessed"
    } else {
        try {
            $container.Delete()
            if($containerName -eq $global:store.SelectedContainer) {
                $global:store.SelectedContainer = $null
                $global:store.SelectedBlob = $null
            }
        } catch {
            show_error "Container could not be deleted"
        }
    }
}


function cmd_container_blobs([string[]]$cmdTokens)
{
    $containerName = $global:store.SelectedContainer
    $accountName = $global:store.Name
    $accountKey = $global:store.Key
    $connection = context_connection $accountName $accountKey
    
            
    list_blobs $connection $containerName
}


### Entity: blob ###

function cmd_blob([string[]]$cmdTokens)
{
    $blobName = $global:store.SelectedBlob
    $containerName = $global:store.SelectedContainer
    $connection = $global:store.Connection
            
    if($null -eq ($container = get_container $containerName $connection)) {
        show_usage $cmdTokens
    } elseif($null -eq ($blob = get_blob $blobName $container)) {
        show_usage $cmdTokens
    } else {
        return $blob
    }
}


function cmd_blob_use([string[]]$cmdTokens)
{
    $blobName = $cmdTokens[2]
    $containerName = context_containerName $cmdTokens[3]
    $accountName = $cmdTokens[4]
    $accountKey = $cmdTokens[5]
    $connection = context_connection $accountName $accountKey
            
    if(($blobName -eq $null) -or ($containerName -eq $null)) {
        show_usage $cmdTokens
    } elseif($connection -eq $null) {
        show_error "Storage account not selected"
    } elseif($null -eq ($containerName = resolve_containerName $containerName $connection)) {
        show_error "Failed to find this container"        
    } elseif($null -eq ($blobName = (resolve_blobName $blobName $containerName $connection))) {
        show_error "Failed to find this blob"        
    } else {
        $global:store.SelectedBlob = $blobName
        $global:store.SelectedContainer = $containerName
        $global:store.Connection = $connection

        if($accountName -ne $null) {
            $global:store.Name = $accountName
        }
        
        if($accountKey -ne $null) {
            $global:store.Key = $accountKey
        }
    }
}


function cmd_blob_reset([string[]]$cmdTokens)
{
    $global:store.SelectedBlob = $null
}


function cmd_blob_delete([string[]]$cmdTokens)
{
    $blobName = context_blobName $cmdTokens[2]
    $containerName = context_containerName $cmdTokens[3]
    $accountName = $cmdTokens[4]
    $accountKey = $cmdTokens[5]
    $connection = context_connection $accountName $accountKey
            
    if($null -eq ($container = get_container $containerName $connection)) {
        show_usage $cmdTokens
    } elseif($null -eq ($blob = get_blob $blobName $container)) {
        show_usage $cmdTokens
    } else {
        $blob.Delete()
        if($blobName -eq $global:store.SelectedBlob) {
            $global:store.SelectedBlob = $null
        }
    }
}


function cmd_blob_upload([string[]]$cmdTokens)
{
    [string]$filepath = $cmdTokens[2]
    $blobName = $cmdTokens[3]
    if(($filepath -ne $null) -and ($blobName -eq $null) -and ($filepath -ne "")) {
        $blobName = Split-Path $filepath -Leaf
    }

    $containerName = context_containerName $cmdTokens[4]
    $accountName = $cmdTokens[5]
    $accountKey = $cmdTokens[6]
    $connection = context_connection $accountName $accountKey

    if(($filepath -eq $null)) {
        show_usage $cmdTokens
    } elseif((Test-Path $filepath) -eq $False) {
        show_error "File not found"
    } elseif($null -eq ($containerName = resolve_containerName $containerName $connection)) {
        show_error "Failed to find this container"        
    } elseif($null -eq ($container = get_container $containerName $connection)) {
        show_usage $cmdTokens
    } elseif($null -eq ($blob = get_blob $blobName $container)) {
        show_usage $cmdTokens
    } else {
        $filepath = Resolve-Path $filepath
        try {
            [void]$blob.UploadFile($filepath)
        } catch {
            show_error "Upload failed"
        }
    }
}


function cmd_blob_download([string[]]$cmdTokens)
{
    $filepath = $cmdTokens[2]
    $blobName = context_blobName $cmdTokens[3]
    $containerName = context_containerName $cmdTokens[4]
    $accountName = $cmdTokens[5]
    $accountKey = $cmdTokens[6]
    $connection = context_connection $accountName $accountKey
            
    if($null -eq $filepath) {
        $filepath = $blobName
    } elseif($null -eq $blobName) {
        $blobName = Split-Path $filepath -Leaf
    }
            
    if($null -eq $filepath) {
        show_usage $cmdTokens
    } elseif($null -eq ($containerName = resolve_containerName $containerName $connection)) {
        show_error "Failed to find this container"        
    } elseif($null -eq ($container = get_container $containerName $connection)) {     
        show_usage $cmdTokens
    } elseif($null -eq ($blob = get_blob $blobName $container)) {
        show_usage $cmdTokens
    } else {
        if(Test-Path $filepath) {
            Remove-Item $filepath
        }
        
        try {
            $blob.DownloadToFile($filepath)
        } catch {
            show_error "Blob download failed"
        }
    }
}


function cmd_blob_uri([string[]]$cmdTokens)
{
    $blobName = context_blobName $cmdTokens[2]
    $containerName = context_containerName $cmdTokens[3]
    $accountName = $cmdTokens[4]
    $accountKey = $cmdTokens[5]
    $connection = context_connection $accountName $accountKey

    if($null -eq ($container = get_container $containerName $connection)) {
        show_usage $cmdTokens
    } elseif($null -eq ($blobName = (resolve_blobName $blobName $containerName $connection))) {
        show_error "Failed to find this blob"
    } elseif($null -eq ($blob = get_blob $blobName $container)) {
        show_usage $cmdTokens
    } else {
        $uri = $blob.Uri.AbsoluteUri
        if($cmdOptions -contains "--notepad") {
            out-notepad $uri
        } else {
            $uri
        }
    }
}


### Entity: service ###

function cmd_service([string[]]$cmdTokens)
{
    if(check_service) {
        Out-Host -inputObject $global:service | Format-Table -AutoSize -Wrap
    } else {
        show_usage $cmdTokens
    }
}


function cmd_service_use([string[]]$cmdTokens) {
    $name = $cmdTokens[2]
    $mgmtURL = $global:sub.ManagementURL 
    $subID = $global:sub.Id 
    $cert = $global:sub.Certificate
    
    if(!(check_sub)) {
        return
    } elseif($null -eq $name) {
        show_usage $cmdTokens
    } elseif($null -eq ($name = resolve_serviceName $name $mgmtURL $subID $cert)) {
        show_error "Failed to find this service"
    } elseif ($null -eq ($xml = get_serviceXML $name $mgmtURL $subID $cert)) {
        show_error "Cannot select this service"
    } else {        
        $global:service.Name = $name
        $global:service.URL = $xml.HostedService.Url
        $global:service.Location = $xml.HostedService.HostedServiceProperties.Location
    }
}


function cmd_service_reset([string[]]$cmdTokens) {
    $global:service.Name = $null
    $global:service.URL = $null
    $global:service.Location = $null
    $global:service.SelectedDeployment = $null
}


function cmd_service_delete([string[]]$cmdTokens) {
    $name = context_serviceName $cmdTokens[2]
    if($name -eq $null) {
        show_usage $cmdTokens
    } elseif(check_sub) {
        $operation = "services/hostedservices/" + $name
        try {
            call_REST $global:sub.ManagementURL $global:sub.Id $operation "DELETE" $global:sub.Certificate
            if($name -eq $global:service.Name) {
                $global:service.Name = $null
                $global:service.URL = $null
                $global:service.Location = $null
            }
        } catch {
            show_error "Failed to delete service"
        }
    }
}


function cmd_service_location([string[]]$cmdTokens) {
    $mgmtURL = $global:sub.ManagementURL
    $subID = $global:sub.Id
    $cert = $global:sub.Certificate
    
    if($null -eq ($name = context_serviceName $cmdTokens[2])) {
        show_usage $cmdTokens
    } elseif(!(check_sub)) {
        return
    } elseif($null -eq ($name = resolve_serviceName $name $mgmtURL $subID $cert)) {
        show_error "Failed to find this service"
    } elseif(($global:service.Location -ne $null) -and ($name -eq $global:service.Name)) {
        $global:service.Location
    } elseif($null -ne ($xml = get_serviceXML $name $mgmtURL $subID $cert)) {
        $xml.HostedService.HostedServiceProperties.Location
    }
}


function cmd_service_status([string[]]$cmdTokens) {
    $mgmtURL = $global:sub.ManagementURL
    $subID = $global:sub.Id
    $cert = $global:sub.Certificate
    
    if($null -eq ($name = context_serviceName $cmdTokens[2])) {
        show_usage $cmdTokens
    } elseif(!(check_sub)) {
        return
    } elseif($null -eq ($name = resolve_serviceName $name $mgmtURL $subID $cert)) {
        show_error "Failed to find this service"
    } elseif($null -ne ($xml = get_serviceXML $name $mgmtURL $subID $cert)) {
        $xml.HostedService.HostedServiceProperties.Status
    }
}


function cmd_service_deployments([string[]]$cmdTokens) {
    if($null -eq ($name = context_serviceName $cmdTokens[2])) {
        show_usage $cmdTokens
    } elseif(!(check_sub)) {
        return
    } else {
        list_deployments $name $global:sub.ManagementURL $global:sub.Id $global:sub.Certificate 
    }
}


### Entity: deployment ###

function cmd_deployment_use([string[]]$cmdTokens) {
    $name = $cmdTokens[2]
    $serviceName = context_serviceName $cmdTokens[3]
    $mgmtURL = $global:sub.ManagementURL 
    $subID = $global:sub.Id 
    $cert = $global:sub.Certificate
    
    if(!(check_sub)) {
        return
    } elseif(($null -eq $name) -or ($null -eq $serviceName)) {
        show_usage $cmdTokens
        return
    } elseif($null -eq ($serviceName = resolve_serviceName $serviceName $mgmtURL $subID $cert)) {
        show_error "Failed to find this service"
    } elseif($null -eq ($name = resolve_deploymentName $name $serviceName $mgmtURL $subID $cert)) {
        show_error "Failed to find this deployment"
    } elseif($null -eq ($xml = get_deploymentXML $name $serviceName $mgmtURL $subID $cert)) {
        show_error "Failed to select the deployment"
    } else {
        $global:service.Name = $serviceName
        $global:service.SelectedDeployment = $name
    }
}


function cmd_deployment_reset([string[]]$cmdTokens) {
    $global:service.SelectedDeployment = $null    
}


function cmd_deployment_url([string[]]$cmdTokens) {
    $name = context_deploymentName $cmdTokens[2]
    $serviceName = context_serviceName $cmdTokens[3]
    $mgmtURL = $global:sub.ManagementURL 
    $subID = $global:sub.Id 
    $cert = $global:sub.Certificate
    
    if(!(check_sub)) {
        return
    } elseif(($null -eq $name) -or ($null -eq $serviceName)) {
        show_usage $cmdTokens
        return
    } elseif($null -eq ($serviceName = resolve_serviceName $serviceName $mgmtURL $subID $cert)) {
        show_error "Failed to find this service"
    } elseif($null -eq ($name = resolve_deploymentName $name $serviceName $mgmtURL $subID $cert)) {
        show_error "Failed to find this deployment"
    } elseif($null -eq ($xml = get_deploymentXML $name $serviceName $mgmtURL  $subID $cert)) {
        show_error "Failed to get the URL"
    } else {
        $url = $xml.Deployment.Url
        if($cmdOptions -contains "--notepad") {
            out-notepad $url
        } else {
            $url
        }
    }
}


### Entity: file ###

function cmd_file_download([string[]]$cmdTokens) {
    $srcURL = $cmdTokens[2]
    [string]$destFilePath = $cmdTokens[3]
    
    if(($srcURL -eq $null) -or ($destFilePath -eq $null)) {
        show_usage $cmdTokens
        return
    }
    
    if(Test-Path $destFilePath) {
        Remove-Item $destFilePath
    }
    
    $webclient = New-Object System.Net.WebClient
    $webclient.DownloadFile($srcURL,$destFilePath)    
}


function cmd_file_exists([string[]]$cmdTokens) {
    $srcURL = $cmdTokens[2]
    if($srcURL -eq $null) {
        show_usage $cmdTokens
        return
    }

    try 
    { 
        $request = [System.Net.HttpWebRequest]::Create($srcURL)
        $response = $request.GetResponse()
        if($response.StatusCode -eq "OK") {
            return $true
        } else {
            return $false
        }
    } 
    catch {
        return $false
    }
}


function cmd_file_zip([string[]]$cmdTokens) {
    $srcPath = $cmdTokens[2]
    $zipPath = $cmdTokens[3]
    if(($srcPath -eq $null) -or ($zipPath -eq $null)) {
        show_usage $cmdTokens
        return
    }
    
    if(Test-Path $zipPath) {
        Remove-Item $zipPath
    }
    
    if(Test-Path $srcPath -PathType Leaf) {
        # zip one file
        ls $srcPath | out_zip $zipPath
    } else {
        # zip directory
        Get-Item ($srcPath + '\\.') | out_zip $zipPath
    }
}


function cmd_file_unzip([string[]]$cmdTokens) {
    $zipPath = $cmdTokens[2]
    $destPath = $cmdTokens[3]
    
    if(($zipPath -eq $null) -or ($destPath -eq $null)) {
        show_usage
        return
    } elseif(-not (Test-Path $zipPath)) {
        show_error "Failed to find the ZIP file"
        return
    }
    
    $app = New-Object -com Shell.Application
    [string]$zipPath = Resolve-Path $zipPath
    $zipFile = $app.NameSpace($zipPath)
    
    [string]$destPath = Resolve-Path $destPath
    $destFile = $app.NameSpace($destPath)

    if($destFile -eq $null) {
        show_error "Failed to open destination folder"
    } elseif($zipFile -eq $null) {
        show_error "Failed to open the ZIP file"
    } 
    
    $destFile.CopyHere($zipFile.Items(), 20)
}


### Entity: dir

function cmd_dir([string[]]$cmdTokens) {
    dir
}


function cmd_dir_files([string[]]$cmdTokens) {
    dir
}


function cmd_dir_use([string[]]$cmdTokens) {
    select_directory $cmdTokens[2]
}


function cmd_cd([string[]]$cmdTokens) {
    select_directory $cmdTokens[1]
}

    
#########################
### Utility functions ###
#########################

function select_directory($path) {
    if($null -eq $path) {
        $(Get-Location).Path
    } elseif(Test-Path $path) {
        cd -path $path
    } else {
        show_error "Failed to select the requested path"
    } 
}

function resolve_serviceName($serviceName, $mgmtURL, $subID, $cert) {
    (list_services $mgmtURL $subID $cert) | Where-Object { $_ -like $serviceName } | Select-Object -first 1
}

function resolve_deploymentName($deploymentName, $serviceName, $mgmtURL, $subID, $cert) {
    (list_deployments $serviceName $mgmtURL $subID $cert) | Where-Object { $_ -like $deploymentName } | Select-Object -first 1
}

function resolve_storeName($storeName) {
    (list_storeNames $global:sub.ManagementURL $global:sub.Id $global:sub.Certificate) | Where-Object { $_ -like $storeName } | Select-Object -first 1
}


function resolve_containerName($containerName, $connection) {
    (list_containers $connection) | Where-Object { $_ -like $containerName } | Select-Object -first 1
}


function resolve_blobName($blobName, $containerName, $connection) {
    (list_blobs $connection $containerName) | Where-Object { $_ -like $blobName } | Select-Object -first 1
}


function out-notepad($text) {
    $wshShell = New-Object -ComObject wscript.shell
    [void]$wshShell.Run("notepad")
    [void]$wshshell.AppActivate("Notepad")
    Start-Sleep -milliseconds 200
    $wshShell.SendKeys($text)
}


function list_blobs($connection, $containerName) {
    if(($null -eq $connection) -or ($null -eq $containerName)) {
        return $null
    } elseif($null -eq ($container = get_container $containerName $connection)) {
        return $null
    } else {
        load_storage_client
        $options = New-Object Microsoft.WindowsAzure.StorageClient.BlobRequestOptions
        $options.UseFlatBlobListing = $true;
        $container.ListBlobs($options) | Where-Object { [String]::IsNullOrEmpty($_.Name) -eq $False } | foreach { $_.Name }
    }
}


function list_services($mgmtURL, $subID, $cert) {
    if($null -eq ($servicesXML = (list_servicesXML $mgmtURL $subID $cert))) {
        return $null
    } else {
        $servicesXML.HostedServices.HostedService | Where-Object { [String]::IsNullOrEmpty($_.ServiceName) -eq $False } | foreach {$_.ServiceName }
    }
}


function list_deployments($serviceName, $mgmtURL, $subID, $cert) {
    if($null -ne ($xml = get_serviceXML $serviceName $mgmtURL $subID $cert)) {
        $xml.HostedService.Deployments.Deployment | Where-Object {[String]::IsNullOrEmpty($_.Name) -eq $False} | foreach { $_.Name }
    } else {
        return $null
    }
}


function list_containers($connectio) {
    if($connection -eq $null) {
        return $null
    } elseif($null -eq ($blobClient = get_blob_client $connection)) {
        return $null
    } else {
        $blobClient.ListContainers() | foreach { 
            if([String]::IsNullOrEmpty($_.Name) -eq $False) { $_.Name}
        }       
    }
}


function get_deploymentXML($name, $serviceName, $cloudURL, $subID, $cert) {
    $operation = "services/hostedservices/" + $serviceName + "/deployments/" + $name
    call_REST $cloudURL $subID $operation "GET" $cert $null
}


function read_cert($certBase64) {
    $certBytes = [System.Convert]::FromBase64String($certBase64)
    $cert = New-Object System.Security.Cryptography.X509Certificates.X509Certificate2
    $cert.Import($certBytes)
    return $cert
}


function check_sub() {
    if(($global:sub.Name -ne $null) -and ($global:sub.Id -ne $null) -and ($global:sub.Certificate -ne $null) -and ($global:sub.ManagementURL -ne $null)) {
        return $true
    } else {
        show_error "Subscription not selected"
        return $false
    }
}


function get_subscriptionXML($cloudURL, $subscriptionID, $cert) {
    call_REST $cloudURL $subscriptionID $null "GET" $cert
}


function get_storageXML($cloudURL, $subscriptionID, $cert) {
    call_REST $cloudURL $subscriptionID "services/storageservices" "GET" $cert
}


function context_deploymentName($name) {
    if($name -eq $null) {
        $name = $global:service.SelectedDeployment
    }
    $name
}


function context_managementURL($mgmtURL) {
    if($mgmtURL -eq $null) {
        $mgmtURL = $global:sub.ManagementURL
    }
    $mgmtURL
}


function context_storeName($storeName) {
    if($storeName -eq $null) {
        $storeName = $global:store.Name
    }
    $storeName
}


function context_dirName($dirName) {
    if($dirName -eq $null) {
        $dirName = $(Get-Location).Path
    }
    $dirName
}


function select_store($name, $key) {
    if(($name -eq $null) -or ($key -eq $null)) {
        $global:store.Connection = $null
        $global:store.Name = $null
        $global:store.Key = $null
    } else {
        $global:store.Name = $name
        $global:store.Key = $key
        $global:store.Connection = connection_string $name $key
    }
    $global:store.SelectedContainer = $null
    $global:store.SelectedBlob = $null
}


function create_storage([string]$name, [string]$location, $cloudURL, $subscriptionID, $cert) {
    $operation = "services/storageServices"
    $body = [xml]"<CreateStorageServiceInput xmlns=""http://schemas.microsoft.com/windowsazure"">
                    <ServiceName/>
                    <Label/>
                    <Location/>
                    <GeoReplicationEnabled/>
                </CreateStorageServiceInput>"
    $body.CreateStorageServiceInput.ServiceName = $name
    $body.CreateStorageServiceInput.Location = $location
    $body.CreateStorageServiceInput.Label = toBase64 $name 
    $body.CreateStorageServiceInput.GeoReplicationEnabled = "false"
    try {
        if($null -eq ($error = call_REST_wait $cloudURL $subscriptionID $operation "POST" $cert $body)) {
            return $True
        } else {
            return $False
        }
    } catch {
        return $False
    }
}


function list_storeNames($cloudURL, $subID, $cert) {
    if($null -eq ($storageXML = (get_storageXML $cloudURL $subID $cert))) {
        return $null
    } else {
        $storageXML.StorageServices.StorageService | foreach {$_.ServiceName } | Where-Object { [String]::IsNullOrEmpty($_) -eq $False }       
    }
}

function toBase64([string]$text) {
    $bytes  = [System.Text.Encoding]::UTF8.GetBytes($text);
    [System.Convert]::ToBase64String($bytes);
}

function list_locationsXML($cloudURL, $subscriptionID, $cert) {
    call_REST $cloudURL $subscriptionID "locations" "GET" $cert
}


function get_storageKey($cloudURL, $subscriptionID, $cert, $storageName) {
    $storeKeysXML = get_storageKeysXML $storageName  $cloudURL $subscriptionID $cert
    if($storeKeysXML -ne $null) {
        $storeKeysXML.StorageService.StorageServiceKeys.Primary
    } else {
        return $null
    }            
}


function get_storageKeysXML($name, $cloudURL, $subscriptionID, $cert) {
    $operation = "services/storageServices/" + $name + "/keys"
    call_REST $cloudURL $subscriptionID $operation "GET" $cert    
}


function context_connection($accountName, $accountKey) {
    if(($accountName -eq $null) -or ($accountKey -eq $null)) {
        $global:store.Connection
    } else {
        connection_string $accountName $accountKey
    }
}


function connection_string($accountName, $accountKey) {
    'DefaultEndpointsProtocol=https;AccountName=' + $accountName + ';AccountKey=' + $accountKey
}


function context_containerName($containerName) {
    if($containerName -eq $null) {
        $containerName = $global:store.SelectedContainer
    }
    $containerName
}


function get_container($name, $connection) {
    if(($connection -eq $null) -or ($name -eq $null)) {
        return $null
    } elseif ($null -eq ($blobClient = get_blob_client $connection)) {
        return $null
    } else {
        $blobClient.GetContainerReference($name)
    }
}


function get_blob_client($connection) {
    load_storage_client
    $storageAccount = [Microsoft.WindowsAzure.CloudStorageAccount]::Parse($connection)
    New-Object Microsoft.WindowsAzure.StorageClient.CloudBlobClient($storageAccount.BlobEndpoint, $storageAccount.Credentials)
}


function load_storage_client() {
    $fileName = 'Microsoft.WindowsAzure.StorageClient.dll'

    $path = Join-Path $myDir $fileName    
    if(-not (Test-Path $path)) {
        # If StorageClient.dll not found in current directory then look for SDK
        $azureSDKPath = findLatestAzureSDKDir
        $path = Join-Path $azureSDKPath $fileName
    }
    
    if(-not (Test-Path $path)) {
        show_error 'The required StorageClient.dll cannot be found'
        return $null
    }

    Add-Type -Path ($path)
}


function findLatestAzureSDKDir() {
    $programFilesDir = (Get-ChildItem env:ProgramFiles).Value
    $sdkDir = Join-Path $programFilesDir 'Microsoft SDKs\Windows Azure\.NET SDK'
    if(-not (Test-Path $sdkDir)) {
        return $null
    } 
    
    $latestDirParent= (Get-ChildItem -Path $sdkDir | Sort-Object -property Name | Select -Last 1)
    $latestDir = Join-Path (Join-Path $sdkDir $latestDirParent) 'bin\'
    if(-not (Test-Path $latestDir)) {
        return $null
    } else {
        return $latestDir
    }
}


function get_blob($name, $blobContainer) {
    if(($name -eq $null) -or ($blobContainer -eq $null)) {
        return $null
    } else {
        $blobContainer.GetBlobReference($name)
    }
}


function context_blobName($blobName) {
    if($blobName -eq $null) {
        $blobName = $global:store.SelectedBlob
    }
    $blobName
}


function list_servicesXML($cloudURL, $subscriptionID, $cert) {
    call_REST $cloudURL $subscriptionID "services/hostedservices" "GET" $cert
}


function get_serviceXML($name, $cloudURL, $subscriptionID, $cert) {
    $operation = "services/hostedservices/" + $name + "?embed-detail=true"
    call_REST $cloudURL $subscriptionID $operation "GET" $cert
}


function context_serviceName($serviceName) {
    if($serviceName -eq $null) {
        $serviceName = $global:service.Name
    }
    $serviceName
}


function check_service() {
    if(($global:service.Name -ne $null) -and ($global:service.URL -ne $null) -and ($global:service.Location -ne $null)) {
        return $true
    } else {
        show_error "Service not selected"
        return $false
    }
}


function call_REST_wait($cloudURL, $subscriptionID, $operation, $method, $cert, [xml]$body) {
    $request = create_web_request $cloudURL $subscriptionID $operation $cert $method $body
    get_response_wait $request
}


function get_response_wait($request) {
    try {
        $response = $request.GetResponse()
    } catch [System.Exception] {
        show_error $_.Exception.Message
    }

    if($response -eq $null) {
        return ""
    } elseif($null -eq ($longRunningID = $response.Headers.GetValues("x-ms-request-id")[0])) {
        return ""
    } 

    Write-Host "# Waiting for confirmation.." -nonewline
    do {
        if($null -eq ([xml]$xml = call_REST $cloudURL $subscriptionID ("operations/" + $longRunningID) "GET" $cert $null)) {
            return ""
        } 
        
        switch($xml.Operation.Status) {
            "InProgress" { 
                Write-Host "." -nonewline 
            }
            "Failed" { 
                Write-Host 
                return $xml
            }
            "Succeeded" {
                Write-Host
                return $null
            }
        }
           
        Start-Sleep -s 4
    } while($True)    
    #$xml.Save([Console]::Out)
}


function call_REST($cloudURL, $subscriptionID, $operation, $method, $cert, [xml]$body) {
    $request = create_web_request $cloudURL $subscriptionID $operation $cert $method $body
    get_response_xml $request
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
    } elseif($null -eq ($stream = $response.GetResponseStream())) {
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


function show_usage([string[]]$cmdTokens) {
    $format = @{Expression={("# " + $_.Name)};Label="# Entity"}, @{Expression={$_.Value};Label="Actions"}
    if(($cmdTokens -eq $null) -or ($cmdTokens.length -eq 0)) {
        $entity = $null
        $action = $null
    } else {
        $entity = $cmdTokens[0]
        if($cmdTokens.length -gt 1) {
            $action = $cmdTokens[1]
        } else {
            $action = $null
        }
    }
    
    if(($entity -eq $null) -or ($null -eq ($entityDef = $entities.Get_Item($entity)))) {
        # Show entities
        "USAGE: <entity> <action> [<argument>*]"
        $entities.GetEnumerator() | Sort-Object Name | Format-Table -AutoSize -Wrap $format | out-string
    } elseif(($action -eq $null) -or ($null -eq ($actionDef = $entityDef.Get_Item($action)))) {
        # Show entity actions
        $help = $entities.GetEnumerator() | Where-Object {$_.Name -eq $entity} | Format-Table -AutoSize -Wrap $format | Out-String
        show_error $help
    } else {
        # Show entity action parameters
        $format = @{Expression={("# " + $entity)};Label="# Entity"}, @{Expression={$_.Name};Label="Action"}, @{Expression={$_.Value};Label="Arguments"}
        $help = $entityDef.GetEnumerator() | Where-Object {$_.Name -eq $action} | Format-Table -AutoSize -Wrap $format | Out-String
        show_error $help
    }
}
 

function show_error($text) {
    if($text -ne $null) {
        $previousColor = [Console]::ForegroundColor
        [Console]::ForegroundColor = [System.ConsoleColor]::Red
        [Console]::Error.WriteLine("# " + $text)
        [Console]::ForegroundColor = $previousColor
        return
    }        
}


function out_zip([string]$path) { 
    if (-not $path.EndsWith('.zip')) {
        $path += '.zip'
    } 
    
    if (-not (Test-Path $path)) { 
        Set-Content $path ("PK" + [char]5 + [char]6 + ("$([char]0)" * 18)) 
    } 
  
    $app = New-Object -com Shell.Application
    $path = Resolve-Path $path
    $zipFile = $app.NameSpace($path)
   
    if($zipFile -eq $null) {
        show_error "ZIP file path is not valid"
    } else {
        $input | foreach { $zipFile.CopyHere($_.fullname, 4 + 1024) }
    
        # Wait for ZIP file creation to end
        while($zipFile.Items().Count -eq 0) {
            Start-Sleep -s 1
        }
    }
} 



#########################
### Command processor ###
#########################


function process_entity([string[]]$cmdTokens)
{
    # Get entity and action
    if($null -eq $cmdTokens) {
        show_usage
        return
    } elseif($null -eq ($entity = $cmdTokens[0])) {
        show_usage
        return
    } elseif(($entity.StartsWith("#")) -or ([String]::IsNullOrEmpty($entity))) {
        return
    } elseif($entity.StartsWith("--")) {
        show_usage
        return
    } else {
        $action = $cmdTokens[1]
    }
    
    # Find entity & action definition
    if($null -eq ($entityDef = $entities.Get_Item($entity))) {
        show_usage
        return
    } elseif($action -eq $null) {
        $actionDef = $null
    } elseif(($entityDef).GetType().Name -eq "Hashtable") {
        $actionDef = $entityDef.Get_Item($action)
    } else {
        $actionDef = $null
    }
    
    # Construct function name to call
    $functionName = "cmd_" + $entity
    if($actionDef -ne $null) {
        $functionName = $functionName + "_" + $action
    }
    
    # Filter out options
    $cmdOptions = ($cmdTokens | Where-Object { $_.StartsWith("-") })
    $cmdTokens = ($cmdTokens | Where-Object { -not(($_.StartsWith("-")) -or ([String]::IsNullOrEmpty($_.Trim()))) })
    
    # Call the function
    if(Get-Command $functionName -ea SilentlyContinue) {
        & $functionName $cmdTokens $cmdOptions
    } else {
        process_NYI $functionName
        show_usage
    }
}

function process_NYI($functionName) {
    show_error ("Not supported:" + $functionName)
}


function process_commands()
{
    $promptBase = "wash"
    [System.Console]::Out.Write($promptBase + ">")
    [string]$commandLine = $null
    
    while($null -ne ($commandLine = read_commandLine)) {
        $commandLine = ($commandLine.Trim())
        if($commandLine -eq "exit") {
            break
        }
        $cmdTokens = $commandLine.Split(" ")
        $cmdTokens = cmdtokens_quoted $cmdTokens
        
        process_entity $cmdTokens
        $prompt = $promptBase
        
        # Put subscription name in quotes if it contains spaces
        $subname = $global:sub.Name
        if(($null -ne $subname) -and ($subname.Contains(" "))) {
            $subname = '"' + $subname + '"'
        }            
        
        if($global:sub.Id -ne $null) {
            $prompt += " " + $subname
        }
        
        if(($global:store.Name -ne $null) -or ($global:service.Name -ne $null)) {
            $prompt += "/"
        }
        
        # Show current store context
        if($global:store.Name -ne $null) {
            $prompt += "[store:" + $global:store.Name
            if($global:store.SelectedContainer -ne $null) {
                $prompt += "/" + $global:store.SelectedContainer
                if($global:store.SelectedBlob -ne $null) {
                    $prompt += "/" + $global:store.SelectedBlob
                }
            }
            $prompt += "]"
        }
        
        # Show current service context
        if($global:service.Name -ne $null) {
            $prompt += "[service:" + $global:service.Name
            if($global:service.SelectedDeployment -ne $null) {
                $prompt += "/" + $global:service.SelectedDeployment
            }
            $prompt += "]"
        }        
        
        $prompt += ">"
        [Console]::Out.Write($prompt)
    }
    [Console]::Out.WriteLine()
}


function read_commandLine()
{
    return [Console]::In.ReadLine()
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


[string[]]$cmdTokens = @()
foreach($arg in $args) {
    $cmdTokens += $arg
}

$myDir = Split-Path -Parent $MyInvocation.MyCommand.Path

(Get-Host).UI.RawUI.WindowTitle = "WASH v0.1.0"
(Get-Host).UI.RawUI.ForegroundColor = "cyan"

if($cmdTokens.Length -gt 0) {
    process_entity $cmdTokens
} else {
    process_commands
}
