/**
 * Copyright (c) Microsoft Corporation
 * 
 * All rights reserved. 
 * 
 * MIT License
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files 
 * (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, 
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH 
 * THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.microsoftopentechnologies.azuremanagementutil.model;

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
