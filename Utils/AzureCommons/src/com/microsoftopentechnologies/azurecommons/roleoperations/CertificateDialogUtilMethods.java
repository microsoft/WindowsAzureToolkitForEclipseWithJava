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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.interopbridges.tools.windowsazure.WindowsAzureCertificate;
import com.microsoftopentechnologies.azurecommons.exception.AzureCommonsException;
import com.microsoftopentechnologies.azurecommons.messagehandler.PropUtil;
import com.microsoftopentechnologies.azurecommons.util.WAEclipseHelperMethods;

public class CertificateDialogUtilMethods {
	private static String certAddErrMsg = PropUtil.getValueFromFile("certAddErrMsg");
	private static String certRegMsg = PropUtil.getValueFromFile("certAddErrMsg");

	public static boolean isNameAlreadyPresent(String name, Map<String, WindowsAzureCertificate> mapCert) {
		boolean isPresent = false;
		for (Iterator<Entry<String, WindowsAzureCertificate>> iterator =
				mapCert.entrySet().iterator();
				iterator.hasNext();) {
			WindowsAzureCertificate cert = iterator.next().getValue();
			if (cert.getName().trim().equalsIgnoreCase(name)) {
				isPresent = true;
				break;
			}
		}
		return isPresent;
	}

	public static String removeSpaceFromCN(String nameParam) {
		String name = nameParam;
		name = name.replaceAll("\\s+", "");
		return name.substring(name.indexOf("=") + 1);
	}

	/**
	 * Method checks if certificate name is already
	 * used then make it unique by concatenating current date.
	 * @param certName
	 */
	public static String populateCertName(String certNameParam,
			Map<String, WindowsAzureCertificate> mapCert) {
		String certName = certNameParam;
		if (isNameAlreadyPresent(certName, mapCert)) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
			Date now = new Date();
			certName = certName + dateFormat.format(now);
		}
		return certName;
	}

	public static boolean validateNameAndThumbprint(String name, String thumb,
			Map<String, WindowsAzureCertificate> mapCert) throws AzureCommonsException {
		boolean retVal = true;
		if (WAEclipseHelperMethods.isAlphaNumericUnderscore(name)) {
			for (Iterator<Entry<String, WindowsAzureCertificate>> iterator =
					mapCert.entrySet().iterator();
					iterator.hasNext();) {
				WindowsAzureCertificate cert = iterator.next().getValue();
				if (cert.getName().trim().equalsIgnoreCase(name)
						|| cert.getFingerPrint().trim().equalsIgnoreCase(thumb)) {
					retVal = false;
					throw new AzureCommonsException(certAddErrMsg);
				}
			}
		} else {
			retVal = false;
			throw new AzureCommonsException(certRegMsg);
		}
		return retVal;
	}
}
