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


public class WindowsAzureRoleComponent {


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
     * importMethod variable represents importMethod attribute
     * in Component tag in package.xml.
     */
    private WindowsAzureRoleComponentImportMethod importMethod = null;

    /**
     * importPath variable represents importPath attribute
     * in Component tag in package.xml.
     */
    private String importPath = "";

    /**
     * deployMethod variable represents deployMethod attribute
     * in Component tag in package.xml.
     */
    private WindowsAzureRoleComponentDeployMethod deployMethod = null;

    /**
     * deployDir variable represents deployDir attribute
     * in Component tag in package.xml.
     */
    private String deployDir = "";

    /**
     * deployName variable represents importas attribute
     * in Component tag in package.xml.
     */
    private String deployName = "";


    /**
     * type variable is to determine if a component is part of a server,
     * JDK or application configuration, based on its @type.
     */
    private String type = "";

    /**
     * Constructor to initialize projectManager and Role instances.
     * @param waProj
     * @param waRole
     */
    public WindowsAzureRoleComponent(WindowsAzureProjectManager waProj,
            WindowsAzureRole waRole) {
        wProj = waProj;
        wRole = waRole;
    }

    /**
     * This method is to find corresponding Component tag in package.xml.
     * @return Element
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    private Element getComponentNode()
            throws WindowsAzureInvalidProjectOperationException {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = wProj.getPackageFileDoc();
            String expr = "";
            Element component = null;

            if (this.deployName.isEmpty()) {
                // In case deployment name is empty, there can be two componetns having same IPATH
                //In case deployname is specified and in other where deployname is empty.
                //This component can be placed in 2 ways...
                //Either importas attribute is not present or value importas attribute is empty

                //Case 1: importas attribute is not present
                expr = String.format(WindowsAzureConstants.COMPONENT_IPATH_NAME,
                        wRole.getName(), this.importPath);
                component = (Element) xPath.evaluate(expr, doc,
                        XPathConstants.NODE);

                if (component == null) {
                  //case 2: Value importas attribute is empty
                    expr = String.format(WindowsAzureConstants.COMPONENT_IPATH_NAME_IAS_EMPTY,
                            wRole.getName(), this.importPath);
                    component = (Element) xPath.evaluate(expr, doc,
                            XPathConstants.NODE);
                }
            } else {
                expr = String.format(WindowsAzureConstants.COMPONENT_IMPORTAS,
                        wRole.getName(), this.deployName);
                component = (Element) xPath.evaluate(expr, doc,
                        XPathConstants.NODE);
            }

            if (component == null) {
                throw new WindowsAzureInvalidProjectOperationException(
                        "Exception while geting component node");
            }
            return component;
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    "Exception while geting component node", ex);
        }
    }

    /**
     * This API is to returns the value of the importMethod attribute
     * @return
     */
    public WindowsAzureRoleComponentImportMethod getImportMethod() {
        return importMethod;
    }

    /**
     * This API sets the importMethod attribute in Component tag
     * in package.xml.
     * Throws an exception if the name is null,
     * @param importMethod
     * @throws WindowsAzureInvalidProjectOperationException
     */
    public void setImportMethod(WindowsAzureRoleComponentImportMethod importMethod)
            throws WindowsAzureInvalidProjectOperationException {
        if (null == importMethod) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants.INVALID_ARG);
        }
        try {
            Element component = getComponentNode();
            component.setAttribute(WindowsAzureConstants.ATTR_IMETHOD, importMethod.toString());
            this.importMethod = importMethod;
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    "Exception in setImportMethod", ex);
        }
    }

    /**
     * This API is to returns the value of the importfrom attribute
     * @return
     */
    public String getImportPath() {
        return importPath;
    }

    /**
     * This API sets the importfrom attribute in Component tag
     * in package.xml.
     * @param importPath should be not null.
     * @throws WindowsAzureInvalidProjectOperationException. If there is any internal problem.
     */
    public void setImportPath(String importPath)
            throws WindowsAzureInvalidProjectOperationException {
        if (null == importPath) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants.INVALID_ARG);
        }
        try {
            if(this.deployName.isEmpty() && (this.importPath.isEmpty())) {
                this.importPath = importPath;
            }
            Element component = getComponentNode();
            component.setAttribute(WindowsAzureConstants.ATTR_IPATH, importPath);
            this.importPath = importPath;
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    "Exception in setImportPath", ex);
        }
    }

    /**
     * This API is to returns the value of the deploymethod attribute
     * @return
     */
    public WindowsAzureRoleComponentDeployMethod getDeployMethod() {
        return deployMethod;
    }

    /**
     * This API sets the deploymethod attribute in Component tag
     * in package.xml.
     * @param deployMethod
     * @throws WindowsAzureInvalidProjectOperationException. If there is any internal problem.
     */
    public void setDeployMethod(WindowsAzureRoleComponentDeployMethod deployMethod)
            throws WindowsAzureInvalidProjectOperationException {
        if (null == deployMethod) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants.INVALID_ARG);
        }
        try {
            Element component = getComponentNode();
            component.setAttribute(WindowsAzureConstants.ATTR_DMETHOD, deployMethod.toString());
            this.deployMethod = deployMethod;
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    "Exception in setDeployMethod", ex);
        }
    }


    /**
     * This API is to returns the value of the deploydir attribute.
     * @return
     */
    public String getDeployDir() {
        return deployDir;
    }


    /**
     * This API sets the deploydir attribute in Component tag
     * in package.xml.
     * @param deployDir
     * @throws WindowsAzureInvalidProjectOperationException. If there is any internal problem.
     */
    public void setDeployDir(String deployDir)
            throws WindowsAzureInvalidProjectOperationException {
        if (null == deployDir) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants.INVALID_ARG);
        }
        try {
            Element component = getComponentNode();
            component.setAttribute(WindowsAzureConstants.ATTR_DDIR, deployDir);
            this.deployDir = deployDir;
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    "Exception in setDeployDir", ex);
        }
    }

    /**
     * This API is to returns the value of the importas attribute
     * in package.xml
     * @return
     */
    public String getDeployName() {
        return deployName;
    }

    /**
     *
     * @param deployName
     * @throws WindowsAzureInvalidProjectOperationException
     */
    public void setDeployname(String deployName)
            throws WindowsAzureInvalidProjectOperationException {
        if (null == deployName) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants.INVALID_ARG);
        }
        try {
            if(this.deployName.isEmpty() && (this.importPath.isEmpty())) {
                this.deployName = deployName;
            }
            Element component = getComponentNode();
            component.setAttribute(WindowsAzureConstants.ATTR_IMPORTAS, deployName);
            this.deployName = deployName;
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    "Exception in setDeployname", ex);
        }
    }

    public String getType() {
        return type;
    }


    /**
    *
    * @param deployName
    * @throws WindowsAzureInvalidProjectOperationException
    */
   public void setType(String type)
           throws WindowsAzureInvalidProjectOperationException {
       if (null == type){
           throw new IllegalArgumentException(
                   WindowsAzureConstants.INVALID_ARG);
       }
       try {
           if(this.type.isEmpty()) {
               this.type = type;
           }
           Element component = getComponentNode();
           component.setAttribute(WindowsAzureConstants.ATTR_TYPE, type);
           this.type = type;
       } catch (Exception ex) {
           throw new WindowsAzureInvalidProjectOperationException(
                   "Exception in setType", ex);
       }
   }


    public Boolean getIsPreconfigured()
            throws WindowsAzureInvalidProjectOperationException {
        boolean isPreconfig = false;
        try {
            Element component = getComponentNode();
            String type = component.getAttribute(WindowsAzureConstants.ATTR_TYPE);
            if (type.toLowerCase().startsWith("server.") ||
                    type.toLowerCase().startsWith("jdk.")) {
                isPreconfig = true;
            }
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    "Exception in getIsPreconfigured", ex);
        }
        return isPreconfig;

    }


    public void delete() throws WindowsAzureInvalidProjectOperationException {
        Element component;
        try {
            wRole.winCompList.remove(this);
            component = getComponentNode();
            component.getParentNode().removeChild(component);
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    "Exception in setDeployDir", ex);
        }
    }
}
