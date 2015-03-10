/**
 * Copyright 2015 Microsoft Open Technologies Inc.
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
package com.microsoftopentechnologies.azurecommons.wacommonutil;

import java.io.File;
import java.io.IOException;

public class EncUtilHelper {


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
	public static String encryptPassword(String password, String certPath, String encPath)
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
}
