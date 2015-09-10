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

public class WARDebuggingUtilMethods {
	private static String dbgEndPtStr = PropUtil.getValueFromFile("dbgEndPtStr");
	private static String dlgDebug = PropUtil.getValueFromFile("dlgDebug");
	private final static int DEBUG_PORT = 8090;
	/**
	 * This method returns the selected endpoint name
	 * which was selected for debugging.
	 * @return selectedEndpoint
	 * @throws WindowsAzureInvalidProjectOperationException 
	 */
	public static WindowsAzureEndpoint getDebugSelectedEndpoint(WindowsAzureRole windowsAzureRole,
			String comboEndPointTxt) throws WindowsAzureInvalidProjectOperationException {
		List<WindowsAzureEndpoint> endpointsList;
		WindowsAzureEndpoint selectedEndpoint = null;
		endpointsList = new ArrayList<WindowsAzureEndpoint>(
				windowsAzureRole.getEndpoints());
		for (WindowsAzureEndpoint endpoint : endpointsList) {
			if (comboEndPointTxt.equals(
					String.format(dbgEndPtStr,
							endpoint.getName(),
							endpoint.getPort(),
							endpoint.getPrivatePort()))) {
				selectedEndpoint = endpoint;
			}
		}
		return selectedEndpoint;
	}

	public static RoleAndEndpoint getDebuggingEndpoint(WindowsAzureRole windowsAzureRole,
			WindowsAzureProjectManager waProjManager) throws WindowsAzureInvalidProjectOperationException {
		int endPointSuffix = 1;
		StringBuffer strBfr = new StringBuffer(dlgDebug);
		WindowsAzureEndpoint endpt = null;
		while (!windowsAzureRole.
				isAvailableEndpointName(strBfr.toString(),
						WindowsAzureEndpointType.Input)) {
			strBfr.delete(9, strBfr.length());
			strBfr.append(endPointSuffix++);
		}
		int publicPort = DEBUG_PORT;
		while (!waProjManager.isValidPort(
				String.valueOf(publicPort),
				WindowsAzureEndpointType.Input)) {
			publicPort++;
		}
		int privatePort = DEBUG_PORT;
		while (!waProjManager
				.isValidPort(String.valueOf(privatePort),
						WindowsAzureEndpointType.Input)) {
			privatePort++;
		}
		if (windowsAzureRole.isValidEndpoint(strBfr.toString(),
				WindowsAzureEndpointType.Input,
				String.valueOf(privatePort), String.valueOf(publicPort))) {
			endpt = windowsAzureRole
					.addEndpoint(strBfr.toString(),
							WindowsAzureEndpointType.Input,
							String.valueOf(privatePort),
							String.valueOf(publicPort));
		}
		RoleAndEndpoint obj = null;
		if (endpt != null) {
			obj = new RoleAndEndpoint(windowsAzureRole, endpt);
		}
		return obj;
	}
}
