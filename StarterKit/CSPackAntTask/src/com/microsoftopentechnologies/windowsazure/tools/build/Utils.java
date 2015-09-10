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
package com.microsoftopentechnologies.windowsazure.tools.build;

import java.io.File;
import java.net.URI;
import java.util.List;

import com.microsoftopentechnologies.azuremanagementutil.model.StorageService;
import com.microsoftopentechnologies.azuremanagementutil.rest.WindowsAzureServiceManagement;
import com.microsoftopentechnologies.azuremanagementutil.exception.InvalidThumbprintException;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.core.OperationStatus;
import com.microsoft.windowsazure.core.OperationStatusResponse;

public class Utils {
	private static String eclipseDeployContainer = "eclipsedeploy";
	private final static String FWD_SLASH = "/";

	public synchronized static WindowsAzureServiceManagement getServiceInstance() {
		WindowsAzureServiceManagement instance = null;
		try {
			instance = new WindowsAzureServiceManagement();
		} catch (InvalidThumbprintException e) {
			e.printStackTrace();
		}
		return instance;
	}

	public static OperationStatus waitForStatus(Configuration configuration,
			WindowsAzureServiceManagement service, String requestId) throws Exception {
		OperationStatusResponse op;
		OperationStatus status = null;
		do {
			op = service.getOperationStatus(configuration, requestId);
			status = op.getStatus();
			if (op.getError() != null) {
				System.out.println("Error Message: " + op.getError().getMessage());
				throw new Exception(op.getError().getMessage());
			}
			Thread.sleep(10000);
		} while (status == OperationStatus.InProgress);
		return status;
	}
	
	public static StorageService createStorageAccountIfNotExists(Configuration configuration,
			WindowsAzureServiceManagement instance,
			String storageAccountName,
			String region,
			String managementUrl) throws Exception {
		System.out.println("Creating storage account : '" + storageAccountName + "' if does not exists");
		ProgressBar progressBar = new ProgressBar(10000, "Creating storage account");
		Thread progressBarThread = new Thread(progressBar);
		progressBarThread.start();
		instance.createStorageAccountIfNotExists(configuration, storageAccountName, region);
		progressBarThread.interrupt();
		try {
			progressBarThread.join();
		} catch (InterruptedException e) {
			;
		}
		// Get storage account object
		StorageService storageAccount = instance.getStorageAccount(configuration, storageAccountName);
		if (managementUrl.equals("https://management.core.chinacloudapi.cn")) {
			List<URI> endpoints = storageAccount.getStorageAccountProperties().getEndpoints();
			for (int i = 0; i < endpoints.size(); i++) {
				String uri = endpoints.get(i).toString();
				if (uri.startsWith("https://")) {
					endpoints.set(i, URI.create(uri.replaceFirst("https://", "http://")));
				}
			}
		}
		return storageAccount;
	}
	
	public static String prepareCloudBlobURL(String filePath,
			String newUrl) {
		if ((filePath == null || filePath.length() == 0)
				|| (newUrl == null || newUrl.length() == 0)) {
			return "";
		}

		File jdkPath = new File(filePath);
		return new StringBuilder(newUrl).append(eclipseDeployContainer)
				.append(FWD_SLASH).append(jdkPath.getName().trim().replaceAll("\\s+", "-"))
				.append(".zip").toString();
	}

	public static String prepareUrlForApp(String asName,
			String url) {
		if ((asName == null || asName.length() == 0)
				|| (url == null || url.length() == 0)) {
			return "";
		}

		return new StringBuilder(url).append(eclipseDeployContainer)
				.append(FWD_SLASH).append(asName).toString();
	}

	public static String prepareUrlForThirdPartyJdk(String cloudValue, String url) {
		String finalUrl = "";
		String dirName = cloudValue.substring(cloudValue.lastIndexOf("\\") + 1,
				cloudValue.length());
		finalUrl = new StringBuilder(url)
		.append(eclipseDeployContainer).append(FWD_SLASH)
		.append(dirName).append(".zip").toString();
		return finalUrl;
	}
	
	/*
	 * Checks if given parameter is null or empty
	 */
	public static boolean isNullOrEmpty(String value) {
		return (value == null) || (value.trim().length() == 0);
		
	}
	
	/*
	 * Checks if given parameter is not null and not empty
	 */
	public static boolean isNotNullOrEmpty(String value) {
		return (value != null) && (value.trim().length() > 0);		
	}
	
    /**
     * Checks is file path is valid
     * @param filePath
     * @return
     */
	public static boolean isValidFilePath(String filePath) {
		if (isNullOrEmpty(filePath)) {
			return false;
		}
		
		String path = filePath.trim();
		
		// Validate publish settings path
		File file = new File(path);
		if ((!file.exists()) || file.isDirectory()) {
			return false; 				
		} else {
			return true;
		}
	}
}
