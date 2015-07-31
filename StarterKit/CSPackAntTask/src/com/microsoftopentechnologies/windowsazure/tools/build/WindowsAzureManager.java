/*
 Copyright Microsoft Corp.
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.microsoftopentechnologies.windowsazure.tools.build;

public class WindowsAzureManager {
    private static final int PROGRESS_INTERVAL = 5000;
    private static final String PROGRESS_MESSAGE = "still uploading";

    public String createContainer(String containerName, String storageName, String accessKey, String blobURL, WindowsAzurePackage waPackage) {
        return BlobOperations.createContainer(containerName, storageName, accessKey, blobURL, waPackage);
    }

    public String uploadBlob(String filePath, String blobName, String containerName, String storageName, String accessKey, String blobURL,
    		WindowsAzurePackage waPackage) {
        // Start showing progress bar
        ProgressBar progressBar = new ProgressBar(PROGRESS_INTERVAL, PROGRESS_MESSAGE);
        Thread progressBarThread = new Thread(progressBar);
        progressBarThread.start();

        String response = BlobOperations.uploadBlob(filePath, blobName, containerName, storageName, accessKey, blobURL, waPackage);

        // Stop the progress bar
        progressBarThread.interrupt();
        try {
            progressBarThread.join();
        } catch (InterruptedException e) {
            ;
        }
        return response;
    }

    public String useBlob(String blobName, String containerName, String storageName, String accessKey, String blobURL, WindowsAzurePackage waPackage) {
        return BlobOperations.useBlob(blobName, containerName, storageName, accessKey, blobURL, waPackage);
    }
}
