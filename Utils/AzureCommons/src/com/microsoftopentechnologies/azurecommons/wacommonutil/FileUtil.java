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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileUtil {
	
	private static final int BUFF_SIZE = 1024;
    
    /**
     * Method writes contents of file.
     * @param inStream
     * @param outStream
     * @throws IOException
     */
    public static void writeFile(InputStream inStream, OutputStream outStream)
    		throws IOException {

        try {
            byte[] buf = new byte[BUFF_SIZE];
            int len = inStream.read(buf);
            while (len > 0) {
                outStream.write(buf, 0, len);
                len = inStream.read(buf);
            }
        } finally {
            if (inStream != null) {
                inStream.close();
            }
            if (outStream != null) {
                outStream.close();
            }
        }
    }
    
	/**
	 * Copies jar file from zip 
	 * @throws IOException 
	 */
	public static boolean copyFileFromZip(File zipResource, String fileName, File destFile) 
	throws IOException {
		
		boolean success = false;
		
		ZipFile zipFile = new ZipFile(zipResource);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		
		File destParentFile = destFile.getParentFile();
		
		// create parent directories if not existing
		if (destFile != null && destParentFile != null	&& !destParentFile.exists()) {
			destParentFile.mkdir();
		}

		while (entries.hasMoreElements()) {
	            ZipEntry zipEntry = (ZipEntry) entries.nextElement();
	            if (zipEntry.getName().equals(fileName)) {
	            	writeFile(zipFile.getInputStream(zipEntry), new BufferedOutputStream(new FileOutputStream(destFile)));
	            	success = true; 
	            	break;
	            }
		}
		zipFile.close();
		
		return success;
	}
	
	/**
	 * Utility method to check for null conditions or empty strings.
	 * @param name 
	 * @return true if null or empty string
	 */
	public static boolean isNullOrEmpty(final String name) {
		boolean isValid = false;
		if (name == null || name.matches("\\s*")) {
			isValid = true;
		}
		return isValid;
	}

}
