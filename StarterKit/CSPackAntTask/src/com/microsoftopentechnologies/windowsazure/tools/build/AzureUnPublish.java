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

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

import com.microsoftopentechnologies.azuremanagementutil.rest.WindowsAzureRestUtils;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.management.compute.models.DeploymentStatus;
import com.microsoft.windowsazure.management.compute.models.HostedServiceGetDetailedResponse;
import com.microsoft.windowsazure.management.compute.models.HostedServiceGetDetailedResponse.Deployment;
import com.microsoftopentechnologies.azuremanagementutil.rest.WindowsAzureServiceManagement;

public class AzureUnPublish extends Task {
	private String publishSettingsPath;
	private String subscriptionId;
	private String cloudServiceName;
	private String deploymentSlot;
	private String defaultServiceName = "AntPublishService";
	private String defaultDeploymentSlot = "Staging";

	public String getPubFilePath() {
		return publishSettingsPath;
	}

	public void setPublishSettingsPath(String publishSettingsPath) {
		this.publishSettingsPath = publishSettingsPath;
	}

	public String getSubscriptionId() {
		return subscriptionId;
	}

	public void setSubscriptionId(String subscriptionId) {
		this.subscriptionId = subscriptionId;
	}

	public String getCloudServiceName() {
		return cloudServiceName;
	}

	public void setCloudServiceName(String cloudServiceName) {
		this.cloudServiceName = cloudServiceName;
	}

	public String getDeploymentSlot() {
		return deploymentSlot;
	}

	public void setDeploymentSlot(String deploymentSlot) {
		this.deploymentSlot = deploymentSlot;
	}

	/**
	 * WindowsAzurePackage constructor
	 */
	public AzureUnPublish() {

	}

	/**
	 * Initializes the task.
	 * Verifies that all package properties are valid
	 * and if they are not specified then initialize with default values. 
	 */
	private void initialize() {
		
		if (Utils.isNotNullOrEmpty(publishSettingsPath)) {
			if (!Utils.isValidFilePath(publishSettingsPath)) {
				throw new BuildException("publishSettingsPath " +publishSettingsPath + " is not valid "
						+ "or points to a file that does not exist");
			}
		} else {
			throw new BuildException("publishSettingsPath is empty");
		}

		if (subscriptionId != null) {
			subscriptionId = subscriptionId.trim();
		}

		if (Utils.isNullOrEmpty(cloudServiceName)) {
			cloudServiceName = defaultServiceName;
		} else {
			cloudServiceName = cloudServiceName.trim();
		}
		
		if (Utils.isNullOrEmpty(deploymentSlot)) {
			deploymentSlot = defaultDeploymentSlot;
		} else {
			deploymentSlot = deploymentSlot.trim();
		}
	}


	/**
	 * Executes the task
	 */
	public void execute() throws BuildException {
		ClassLoader thread = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(AzureUnPublish.class.getClassLoader());
			initialize();
			
			this.log("Using Publish settings file : " + this.publishSettingsPath);
			File pubFile =  new File(this.publishSettingsPath);
			
			if (Utils.isNullOrEmpty(subscriptionId)) {
				try {
					subscriptionId = XMLUtil.getDefaultSubscription(pubFile);
				} catch (Exception e) {
					throw new BuildException(e);
				}
			}
			this.log("Using subscription id : " + subscriptionId);
			
			// Get configuration object needed to call Azure management service APIS
			Configuration configuration = null;
			try {
				configuration = WindowsAzureRestUtils.getConfiguration(pubFile, subscriptionId);
			} catch (Exception e) {
				throw new BuildException("Error: Failed to load publish settings, ensure"
						+ " publish settings file "+pubFile.getAbsolutePath()+ 
						" and subscriptionId "+subscriptionId+ " is valid");
			}
			
			// check Azure services can be invoked with configuration object.
			AzurePublish.pingAzure(configuration);
			
			WindowsAzureServiceManagement instance = Utils.getServiceInstance();
			HostedServiceGetDetailedResponse hostedServiceDetailed =
					instance.getHostedServiceWithProperties(configuration, cloudServiceName);
			boolean isDeleted = false;
			for (Deployment deployment : hostedServiceDetailed.getDeployments()) {
				if (deployment.getName() == null) {
					continue;
				}
				if (deployment.getStatus() == DeploymentStatus.Running &&
						deployment.getDeploymentSlot().toString().equalsIgnoreCase(deploymentSlot)) {
					this.log("Undeploying " + cloudServiceName + "-" + deploymentSlot);
					ProgressBar progressBar = new ProgressBar(10000, "Undeploying deployment");
					Thread progressBarThread = new Thread(progressBar);
					progressBarThread.start();
					String requestId = instance.
							deleteDeployment(configuration, cloudServiceName, deployment.getName());
					Utils.waitForStatus(configuration, instance, requestId);
					isDeleted = true;
					progressBarThread.interrupt();
					try {
						progressBarThread.join();
					} catch (InterruptedException e) {
						;
					}
					this.log("Unpublished.");
				}
			}
			if (!isDeleted) {
				String text = String.format("No deployments found for %s environment.",
						cloudServiceName + "-" + deploymentSlot);
				this.log(text);
			}
		} catch (Exception e) {
			throw new BuildException(e);
		} finally {
			Thread.currentThread().setContextClassLoader(thread);
		}
	}

}
