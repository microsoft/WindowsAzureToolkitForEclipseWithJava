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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.microsoftopentechnologies.messagehandler.PropUtil;
import com.microsoftopentechnologies.wacommonutil.FileUtil;

public class WAResetEmulator {
	private static final int BUFF_SIZE = 1024;
	private static String rstEmCmd = PropUtil.getValueFromFile("rstEmCmd");

	public static void resetEmulator(String strKitLoc, String jarFileName, File destFile)
			throws Exception {
		try {
			StringBuilder output = new StringBuilder();
			ZipFile zipFile = new ZipFile(strKitLoc);

			FileUtil.copyFileFromZip(
					new File(strKitLoc),
					jarFileName,
					destFile);

			@SuppressWarnings("rawtypes")
			Enumeration entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				if (entry.getName().toLowerCase().indexOf(rstEmCmd) != -1) {
					InputStream in = zipFile.getInputStream(entry);
					Reader reader = new InputStreamReader(in);
					char[] buf = new char[BUFF_SIZE];
					int length = reader.read(buf, 0, buf.length);
					while (length > 0) {
						output.append(buf, 0, length);
						length = reader.read(buf, 0, buf.length);
					}
					break;
				}
			}
			zipFile.close();
			WindowsAzureProjectManager.resetEmulator(output.toString());
		} catch (Exception e) {
			throw new Exception(e.getMessage(), e);
		}
	}
}
