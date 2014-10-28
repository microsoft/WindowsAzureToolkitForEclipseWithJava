/**
* Copyright 2014 Microsoft Open Technologies, Inc.
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
package com.gigaspaces.azure.rest;

import com.microsoftopentechnologies.deploy.model.Response;
import com.microsoftopentechnologies.exception.RestAPIException;


public class RestAPIConflictException extends RestAPIException {

	private static final long serialVersionUID = 107514770294762237L;
	
	public RestAPIConflictException(Response<?> response) {
		super(response, response.getDescription());
	}
}
