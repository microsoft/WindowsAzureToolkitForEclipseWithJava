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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.core.OperationResponse;
import com.microsoft.windowsazure.core.OperationStatusResponse;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.management.compute.models.CertificateFormat;
import com.microsoft.windowsazure.management.compute.models.DeploymentCreateParameters;
import com.microsoft.windowsazure.management.compute.models.DeploymentGetResponse;
import com.microsoft.windowsazure.management.compute.models.DeploymentSlot;
import com.microsoft.windowsazure.management.compute.models.DeploymentUpdateStatusParameters;
import com.microsoft.windowsazure.management.compute.models.DeploymentUpgradeMode;
import com.microsoft.windowsazure.management.compute.models.DeploymentUpgradeParameters;
import com.microsoft.windowsazure.management.compute.models.HostedServiceCheckNameAvailabilityResponse;
import com.microsoft.windowsazure.management.compute.models.HostedServiceCreateParameters;
import com.microsoft.windowsazure.management.compute.models.HostedServiceGetDetailedResponse;
import com.microsoft.windowsazure.management.compute.models.HostedServiceListResponse;
import com.microsoft.windowsazure.management.compute.models.HostedServiceListResponse.HostedService;
import com.microsoft.windowsazure.management.compute.models.ServiceCertificateCreateParameters;
import com.microsoft.windowsazure.management.compute.models.ServiceCertificateListResponse.Certificate;
import com.microsoft.windowsazure.management.compute.models.UpdatedDeploymentStatus;
import com.microsoft.windowsazure.management.models.AffinityGroupListResponse;
import com.microsoft.windowsazure.management.models.LocationsListResponse;
import com.microsoft.windowsazure.management.models.LocationsListResponse.Location;
import com.microsoft.windowsazure.management.models.SubscriptionGetResponse;
import com.microsoft.windowsazure.management.storage.models.CheckNameAvailabilityResponse;
import com.microsoft.windowsazure.management.storage.models.StorageAccount;
import com.microsoft.windowsazure.management.storage.models.StorageAccountCreateParameters;
import com.microsoft.windowsazure.management.storage.models.StorageAccountGetKeysResponse;
import com.microsoft.windowsazure.management.storage.models.StorageAccountGetResponse;
import com.microsoft.windowsazure.management.storage.models.StorageAccountListResponse;
import com.microsoftopentechnologies.azuremanagementutil.exception.InvalidThumbprintException;
import com.microsoftopentechnologies.azuremanagementutil.model.ModelFactory;
import com.microsoftopentechnologies.azuremanagementutil.model.StorageService;
import com.microsoftopentechnologies.azuremanagementutil.model.Subscription;
import com.microsoftopentechnologies.azuremanagementutil.task.LoadStorageServiceTask;


public class WindowsAzureServiceManagement extends WindowsAzureServiceImpl {

	public WindowsAzureServiceManagement() throws InvalidThumbprintException {
		super();
		context = ModelFactory.createInstance();
	}

	public Subscription getSubscription(Configuration configuration)
			throws Exception, ServiceException {
		SubscriptionGetResponse response;
		response = WindowsAzureRestUtils.getSubscription(configuration);
		return SubscriptionTransformer.transform(response);
	}

	public HostedServiceGetDetailedResponse getHostedServiceWithProperties(
			Configuration configuration, String serviceName)
					throws Exception {
		try {
			HostedServiceGetDetailedResponse response = WindowsAzureRestUtils.
					getHostedServicesDetailed(configuration, serviceName);
			return response;
		} catch (Exception ex) {
			throw new Exception("Exception when getting storage keys", ex);
		}
	}

	public StorageService getStorageAccount(Configuration configuration, String serviceName)
			throws Exception, ServiceException {
		StorageService storageService = getStorageKeys(configuration, serviceName);
		StorageAccountGetResponse response = WindowsAzureRestUtils.getStorageAccount(configuration, serviceName);
		storageService.setStorageAccountProperties(response.getStorageAccount().getProperties());
		return storageService;
	}

	public boolean checkForStorageAccountDNSAvailability(
			Configuration configuration, final String storageAccountName)
					throws Exception, ServiceException {
		CheckNameAvailabilityResponse response = WindowsAzureRestUtils.checkStorageNameAvailability(configuration, storageAccountName);
		return response.isAvailable();
	}

	public boolean checkForCloudServiceDNSAvailability(
			Configuration configuration, final String hostedServiceName)
					throws Exception, ServiceException {
		HostedServiceCheckNameAvailabilityResponse response =
				WindowsAzureRestUtils.checkHostedServiceNameAvailability(configuration, hostedServiceName);
		return response.isAvailable();
	}

	public OperationStatusResponse getOperationStatus(
			Configuration configuration, String requestId)
					throws Exception, ServiceException {
		OperationStatusResponse response = WindowsAzureRestUtils.getOperationStatus(configuration, requestId);
		return response;
	}
	
	public synchronized List<StorageService> listStorageAccounts(Configuration configuration)
			throws Exception, ServiceException {
        List<StorageService> storageServices = new ArrayList<StorageService>();
        StorageAccountListResponse response;
        try {
            response = WindowsAzureRestUtils.getStorageServices(configuration);
        } catch (Exception ex) {
        	throw new Exception("Exception when getting storage services", ex);
        }
        
        if (response.getStorageAccounts() != null && response.getStorageAccounts().size() > 0) {
	        ExecutorService executorService = Executors.newCachedThreadPool();
	        
	        List<Callable<StorageService>> taskList = new ArrayList<Callable<StorageService>>();
	        
	        // Prepare tasks for parallel execution
	        for (StorageAccount ss : response.getStorageAccounts()) {
	        	LoadStorageServiceTask storage = new LoadStorageServiceTask();
	        	storage.setConfiguration(configuration);
	        	storage.setStorageAccount(ss);
	        	taskList.add(storage);
	        }
	        
	        // Execute tasks
	        // see if it is possible to gracefully ignore individual task failures
	        try {
	        	List<Future<StorageService>> taskResultList = null;
				try {
					taskResultList = executorService.invokeAll(taskList);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
	        	
				if (taskResultList != null) {
		        	for(Future<StorageService> taskResult : taskResultList) {
		        		// Get will block until time expires or until task completes
				    	StorageService storageService = taskResult.get(60, TimeUnit.SECONDS);
				    	storageServices.add(storageService);
		        	}
				}
	        } catch (TimeoutException timeoutException) {
	        	throw new Exception("Timed out occurred while getting storage services information, please try again", timeoutException);
	        } catch (Exception e) {
	        	throw new Exception("Exception when getting storage services", e);
	        } finally {
	        	executorService.shutdown();
	        }
        }
		return storageServices;
	}

	public static StorageService getStorageKeys(Configuration configuration, String serviceName)
			throws ServiceException, Exception {
		StorageAccountGetKeysResponse response = WindowsAzureRestUtils.getStorageKeys(configuration, serviceName);
		return new StorageService(serviceName, response);
	}

	public ArrayList<Location> listLocations(Configuration configuration)
			throws Exception, ServiceException {
		LocationsListResponse response = WindowsAzureRestUtils.getLocations(configuration);
		return response.getLocations();
	}

	/**
	 * Note: this method is not currently used
	 * @param configuration
	 * @return
	 * @throws WACommonException
	 */
	public AffinityGroupListResponse listAffinityGroups(Configuration configuration)
			throws Exception, ServiceException {
		AffinityGroupListResponse response = WindowsAzureRestUtils.listAffinityGroups(configuration);
		return response;
	}

	public ArrayList<HostedService> listHostedServices(Configuration configuration)
			throws Exception, ServiceException {
		HostedServiceListResponse response = WindowsAzureRestUtils.getHostedServices(configuration);
		return response.getHostedServices();
	}

	public List<Certificate> listCertificates(Configuration configuration, String serviceName)
			throws Exception, ServiceException {
		List<Certificate> certificates = WindowsAzureRestUtils.getCertificates(configuration, serviceName);
		return certificates;
	}


	public String createHostedService(Configuration configuration,
			HostedServiceCreateParameters hostedServiceCreateParameters) throws
			Exception, ServiceException {
		OperationResponse response = WindowsAzureRestUtils.createHostedService(
				configuration, hostedServiceCreateParameters);
		return response.getRequestId();
	}
	
	/**
	 * Creates cloud service if it does not exist in subscription.
	 * @param configuration
	 * @param cloudServiceName
	 * @param region
	 * @throws ServiceException
	 * @throws Exception
	 */
	public void createCloudServiceIfNotExists(Configuration configuration,
			String cloudServiceName, String region) throws Exception {
		boolean isCloudServiceExist = false;
		ArrayList<HostedService> list = listHostedServices(configuration);
		for (HostedService hostedService : list) {
			if (hostedService.getServiceName().equalsIgnoreCase(cloudServiceName)) {
				isCloudServiceExist = true;
				break;
			}
		}
		if (!isCloudServiceExist) {
			HostedServiceCreateParameters createHostedService = new HostedServiceCreateParameters();
			createHostedService.setServiceName(cloudServiceName);
			createHostedService.setLabel(cloudServiceName);
			createHostedService.setLocation(region);
			createHostedService.setDescription(cloudServiceName);

			createHostedService(configuration, createHostedService);
		}
	}

	public String createStorageAccount(Configuration configuration,
			StorageAccountCreateParameters accountParameters)
					throws ServiceException, Exception {
		return WindowsAzureRestUtils.createStorageAccount(configuration, accountParameters).getRequestId();
	}

	/**
	 * Creates storage account if it does not exist in subscription. 
	 * @param configuration
	 * @param storageAccountName
	 * @param region
	 * @throws Exception
	 */
	public void createStorageAccountIfNotExists(Configuration configuration,
			String storageAccountName, String region) throws Exception {
		boolean isStorageAccountExist = false;
		List<StorageService> storageAccountList = listStorageAccounts(configuration);
		for (StorageService storageService : storageAccountList) {
			if (storageService.getServiceName().equalsIgnoreCase(storageAccountName)) {
				isStorageAccountExist = true;
				break;
			}
		}
		if (!isStorageAccountExist) {
			StorageAccountCreateParameters accountParameters = new StorageAccountCreateParameters();
			accountParameters.setName(storageAccountName);
			accountParameters.setLabel(storageAccountName);
			accountParameters.setLocation(region);
			accountParameters.setDescription(storageAccountName);
			createStorageAccount(configuration, accountParameters);
		}
	}

	public DeploymentGetResponse getDeployment(Configuration configuration,
			String serviceName, String deploymentName)
					throws Exception, ServiceException {
		DeploymentGetResponse response = WindowsAzureRestUtils.
				getDeployment(configuration, serviceName, deploymentName);
		return response;
	}

	public String deleteDeployment(Configuration configuration,
			String serviceName, String deploymentName)
					throws Exception, ServiceException {
		OperationResponse response = WindowsAzureRestUtils.
				deleteDeployment(configuration, serviceName, deploymentName, true);
		return response.getRequestId();
	}

	public String updateDeploymentStatus(Configuration configuration,
			String serviceName, String deploymentName, UpdatedDeploymentStatus status)
					throws ServiceException, Exception {
		DeploymentUpdateStatusParameters deploymentStatus = new DeploymentUpdateStatusParameters();
		deploymentStatus.setStatus(status);
		OperationResponse response = WindowsAzureRestUtils.updateDeploymentStatus(
				configuration, serviceName, deploymentName, deploymentStatus);
		return response.getRequestId();
	}

	public String createDeployment(Configuration configuration,
			String serviceName,
			String slotName,
			DeploymentCreateParameters parameters,
			String unpublish)
					throws Exception, ServiceException {

		DeploymentSlot deploymentSlot;
		if (DeploymentSlot.Staging.toString().equalsIgnoreCase(slotName)) {
			deploymentSlot = DeploymentSlot.Staging;
		} else if (DeploymentSlot.Production.toString().equalsIgnoreCase(slotName)) {
			deploymentSlot = DeploymentSlot.Production;
		} else {
			throw new Exception("Invalid deployment slot name");
		}
		OperationStatusResponse response;
		try {
			response = WindowsAzureRestUtils.createDeployment(configuration, serviceName, deploymentSlot, parameters);
			return response.getRequestId();
		} catch (ServiceException ex) {
			/*
			 * If delete deployment option is selected and
			 * conflicting deployment exists then upgrade deployment.
			 */
			if (unpublish.equalsIgnoreCase("true") && ex.getHttpStatusCode() == 409) {
				DeploymentUpgradeParameters upgradeParameters = new DeploymentUpgradeParameters();
				upgradeParameters.setConfiguration(parameters.getConfiguration());
				upgradeParameters.setForce(true);
				upgradeParameters.setLabel(parameters.getName());
				upgradeParameters.setMode(DeploymentUpgradeMode.Auto);
				upgradeParameters.setPackageUri(parameters.getPackageUri());
				response = WindowsAzureRestUtils.upgradeDeployment(
						configuration, serviceName, deploymentSlot, upgradeParameters);
				return response.getRequestId();
			} else {
				throw ex;
			}
		}
	}

	public String addCertificate(Configuration configuration,
			String serviceName,
			ServiceCertificateCreateParameters createParameters)
					throws Exception, ServiceException {
		return WindowsAzureRestUtils.addCertificate(configuration, serviceName, createParameters).getRequestId();
	}
	
	public void uploadCertificate(Configuration configuration,
			String cloudServiceName,
			String pfxPath, String pfxPwd) throws Exception {
		File pfxFile = new File(pfxPath);
		byte[] buff = new byte[(int) pfxFile.length()];
		FileInputStream fileInputStram = null;
		DataInputStream dis = null;
		try {
			fileInputStram = new FileInputStream(pfxFile);
			dis = new DataInputStream(fileInputStram);
			dis.readFully(buff);
		}
		finally {
			if (fileInputStram != null) {
				fileInputStram.close();
			}
			if (dis != null) {
				dis.close();
			}
		}
		ServiceCertificateCreateParameters createParameters = new ServiceCertificateCreateParameters();
		createParameters.setData(buff);
		createParameters.setPassword(pfxPwd);
		createParameters.setCertificateFormat(CertificateFormat.Pfx);
		addCertificate(configuration, cloudServiceName, createParameters);
	}
}
