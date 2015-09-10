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
