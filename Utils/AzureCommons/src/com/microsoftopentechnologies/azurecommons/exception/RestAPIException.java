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
