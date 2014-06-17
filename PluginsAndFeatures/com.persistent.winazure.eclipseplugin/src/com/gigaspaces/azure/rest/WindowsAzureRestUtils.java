// CHECKSTYLE:OFF


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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.core.OperationResponse;
import com.microsoft.windowsazure.core.OperationStatusResponse;
import com.microsoft.windowsazure.core.utils.KeyStoreType;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.management.ManagementClient;
import com.microsoft.windowsazure.management.ManagementService;
import com.microsoft.windowsazure.management.compute.ComputeManagementClient;
import com.microsoft.windowsazure.management.compute.ComputeManagementService;
import com.microsoft.windowsazure.management.compute.models.*;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;
import com.microsoft.windowsazure.management.configuration.PublishSettingsLoader;
import com.microsoft.windowsazure.management.models.AffinityGroupListResponse;
import com.microsoft.windowsazure.management.models.LocationsListResponse;
import com.microsoft.windowsazure.management.models.SubscriptionGetResponse;
import com.microsoft.windowsazure.management.storage.StorageManagementClient;
import com.microsoft.windowsazure.management.storage.StorageManagementService;
import com.microsoft.windowsazure.management.storage.models.*;
import com.microsoftopentechnologies.wacommon.utils.WACommonException;
import org.eclipse.core.runtime.Platform;

import waeclipseplugin.Activator;
import com.microsoftopentechnologies.wacommon.utils.Base64;
import com.gigaspaces.azure.util.Messages;
import com.gigaspaces.azure.util.PublishData;
import com.gigaspaces.azure.util.PublishProfile;

public class WindowsAzureRestUtils {

	private static String pluginFolder;

	private static WindowsAzureRestUtils instance;

	public static synchronized WindowsAzureRestUtils getInstance() {

		if (instance == null) {
			String eclipseInstallation = Platform.getInstallLocation().getURL().getPath();
			if (eclipseInstallation.charAt(0) == '/'
					|| eclipseInstallation.charAt(0) == '\\') {
				eclipseInstallation = eclipseInstallation.substring(1);
			}
			eclipseInstallation = eclipseInstallation.replace("/",
					File.separator);
			pluginFolder = String.format("%s%s%s%s%s", eclipseInstallation,
					File.separator, com.persistent.util.Messages.pluginFolder,
					File.separator, com.persistent.util.Messages.pluginId);

			instance = new WindowsAzureRestUtils();
		}

		return instance;
	}

	public void launchRDP(DeploymentGetResponse deployment, String userName) {
		try {

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

		} catch (IOException e) {
			Activator.getDefault().log(Messages.error, e);
		}
	}

	public static PublishData parse(File file) throws JAXBException {

		PublishData publishData = null;

		try {
			JAXBContext context = JAXBContext.newInstance(PublishData.class);

			publishData = (PublishData) context.createUnmarshaller().unmarshal(
					file);
		} catch (JAXBException e) {
			Activator.getDefault().log(Messages.error, e);
			throw e;
		}

		return publishData;
	}

	public static PublishData parsePfx(File file) throws Exception {
		PublishData publishData = new PublishData();

		PublishProfile profile = new PublishProfile();
		FileInputStream input = null;
		DataInputStream dis = null;

		byte[] buff = new byte[(int) file.length()];
		try {
			input = new FileInputStream(file);
			dis = new DataInputStream(input);
			dis.readFully(buff);
			profile.setManagementCertificate(Base64.encode(buff)); //$NON-NLS-1$
		} catch (FileNotFoundException e) {
			Activator.getDefault().log(Messages.error, e);
			throw e;
		} catch (IOException e) {
			Activator.getDefault().log(Messages.error, e);
			throw e;
		} finally {
			if (input != null) {
				input.close();
			}
			if (dis != null) {
				dis.close();
			}
		}

		publishData.setPublishProfile(profile);

		return publishData;
	}

    public static String installPublishSettings(File file, String subscriptionId, String password) throws Exception {
        try {
            if (password == null && file.getName().endsWith(com.gigaspaces.azure.wizards.Messages.publishSettExt)) {
                Configuration configuration = PublishSettingsLoader.createManagementConfiguration(file.getPath(), subscriptionId);
                String sId = (String) configuration.getProperty(ManagementConfiguration.SUBSCRIPTION_ID);
                Activator.getDefault().log("SubscriptionId is: " + sId);
                return sId;
            }
        } catch (Exception ex) {
            Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
            throw ex;
        }
        return null;
    }

    public static Configuration getConfiguration(File file, String subscriptionId) throws IOException {
        try {
            Configuration configuration = PublishSettingsLoader.createManagementConfiguration(file.getPath(), subscriptionId);
            Activator.getDefault().log("Created configuration for subscriptionId: " + subscriptionId);
            return configuration;
        } catch (IOException ex) {
            Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
            throw ex;
        }
    }

    public static SubscriptionGetResponse getSubscription(Configuration configuration) throws ServiceException, WACommonException {
        try {
            ManagementClient client = ManagementService.create(configuration);
            SubscriptionGetResponse response = client.getSubscriptionsOperations().get();
            return response;
        } catch (ServiceException ex) {
            Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
            throw ex;
        } catch (Exception ex) {
            Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
            throw new WACommonException(com.gigaspaces.azure.rest.Messages.error, ex);
        }
    }

    public static HostedServiceListResponse getHostedServices(Configuration configuration) throws ServiceException, WACommonException {
        try {
            ComputeManagementClient client = ComputeManagementService.create(configuration);
            HostedServiceListResponse response = client.getHostedServicesOperations().list();
            return response;
        } catch (ServiceException ex) {
            Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
            throw ex;
        } catch (Exception ex) {
            Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
            throw new WACommonException(com.gigaspaces.azure.rest.Messages.error, ex);
        }
    }

    public static LocationsListResponse getLocations(Configuration configuration) throws ServiceException, WACommonException {
        try {
            ManagementClient client = ManagementService.create(configuration);
            LocationsListResponse response = client.getLocationsOperations().list();
            return response;
        } catch (ServiceException ex) {
            Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
            throw ex;
        } catch (Exception ex) {
            Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
            throw new WACommonException(com.gigaspaces.azure.rest.Messages.error, ex);
        }
    }

    public static StorageAccountListResponse getStorageServices(Configuration configuration) throws ServiceException, WACommonException {
        try {
            StorageManagementClient client = StorageManagementService.create(configuration);
            StorageAccountListResponse response = client.getStorageAccountsOperations().list();
            return response;
        } catch (ServiceException ex) {
            Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
            throw ex;
        } catch (Exception ex) {
            Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
            throw new WACommonException(com.gigaspaces.azure.rest.Messages.error, ex);
        }
    }

    public static StorageAccountGetKeysResponse getStorageKeys(Configuration configuration, String serviceName)
            throws ServiceException, WACommonException {
        try {
            StorageManagementClient client = StorageManagementService.create(configuration);
            StorageAccountGetKeysResponse response = client.getStorageAccountsOperations().getKeys(serviceName);
            return response;
        } catch (ServiceException ex) {
            Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
            throw ex;
        } catch (Exception ex) {
            Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
            throw new WACommonException(com.gigaspaces.azure.rest.Messages.error, ex);
        }
    }

    public static OperationStatusResponse createStorageAccount(Configuration configuration, StorageAccountCreateParameters accountParameters)
            throws ServiceException, WACommonException {
        try {
            StorageManagementClient client = StorageManagementService.create(configuration);
            OperationStatusResponse response = client.getStorageAccountsOperations().create(accountParameters);
            return response;
        } catch (ServiceException ex) {
            Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
            throw ex;
        } catch (Exception ex) {
            Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
            throw new WACommonException(com.gigaspaces.azure.rest.Messages.error, ex);
        }
    }

    public static StorageAccountGetResponse getStorageAccount(Configuration configuration, String serviceName)
            throws ServiceException, WACommonException {
        try {
            StorageManagementClient client = StorageManagementService.create(configuration);
            StorageAccountGetResponse response = client.getStorageAccountsOperations().get(serviceName);
            return response;
        } catch (ServiceException ex) {
            Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
            throw ex;
        } catch (Exception ex) {
            Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
            throw new WACommonException(com.gigaspaces.azure.rest.Messages.error, ex);
        }
    }

    public static CheckNameAvailabilityResponse checkStorageNameAvailability(Configuration configuration, String storageAccountName)
            throws ServiceException, WACommonException {
        try {
            StorageManagementClient client = StorageManagementService.create(configuration);
            CheckNameAvailabilityResponse response = client.getStorageAccountsOperations().checkNameAvailability(storageAccountName);
            return response;
        } catch (ServiceException ex) {
            Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
            throw ex;
        } catch (Exception ex) {
            Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
            throw new WACommonException(com.gigaspaces.azure.rest.Messages.error, ex);
        }
    }

    public static HostedServiceGetDetailedResponse getHostedServicesDetailed(Configuration configuration, String serviceName)
            throws ServiceException, WACommonException {
        try {
            ComputeManagementClient client = ComputeManagementService.create(configuration);
            HostedServiceGetDetailedResponse response = client.getHostedServicesOperations().getDetailed(serviceName);
            return response;
        } catch (ServiceException ex) {
            Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
            throw ex;
        } catch (Exception ex) {
            Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
            throw new WACommonException(com.gigaspaces.azure.rest.Messages.error, ex);
        }
    }

    public static OperationResponse createHostedService(Configuration configuration, HostedServiceCreateParameters hostedServiceCreateParameters)
            throws ServiceException, WACommonException {
        try {
            ComputeManagementClient client = ComputeManagementService.create(configuration);
            OperationResponse response = client.getHostedServicesOperations().create(hostedServiceCreateParameters);
            return response;
        } catch (ServiceException ex) {
            Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
            throw ex;
        } catch (Exception ex) {
            Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
            throw new WACommonException(com.gigaspaces.azure.rest.Messages.error, ex);
        }
    }

    public static HostedServiceCheckNameAvailabilityResponse checkHostedServiceNameAvailability(Configuration configuration, String hostedServiceName)
            throws ServiceException, WACommonException {
        try {
            ComputeManagementClient client = ComputeManagementService.create(configuration);
            HostedServiceCheckNameAvailabilityResponse response = client.getHostedServicesOperations().checkNameAvailability(hostedServiceName);
            return response;
        } catch (ServiceException ex) {
            Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
            throw ex;
        } catch (Exception ex) {
            Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
            throw new WACommonException(com.gigaspaces.azure.rest.Messages.error, ex);
        }
    }

    public static DeploymentGetResponse getDeployment(Configuration configuration, String serviceName, String deploymentName)
            throws ServiceException, WACommonException {
        try {
            ComputeManagementClient client = ComputeManagementService.create(configuration);
            DeploymentGetResponse response = client.getDeploymentsOperations().getByName(serviceName, deploymentName);
            return response;
        } catch (ServiceException ex) {
            Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
            throw ex;
        } catch (Exception ex) {
            Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
            throw new WACommonException(com.gigaspaces.azure.rest.Messages.error, ex);
        }
    }

    public static OperationResponse updateDeploymentStatus(Configuration configuration, String serviceName, String deploymentName,
                                                           DeploymentUpdateStatusParameters deploymentStatus) throws ServiceException, WACommonException {
        try {
            ComputeManagementClient client = ComputeManagementService.create(configuration);
            OperationResponse response = client.getDeploymentsOperations().beginUpdatingStatusByDeploymentName(serviceName, deploymentName, deploymentStatus);
            return response;
        } catch (ServiceException ex) {
            Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
            throw ex;
        } catch (Exception ex) {
            Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
            throw new WACommonException(com.gigaspaces.azure.rest.Messages.error, ex);
        }
    }

    public static OperationResponse deleteDeployment(Configuration configuration, String serviceName, String deploymentName,
                                                     boolean deleteFromStorage) throws ServiceException, WACommonException {
        try {
            ComputeManagementClient client = ComputeManagementService.create(configuration);
            OperationResponse response = client.getDeploymentsOperations().deleteByName(serviceName, deploymentName, deleteFromStorage);
            return response;
        } catch (ServiceException ex) {
            Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
            throw ex;
        } catch (IOException ex) {
        	Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
        	throw new WACommonException(com.gigaspaces.azure.rest.Messages.error, ex);
        } catch (InterruptedException ex) {
        	Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
        	throw new WACommonException(com.gigaspaces.azure.rest.Messages.error, ex);
        } catch (ExecutionException ex) {
        	Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
        	throw new WACommonException(com.gigaspaces.azure.rest.Messages.error, ex);
        }
    }

    public static OperationStatusResponse getOperationStatus(Configuration configuration, String requestId)
            throws ServiceException, WACommonException {
        try {
            ManagementClient client = ManagementService.create(configuration);
            OperationStatusResponse response = client.getOperationStatus(requestId);
            return response;
        } catch (ServiceException ex) {
            Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
            throw ex;
        } catch (Exception ex) {
            Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
            throw new WACommonException(com.gigaspaces.azure.rest.Messages.error, ex);
        }
    }

    public static List<ServiceCertificateListResponse.Certificate> getCertificates(Configuration configuration, String serviceName)
            throws ServiceException, WACommonException {
        try {
            ComputeManagementClient client = ComputeManagementService.create(configuration);
            ServiceCertificateListResponse response = client.getServiceCertificatesOperations().list(serviceName);
            return response.getCertificates();
        } catch (ServiceException ex) {
            Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
            throw ex;
        } catch (Exception ex) {
            Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
            throw new WACommonException(com.gigaspaces.azure.rest.Messages.error, ex);
        }
    }

    public static OperationStatusResponse addCertificate(Configuration configuration, String serviceName, ServiceCertificateCreateParameters parameters)
            throws ServiceException, WACommonException {
        try {
            ComputeManagementClient client = ComputeManagementService.create(configuration);
            OperationStatusResponse response = client.getServiceCertificatesOperations().create(serviceName, parameters);
            return response;
        } catch (ServiceException ex) {
            Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
            throw ex;
        } catch (Exception ex) {
            Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
            throw new WACommonException(com.gigaspaces.azure.rest.Messages.error, ex);
        }
    }

    public static OperationStatusResponse createDeployment(Configuration configuration, String serviceName, DeploymentSlot deploymentSlot,
                                                           DeploymentCreateParameters parameters)
            throws ServiceException, WACommonException {
        try {
            ComputeManagementClient client = ComputeManagementService.create(configuration);
            OperationStatusResponse response = client.getDeploymentsOperations().create(serviceName, deploymentSlot, parameters);
            return response;
        } catch (ServiceException ex) {
            Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
            throw ex;
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof ServiceException) {
                Activator.getDefault().log("status is: " + ((ServiceException)cause).getHttpStatusCode() + " error code: " + ((ServiceException)cause).getErrorCode());
                throw (ServiceException)cause;
            }
            throw new WACommonException("Exception when create deployment", ex);
        } catch (Exception ex) {
            Activator.getDefault().log("ExceptionType is: " + ex.getClass().getName() + " package is: " + ex.getClass().getPackage().getName());
            throw new WACommonException("Exception when create deployment", ex);
        }
    }

    public static AffinityGroupListResponse listAffinityGroups(Configuration configuration) throws ServiceException, WACommonException {
        try {
            ManagementClient client = ManagementService.create(configuration);
            AffinityGroupListResponse response = client.getAffinityGroupsOperations().list();
            return response;
        } catch (ServiceException ex) {
            Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
            throw ex;
        } catch (Exception ex) {
            Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, ex);
            throw new WACommonException(com.gigaspaces.azure.rest.Messages.error, ex);
        }
    }

    public static Configuration loadConfiguration(String subscriptionId, String url) throws IOException {
        String keystore = System.getProperty("user.home") + File.separator + ".azure" + File.separator + subscriptionId + ".out";
        URI mngUri = URI.create(url);
        return ManagementConfiguration.configure(mngUri, subscriptionId, keystore, "", KeyStoreType.pkcs12);
    }
}
