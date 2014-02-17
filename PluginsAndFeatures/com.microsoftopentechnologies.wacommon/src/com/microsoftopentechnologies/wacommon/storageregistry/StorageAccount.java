/**
 * Copyright 2013 Persistent Systems Ltd.
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
package com.microsoftopentechnologies.wacommon.storageregistry;

import java.io.Serializable;
/**
* Copyright 2014 Microsoft Open Technologies, Inc.
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
