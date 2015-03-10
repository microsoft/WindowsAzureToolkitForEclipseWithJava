/**
* Copyright 2015 Microsoft Open Technologies, Inc.
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

import java.util.List;

public class CertificateUploadList {

	List<CertificateUpload> list;

	public CertificateUploadList() {
		super();
	}

	public CertificateUploadList(List<CertificateUpload> list) {
		super();
		this.list = list;
	}

	public List<CertificateUpload> getList() {
		return list;
	}

	public void setList(List<CertificateUpload> list) {
		this.list = list;
	}
}
