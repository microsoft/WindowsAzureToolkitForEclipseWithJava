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

package com.microsoftopentechnologies.azurecommons.roleoperations;

import java.util.ArrayList;
import java.util.List;

import com.interopbridges.tools.windowsazure.WindowsAzureEndpointType;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;

public class WASSLOffloadingUtilMethods {
	private static String lbSSLName = "SSL";
	private final static int HTTP_PRV_PORT = 8080;

	public static List<String> prepareEndpt(
			int suggPort,
			WindowsAzureRole waRole,
			WindowsAzureProjectManager waProjManager)
					throws WindowsAzureInvalidProjectOperationException {
		List<String> endPtData =  new ArrayList<String>();
		StringBuffer endptName = new StringBuffer(lbSSLName);
		int index = 1;
		int pubPort = suggPort;
		int priPort = HTTP_PRV_PORT;
		// find suitable name
		while (!waRole.isAvailableEndpointName(
				endptName.toString(),
				WindowsAzureEndpointType.Input)) {
			endptName.insert(3, index++);
		}
		// find suitable public port
		while (!waProjManager.isValidPort(
				String.valueOf(pubPort),
				WindowsAzureEndpointType.Input)) {
			pubPort++;
		}
		// find suitable private port
		while (!waRole.isValidEndpoint(
				endptName.toString(),
				WindowsAzureEndpointType.Input,
				String.valueOf(priPort),
				String.valueOf(pubPort))) {
			priPort++;
		}
		endPtData.add(0, endptName.toString());
		endPtData.add(1, String.valueOf(pubPort));
		endPtData.add(2, String.valueOf(priPort));
		return endPtData;
	}

}
