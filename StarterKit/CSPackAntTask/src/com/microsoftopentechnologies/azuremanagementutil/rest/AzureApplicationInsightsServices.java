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
package com.microsoftopentechnologies.azuremanagementutil.rest;

import java.io.IOException;
import java.util.List;

import com.microsoft.applicationinsights.management.rest.ApplicationInsightsManagementClient;
import com.microsoft.applicationinsights.management.rest.client.RestOperationException;
import com.microsoft.applicationinsights.management.rest.model.Resource;
import com.microsoft.applicationinsights.management.rest.model.ResourceGroup;
import com.microsoft.applicationinsights.management.rest.model.Subscription;
import com.microsoftopentechnologies.auth.AuthenticationResult;
import com.microsoftopentechnologies.auth.browser.BrowserLauncher;


public class AzureApplicationInsightsServices {
	static String userAgent = "Mozilla/5.0 (Windows NT 6.2; WOW64; rv:19.0) Gecko/20100101 Firefox/19.0";

	private AzureApplicationInsightsServices(){}

	private static class SingletonHelper {
		private static final AzureApplicationInsightsServices INSTANCE = new AzureApplicationInsightsServices();
	}

	public static AzureApplicationInsightsServices getInstance(){
		return SingletonHelper.INSTANCE;
	}

	public ApplicationInsightsManagementClient getApplicationInsightsManagementClient(
			AuthenticationResult result, BrowserLauncher launcher) throws IOException, RestOperationException {
		ApplicationInsightsManagementClient client = new ApplicationInsightsManagementClient(
				result, userAgent, launcher);
		return client;
	}

	public List<Subscription> getSubscriptions(ApplicationInsightsManagementClient client)
			throws IOException, RestOperationException {
		List<Subscription> list = client.getSubscriptions();
		return list;
	}

	public List<ResourceGroup> getResourceGroups(ApplicationInsightsManagementClient client, String subscriptionId)
			throws IOException, RestOperationException {
		List<ResourceGroup> list = client.getResourceGroups(subscriptionId);
		return list;
	}

	public List<String> getAvailableGeoLocations(ApplicationInsightsManagementClient client)
			throws IOException, RestOperationException {
		List<String> list = client.getAvailableGeoLocations();
		return list;
	}

	public List<Resource> getApplicationInsightsResources(ApplicationInsightsManagementClient client,
			String subscriptionId) throws IOException, RestOperationException {
		List<Resource> list = client.getResources(subscriptionId);
		return list;
	}

	public Resource createApplicationInsightsResource(ApplicationInsightsManagementClient client,
			String subscriptionId,
			String resourceGroupName,
			String resourceName,
			String location) throws IOException, RestOperationException {
		Resource resource = client.createResource(subscriptionId, resourceGroupName, resourceName, location);
		return resource;
	}

	public ResourceGroup createResourceGroup(ApplicationInsightsManagementClient client,
			String subscriptionId,
			String resourceGroupName,
			String location) throws IOException, RestOperationException {
		ResourceGroup group = client.createResourceGroup(subscriptionId, resourceGroupName, location);
		return group;
	}
}
