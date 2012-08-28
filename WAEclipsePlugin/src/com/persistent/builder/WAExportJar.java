/**
 * Copyright 2012 Persistent Systems Ltd.
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
package com.persistent.builder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import waeclipseplugin.Activator;

/**
 * This class Export dependent java project as jar.
 */
public class WAExportJar {
	public static int bufferSize = 10240;
	private static String errorMessage;


	/**
	 * This method export dependent java project as jar
	 * with their dependent libraries if any.
	 * @param archiveFile
	 * @param tobeJared
	 */
	public static void createJarArchive(
			File archiveFile, File[] tobeJared) {
		// Open archive file
		FileOutputStream fOutStream = null;
		try {
			fOutStream = new
					FileOutputStream(archiveFile);
		} catch (Exception e) {
			errorMessage = Messages.flNtFdErrMsg;
			Activator.getDefault().log(errorMessage, e);
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
			errorMessage = Messages.jarErrMsg;
			Activator.getDefault().log(errorMessage, e);
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
			File tobeJared, String name) {
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
						errorMessage = Messages.flNtFdErrMsg;
						Activator.getDefault().log(errorMessage, e);
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
			errorMessage = Messages.jarErrMsg;
			Activator.getDefault().log(errorMessage, e);
		}
	}
}

