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
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.microsoftopentechnologies.azurecommons.exception.AzureCommonsException;
import com.microsoftopentechnologies.azurecommons.messagehandler.PropUtil;


public class WAEnvVarDialogUtilMethods {
	private static String evInUseMsg = PropUtil.getValueFromFile("evInUseMsg");
	private static String envValMsg = PropUtil.getValueFromFile("envValMsg");
	/**
	 * Validates the variable name so that it should not be empty
	 * or an existing one.
	 *
	 * @return true if the variable name is valid, else false
	 * @throws AzureCommonsException 
	 */
	public static boolean validateName(String name,
			Map<String, String> mapEnvVar,
			boolean isEditVariable,
			String varName,
			WindowsAzureRole waRole) throws AzureCommonsException {
		boolean retVal = true;
		boolean isValidName = true;
		for (Iterator<String> iterator = mapEnvVar.keySet().iterator();
				iterator.hasNext();) {
			String key = iterator.next();
			if (key.trim().equalsIgnoreCase(name.trim())) {
				isValidName = false;
				break;
			}
		}
		try {
			if (!isValidName
					&& !(isEditVariable
							&& varName.equalsIgnoreCase(name))
							|| waRole.getLsEnv().contains(name)) {
				// Here the comparison is case-insensitive to avoid this message,
				// when user only changes case (upper/lower) of the name.
				throw new AzureCommonsException(evInUseMsg);
			}
		} catch (WindowsAzureInvalidProjectOperationException e) {
			throw new AzureCommonsException(envValMsg);
		}
		return retVal;
	}
}
