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
package com.microsoft.applicationinsights.preference;

import java.io.IOException;
import java.util.List;

import com.microsoft.applicationinsights.management.rest.ApplicationInsightsManagementClient;
import com.microsoft.applicationinsights.management.rest.client.RestOperationException;
import com.microsoft.applicationinsights.management.rest.model.Resource;
import com.microsoft.applicationinsights.management.rest.model.Subscription;
import com.microsoft.applicationinsights.ui.config.AIResourceChangeListener;
import com.microsoftopentechnologies.azuremanagementutil.rest.AzureApplicationInsightsServices;


public class ApplicationInsightsResourceRegistryEclipse {

	/**
	 * Method updates application insights registry by adding, removing or updating resources.
	 * @param client
	 * @throws IOException
	 * @throws RestOperationException
	 */
	public static void updateApplicationInsightsResourceRegistry(
			ApplicationInsightsManagementClient client) throws IOException, RestOperationException {
		AzureApplicationInsightsServices instance = AzureApplicationInsightsServices.getInstance();
		List<Subscription> subList = instance.getSubscriptions(client);
		for (Subscription sub : subList) {
			// fetch resources available for particular subscription
			List<Resource> resourceList = instance.getApplicationInsightsResources(client, sub.getId());

			// Removal logic
			List<ApplicationInsightsResource> registryList = ApplicationInsightsResourceRegistry.
					getResourceListAsPerSub(sub.getId());
			List<ApplicationInsightsResource> importedList = ApplicationInsightsResourceRegistry.
					prepareAppResListFromRes(resourceList, sub);
			List<String> inUsekeyList = AIResourceChangeListener.getInUseInstrumentationKeys();
			for (ApplicationInsightsResource registryRes : registryList) {
				if (!importedList.contains(registryRes)) {
					String key = registryRes.getInstrumentationKey();
					int index = ApplicationInsightsResourceRegistry.getResourceIndexAsPerKey(key);
					if (inUsekeyList.contains(key)) {
						/*
						 * key is used by project but not present in cloud,
						 * so make it as manually added resource and not imported.
						 */
						ApplicationInsightsResource resourceToAdd = new ApplicationInsightsResource(
								key, key, Messages.unknown, Messages.unknown,
								Messages.unknown, Messages.unknown, false);
						ApplicationInsightsResourceRegistry.getAppInsightsResrcList().set(index, resourceToAdd);
					} else {
						// key is not used by any project then delete it.
						ApplicationInsightsResourceRegistry.getAppInsightsResrcList().remove(index);
					}
				}
			}

			// Addition logic
			List<ApplicationInsightsResource> list = ApplicationInsightsResourceRegistry.
					getAppInsightsResrcList();
			for (Resource resource : resourceList) {
				ApplicationInsightsResource resourceToAdd = new ApplicationInsightsResource(
						resource.getName(), resource.getInstrumentationKey(),
						sub.getName(), sub.getId(),
						resource.getLocation(), resource.getResourceGroup(), true);
				if (list.contains(resourceToAdd)) {
					int index = ApplicationInsightsResourceRegistry.
							getResourceIndexAsPerKey(resource.getInstrumentationKey());
					ApplicationInsightsResource objectFromRegistry = list.get(index);
					if (!objectFromRegistry.isImported()) {
						ApplicationInsightsResourceRegistry.
						getAppInsightsResrcList().set(index, resourceToAdd);
					}
				} else {
					ApplicationInsightsResourceRegistry.
					getAppInsightsResrcList().add(resourceToAdd);
				}
			}
		}
		ApplicationInsightsPreferences.save();
	}
}
