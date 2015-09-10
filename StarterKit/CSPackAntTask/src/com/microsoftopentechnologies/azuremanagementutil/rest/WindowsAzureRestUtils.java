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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.core.OperationResponse;
import com.microsoft.windowsazure.core.OperationStatusResponse;
import com.microsoft.windowsazure.core.utils.KeyStoreType;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.management.ManagementClient;
import com.microsoft.windowsazure.management.ManagementService;
import com.microsoft.windowsazure.management.compute.ComputeManagementClient;
import com.microsoft.windowsazure.management.compute.ComputeManagementService;
import com.microsoft.windowsazure.management.compute.models.DeploymentCreateParameters;
import com.microsoft.windowsazure.management.compute.models.DeploymentGetResponse;
import com.microsoft.windowsazure.management.compute.models.DeploymentSlot;
import com.microsoft.windowsazure.management.compute.models.DeploymentUpdateStatusParameters;
import com.microsoft.windowsazure.management.compute.models.DeploymentUpgradeParameters;
import com.microsoft.windowsazure.management.compute.models.HostedServiceCheckNameAvailabilityResponse;
import com.microsoft.windowsazure.management.compute.models.HostedServiceCreateParameters;
import com.microsoft.windowsazure.management.compute.models.HostedServiceGetDetailedResponse;
import com.microsoft.windowsazure.management.compute.models.HostedServiceListResponse;
import com.microsoft.windowsazure.management.compute.models.RoleInstance;
import com.microsoft.windowsazure.management.compute.models.ServiceCertificateCreateParameters;
import com.microsoft.windowsazure.management.compute.models.ServiceCertificateListResponse;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;
import com.microsoft.windowsazure.management.configuration.PublishSettingsLoader;
import com.microsoft.windowsazure.management.models.AffinityGroupListResponse;
import com.microsoft.windowsazure.management.models.LocationsListResponse;
import com.microsoft.windowsazure.management.models.SubscriptionGetResponse;
import com.microsoft.windowsazure.management.storage.StorageManagementClient;
import com.microsoft.windowsazure.management.storage.StorageManagementService;
import com.microsoft.windowsazure.management.storage.models.CheckNameAvailabilityResponse;
import com.microsoft.windowsazure.management.storage.models.StorageAccountCreateParameters;
import com.microsoft.windowsazure.management.storage.models.StorageAccountGetKeysResponse;
import com.microsoft.windowsazure.management.storage.models.StorageAccountGetResponse;
import com.microsoft.windowsazure.management.storage.models.StorageAccountListResponse;



public class WindowsAzureRestUtils {
	public final static String STORAGE_ACCOUNT_DEFAULT_ACCOUNT_TYPE = "Standard_GRS";
	static String publishSettExt = ".publishsettings";
	
	// Bill Pugh Singleton Implementation
	private WindowsAzureRestUtils(){}

	private static class SingletonHelper {
		private static final WindowsAzureRestUtils INSTANCE = new WindowsAzureRestUtils();
	}

	public static WindowsAzureRestUtils getInstance(){
		return SingletonHelper.INSTANCE;
	}
	
	public void launchRDP(DeploymentGetResponse deployment,
			String userName,
			String pluginFolder)
			throws IOException {
		List<RoleInstance> instances = deployment.getRoleInstances();

		RoleInstance instance = instances.get(0);

		StringBuilder command = new StringBuilder();

		URL url = deployment.getUri().toURL();

		command.append("full address:s:" + url.getHost() + "\r\n");
		command.append("username:s:" + userName + "\r\n");
		command.append(String.format(
				"LoadBalanceInfo:s:Cookie: mstshash=%s#%s",
				instance.getRoleName(), instance.getInstanceName()));

		String fileName = String.format("%s\\%s-%s.rdp", pluginFolder, deployment.getLabel(), instance.getInstanceName());

		File file = new File(fileName);
		boolean fileCreated = false;
		if (!file.exists()) {
			fileCreated = file.createNewFile();
		}

		if (fileCreated) {
			FileOutputStream output = new FileOutputStream(file);

			output.write(command.toString().getBytes("UTF-8"));

			output.close();
			String commandArgs[] = {"cmd.exe","/C",fileName};
			new ProcessBuilder(commandArgs).start();
		}

	}


	public static String installPublishSettings(File file, String subscriptionId, String password)
			throws Exception {
		try {
			if (password == null && file.getName().endsWith(publishSettExt)) {
				Configuration configuration = PublishSettingsLoader.createManagementConfiguration(file.getPath(), subscriptionId);
				String sId = (String) configuration.getProperty(ManagementConfiguration.SUBSCRIPTION_ID);
				return sId;
			}
		} catch (Exception ex) {
			throw ex;
		}
		return null;
	}

	public static Configuration getConfiguration(File file, String subscriptionId)
			throws IOException {
		try {
			Configuration configuration = PublishSettingsLoader.createManagementConfiguration(file.getPath(), subscriptionId);
			return configuration;
		} catch (IOException ex) {
			throw ex;
		}
	}

	public static SubscriptionGetResponse getSubscription(Configuration configuration)
			throws ServiceException, Exception {
		try {
			ManagementClient client = ManagementService.create(configuration);
			SubscriptionGetResponse response = client.getSubscriptionsOperations().get();
			return response;
		} catch (ServiceException ex) {
			throw ex;
		} catch (Exception ex) {
			throw ex;
		}
	}

	public static HostedServiceListResponse getHostedServices(Configuration configuration)
			throws ServiceException, Exception {
		try {
			ComputeManagementClient client = ComputeManagementService.create(configuration);
			HostedServiceListResponse response = client.getHostedServicesOperations().list();
			return response;
		} catch (ServiceException ex) {
			throw ex;
		} catch (Exception ex) {
			throw ex;
		}
	}

	public static LocationsListResponse getLocations(Configuration configuration)
			throws ServiceException, Exception {
		try {
			ManagementClient client = ManagementService.create(configuration);
			LocationsListResponse response = client.getLocationsOperations().list();
			return response;
		} catch (ServiceException ex) {
			throw ex;
		} catch (Exception ex) {
			throw ex;
		}
	}

	public static StorageAccountListResponse getStorageServices(Configuration configuration)
			throws ServiceException, Exception {
		try {
			StorageManagementClient client = StorageManagementService.create(configuration);
			StorageAccountListResponse response = client.getStorageAccountsOperations().list();
			return response;
		} catch (ServiceException ex) {
			throw ex;
		} catch (Exception ex) {
			throw ex;
		}
	}

	public static StorageAccountGetKeysResponse getStorageKeys(Configuration configuration, String serviceName)
			throws ServiceException, Exception {
		try {
			StorageManagementClient client = StorageManagementService.create(configuration);
			StorageAccountGetKeysResponse response = client.getStorageAccountsOperations().getKeys(serviceName);
			return response;
		} catch (ServiceException ex) {
			throw ex;
		} catch (Exception ex) {
			throw ex;
		}
	}

	public static OperationStatusResponse createStorageAccount(Configuration configuration, StorageAccountCreateParameters accountParameters)
			throws ServiceException, Exception {
		try {
			// set default account type is it is not already defined.
			if (accountParameters != null) {
				if (accountParameters.getAccountType() == null || accountParameters.getAccountType().trim().length() == 0) {
					accountParameters.setAccountType(STORAGE_ACCOUNT_DEFAULT_ACCOUNT_TYPE);
				}
			}
			
			StorageManagementClient client = StorageManagementService.create(configuration);
			OperationStatusResponse response = client.getStorageAccountsOperations().create(accountParameters);
			return response;
		} catch (ServiceException ex) {
			throw ex;
		} catch (Exception ex) {
			throw ex;
		}
	}

	public static StorageAccountGetResponse getStorageAccount(Configuration configuration, String serviceName)
			throws ServiceException, Exception {
		try {
			StorageManagementClient client = StorageManagementService.create(configuration);
			StorageAccountGetResponse response = client.getStorageAccountsOperations().get(serviceName);
			return response;
		} catch (ServiceException ex) {
			throw ex;
		} catch (Exception ex) {
			throw ex;
		}
	}

	public static CheckNameAvailabilityResponse checkStorageNameAvailability(Configuration configuration, String storageAccountName)
			throws ServiceException, Exception {
		try {
			StorageManagementClient client = StorageManagementService.create(configuration);
			CheckNameAvailabilityResponse response = client.getStorageAccountsOperations().checkNameAvailability(storageAccountName);
			return response;
		} catch (ServiceException ex) {
			throw ex;
		} catch (Exception ex) {
			throw ex;
		}
	}

	public static HostedServiceGetDetailedResponse getHostedServicesDetailed(Configuration configuration, String serviceName)
			throws ServiceException, Exception {
		try {
			ComputeManagementClient client = ComputeManagementService.create(configuration);
			HostedServiceGetDetailedResponse response = client.getHostedServicesOperations().getDetailed(serviceName);
			return response;
		} catch (ServiceException ex) {
			throw ex;
		} catch (Exception ex) {
			throw ex;
		}
	}

	public static OperationResponse createHostedService(Configuration configuration, HostedServiceCreateParameters hostedServiceCreateParameters)
			throws ServiceException, Exception {
		try {
			ComputeManagementClient client = ComputeManagementService.create(configuration);
			OperationResponse response = client.getHostedServicesOperations().create(hostedServiceCreateParameters);
			return response;
		} catch (ServiceException ex) {
			throw ex;
		} catch (Exception ex) {
			throw ex;
		}
	}

	public static HostedServiceCheckNameAvailabilityResponse checkHostedServiceNameAvailability(Configuration configuration, String hostedServiceName)
			throws ServiceException, Exception {
		try {
			ComputeManagementClient client = ComputeManagementService.create(configuration);
			HostedServiceCheckNameAvailabilityResponse response = client.getHostedServicesOperations().checkNameAvailability(hostedServiceName);
			return response;
		} catch (ServiceException ex) {
			throw ex;
		} catch (Exception ex) {
			throw ex;
		}
	}

	public static DeploymentGetResponse getDeployment(Configuration configuration, String serviceName, String deploymentName)
			throws ServiceException, Exception {
		try {
			ComputeManagementClient client = ComputeManagementService.create(configuration);
			DeploymentGetResponse response = client.getDeploymentsOperations().getByName(serviceName, deploymentName);
			return response;
		} catch (ServiceException ex) {
			throw ex;
		} catch (Exception ex) {
			throw ex;
		}
	}

	public static DeploymentGetResponse getDeploymentBySlot(Configuration configuration, String serviceName, DeploymentSlot deploymentSlot)
			throws ServiceException, Exception {
		try {
			ComputeManagementClient client = ComputeManagementService.create(configuration);
			DeploymentGetResponse response = client.getDeploymentsOperations().getBySlot(serviceName, deploymentSlot);
			return response;
		} catch (ServiceException ex) {
			throw ex;
		} catch (Exception ex) {
			throw ex;
		}
	}

	public static OperationResponse updateDeploymentStatus(Configuration configuration, String serviceName, String deploymentName,
			DeploymentUpdateStatusParameters deploymentStatus) throws ServiceException, Exception {
		try {
			ComputeManagementClient client = ComputeManagementService.create(configuration);
			OperationResponse response = client.getDeploymentsOperations().beginUpdatingStatusByDeploymentName(serviceName, deploymentName, deploymentStatus);
			return response;
		} catch (ServiceException ex) {
			throw ex;
		} catch (Exception ex) {
			throw ex;
		}
	}

	public static OperationResponse deleteDeployment(Configuration configuration, String serviceName, String deploymentName,
			boolean deleteFromStorage) throws ServiceException, Exception {
		try {
			ComputeManagementClient client = ComputeManagementService.create(configuration);
			OperationResponse response = client.getDeploymentsOperations().deleteByName(serviceName, deploymentName, deleteFromStorage);
			return response;
		} catch (ServiceException ex) {
			throw ex;
		} catch (IOException ex) {
			throw new Exception(ex);
		} catch (InterruptedException ex) {
			throw new Exception(ex);
		} catch (ExecutionException ex) {
			throw new Exception(ex);
		}
	}

	public static OperationStatusResponse getOperationStatus(Configuration configuration, String requestId)
			throws ServiceException, Exception {
		try {
			ManagementClient client = ManagementService.create(configuration);
			OperationStatusResponse response = client.getOperationStatus(requestId);
			return response;
		} catch (ServiceException ex) {
			throw ex;
		} catch (Exception ex) {
			throw ex;
		}
	}

	public static List<ServiceCertificateListResponse.Certificate> getCertificates(Configuration configuration, String serviceName)
			throws ServiceException, Exception {
		try {
			ComputeManagementClient client = ComputeManagementService.create(configuration);
			ServiceCertificateListResponse response = client.getServiceCertificatesOperations().list(serviceName);
			return response.getCertificates();
		} catch (ServiceException ex) {
			throw ex;
		} catch (Exception ex) {
			throw ex;
		}
	}

	public static OperationStatusResponse addCertificate(Configuration configuration, String serviceName, ServiceCertificateCreateParameters parameters)
			throws ServiceException, Exception {
		try {
			ComputeManagementClient client = ComputeManagementService.create(configuration);
			OperationStatusResponse response = client.getServiceCertificatesOperations().create(serviceName, parameters);
			return response;
		} catch (ServiceException ex) {
			throw ex;
		} catch (Exception ex) {
			throw ex;
		}
	}

	public static OperationStatusResponse createDeployment(Configuration configuration, String serviceName, DeploymentSlot deploymentSlot,
			DeploymentCreateParameters parameters)
					throws ServiceException, Exception {
		try {
			ComputeManagementClient client = ComputeManagementService.create(configuration);
			OperationStatusResponse response = client.getDeploymentsOperations().create(serviceName, deploymentSlot, parameters);
			return response;
		} catch (ServiceException ex) {
			throw ex;
		} catch (ExecutionException ex) {
			Throwable cause = ex.getCause();
			if (cause instanceof ServiceException) {
				throw (ServiceException)cause;
			}
			throw new Exception("Exception when create deployment", ex);
		} catch (Exception ex) {
			throw new Exception("Exception when create deployment", ex);
		}
	}

	public static OperationStatusResponse upgradeDeployment(Configuration configuration,
			String serviceName, DeploymentSlot deploymentSlot,
			DeploymentUpgradeParameters parameters) throws ServiceException, Exception {
		try {
			ComputeManagementClient client = ComputeManagementService.create(configuration);
			OperationStatusResponse response = client.getDeploymentsOperations().upgradeBySlot(serviceName, deploymentSlot, parameters);
			return response;
		} catch (ServiceException ex) {
			throw ex;
		} catch (ExecutionException ex) {
			Throwable cause = ex.getCause();
			if (cause instanceof ServiceException) {
				throw (ServiceException)cause;
			}
			throw new Exception("Exception when upgrading deployment", ex);
		} catch (Exception ex) {
			throw new Exception("Exception when upgrading deployment", ex);
		}
	}

	public static AffinityGroupListResponse listAffinityGroups(Configuration configuration)
			throws ServiceException, Exception {
		try {
			ManagementClient client = ManagementService.create(configuration);
			AffinityGroupListResponse response = client.getAffinityGroupsOperations().list();
			return response;
		} catch (ServiceException ex) {
			throw ex;
		} catch (Exception ex) {
			throw ex;
		}
	}

	public static Configuration loadConfiguration(String subscriptionId, String url) throws IOException {
		String keystore = System.getProperty("user.home") + File.separator + ".azure" + File.separator + subscriptionId + ".out";
		URI mngUri = URI.create(url);
		return ManagementConfiguration.configure(null, Configuration.load(), mngUri, subscriptionId, keystore, "", KeyStoreType.pkcs12);
	}
}
