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

public enum Code {
	MissingOrIncorrectVersionHeader(400), 
	InvalidXmlRequest(400), 
	MissingOrInvalidRequiredQueryParameter(400), 
	InvalidHttpVerb(400), 
	AuthenticationFailed(403), 
	ResourceNotFound(404), 
	InternalError(500), 
	OperationTimedOut(500), 
	ServerBusy(503), 
	SubscriptionDisabled(403), 
	BadRequest(400), 
	ConflictError(409);

	private int code;

	Code(int code) {
		this.code = code;	
	}
	
	public int getCode() {
		return code;
	}
}
