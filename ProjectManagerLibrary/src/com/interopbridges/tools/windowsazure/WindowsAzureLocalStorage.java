package com.interopbridges.tools.windowsazure;
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

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * This class is for representing a local storage resource.
 */
public class WindowsAzureLocalStorage {

    /**
     * name variable is represents name attribute in
     * LocalStorage tag in ServiceDefinition.csdef.
     */
    private String name = "";

    /**
     * size variable is represents sizeInMB attribute
     * in LocalStorage tag in ServiceDefinition.csdef.
     */
    private int size;

    /**
     * cleanOnRecycle variable is represents cleanOnRoleRecycle attribute
     * in LocalStorage tag in ServiceDefinition.csdef.
     */
    private Boolean cleanOnRecycle;

    /**
     * pathEnv represents path variable associate with the role.
     */
    private static String pathEnv;

    /**
     * wProj Variable is to represent corresponding object of
     * WindowsAzureProjectManager.
     */
    private WindowsAzureProjectManager wProj = null;

    /**
     * wRole Variable is to represent corresponding object of
     * WindowsAzureRole.
     */
    private WindowsAzureRole wRole = null;


    /**
     * lsVarList contsins list of all env variable related to local storage
     * WindowsAzureRole.
     */

    protected WindowsAzureLocalStorage(WindowsAzureProjectManager waProj,
            WindowsAzureRole waRole) {
        wProj = waProj;
        wRole = waRole;
    }

    private Element getLocalStorageNode()
            throws WindowsAzureInvalidProjectOperationException {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = wProj.getdefinitionFileDoc();
            String expr = String.format(WindowsAzureConstants.LS_NAME,
                    wRole.getName(), this.getName());
            Element locSt = (Element) xPath.evaluate(expr, doc,
                    XPathConstants.NODE);
            if (locSt == null) {
                throw new WindowsAzureInvalidProjectOperationException(
                        "Exception while geting Local storege node");
            }
            return locSt;
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    "Exception while geting Local storege node", ex);
        }
    }

    /**
     * This API is to returns the value of the name attribute
     * of the corresponding <LocalStorage> XML node.
     * @return name .
     */
    public String getName() {
        return name;
    }

    /**
     * This API changes the name.
     * Throws an exception if the name is null or blank,
     * duplicated, or otherwise invalid .
     * @param lsName .
     * @throws WindowsAzureInvalidProjectOperationException
     */
    public void setName(String lsName)
            throws WindowsAzureInvalidProjectOperationException {
        if ((null == lsName) || (lsName.isEmpty())) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants.INVALID_ARG);
        }
        try {
            if (this.name.isEmpty()) {
                this.name = lsName;
            }
            Element lsEle = getLocalStorageNode();
            lsEle.setAttribute(WindowsAzureConstants.ATTR_NAME, lsName);

            //change in corresponding <RoleInstanceValue>
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = wProj.getdefinitionFileDoc();
            String expr = String.format(WindowsAzureConstants
                    .ROLE_INSTANCE_NODE,
                    wRole.getName(), getPathEnv(), getName());
            Element eleRInstance = (Element) xPath.evaluate(expr, doc,
                    XPathConstants.NODE);
            if (eleRInstance != null) {
                eleRInstance.setAttribute("xpath", String.format(
                        WindowsAzureConstants.ROLE_INSTANCE_PATH, lsName));
            }
            WindowsAzureLocalStorage obj = wRole.locStoMap.get(this.name);
            wRole.locStoMap.remove(this.name);
            wRole.locStoMap.put(lsName, obj);
            this.name = lsName;
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    "Exception in setName", ex);
        }
    }


    /**
     * This API returns the value of the size attribute
     * of the corresponding <LocalStorage> XML node.
     * @return size .
     */
    public int getSize() {
        return size;
    }

    /**
     * This API sets the size. Must be no less than 1.
     * It may be greater than the maximum size allowed for the role.
     * @param lsSize .
     * @throws WindowsAzureInvalidProjectOperationException
     */
    public void setSize(int lsSize)
            throws WindowsAzureInvalidProjectOperationException {
        if (lsSize < 1) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants.INVALID_ARG);
        }
        try {
            Element lsEle = getLocalStorageNode();
            lsEle.setAttribute(WindowsAzureConstants.ATTR_SIZEINMB,
                    String.valueOf(lsSize));
            this.size = lsSize;
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    "Exception in setSize", ex);
        }
    }

    /**
     * This API returns True if the cleanOnRecycle attribute
     * of the corresponding <LocalStorage> XML node is “True”, else False.
     * @return cleanOnRecycle .
     */
    public Boolean getCleanOnRecycle() {
        return cleanOnRecycle;
    }

    /**
     * This API sets the cleanOnRecycle attribute.
     * @param lsCleanOnRecycle .
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    public void setCleanOnRecycle(Boolean lsCleanOnRecycle)
            throws WindowsAzureInvalidProjectOperationException {
        try {
            Element lsEle = getLocalStorageNode();
            lsEle.setAttribute(WindowsAzureConstants.ATTR_CLE_ON_ROLE_RECYCLE,
                    String.valueOf(lsCleanOnRecycle));
            this.cleanOnRecycle = lsCleanOnRecycle;
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    "Exception in setCleanOnRecycle", ex);
        }
    }

    /**
     * this API Returns the name of the associated path variable,
     * or null if none.
     * @return
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    public String getPathEnv()
            throws WindowsAzureInvalidProjectOperationException {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc =  wProj.getdefinitionFileDoc();
            String expr = String.format(WindowsAzureConstants
                    .VAR_LS_ENV_NAME,
                    wRole.getName(), getName());
            pathEnv = xPath.evaluate(expr, doc);
            return pathEnv;
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    "Exception in getPathEnv", ex);
        }
    }

    /**
     * this API sets the name of the associated path variable,
     * or null if none.
     * @param pathEnv
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    public void setPathEnv(String pathEnv)
            throws WindowsAzureInvalidProjectOperationException {

        if (pathEnv == null) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants.INVALID_ARG);
        }
        try {
            if (pathEnv.isEmpty()) {
                //delete path environment variable if exist
                if (!getPathEnv().isEmpty()) {
                    deleteLsEnv(getPathEnv());
                }
            } else {
                //Check the path variable already exist,
                //if path is exist throws exception
                if (wRole.getLsEnv().contains(pathEnv)
                        && (!getPathEnv().equals(pathEnv))) {
                    throw new WindowsAzureInvalidProjectOperationException(
                            "Path Already exist");
                }
                if (!getPathEnv().isEmpty()) {
                    deleteLsEnv(getPathEnv());
                }
                    // set path environment variable
                    wRole.setVarInDefFile(pathEnv);
                    wRole.lsVarList.add(pathEnv);
                    XPath xPath = XPathFactory.newInstance().newXPath();
                    Document doc = wProj.getdefinitionFileDoc();
                    String expr = String.format(WindowsAzureConstants
                            .ROLE_INSTANCE_NODE,
                            wRole.getName(), pathEnv, getName());
                    Element eleRInstance = (Element) xPath.evaluate(expr, doc,
                            XPathConstants.NODE);
                    if (eleRInstance == null) {
                        //Create <RoleInstanceValue> node and
                        //append to path variable node
                        expr = String.format(WindowsAzureConstants
                                .VAR_WITH_SPECIFIC_NAME,
                                wRole.getName(), pathEnv);
                        Element eleVar = (Element) xPath.evaluate(expr, doc,
                                XPathConstants.NODE);
                        eleRInstance = doc.createElement("RoleInstanceValue");
                        eleVar.appendChild(eleRInstance);
                    }
                    eleRInstance.setAttribute("xpath", String.format(
                            WindowsAzureConstants.ROLE_INSTANCE_PATH,
                            getName()));
            }
            } catch (Exception ex) {
                throw new WindowsAzureInvalidProjectOperationException(
                        "Exception in setPathEnv", ex);
            }
            WindowsAzureLocalStorage.pathEnv = pathEnv;
        }

        /**
         * This API Deletes the corresponding <LocalStorage> XML from CSDEF.
         * If the parent <LocalResources> element is empty as a result,
         * then delete that as well.
         * @throws WindowsAzureInvalidProjectOperationException .
         */
        public void delete()
                throws WindowsAzureInvalidProjectOperationException {
            //delete corresponding environment variable
            if(!getPathEnv().isEmpty()) {
                deleteLsEnv(getPathEnv());
            }
            wRole.locStoMap.remove(getName());
            Element lsEle = getLocalStorageNode();
            lsEle.getParentNode().removeChild(lsEle);
        }

        public void deleteLsEnv(String varName)
                throws WindowsAzureInvalidProjectOperationException {
            if ((null == varName) || varName.isEmpty()) {
                throw new IllegalArgumentException(
                        WindowsAzureConstants.INVALID_ARG);
            }
            try {
                XPath xPath = XPathFactory.newInstance().newXPath();
                Document doc = wProj.getdefinitionFileDoc();
                String expr = String.format(WindowsAzureConstants
                        .VAR_WITH_SPECIFIC_NAME,
                        wRole.getName(), varName);
                Element var = (Element) xPath.evaluate(expr, doc,
                        XPathConstants.NODE);
                if (var == null) {
                    throw new WindowsAzureInvalidProjectOperationException(
                            varName + " variable does not exist");
                }
               wRole.lsVarList.remove(varName);
                var.getParentNode().removeChild(var);
            } catch (Exception ex) {
                throw new WindowsAzureInvalidProjectOperationException(
                        WindowsAzureConstants.EXCP, ex);
            }
        }
}