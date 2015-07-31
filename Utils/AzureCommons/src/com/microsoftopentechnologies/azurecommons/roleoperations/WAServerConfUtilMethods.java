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

import java.io.File;
import java.util.List;

import com.interopbridges.tools.windowsazure.WindowsAzureEndpointType;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzurePackageType;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.interopbridges.tools.windowsazure.WindowsAzureRoleComponent;
import com.microsoftopentechnologies.azurecommons.messagehandler.PropUtil;



public class WAServerConfUtilMethods {
	private static String lbHttpEndpt = PropUtil.getValueFromFile("lbHttpEndpt");
	private final static int HTTP_PORT = 80;
	private static String jvHome = PropUtil.getValueFromFile("jvHome");
	private static String jvHomeErr = PropUtil.getValueFromFile("jvHomeErr");
	private static String typeSrvHm = PropUtil.getValueFromFile("typeSrvHm");
	private static String srvHomeErr = PropUtil.getValueFromFile("srvHomeErr");

	public static WindowsAzureRole addEndpt(String srvPriPort, WindowsAzureRole windowsAzureRole)
			throws WindowsAzureInvalidProjectOperationException {
		StringBuffer endptName = new StringBuffer(lbHttpEndpt);
		int index = 1;
		// find suitable name
		while (!windowsAzureRole.isAvailableEndpointName(
				endptName.toString(),
				WindowsAzureEndpointType.Input)) {
			endptName.insert(4, index++);
		}
		windowsAzureRole.addEndpoint(endptName.toString(),
				WindowsAzureEndpointType.Input,
				srvPriPort,
				String.valueOf(HTTP_PORT));
		return windowsAzureRole;
	}

	/**
	 * Method returns component object according to component type.
	 * If component not present then returns NULL.
	 * @param cmpntType
	 * @return WindowsAzureRoleComponent
	 * @throws WindowsAzureInvalidProjectOperationException 
	 */
	public static WindowsAzureRoleComponent getPrevCmpnt(
			String cmpntType, WindowsAzureRole windowsAzureRole)
					throws WindowsAzureInvalidProjectOperationException {
		List<WindowsAzureRoleComponent> listComponents = null;
		WindowsAzureRoleComponent cmp = null;
		listComponents = windowsAzureRole.getComponents();
		for (int i = 0; i < listComponents.size(); i++) {
			if (listComponents.get(i).getType().
					equalsIgnoreCase(cmpntType)) {
				cmp = listComponents.get(i);
			}
		}
		return cmp;
	}

	/**
	 * Method updates java home,
	 * according to current package type.
	 * Method will get called when user click
	 * on OK button or tries to navigate to other page.
	 * @param javaHome
	 * @throws Exception 
	 */
	public static WindowsAzureRole updateJavaHome(String javaHome,
			WindowsAzureRole windowsAzureRole,
			WindowsAzureProjectManager waProjManager,
			String text,
			File cmpntFile) throws Exception {
		try {
			if (waProjManager.getPackageType().
					equals(WindowsAzurePackageType.LOCAL)) {
				windowsAzureRole.setJDKCloudHome(
						javaHome);
				windowsAzureRole.setJDKLocalHome(null);
			} else {
				windowsAzureRole.
				setRuntimeEnv(jvHome, javaHome);
				windowsAzureRole.setJDKLocalHome(
						WindowsAzureRole.constructJdkHome(
								text,
								cmpntFile));
			}
			return windowsAzureRole;
		} catch (Exception e) {
			throw new Exception(jvHomeErr, e);
		}
	}

	/**
	 * Method updates server home,
	 * according to current package type.
	 * Method will get called when user click
	 * on OK button or tries to navigate to other page.
	 * @param srvHome
	 * @throws Exception 
	 */
	public static WindowsAzureRole updateServerHome(String srvHome,
			WindowsAzureRole windowsAzureRole,
			WindowsAzureProjectManager waProjManager,
			String path,
			String name,
			File cmpntFile) throws Exception {
		try {
			if (waProjManager.getPackageType().
					equals(WindowsAzurePackageType.LOCAL)) {
				windowsAzureRole.setServerCloudHome(
						srvHome);
				windowsAzureRole.setServerLocalHome(null);
			} else {
				windowsAzureRole.
				setRuntimeEnv(windowsAzureRole.
						getRuntimeEnvName(
								typeSrvHm),
								srvHome);
				windowsAzureRole.setServerLocalHome(
						WindowsAzureRole.constructServerHome(
								name,
								path,
								cmpntFile));
			}
			return windowsAzureRole;
		} catch (Exception e) {
			throw new Exception(srvHomeErr, e);
		}
	}

	/**
	 * Method removes java home settings,
	 * according to current package type.
	 * Method will get called on the event of
	 * check box uncheck.
	 * @throws WindowsAzureInvalidProjectOperationException 
	 */
	public static WindowsAzureRole removeJavaHomeSettings(WindowsAzureRole windowsAzureRole,
			WindowsAzureProjectManager waProjManager)
					throws WindowsAzureInvalidProjectOperationException {
		if (waProjManager.getPackageType().
				equals(WindowsAzurePackageType.LOCAL)) {
			windowsAzureRole.setJDKCloudHome(null);
		} else {
			String localVal =
					windowsAzureRole.getJDKLocalHome();
			if (localVal != null
					&& !localVal.isEmpty()) {
				windowsAzureRole.setRuntimeEnv(jvHome, localVal);
			}
			windowsAzureRole.setJDKLocalHome(null);
		}
		return windowsAzureRole;
	}

	/**
	 * Method removes server home settings,
	 * according to current package type.
	 * Method will get called on the event of
	 * check box uncheck.
	 * @throws WindowsAzureInvalidProjectOperationException 
	 */
	public static WindowsAzureRole removeServerHomeSettings(WindowsAzureRole windowsAzureRole,
			WindowsAzureProjectManager waProjManager)
					throws WindowsAzureInvalidProjectOperationException {
		if (waProjManager.getPackageType().
				equals(WindowsAzurePackageType.LOCAL)) {
			windowsAzureRole.setServerCloudHome(null);
		} else {
			String localVal =
					windowsAzureRole.getServerLocalHome();
			String name = windowsAzureRole.getRuntimeEnvName(typeSrvHm);
			if (name != null
					&& !name.isEmpty()
					&& localVal != null) {
				windowsAzureRole.setRuntimeEnv(name, localVal);
			}
			windowsAzureRole.setServerLocalHome(null);
		}
		return windowsAzureRole;
	}
}
