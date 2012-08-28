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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.gigaspaces.azure.rest.InvalidRestAPIArgument;

@XmlRootElement(name = "CreateDeployment")
@XmlType(propOrder = { "name", "packageUrl", "label", "configuration",
		"startDeployment", "treatWarningsAsError" })
public class CreateDeployment {

	private String name;
	private String packageUrl;
	private String label;
	private String configuration;
	private boolean startDeployment;
	private boolean treatWarningsAsError;

	public CreateDeployment() {
	}

	public CreateDeployment(String name, String packageUrl, String label,
			String configuration) {

		if (name == null || name.isEmpty())
			throw new InvalidRestAPIArgument(Messages.invalidName);

		this.name = name;

		if (packageUrl == null || packageUrl.isEmpty())
			throw new InvalidRestAPIArgument(Messages.invalidPackageUrl);

		this.packageUrl = packageUrl;

		if (label == null || label.isEmpty())
			throw new InvalidRestAPIArgument(Messages.invalidLabel);

		this.label = label;

		if (configuration == null || configuration.isEmpty())
			throw new InvalidRestAPIArgument(Messages.invalidConfiguration);

		this.configuration = configuration;
	}

	@XmlElement(name = "Name")
	public String getName() {
		return name;
	}

	@XmlElement(name = "PackageUrl")
	public String getPackageUrl() {
		return packageUrl;
	}

	@XmlElement(name = "Label")
	public String getLabel() {
		return label;
	}

	@XmlElement(name = "Configuration")
	public String getConfiguration() {
		return configuration;
	}

	@XmlElement(name = "StartDeployment")
	public boolean isStartDeployment() {
		return startDeployment;
	}

	public void setStartDeployment(boolean startDeployment) {
		this.startDeployment = startDeployment;
	}

	@XmlElement(name = "TreatWarningsAsError")
	public boolean isTreatWarningsAsError() {
		return treatWarningsAsError;
	}

	public void setTreatWarningsAsError(boolean treatWarningsAsError) {
		this.treatWarningsAsError = treatWarningsAsError;
	}
}
