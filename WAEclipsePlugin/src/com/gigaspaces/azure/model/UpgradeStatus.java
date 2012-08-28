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
import javax.xml.bind.annotation.XmlType;

@XmlType
public class UpgradeStatus {

	private UpgradeStatus upgradeType;
	private UpgradeDomainState currentUpgradeDomainState;
	private String currentUpgradeDomain;
	
	@XmlElement(name="UpgradeType")
	public UpgradeStatus getUpgradeType() {
		return upgradeType;
	}
	public void setUpgradeType(UpgradeStatus upgradeType) {
		this.upgradeType = upgradeType;
	}
	
	@XmlElement(name="CurrentUpgradeDomainState")
	public UpgradeDomainState getCurrentUpgradeDomainState() {
		return currentUpgradeDomainState;
	}
	public void setCurrentUpgradeDomainState(UpgradeDomainState currentUpgradeDomainState) {
		this.currentUpgradeDomainState = currentUpgradeDomainState;
	}
	@XmlElement(name="CurrentUpgradeDomain")
	public String getCurrentUpgradeDomain() {
		return currentUpgradeDomain;
	}
	public void setCurrentUpgradeDomain(String currentUpgradeDomain) {
		this.currentUpgradeDomain = currentUpgradeDomain;
	}
}
