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

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * Class representing Windows Azure endpoint.
 */
public class WindowsAzureEndpoint {
    private WindowsAzureProjectManager winProjMgr;
    private WindowsAzureRole wRole;
    private String name="";
    private String port = "";
    private String localPort = "";
    private String minPort = "";
    private String maxPort = "";

    public WindowsAzureEndpoint(
            WindowsAzureProjectManager winProMgr,
            WindowsAzureRole winRole) {
        winProjMgr = winProMgr;
        wRole = winRole;
    }

    /**
     * Get instance of ProjectManager Class.
     * @return
     * @throws WindowsAzureInvalidProjectOperationException
     */
    protected WindowsAzureProjectManager getWindowsAzureProjMgr()
            throws WindowsAzureInvalidProjectOperationException {
        if (winProjMgr == null) {
            throw new WindowsAzureInvalidProjectOperationException(
                    WindowsAzureConstants.PMGR_NULL);
        }
        return winProjMgr;
    }

    /**
     * Gets the EndpointName name.
     *
     * @return name .
     */
    public String getName() {
        return name;
    }


    /**
     * This method will return the node of the selected endpoint.
     * @return
     * @throws WindowsAzureInvalidProjectOperationException
     */
    private Node getThisEndPointNode() throws WindowsAzureInvalidProjectOperationException {
        try {
            Node epNode = null;
            if (!getName().isEmpty()) {
                XPath xPath = XPathFactory.newInstance().newXPath();
                Document doc = getWindowsAzureProjMgr().getdefinitionFileDoc();
                String expr = "";

                if (getEndPointType().equals(WindowsAzureEndpointType.Input)) {
                    expr = String.format(
                            WindowsAzureConstants.INPUT_ENDPOINT, wRole.getName(),
                            getName());
                } else if(getEndPointType().equals(WindowsAzureEndpointType.Internal)) {
                    expr = String.format(
                            WindowsAzureConstants.INTERNAL_ENDPOINT, wRole.getName(),
                            getName());
                } else  {
                    expr = String.format(
                            WindowsAzureConstants.INSTANCE_ENDPOINT, wRole.getName(),
                            getName());
                }
                epNode = (Node) xPath.evaluate(expr, doc,
                        XPathConstants.NODE);
            }
            return epNode;
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    "In getThisEndPointNode", ex);
        }
    }

    /**
     * Sets the EndpointName name.
     *
     * @return
     * @throws WindowsAzureInvalidProjectOperationException
     */
    public void setName(String endPointName)
            throws WindowsAzureInvalidProjectOperationException {
        if ((endPointName == null) || (endPointName.isEmpty())) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants.INVALID_ARG);
        }

        try {
            if (getName().isEmpty()) {
                this.name = endPointName;
                return;
            }

//            if(isCachingEndPoint()) {
//            	throw new WindowsAzureInvalidProjectOperationException(
//            			"Endpoint is associated with caching");
//            }

            WindowsAzureEndpoint saEndPt = wRole.getSessionAffinityInputEndpoint();
            if(saEndPt != null && getName().equalsIgnoreCase(saEndPt.getName()))
                wRole.reconfigureSessionAffinity(wRole.getEndpoint(getName()),endPointName);
            Node epNode = getThisEndPointNode();
            epNode.getAttributes().getNamedItem("name").
            setNodeValue(endPointName);
            this.name = endPointName;
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    "Exception While setting endpoint name", ex);
        }
    }

    /**
     * Gets the external Port.
     *
     * @return
     */

    public String getPort() {
        return port;
    }

    /**
     * Sets the EndpointName port.
     *
     * @return
     * @throws WindowsAzureInvalidProjectOperationException
     */
    public void setPort(String endPointPort)
            throws WindowsAzureInvalidProjectOperationException {
        if ((endPointPort == null) || (endPointPort.isEmpty())) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants.EXCP_EMPTY_PORT);
        }
        try {
        String epProt = endPointPort;

        if (getEndPointType().equals(WindowsAzureEndpointType.Input)) {
            Node epNode = getThisEndPointNode();
            epNode.getAttributes().getNamedItem("port")
            .setNodeValue(endPointPort);
            this.port = endPointPort;
        } else if (getEndPointType().equals(WindowsAzureEndpointType.InstanceInput)) {
            this.minPort = endPointPort;
            this.maxPort = endPointPort;
            if(endPointPort.contains("-")){
                String[] ports = endPointPort.split("-");
                this.minPort = ports[0];
                this.maxPort = ports[1];
            }
            epProt = String.format("%s-%s", minPort, maxPort);
            String expr =  "./AllocatePublicPortFrom/FixedPortRange";
            XPath xPath = XPathFactory.newInstance().newXPath();
            Element eleFxdPortRan = (Element) xPath.evaluate(expr, getThisEndPointNode(), XPathConstants.NODE);
            eleFxdPortRan.setAttribute(WindowsAzureConstants.ATTR_MINPORT, this.minPort);
            eleFxdPortRan.setAttribute(WindowsAzureConstants.ATTR_MAXPORT, this.maxPort);
            this.port = epProt;
        }
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException(
                    "Exception in setPort ", e);
        }

    }

    /**
     * Sets the Input Local Port.
     *
     * @param localPort
     * @throws WindowsAzureInvalidProjectOperationException
     */
    protected void setLocalPort(String localPort)
            throws WindowsAzureInvalidProjectOperationException {
        if (localPort == null) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants.EXCP_EMPTY_INPUT_LOCAL_PORT);
        }
        if (!getName().isEmpty()) {
            try {
                Node epNode = getThisEndPointNode();
                epNode.getAttributes().getNamedItem("localPort")
                .setNodeValue(localPort);
            } catch (Exception ex) {
                throw new WindowsAzureInvalidProjectOperationException(
                        WindowsAzureConstants.EXCP_SET_LOCAL_PORT, ex);
            }
        }
        this.localPort = localPort;
    }

    /**
     * Gets the private Port.
     *
     * @return
     */
    public String getPrivatePort() {
        return localPort;
    }

    /**
     * Sets the private EndpointName port.
     *
     * @return
     * @throws WindowsAzureInvalidProjectOperationException
     */
    public void setPrivatePort(String endPointPort)
            throws WindowsAzureInvalidProjectOperationException {
        if ((endPointPort == null) || (endPointPort.isEmpty())) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants.EXCP_EMPTY_PRIVATE_PORT);
        }
        try {
        Element epNode = (Element) getThisEndPointNode();
        if (getEndPointType().equals(WindowsAzureEndpointType.Input)||
                (getEndPointType().equals(WindowsAzureEndpointType.InstanceInput))) {
            setLocalPort(endPointPort);
        } else {
            for (Node child = epNode.getFirstChild();
                    child != null; child = child.getNextSibling()) {
                if (child.getNodeName().equalsIgnoreCase("FixedPort")) {
                    Element eleChild = (Element) child;
                    
                    if(endPointPort.contains("-")) { 
                    	// create internal input range
                    	Element eleFxdPortRan = winProjMgr.getdefinitionFileDoc().createElement("FixedPortRange");
                    	String[] ports = endPointPort.split("-");
                        String minPort = ports[0];
                        String maxPort = ports[1];
                        eleFxdPortRan.setAttribute(WindowsAzureConstants.ATTR_MINPORT, minPort);
                        eleFxdPortRan.setAttribute(WindowsAzureConstants.ATTR_MAXPORT, maxPort);
                        eleChild.getParentNode().appendChild(eleFxdPortRan);
                        
                        // Remove fixed port
                        eleChild.getParentNode().removeChild(eleChild);
                    } else { // Just update port
                    	eleChild.setAttribute("port", endPointPort);
                    }
                } else if(child.getNodeName().equalsIgnoreCase("FixedPortRange")) {
                	Element eleChild = (Element) child;
                	
                	if(endPointPort.contains("-")) {
                		// updates port range values
                		String[] ports = endPointPort.split("-");
                        String minPort = ports[0];
                        String maxPort = ports[1];
                        eleChild.setAttribute(WindowsAzureConstants.ATTR_MINPORT, minPort);
                        eleChild.setAttribute(WindowsAzureConstants.ATTR_MAXPORT, maxPort);
                        
                	} else { 
                		//create internal fixed port
                		Element eleFxdPort = winProjMgr.getdefinitionFileDoc().createElement("FixedPort");
                		eleFxdPort.setAttribute("port", endPointPort);
                		eleChild.getParentNode().appendChild(eleFxdPort);
                		
                		// Removing internal port range
                		eleChild.getParentNode().removeChild(eleChild);
                	}
                }
            }
        }
        this.localPort = endPointPort;
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException(
                    "Exception occured" , e);
        }
    }

    /**
     * Gets Endpoint type.
     *
     * @return internalFixedPort
     * @throws WindowsAzureInvalidProjectOperationException
     */
    public WindowsAzureEndpointType getEndPointType()
            throws WindowsAzureInvalidProjectOperationException {
        WindowsAzureEndpointType type = null;
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = getWindowsAzureProjMgr().getdefinitionFileDoc();
            String expr = String.format(WindowsAzureConstants.ENDPOINT+ "/*[@name='" + getName() + "']", wRole.getName());
            Node epNode = (Node) xPath.evaluate(expr, doc,
                    XPathConstants.NODE);
            if (epNode.getNodeName().equalsIgnoreCase("InternalEndpoint")) {
                type = WindowsAzureEndpointType.Internal;
            } else if(epNode.getNodeName().equalsIgnoreCase("InputEndpoint")) {
                type = WindowsAzureEndpointType.Input;
            } else {
                type = WindowsAzureEndpointType.InstanceInput;
            }
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException(
                    "Exception occured" , e);
        }
        return type;
    }

    /**
     * Sets Endpoint type.
     *
     * @param type
     * @throws WindowsAzureInvalidProjectOperationException
     */
    public void setEndPointType(WindowsAzureEndpointType type)
            throws WindowsAzureInvalidProjectOperationException {

        try {
            Document doc = getWindowsAzureProjMgr().getdefinitionFileDoc();
            if (getEndPointType() != type) {

                if(getEndPointType().equals(WindowsAzureEndpointType.InstanceInput)) {
                    XPath xPath = XPathFactory.newInstance().newXPath();
                    String VarExpr = String.format(WindowsAzureConstants.VAR_WITH_SPECIFIC_NAME,wRole.getName(), getName() + "_PUBLICPORT");
                    Element eleVar = (Element) xPath.evaluate(VarExpr, doc, XPathConstants.NODE);
                    if(eleVar != null) {
                        eleVar.getParentNode().removeChild(eleVar);
                    }
                }

                if (type == WindowsAzureEndpointType.Input) {
                    // check if endpoint is associated with session affinity, if yes then throw error
                    WindowsAzureEndpoint saEndPt = wRole.getSessionAffinityInternalEndpoint();
                    if(saEndPt != null && getName().equalsIgnoreCase(saEndPt.getName()))
                        throw new WindowsAzureInvalidProjectOperationException(
                                WindowsAzureConstants.EXCP_SA_ENDPOINT_TYPE_CHANGE);
                    String newPubPort = "";
                    if(getEndPointType() == WindowsAzureEndpointType.Internal) {
                        // while changing internal to input, private and public port
                        // are similar
                        newPubPort = getPrivatePort();
                        if(newPubPort.contains("-")) {
                            newPubPort = (newPubPort.split("-"))[0];
                        }
                    } else {
                        //old type is instance
                        newPubPort = getPort();
                        if(newPubPort.contains("-")) {
                            newPubPort = (newPubPort.split("-"))[0];
                        }
                    }
                    // CreateInputNode
                    Element eleInputNode = doc.createElement("InputEndpoint");
                    eleInputNode.setAttribute("name", getName());
                    eleInputNode.setAttribute("port", newPubPort);
                    eleInputNode.setAttribute("localPort", getPrivatePort());
                    eleInputNode.setAttribute("protocol", "tcp");
                    Node internalNode = getThisEndPointNode();
                    internalNode.getParentNode().appendChild(eleInputNode);
                    // remove internal node
                    internalNode.getParentNode().removeChild(internalNode);
                    //set the port to update the corresponding cache of port
                    setPort(newPubPort);
                }
                if (type == WindowsAzureEndpointType.Internal) {
                    // check if endpoint is associated with session affinity , if yes then throw error
                    WindowsAzureEndpoint saEndPt = wRole.getSessionAffinityInputEndpoint();
                    if(saEndPt != null && getName().equalsIgnoreCase(saEndPt.getName()))
                        throw new WindowsAzureInvalidProjectOperationException(
                                WindowsAzureConstants.EXCP_SA_ENDPOINT_TYPE_CHANGE);
                    // change in cache
                    Element eleInternalNode = doc
                            .createElement("InternalEndpoint");
                    eleInternalNode.setAttribute("name",
                            getName());
                    eleInternalNode.setAttribute("protocol", "tcp");
                    Element eleFixedPort = doc.createElement("FixedPort");
                    eleFixedPort.setAttribute("port", getPrivatePort());
                    eleInternalNode.appendChild(eleFixedPort);
                    Node currNode = getThisEndPointNode();
                    currNode.getParentNode().appendChild(eleInternalNode);
                    currNode.getParentNode().removeChild(currNode);
                }
               if( type == WindowsAzureEndpointType.InstanceInput) {
                   WindowsAzureEndpoint saEndPt = wRole.getSessionAffinityInternalEndpoint();
                   if(saEndPt != null && getName().equalsIgnoreCase(saEndPt.getName()))
                       throw new WindowsAzureInvalidProjectOperationException(
                               WindowsAzureConstants.EXCP_SA_ENDPOINT_TYPE_CHANGE);

                   String newPubPort = "";
                   if(getEndPointType() == WindowsAzureEndpointType.Internal) {
                       // while changing internal to instance, private and public port
                       // are similar
                       newPubPort = getPrivatePort();
                       if(newPubPort.contains("-")) {
                           newPubPort = (newPubPort.split("-"))[0];
                       }
                   } else {
                       //old type is input
                       newPubPort = getPort();
                   }
                   if (!newPubPort.contains("-")) {
                       newPubPort = String.format("%s-%s", newPubPort,newPubPort);
                   }


                   // CreateInputNode
                   Element eleInstanceNode = doc.createElement("InstanceInputEndpoint");
                   eleInstanceNode.setAttribute(WindowsAzureConstants.ATTR_NAME, getName());
                   eleInstanceNode.setAttribute("protocol", "tcp");
                   eleInstanceNode.setAttribute("localPort", getPrivatePort());

                   Element eleAllPubPort = doc.createElement("AllocatePublicPortFrom");
                   Element eleFxdPortRan = doc.createElement("FixedPortRange");
                   eleFxdPortRan.setAttribute(WindowsAzureConstants.ATTR_MINPORT, minPort);
                   eleFxdPortRan.setAttribute(WindowsAzureConstants.ATTR_MAXPORT, maxPort);

                   eleAllPubPort.appendChild(eleFxdPortRan);
                   eleInstanceNode.appendChild(eleAllPubPort);

                   wRole.setVarInDefFile(getName() + "_PUBLICPORT");
                   XPath xPath = XPathFactory.newInstance().newXPath();
                   String VarExpr = String.format(WindowsAzureConstants.VAR_WITH_SPECIFIC_NAME,wRole.getName(), getName() + "_PUBLICPORT");
                   Element eleVar = (Element) xPath.evaluate(VarExpr, doc, XPathConstants.NODE);
                   Element insval = doc.createElement("RoleInstanceValue");
                   String val = String.format(WindowsAzureConstants.EP_INSTANCE_VAR, getName());
                   insval.setAttribute("xpath", val);
                   eleVar.appendChild(insval);

                   Node currNode = getThisEndPointNode();
                   currNode.getParentNode().appendChild(eleInstanceNode);
                   // remove internal node
                   currNode.getParentNode().removeChild(currNode);
                   //Update cache
                   setPort(newPubPort);
               }
            }
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    WindowsAzureConstants.EXCP_SET_TYPE, ex);
        }
    }

    /**
     * Deletes the current endpoint from WindowsAzureRole.
     *
     * @throws WindowsAzureInvalidProjectOperationException
     */
    public void delete() throws WindowsAzureInvalidProjectOperationException {
        try {

            if (isCachingEndPoint() && wRole.isCachingEnable()) {
                 throw new WindowsAzureInvalidProjectOperationException(
                         "Endpoint is assoiciated with caching");
            }

            if(getEndPointType().equals(WindowsAzureEndpointType.InstanceInput)) {
                XPath xPath = XPathFactory.newInstance().newXPath();
                Document doc = getWindowsAzureProjMgr().getdefinitionFileDoc();
                String VarExpr = String.format(WindowsAzureConstants.VAR_WITH_SPECIFIC_NAME,wRole.getName(), getName() + "_PUBLICPORT");
                Element eleVar = (Element) xPath.evaluate(VarExpr, doc, XPathConstants.NODE);
                if(eleVar != null) {
                    eleVar.getParentNode().removeChild(eleVar);
                }
            }

            Node endPoint = getThisEndPointNode();
            if (endPoint != null) {
                endPoint.getParentNode().removeChild(endPoint);
                wRole.getEndpoints().remove(this);
            }
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    WindowsAzureConstants.EXCP_DEL_ENDPOINT, ex);
        }
    }

    /**
     * This API will return true if this endpoint is associated with windows azure cache.
     * @return
     * @throws WindowsAzureInvalidProjectOperationException
     */
    public boolean isCachingEndPoint() throws WindowsAzureInvalidProjectOperationException {
        boolean isCachingAssociated = false;
        try {

            if(getName().startsWith("memcache_") &&	wRole.getNamedCaches().keySet().contains(
                    getName().subSequence("memcache_".length(), getName().length()))) {
                isCachingAssociated = true;
            }
        }catch(Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    WindowsAzureConstants.EXCP);
        }

        return isCachingAssociated;
    }

}
