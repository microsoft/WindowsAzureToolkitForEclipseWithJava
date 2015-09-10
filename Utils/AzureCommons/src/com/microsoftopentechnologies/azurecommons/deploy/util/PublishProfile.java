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

package com.microsoftopentechnologies.azurecommons.deploy.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.microsoftopentechnologies.azuremanagementutil.model.Subscription;


@XmlType
public class PublishProfile implements Serializable, Cloneable {
	private static final long serialVersionUID = -6569753234382033009L;
	private String publishMethod;
	private String url;
	private String managementCertificate;
	private List<Subscription> subscriptions = new ArrayList<Subscription>();
	private String password = "";
	private String schemaVersion;

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

	@XmlAttribute(name = "SchemaVersion")
	public String getSchemaVersion() {
		return schemaVersion;
	}

	public void setSchemaVersion(String schemaVersion) {
		this.schemaVersion = schemaVersion;
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
}