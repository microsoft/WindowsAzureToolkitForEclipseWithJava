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

import java.util.Iterator;
import java.util.Map;

import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureLocalStorage;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.microsoftopentechnologies.azurecommons.exception.AzureCommonsException;
import com.microsoftopentechnologies.azurecommons.messagehandler.PropUtil;



public class LocalStrgResDialogUtilMethods {

	public static String lclStgResStr = PropUtil.getValueFromFile("lclStgResStr");
	private static String lclStgPathErrMsg = PropUtil.getValueFromFile("lclStgPathErrMsg");
	private static String lclStgEnvVarMsg = PropUtil.getValueFromFile("lclStgEnvVarMsg");
	private static String lclStgNameErrMsg = PropUtil.getValueFromFile("lclStgNameErrMsg");
	private static String lclStgSetErrMsg = PropUtil.getValueFromFile("lclStgSetErrMsg");

	// method to be used in createDialogArea to form local storage name
	public static StringBuffer formName(Map<String,WindowsAzureLocalStorage> lclStgMap) {
		StringBuffer strBfr = new StringBuffer(lclStgResStr);
		int lclStgSuffix = 1;
		boolean isValidName = true;
		do {
			isValidName = true;
			for (Iterator<String> iterator = lclStgMap.keySet().iterator();
					iterator.hasNext();) {
				String key = iterator.next();
				if (key.equalsIgnoreCase(strBfr.toString())) {
					isValidName = false;
					strBfr.delete(12, strBfr.length());
					strBfr.append(lclStgSuffix++);
					break;
				}
			}

		} while (!isValidName);
		return strBfr;
	}

	/**
	 * Validates the environment path.
	 *
	 * @param path : user given path
	 * @return retVal : true if valid path else false
	 * @throws AzureCommonsException 
	 * @throws WindowsAzureInvalidProjectOperationException 
	 */
	public static boolean isValidPath(String path,
			Map<String,WindowsAzureLocalStorage> lclStgMap,
			WindowsAzureRole windowsAzureRole)
					throws AzureCommonsException {
		boolean retVal = true;
		try {
			StringBuffer strBfr = new StringBuffer(path);
			if (!path.isEmpty()) {
				for (Iterator<WindowsAzureLocalStorage> iterator =
						lclStgMap.values().iterator(); iterator.hasNext();) {
					WindowsAzureLocalStorage type =
							(WindowsAzureLocalStorage) iterator.next();
					if (type.getPathEnv().
							equalsIgnoreCase(strBfr.toString())) {
						retVal = false;
						throw new AzureCommonsException(lclStgPathErrMsg);
					}
				}
				if (windowsAzureRole.getRuntimeEnv().
						containsKey(strBfr.toString())) {
					retVal = false;
					throw new AzureCommonsException(lclStgEnvVarMsg);
				}
			}
		} catch (Exception e) {
			retVal = false;
			throw new AzureCommonsException(lclStgSetErrMsg);
		}
		return retVal;
	}

	/**
	 * Validates the resource name of local storage.
	 *
	 * @param name : name to be validated.
	 * @return retVal : true if name is valid else false
	 * @throws AzureCommonsException 
	 */
	public static boolean isValidName(String name,
			Map<String,WindowsAzureLocalStorage> lclStgMap,
			boolean isResEdit,
			String resName) throws AzureCommonsException {
		boolean retVal = true;
		StringBuffer strBfr = new StringBuffer(name);
		boolean isValidName = true;
		for (Iterator<String> iterator = lclStgMap.keySet().iterator();
				iterator.hasNext();) {
			String key = iterator.next();
			if (key.equalsIgnoreCase(strBfr.toString())) {
				isValidName = false;
				break;
			}
		}

		if (!isValidName
				&& !(isResEdit && strBfr.toString().
						equalsIgnoreCase(resName))) {
			retVal = false;
			throw new AzureCommonsException(lclStgNameErrMsg);
		}
		return retVal;
	}
}
