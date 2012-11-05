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

import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="AffinityGroups")
public class AffinityGroups implements Iterable<AffinityGroup> {

	private List<AffinityGroup> affinityGroups;

	/**
	 * @return the affinityGroups
	 */
	@XmlElement(name = "AffinityGroup")
	public List<AffinityGroup> getAffinityGroups() {
		return affinityGroups;
	}

	/**
	 * @param affinityGroups
	 *            the affinityGroups to set
	 */
	public void setAffinityGroups(List<AffinityGroup> affinityGroups) {
		this.affinityGroups = affinityGroups;
	}

	@Override
	public Iterator<AffinityGroup> iterator() {
		return affinityGroups.iterator();
	}
}
