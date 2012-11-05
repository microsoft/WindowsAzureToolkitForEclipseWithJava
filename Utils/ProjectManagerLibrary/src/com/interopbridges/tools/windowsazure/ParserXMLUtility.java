/**
 * Copyright 2011 Persistent Systems Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.interopbridges.tools.windowsazure;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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


final class ParserXMLUtility {

    private static final int BUFF_SIZE = 1024;

    private ParserXMLUtility() {

    }
    /** Parses XML file and returns XML document.
     * @param fileName .
     * @return XML document or <B>null</B> if error occured
     * @throws WindowsAzureInvalidProjectOperationException
     */
    protected static Document parseXMLFile(final String fileName)
    throws WindowsAzureInvalidProjectOperationException {
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
            throw new WindowsAzureInvalidProjectOperationException(
                    WindowsAzureConstants.EXCP_RETRIEVE_DATA + fileName, e);
        }

    }

    /** save XML file and saves XML document.
     * @param fileName
     * @param doc
     * @return XML document or <B>null</B> if error occured
     * @throws IOException
     * @throws WindowsAzureInvalidProjectOperationException
     */

    protected static boolean saveXMLFile(String fileName, Document doc)
    throws IOException, WindowsAzureInvalidProjectOperationException {
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
            throw new WindowsAzureInvalidProjectOperationException(
                    WindowsAzureConstants.EXCP_SAVE + fileName, excp);
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
        return true;
    }


    public static void copyDir(File source, final File destination)
    throws IOException, WindowsAzureInvalidProjectOperationException {

        InputStream instream = null;
        if (source.isDirectory()) {
            if (!destination.exists()) {
                destination.mkdirs();
            }
            String[] kid = source.list();
            for (int i = 0; i < kid.length; i++) {
                copyDir(new File(source, kid[i]),
                        new File(destination, kid[i]));
            }
        } else {
            //InputStream instream = null;
            OutputStream out = null;
            try {
                instream = new FileInputStream(source);
                out = new FileOutputStream(destination);
                byte[] buf = new byte[BUFF_SIZE];
                int len = instream.read(buf);

                while (len > 0) {
                    out.write(buf, 0, len);
                    len = instream.read(buf);
                }
            } catch (Exception ex) {
                throw new WindowsAzureInvalidProjectOperationException(
                        "Exception while copying dir", ex);

            } finally {
                if (instream != null) {
                    instream.close();
                }
                if (out != null) {
                    out.close();
                }
            }
        }
    }

    public static void writeFile(InputStream inStream, OutputStream out) throws IOException {
        try {
            byte[] buf = new byte[BUFF_SIZE];
            int len = inStream.read(buf);
            while (len > 0) {
                out.write(buf, 0, len);
                len = inStream.read(buf);
            }
        } finally {
            if (inStream != null) {
                inStream.close();
            }
            if (out != null) {
                out.close();
            }
        }

    }

    /** Generic API to delete elements from DOM */
    public static void deleteElement(Document doc ,String expr) throws XPathExpressionException {
        if (doc == null ) {
            throw new IllegalArgumentException(WindowsAzureConstants.INVALID_ARG);
        } else {
             XPath xPath = XPathFactory.newInstance().newXPath();
             Element element = (Element) xPath.evaluate(expr, doc,XPathConstants.NODE);

             if (element != null ) {
                 Node parentNode = element.getParentNode() ;
                 parentNode.removeChild(element);
             }

        }
    }

    /** This API evaluates  XPath expression and return the result as a String.*/
    public static String getExpressionValue(Document doc ,String expr) throws XPathExpressionException {
           if (doc == null || expr == null || expr.isEmpty())
            throw new IllegalArgumentException(WindowsAzureConstants.INVALID_ARG);

           XPath xPath = XPathFactory.newInstance().newXPath();
           return xPath.evaluate(expr,doc);
    }

    /** This API evaluates  XPath expression and sets the value.*/
    public static void setExpressionValue(Document doc ,String expr, String value) throws XPathExpressionException {
           if (doc == null || expr == null || expr.isEmpty())
            throw new IllegalArgumentException(WindowsAzureConstants.INVALID_ARG);

        XPath xPath = XPathFactory.newInstance().newXPath();
        Node node = (Node) xPath.evaluate(expr,doc,XPathConstants.NODE);
        node.setNodeValue(value);
    }
    
    protected static boolean isEpPortEqualOrInRange(String oldPort, String newPort) {
        boolean isEqual = false;
        try {

            int oldMin, oldMax, newMin, newMax;
            if(oldPort.contains("-")) {
                String[] rang = oldPort.split("-");
                oldMin = Integer.valueOf(rang[0]);
                oldMax = Integer.valueOf(rang[1]);
            } else {
                oldMax = Integer.valueOf(oldPort);
                oldMin = Integer.valueOf(oldPort);
            }

            if(newPort.contains("-")) {
                String[] rang = newPort.split("-");
                newMin = Integer.valueOf(rang[0]);
                newMax = Integer.valueOf(rang[1]);
            } else {
                newMax = Integer.valueOf(newPort);
                newMin = Integer.valueOf(newPort);
            }


            //check for newmin range is in between old range
            if((newMin == oldMin) || newMin == oldMax || (newMin > oldMin && newMin < oldMax)) {
                isEqual = true;
            } else if((newMax == oldMin) || newMax == oldMax || (newMax > oldMin && newMax < oldMax)) {
                    //check for newmax range is in between old range
                isEqual = true;
            } else if( (oldMin > newMin && oldMin < newMax)) {
                // check for oldnim should not be in new range i.e. check for overlapping
                isEqual = true;
            } else if(oldMax > newMin && oldMax < newMax) {
                isEqual = true;
            }


        } catch (Exception e) {
            isEqual = false;
        }
        return isEqual;
    }

    protected static boolean isValidRange(String range) {
        boolean isValid = true;
        try {
            String[] ports = range.split("-");
            int min = Integer.parseInt(ports[0]);
            int max = Integer.parseInt(ports[1]);
            if(min > max) {
                isValid = false;
            }
        } catch (Exception e) {
            isValid = false;
        }
        return isValid;
    }

}
