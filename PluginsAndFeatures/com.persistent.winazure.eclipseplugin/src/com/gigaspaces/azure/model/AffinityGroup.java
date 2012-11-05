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

import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
public class AffinityGroup {

	private UUID name;
	private String label;
	private String description;
	private String location;

	/**
	 * @return the name
	 */
	@XmlElement(name = "Name")
	public UUID getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(UUID name) {
		this.name = name;
	}

	/**
	 * @return the label
	 */
	@XmlElement(name = "Label")
	public String getLabel() {
		return label;
	}

	/**
	 * @param label
	 *            the label to set The user supplied name of the affinity group.
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the description
	 */
	@XmlElement(name = "Description")
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set The user supplied label of the affinity
	 *            group returned as a base-64 encoded string.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the location The user specified data center that this affinity
	 *         groups is located in.
	 */
	@XmlElement(name = "Location")
	public String getLocation() {
		return location;
	}

	/**
	 * @param location
	 *            the location to set
	 */
	public void setLocation(String location) {
		this.location = location;
	}
}
