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
package com.microsoftopentechnologies.wacommon.storageregistry;

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
}
