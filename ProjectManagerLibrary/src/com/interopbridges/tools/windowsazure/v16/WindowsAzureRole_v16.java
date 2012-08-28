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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class representing Windows Azure role.
 */
public class WindowsAzureRole_v16 {
    private String name;
    private String instances;
    private String vmSize;
    private String accUsername = "";
    private String accPassword = "";
    private String accExpiryDate = "";
    private String certThumbprint = "";
    private List<WindowsAzureEndpoint_v16> winEndPtList = new ArrayList<WindowsAzureEndpoint_v16>();
    private WindowsAzureProjectManager_v16 winProjMgr = null;
    protected Map<String, String> envVarMap = new HashMap<String, String>();
    protected Map<String, WindowsAzureLocalStorage_v16> locStoMap =
            new HashMap<String, WindowsAzureLocalStorage_v16>();

    protected WindowsAzureProjectManager_v16 getWinProjMgr()
            throws WindowsAzureInvalidProjectOperationException_v16 {
        if (winProjMgr == null) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.PMGR_NULL);
        }
        return winProjMgr;
    }

    public WindowsAzureRole_v16(
            WindowsAzureProjectManager_v16 winPrjMgr) {
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
            throws WindowsAzureInvalidProjectOperationException_v16 {
        if ((null == name) || (name.isEmpty())) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants_v16.EXCP_EMPTY_ROLENAME);
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
                String[] value = {oldpath, newPath};

                //if already rename option done on same role and not yet saved,
                //need to retrieve original name of folder and
                //add it in map with new changed name.
                if (getWinProjMgr().mapActivity.
                        containsKey("rename")) {
                    String[] oldVal =
                            getWinProjMgr().mapActivity.get("rename");
                    value[0] = oldVal[0];
                }

                getWinProjMgr().mapActivity.put("rename", value);
            }
            this.name = name;
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_SET_ROLENAME, ex);
        }
    }


    private void setNameInDefFile(String name)
            throws WindowsAzureInvalidProjectOperationException_v16 {
        try {
            Document doc = getWinProjMgr().getdefinitionFileDoc();
            XPath xPath = XPathFactory.newInstance().newXPath();
            String expr = String.format(
                    WindowsAzureConstants_v16.WR_NAME, getName());
            Node role = (Node) xPath.evaluate(expr, doc, XPathConstants.NODE);

            if (role != null) {
                role.getAttributes().getNamedItem(
                        WindowsAzureConstants_v16.ATTR_NAME)
                        .setNodeValue(name);
            }
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    "Exception in setNameInDefFile ", ex);
        }
    }



    private void setNameInConfigFile(String name)
            throws WindowsAzureInvalidProjectOperationException_v16 {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document configDoc = getWinProjMgr().getConfigFileDoc();
            if (configDoc != null) {
                String expr = String.format(
                        WindowsAzureConstants_v16.ROLE_NAME, getName());
                Node role = (Node) xPath.evaluate(expr, configDoc,
                        XPathConstants.NODE);
                role.getAttributes().getNamedItem(
                        WindowsAzureConstants_v16.ATTR_NAME)
                        .setNodeValue(name);
            }
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    "Exception in setNameInConfigFile", ex);
        }
    }

    private void setNameInPackageFile(String name)
            throws WindowsAzureInvalidProjectOperationException_v16 {

        WindowsAzureEndpoint_v16 saInputEndPoint = getSessionAffinityInputEndpoint();
        WindowsAzureEndpoint_v16 saInternalEndPoint = getSessionAffinityInternalEndpoint();

        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document projDoc = getWinProjMgr().getPackageFileDoc();

            //Change session affinity settings in package.xml
            if(saInputEndPoint != null ) {
                String endPointExpr = String.format(WindowsAzureConstants_v16.SA_INPUT_ENDPOINT, this.getName()) ;
                Element propEndPoint = (Element) xPath.evaluate(endPointExpr, projDoc,XPathConstants.NODE);
                if(propEndPoint != null)
                    propEndPoint.setAttribute(WindowsAzureConstants_v16.ATTR_NAME,String.format(WindowsAzureConstants_v16.SA_INPUT_ENDPOINT_NAME_PROP,name));

                //Change internal endpoint name as well
                if(saInternalEndPoint != null) {
                    endPointExpr = String.format(WindowsAzureConstants_v16.SA_INTERNAL_ENDPOINT, this.getName()) ;
                    propEndPoint = (Element) xPath.evaluate(endPointExpr, projDoc,XPathConstants.NODE);
                    if(propEndPoint != null)
                        propEndPoint.setAttribute(WindowsAzureConstants_v16.ATTR_NAME,String.format(WindowsAzureConstants_v16.SA_INTERNAL_ENDPOINT_NAME_PROP,name));

                }

            }

            String expr = String.format(
                    WindowsAzureConstants_v16.WA_PACK_NAME, getName());
            Node role = (Node) xPath.evaluate(expr, projDoc,
                    XPathConstants.NODE);
            if (role != null) {
                role.getAttributes().getNamedItem(
                        WindowsAzureConstants_v16.ATTR_NAME)
                        .setNodeValue(name);
                role.getAttributes()
                .getNamedItem("approotdir")
                .setNodeValue(String.format("%s%s%s",
                        "${basedir}\\", name, "\\approot"));
            }




        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
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
            throws WindowsAzureInvalidProjectOperationException_v16 {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document configDoc = getWinProjMgr().getConfigFileDoc();
            // /ServiceConfiguration/Role
            String expr = String.format(
                    WindowsAzureConstants_v16.ROLE_COUNT, getName());
            this.instances = xPath.evaluate(expr, configDoc);
            return  this.instances;
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_GET_INSTANCES, ex);
        }
    }

    /**
     * Sets the instance count.
     *
     * @param count .
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    public void setInstances(String count)
            throws WindowsAzureInvalidProjectOperationException_v16 {
        if ((null == count) || (count.isEmpty())) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants_v16.INVALID_ARG);
        }
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document configDoc = getWinProjMgr().getConfigFileDoc();
            String expr = String.format(WindowsAzureConstants_v16.ROLE_INSTANCES,
                    getName());
            Element role = (Element) xPath.evaluate(expr, configDoc,
                    XPathConstants.NODE);
            role.setAttribute("count", count);
            this.instances = count;
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_SET_INSTANCES, ex);
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
            throws WindowsAzureInvalidProjectOperationException_v16 {
        if ((vMSize == null) || (vMSize.isEmpty())) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants_v16.EXCP_EMPTY_VMSIZE);
        }
        try {
            if (getName() != null) {
                Document doc = getWinProjMgr().getdefinitionFileDoc();
                String objName = getName();
                XPath xPath = XPathFactory.newInstance().newXPath();

                String expr = String.format(WindowsAzureConstants_v16.WR_NAME,
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
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_SET_VMSIZE, ex);
        }
    }

    /**
     * Gets the list of endpoints that are associated with this role.
     *
     * @return new instance of WindowsAzureEndpoint.
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    public List<WindowsAzureEndpoint_v16> getEndpoints()
            throws WindowsAzureInvalidProjectOperationException_v16 {

        try {
            if (winEndPtList.isEmpty()) {
                Document doc = getWinProjMgr().getdefinitionFileDoc();
                String objName = getName();
                XPath xPath = XPathFactory.newInstance().newXPath();
                String expr = String.format(
                        WindowsAzureConstants_v16.INPUTS_WR_NAME, objName);
                NodeList endPtList = (NodeList) xPath.evaluate(
                        expr, doc, XPathConstants.NODESET);
                for (int i = 0; i < endPtList.getLength(); i++) {
                    Element endptEle = (Element) endPtList.item(i);
                    winEndPtList.add(createWinInputEndPt(endptEle));
                }

                expr = String.format(
                        WindowsAzureConstants_v16.INTERNAL_WR_NAME, objName);
                endPtList = (NodeList) xPath.evaluate(
                        expr, doc, XPathConstants.NODESET);
                for (int i = 0; i < endPtList.getLength(); i++) {
                    Element endptEle = (Element) endPtList.item(i);
                    winEndPtList.add(createWinIntenalEndPt(endptEle));
                }
            }
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_GET_FP, ex);
        }
        return winEndPtList;
    }

    private WindowsAzureEndpoint_v16 createWinInputEndPt( Element endptEle)
            throws WindowsAzureInvalidProjectOperationException_v16 {
        try {
            WindowsAzureEndpoint_v16 winAzureEndpoint =
                    new WindowsAzureEndpoint_v16(getWinProjMgr(), this);
            winAzureEndpoint.setInputEndpointName(
                    endptEle.getAttribute(
                            WindowsAzureConstants_v16.ATTR_NAME));
            winAzureEndpoint.setInputLocalPort(
                    endptEle.getAttribute("localPort"));
            winAzureEndpoint.setInputPort(
                    endptEle.getAttribute("port"));
            return winAzureEndpoint;
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    "Exception in createWinInputEndPt ", ex);
        }
    }


    private WindowsAzureEndpoint_v16 createWinIntenalEndPt( Element endptEle)
            throws WindowsAzureInvalidProjectOperationException_v16 {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            WindowsAzureEndpoint_v16 winAzureEndpoint = new
                    WindowsAzureEndpoint_v16(getWinProjMgr(), this);
            winAzureEndpoint.setInternalEndpointName(
                    endptEle.getAttribute(
                            WindowsAzureConstants_v16.ATTR_NAME));
            winAzureEndpoint.setInternalFixedPort(
                    xPath.evaluate("./FixedPort/@port",
                            endptEle));
            return winAzureEndpoint;
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    "Exception in createWinInputEndPt ", ex);
        }
    }
    protected void setEndpoints(List<WindowsAzureEndpoint_v16> winEndPList) {

        this.winEndPtList = winEndPList;
    }
    /** This API returns windows azure endpoint */
    public WindowsAzureEndpoint_v16 getEndpoint(String endPointName) throws WindowsAzureInvalidProjectOperationException_v16 {

        WindowsAzureEndpoint_v16 windowsAzureEndpoint = null ;
         List<WindowsAzureEndpoint_v16> endPoints = this.getEndpoints();
        if(endPoints != null) {
            for (int index = 0; index < endPoints.size();index++) {
                if (endPoints.get(index).getInputEndpointName().equalsIgnoreCase(endPointName)
                    || endPoints.get(index).getInternalEndpointName().equalsIgnoreCase(endPointName)) {
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
    public WindowsAzureEndpoint_v16 addEndpoint(String endpointName,
            WindowsAzureEndpointType_v16 endpointType, String localPortNumber,
            String externPortNo)
                    throws WindowsAzureInvalidProjectOperationException_v16 {

        if ((endpointName == null) || (endpointType == null)
                || (localPortNumber == null) || (externPortNo == null)) {
            throw new IllegalArgumentException();
        }

        WindowsAzureEndpoint_v16 newEndPoint = new WindowsAzureEndpoint_v16(
                getWinProjMgr(), this);
        try {
            // newEndPoint.set
            Document doc = getWinProjMgr().getdefinitionFileDoc();
            XPath xPath = XPathFactory.newInstance().newXPath();

            String expr1 = String.format(WindowsAzureConstants_v16.WR_NAME,
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
            String expr = String.format(WindowsAzureConstants_v16.ENDPOINT_WR_NAME,
                    getName());
            Node endPoint;
            endPoint = (Node) xPath.evaluate(expr, doc, XPathConstants.NODE);
            if (endpointType == WindowsAzureEndpointType_v16.Input) {
                Element eleInputEndpoint = doc.createElement("InputEndpoint");
                eleInputEndpoint.setAttribute(
                        WindowsAzureConstants_v16.ATTR_NAME, endpointName);
                eleInputEndpoint.setAttribute("port", externPortNo);
                eleInputEndpoint.setAttribute("localPort", localPortNumber);
                eleInputEndpoint.setAttribute("protocol", "tcp");
                endPoint.appendChild(eleInputEndpoint);
                newEndPoint.setInputEndpointName(endpointName);
                newEndPoint.setInputLocalPort(localPortNumber);
                newEndPoint.setInputPort(externPortNo);
            }
            if (endpointType == WindowsAzureEndpointType_v16.Internal) {
                Element eleInternalEpt = doc
                        .createElement("InternalEndpoint");
                eleInternalEpt.setAttribute(
                        WindowsAzureConstants_v16.ATTR_NAME, endpointName);
                eleInternalEpt.setAttribute("protocol", "tcp");
                Node node = endPoint.appendChild(eleInternalEpt);
                Element eleFixedport = doc.createElement("FixedPort");
                eleFixedport.setAttribute("port", localPortNumber);
                node.appendChild(eleFixedport);

                newEndPoint.setInternalEndpointName(endpointName);
                newEndPoint.setInternalFixedPort(localPortNumber);
            }
            winEndPtList.add(newEndPoint);
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_ADD_ENDPOINT, ex);
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
    public Boolean isAvailableEndpointName(String endpointName)
            throws WindowsAzureInvalidProjectOperationException_v16 {
        Boolean isAvlEpName = true;
        try {
            if (endpointName == null) {
                isAvlEpName = false;
            } else if (endpointName.isEmpty()) {
                isAvlEpName = false;
            }
            if (isAvlEpName) {
                List<WindowsAzureRole_v16> roles = getWinProjMgr()
                        .getRoles();
                for (int i = 0; i < roles.size(); i++) {
                    List<WindowsAzureEndpoint_v16> endPoints = roles.get(i)
                            .getEndpoints();
                    for (int nEndpoint = 0; nEndpoint < endPoints.size();
                            nEndpoint++) {
                        if (endPoints.get(nEndpoint).getInputEndpointName()
                                .equalsIgnoreCase(endpointName)
                                || endPoints.get(nEndpoint)
                                .getInternalEndpointName()
                                .equalsIgnoreCase(endpointName)) {
                            isAvlEpName = false;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_RETRIEVING_ENDPOINT_NAME, ex);
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
            WindowsAzureEndpointType_v16 endpointType, String localPortNumber,
            String externPortNo)
                    throws WindowsAzureInvalidProjectOperationException_v16 {
        Boolean isValidEp = true;

        try {
            Boolean ischkLocal = true;
            Boolean ischkExt = true;

            if ((endpointName == null) || (endpointType == null)
                    || (localPortNumber == null) || (externPortNo == null)) {
                isValidEp = false;
            } else if ((endpointType == WindowsAzureEndpointType_v16.Internal)
                    && (localPortNumber.equalsIgnoreCase(externPortNo))) {
                isValidEp = false;
            }
            if (isValidEp) {
                WindowsAzureEndpoint_v16 waEpt = null;
                List<WindowsAzureEndpoint_v16> list =  getEndpoints();
                for (WindowsAzureEndpoint_v16 waEp : list) {
                    if (waEp.getName().equals(endpointName)) {
                        waEpt = waEp;
                    }
                }
                if (null != waEpt) {
                    if (waEpt.getPrivatePort().equalsIgnoreCase(
                            localPortNumber)) {
                        if (localPortNumber.equalsIgnoreCase(externPortNo)) {
                            ischkExt = false;
                        }
                        ischkLocal = false;
                    }
                    if (waEpt.getPort().equalsIgnoreCase(externPortNo)) {
                        if (localPortNumber.equalsIgnoreCase(externPortNo)) {
                            ischkLocal = false;
                        }
                        ischkExt = false;
                    }
                }
                if (ischkLocal && !getWinProjMgr().isValidPort(
                        localPortNumber, endpointType)) {
                    isValidEp = false;
                }
            }
            if (isValidEp) {
                if ((endpointType == WindowsAzureEndpointType_v16.Internal)
                        && (externPortNo.isEmpty())
                        || (localPortNumber.equalsIgnoreCase(externPortNo))) {
                    isValidEp = true;
                } else if (ischkExt && !getWinProjMgr().isValidPort(
                        externPortNo, endpointType)) {
                    isValidEp = false;
                }
            }

        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_RETRIEVING_ENDPOINT_NAME, ex);
        }
        return isValidEp;
    }

    /**
     * Deletes the endpoint from WindowsAzureProjectManager.
     *
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    public void delete()
            throws WindowsAzureInvalidProjectOperationException_v16 {

        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            String[] value = {getName()};

            // Delete from definition file
            Document doc = getWinProjMgr().getdefinitionFileDoc();
            String expr = String.format(WindowsAzureConstants_v16.WR_NAME,
                    getName());
            Node role = (Node) xPath.evaluate(expr, doc, XPathConstants.NODE);
            if (role != null) {
                role.getParentNode().removeChild(role);
            }
            // Delete from Congugration file
            doc = getWinProjMgr().getConfigFileDoc();
            expr = String.format(WindowsAzureConstants_v16.ROLE_NAME, getName());
            role = (Node) xPath.evaluate(expr, doc, XPathConstants.NODE);
            if (role != null) {
                role.getParentNode().removeChild(role);
            }
            // delete from package.xml
            doc = getWinProjMgr().getPackageFileDoc();
            expr = String.format(WindowsAzureConstants_v16.WA_PACK_NAME, getName());
            role = (Node) xPath.evaluate(expr, doc, XPathConstants.NODE);
            if (role != null) {
                role.getParentNode().removeChild(role);
            }
            // Delete folder from HD
            getWinProjMgr().mapActivity.put("delete", value);
            //Add remoteForward to another role
            winProjMgr.addRemoteForwarder();
            winProjMgr.roleList.remove(this);
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_DEL_ROLE, ex);
        }
    }

    /**
     * @return the username .
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    protected String getAccUsername()
            throws WindowsAzureInvalidProjectOperationException_v16 {
        try {
            if (this.accUsername.isEmpty()) {
                XPath xPath = XPathFactory.newInstance().newXPath();
                Document doc = getWinProjMgr().getConfigFileDoc();
                String expr = String.format(
                        WindowsAzureConstants_v16.RA_ROLE_UNAME_VAL, getName());
                String username = xPath.evaluate(expr, doc);
                this.accUsername = username;
            }
            return this.accUsername;
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_GET_UNAME, e);
        }
    }

    /**
     * @param username the username to set
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    protected void setAccUsername(String username)
            throws WindowsAzureInvalidProjectOperationException_v16 {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = getWinProjMgr().getConfigFileDoc();
            String expr = String.format(
                    WindowsAzureConstants_v16.RA_ROLE_UNAME, getName());
            Element eleSetUname = (Element) xPath.evaluate(expr,
                    doc, XPathConstants.NODE);
            if (eleSetUname == null) {
                eleSetUname = doc.createElement("Setting");
                eleSetUname.setAttribute(WindowsAzureConstants_v16.ATTR_NAME,
                        WindowsAzureConstants_v16.REMOTEACCESS_USERNAME);
                eleSetUname.setAttribute("value", username);
                expr = String.format(WindowsAzureConstants_v16.CONFIG_ROLE_SET,
                        getName());
                Element eleConfigSettings = (Element) xPath.evaluate(expr,
                        doc, XPathConstants.NODE);
                eleConfigSettings.appendChild(eleSetUname);
            } else {
                eleSetUname.setAttribute("value", username);
            }
            this.accUsername = username;
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_SET_UNAME, e);
        }
    }

    /**
     * @return the accPassword .
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    protected String getAccPassword()
            throws WindowsAzureInvalidProjectOperationException_v16 {
        try {
            if (this.accPassword.isEmpty()) {
                XPath xPath = XPathFactory.newInstance().newXPath();
                Document doc = getWinProjMgr().getConfigFileDoc();
                String expr = String.format(
                        WindowsAzureConstants_v16.RA_ROLE_PWD_VAL, getName());
                String password = xPath.evaluate(expr, doc);
                this.accPassword = password;
            }
            return this.accPassword;
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_GET_PWD, e);
        }
    }

    /**
     * @param password the password to set
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    protected void setAccPassword(String password)
            throws WindowsAzureInvalidProjectOperationException_v16 {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = getWinProjMgr().getConfigFileDoc();
            String expr = String.format(WindowsAzureConstants_v16.RA_ROLE_PWD,
                    getName());
            Element eleSetPwd = (Element) xPath.evaluate(expr, doc,
                    XPathConstants.NODE);
            if (eleSetPwd == null) {
                eleSetPwd = doc.createElement("Setting");
                eleSetPwd.setAttribute(WindowsAzureConstants_v16.ATTR_NAME,
                        WindowsAzureConstants_v16.REMOTEACCESS_PASSWORD);
                eleSetPwd.setAttribute("value", password);
                expr = String.format(WindowsAzureConstants_v16.CONFIG_ROLE_SET,
                        getName());
                Element eleConfigSettings = (Element) xPath.evaluate(expr, doc,
                        XPathConstants.NODE);
                eleConfigSettings.appendChild(eleSetPwd);
            } else {
                eleSetPwd.setAttribute("value", password);
            }
            this.accPassword = password;
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_SET_PWD, e);
        }
    }

    /**
     * @return the accExpiryDate
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    protected String getAccExpiryDate()
            throws WindowsAzureInvalidProjectOperationException_v16 {
        try {
            if (this.accExpiryDate.isEmpty()) {
                XPath xPath = XPathFactory.newInstance().newXPath();
                Document doc = getWinProjMgr().getConfigFileDoc();
                String expr = String.format(
                        WindowsAzureConstants_v16.RA_ROLE_EXPIRY_VAL, getName());
                String expiry = xPath.evaluate(expr, doc);
                this.accExpiryDate = expiry;
            }
            return this.accExpiryDate;
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_GET_EDATE, e);
        }
    }

    /**
     * @param expiryDate the accExpiryDate to set
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    protected void setAccExpiryDate(String expiryDate)
            throws WindowsAzureInvalidProjectOperationException_v16 {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = getWinProjMgr().getConfigFileDoc();
            String expr = String.format(
                    WindowsAzureConstants_v16.RA_ROLE_EXPIRY,
                    getName());
            Element eleSettingExpiry = (Element) xPath.evaluate(expr, doc,
                    XPathConstants.NODE);
            if (eleSettingExpiry == null) {
                eleSettingExpiry = doc.createElement("Setting");
                eleSettingExpiry.setAttribute(WindowsAzureConstants_v16.ATTR_NAME,
                        WindowsAzureConstants_v16.REMOTEACCESS_EXPIRY);
                eleSettingExpiry.setAttribute("value", expiryDate);
                expr = String.format(WindowsAzureConstants_v16.CONFIG_ROLE_SET,
                        getName());
                Element eleConfigSettings = (Element) xPath.evaluate(expr,
                        doc, XPathConstants.NODE);
                eleConfigSettings.appendChild(eleSettingExpiry);
            } else {
                eleSettingExpiry.setAttribute("value", expiryDate);
            }
            this.accExpiryDate = expiryDate;
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_SET_EDATE, e);
        }
    }

    /**
     * @return the thumbprint .
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    protected String getThumbprint()
            throws WindowsAzureInvalidProjectOperationException_v16 {
        try {
            if (this.certThumbprint.isEmpty()) {
                XPath xPath = XPathFactory.newInstance().newXPath();
                Document doc = getWinProjMgr().getConfigFileDoc();
                String expr = String.format(
                        WindowsAzureConstants_v16.RA_ROLE_TPRINT_TPRINT, getName());
                String thumbprint = xPath.evaluate(expr, doc);
                this.certThumbprint = thumbprint;
            }
            return this.certThumbprint;
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_GET_THUMBP, e);
        }
    }

    /**
     * @param thumbprint the thumbprint to set
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    protected void setThumbprint(String thumbprint)
            throws WindowsAzureInvalidProjectOperationException_v16 {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = getWinProjMgr().getConfigFileDoc();
            String expr = String.format(WindowsAzureConstants_v16.RA_ROLE_FPRINT,
                    getName());
            Element eleCertificate = (Element) xPath.evaluate(expr, doc,
                    XPathConstants.NODE);
            if (eleCertificate == null) {
                eleCertificate = doc.createElement("Certificate");
                eleCertificate.setAttribute(WindowsAzureConstants_v16.ATTR_NAME,
                        WindowsAzureConstants_v16.REMOTEACCESS_FINGERPRINT);
                eleCertificate.setAttribute("thumbprint", thumbprint);
                eleCertificate.setAttribute("thumbprintAlgorithm", "sha1");
                expr = String.format(WindowsAzureConstants_v16.CERT_ROLE,
                        getName());
                Element eleCertificates = (Element) xPath.evaluate(expr, doc,
                        XPathConstants.NODE);
                eleCertificates.appendChild(eleCertificate);
            } else {
                eleCertificate.setAttribute("thumbprint", thumbprint);
            }
            this.certThumbprint = thumbprint;
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_SET_THUMBP, e);
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
    protected void enableDebugging(WindowsAzureEndpoint_v16 endpoint,
            Boolean startSuspended)
                    throws WindowsAzureInvalidProjectOperationException_v16 {
        if ((endpoint == null) || startSuspended == null) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants_v16.INVALID_ARG);
        }
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = getWinProjMgr().getdefinitionFileDoc();


            //Find Startup tag
            String startupWa = String.format(
                    WindowsAzureConstants_v16.STARTUP_WR_NAME,  getName());
            String parentNode = String.format(WindowsAzureConstants_v16.WR_NAME,
                    getName());
            updateOrCreateElement(doc, startupWa, parentNode, "Startup", true,
                    null);

            //find task Tag
            String taskCmd = String.format(
                    WindowsAzureConstants_v16.STARTUP_WITH_STARTUP_CMD, getName());
            Map<String, String> attr = new HashMap<String, String>();
            attr.put("commandLine", WindowsAzureConstants_v16.TASK_CMD_LINE);
            updateOrCreateElement(doc, taskCmd, startupWa, "Task", false, attr);

            //environment tag
            String expr = String.format(WindowsAzureConstants_v16.ENVIRONMENT,
                    getName());
            updateOrCreateElement(doc, expr, taskCmd, "Environment",
                    true, null);


            Element env = (Element) xPath.evaluate(expr, doc,
                    XPathConstants.NODE);

            // find Variable tag, if not present create new
            Element var = (Element) xPath.evaluate("./Variable[@name="
                    + "'_JAVA_OPTIONS']", env, XPathConstants.NODE);
            if (var == null) {
                var = doc.createElement("Variable");
                var.setAttribute(WindowsAzureConstants_v16.ATTR_NAME,
                        WindowsAzureConstants_v16.DBG_ENV_VAR);
                env.appendChild(var);
            }

            String existingStr = var.getAttribute("value");
            String newDbg = getDebugStrWithPortAndSusMode(endpoint.
                    getPrivatePort(), startSuspended);
            String value = changeJavaOptionsVal(existingStr, newDbg);
            var.setAttribute("value", value);
            envVarMap.put(WindowsAzureConstants_v16.DBG_ENV_VAR, value);
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP, ex);
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
            throws WindowsAzureInvalidProjectOperationException_v16 {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = getWinProjMgr().getdefinitionFileDoc();
            String expr = String.format(WindowsAzureConstants_v16.VARIABLE_JOP,
                    getName());
            Element var = (Element) xPath.evaluate(expr, doc,
                    XPathConstants.NODE);

            if (var != null) {
                String existingStr = var.getAttribute("value");
                //remove -agentlib option from java_option
                String newVal = changeJavaOptionsVal(existingStr, "");
                //if no other options specified remove variable tab
                if (newVal.isEmpty()) {
                    var.getParentNode().removeChild(var);
                    envVarMap.remove(WindowsAzureConstants_v16.DBG_ENV_VAR);
                } else {
                    var.setAttribute("value", newVal);
                    envVarMap.put(WindowsAzureConstants_v16.DBG_ENV_VAR, newVal);
                }
            }

        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP, ex);
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
            Document doc = getWinProjMgr().getdefinitionFileDoc();
            String expr = String.format(WindowsAzureConstants_v16.VARIABLE_JOP,
                    getName()).concat("/@value");
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
    public WindowsAzureEndpoint_v16 getDebuggingEndpoint()
            throws WindowsAzureInvalidProjectOperationException_v16 {
        try {
            String port = "";
            String agentLib = "-agentlib:jdwp=";
            WindowsAzureEndpoint_v16 endPt = null;

            if (getDebuggingEnabled()) {
                XPath xPath = XPathFactory.newInstance().newXPath();
                Document doc = getWinProjMgr().getdefinitionFileDoc();
                String expr = String.format(WindowsAzureConstants_v16.VARIABLE_JOP,
                        getName()) + "/@value";
                String val = xPath.evaluate(expr, doc);

                if (val.isEmpty()) {
                    throw new WindowsAzureInvalidProjectOperationException_v16(
                            WindowsAzureConstants_v16.DBG_NOT_ENABLED);
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
                List<WindowsAzureEndpoint_v16> epList = getEndpoints();
                for (WindowsAzureEndpoint_v16 waEndpt : epList) {
                    if (port.equals(waEndpt.getPrivatePort())) {
                        endPt = waEndpt;
                        break;
                    }
                }
                //if port number does not match with any endpoint,
                //should throw an exception
                if (endPt == null) {
                    throw new WindowsAzureInvalidProjectOperationException_v16(
                            "Port specified in debugging is " +
                            "not a valid port for any endpoint");
                }
            }
            return endPt;
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP, ex);
        }

    }

    /**
     * This API sets the local port of the WindowsAzureEndpoint object as the
     * address subsetting of the –agentlib:jdwp setting.
     * Throws an exception if debugging is not enabled.
     * @param endPoint .
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    public void setDebuggingEndpoint(WindowsAzureEndpoint_v16 endPoint)
            throws WindowsAzureInvalidProjectOperationException_v16 {
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
                Document doc = getWinProjMgr().getdefinitionFileDoc();
                String expr = String.format(WindowsAzureConstants_v16.VARIABLE_JOP,
                        getName()) + "/@value";
                String existingStr = xPath.evaluate(expr, doc);
                String dbgStr = getDebugString(existingStr);

                if (dbgStr.isEmpty()) {
                    throw new WindowsAzureInvalidProjectOperationException_v16(
                            WindowsAzureConstants_v16.DBG_NOT_ENABLED);
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
                expr = String.format(WindowsAzureConstants_v16.VARIABLE_JOP,
                        getName());
                Element var = (Element) xPath.evaluate(expr, doc,
                        XPathConstants.NODE);
                String value = changeJavaOptionsVal(existingStr, newVal);
                var.setAttribute("value", value);

                envVarMap.put(WindowsAzureConstants_v16.DBG_ENV_VAR, value);

            } catch (Exception ex) {
                throw new WindowsAzureInvalidProjectOperationException_v16(
                        WindowsAzureConstants_v16.EXCP, ex);
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
            throws WindowsAzureInvalidProjectOperationException_v16 {
        try {
            Boolean status = false;
            String agentLib = "-agentlib:jdwp=";
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = getWinProjMgr().getdefinitionFileDoc();
            String expr = String.format(WindowsAzureConstants_v16.VARIABLE_JOP,
                    getName()) + "/@value";
            String existingStr = xPath.evaluate(expr, doc);
            String dbgStr = getDebugString(existingStr);

            if (dbgStr.isEmpty()) {
                throw new WindowsAzureInvalidProjectOperationException_v16(
                        WindowsAzureConstants_v16.DBG_NOT_ENABLED);
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
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP, ex);
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
            throws WindowsAzureInvalidProjectOperationException_v16 {
        if (null == status) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants_v16.INVALID_ARG);
        }
        try {
            String agentLib = "-agentlib:jdwp=";
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = getWinProjMgr().getdefinitionFileDoc();
            String expr = String.format(WindowsAzureConstants_v16.VARIABLE_JOP,
                    getName()) + "/@value";
            String existingStr = xPath.evaluate(expr, doc);
            String dbgStr = getDebugString(existingStr);

            if (dbgStr.isEmpty()) {
                throw new WindowsAzureInvalidProjectOperationException_v16(
                        WindowsAzureConstants_v16.DBG_NOT_ENABLED);
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
            expr = String.format(WindowsAzureConstants_v16.VARIABLE_JOP,
                    getName());
            Element var = (Element) xPath.evaluate(expr, doc,
                    XPathConstants.NODE);
            String value = changeJavaOptionsVal(existingStr, newVal);
            var.setAttribute("value", value);
            envVarMap.put(WindowsAzureConstants_v16.DBG_ENV_VAR, value);
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP, ex);
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
        String str = WindowsAzureConstants_v16.DBG_STR;
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
            throws WindowsAzureInvalidProjectOperationException_v16 {

        try {
            if(envVarMap.isEmpty()) {
                XPath xPath = XPathFactory.newInstance().newXPath();
                Document doc = getWinProjMgr().getdefinitionFileDoc();
                String expr = String.format(WindowsAzureConstants_v16.VARIABLE,
                        getName());
                NodeList varList = (NodeList) xPath.evaluate(expr, doc,
                        XPathConstants.NODESET);
                for (int i = 0; i < varList.getLength(); i++) {
                    Element var =  (Element) varList.item(i);
                    envVarMap.put(var.getAttribute(WindowsAzureConstants_v16.ATTR_NAME),
                            var.getAttribute(WindowsAzureConstants_v16.ATTR_VALUE));
                }
            }
        } catch (Exception ex) {
            new WindowsAzureInvalidProjectOperationException_v16(
                    "Exception while getting RunTinme env variables: ", ex);
        }
        return envVarMap;
    }

    /**
     * This API is to return the value of a specific variable.
     * @param name .
     * @return value.
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    public String getRuntimeEnv(String name)
            throws WindowsAzureInvalidProjectOperationException_v16 {
        String value = "";
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants_v16.INVALID_ARG);
        }
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = getWinProjMgr().getdefinitionFileDoc();
            String expr = String.format(WindowsAzureConstants_v16
                    .VAR_WITH_SPECIFIC_NAME,
                    getName(), name)  + "/@value";
            value = xPath.evaluate(expr, doc);
        } catch (Exception ex) {
            new WindowsAzureInvalidProjectOperationException_v16(
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
    public void setRuntimeEnv(String name, String value)
            throws WindowsAzureInvalidProjectOperationException_v16 {
        //String value = "";
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants_v16.INVALID_ARG);
        }

        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = getWinProjMgr().getdefinitionFileDoc();

            //Find Startup tag
            String startupWa = String.format(
                    WindowsAzureConstants_v16.STARTUP_WR_NAME, getName());
            String parentNode = String.format(WindowsAzureConstants_v16.WR_NAME,
                    getName());
            updateOrCreateElement(doc, startupWa, parentNode, "Startup", true,
                    null);

            //find task Tag
            String taskCmd = String.format(
                    WindowsAzureConstants_v16.STARTUP_WITH_STARTUP_CMD, getName());
            Map<String, String> attr = new HashMap<String, String>();
            attr.put("commandLine", WindowsAzureConstants_v16.TASK_CMD_LINE);
            updateOrCreateElement(doc, taskCmd, startupWa, "Task", false, attr);

            //environment tag
            String expr = String.format(WindowsAzureConstants_v16.ENVIRONMENT,
                    getName());
            updateOrCreateElement(doc, expr, taskCmd, "Environment", true, null);

            Element env = (Element) xPath.evaluate(expr, doc,
                    XPathConstants.NODE);

            // find Variable tag, if not present create new
            Element var = (Element) xPath.evaluate(
                    String.format("./Variable[@name='%s']", name),
                    env, XPathConstants.NODE);
            if (var == null) {
                var = doc.createElement("Variable");
                var.setAttribute(WindowsAzureConstants_v16.ATTR_NAME, name);
                env.appendChild(var);
            }

            envVarMap.put(name, value);

            if (value.isEmpty()) {
                var.removeAttribute(value);
            } else {
                var.setAttribute(WindowsAzureConstants_v16.ATTR_VALUE, value);
            }

        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP, ex);
        }
    }


    /**
     * This API delete a specific variable.
     * @param oldName .
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    public void renameRuntimeEnv(String oldName, String newName)
            throws WindowsAzureInvalidProjectOperationException_v16 {
        if ((null == oldName) || oldName.isEmpty() || (newName == null)) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants_v16.INVALID_ARG);
        }

        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = getWinProjMgr().getdefinitionFileDoc();
            String expr = String.format(WindowsAzureConstants_v16
                    .VAR_WITH_SPECIFIC_NAME,
                    getName(), oldName);
            Element var = (Element) xPath.evaluate(expr, doc,
                    XPathConstants.NODE);
            if (var == null) {
                throw new WindowsAzureInvalidProjectOperationException_v16(
                        oldName + " variable does not exist" );
            }
            if (newName.isEmpty()) {
                envVarMap.remove(oldName);
                var.getParentNode().removeChild(var);
            } else {
                String val = envVarMap.get(oldName);
                var.setAttribute("name", newName);
                envVarMap.remove(oldName);
                envVarMap.put(newName, val);
            }
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP, ex);
        }
    }

    /**
     * This API is for getting to the local storage.
     * @return a name-object list of all the local storage resources.
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    public Map<String, WindowsAzureLocalStorage_v16> getLocalStorage()
            throws WindowsAzureInvalidProjectOperationException_v16 {

        try {
            if(locStoMap.isEmpty()) {
                XPath xPath = XPathFactory.newInstance().newXPath();
                Document doc = getWinProjMgr().getdefinitionFileDoc();
                String expr = String.format(WindowsAzureConstants_v16.LOCAL_STORAGE,
                        getName());
                NodeList locStoList = (NodeList) xPath.evaluate(expr, doc,
                        XPathConstants.NODESET);
                for (int i = 0; i < locStoList.getLength(); i++) {
                    Element locSto =  (Element) locStoList.item(i);
                    locStoMap.put(locSto.getAttribute(
                            WindowsAzureConstants_v16.ATTR_NAME),
                            getWinLocStObj(locSto));
                }
            }
        } catch (Exception ex) {
            new WindowsAzureInvalidProjectOperationException_v16(
                    "Exception while getting RunTinme env variables: ", ex);
        }
        return locStoMap;
    }


    private WindowsAzureLocalStorage_v16 getWinLocStObj(Element locSto)
            throws WindowsAzureInvalidProjectOperationException_v16 {
        WindowsAzureLocalStorage_v16 winLocSt = null;
        if (locSto != null) {
            winLocSt = new WindowsAzureLocalStorage_v16(winProjMgr, this);
            winLocSt.setName(locSto.getAttribute(
                    WindowsAzureConstants_v16.ATTR_NAME));
            winLocSt.setSize(Integer.parseInt(locSto.getAttribute(
                    WindowsAzureConstants_v16.ATTR_SIZEINMB)));
            winLocSt.setCleanOnRecycle(Boolean.parseBoolean(locSto.getAttribute(
                    WindowsAzureConstants_v16.ATTR_CLE_ON_ROLE_RECYCLE)));
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
    public WindowsAzureLocalStorage_v16 getLocalStorage(String name)
            throws WindowsAzureInvalidProjectOperationException_v16 {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants_v16.INVALID_ARG);
        }
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = getWinProjMgr().getdefinitionFileDoc();
            String expr = String.format(WindowsAzureConstants_v16.LS_NAME,
                    getName(), name);
            Element locSt = (Element) xPath.evaluate(expr, doc,
                    XPathConstants.NODE);
            return getWinLocStObj(locSt);
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    "Exception while getting Local Storage: ", ex);
        }
    }

    public WindowsAzureLocalStorage_v16 addLocalStorage(String name, int size,
            boolean cleanOnRecycle, String pathEnv)
                    throws WindowsAzureInvalidProjectOperationException_v16 {

        if ((name == null || name.isEmpty()) ||
                (size < 1) || (pathEnv == null) || pathEnv.isEmpty()) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants_v16.INVALID_ARG);
        }
        WindowsAzureLocalStorage_v16 winLocSt = null;
        try {

            Document doc = getWinProjMgr().getdefinitionFileDoc();
            XPath xPath = XPathFactory.newInstance().newXPath();

            // Check <LocalResources>,  if not find create new
            //Find RunTime tag, if not present create new
            String expr = String.format(WindowsAzureConstants_v16.LOCAL_RESOURCES,
                    getName());
            Element eleLocRes = (Element) xPath.evaluate(expr, doc,
                    XPathConstants.NODE);

            if (eleLocRes ==  null) {
                String objName = getName();
                eleLocRes = doc.createElement("LocalResources");
                //Append <LocalResources> to <WorkerRole>
                expr = String.format(WindowsAzureConstants_v16.WR_NAME,
                        objName);
                Node role = (Node) xPath.evaluate(expr, doc,
                        XPathConstants.NODE);
                role.appendChild(eleLocRes);
            }

            Element eleLocSto = doc.createElement("LocalStorage");
            eleLocSto.setAttribute(WindowsAzureConstants_v16.ATTR_NAME, name);
            eleLocSto.setAttribute(
                    WindowsAzureConstants_v16.ATTR_CLE_ON_ROLE_RECYCLE,
                    String.valueOf(cleanOnRecycle));
            eleLocSto.setAttribute(WindowsAzureConstants_v16.ATTR_SIZEINMB,
                    String.valueOf(size));
            eleLocRes.appendChild(eleLocSto);
            winLocSt = new WindowsAzureLocalStorage_v16(winProjMgr, this);
            winLocSt.setName(eleLocSto.getAttribute(WindowsAzureConstants_v16.ATTR_NAME));
            winLocSt.setSize(Integer.parseInt(eleLocSto.getAttribute(
                    WindowsAzureConstants_v16.ATTR_SIZEINMB)));
            winLocSt.setCleanOnRecycle(Boolean.parseBoolean(eleLocSto.getAttribute(
                    WindowsAzureConstants_v16.ATTR_CLE_ON_ROLE_RECYCLE)));
            // Changes inside <WorkerRole> element
            winLocSt.setPathEnv(pathEnv);
            locStoMap.put(name, winLocSt);
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    "Exception while adding local storage: ", ex);
        }
        return winLocSt;
    }


    /** This API is for enabling or disabling session affinity */
    public void setSessionAffinityInputEndpoint(WindowsAzureEndpoint_v16 value)
                        throws WindowsAzureInvalidProjectOperationException_v16 {
       if(value != null )
           configureSessionAffinity(value);
       else
           disableSessionAffinity();

    }
    /** This API is for configuring  session affinity */
    private void configureSessionAffinity(WindowsAzureEndpoint_v16 windowsAzureEndpoint)
    throws WindowsAzureInvalidProjectOperationException_v16 {
        // Check if end point is input end point or not
        if(windowsAzureEndpoint == null || (windowsAzureEndpoint.getEndPointType() != WindowsAzureEndpointType_v16.Input))
            throw new WindowsAzureInvalidProjectOperationException_v16(WindowsAzureConstants_v16.EXCP_EMPTY_OR_INVALID_ENDPOINT);


        // Check if sessionAffinity is already enabled, if yes then throw error
        WindowsAzureEndpoint_v16 saEndpt = getSessionAffinityInputEndpoint();
        if(saEndpt != null && saEndpt.getInputEndpointName().equalsIgnoreCase(windowsAzureEndpoint.getInputEndpointName()))
            throw new WindowsAzureInvalidProjectOperationException_v16(WindowsAzureConstants_v16.EXCP_SA_ENABLED);

        // Remove previous configuration except file copy
        if(saEndpt != null ) {
            removeSASettingsFromDefDoc();
            removeSASettingsFromPkgDoc();
        }

        // Generate port and name for SA internal end point.
        int iisArrPort = getSessionAffinityPort();
        String saInternalEndPointName = generateSAInternalEndPointName(windowsAzureEndpoint.getInputEndpointName());
        int stepsCompleted = -1 ;

        try{
            // update or create properties in package.xml
            addSAPropertiesInPackage(windowsAzureEndpoint.getInputEndpointName(),saInternalEndPointName);
            stepsCompleted = WindowsAzureConstants_v16.PACKAGE_DOC_SA_PROPERTIES;

            // add SA settings in service definition file
            addSASettingsInSvcDef(windowsAzureEndpoint,saInternalEndPointName,iisArrPort);
            stepsCompleted = WindowsAzureConstants_v16.DEFINITION_DOC_SA_CHANGES;

			// create SA configuration files
            String[] saInfo = {this.getName()};
            if(getWinProjMgr().mapActivity.get("addSAFilesForRole") != null)
                getWinProjMgr().mapActivity.remove("addSAFilesForRole");
            if(getWinProjMgr().mapActivity.get("delSAFilesForRole") != null)
                getWinProjMgr().mapActivity.remove("delSAFilesForRole");
            getWinProjMgr().mapActivity.put("addSAFilesForRole", saInfo);
            stepsCompleted = WindowsAzureConstants_v16.SA_FILES_COPIED;

        }catch(Exception e) {
            handleRollback(stepsCompleted);
            throw new WindowsAzureInvalidProjectOperationException_v16(e.getMessage(),e);
         }

    }

    /** Adds or updates properties in package.xml. */
    private void addSAPropertiesInPackage(String inputEndPointName, String internalEndPointName)
    throws WindowsAzureInvalidProjectOperationException_v16 {
        Document packageFileDoc = getWinProjMgr().getPackageFileDoc();

        if(packageFileDoc != null ){
            String nodeExpr       =  String.format(WindowsAzureConstants_v16.SA_INPUT_ENDPOINT,  this.getName());
            String parentNodeExpr =  WindowsAzureConstants_v16.PROJ_PROPERTY;

            HashMap<String,String> nodeAttribites = new HashMap<String, String>();
            nodeAttribites.put(WindowsAzureConstants_v16.ATTR_NAME,String.format(WindowsAzureConstants_v16.SA_INPUT_ENDPOINT_NAME_PROP,this.getName()));
            nodeAttribites.put(WindowsAzureConstants_v16.ATTR_VALUE, inputEndPointName);
            updateOrCreateElement(packageFileDoc,nodeExpr,parentNodeExpr,WindowsAzureConstants_v16.PROJ_PROPERTY_ELEMENT_NAME,false,nodeAttribites);

            nodeExpr = String.format(WindowsAzureConstants_v16.SA_INTERNAL_ENDPOINT,this.getName());
            nodeAttribites.clear();
            nodeAttribites.put(WindowsAzureConstants_v16.ATTR_NAME, String.format(WindowsAzureConstants_v16.SA_INTERNAL_ENDPOINT_NAME_PROP,this.getName()));
            nodeAttribites.put(WindowsAzureConstants_v16.ATTR_VALUE, internalEndPointName);
            updateOrCreateElement(packageFileDoc,nodeExpr,parentNodeExpr,WindowsAzureConstants_v16.PROJ_PROPERTY_ELEMENT_NAME,false,nodeAttribites);
        }
    }

    /** This method adds Session Affinity settings in service definition */
    private void addSASettingsInSvcDef(WindowsAzureEndpoint_v16 inpEndPt,String intEndPt, int iisArrPort)
    throws WindowsAzureInvalidProjectOperationException_v16 {
        // Add startup task entry
        Document definitionFiledoc  = getWinProjMgr().getdefinitionFileDoc();
        String   nodeExpr           = String.format(WindowsAzureConstants_v16.STARTUP_WR_NAME,getName());
        String   parentNodeExpr     = String.format(WindowsAzureConstants_v16.WR_NAME, this.getName()) ;
        updateOrCreateElement(definitionFiledoc,nodeExpr,parentNodeExpr,WindowsAzureConstants_v16.DEF_FILE_STARTUP_ELEMENT_NAME,true,null);

        String taskNodeExpr     = String.format(WindowsAzureConstants_v16.STARTUP_TASK_CMD,this.getName(),WindowsAzureConstants_v16.TASK_CMD_VALUE
                                    ,inpEndPt.getInputEndpointName(),intEndPt);
        HashMap<String,String> nodeAttributes = new HashMap<String, String>();
        nodeAttributes.put("commandLine", String.format(WindowsAzureConstants_v16.TASK_CMD_VALUE, inpEndPt.getInputEndpointName(),intEndPt));
        nodeAttributes.put("executionContext", "elevated");
        nodeAttributes.put("taskType", "simple");
        Element element = updateOrCreateElement(definitionFiledoc,taskNodeExpr,nodeExpr,WindowsAzureConstants_v16.DEF_FILE_TASK_ELEMENT_NAME,true,nodeAttributes);

        //Add environment element
        element = createElement(definitionFiledoc,null,element,WindowsAzureConstants_v16.DEF_FILE_ENV_NAME,false,null);

        //Add variable element
        nodeAttributes.clear();
        nodeAttributes.put("name", "EMULATED");
        element = createElement(definitionFiledoc,null,element,WindowsAzureConstants_v16.DEF_FILE_VAR_ELE_NAME,false,nodeAttributes);

        //Add xpath expression role instance value
        nodeAttributes.clear();
        nodeAttributes.put("xpath", "/RoleEnvironment/Deployment/@emulated");
        element = createElement(definitionFiledoc,null,element,WindowsAzureConstants_v16.DEF_FILE_ENV_RIV_NAME,false,nodeAttributes);


        // Add comments to startup task element to warn the user not to insert any task before this.
        createCommentNode(WindowsAzureConstants_v16.STARTUP_TASK_COMMENTS,definitionFiledoc,nodeExpr);

        // Add SA input end point and SA internal end point
        String inputEndPointLocalPort = inpEndPt.getInputLocalPort();
        nodeExpr  = String.format(WindowsAzureConstants_v16.INPUT_ENDPOINT,inpEndPt.getInputEndpointName());
        int index = winEndPtList.indexOf(inpEndPt);
        winEndPtList.get(index).setPrivatePort(String.valueOf(iisArrPort));
        addEndpoint(intEndPt, WindowsAzureEndpointType_v16.Internal, inputEndPointLocalPort, "");
       
    }
    
    /** Adds comments as a first node */
    private void createCommentNode(String startupTaskComments,Document doc,String nodeExpr)
    throws WindowsAzureInvalidProjectOperationException_v16 {
        try {
            Comment taskComment = doc.createComment(WindowsAzureConstants_v16.STARTUP_TASK_COMMENTS);
            XPath xPath         = XPathFactory.newInstance().newXPath();
            Element element     = (Element) xPath.evaluate(nodeExpr, doc,XPathConstants.NODE);
            element.insertBefore(taskComment, element != null? element.getFirstChild():null);
        }catch(Exception e ){
            throw new WindowsAzureInvalidProjectOperationException_v16(WindowsAzureConstants_v16.EXCP_COMMENT_NODE,e);
        }
    }

    /** This method changes session affinity end point info in start up task if there is a name change */
    private void changeEndPointInfoInStartupTask(String inputEndPointNAme, String internalEndPointName)
    throws WindowsAzureInvalidProjectOperationException_v16 {

            String taskNodeExpr = String.format(WindowsAzureConstants_v16.STARTUP_TASK_STARTS_WITH,this.getName(),
                                  WindowsAzureConstants_v16.TASK_CMD_ONLY);
            String   parentNodeExpr           = String.format(WindowsAzureConstants_v16.STARTUP_WR_NAME,getName());
            Document definitionFiledoc  = getWinProjMgr().getdefinitionFileDoc();
            HashMap<String,String> nodeAttributes = new HashMap<String, String>();

            nodeAttributes.put("commandLine", String.format(WindowsAzureConstants_v16.TASK_CMD_VALUE, inputEndPointNAme,internalEndPointName));
            nodeAttributes.put("executionContext", "elevated");
            nodeAttributes.put("taskType", "simple");
            updateOrCreateElement(definitionFiledoc,taskNodeExpr,parentNodeExpr,WindowsAzureConstants_v16.DEF_FILE_TASK_ELEMENT_NAME,true,nodeAttributes);

    }

    /** API to handle rollback logic. */
    private void handleRollback(int stepsCompleted) {
        switch (stepsCompleted) {
            case WindowsAzureConstants_v16.PACKAGE_DOC_SA_PROPERTIES:
               removeSASettingsFromPkgDoc();
               break;


            case WindowsAzureConstants_v16.DEFINITION_DOC_SA_CHANGES:
                removeSASettingsFromDefDoc();
                removeSASettingsFromPkgDoc();
                break;

            case WindowsAzureConstants_v16.SA_FILES_COPIED:
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
            String expr = String.format(WindowsAzureConstants_v16.STARTUP_TASK_STARTS_WITH,this.getName(),
                          WindowsAzureConstants_v16.TASK_CMD_ONLY);
            deleteElement(definitionFiledoc, expr);

            // Delete Comments child node
            String nodeExpr = String.format(WindowsAzureConstants_v16.STARTUP_WR_NAME,getName());
            deleteCommentNode(WindowsAzureConstants_v16.STARTUP_TASK_COMMENTS,definitionFiledoc,nodeExpr) ;

            WindowsAzureEndpoint_v16 windowsAzureEndpoint = getSessionAffinityInternalEndpoint();
            String port = windowsAzureEndpoint.getInternalFixedPort();
            windowsAzureEndpoint.delete();

            windowsAzureEndpoint = getSessionAffinityInputEndpoint() ;
            windowsAzureEndpoint.setInputLocalPort(port);
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
            String[] saInfo = {this.getName()};
            if(getWinProjMgr().mapActivity.get("addSAFilesForRole") != null)
                getWinProjMgr().mapActivity.remove("addSAFilesForRole");
            if(getWinProjMgr().mapActivity.get("delSAFilesForRole") != null)
                getWinProjMgr().mapActivity.remove("delSAFilesForRole");
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
                String inputEndPointExpr =  String.format(WindowsAzureConstants_v16.SA_INPUT_ENDPOINT,this.getName());
                deleteElement(packageFileDoc,inputEndPointExpr);

                String internalEndPointExpr = String.format(WindowsAzureConstants_v16.SA_INTERNAL_ENDPOINT,this.getName());
                deleteElement(packageFileDoc,internalEndPointExpr);
             }
         }catch(Exception e) {
             //Die silently , there is nothing much that can be done at this point
         }
    }

    /** This API is for disabling session affinity */
    private void disableSessionAffinity() throws WindowsAzureInvalidProjectOperationException_v16 {
        removeSAFilesFromPrj();
        removeSASettingsFromDefDoc();
        removeSASettingsFromPkgDoc();
    }

    /** Get Session Affinity IIS port. By default this returns 31221 if not available then
        returns next available one */
    private int  getSessionAffinityPort() throws WindowsAzureInvalidProjectOperationException_v16 {
        int port = WindowsAzureConstants_v16.IIS_ARR_PORT ;

        while(!winProjMgr.isValidPort(port+"", WindowsAzureEndpointType_v16.Input)){
                port++ ;
        }
        return port ;
    }

    /** This method suffixes a string to the name and checks if that name is available for endpoint */
    private String generateSAInternalEndPointName(String name) throws WindowsAzureInvalidProjectOperationException_v16{
        StringBuilder saInternalEndPoint = new StringBuilder(name);
        saInternalEndPoint.append(WindowsAzureConstants_v16.SA_INTERNAL_ENDPOINT_SUFFIX);

        int sufIncrement = 1 ;
        while(!winProjMgr.isAvailableRoleName(saInternalEndPoint.toString())) {
            saInternalEndPoint.append(sufIncrement);
            sufIncrement++ ;
        }
        return saInternalEndPoint.toString() ;
    }

    /** Generic API to update or create DOM elements */
    private Element updateOrCreateElement(Document doc ,String expr,String parentNodeExpr,String elementName,boolean firstChild,
                                        Map<String,String> attributes )
    throws WindowsAzureInvalidProjectOperationException_v16 {

        if(doc == null ) {
            throw new IllegalArgumentException(WindowsAzureConstants_v16.INVALID_ARG);
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
                throw new WindowsAzureInvalidProjectOperationException_v16(WindowsAzureConstants_v16.EXCP_UPDATE_OR_CREATE_ELEMENT, e);
            }
        }
    }


    /** Generic API to update or create DOM elements */
    private Element createElement(Document doc ,String expr,Element parentElement,String elementName,boolean firstChild,
                                        Map<String,String> attributes )
    throws WindowsAzureInvalidProjectOperationException_v16 {

        if(doc == null ) {
            throw new IllegalArgumentException(WindowsAzureConstants_v16.INVALID_ARG);
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
                throw new WindowsAzureInvalidProjectOperationException_v16(WindowsAzureConstants_v16.EXCP_UPDATE_OR_CREATE_ELEMENT, e);
            }
        }
    }


    /** Generic API to delete elements from DOM */
    private void deleteElement(Document doc ,String expr) throws XPathExpressionException {
        if(doc == null ) {
            throw new IllegalArgumentException(WindowsAzureConstants_v16.INVALID_ARG);
        } else {
             XPath xPath = XPathFactory.newInstance().newXPath();
             Element element = (Element) xPath.evaluate(expr, doc,XPathConstants.NODE);

             if(element != null ) {
                 Node parentNode = element.getParentNode() ;
                 parentNode.removeChild(element);
             }

        }
    }


    /** Returns Session Affinity input endpoint */
    public WindowsAzureEndpoint_v16 getSessionAffinityInputEndpoint() throws WindowsAzureInvalidProjectOperationException_v16 {
        return getSessionAffinityEndpoint(WindowsAzureEndpointType_v16.Input);
    }

    /** Returns session affinity internal endpoint */
    public WindowsAzureEndpoint_v16 getSessionAffinityInternalEndpoint() throws WindowsAzureInvalidProjectOperationException_v16 {
        return getSessionAffinityEndpoint(WindowsAzureEndpointType_v16.Internal);
    }
    /** Returns endpoint associated with session affinity */
    private WindowsAzureEndpoint_v16 getSessionAffinityEndpoint(WindowsAzureEndpointType_v16 windowsAzureEndpointType)
    throws WindowsAzureInvalidProjectOperationException_v16 {

        WindowsAzureEndpoint_v16 windowsAzureEndpoint = null;
        try{
          Document packageFileDoc = getWinProjMgr().getPackageFileDoc();
          if(packageFileDoc != null) {
              XPath xPath = XPathFactory.newInstance().newXPath();
              String endPointExpr = null ;
              if(windowsAzureEndpointType == WindowsAzureEndpointType_v16.Input) {
                  endPointExpr = String.format(WindowsAzureConstants_v16.SA_INPUT_ENDPOINT, this.getName()) ;
              } else {
                  endPointExpr = String.format(WindowsAzureConstants_v16.SA_INTERNAL_ENDPOINT, this.getName()) ;
              }
              Element propEndPoint = (Element) xPath.evaluate(endPointExpr, packageFileDoc,XPathConstants.NODE);
              if(propEndPoint != null ) {
                  windowsAzureEndpoint = getEndpoint(propEndPoint.getAttribute("value")) ;
              }
          }
        }catch(Exception e ){
            throw new WindowsAzureInvalidProjectOperationException_v16("Internal error occured while fetching endpoint info from package.xml",e);
        }

        return windowsAzureEndpoint ;

    }

    /** This API changes session affinity settings in startup task and package.xml if session affinity end point changes */
    protected void reconfigureSessionAffinity(WindowsAzureEndpoint_v16 value,String newName) throws WindowsAzureInvalidProjectOperationException_v16 {
        if(value == null ) {
            throw new IllegalArgumentException(WindowsAzureConstants_v16.EXCP_EMPTY_OR_INVALID_ENDPOINT);
        }

        String endPointName = null ;
        if(value.getEndPointType() == WindowsAzureEndpointType_v16.Input)
            endPointName = getSessionAffinityInternalEndpoint().getInternalEndpointName() ;
        else
            endPointName = getSessionAffinityInputEndpoint().getInputEndpointName();

        Document packageFileDoc = getWinProjMgr().getPackageFileDoc();

        try{
           if(packageFileDoc != null) {
                XPath xPath = XPathFactory.newInstance().newXPath();
                String endPointExpr = null ;
                // Change in startup task
                if(value.getEndPointType() == WindowsAzureEndpointType_v16.Input) {
                    endPointExpr = String.format(WindowsAzureConstants_v16.SA_INPUT_ENDPOINT, this.getName()) ;
                    changeEndPointInfoInStartupTask(newName,endPointName);
                }else if(value.getEndPointType() == WindowsAzureEndpointType_v16.Internal) {
                    endPointExpr = String.format(WindowsAzureConstants_v16.SA_INTERNAL_ENDPOINT, this.getName()) ;
                    changeEndPointInfoInStartupTask(endPointName,newName);
                }
                // Change in package.xml
                Element propEndPoint = (Element) xPath.evaluate(endPointExpr, packageFileDoc,XPathConstants.NODE);
                if(propEndPoint != null)
                    propEndPoint.setAttribute("value", newName);
            }
         }catch(Exception e ){
             throw new WindowsAzureInvalidProjectOperationException_v16(WindowsAzureConstants_v16.EXCP_SA_NAME_CHANGE,e);
         }


     }



}
