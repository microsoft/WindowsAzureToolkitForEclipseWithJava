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

package com.microsoftopentechnologies.azurecommons.deploy.util;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;
import com.microsoft.windowsazure.management.configuration.PublishSettingsLoader;
import com.microsoftopentechnologies.azurecommons.messagehandler.PropUtil;
import com.microsoftopentechnologies.azuremanagementutil.util.Base64;


public class UIUtils {
	private static String publishSettExt = PropUtil.getValueFromFile("publishSettExt");

	public static PublishData parse(File file) throws JAXBException {
		PublishData publishData = null;
		try {
			JAXBContext context = JAXBContext.newInstance(PublishData.class);
			publishData = (PublishData) context.createUnmarshaller().unmarshal(
					file);
		} catch (JAXBException e) {
			throw e;
		}
		return publishData;
	}

	public static PublishData parsePfx(File file) throws Exception {
		PublishData publishData = new PublishData();
		PublishProfile profile = new PublishProfile();
		FileInputStream input = null;
		DataInputStream dis = null;
		byte[] buff = new byte[(int) file.length()];
		try {
			input = new FileInputStream(file);
			dis = new DataInputStream(input);
			dis.readFully(buff);
			profile.setManagementCertificate(Base64.encode(buff)); //$NON-NLS-1$
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			if (input != null) {
				input.close();
			}
			if (dis != null) {
				dis.close();
			}
		}
		publishData.setPublishProfile(profile);
		return publishData;
	}

	public static String installPublishSettings(File file, String subscriptionId, String password) throws Exception {
		try {
			if (password == null && file.getName().endsWith(publishSettExt)) {
				Configuration configuration = PublishSettingsLoader.createManagementConfiguration(file.getPath(), subscriptionId);
				String sId = (String) configuration.getProperty(ManagementConfiguration.SUBSCRIPTION_ID);
				return sId;
			}
		} catch (Exception ex) {
			throw ex;
		}
		return null;
	}

}
