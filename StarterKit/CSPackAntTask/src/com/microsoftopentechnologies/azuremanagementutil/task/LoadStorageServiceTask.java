/**
* Copyright Microsoft Corp.
*
* Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*	 http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/
package com.microsoftopentechnologies.azuremanagementutil.task;

import java.util.concurrent.Callable;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.management.storage.models.StorageAccount;
import com.microsoftopentechnologies.azuremanagementutil.model.StorageService;
import com.microsoftopentechnologies.azuremanagementutil.rest.WindowsAzureServiceManagement;

public class LoadStorageServiceTask implements Callable<StorageService> {
	
	private Configuration configuration;
	private StorageAccount storageAccount;
	

	public StorageService call() throws Exception {
		StorageService storageService = WindowsAzureServiceManagement.getStorageKeys(configuration, storageAccount.getName());
        storageService.setServiceName(storageAccount.getName());
        storageService.setStorageAccountProperties(storageAccount.getProperties());
		return storageService;
	}
	
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public void setStorageAccount(StorageAccount storageAccount) {
		this.storageAccount= storageAccount;
	}
}
