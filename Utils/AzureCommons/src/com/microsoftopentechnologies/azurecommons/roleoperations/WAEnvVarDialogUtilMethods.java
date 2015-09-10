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
