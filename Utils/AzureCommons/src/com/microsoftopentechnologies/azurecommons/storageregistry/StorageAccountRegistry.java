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

import java.util.ArrayList;
import java.util.List;
/**
 * Class maintains list of storage accounts as a repository.
 * Performs various operations on list.
 */
public class StorageAccountRegistry {
	/**
	 * List of storage accounts maintained
	 * to be in sync with preference file data.
	 */
	private static List<StorageAccount> strgList =
			new ArrayList<StorageAccount>();

	public static List<StorageAccount> getStrgList() {
		return strgList;
	}

	/**
	 * Adds storage account to list.
	 * @param storageAcc
	 */
	public static void addAccount(StorageAccount storageAcc) {
		getStrgList().add(storageAcc);
	}

	/**
	 * Edits storage account by changing access key.
	 * @param account
	 * @param key
	 */
	public static void editAccountAccessKey(
			StorageAccount account, String key) {
		int index = getStrgList().indexOf(account);
		StorageAccount storageAccount = getStrgList().get(index);
		storageAccount.setStrgKey(key);
	}
	
	/**
	 * Edit storage account's blob endpoint's protocol as per cloud type.
	 * Retain SSL (https) for storage endpoint URIs for regions that are outside of China.
	 */
	public static void editUrlsAsPerCloud() {
		for (int i = 0; i < getStrgList().size(); i++) {
			StorageAccount storageAccount = getStrgList().get(i);
			String tempUrl = storageAccount.getStrgUrl();
			if (tempUrl.contains("blob.core.windows.net")
					&& tempUrl.startsWith("http://")) {
				storageAccount.setStrgUrl(storageAccount.getStrgUrl().replaceFirst("http://", "https://"));
			} else if (tempUrl.contains("blob.core.chinacloudapi.cn")
					&& tempUrl.startsWith("https://")) {
				storageAccount.setStrgUrl(storageAccount.getStrgUrl().replaceFirst("https://", "http://"));
			}
		}
	}
}
