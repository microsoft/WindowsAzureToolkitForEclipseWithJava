/*******************************************************************************
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
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

package com.gigaspaces.azure.model;

import java.util.Date;

import com.interopbridges.tools.windowsazure.WindowsAzurePackageType;

public class DeployDescriptor {

	private final String deploymentId;
	private final Date startTime;
	private final String cspkgFile;
	private final String cscfgFile;
	private final String deployState;
	private final String subscriptionId;
	private final StorageService storageAcount;
	private final KeyName accessKey;
	private final HostedService hostedService;
	private final RemoteDesktopDescriptor remoteDesktopDescriptor;
	private final WindowsAzurePackageType deployMode;

	public DeployDescriptor(WindowsAzurePackageType deployMode,
			String subscriptionId, StorageService storageAcount,
			KeyName accessKey, HostedService hostedService, String cspkgFile,
			String cscfgFile, String deployState,
			RemoteDesktopDescriptor remoteDesktopDescriptor) {
		this.deployMode = deployMode;
		this.startTime = new Date();
		this.deploymentId = String.format(Messages.deploymentIdFormat,
				hostedService.getServiceName(), deployState);
		this.subscriptionId = subscriptionId;
		this.storageAcount = storageAcount;
		this.accessKey = accessKey;
		this.hostedService = hostedService;
		this.remoteDesktopDescriptor = remoteDesktopDescriptor;
		this.cspkgFile = cspkgFile;
		this.cscfgFile = cscfgFile;
		this.deployState = deployState;
	}

	public WindowsAzurePackageType getDeployMode() {
		return deployMode;
	}

	/**
	 * @return the deploymentId
	 */
	public String getDeploymentId() {
		return deploymentId;
	}

	/**
	 * @return the startTime
	 */
	public Date getStartTime() {
		return startTime;
	}

	public String getSubscriptionId() {
		return subscriptionId;
	}

	public StorageService getStorageAccount() {
		return storageAcount;
	}

	public String getStorageKey() {
		if (accessKey == KeyName.Primary) {
			return storageAcount.getStorageServiceKeys().getPrimary();
		}
		return storageAcount.getStorageServiceKeys().getSecondary();
	}

	public HostedService getHostedService() {
		return hostedService;
	}

	public String getCspkgFile() {
		return cspkgFile;
	}

	public String getCscfgFile() {
		return cscfgFile;
	}

	public String getDeployState() {
		return deployState;
	}

	/**
	 * @return the storageAcountDescriptor
	 */
	public StorageService getStorageAcountDescriptor() {
		return storageAcount;
	}

	/**
	 * @return the remoteDesktopDescriptor
	 */
	public RemoteDesktopDescriptor getRemoteDesktopDescriptor() {
		return remoteDesktopDescriptor;
	}

	/**
	 * @return the enableRemoteDesktop
	 */
	public boolean isEnableRemoteDesktop() {
		return remoteDesktopDescriptor.isEnabled();
	}

	public boolean isStartRdpOnDeploy() {
		return remoteDesktopDescriptor.isStartRemoteRDP();
	}
}