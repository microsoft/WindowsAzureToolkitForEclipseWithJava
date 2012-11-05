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

package com.gigaspaces.azure.propertypage;

public class SubscriptionPropertyPageTableElement {
	
	private String subscriptionName;
	private String subscriptionId;
	private String publishDataThumbprint;
	
	public String getSubscriptionName() {
		return subscriptionName;
	}
	public void setSubscriptionName(String subscriptionName) {
		this.subscriptionName = subscriptionName;
	}
	public String getSubscriptionId() {
		return subscriptionId;
	}
	public void setSubscriptionId(String subscriptionId) {
		this.subscriptionId = subscriptionId;
	}
	public String getPublishDataThumbprint() {
		return publishDataThumbprint;
	}
	public void setPublishDataThumbprint(String publishDataThumbprint) {
		this.publishDataThumbprint = publishDataThumbprint;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((publishDataThumbprint == null) ? 0 : publishDataThumbprint
						.hashCode());
		result = prime * result
				+ ((subscriptionId == null) ? 0 : subscriptionId.hashCode());
		result = prime
				* result
				+ ((subscriptionName == null) ? 0 : subscriptionName.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SubscriptionPropertyPageTableElement other = (SubscriptionPropertyPageTableElement) obj;
		if (publishDataThumbprint == null) {
			if (other.publishDataThumbprint != null)
				return false;
		} else if (!publishDataThumbprint.equals(other.publishDataThumbprint))
			return false;
		if (subscriptionId == null) {
			if (other.subscriptionId != null)
				return false;
		} else if (!subscriptionId.equals(other.subscriptionId))
			return false;
		if (subscriptionName == null) {
			if (other.subscriptionName != null)
				return false;
		} else if (!subscriptionName.equals(other.subscriptionName))
			return false;
		return true;
	}
}
