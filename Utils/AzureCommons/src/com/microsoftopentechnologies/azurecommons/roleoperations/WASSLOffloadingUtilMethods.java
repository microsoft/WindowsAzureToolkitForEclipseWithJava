/**
* Copyright Microsoft Corp.
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
