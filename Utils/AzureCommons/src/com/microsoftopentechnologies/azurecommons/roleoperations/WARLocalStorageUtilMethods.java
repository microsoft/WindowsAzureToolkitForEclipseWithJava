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

import java.util.Iterator;
import java.util.Map;

import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureLocalStorage;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.microsoftopentechnologies.azurecommons.exception.AzureCommonsException;
import com.microsoftopentechnologies.azurecommons.messagehandler.PropUtil;


public class WARLocalStorageUtilMethods {
	private static String lclStgNameEmpMsg = PropUtil.getValueFromFile("lclStgNameEmpMsg");
	private static String lclStgNameErrMsg = PropUtil.getValueFromFile("lclStgNameErrMsg");
	private static String lclStgEnvVarMsg = PropUtil.getValueFromFile("lclStgEnvVarMsg");
	private static String lclStgPathErrMsg = PropUtil.getValueFromFile("lclStgPathErrMsg");
	private static String lclStgSetErrMsg = PropUtil.getValueFromFile("lclStgSetErrMsg");

	/**
	 * Handles the modification of local storage resource name.
	 *
	 * @param loclRes : the local storage resource being modified.
	 * @param modifiedVal : new value for resource name.
	 * @throws AzureCommonsException 
	 * @throws WindowsAzureInvalidProjectOperationException .
	 */
	public static WindowsAzureLocalStorage modifyName(WindowsAzureLocalStorage loclRes,
			Object modifiedVal,
			Map<String, WindowsAzureLocalStorage> mapLclStg)
					throws WindowsAzureInvalidProjectOperationException, AzureCommonsException {
		// Validate resource name
		if (modifiedVal.toString().isEmpty()) {
			throw new AzureCommonsException(lclStgNameEmpMsg);
		} else {
			StringBuffer strBfr =
					new StringBuffer(modifiedVal.toString());
			boolean isValidName = true;
			for (Iterator<String> iterator =
					mapLclStg.keySet().iterator();
					iterator.hasNext();) {
				String key = iterator.next();
				if (key.equalsIgnoreCase(strBfr.toString())) {
					isValidName = false;
					break;
				}
			}
			if (isValidName || modifiedVal.toString().equalsIgnoreCase(
					loclRes.getName())) {
				loclRes.setName(modifiedVal.toString());
			} else {
				throw new AzureCommonsException(lclStgNameErrMsg);
			}
		}
		return loclRes;
	}

	/**
	 * Handles the in-place modification of local storage resource.
	 *
	 * @param loclRes : the local storage resource being modified.
	 * @param modifiedVal : new value for path.
	 * @throws AzureCommonsException 
	 * @throws WindowsAzureInvalidProjectOperationException .
	 */
	public static WindowsAzureLocalStorage modifyPath(WindowsAzureLocalStorage loclRes,
			Object modifiedVal,
			Map<String, WindowsAzureLocalStorage> mapLclStg,
			WindowsAzureRole windowsAzureRole)
					throws WindowsAzureInvalidProjectOperationException, AzureCommonsException {
		StringBuffer strBfr = new StringBuffer(modifiedVal.toString());
		if (modifiedVal.toString().isEmpty()) {
			loclRes.setPathEnv("");
		}
		else if (!modifiedVal.toString().
				equalsIgnoreCase(loclRes.getPathEnv())) {
			try {
				boolean isPathValid = true;
				for (Iterator<WindowsAzureLocalStorage> iterator =
						mapLclStg.values().iterator();
						iterator.hasNext();) {
					WindowsAzureLocalStorage type =
							(WindowsAzureLocalStorage) iterator.next();
					if (type.getPathEnv().
							equalsIgnoreCase(strBfr.toString())) {
						isPathValid = false;
						break;
					}
				}
				if (windowsAzureRole.getRuntimeEnv().
						containsKey(strBfr.toString())) {
					isPathValid = false;
					throw new AzureCommonsException(lclStgEnvVarMsg);
				}
				else if (isPathValid
						|| modifiedVal.toString().equalsIgnoreCase(
								loclRes.getPathEnv())) {
					loclRes.setPathEnv(modifiedVal.toString());
				} else {
					throw new AzureCommonsException(lclStgPathErrMsg);
				}
			}
			catch (Exception e) {
				throw new AzureCommonsException(lclStgSetErrMsg);
			}
		}
		return loclRes;
	}

}
