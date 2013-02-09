/*
 Copyright 2013 Microsoft Open Technologies, Inc.
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package com.microsoftopentechnologies.windowsazure.tools.build;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
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
}
