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

@XmlRootElement(name = "HostedService")
@XmlType(propOrder = { "url", "serviceName", "hostedServiceProperties",
		"deployments" })
public class HostedService {

	private String url;
	private String serviceName;
	private HostedServiceProperties hostedServiceProperties;
	private Deployments deployments;

	@XmlElement(name = "Url")
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@XmlElement(name = "ServiceName")
	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	@XmlElement(name = "HostedServiceProperties")
	public HostedServiceProperties getHostedServiceProperties() {
		return hostedServiceProperties;
	}

	public void setHostedServiceProperties(
			HostedServiceProperties hostedServiceProperties) {
		this.hostedServiceProperties = hostedServiceProperties;
	}

	@XmlElement(name = "Deployments")
	public Deployments getDeployments() {
		return deployments;
	}

	public void setDeployments(Deployments deployments) {
		this.deployments = deployments;
	}
}
