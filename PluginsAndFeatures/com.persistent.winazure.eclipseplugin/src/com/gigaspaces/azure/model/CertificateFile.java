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
import com.microsoftopentechnologies.wacommon.utils.Base64;


@XmlRootElement(name = "CertificateFile")
@XmlType(propOrder = { "data", "certificateFormat", "password" })
public class CertificateFile {

	private String data;
	private String password;

	public CertificateFile() {
	}

	public CertificateFile(byte[] data, String passwod) {
		this.data = Base64.encode(data);		
		this.password = passwod;
	}

	@XmlElement(name = "Data")
	public String getData() {
		return data;
	}

	@XmlElement(name = "CertificateFormat")
	public String getCertificateFormat() {
		return Messages.certificateFormat;
	}

	@XmlElement(name = "Password")
	public String getPassword() {
		return password;
	}
}
