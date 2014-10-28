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

package com.microsoftopentechnologies.wacommonutil;

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
