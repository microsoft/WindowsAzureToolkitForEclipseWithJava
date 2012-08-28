/**
* Copyright 2011 Persistent Systems Ltd.
*
* Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*	http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/
package com.persistent.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;


public class ParseXML {

    /** Parses XML file and returns XML document.
     * @param fileName XML file to parse
     * @return XML document or <B>null</B> if error occurred
     * @throws Exception object
     */
    static Document parseFile(String fileName) throws Exception {
        DocumentBuilder docBuilder;
        Document doc = null;
        DocumentBuilderFactory docBuilderFactory =
            DocumentBuilderFactory.newInstance();
        docBuilderFactory.setIgnoringElementContentWhitespace(true);
        try {
            docBuilder = docBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new Exception(Messages.pXMLParseExcp);
        }
        File sourceFile = new File(fileName);
        try {
            doc = docBuilder.parse(sourceFile);
        } catch (SAXException e) {
            throw new Exception(Messages.pXMLParseExcp);
        } catch (IOException e) {
            throw new Exception(Messages.pXMLParseExcp);
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
    static boolean saveXMLDocument(String fileName, Document doc)
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
        doc = parseFile(filePath);

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
            saveXMLDocument(filePath, doc);
        }
    }
}
