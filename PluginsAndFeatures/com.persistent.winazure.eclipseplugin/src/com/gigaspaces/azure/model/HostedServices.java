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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "HostedServices",namespace="http://schemas.microsoft.com/windowsazure")
public class HostedServices implements Iterable<HostedService> {

	private List<HostedService> hostedServices;

	/**
	 * @return the hostedService
	 */
	@XmlElement(name = "HostedService")
	public List<HostedService> getHostedServices() {
		return hostedServices;
	}

	/**
	 * @param hostedService
	 *            the hostedService to set
	 */
	public void setHostedServices(List<HostedService> hostedServices) {
		this.hostedServices = hostedServices;
	}

	@Override
	public Iterator<HostedService> iterator() {

		return hostedServices.iterator();
	}
	
	public int size() {
		if (isEmpty()) return 0;
		return hostedServices.size();
	}
	
	public boolean isEmpty() {
		return (hostedServices == null);
	}
	
	public void add(HostedService hostedService) {
		if (hostedServices == null) {
			hostedServices = new ArrayList<HostedService>();
		}
		hostedServices.add(hostedService);
	}
	
	public void remove(final String hostedServiceName) {
		
		HostedService toRemove = null;
		for (HostedService hs : hostedServices) {
			if (hs.getServiceName().equals(hostedServiceName)) {
				toRemove = hs;
			}
		}
		if (toRemove != null) {
			hostedServices.remove(toRemove);
		}
		
	}
}
