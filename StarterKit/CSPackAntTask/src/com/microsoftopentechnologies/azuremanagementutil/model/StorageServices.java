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
