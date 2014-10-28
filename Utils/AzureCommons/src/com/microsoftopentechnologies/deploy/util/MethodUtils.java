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
package com.microsoftopentechnologies.deploy.util;

import java.util.Collection;
import java.util.List;

import com.microsoftopentechnologies.model.StorageService;
import com.microsoftopentechnologies.model.StorageServices;
import com.microsoftopentechnologies.model.Subscription;
import com.microsoftopentechnologies.storageregistry.StorageAccount;

public class MethodUtils {

	public static List<StorageAccount> prepareListFromPublishData(List<StorageAccount> strgList,
			Collection<PublishData> publishDatas) {
		for (PublishData pd : publishDatas) {
			for (Subscription sub : pd.getPublishProfile().getSubscriptions()) {
				/*
				 * Get collection of storage services in each subscription.
				 */
				StorageServices services = pd.getStoragesPerSubscription().get(sub.getId());
				// iterate over collection of services.
				for (StorageService strgService : services) {
					StorageAccount strEle =
							new StorageAccount(
									strgService.getServiceName(),
									strgService.getPrimaryKey(),
									strgService.getStorageAccountProperties().
									getEndpoints().get(0).toString());
					/*
					 * Check if storage account is already present
					 * in centralized repository,
					 * if present then do not add.
					 * if not present then check
					 * access key is valid or not.
					 * If not then update with correct one in registry. 
					 */
					if (strgList.contains(strEle)) {
						int index = strgList.indexOf(strEle);
						StorageAccount account = strgList.get(index);
						String newKey = strEle.getStrgKey();
						if (!account.getStrgKey().equals(newKey)) {
							account.setStrgKey(newKey);
						}
					} else {
						strgList.add(strEle);
					}
				}
			}
		}
		return strgList;
	}
}
