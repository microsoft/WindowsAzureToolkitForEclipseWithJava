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

import com.interopbridges.tools.windowsazure.WindowsAzureEndpoint;
import com.interopbridges.tools.windowsazure.WindowsAzureEndpointType;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.microsoftopentechnologies.azurecommons.messagehandler.PropUtil;
import com.microsoftopentechnologies.azurecommons.model.RoleAndEndpoint;

public class WARLoadBalanceUtilMethods {
	private final static int HTTP_PORT = 80;
	private final static int HTTP_PRV_PORT = 8080;
	private static String dbgEndPtStr = PropUtil.getValueFromFile("dbgEndPtStr");
	private static String lbHttpEndpt = PropUtil.getValueFromFile("lbHttpEndpt");

	/**
	 * find input endpoint to associate it with
	 * session affinity.
	 * @return WindowsAzureEndpoint
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public static WindowsAzureEndpoint findInputEndpt(WindowsAzureRole waRole,
			List<WindowsAzureEndpoint> endpointsList)
					throws WindowsAzureInvalidProjectOperationException {
		WindowsAzureEndpoint endpt = null;
		boolean isFirst = true;
		WindowsAzureEndpoint sslEndPt = waRole.getSslOffloadingInputEndpoint();
		if (sslEndPt != null) {
			endpt = sslEndPt;
		} else {
			for (WindowsAzureEndpoint endpoint : endpointsList) {
				if (endpoint.getEndPointType().
						equals(WindowsAzureEndpointType.Input)
						&& endpoint.getPrivatePort() != null
						&& !endpoint.equals(waRole.getDebuggingEndpoint())) {
					if (isFirst) {
						endpt = endpoint;
						isFirst = false;
					}
					if (endpoint.getPort().equalsIgnoreCase(String.valueOf(HTTP_PORT))) {
						endpt = endpoint;
						break;
					}
				}
			}
		}
		return endpt;
	}

	/**
	 * This method returns the selected endpoint name
	 * which was selected for sticky session.
	 * @return selectedEndpoint
	 * @throws WindowsAzureInvalidProjectOperationException 
	 */
	public static WindowsAzureEndpoint getStickySelectedEndpoint(WindowsAzureRole waRole,
			String comboEndptTxt) throws WindowsAzureInvalidProjectOperationException {
		List<WindowsAzureEndpoint> endpointsList;
		WindowsAzureEndpoint selectedEndpoint = null;

		endpointsList = new ArrayList<WindowsAzureEndpoint>(
				waRole.getEndpoints());
		for (WindowsAzureEndpoint endpoint : endpointsList) {
			if (comboEndptTxt.equals(
					String.format(dbgEndPtStr,
							endpoint.getName(),
							endpoint.getPort(),
							endpoint.getPrivatePort()))) {
				selectedEndpoint = endpoint;
			}
		}
		return selectedEndpoint;
	}

	/**
	 * Method creates endpoint associated with session affinity.
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public static RoleAndEndpoint createEndpt(WindowsAzureRole waRole,
			WindowsAzureProjectManager waProjManager)
					throws WindowsAzureInvalidProjectOperationException {
		WindowsAzureEndpoint endpt = null;
		StringBuffer endptName = new StringBuffer(lbHttpEndpt);
		int index = 1;
		int httpPort = HTTP_PORT;
		while (!waRole.isAvailableEndpointName(
				endptName.toString(),
				WindowsAzureEndpointType.Input)) {
			endptName.insert(4, index++);
		}

		while (!waProjManager.isValidPort(String.valueOf(httpPort),
				WindowsAzureEndpointType.Input)) {
			httpPort++;
		}
		endpt = waRole.addEndpoint(endptName.toString(),
				WindowsAzureEndpointType.Input,
				String.valueOf(HTTP_PRV_PORT),
				String.valueOf(httpPort));
		RoleAndEndpoint obj = new RoleAndEndpoint(waRole, endpt);
		return obj;
	}
}
