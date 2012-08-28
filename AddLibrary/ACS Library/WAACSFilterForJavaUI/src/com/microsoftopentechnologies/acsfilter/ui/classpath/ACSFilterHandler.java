/**
 * Copyright 2012 Persistent Systems Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoftopentechnologies.acsfilter.ui.classpath;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.microsoftopentechnologies.acsfilter.ui.activator.Activator;

/**
 * Handler class for creating and modifying acs filter components
 */
public class ACSFilterHandler {

    Document doc = null;
    String xmlPath = "";

    /**
     * Constructor.
     * @param webXmlPath
     * @throws Exception
     */
    ACSFilterHandler(String webXmlPath) throws Exception {
        try {
            xmlPath = webXmlPath;
            File xmlFile = new File(webXmlPath);
            if (xmlFile.exists()) {
            doc = ACSParserXMLUtility.parseXMLFile(webXmlPath);
            } else {
            	throw new Exception(String.format("%s%s",
            			webXmlPath, Messages.fileErrMsg));
            }
        } catch (Exception e) {
             Activator.getDefault().log(e.getMessage(), e);
            throw new Exception(Messages.acsErrMsg);
        }

    }


    /**
     * This method sets the ACS filter attributes.
     * @param pName
     * @param pValue
     * @throws Exception
     */
    public void setAcsFilterParams(String pName, String pValue) throws Exception {
        if ((pName == null) || pName.isEmpty()
        		|| (pValue == null) || pValue.isEmpty()) {
            throw new IllegalArgumentException();
        }

        try {
            //Check Filter tag is present or not. If not exist create new.
            String exprFilter = Messages.exprConst;
            XPath xpath = XPathFactory.newInstance().newXPath();
            Element eleFilter = (Element) xpath.
            		evaluate(exprFilter, doc, XPathConstants.NODE);
            if (eleFilter == null) {
                Element filter = doc.createElement(Messages.filterTag);
                Element filterName = doc.createElement(Messages.filterEle);
                filterName.setTextContent(Messages.acsfilter);
                filter.appendChild(filterName);

                Element fClass = doc.createElement("filter-class");
                fClass.setTextContent(Messages.acsClassName);
                filter.appendChild(fClass);

                eleFilter = (Element) doc.getDocumentElement().
                		appendChild(filter);
            }

            setFilterMapping();

            //check pName is already exist or not
            String exprAcsParm = String.format(Messages.exprAcsPName, pName);
            Element initParam = (Element) xpath.evaluate(exprAcsParm,
            		doc, XPathConstants.NODE);

            if (initParam == null) {
                initParam = doc.createElement(Messages.initPar);
                Element paramName = doc.createElement(Messages.parName);
                Element paramval = doc.createElement(Messages.parVal);
                paramName.setTextContent(pName);
                paramval.setTextContent(pValue);
                initParam.appendChild(paramName);
                initParam.appendChild(paramval);
                eleFilter.appendChild(initParam);
            } else {
                String strParamVal = "./" + Messages.parVal;
                Element paramVal = (Element) xpath.evaluate(strParamVal,
                		initParam, XPathConstants.NODE);
                paramVal.setTextContent(pValue);
            }
        } catch (Exception ex) {
            Activator.getDefault().log(ex.getMessage(), ex);
            throw new Exception(String.format("%s%s",
            		Messages.acsParamErr, ex.getMessage()));
        }
    }
    
    /**
     * This method removes ACS filter attribute if exists.
     * @throws Exception
     */
    public void removeParamsIfExists(String pName) throws Exception {
        if ((pName == null) || pName.isEmpty()) {
            throw new IllegalArgumentException();
        }
        try {
            //check pName already exists or not
        	XPath xpath = XPathFactory.newInstance().newXPath();
            String exprAcsParm = String.format(Messages.exprAcsPName, pName);
            Element initParam = (Element) xpath.evaluate(exprAcsParm,
            		doc, XPathConstants.NODE);
            
            if (initParam != null)
            	initParam.getParentNode().removeChild(initParam);
        } catch (Exception ex) {
            Activator.getDefault().log(ex.getMessage(), ex);
            throw new Exception(String.format("%s%s",
            		Messages.acsParamErr, ex.getMessage()));
        }
    }

    /**
     * This method adds filter mapping tags in web.xml.
     */
    private void setFilterMapping() {
        try {
             XPath xpath = XPathFactory.newInstance().newXPath();
             Element eleFltMapping = (Element) xpath.
            		 evaluate(Messages.exprFltMapping,
            				 doc, XPathConstants.NODE);
             if (eleFltMapping == null) {
                 Element filterMapping = doc.
                		 createElement(Messages.filterMapTag);
                 Element filterName = doc.createElement(Messages.filterEle);
                 filterName.setTextContent(Messages.acsfilter);
                 filterMapping.appendChild(filterName);

                 Element urlPattern = doc.createElement(Messages.urlPatternTag);
                 urlPattern.setTextContent("/*");
                 filterMapping.appendChild(urlPattern);
                 doc.getDocumentElement().appendChild(filterMapping);
             }
        } catch (Exception ex) {
        	Activator.getDefault().log(ex.getMessage(), ex);
        }

    }

    /**
     * This method is to get all ACS related parameters.
     * @return
     * @throws Exception
     */
    public HashMap<String, String> getAcsFilterParams() throws Exception {
        try {
            HashMap<String, String> fltParams = new HashMap<String, String>();
            String exprAcsParm = Messages.exprAcsParam;
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList params = (NodeList) xpath.evaluate(exprAcsParm,
            		doc, XPathConstants.NODESET);
            String paramName = "./param-name/text()";
            String paramVal = "./param-value/text()";
            if (params != null) {
                for (int i = 0; i < params.getLength(); i++) {
                    Element param = (Element) params.item(i);
                    fltParams.put(xpath.evaluate(paramName, param),
                    		xpath.evaluate(paramVal, param));
                }
            }
            return fltParams;
        } catch (Exception ex) {
            Activator.getDefault().log(ex.getMessage(), ex);
            throw new Exception(String.format("%s%s",
            		Messages.acsGetParamErr, ex.getMessage()));
        }
    }


    /**
     * This method saves the web.xml changes.
     * @throws IOException
     * @throws Exception
     */
    public void save() throws IOException, Exception {
        ACSParserXMLUtility.saveXMLFile(xmlPath, doc);
    }

    /**
     * This method remove all ACS related settings from Web.xml.
     * @throws Exception
     */
    public void remove() throws Exception {
        try {

            String exprFilter = Messages.exprConst;
            XPath xpath = XPathFactory.newInstance().newXPath();
            Element eleFilter = (Element) xpath.evaluate(exprFilter,
            		doc, XPathConstants.NODE);
            if (eleFilter != null) {
                eleFilter.getParentNode().removeChild(eleFilter);
            }

            String exprFltMapping = Messages.exprFltMapping;
            Element eleFilMapping = (Element) xpath.
            		evaluate(exprFltMapping, doc,
            				XPathConstants.NODE);
            if (eleFilMapping != null) {
            	eleFilMapping.getParentNode().removeChild(eleFilMapping);
            }

        } catch (Exception ex) {
            Activator.getDefault().log(ex.getMessage(), ex);
            throw new Exception(String.format("%s%s",
            		Messages.acsRemoveErr, ex.getMessage()));
        }
    }
}
