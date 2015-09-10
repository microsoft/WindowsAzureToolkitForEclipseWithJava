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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.microsoftopentechnologies.azurecommons.messagehandler.PropUtil;
import com.microsoftopentechnologies.azurecommons.wacommonutil.FileUtil;

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
