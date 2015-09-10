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
