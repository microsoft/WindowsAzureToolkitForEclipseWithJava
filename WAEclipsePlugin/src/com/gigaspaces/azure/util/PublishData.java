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
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.gigaspaces.azure.model.HostedServices;
import com.gigaspaces.azure.model.Locations;
import com.gigaspaces.azure.model.StorageServices;
import com.gigaspaces.azure.model.Subscription;

@XmlRootElement(name = "PublishData")
public class PublishData implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5687551495552719808L;

	private PublishProfile publishProfile;

	private AtomicBoolean initializing;

	private transient Map<String, HostedServices> servicesPerSubscription;
	private transient Map<String, StorageServices> storagesPerSubscription;
	private transient Map<String, Locations> locationsPerSubscription;

	private Subscription currentSubscription;

	public Map<String, StorageServices> getStoragesPerSubscription() {
		return storagesPerSubscription;
	}

	public void setStoragesPerSubscription(
			Map<String, StorageServices> storagesPerSubscription) {
		this.storagesPerSubscription = storagesPerSubscription;
	}

	public Map<String, Locations> getLocationsPerSubscription() {
		return locationsPerSubscription;
	}

	public void setLocationsPerSubscription(
			Map<String, Locations> locationsPerSubscription) {
		this.locationsPerSubscription = locationsPerSubscription;
	}

	public Map<String, HostedServices> getServicesPerSubscription() {
		return servicesPerSubscription;
	}

	public void setServicesPerSubscription(
			Map<String, HostedServices> servicesPerSubscription) {
		this.servicesPerSubscription = servicesPerSubscription;
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

	public String getThumbprint() {
		return publishProfile.getThumbprint();
	}

	public void setThumbprint(String thumbprint) {
		publishProfile.setThumbprint(thumbprint);
	}

}
