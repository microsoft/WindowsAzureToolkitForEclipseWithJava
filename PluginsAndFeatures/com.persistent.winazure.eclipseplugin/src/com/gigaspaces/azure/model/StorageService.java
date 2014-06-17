/*******************************************************************************
 * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.gigaspaces.azure.model;

import com.microsoft.windowsazure.management.storage.models.StorageAccountGetKeysResponse;
import com.microsoft.windowsazure.management.storage.models.StorageAccountProperties;

public class StorageService {
		      
	private String url;
	private String serviceName;
    private String primaryKey;
    private String secondaryKey;

	private StorageAccountProperties storageAccountProperties;

    public StorageService() {
    }

    public StorageService(String serviceName, StorageAccountGetKeysResponse response) {
        this.serviceName = serviceName;
        this.url = response.getUri().toString();
        this.primaryKey = response.getPrimaryKey();
        this.secondaryKey = response.getSecondaryKey();
    }

	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getServiceName() {
		return serviceName;
	}
	
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

    public String getPrimaryKey() {
        return primaryKey;
    }

    public String getSecondaryKey() {
        return secondaryKey;
    }

    public void setStorageServiceKeys(String primaryKey, String secondaryKey) {
		this.primaryKey = primaryKey;
        this.secondaryKey = secondaryKey;
	}
	
	public StorageAccountProperties getStorageAccountProperties() {
		return storageAccountProperties;
	}

	public void setStorageAccountProperties(StorageAccountProperties storageAccountProperties) {
		this.storageAccountProperties = storageAccountProperties;
	}
}
