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
import java.util.Map.Entry;

import com.interopbridges.tools.windowsazure.WindowsAzureCertificate;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.microsoftopentechnologies.azurecommons.exception.AzureCommonsException;
import com.microsoftopentechnologies.azurecommons.messagehandler.PropUtil;
import com.microsoftopentechnologies.azurecommons.util.WAEclipseHelperMethods;


public class WARCertificatesUtilMethods {
	private static String certInvMsg = PropUtil.getValueFromFile("certInvMsg");
	private static String certAddErrMsg = PropUtil.getValueFromFile("certAddErrMsg");
	private static String certRegMsg = PropUtil.getValueFromFile("certRegMsg");

	public static void modifyName(WindowsAzureCertificate cert, Object modifiedVal,
			Map<String, WindowsAzureCertificate> mapCert)
					throws WindowsAzureInvalidProjectOperationException, AzureCommonsException {
		String modifiedTxt = modifiedVal.toString().trim();
		if (modifiedTxt.isEmpty()) {
			throw new AzureCommonsException(certInvMsg);
		} else {
			if (WAEclipseHelperMethods.isAlphaNumericUnderscore(modifiedTxt)) {
				boolean isValidName = true;
				for (Iterator<String> iterator =
						mapCert.keySet().iterator();
						iterator.hasNext();) {
					String key = iterator.next();
					if (key.equalsIgnoreCase(modifiedTxt)) {
						isValidName = false;
						break;
					}
				}
				if (isValidName || modifiedTxt.equalsIgnoreCase(
						cert.getName())) {
					cert.setName(modifiedTxt);
				} else {
					throw new AzureCommonsException(certAddErrMsg);
				}
			} else {
				throw new AzureCommonsException(certRegMsg);
			}
		}
	}

	public static void modifyThumb(WindowsAzureCertificate cert, Object modifiedVal,
			Map<String, WindowsAzureCertificate> mapCert)
					throws WindowsAzureInvalidProjectOperationException, AzureCommonsException {
		String modifiedTxt = modifiedVal.toString().trim();
		if (modifiedTxt.isEmpty()) {
			throw new AzureCommonsException(certInvMsg);
		} else {
			boolean isValidName = true;
			for (Iterator<Entry<String, WindowsAzureCertificate>> iterator =
					mapCert.entrySet().iterator();
					iterator.hasNext();) {
				WindowsAzureCertificate certObj = iterator.next().getValue();
				if (certObj.getFingerPrint().equalsIgnoreCase(modifiedTxt)) {
					isValidName = false;
					break;
				}
			}
			if (isValidName || modifiedTxt.equalsIgnoreCase(
					cert.getFingerPrint())) {
				cert.setFingerPrint(modifiedTxt.toUpperCase());
			} else {
				throw new AzureCommonsException(certAddErrMsg);
			}
		}
	}
}
