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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import waeclipseplugin.Activator;

import com.gigaspaces.azure.deploy.Notifier;
import com.gigaspaces.azure.model.EnumerationResults;
import com.gigaspaces.azure.model.Response;
import com.gigaspaces.azure.model.StorageService;
import com.gigaspaces.azure.util.CommandLineException;
import com.microsoft.windowsazure.services.blob.client.BlobContainerPermissions;
import com.microsoft.windowsazure.services.blob.client.BlobContainerPublicAccessType;
import com.microsoft.windowsazure.services.blob.client.CloudBlobClient;
import com.microsoft.windowsazure.services.blob.client.CloudBlobContainer;
import com.microsoft.windowsazure.services.blob.client.CloudBlockBlob;
import com.microsoft.windowsazure.services.core.storage.CloudStorageAccount;
import com.microsoft.windowsazure.services.core.storage.RetryNoRetry;
import com.microsoft.windowsazure.services.core.storage.StorageCredentialsAccountAndKey;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoftopentechnologies.wacommon.utils.WACommonException;


public class WindowsAzureStorageServices extends WindowsAzureServiceImpl {

	private static final int TIME_OUT = 500 * 1000; //500 seconds
	private StorageService storageAccount;
	private String storageKey;
	private final static int NTHREAD = 4;

	public WindowsAzureStorageServices(StorageService storageAccount, String storageKey)
			throws NoSuchAlgorithmException, InvalidKeyException {
		super();
		if (storageAccount == null)
			throw new InvalidRestAPIArgument(Messages.invalidStrgAcc);
		this.storageAccount = storageAccount;
		if (storageKey == null || storageKey.isEmpty())
			throw new InvalidRestAPIArgument(Messages.invalidStorageKey);
		this.storageKey = storageKey;
	}

	public EnumerationResults listContainers() throws WACommonException, InvalidKeyException,
	RestAPIException, InterruptedException, CommandLineException {
		String url = storageAccount.
				getStorageServiceProperties().getEndpoints().
				getEndpoints().get(0).concat(LIST_CONTAINERS);

		HashMap<String, String> headers = new HashMap<String, String>();

		headers.put(X_MS_VERSION, Messages.xMsVersion);

		String result = WindowsAzureRestUtils.getInstance().runStorage(HttpVerb.GET, url,storageKey, headers, null);

		Response<?> response = (Response<?>) deserialize(result);

		validateResponse(response);

		return (EnumerationResults) response.getBody();
	}

	/**
	 * Method adds file to blob storage.
	 * @param container
	 * @param blobName
	 * @param file
	 * @param notifier
	 * @throws CommandLineException
	 * @throws FileNotFoundException
	 */
	public void putBlob(String container, String blobName, File file, Notifier notifier) throws
	CommandLineException, FileNotFoundException {
		long time1 = System.currentTimeMillis();
		CloudBlockBlob blob = null;
		CloudBlobContainer 	cloudBlobContainer = null;

		// setting option to use existing system default proxy
		System.setProperty("java.net.useSystemProxies", "true");
		FileInputStream fis = new FileInputStream(file);
		BufferedInputStream bis = new BufferedInputStream(fis);

		try {
			cloudBlobContainer = getBlobContainerReference(
					storageAccount, storageKey, container, true, true, false, NTHREAD);
			blob = cloudBlobContainer.getBlockBlobReference(blobName);
			blob.upload(bis, file.length());
		} catch (Exception e) {
			e.printStackTrace();
			Activator.getDefault().log(Messages.upExcpTrce, e);
			throw new CommandLineException(Messages.upErr);
		} finally {
			try {
				if (bis != null) {
					bis.close();
				}
			} catch (Exception e) {
				// ignore if there is any exception
			}
		}

		long time2 = System.currentTimeMillis();
		String timeTaken = ((time2 - time1) / 1000) +" seconds";
		Activator.getDefault().log(Messages.upTime
				+ file.getName()
				+ " is "
				+ timeTaken);
	}

	/**
	 * Method deletes specified blob.
	 * @param container
	 * @param blobName
	 * @param file
	 * @param notifier
	 * @throws CommandLineException
	 * @throws FileNotFoundException
	 */
	public void deleteBlob(String container, String blobName, Notifier notifier) throws
	CommandLineException, FileNotFoundException {
		CloudBlockBlob blob = null;
		CloudBlobContainer 	cloudBlobContainer = null;
		// setting option to use existing system default proxy
		System.setProperty("java.net.useSystemProxies", "true");
		try {
			cloudBlobContainer = getBlobContainerReference(storageAccount,
					storageKey, container, true, true, false, NTHREAD);
			blob = cloudBlobContainer.getBlockBlobReference(blobName);
			blob.deleteIfExists();
		} catch (Exception e) {
			e.printStackTrace();
			Activator.getDefault().log(Messages.delExcpTrce, e);
			throw new CommandLineException(Messages.delErr);
		}
	}

	/**
	 * Returns reference of Azure cloud blob container.
	 * @param strAccName 	storage account name
	 * @param key     		storage account primary access key
	 * @param containerName name of the container
	 * @param createCnt 	Indicates if container needs to be created
	 * @param allowRetry 	sets retry policy
	 * @param cntPubAccess 	Permissions for container
	 * @param concurrentRequestCount sets max number of concurrent requests
	 * @return              reference of CloudBlobContainer
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws WACommonException
	 */
	private CloudBlobContainer getBlobContainerReference(StorageService strAccName, String key, String containerName, 
			boolean createCnt, boolean allowRetry, Boolean cntPubAccess, int concurrentRequestCount) 
	throws URISyntaxException, StorageException, WACommonException {

		CloudStorageAccount cloudStorageAccount;
        CloudBlobClient 	serviceClient;
        CloudBlobContainer  container;
        StorageCredentialsAccountAndKey credentials;

        credentials = new StorageCredentialsAccountAndKey(strAccName.getServiceName(), key);

        String blobURL = strAccName.
        		getStorageServiceProperties().getEndpoints().
        		getEndpoints().get(0);
       	cloudStorageAccount = new CloudStorageAccount(credentials,new URI(blobURL),null,null);


        serviceClient = cloudStorageAccount.createCloudBlobClient();
        if (!allowRetry) {
            // Setting no retry policy
            RetryNoRetry  rnr = new RetryNoRetry();
            serviceClient.setRetryPolicyFactory(rnr);
        }

        container 	  = serviceClient.getContainerReference(containerName);

        if (createCnt) {
        	container.createIfNotExist();
        }

        // set max number of concurrent requests
        // If requested value is 1 then just leave it to defaults set by third party jars
        if	(concurrentRequestCount != 1) {
        	serviceClient.setConcurrentRequestCount(concurrentRequestCount);
        	serviceClient.setTimeoutInMs(TIME_OUT);
        }

        if (cntPubAccess != null) {
        	// Set access permissions on container.
            BlobContainerPermissions cntPerm;
            cntPerm = new BlobContainerPermissions();
            if (cntPubAccess) {
            	cntPerm.setPublicAccess(BlobContainerPublicAccessType.CONTAINER);
            } else {
            	cntPerm.setPublicAccess(BlobContainerPublicAccessType.OFF);
            }
            container.uploadPermissions(cntPerm);
        }
        return container;
	}

	public String createContainer(String container)
			throws WACommonException, RestAPIException, InterruptedException, CommandLineException {

		String url = storageAccount.
				getStorageServiceProperties().getEndpoints().
				getEndpoints().get(0).
				concat(container.toLowerCase() + "?restype=container");

		HashMap<String, String> headers = new HashMap<String, String>();

		headers.put(X_MS_VERSION, "2011-08-18");

		headers.put("x-ms-meta-Name", container);

		String result = WindowsAzureRestUtils.getInstance().runStorage(HttpVerb.PUT, url,
				storageKey, headers, null);

		Response<?> response = (Response<?>) deserialize(result);

		return getXRequestId(response);
	}
}
