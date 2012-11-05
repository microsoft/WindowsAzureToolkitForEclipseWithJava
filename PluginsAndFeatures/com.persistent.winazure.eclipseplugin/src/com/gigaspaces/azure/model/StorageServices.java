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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="StorageServices")
public class StorageServices implements Iterable<StorageService> {

	private List<StorageService> storageServices;

	@XmlElement(name="StorageService")
	public List<StorageService> getStorageServices() {
		return storageServices;
	}

	public void setStorageServices(List<StorageService> storageServices) {
		this.storageServices = storageServices;
	}

	@Override
	public Iterator<StorageService> iterator() {
		
		return storageServices.iterator();
	}
	
	public int size() {
		if (isEmpty()) return 0;
		return storageServices.size();
	}
	
	public boolean isEmpty() {
		return (storageServices == null);
	}
	
	public void add(StorageService storageService) {
		if (storageServices == null) {
			storageServices = new ArrayList<StorageService>();
		}
		storageServices.add(storageService);
	}
	
	public void remove(final String storageAccountName) {
		
		StorageService toRemove = null;
		for (StorageService st : storageServices) {
			if (st.getServiceName().equals(storageAccountName)) {
				toRemove = st;
			}
		}
		if (toRemove != null) {
			storageServices.remove(toRemove);
		}
		
	}

}
