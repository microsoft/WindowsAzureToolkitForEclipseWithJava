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

package com.microsoftopentechnologies.azurecommons.propertypage;

import com.interopbridges.tools.windowsazure.OSFamilyType;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzurePackageType;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.microsoftopentechnologies.azurecommons.messagehandler.PropUtil;

public class Azure {
	private static String proPageBFEmul = PropUtil.getValueFromFile("proPageBFEmul");

	public static WindowsAzureProjectManager performOK(WindowsAzureProjectManager waProjManager,
			String serviceName,
			String type,
			String osTypeCombo) throws WindowsAzureInvalidProjectOperationException {
		if (waProjManager.isValidServiceName(serviceName)) {
			waProjManager.setServiceName(serviceName);
		}
		if (type.equalsIgnoreCase(proPageBFEmul)) {
			waProjManager.setPackageType(WindowsAzurePackageType.LOCAL);
		} else {
			waProjManager.setPackageType(WindowsAzurePackageType.CLOUD);
		}

		for (OSFamilyType osType : OSFamilyType.values()) {
			if (osType.getName().equalsIgnoreCase(osTypeCombo)) {
				waProjManager.setOSFamily(osType);
			}
		}
		return waProjManager;
	}
}
