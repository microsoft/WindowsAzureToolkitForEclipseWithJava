package com.microsoftopentechnologies.windowsazure.tools.build;

import com.microsoft.windowsazure.services.blob.client.CloudBlobClient;
import com.microsoft.windowsazure.services.blob.client.CloudBlobContainer;
import com.microsoft.windowsazure.services.blob.client.CloudBlockBlob;
import com.microsoft.windowsazure.services.core.storage.CloudStorageAccount;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import org.apache.tools.ant.Project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

/**
 * @author v-elenla
 *         1/15/14
 */
public class BlobOperations {
    private static String SUCCESS = "success";

    public static String uploadBlob(String filePath, String blobName, String containerName, String storageName, String accessKey,
                                    WindowsAzurePackage waPackage) {
        try {
            // Retrieve storage account from connection-string
            CloudStorageAccount storageAccount = CloudStorageAccount.parse(createStorageConnectionString(storageName, accessKey));

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

    public static String createContainer(String containerName, String storageName, String accessKey, WindowsAzurePackage waPackage) {
        try {
            // Retrieve storage account from connection-string
            CloudStorageAccount storageAccount = CloudStorageAccount.parse(createStorageConnectionString(storageName, accessKey));

            // Create the blob client
            CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

            // Get a reference to a container
            // The container name must be lower case
            CloudBlobContainer container = blobClient.getContainerReference(containerName);
            // Create the container if it does not exist
            container.createIfNotExist();
            return SUCCESS;
        } catch (Exception e) {
            waPackage.log(e, Project.MSG_WARN);
        }
        return null;
    }

    public static String useBlob(String blobName, String containerName, String storageName, String accessKey, WindowsAzurePackage waPackage) {
        try {
            CloudStorageAccount storageAccount = CloudStorageAccount.parse(createStorageConnectionString(storageName, accessKey));
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

    public static String createStorageConnectionString(String storageName, String accessKey) {
        return "DefaultEndpointsProtocol=http;AccountName=" + storageName + ";AccountKey=" + accessKey;
    }
}
