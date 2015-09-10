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

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageCredentialsAccountAndKey;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

import org.apache.tools.ant.Project;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class BlobOperations {
    private static String SUCCESS = "success";
    private static final String BLOB = "blob";
    private static final String TABLE = "table";
    private static final String QUEUE = "queue";
	

    public static String uploadBlob(String filePath, String blobName, String containerName, String storageName, String accessKey,
                                    String blobURL, WindowsAzurePackage waPackage) {
        try {
            // Retrieve storage account from connection-string
            CloudStorageAccount storageAccount = getCloudStorageAccount(storageName, accessKey, blobURL);

            // Create the blob client
            CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

            // Retrieve reference to a previously created container
            CloudBlobContainer container = blobClient.getContainerReference(containerName);

            // Create or overwrite the "myimage.jpg" blob with contents from a local file
            CloudBlockBlob blob = container.getBlockBlobReference(blobName);
            File source = new File(filePath);
            blob.upload(new FileInputStream(source), source.length());
            return SUCCESS;
        } catch (Exception e) {
            waPackage.log(e, Project.MSG_WARN);
        }
        return null;
    }

    public static String createContainer(String containerName, String storageName, String accessKey, String blobURL, WindowsAzurePackage waPackage) {
        try {
            // Retrieve storage account from connection-string
            CloudStorageAccount storageAccount = getCloudStorageAccount(storageName, accessKey, blobURL);

            // Create the blob client
            CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

            // Get a reference to a container
            // The container name must be lower case
            CloudBlobContainer container = blobClient.getContainerReference(containerName);
            // Create the container if it does not exist
            container.createIfNotExists();
            return SUCCESS;
        } catch (Exception e) {
        	e.printStackTrace();
            waPackage.log(e, Project.MSG_WARN);
        }
        return null;
    }

    public static String useBlob(String blobName, String containerName, String storageName, String accessKey, String blobURL, WindowsAzurePackage waPackage) {
        try {
            CloudStorageAccount storageAccount = getCloudStorageAccount(storageName, accessKey, blobURL);
            CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

            CloudBlobContainer container = blobClient.getContainerReference(containerName);
            CloudBlockBlob blob = container.getBlockBlobReference(blobName);
            if (blob.exists()) {
                return SUCCESS;
            }
        } catch (Exception e) {
            waPackage.log(e, Project.MSG_WARN);
        }
        return null;
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
