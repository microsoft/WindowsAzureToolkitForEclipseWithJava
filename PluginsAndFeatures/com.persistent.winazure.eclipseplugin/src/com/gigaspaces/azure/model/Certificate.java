/**
* Copyright 2013 Persistent Systems Ltd.
*
* Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*	 http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/

package com.gigaspaces.azure.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlRootElement(name = "Certificate",namespace="http://schemas.microsoft.com/windowsazure")
@XmlType(propOrder = { "certificateUrl", "thumbprint", "thumbprintAlgorithm",
		"data" })
public class Certificate {
	
	private String certificateUrl;
	private String thumbprint;
	private String thumbprintAlgorithm;
	private String data;
	
	@XmlElement(name = "CertificateUrl", namespace="http://schemas.microsoft.com/windowsazure")
	public String getCertificateUrl() {
		return certificateUrl;
	}
	
	public void setCertificateUrl(String certificateUrl) {
		this.certificateUrl = certificateUrl;
	}
	
	@XmlElement(name = "Thumbprint", namespace="http://schemas.microsoft.com/windowsazure")
	public String getThumbprint() {
		return thumbprint;
	}
	
	public void setThumbprint(String thumbprint) {
		this.thumbprint = thumbprint;
	}
	
	@XmlElement(name = "ThumbprintAlgorithm", namespace="http://schemas.microsoft.com/windowsazure")
	public String getThumbprintAlgorithm() {
		return thumbprintAlgorithm;
	}
	
	public void setThumbprintAlgorithm(String thumbprintAlgorithm) {
		this.thumbprintAlgorithm = thumbprintAlgorithm;
	}
	
	@XmlElement(name = "Data" ,namespace="http://schemas.microsoft.com/windowsazure")
	public String getData() {
		return data;
	}
	
	public void setData(String data) {
		this.data = data;
	}
}
