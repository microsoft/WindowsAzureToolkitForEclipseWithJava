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

package com.microsoftopentechnologies.azurecommons.deploy.propertypages;

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
