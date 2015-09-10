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

package com.microsoftopentechnologies.azurecommons.deploy.util;

import java.util.Collection;
import java.util.List;





import com.microsoftopentechnologies.azuremanagementutil.model.StorageService;
import com.microsoftopentechnologies.azuremanagementutil.model.StorageServices;
import com.microsoftopentechnologies.azuremanagementutil.model.Subscription;
import com.microsoftopentechnologies.azurecommons.storageregistry.StorageAccount;
import com.microsoftopentechnologies.azurecommons.storageregistry.StorageAccountRegistry;
import com.microsoftopentechnologies.azurecommons.wacommonutil.PreferenceSetUtil;

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
		StorageAccountRegistry.editUrlsAsPerCloud();
		return strgList;
	}

	public static String getManagementUrlAsPerPubFileVersion(PublishData publishData,
			Subscription subscription,
			String prefFilePath)
					throws Exception {
		String schemaVer = publishData.getPublishProfile().getSchemaVersion();
		boolean isNewSchema = schemaVer != null && !schemaVer.isEmpty() && schemaVer.equalsIgnoreCase("2.0");
		// URL if schema version is 1.0
		String url = publishData.getPublishProfile().getUrl();
		if (isNewSchema) {
			// publish setting file is of schema version 2.0
			url = subscription.getServiceManagementUrl();
		}
		if (url == null || url.isEmpty()) {
			url = PreferenceSetUtil.getManagementURL(
					PreferenceSetUtil.getSelectedPreferenceSetName(prefFilePath),
					prefFilePath);
			url = url.substring(0, url.lastIndexOf("/"));
		}
		return url;
	}
}
