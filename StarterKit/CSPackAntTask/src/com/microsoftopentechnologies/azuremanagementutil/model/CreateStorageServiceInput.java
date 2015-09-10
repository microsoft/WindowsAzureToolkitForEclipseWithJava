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
package com.microsoftopentechnologies.azuremanagementutil.model;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.microsoftopentechnologies.azuremanagementutil.exception.InvalidRestAPIArgument;
import com.microsoftopentechnologies.azuremanagementutil.util.Base64;

@XmlRootElement(name = "CreateStorageServiceInput")
@XmlType(propOrder = { "serviceName", "description", "label",  "location", "affinityGroup" })
public class CreateStorageServiceInput {
	public static String invalidServiceName = "incorrect serviceName value.";
	public static String invalidLabelValue = "incorrect label value.";
	public static String invalidAffinityGroup = "incorrect AffinityGroup value.";
	public static String invalidLocation = "incorrect location value.";
	public static String invalidDescription = "the description can not be longer than 1024 characters.";
	public static String invalidLabelLength = "the label can not be longer than 100 characters.";

	private String serviceName;
	private String label;
	private String description;
	private String location;
	private UUID affinityGroup;


	public CreateStorageServiceInput() {

	}

	/**
	 * @param serviceName
	 *            the serviceName to set Required. A name for the hosted service
	 *            that is unique within Azure. This name is the DNS
	 *            prefix name and can be used to access the hosted service.
	 * @param label
	 *            the label to set Required. A name for the hosted service that
	 *            is base-64 encoded. The name can be up to 100 characters in
	 *            length. The name can be used identify the storage account for
	 *            your tracking purposes.
	 */
	private CreateStorageServiceInput(String serviceName, String label) {
		if (serviceName == null || serviceName.isEmpty())
			throw new InvalidRestAPIArgument(invalidServiceName);

		if (label == null || label.isEmpty())
			throw new InvalidRestAPIArgument(invalidLabelValue);

		this.serviceName = serviceName;

		String labelLength = label;
		try {
			this.label = Base64.encode(label.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
		}
		if (labelLength.length() > 100)
			throw new InvalidRestAPIArgument(
					invalidLabelLength);
	}

	public CreateStorageServiceInput(String serviceName, String label, UUID affinityGroup) {
		this(serviceName, label);

		if (affinityGroup == null)
			throw new InvalidRestAPIArgument(invalidAffinityGroup);

		this.affinityGroup = affinityGroup;
	}

	public CreateStorageServiceInput(String serviceName, String label, String location) {
		this(serviceName, label);

		if (location == null || location.isEmpty())
			throw new InvalidRestAPIArgument(invalidLocation);

		this.location = location;
	}

	/**
	 * @return the serviceName
	 */
	@XmlElement(name = "ServiceName",namespace="")
	public String getServiceName() {
		return serviceName;
	}

	/**
	 * @return the label
	 */
	@XmlElement(name = "Label",namespace="")
	public String getLabel() {
		return label;
	}

	/**
	 * @return the description
	 */
	@XmlElement(name = "Description",namespace="")
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set Optional. A description for the hosted
	 *            service. The description can be up to 1024 characters in
	 *            length.
	 */
	public void setDescription(String description) {
		if (description.length() > 1024)
			throw new InvalidRestAPIArgument(
					invalidDescription);

		this.description = description;
	}

	/**
	 * @return the location
	 */
	@XmlElement(name = "Location",namespace="")
	public String getLocation() {
		return location;
	}

	/**
	 * @return the affinityGroup
	 */
	@XmlElement(name = "AffinityGroup",namespace="")
	public UUID getAffinityGroup() {
		return affinityGroup;
	}

}
