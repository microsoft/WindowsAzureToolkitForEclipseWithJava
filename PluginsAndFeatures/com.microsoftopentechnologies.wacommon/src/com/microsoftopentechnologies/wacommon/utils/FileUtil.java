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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.FileLocator;

import com.microsoftopentechnologies.wacommon.Activator;


public class FileUtil {
	
	private static final int BUFF_SIZE = 1024;
	
    
    /**
     * copy specified file to eclipse plugins folder
     * @param name : Name of file
     * @param entry : Location of file
     */
    public static void copyResourceFile(String resourceFile , String destFile) {
    	URL url = Activator.getDefault().getBundle()
                .getEntry(resourceFile);
        URL fileURL;
		try {
			fileURL = FileLocator.toFileURL(url);
			URL resolve = FileLocator.resolve(fileURL);
	        File file = new File(resolve.getFile());
	        FileInputStream fis = new FileInputStream(file);
	        File outputFile = new File(destFile);
	        FileOutputStream fos = new FileOutputStream(outputFile);
	        writeFile(fis , fos);
		} catch (IOException e) {
			Activator.getDefault().log(e.getMessage(), e);
		}

    }
    
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
	public static boolean copyFileFromZip(File zipResource, String jarFileNAme, File destFile) 
	throws IOException {
		
		boolean jarCopied = false;
		
		ZipFile zipFile = new ZipFile(zipResource);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();

		while (entries.hasMoreElements()) {
	            ZipEntry zipEntry = (ZipEntry) entries.nextElement();
	            if (zipEntry.getName().equals(jarFileNAme)) {
	            	writeFile(zipFile.getInputStream(zipEntry), new BufferedOutputStream(new FileOutputStream(destFile)));
	            	jarCopied = true; 
	            }
		}
		zipFile.close();
		
		return jarCopied;
		
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
