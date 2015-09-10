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

package com.microsoftopentechnologies.azurecommons.storageregistry;

import java.io.Serializable;

/**
 * Storage account class with attributes
 * name, access key and URL.
 */
public class StorageAccount implements Serializable, Cloneable, Comparable<StorageAccount> {
	private static final long serialVersionUID = -5687551495552719808L;
	private String strgName;
	private String strgKey;
	private String strgUrl;

	public StorageAccount() {
		super();
	}

	public StorageAccount(String strgName,
			String strgKey,
			String strgUrl) {
		super();
		this.strgName = strgName;
		this.strgKey = strgKey;
		this.strgUrl = strgUrl;
	}

	public String getStrgName() {
		return strgName;
	}

	public void setStrgName(String strgName) {
		this.strgName = strgName;
	}

	public String getStrgKey() {
		return strgKey;
	}

	public void setStrgKey(String strgKey) {
		this.strgKey = strgKey;
	}

	public String getStrgUrl() {
		return strgUrl;
	}

	public void setStrgUrl(String strgUrl) {
		this.strgUrl = strgUrl;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}
		/*
		 * If storage account name and service URL is same,
		 * then objects are equals.
		 */
		StorageAccount account = (StorageAccount) obj;
		String url = account.getStrgUrl();
		// get service URL.
		String serviceUrl = StorageRegistryUtilMethods.
				getSubStrAccNmSrvcUrlFrmUrl(url);
		String serviceUrlToChk =  StorageRegistryUtilMethods.
				getSubStrAccNmSrvcUrlFrmUrl(strgUrl);
		boolean value = (strgName != null && strgName.equals(account.getStrgName()))
				&& (strgUrl != null && serviceUrlToChk.equals(serviceUrl));
		return value;
	}

	@Override
	public int compareTo(StorageAccount object) {
		int value = strgName.compareToIgnoreCase(object.getStrgName());
		return value;
	}
}
