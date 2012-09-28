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

package com.gigaspaces.azure.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.gigaspaces.azure.model.AffinityGroups;
import com.gigaspaces.azure.model.AvailabilityResponse;
import com.gigaspaces.azure.model.CertificateFile;
import com.gigaspaces.azure.model.CreateDeployment;
import com.gigaspaces.azure.model.CreateHostedService;
import com.gigaspaces.azure.model.CreateStorageServiceInput;
import com.gigaspaces.azure.model.Deployment;
import com.gigaspaces.azure.model.HostedService;
import com.gigaspaces.azure.model.HostedServices;
import com.gigaspaces.azure.model.Locations;
import com.gigaspaces.azure.model.ModelFactory;
import com.gigaspaces.azure.model.Operation;
import com.gigaspaces.azure.model.Response;
import com.gigaspaces.azure.model.Status;
import com.gigaspaces.azure.model.StorageService;
import com.gigaspaces.azure.model.StorageServiceKeys;
import com.gigaspaces.azure.model.StorageServices;
import com.gigaspaces.azure.model.Subscription;
import com.gigaspaces.azure.model.UpdateDeploymentStatus;
import com.gigaspaces.azure.util.CommandLineException;

public class WindowsAzureServiceManagement extends WindowsAzureServiceImpl {

	private final String thumbprint;

	public WindowsAzureServiceManagement(String thumbprint) throws InvalidThumbprintException {
		super();
		if (thumbprint == null || thumbprint.isEmpty()) {
			throw new InvalidThumbprintException();
		}
		this.thumbprint = thumbprint;
		context = ModelFactory.createInstance();
	}

	public Subscription getSubscription(String subscriptionId) throws RestAPIException, InterruptedException, CommandLineException {

		String url = MNGMT_SERV_URL.replace(SUBSCRIPTION_ID,subscriptionId);

		HashMap<String, String> headers = new HashMap<String, String>();

		headers.put(X_MS_VERSION, "2011-10-01");

		String result = WindowsAzureRestUtils.getInstance().runRest(HttpVerb.GET, url,
				headers, null, thumbprint);

		Response<?> response = ((Response<?>) deserialize(result));

		validateResponse(response);	

		return (Subscription) response.getBody();
	}

	public HostedService getHostedServiceWithProperties(String subscriptionId,
			String serviceName) throws RestAPIException, InterruptedException, CommandLineException {

		String url = MNGMT_SERV_URL
				.replace(SUBSCRIPTION_ID, subscriptionId)
				.concat(LIST_HOST_SERV).concat(HOST_SERV)
				.replace(SERVICE_NAME, serviceName)
				.concat("?embed-detail=true"); //$NON-NLS-1$

		HashMap<String, String> headers = new HashMap<String, String>();

		headers.put(X_MS_VERSION, "2011-10-01"); //$NON-NLS-1$

		String result = WindowsAzureRestUtils.getInstance().runRest(HttpVerb.GET, url,
				headers, null, thumbprint);

		Response<?> response = ((Response<?>) deserialize(result));

		validateResponse(response);

		return (HostedService) response.getBody();
	}

	public StorageService getStorageAccount(String subscriptionId,
			String serviceName) throws RestAPIException, InterruptedException, CommandLineException {

		String url = MNGMT_SERV_URL
				.replace(SUBSCRIPTION_ID, subscriptionId)
				.concat(LIST_STRG_ACC).concat(HOST_SERV)
				.replace(SERVICE_NAME, serviceName);

		HashMap<String, String> headers = new HashMap<String, String>();

		addXMsVer2012(headers);

		String result = WindowsAzureRestUtils.getInstance().runRest(HttpVerb.GET, url,
				headers, null, thumbprint);

		Response<?> response = ((Response<?>) deserialize(result));

		validateResponse(response);

		StorageService storageService = (StorageService) response.getBody();
		StorageServiceKeys storageServiceKeys = getStorageKeys(subscriptionId, storageService.getServiceName()).getStorageServiceKeys();
		storageService.setStorageServiceKeys(storageServiceKeys);

		return storageService;
	}
	
	public boolean checkForStorageAccountDNSAvailability(final String subscriptionId, final String storageAccountName) throws InterruptedException, CommandLineException, RestAPIException {
		
		String url = MNGMT_SERV_URL
				.replace(SUBSCRIPTION_ID, subscriptionId)
				.concat("/services/storageservices/operations/isavailable/" + storageAccountName);

		HashMap<String, String> headers = new HashMap<String, String>();

		addXMsVer2012(headers);
		
		String result = WindowsAzureRestUtils.getInstance().runRest(HttpVerb.GET, url,
				headers, null, thumbprint);

		Response<?> response = ((Response<?>) deserialize(result));

		validateResponse(response);
		
		AvailabilityResponse availabilityResponse = (AvailabilityResponse) response.getBody();
		return availabilityResponse.getResult();

	}
	
	public boolean checkForCloudServiceDNSAvailability(final String subscriptionId, final String hostedServiceName) throws InterruptedException, CommandLineException, RestAPIException {
		
		String url = MNGMT_SERV_URL
				.replace(SUBSCRIPTION_ID, subscriptionId)
				.concat("/services/hostedservices/operations/isavailable/" + hostedServiceName);

		HashMap<String, String> headers = new HashMap<String, String>();

		addXMsVer2012(headers);
		
		String result = WindowsAzureRestUtils.getInstance().runRest(HttpVerb.GET, url,
				headers, null, thumbprint);

		Response<?> response = ((Response<?>) deserialize(result));

		validateResponse(response);
		
		AvailabilityResponse availabilityResponse = (AvailabilityResponse) response.getBody();
		return availabilityResponse.getResult();

	}	


	public Operation getOperationStatus(String subscriptionId, String requestId) throws RestAPIException, InterruptedException, CommandLineException {
		String url = MNGMT_SERV_URL
				.replace(SUBSCRIPTION_ID, subscriptionId)
				.concat(GET_OPERTN_STAT).replace(REQUEST_ID, requestId);

		HashMap<String, String> headers = new HashMap<String, String>();

		addXMsVer2012(headers);

		String result = WindowsAzureRestUtils.getInstance().runRest(HttpVerb.GET, url,
				headers, null, thumbprint);

		Response<?> response = ((Response<?>) deserialize(result));

		validateResponse(response);

		return (Operation) response.getBody();
	}

	public synchronized List<StorageService> listStorageAccounts(
			String subscriptionId) throws InterruptedException, CommandLineException {
		String url = MNGMT_SERV_URL.replace(SUBSCRIPTION_ID,
				subscriptionId).concat(LIST_STRG_ACC);

		HashMap<String, String> headers = new HashMap<String, String>();

		addXMsVer2012(headers);

		List<StorageService> storageServices = new ArrayList<StorageService>();

		String result = WindowsAzureRestUtils.getInstance().runRest(HttpVerb.GET, url,
				headers, null, thumbprint);

		Response<?> response = ((Response<?>) deserialize(result));

		StorageServices services = (StorageServices) response.getBody();

		if (!services.isEmpty()) {
			for (StorageService ss : services) {
				StorageService storageService = getStorageKeys(subscriptionId,
						ss.getServiceName());
				storageService.setServiceName(ss.getServiceName());

				storageServices.add(storageService);
			}
		}

		return storageServices;
	}

	public StorageService getStorageKeys(String subscriptionId,
			String serviceName) throws InterruptedException, CommandLineException {
		String url = MNGMT_SERV_URL
				.replace(SUBSCRIPTION_ID, subscriptionId)
				.concat(LIST_STRG_ACC).concat(GET_STRG_KEYS)
				.replace(SERVICE_NAME, serviceName);

		HashMap<String, String> headers = new HashMap<String, String>();

		addXMsVer2012(headers);

		String result = WindowsAzureRestUtils.getInstance().runRest(HttpVerb.GET, url,
				headers, null, thumbprint);

		Response<?> response = ((Response<?>) deserialize(result));

		return (StorageService) response.getBody();
	}

	public synchronized Locations listLocations(String subscriptionId) throws InterruptedException, CommandLineException {

		String url = MNGMT_SERV_URL.replace(SUBSCRIPTION_ID,
				subscriptionId).concat(LIST_LOC);

		HashMap<String, String> headers = new HashMap<String, String>();

		addXMsVer2012(headers);

		String result = WindowsAzureRestUtils.getInstance().runRest(HttpVerb.GET, url,
				headers, null, thumbprint);

		Response<?> response = (Response<?>) deserialize(result);

		return (Locations) response.getBody();
	}

	public AffinityGroups listAffinityGroups(String subscriptionId) throws InterruptedException, CommandLineException {

		String url = MNGMT_SERV_URL.replace(SUBSCRIPTION_ID,
				subscriptionId).concat(LIST_AFF_GRPS);

		HashMap<String, String> headers = new HashMap<String, String>();

		addXMsVer2012(headers);

		String result = WindowsAzureRestUtils.getInstance().runRest(HttpVerb.GET, url,
				headers, null, thumbprint);

		Response<?> response = ((Response<?>) deserialize(result));

		return (AffinityGroups) response.getBody();
	}

	public synchronized HostedServices listHostedServices(String subscriptionId) throws InterruptedException, CommandLineException {

		String url = MNGMT_SERV_URL.replace(SUBSCRIPTION_ID,
				subscriptionId).concat(LIST_HOST_SERV);

		HashMap<String, String> headers = new HashMap<String, String>();

		addXMsVer2012(headers);

		String result = WindowsAzureRestUtils.getInstance().runRest(HttpVerb.GET, url,
				headers, null, thumbprint);

		Response<?> response = ((Response<?>) deserialize(result));

		return (HostedServices) response.getBody();
	}

	public String createHostedService(String subscriptionId, CreateHostedService body) throws RestAPIException, InterruptedException, CommandLineException {

		String url = MNGMT_SERV_URL.replace(SUBSCRIPTION_ID,
				subscriptionId).concat(LIST_HOST_SERV);

		HashMap<String, String> headers = new HashMap<String, String>();

		addXMsVer2012(headers);

		headers.put(CONTENT_TYPE, MediaType.APPLICATION_XML);

		String result = WindowsAzureRestUtils.getInstance().runRest(HttpVerb.POST, url,
				headers, body, thumbprint);

		Response<?> response = ((Response<?>) deserialize(result));

		validateResponse(response);

		return getXRequestId(response);
	}


	public String createStorageAccount(String subscriptionId, CreateStorageServiceInput body) throws RestAPIException, InterruptedException, CommandLineException {

		String url = MNGMT_SERV_URL.replace(SUBSCRIPTION_ID,
				subscriptionId).concat(LIST_STRG_ACC);

		HashMap<String, String> headers = new HashMap<String, String>();

		addXMsVer2012(headers);

		headers.put(CONTENT_TYPE, MediaType.APPLICATION_XML);

		String result = WindowsAzureRestUtils.getInstance().runRest(HttpVerb.POST, url,
				headers, body, thumbprint);

		Response<?> response = ((Response<?>) deserialize(result));

		validateResponse(response);
		
		return getXRequestId(response);
	}


	public Deployment getDeployment(String subscriptionId, String serviceName,String deploymentName) throws RestAPIException, InterruptedException, CommandLineException {
		String url = MNGMT_SERV_URL
				.replace(SUBSCRIPTION_ID, subscriptionId)
				.concat(LIST_HOST_SERV).concat(HOST_SERV)
				.replace(SERVICE_NAME, serviceName).concat(DPLY_NAME)
				.replace(DEPLOYMENT_NAME, deploymentName);

		HashMap<String, String> headers = new HashMap<String, String>();

		headers.put(X_MS_VERSION, "2011-10-01");

		String result = WindowsAzureRestUtils.getInstance().runRest(HttpVerb.GET, url,
				headers, null, thumbprint);

		Response<?> response = ((Response<?>) deserialize(result));
		
		validateResponse(response);

		return (Deployment) response.getBody();			
	}

	public String deleteDeployment(String subscriptionId, String serviceName,
			String deploymentName) throws RestAPIException, InterruptedException, CommandLineException {
		String url = MNGMT_SERV_URL
				.replace(SUBSCRIPTION_ID, subscriptionId)
				.concat(LIST_HOST_SERV).concat(HOST_SERV)
				.replace(SERVICE_NAME, serviceName).concat(DPLY_NAME)
				.replace(DEPLOYMENT_NAME, deploymentName);

		HashMap<String, String> headers = new HashMap<String, String>();

		addXMsVer2012(headers);

		String result = WindowsAzureRestUtils.getInstance().runRest(HttpVerb.DELETE, url,
				headers, null, thumbprint);

		Response<?> response = ((Response<?>) deserialize(result));

		return getXRequestId(response);
	}

	public String updateDeploymentStatus(String subscriptionId,
			String serviceName, String deploymentName, Status status) throws RestAPIException, InterruptedException, CommandLineException {
		String url = MNGMT_SERV_URL
				.replace(SUBSCRIPTION_ID, subscriptionId)
				.concat(LIST_HOST_SERV).concat(HOST_SERV)
				.replace(SERVICE_NAME, serviceName).concat(DPLY_NAME)
				.replace(DEPLOYMENT_NAME, deploymentName)
				.concat("/?comp=status");

		HashMap<String, String> headers = new HashMap<String, String>();

		headers.put(CONTENT_TYPE, MediaType.APPLICATION_XML);

		headers.put(X_MS_VERSION, "2009-10-01");

		UpdateDeploymentStatus body = new UpdateDeploymentStatus();

		body.setStatus(status);

		String result = WindowsAzureRestUtils.getInstance().runRest(HttpVerb.POST, url,
				headers, body, thumbprint);

		Response<?> response = ((Response<?>) deserialize(result));

		return getXRequestId(response);
	}

	public String createDeployment(String subscriptionId, String serviceName,
			String slotName, CreateDeployment body) throws RestAPIException , InterruptedException, CommandLineException{

		String url = MNGMT_SERV_URL
				.replace(SUBSCRIPTION_ID, subscriptionId)
				.concat(LIST_HOST_SERV).concat(HOST_SERV)
				.replace(SERVICE_NAME, serviceName.toLowerCase())
				.concat(CREATE_DPLY)
				.replace(DEPLOYMENT_SLOT_NAME, slotName);

		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put(CONTENT_TYPE, MediaType.APPLICATION_XML);

		addXMsVer2012(headers);

		addContentLength(headers, body);

		String result = WindowsAzureRestUtils.getInstance().runRest(HttpVerb.POST, url,
				headers, body, thumbprint, true); // pass a flag indicating we want the body to be saved to a file. instead of passing it as a string to the command line.

		Response<?> response = ((Response<?>) deserialize(result));

		validateResponse(response);

		return getXRequestId(response);
	}

	public String addCertificate(String subscriptionId, String serviceName,
			CertificateFile body) throws RestAPIException, InterruptedException, CommandLineException {

		String url = MNGMT_SERV_URL
				.replace(SUBSCRIPTION_ID, subscriptionId)
				.concat(LIST_HOST_SERV).concat(ADD_CERT)
				.replace(SERVICE_NAME, serviceName);
		HashMap<String, String> headers = new HashMap<String, String>();

		addXMsVer2012(headers);

		headers.put(CONTENT_TYPE, MediaType.APPLICATION_XML);

		String result = WindowsAzureRestUtils.getInstance().runRest(HttpVerb.POST, url,
				headers, body, thumbprint);

		Response<?> response = ((Response<?>) deserialize(result));

		return getXRequestId(response);
	}
	
	private void addXMsVer2012(HashMap<String, String> headers) {
		// addx_ms_version2012_03_01
		headers.put(X_MS_VERSION, "2012-03-01"); //$NON-NLS-1$
	}
}
