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

package com.microsoftopentechnologies.azurecommons.preference;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.microsoftopentechnologies.azurecommons.messagehandler.PropUtil;
import com.microsoftopentechnologies.azurecommons.storageregistry.StorageAccount;
import com.microsoftopentechnologies.azurecommons.storageregistry.StorageAccountRegistry;
import com.microsoftopentechnologies.azurecommons.util.WAEclipseHelperMethods;

public class PreferenceUtilMethods {

	private static String namelnErMsg = PropUtil.getValueFromFile("namelnErMsg");
	private static String nameRxErMsg = PropUtil.getValueFromFile("nameRxErMsg");
	private static String keyErrMsg = PropUtil.getValueFromFile("keyErrMsg");
	private static String keyRegexErrMsg = PropUtil.getValueFromFile("keyRegexErrMsg");
	private static String blobEnPt = PropUtil.getValueFromFile("blobEnPt");
	private static String http = PropUtil.getValueFromFile("http");
	private static String https = PropUtil.getValueFromFile("https");
	private static String urlErMsg = PropUtil.getValueFromFile("urlErMsg");
	private static String urlPreErrMsg = PropUtil.getValueFromFile("urlPreErrMsg");

	public static StorageAccPrefPageTableElements getPrefPageTableElements() {
		List<StorageAccount> strgList =
				StorageAccountRegistry.getStrgList();
		List<StorageAccPrefPageTableElement> tableRowElements =
				new ArrayList<StorageAccPrefPageTableElement>();
		for (StorageAccount storageAcc : strgList) {
			if (storageAcc != null) {
				StorageAccPrefPageTableElement ele =
						new StorageAccPrefPageTableElement();
				ele.setStorageName(storageAcc.getStrgName());
				ele.setStorageUrl(storageAcc.getStrgUrl());
				tableRowElements.add(ele);
			}
		}
		StorageAccPrefPageTableElements elements =
				new StorageAccPrefPageTableElements();
		elements.setElements(tableRowElements);
		return elements;
	}

	/**
	 * Method validates storage account name and return error string if any.
	 * @return
	 */
	public static String validateName(String name) {
		String error = "";
		if (WAEclipseHelperMethods.isLowerCaseAndInteger(name)) {
			if (name.length() >= 3
					&& name.length() <= 24) {
				error = "";
			} else {
				error = namelnErMsg;
			}
		} else {
			error = nameRxErMsg;
		}
		return error;
	}


	public static String storageAccDlgOkPressed(boolean isEdit,
			String key,
			String name,
			String url,
			StorageAccount account) {
		String error = "";
		// edit scenario.
		if (isEdit) {
			// check access key is changed, then edit else not.
			if (!account.getStrgKey().equals(key)) {
				if (key.contains(" ")) {
					error = keyErrMsg;
				} else if (!WAEclipseHelperMethods.isValidStorageAccAccessKey(key)) {
					error = keyRegexErrMsg;
				} else {
					StorageAccountRegistry.
					editAccountAccessKey(account, key);
				}
			}
		} else {
			// add scenario.
			// validate account name'
			error = validateName(name);
			if (error != null && error.isEmpty()) {
				// validate URL
				try {
					// append '/' if not present.
					if (!url.endsWith("/")) {
						url = url + "/";
					}
					if (url.equalsIgnoreCase(name + blobEnPt)) {
						url = String.format("%s%s%s",
								http,
								name,
								blobEnPt);
					}
					new URL(url);
					if (url.startsWith(http + name + '.')
							|| url.startsWith(https + name + '.')) {
						// validate access key
						if (key.contains(" ")) {
							error = keyErrMsg;
						} else if (!WAEclipseHelperMethods.isValidStorageAccAccessKey(key)) {
							error = keyRegexErrMsg;
						} else {
							// check account does not exist previously
							StorageAccount accToCheck = new StorageAccount(
									name,
									key,
									url);
							if (!StorageAccountRegistry.getStrgList().contains(accToCheck)) {
								StorageAccountRegistry.addAccount(accToCheck);
							} else {
								error = urlPreErrMsg;
							}
						}
					} else {
						error = urlErMsg;
					}
				} catch (MalformedURLException e) {
					error = urlErMsg;
				}
			}
		}
		return error;
	}
}
