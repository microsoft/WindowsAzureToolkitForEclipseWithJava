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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "EnumerationResults",namespace="")
public class EnumerationResults implements Iterable<Container> {

	private String accountName;
	private String prefix;
	private String marker;
	private int maxResults;
	private Containers containers;
	private String nextMarker;

	@Override
	public Iterator<Container> iterator() {
		if (getContainers() == null)
			return (new ArrayList<Container>()).iterator();

		return getContainers().iterator();
	}

	@XmlAttribute(name = "AccountName")
	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	@XmlElement(name = "MaxResults")
	public int getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}

	@XmlElement(name = "Marker")
	public String getMarker() {
		return marker;
	}

	public void setMarker(String marker) {
		this.marker = marker;
	}

	@XmlElement(name = "Prefix")
	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@XmlElement(name = "NextMarker")
	public String getNextMarker() {
		return nextMarker;
	}

	public void setNextMarker(String nextMarker) {
		this.nextMarker = nextMarker;
	}

	@XmlElement(name="Containers" ,namespace="")
	public Containers getContainers() {
		return containers;
	}

	public void setContainers(Containers containers) {
		this.containers = containers;
	}

}
