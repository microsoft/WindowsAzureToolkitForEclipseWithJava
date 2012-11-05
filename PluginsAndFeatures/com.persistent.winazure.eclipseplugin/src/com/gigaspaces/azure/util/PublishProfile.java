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

package com.gigaspaces.azure.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.gigaspaces.azure.model.Subscription;

@XmlType
public class PublishProfile implements Serializable, Cloneable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6569753234382033009L;
	private String publishMethod;
	private String url;
	private String managementCertificate;
	private List<Subscription> subscriptions = new ArrayList<Subscription>();
	private String password = ""; //$NON-NLS-1$
	private String thumbprint;
	
	@XmlAttribute(name = "PublishMethod")
	public String getPublishMethod() {
		return publishMethod;
	}

	public void setPublishMethod(String publishMethod) {
		this.publishMethod = publishMethod;
	}

	@XmlAttribute(name = "Url")
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@XmlAttribute(name = "ManagementCertificate")
	public String getManagementCertificate() {
		return managementCertificate;
	}

	public void setManagementCertificate(String managementCertificate) {
		this.managementCertificate = managementCertificate;
	}

	@XmlElement(name = "Subscription")
	public synchronized List<Subscription> getSubscriptions() {
		return subscriptions;
	}

	public synchronized void setSubscriptions(List<Subscription> subscriptions) {
		this.subscriptions = subscriptions;
	}

	@XmlElement(name = "Password")
	public synchronized String getPassword() {
		return this.password;
	}

	public void setPassword(String pfxPassword) {
		this.password = pfxPassword;
	}

	public String getThumbprint() {
		return thumbprint;
	}

	public void setThumbprint(String thumbprint) {
		this.thumbprint = thumbprint;
	}
}