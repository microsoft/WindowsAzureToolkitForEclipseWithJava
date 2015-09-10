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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureRoleComponent;
import com.interopbridges.tools.windowsazure.WindowsAzureRoleComponentCloudMethod;
import com.interopbridges.tools.windowsazure.WindowsAzureRoleComponentDeployMethod;
import com.interopbridges.tools.windowsazure.WindowsAzureRoleComponentImportMethod;
import com.microsoftopentechnologies.azurecommons.util.WAEclipseHelperMethods;

public class ImportExportDialogUtilMethods {
	/**
	 * Method validates URL and access key given
	 * for deploy from download group.
	 * @return
	 */
	public static boolean validateDlGroup(boolean dlCheckBtn, String url) {
		boolean isValidUrl = true;
		try {
			if (dlCheckBtn) {
				if (url.isEmpty()) {
					isValidUrl = false;
				} else {
					new URL(url);
					if (!WAEclipseHelperMethods.isBlobStorageUrl(url)) {
						isValidUrl = false;
					}
				}
			}
		} catch (MalformedURLException e) {
			isValidUrl = false;
		}
		return isValidUrl;
	}

	/**
	 * Method returns As Name according to
	 * import method.
	 * @return
	 */
	public static String getAsName(File file, String comboImportTxt) {
		String name = file.getName();
		// Replacing spaces with underscore
		if (name != null && !name.isEmpty()) {
			name = name.trim().replaceAll("\\s+", "_");
		}
		if (comboImportTxt.equalsIgnoreCase("EAR")) {
			name = name.concat(".ear");
		} else if (comboImportTxt.equalsIgnoreCase("WAR")) {
			name = name.concat(".war");
		} else if (comboImportTxt.equalsIgnoreCase("JAR")) {
			name = name.concat(".jar");
		} else if (comboImportTxt.equalsIgnoreCase(
				WindowsAzureRoleComponentImportMethod.zip.name())) {
			if (file.isFile()) {
				name = name.substring(0, name.lastIndexOf("."));
			}
			name = name.concat(".zip");
		}
		return name;
	}

	/**
	 * Converts the array of objects to String array.
	 * @param obj : array of objects
	 * @return
	 */
	public static String[] convertObjToStr(Object[] obj) {
		String[] text = new String[obj.length];
		for (int i = 0; i < obj.length; i++) {
			text[i] = obj[i].toString();
		}
		return text;
	}

	public static WindowsAzureRoleComponent okPressedPart2(WindowsAzureRoleComponent winAzureRoleCmpnt,
			String txtToDir,
			String deployMethod,
			String name,
			String importMethod,
			String url,
			String[] cloudMethods,
			String cloudMethod,
			String key) throws WindowsAzureInvalidProjectOperationException {
		if (!txtToDir.isEmpty()
				|| !txtToDir.equalsIgnoreCase("\\.")) {
			winAzureRoleCmpnt.setDeployDir(txtToDir);
		}
		winAzureRoleCmpnt.setDeployMethod(
				WindowsAzureRoleComponentDeployMethod.
				valueOf(deployMethod));
		winAzureRoleCmpnt.setDeployName(name);
		if (importMethod.equalsIgnoreCase("WAR")
				|| importMethod.equalsIgnoreCase("JAR")
				|| importMethod.equalsIgnoreCase("EAR")) {
			winAzureRoleCmpnt.setImportMethod(
					WindowsAzureRoleComponentImportMethod.auto);
		} else {
			winAzureRoleCmpnt.setImportMethod(
					WindowsAzureRoleComponentImportMethod.
					valueOf(importMethod));
		}
		// set cloud URL.
		winAzureRoleCmpnt.setCloudDownloadURL(url);
		// set access key.
		winAzureRoleCmpnt.setCloudKey(key);
		// set cloud method.
		if (cloudMethod.equalsIgnoreCase(cloudMethods[0])
				|| cloudMethod.isEmpty()) {
			winAzureRoleCmpnt.setCloudMethod(
					WindowsAzureRoleComponentCloudMethod.none);
		} else {
			winAzureRoleCmpnt.setCloudMethod(
					WindowsAzureRoleComponentCloudMethod.
					valueOf(cloudMethod));
		}
		return winAzureRoleCmpnt;
	}
}
