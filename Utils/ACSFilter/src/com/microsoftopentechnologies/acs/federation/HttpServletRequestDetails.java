/*
 Copyright 2013 Microsoft Open Technologies, Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package com.microsoftopentechnologies.acs.federation; 

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Class to capture the details of HTTP Servlet request.
 */

public final class HttpServletRequestDetails implements Serializable {
	private static final long serialVersionUID = -7502708191501514754L;

	private String method;
	private Map<String, String[]> parameterMap;

	public String getMethod() {
		return method;
	}
	public Map<String, String[]> getParameterMap() {
		return parameterMap;
	}

	private HttpServletRequestDetails()	{
	}

	public static HttpServletRequestDetails extractDetailsFromRequest(HttpServletRequest httpRequest) {
		HttpServletRequestDetails requestDetails = new HttpServletRequestDetails();
		requestDetails.method = httpRequest.getMethod();

		requestDetails.parameterMap = new HashMap<String, String[]>(httpRequest.getParameterMap()); //shallow copy
		return requestDetails;
	}
}
