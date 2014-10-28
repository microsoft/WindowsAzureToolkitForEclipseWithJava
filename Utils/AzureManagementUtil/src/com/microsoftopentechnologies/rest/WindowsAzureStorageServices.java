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
package com.microsoftopentechnologies.rest;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.RetryNoRetry;
import com.microsoft.azure.storage.StorageCredentialsAccountAndKey;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;
import com.microsoftopentechnologies.exception.InvalidRestAPIArgument;
import com.microsoftopentechnologies.model.Notifier;
import com.microsoftopentechnologies.model.StorageService;

public class WindowsAzureStorageServices extends WindowsAzureServiceImpl {

	private static final int TIME_OUT = 500 * 1000; //500 seconds
	private StorageService storageAccount;
	private String storageKey;
	private final static int NTHREAD = 4;
	private static final String BLOB = "blob";
	private static final String TABLE = "table";
	private static final String QUEUE = "queue";
	private static String invalidStrgAcc = "Incorrect storage account";
	private static String invalidStorageKey = "Incorrect storage key";
	private static String upErr = "Upload failed - exception trace ";
	private static String delExcpTrce = "Blob Delete failed - exception trace "; 

	public WindowsAzureStorageServices(StorageService storageAccount, String storageKey)
			throws NoSuchAlgorithmException, InvalidKeyException {
		super();
		if (storageAccount == null)
			throw new InvalidRestAPIArgument(invalidStrgAcc);
		this.storageAccount = storageAccount;
		if (storageKey == null || storageKey.isEmpty())
			throw new InvalidRestAPIArgument(invalidStorageKey);
		this.storageKey = storageKey;
	}

	public void createContainer(String containerName) throws Exception {
		try {
			// setting option to use existing system default proxy
			System.setProperty("java.net.useSystemProxies", "true");
			CloudStorageAccount cloudStorageAccount =
					getCloudStorageAccount(storageAccount.getServiceName(),
							storageKey,
							storageAccount.getStorageAccountProperties().getEndpoints().get(0).toString());
			CloudBlobClient blobClient = cloudStorageAccount.createCloudBlobClient();
			CloudBlobContainer container = blobClient.getContainerReference(containerName);
			// Create the container if it does not exist
			container.createIfNotExists();
		} catch (Exception ex) {
			throw ex;
		}
	}
	/**
	 * Method adds file to blob storage.
	 * @param container
	 * @param blobName
	 * @param file
	 * @param notifier
	 * @return - Time taken to upload blob
	 * @throws Exception
	 * @throws FileNotFoundException
	 */
	public String putBlob(String container, String blobName, File file, Notifier notifier) throws
	Exception, FileNotFoundException {
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
			throw new Exception(upErr, e);
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
		return timeTaken;
	}

	/**
	 * Method deletes specified blob.
	 * @param container
	 * @param blobName
	 * @param notifier
	 * @throws CommandLineException
	 * @throws FileNotFoundException
	 */
	public void deleteBlob(String container, String blobName, Notifier notifier) throws
	Exception, FileNotFoundException {
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
			throw new Exception(delExcpTrce, e);
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
					throws URISyntaxException, StorageException {

		CloudStorageAccount cloudStorageAccount;
		CloudBlobClient 	serviceClient;
		CloudBlobContainer  container;
		StorageCredentialsAccountAndKey credentials;

		credentials = new StorageCredentialsAccountAndKey(strAccName.getServiceName(), key);
		List<URI> endpoints = strAccName.getStorageAccountProperties().getEndpoints();
		cloudStorageAccount = new CloudStorageAccount(credentials, endpoints.get(0), endpoints.get(1), endpoints.get(2));

		serviceClient = cloudStorageAccount.createCloudBlobClient();
		if (!allowRetry) {
			// Setting no retry policy
			RetryNoRetry rnr = new RetryNoRetry();
			serviceClient.setRetryPolicyFactory(rnr);
		}

		container 	  = serviceClient.getContainerReference(containerName);

		if (createCnt) {
			container.createIfNotExists();
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

	/** Returns CloudStorageAccount  */
	private static CloudStorageAccount getCloudStorageAccount(String storageName, String accessKey, String blobURL) {
		CloudStorageAccount cloudStorageAccount = null;
		StorageCredentialsAccountAndKey credentials = new StorageCredentialsAccountAndKey(storageName, accessKey);
		try {
			if (blobURL == null || blobURL.length() == 0) {
				cloudStorageAccount = new CloudStorageAccount(credentials);
			} else {
				cloudStorageAccount =  new CloudStorageAccount(credentials, new URI(blobURL), 
						new URI(getCustomURI(storageName, QUEUE, blobURL)), 
						new URI(getCustomURI(storageName, TABLE, blobURL)));
			}
		} catch (Exception e) {
			// Incase of exception returning storage account for default cloud.
			try {
				cloudStorageAccount = new CloudStorageAccount(credentials);
			} catch (Exception e1) {
				// This case should not occur - just returning null if happens
				e1.printStackTrace();
				return null;
			}
		}
		return cloudStorageAccount;
	}

	/** Returns custom URL for queue and table. */
	private static String getCustomURI(String storageAccountName, String type, String blobURL) {
		if (QUEUE.equalsIgnoreCase(type)) {
			return blobURL.replace(storageAccountName + "." + BLOB,
					storageAccountName + "." + type);
		} else if (TABLE.equalsIgnoreCase(type)) {
			return blobURL.replace(storageAccountName + "." + BLOB,
					storageAccountName + "." + type);
		} else {
			return null;
		}
	}
}
