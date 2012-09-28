
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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Class representing Windows Azure role.
 */
public class WindowsAzureRole {
    private String name;
    private String instances;
    private String vmSize;
    private String accUsername = "";
    private String accPassword = "";
    private String accExpiryDate = "";
    private String certThumbprint = "";
    private List<WindowsAzureEndpoint> winEndPtList = new ArrayList<WindowsAzureEndpoint>();
    private WindowsAzureProjectManager winProjMgr = null;
    protected Map<String, String> envVarMap = new HashMap<String, String>();
    protected List<String> lsVarList = new ArrayList<String>();
    protected Map<String, WindowsAzureLocalStorage> locStoMap =
            new HashMap<String, WindowsAzureLocalStorage>();
    protected List<WindowsAzureRoleComponent> winCompList  =
            new ArrayList<WindowsAzureRoleComponent>();
    protected Map<String, WindowsAzureNamedCache> cacheMap =
            new HashMap<String, WindowsAzureNamedCache>();

    protected WindowsAzureProjectManager getWinProjMgr()
            throws WindowsAzureInvalidProjectOperationException {
        if (winProjMgr == null) {
            throw new WindowsAzureInvalidProjectOperationException(
                    WindowsAzureConstants.PMGR_NULL);
        }
        return winProjMgr;
    }

    public WindowsAzureRole(
            WindowsAzureProjectManager winPrjMgr) {
        winProjMgr = winPrjMgr;
    }

    /**
     * Gets the role name. * @return name
     **/
    public String getName() {
        return name;
    }

    /**
     * Sets the role name.
     *
     * @param name .
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    public void setName(String name)
            throws WindowsAzureInvalidProjectOperationException {
        if ((null == name) || (name.isEmpty())) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants.EXCP_EMPTY_ROLENAME);
        }
        try {
            String objName = getName();
            String oldpath = String.format("%s%s%s",
                    getWinProjMgr().projDirPath, File.separator,
                    getName());
            String newPath = String.format("%s%s%s",
                    getWinProjMgr().projDirPath, File.separator, name);
            if (objName != null) {
                // Change definition File
                setNameInDefFile(name);
                // Change Configuration File
                setNameInConfigFile(name);

                // Change in package.xml
                setNameInPackageFile(name);
                // Rename folder on HD


                Vector<String> value = new Vector<String>();
                value.add(oldpath);
                value.add(newPath);

                //if already rename option done on same role and not yet saved,
                //need to retrieve original name of folder and
                //add it in map with new changed name.
                if (getWinProjMgr().mapActivity.
                        containsKey("rename")) {
                    Vector<String> oldVal =
                            getWinProjMgr().mapActivity.get("rename");
                    value.set(0, oldVal.get(0));
                }

                getWinProjMgr().mapActivity.put("rename", value);
            }
            this.name = name;
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    WindowsAzureConstants.EXCP_SET_ROLENAME, ex);
        }
    }


    private void setNameInDefFile(String name)
            throws WindowsAzureInvalidProjectOperationException {
        try {
            Document doc = getWinProjMgr().getdefinitionFileDoc();
            XPath xPath = XPathFactory.newInstance().newXPath();
            String expr = String.format(
                    WindowsAzureConstants.WR_NAME, getName());
            Node role = (Node) xPath.evaluate(expr, doc, XPathConstants.NODE);

            if (role != null) {
                role.getAttributes().getNamedItem(
                        WindowsAzureConstants.ATTR_NAME)
                        .setNodeValue(name);
            }
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    "Exception in setNameInDefFile ", ex);
        }
    }


    private void setNameInConfigFile(String name)
            throws WindowsAzureInvalidProjectOperationException {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document configDoc = getWinProjMgr().getConfigFileDoc();
            if (configDoc != null) {
                String expr = String.format(
                        WindowsAzureConstants.ROLE_NAME, getName());
                Node role = (Node) xPath.evaluate(expr, configDoc,
                        XPathConstants.NODE);
                role.getAttributes().getNamedItem(
                        WindowsAzureConstants.ATTR_NAME)
                        .setNodeValue(name);
            }
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    "Exception in setNameInConfigFile", ex);
        }
    }

    private void setNameInPackageFile(String name)
            throws WindowsAzureInvalidProjectOperationException {

        WindowsAzureEndpoint saInputEndPoint =
                getSessionAffinityInputEndpoint();
        WindowsAzureEndpoint saInternalEndPoint =
                getSessionAffinityInternalEndpoint();

        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document projDoc = getWinProjMgr().getPackageFileDoc();

            //Change session affinity settings in package.xml
            if (saInputEndPoint != null) {
                String endPointExpr = String.format(WindowsAzureConstants.
                        SA_INPUT_ENDPOINT, this.getName());
                Element propEndPoint = (Element) xPath.evaluate(endPointExpr,
                        projDoc, XPathConstants.NODE);
                if (propEndPoint != null) {
                    propEndPoint.setAttribute(WindowsAzureConstants.ATTR_NAME,
                            String.format(WindowsAzureConstants.
                                    SA_INPUT_ENDPOINT_NAME_PROP, name));
                }
                //Change internal endpoint name as well
                if (saInternalEndPoint != null) {
                    endPointExpr = String.format(WindowsAzureConstants.
                            SA_INTERNAL_ENDPOINT, this.getName());
                    propEndPoint = (Element) xPath.evaluate(endPointExpr,
                            projDoc, XPathConstants.NODE);
                    if (propEndPoint != null) {
                        propEndPoint.setAttribute(WindowsAzureConstants.
                                ATTR_NAME, String.format(WindowsAzureConstants.
                                        SA_INTERNAL_ENDPOINT_NAME_PROP, name));
                    }
                }

            }

            //changes to rename role name in property
            String parentNodeExpr = WindowsAzureConstants.PROJ_PROPERTY;
            String eleName = WindowsAzureConstants.PROJ_PROPERTY_ELEMENT_NAME;
            Map<String, String> attrs = new HashMap<String, String>();

            //server property
            if(getServerName() != null && !getServerName().isEmpty()) {
                attrs.put(WindowsAzureConstants.ATTR_NAME,
                        String.format(WindowsAzureConstants.SERVER_PROP_NAME, name));
                String serExpr = String.format(WindowsAzureConstants.SERVER_PROP_PATH,getName());
                updateOrCreateElement(projDoc, serExpr, parentNodeExpr, eleName, false, attrs);
            }
            // cache account name
            if(getCacheStorageAccountName() != null && !getCacheStorageAccountName().isEmpty()) {
                attrs.clear();
                attrs.put(WindowsAzureConstants.ATTR_NAME,
                        String.format(WindowsAzureConstants.CACHE_ST_ACC_NAME_PROP, name));
                String propName = String.format(WindowsAzureConstants.CACHE_ST_ACC_NAME_PROP,getName());
                String nodeExpr =  String.format(WindowsAzureConstants.ROLE_PROP, propName);
                updateOrCreateElement(projDoc, nodeExpr, parentNodeExpr, eleName, false, attrs);
            }
            // cache account key
            if (getCacheStorageAccountKey() != null && !getCacheStorageAccountKey().isEmpty()) {
                attrs.clear();
                attrs.put(WindowsAzureConstants.ATTR_NAME,
                        String.format(WindowsAzureConstants.CACHE_ST_ACC_KEY_PROP, name));
                String propName = String.format(WindowsAzureConstants.CACHE_ST_ACC_KEY_PROP,getName());
                String nodeExpr =  String.format(WindowsAzureConstants.ROLE_PROP, propName);
                updateOrCreateElement(projDoc, nodeExpr, parentNodeExpr, eleName, false, attrs);
            }

            String expr = String.format(
                    WindowsAzureConstants.WA_PACK_NAME, getName());
            Node role = (Node) xPath.evaluate(expr, projDoc,
                    XPathConstants.NODE);
            if (role != null) {
                role.getAttributes().getNamedItem(
                        WindowsAzureConstants.ATTR_NAME)
                        .setNodeValue(name);
                role.getAttributes()
                .getNamedItem("approotdir")
                .setNodeValue(String.format("%s%s%s",
                        "${basedir}\\", name, "\\approot"));
            }

        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    "exception in setNameInPackageFile ", ex);

        }
    }
    /**
     * Gets the instance count.
     *
     * @return count
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    public String getInstances()
            throws WindowsAzureInvalidProjectOperationException {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document configDoc = getWinProjMgr().getConfigFileDoc();
            // /ServiceConfiguration/Role
            String expr = String.format(
                    WindowsAzureConstants.ROLE_COUNT, getName());
            this.instances = xPath.evaluate(expr, configDoc);
            return  this.instances;
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    WindowsAzureConstants.EXCP_GET_INSTANCES, ex);
        }
    }

    /**
     * Sets the instance count.
     *
     * @param count .
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    public void setInstances(String count)
            throws WindowsAzureInvalidProjectOperationException {
        if ((null == count) || (count.isEmpty())) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants.INVALID_ARG);
        }
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document configDoc = getWinProjMgr().getConfigFileDoc();
            String expr = String.format(WindowsAzureConstants.ROLE_INSTANCES,
                    getName());
            Element role = (Element) xPath.evaluate(expr, configDoc,
                    XPathConstants.NODE);
            role.setAttribute("count", count);
            this.instances = count;
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    WindowsAzureConstants.EXCP_SET_INSTANCES, ex);
        }
    }

    /**
     * Gets the VM size.
     *
     * @param name
     * @return vmSize .
     */
    public String getVMSize() {
        return vmSize;
    }

    /**
     * Sets the VM size.
     *
     * @param vMSize .
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    public void setVMSize(final String vMSize)
            throws WindowsAzureInvalidProjectOperationException {
        if ((vMSize == null) || (vMSize.isEmpty())) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants.EXCP_EMPTY_VMSIZE);
        }
        try {
            if (getName() != null) {
                Document doc = getWinProjMgr().getdefinitionFileDoc();
                String objName = getName();
                XPath xPath = XPathFactory.newInstance().newXPath();

                String expr = String.format(WindowsAzureConstants.WR_NAME,
                        objName);
                Node role = (Node) xPath.evaluate(expr, doc,
                        XPathConstants.NODE);
                if (role != null) {
                    Element roleEle = (Element) role;
                    roleEle.setAttribute("vmsize", vMSize);
                }
            }

            this.vmSize = vMSize;
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    WindowsAzureConstants.EXCP_SET_VMSIZE, ex);
        }
    }

    /**
     * Gets the list of endpoints that are associated with this role.
     *
     * @return new instance of WindowsAzureEndpoint.
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    public List<WindowsAzureEndpoint> getEndpoints()
            throws WindowsAzureInvalidProjectOperationException {

        try {
            if (winEndPtList.isEmpty()) {
                Document doc = getWinProjMgr().getdefinitionFileDoc();
                String objName = getName();
                XPath xPath = XPathFactory.newInstance().newXPath();
                String expr = String.format(
                        WindowsAzureConstants.INPUTS_WR_NAME, objName);
                NodeList endPtList = (NodeList) xPath.evaluate(
                        expr, doc, XPathConstants.NODESET);
                for (int i = 0; i < endPtList.getLength(); i++) {
                    Element endptEle = (Element) endPtList.item(i);
                    winEndPtList.add(createWinInputEndPt(endptEle));
                }

                expr = String.format(
                        WindowsAzureConstants.INTERNAL_WR_NAME, objName);
                endPtList = (NodeList) xPath.evaluate(
                        expr, doc, XPathConstants.NODESET);
                for (int i = 0; i < endPtList.getLength(); i++) {
                    Element endptEle = (Element) endPtList.item(i);
                    winEndPtList.add(createWinIntenalEndPt(endptEle));
                }

                expr = String.format(
                        WindowsAzureConstants.INSTANCE_WR_NAME, objName);
                endPtList = (NodeList) xPath.evaluate(
                        expr, doc, XPathConstants.NODESET);
                for (int i = 0; i < endPtList.getLength(); i++) {
                    Element endptEle = (Element) endPtList.item(i);
                    winEndPtList.add(createWinInstanceEndPt(endptEle));
                }
            }
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    WindowsAzureConstants.EXCP_GET_FP, ex);
        }
        return winEndPtList;
    }

    private WindowsAzureEndpoint createWinInputEndPt( Element endptEle)
            throws WindowsAzureInvalidProjectOperationException {
        try {
            WindowsAzureEndpoint winAzureEndpoint =
                    new WindowsAzureEndpoint(getWinProjMgr(), this);
            winAzureEndpoint.setName(
                    endptEle.getAttribute(
                            WindowsAzureConstants.ATTR_NAME));
            winAzureEndpoint.setLocalPort(
                    endptEle.getAttribute("localPort"));
            winAzureEndpoint.setPort(
                    endptEle.getAttribute("port"));
            return winAzureEndpoint;
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    "Exception in createWinInputEndPt ", ex);
        }
    }

    private WindowsAzureEndpoint createWinInstanceEndPt( Element endptEle)
            throws WindowsAzureInvalidProjectOperationException {
        try {
            WindowsAzureEndpoint winAzureEndpoint =
                    new WindowsAzureEndpoint(getWinProjMgr(), this);
            winAzureEndpoint.setName (
                    endptEle.getAttribute(
                            WindowsAzureConstants.ATTR_NAME));
            winAzureEndpoint.setLocalPort(
                    endptEle.getAttribute("localPort"));

            String expr =  WindowsAzureConstants.INS_FIX_RANGE;
            XPath xPath = XPathFactory.newInstance().newXPath();
            Element eleFxdPortRan = (Element) xPath.evaluate(expr, endptEle, XPathConstants.NODE);

            winAzureEndpoint.setPort(
                    eleFxdPortRan.getAttribute(WindowsAzureConstants.ATTR_MINPORT) + "-" +
                            eleFxdPortRan.getAttribute(WindowsAzureConstants.ATTR_MAXPORT));
            return winAzureEndpoint;
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    "Exception in createWinInstanceEndPt ", ex);
        }
    }


    private WindowsAzureEndpoint createWinIntenalEndPt( Element endptEle)
            throws WindowsAzureInvalidProjectOperationException {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            WindowsAzureEndpoint winAzureEndpoint = new
                    WindowsAzureEndpoint(getWinProjMgr(), this);
            winAzureEndpoint.setName(
                    endptEle.getAttribute(
                            WindowsAzureConstants.ATTR_NAME));
            String port = xPath.evaluate("./FixedPort/@port",
                    endptEle);
            if(port == null || port.isEmpty()) {
                port = String.format("%s-%s", xPath.evaluate("./FixedPortRange/@min",
                        endptEle),xPath.evaluate("./FixedPortRange/@max",
                                endptEle)) ;
            }

            winAzureEndpoint.setPrivatePort(port);
            return winAzureEndpoint;
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    "Exception in createWinInputEndPt ", ex);
        }
    }
    protected void setEndpoints(List<WindowsAzureEndpoint> winEndPList) {

        this.winEndPtList = winEndPList;
    }
    /** This API returns windows azure endpoint */
    public WindowsAzureEndpoint getEndpoint(String endPointName) throws WindowsAzureInvalidProjectOperationException {

        WindowsAzureEndpoint windowsAzureEndpoint = null ;
         List<WindowsAzureEndpoint> endPoints = this.getEndpoints();
        if(endPoints != null) {
            for (int index = 0; index < endPoints.size();index++) {
                if (endPoints.get(index).getName().equalsIgnoreCase(endPointName))
                   {
                    windowsAzureEndpoint = endPoints.get(index);
                    break;
                }
            }
        }
        return windowsAzureEndpoint;
    }

    /**
     * Creates a new instance of WindowsAzureEndpoint, and adds it to the
     * current role.
     *
     * @param endpointName .
     * @param endpointType .
     * @param localPortNumber .
     * @param externPortNo .
     * @return new instance of WindowsAzureEndpoint.
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    public WindowsAzureEndpoint addEndpoint(String endpointName,
            WindowsAzureEndpointType endpointType, String localPortNumber,
            String externPortNo)
                    throws WindowsAzureInvalidProjectOperationException {

        if ((endpointName == null) || (endpointType == null)
                || (localPortNumber == null) || (externPortNo == null)) {
            throw new IllegalArgumentException();
        }

        WindowsAzureEndpoint newEndPoint = new WindowsAzureEndpoint(
                getWinProjMgr(), this);
        try {
            // newEndPoint.set
            Document doc = getWinProjMgr().getdefinitionFileDoc();
            XPath xPath = XPathFactory.newInstance().newXPath();

            String expr1 = String.format(WindowsAzureConstants.WR_NAME,
                    getName());
            Node role;
            role = (Node) xPath.evaluate(expr1, doc, XPathConstants.NODE);
            boolean flag = false;
            for (Node child = role.getFirstChild(); child != null; child = child
                    .getNextSibling()) {
                if (child.getNodeName().equalsIgnoreCase("Endpoints")) {
                    flag = true;
                }
            }
            // if endpoint tag doesn't exist, create new endpoint tag
            if (!flag) {
                Element eleEndpoint = doc.createElement("Endpoints");
                role.appendChild(eleEndpoint);
            }
            String expr = String.format(WindowsAzureConstants.ENDPOINT_WR_NAME,
                    getName());
            Node endPoint;
            endPoint = (Node) xPath.evaluate(expr, doc, XPathConstants.NODE);
            if (endpointType == WindowsAzureEndpointType.Input) {
                Element eleInputEndpoint = doc.createElement("InputEndpoint");
                eleInputEndpoint.setAttribute(
                        WindowsAzureConstants.ATTR_NAME, endpointName);
                eleInputEndpoint.setAttribute("port", externPortNo);
                eleInputEndpoint.setAttribute("localPort", localPortNumber);
                eleInputEndpoint.setAttribute("protocol", "tcp");
                endPoint.appendChild(eleInputEndpoint);
                newEndPoint.setName(endpointName);
                newEndPoint.setLocalPort(localPortNumber);
                newEndPoint.setPort(externPortNo);
            }
            if (endpointType == WindowsAzureEndpointType.Internal) {
                Element eleInternalEpt = doc
                        .createElement("InternalEndpoint");
                eleInternalEpt.setAttribute(
                        WindowsAzureConstants.ATTR_NAME, endpointName);
                eleInternalEpt.setAttribute("protocol", "tcp");
                Node node = endPoint.appendChild(eleInternalEpt);
                if(localPortNumber.contains("-")) {
                    String[] ports = localPortNumber.split("-");
                    String minPort = ports[0];
                    String maxPort = ports[1];
                    Element eleFxdPortRan = doc.createElement("FixedPortRange");
                    eleFxdPortRan.setAttribute(WindowsAzureConstants.ATTR_MINPORT, minPort);
                    eleFxdPortRan.setAttribute(WindowsAzureConstants.ATTR_MAXPORT, maxPort);
                    node.appendChild(eleFxdPortRan);
                } else {
                	Element eleFixedport = doc.createElement("FixedPort");
                    eleFixedport.setAttribute("port", localPortNumber);
                    node.appendChild(eleFixedport);
                }
                newEndPoint.setName(endpointName);
                newEndPoint.setPrivatePort(localPortNumber);
            }
            if (endpointType == WindowsAzureEndpointType.InstanceInput) {
                String minPort = externPortNo;
                String maxPort = externPortNo;
                if (externPortNo.contains("-")) {
                    String[] ports = externPortNo.split("-");
                    minPort = ports [0];
                    maxPort = ports [1];
                }
                Element eleInstanceNode = doc.createElement("InstanceInputEndpoint");
                eleInstanceNode.setAttribute(WindowsAzureConstants.ATTR_NAME, endpointName);
                eleInstanceNode.setAttribute("protocol", "tcp");
                eleInstanceNode.setAttribute("localPort", localPortNumber);
                Element eleAllPubPort = doc.createElement("AllocatePublicPortFrom");
                Element eleFxdPortRan = doc.createElement("FixedPortRange");
                eleFxdPortRan.setAttribute(WindowsAzureConstants.ATTR_MINPORT, minPort);
                eleFxdPortRan.setAttribute(WindowsAzureConstants.ATTR_MAXPORT, maxPort);
                eleAllPubPort.appendChild(eleFxdPortRan);
                eleInstanceNode.appendChild(eleAllPubPort);

                endPoint.appendChild(eleInstanceNode);
                setVarInDefFile(endpointName + "_PUBLICPORT");
                String VarExpr = String.format(WindowsAzureConstants.VAR_WITH_SPECIFIC_NAME, getName(), endpointName + "_PUBLICPORT");
                Element elevar = (Element) xPath.evaluate(VarExpr, doc, XPathConstants.NODE);
                Element insval = doc.createElement("RoleInstanceValue");
                String val = String.format(WindowsAzureConstants.EP_INSTANCE_VAR, endpointName);
                insval.setAttribute("xpath", val);
                elevar.appendChild(insval);

                newEndPoint.setName(endpointName);
                newEndPoint.setLocalPort(localPortNumber);
                newEndPoint.setPort(externPortNo);
            }
        winEndPtList.add(newEndPoint);
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    WindowsAzureConstants.EXCP_ADD_ENDPOINT, ex);
        }
        return newEndPoint;
    }

    /**
     * Validates the specified endpoint name.
     *
     * @param endpointName .
     * @return true if the specified endpoint name is valid; false otherwise.
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    public Boolean isAvailableEndpointName(String endpointName, WindowsAzureEndpointType epType)
            throws WindowsAzureInvalidProjectOperationException {
        Boolean isAvlEpName = true;
        try {
            if (endpointName == null) {
                isAvlEpName = false;
            } else if (endpointName.isEmpty()) {
                isAvlEpName = false;
            }
            if (isAvlEpName) {
                if(epType == WindowsAzureEndpointType.Internal) {
                    List<WindowsAzureEndpoint> eps =  getEndpoints();
                    for (WindowsAzureEndpoint ep : eps) {
                        if(ep.getName().equalsIgnoreCase(endpointName)) {
                            isAvlEpName = false;
                        }
                    }
                } else {
                    List<WindowsAzureRole> roles = getWinProjMgr()
                            .getRoles();
                    for (int i = 0; i < roles.size(); i++) {
                        List<WindowsAzureEndpoint> endPoints = roles.get(i)
                                .getEndpoints();
                        for (int nEndpoint = 0; nEndpoint < endPoints.size();
                                nEndpoint++) {
                            if (endPoints.get(nEndpoint).getName()
                                    .equalsIgnoreCase(endpointName)) {
                                isAvlEpName = false;
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    WindowsAzureConstants.EXCP_RETRIEVING_ENDPOINT_NAME, ex);
        }
        return isAvlEpName;
    }

    /**
     * Validates the specified endpoint.
     *
     * @param endpointName .
     * @param endpointType .
     * @param localPortNumber .
     * @param externPortNo .
     * @return true if the specified endpoint is valid; false otherwise.
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    public Boolean isValidEndpoint(String endpointName,
            WindowsAzureEndpointType endpointType, String localPortNumber,
            String externPortNo)
                    throws WindowsAzureInvalidProjectOperationException {
        Boolean isValidEp = true;
        try {
            if ((endpointName == null) || (endpointType == null)
                    || (localPortNumber == null) || (externPortNo == null)) {
                isValidEp = false;
            }
            //Check for number
            try {
                if (localPortNumber.contains("-")) {
                    String[] str = localPortNumber.split("-");
                    String min = str[0];
                    String max = str[1];
                    Integer.valueOf(min);
                    Integer.valueOf(max);
                } else {
                    Integer.valueOf(localPortNumber);
                }
                if (externPortNo.contains("-")) {
                    String[] str = externPortNo.split("-");
                    String min = str[0];
                    String max = str[1];
                    Integer.valueOf(min);
                    Integer.valueOf(max);
                } else if (!externPortNo.isEmpty()){
                    Integer.valueOf(externPortNo);
                }
            } catch (NumberFormatException ex) {
                return false;
            }

            if (endpointType == WindowsAzureEndpointType.Input) {
                isValidEp = isValidInputEp(endpointName, localPortNumber, externPortNo);
            } else if (endpointType == WindowsAzureEndpointType.Internal) {
                isValidEp = isValidInternalEp(endpointName, localPortNumber,externPortNo);
            } else {
                isValidEp = isValidInstanceEp(endpointName, localPortNumber, externPortNo);
            }
        } catch (Exception ex) {
            isValidEp = false;
        }
        return isValidEp;
    }

    /**
     * This method will check input endpoint is valid or not
     * @param endpointName
     * @param localPort
     * @param pubPort
     * @return
     */
    private boolean isValidInputEp(String endpointName, String localPort, String pubPort) {
        try {
            boolean isValidEp = true;
            if(localPort.contains("-") || pubPort.contains("-") ||
                    localPort.isEmpty() || pubPort.isEmpty()) {
                isValidEp = false;
            } else {
                //check that localPort should be unique in role
                List<WindowsAzureEndpoint> eps = getEndpoints();
                for (WindowsAzureEndpoint ep : eps) {
                    if(endpointName.equalsIgnoreCase(ep.getName())) {
                        //edit case
                        continue;
                    }

                    //Private port of instance endpoint  and input endpoint's private port can be same.
                    if (!ep.getEndPointType().equals(WindowsAzureEndpointType.InstanceInput) &&
                    		ParserXMLUtility.isEpPortEqualOrInRange(ep.getPrivatePort(), localPort)) {
                        isValidEp = false;
                        break;
                    }

                    if (ParserXMLUtility.isEpPortEqualOrInRange(ep.getPort(), localPort) ||
                            (ParserXMLUtility.isEpPortEqualOrInRange(ep.getPort(), pubPort) ||
                                    ParserXMLUtility.isEpPortEqualOrInRange(ep.getPrivatePort(), pubPort))) {
                        isValidEp = false;
                        break;
                    }
                }
                if(isValidEp) {
                    isValidEp = winProjMgr.isDupInputPubPort(pubPort, endpointName);
                }
            }
            return isValidEp;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * This method will check internal endpoint is valid or not
     * @param endpointName
     * @param localPort
     * @param pubPort
     * @return
     */
    private boolean isValidInternalEp(String endpointName, String localPort, String pubPort) {
        boolean isValid = true;
        try {
            if(!pubPort.isEmpty()) {
                //For internal endpoint public port must be empty.
                isValid = false;
            } else {
                // check for max range should be greater than or equal to min
                if(localPort.contains("-")) {
                    isValid = ParserXMLUtility.isValidRange(localPort);
                }
                if(isValid) {
                    //check that localPort should be unique in role
                    List<WindowsAzureEndpoint> eps = getEndpoints();
                    for (WindowsAzureEndpoint ep : eps) {
                        if(endpointName.equalsIgnoreCase(ep.getName())) {
                            //edit case
                            continue;
                        }
                        if (ParserXMLUtility.isEpPortEqualOrInRange(ep.getPort(), localPort) ||
                                ParserXMLUtility.isEpPortEqualOrInRange(ep.getPrivatePort(), localPort)) {
                            isValid = false;
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            isValid = false;
        }
        return isValid;
    }

    /**
     * This method will check instance endpoint is valid or not
     * @param endpointName
     * @param localPort
     * @param pubPort
     * @return
     */
    private boolean isValidInstanceEp(String endpointName, String localPort, String pubPort) {
        boolean isValid = true;
        try {
            if(localPort.isEmpty() || pubPort.isEmpty() || localPort.contains("-")) {
                isValid = false;
            } else {
                // check for max range should be greater than or equal to min
                if(pubPort.contains("-")) {
                    isValid = ParserXMLUtility.isValidRange(pubPort);
                }
                if(isValid) {
                    //check that localPort should be unique in role
                    List<WindowsAzureEndpoint> eps = getEndpoints();
                    for (WindowsAzureEndpoint ep : eps) {
                        if(endpointName.equalsIgnoreCase(ep.getName())) {
                            //edit case
                            continue;
                        }
                        //Private port of instance endpoint  and input endpoint's private port can be same.
                        if (!ep.getEndPointType().equals(WindowsAzureEndpointType.Input) &&
                        		ParserXMLUtility.isEpPortEqualOrInRange(ep.getPrivatePort(), localPort)) {
                            isValid = false;
                            break;
                        }

                        if (ParserXMLUtility.isEpPortEqualOrInRange(ep.getPort(), localPort) ||
                                ParserXMLUtility.isEpPortEqualOrInRange(ep.getPort(), pubPort) ||
                                ParserXMLUtility.isEpPortEqualOrInRange(ep.getPrivatePort(), pubPort)) {
                            isValid = false;
                            break;
                        }
                    }
                    if (isValid) {
                        //validate public port in project
                        isValid = winProjMgr.isDupInputPubPort(pubPort, endpointName);
                    }
                }
            }
        } catch (Exception e) {
            isValid = false;
        }
        return isValid;
    }

    /**
     * Deletes the endpoint from WindowsAzureProjectManager.
     *
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    public void delete()
            throws WindowsAzureInvalidProjectOperationException {

        try {
            Vector<String> value = new Vector<String>();
            value.add(getName());

            // Delete from definition file
            Document doc = getWinProjMgr().getdefinitionFileDoc();
            String expr = String.format(WindowsAzureConstants.WR_NAME,
                    getName());
            ParserXMLUtility.deleteElement(doc, expr);

            // Delete from Congugration file
            doc = getWinProjMgr().getConfigFileDoc();
            expr = String.format(WindowsAzureConstants.ROLE_NAME, getName());
            ParserXMLUtility.deleteElement(doc, expr);

            // delete from package.xml
            doc = getWinProjMgr().getPackageFileDoc();
            expr = String.format(WindowsAzureConstants.WA_PACK_NAME, getName());
            ParserXMLUtility.deleteElement(doc, expr);
            // Delete folder from HD
            getWinProjMgr().mapActivity.put("delete", value);
            //Add remoteForward to another role
            winProjMgr.addRemoteForwarder();
            winProjMgr.roleList.remove(this);
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    WindowsAzureConstants.EXCP_DEL_ROLE, ex);
        }
    }

    /**
     * @return the username .
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    protected String getAccUsername()
            throws WindowsAzureInvalidProjectOperationException {
        try {
            if (this.accUsername.isEmpty()) {
                XPath xPath = XPathFactory.newInstance().newXPath();
                Document doc = getWinProjMgr().getConfigFileDoc();
                String expr = String.format(
                        WindowsAzureConstants.RA_ROLE_UNAME_VAL, getName());
                String username = xPath.evaluate(expr, doc);
                this.accUsername = username;
            }
            return this.accUsername;
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException(
                    WindowsAzureConstants.EXCP_GET_UNAME, e);
        }
    }

    /**
     * @param username the username to set
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    protected void setAccUsername(String username)
            throws WindowsAzureInvalidProjectOperationException {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = getWinProjMgr().getConfigFileDoc();
            String expr = String.format(
                    WindowsAzureConstants.RA_ROLE_UNAME, getName());
            Element eleSetUname = (Element) xPath.evaluate(expr,
                    doc, XPathConstants.NODE);
            if (eleSetUname == null) {
                eleSetUname = doc.createElement("Setting");
                eleSetUname.setAttribute(WindowsAzureConstants.ATTR_NAME,
                        WindowsAzureConstants.REMOTEACCESS_USERNAME);
                eleSetUname.setAttribute("value", username);
                expr = String.format(WindowsAzureConstants.CONFIG_ROLE_SET,
                        getName());
                Element eleConfigSettings = (Element) xPath.evaluate(expr,
                        doc, XPathConstants.NODE);
                eleConfigSettings.appendChild(eleSetUname);
            } else {
                eleSetUname.setAttribute("value", username);
            }
            this.accUsername = username;
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException(
                    WindowsAzureConstants.EXCP_SET_UNAME, e);
        }
    }

    /**
     * @return the accPassword .
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    protected String getAccPassword()
            throws WindowsAzureInvalidProjectOperationException {
        try {
            if (this.accPassword.isEmpty()) {
                XPath xPath = XPathFactory.newInstance().newXPath();
                Document doc = getWinProjMgr().getConfigFileDoc();
                String expr = String.format(
                        WindowsAzureConstants.RA_ROLE_PWD_VAL, getName());
                String password = xPath.evaluate(expr, doc);
                this.accPassword = password;
            }
            return this.accPassword;
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException(
                    WindowsAzureConstants.EXCP_GET_PWD, e);
        }
    }

    /**
     * @param password the password to set
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    protected void setAccPassword(String password)
            throws WindowsAzureInvalidProjectOperationException {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = getWinProjMgr().getConfigFileDoc();
            String expr = String.format(WindowsAzureConstants.RA_ROLE_PWD,
                    getName());
            Element eleSetPwd = (Element) xPath.evaluate(expr, doc,
                    XPathConstants.NODE);
            if (eleSetPwd == null) {
                eleSetPwd = doc.createElement("Setting");
                eleSetPwd.setAttribute(WindowsAzureConstants.ATTR_NAME,
                        WindowsAzureConstants.REMOTEACCESS_PASSWORD);
                eleSetPwd.setAttribute("value", password);
                expr = String.format(WindowsAzureConstants.CONFIG_ROLE_SET,
                        getName());
                Element eleConfigSettings = (Element) xPath.evaluate(expr, doc,
                        XPathConstants.NODE);
                eleConfigSettings.appendChild(eleSetPwd);
            } else {
                eleSetPwd.setAttribute("value", password);
            }
            this.accPassword = password;
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException(
                    WindowsAzureConstants.EXCP_SET_PWD, e);
        }
    }

    /**
     * @return the accExpiryDate
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    protected String getAccExpiryDate()
            throws WindowsAzureInvalidProjectOperationException {
        try {
            if (this.accExpiryDate.isEmpty()) {
                XPath xPath = XPathFactory.newInstance().newXPath();
                Document doc = getWinProjMgr().getConfigFileDoc();
                String expr = String.format(
                        WindowsAzureConstants.RA_ROLE_EXPIRY_VAL, getName());
                String expiry = xPath.evaluate(expr, doc);
                this.accExpiryDate = expiry;
            }
            return this.accExpiryDate;
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException(
                    WindowsAzureConstants.EXCP_GET_EDATE, e);
        }
    }

    /**
     * @param expiryDate the accExpiryDate to set
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    protected void setAccExpiryDate(String expiryDate)
            throws WindowsAzureInvalidProjectOperationException {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = getWinProjMgr().getConfigFileDoc();
            String expr = String.format(
                    WindowsAzureConstants.RA_ROLE_EXPIRY,
                    getName());
            Element eleSettingExpiry = (Element) xPath.evaluate(expr, doc,
                    XPathConstants.NODE);
            if (eleSettingExpiry == null) {
                eleSettingExpiry = doc.createElement("Setting");
                eleSettingExpiry.setAttribute(WindowsAzureConstants.ATTR_NAME,
                        WindowsAzureConstants.REMOTEACCESS_EXPIRY);
                eleSettingExpiry.setAttribute("value", expiryDate);
                expr = String.format(WindowsAzureConstants.CONFIG_ROLE_SET,
                        getName());
                Element eleConfigSettings = (Element) xPath.evaluate(expr,
                        doc, XPathConstants.NODE);
                eleConfigSettings.appendChild(eleSettingExpiry);
            } else {
                eleSettingExpiry.setAttribute("value", expiryDate);
            }
            this.accExpiryDate = expiryDate;
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException(
                    WindowsAzureConstants.EXCP_SET_EDATE, e);
        }
    }

    /**
     * @return the thumbprint .
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    protected String getThumbprint()
            throws WindowsAzureInvalidProjectOperationException {
        try {
            if (this.certThumbprint.isEmpty()) {
                XPath xPath = XPathFactory.newInstance().newXPath();
                Document doc = getWinProjMgr().getConfigFileDoc();
                String expr = String.format(
                        WindowsAzureConstants.RA_ROLE_TPRINT_TPRINT, getName());
                String thumbprint = xPath.evaluate(expr, doc);
                this.certThumbprint = thumbprint;
            }
            return this.certThumbprint;
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException(
                    WindowsAzureConstants.EXCP_GET_THUMBP, e);
        }
    }

    /**
     * @param thumbprint the thumbprint to set
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    protected void setThumbprint(String thumbprint)
            throws WindowsAzureInvalidProjectOperationException {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = getWinProjMgr().getConfigFileDoc();
            String expr = String.format(WindowsAzureConstants.RA_ROLE_FPRINT,
                    getName());
            Element eleCertificate = (Element) xPath.evaluate(expr, doc,
                    XPathConstants.NODE);
            if (eleCertificate == null) {
                eleCertificate = doc.createElement("Certificate");
                eleCertificate.setAttribute(WindowsAzureConstants.ATTR_NAME,
                        WindowsAzureConstants.REMOTEACCESS_FINGERPRINT);
                eleCertificate.setAttribute("thumbprint", thumbprint);
                eleCertificate.setAttribute("thumbprintAlgorithm", "sha1");
                expr = String.format(WindowsAzureConstants.CERT_ROLE,
                        getName());
                Element eleCertificates = (Element) xPath.evaluate(expr, doc,
                        XPathConstants.NODE);
                eleCertificates.appendChild(eleCertificate);
            } else {
                eleCertificate.setAttribute("thumbprint", thumbprint);
            }
            this.certThumbprint = thumbprint;
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException(
                    WindowsAzureConstants.EXCP_SET_THUMBP, e);
        }
    }

    /**
     * This API is for enabling debugging by creating all the necessary XML
     * markup if it’s not already there.
     * @param endpoint as debugging end point
     * @param startSuspended to indicate the debugging is in
     * suspended mode or not
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    protected void enableDebugging(WindowsAzureEndpoint endpoint,
            Boolean startSuspended)
                    throws WindowsAzureInvalidProjectOperationException {
        if ((endpoint == null) || startSuspended == null) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants.INVALID_ARG);
        }
        try {
            String existingStr = getRuntimeEnv(WindowsAzureConstants.JAVA_OPTIONS_ENV_VAR);
            String newDbg = getDebugStrWithPortAndSusMode(endpoint.
                    getPrivatePort(), startSuspended);
            String value = changeJavaOptionsVal(existingStr, newDbg);
            setRuntimeEnv(WindowsAzureConstants.JAVA_OPTIONS_ENV_VAR, value);


            //var.setAttribute("value", value);
            getRuntimeEnv().put(WindowsAzureConstants.JAVA_OPTIONS_ENV_VAR, value);
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    WindowsAzureConstants.EXCP, ex);
        }
    }


    /**
     * This API on WindowsAzureRole class to disable debugging by removing the
     * entire –agentlib:jdwp=…  setting from the _JAVA_OPTIONS variable
     * but leaving others that the user may have put in there in place,
     * unless there are no other options specified in it,
     * then also removing the corresponding <Variable> element itself.
     * @throws WindowsAzureInvalidProjectOperationException .
     */

    protected void disableDebugging()
            throws WindowsAzureInvalidProjectOperationException {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = getWinProjMgr().getPackageFileDoc();
            String expr = String.format(WindowsAzureConstants.WA_PACK_SENV_NAME,
                    getName(), WindowsAzureConstants.JAVA_OPTIONS_ENV_VAR);
            Element var = (Element) xPath.evaluate(expr, doc,
                    XPathConstants.NODE);

            if (var != null) {
                String existingStr = var.getAttribute("value");
                //remove -agentlib option from java_option
                String newVal = changeJavaOptionsVal(existingStr, "");
                //if no other options specified remove variable tab
                if (newVal.isEmpty()) {
                    var.getParentNode().removeChild(var);
                    getRuntimeEnv().remove(WindowsAzureConstants.JAVA_OPTIONS_ENV_VAR);
                } else {
                    var.setAttribute("value", newVal);
                    getRuntimeEnv().put(WindowsAzureConstants.JAVA_OPTIONS_ENV_VAR, newVal);
                }
            }

        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    WindowsAzureConstants.EXCP, ex);
        }

    }


    /**
     * This API is for determines whether debugging has been enabled. Returns
     * True by testing _JAVA_OPTIONS for the presence of:
     * the “-agentlib:jdwp=“ option setting,
     * AND the “transport=dt_socket” subsetting inside it
     * AND the “server=y” subsetting
     * Else False.
     * @return isEbabled .
     */
    protected Boolean getDebuggingEnabled() {
        //=“-agentlib:jdwp=transport=dt_socket,server=y,address=8081,suspend=n
        Boolean isEnabled = false;
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            String agentLib = "-agentlib:jdwp=";
            Document doc = getWinProjMgr().getPackageFileDoc();
            String expr = String.format(WindowsAzureConstants.WA_PACK_SENV_NAME,
                    getName(), WindowsAzureConstants.JAVA_OPTIONS_ENV_VAR) + "/@value";
            String existingStr = xPath.evaluate(expr, doc);
            String dbuStr = getDebugString(existingStr);

            if (dbuStr.startsWith(agentLib)) {
                dbuStr = dbuStr.substring(agentLib.length(), dbuStr.length());
                String[] split = dbuStr.split(",");
                List<String> argList = new ArrayList<String>();
                for (String str : split) {
                    argList.add(str);
                }
                if ((argList.contains("transport=dt_socket"))
                        && argList.contains("server=y")) {
                    isEnabled = true;
                }
            }
        } catch (Exception ex) {
            isEnabled = false;
        }
        return isEnabled;
    }

    /**
     * This API is to return the WindowsAzureEndpoint object whose local port is
     * the same as the port inside the address subsetting form the
     * _JAVA_OPTIONS variable’s -agentlib:jdwp setting.
     * @return WindowsAzureEndpoint .
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    public WindowsAzureEndpoint getDebuggingEndpoint()
            throws WindowsAzureInvalidProjectOperationException {
        try {
            String port = "";
            String agentLib = "-agentlib:jdwp=";
            WindowsAzureEndpoint endPt = null;

            if (getDebuggingEnabled()) {
                XPath xPath = XPathFactory.newInstance().newXPath();
                Document doc = getWinProjMgr().getPackageFileDoc();
                String expr = String.format(WindowsAzureConstants.WA_PACK_SENV_NAME,
                        getName(), WindowsAzureConstants.JAVA_OPTIONS_ENV_VAR) + "/@value";
                String val = xPath.evaluate(expr, doc);

                if (val.isEmpty()) {
                    throw new WindowsAzureInvalidProjectOperationException(
                            WindowsAzureConstants.DBG_NOT_ENABLED);
                }
                String dbgStr = getDebugString(val);

                // find debugging port number
                if (dbgStr.startsWith(agentLib)) {
                    dbgStr = dbgStr.substring(agentLib.length(),
                            dbgStr.length());
                    String[] split = dbgStr.split(",");
                    for (String str : split) {
                        if (str.startsWith("address=")) {
                            String[] add = str.split("=");
                            port = add[1];
                            break;
                        }
                    }
                }

                // find endpoint with corresponding port
                List<WindowsAzureEndpoint> epList = getEndpoints();
                for (WindowsAzureEndpoint waEndpt : epList) {
                    if (port.equals(waEndpt.getPrivatePort())) {
                        endPt = waEndpt;
                        break;
                    }
                }
                //if port number does not match with any endpoint,
                //should throw an exception
                if (endPt == null) {
                    throw new WindowsAzureInvalidProjectOperationException(
                            "Port specified in debugging is " +
                            "not a valid port for any endpoint");
                }
            }
            return endPt;
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    WindowsAzureConstants.EXCP, ex);
        }

    }

    /**
     * This API sets the local port of the WindowsAzureEndpoint object as the
     * address subsetting of the –agentlib:jdwp setting.
     * Throws an exception if debugging is not enabled.
     * @param endPoint .
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    public void setDebuggingEndpoint(WindowsAzureEndpoint endPoint)
            throws WindowsAzureInvalidProjectOperationException {
        if (endPoint == null) {
            // when endpoint is null, it means the debugging
            disableDebugging();
        } else {

            try {

                //checkDebugging is enabled or not
                if (!getDebuggingEnabled()) {
                    Boolean startSuspended = false;
                    enableDebugging(endPoint, startSuspended);
                }

                String port = endPoint.getPrivatePort();
                String agentLib = "-agentlib:jdwp=";
                XPath xPath = XPathFactory.newInstance().newXPath();
                Document doc = getWinProjMgr().getPackageFileDoc();
                String expr = String.format(WindowsAzureConstants.WA_PACK_SENV_NAME,
                        getName(), WindowsAzureConstants.JAVA_OPTIONS_ENV_VAR) + "/@value";
                String existingStr = xPath.evaluate(expr, doc);
                String dbgStr = getDebugString(existingStr);

                if (dbgStr.isEmpty()) {
                    throw new WindowsAzureInvalidProjectOperationException(
                            WindowsAzureConstants.DBG_NOT_ENABLED);
                }

                // replace debugging port number in string and create new string
                dbgStr = dbgStr.substring(agentLib.length(), dbgStr.length());
                String newVal = agentLib;
                String[] split = dbgStr.split(",");
                for (int i = 0; i < split.length; i++) {
                    String str = split[i];
                    if (str.startsWith("address=")) {
                        str = "address=" + port;
                    }
                    newVal = newVal.concat(str);
                    if (i != (split.length - 1)) {
                        newVal = newVal.concat(",");
                    }
                }

                //replace new string in xml
                expr = String.format(WindowsAzureConstants.WA_PACK_SENV_NAME,
                        getName(), WindowsAzureConstants.JAVA_OPTIONS_ENV_VAR);
                Element var = (Element) xPath.evaluate(expr, doc,
                        XPathConstants.NODE);
                String value = changeJavaOptionsVal(existingStr, newVal);
                var.setAttribute("value", value);

                getRuntimeEnv().put(WindowsAzureConstants.JAVA_OPTIONS_ENV_VAR, value);

            } catch (Exception ex) {
                throw new WindowsAzureInvalidProjectOperationException(
                        WindowsAzureConstants.EXCP, ex);
            }
        }
    }

    /**
     * This API on WindowsAzureRole class to find whether the JVM should
     * start in suspended mode. This corresponds to the “suspend=y|n”
     * subsetting of the –agentlib:jdwp setting.
     * Throws an exception if debugging is not enabled.
     * @return Boolean status
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    public Boolean getStartSuspended()
            throws WindowsAzureInvalidProjectOperationException {
        try {
            Boolean status = false;
            String agentLib = "-agentlib:jdwp=";
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = getWinProjMgr().getPackageFileDoc();
            String expr = String.format(WindowsAzureConstants.WA_PACK_SENV_NAME,
                    getName(), WindowsAzureConstants.JAVA_OPTIONS_ENV_VAR) + "/@value";
            String existingStr = xPath.evaluate(expr, doc);
            String dbgStr = getDebugString(existingStr);

            if (dbgStr.isEmpty()) {
                throw new WindowsAzureInvalidProjectOperationException(
                        WindowsAzureConstants.DBG_NOT_ENABLED);
            }

            // find debugging status
            if (dbgStr.startsWith(agentLib)) {
                dbgStr = dbgStr.substring(agentLib.length(), dbgStr.length());
                String[] split = dbgStr.split(",");
                for (String str : split) {
                    if (str.startsWith("suspend=")) {
                        String[] syspend = str.split("=");
                        if (syspend[1].equalsIgnoreCase("y")) {
                            status = true;
                            break;
                        }
                    }
                }
            }
            return status;
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    WindowsAzureConstants.EXCP, ex);
        }
    }

    /**
     * This API on WindowsAzureRole class to specify whether the JVM should
     * start in suspended mode. This corresponds to the “suspend=y|n”
     * subsetting of the –agentlib:jdwp setting.
     * Throws an exception if debugging is not enabled.
     * @param status .
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    public void setStartSuspended(Boolean status)
            throws WindowsAzureInvalidProjectOperationException {
        if (null == status) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants.INVALID_ARG);
        }
        try {
            String agentLib = "-agentlib:jdwp=";
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = getWinProjMgr().getPackageFileDoc();
            String expr = String.format(WindowsAzureConstants.WA_PACK_SENV_NAME,
                    getName(), WindowsAzureConstants.JAVA_OPTIONS_ENV_VAR) + "/@value";
            String existingStr = xPath.evaluate(expr, doc);
            String dbgStr = getDebugString(existingStr);

            if (dbgStr.isEmpty()) {
                throw new WindowsAzureInvalidProjectOperationException(
                        WindowsAzureConstants.DBG_NOT_ENABLED);
            }

            // replace debugging status in string and create new string
            dbgStr = dbgStr.substring(agentLib.length(), dbgStr.length());
            String newVal = agentLib;
            String[] split = dbgStr.split(",");
            for (int i = 0; i < split.length; i++) {
                String str = split[i];
                if (str.startsWith("suspend=")) {
                    if (status) {
                        str = "suspend=y";
                    } else {
                        str = "suspend=n";
                    }
                }
                newVal = newVal.concat(str);
                if (i != (split.length - 1)) {
                    newVal = newVal.concat(",");
                }
            }
            //replace new string in xml
            expr = String.format(WindowsAzureConstants.WA_PACK_SENV_NAME,
                    getName(), WindowsAzureConstants.JAVA_OPTIONS_ENV_VAR);
            Element var = (Element) xPath.evaluate(expr, doc,
                    XPathConstants.NODE);
            String value = changeJavaOptionsVal(existingStr, newVal);
            var.setAttribute("value", value);
            getRuntimeEnv().put(WindowsAzureConstants.JAVA_OPTIONS_ENV_VAR, value);
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    WindowsAzureConstants.EXCP, ex);
        }
    }


    /**
     * This method is to create debug string using give port
     * and suspended mode status.
     * @return String dbgStr
     * @param port .
     * @param suspendMode status.
     */
    protected String getDebugStrWithPortAndSusMode(String port,
            Boolean suspendMode) {
        String str = WindowsAzureConstants.DBG_STR;
        str = str.concat(port).concat(",suspend=");
        if (suspendMode) {
            str = str.concat("y");
        } else {
            str = str.concat("n");
        }
        return str;
    }
    protected String changeJavaOptionsVal(String existingStr, String newDbg) {
        String newStr = newDbg;
        if (!existingStr.isEmpty()) {
            newStr = "";
            boolean isExist = false;
            //Already exist replace the value of agentlib:jdwp...
            //else append the value
            String[] split = existingStr.split(" ");
            for (int i = 0; i < split.length; i++) {
                String string = split[i];
                if (string.startsWith("-agentlib:jdwp=")) {
                    newStr = newStr.concat(newDbg);
                    isExist = true;
                } else {
                    newStr = newStr.concat(string);
                }
                newStr = newStr.concat(" ");
            }
            if (!isExist) {
                newStr = newStr.concat(newDbg);
            }
        }
        return newStr.trim();
    }

    protected String getDebugString(String existingStr) {
        String dbgStr = "";
        if (!existingStr.isEmpty()) {
            String[] split = existingStr.split(" ");
            for (int i = 0; i < split.length; i++) {
                String string = split[i];
                if (string.startsWith("-agentlib:jdwp=")) {
                    dbgStr = string;
                    break;
                }
            }
        }
        return dbgStr.trim();
    }


    /**
     * This API is to return a name-value list of all the variables.
     * @return map.
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    public Map<String, String> getRuntimeEnv()
            throws WindowsAzureInvalidProjectOperationException {

        try {
            if(envVarMap.isEmpty()) {
                XPath xPath = XPathFactory.newInstance().newXPath();
                Document doc = getWinProjMgr().getPackageFileDoc();
                String expr = String.format(WindowsAzureConstants.WA_PACK_STARTUPENV,
                        getName());
                NodeList varList = (NodeList) xPath.evaluate(expr, doc,
                        XPathConstants.NODESET);
                for (int i = 0; i < varList.getLength(); i++) {
                    Element var =  (Element) varList.item(i);
                    envVarMap.put(var.getAttribute(WindowsAzureConstants.ATTR_NAME),
                            var.getAttribute(WindowsAzureConstants.ATTR_VALUE));
                }
            }
        } catch (Exception ex) {
            new WindowsAzureInvalidProjectOperationException(
                    "Exception while getting RunTinme env variables: ", ex);
        }
        return envVarMap;
    }

    /**
     * This API is to return a name list of all the local storage variables.
     * @return map.
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    public List<String> getLsEnv()
            throws WindowsAzureInvalidProjectOperationException {
        try {
            if (lsVarList.isEmpty()) {
                XPath xPath = XPathFactory.newInstance().newXPath();
                Document doc = getWinProjMgr().getdefinitionFileDoc();
                String expr = String.format(WindowsAzureConstants.VARIABLE,
                        getName());
                NodeList varList = (NodeList) xPath.evaluate(expr, doc,
                        XPathConstants.NODESET);
                for (int i = 0; i < varList.getLength(); i++) {
                    Element var =  (Element) varList.item(i);
                    lsVarList.add(var.getAttribute(WindowsAzureConstants.
                            ATTR_NAME));
                }
            }
        } catch (Exception ex) {
            new WindowsAzureInvalidProjectOperationException(
                    "Exception while getting RunTinme env variables: ", ex);
        }
        return lsVarList;
    }


    /**
     * This API is to return the value of a specific variable.
     * @param name .
     * @return value.
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    public String getRuntimeEnv(String name)
            throws WindowsAzureInvalidProjectOperationException {
        String value = "";
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants.INVALID_ARG);
        }
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = getWinProjMgr().getPackageFileDoc();
            String expr = String.format(WindowsAzureConstants
                    .WA_PACK_SENV_NAME,
                    getName(), name)  + "/@value";
            value = xPath.evaluate(expr, doc);
        } catch (Exception ex) {
            new WindowsAzureInvalidProjectOperationException(
                    "Exception while getting RunTinme env variables: ", ex);
        }
        return value;
    }



    /**
     * This API sets the value of a specific variable.
     * @param name .
     * @param value .
     * @throws WindowsAzureInvalidProjectOperationException .
     */

    public void setRuntimeEnv(String name, String value) throws WindowsAzureInvalidProjectOperationException {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants.INVALID_ARG);
        }
        try {
            Document doc = getWinProjMgr().getPackageFileDoc();
            XPath xPath = XPathFactory.newInstance().newXPath();
            //check first the env is already present, if yes. edit the same
            String env = String.format(WindowsAzureConstants.WA_PACK_SENV_NAME, getName(), name);
            Element envNode = (Element) xPath.evaluate(env,doc, XPathConstants.NODE);

            if(envNode == null) {
                String parentNode = String.format(
                        WindowsAzureConstants.WA_PACK_ROLE, getName());
                Element role = (Element)xPath.evaluate(parentNode, doc, XPathConstants.NODE);
                envNode = doc.createElement("startupenv");
                envNode.setAttribute(WindowsAzureConstants.ATTR_NAME, name);
                role.appendChild(envNode);
            }
            envNode.setAttribute(WindowsAzureConstants.ATTR_VALUE, value);
            getRuntimeEnv().put(name, value);
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    WindowsAzureConstants.EXCP, ex);
        }
    }
    /**
     * This API rename a specific variable.
     * @param oldName .
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    public void renameRuntimeEnv(String oldName, String newName)
            throws WindowsAzureInvalidProjectOperationException {
        if ((null == oldName) || oldName.isEmpty() || (newName == null)) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants.INVALID_ARG);
        }
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = getWinProjMgr().getPackageFileDoc();
            String expr = String.format(WindowsAzureConstants
                    .WA_PACK_SENV_NAME,
                    getName(), oldName);
            Element var = (Element) xPath.evaluate(expr, doc,
                    XPathConstants.NODE);
            if (var == null) {
                throw new WindowsAzureInvalidProjectOperationException(
                        oldName + " variable does not exist");
            }
            if (newName.isEmpty()) {
                getRuntimeEnv().remove(oldName);
                var.getParentNode().removeChild(var);
            } else {
                String val = getRuntimeEnv().get(oldName);
                var.setAttribute("name", newName);
                getRuntimeEnv().remove(oldName);
                getRuntimeEnv().put(newName, val);
            }
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    WindowsAzureConstants.EXCP, ex);
        }
    }

    /**
     * This API is for getting to the local storage.
     * @return a name-object list of all the local storage resources.
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    public Map<String, WindowsAzureLocalStorage> getLocalStorage()
            throws WindowsAzureInvalidProjectOperationException {

        try {
            if(locStoMap.isEmpty()) {
                XPath xPath = XPathFactory.newInstance().newXPath();
                Document doc = getWinProjMgr().getdefinitionFileDoc();
                String expr = String.format(WindowsAzureConstants.LOCAL_STORAGE,
                        getName());
                NodeList locStoList = (NodeList) xPath.evaluate(expr, doc,
                        XPathConstants.NODESET);
                for (int i = 0; i < locStoList.getLength(); i++) {
                    Element locSto =  (Element) locStoList.item(i);
                    locStoMap.put(locSto.getAttribute(
                            WindowsAzureConstants.ATTR_NAME),
                            getWinLocStObj(locSto));
                }
            }
        } catch (Exception ex) {
            new WindowsAzureInvalidProjectOperationException(
                    "Exception while getting RunTinme env variables: ", ex);
        }
        return locStoMap;
    }


    private WindowsAzureLocalStorage getWinLocStObj(Element locSto)
            throws WindowsAzureInvalidProjectOperationException {
        WindowsAzureLocalStorage winLocSt = null;
        if (locSto != null) {
            winLocSt = new WindowsAzureLocalStorage(winProjMgr, this);
            winLocSt.setName(locSto.getAttribute(
                    WindowsAzureConstants.ATTR_NAME));
            winLocSt.setSize(Integer.parseInt(locSto.getAttribute(
                    WindowsAzureConstants.ATTR_SIZEINMB)));
            winLocSt.setCleanOnRecycle(Boolean.parseBoolean(locSto.getAttribute(
                    WindowsAzureConstants.ATTR_CLE_ON_ROLE_RECYCLE)));
            String pathEnv = winLocSt.getPathEnv();
            if (pathEnv != null) {
                winLocSt.setPathEnv(pathEnv);
            }
        }
        return winLocSt;
    }

    /**
     * This API is for getting object of WindowsAzureLocalStorage
     * @return a name-object list of all the local storage resources.
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    public WindowsAzureLocalStorage getLocalStorage(String name)
            throws WindowsAzureInvalidProjectOperationException {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants.INVALID_ARG);
        }
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = getWinProjMgr().getdefinitionFileDoc();
            String expr = String.format(WindowsAzureConstants.LS_NAME,
                    getName(), name);
            Element locSt = (Element) xPath.evaluate(expr, doc,
                    XPathConstants.NODE);
            return getWinLocStObj(locSt);
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    "Exception while getting Local Storage: ", ex);
        }
    }

    public WindowsAzureLocalStorage addLocalStorage(String name, int size,
            boolean cleanOnRecycle, String pathEnv)
                    throws WindowsAzureInvalidProjectOperationException {

        if ((name == null || name.isEmpty()) ||
                (size < 1) || (pathEnv == null) || pathEnv.isEmpty()) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants.INVALID_ARG);
        }
        WindowsAzureLocalStorage winLocSt = null;
        try {

            Document doc = getWinProjMgr().getdefinitionFileDoc();
            XPath xPath = XPathFactory.newInstance().newXPath();

            // Check <LocalResources>,  if not find create new
            //Find RunTime tag, if not present create new
            String expr = String.format(WindowsAzureConstants.LOCAL_RESOURCES,
                    getName());
            Element eleLocRes = (Element) xPath.evaluate(expr, doc,
                    XPathConstants.NODE);

            if (eleLocRes ==  null) {
                String objName = getName();
                eleLocRes = doc.createElement("LocalResources");
                //Append <LocalResources> to <WorkerRole>
                expr = String.format(WindowsAzureConstants.WR_NAME,
                        objName);
                Node role = (Node) xPath.evaluate(expr, doc,
                        XPathConstants.NODE);
                role.appendChild(eleLocRes);
            }

            Element eleLocSto = doc.createElement("LocalStorage");
            eleLocSto.setAttribute(WindowsAzureConstants.ATTR_NAME, name);
            eleLocSto.setAttribute(
                    WindowsAzureConstants.ATTR_CLE_ON_ROLE_RECYCLE,
                    String.valueOf(cleanOnRecycle));
            eleLocSto.setAttribute(WindowsAzureConstants.ATTR_SIZEINMB,
                    String.valueOf(size));
            eleLocRes.appendChild(eleLocSto);
            winLocSt = new WindowsAzureLocalStorage(winProjMgr, this);
            winLocSt.setName(eleLocSto.getAttribute(WindowsAzureConstants.ATTR_NAME));
            winLocSt.setSize(Integer.parseInt(eleLocSto.getAttribute(
                    WindowsAzureConstants.ATTR_SIZEINMB)));
            winLocSt.setCleanOnRecycle(Boolean.parseBoolean(eleLocSto.getAttribute(
                    WindowsAzureConstants.ATTR_CLE_ON_ROLE_RECYCLE)));
            // Changes inside <WorkerRole> element
            winLocSt.setPathEnv(pathEnv);
            locStoMap.put(name, winLocSt);
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    "Exception while adding local storage: ", ex);
        }
        return winLocSt;
    }


    /** This API is for enabling or disabling session affinity */
    public void setSessionAffinityInputEndpoint(WindowsAzureEndpoint value)
                        throws WindowsAzureInvalidProjectOperationException {
       if(value != null ) {
           configureSessionAffinity(value);
       }
       else {
           disableSessionAffinity();
       }

    }
    /** This API is for configuring  session affinity */
    private void configureSessionAffinity(WindowsAzureEndpoint windowsAzureEndpoint)
    throws WindowsAzureInvalidProjectOperationException {
        // Check if end point is input end point or not
        if(windowsAzureEndpoint == null || (windowsAzureEndpoint.getEndPointType() != WindowsAzureEndpointType.Input)) {
            throw new WindowsAzureInvalidProjectOperationException(WindowsAzureConstants.EXCP_EMPTY_OR_INVALID_ENDPOINT);
        }


        // Check if sessionAffinity is already enabled, if yes then throw error
        WindowsAzureEndpoint saEndpt = getSessionAffinityInputEndpoint();
        if(saEndpt != null && saEndpt.getName().equalsIgnoreCase(windowsAzureEndpoint.getName())) {
            throw new WindowsAzureInvalidProjectOperationException(WindowsAzureConstants.EXCP_SA_ENABLED);
        }
        // Remove previous configuration except file copy
        if(saEndpt != null ) {
            removeSASettingsFromDefDoc();
            removeSASettingsFromPkgDoc();
        }

        // Generate port and name for SA internal end point.
        int iisArrPort = getSessionAffinityPort();
        String saInternalEndPointName = generateSAInternalEndPointName(windowsAzureEndpoint.getName());
        int stepsCompleted = -1 ;

        try{
            // update or create properties in package.xml
            addSAPropertiesInPackage(windowsAzureEndpoint.getName(),saInternalEndPointName);
            stepsCompleted = WindowsAzureConstants.PACKAGE_DOC_SA_PROPERTIES;

            // add SA settings in service definition file
            addSASettingsInSvcDef(windowsAzureEndpoint,saInternalEndPointName,iisArrPort);
            stepsCompleted = WindowsAzureConstants.DEFINITION_DOC_SA_CHANGES;

            // create SA configuration files
            Vector<String> saInfo = new Vector<String>();
            saInfo.add(this.getName());
            if(getWinProjMgr().mapActivity.get("addSAFilesForRole") != null)
                getWinProjMgr().mapActivity.remove("addSAFilesForRole");
            if(getWinProjMgr().mapActivity.get("delSAFilesForRole") != null)
                getWinProjMgr().mapActivity.remove("delSAFilesForRole");
            getWinProjMgr().mapActivity.put("addSAFilesForRole", saInfo);
            stepsCompleted = WindowsAzureConstants.SA_FILES_COPIED;

        }catch(Exception e) {
            handleRollback(stepsCompleted);
            throw new WindowsAzureInvalidProjectOperationException(e.getMessage(),e);
         }

    }

    /** Adds or updates properties in package.xml. */
    private void addSAPropertiesInPackage(String inputEndPointName, String internalEndPointName)
    throws WindowsAzureInvalidProjectOperationException {
        Document packageFileDoc = getWinProjMgr().getPackageFileDoc();

        if(packageFileDoc != null ){
            String nodeExpr       =  String.format(WindowsAzureConstants.SA_INPUT_ENDPOINT,  this.getName());
            String parentNodeExpr =  WindowsAzureConstants.PROJ_PROPERTY;

            HashMap<String,String> nodeAttribites = new HashMap<String, String>();
            nodeAttribites.put(WindowsAzureConstants.ATTR_NAME,String.format(WindowsAzureConstants.SA_INPUT_ENDPOINT_NAME_PROP,this.getName()));
            nodeAttribites.put(WindowsAzureConstants.ATTR_VALUE, inputEndPointName);
            updateOrCreateElement(packageFileDoc,nodeExpr,parentNodeExpr,WindowsAzureConstants.PROJ_PROPERTY_ELEMENT_NAME,false,nodeAttribites);

            nodeExpr = String.format(WindowsAzureConstants.SA_INTERNAL_ENDPOINT,this.getName());
            nodeAttribites.clear();
            nodeAttribites.put(WindowsAzureConstants.ATTR_NAME, String.format(WindowsAzureConstants.SA_INTERNAL_ENDPOINT_NAME_PROP,this.getName()));
            nodeAttribites.put(WindowsAzureConstants.ATTR_VALUE, internalEndPointName);
            updateOrCreateElement(packageFileDoc,nodeExpr,parentNodeExpr,WindowsAzureConstants.PROJ_PROPERTY_ELEMENT_NAME,false,nodeAttribites);
        }
    }

    /** This method adds Session Affinity settings in service definition */
    private void addSASettingsInSvcDef(WindowsAzureEndpoint inpEndPt,String intEndPt, int iisArrPort)
    throws WindowsAzureInvalidProjectOperationException {
        // Add startup task entry
        Document definitionFiledoc  = getWinProjMgr().getdefinitionFileDoc();
        String   nodeExpr           = String.format(WindowsAzureConstants.STARTUP_WR_NAME,getName());
        String   parentNodeExpr     = String.format(WindowsAzureConstants.WR_NAME, this.getName()) ;
        updateOrCreateElement(definitionFiledoc,nodeExpr,parentNodeExpr,WindowsAzureConstants.DEF_FILE_STARTUP_ELEMENT_NAME,true,null);

        String taskNodeExpr     = String.format(WindowsAzureConstants.STARTUP_TASK_CMD,this.getName(),WindowsAzureConstants.TASK_CMD_VALUE
                                    ,inpEndPt.getName(),intEndPt);
        HashMap<String,String> nodeAttributes = new HashMap<String, String>();
        nodeAttributes.put("commandLine", String.format(WindowsAzureConstants.TASK_CMD_VALUE, inpEndPt.getName(),intEndPt));
        nodeAttributes.put("executionContext", "elevated");
        nodeAttributes.put("taskType", "simple");
        Element element = updateOrCreateElement(definitionFiledoc,taskNodeExpr,nodeExpr,WindowsAzureConstants.DEF_FILE_TASK_ELEMENT_NAME,true,nodeAttributes);

        //Add environment element
        element = createElement(definitionFiledoc,null,element,WindowsAzureConstants.DEF_FILE_ENV_NAME,false,null);

        //Add variable element
        nodeAttributes.clear();
        nodeAttributes.put("name", "EMULATED");
        element = createElement(definitionFiledoc,null,element,WindowsAzureConstants.DEF_FILE_VAR_ELE_NAME,false,nodeAttributes);

        //Add xpath expression role instance value
        nodeAttributes.clear();
        nodeAttributes.put("xpath", "/RoleEnvironment/Deployment/@emulated");
        element = createElement(definitionFiledoc,null,element,WindowsAzureConstants.DEF_FILE_ENV_RIV_NAME,false,nodeAttributes);


        // Add comments to startup task element to warn the user not to insert any task before this.
        createCommentNode(WindowsAzureConstants.STARTUP_TASK_COMMENTS,definitionFiledoc,nodeExpr);

        // Add SA input end point and SA internal end point
        String inputEndPointLocalPort = inpEndPt.getPrivatePort();
        nodeExpr  = String.format(WindowsAzureConstants.INPUT_ENDPOINT, getName(), inpEndPt.getName());
        int index = winEndPtList.indexOf(inpEndPt);
        winEndPtList.get(index).setPrivatePort(String.valueOf(iisArrPort));
        addEndpoint(intEndPt, WindowsAzureEndpointType.Internal, inputEndPointLocalPort, "");

    }

    /** Adds comments as a first node */
    private void createCommentNode(String startupTaskComments,Document doc,String nodeExpr)
    throws WindowsAzureInvalidProjectOperationException {
        try {
            Comment taskComment = doc.createComment(WindowsAzureConstants.STARTUP_TASK_COMMENTS);
            XPath xPath         = XPathFactory.newInstance().newXPath();
            Element element     = (Element) xPath.evaluate(nodeExpr, doc,XPathConstants.NODE);
            element.insertBefore(taskComment, element != null? element.getFirstChild():null);
        }catch(Exception e ){
            throw new WindowsAzureInvalidProjectOperationException(WindowsAzureConstants.EXCP_COMMENT_NODE,e);
        }
    }

    /** This method changes session affinity end point info in start up task if there is a name change */
    private void changeEndPointInfoInStartupTask(String inputEndPointNAme, String internalEndPointName)
    throws WindowsAzureInvalidProjectOperationException {

            String taskNodeExpr = String.format(WindowsAzureConstants.STARTUP_TASK_STARTS_WITH,this.getName(),
                                  WindowsAzureConstants.TASK_CMD_ONLY);
            String   parentNodeExpr           = String.format(WindowsAzureConstants.STARTUP_WR_NAME,getName());
            Document definitionFiledoc  = getWinProjMgr().getdefinitionFileDoc();
            HashMap<String,String> nodeAttributes = new HashMap<String, String>();

            nodeAttributes.put("commandLine", String.format(WindowsAzureConstants.TASK_CMD_VALUE, inputEndPointNAme,internalEndPointName));
            nodeAttributes.put("executionContext", "elevated");
            nodeAttributes.put("taskType", "simple");
            updateOrCreateElement(definitionFiledoc,taskNodeExpr,parentNodeExpr,WindowsAzureConstants.DEF_FILE_TASK_ELEMENT_NAME,true,nodeAttributes);

    }

    /** API to handle rollback logic. */
    private void handleRollback(int stepsCompleted) {
        switch (stepsCompleted) {
            case WindowsAzureConstants.PACKAGE_DOC_SA_PROPERTIES:
               removeSASettingsFromPkgDoc();
               break;


            case WindowsAzureConstants.DEFINITION_DOC_SA_CHANGES:
                removeSASettingsFromDefDoc();
                removeSASettingsFromPkgDoc();
                break;

            case WindowsAzureConstants.SA_FILES_COPIED:
                removeSAFilesFromPrj();
                removeSASettingsFromDefDoc();
                removeSASettingsFromPkgDoc();
                break;

            default:
                break;
        }

    }
    /** Remove SA settings from service definition file */
    private void removeSASettingsFromDefDoc() {
        try {
            Document definitionFiledoc = getWinProjMgr().getdefinitionFileDoc();
            String expr = String.format(WindowsAzureConstants.STARTUP_TASK_STARTS_WITH,this.getName(),
                          WindowsAzureConstants.TASK_CMD_ONLY);
            ParserXMLUtility.deleteElement(definitionFiledoc, expr);

            // Delete Comments child node
            String nodeExpr = String.format(WindowsAzureConstants.STARTUP_WR_NAME,getName());
            deleteCommentNode(WindowsAzureConstants.STARTUP_TASK_COMMENTS,definitionFiledoc,nodeExpr) ;

            WindowsAzureEndpoint windowsAzureEndpoint = getSessionAffinityInternalEndpoint();
            String port = windowsAzureEndpoint.getPrivatePort();
            windowsAzureEndpoint.delete();

            windowsAzureEndpoint = getSessionAffinityInputEndpoint() ;
            windowsAzureEndpoint.setLocalPort(port);
        }catch (Exception e) {
                      // Don't throw error , ignore silently
        }


    }

    /** This API deletes comments node */
    private void deleteCommentNode(String startupTaskComments,Document definitionFiledoc, String nodeExpr)
    throws XPathExpressionException {

        XPath xPath = XPathFactory.newInstance().newXPath();
        Element element = (Element) xPath.evaluate(nodeExpr, definitionFiledoc,XPathConstants.NODE);

        NodeList nodeList = element.getChildNodes() ;
        if(nodeList.getLength() > 0) {
            for(int i = 0 ; i<nodeList.getLength() ; i++) {
                Node node = nodeList.item(i) ;
                if(node.getNodeType() == Node.COMMENT_NODE) {
                    Comment comment = (Comment)node;
                    if(startupTaskComments.equals(comment.getData())) {
                        comment.getParentNode().removeChild(node);
                        break;
                    }
                }

            }
        }
   }

    /** Removes SA files from project */
    private void removeSAFilesFromPrj() {
       try {
           Vector<String> saInfo = new Vector<String>();
           saInfo.add(this.getName());
            if(getWinProjMgr().mapActivity.get("addSAFilesForRole") != null) {
                getWinProjMgr().mapActivity.remove("addSAFilesForRole");
            }
            if(getWinProjMgr().mapActivity.get("delSAFilesForRole") != null) {
                getWinProjMgr().mapActivity.remove("delSAFilesForRole");
            }
            getWinProjMgr().mapActivity.put("delSAFilesForRole", saInfo);

        }catch(Exception e) {
             //Ignore exceptions
        }
    }
    /** Removes SA settings from package.xml */
    private void removeSASettingsFromPkgDoc() {
        try {
            Document packageFileDoc = getWinProjMgr().getPackageFileDoc();
            if(packageFileDoc != null ){
                String inputEndPointExpr =  String.format(WindowsAzureConstants.SA_INPUT_ENDPOINT,this.getName());
                ParserXMLUtility.deleteElement(packageFileDoc,inputEndPointExpr);

                String internalEndPointExpr = String.format(WindowsAzureConstants.SA_INTERNAL_ENDPOINT,this.getName());
                ParserXMLUtility.deleteElement(packageFileDoc,internalEndPointExpr);
             }
         }catch(Exception e) {
             //Die silently , there is nothing much that can be done at this point
         }
    }

    /** This API is for disabling session affinity */
    private void disableSessionAffinity() throws WindowsAzureInvalidProjectOperationException {
        removeSAFilesFromPrj();
        removeSASettingsFromDefDoc();
        removeSASettingsFromPkgDoc();
    }

    /** Get Session Affinity IIS port. By default this returns 31221 if not available then
        returns next available one */
    private int  getSessionAffinityPort() throws WindowsAzureInvalidProjectOperationException {
        int port = WindowsAzureConstants.IIS_ARR_PORT ;

        while(!winProjMgr.isValidPort(String.valueOf(port), WindowsAzureEndpointType.Input)){
                port++ ;
        }
        return port ;
    }

    /** This method suffixes a string to the name and checks if that name is available for endpoint */
    private String generateSAInternalEndPointName(String name) throws WindowsAzureInvalidProjectOperationException{
        StringBuilder saInternalEndPoint = new StringBuilder(name);
        saInternalEndPoint.append(WindowsAzureConstants.SA_INTERNAL_ENDPOINT_SUFFIX);

        int sufIncrement = 1 ;
        while(!winProjMgr.isAvailableRoleName(saInternalEndPoint.toString())) {
            saInternalEndPoint.append(sufIncrement);
            sufIncrement++ ;
        }
        return saInternalEndPoint.toString() ;
    }

    /** Generic API to update or create DOM elements */
    protected static Element updateOrCreateElement(Document doc ,String expr,String parentNodeExpr,String elementName,boolean firstChild,
                                        Map<String,String> attributes )
    throws WindowsAzureInvalidProjectOperationException {

        if(doc == null ) {
            throw new IllegalArgumentException(WindowsAzureConstants.INVALID_ARG);
        } else {
            try {
                XPath xPath = XPathFactory.newInstance().newXPath();
                Element element = null ;
                if(expr != null)
                    element = (Element) xPath.evaluate(expr, doc,XPathConstants.NODE);

                //If element doesn't exist create one
                if(element == null ){
                    element = doc.createElement(elementName);
                    Element parentElement = (Element) xPath.evaluate(parentNodeExpr, doc,XPathConstants.NODE);
                    if(firstChild) {
                        parentElement.insertBefore(element, parentElement != null? parentElement.getFirstChild():null);
                    } else {
                        parentElement.appendChild(element) ;
                    }
                }

                if (attributes != null && !attributes.isEmpty()) {
                    for (Map.Entry<String, String> attribute : attributes.entrySet()) {
                        element.setAttribute(attribute.getKey(), attribute.getValue());
                     }
                }
                return element ;
            }catch(Exception e ){
                throw new WindowsAzureInvalidProjectOperationException(WindowsAzureConstants.EXCP_UPDATE_OR_CREATE_ELEMENT, e);
            }
        }
    }


    /** Generic API to update or create DOM elements */
    private Element createElement(Document doc ,String expr,Element parentElement,String elementName,boolean firstChild,
                                        Map<String,String> attributes )
    throws WindowsAzureInvalidProjectOperationException {

        if(doc == null ) {
            throw new IllegalArgumentException(WindowsAzureConstants.INVALID_ARG);
        } else {
            try {
                XPath xPath = XPathFactory.newInstance().newXPath();
                Element element = null ;
                if(expr != null)
                    element = (Element) xPath.evaluate(expr, doc,XPathConstants.NODE);

                //If element doesn't exist create one
                if(element == null ){
                    element = doc.createElement(elementName);
                    if(firstChild) {
                        parentElement.insertBefore(element, parentElement != null? parentElement.getFirstChild():null);
                    } else {
                        parentElement.appendChild(element) ;
                    }
                }

                if (attributes != null && !attributes.isEmpty()) {
                    for (Map.Entry<String, String> attribute : attributes.entrySet()) {
                        element.setAttribute(attribute.getKey(), attribute.getValue());
                     }
                }
                return element ;
            }catch(Exception e ){
                throw new WindowsAzureInvalidProjectOperationException(WindowsAzureConstants.EXCP_UPDATE_OR_CREATE_ELEMENT, e);
            }
        }
    }




    /** Returns Session Affinity input endpoint */
    public WindowsAzureEndpoint getSessionAffinityInputEndpoint() throws WindowsAzureInvalidProjectOperationException {
        return getSessionAffinityEndpoint(WindowsAzureEndpointType.Input);
    }

    /** Returns session affinity internal endpoint */
    public WindowsAzureEndpoint getSessionAffinityInternalEndpoint() throws WindowsAzureInvalidProjectOperationException {
        return getSessionAffinityEndpoint(WindowsAzureEndpointType.Internal);
    }
    /** Returns endpoint associated with session affinity */
    private WindowsAzureEndpoint getSessionAffinityEndpoint(WindowsAzureEndpointType windowsAzureEndpointType)
    throws WindowsAzureInvalidProjectOperationException {

        WindowsAzureEndpoint windowsAzureEndpoint = null;
        try{
          Document packageFileDoc = getWinProjMgr().getPackageFileDoc();
          if (packageFileDoc != null) {
              XPath xPath = XPathFactory.newInstance().newXPath();
              String endPointExpr = null ;
              if (windowsAzureEndpointType == WindowsAzureEndpointType.Input) {
                  endPointExpr = String.format(WindowsAzureConstants.SA_INPUT_ENDPOINT, this.getName()) ;
              } else {
                  endPointExpr = String.format(WindowsAzureConstants.SA_INTERNAL_ENDPOINT, this.getName()) ;
              }
              Element propEndPoint = (Element) xPath.evaluate(endPointExpr, packageFileDoc,XPathConstants.NODE);
              if (propEndPoint != null ) {
                  windowsAzureEndpoint = getEndpoint(propEndPoint.getAttribute("value")) ;
              }
          }
        }catch(Exception e ){
            throw new WindowsAzureInvalidProjectOperationException("Internal error occured while fetching endpoint info from package.xml",e);
        }

        return windowsAzureEndpoint ;

    }

    /** This API changes session affinity settings in startup task and package.xml if session affinity end point changes */
    protected void reconfigureSessionAffinity(WindowsAzureEndpoint value,String newName) throws WindowsAzureInvalidProjectOperationException {
        if(value == null ) {
            throw new IllegalArgumentException(WindowsAzureConstants.EXCP_EMPTY_OR_INVALID_ENDPOINT);
        }

        String endPointName = null ;
        if(value.getEndPointType() == WindowsAzureEndpointType.Input) {
            endPointName = getSessionAffinityInternalEndpoint().getName() ;
        }
        else {
            endPointName = getSessionAffinityInputEndpoint().getName();
        }
        Document packageFileDoc = getWinProjMgr().getPackageFileDoc();
        try{
           if (packageFileDoc != null) {
                XPath xPath = XPathFactory.newInstance().newXPath();
                String endPointExpr = null ;
                // Change in startup task
                if (value.getEndPointType() == WindowsAzureEndpointType.Input) {
                    endPointExpr = String.format(WindowsAzureConstants.SA_INPUT_ENDPOINT, this.getName()) ;
                    changeEndPointInfoInStartupTask(newName,endPointName);
                } else if(value.getEndPointType() == WindowsAzureEndpointType.Internal) {
                    endPointExpr = String.format(WindowsAzureConstants.SA_INTERNAL_ENDPOINT, this.getName()) ;
                    changeEndPointInfoInStartupTask(endPointName,newName);
                }
                // Change in package.xml
                Element propEndPoint = (Element) xPath.evaluate(endPointExpr, packageFileDoc,XPathConstants.NODE);
                if (propEndPoint != null)
                    propEndPoint.setAttribute("value", newName);
            }
         } catch(Exception e){
             throw new WindowsAzureInvalidProjectOperationException(
                     WindowsAzureConstants.EXCP_SA_NAME_CHANGE,e);
         }


     }

    /**
     * Gets the list of components that are associated with this role.
     * @return list of components
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    public List <WindowsAzureRoleComponent> getComponents()
            throws WindowsAzureInvalidProjectOperationException {
        try {
            if (winCompList.isEmpty()) {
                XPath xPath = XPathFactory.newInstance().newXPath();
                Document doc = winProjMgr.getPackageFileDoc();
                String expr = String.format(WindowsAzureConstants.COMPONENT,
                        getName());
                NodeList components = (NodeList) xPath.evaluate(expr, doc,
                        XPathConstants.NODESET);
                if (components != null) {
                    for (int i = 0; i < components.getLength(); i++) {
                        Element compEle = (Element) components.item(i);
                        winCompList.add(getComponentObjFromEle(compEle));
                    }
                }
            }
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException(
                    "Exception in getComponents", e);
        }
        return winCompList;
    }


    /**
     * create new component element
     * @param name is importas attribute of component
     * @return component element
     * @throws WindowsAzureInvalidProjectOperationException
     */
    private Element addComponentElement() throws WindowsAzureInvalidProjectOperationException {
        try {
            Document doc = getWinProjMgr().getPackageFileDoc();
            XPath xPath = XPathFactory.newInstance().newXPath();
            String expr = String.format(WindowsAzureConstants.WA_PACK_NAME, getName());
            Element role = (Element) xPath.evaluate(expr, doc, XPathConstants.NODE);
            Element eleComp = doc.createElement("component");
            return (Element) role.appendChild(eleComp);
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    "Exception while creating new component", ex);
        }
    }

    /**
     * Create and associate component element with role
     * @param name
     * @return
     * @throws WindowsAzureInvalidProjectOperationException
     */
    public WindowsAzureRoleComponent addComponent(String attr_name, String value) throws WindowsAzureInvalidProjectOperationException {
        try {
            List<WindowsAzureRoleComponent> comps = getComponents();
            WindowsAzureRoleComponent winComp = null;
            Element comp =  addComponentElement();
            if (attr_name.equalsIgnoreCase(WindowsAzureConstants.ATTR_IMPORTAS)) {
                comp.setAttribute(WindowsAzureConstants.ATTR_IMPORTAS, value);
                winComp = new WindowsAzureRoleComponent(winProjMgr, this);
                winComp.setDeployname(value);
            } else {
                comp.setAttribute(WindowsAzureConstants.ATTR_IPATH, value);
                winComp = new WindowsAzureRoleComponent(winProjMgr, this);
                winComp.setImportPath(value);
            }
            comps.add(winComp);
            return winComp;
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    "Exception while adding component", ex);
        }
    }

    /**
     * Method to determine if a component is part of a server, JDK or application configuration
     * @param envName
     * @return
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    public boolean getIsEnvPreconfigured(String envName)
            throws WindowsAzureInvalidProjectOperationException {
        boolean preConFig = false;
        if (envName == null || envName.isEmpty()) {
            throw new IllegalArgumentException(WindowsAzureConstants.INVALID_ARG);
        }
        try {
            //Check all components associated with role
            List<WindowsAzureRoleComponent> compList = getComponents();
            for (int i = 0; i < compList.size(); i++) {
                WindowsAzureRoleComponent comp = compList.get(i);
                if (comp.getDeployDir().contains("%" + envName + "%") ||
                        comp.getImportPath().contains("%" + envName + "%")) {
                    preConFig = true;
                    break;
                }
            }
            if (!preConFig) {
                //check all env variables associated with role
               Map<String, String> envMap = getRuntimeEnv();
               Collection<String> col = envMap.values();
               for (Iterator<String> iterator = col.iterator(); iterator.hasNext();) {
                String envVal = iterator.next();
                if (envVal.contains("%" + envName + "%")) {
                    preConFig = true;
                    break;
                }
            }
            }
        } catch (Exception e) {
                throw new WindowsAzureInvalidProjectOperationException(
                        "Exception occured in", e);
        }
        return preConFig;
    }

    /**
     * Check the deployment name is valid or not
     * @param name
     * @return
     * @throws WindowsAzureInvalidProjectOperationException
     */
    public boolean isValidDeployName(String name)
            throws WindowsAzureInvalidProjectOperationException {
        boolean isValid = true;
        if ((name == null) || (name.isEmpty())) {
            isValid = false;
        } else {
            List<WindowsAzureRoleComponent> components = getComponents();
            for (Iterator<WindowsAzureRoleComponent> it = components.iterator(); it.hasNext();) {
                WindowsAzureRoleComponent comp = (WindowsAzureRoleComponent) it
                        .next();
                if (name.equalsIgnoreCase(comp.getDeployName())) {
                    isValid = false;
                    break;
                }
            }
        }
        return isValid;
    }

    /**
     *This API exposes the source path of the JDK if one is configured,
     *depending on the presence of Xpath component[@type=“jdk.deploy”] inside the
     *appropriate <workerrole> element in package.xml.
     *If no JDK configured, the API shall return NULL.
     *The return string comes directly from @importsrc
     * @return source path of JDK
     * @throws WindowsAzureInvalidProjectOperationException
     */
    public String getJDKSourcePath() throws WindowsAzureInvalidProjectOperationException {
        String sourcePath = null;
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
                Document doc = winProjMgr.getPackageFileDoc();
                String expr = String.format(WindowsAzureConstants.COMPONENT_TYPE,
                        getName(), "jdk.deploy");
                Element component = (Element) xPath.evaluate(expr, doc,
                        XPathConstants.NODE);
                if (component != null) {
                    sourcePath = component.getAttribute(WindowsAzureConstants.ATTR_IPATH);
                    if (sourcePath.isEmpty()) {
                        sourcePath = null;
                    }
                }
            } catch (Exception ex) {
                throw new WindowsAzureInvalidProjectOperationException(
                        "Exception while geting getJDKSourcePath", ex);
            }

        return sourcePath;
    }


    /**
     *This API sets the JDK source path, adding the JDK configuration from the
     *template file (e.g. componentsets.xml ) if it’s not in package.xml yet.
     *Only one JDK can be configured per role. When set to NULL, all <component>
     *and <startupenv> XML with @type starting with the substring “server.”
     *shall be removed from <workerrole>
     * @param path
     * @param templateFile
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    public void setJDKSourcePath(String path, File templateFile)
            throws WindowsAzureInvalidProjectOperationException {
        if (templateFile == null || !templateFile.exists()) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants.INVALID_ARG);
        }

        try {
            if (path == null) {
                remAssocFromRole("jdk.");
                remAssocFromRole("server.");
                return;
            }

            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = winProjMgr.getPackageFileDoc();
            String expr = String.format(WindowsAzureConstants.COMPONENT_TYPE,
                    getName(), "jdk.deploy");
            Element component = (Element) xPath.evaluate(expr, doc,
                    XPathConstants.NODE);
            List<WindowsAzureRoleComponent> comps = getComponents();
            if (component == null) {
                //parse template file and find componentset name
                Document compDoc = ParserXMLUtility.parseXMLFile(templateFile.getAbsolutePath());
                expr = String.format(WindowsAzureConstants.TEMP_COMPONENTSET, "JDK");
                Element compSet = (Element) xPath.evaluate(expr, compDoc, XPathConstants.NODE);
                if (compSet != null) {
                    NodeList nodelist = compSet.getChildNodes();
                    String parentNode = String.format(
                            WindowsAzureConstants.WA_PACK_ROLE, getName());
                    Element role = (Element) xPath.evaluate(parentNode, doc,
                            XPathConstants.NODE);
                    Node preNode = role.getFirstChild();
                    Element ele = null;
                    //Iterate on child nodes, if it is startupenv \ component add it in package.xml
                    for (int i = 0; i < nodelist.getLength(); i++) {
                        Node compNode = (Node) nodelist.item(i);
                        if (!compNode.hasAttributes()) {
                            continue;
                        }
                        Element compEle = (Element) compNode;
                        if (compEle.getNodeName().equalsIgnoreCase("startupenv")) {
                            ele = doc.createElement("startupenv");
                            ele.setAttribute("name", compEle.getAttribute("name"));
                            ele.setAttribute("value", compEle.getAttribute("value"));
                            ele.setAttribute("type", compEle.getAttribute("type"));
                            preNode = role.insertBefore(ele, preNode);
                            getRuntimeEnv().put(compEle.getAttribute("name"), compEle.getAttribute("value"));
                        } else if(compEle.getNodeName().equalsIgnoreCase("component")) {
                            ele = doc.createElement("component");
                            ele.setAttribute(WindowsAzureConstants.ATTR_IMPORTAS, compEle.getAttribute(WindowsAzureConstants.ATTR_IMPORTAS));
                            ele.setAttribute(WindowsAzureConstants.ATTR_IMETHOD, compEle.getAttribute(WindowsAzureConstants.ATTR_IMETHOD));
                            ele.setAttribute(WindowsAzureConstants.ATTR_IPATH, path);
                            ele.setAttribute(WindowsAzureConstants.ATTR_TYPE, compEle.getAttribute(WindowsAzureConstants.ATTR_TYPE));
                            preNode = role.insertBefore(ele, preNode);
                            WindowsAzureRoleComponent comp = new WindowsAzureRoleComponent(winProjMgr, this);
                            comp.setDeployname(compEle.getAttribute(WindowsAzureConstants.ATTR_IMPORTAS));
                            comp.setImportMethod(WindowsAzureRoleComponentImportMethod.valueOf(
                                    compEle.getAttribute(WindowsAzureConstants.ATTR_IMETHOD)));
                            comp.setImportPath(path);
                            comp.setType(compEle.getAttribute(WindowsAzureConstants.ATTR_TYPE));
                            comps.add(comp);
                        }

                        preNode = preNode.getNextSibling();
                    }
                }
            } else {
                for (int i=0; i<comps.size(); i++) {
                    if(comps.get(i).getType().equalsIgnoreCase("jdk.deploy")){
                        comps.get(i).setImportPath(path);
                    }
                }
            }
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    "Exception while geting setJDKSourcePath", ex);
        }

    }



    /**
     * This API sets the Server source path and name,
     * finding the right <componentset> based on the provided name  and @type=“server”.
     * It adds the Server configuration from the template file if it’s not in package.xml yet
     * (or replacing whatever is there). Only one server can be configured per role.
     * When set to NULL, all <component> and <startupenv> XML with @type
     * starting with the substring “server.” shall be removed from <workerrole>
     * @param name
     * @param path
     * @param templateFile
     * @throws WindowsAzureInvalidProjectOperationException
     */
    public void setServer(String name, String path, File templateFile) throws WindowsAzureInvalidProjectOperationException {
        if (path == null || templateFile == null || path.isEmpty() ) {
            throw new IllegalArgumentException();
        }

        try {
            if (name == null) {
                //unassociate the server configuration from role
                remAssocFromRole("server.");
                return;
            }

            //parse component.xml and go the selected server
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document pacDoc = winProjMgr.getPackageFileDoc();
            Document compDoc = ParserXMLUtility.parseXMLFile(templateFile.getAbsolutePath());
            String expr = String.format(WindowsAzureConstants.TEMP_SERVER_COMP, "server", name);
            Element compSet = (Element) xPath.evaluate(expr, compDoc, XPathConstants.NODE);

            String parentNode = String.format(
                    WindowsAzureConstants.WA_PACK_ROLE, getName());
            Element role = (Element) xPath.evaluate(parentNode, pacDoc,
                    XPathConstants.NODE);

            String serApp = String.format(WindowsAzureConstants.COMPONENT_TYPE,
                    getName(), "server.app");
            NodeList serApps = (NodeList) xPath.evaluate(serApp, pacDoc, XPathConstants.NODESET);

            if (compSet != null) {

                //set server name in property
                expr = String.format(WindowsAzureConstants.SERVER_PROP_PATH,getName());
                Element property = (Element) xPath.evaluate(expr, pacDoc, XPathConstants.NODE);
                if (property ==  null) {
                    //find parent and append child
                    Element projProper = (Element) xPath.evaluate(
                            WindowsAzureConstants.PROJ_PROPERTY, pacDoc, XPathConstants.NODE);
                    property = pacDoc.createElement("property");
                    property.setAttribute(WindowsAzureConstants.ATTR_NAME,
                            String.format(WindowsAzureConstants.SERVER_PROP_NAME,getName()));
                    property.setAttribute(WindowsAzureConstants.ATTR_VALUE, name);
                    projProper.appendChild(property);
                }
                NodeList nodeList = compSet.getChildNodes();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    if (!node.hasAttributes()) {
                        continue;
                    }
                    Element ele = null;
                    Element compEle = (Element) node;
                    if (compEle.getNodeName().equalsIgnoreCase("startupenv")) {
                        String jdkDirName = new File(path).getName();
                        String envVal = compEle.getAttribute("value");
                        envVal = envVal.replace("${placeholder}", "\\%ROLENAME%\\" + jdkDirName);
                        ele = pacDoc.createElement("startupenv");
                        ele.setAttribute("name", compEle.getAttribute("name"));
                        ele.setAttribute("value", envVal);
                        ele.setAttribute("type", compEle.getAttribute("type"));
                        if((serApps != null) && (serApps.getLength() != 0)) {
                            role.insertBefore(ele, serApps.item(0));
                        } else {
                        role.appendChild(ele);
                        }
                        getRuntimeEnv().put(compEle.getAttribute("name"), envVal);
                    } else if (compEle.getNodeName().equalsIgnoreCase("component")) {
                        ele = pacDoc.createElement("component");
                        String type = compEle.getAttribute(WindowsAzureConstants.ATTR_TYPE);
                        if(type.equalsIgnoreCase("server.deploy")) {
                            NamedNodeMap map = compEle.getAttributes();
                            for(int j=0; j<map.getLength();j++) {
                                ele.setAttribute(map.item(j).getNodeName(), map.item(j).getNodeValue());
                            }
                            ele.setAttribute(WindowsAzureConstants.ATTR_IPATH, path);
                            if((serApps != null) && (serApps.getLength() != 0)) {
                                role.insertBefore(ele, serApps.item(0));
                            } else {
                            role.appendChild(ele);
                            }
                        } else if (type.equalsIgnoreCase("server.start")) {
                            NamedNodeMap map = compEle.getAttributes();
                            for (int j = 0; j < map.getLength();j++) {
                                ele.setAttribute(map.item(j).getNodeName(), map.item(j).getNodeValue());
                            }
                            role.appendChild(ele);
                        } else {
                            continue;
                        }
                        getComponents().add(getComponentObjFromEle(ele));
                    }
                }
            }
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException(
                    "Exception while setServer", e);
        }
    }

   protected WindowsAzureRoleComponent getComponentObjFromEle(Element compEle) throws WindowsAzureInvalidProjectOperationException {
        try {
            WindowsAzureRoleComponent compobj = new WindowsAzureRoleComponent(winProjMgr, this);
            if(compEle.hasAttribute(WindowsAzureConstants.ATTR_IMPORTAS)
                && (!compEle.getAttribute(WindowsAzureConstants.ATTR_IMPORTAS).isEmpty())) {
                compobj.setDeployname(compEle.getAttribute(WindowsAzureConstants.ATTR_IMPORTAS));
            }

            if (compEle.hasAttribute(WindowsAzureConstants.ATTR_IPATH)){
                compobj.setImportPath(compEle.getAttribute(WindowsAzureConstants.ATTR_IPATH));
            }

            if(compEle.hasAttribute(WindowsAzureConstants.ATTR_TYPE)) {
                compobj.setType(compEle.getAttribute(WindowsAzureConstants.ATTR_TYPE));
            }

            if(compEle.hasAttribute(WindowsAzureConstants.ATTR_IMETHOD)) {
                compobj.setImportMethod(WindowsAzureRoleComponentImportMethod.valueOf(compEle.getAttribute(WindowsAzureConstants.ATTR_IMETHOD)));
            }

            if(compEle.hasAttribute(WindowsAzureConstants.ATTR_DDIR)) {
                compobj.setDeployDir(compEle.getAttribute(WindowsAzureConstants.ATTR_DDIR));
            }

            if(compEle.hasAttribute(WindowsAzureConstants.ATTR_DMETHOD)) {
                compobj.setDeployMethod(WindowsAzureRoleComponentDeployMethod.valueOf(compEle.getAttribute(WindowsAzureConstants.ATTR_DMETHOD)));
            }
            return compobj;
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException(
                    "Exception in getComponentObjFromEle", e);
        }
    }


    protected void remAssocFromRole(String type) throws WindowsAzureInvalidProjectOperationException {
        //find all components and env have type server...
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = winProjMgr.getPackageFileDoc();
            if (type.equalsIgnoreCase("server.")) {
                //remove server property
                String exprPro = String.format(WindowsAzureConstants.SERVER_PROP_PATH,getName());
                Element property = (Element) xPath.evaluate(exprPro, doc, XPathConstants.NODE);
                if(property != null) {
                    property.getParentNode().removeChild(property);
                }
            }

            //remove component and start env
            String expr =  String.format(WindowsAzureConstants.SERVER_ASSO, getName(), type);
            NodeList nodelist = (NodeList)xPath.evaluate(expr, doc,XPathConstants.NODESET);
            for(int i=0; i<nodelist.getLength();i++){
                if (nodelist.item(i).getNodeName().equalsIgnoreCase("startupenv")) {
                    //remove start env
                   Element ele = (Element) nodelist.item(i);
                   renameRuntimeEnv(ele.getAttribute("name"), "");
                   // envVarMap.remove(ele.getAttribute("name"));
                   // nodelist.item(i).getParentNode().removeChild(nodelist.item(i));
                }
                if (nodelist.item(i).getNodeName().equalsIgnoreCase("component")) {
                    Element ele = (Element) nodelist.item(i);
                    String componenttype = ele.getAttribute("type");
                    if(!"server.app".equalsIgnoreCase(componenttype)) {
                        WindowsAzureRoleComponent waComp = null;
                        for (int j = 0; j < getComponents().size(); j++) {
                            waComp = getComponents().get(j);
                            if (waComp.getDeployName().equalsIgnoreCase(
                                ele.getAttribute(WindowsAzureConstants.ATTR_IMPORTAS)) ) {
                                break;
                            }
                        }
                        if (waComp != null) {
                            getComponents().remove(waComp);
                        }
                        nodelist.item(i).getParentNode().removeChild(nodelist.item(i));
                    }
                 }
            }
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException(
                    "Exception while removing server association from role", e);
        }
    }

    /**
     * This function expose the source path and the name of the server if one is configured,
     * depending on the presence of the special Ant property storing the name of the server
     * and then the presence of component[@type=“server.deploy”] inside the appropriate
     * <workerrole> element. If no server is configured, return NULL.
     * @return
     * @throws WindowsAzureInvalidProjectOperationException
     */
    public String getServerSourcePath()
            throws WindowsAzureInvalidProjectOperationException {
        try {
            String srcPath = null;

            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = winProjMgr.getPackageFileDoc();
            String expr =  String.format(WindowsAzureConstants.SERVER_TYPE, getName(), "server.deploy");
            Element node = (Element) xPath.evaluate(expr, doc,XPathConstants.NODE);
            if (node != null) {
                srcPath = node.getAttribute(WindowsAzureConstants.ATTR_IPATH);
            }
            return srcPath;
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException(
                    "Exception in getServerSourcePath", e);
        }
    }


    /**
     * This function expose the source path and the name of the server if one is configured,
     * depending on the presence of the special Ant property storing the name of the server
     * and then the presence of component[@type=“server.deploy”] inside the appropriate
     * <workerrole> element. If no server is configured .
     * @param path
     * @throws WindowsAzureInvalidProjectOperationException
     */
  public void setServerSourcePath(String name, String path, File templateFile)
          throws WindowsAzureInvalidProjectOperationException {
      if(path == null || path.isEmpty()) {
          throw new IllegalArgumentException();
      }
      try {
          XPath xPath = XPathFactory.newInstance().newXPath();
          Document doc = winProjMgr.getPackageFileDoc();
          String expr =  String.format(WindowsAzureConstants.WA_PACK_SENV_TYPE, getName(), "server.home");
          Element envNameEle = (Element) xPath.evaluate(expr, doc, XPathConstants.NODE);
          if (envNameEle == null) {
              throw new WindowsAzureInvalidProjectOperationException("server is not configured");
          }
          //find ${placeholder} value from templateFile
          Document compDoc = ParserXMLUtility.parseXMLFile(templateFile.getAbsolutePath());
          expr = String.format(WindowsAzureConstants.SERVER_HOME, "server", name);
          Element serHome = (Element) xPath.evaluate(expr, compDoc, XPathConstants.NODE);
          String envVal = "";
          if (serHome != null) {
              envVal= serHome.getAttribute(WindowsAzureConstants.ATTR_VALUE);
              envVal = envVal.replace("${placeholder}", "\\%ROLENAME%\\" + new File(path).getName());
          }


          envNameEle.setAttribute(WindowsAzureConstants.ATTR_VALUE, envVal);
          String envName = envNameEle.getAttribute(WindowsAzureConstants.ATTR_NAME);
          getRuntimeEnv().put(envName, envVal);
          for (int i = 0; i < getComponents().size(); i++) {
              if (winCompList.get(i).getType().equalsIgnoreCase("server.deploy")){
                  getComponents().get(i).setImportPath(path);
              }
          }

      } catch (Exception e) {
          throw new WindowsAzureInvalidProjectOperationException(
                  "Exception in setServerSourcePath", e);
      }

  }

  /**
   * This function expose the source path and the name of the server if one is configured,
   * depending on the presence of the special Ant property storing the name of the server
   * and then the presence of component[@type=“server.deploy”] inside the appropriate
   * <workerrole> element. If no server is configured, return NULL.
   * @return
   * @throws WindowsAzureInvalidProjectOperationException
   */
  public String getServerName()
          throws WindowsAzureInvalidProjectOperationException {

      try {
          String srcPath = null;
          XPath xPath = XPathFactory.newInstance().newXPath();
          Document doc = winProjMgr.getPackageFileDoc();
          String expr = String.format(WindowsAzureConstants.SERVER_PROP_PATH,getName());
          Element property = (Element) xPath.evaluate(expr, doc, XPathConstants.NODE);
          if (property !=  null) {
              srcPath = property.getAttribute(WindowsAzureConstants.ATTR_VALUE);
          }
          return srcPath;
      } catch (Exception e) {
          throw new WindowsAzureInvalidProjectOperationException(
                  "Exception in getServerName", e);
      }
  }

  /**
   * This finds the appropriate <component> template  (@type=“server.app”)
   *  inside the provided componentsets.xml file, copies it over right before
   *  the <component> element with @type=“server.start” in package.xml,
   *  and sets its @importsrc, @importmethod (auto or copy),
   *  and @importas, as figured out by the plugin depending on the user input.
   * @param importSrc
   * @param importAs
   * @param importMethod
   * @param templateFile
   * @throws WindowsAzureInvalidProjectOperationException
   */
  public void addServerApplication(String importSrc, String importAs,
          String importMethod, File templateFile)
                  throws WindowsAzureInvalidProjectOperationException {
      if (importSrc == null || importAs == null  || importMethod == null ||
              templateFile == null || importSrc.isEmpty() || importAs.isEmpty() || importMethod.isEmpty()) {
          throw new IllegalArgumentException();
      }

      try {

          List<WindowsAzureRoleComponent> comps = getComponents();
          //find appropriate component node in template file
          XPath xPath = XPathFactory.newInstance().newXPath();
          Document compDoc = ParserXMLUtility.parseXMLFile(templateFile.getAbsolutePath());
          String expr = String.format(WindowsAzureConstants.SERVER_APP, "server", getServerName());
          Element comp = (Element) xPath.evaluate(expr, compDoc, XPathConstants.NODE);

          //copy the node in package.xml file
          Document pacDoc = winProjMgr.getPackageFileDoc();
          String parentNode = String.format(
                  WindowsAzureConstants.WA_PACK_ROLE, getName());
          Element role = (Element) xPath.evaluate(parentNode, pacDoc,
                  XPathConstants.NODE);

          String serverStart = String.format(
                  WindowsAzureConstants.SERVER_TYPE, getName(), "server.start");
          Element serStartNode = (Element) xPath.evaluate(serverStart, pacDoc,
                  XPathConstants.NODE);

          Element app = pacDoc.createElement("component");
          if (comp!= null) {
          NamedNodeMap map = comp.getAttributes();
          for (int j = 0; j < map.getLength(); j++) {
              app.setAttribute(map.item(j).getNodeName(), map.item(j).getNodeValue());
          }
          } else {
              app.setAttribute(WindowsAzureConstants.ATTR_DDIR, "%SERVER_APPS_LOCATION%");
              app.setAttribute(WindowsAzureConstants.ATTR_DMETHOD, "copy");
              app.setAttribute(WindowsAzureConstants.ATTR_TYPE, "server.app");
          }
          app.setAttribute(WindowsAzureConstants.ATTR_IPATH, importSrc);
          app.setAttribute(WindowsAzureConstants.ATTR_IMPORTAS, importAs);
          app.setAttribute(WindowsAzureConstants.ATTR_IMETHOD, importMethod);

          if (serStartNode == null ) {
              role.appendChild(app);
          } else {
          role.insertBefore(app, serStartNode);
          }
          comps.add(getComponentObjFromEle(app));

      } catch (Exception e) {
          throw new WindowsAzureInvalidProjectOperationException(
                  "Exception in addServerApplication", e);
      }
  }

  /***
   * This API returns the names of the applications configured for this role,
   * based on an XPath like this, in the context of the appropriate <workerrole> in package.xml:
   * component[@type="server.app"]/@importas
   * @return
   * @throws WindowsAzureInvalidProjectOperationException
   */
  public List<WindowsAzureRoleComponent> getServerApplications() throws WindowsAzureInvalidProjectOperationException {

      try {
          ArrayList<WindowsAzureRoleComponent> serverComp = new ArrayList<WindowsAzureRoleComponent>();
          for (int i = 0; i < getComponents().size(); i++) {
              if (getComponents().get(i).getType().equalsIgnoreCase("server.app")) {
                  serverComp.add(getComponents().get(i));
              }
          }
          return serverComp;
      } catch (Exception e) {
          throw new WindowsAzureInvalidProjectOperationException(
                  "Exception in ", e);
      }
  }

  /**
   * This API will remove an application from package.xml.
   * This deletes the <component> (@type=“server.app” and @name=“?”) inside
   * the appropriate <workerrole> element in package.xml.
 * @throws WindowsAzureInvalidProjectOperationException
   *
   */
  public void removeServerApplication(String importAs) throws WindowsAzureInvalidProjectOperationException {
      if(importAs == null || importAs.isEmpty()) {
          throw new IllegalArgumentException();
      }
      try{
          XPath xPath = XPathFactory.newInstance().newXPath();
          Document doc = winProjMgr.getPackageFileDoc();
          String expr = String.format(WindowsAzureConstants.COMPONENT_TYPE_IMPORTAS,
                  getName(), "server.app", importAs);
          Node comp = (Node) xPath.evaluate(expr, doc,
                  XPathConstants.NODE);
          if (comp != null) {
              WindowsAzureRoleComponent waComp = null;
              for (int i = 0; i < getComponents().size(); i++) {
                  waComp = getComponents().get(i);
                  if (waComp.getDeployName().equalsIgnoreCase(importAs)) {
                      break;
                  }
              }
              if (waComp != null) {
                  getComponents().remove(waComp);
              }
              comp.getParentNode().removeChild(comp);
          }

      } catch (Exception e) {
          throw new WindowsAzureInvalidProjectOperationException(
                  "Exception in ", e);
      }
  }


  /**
   * This API create corresponding variable in serviceDefination file.
   * @param name
   * @throws WindowsAzureInvalidProjectOperationException
   */
  protected void setVarInDefFile(String name)
          throws WindowsAzureInvalidProjectOperationException {
      //String value = "";
      if (name == null || name.isEmpty()) {
          throw new IllegalArgumentException(
                  WindowsAzureConstants.INVALID_ARG);
      }

      try {
          XPath xPath = XPathFactory.newInstance().newXPath();
          Document doc = winProjMgr.getdefinitionFileDoc();

          //Find Startup tag
          String startupWa = String.format(
                  WindowsAzureConstants.STARTUP_WR_NAME, getName());
          String parentNode = String.format(WindowsAzureConstants.WR_NAME,
                  getName());
         WindowsAzureRole.updateOrCreateElement(doc, startupWa,
                 parentNode, "Startup", true, null);

          //find task Tag
          String taskCmd = String.format(
                  WindowsAzureConstants.STARTUP_WITH_STARTUP_CMD,
                  getName());
          Map<String, String> attr = new HashMap<String, String>();
          attr.put("commandLine", WindowsAzureConstants.TASK_CMD_LINE);
          WindowsAzureRole.updateOrCreateElement(doc, taskCmd, startupWa,
                  "Task", false, attr);

          //environment tag
          String expr = String.format(WindowsAzureConstants.ENVIRONMENT,
                  getName());
          WindowsAzureRole.updateOrCreateElement(doc, expr, taskCmd,
                  "Environment", true, null);

          Element env = (Element) xPath.evaluate(expr, doc,
                  XPathConstants.NODE);

          // find Variable tag, if not present create new
          Element var = (Element) xPath.evaluate(
                  String.format("./Variable[@name='%s']", name),
                  env, XPathConstants.NODE);
          if (var == null) {
              var = doc.createElement("Variable");
              var.setAttribute(WindowsAzureConstants.ATTR_NAME, name);
              env.appendChild(var);
          }

      } catch (Exception ex) {
          throw new WindowsAzureInvalidProjectOperationException(
                  WindowsAzureConstants.EXCP, ex);
      }
  }

  /**
   * API to find the type of the environment variable set in the role.
   * @return
 * @throws WindowsAzureInvalidProjectOperationException .
   */
  public String getRuntimeEnvType(String varName)
          throws WindowsAzureInvalidProjectOperationException {
      if (varName == null || varName.isEmpty()) {
          throw new IllegalArgumentException(WindowsAzureConstants.INVALID_ARG);
      }
      try {
          XPath xPath = XPathFactory.newInstance().newXPath();
          Document doc = getWinProjMgr().getPackageFileDoc();
          String expr = String.format(WindowsAzureConstants
                  .WA_PACK_SENV_NAME,
                  getName(), varName)  + "/@type";
          String type = xPath.evaluate(expr, doc);
          if (type.isEmpty()) {
              type = null;
          }
          return type;
      } catch (Exception ex) {
          throw new WindowsAzureInvalidProjectOperationException(
                  WindowsAzureConstants.EXCP, ex);
    }
  }

  /**
   *
   * @param varName
   * @param envType
   * @throws WindowsAzureInvalidProjectOperationException
   */
  public void setRuntimeEnvType(String varName, String envType)
          throws WindowsAzureInvalidProjectOperationException {
      if (varName == null || varName.isEmpty() || envType == null) {
          throw new IllegalArgumentException(
                  WindowsAzureConstants.INVALID_ARG);
      }
      try {
          Document doc = getWinProjMgr().getPackageFileDoc();
          XPath xPath = XPathFactory.newInstance().newXPath();
          //check first the env is already present, if yes. edit the same
          String env = String.format(WindowsAzureConstants.WA_PACK_SENV_NAME, getName(), varName);
          Element envNode = (Element) xPath.evaluate(env,doc, XPathConstants.NODE);

          if (envNode == null) {
              throw new WindowsAzureInvalidProjectOperationException(
                      varName + " Runtime variable is not present");
          }
          envNode.setAttribute(WindowsAzureConstants.ATTR_TYPE, envType);
      } catch (Exception ex) {
          throw new WindowsAzureInvalidProjectOperationException(
                  WindowsAzureConstants.EXCP, ex);
      }

  }

  /**
   * This API to set cache memory size (and enable caching)
   * @param value
   * @throws WindowsAzureInvalidProjectOperationException
   * @throws XPathExpressionException
   */
  public void setCacheMemoryPercent(int value)
          throws WindowsAzureInvalidProjectOperationException {
      if (value > 100 || value < 0) {
          throw new IllegalArgumentException(
                  WindowsAzureConstants.INVALID_ARG);
      }

      if (value == 0) {
          //if value is 0 then disable all cache.
          if (isCachingEnable()) {
              disableCache();
              setCacheStorageAccountName("");
              setCacheStorageAccountKey("");
          }
      } else {
          try {
              // check cache is already exist...
              if (!isCachingEnable()) {
                  //enable Cache
                  enableCache();
              }

              Element ele = getSettingElement(WindowsAzureConstants.SET_CACHESIZEPER);
              ele.setAttribute("value", String.valueOf(value));

          } catch (Exception ex) {
              throw new WindowsAzureInvalidProjectOperationException(
                      WindowsAzureConstants.EXCP, ex);
          }
      }
  }


  private Element getSettingElement(String settingName)
          throws WindowsAzureInvalidProjectOperationException {
      Element ele = null;
      try {
          Document doc = getWinProjMgr().getConfigFileDoc();
          XPath xPath = XPathFactory.newInstance().newXPath();
          String expr = String.format(WindowsAzureConstants.CONFIG_SETTING_ROLE,getName(),
                  settingName);
          ele = (Element) xPath.evaluate(expr, doc,
                  XPathConstants.NODE);
      } catch (Exception  ex) {
          ele = null;
      }
      return ele;
  }

  /**
   * This API will remove all setting from
   * CSDEF: For corresponding WorkerRole element, in Imports, ensure  presence of <Import moduleName="Caching" />
   * CSCFG: For corresponding Role element, in ConfigurationSettings, ensure presence of:
   *   <Setting name="Microsoft.WindowsAzure.Plugins.Caching.CacheSizePercentage" value="value" />
   *   <Setting name="Microsoft.WindowsAzure.Plugins.Caching.Loglevel" value="" />
   *   <Setting name="Microsoft.WindowsAzure.Plugins.Caching.ConfigStoreConnectionString" value="UseDevelopmentStorage=true" />
   *   <Setting name="Microsoft.WindowsAzure.Plugins.Caching.NamedCaches"
   *   value="{&quot;caches&quot;:[{&quot;name&quot;:&quot;default&quot;,&quot;policy&quot;:{&quot;expiration&quot;:{&quot;defaultTTL&quot;:10,&quot;isExpirable&quot;:true,&quot;type&quot;:1}}}]}"/>*(The goo inside is HTML-encoded JSON)
 * @throws WindowsAzureInvalidProjectOperationException
 * @throws XPathExpressionException
   *
   */
  private void disableCache()
          throws WindowsAzureInvalidProjectOperationException {
      try {

      HashMap<String, WindowsAzureNamedCache> mapcache = (HashMap<String, WindowsAzureNamedCache>) getNamedCaches();
      // Remove entries from CSDEF file
      Element eleImport = getImportEle("Caching");
      eleImport.getParentNode().removeChild(eleImport);

      //Remove all memcache including default
      //Get all cache from role and remove all by iterating on map


      List< WindowsAzureNamedCache> list = new ArrayList<WindowsAzureNamedCache>();
      for (WindowsAzureNamedCache cache : mapcache.values()) {
          list.add(cache);
      }
      for (int i = 0; i < list.size(); i++) {
          list.get(i).delete();
      }
      getLocalStorage(WindowsAzureConstants.CACHE_LS_NAME).delete();

      // Remove entries from CSDFG file
      Document doc = getWinProjMgr().getConfigFileDoc();
      String expr = String.format(WindowsAzureConstants.CONFIG_SETTING_ROLE,getName(),
              WindowsAzureConstants.SET_CACHESIZEPER);
      ParserXMLUtility.deleteElement(doc, expr);

      expr = String.format(WindowsAzureConstants.CONFIG_SETTING_ROLE,getName(),
              WindowsAzureConstants.SET_CONFIGCONN);
      ParserXMLUtility.deleteElement(doc, expr);

      expr = String.format(WindowsAzureConstants.CONFIG_SETTING_ROLE,getName(),
              WindowsAzureConstants.SET_DIAGLEVEL);
      ParserXMLUtility.deleteElement(doc, expr);

      expr = String.format(WindowsAzureConstants.CONFIG_SETTING_ROLE,getName(),
              WindowsAzureConstants.SET_NAMEDCACHE);
      ParserXMLUtility.deleteElement(doc, expr);

      } catch (Exception ex) {
          throw new WindowsAzureInvalidProjectOperationException(
                  WindowsAzureConstants.EXCP, ex);
      }

  }

  /**
   * This API will add import statement, local storege and endpoint in csdef file
   * and settings tags in cscfg file.
   * @throws WindowsAzureInvalidProjectOperationException
   */
  private void enableCache()
          throws WindowsAzureInvalidProjectOperationException {
      try {
          //add import
          Document doc = winProjMgr.getdefinitionFileDoc();
          String expr = String.format(WindowsAzureConstants.IMPORT_NANE,
                  getName(), "Caching");
          String parentNodeExpr = String.format(
                  WindowsAzureConstants.IMPORT, getName());
          HashMap<String, String> map = new HashMap<String, String>();
          map.put("moduleName", "Caching");
          updateOrCreateElement(doc, expr, parentNodeExpr, "Import", false, map);

          //add local storage
          addLocalStorage(WindowsAzureConstants.CACHE_LS_NAME, 20000,
                  false, WindowsAzureConstants.CACHE_LS_PATH);

          //add setting tags

          doc = winProjMgr.getConfigFileDoc();
          expr = String.format(WindowsAzureConstants.CONFIG_SETTING_ROLE, getName(),
                  WindowsAzureConstants.SET_CACHESIZEPER);
          parentNodeExpr = String.format(
                  WindowsAzureConstants.CONFIG_ROLE_SET, getName());
          map.clear();
          map.put("name", WindowsAzureConstants.SET_CACHESIZEPER);
          map.put("value", "");
          updateOrCreateElement(doc, expr, parentNodeExpr, "Setting", false, map);

          expr = String.format(WindowsAzureConstants.CONFIG_SETTING_ROLE, getName(),
                  WindowsAzureConstants.SET_DIAGLEVEL);
          map.clear();
          map.put("name", WindowsAzureConstants.SET_DIAGLEVEL);
          map.put("value", "1");
          updateOrCreateElement(doc, expr, parentNodeExpr, "Setting", false, map);

          expr = String.format(WindowsAzureConstants.CONFIG_SETTING_ROLE, getName(),
                  WindowsAzureConstants.SET_CONFIGCONN);
          map.clear();
          map.put("name", WindowsAzureConstants.SET_CONFIGCONN);
          map.put("value", WindowsAzureConstants.SET_CONFIGCONN_VAL);
          updateOrCreateElement(doc, expr, parentNodeExpr, "Setting", false, map);

          //add Internal endpoint
          String newCache = JSONHelper.createObject();
          // Add setting in config file
          parentNodeExpr = String.format(
                  WindowsAzureConstants.CONFIG_ROLE_SET, getName());
          expr = String.format(WindowsAzureConstants.CONFIG_SETTING_ROLE, getName(),
                  WindowsAzureConstants.SET_NAMEDCACHE);
          map.clear();
          map.put("name", WindowsAzureConstants.SET_NAMEDCACHE);
          map.put("value", newCache);
          updateOrCreateElement(doc, expr, parentNodeExpr, "Setting", false, map);
          addNamedCache("default", 11211);

      } catch (Exception ex) {
          throw new WindowsAzureInvalidProjectOperationException(
                  WindowsAzureConstants.EXCP, ex);
      }

  }

  /**
   * This method is to tell whether caching is enable or not.
   * @return
   * @throws WindowsAzureInvalidProjectOperationException
   */
  protected boolean isCachingEnable() throws WindowsAzureInvalidProjectOperationException {
      boolean isEnable = false;
      Element eleCache = getImportEle("Caching");
      if (eleCache != null) {
          isEnable = true;
      }
      return isEnable;
  }


  private Element getImportEle(String moduleName) throws WindowsAzureInvalidProjectOperationException {
      Element eleImport = null;
      try {
          Document doc = getWinProjMgr().getdefinitionFileDoc();
          XPath xPath = XPathFactory.newInstance().newXPath();
          String expr = String.format(WindowsAzureConstants.IMPORT_NANE,
                  getName(), moduleName);
          eleImport = (Element) xPath.evaluate(expr, doc,
                  XPathConstants.NODE);
      } catch (Exception ex) {
          throw new WindowsAzureInvalidProjectOperationException(
                  WindowsAzureConstants.EXCP, ex);
      }
      return eleImport;
  }


  /**
   * API to read Cache memory setting (and to determine if
   * caching is enabled if this returns > 0).
   * @return
   * @throws WindowsAzureInvalidProjectOperationException .
   */
  public int getCacheMemoryPercent()
          throws WindowsAzureInvalidProjectOperationException {
      int per = 0;
      try {
          if (isCachingEnable()) {
              Document doc = getWinProjMgr().getConfigFileDoc();
              XPath xPath = XPathFactory.newInstance().newXPath();
              String expr = String.format(WindowsAzureConstants.CONFIG_SETTING_ROLE_VAL,getName(),
                      WindowsAzureConstants.SET_CACHESIZEPER);
              String val = xPath.evaluate(expr, doc);
              per = Integer.valueOf(val);
          }
      } catch (Exception ex) {
          throw new WindowsAzureInvalidProjectOperationException(
                  WindowsAzureConstants.EXCP, ex);
      }
      return per;
  }


  /**
   * set the storage account name associated with the cache.
   * The information is persisted on the plug-in property in package.xml named
   * project.workerrole1.cachestorageaccount.name.
   * It also written to the CSCFG but what exactly is put into the CSCFG
   * depends on whether the project is built for the cloud or for the emulator.
   * The basic idea is that as long as it's for Emulator, dev storage is enabled in the CSCFG
   * (regardless of storage account settings), and if it's for the Cloud, then the account credentials are enabled in the CSCFG.
   * @param name .
   * @throws WindowsAzureInvalidProjectOperationException .
   */

  public void setCacheStorageAccountName(String name) throws WindowsAzureInvalidProjectOperationException {
      try {
          String propName = String.format(WindowsAzureConstants.CACHE_ST_ACC_NAME_PROP,getName());
          if(name.isEmpty()) {
              //remove property from package.xml
              ParserXMLUtility.deleteElement(winProjMgr.getPackageFileDoc(),
                      String.format(WindowsAzureConstants.ROLE_PROP,  propName));
          } else {
              //add entry in package.xml
              addPropertyInPackageXML(propName, name);
          }
          //sets entry in cscfg
          setCacheSettingInCscfg(name, getCacheStorageAccountKey());
      } catch(Exception ex) {
          throw new WindowsAzureInvalidProjectOperationException(
                  WindowsAzureConstants.EXCP, ex);
      }
  }

  /**
   * Sets the storage account name from the plugin property and
   * sets setting value in config depending upon the package type.
   * @param key
   * @throws WindowsAzureInvalidProjectOperationException
   */
  public void setCacheStorageAccountKey(String key) throws WindowsAzureInvalidProjectOperationException {
      try {
          String propName = String.format(WindowsAzureConstants.CACHE_ST_ACC_KEY_PROP,getName());
          if(key.isEmpty()) {
              //remove property from package.xml
              ParserXMLUtility.deleteElement(winProjMgr.getPackageFileDoc(),
                      String.format(WindowsAzureConstants.ROLE_PROP,  propName));
          } else {
              //add entry in package.xml
              addPropertyInPackageXML(propName, key);
          }
          //sets entry in cscfg
          setCacheSettingInCscfg(getCacheStorageAccountName(), key);
      } catch(Exception ex) {
          throw new WindowsAzureInvalidProjectOperationException(
                  WindowsAzureConstants.EXCP, ex);
      }
  }

  /**
   * This API sets cache setting in config file.
   * If package type is local then the the value will be UseDevelopmentStorage=true
   * else  "DefaultEndpointsProtocol=https;AccountName=name;AccountKey=key".
   * @param name
   * @param value
   * @throws WindowsAzureInvalidProjectOperationException
   */
  protected void setCacheSettingInCscfg(String name, String value) throws WindowsAzureInvalidProjectOperationException {
      try {
          Document doc = winProjMgr.getConfigFileDoc();
          String expr = String.format(WindowsAzureConstants.CONFIG_SETTING_ROLE, getName(),
                  WindowsAzureConstants.SET_CONFIGCONN);
          String parentNodeExpr = String.format(
                  WindowsAzureConstants.CONFIG_ROLE_SET, getName());
          HashMap<String, String> map = new HashMap<String, String>();
          if(isCachingEnable()) {
              if(winProjMgr.getPackageType() == WindowsAzurePackageType.LOCAL || name == null || value == null ||
                      name.isEmpty() || value.isEmpty()) {
                  map.clear();
                  map.put(WindowsAzureConstants.ATTR_NAME, WindowsAzureConstants.SET_CONFIGCONN);
                  map.put(WindowsAzureConstants.ATTR_VALUE, WindowsAzureConstants.SET_CONFIGCONN_VAL);
                  updateOrCreateElement(doc, expr, parentNodeExpr, "Setting", false, map);
              } else if(name != null && value != null) {
                  String val =  String.format(WindowsAzureConstants.SET_CONFIGCONN_VAL_CLOULD,
                          name,value);
                  map.clear();
                  map.put(WindowsAzureConstants.ATTR_NAME, WindowsAzureConstants.SET_CONFIGCONN);
                  map.put(WindowsAzureConstants.ATTR_VALUE, val);
                  updateOrCreateElement(doc, expr, parentNodeExpr, "Setting", false, map);
              }
          }
      } catch(Exception ex) {
          throw new WindowsAzureInvalidProjectOperationException(
                  WindowsAzureConstants.EXCP, ex);
      }
  }

  /**
   * Gets the storage account key from the plugin property.
   * @return
   * @throws WindowsAzureInvalidProjectOperationException
   */
  public String getCacheStorageAccountKey() throws WindowsAzureInvalidProjectOperationException {
      String key = null;
      try {
          Document packageFileDoc = getWinProjMgr().getPackageFileDoc();
          if(packageFileDoc != null ){
              String propName = String.format(WindowsAzureConstants.CACHE_ST_ACC_KEY_PROP,getName());
              String nodeExpr =  String.format(WindowsAzureConstants.ROLE_PROP_VAL, propName);
              XPath xPath = XPathFactory.newInstance().newXPath();
              key = xPath.evaluate(nodeExpr, packageFileDoc);
          }
      } catch(Exception ex) {
          throw new WindowsAzureInvalidProjectOperationException(
                  WindowsAzureConstants.EXCP, ex);
      }
      return key;
  }

  /**
   * Gets the storage account name from the plug-in property.
   * @return
   * @throws WindowsAzureInvalidProjectOperationException
   */
  public String getCacheStorageAccountName() throws WindowsAzureInvalidProjectOperationException {
      String name = null;
      try {
          Document packageFileDoc = getWinProjMgr().getPackageFileDoc();
          if(packageFileDoc != null ){
              String propName = String.format(WindowsAzureConstants.CACHE_ST_ACC_NAME_PROP,getName());
              String nodeExpr =  String.format(WindowsAzureConstants.ROLE_PROP_VAL, propName);
              XPath xPath = XPathFactory.newInstance().newXPath();
              name = xPath.evaluate(nodeExpr, packageFileDoc);
          }
      } catch(Exception ex) {
          throw new WindowsAzureInvalidProjectOperationException(
                  WindowsAzureConstants.EXCP, ex);
      }
      return name;
  }

  /**
   * This API will set property in package.xml in waprojectproperties target.
   * @param name
   * @param value
   * @throws WindowsAzureInvalidProjectOperationException
   */
  private void addPropertyInPackageXML(String name, String value) throws WindowsAzureInvalidProjectOperationException {
      Document packageFileDoc;
      try {
          packageFileDoc = getWinProjMgr().getPackageFileDoc();
          if(packageFileDoc != null ){
              String nodeExpr       =  String.format(WindowsAzureConstants.ROLE_PROP,  name);
              String parentNodeExpr =  WindowsAzureConstants.PROJ_PROPERTY;

              HashMap<String,String> nodeAttr = new HashMap<String, String>();
              nodeAttr.put(WindowsAzureConstants.ATTR_NAME,name);
              nodeAttr.put(WindowsAzureConstants.ATTR_VALUE, value);
              updateOrCreateElement(packageFileDoc,nodeExpr,parentNodeExpr,WindowsAzureConstants.PROJ_PROPERTY_ELEMENT_NAME,false,nodeAttr);
          }
      } catch (WindowsAzureInvalidProjectOperationException ex) {
          throw new WindowsAzureInvalidProjectOperationException(
                  WindowsAzureConstants.EXCP, ex);
      }
  }

  /**
   * This function will add a new named cache.
   * Throw if getCacheMemoryPercent() returns 0, or
   * name is null or in use by another cache (case insensitive comparison) or
   * port is not between 1-65535, or already in use by anything else
   * Create internal endpoint named “memcache_name” and the supplied port
   * Inject the new cache object into the JSON with the specified name and return a WindowsAzureNamedCache object from the API.
   * @param name
   * @param port
   * @return
   * @throws WindowsAzureInvalidProjectOperationException
   */

  public WindowsAzureNamedCache addNamedCache(String name, int port) throws WindowsAzureInvalidProjectOperationException {
      if(name == null || name.isEmpty() || port < 1 || port > 65535 ) {
          throw new IllegalArgumentException(WindowsAzureConstants.INVALID_ARG);
      }

      if(!isCachingEnable()) {
          throw new WindowsAzureInvalidProjectOperationException("Caching is not enabled");
      }

      if(getNamedCaches().keySet().contains(name)) {
          throw new WindowsAzureInvalidProjectOperationException(name + " already exist");
      }
//
//	  if(!getWinProjMgr().isValidPort(String.valueOf(port), WindowsAzureEndpointType.Internal)) {
//		  throw new WindowsAzureInvalidProjectOperationException(port + " already exist");
//	  }

      WindowsAzureNamedCache cache = null;
      try {
          Document doc = getWinProjMgr().getConfigFileDoc();

          //Create JSON Object
          String expr = String.format(WindowsAzureConstants.CONFIG_SETTING_ROLE_VAL, getName(),
                  WindowsAzureConstants.SET_NAMEDCACHE);
          String encodedCache = ParserXMLUtility.getExpressionValue(doc, expr);
          String newEncCompCache = JSONHelper.addCache(encodedCache, name, 0, 10, 1);
          ParserXMLUtility.setExpressionValue(doc, expr, newEncCompCache);

          String newCache = JSONHelper.getCaches(newEncCompCache).get(name);
          // Add endpoint
          addEndpoint("memcache_" + name, WindowsAzureEndpointType.Internal, String.valueOf(port), "");

          cache =  getWindowsCacheObjFromEle(newCache);
          cacheMap.put(name, cache);

      } catch (Exception ex) {
          throw new WindowsAzureInvalidProjectOperationException(
                  WindowsAzureConstants.EXCP, ex);
      }
      return cache;
  }

  /**
   * Creates a WindowsAzureNamedCache object with given encoded string.
   * @param str
   * @return
   * @throws WindowsAzureInvalidProjectOperationException
   */
  private WindowsAzureNamedCache getWindowsCacheObjFromEle(String str) throws WindowsAzureInvalidProjectOperationException {

      try {
      WindowsAzureNamedCache waCache = new WindowsAzureNamedCache(this,getWinProjMgr());
      waCache.setName(JSONHelper.getParamVal(str, "name"));

      String type = JSONHelper.getParamVal(str, "policy.expiration.type");
      waCache.setExpirationPolicy(WindowsAzureCacheExpirationPolicy.values()[Integer.parseInt(type)]
              );

      if(JSONHelper.getParamVal(str, "secondaries").equals("1")) {
          waCache.setBackups(true);
      } else {
          waCache.setBackups(false);
      }

      waCache.setMinutesToLive(Integer.parseInt(
              JSONHelper.getParamVal(str, "policy.expiration.defaultTTL")));
      return waCache;
      }catch (Exception ex) {
          throw new WindowsAzureInvalidProjectOperationException(
                  WindowsAzureConstants.EXCP, ex);
      }
  }

  /**
   * This API will return the collection of caches associated with this cache
   * @return
   * @throws WindowsAzureInvalidProjectOperationException
   */
  public Map<String, WindowsAzureNamedCache> getNamedCaches()
          throws WindowsAzureInvalidProjectOperationException {
      if (!cacheMap.isEmpty()) {
          return cacheMap;
      }
      try {
          Document doc = getWinProjMgr().getConfigFileDoc();
          String expr = String.format(WindowsAzureConstants.CONFIG_SETTING_ROLE_VAL, getName(),
                  WindowsAzureConstants.SET_NAMEDCACHE);
          String encodedCache = ParserXMLUtility.getExpressionValue(doc, expr);
          if (!encodedCache.isEmpty()) {
              Map<String, String> encodedMap = JSONHelper.getCaches(encodedCache);
              Set<Entry<String, String>> set = encodedMap.entrySet();
              for (Entry<String, String> entry : set) {
                  cacheMap.put(entry.getKey(), getWindowsCacheObjFromEle(entry.getValue()));
              }
          }
          return cacheMap;
      } catch (Exception ex) {
          throw new WindowsAzureInvalidProjectOperationException(
                  WindowsAzureConstants.EXCP, ex);
      }
  }
}
