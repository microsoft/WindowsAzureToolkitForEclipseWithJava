/**
 * Copyright 2012 Microsoft Open Technologies Inc.
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

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.runtime.Platform;
import org.w3c.dom.Document;

public class PreferenceSetUtil {

	public static final String PREF_SET_DEFNAME = "/preferencesets/@default";
	public static final String PREF_SET_NAME = "/preferencesets/preferenceset[@name='%s']/@name";
	public static final String PREF_SET_PUBLISHSET = "/preferencesets/preferenceset[@name='%s']/@publishsettings";
	public static final String PREF_SET_BLOB = "/preferencesets/preferenceset[@name='%s']/@blob";
	public static final String PREF_SET_MGT = "/preferencesets/preferenceset[@name='%s']/@management";
	
	private static String prefFilePath = String.format("%s%s%s%s%s%s%s", new File(Platform.getInstallLocation().getURL().getFile()).getPath().toString() ,
            File.separator, Messages.pluginFolder , File.separator, Messages.waCommonFolderID, File.separator,"preferencesets.xml");

	public static String getSelectedPreferenceSetName() throws WACommonException {
		try {
			Document doc = parseXMLFile(prefFilePath);
			String expr = PREF_SET_DEFNAME;
			String defname = getExpressionValue(doc, expr);
			String name = getExpressionValue(doc, expr);
			if(name == null || name.isEmpty()) {
				throw new Exception("default name not exist");
			}
			return defname;
		} catch (Exception e) {
			throw new WACommonException("Error Occured while getting preference set name",e);
		}
	}

	public static String getSelectedPublishSettingsURL() throws WACommonException {
		try {
			Document doc = parseXMLFile(prefFilePath);
			String name = getSelectedPreferenceSetName();
			String expr = String.format(PREF_SET_PUBLISHSET, name);
			String pubSetURL = getExpressionValue(doc, expr);
			if(pubSetURL == null || pubSetURL.isEmpty()) {
				throw new WACommonException("public setting URL not present");
			}
			return pubSetURL;
		} catch(Exception e ) {
			throw new WACommonException("Error Occured while getting publish settings url",e);
		}
	}

	public static String getSelectedBlobServiceURL(String storageName) throws WACommonException {
		try {
			Document doc = parseXMLFile(prefFilePath);
			String name = getSelectedPreferenceSetName();
			String expr = String.format(PREF_SET_BLOB, name);
			String blobURL = getExpressionValue(doc, expr);
			if(blobURL == null || blobURL.isEmpty()) {
				throw new WACommonException("Blob sevrvice URL not present");
			}
			blobURL = blobURL.replace("${storage-service-name}", storageName);
			// For blob it always needs to end with forward slash and customers may forgot about this ,
			// while editing preferences , hence its safe to append if not exists
			if(!blobURL.endsWith("/"))
				return blobURL + "/";
			return blobURL;
		} catch(Exception e) {
			throw new WACommonException("Error Occured while getting blob service url",e);
		}
	}

	public static String getSelectedManagementURL(String subscriptionID) throws WACommonException {
		try {
			Document doc = parseXMLFile(prefFilePath);
			String name = getSelectedPreferenceSetName();
			String expr = String.format(PREF_SET_MGT, name);
			String mgtURL = getExpressionValue(doc, expr);
			if(mgtURL == null || mgtURL.isEmpty()) {
				throw new WACommonException("Management URL not present");
			}
			mgtURL = mgtURL.replace("${subscription-id}", subscriptionID);
			return mgtURL;
		} catch(Exception e) {
			throw new WACommonException("Error Occured while getting management service url",e);
		}
	}



    /** This API evaluates  XPath expression and return the result as a String.*/
    public static String getExpressionValue(Document doc ,String expr) throws XPathExpressionException {
           if (doc == null || expr == null || expr.isEmpty())
            throw new IllegalArgumentException("Invalid argument");

           XPath xPath = XPathFactory.newInstance().newXPath();
           return xPath.evaluate(expr,doc);
    }

    /** Parses XML file and returns XML document.
     * @param fileName .
     * @return XML document or <B>null</B> if error occured
     * @throws Exception
     */
    protected static Document parseXMLFile(final String fileName) throws Exception {
        try {
            DocumentBuilder docBuilder;
            Document doc = null;
            DocumentBuilderFactory docBuilderFactory =
                DocumentBuilderFactory.newInstance();
            docBuilderFactory.setIgnoringElementContentWhitespace(true);
            docBuilder = docBuilderFactory.newDocumentBuilder();
            File xmlFile = new File(fileName);
            doc = docBuilder.parse(xmlFile);
            return doc;
        } catch (Exception e) {
            throw new Exception("Not able to parse preference file");
        }
    }
}
