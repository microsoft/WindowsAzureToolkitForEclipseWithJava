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
package com.microsoftopentechnologies.wacommon.utils;

import java.io.File;
import java.security.cert.X509Certificate;

import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Class has utility methods to work with certificate files .cer and .pfx
 */
public class CerPfxUtil {

	/**
	 * Method opens file dialog to accept .cer or .pfx file.
	 * 
	 * @param shell
	 * @param path
	 * @return
	 */
	public static String importCerPfx(Shell shell, String path) {
		FileDialog dialog = new FileDialog(shell);
		String[] extensions = {"*.cer", "*.CER", "*.pfx", "*.PFX"};
		dialog.setFilterExtensions(extensions);
		dialog.setText("Select Certificate");
		// Default directory should be the cert directory in the project, and if
		// it
		// doesn't exist, then it should be the project directory
		String certPath = path + File.separator + "cert";
		if (new File(certPath).exists()) {
			dialog.setFilterPath(certPath);
		} else {
			dialog.setFilterPath(path);
		}
		String file = dialog.open();
		if (file != null) {
			file.replace('\\', '/');
		}
		return file;
	}

	/**
	 * Method returns X509Certificate certificate object.
	 * 
	 * @param certURL
	 * @return
	 */
	public static X509Certificate getCert(String certURL, String password) {
		return com.microsoftopentechnologies.azurecommons.wacommonutil.CerPfxUtil.getCert(certURL, password);
	}

	/**
	 * This method resolves environment variables in path. Format of env
	 * variables is ${env.VAR_NAME} e.g. ${env.JAVA_HOME}
	 * 
	 * @param rawPath
	 * @return
	 */
	public static String getCertificatePath(String rawPath) {
		return com.microsoftopentechnologies.azurecommons.wacommonutil.CerPfxUtil.getCertificatePath(rawPath);
	}
}
