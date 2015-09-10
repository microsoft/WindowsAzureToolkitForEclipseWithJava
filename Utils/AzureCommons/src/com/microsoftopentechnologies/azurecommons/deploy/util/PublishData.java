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
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.management.compute.models.HostedServiceListResponse;
import com.microsoft.windowsazure.management.compute.models.HostedServiceListResponse.HostedService;
import com.microsoft.windowsazure.management.models.LocationsListResponse.Location;
import com.microsoftopentechnologies.azuremanagementutil.model.StorageServices;
import com.microsoftopentechnologies.azuremanagementutil.model.Subscription;

@XmlRootElement(name = "PublishData")
public class PublishData implements Serializable, Cloneable {

	private static final long serialVersionUID = -5687551495552719808L;

	private PublishProfile publishProfile;

	private AtomicBoolean initializing;

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

	public PublishData() {
		initializing = new AtomicBoolean(false);
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

	public List<String> getSubscriptionNames() {
		List<String> ids = new ArrayList<String>();
		if (publishProfile != null) {
			List<Subscription> subscriptions = publishProfile
					.getSubscriptions();
			for (Subscription s : subscriptions) {
				ids.add(s.getName());
			}
			return ids;
		}
		return null;
	}

	public AtomicBoolean isInitializing() {

		if (initializing == null)
			initializing = new AtomicBoolean(false);

		return initializing;
	}

	public boolean isInitialized() {

		boolean pojosNotNull = (locationsPerSubscription != null)
				&& (servicesPerSubscription != null)
				&& (storagesPerSubscription != null)
				&& (publishProfile != null)
				&& (publishProfile.getSubscriptions() != null);

		boolean subscriptionIdsNotNull = true;
		for (Subscription s : publishProfile.getSubscriptions()) {
			if (s.getId() == null) {
				subscriptionIdsNotNull = false;
				break;
			}
		}

		boolean subscriptionNamesNotNullAndDoesNotEqualId = true;
		for (Subscription s : publishProfile.getSubscriptions()) {
			if (s.getName() == null || s.getName().equals(s.getId())) {
				subscriptionNamesNotNullAndDoesNotEqualId = false;
				break;
			}
		}

		return pojosNotNull && subscriptionIdsNotNull
				&& subscriptionNamesNotNullAndDoesNotEqualId;
	}

	public void reset() {

		List<Subscription> subscriptions = publishProfile.getSubscriptions();

		if (subscriptions != null) {
			for (Subscription s : subscriptions) {
				s.setSubscriptionName(s.getSubscriptionID());
			}
		}
	}
}
