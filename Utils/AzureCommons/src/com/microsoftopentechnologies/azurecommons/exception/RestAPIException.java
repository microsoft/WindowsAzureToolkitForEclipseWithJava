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
package com.microsoftopentechnologies.azurecommons.exception;

import com.microsoftopentechnologies.azurecommons.deploy.model.Response;
import com.microsoftopentechnologies.azurecommons.messagehandler.PropUtil;

public class RestAPIException extends Exception {

	private static final long serialVersionUID = -9138753688813724122L;
	private static String restAPIException = PropUtil.getValueFromFile("restAPIException");
	
	protected String status;
	protected String description;

	public RestAPIException(Response<?> response) {
		super(String.format(restAPIException,
				response.getStatus(), response.getDescription()));
	}
	
	public RestAPIException(Response<?> response, String desc) {
		super(String.format(restAPIException,
				response.getStatus(), desc));		
	}
	
	public RestAPIException(String errorCode, String errorMessage) {
		super(String.format(restAPIException,
				errorCode, errorMessage));		
	}

	public RestAPIException(String message) {
		super(message);
	}
}
