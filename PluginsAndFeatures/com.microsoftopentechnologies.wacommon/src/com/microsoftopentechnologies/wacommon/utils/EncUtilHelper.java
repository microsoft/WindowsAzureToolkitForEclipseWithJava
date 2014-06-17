/**
 * Copyright 2014 Microsoft Open Technologies Inc.
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
import java.io.IOException;

import org.eclipse.core.runtime.Platform;

public class EncUtilHelper {

	private static String encPath = String.format("%s%s%s%s%s", new File(
			Platform.getInstallLocation().getURL().getFile()).getPath()
			.toString(), File.separator, Messages.pluginFolder, File.separator,
			Messages.waCommonFolderID);

	/**
	 * This method will encrypt the password using encutil.
	 * 
	 * @param password
	 *            :- Password to be encrypted.
	 * @param certPath
	 *            :- location of certificate file.
	 * @return encrypted password.
	 * @throws Exception
	 * @throws IOException
	 */
	public static String encryptPassword(String password, String certPath)
			throws Exception, IOException {

		// we dont include cmd.exe /C in the command since we are not invoking
		// .bat or .cmd files. when invoking .exe files the cmd.exe is not
		// needed
		// and causes problems with file paths.

		String[] commandArgs = {
				"\"" + encPath + File.separator + "encutil.exe\"", "-encrypt",
				"-text", password, "-cert", certPath };
		String encpwd = Utils.cmdInvocation(commandArgs, false);
		return encpwd;
	}

	/**
	 * This method generates thumb print for given certificate.
	 * 
	 * @param cerPath
	 *            :- location of certificate file.
	 * @return thumbprint.
	 * @throws Exception
	 * @throws IOException
	 */
	/*
	 * public static String getThumbPrint(String cerPath) throws Exception,
	 * IOException {
	 * 
	 * // we dont include cmd.exe /C in the command since we are not invoking
	 * .bat or .cmd files. when invoking .exe files the cmd.exe is not needed //
	 * and causes problems with file paths.
	 * 
	 * String thumbprint = ""; String[] commandArgs = {encPath + File.separator
	 * + "encutil", "-thumbprint" , "-cert" , cerPath }; thumbprint =
	 * encInvocation(commandArgs); return thumbprint; }
	 */

	/**
	 * This method creates a certificate for given password.
	 * 
	 * @param certPath
	 *            :- location of certificate file.
	 * @param pfxPath
	 *            :- location of pfx file.
	 * @param alias
	 *            :- User alias.
	 * @param password
	 *            :- alias password.
	 * @return certificate.
	 * @throws Exception
	 * @throws IOException
	 */
	/*
	 * public static String createCertificate(String certPath, String pfxPath
	 * ,String alias, String password, String cnName) throws Exception,
	 * IOException {
	 * 
	 * // we dont include cmd.exe /C in the command since we are not invoking
	 * .bat or .cmd files. when invoking .exe files the cmd.exe is not needed //
	 * and causes problems with file paths.
	 * 
	 * String newCertificate = ""; String[] commandArgs = {encPath +
	 * File.separator + "encutil", "-create" , "-cert" , '"' + certPath + '"' ,
	 * "-pfx" , pfxPath , "-alias", alias , "-pwd" , password, "-CN", cnName};
	 * newCertificate = Utils.cmdInvocation(commandArgs); return newCertificate;
	 * }
	 */
}
