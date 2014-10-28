/**
* Copyright 2014 Microsoft Open Technologies, Inc.
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
package com.microsoftopentechnologies.roleoperations;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureRoleComponent;
import com.interopbridges.tools.windowsazure.WindowsAzureRoleComponentCloudMethod;
import com.interopbridges.tools.windowsazure.WindowsAzureRoleComponentDeployMethod;
import com.interopbridges.tools.windowsazure.WindowsAzureRoleComponentImportMethod;
import com.microsoftopentechnologies.util.WAEclipseHelperMethods;

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
