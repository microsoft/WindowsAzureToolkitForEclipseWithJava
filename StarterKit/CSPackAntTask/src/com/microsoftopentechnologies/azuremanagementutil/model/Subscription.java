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
package com.microsoftopentechnologies.azuremanagementutil.model;

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
	private String serviceManagementUrl;
	private String managementCertificate;

	@XmlAttribute(name = "ServiceManagementUrl")
	public String getServiceManagementUrl() {
		return serviceManagementUrl;
	}

	public void setServiceManagementUrl(String serviceManagementUrl) {
		this.serviceManagementUrl = serviceManagementUrl;
	}

	@XmlAttribute(name = "ManagementCertificate")
	public String getManagementCertificate() {
		return managementCertificate;
	}

	public void setManagementCertificate(String managementCertificate) {
		this.managementCertificate = managementCertificate;
	}

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
