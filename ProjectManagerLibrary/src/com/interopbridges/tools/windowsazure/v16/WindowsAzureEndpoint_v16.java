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

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * Class representing Windows Azure endpoint.
 */
public class WindowsAzureEndpoint_v16 {
    private String inputEndpointName = "";
    private String intEndptName = "";
    private String inputPort = "";
    private String inputLocalPort = "";
    private String internalPort = "";
    private String internalFixedPort = "";
    private WindowsAzureProjectManager_v16 winProjMgr;
    private WindowsAzureRole_v16 wRole;

    public WindowsAzureEndpoint_v16(
            WindowsAzureProjectManager_v16 winProMgr,
            WindowsAzureRole_v16 winRole) {
        winProjMgr = winProMgr;
        wRole = winRole;

    }

    /**
     * Get instance of ProjectManager Class.
     * @return
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    protected WindowsAzureProjectManager_v16 getWindowsAzureProjMgr()
    throws WindowsAzureInvalidProjectOperationException_v16 {
        if (winProjMgr == null) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.PMGR_NULL);
        }
        return winProjMgr;
    }

    /**
     * Gets the EndpointName name.
     *
     * @return name .
     */
    public String getName() {
        String name = "";
        if (getEndPointType().equals(WindowsAzureEndpointType_v16.Internal)) {
            name = intEndptName;
        } else {
            name = inputEndpointName;
        }
        return name;
    }

    /**
     * Sets the EndpointName name.
     *
     * @return
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    public void setName(String endPointName)
    throws WindowsAzureInvalidProjectOperationException_v16 {
        if ((endPointName == null) || (endPointName.isEmpty())) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants_v16.INVALID_ARG);
        }
        if (getEndPointType().equals(WindowsAzureEndpointType_v16.Input)) {
            setInputEndpointName(endPointName);
        } else {
            setInternalEndpointName(endPointName);
        }
    }

    /**
     * Gets the inputEndpointName name.
     *
     * @return
     */
    protected String getInputEndpointName() {
        return this.inputEndpointName;
    }

    /**
     * Sets the inputEndpointName name.
     *
     * @param name
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    protected void setInputEndpointName(String inputEndpointName)
    throws WindowsAzureInvalidProjectOperationException_v16 {
        if (inputEndpointName == null) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants_v16.INVALID_ARG);
        }
        if (!getInputEndpointName().isEmpty()) {
            // Change in package.xml
            WindowsAzureEndpoint_v16 saEndPt = wRole.getSessionAffinityInputEndpoint();
            if(saEndPt != null && getInputEndpointName().equalsIgnoreCase(saEndPt.getInputEndpointName()))
                wRole.reconfigureSessionAffinity(wRole.getEndpoint(getInputEndpointName()),inputEndpointName);
            
            try {
                XPath xPath = XPathFactory.newInstance().newXPath();
                Document doc = getWindowsAzureProjMgr().getdefinitionFileDoc();
                String expr = String.format(
                        WindowsAzureConstants_v16.INPUT_ENDPOINT,
                        getInputEndpointName());
                Node inputNode = (Node) xPath.evaluate(expr, doc,
                        XPathConstants.NODE);
                inputNode.getAttributes().getNamedItem("name")
                .setNodeValue(inputEndpointName);
            } catch (Exception ex) {
                throw new WindowsAzureInvalidProjectOperationException_v16(
                        WindowsAzureConstants_v16.EXCP_SET_INPUT_ENDPOINT_NAME, ex);
            }
        }
        this.inputEndpointName = inputEndpointName;
    }

    /**
     * Gets the internalEndpointName name.
     *
     * @return internalEndpointName
     */
    protected String getInternalEndpointName() {
        return this.intEndptName;
    }

    /**
     * Sets the internalEndpointName name.
     *
     * @param name
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    protected void setInternalEndpointName(String intEptName)
    throws WindowsAzureInvalidProjectOperationException_v16 {
        if (intEptName == null) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants_v16.EXCP_EMPTY_INTERNAL_NAME);
        }

        if (!getInternalEndpointName().isEmpty()) {
            // Change in package.xml
            WindowsAzureEndpoint_v16 saEndPt = wRole.getSessionAffinityInternalEndpoint();
            if(saEndPt != null && getInternalEndpointName().equalsIgnoreCase(saEndPt.getInternalEndpointName()))
                wRole.reconfigureSessionAffinity(wRole.getEndpoint(getInternalEndpointName()),intEptName);
                
            try {
                XPath xPath = XPathFactory.newInstance().newXPath();
                Document doc = getWindowsAzureProjMgr().getdefinitionFileDoc();
                String expr = String.format(
                        WindowsAzureConstants_v16.INTERNAL_ENDPOINT,
                        getInternalEndpointName());
                Node internalNode = (Node) xPath.evaluate(expr, doc,
                        XPathConstants.NODE);
                internalNode.getAttributes().getNamedItem("name")
                .setNodeValue(intEptName);
            } catch (Exception ex) {
                throw new WindowsAzureInvalidProjectOperationException_v16(
                        WindowsAzureConstants_v16.EXCP_SET_INTERNAL_ENDPOINT_NAME,
                        ex);
            }
        }
        this.intEndptName = intEptName;
    }

    /**
     * Gets the external Port.
     *
     * @return
     */

    public String getPort() {
        String port = "";
        if (getEndPointType().equals(WindowsAzureEndpointType_v16.Internal)) {
            port = internalPort;
        } else {
            port = inputPort;
        }
        return port;
    }

    /**
     * Sets the EndpointName port.
     *
     * @return
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    public void setPort(String endPointPort)
    throws WindowsAzureInvalidProjectOperationException_v16 {
        if ((endPointPort == null) || (endPointPort.isEmpty())) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants_v16.EXCP_EMPTY_PORT);
        }
        if (getEndPointType().equals(WindowsAzureEndpointType_v16.Input)) {
            setInputPort(endPointPort);
        }
    }

    /**
     * Gets the Input Port.
     *
     * @return
     */
    protected String getInputPort() {
        return inputPort;
    }

    protected void setInputPort(String inputPort)
    throws WindowsAzureInvalidProjectOperationException_v16 {
        if (inputPort == null) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants_v16.EXCP_EMPTY_ENDPOINT_NAME);
        }
        if (!getInputEndpointName().isEmpty()) {
            try {
                XPath xPath = XPathFactory.newInstance().newXPath();
                Document doc = getWindowsAzureProjMgr().getdefinitionFileDoc();
                String expr = String.format(
                        WindowsAzureConstants_v16.INPUT_ENDPOINT,
                        getInputEndpointName());
                Node inputNode = (Node) xPath.evaluate(expr, doc,
                        XPathConstants.NODE);
                inputNode.getAttributes().getNamedItem("port")
                .setNodeValue(inputPort);
            } catch (Exception ex) {
                throw new WindowsAzureInvalidProjectOperationException_v16(
                        WindowsAzureConstants_v16.EXCP_SET_INPUT_PORT, ex);
            }
        }
        this.inputPort = inputPort;
    }

    /**
     * Gets the Input Local Port.
     *
     * @return
     */
    protected String getInputLocalPort() {
        return inputLocalPort;
    }

    /**
     * Sets the Input Local Port.
     *
     * @param inputLocalPort
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    protected void setInputLocalPort(String inputLocalPort)
    throws WindowsAzureInvalidProjectOperationException_v16 {
        if (inputLocalPort == null) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants_v16.EXCP_EMPTY_INPUT_LOCAL_PORT);
        }
        if (!getInputEndpointName().isEmpty()) {
            try {
                XPath xPath = XPathFactory.newInstance().newXPath();
                Document doc = getWindowsAzureProjMgr().getdefinitionFileDoc();
                String expr = String.format(
                        WindowsAzureConstants_v16.INPUT_ENDPOINT,
                        getInputEndpointName());
                Node inputNode = (Node) xPath.evaluate(expr, doc,
                        XPathConstants.NODE);
                inputNode.getAttributes().getNamedItem("localPort")
                .setNodeValue(inputLocalPort);
            } catch (Exception ex) {
                throw new WindowsAzureInvalidProjectOperationException_v16(
                        WindowsAzureConstants_v16.EXCP_SET_INPUT_LOCAL_PORT, ex);
            }
        }
        this.inputLocalPort = inputLocalPort;
    }

    /**
     * Gets the Internal Port.
     *
     * @return
     */
    protected String getInternalPort() {
        return internalPort;
    }

    /**
     * Gets the private Port.
     *
     * @return
     */
    public String getPrivatePort() {
        String pvtPort = "";
        if (getEndPointType().equals(WindowsAzureEndpointType_v16.Internal)) {
            pvtPort = internalFixedPort;
        } else {
            pvtPort = inputLocalPort;
        }
        return pvtPort;
    }

    /**
     * Sets the private EndpointName port.
     *
     * @return
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    public void setPrivatePort(String endPointPort)
    throws WindowsAzureInvalidProjectOperationException_v16 {
        if ((endPointPort == null) || (endPointPort.isEmpty())) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants_v16.EXCP_EMPTY_PRIVATE_PORT);
        }
        if (getEndPointType().equals(WindowsAzureEndpointType_v16.Input)) {
            setInputLocalPort(endPointPort);
        } else {
            setInternalFixedPort(endPointPort);
        }
    }

    /**
     * Gets the Internal Fixed Port.
     *
     * @return internalFixedPort
     */
    protected String getInternalFixedPort() {
        return internalFixedPort;
    }

    /**
     * Sets the Internal Fixed Port.
     *
     * @param internalFixedPort
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    protected void setInternalFixedPort(String internalFixedPort)
    throws WindowsAzureInvalidProjectOperationException_v16 {
        if (internalFixedPort == null) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants_v16.EXCP_EMPTY_FIXED_PORT);
        }
        if (!getInternalEndpointName().isEmpty()) {
            try {
                XPath xPath = XPathFactory.newInstance().newXPath();
                Document doc = getWindowsAzureProjMgr().getdefinitionFileDoc();
                String expr = String.format(
                        WindowsAzureConstants_v16.INTERNAL_ENDPOINT,
                        getInternalEndpointName());
                Node internalNode = (Node) xPath.evaluate(expr, doc,
                        XPathConstants.NODE);
                if (internalNode != null) {
                    for (Node child = internalNode.getFirstChild();
                    child != null; child = child.getNextSibling()) {
                        if (child.getNodeName().equalsIgnoreCase("FixedPort")) {
                            child.getAttributes().getNamedItem("port")
                            .setNodeValue(internalFixedPort);
                        }
                    }
                }
            } catch (Exception ex) {
                throw new WindowsAzureInvalidProjectOperationException_v16(
                        WindowsAzureConstants_v16.EXCP_SET_INTERNAL_FIXED_PORT, ex);
            }
            this.internalFixedPort = internalFixedPort;
        }
    }

    /**
     * Gets Endpoint type.
     *
     * @return internalFixedPort
     */
    public WindowsAzureEndpointType_v16 getEndPointType() {
        WindowsAzureEndpointType_v16 type ;
        if (inputEndpointName.isEmpty()) {
            type = WindowsAzureEndpointType_v16.Internal;
        } else {
            type = WindowsAzureEndpointType_v16.Input;
        }
        return type;
    }

    /**
     * Sets Endpoint type.
     *
     * @param type
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    public void setEndPointType(WindowsAzureEndpointType_v16 type)
    throws WindowsAzureInvalidProjectOperationException_v16 {

        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = getWindowsAzureProjMgr().getdefinitionFileDoc();
            if (getEndPointType() != type) {
                if (type == WindowsAzureEndpointType_v16.Input) {
                    // check if endpoint is associated with session affinity, if yes then throw error
                    WindowsAzureEndpoint_v16 saEndPt = wRole.getSessionAffinityInternalEndpoint();
                    if(saEndPt != null && getInternalEndpointName().equalsIgnoreCase(saEndPt.getInternalEndpointName()))
                        throw new WindowsAzureInvalidProjectOperationException_v16(WindowsAzureConstants_v16.EXCP_SA_ENDPOINT_TYPE_CHANGE);
                    
                    // CreateInputNode
                    Element eleInputNode = doc.createElement("InputEndpoint");
                    eleInputNode
                    .setAttribute("name", getInternalEndpointName());
                    // while changing internal to input, private and public port
                    // are similar
                    eleInputNode.setAttribute("port", getInternalFixedPort());
                    eleInputNode.setAttribute("localPort",
                            getInternalFixedPort());
                    eleInputNode.setAttribute("protocol", "tcp");

                    String expr = String.format(
                            WindowsAzureConstants_v16.INTERNAL_ENDPOINT,
                            getInternalEndpointName());
                    Node internalNode = (Node) xPath.evaluate(expr, doc,
                            XPathConstants.NODE);
                    internalNode.getParentNode().appendChild(eleInputNode);
                    // change in cache

                    setInputEndpointName(getInternalEndpointName());
                    setInputLocalPort(getInternalFixedPort());
                    setInputPort(getInternalFixedPort());
                    setInternalEndpointName("");
                    setInternalFixedPort("");
                    // remove internal node
                    internalNode.getParentNode().removeChild(internalNode);
                }
                if (type == WindowsAzureEndpointType_v16.Internal) {
                    
                    // check if endpoint is associated with session affinity , if yes then throw error
                    WindowsAzureEndpoint_v16 saEndPt = wRole.getSessionAffinityInputEndpoint();
                    if(saEndPt != null && getInputEndpointName().equalsIgnoreCase(saEndPt.getInputEndpointName()))
                        throw new WindowsAzureInvalidProjectOperationException_v16(WindowsAzureConstants_v16.EXCP_SA_ENDPOINT_TYPE_CHANGE);
                    // change in cache
                    setInternalEndpointName(getInputEndpointName());
                    setInternalFixedPort(getInputLocalPort());
                    // create internal endpoint node
                    Element eleInternalNode = doc
                    .createElement("InternalEndpoint");
                    eleInternalNode.setAttribute("name",
                            getInternalEndpointName());
                    eleInternalNode.setAttribute("protocol", "tcp");
                    Element eleFixedPort = doc.createElement("FixedPort");
                    eleFixedPort.setAttribute("port", getInternalFixedPort());
                    eleInternalNode.appendChild(eleFixedPort);
                    String expr = String.format(
                            WindowsAzureConstants_v16.INPUT_ENDPOINT,
                            getInternalEndpointName());
                    Node inputNode = (Node) xPath.evaluate(expr, doc,
                            XPathConstants.NODE);
                    inputNode.getParentNode().appendChild(eleInternalNode);
                    setInputEndpointName("");
                    setInputLocalPort("");
                    // setInputPort("");
                    inputNode.getParentNode().removeChild(inputNode);
                }
            }
        } catch (XPathExpressionException ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_SET_TYPE, ex);
        }
    }

    /**
     * Deletes the current endpoint from WindowsAzureRole.
     *
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    public void delete() throws WindowsAzureInvalidProjectOperationException_v16 {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = getWindowsAzureProjMgr().getdefinitionFileDoc();
            String expr;
            if (getEndPointType().equals(WindowsAzureEndpointType_v16.Internal)) {
                String internalName = getInternalEndpointName();
                expr = String.format(WindowsAzureConstants_v16.INTERNAL_ENDPOINT,
                        internalName);
            } else {
                String inputName = getInputEndpointName();
                expr = String.format(WindowsAzureConstants_v16.INPUT_ENDPOINT,
                        inputName);

            }
            Node endPoint = (Node) xPath.evaluate(expr, doc,
                    XPathConstants.NODE);
            if (endPoint != null) {
                endPoint.getParentNode().removeChild(endPoint);
                wRole.getEndpoints().remove(this);
            }
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_DEL_ENDPOINT, ex);
        }
    }  
    
}
