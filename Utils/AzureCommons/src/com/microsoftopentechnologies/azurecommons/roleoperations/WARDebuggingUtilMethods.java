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
