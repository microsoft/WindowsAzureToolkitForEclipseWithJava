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
package com.microsoftopentechnologies.deploy.util;

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
import com.microsoftopentechnologies.messagehandler.PropUtil;
import com.microsoftopentechnologies.util.Base64;


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
