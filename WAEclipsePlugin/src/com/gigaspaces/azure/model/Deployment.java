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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "Deployment")
@XmlType
public class Deployment {
	private String name;
	private DeploymentSlot deploymentSlot;
	private String privateID;
	private Status status;
	private String label;
	private String url;
	private String configuration;
	private RoleInstanceList roleInstanceList;
	private UpgradeStatus upgradeStatus;
	private int upgradeDomainCount;
	private RoleList roleList;
	private String sdkVersion;
	private InputEndpointList inputEndpointList;
	private boolean locked;
	private boolean rollbackAllowed;

	@XmlElement(name = "Name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlElement(name = "DeploymentSlot")
	public DeploymentSlot getDeploymentSlot() {
		return deploymentSlot;
	}

	public void setDeploymentSlot(DeploymentSlot deploymentSlot) {
		this.deploymentSlot = deploymentSlot;
	}

	@XmlElement(name = "PrivateID")
	public String getPrivateID() {
		return privateID;
	}

	public void setPrivateID(String privateID) {
		this.privateID = privateID;
	}

	@XmlElement(name = "Status")
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@XmlElement(name = "Label")
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@XmlElement(name = "Url")
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@XmlElement(name = "Configuration")
	public String getConfiguration() {
		return configuration;
	}

	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}

	@XmlElement(name = "RoleInstanceList")
	public RoleInstanceList getRoleInstanceList() {
		return roleInstanceList;
	}

	public void setRoleInstanceList(RoleInstanceList roleInstanceList) {
		this.roleInstanceList = roleInstanceList;
	}

	@XmlElement(name = "UpgradeDomainCount")
	public int getUpgradeDomainCount() {
		return upgradeDomainCount;
	}

	public void setUpgradeDomainCount(int upgradeDomainCount) {
		this.upgradeDomainCount = upgradeDomainCount;
	}

	@XmlElement(name = "RoleList")
	public RoleList getRoleList() {
		return roleList;
	}

	public void setRoleList(RoleList roleList) {
		this.roleList = roleList;
	}

	@XmlElement(name = "SdkVersion")
	public String getSdkVersion() {
		return sdkVersion;
	}

	public void setSdkVersion(String sdkVersion) {
		this.sdkVersion = sdkVersion;
	}

	@XmlElement(name = "InputEndpointList")
	public InputEndpointList getInputEndpointList() {
		return inputEndpointList;
	}

	public void setInputEndpointList(InputEndpointList inputEndpointList) {
		this.inputEndpointList = inputEndpointList;
	}

	@XmlElement(name = "Locked")
	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	@XmlElement(name = "RollbackAllowed")
	public boolean isRollbackAllowed() {
		return rollbackAllowed;
	}

	public void setRollbackAllowed(boolean rollbackAllowed) {
		this.rollbackAllowed = rollbackAllowed;
	}

	@XmlElement(name = "UpgradeStatus")
	public UpgradeStatus getUpgradeStatus() {
		return upgradeStatus;
	}

	public void setUpgradeStatus(UpgradeStatus upgradeStatus) {
		this.upgradeStatus = upgradeStatus;
	}
}
