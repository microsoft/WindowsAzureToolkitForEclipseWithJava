/**
* Copyright Microsoft Corp.
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
package com.microsoftopentechnologies.azurecommons.deploy.model;

public class CertificateUpload {
	String name;
	String thumbprint;
	String pfxPath;
	String pfxPwd;

	public CertificateUpload() {
		super();
	}

	public CertificateUpload(
			String name,
			String thumbprint,
			String pfxPath,
			String pfxPwd) {
		super();
		this.name = name;
		this.thumbprint = thumbprint;
		this.pfxPath = pfxPath;
		this.pfxPwd = pfxPwd;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getThumbprint() {
		return thumbprint;
	}

	public void setThumbprint(String thumbprint) {
		this.thumbprint = thumbprint;
	}

	public String getPfxPath() {
		return pfxPath;
	}

	public void setPfxPath(String pfxPath) {
		this.pfxPath = pfxPath;
	}

	public String getPfxPwd() {
		return pfxPwd;
	}

	public void setPfxPwd(String pfxPwd) {
		this.pfxPwd = pfxPwd;
	}
}
