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

package com.microsoftopentechnologies.azurecommons.xmlhandling;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.microsoftopentechnologies.azurecommons.messagehandler.PropUtil;


public class ParseXMLUtilMethods {
	private static String pXMLParseExcp = PropUtil.getValueFromFile("pXMLParseExcp");

	/** Parses XML file and returns XML document.
	 * @param fileName XML file to parse
	 * @return XML document or <B>null</B> if error occurred
	 * @throws Exception object
	 */
	public static Document parseFile(String fileName) throws Exception {
		DocumentBuilder docBuilder;
		Document doc = null;
		DocumentBuilderFactory docBuilderFactory =
				DocumentBuilderFactory.newInstance();
		docBuilderFactory.setIgnoringElementContentWhitespace(true);
		try {
			docBuilder = docBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new Exception(pXMLParseExcp, e);
		}
		File sourceFile = new File(fileName);
		try {
			doc = docBuilder.parse(sourceFile);
		} catch (SAXException e) {
			throw new Exception(pXMLParseExcp, e);
		} catch (IOException e) {
			throw new Exception(pXMLParseExcp, e);
		}
		return doc;
	}

	/**
	 * Save XML file and saves XML document.
	 *
	 * @param fileName
	 * @param doc
	 * @return boolean
	 * @throws Exception object
	 */
	public static boolean saveXMLDocument(String fileName, Document doc)
			throws Exception {
		// open output stream where XML Document will be saved
		File xmlOutputFile = new File(fileName);
		FileOutputStream fos = null;
		Transformer transformer;
		try {
			fos = new FileOutputStream(xmlOutputFile);
			// Use a Transformer for output
			TransformerFactory transformerFactory =
					TransformerFactory.newInstance();
			transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(fos);
			// transform source into result will do save
			transformer.transform(source, result);
		} finally {
			if (fos != null) {
				fos.close();
			}
		}

		return true;
	}

	/** Generic API to update or create DOM elements */
	public static Element updateOrCreateElement(Document doc, String expr,
			String parentNodeExpr, String elementName, boolean firstChild,
			Map<String, String> attributes) throws Exception {
		if (doc == null) {
			throw new IllegalArgumentException();
		} else {
			XPath xPath = XPathFactory.newInstance().newXPath();
			Element element = null;
			if (expr != null)
				element = (Element) xPath.evaluate(expr, doc,
						XPathConstants.NODE);

			// If element doesn't exist create one
			if (element == null) {
				element = doc.createElement(elementName);
				Element parentElement = (Element) xPath.evaluate(
						parentNodeExpr, doc, XPathConstants.NODE);
				if (firstChild) {
					parentElement.insertBefore(
							element,
							parentElement != null ? parentElement
									.getFirstChild() : null);
				} else {
					parentElement.appendChild(element);
				}
			}

			if (attributes != null && !attributes.isEmpty()) {
				for (Map.Entry<String, String> attribute : attributes
						.entrySet()) {
					element.setAttribute(attribute.getKey(),
							attribute.getValue());
				}
			}
			return element;
		}
	}
}
