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
