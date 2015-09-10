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

package com.microsoftopentechnologies.azurecommons.builder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import com.microsoftopentechnologies.azurecommons.messagehandler.PropUtil;


/**
 * This class Export dependent java project as jar.
 */
public class WAExportJar {
	private static int bufferSize = 10240;
	private static String flNtFdErrMsg = PropUtil.getValueFromFile("flNtFdErrMsg");
	private static String jarErrMsg = PropUtil.getValueFromFile("jarErrMsg");

	/**
	 * This method export dependent java project as jar
	 * with their dependent libraries if any.
	 * @param archiveFile
	 * @param tobeJared
	 * @throws Exception 
	 */
	public static void createJarArchive(
			File archiveFile, File[] tobeJared) throws Exception {
		// Open archive file
		FileOutputStream fOutStream = null;
		try {
			fOutStream = new
					FileOutputStream(archiveFile);
		} catch (Exception e) {
			throw new Exception(flNtFdErrMsg, e);
		}
		JarOutputStream jOutStream = null;
		try {
			jOutStream = new
					JarOutputStream(fOutStream, new Manifest());
			for (int i = 0; i < tobeJared.length; i++) {
				if (tobeJared[i] == null
						|| !tobeJared[i].exists()) {
					continue;
				}
				addEntry(fOutStream, jOutStream, tobeJared[i], "");
			}
			jOutStream.close();
			fOutStream.close();
		} catch (IOException e) {
			throw new IOException(jarErrMsg, e);
		}
	}

	/**
	 * This method adds directories and files
	 * from source location to destination jar.
	 * @param fOutStream
	 * @param jOutStream
	 * @param tobeJared
	 * @param name
	 */
	public static void addEntry(
			FileOutputStream fOutStream, JarOutputStream jOutStream,
			File tobeJared, String name) throws Exception {
		byte[] buffer = new byte[bufferSize];
		// Add archive entry
		try {
			if (tobeJared.isDirectory()) {
				JarEntry jarEntry = new
						JarEntry(String.format(
								"%s%s", tobeJared.getName(), "/"));
				jarEntry.setTime(tobeJared.lastModified());
				jOutStream.putNextEntry(jarEntry);
				File[] dirEntries = tobeJared.listFiles();
				for (int i = 0; i < dirEntries.length; i++) {
					if (dirEntries[i] == null
							|| !dirEntries[i].exists()) {
						continue;
					}

					addEntry(fOutStream, jOutStream, dirEntries[i],
							String.format("%s%s%s", name, tobeJared.getName(), "/"));
				}
			}
			else {
				JarEntry jarEntry = new
						JarEntry(name + tobeJared.getName());
				jarEntry.setTime(tobeJared.lastModified());
				jOutStream.putNextEntry(jarEntry);
				// Write file to archive
				if (!tobeJared.isDirectory()) {
					FileInputStream inStr = null;
					try {
						inStr = new
								FileInputStream(tobeJared);
					} catch (FileNotFoundException e) {
						throw new FileNotFoundException(flNtFdErrMsg);
					}
					while (true) {
						int nRead = inStr.read(buffer, 0, buffer.length);
						if (nRead <= 0) {
							break;
						}
						jOutStream.write(buffer, 0, nRead);
					}
					inStr.close();
				}
			}
		} catch (IOException e) {
			throw new IOException(jarErrMsg, e);
		}
	}
}

