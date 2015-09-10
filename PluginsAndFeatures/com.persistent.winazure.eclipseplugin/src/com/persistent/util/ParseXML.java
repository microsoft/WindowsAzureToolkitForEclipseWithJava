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
package com.persistent.util;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.microsoftopentechnologies.azurecommons.xmlhandling.ParseXMLUtilMethods;

public class ParseXML {

	/**
	 * Replaces old project name with new project name in launch file.
	 *
	 * @param filePath
	 * @param oldName
	 * @param newName
	 * @throws Exception
	 */
	public static void setProjectNameinLaunch(String filePath,
			String oldName, String newName) throws Exception {
		Document doc = null;
		doc = ParseXMLUtilMethods.parseFile(filePath);

		if (doc != null) {
			Node root = doc.getDocumentElement();
			if (root.hasChildNodes()) {
				for (Node child = root.getFirstChild(); child != null;
						child = child.getNextSibling()) {
					NamedNodeMap  nMap = child.getAttributes();
					if (nMap != null) {
						if (nMap.getNamedItem("key").getNodeValue()
								.equalsIgnoreCase(Messages.pXMLProjAttr)) {
							nMap.getNamedItem("value").setNodeValue(newName);
						} else if (nMap.getNamedItem("key").getNodeValue()
								.equalsIgnoreCase(Messages.pXMLAttrLoc)) {
							String value = nMap.getNamedItem("value")
									.getNodeValue();
							String workLoc = Messages.pXMLWorkLoc;
							value = value.replaceFirst(workLoc.concat(oldName),
									workLoc.concat(newName));
							nMap.getNamedItem("value").setNodeValue(value);
						} else if (nMap.getNamedItem("key").getNodeValue()
								.equalsIgnoreCase(Messages.pXMLAttrDir)) {
							String value = nMap.getNamedItem("value")
									.getNodeValue();
							String workLoc = Messages.pXMLWorkLoc;
							value = value.replaceFirst(workLoc.concat(oldName),
									workLoc.concat(newName));
							nMap.getNamedItem("value").setNodeValue(value);
						}
					}
				}
			}
			ParseXMLUtilMethods.saveXMLDocument(filePath, doc);
		}
	}
}
