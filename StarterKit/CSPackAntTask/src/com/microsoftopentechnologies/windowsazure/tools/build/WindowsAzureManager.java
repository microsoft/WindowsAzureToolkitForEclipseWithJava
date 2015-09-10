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
