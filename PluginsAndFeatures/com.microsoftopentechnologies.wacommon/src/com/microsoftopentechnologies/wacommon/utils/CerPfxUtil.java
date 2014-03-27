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
package com.microsoftopentechnologies.wacommon.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import com.microsoftopentechnologies.wacommon.Activator;
/**
 * Class has utility methods to work with
 * certificate files .cer and .pfx
 */
public class CerPfxUtil {

	/**
	 * Method opens file dialog to accept .cer or .pfx file.
	 * @param shell
	 * @param path
	 * @return
	 */
	public static String importCerPfx(Shell shell, String path) {
		FileDialog dialog = new FileDialog(shell);
		String[] extensions = new String [2];
		extensions[0] = "*.CER";
		extensions[1] = "*.PFX";
		dialog.setFilterExtensions(extensions);
		dialog.setText("Select Certificate");
		// Default directory should be the cert directory in the project, and if it 
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
	 * @param certURL
	 * @return
	 */
	public static X509Certificate getCert(String certURL, String password) {
		InputStream inputStream = null;
		String url = certURL;
		X509Certificate cert = null;
		try {
			url = getCertificatePath(url);
			if(url == null || url.isEmpty())
				return null;
			File file = new File(url);
			if(!file.exists())
				return null;
			inputStream = new FileInputStream(url);
			if (url.endsWith(".cer")) {
				CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
				cert = (X509Certificate)certificateFactory.generateCertificate(inputStream);
			} else if (url.endsWith(".pfx")) {
				KeyStore ks = KeyStore.getInstance("PKCS12", "SunJSSE");
				ks.load(inputStream, password.toCharArray());
				String alias = ks.aliases().nextElement();
				cert = (X509Certificate) ks.getCertificate(alias);
			}
			return cert;
		} catch(Exception e) {
			Activator.getDefault().log(e.getMessage(), e);
			return null;
		} finally {
			try {
				if(inputStream != null)
					inputStream.close();
			}catch(Exception e ){
				//Die silently,no need to throw any error 
			}
		}
	}

	/**
	 * This method resolves environment variables in path. Format of env variables is ${env.VAR_NAME}
	 * e.g. ${env.JAVA_HOME} 
	 * @param rawPath
	 * @return
	 */
	public static String getCertificatePath(String rawPath) {
		String certPath = null;
		String pathToUse = rawPath;
		if (pathToUse != null && pathToUse.length() > 0) {
			pathToUse = pathToUse.replace('\\', '/');
			String[] result = pathToUse.split("/");
			StringBuilder  path = new StringBuilder();

			for (int x = 0; x < result.length; x++) {
				if (result[x].startsWith("${env")) {
					String envValue = System.getenv(result[x].substring("${env.".length(), (result[x].length() - 1)));
					path.append(envValue).append(File.separator);
				} else {
					path.append(result[x]).append(File.separator);
				}
			}
			//Delete last trailing slash
			path = path.deleteCharAt(path.length() - 1);
			certPath = path.toString();
		}
		return certPath;
	}

	public static String getThumbPrint(X509Certificate cert) 
			throws NoSuchAlgorithmException, CertificateEncodingException {
		MessageDigest mdigest = MessageDigest.getInstance("SHA-1");
		byte[] der = cert.getEncoded();
		mdigest.update(der);
		byte[] digest = mdigest.digest();
		return hexify(digest);
	}

	public static String hexify (byte bytes[]) {
		char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', 
				'8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
		StringBuffer buf = new StringBuffer(bytes.length * 2);
		for (int i = 0; i < bytes.length; ++i) {
			buf.append(hexDigits[(bytes[i] & 0xf0) >> 4]);
			buf.append(hexDigits[bytes[i] & 0x0f]);
		}
		return buf.toString();
	}
	
	/**
	 * Validate pfx password entered for pfx file.
	 * @return boolean
	 */
	public static boolean validatePfxPwd(String path, String pwd) {
		boolean retval = true;
		try {
			KeyStore ks = KeyStore.getInstance("PKCS12");
			FileInputStream input = new FileInputStream(
					new File(path));
			ks.load(input, pwd.toCharArray());
		} catch (Exception e) {
			retval = false;
		}
		return retval;
	}
}
