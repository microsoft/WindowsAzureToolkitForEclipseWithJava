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
package com.microsoftopentechnologies.windowsazure.tools.build;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;



/**
 * 
 * A class that deals with XML parsing.
 * 
 */
public class XMLUtil {

	/**
	 * Parses contents of given XML file and returns DOM Object
	 * @param fileName Path of XML file 
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static Document parseXMLFile(final File xmlFile) 
			throws ParserConfigurationException, SAXException, IOException {

		if (xmlFile == null)
			return null;

		Document doc = null;
		DocumentBuilder docBuilder;
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilderFactory.setIgnoringElementContentWhitespace(true);
		docBuilder = docBuilderFactory.newDocumentBuilder();

		doc = docBuilder.parse(xmlFile);
		doc.getDocumentElement().normalize();
		return doc;        
	}

	/**
	 * Evaluates given XPath expression and returns node list. 
	 * @param expr XPath expression
	 * @param fileName path of the XML file to be parsed
	 * @return 
	 * @throws XPathExpressionException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public  static NodeList getNodeList(final String expr, final File xmlFile) 
			throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {

		XPath    xPath 	   = XPathFactory.newInstance().newXPath();
		NodeList nodeList  = (NodeList) xPath.evaluate(expr, parseXMLFile(xmlFile), XPathConstants.NODESET);

		return nodeList;
	}
	
	/**
	 * Parses publish settings file and returns first subscription as default subscription.
	 * @return
	 */
	public static String getDefaultSubscription(File publishSettingsFile) throws IOException {
		Document publishSettingsDoc = null;
		
		try {
			publishSettingsDoc = parseXMLFile(publishSettingsFile);
		} catch (Exception e) {
			throw new IOException("Exception occurred while parsing publish settings file " + 
						publishSettingsFile.getAbsolutePath() + ", ensure that it is valid");
		}
		
		NodeList publishProfileNodeList = publishSettingsDoc.getElementsByTagName("PublishProfile");
		if (publishProfileNodeList != null) {
			Element publishProfileElement = (Element) publishProfileNodeList.item(0);
			
			if (publishProfileElement != null) {
				// should work for both 1.0 and 2.0 publish settings file.
				Element subscriptionElement = (Element) publishProfileElement.getElementsByTagName("Subscription").item(0);
				
				if (subscriptionElement != null) {
					return subscriptionElement.getAttribute("Id");
				}
			}
		}
		
       	throw new IllegalArgumentException("Publish settings file " +publishSettingsFile.getAbsolutePath() + " is not valid");
	}

	public static String getManagementUrl(File publishSettingsFile,
			String subscriptionId) throws IOException, XPathExpressionException {
		Document publishSettingsDoc = null;
		String url = "";
		try {
			publishSettingsDoc = parseXMLFile(publishSettingsFile);
		} catch (Exception e) {
			throw new IOException("Exception occurred while parsing publish settings file " +
					publishSettingsFile.getAbsolutePath() + ", ensure that it is valid");
		}

		NodeList publishProfileNodeList = publishSettingsDoc.getElementsByTagName("PublishProfile");
		if (publishProfileNodeList != null) {
			Element publishProfileElement = (Element) publishProfileNodeList.item(0);
			String schemaVer = publishProfileElement.getAttribute("SchemaVersion");
			if (schemaVer != null && !schemaVer.isEmpty() && schemaVer.equalsIgnoreCase("2.0")) {
				XPath xPath = XPathFactory.newInstance().newXPath();
				String expr = String.format(
						"/PublishData/PublishProfile/Subscription[@Id='%s']/@ServiceManagementUrl",
						subscriptionId);
				url = xPath.evaluate(expr, publishSettingsDoc);
			} else {
				url = publishProfileElement.getAttribute("Url");
			}
		}
		return url;
	}

	/**
	 * Gets package type.
	 * @param doc
	 * @param expr
	 * @return
	 * @throws XPathExpressionException
	 */
	public static PackageType getPackageType(Document doc, String expr)
			throws XPathExpressionException  {
		XPath xPath = XPathFactory.newInstance().newXPath();
		String packageType = xPath.evaluate(expr, doc);
		return PackageType.valueOf(packageType);
	}

	/**
	 * Check sample certificate is used inside project
	 * using sample certificate thumb print
	 * 875F1656A34D93B266E71BF19C116C39F16B6987
	 * @param doc
	 * @param roleName
	 * @return
	 * @throws XPathExpressionException 
	 */
	public static boolean isSampleCertUsedInRole(Document doc, List<String> roleList)
			throws XPathExpressionException {
		boolean isUsed = false;
		XPath xPath = XPathFactory.newInstance().newXPath();
		String exprCert = "/ServiceConfiguration/Role[@name='%s']/Certificates/Certificate";
		for (String roleName : roleList) {
			String expr = String.format(exprCert, roleName);
			NodeList certList = (NodeList) xPath.evaluate(expr, doc, XPathConstants.NODESET);
			for (int i = 0; i < certList.getLength(); i++) {
				Element ele = (Element) certList.item(i);
				String thumb = ele.getAttribute("thumbprint");
				if (thumb.equalsIgnoreCase("875F1656A34D93B266E71BF19C116C39F16B6987")) {
					isUsed = true;
					return isUsed;
				}
			}
		}
		return isUsed;
	}
	
	public static String getThrdPartyServerCloudValue(Document doc, String roleName)
			throws XPathExpressionException {
		XPath xPath = XPathFactory.newInstance().newXPath();
		String expr = "/project/target[@name='createwapackage']/parallel/windowsazurepackage/workerrole[@name='%s']/startupenv[@type='%s']";
		String exprWithVal = String.format(expr, roleName, "server.home");
		Element element = (Element) xPath.evaluate(exprWithVal, doc, XPathConstants.NODE);
		return element.getAttribute("cloudvalue");
	}

	public static List<String> getRoleList(final String expr, final File xmlFile)
			throws Exception {
		List<String> roleList = new ArrayList<String>();
		NodeList nodeList = getNodeList(expr, xmlFile);
		for (int i = 0; i < nodeList.getLength(); i++) {
			Element role = (Element) nodeList.item(i);
			roleList.add(role.getAttribute("name"));
		}
		return roleList;
	}

	public static String getFirstApplicationName(Document doc) throws XPathExpressionException {
		XPath xPath = XPathFactory.newInstance().newXPath();
		String roleExpr = "/project/target/parallel/windowsazurepackage/workerrole";
		NodeList roleList = (NodeList) xPath.evaluate(roleExpr, doc, XPathConstants.NODESET);
		for (int i = 0; i < roleList.getLength(); i++) {
			Element workerRole = (Element) roleList.item(i);
			String cmpntExpr = String.format(
					"/project/target/parallel/windowsazurepackage/workerrole[@name='%s']/component",
					workerRole.getAttribute("name"));
			NodeList componentList = (NodeList) xPath.evaluate(cmpntExpr, doc, XPathConstants.NODESET);
			if (componentList != null) {
				boolean isJdkPresent = false;
				boolean isServerPresent = false;
				for (int j = 0; j < componentList.getLength(); j++) {
					Element compEle = (Element) componentList.item(j);
					String type = compEle.getAttribute("type");
					if (type.equalsIgnoreCase("jdk.deploy")) {
						isJdkPresent = true;
					} else if (type.equalsIgnoreCase("server.deploy")) {
						isServerPresent = true;
					}
				}
				if (isJdkPresent && isServerPresent) {
					for (int j = 0; j < componentList.getLength(); j++) {
						Element compEle = (Element) componentList.item(j);
						if (compEle.getAttribute("type").equalsIgnoreCase("server.app")) {
							String deployName = compEle.getAttribute("importas");
							return deployName.substring(0, deployName.lastIndexOf("."));
						}
					}
				}
			}
		}
		return "";
	}

	/**
	 * Generic API to update DOM elements
	 * @param doc
	 * @param expr
	 * @param attributes
	 * @return
	 * @throws Exception
	 */
	public static Element updateElementAttributeValue(
			Document doc, String expr, Map<String, String> attributes) throws Exception {
		if (doc == null) {
			throw new IllegalArgumentException("Illegal document.");
		} else {
			try {
				XPath xPath = XPathFactory.newInstance().newXPath();
				Element element = null;
				if (expr != null)
					element = (Element) xPath.evaluate(expr, doc, XPathConstants.NODE);

				if (attributes != null && !attributes.isEmpty()) {
					for (Map.Entry<String, String> attribute : attributes.entrySet()) {
						element.setAttribute(attribute.getKey(), attribute.getValue());
					}
				}
				return element;
			} catch (Exception e) {
				throw new Exception("Exception occurred while updating attributes.", e);
			}
		}
	}
	
	/**
	 * API to save XML file.
	 * @param fileName
	 * @param doc
	 * @return
	 * @throws Exception
	 */
	public static boolean saveXMLDocument(String fileName, Document doc)
			throws Exception {
		File xmlFile = null;
		FileOutputStream fos = null;
		Transformer transformer;
		try {
			xmlFile = new File(fileName);
			fos = new FileOutputStream(xmlFile);
			TransformerFactory transFactory = TransformerFactory.newInstance();
			transformer = transFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult destination = new StreamResult(fos);
			// transform source into result will do save
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.transform(source, destination);
		} catch (Exception excp) {
			throw new Exception("Exception occurred while saving file.", excp);
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
		return true;
	}
}
