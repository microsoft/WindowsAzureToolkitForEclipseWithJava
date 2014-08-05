/*******************************************************************************
 * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
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
import java.util.List;

import com.gigaspaces.azure.deploy.DeploymentManager;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.core.OperationResponse;
import com.microsoft.windowsazure.core.OperationStatusResponse;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.management.compute.models.*;
import com.microsoft.windowsazure.management.compute.models.ServiceCertificateListResponse.Certificate;
import com.microsoft.windowsazure.management.compute.models.HostedServiceListResponse.HostedService;
import com.microsoft.windowsazure.management.models.AffinityGroupListResponse;
import com.microsoft.windowsazure.management.models.LocationsListResponse.Location;
import com.gigaspaces.azure.model.ModelFactory;
import com.gigaspaces.azure.model.StorageService;
import com.gigaspaces.azure.model.Subscription;
import com.microsoft.windowsazure.management.models.LocationsListResponse;
import com.microsoft.windowsazure.management.models.SubscriptionGetResponse;
import com.microsoft.windowsazure.management.storage.models.*;
import com.microsoftopentechnologies.wacommon.utils.WACommonException;
import waeclipseplugin.Activator;

public class WindowsAzureServiceManagement extends WindowsAzureServiceImpl {


	public WindowsAzureServiceManagement() throws InvalidThumbprintException {
		super();
		context = ModelFactory.createInstance();
	}

	public Subscription getSubscription(Configuration configuration) throws WACommonException, ServiceException {
        SubscriptionGetResponse response;
        response = WindowsAzureRestUtils.getSubscription(configuration);
        return SubscriptionTransformer.transform(response);
	}

	public HostedServiceGetDetailedResponse getHostedServiceWithProperties(Configuration configuration, String serviceName) throws WACommonException {
        try {
            HostedServiceGetDetailedResponse response = WindowsAzureRestUtils.getHostedServicesDetailed(configuration, serviceName);
            return response;
        } catch (Exception ex) {
            Activator.getDefault().log("exception", ex);
            throw new WACommonException("Exception when getting storage keys", ex);
        }
	}

	public StorageService getStorageAccount(Configuration configuration, String serviceName) throws WACommonException, ServiceException {
        StorageService storageService = getStorageKeys(configuration, serviceName);
        StorageAccountGetResponse response = WindowsAzureRestUtils.getStorageAccount(configuration, serviceName);
        storageService.setStorageAccountProperties(response.getStorageAccount().getProperties());
        return storageService;
    }
	
	public boolean checkForStorageAccountDNSAvailability(Configuration configuration, final String storageAccountName)
            throws WACommonException, ServiceException {
        CheckNameAvailabilityResponse response = WindowsAzureRestUtils.checkStorageNameAvailability(configuration, storageAccountName);
        return response.isAvailable();
	}
	
	public boolean checkForCloudServiceDNSAvailability(Configuration configuration, final String hostedServiceName) throws WACommonException, ServiceException {
        HostedServiceCheckNameAvailabilityResponse response =
                WindowsAzureRestUtils.checkHostedServiceNameAvailability(configuration, hostedServiceName);
        return response.isAvailable();
	}

	public OperationStatusResponse getOperationStatus(Configuration configuration, String requestId) throws WACommonException, ServiceException {
        OperationStatusResponse response = WindowsAzureRestUtils.getOperationStatus(configuration, requestId);
        return response;
    }

	public synchronized List<StorageService> listStorageAccounts(Configuration configuration) throws WACommonException, ServiceException {
        List<StorageService> storageServices = new ArrayList<StorageService>();
        StorageAccountListResponse response;
        try {
            response = WindowsAzureRestUtils.getStorageServices(configuration);
        } catch (Exception ex) {
            Activator.getDefault().log("exception", ex);
            throw new WACommonException("Exception when getting storage services", ex);
        }

        for (StorageAccount ss : response.getStorageAccounts()) {
            StorageService storageService = getStorageKeys(configuration, ss.getName());
            storageService.setServiceName(ss.getName());
            storageService.setStorageAccountProperties(ss.getProperties());
            storageServices.add(storageService);
        }
		return storageServices;
	}

	public StorageService getStorageKeys(Configuration configuration, String serviceName) throws ServiceException, WACommonException {
        StorageAccountGetKeysResponse response = WindowsAzureRestUtils.getStorageKeys(configuration, serviceName);
        return new StorageService(serviceName, response);
    }

	public synchronized ArrayList<Location> listLocations(Configuration configuration) throws WACommonException, ServiceException {
        LocationsListResponse response = WindowsAzureRestUtils.getLocations(configuration);
        return response.getLocations();
    }

    /**
     * Note: this method is not currently used
     * @param configuration
     * @return
     * @throws WACommonException
     */
	public AffinityGroupListResponse listAffinityGroups(Configuration configuration) throws WACommonException, ServiceException {
        AffinityGroupListResponse response = WindowsAzureRestUtils.listAffinityGroups(configuration);
        return response;
	}

	public synchronized ArrayList<HostedService> listHostedServices(Configuration configuration) throws WACommonException, ServiceException {
        HostedServiceListResponse response = WindowsAzureRestUtils.getHostedServices(configuration);
        return response.getHostedServices();
    }
	
	public synchronized List<Certificate> listCertificates(Configuration configuration, String serviceName) throws WACommonException, ServiceException {
        List<Certificate> certificates = WindowsAzureRestUtils.getCertificates(configuration, serviceName);
        return certificates;
    }


	public String createHostedService(Configuration configuration, HostedServiceCreateParameters hostedServiceCreateParameters) throws
            WACommonException, ServiceException {
        OperationResponse response = WindowsAzureRestUtils.createHostedService(configuration, hostedServiceCreateParameters);
        return response.getRequestId();
    }


	public String createStorageAccount(Configuration configuration, StorageAccountCreateParameters accountParameters) throws ServiceException, WACommonException {

        return WindowsAzureRestUtils.createStorageAccount(configuration, accountParameters).getRequestId();
	}


	public DeploymentGetResponse getDeployment(Configuration configuration, String serviceName, String deploymentName) throws WACommonException, ServiceException {
        DeploymentGetResponse response = WindowsAzureRestUtils.getDeployment(configuration, serviceName, deploymentName);
        return response;
    }

	public String deleteDeployment(Configuration configuration, String serviceName, String deploymentName) throws WACommonException, ServiceException {
        OperationResponse response = WindowsAzureRestUtils.deleteDeployment(configuration, serviceName, deploymentName, true);
        return response.getRequestId();
    }

	public String updateDeploymentStatus(Configuration configuration, String serviceName, String deploymentName, UpdatedDeploymentStatus status)
            throws ServiceException, WACommonException {
        DeploymentUpdateStatusParameters deploymentStatus = new DeploymentUpdateStatusParameters();
        deploymentStatus.setStatus(status);
        OperationResponse response = WindowsAzureRestUtils.updateDeploymentStatus(configuration, serviceName, deploymentName, deploymentStatus);
        return response.getRequestId();
    }

    public String createDeployment(Configuration configuration,
                                   String serviceName,
                                   String slotName,
                                   DeploymentCreateParameters parameters,
                                   String unpublish)
            throws WACommonException, ServiceException {

        DeploymentSlot deploymentSlot;
        if (DeploymentSlot.Staging.toString().equalsIgnoreCase(slotName)) {
            deploymentSlot = DeploymentSlot.Staging;
        } else if (DeploymentSlot.Production.toString().equalsIgnoreCase(slotName)) {
            deploymentSlot = DeploymentSlot.Production;
        } else {
            throw new WACommonException("Invalid deployment slot name");
        }
        OperationStatusResponse response;
        try {
            response = WindowsAzureRestUtils.createDeployment(configuration, serviceName, deploymentSlot, parameters);
            return response.getRequestId();
        } catch (ServiceException ex) {
		    /*
            * If delete deployment option is selected and
		    * conflicting deployment exists then unpublish
		    * deployment first and then again try to publish.
		    */
            if (unpublish.equalsIgnoreCase("true") && ex.getHttpStatusCode() == 409) {
                HostedServiceGetDetailedResponse hostedServiceDetailed = getHostedServiceWithProperties(configuration, serviceName);
                List<HostedServiceGetDetailedResponse.Deployment> list = hostedServiceDetailed.getDeployments();
                String deploymentName = "";
                for (int i = 0; i < list.size(); i++) {
                    HostedServiceGetDetailedResponse.Deployment deployment = list.get(i);
                    if (deployment.getDeploymentSlot().name().equalsIgnoreCase(slotName)) {
                        deploymentName = deployment.getName();
                    }
                }
                int[] progressArr = new int[]{0, 0};
                DeploymentManager.getInstance().unPublish(configuration, serviceName, deploymentName, progressArr);
                response = WindowsAzureRestUtils.createDeployment(configuration, serviceName, deploymentSlot, parameters);

                return response.getRequestId();
            } else {
                Activator.getDefault().log(Messages.error, ex);
                throw ex;
            }
        }
    }

	public String addCertificate(Configuration configuration, String serviceName, ServiceCertificateCreateParameters createParameters)
            throws WACommonException, ServiceException {
        return WindowsAzureRestUtils.addCertificate(configuration, serviceName, createParameters).getRequestId();
	}
}
