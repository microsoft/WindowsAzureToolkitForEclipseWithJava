package com.interopbridges.tools.windowsazure.v16;
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
public class WindowsAzureLocalStorage_v16 {

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
    private WindowsAzureProjectManager_v16 wProj = null;

    /**
     * wRole Variable is to represent corresponding object of
     * WindowsAzureRole.
     */
    private WindowsAzureRole_v16 wRole = null;

    public WindowsAzureLocalStorage_v16(WindowsAzureProjectManager_v16 waProj,
            WindowsAzureRole_v16 waRole) {
        wProj = waProj;
        wRole = waRole;
    }

    private Element getLocalStorageNode()
            throws WindowsAzureInvalidProjectOperationException_v16 {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = wProj.getdefinitionFileDoc();
            String expr = String.format(WindowsAzureConstants_v16.LS_NAME,
                    wRole.getName(), this.getName());
            Element locSt = (Element) xPath.evaluate(expr, doc,
                    XPathConstants.NODE);
            if (locSt == null) {
                throw new WindowsAzureInvalidProjectOperationException_v16(
                        "Exception while geting Local storege node");
            }
            return locSt;
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
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
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    public void setName(String lsName)
            throws WindowsAzureInvalidProjectOperationException_v16 {
        if ((null == lsName) || (lsName.isEmpty())) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants_v16.INVALID_ARG);
        }
        try {
            if (this.name.isEmpty()) {
                this.name = lsName;
            }
            Element lsEle = getLocalStorageNode();
            lsEle.setAttribute(WindowsAzureConstants_v16.ATTR_NAME, lsName);

            //change in corresponding <RoleInstanceValue>
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = wProj.getdefinitionFileDoc();
            String expr = String.format(WindowsAzureConstants_v16
                    .ROLE_INSTANCE_NODE,
                    wRole.getName(), getPathEnv(), getName());
            Element eleRInstance = (Element) xPath.evaluate(expr, doc,
                    XPathConstants.NODE);
            if (eleRInstance != null) {
                eleRInstance.setAttribute("xpath", String.format(
                        WindowsAzureConstants_v16.ROLE_INSTANCE_PATH, lsName));
            }


            WindowsAzureLocalStorage_v16 obj = wRole.locStoMap.get(this.name);
            wRole.locStoMap.remove(this.name);
            wRole.locStoMap.put(lsName, obj);

            this.name = lsName;

        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
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
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    public void setSize(int lsSize)
            throws WindowsAzureInvalidProjectOperationException_v16 {
        if (lsSize < 1) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants_v16.INVALID_ARG);
        }
        try {
            Element lsEle = getLocalStorageNode();
            lsEle.setAttribute(WindowsAzureConstants_v16.ATTR_SIZEINMB,
                    String.valueOf(lsSize));
            this.size = lsSize;
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
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
            throws WindowsAzureInvalidProjectOperationException_v16 {
        try {
            Element lsEle = getLocalStorageNode();
            lsEle.setAttribute(WindowsAzureConstants_v16.ATTR_CLE_ON_ROLE_RECYCLE,
                    String.valueOf(lsCleanOnRecycle));
            this.cleanOnRecycle = lsCleanOnRecycle;
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
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
            throws WindowsAzureInvalidProjectOperationException_v16 {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc =  wProj.getdefinitionFileDoc();
            String expr = String.format(WindowsAzureConstants_v16
                    .VAR_LS_ENV_NAME,
                    wRole.getName(), getName());
            pathEnv = xPath.evaluate(expr, doc);
            //            if(pathEnv.isEmpty()) {
            //                pathEnv = null;
            //            }
            return pathEnv;
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
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
            throws WindowsAzureInvalidProjectOperationException_v16 {

        if (pathEnv == null) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants_v16.INVALID_ARG);
        }
        try {
            if (pathEnv.isEmpty()) {
                //delete path environment variable if exist
                if (!getPathEnv().isEmpty()) {
                    wRole.renameRuntimeEnv(getPathEnv(), "");
                }
            } else {
                //Check the path variable already exist,
                //if path is exist throws exception
                if (wRole.getRuntimeEnv().containsKey(pathEnv)
                        && (!getPathEnv().equals(pathEnv))) {
                    throw new WindowsAzureInvalidProjectOperationException_v16(
                            "Path Already exist");
                }
                if (!getPathEnv().isEmpty()) {
                    wRole.renameRuntimeEnv(getPathEnv(), "");
                }
                    // set path environment variable
                    wRole.setRuntimeEnv(pathEnv, "");
                    XPath xPath = XPathFactory.newInstance().newXPath();
                    Document doc = wProj.getdefinitionFileDoc();
                    String expr = String.format(WindowsAzureConstants_v16
                            .ROLE_INSTANCE_NODE,
                            wRole.getName(), pathEnv, getName());
                    Element eleRInstance = (Element) xPath.evaluate(expr, doc,
                            XPathConstants.NODE);
                    if (eleRInstance == null) {
                        //Create <RoleInstanceValue> node and
                        //append to path variable node
                        expr = String.format(WindowsAzureConstants_v16
                                .VAR_WITH_SPECIFIC_NAME,
                                wRole.getName(), pathEnv);
                        Element eleVar = (Element) xPath.evaluate(expr, doc,
                                XPathConstants.NODE);
                        eleRInstance = doc.createElement("RoleInstanceValue");

                        eleVar.appendChild(eleRInstance);
                    }
                    eleRInstance.setAttribute("xpath", String.format(
                            WindowsAzureConstants_v16.ROLE_INSTANCE_PATH,
                            getName()));
            }
            } catch (Exception ex) {
                throw new WindowsAzureInvalidProjectOperationException_v16(
                        "Exception in setPathEnv", ex);
            }
            WindowsAzureLocalStorage_v16.pathEnv = pathEnv;
        }

        /**
         * This API Deletes the corresponding <LocalStorage> XML from CSDEF.
         * If the parent <LocalResources> element is empty as a result,
         * then delete that as well.
         * @throws WindowsAzureInvalidProjectOperationException .
         */
        public void delete()
                throws WindowsAzureInvalidProjectOperationException_v16 {
            //delete corresponding environment variable
            if(!getPathEnv().isEmpty()) {
                wRole.renameRuntimeEnv(getPathEnv(), "");
            }
            wRole.locStoMap.remove(getName());
            Element lsEle = getLocalStorageNode();
            lsEle.getParentNode().removeChild(lsEle);
        }


}
