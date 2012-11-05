/*******************************************************************************
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="StorageService")
@XmlType
public class StorageService {
		      
	private String url;
	private String serviceName;
	private StorageServiceKeys storageServiceKeys;
	private StorageServiceProperties storageServiceProperties;
	
	@XmlElement(name="Url")
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	@XmlElement(name="ServiceName")
	public String getServiceName() {
		return serviceName;
	}
	
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	@XmlElement(name="StorageServiceKeys")
	public StorageServiceKeys getStorageServiceKeys() {
		return storageServiceKeys;
	}

	public void setStorageServiceKeys(StorageServiceKeys storageServiceKeys) {
		this.storageServiceKeys = storageServiceKeys;
	}
	
	@XmlElement(name = "StorageServiceProperties")
	public StorageServiceProperties getStorageServiceProperties() {
		return storageServiceProperties;
	}

	public void setStorageServiceProperties(StorageServiceProperties storageServiceProperties) {
		this.storageServiceProperties = storageServiceProperties;
	}
}
