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

@XmlRootElement(name = "Operation")
public class Operation {
	
	private String id;
	private String status;
	private String httpStatusCode;
	private Error error;
	
	@XmlElement(name="ID")
	public String getID() {
		return id;
	}
	
	public void setID(String iD) {
		id = iD;
	}
	
	@XmlElement(name="Status")
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	@XmlElement(name="HttpStatusCode")
	public String getHttpStatusCode() {
		return httpStatusCode;
	}
	
	public void setHttpStatusCode(String httpStatusCode) {
		this.httpStatusCode = httpStatusCode;
	}

	@XmlElement(name="Error")
	public Error getError() {
		return error;
	}

	public void setError(Error error) {
		this.error = error;
	}

}
