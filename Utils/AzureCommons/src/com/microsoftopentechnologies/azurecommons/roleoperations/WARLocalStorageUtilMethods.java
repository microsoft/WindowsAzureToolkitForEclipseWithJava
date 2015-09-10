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
