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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.microsoftopentechnologies.azurecommons.messagehandler.PropUtil;

/**
 * Class parses the preferencesets.xml file
 * and returns required attribute values.
 */
public class PreferenceSetUtil {

	public static final String PREF_SET_DEFNAME = "/preferencesets/@default";
	public static final String PREF_SET_DEFAULT = "/preferencesets";
	public static final String PREF_SET = "/preferencesets/preferenceset";
	public static final String PREF_SET_NAME = "/preferencesets/preferenceset[@name='%s']/@name";
	public static final String PREF_SET_PUBLISHSET = "/preferencesets/preferenceset[@name='%s']/@publishsettings";
	public static final String PREF_SET_PORTURL = "/preferencesets/preferenceset[@name='%s']/@portalURL";
	public static final String PREF_SET_PORTURL_SMALL = "/preferencesets/preferenceset[@name='%s']/@portalurl";
	public static final String PREF_SET_BLOB = "/preferencesets/preferenceset[@name='%s']/@blob";
	public static final String PREF_SET_MGT = "/preferencesets/preferenceset[@name='%s']/@management";
	
	private static String nameGetErMsg = PropUtil.getValueFromFile("nameGetErMsg");
	private static String nameSetErMsg = PropUtil.getValueFromFile("nameSetErMsg");
	private static String nameNtErMsg = PropUtil.getValueFromFile("nameNtErMsg");
	private static String pubUrlNtErMsg = PropUtil.getValueFromFile("pubUrlNtErMsg");
	private static String pubUrlGetErMsg = PropUtil.getValueFromFile("pubUrlGetErMsg");
	private static String portUrlGetErMsg = PropUtil.getValueFromFile("portUrlGetErMsg");
	private static String blbUrlNtErMsg = PropUtil.getValueFromFile("blbUrlNtErMsg");
	private static String blbUrlGetErMsg = PropUtil.getValueFromFile("blbUrlGetErMsg");
	private static String mngUrlNtErMsg = PropUtil.getValueFromFile("mngUrlNtErMsg");
	private static String mngUrlGetErMsg = PropUtil.getValueFromFile("mngUrlGetErMsg");
	private static String parseErMsg = PropUtil.getValueFromFile("parseErMsg");
	private static String saveErMsg = PropUtil.getValueFromFile("saveErMsg");
	private static String inValArg = PropUtil.getValueFromFile("inValArg");

	/**
	 * Method returns preference set name array.
	 * @return
	 * @throws WACommonException
	 */
	public static String[] getPrefSetNameArr(String prefFilePath)
			throws Exception {
		ArrayList<String> nameList = new ArrayList<String>();
		try {
			XPath xPath = XPathFactory.newInstance().newXPath();
			Document doc = parseXMLFile(prefFilePath);
			String expr = String.format(PREF_SET);
			NodeList varList = (NodeList) xPath.evaluate(expr, doc,
					XPathConstants.NODESET);
			for (int i = 0; i < varList.getLength(); i++) {
				Element var =  (Element) varList.item(i);
				nameList.add(var.getAttribute("name"));
			}
		} catch (Exception ex) {
			throw new Exception(nameGetErMsg, ex);
		}
		return nameList.toArray(new String[nameList.size()]);
	}

	/**
	 * Method returns preferenceset's name.
	 * @return
	 * @throws WACommonException
	 */
	public static String getSelectedPreferenceSetName(String prefFilePath)
			throws Exception {
		try {
			Document doc = parseXMLFile(prefFilePath);
			String expr = PREF_SET_DEFNAME;
			String defname = getExpressionValue(doc, expr);
			if (defname == null || defname.isEmpty()) {
				throw new Exception(nameNtErMsg);
			}
			return defname;
		} catch (Exception e) {
			throw new Exception(nameGetErMsg, e);
		}
	}

	/**
	 * Method returns preferenceset's publish settings URL.
	 * @param name
	 * @return
	 * @throws WACommonException
	 */
	public static String getSelectedPublishSettingsURL(String name, String prefFilePath)
			throws Exception {
		try {
			Document doc = parseXMLFile(prefFilePath);
			String expr = String.format(PREF_SET_PUBLISHSET, name);
			String pubSetURL = getExpressionValue(doc, expr);
			if (pubSetURL == null || pubSetURL.isEmpty()) {
				throw new Exception(pubUrlNtErMsg);
			}
			return pubSetURL;
		} catch (Exception e) {
			throw new Exception(pubUrlGetErMsg, e);
		}
	}

	/**
	 * Method returns preferenceset's portal URL.
	 * @param name
	 * @return
	 * @throws WACommonException
	 */
	public static String getSelectedPortalURL(String name, String prefFilePath)
			throws Exception {
		try {
			Document doc = parseXMLFile(prefFilePath);
			String expr = String.format(PREF_SET_PORTURL, name);
			String portalURL = getExpressionValue(doc, expr);
			/*
			 * If "portalURL" attribute is not present then
			 * check for "portalurl" attribute
			 */
			if (portalURL.isEmpty() || portalURL == null) {
				String exprSmall = String.format(
						PREF_SET_PORTURL_SMALL, name);
				portalURL = getExpressionValue(doc, exprSmall);
			}
			return portalURL;
		} catch (Exception e) {
			throw new Exception(portUrlGetErMsg, e);
		}
	}

	/**
	 * Method returns preferenceset's blob service URL.
	 * according to storage account name passed.
	 * @param storageName
	 * @return
	 * @throws WACommonException
	 */
	public static String getSelectedBlobServiceURL(String storageName, String prefFilePath)
			throws Exception {
		String blobURL = getBlobServiceURL(
				getSelectedPreferenceSetName(prefFilePath), prefFilePath);
		blobURL = blobURL.replace(
				"${storage-service-name}", storageName);
		// For blob it always needs to end with forward slash
		// and customers may forgot about this,
		// while editing preferences,
		// hence its safe to append if not exists
		if (!blobURL.endsWith("/")) {
			return blobURL + "/";
		}
		return blobURL;
	}

	/**
	 * Method returns preferenceset's blob service URL.
	 * @param name
	 * @return
	 * @throws WACommonException
	 */
	public static String getBlobServiceURL(String name, String prefFilePath)
			throws Exception {
		try {
			Document doc = parseXMLFile(prefFilePath);
			String expr = String.format(PREF_SET_BLOB, name);
			String blobURL = getExpressionValue(doc, expr);
			if (blobURL == null || blobURL.isEmpty()) {
				throw new Exception(blbUrlNtErMsg);
			}
			return blobURL;
		} catch (Exception e) {
			throw new Exception(blbUrlGetErMsg, e);
		}
	}

	/**
	 * Method returns preferenceset's management URL.
	 * according to subscription ID passed.
	 * @param subscriptionID
	 * @return
	 * @throws WACommonException
	 */
	public static String getSelectedManagementURL(String subscriptionID, String url, String prefFilePath)
			throws Exception {
		String mgtURL;
		/*
		 * URL will be null if user is going to add
		 * subscription using Add.. button on subscription
		 * property page as we don't know management URL.
		 */
		if (url == null) {
			mgtURL = getManagementURL(getSelectedPreferenceSetName(prefFilePath), prefFilePath);
			mgtURL = mgtURL.replace("${subscription-id}", subscriptionID);
		} else {
			mgtURL = String.format("%s%s%s", url, "/", subscriptionID);
		}
		return mgtURL;
	}

	/**
	 * Method returns preferenceset's management URL.
	 * @param name
	 * @return
	 * @throws WACommonException
	 */
	public static String getManagementURL(String name, String prefFilePath)
			throws Exception {
		try {
			Document doc = parseXMLFile(prefFilePath);
			String expr = String.format(PREF_SET_MGT, name);
			String mgtURL = getExpressionValue(doc, expr);
			if (mgtURL == null || mgtURL.isEmpty()) {
				throw new Exception(mngUrlNtErMsg);
			}
			return mgtURL;
		} catch (Exception e) {
			throw new Exception(mngUrlGetErMsg, e);
		}
	}

	/**
	 * This API evaluates  XPath expression
	 * and return the result as a String.
	 * @param doc
	 * @param expr
	 * @return
	 * @throws XPathExpressionException
	 */
	public static String getExpressionValue(Document doc, String expr)
			throws XPathExpressionException {
		if (doc == null || expr == null || expr.isEmpty()) {
			throw new IllegalArgumentException(inValArg);
		}

		XPath xPath = XPathFactory.newInstance().newXPath();
		return xPath.evaluate(expr, doc);
	}

	/** Parses XML file and returns XML document.
	 * @param fileName .
	 * @return XML document or <B>null</B> if error occurred
	 * @throws Exception
	 */
	protected static Document parseXMLFile(final String fileName)
			throws Exception {
		try {
			DocumentBuilder docBuilder;
			Document doc = null;
			DocumentBuilderFactory docBuilderFactory =
					DocumentBuilderFactory.newInstance();
			docBuilderFactory.
			setIgnoringElementContentWhitespace(true);
			docBuilder = docBuilderFactory.newDocumentBuilder();
			File xmlFile = new File(fileName);
			doc = docBuilder.parse(xmlFile);
			return doc;
		} catch (Exception e) {
			throw new Exception(parseErMsg);

		}
	}

	/**
	 * Method sets preference sets default value.
	 * @param defaultValToSet
	 * @throws WACommonException
	 */
	public static void setPrefDefault(final String defaultValToSet, String prefFilePath)
			throws Exception {
		if ((defaultValToSet == null) || (defaultValToSet.isEmpty())) {
			throw new IllegalArgumentException(inValArg);
		}
		try {
			Document doc = parseXMLFile(prefFilePath);
			XPath xPath = XPathFactory.newInstance().newXPath();
			String expr = String.format(PREF_SET_DEFAULT);
			Node prefSets = (Node) xPath.evaluate(expr, doc,
					XPathConstants.NODE);
			if (prefSets != null) {
				Element prefSetsEle = (Element) prefSets;
				prefSetsEle.setAttribute("default",
						defaultValToSet);
			}
			saveXMLFile(prefFilePath, doc);
		} catch (Exception ex) {
			throw new Exception(nameSetErMsg, ex);
		}
	}

	/**
	 * Saves XML file.
	 * @param fileName
	 * @param doc
	 * @return
	 * @throws IOException
	 * @throws WACommonException
	 */
	protected static boolean saveXMLFile(String fileName, Document doc)
			throws Exception {
		File xmlFile = null;
		FileOutputStream fos = null;
		Transformer transformer;
		try {
			xmlFile = new File(fileName);
			fos = new FileOutputStream(xmlFile);
			TransformerFactory transFactory =
					TransformerFactory.newInstance();
			transformer = transFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult destination = new StreamResult(fos);
			// transform source into result will do save
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.transform(source, destination);
		} catch (Exception excp) {
			throw new Exception(saveErMsg, excp);
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
		return true;
	}
}
