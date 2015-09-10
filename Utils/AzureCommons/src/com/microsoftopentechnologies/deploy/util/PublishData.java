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

package com.microsoftopentechnologies.deploy.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.management.compute.models.HostedServiceListResponse;
import com.microsoft.windowsazure.management.compute.models.HostedServiceListResponse.HostedService;
import com.microsoft.windowsazure.management.models.LocationsListResponse.Location;
import com.microsoftopentechnologies.azuremanagementutil.model.StorageServices;
import com.microsoftopentechnologies.model.Subscription;
/* Class needs to be removed after 2.5.1 release.
 * Class is added just to take care of project upgrade scenario.
 */
@XmlRootElement(name = "PublishData")
public class PublishData implements Serializable, Cloneable {
	private static final long serialVersionUID = -5687551495552719808L;
	private PublishProfile publishProfile;
	private transient Map<String, ArrayList<HostedService>> servicesPerSubscription;
	private transient Map<String, StorageServices> storagesPerSubscription;
	private transient Map<String, ArrayList<Location>> locationsPerSubscription;

	private Subscription currentSubscription;
	private Map<String, Configuration> configurationPerSubscription;

	public Map<String, StorageServices> getStoragesPerSubscription() {
		return storagesPerSubscription;
	}

	public void setStoragesPerSubscription(Map<String, StorageServices> storagesPerSubscription) {
		this.storagesPerSubscription = storagesPerSubscription;
	}

	public Map<String, ArrayList<Location>> getLocationsPerSubscription() {
		return locationsPerSubscription;
	}

	public void setLocationsPerSubscription(Map<String, ArrayList<Location>> locationsPerSubscription) {
		this.locationsPerSubscription = locationsPerSubscription;
	}

	public Map<String, ArrayList<HostedServiceListResponse.HostedService>> getServicesPerSubscription() {
		return servicesPerSubscription;
	}

	public void setServicesPerSubscription(
			Map<String, ArrayList<HostedService>> servicesPerSubscription) {
		this.servicesPerSubscription = servicesPerSubscription;
	}

	public Configuration getCurrentConfiguration() {
		return configurationPerSubscription.get(currentSubscription.getId());
	}

	public Configuration getConfiguration(String subscriptionId) {
		return configurationPerSubscription.get(subscriptionId);
	}

	public void setConfigurationPerSubscription(Map<String, Configuration> configurationPerSubscription) {
		this.configurationPerSubscription = configurationPerSubscription;
	}

	public Subscription getCurrentSubscription() {
		return currentSubscription;
	}

	public void setCurrentSubscription(Subscription currentSubscription) {
		this.currentSubscription = currentSubscription;
	}

	@XmlElement(name = "PublishProfile")
	public PublishProfile getPublishProfile() {
		return publishProfile;
	}

	public void setPublishProfile(PublishProfile publishProfile) {
		this.publishProfile = publishProfile;
	}

	public synchronized List<String> getSubscriptionIds() {
		List<String> ids = new ArrayList<String>();
		if (publishProfile != null) {
			List<Subscription> subscriptions = publishProfile
					.getSubscriptions();
			for (Subscription s : subscriptions) {
				ids.add(s.getId());
			}
			return ids;
		}
		return null;
	}
}