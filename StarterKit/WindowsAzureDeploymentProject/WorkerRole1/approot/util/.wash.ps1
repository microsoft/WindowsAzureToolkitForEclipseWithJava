# wash v0.2.2
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

$global:context = @{
	subscriptionID = $null;
	subscriptionName = $null;
	subscriptionCertificate = $null;
	subscriptionManagementURL = $null;
	publishDataXML = $null;
	storeName = $null;
	storeKey = $null;
	storeURL = $null;
	containerName = $null;
	blobName = $null;
	queueName = $null;
	serviceName = $null;
	serviceURL = $null;
	serviceLocation = $null;
	deploymentName = $null;
}

[xml]$global:publishDataXML

$entities = @{
    "subs" = @{
        "load" = @("<publish-settings-file>");
    }
        
    "sub" = @{
        "use" = @("<subscription-id> | <subscription-name>", "<mgmt-url>");
        "cert" = @("<cert-file>");
    }

    "stores" = @("[store-name-filter]", "[--object]");
    
    "store" = @{
        "use" = @("<name>", "[<key>]", "[""<service-base-url>""]");
        "create" = @("<name>", "<location>");
        "delete" = @("[<name>]");
        "locations" = @("<>");
        "key" = @("[<name>]", "[--notepad]");
        "reset" = @("<>");
    }
    
    "queues" = @("[queue-name-filter]", "[--object]");
    
    "queue" = @{
        "" = @("<queue-name>", "[<account-name>]", "[<account-key>]", "[""<service-base-url>""]");
        "use" =    @("<queue-name>", "[<account-name>]", "[<account-key>]", "[""<service-base-url>""]");
        "create" = @("<queue-name>", "[<account-name>]", "[<account-key>]", "[""<service-base-url>""]");
        "delete" = @("[<queue-name>]", "[<account-name>]", "[<account-key>]", "[""<service-base-url>""]");
        "post" = @("<message>", "[<queue-name>]", "[<account-name>]", "[<account-key>]");
        "peek" = @("[<queue-name>]", "[<account-name>]", "[<account-key>]");
        "get" = @("[<queue-name>]", "[<account-name>]", "[<account-key>]");
        "count" = @("[<queue-name>]", "[<account-name>]", "[<account-key>]");
        "record" = @("[<queue-name>]", "[<account-name>]", "[<account-key>]");
        "watch" = @("[<queue-name>]", "[<account-name>]", "[<account-key>]");
        "reset" = @("<>");
    }
    
    "containers" = @("[container-name-filter]", "[--object]");
    
    "container" = @{
        "use" =    @("<container-name>", "[<account-name>]", "[<account-key>]", "[""<service-base-url>""]");
        "create" = @("<container-name>", "[<account-name>]", "[<account-key>]", "[""<service-base-url>""]");
        "delete" = @("[<container-name>]", "[<account-name>]", "[<account-key>]", "[""<service-base-url>""]");
        "reset" = @("<>");
        "access" = @("container | blob | off | ?", "[<container-name>]", "[<account-name>]", "[<account-key>]", "[""<service-base-url>""]");
    }
    
    "blobs" = @("[blob-name-filter]", "[--object]");
    
    "blob" = @{
        "" = @("[<blob-name>]", "[<container-name>]", "[<account-name>]", "[<account-key>]", "[--lastchanged]");
        "use" = @("[<blob-name>]", "[<container-name>]", "[<account-name>]", "[<account-key>]");
        "delete" = @("[<blob-name>]", "[<container-name>]", "[<account-name>]", "[<account-key>]", "[""<service-base-url>""]");
        "download" = @("<local-path>", "[<blob-name>]", "[<container-name>]", "[<accountName>]", "[<accountKey>]", "[""<service-base-url>""]");
        "url" = @("[<blob-name>]", "[<container-name>]", "[<account-name>]", "[<account-key>]", "[--notepad]");
        "reset" = @("<>");
        "upload" = @("<local-path>", "[<blob-name>]", "[<container-name>]", "[<account-name>]", "[""<account-key>""]", "[""<service-base-url>""]");
    }

    "services" = @("[service-name-filter]", "[--object]");
        
    "service" = @{
        "use" = @("<service-name>");
        "delete" = @("[<service-name>]");
        "location" = @("[<service-name>]");
        "status" = @("[<service-name>]");
        "reset" = @("<>");
    }
    
    "deployments" = @("[deployment-name-filter]", "[--object]");

    "deployment" = @{
        "use" = @("<name>", "[<service-name>]");
        "url" = @("[<name>]", "[<service-name>]", "[--notepad]");
        "reset" = @("<>");
    }
        
    "files" = @{};
    
    "file" = @{
        "download" = @("<URL>", "<local-path>");
        "zip" = @("<src-path>", "<zip-path>");
        "unzip" = @("<zip-path>", "<dest-path>");
        "exists" = @("<url>")
    }
    
    "dir" = @{
        "use" = @("<directory-path>");
        "watch" = @("<directory-path>");
    }
    
    "prompt" = @{
        "[on | off]" = @();
        "out" = @("<text>");
        "short" = @();
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
            Write-ErrorBrief "File not found"
            return
        } elseif((Test-Path ($filename + ".publishsettings")) -eq $False) {
            Write-ErrorBrief "File not found"
            return
        } else {
            $filepath += ".publishsettings"
        }
    }
    
    $global:publishDataXML = [xml](Get-Content $filepath)
    $global:context["subscriptionManagementURL"] = $global:publishDataXML.PublishData.PublishProfile.Url.TrimEnd("/")
    $certBase64 = $global:publishDataXML.PublishData.PublishProfile.ManagementCertificate
    $global:context["subscriptionCertificate"] = Import-Cert $certBase64
}


function cmd_subs([string[]]$cmdTokens) {
    if($global:publishDataXML -eq $null) {
        Write-ErrorBrief "Publish settings file not loaded"
    } else {
        $global:publishDataXML.PublishData.PublishProfile.Subscription | foreach {$_}
    }
}


### Entity: sub ###

function cmd_sub_use([string[]]$cmdTokens) {
    $subID = $cmdTokens[2]
    $mgmtURL = context 'subscriptionManagementURL' $cmdTokens[3]
    
    if(($mgmtURL -eq $null) -or ($subID -eq $null)) {
        show_usage $cmdTokens
        return
    }
    
    # Discover sub ID if known
    if($global:publishDataXML -ne $null) {
        $subs = ($global:publishDataXML.PublishData.PublishProfile.Subscription | Where-Object {($_.Name -like $subID) -or ($_.Id -like $subID)})
        if($subs.Length -ge 2) {
            Write-ErrorBrief "Failed to select a subscription because multiple subscriptions exists with this name. Use the id instead"
            return
        } else {
            $subId = $subs.Id
        }
    }
    
    $global:context["subscriptionManagementURL"] = $mgmtURL.TrimEnd("/")
    $xml = get_subscriptionXML $global:context["subscriptionManagementURL"] $subID $global:context["subscriptionCertificate"]
	$global:context["subscriptionName"] = $xml.Subscription.SubscriptionName
    $global:context["subscriptionID"] = $xml.Subscription.SubscriptionID
}


function cmd_sub([string[]]$cmdTokens) {
    if($null -eq $global:context["subscriptionID"]) {
        show_usage $cmdTokens
    } elseif($cmdTokens.length -gt 1) {
        show_usage $cmdTokens
    } else {
        Out-Host -inputObject $global:context["subscriptionName"]
    }
}


function cmd_sub_cert([string[]]$cmdTokens) {
    if($null -ne ($certfile = $cmdTokens[2])) {
        $certBase64 = Get-Content $certfile
        $global:context["subscriptionCertificate"] = Import-Cert $certBase64
    } 
            
    if($global:context["subscriptionCertificate"] -eq $null) {
        show_usage $cmdTokens
    } else {
        Out-Host -inputObject $global:context["subscriptionCertificate"]
    }
}


### Entity: store ###

function cmd_stores([string[]]$cmdTokens, [string[]]$cmdOptions) {
	$mgmtURL = $global:context["subscriptionManagementURL"]
	$subID = $global:context["subscriptionID"]
	$cert = $global:context["subscriptionCertificate"]
	if($null -eq ($filter = $cmdTokens[1])) {
        $filter = "*"
    }

	if(-not (check_sub)) {
        return
	}
	
	$stores = list_stores $mgmtURL $subID $cert | Where-Object {$_.ServiceName -like $filter}
	if($cmdOptions -contains "--object") {
        list_stores $mgmtURL $subID $cert | Where-Object {$_.ServiceName -like $filter}
    } else {
        list_stores $mgmtURL $subID $cert | Where-Object {$_.ServiceName -like $filter} | % { $_.ServiceName}
    }
}


function cmd_store([string[]]$cmdTokens) {
    if(($null -eq $global:context["storeName"]) -or ($null -eq $global:context["storeKey"])) {
        show_usage $cmdTokens
    } elseif($cmdTokens.length -gt 1) {
        show_usage $cmdTokens
    } else  {
        Out-Host -inputObject $global:context["storeName"]
    }
}


function cmd_store_use([string[]]$cmdTokens) {
    $name = context 'storeName' $cmdTokens[2]
    $key  = $cmdTokens[3]
	$baseURL = context 'storeURL' $cmdTokens[4]
    
    if($name -eq $null) {
        show_usage $cmdTokens
    } elseif(($null -ne $key) -and ($null -ne $baseURL)) {
        select_store $name $key $baseURL
    } elseif(!(check_sub)) {
        return
    } elseif($null -eq ($name = resolve_storeName $name)) {
        Write-ErrorBrief "Failed to find this storage account"
    } else {
		if($null -eq $baseURL) {
			$baseURL = get_storageURL $global:context["subscriptionManagementURL"] $global:context["subscriptionID"] $global:context["subscriptionCertificate"] $name
		}
		if($null -eq $key) {
        	$key = get_storageKey $global:context["subscriptionManagementURL"] $global:context["subscriptionID"] $global:context["subscriptionCertificate"] $name
		}
        select_store $name $key $baseURL
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
    } elseif($True -eq (create_storage $accountName $location $global:context["subscriptionManagementURL"] $global:context["subscriptionID"] $global:context["subscriptionCertificate"])) {
        select_store $accountName
    } else {
        Write-ErrorBrief "Failed to create storage account"  
    }
}


function cmd_store_delete([string[]]$cmdTokens) {
    $accountName = context 'storeName' $cmdTokens[2]
            
    if($accountName -eq $null) {
        show_usage $cmdTokens
    } elseif(check_sub) {
        $operation = "services/storageServices/" + $accountName
        try {
            Call-Subscription $global:context["subscriptionManagementURL"] $global:context["subscriptionID"] $operation "DELETE" $global:context["subscriptionCertificate"]
            select_store $null $null
        } catch {
            Write-ErrorBrief "Failed to delete storage account"
        }
    }
}


function cmd_store_locations([string[]]$cmdTokens) {
    if(-not (check_sub)) {
        return
    } elseif($null -eq ($locationsXML = (list_locationsXML $global:context["subscriptionManagementURL"] $global:context["subscriptionID"] $global:context["subscriptionCertificate"]))) {
        Write-ErrorBrief "Failed to get the storage locations list"
    } else {
        $locationsXML.Locations.Location | foreach { $_.Name }
    }
}


function cmd_store_key([string[]]$cmdTokens, [string[]]$cmdOptions) {
    $name = context 'storeName' $cmdTokens[2]
            
    if($name -eq $null) {
        show_usage $cmdTokens
    } elseif($null -eq ($name = resolve_storeName $name)) {
        Write-ErrorBrief "Failed to find this storage account"
    } elseif (-not(check_sub)) {
        return
    } elseif($null -eq ($key = get_storageKey $global:context["subscriptionManagementURL"] $global:context["subscriptionID"] $global:context["subscriptionCertificate"] $name)) {
        Write-ErrorBrief "Failed to get the key for this storage account"
    } else {
        if($name -eq $global:context["storeName"]) {
            $global:context["storeKey"] = $key
        }

        # Option: --notepad
        if($cmdOptions -contains "--notepad") {
            $key | Out-Notepad
        } 
        
        # Option: --copy
        if($cmdOptions -contains "--copy") {
            $key | Out-Clipboard
        }
        
        $key
    }
}


### Entity: queue ###

function cmd_queues([string[]]$cmdTokens, [string[]]$cmdOptions) {
	list_storage_entities $cmdTokens $cmdOptions 'queue'
}


function cmd_queue([string[]]$cmdTokens) {
    $queueName = context 'queueName' $cmdTokens[1]
    $accountName = context 'storeName' $cmdTokens[2]
    $accountKey = context 'storeKey' $cmdTokens[3]
	$baseURL = context 'storeURL' $cmdTokens[4]
    
    if(!$queueName -or !$accountName -or !$accountKey -or !$baseURL) {
        return $null
    } elseif($null -eq  ($queue = get_queue $queueName $accountName $accountKey $baseURL)) {
        Write-ErrorBrief "Failed to access the queue"
    } else {
        $queue
    }
}


function cmd_queue_create([string[]]$cmdTokens) {
    $queueName = $cmdTokens[2]
    
    $cmdArgs = $cmdTokens[1..$cmdTokens.Length]
    if($null -eq ($queue = cmd_queue $cmdArgs)) {
        Write-ErrorBrief "Failed to create the queue"
    } else {
        try {
            [void]$queue.CreateIfNotExist()
        } catch {
            Write-ErrorBrief "Failed to create the queue"
        }
    }
}


function cmd_queue_delete([string[]]$cmdTokens) {
    $cmdArgs = $cmdTokens[1..$cmdTokens.Length]
    if($null -eq ($queue = cmd_queue $cmdArgs)) {
        Write-ErrorBrief "Failed to create the queue"
    } else {
        try {
            $queueName = $queue.Name
            $queue.Delete()
            if($queueName -eq $global:context["queueName"]) {
                $global:context["queueName"] = $null
            }
        } catch {
            Write-ErrorBrief "Failed to delete the queue"
        }
    }
}


function cmd_queue_use([string[]]$cmdTokens) {
    $name = $cmdTokens[2] 
    $accountName = context 'storeName' $cmdTokens[3]
    $accountKey = context 'storeKey' $cmdTokens[4]
	$baseURL = context 'storeURL' $cmdTokens[5]
            
    if(!$name -or !$accountName -or !$accountKey -or !$baseURL) {
        show_usage $cmdTokens
    } elseif($null -eq ($name = resolve_queueName $name $accountName $accountKey $baseURL)) {
        Write-ErrorBrief "Failed to access the queue"
    } else {
        if($global:context["queueName"] -ne $name) {
            $global:context["queueName"] = $null
        }
        
        $global:context["queueName"] = $name
		$global:context["storeName"] = $accountName
		$global:context["storeKey"] = $accountKey
		$global:context["storeURL"] = $baseURL
    } 
}


function cmd_queue_reset([string[]]$cmdTokens) {
    $global:context["queueName"] = $null
}


function cmd_queue_post([string[]]$cmdTokens) {
    $message = $cmdTokens[2]
    $filter = $cmdTokens[3]
    $cmdArgs = $cmdTokens[1..$cmdTokens.Length]
    $cmdOptions = @("--object")
    $cmdArgs2 = $cmdTokens[2..$cmdTokens.Length]
    
    $queues = @()
    if($null -eq $message) {
        show_usage $cmdTokens
        return
    } 
    
    if($filter -ne $null) {
        $queues = @(cmd_queues $cmdArgs $cmdOptions)
    }
    
    if($queues.Length -gt 0) {
        # Skip
    } elseif($null -eq ($queue = cmd_queue $cmdArgs2)) {
        Write-ErrorBrief "Failed to access the selected queues"
        return
    } else {
        $queues += $queue
    }
    
	foreach($queue in $queues) {
		$queue.EncodeMessage = $true
		$msgObject = New-Object Microsoft.WindowsAzure.StorageClient.CloudQueueMessage($message)
		$queue.AddMessage($msgObject)
	}
}


function cmd_queue_peek([string[]]$cmdTokens) {
    $cmdArgs = $cmdTokens[1..$cmdTokens.Length]
    if($null -eq ($queue = cmd_queue $cmdArgs)) {
        Write-ErrorBrief "Failed to access the queue"
    } else {
        try {
            Write-Output $queue.PeekMessage().AsString
        } catch {
            Write-ErrorBrief "Failed to peek a message on the queue"
        }
    }
}


function cmd_queue_count([string[]]$cmdTokens) {
    $cmdArgs = $cmdTokens[1..$cmdTokens.Length]
    if($null -eq ($queue = cmd_queue $cmdArgs)) {
        Write-ErrorBrief "Failed to access the queue"
    } else {
        try {
            Write-Output $queue.RetrieveApproximateMessageCount()
        } catch {
            Write-ErrorBrief "Failed to get an approximate message count"
        }
    }
}


function cmd_queue_get([string[]]$cmdTokens, [string[]]$cmdOptions) {
    $cmdArgs = $cmdTokens[1..$cmdTokens.Length]
    if($null -eq ($queue = cmd_queue $cmdArgs)) {
        Write-ErrorBrief "Failed to access the queue"
    } else {
        try {
            $timeSpan = New-Object System.TimeSpan(1)            
            $msgObject = $queue.GetMessage($timeSpan)
            if($msgObject -eq $null) {
                return
            } elseif($cmdOptions -contains "--object") {
                Write-Output $msgObject
            } else {
                Write-Output $msgObject.AsString
            }
            $queue.DeleteMessage($msgObject)            
        } catch {
            Write-ErrorBrief "Failed to get a message"
        }
    }
}


function cmd_queue_watch([string[]]$cmdTokens) {
    $maxBackoff = 10.0
    $startBackoff = 1.0
    $backoffRate = 1.5
    $backoff = $startBackoff
    while($true) {
        $message = cmd_queue_get $cmdTokens
        if($message -eq $null) {
            Start-Sleep -s $backoff
            $backoff *= $backoffRate
            if($backoff -gt $maxBackoff) {
                $backoff = $maxBackoff
            }
        } else {
            Write-Output $message
            $backoff = $startBackoff
        }
    }
}


function cmd_queue_record([string[]]$cmdTokens) {
    $cmdArgs = $cmdTokens[1..$cmdTokens.Length]
    if($null -eq ($queue = cmd_queue $cmdArgs)) {
        Write-ErrorBrief "Failed to access the queue"
    } else {
        while($null -ne ($message = read_commandLine)) {
            try {
                $queue.EncodeMessage = $true
                $msgObject = New-Object Microsoft.WindowsAzure.StorageClient.CloudQueueMessage($message)
                $queue.AddMessage($msgObject)
            } catch {
                Write-ErrorBrief "Failed to add a message to the queue"
            }
        }
    }
}


### Entity: container ###

function cmd_containers([string[]]$cmdTokens, [string[]]$cmdOptions) {
    list_storage_entities $cmdTokens $cmdOptions 'container'
}


function cmd_container([string[]]$cmdTokens) {
    $accountName = context 'storeName' $cmdTokens[2]
    $accountKey = context 'storeKey' $cmdTokens[3]
	$baseURL = context 'storeURL' $cmdTokens[4]
    $containerName = context_containerName $cmdTokens[1] $accountName $accountKey $baseURL
    
    if($null -eq $containerName) {
        Write-ErrorBrief "Failed to find this container"
    } elseif($accountName -and $accountKey -and $baseURL) {
        get_container $containerName $accountName $accountKey $baseURL
	} else {
        Write-ErrorBrief "Failed to open a connection with the storage service"
    }
}


function cmd_container_use([string[]]$cmdTokens) {
    $name = $cmdTokens[2]
    $accountName = context 'storeName' $cmdTokens[3]
    $accountKey = context 'storeKey' $cmdTokens[4]
	$baseURL = context 'storeURL' $cmdTokens[5]
    
    if(!$name -or !$accountName -or !$accountKey -or !$baseURL) {
        show_usage $cmdTokens
    } elseif($null -eq ($name = resolve_containerName $name $accountName $accountKey $baseURL)) {
        Write-ErrorBrief "Failed to find this container"        
    } else {
        if($global:context["containerName"] -ne $name) {
            $global:context["blobName"] = $null
        }
        
        $global:context["containerName"] = $name        
		$global:context["storeName"] = $accountName
		$global:context["storeKey"] = $accountKey
		$global:context["storeURL"] = $baseURL
    } 
}


function cmd_container_reset([string[]]$cmdTokens) {
    $global:context["blobName"] = $null
    $global:context["containerName"] = $null
}


function cmd_container_create([string[]]$cmdTokens) {
    $containerName = $cmdTokens[2]
    $accountName = context 'storeName' $cmdTokens[3]
    $accountKey = context 'storeKey' $cmdTokens[4]
	$baseURL = context 'storeURL' $cmdTokens[5]
    
    if(!$containerName -or !$accountName -or !$accountKey) {
        Write-ErrorBrief "Storage account missing"
    } elseif($null -eq ($container = get_container $containerName $accountName $accountKey $baseURL)) {
        Write-ErrorBrief "Failed to create the container"
    } else {
        try {
            [void]$container.CreateIfNotExist()
        } catch {
            Write-ErrorBrief "Failed to create the container"
        }
    }
}


function cmd_container_delete([string[]]$cmdTokens) {
    $cmdArgs = $cmdTokens[1..$cmdTokens.Length]
    if($null -eq ($container = cmd_container $cmdArgs)) {
        Write-ErrorBrief "Failed to access the container"
    } else {
		$containerName = $container.Name
		$container.Delete()
		if($containerName -eq $global:context["containerName"]) {
			$global:context["containerName"] = $null
			$global:context["blobName"] = $null
		}
    }
}


function cmd_container_access([string[]]$cmdTokens) {
    [string]$access = $cmdTokens[2]
    $cmdArgs = $cmdTokens[2..$cmdTokens.Length]
    if($null -eq ($container = cmd_container $cmdArgs)) {
        Write-ErrorBrief "Failed to access the container"
    } elseif($null -eq ($perms = $container.GetPermissions())) {
        Write-ErrorBrief "Failed to read container access permissions"
	} elseif(($access -eq '?') -or !$access) {
        ([string]($perms.PublicAccess)).ToLower()		
    } elseif(@("container", "off", "blob") -notcontains $access) {
        show_usage $cmdTokens
    } else {
        $perms = New-Object Microsoft.WindowsAzure.StorageClient.BlobContainerPermissions
		$perms.PublicAccess = $access.ToLower()
		$container.SetPermissions($perms)
    }
}


### Entity: blob ###

function cmd_blobs([string[]]$cmdTokens, [string[]]$cmdOptions) {
    list_storage_entities $cmdTokens $cmdOptions 'blob'
}


function cmd_blob([string[]]$cmdTokens, [string[]]$cmdOptions) {
    $blobName = context 'blobName' $cmdTokens[1]
    $containerName = context_containerName $cmdTokens[2]
    $accountName = context 'storeName' $cmdTokens[3]
    $accountKey = context 'storeKey' $cmdTokens[4]
    
    if(!$blobName -or !$containerName -or !$accountName -or !$accountKey) {
        return $null
    } elseif($null -eq ($container = get_container $containerName $accountName $accountKey)) {
        Write-ErrorBrief "Container not selected"
    } elseif($null -eq  ($blob = $container.GetBlobReference($blobName))) {
        Write-ErrorBrief "Failed to access the blob"
    } elseif($cmdOptions -contains "--lastchanged") {
        $blob.FetchAttributes()
        return $blob.Properties.LastModifiedUtc
    } else {
        return $blob
    }
}


function cmd_blob_use([string[]]$cmdTokens) {
    $blobName = $cmdTokens[2]
    $accountName = context 'storeName' $cmdTokens[4]
    $accountKey = context 'storeKey' $cmdTokens[5]
	$baseURL = context 'storeURL' $cmdTokens[6]
    $containerName = context_containerName $cmdTokens[3] $accountName $accountKey $baseURL
            
    if(!$blobName -or !$containerName) {
        show_usage $cmdTokens
    } elseif(!$accountName -or !$accountKey) {
        Write-ErrorBrief "Storage account not selected"
    } elseif($null -eq ($containerName = resolve_containerName $containerName $accountName $accountKey $baseURL)) {
        Write-ErrorBrief "Failed to find this container"        
    } elseif($null -eq ($blobName = (resolve_blobName $blobName $containerName $accountName $accountKey $baseURL))) {
        Write-ErrorBrief "Failed to find this blob"        
    } else {
        $global:context["blobName"] = $blobName
        $global:context["containerName"] = $containerName
		$global:context["storeName"] = $accountName
		$global:context["storeKey"] = $accountKey
		$global:context["storeURL"] = $baseURL
    }
}


function cmd_blob_upload([string[]]$cmdTokens) {
    [string]$filepath = $cmdTokens[2]
    [string]$blobName = $cmdTokens[3]
	$containerName = context 'containerName' $cmdTokens[4]
    $accountName = context 'storeName' $cmdTokens[5]
    $accountKey = context 'storeKey' $cmdTokens[6]
    $baseURL = context 'storeURL' $cmdTokens[7]
	
    if(($filepath -eq $null)) {
        show_usage $cmdTokens
        return
    } elseif((Test-Path $filepath) -eq $False) {
        Write-ErrorBrief "File not found"
        return
    } elseif($null -eq ($container = get_container $containerName $accountName $accountKey $baseURL)) {
        show_usage $cmdTokens
        return
    } 

	$paths = @(Resolve-Path $filepath)
    if($null -ne $blobName) {
        $paths= $paths[0..0]
    }

    foreach($filepath in $paths) {
        $blobName = Split-Path $filepath -Leaf
        if($null -eq ($blob = $container.GetBlobReference($blobName))) {
            Write-ErrorBrief "Failed to upload '$filepath'"
        } else {
            try {
                [void]$blob.UploadFile($filepath)
            } catch {
				Write-Error $_
                Write-ErrorBrief "Failed to upload '$filepath'"
            }
        }
    }
}


function cmd_blob_reset([string[]]$cmdTokens) {
    $global:context["blobName"] = $null
}


function cmd_blob_delete([string[]]$cmdTokens) {
    $blobName = context 'blobName' $cmdTokens[2]
    $accountName = context 'storeName' $cmdTokens[4]
    $accountKey = context 'storeKey' $cmdTokens[5]
	$baseURL = context 'storeURL' $cmdTokens[6]
    $containerName = context_containerName $cmdTokens[3] $accountName $accountKey $baseURL

    if($null -eq ($container = get_container $containerName $accountName $accountKey $baseURL)) {
        show_usage $cmdTokens
    } elseif($null -eq ($blob = $container.GetBlobReference($blobName))) {
        show_usage $cmdTokens
    } else {
        $blob.Delete()
        if($blobName -eq $global:context["blobName"]) {
            $global:context["blobName"] = $null
        }
    }
}


function cmd_blob_download([string[]]$cmdTokens) {
    $filepath = $cmdTokens[2]
    $blobName = context 'blobName' $cmdTokens[3]
    $accountName = context 'storeName' $cmdTokens[5]
    $accountKey = context 'storeKey' $cmdTokens[6]
	$baseURL = context 'storeURL' $cmdTokens[7]
    $containerName = context_containerName $cmdTokens[4] $accountName $accountKey $baseURL

    if($null -eq $filepath) {
        $filepath = $blobName
    } elseif($null -eq $blobName) {
        $blobName = Split-Path $filepath -Leaf
    }
            
    if($null -eq $filepath) {
        show_usage $cmdTokens
    } elseif($null -eq ($containerName = resolve_containerName $containerName $accountName $accountKey $baseURL)) {
        Write-ErrorBrief "Failed to find this container"
    } elseif($null -eq ($container = get_container $containerName $accountName $accountKey $baseURL)) {     
        show_usage $cmdTokens
    } elseif($null -eq ($blob = $container.GetBlobReference($blobName))) {
        show_usage $cmdTokens
    } else {
        if(Test-Path $filepath) {
            Remove-Item $filepath
        }
        
        try {
            $blob.DownloadToFile($filepath)
        } catch {
            Write-ErrorBrief "Blob download failed"
			Remove-Item $filepath
        }
    }
}


function cmd_blob_url([string[]]$cmdTokens) {
    $blobName = context 'blobName' $cmdTokens[2]
    $accountName = context 'storeName' $cmdTokens[4]
    $accountKey = context 'storeKey' $cmdTokens[5]
    $containerName = context_containerName $cmdTokens[3] $accountName $accountKey

    if($null -eq ($container = get_container $containerName $accountName $accountKey)) {
        show_usage $cmdTokens
    } elseif($null -eq ($blobName = (resolve_blobName $blobName $containerName $accountName $accountKey))) {
        Write-ErrorBrief "Failed to find this blob"
    } elseif($null -eq ($blob = $container.GetBlobReference($blobName))) {
        show_usage $cmdTokens
    } else {
        $uri = $blob.Uri.AbsoluteUri
        
        # Option: --notepad
        if($cmdOptions -contains "--notepad") {
            $uri | Out-Notepad
        } 
        
        # Option: --copy
        if($cmdOptions -contains "--copy") {
            $uri | Out-Clipboard
        }
        
        $uri
    }
}


### Entity: service ###

function cmd_services([string[]]$cmdTokens, [string[]]$cmdOptions) {
    $mgmtURL = $global:context["subscriptionManagementURL"]
	$subID= $global:context["subscriptionID"]
	$cert = $global:context["subscriptionCertificate"]
	if($null -eq ($filter = $cmdTokens[1])) {
        $filter = "*"
    }

	if(-not (check_sub)) {
        return
	}
	
    if($cmdOptions -contains "--object") {
        list_services $mgmtURL $subID $cert | Where-Object {$_.ServiceName -like $filter}
    } else {
        list_services $mgmtURL $subID $cert | Where-Object {$_.ServiceName -like $filter} | foreach { $_.ServiceName }
    }
}


function cmd_service([string[]]$cmdTokens) {
    if(check_service) {
        Out-Host -inputObject $global:context["serviceName"]
    } else {
        show_usage $cmdTokens
    }
}


function cmd_service_use([string[]]$cmdTokens) {
    $name = $cmdTokens[2]
    $mgmtURL = $global:context["subscriptionManagementURL"] 
    $subID = $global:context["subscriptionID"] 
    $cert = $global:context["subscriptionCertificate"]
    
    if(!(check_sub)) {
        return
    } elseif($null -eq $name) {
        show_usage $cmdTokens
    } elseif($null -eq ($name = resolve_serviceName $name $mgmtURL $subID $cert)) {
        Write-ErrorBrief "Failed to find this service"
    } elseif ($null -eq ($xml = get_serviceXML $name $mgmtURL $subID $cert)) {
        Write-ErrorBrief "Cannot select this service"
    } else {        
        $global:context["serviceName"] = $name
        $global:context["serviceURL"] = $xml.HostedService.Url
        $global:context["serviceLocation"] = $xml.HostedService.HostedServiceProperties.Location
    }
}


function cmd_service_reset([string[]]$cmdTokens) {
    $global:context["serviceName"] = $null
    $global:context["serviceURL"] = $null
    $global:context["serviceLocation"] = $null
    $global:context["deploymentName"] = $null
}


function cmd_service_delete([string[]]$cmdTokens) {
    $name = context "serviceName" $cmdTokens[2]
    if($name -eq $null) {
        show_usage $cmdTokens
    } elseif(check_sub) {
        $operation = "services/hostedservices/" + $name
        try {
            Call-Subscription $global:context["subscriptionManagementURL"] $global:context["subscriptionID"] $operation "DELETE" $global:context["subscriptionCertificate"]
            if($name -eq $global:context["serviceName"]) {
                $global:context["serviceName"] = $null
                $global:context["serviceURL"] = $null
                $global:context["serviceLocation"] = $null
            }
        } catch {
            Write-ErrorBrief "Failed to delete service"
        }
    }
}


function cmd_service_location([string[]]$cmdTokens) {
    $mgmtURL = $global:context["subscriptionManagementURL"]
    $subID = $global:context["subscriptionID"]
    $cert = $global:context["subscriptionCertificate"]
    
    if($null -eq ($name = context "serviceName" $cmdTokens[2])) {
        show_usage $cmdTokens
    } elseif(!(check_sub)) {
        return
    } elseif($null -eq ($name = resolve_serviceName $name $mgmtURL $subID $cert)) {
        Write-ErrorBrief "Failed to find this service"
    } elseif(($global:context["serviceLocation"] -ne $null) -and ($name -eq $global:context["serviceName"])) {
        $global:context["serviceLocation"]
    } elseif($null -ne ($xml = get_serviceXML $name $mgmtURL $subID $cert)) {
        $xml.HostedService.HostedServiceProperties.Location
    }
}


function cmd_service_status([string[]]$cmdTokens) {
    $mgmtURL = $global:context["subscriptionManagementURL"]
    $subID = $global:context["subscriptionID"]
    $cert = $global:context["subscriptionCertificate"]
    
    if($null -eq ($name = context "serviceName" $cmdTokens[2])) {
        show_usage $cmdTokens
    } elseif(!(check_sub)) {
        return
    } elseif($null -eq ($name = resolve_serviceName $name $mgmtURL $subID $cert)) {
        Write-ErrorBrief "Failed to find this service"
    } elseif($null -ne ($xml = get_serviceXML $name $mgmtURL $subID $cert)) {
        $xml.HostedService.HostedServiceProperties.Status
    }
}


### Entity: deployment ###

function cmd_deployments([string[]]$cmdTokens, [string[]]$cmdOptions) {
	$mgmtURL = $global:context["subscriptionManagementURL"]
	$subID= $global:context["subscriptionID"]
	$cert = $global:context["subscriptionCertificate"]
	$name = $global:context["serviceName"]
	if($null -eq ($filter = $cmdTokens[1])) {
        $filter = "*"
    }

	if(!(check_sub)) {
        return
    } elseif($cmdOptions -contains "--object") {
        list_deployments $name $mgmtURL $subID $cert | Where-Object {$_.Name -like $filter}
    } else {
		list_deployments $name $mgmtURL $subID $cert | Where-Object {$_.Name -like $filter} | % { $_.Name}
	}
}


function cmd_deployment_use([string[]]$cmdTokens) {
    $name = $cmdTokens[2]
    $serviceName = context "serviceName" $cmdTokens[3]
    $mgmtURL = $global:context["subscriptionManagementURL"] 
    $subID = $global:context["subscriptionID"] 
    $cert = $global:context["subscriptionCertificate"]
    
    if(!(check_sub)) {
        return
    } elseif(($null -eq $name) -or ($null -eq $serviceName)) {
        show_usage $cmdTokens
        return
    } elseif($null -eq ($serviceName = resolve_serviceName $serviceName $mgmtURL $subID $cert)) {
        Write-ErrorBrief "Failed to find this service"
    } elseif($null -eq ($name = resolve_deploymentName $name $serviceName $mgmtURL $subID $cert)) {
        Write-ErrorBrief "Failed to find this deployment"
    } elseif($null -eq ($xml = get_deploymentXML $name $serviceName $mgmtURL $subID $cert)) {
        Write-ErrorBrief "Failed to select the deployment"
    } else {
        $global:context["serviceName"] = $serviceName
        $global:context["deploymentName"] = $name
    }
}


function cmd_deployment_reset([string[]]$cmdTokens) {
    $global:context["deploymentName"] = $null    
}


function cmd_deployment_url([string[]]$cmdTokens, [string[]]$cmdOptions) {
    $name = context "deploymentName" $cmdTokens[2]
    $serviceName = context "serviceName" $cmdTokens[3]
    $mgmtURL = $global:context["subscriptionManagementURL"] 
    $subID = $global:context["subscriptionID"] 
    $cert = $global:context["subscriptionCertificate"]
    
    if(!(check_sub)) {
        return
    } elseif(($null -eq $name) -or ($null -eq $serviceName)) {
        show_usage $cmdTokens
        return
    } elseif($null -eq ($serviceName = resolve_serviceName $serviceName $mgmtURL $subID $cert)) {
        Write-ErrorBrief "Failed to find this service"
    } elseif($null -eq ($name = resolve_deploymentName $name $serviceName $mgmtURL $subID $cert)) {
        Write-ErrorBrief "Failed to find this deployment"
    } elseif($null -eq ($xml = get_deploymentXML $name $serviceName $mgmtURL  $subID $cert)) {
        Write-ErrorBrief "Failed to get the URL"
    } else {
        $url = $xml.Deployment.Url
        
        # Option: --notepad
        if($cmdOptions -contains "--notepad") {
            $url | Out-Notepad
        }
        
        # Option: --browser
        if($cmdOptions -contains "--browser") {
            Start-Process $url
        } 
        
        # Option: --copy
        if($cmdOptions -contains "--copy") {
            $url | Out-Clipboard
        }
        
        $url        
    }
}


### Entity: file ###

function cmd_file_download([string[]]$cmdTokens) {
	Download-File $cmdTokens[2] $cmdTokens[3]
}


function cmd_file_exists([string[]]$cmdTokens) {
	Test-URL $cmdTokens[2]
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
        ls $srcPath | Out-ZIP $zipPath
    } else {
        # zip directory
        Get-Item ($srcPath + '\\.') | Out-ZIP $zipPath
    }
}


function cmd_file_unzip([string[]]$cmdTokens) {
    $zipPath = $cmdTokens[2]
    $destPath = $cmdTokens[3]
    
    if(($zipPath -eq $null) -or ($destPath -eq $null)) {
        show_usage $cmdTokens
        return
    } elseif(-not (Test-Path $zipPath)) {
        Write-ErrorBrief "Failed to find the ZIP file"
        return
    }
    
    $app = New-Object -com Shell.Application
    [string]$zipPath = Resolve-Path $zipPath
    $zipFile = $app.NameSpace($zipPath)
    [string]$destPath = Resolve-Path $destPath
    $destFile = $app.NameSpace($destPath)

    if($destFile -eq $null) {
        Write-ErrorBrief "Failed to open destination folder"
    } elseif($zipFile -eq $null) {
        Write-ErrorBrief "Failed to open the ZIP file"
    } else {
        $destFile.CopyHere($zipFile.Items(), 20) 2> err.txt #####
    }
    
    $destFile = $null
    $zipFile = $null
    $app = $null
}


### Entity: dir ###

function cmd_dir([string[]]$cmdTokens) {
    dir
}


function cmd_dir_use([string[]]$cmdTokens) {
    select_directory $cmdTokens[2]
}


function cmd_cd([string[]]$cmdTokens) {
    select_directory $cmdTokens[1]
}


function cmd_dir_watch([string[]]$cmdTokens) {
    [string]$path = context_dirName $cmdTokens[2]
    [string]$filter = $cmdTokens[3]
    [string]$watcherID = ([string](Get-Random) + ".")
    
    $path = [System.Environment]::ExpandEnvironmentVariables($path)
    if(-not(Test-Path $path)) {
        Write-ErrorBrief "Failed to find path $path"
        return
    }
        
    [string]$pathID = ($watcherID + (Get-Random))
    $path = Resolve-Path $path
        
    $watcher = New-Object System.IO.FileSystemWatcher
    $watcher.Path = $path
    $watcher.IncludeSubdirectories = $true
    if($filter -ne $null) {
        $watcher.Filter = $filter
    }
    $watcher.EnableRaisingEvents = $true

    @("Changed", "Created", "Deleted", "Renamed") | % { Register-ObjectEvent $watcher $_ -SourceIdentifier "$pathID.$_" }        
    $dirWatchers += $watcher
    
    # Wait for event
    while($true) {
        $event = Wait-Event -SourceIdentifier "$pathID.*"
        [string]$eventType = $event.SourceIdentifier.Split(".")[-1]        
        Write-Output ($eventType + ", " + $event.SourceEventArgs.FullPath)
        Remove-Event -SourceIdentifier $event.SourceIdentifier
    }
}


function cmd_files([string[]]$cmdTokens) {
    dir
}


### Entity: prompt ###

function cmd_prompt([string[]]$cmdTokens) {
    [string]$state = $cmdTokens[1]
    if([String]::IsNullOrEmpty($state)) {
        $global:showPrompt
    } elseif(@("on", "off", "short") -notcontains $state) {
        show_usage $cmdTokens
    } else {
        $global:showPrompt = $state.ToLower()
    }
}


function cmd_prompt_out([string[]]$cmdTokens) {
    $text = $cmdTokens[2]
    if($text -eq $null) {
        Write-Output (get_prompt)
    } else {
        Write-Output $text
    }
}


#########################
### Utility functions ###
#########################

function list_storage_entities([string[]]$cmdTokens, [string[]]$cmdOptions, [string]$entityType) {
    $accountName = $global:context["storeName"]
    $accountKey = $global:context["storeKey"]
	$containerName = $global:context["containerName"]
	$baseURL = $global:context["storeURL"]
	
	if($null -eq ($filter = $cmdTokens[1])) {
        $filter = "*"
    }

	$entityListFunctionMap = @{
		'container' = 'list_containers';
		'queue' = 'list_queues'
		'blob' = 'list_blobs'
	}
	
	if($null -eq ($listFunction = $entityListFunctionMap[$entityType])) {
		# Error?
	} elseif(!(Get-Command $functionName -ea SilentlyContinue)) {
		# Error?
	} elseif($cmdOptions -contains "--object") {
		& $listFunction $accountName $accountKey $baseURL $containerName | Where-Object {$_ -ne $null} | Where-Object {$_.Name -like $filter}
    } else {
        & $listFunction $accountName $accountKey $baseURL $containerName | Where-Object {$_ -ne $null} | Where-Object {$_.Name -like $filter} | foreach { $_.Name }
    }	
}


function list_containers([string]$accountName, [string]$accountKey, [string]$baseURL) {
    if($null -ne ($blobClient = get_blob_client $accountName $accountKey $baseURL)) {
		$blobClient.ListContainers()
	}
}


function list_queues($accountName, $accountKey, $baseURL) {
    if($null -ne ($queueClient = get_queue_client $accountName $accountKey $baseURL)) {
        $queueClient.ListQueues()
    }
}


function list_blobs($accountName, $accountKey, $baseURL, $containerName) {
	if($null -ne ($container = get_container $containerName $accountName $accountKey $baseURL)) {
        load_storage_client
        $options = New-Object Microsoft.WindowsAzure.StorageClient.BlobRequestOptions
        $options.UseFlatBlobListing = $true;
        $container.ListBlobs($options) | Where-Object { $_ -ne $null }
    }
}


function select_directory($path) {
    if($null -eq $path) {
        $(Get-Location).Path
    } elseif(Test-Path $path) {
        cd -path $path
    } else {
        Write-ErrorBrief "Failed to select the requested path"
    } 
}


function select_store($name, $key, $baseURL) {
    if(($name -eq $null) -or ($key -eq $null)) {
        $global:context["storeName"] = $null
        $global:context["storeKey"] = $null
		$global:context["storeURL"] = $null
    } else {
        $global:context["storeName"] = $name
        $global:context["storeKey"] = $key
		$global:context["storeURL"] = $baseURL
    }
    $global:context["containerName"] = $null
    $global:context["blobName"] = $null
}


function context($key, $default) {
	if($default -eq $null) {
		$default = $global:context[$key]
	}
	$default
}


function context_containerName($containerName, [string]$accountName, [string]$accountKey, [string]$baseURL) {
    if($containerName -eq $null) {
        $global:context["containerName"]
    } elseif($accountName -and $accountKey) {
        resolve_containerName $containerName $accountName $accountKey $baseURL
    }
}


function context_dirName($dirName) {
    if($dirName -eq $null) {
        $dirName = $(Get-Location).Path
    }
    $dirName
}


function resolve_serviceName($serviceName, $mgmtURL, $subID, $cert) {
    (list_services $mgmtURL $subID $cert) | Where-Object { $_.ServiceName -like $serviceName } | Select-Object -ExpandProperty ServiceName -first 1
}

function resolve_deploymentName($deploymentName, $serviceName, $mgmtURL, $subID, $cert) {
    (list_deployments $serviceName $mgmtURL $subID $cert) | Where-Object { $_.Name -like $deploymentName } | Select-Object -ExpandProperty Name -first 1
}

function resolve_storeName($storeName) {
    (list_storeNames $global:context["subscriptionManagementURL"] $global:context["subscriptionID"] $global:context["subscriptionCertificate"]) | Where-Object { $_ -like $storeName } | Select-Object -first 1
}


function resolve_queueName($queueName, $accountName, $accountKey, $baseURL) {
    (list_queues $accountName $accountKey $baseURL) | Where-Object { $_.Name -like $queueName } | Select-Object -ExpandProperty Name -first 1
}


function resolve_containerName($containerName, $accountName, $accountKey, $baseURL) {
    (list_containers $accountName $accountKey $baseURL) | Where-Object { $_.Name -like $containerName } | Select-Object -ExpandProperty Name -first 1
}


function resolve_blobName($blobName, $containerName, $accountName, $accountKey, $baseURL) {
    (list_blobs $accountName $accountKey $baseURL $containerName) | Where-Object { $_.Name -like $blobName } | Select-Object -ExpandProperty Name -first 1
}


function list_services($mgmtURL, $subID, $cert) {
    if($null -ne ($servicesXML = (list_servicesXML $mgmtURL $subID $cert))) {
        $servicesXML.HostedServices.HostedService | Where-Object {$_ -ne $null}
    }
}


function list_deployments($serviceName, $mgmtURL, $subID, $cert) {
    if($null -ne ($xml = get_serviceXML $serviceName $mgmtURL $subID $cert)) {
        $xml.HostedService.Deployments.Deployment | Where-Object {$_ -ne $null}
    }
}


function get_deploymentXML($name, $serviceName, $cloudURL, $subID, $cert) {
    $operation = "services/hostedservices/" + $serviceName + "/deployments/" + $name
    Call-Subscription $cloudURL $subID $operation "GET" $cert $null
}


function check_sub() {
    if(($global:context["subscriptionName"] -ne $null) -and ($global:context["subscriptionID"] -ne $null) -and ($global:context["subscriptionCertificate"] -ne $null) -and ($global:context["subscriptionManagementURL"] -ne $null)) {
        return $true
    } else {
        Write-ErrorBrief "Subscription not selected"
        return $false
    }
}


function get_subscriptionXML($cloudURL, $subscriptionID, $cert) {
    Call-Subscription $cloudURL $subscriptionID $null "GET" $cert
}


function get_storesXML($cloudURL, $subscriptionID, $cert) {
    Call-Subscription $cloudURL $subscriptionID "services/storageservices" "GET" $cert
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
    $body.CreateStorageServiceInput.Label = ConvertTo-Base64 $name 
    $body.CreateStorageServiceInput.GeoReplicationEnabled = "false"
	Call-SubscriptionAndWait $cloudURL $subscriptionID $operation "POST" $cert $body
}


function list_stores($cloudURL, $subID, $cert) {
    if($null -ne ($storageXML = (get_storesXML $cloudURL $subID $cert))) {
        $storageXML.StorageServices.StorageService | Where-Object { $_ -ne $null }       
    }
}


function list_storeNames($cloudURL, $subID, $cert) {
    if($null -eq ($storageXML = (get_storesXML $cloudURL $subID $cert))) {
        return $null
    } else {
        $storageXML.StorageServices.StorageService | foreach {$_.ServiceName } | Where-Object { [String]::IsNullOrEmpty($_) -eq $False }       
    }
}


function list_locationsXML($cloudURL, $subscriptionID, $cert) {
    Call-Subscription $cloudURL $subscriptionID "locations" "GET" $cert
}


function get_storageKey($cloudURL, $subscriptionID, $cert, $storageName) {
    $storeKeysXML = get_storageKeysXML $storageName  $cloudURL $subscriptionID $cert
    if($storeKeysXML -ne $null) {
        $storeKeysXML.StorageService.StorageServiceKeys.Primary
    }            
}


function get_storageKeysXML($name, $cloudURL, $subscriptionID, $cert) {
    $operation = "services/storageServices/" + $name + "/keys"
    Call-Subscription $cloudURL $subscriptionID $operation "GET" $cert    
}


function get_storageURL($cloudURL, $subscriptionID, $cert, $storageName) {
	if($null -ne ($storageXML = get_storageXML $cloudURL $subscriptionID $cert $storageName)) {
		$url = $storageXML.StorageService.StorageServiceProperties.Endpoints.Endpoint[0]
		if($url.StartsWith("http://")) {
			$http = "http://"
		} elseif($url.StartsWith("https://")) {
			$http = "https://"
		}
		[string[]]$urlParts = $url.Split(".")
		$urlParts[0] = ""	#Blank out storage name
		$urlParts[1] = ""	#Blank out service type
		$url = $urlParts -join '.'
		$http + $url.TrimStart('.').TrimEnd('/')
	} 
}


function get_storageXML($cloudURL, $subscriptionID, $cert, $storageName) {
    $operation = "services/storageServices/" + $name
	Call-Subscription $cloudURL $subscriptionID $operation "GET" $cert
}

function connection_string($accountName, $accountKey) {
    'DefaultEndpointsProtocol=https;AccountName=' + $accountName + ';AccountKey=' + $accountKey
}


function get_container($name, [string]$accountName, [string]$accountKey, [string]$baseURL) {
    if(!$name -or !$accountName -or !$accountKey) {
        return $null
    } elseif ($null -eq ($blobClient = get_blob_client $accountName $accountKey $baseURL)) {
        return $null
    } else {
        $blobClient.GetContainerReference($name)
    }
}

function get_queue($name, $accountName, $accountKey, $baseURL) {
    if(!$accountName -or !$name -or !$accountKey -or !$baseURL) {
        return $null
    } elseif ($null -eq ($queueClient = get_queue_client $accountName $accountKey $baseURL)) {
        return $null
    } else {
        $queueClient.GetQueueReference($name)
    }
}


function get_blob_client([string]$accountName, [string]$accountKey, [string]$baseURL) {
	$storageAccount = get_storageAccount $accountName $accountKey $baseURL
    if($storageAccount) {
		[Microsoft.WindowsAzure.StorageClient.CloudStorageAccountStorageClientExtensions]::CreateCloudBlobClient($storageAccount)
	}
}


function get_queue_client($accountName, $accountKey, $baseURL) {
	$storageAccount = get_storageAccount $accountName $accountKey $baseURL
    if($storageAccount) {
		[Microsoft.WindowsAzure.StorageClient.CloudStorageAccountStorageClientExtensions]::CreateCloudQueueClient($storageAccount)
	}
}


function get_storageAccount($accountName, $accountKey, $baseURL) {
    load_storage_client
	if(!$baseURL) {
		$connection = connection_string $accountName $accountKey
    	[Microsoft.WindowsAzure.CloudStorageAccount]::Parse($connection)
	} else {
		if($baseURL.StartsWith('http://')) {
			$http = 'http://'
			$dns = $baseURL.Substring($http.Length)
		} elseif($baseURL.StartsWith('https://')) {
			$http = 'https://'
			$dns = $baseURL.Substring($http.Length)
		} else {
			$http = 'http://'
			$dns = $baseURL
		}
		
		$blobURL = ($http + $accountName + '.' + 'blob.' + $dns)
		$tableURL = $blobURL.Replace('blob', 'table')
		$queueURL = $blobURL.Replace('blob', 'queue')
		$storageCredentials = New-Object Microsoft.WindowsAzure.StorageCredentialsAccountAndKey($accountName, $accountKey)
		New-Object Microsoft.WindowsAzure.CloudStorageAccount($storageCredentials, $blobURL, $queueURL, $tableURL)
	}
}


function load_storage_client() {
    $fileName = 'Microsoft.WindowsAzure.StorageClient.dll'
    $path = Join-Path $myDir $fileName    
    if(-not (Test-Path $path)) {
        # If not found in current directory then look for SDK
        $azureSDKPath = findLatestAzureSDKDir
        $path = Join-Path $azureSDKPath $fileName
    }
    
    if(-not (Test-Path $path)) {
        Write-ErrorBrief 'The required StorageClient.dll cannot be found'
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


function list_servicesXML($cloudURL, $subscriptionID, $cert) {
    Call-Subscription $cloudURL $subscriptionID "services/hostedservices" "GET" $cert
}


function get_serviceXML($name, $cloudURL, $subscriptionID, $cert) {
    $operation = "services/hostedservices/" + $name + "?embed-detail=true"
    Call-Subscription $cloudURL $subscriptionID $operation "GET" $cert
}


function check_service() {
    if(($global:context["serviceName"] -ne $null) -and ($global:context["serviceURL"] -ne $null) -and ($global:context["serviceLocation"] -ne $null)) {
        return $true
    } else {
        Write-ErrorBrief "Service not selected"
        return $false
    }
}


function show_usage([string[]]$cmdTokens) {
    $format = @{Expression={$_.Name};Label="Entity"}, @{Expression={$_.Value};Label="Actions"}
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
        $entities.GetEnumerator() | Sort-Object Name | Format-Table -AutoSize -Wrap $format | Out-String
    } elseif(($action -eq $null) -or ($null -eq ($actionDef = $entityDef.Get_Item($action)))) {
        # Show entity actions
        $help = $entities.GetEnumerator() | Where-Object {$_.Name -eq $entity} | Format-Table -AutoSize -Wrap $format | Out-String
        Write-ErrorBrief $help
    } else {
        # Show entity action parameters
        $format = @{Expression={$entity};Label="Entity"}, @{Expression={$_.Name};Label="Action"}, @{Expression={$_.Value};Label="Arguments"}
        $help = $entityDef.GetEnumerator() | Where-Object {$_.Name -eq $action} | Format-Table -AutoSize -Wrap $format | Out-String
        Write-ErrorBrief $help
    }
}


#########################
### General Util ########
#########################


function Write-ErrorBrief {
	param([string]$message)
	$host.ui.WriteErrorLine($message)
}


function Out-Notepad() {
	param(
		[string]$text
	)
	
	if($text) {
		$input = $text
	}
	
    $wshShell = New-Object -ComObject wscript.shell
    [void]$wshShell.Run("notepad")
    [void]$wshshell.AppActivate("Notepad")
    Start-Sleep -milliseconds 200

	$input | % {
		$text = $_.Replace('{', '{{@_notepd_@').Replace('}', '{}@_notepd_@').Replace('@_notepd_@', '}')
		@('(', ')', '[', ']', '+', '^', ,'%', '~') | % { $text = $text.Replace($_, "{$_}") }
	    $wshShell.SendKeys($text + '~')
	}
}


function ConvertTo-Base64 {
	param(
		[string]$content
	)
	
	if($content) {
		$input = $content
	}
	
	$input | % {
    	$bytes  = [System.Text.Encoding]::UTF8.GetBytes($_);
    	[System.Convert]::ToBase64String($bytes);
	}
}


function Import-Cert {
	param(
		[string]$certBase64
	)
	
	if($certBase64) {
		$input = $certBase64
	}
	
	$input | % {
		$certBytes = [System.Convert]::FromBase64String($_)
    	$cert = New-Object System.Security.Cryptography.X509Certificates.X509Certificate2
    	$cert.Import($certBytes)
    	$cert
	}
}


function Test-URL {
	param(
		[string]$url
	)
	
	if($url) {
		$input = $url
	}
	
	$input | % {
    	try 
    	{ 
        	$request = [System.Net.HttpWebRequest]::Create($_)
        	$response = $request.GetResponse()
        	if($response.StatusCode -eq "OK") {
            	Write-Output $true
        	} else {
            	Write-Output $false
        	}
    	} 
    	catch {
        	Write-Output $false
    	}
	}
}


function Download-File {
	param(
		[string] $srcURL,
		[string] $destFilePath
	)
    
	if(-not($destFilePath)) {
		$uri = [System.Uri]$srcURL
		$destFilePath =  $uri.Segments[-1];
	}
	
    if(Test-Path $destFilePath) {
        Remove-Item $destFilePath
    }
    
    $webclient = New-Object System.Net.WebClient
    $webclient.DownloadFile($srcURL,$destFilePath)  
}


function Out-ZIP { 
	param(
		[string]$zipFilePath
	)

	if(-not($zipFilePath)) {
		Write-Error "Zip file path missing"
		return
	} elseif (-not $zipFilePath.EndsWith('.zip')) {
        $zipFilePath += '.zip'
    } 
    
    if (-not (Test-Path $zipFilePath)) { 
        Set-Content $zipFilePath ("PK" + [char]5 + [char]6 + ("$([char]0)" * 18)) 
    } 
  
    $app = New-Object -com Shell.Application
    $zipFilePath = Resolve-Path $zipFilePath
    $zipFile = $app.NameSpace($zipFilePath)
   
    if($zipFile -eq $null) {
        Write-Error "ZIP file path is not valid"
    } else {
        $input | foreach { $zipFile.CopyHere($_.FullName, 4 + 1024) }
    
        # Wait for ZIP file creation to end
        while($zipFile.Items().Count -eq 0) {
            Start-Sleep -s 1
        }
    }
} 


function Call-SubscriptionAndWait {
    Param(
        [string]$cloudURL, 
        [string]$subscriptionID, 
        [string]$operation, 
        [string]$method, 
        [System.Security.Cryptography.X509Certificates.X509Certificate2]$cert, 
        [xml]$body
    )
    
	$request = New-SubscriptionWebRequest $cloudURL $subscriptionID $operation $cert $method $body
	$response = Call-Web $request
	$longRunningID = Get-WebResponseHeaderValues "x-ms-request-id" -Response $response
	
	Wait-SubscriptionOperation $cloudURL $subscriptionID $cert $longRunningID
}


function Wait-SubscriptionOperation {
	param(
		$cloudURL,
		$subscriptionID,
		$cert,
		$operationID
	)

	if($operationID) {
		$input = $operationID
	}

	$input | % {
		Write-Host "# Waiting for confirmation.." -nonewline
		try {
			do {
				if($null -eq ($request = New-SubscriptionWebRequest $cloudURL $subscriptionID ("operations/" + $_) $cert "GET" $null)) {
					$errorText = "Failed to create new subscription request"
				} elseif($null -eq ($response = Call-Web $request)) {
					$errorText = "Failed to get response"
				} elseif($null -eq ([xml]$xml = Read-WebResponseBody $response)) {
					$errorText = "Failed to get XML from response"
				} else {
					switch($xml.Operation.Status) {
						"InProgress" { 
							Write-Host "." -nonewline
						}
						"Failed" { 
							return $false
						}
						"Succeeded" {
							return $true
						}
					}
					Start-Sleep -s 4
				}
			} while($True)

		} catch {
			return $false
		} finally {
			Write-Host
			if($errorText) {
				Write-Error $errorText
			}
		}
	
		return $false
	}
}


function Call-Subscription {
    Param(
        [string]$cloudURL,
        [string]$subscriptionID,
        [string]$operation,
        [string]$method,
        [System.Security.Cryptography.X509Certificates.X509Certificate2]$cert,
        [xml]$body
    )
    
    $request = New-SubscriptionWebRequest $cloudURL $subscriptionID $operation $cert $method $body
    $response = Call-Web $request
	if($null -ne $response) {
		[xml](Read-WebResponseBody $response)
	}
}


function Read-WebResponseBody {
	param([System.Net.WebResponse]$response)

	if($response) {
		$input= $response
	} 
	
	$input | % {
		try {
			$stream = $response.GetResponseStream()
			$reader = New-Object System.IO.StreamReader($stream)
			$responseContent = $reader.ReadToEnd()
			if(-not([System.String]::IsNullOrEmpty($responseContent))) {
				Write-Output $responseContent
			}
		} catch {
		} finally {
			if($reader) {
				$reader.Close()
			}
			if($stream) {
				$stream.Close()
			}
		}
	}
}


function Get-WebResponseHeaderValues {
	param(
		[string[]] $headers,
		[System.Net.WebResponse] $response
	)

	if($headers -eq $null) {
		return
	} elseif($response) {
		$input = $response
	} 

	$input | % {
		foreach($headerKey in $headers) {
			Write-Output ($_.Headers.GetValues($headerKey)[0])
		}
	}
}


function Call-Web {
    Param(
		[Parameter(Mandatory=$true)] [System.Net.WebRequest]$request
    )

    if($request -eq $null) {
        $requests = $input
    } else {
        $requests = @($request)
    }

    $requests | % {    
        try {
            $response = $_.GetResponse()
        } catch [System.Exception] {
            Write-Error $_.Exception.Message
        } finally {
            Write-Output $response
        }
    }
}


function New-SubscriptionWebRequest {
    Param(
        [string]$cloudURL,
        [string]$subscriptionID,
        [string]$operation,
        [System.Security.Cryptography.X509Certificates.X509Certificate2]$cert,
        [string]$method,
        [xml]$body
    )
    
    $uri = $cloudURL + "/" + $subscriptionID 
    if(-not([String]::IsNullOrEmpty($operation))) {
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


function Out-XML([xml]$xml) {
    $xml.Save([Console]::Out)
    Write-Output ""
}


#########################
### Command processor ###
#########################
 
[string]$global:showPrompt = "on"
new-alias  Out-Clipboard $env:SystemRoot\system32\clip.exe

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
    Write-ErrorBrief "Not supported:  $functionName"
}


function get_prompt()
{
    $promptBase = "wash"
    $prompt = $promptBase
    
    if($global:showPrompt -eq "off") {
        return $null
    } elseif($global:showPrompt -eq "on") {
        # Put subscription name in quotes if it contains spaces
        $subname = $global:context["subscriptionName"]
        if(($null -ne $subname) -and ($subname.Contains(" "))) {
            $subname = '"' + $subname + '"'
        }            
        
        if($global:context["subscriptionID"] -ne $null) {
            $prompt += " " + $subname
        }
    
        if(($global:context["storeName"] -ne $null) -or ($global:context["serviceName"] -ne $null)) {
            $prompt += "/"
        }

        # Show current store context
        if($global:context["storeName"] -ne $null) {
            $prompt += "[store:" + $global:context["storeName"]
            if($global:context["containerName"] -ne $null) {
                $prompt += "/" + $global:context["containerName"]
                if($global:context["blobName"] -ne $null) {
                    $prompt += "/" + $global:context["blobName"]
                }
            }
            $prompt += "]"
        }
        
        # Show current service context
        if($global:context["serviceName"] -ne $null) {
            $prompt += "[service:" + $global:context["serviceName"]
            if($global:context["deploymentName"] -ne $null) {
                $prompt += "/" + $global:context["deploymentName"]
            }
            $prompt += "]"
        }
    }
    
    $prompt += ">"
    return $prompt
}


function process_commands()
{
    $promptBase = "wash"
    
    if($showPrompt) {
        $prompt = get_prompt
        Write-Host -NoNewLine $prompt
    }
    
    while($null -ne ($commandLine = read_commandLine)) {
        $commandLine = ($commandLine.Trim())
        if($commandLine -eq "exit") {
            break
        }
        $cmdTokens = $commandLine.Split(" ")
        $cmdTokens = cmdtokens_quoted $cmdTokens
        
        process_entity $cmdTokens
        if($null -ne ($prompt = get_prompt)) {
            Write-Host -NoNewLine $prompt
        }
    }
    Write-Host
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
#. "$myDir\.washutil.ps1"

(Get-Host).UI.RawUI.WindowTitle = "WASH v0.2.1"
(Get-Host).UI.RawUI.ForegroundColor = "cyan"

if($cmdTokens.Length -gt 0) {
    process_entity $cmdTokens
} else {
    process_commands
}
