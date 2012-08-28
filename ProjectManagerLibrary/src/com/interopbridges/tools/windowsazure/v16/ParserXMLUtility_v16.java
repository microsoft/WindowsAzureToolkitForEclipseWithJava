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
package com.interopbridges.tools.windowsazure.v16;
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

import org.w3c.dom.Document;


final class ParserXMLUtility_v16 {

    private static final int BUFF_SIZE = 1024;

    private ParserXMLUtility_v16() {

    }
    /** Parses XML file and returns XML document.
     * @param fileName .
     * @return XML document or <B>null</B> if error occured
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    protected static Document parseXMLFile(final String fileName)
    throws WindowsAzureInvalidProjectOperationException_v16 {
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
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_RETRIEVE_DATA + fileName, e);
        }

    }

    /** save XML file and saves XML document.
     * @param fileName
     * @param doc
     * @return XML document or <B>null</B> if error occured
     * @throws IOException
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */

    protected static boolean saveXMLFile(String fileName, Document doc)
    throws IOException, WindowsAzureInvalidProjectOperationException_v16 {
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
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_SAVE + fileName, excp);
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
        return true;
    }


    public static void copyDir(File source, final File destination)
    throws IOException, WindowsAzureInvalidProjectOperationException_v16 {

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
                throw new WindowsAzureInvalidProjectOperationException_v16(
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
}
