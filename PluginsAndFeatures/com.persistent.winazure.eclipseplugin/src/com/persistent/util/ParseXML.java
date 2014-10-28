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
package com.persistent.util;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.microsoftopentechnologies.xmlhandling.ParseXMLUtilMethods;

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
