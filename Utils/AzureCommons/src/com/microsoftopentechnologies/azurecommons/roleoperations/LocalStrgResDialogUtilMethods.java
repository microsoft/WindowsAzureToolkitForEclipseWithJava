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
