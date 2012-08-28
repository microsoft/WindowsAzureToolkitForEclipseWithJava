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

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Subscription",namespace="http://schemas.microsoft.com/windowsazure")
public class Subscription implements Serializable,Cloneable {

	private static final long serialVersionUID = 5054516244728910210L;

	private String subscriptionID;
	private String subscriptionName;
	private String subscriptionStatus;
	private String accountAdminLiveEmailId;
	private String serviceAdminLiveEmailId;
	private int maxCoreCount;
	private int maxStorageAccounts;
	private int maxHostedServices;
	private int currentCoreCount;
	private int currentHostedServices;
	private int currentStorageAccounts;

	@XmlAttribute(name = "Id")
	public String getId() {
		return subscriptionID;
	}

	public void setId(String Id) {
		subscriptionID = Id;
	}

	@XmlAttribute(name = "Name")
	public String getName() {
		return subscriptionName;
	}

	public void setName(String name) {
		subscriptionName = name;
	}

	@XmlElement(name = "SubscriptionID")
	public String getSubscriptionID() {
		return subscriptionID;
	}

	public void setSubscriptionID(String subscriptionID) {
		this.subscriptionID = subscriptionID;
	}

	@XmlElement(name = "SubscriptionName")
	public String getSubscriptionName() {
		return subscriptionName;
	}

	public void setSubscriptionName(String subscriptionName) {
		this.subscriptionName = subscriptionName;
	}

	@XmlElement(name = "SubscriptionStatus")
	public String getSubscriptionStatus() {
		return subscriptionStatus;
	}

	public void setSubscriptionStatus(String subscriptionStatus) {
		this.subscriptionStatus = subscriptionStatus;
	}

	@XmlElement(name = "AccountAdminLiveEmailId")
	public String getAccountAdminLiveEmailId() {
		return accountAdminLiveEmailId;
	}

	public void setAccountAdminLiveEmailId(String accountAdminLiveEmailId) {
		this.accountAdminLiveEmailId = accountAdminLiveEmailId;
	}

	@XmlElement(name = "ServiceAdminLiveEmailId")
	public String getServiceAdminLiveEmailId() {
		return serviceAdminLiveEmailId;
	}

	public void setServiceAdminLiveEmailId(String serviceAdminLiveEmailId) {
		this.serviceAdminLiveEmailId = serviceAdminLiveEmailId;
	}

	@XmlElement(name = "MaxCoreCount")
	public int getMaxCoreCount() {
		return maxCoreCount;
	}

	public void setMaxCoreCount(int maxCoreCount) {
		this.maxCoreCount = maxCoreCount;
	}

	@XmlElement(name = "MaxStorageAccounts")
	public int getMaxStorageAccounts() {
		return maxStorageAccounts;
	}

	public void setMaxStorageAccounts(int maxStorageAccounts) {
		this.maxStorageAccounts = maxStorageAccounts;
	}

	@XmlElement(name = "MaxHostedServices")
	public int getMaxHostedServices() {
		return maxHostedServices;
	}

	public void setMaxHostedServices(int maxHostedServices) {
		this.maxHostedServices = maxHostedServices;
	}

	@XmlElement(name = "CurrentCoreCount")
	public int getCurrentCoreCount() {
		return currentCoreCount;
	}

	public void setCurrentCoreCount(int currentCoreCount) {
		this.currentCoreCount = currentCoreCount;
	}

	@XmlElement(name = "CurrentHostedServices")
	public int getCurrentHostedServices() {
		return currentHostedServices;
	}

	public void setCurrentHostedServices(int currentHostedServices) {
		this.currentHostedServices = currentHostedServices;
	}

	@XmlElement(name = "CurrentStorageAccounts")
	public int getCurrentStorageAccounts() {
		return currentStorageAccounts;
	}

	public void setCurrentStorageAccounts(int currentStorageAccounts) {
		this.currentStorageAccounts = currentStorageAccounts;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {		
		return super.clone();
	}
}
