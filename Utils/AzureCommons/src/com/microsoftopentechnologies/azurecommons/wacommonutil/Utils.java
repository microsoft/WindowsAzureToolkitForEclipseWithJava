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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

	public static String replaceLastSubString(String location, String find,
			String replaceWith) {
		if (location == null || location.isEmpty())
			return location;

		int lastIndex = location.lastIndexOf(find);

		if (lastIndex < 0)
			return location;

		String end = location.substring(lastIndex).replaceFirst(find,
				replaceWith);
		return location.substring(0, lastIndex) + end;
	}

	public static String getDefaultCNName() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date now = new Date();
		return "Self Signed Certificate " + dateFormat.format(now);
	}

	/**
	 * This method is used for invoking native commands.
	 * 
	 * @param command
	 *            :- command to invoke.
	 * @param ignoreErrorStream
	 *            : Boolean which controls whether to throw exception or not
	 *            based on error stream.
	 * @return result :- depending on the method invocation.
	 * @throws Exception
	 * @throws IOException
	 */
	public static String cmdInvocation(String[] command,
			boolean ignoreErrorStream) throws Exception, IOException {
		String result = "";
		String error = "";
		InputStream inputStream = null;
		InputStream errorStream = null;
		BufferedReader br = null;
		BufferedReader ebr = null;
		try {
			Process process = new ProcessBuilder(command).start();
			;
			inputStream = process.getInputStream();
			errorStream = process.getErrorStream();
			br = new BufferedReader(new InputStreamReader(inputStream));
			result = br.readLine();
			process.waitFor();
			ebr = new BufferedReader(new InputStreamReader(errorStream));
			error = ebr.readLine();
			if (error != null && (!error.equals(""))) {
				// To do - Log error message

				if (!ignoreErrorStream) {
					throw new Exception(error, null);
				}
			}
		} catch (Exception e) {
			throw new Exception("Exception occurred while invoking command", e);
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
			if (errorStream != null) {
				errorStream.close();
			}
			if (br != null) {
				br.close();
			}
			if (ebr != null) {
				ebr.close();
			}
		}
		return result;
	}

}
