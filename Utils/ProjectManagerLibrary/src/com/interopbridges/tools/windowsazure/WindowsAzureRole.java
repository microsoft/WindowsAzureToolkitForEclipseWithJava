/**
 * Copyright (c) Microsoft Corporation
 * 
 * All rights reserved. 
 * 
 * MIT License
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files 
 * (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, 
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH 
 * THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.interopbridges.tools.windowsazure;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
 * Class representing Azure role.
 */
public class WindowsAzureRole {
	private String name;
	private String instances;
	private String vmSize;
	private String accUsername = "";
	private String accPassword = "";
	private String accExpiryDate = "";
	private String certThumbprint = "";
	private final int HTTP_PORT = 80;
	private final int HTTPS_PORT = 443;

	private List<WindowsAzureEndpoint> winEndPtList = new ArrayList<WindowsAzureEndpoint>();
	private WindowsAzureProjectManager winProjMgr = null;
	protected Map<String, String> envVarMap = new HashMap<String, String>();
	protected List<String> lsVarList = new ArrayList<String>();
	protected Map<String, WindowsAzureLocalStorage> locStoMap = new HashMap<String, WindowsAzureLocalStorage>();
	protected List<WindowsAzureRoleComponent> winCompList = new ArrayList<WindowsAzureRoleComponent>();
	protected Map<String, WindowsAzureNamedCache> cacheMap = new HashMap<String, WindowsAzureNamedCache>();
	protected Map<String, WindowsAzureCertificate> certMap = new HashMap<String, WindowsAzureCertificate>();

	protected WindowsAzureProjectManager getWinProjMgr()
			throws WindowsAzureInvalidProjectOperationException {
		if (winProjMgr == null) {
			throw new WindowsAzureInvalidProjectOperationException(
					WindowsAzureConstants.PMGR_NULL);
		}
		return winProjMgr;
	}

	public WindowsAzureRole(WindowsAzureProjectManager winPrjMgr) {
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
	 * @param name
	 *            .
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
					getWinProjMgr().projDirPath, File.separator, getName());
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

				// if already rename option done on same role and not yet saved,
				// need to retrieve original name of folder and
				// add it in map with new changed name.
				if (getWinProjMgr().mapActivity.containsKey("rename")) {
					Vector<String> oldVal = getWinProjMgr().mapActivity
							.get("rename");
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
			String expr = String.format(WindowsAzureConstants.WR_NAME,
					getName());
			Node role = (Node) xPath.evaluate(expr, doc, XPathConstants.NODE);

			if (role != null) {
				role.getAttributes()
						.getNamedItem(WindowsAzureConstants.ATTR_NAME)
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
				String expr = String.format(WindowsAzureConstants.ROLE_NAME,
						getName());
				Node role = (Node) xPath.evaluate(expr, configDoc,
						XPathConstants.NODE);
				role.getAttributes()
						.getNamedItem(WindowsAzureConstants.ATTR_NAME)
						.setNodeValue(name);
			}
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception in setNameInConfigFile", ex);
		}
	}

	private void setNameInPackageFile(String name)
			throws WindowsAzureInvalidProjectOperationException {
		try {
			XPath xPath = XPathFactory.newInstance().newXPath();
			Document projDoc = getWinProjMgr().getPackageFileDoc();
			String expr = WindowsAzureConstants.PROJ_PROPERTIES;
			NodeList properties = (NodeList) xPath.evaluate(expr, projDoc,
					XPathConstants.NODESET);
			for (int i = 0; i < properties.getLength(); i++) {
				Element property = (Element) properties.item(i);
				String attrVal = property.getAttribute("name");
				if (attrVal.startsWith("project." + getName())) {
					String newProVal = attrVal.replace(getName(), name);
					property.setAttribute("name", newProVal);
				}
			}

			expr = String.format(WindowsAzureConstants.WA_PACK_NAME, getName());
			Node role = (Node) xPath.evaluate(expr, projDoc,
					XPathConstants.NODE);
			if (role != null) {
				role.getAttributes()
						.getNamedItem(WindowsAzureConstants.ATTR_NAME)
						.setNodeValue(name);
				role.getAttributes()
						.getNamedItem("approotdir")
						.setNodeValue(
								String.format("%s%s%s", "${basedir}\\", name,
										"\\approot"));
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
			String expr = "";
			Document doc = null;
			XPath xPath = XPathFactory.newInstance().newXPath();
			String count = "";
			if (winProjMgr.getPackageType().equals(
					WindowsAzurePackageType.LOCAL)
					&& getServerName() != null) {
				String instPro = String.format(
						WindowsAzureConstants.INSTANCE_PROPERTY, getName());
				expr = String.format(WindowsAzureConstants.ROLE_PROP_VAL,
						instPro);
				doc = getWinProjMgr().getPackageFileDoc();
				count = xPath.evaluate(expr, doc);
			}
			if (!count.isEmpty()) {
				this.instances = count;
			} else {
				expr = String.format(WindowsAzureConstants.ROLE_COUNT,
						getName());
				doc = getWinProjMgr().getConfigFileDoc();
				this.instances = xPath.evaluate(expr, doc);
			}
			return this.instances;
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					WindowsAzureConstants.EXCP_GET_INSTANCES, ex);
		}
	}

	/**
	 * Sets the instance count.
	 * 
	 * @param count
	 *            .
	 * @throws WindowsAzureInvalidProjectOperationException .
	 */
	public void setInstances(String count)
			throws WindowsAzureInvalidProjectOperationException {
		if ((null == count) || (count.isEmpty())) {
			throw new IllegalArgumentException(
					WindowsAzureConstants.INVALID_ARG);
		}
		try {
			String iCount = count;
			String instPro = String.format(
					WindowsAzureConstants.INSTANCE_PROPERTY, getName());
			if (winProjMgr.getPackageType().equals(
					WindowsAzurePackageType.LOCAL)
					&& getServerName() != null && !count.equals("1")) {
				iCount = "1";
				addPropertyInPackageXML(instPro, count);
			} else {
				String expr = String.format(WindowsAzureConstants.ROLE_PROP,
						instPro);
				Document doc = getWinProjMgr().getPackageFileDoc();
				ParserXMLUtility.deleteElement(doc, expr);
			}

			XPath xPath = XPathFactory.newInstance().newXPath();
			Document configDoc = getWinProjMgr().getConfigFileDoc();
			String expr = String.format(WindowsAzureConstants.ROLE_INSTANCES,
					getName());
			Element role = (Element) xPath.evaluate(expr, configDoc,
					XPathConstants.NODE);
			role.setAttribute("count", iCount);
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
	 * @param vMSize
	 *            .
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
				NodeList endPtList = (NodeList) xPath.evaluate(expr, doc,
						XPathConstants.NODESET);
				for (int i = 0; i < endPtList.getLength(); i++) {
					Element endptEle = (Element) endPtList.item(i);
					winEndPtList.add(createWinInputEndPt(endptEle));
				}

				expr = String.format(WindowsAzureConstants.INTERNAL_WR_NAME,
						objName);
				endPtList = (NodeList) xPath.evaluate(expr, doc,
						XPathConstants.NODESET);
				for (int i = 0; i < endPtList.getLength(); i++) {
					Element endptEle = (Element) endPtList.item(i);
					winEndPtList.add(createWinIntenalEndPt(endptEle));
				}

				expr = String.format(WindowsAzureConstants.INSTANCE_WR_NAME,
						objName);
				endPtList = (NodeList) xPath.evaluate(expr, doc,
						XPathConstants.NODESET);
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

	/**
	 * Method creates endpoint of type Input.
	 * 
	 * @param endptEle
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	private WindowsAzureEndpoint createWinInputEndPt(Element endptEle)
			throws WindowsAzureInvalidProjectOperationException {
		try {
			WindowsAzureEndpoint winAzureEndpoint = new WindowsAzureEndpoint(
					getWinProjMgr(), this);
			winAzureEndpoint.setName(endptEle
					.getAttribute(WindowsAzureConstants.ATTR_NAME));
			winAzureEndpoint.setLocalPort(endptEle.getAttribute("localPort"));
			winAzureEndpoint.setPort(endptEle.getAttribute("port"));
			winAzureEndpoint.setProtocol(endptEle.getAttribute("protocol"));

			String cert = endptEle.getAttribute("certificate");
			if (cert != null && !cert.isEmpty()) {
				winAzureEndpoint.setCertificate(cert);
			}
			return winAzureEndpoint;
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception in createWinInputEndPt ", ex);
		}
	}

	/**
	 * Method creates endpoint of type Instance.
	 * 
	 * @param endptEle
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	private WindowsAzureEndpoint createWinInstanceEndPt(Element endptEle)
			throws WindowsAzureInvalidProjectOperationException {
		try {
			WindowsAzureEndpoint winAzureEndpoint = new WindowsAzureEndpoint(
					getWinProjMgr(), this);
			winAzureEndpoint.setName(endptEle
					.getAttribute(WindowsAzureConstants.ATTR_NAME));
			winAzureEndpoint.setLocalPort(endptEle.getAttribute("localPort"));

			String expr = WindowsAzureConstants.INS_FIX_RANGE;
			XPath xPath = XPathFactory.newInstance().newXPath();
			Element eleFxdPortRan = (Element) xPath.evaluate(expr, endptEle,
					XPathConstants.NODE);

			winAzureEndpoint.setPort(eleFxdPortRan
					.getAttribute(WindowsAzureConstants.ATTR_MINPORT)
					+ "-"
					+ eleFxdPortRan
							.getAttribute(WindowsAzureConstants.ATTR_MAXPORT));
			return winAzureEndpoint;
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception in createWinInstanceEndPt ", ex);
		}
	}

	/**
	 * Method creates endpoint of type Internal.
	 * 
	 * @param endptEle
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	private WindowsAzureEndpoint createWinIntenalEndPt(Element endptEle)
			throws WindowsAzureInvalidProjectOperationException {
		try {
			XPath xPath = XPathFactory.newInstance().newXPath();
			WindowsAzureEndpoint winAzureEndpoint = new WindowsAzureEndpoint(
					getWinProjMgr(), this);
			winAzureEndpoint.setName(endptEle
					.getAttribute(WindowsAzureConstants.ATTR_NAME));
			String port = xPath.evaluate("./FixedPort/@port", endptEle);
			if (port == null || port.isEmpty()) {
				port = String.format("%s-%s",
						xPath.evaluate("./FixedPortRange/@min", endptEle),
						xPath.evaluate("./FixedPortRange/@max", endptEle));
				if (port == null || port.isEmpty() || port.equals("-")) {
					port = null;
				}
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

	/** This API returns azure endpoint */
	public WindowsAzureEndpoint getEndpoint(String endPointName)
			throws WindowsAzureInvalidProjectOperationException {

		if (endPointName == null || endPointName.trim().length() == 0) {
			return null;
		}

		WindowsAzureEndpoint windowsAzureEndpoint = null;
		List<WindowsAzureEndpoint> endPoints = this.getEndpoints();
		if (endPoints != null) {
			for (int index = 0; index < endPoints.size(); index++) {
				if (endPoints.get(index).getName()
						.equalsIgnoreCase(endPointName)) {
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
	 * @param endpointName
	 *            .
	 * @param endpointType
	 *            .
	 * @param localPortNumber
	 *            .
	 * @param externPortNo
	 *            .
	 * @return new instance of WindowsAzureEndpoint.
	 * @throws WindowsAzureInvalidProjectOperationException .
	 */
	public WindowsAzureEndpoint addEndpoint(String endpointName,
			WindowsAzureEndpointType endpointType, String localPortNumber,
			String externPortNo)
			throws WindowsAzureInvalidProjectOperationException {

		if ((endpointName == null) || (endpointType == null)
				|| (externPortNo == null)) {
			throw new IllegalArgumentException();
		}
		if (endpointType.equals(WindowsAzureEndpointType.InstanceInput)
				&& localPortNumber == null) {
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
				if (localPortNumber == null) {
					localPortNumber = "*";
				}
				Element eleInputEndpoint = doc.createElement("InputEndpoint");
				eleInputEndpoint.setAttribute(WindowsAzureConstants.ATTR_NAME,
						endpointName);
				eleInputEndpoint.setAttribute("port", externPortNo);
				eleInputEndpoint.setAttribute("localPort", localPortNumber);
				eleInputEndpoint.setAttribute("protocol", "tcp");
				endPoint.appendChild(eleInputEndpoint);
				newEndPoint.setName(endpointName);
				newEndPoint.setLocalPort(localPortNumber);
				newEndPoint.setPort(externPortNo);
			}
			if (endpointType == WindowsAzureEndpointType.Internal) {
				Element eleInternalEpt = doc.createElement("InternalEndpoint");
				eleInternalEpt.setAttribute(WindowsAzureConstants.ATTR_NAME,
						endpointName);
				eleInternalEpt.setAttribute("protocol", "tcp");
				Node node = endPoint.appendChild(eleInternalEpt);
				if (localPortNumber != null) {
					if (localPortNumber.contains("-")) {
						String[] ports = localPortNumber.split("-");
						String minPort = ports[0];
						String maxPort = ports[1];
						Element eleFxdPortRan = doc
								.createElement("FixedPortRange");
						eleFxdPortRan.setAttribute(
								WindowsAzureConstants.ATTR_MINPORT, minPort);
						eleFxdPortRan.setAttribute(
								WindowsAzureConstants.ATTR_MAXPORT, maxPort);
						node.appendChild(eleFxdPortRan);
					} else {
						Element eleFixedport = doc.createElement("FixedPort");
						eleFixedport.setAttribute("port", localPortNumber);
						node.appendChild(eleFixedport);
					}
				}
				newEndPoint.setName(endpointName);
				newEndPoint.setPrivatePort(localPortNumber);
			}
			if (endpointType == WindowsAzureEndpointType.InstanceInput) {
				String minPort = externPortNo;
				String maxPort = externPortNo;
				if (externPortNo.contains("-")) {
					String[] ports = externPortNo.split("-");
					minPort = ports[0];
					maxPort = ports[1];
				}
				Element eleInstanceNode = doc
						.createElement("InstanceInputEndpoint");
				eleInstanceNode.setAttribute(WindowsAzureConstants.ATTR_NAME,
						endpointName);
				eleInstanceNode.setAttribute("protocol", "tcp");
				eleInstanceNode.setAttribute("localPort", localPortNumber);
				Element eleAllPubPort = doc
						.createElement("AllocatePublicPortFrom");
				Element eleFxdPortRan = doc.createElement("FixedPortRange");
				eleFxdPortRan.setAttribute(WindowsAzureConstants.ATTR_MINPORT,
						minPort);
				eleFxdPortRan.setAttribute(WindowsAzureConstants.ATTR_MAXPORT,
						maxPort);
				eleAllPubPort.appendChild(eleFxdPortRan);
				eleInstanceNode.appendChild(eleAllPubPort);

				endPoint.appendChild(eleInstanceNode);
				setVarInDefFile(endpointName + "_PUBLICPORT");
				String VarExpr = String.format(
						WindowsAzureConstants.VAR_WITH_SPECIFIC_NAME,
						getName(), endpointName + "_PUBLICPORT");
				Element elevar = (Element) xPath.evaluate(VarExpr, doc,
						XPathConstants.NODE);
				Element insval = doc.createElement("RoleInstanceValue");
				String val = String.format(
						WindowsAzureConstants.EP_INSTANCE_VAR, endpointName);
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
	 * @param endpointName
	 *            .
	 * @return true if the specified endpoint name is valid; false otherwise.
	 * @throws WindowsAzureInvalidProjectOperationException .
	 */
	public Boolean isAvailableEndpointName(String endpointName,
			WindowsAzureEndpointType epType)
			throws WindowsAzureInvalidProjectOperationException {
		Boolean isAvlEpName = true;
		try {
			if (endpointName == null) {
				isAvlEpName = false;
			} else if (endpointName.isEmpty()) {
				isAvlEpName = false;
			}
			if (isAvlEpName) {
				if (epType == WindowsAzureEndpointType.Internal) {
					List<WindowsAzureEndpoint> eps = getEndpoints();
					for (WindowsAzureEndpoint ep : eps) {
						if (ep.getName().equalsIgnoreCase(endpointName)) {
							isAvlEpName = false;
						}
					}
				} else {
					List<WindowsAzureRole> roles = getWinProjMgr().getRoles();
					for (int i = 0; i < roles.size(); i++) {
						List<WindowsAzureEndpoint> endPoints = roles.get(i)
								.getEndpoints();
						for (int nEndpoint = 0; nEndpoint < endPoints.size(); nEndpoint++) {
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
	 * @param endpointName
	 *            .
	 * @param endpointType
	 *            .
	 * @param localPortNumber
	 *            .
	 * @param externPortNo
	 *            .
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
					|| (externPortNo == null)) {
				isValidEp = false;
			}
			if (localPortNumber == null) {
				localPortNumber = "*";
			} else {
				// Check for number
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
					} else if (!externPortNo.isEmpty()) {
						Integer.valueOf(externPortNo);
					}
				} catch (NumberFormatException ex) {
					return false;
				}
			}
			if (endpointType == WindowsAzureEndpointType.Input) {
				isValidEp = isValidInputEp(endpointName, localPortNumber,
						externPortNo);
			} else if (endpointType == WindowsAzureEndpointType.Internal) {
				isValidEp = isValidInternalEp(endpointName, localPortNumber,
						externPortNo);
			} else {
				isValidEp = isValidInstanceEp(endpointName, localPortNumber,
						externPortNo);
			}
		} catch (Exception ex) {
			isValidEp = false;
		}
		return isValidEp;
	}

	/**
	 * This method will check input endpoint is valid or not
	 * 
	 * @param endpointName
	 * @param localPort
	 * @param pubPort
	 * @return
	 */
	private boolean isValidInputEp(String endpointName, String localPort,
			String pubPort) {
		try {
			boolean isValidEp = true;
			if (localPort.contains("-") || pubPort.contains("-")
					|| localPort.isEmpty() || pubPort.isEmpty()) {
				isValidEp = false;
			} else {
				// check that localPort should be unique in role
				if (localPort != "*") {
					List<WindowsAzureEndpoint> eps = getEndpoints();
					for (WindowsAzureEndpoint ep : eps) {
						if (endpointName.equalsIgnoreCase(ep.getName())) {
							// edit case
							continue;
						}

						// Private port of instance endpoint and input
						// endpoint's private port can be same.
						if (!ep.getEndPointType().equals(
								WindowsAzureEndpointType.InstanceInput)
								&& ParserXMLUtility.isEpPortEqualOrInRange(
										ep.getPrivatePortWrapper(), localPort)) {
							isValidEp = false;
							break;
						}

						if (ParserXMLUtility.isEpPortEqualOrInRange(
								ep.getPort(), localPort)
								|| (ParserXMLUtility.isEpPortEqualOrInRange(
										ep.getPort(), pubPort) || ParserXMLUtility
										.isEpPortEqualOrInRange(
												ep.getPrivatePortWrapper(),
												pubPort))) {
							isValidEp = false;
							break;
						}
					}
				}
				if (isValidEp) {
					isValidEp = winProjMgr.isDupInputPubPort(pubPort,
							endpointName);
				}
			}
			return isValidEp;
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * This method will check internal endpoint is valid or not
	 * 
	 * @param endpointName
	 * @param localPort
	 * @param pubPort
	 * @return
	 */
	private boolean isValidInternalEp(String endpointName, String localPort,
			String pubPort) {
		boolean isValid = true;
		try {
			if (!pubPort.isEmpty()) {
				// For internal endpoint public port must be empty.
				isValid = false;
			} else if (localPort == "*") {
				isValid = true;
			} else {
				// check for max range should be greater than or equal to min
				if (localPort.contains("-")) {
					isValid = ParserXMLUtility.isValidRange(localPort);
				}
				if (isValid) {
					// check that localPort should be unique in role
					List<WindowsAzureEndpoint> eps = getEndpoints();
					for (WindowsAzureEndpoint ep : eps) {
						if (endpointName.equalsIgnoreCase(ep.getName())) {
							// edit case
							continue;
						}
						if (ParserXMLUtility.isEpPortEqualOrInRange(
								ep.getPort(), localPort)
								|| ParserXMLUtility.isEpPortEqualOrInRange(
										ep.getPrivatePortWrapper(), localPort)) {
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
	 * 
	 * @param endpointName
	 * @param localPort
	 * @param pubPort
	 * @return
	 */
	private boolean isValidInstanceEp(String endpointName, String localPort,
			String pubPort) {
		boolean isValid = true;
		try {
			if (localPort.isEmpty() || pubPort.isEmpty()
					|| localPort.contains("-") || localPort.equals("*")) {
				isValid = false;
			} else {
				// check for max range should be greater than or equal to min
				if (pubPort.contains("-")) {
					isValid = ParserXMLUtility.isValidRange(pubPort);
				}
				if (isValid) {
					// check that localPort should be unique in role
					List<WindowsAzureEndpoint> eps = getEndpoints();
					for (WindowsAzureEndpoint ep : eps) {
						if (endpointName.equalsIgnoreCase(ep.getName())) {
							// edit case
							continue;
						}
						// Private port of instance endpoint and input
						// endpoint's private port can be same.
						if (!ep.getEndPointType().equals(
								WindowsAzureEndpointType.Input)
								&& ParserXMLUtility.isEpPortEqualOrInRange(
										ep.getPrivatePortWrapper(), localPort)) {
							isValid = false;
							break;
						}
						if (ep.getEndPointType().equals(
								WindowsAzureEndpointType.InstanceInput)) {
							if (ParserXMLUtility.isEpPortEqualOrInRange(
									ep.getPort(), pubPort)) {
								isValid = false;
								break;
							}
						} else {
							if (ParserXMLUtility.isEpPortEqualOrInRange(
									ep.getPort(), localPort)
									|| ParserXMLUtility.isEpPortEqualOrInRange(
											ep.getPort(), pubPort)
									|| ParserXMLUtility
											.isEpPortEqualOrInRange(
													ep.getPrivatePortWrapper(),
													pubPort)) {
								isValid = false;
								break;
							}
						}
					}
					if (isValid) {
						// validate public port in project
						isValid = winProjMgr.isDupInputPubPort(pubPort,
								endpointName);
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
	public void delete() throws WindowsAzureInvalidProjectOperationException {

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

			// delete properties from package.xml
			XPath xPath = XPathFactory.newInstance().newXPath();
			expr = WindowsAzureConstants.PROJ_PROPERTIES;
			NodeList properties = (NodeList) xPath.evaluate(expr, doc,
					XPathConstants.NODESET);
			for (int i = 0; i < properties.getLength(); i++) {
				Node node = properties.item(i);
				Element property = (Element) node;
				String attrVal = property.getAttribute("name");
				if (attrVal.startsWith("project." + getName())) {
					node.getParentNode().removeChild(node);
				}
			}
			// Delete folder from HD
			getWinProjMgr().mapActivity.put("delete", value);
			// Add remoteForward to another role
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
	 * @param username
	 *            the username to set
	 * @throws WindowsAzureInvalidProjectOperationException .
	 */
	protected void setAccUsername(String username)
			throws WindowsAzureInvalidProjectOperationException {
		try {
			XPath xPath = XPathFactory.newInstance().newXPath();
			Document doc = getWinProjMgr().getConfigFileDoc();
			String expr = String.format(WindowsAzureConstants.RA_ROLE_UNAME,
					getName());
			Element eleSetUname = (Element) xPath.evaluate(expr, doc,
					XPathConstants.NODE);
			if (eleSetUname == null) {
				eleSetUname = doc.createElement("Setting");
				eleSetUname.setAttribute(WindowsAzureConstants.ATTR_NAME,
						WindowsAzureConstants.REMOTEACCESS_USERNAME);
				eleSetUname.setAttribute("value", username);
				expr = String.format(WindowsAzureConstants.CONFIG_ROLE_SET,
						getName());
				Element eleConfigSettings = (Element) xPath.evaluate(expr, doc,
						XPathConstants.NODE);
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
	 * @param password
	 *            the password to set
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
	 * @param expiryDate
	 *            the accExpiryDate to set
	 * @throws WindowsAzureInvalidProjectOperationException .
	 */
	protected void setAccExpiryDate(String expiryDate)
			throws WindowsAzureInvalidProjectOperationException {
		try {
			XPath xPath = XPathFactory.newInstance().newXPath();
			Document doc = getWinProjMgr().getConfigFileDoc();
			String expr = String.format(WindowsAzureConstants.RA_ROLE_EXPIRY,
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
				Element eleConfigSettings = (Element) xPath.evaluate(expr, doc,
						XPathConstants.NODE);
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
	 * @param thumbprint
	 *            the thumbprint to set
	 * @throws WindowsAzureInvalidProjectOperationException .
	 */
	protected void setThumbprint(String thumbprint)
			throws WindowsAzureInvalidProjectOperationException {
		try {
			for (Iterator<Entry<String, WindowsAzureCertificate>> iterator = getCertificates()
					.entrySet().iterator(); iterator.hasNext();) {
				WindowsAzureCertificate cert = iterator.next().getValue();
				if (cert.getFingerPrint().equalsIgnoreCase(thumbprint)) {
					throw new WindowsAzureInvalidProjectOperationException(
							"The certificate with same fingerprint already exists");
				}
			}

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
				expr = String
						.format(WindowsAzureConstants.CERT_ROLE, getName());
				Element eleCertificates = (Element) xPath.evaluate(expr, doc,
						XPathConstants.NODE);
				eleCertificates.appendChild(eleCertificate);

				WindowsAzureCertificate certToInsert = new WindowsAzureCertificate(
						getWinProjMgr(), this,
						WindowsAzureConstants.REMOTEACCESS_FINGERPRINT,
						thumbprint);
				getCertificates();
				certMap.put(WindowsAzureConstants.REMOTEACCESS_FINGERPRINT,
						certToInsert);
			} else {
				eleCertificate.setAttribute("thumbprint", thumbprint);
				getCertificates().get(
						WindowsAzureConstants.REMOTEACCESS_FINGERPRINT)
						.setFingerPrint(thumbprint);
			}
			this.certThumbprint = thumbprint;
		} catch (Exception e) {
			throw new WindowsAzureInvalidProjectOperationException(
					WindowsAzureConstants.EXCP_SET_THUMBP, e);
		}
	}

	/**
	 * This API is for enabling debugging by creating all the necessary XML
	 * markup if its not already there.
	 * 
	 * @param endpoint
	 *            as debugging end point
	 * @param startSuspended
	 *            to indicate the debugging is in suspended mode or not
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
			String newDbg = getDebugStrWithPortAndSusMode(
					endpoint.getPrivatePortWrapper(), startSuspended);
			String value = changeJavaOptionsVal(existingStr, newDbg);
			setRuntimeEnv(WindowsAzureConstants.JAVA_OPTIONS_ENV_VAR, value);

			// var.setAttribute("value", value);
			getRuntimeEnv().put(WindowsAzureConstants.JAVA_OPTIONS_ENV_VAR,
					value);
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					WindowsAzureConstants.EXCP, ex);
		}
	}

	/**
	 * This API on WindowsAzureRole class to disable debugging by removing the
	 * entire agentlib:jdwp= setting from the _JAVA_OPTIONS variable but
	 * leaving others that the user may have put in there in place, unless there
	 * are no other options specified in it, then also removing the
	 * corresponding <Variable> element itself.
	 * 
	 * @throws WindowsAzureInvalidProjectOperationException .
	 */

	protected void disableDebugging()
			throws WindowsAzureInvalidProjectOperationException {
		try {
			XPath xPath = XPathFactory.newInstance().newXPath();
			Document doc = getWinProjMgr().getPackageFileDoc();
			String expr = String.format(
					WindowsAzureConstants.WA_PACK_SENV_NAME, getName(),
					WindowsAzureConstants.JAVA_OPTIONS_ENV_VAR);
			Element var = (Element) xPath.evaluate(expr, doc,
					XPathConstants.NODE);

			if (var != null) {
				String existingStr = var.getAttribute("value");
				// remove -agentlib option from java_option
				String newVal = changeJavaOptionsVal(existingStr, "");
				// if no other options specified remove variable tab
				if (newVal.isEmpty()) {
					var.getParentNode().removeChild(var);
					getRuntimeEnv().remove(
							WindowsAzureConstants.JAVA_OPTIONS_ENV_VAR);
				} else {
					var.setAttribute("value", newVal);
					getRuntimeEnv().put(
							WindowsAzureConstants.JAVA_OPTIONS_ENV_VAR, newVal);
				}
			}

		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					WindowsAzureConstants.EXCP, ex);
		}

	}

	/**
	 * This API is for determines whether debugging has been enabled. Returns
	 * True by testing _JAVA_OPTIONS for the presence of: the -agentlib:jdwp=
	 * option setting, AND the transport=dt_socket subsetting inside it AND
	 * the server=y subsetting Else False.
	 * 
	 * @return isEbabled .
	 */
	protected Boolean getDebuggingEnabled() {
		// =-agentlib:jdwp=transport=dt_socket,server=y,address=8081,suspend=n
		Boolean isEnabled = false;
		try {
			XPath xPath = XPathFactory.newInstance().newXPath();
			String agentLib = "-agentlib:jdwp=";
			Document doc = getWinProjMgr().getPackageFileDoc();
			String expr = String.format(
					WindowsAzureConstants.WA_PACK_SENV_NAME, getName(),
					WindowsAzureConstants.JAVA_OPTIONS_ENV_VAR)
					+ "/@value";
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
	 * the same as the port inside the address subsetting form the _JAVA_OPTIONS
	 * variables -agentlib:jdwp setting.
	 * 
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
				String expr = String.format(
						WindowsAzureConstants.WA_PACK_SENV_NAME, getName(),
						WindowsAzureConstants.JAVA_OPTIONS_ENV_VAR)
						+ "/@value";
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
					if (port.equals(waEndpt.getPrivatePortWrapper())) {
						endPt = waEndpt;
						break;
					}
				}
				// if port number does not match with any endpoint,
				// should throw an exception
				if (endPt == null) {
					throw new WindowsAzureInvalidProjectOperationException(
							"Port specified in debugging is "
									+ "not a valid port for any endpoint");
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
	 * address subsetting of the agentlib:jdwp setting. Throws an exception if
	 * debugging is not enabled.
	 * 
	 * @param endPoint
	 *            .
	 * @throws WindowsAzureInvalidProjectOperationException .
	 */
	public void setDebuggingEndpoint(WindowsAzureEndpoint endPoint)
			throws WindowsAzureInvalidProjectOperationException {
		if (endPoint == null) {
			// when endpoint is null, it means the debugging
			disableDebugging();
		} else {

			try {

				// checkDebugging is enabled or not
				if (!getDebuggingEnabled()) {
					Boolean startSuspended = false;
					enableDebugging(endPoint, startSuspended);
				}

				String port = endPoint.getPrivatePortWrapper();
				String agentLib = "-agentlib:jdwp=";
				XPath xPath = XPathFactory.newInstance().newXPath();
				Document doc = getWinProjMgr().getPackageFileDoc();
				String expr = String.format(
						WindowsAzureConstants.WA_PACK_SENV_NAME, getName(),
						WindowsAzureConstants.JAVA_OPTIONS_ENV_VAR)
						+ "/@value";
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

				// replace new string in xml
				expr = String.format(WindowsAzureConstants.WA_PACK_SENV_NAME,
						getName(), WindowsAzureConstants.JAVA_OPTIONS_ENV_VAR);
				Element var = (Element) xPath.evaluate(expr, doc,
						XPathConstants.NODE);
				String value = changeJavaOptionsVal(existingStr, newVal);
				var.setAttribute("value", value);

				getRuntimeEnv().put(WindowsAzureConstants.JAVA_OPTIONS_ENV_VAR,
						value);

			} catch (Exception ex) {
				throw new WindowsAzureInvalidProjectOperationException(
						WindowsAzureConstants.EXCP, ex);
			}
		}
	}

	/**
	 * This API on WindowsAzureRole class to find whether the JVM should start
	 * in suspended mode. This corresponds to the suspend=y|n subsetting of
	 * the agentlib:jdwp setting. Throws an exception if debugging is not
	 * enabled.
	 * 
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
			String expr = String.format(
					WindowsAzureConstants.WA_PACK_SENV_NAME, getName(),
					WindowsAzureConstants.JAVA_OPTIONS_ENV_VAR)
					+ "/@value";
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
	 * start in suspended mode. This corresponds to the suspend=y|n subsetting
	 * of the agentlib:jdwp setting. Throws an exception if debugging is not
	 * enabled.
	 * 
	 * @param status
	 *            .
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
			String expr = String.format(
					WindowsAzureConstants.WA_PACK_SENV_NAME, getName(),
					WindowsAzureConstants.JAVA_OPTIONS_ENV_VAR)
					+ "/@value";
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
			// replace new string in xml
			expr = String.format(WindowsAzureConstants.WA_PACK_SENV_NAME,
					getName(), WindowsAzureConstants.JAVA_OPTIONS_ENV_VAR);
			Element var = (Element) xPath.evaluate(expr, doc,
					XPathConstants.NODE);
			String value = changeJavaOptionsVal(existingStr, newVal);
			var.setAttribute("value", value);
			getRuntimeEnv().put(WindowsAzureConstants.JAVA_OPTIONS_ENV_VAR,
					value);
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					WindowsAzureConstants.EXCP, ex);
		}
	}

	/**
	 * This method is to create debug string using give port and suspended mode
	 * status.
	 * 
	 * @return String dbgStr
	 * @param port
	 *            .
	 * @param suspendMode
	 *            status.
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
			// Already exist replace the value of agentlib:jdwp...
			// else append the value
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
	 * 
	 * @return map.
	 * @throws WindowsAzureInvalidProjectOperationException .
	 */
	public Map<String, String> getRuntimeEnv()
			throws WindowsAzureInvalidProjectOperationException {

		try {
			if (envVarMap.isEmpty()) {
				XPath xPath = XPathFactory.newInstance().newXPath();
				Document doc = getWinProjMgr().getPackageFileDoc();
				String expr = String.format(
						WindowsAzureConstants.WA_PACK_STARTUPENV, getName());
				NodeList varList = (NodeList) xPath.evaluate(expr, doc,
						XPathConstants.NODESET);
				for (int i = 0; i < varList.getLength(); i++) {
					Element var = (Element) varList.item(i);
					envVarMap.put(
							var.getAttribute(WindowsAzureConstants.ATTR_NAME),
							var.getAttribute(WindowsAzureConstants.ATTR_VALUE));
				}
			}
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception while getting RunTinme env variables: ", ex);
		}
		return envVarMap;
	}

	/**
	 * This API is to return a name list of all the local storage variables.
	 * 
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
					Element var = (Element) varList.item(i);
					lsVarList.add(var
							.getAttribute(WindowsAzureConstants.ATTR_NAME));
				}
			}
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception while getting RunTinme env variables: ", ex);
		}
		return lsVarList;
	}

	/**
	 * This API is to return the value of a specific variable.
	 * 
	 * @param name
	 *            .
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
			String expr = String.format(
					WindowsAzureConstants.WA_PACK_SENV_NAME, getName(), name)
					+ "/@value";
			value = xPath.evaluate(expr, doc);
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception while getting RunTinme env variables: ", ex);
		}
		return value;
	}

	/**
	 * This API sets the value of a specific variable.
	 * 
	 * @param name
	 *            .
	 * @param value
	 *            .
	 * @throws WindowsAzureInvalidProjectOperationException .
	 */

	public void setRuntimeEnv(String name, String value)
			throws WindowsAzureInvalidProjectOperationException {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException(
					WindowsAzureConstants.INVALID_ARG);
		}
		try {
			Document doc = getWinProjMgr().getPackageFileDoc();
			XPath xPath = XPathFactory.newInstance().newXPath();
			// check first the env is already present, if yes. edit the same
			String env = String.format(WindowsAzureConstants.WA_PACK_SENV_NAME,
					getName(), name);
			Element envNode = (Element) xPath.evaluate(env, doc,
					XPathConstants.NODE);

			if (envNode == null) {
				String parentNode = String.format(
						WindowsAzureConstants.WA_PACK_ROLE, getName());
				Element role = (Element) xPath.evaluate(parentNode, doc,
						XPathConstants.NODE);
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
	 * 
	 * @param oldName
	 *            .
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
			String expr = String
					.format(WindowsAzureConstants.WA_PACK_SENV_NAME, getName(),
							oldName);
			Element var = (Element) xPath.evaluate(expr, doc,
					XPathConstants.NODE);
			if (var == null) {
				throw new WindowsAzureInvalidProjectOperationException(oldName
						+ " variable does not exist");
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
	 * 
	 * @return a name-object list of all the local storage resources.
	 * @throws WindowsAzureInvalidProjectOperationException .
	 */
	public Map<String, WindowsAzureLocalStorage> getLocalStorage()
			throws WindowsAzureInvalidProjectOperationException {

		try {
			if (locStoMap.isEmpty()) {
				XPath xPath = XPathFactory.newInstance().newXPath();
				Document doc = getWinProjMgr().getdefinitionFileDoc();
				String expr = String.format(
						WindowsAzureConstants.LOCAL_STORAGE, getName());
				NodeList locStoList = (NodeList) xPath.evaluate(expr, doc,
						XPathConstants.NODESET);
				for (int i = 0; i < locStoList.getLength(); i++) {
					Element locSto = (Element) locStoList.item(i);
					locStoMap.put(locSto
							.getAttribute(WindowsAzureConstants.ATTR_NAME),
							getWinLocStObj(locSto));
				}
			}
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception while getting RunTinme env variables: ", ex);
		}
		return locStoMap;
	}

	private WindowsAzureLocalStorage getWinLocStObj(Element locSto)
			throws WindowsAzureInvalidProjectOperationException {
		WindowsAzureLocalStorage winLocSt = null;
		if (locSto != null) {
			winLocSt = new WindowsAzureLocalStorage(winProjMgr, this);
			winLocSt.setName(locSto
					.getAttribute(WindowsAzureConstants.ATTR_NAME));
			winLocSt.setSize(Integer.parseInt(locSto
					.getAttribute(WindowsAzureConstants.ATTR_SIZEINMB)));
			winLocSt.setCleanOnRecycle(Boolean.parseBoolean(locSto
					.getAttribute(WindowsAzureConstants.ATTR_CLE_ON_ROLE_RECYCLE)));
			String pathEnv = winLocSt.getPathEnv();
			if (pathEnv != null) {
				winLocSt.setPathEnv(pathEnv);
			}
		}
		return winLocSt;
	}

	/**
	 * This API is for getting object of WindowsAzureLocalStorage
	 * 
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

		if ((name == null || name.isEmpty()) || (size < 1) || (pathEnv == null)
				|| pathEnv.isEmpty()) {
			throw new IllegalArgumentException(
					WindowsAzureConstants.INVALID_ARG);
		}
		WindowsAzureLocalStorage winLocSt = null;
		try {

			Document doc = getWinProjMgr().getdefinitionFileDoc();
			XPath xPath = XPathFactory.newInstance().newXPath();

			// Check <LocalResources>, if not find create new
			// Find RunTime tag, if not present create new
			String expr = String.format(WindowsAzureConstants.LOCAL_RESOURCES,
					getName());
			Element eleLocRes = (Element) xPath.evaluate(expr, doc,
					XPathConstants.NODE);

			if (eleLocRes == null) {
				String objName = getName();
				eleLocRes = doc.createElement("LocalResources");
				// Append <LocalResources> to <WorkerRole>
				expr = String.format(WindowsAzureConstants.WR_NAME, objName);
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
			winLocSt.setName(eleLocSto
					.getAttribute(WindowsAzureConstants.ATTR_NAME));
			winLocSt.setSize(Integer.parseInt(eleLocSto
					.getAttribute(WindowsAzureConstants.ATTR_SIZEINMB)));
			winLocSt.setCleanOnRecycle(Boolean.parseBoolean(eleLocSto
					.getAttribute(WindowsAzureConstants.ATTR_CLE_ON_ROLE_RECYCLE)));
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
		if (value != null) {
			configureSessionAffinity(value);
		} else {
			disableSessionAffinity();
		}

	}

	/** This API is for enabling or disabling SSL configuration */
	public void setSslOffloading(WindowsAzureEndpoint endpoint,
			WindowsAzureCertificate cert)
			throws WindowsAzureInvalidProjectOperationException {

		if (endpoint != null && cert != null) {
			configureSslOffloading(endpoint, cert);
		} else if (endpoint == null && cert == null) {
			disableSslOffloading();
		} else {
			throw new WindowsAzureInvalidProjectOperationException(
					WindowsAzureConstants.EXCP_EMPTY_ENDPOINT_OR_CERT);
		}

	}

	/** This API is for configuring session affinity */
	private void configureSessionAffinity(
			WindowsAzureEndpoint windowsAzureEndpoint)
			throws WindowsAzureInvalidProjectOperationException {
		boolean enableLoadBalancing = true;
		// Check if end point is input end point or not
		if (windowsAzureEndpoint == null
				|| (windowsAzureEndpoint.getEndPointType() != WindowsAzureEndpointType.Input)) {
			throw new WindowsAzureInvalidProjectOperationException(
					WindowsAzureConstants.EXCP_EMPTY_OR_INVALID_ENDPOINT);
		}

		// Check if sessionAffinity is already enabled for endpoint, if yes then
		// throw error
		WindowsAzureEndpoint saEndpt = getSessionAffinityInputEndpoint();
		if (saEndpt != null
				&& saEndpt.getName().equalsIgnoreCase(
						windowsAzureEndpoint.getName())) {
			throw new WindowsAzureInvalidProjectOperationException(
					WindowsAzureConstants.EXCP_SA_ENABLED);
		} else if (saEndpt != null) {
			disableSessionAffinity(); // Disable existing session affinity and
										// enable for new end point
		}

		// Check if SSL is already configured, if yes then just turn on load
		// balancing option.
		WindowsAzureEndpoint sslProxyEP = getSslOffloadingInputEndpoint();
		WindowsAzureEndpoint sslInternalProxyEP = getSslOffloadingInternalEndpoint();

		if (sslProxyEP != null) {
			setLBInProxy(enableLoadBalancing);

			// set properties in package.xml
			addPropertiesInPackage(WindowsAzureConstants.SA_INPUT_ENDPOINT,
					WindowsAzureConstants.SA_INPUT_ENDPOINT_NAME_PROP,
					windowsAzureEndpoint.getName());
			addPropertiesInPackage(WindowsAzureConstants.SA_INTERNAL_ENDPOINT,
					WindowsAzureConstants.SA_INTERNAL_ENDPOINT_NAME_PROP,
					sslInternalProxyEP.getName());

			return;
		}

		// Generate port and name for SA internal end point.
		int iisArrPort = getARRPort();
		String saInternalEndPointName = generateEndPointName(
				windowsAzureEndpoint.getName(),
				WindowsAzureConstants.ARR_INTERNAL_ENDPOINT_SUFFIX);
		int stepsCompleted = -1;

		try {
			// update or create properties in package.xml
			addPropertiesInPackage(WindowsAzureConstants.SA_INPUT_ENDPOINT,
					WindowsAzureConstants.SA_INPUT_ENDPOINT_NAME_PROP,
					windowsAzureEndpoint.getName());
			addPropertiesInPackage(WindowsAzureConstants.SA_INTERNAL_ENDPOINT,
					WindowsAzureConstants.SA_INTERNAL_ENDPOINT_NAME_PROP,
					saInternalEndPointName);
			stepsCompleted = WindowsAzureConstants.PACKAGE_DOC_SA_PROPERTIES;

			// add SA settings in service definition file
			addSASettingsInSvcDef(windowsAzureEndpoint, saInternalEndPointName,
					iisArrPort);
			stepsCompleted = WindowsAzureConstants.DEFINITION_DOC_SA_CHANGES;

			// create Proxy configuration files
			createProxyConfFiles();
			stepsCompleted = WindowsAzureConstants.SA_FILES_COPIED;

		} catch (Exception e) {
			handleRollback(stepsCompleted);
			throw new WindowsAzureInvalidProjectOperationException(
					e.getMessage(), e);
		}

	}

	/** This API is for configuring SSL Offloading */
	private void configureSslOffloading(WindowsAzureEndpoint endPoint,
			WindowsAzureCertificate sslCert)
			throws WindowsAzureInvalidProjectOperationException {

		String internalEndPointName = null;
		String enableLoadBalancing = "false";

		// Check if inputs are not null and end point is input end point or not.
		if (endPoint == null
				|| sslCert == null
				|| (endPoint.getEndPointType() != WindowsAzureEndpointType.Input)) {
			throw new WindowsAzureInvalidProjectOperationException(
					WindowsAzureConstants.EXCP_EMPTY_OR_INVALID_ENDPOINT);
		}

		// Pre-check: Check if SSL offloading is already enabled for end-point
		WindowsAzureEndpoint proxyInputEP = getSslOffloadingInputEndpoint();
		if (proxyInputEP != null
				&& proxyInputEP.getName().equalsIgnoreCase(endPoint.getName())) {
			throw new WindowsAzureInvalidProjectOperationException(
					WindowsAzureConstants.EXCP_SSL_ENABLED);
		} else {
			disableSslOffloading(); // Disable existing ssl offloading and add
									// it for new end point.
		}

		// Check if sessionAffinity is already enabled, if yes then update csdef
		// and package.xml accordingly
		WindowsAzureEndpoint saEndpt = getSessionAffinityInputEndpoint();

		// Check if it ok to enable http redirection
		boolean enableRedirection = getWinProjMgr().isValidPort(
				String.valueOf(HTTP_PORT), WindowsAzureEndpointType.Input);

		String sslRedirectEndPointName = null;
		if (enableRedirection) {
			sslRedirectEndPointName = generateEndPointName(endPoint.getName(),
					WindowsAzureConstants.ARR_SSL_REDIRECT_ENDPOINT_SUFFIX);
		}

		if (saEndpt != null) {
			enableLoadBalancing = "true";
			internalEndPointName = getSessionAffinityInternalEndpoint()
					.getName();

			String startUpCmd = String.format(
					WindowsAzureConstants.SSL_TASK_CMD_VALUE,
					endPoint.getName(), internalEndPointName,
					enableLoadBalancing, sslCert.getFingerPrint(),
					WindowsAzureConstants.SSL_STORE_NAME);

			if (enableRedirection) {
				startUpCmd += " " + sslRedirectEndPointName;
			}

			addOrUpdateStartUpCmdForProxy(startUpCmd, true);

			// Update protocol and certificate element
			saEndpt.setProtocol("https");
			saEndpt.setCertificate(sslCert.getName());
		} else { // configure ssl offloading
			internalEndPointName = generateEndPointName(endPoint.getName(),
					WindowsAzureConstants.ARR_INTERNAL_ENDPOINT_SUFFIX);

			// create comment node
			Document definitionFiledoc = getWinProjMgr().getdefinitionFileDoc();
			String nodeExpr = String.format(
					WindowsAzureConstants.STARTUP_WR_NAME, getName());
			createCommentNode(definitionFiledoc, nodeExpr);

			// Reconfigure endpoints for proxy
			reConfigureEPForProxy(endPoint, internalEndPointName, getARRPort(),
					"https", sslCert.getName());

			// Add webdeploy import module
			addImportModule(WindowsAzureConstants.IMPORT_WEB_DEPLOY,
					"WebDeploy");

			// create Proxy configuration files
			createProxyConfFiles();

			// add startup command in service definition
			String startUpCmd = String.format(
					WindowsAzureConstants.SSL_TASK_CMD_VALUE,
					endPoint.getName(), internalEndPointName,
					enableLoadBalancing, sslCert.getFingerPrint(),
					WindowsAzureConstants.SSL_STORE_NAME);

			if (enableRedirection) {
				startUpCmd += " " + sslRedirectEndPointName;
			}
			addOrUpdateStartUpCmdForProxy(startUpCmd, false);
		}

		// Add properties in package.xml
		addPropertiesInPackage(WindowsAzureConstants.SSL_INPUT_ENDPOINT,
				WindowsAzureConstants.SSL_INPUT_ENDPOINT_NAME_PROP,
				endPoint.getName());
		addPropertiesInPackage(WindowsAzureConstants.SSL_INTERNAL_ENDPOINT,
				WindowsAzureConstants.SSL_INTERNAL_ENDPOINT_NAME_PROP,
				internalEndPointName);

		if (enableRedirection) {
			// Add Redirection Endpoint
			addEndpoint(sslRedirectEndPointName,
					WindowsAzureEndpointType.Input, "*",
					String.valueOf(HTTP_PORT));
			// Add property in package.xml
			addPropertiesInPackage(
					WindowsAzureConstants.SSL_REDIRECTION_ENDPOINT,
					WindowsAzureConstants.SSL_REDIRECT_ENDPOINT_NAME_PROP,
					sslRedirectEndPointName);
		}
		addPropertiesInPackage(WindowsAzureConstants.SSL_CERT_NAME,
				WindowsAzureConstants.SSL_CERT_NAME_PROP, sslCert.getName());
		addPropertiesInPackage(WindowsAzureConstants.SSL_CERT_FINGERPRINT,
				WindowsAzureConstants.SSL_CERT_FINGERPRINT_PROP,
				sslCert.getFingerPrint());
	}

	/**
	 * Adds or updates properties for session affinity and ssl offloading in
	 * package.xml.
	 */
	private void addPropertiesInPackage(String nodePath, String attrName,
			String attrValue)
			throws WindowsAzureInvalidProjectOperationException {

		Document packageFileDoc = getWinProjMgr().getPackageFileDoc();

		if (packageFileDoc != null) {
			String nodeExpr = String.format(nodePath, this.getName());
			String parentNodeExpr = WindowsAzureConstants.PROJ_PROPERTY;

			HashMap<String, String> nodeAttribites = new HashMap<String, String>();
			nodeAttribites.put(WindowsAzureConstants.ATTR_NAME,
					String.format(attrName, this.getName()));
			nodeAttribites.put(WindowsAzureConstants.ATTR_VALUE, attrValue);
			ParserXMLUtility.updateOrCreateElement(packageFileDoc, nodeExpr,
					parentNodeExpr,
					WindowsAzureConstants.PROJ_PROPERTY_ELEMENT_NAME, false,
					nodeAttribites);
		}
	}

	/** This method adds Session Affinity settings in service definition */
	private void addSASettingsInSvcDef(WindowsAzureEndpoint inpEndPt,
			String intEndPt, int iisArrPort)
			throws WindowsAzureInvalidProjectOperationException {
		boolean enableLoadBalancing = true;
		// Add startup task entry
		Document definitionFiledoc = getWinProjMgr().getdefinitionFileDoc();
		String nodeExpr = String.format(WindowsAzureConstants.STARTUP_WR_NAME,
				getName());
		String parentNodeExpr = String.format(WindowsAzureConstants.WR_NAME,
				this.getName());
		ParserXMLUtility
				.updateOrCreateElement(definitionFiledoc, nodeExpr,
						parentNodeExpr,
						WindowsAzureConstants.DEF_FILE_STARTUP_ELEMENT_NAME,
						true, null);

		String taskNodeExpr = String.format(
				WindowsAzureConstants.STARTUP_TASK_CMD, this.getName(),
				WindowsAzureConstants.TASK_CMD_VALUE, inpEndPt.getName(),
				intEndPt);
		HashMap<String, String> nodeAttributes = new HashMap<String, String>();
		nodeAttributes.put(
				"commandLine",
				String.format(WindowsAzureConstants.TASK_CMD_VALUE,
						inpEndPt.getName(), intEndPt, enableLoadBalancing));
		nodeAttributes.put("executionContext", "elevated");
		nodeAttributes.put("taskType", "simple");
		Element element = ParserXMLUtility.updateOrCreateElement(
				definitionFiledoc, taskNodeExpr, nodeExpr,
				WindowsAzureConstants.DEF_FILE_TASK_ELEMENT_NAME, true,
				nodeAttributes);

		// Add environment element
		element = ParserXMLUtility.createElement(definitionFiledoc, null,
				element, WindowsAzureConstants.DEF_FILE_ENV_NAME, false, null);

		// Add variable element
		nodeAttributes.clear();
		nodeAttributes.put("name", "EMULATED");
		element = ParserXMLUtility.createElement(definitionFiledoc, null,
				element, WindowsAzureConstants.DEF_FILE_VAR_ELE_NAME, false,
				nodeAttributes);

		// Add xpath expression role instance value
		nodeAttributes.clear();
		nodeAttributes.put("xpath", "/RoleEnvironment/Deployment/@emulated");
		element = ParserXMLUtility.createElement(definitionFiledoc, null,
				element, WindowsAzureConstants.DEF_FILE_ENV_RIV_NAME, false,
				nodeAttributes);

		// Add comments to startup task element to warn the user not to insert
		// any task before this.
		createCommentNode(definitionFiledoc, nodeExpr);

		// Add SA input end point and SA internal end point
		String inputEndPointLocalPort = inpEndPt.getPrivatePortWrapper();
		nodeExpr = String.format(WindowsAzureConstants.INPUT_ENDPOINT,
				getName(), inpEndPt.getName());
		int index = winEndPtList.indexOf(inpEndPt);
		winEndPtList.get(index).setPrivatePort(String.valueOf(iisArrPort));
		addEndpoint(intEndPt, WindowsAzureEndpointType.Internal,
				inputEndPointLocalPort, "");

		// Add webdeploy import option
		String importsNode = String.format(WindowsAzureConstants.IMPORT,
				this.getName());
		ParserXMLUtility.updateOrCreateElement(definitionFiledoc, importsNode,
				parentNodeExpr,
				WindowsAzureConstants.DEF_FILE_IMPORTS_ELEMENT_NAME, false,
				null);

		nodeExpr = String.format(WindowsAzureConstants.IMPORT_WEB_DEPLOY,
				this.getName());
		nodeAttributes.clear();
		nodeAttributes.put("moduleName", "WebDeploy");
		ParserXMLUtility.updateOrCreateElement(definitionFiledoc, nodeExpr,
				importsNode,
				WindowsAzureConstants.DEF_FILE_IMPORT_ELEMENT_NAME, true,
				nodeAttributes);
	}

	private void createProxyConfFiles()
			throws WindowsAzureInvalidProjectOperationException {
		// create SA configuration files
		Vector<String> saInfo = new Vector<String>();
		saInfo.add(this.getName());
		if (getWinProjMgr().mapActivity.get("addProxyFilesForRole") != null)
			getWinProjMgr().mapActivity.remove("addProxyFilesForRole");
		if (getWinProjMgr().mapActivity.get("delProxyFilesForRole") != null)
			getWinProjMgr().mapActivity.remove("delProxyFilesForRole");
		getWinProjMgr().mapActivity.put("addProxyFilesForRole", saInfo);

	}

	/**
	 * This method add import modules to service definition
	 * 
	 */
	private void addImportModule(String expr, String moduleName)
			throws WindowsAzureInvalidProjectOperationException {
		Document definitionFiledoc = getWinProjMgr().getdefinitionFileDoc();
		String parentNodeExpr = String.format(WindowsAzureConstants.WR_NAME,
				this.getName());

		String importsNode = String.format(WindowsAzureConstants.IMPORT,
				this.getName());
		ParserXMLUtility.updateOrCreateElement(definitionFiledoc, importsNode,
				parentNodeExpr,
				WindowsAzureConstants.DEF_FILE_IMPORTS_ELEMENT_NAME, false,
				null);

		String nodeExpr = String.format(expr, this.getName());
		HashMap<String, String> nodeAttributes = new HashMap<String, String>();
		nodeAttributes.clear();
		nodeAttributes.put("moduleName", moduleName);
		ParserXMLUtility.updateOrCreateElement(definitionFiledoc, nodeExpr,
				importsNode,
				WindowsAzureConstants.DEF_FILE_IMPORT_ELEMENT_NAME, true,
				nodeAttributes);
	}

	/**
	 * This method adds startup task in csdef for both SA and/or SSL
	 * configuration.
	 */
	private void addOrUpdateStartUpCmdForProxy(String commandLine,
			boolean updateOnly)
			throws WindowsAzureInvalidProjectOperationException {

		String taskNodeExpr = String.format(
				WindowsAzureConstants.STARTUP_TASK_STARTS_WITH, this.getName(),
				WindowsAzureConstants.TASK_CMD_ONLY);

		// Add startup task entry
		Document definitionFiledoc = getWinProjMgr().getdefinitionFileDoc();
		String nodeExpr = String.format(WindowsAzureConstants.STARTUP_WR_NAME,
				getName());
		String parentNodeExpr = String.format(WindowsAzureConstants.WR_NAME,
				this.getName());
		ParserXMLUtility
				.updateOrCreateElement(definitionFiledoc, nodeExpr,
						parentNodeExpr,
						WindowsAzureConstants.DEF_FILE_STARTUP_ELEMENT_NAME,
						true, null);

		HashMap<String, String> nodeAttributes = new HashMap<String, String>();
		nodeAttributes.put("commandLine", commandLine);
		nodeAttributes.put("executionContext", "elevated");
		nodeAttributes.put("taskType", "simple");
		Element element = ParserXMLUtility.updateOrCreateElement(
				definitionFiledoc, taskNodeExpr, nodeExpr,
				WindowsAzureConstants.DEF_FILE_TASK_ELEMENT_NAME, true,
				nodeAttributes);

		if (!updateOnly) {
			// Add environment element
			element = ParserXMLUtility.createElement(definitionFiledoc, null,
					element, WindowsAzureConstants.DEF_FILE_ENV_NAME, false,
					null);

			// Add variable element
			nodeAttributes.clear();
			nodeAttributes.put("name", "EMULATED");
			element = ParserXMLUtility.createElement(definitionFiledoc, null,
					element, WindowsAzureConstants.DEF_FILE_VAR_ELE_NAME,
					false, nodeAttributes);

			// Add xpath expression role instance value
			nodeAttributes.clear();
			nodeAttributes
					.put("xpath", "/RoleEnvironment/Deployment/@emulated");
			element = ParserXMLUtility.createElement(definitionFiledoc, null,
					element, WindowsAzureConstants.DEF_FILE_ENV_RIV_NAME,
					false, nodeAttributes);
		}
	}

	/**
	 * This API reconfigures endpoints for proxy configurations.
	 * 
	 * @param inpEndPt
	 * @param intEndPt
	 * @param iisArrPort
	 * @param certName
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	private void reConfigureEPForProxy(WindowsAzureEndpoint inpEndPt,
			String intEndPt, int iisArrPort, String protocol, String certName)
			throws WindowsAzureInvalidProjectOperationException {
		// Add SA input end point and SA internal end point
		String inputEndPointLocalPort = inpEndPt.getPrivatePortWrapper();
		int index = winEndPtList.indexOf(inpEndPt);
		winEndPtList.get(index).setPrivatePort(String.valueOf(iisArrPort));
		winEndPtList.get(index).setProtocol(protocol);
		winEndPtList.get(index).setCertificate(certName);
		addEndpoint(intEndPt, WindowsAzureEndpointType.Internal,
				inputEndPointLocalPort, "");
	}

	/** Adds comments as a first node */
	private void createCommentNode(Document doc, String nodeExpr)
			throws WindowsAzureInvalidProjectOperationException {
		if (doc == null || nodeExpr == null) {
			throw new IllegalArgumentException(
					WindowsAzureConstants.INVALID_ARG);
		}
		try {
			Comment taskComment = doc
					.createComment(WindowsAzureConstants.STARTUP_TASK_COMMENTS);
			XPath xPath = XPathFactory.newInstance().newXPath();
			Element element = (Element) xPath.evaluate(nodeExpr, doc,
					XPathConstants.NODE);
			element.insertBefore(taskComment,
					element != null ? element.getFirstChild() : null);
		} catch (Exception e) {
			throw new WindowsAzureInvalidProjectOperationException(
					WindowsAzureConstants.EXCP_COMMENT_NODE, e);
		}
	}

	/**
	 * This method changes session affinity end point info in start up task if
	 * there is a name change
	 */
	private void changeEndPointInfoInStartupTask(String inputEndPointNAme,
			String internalEndPointName, boolean enableAffinity)
			throws WindowsAzureInvalidProjectOperationException {

		String taskNodeExpr = String.format(
				WindowsAzureConstants.STARTUP_TASK_STARTS_WITH, this.getName(),
				WindowsAzureConstants.TASK_CMD_ONLY);
		String parentNodeExpr = String.format(
				WindowsAzureConstants.STARTUP_WR_NAME, getName());
		Document definitionFiledoc = getWinProjMgr().getdefinitionFileDoc();
		HashMap<String, String> nodeAttributes = new HashMap<String, String>();

		nodeAttributes.put("commandLine", String.format(
				WindowsAzureConstants.TASK_CMD_VALUE, inputEndPointNAme,
				internalEndPointName, enableAffinity));
		nodeAttributes.put("executionContext", "elevated");
		nodeAttributes.put("taskType", "simple");
		ParserXMLUtility.updateOrCreateElement(definitionFiledoc, taskNodeExpr,
				parentNodeExpr,
				WindowsAzureConstants.DEF_FILE_TASK_ELEMENT_NAME, true,
				nodeAttributes);

	}

	/**
	 * API to handle rollback logic.
	 * 
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	private void handleRollback(int stepsCompleted)
			throws WindowsAzureInvalidProjectOperationException {
		switch (stepsCompleted) {
		case WindowsAzureConstants.PACKAGE_DOC_SA_PROPERTIES:
			removeProxySettingsFromDefDoc(getSessionAffinityInputEndpoint(),
					getSessionAffinityInternalEndpoint(),
					getSslOffloadingRedirectionEndpoint());
			break;

		case WindowsAzureConstants.DEFINITION_DOC_SA_CHANGES:
			removeProxySettingsFromDefDoc(getSessionAffinityInputEndpoint(),
					getSessionAffinityInternalEndpoint(),
					getSslOffloadingRedirectionEndpoint());
			// remove properties from package.xml
			removeProxyProperties(WindowsAzureConstants.SA_INPUT_ENDPOINT);
			removeProxyProperties(WindowsAzureConstants.SA_INTERNAL_ENDPOINT);

			break;

		case WindowsAzureConstants.SA_FILES_COPIED:
			removeProxyFilesFromPrj();
			removeProxySettingsFromDefDoc(getSessionAffinityInputEndpoint(),
					getSessionAffinityInternalEndpoint(),
					getSslOffloadingRedirectionEndpoint());
			// remove properties from package.xml
			removeProxyProperties(WindowsAzureConstants.SA_INPUT_ENDPOINT);
			removeProxyProperties(WindowsAzureConstants.SA_INTERNAL_ENDPOINT);
			break;

		default:
			break;
		}

	}

	/** Remove SA settings from service definition file */
	private void removeProxySettingsFromDefDoc(
			WindowsAzureEndpoint inputEndPoint,
			WindowsAzureEndpoint intEndPoint,
			WindowsAzureEndpoint sslRedirectEndPoint) {
		try {
			Document definitionFiledoc = getWinProjMgr().getdefinitionFileDoc();
			String expr = String.format(
					WindowsAzureConstants.STARTUP_TASK_STARTS_WITH,
					this.getName(), WindowsAzureConstants.TASK_CMD_ONLY);
			ParserXMLUtility.deleteElement(definitionFiledoc, expr);

			// Remove old SA settings if exists
			expr = String.format(
					WindowsAzureConstants.STARTUP_TASK_STARTS_WITH,
					this.getName(), WindowsAzureConstants.OLD_TASK_CMD_ONLY);
			ParserXMLUtility.deleteElement(definitionFiledoc, expr);

			// remove web deploy import statement from definition file
			expr = String.format(WindowsAzureConstants.IMPORT_WEB_DEPLOY,
					this.getName());
			ParserXMLUtility.deleteElement(definitionFiledoc, expr);

			// Delete Comments child node
			String nodeExpr = String.format(
					WindowsAzureConstants.STARTUP_WR_NAME, getName());
			deleteCommentNode(WindowsAzureConstants.STARTUP_TASK_COMMENTS,
					definitionFiledoc, nodeExpr);

			String port = intEndPoint.getPrivatePortWrapper();
			intEndPoint.delete();

			if (sslRedirectEndPoint != null) {
				inputEndPoint.setPort(sslRedirectEndPoint.getPort());
				sslRedirectEndPoint.delete();
			} else if (HTTPS_PORT == Integer.valueOf(inputEndPoint.getPort())) {
				// If public port is 443 then set it back to 80
				if (getWinProjMgr().isValidPort(String.valueOf(HTTP_PORT),
						WindowsAzureEndpointType.Input)) {
					inputEndPoint.setPort(HTTP_PORT + "");
				}
			}

			inputEndPoint.setLocalPort(port);

			// reset protocol and remove certificate attribute
			inputEndPoint.setProtocol("tcp");
			inputEndPoint.removeAttribute("certificate");
		} catch (Exception e) {
			// Don't throw error , ignore silently
		}
	}

	/** This API deletes comments node */
	private void deleteCommentNode(String startupTaskComments,
			Document definitionFiledoc, String nodeExpr)
			throws XPathExpressionException {

		XPath xPath = XPathFactory.newInstance().newXPath();
		Element element = (Element) xPath.evaluate(nodeExpr, definitionFiledoc,
				XPathConstants.NODE);

		NodeList nodeList = element.getChildNodes();
		if (nodeList.getLength() > 0) {
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node.getNodeType() == Node.COMMENT_NODE) {
					Comment comment = (Comment) node;
					if (startupTaskComments.equals(comment.getData())) {
						comment.getParentNode().removeChild(node);
						break;
					}
				}

			}
		}
	}

	/** Removes proxy configuration files from project */
	private void removeProxyFilesFromPrj() {
		try {
			Vector<String> saInfo = new Vector<String>();
			saInfo.add(this.getName());
			if (getWinProjMgr().mapActivity.get("addProxyFilesForRole") != null) {
				getWinProjMgr().mapActivity.remove("addProxyFilesForRole");
			}
			if (getWinProjMgr().mapActivity.get("delProxyFilesForRole") != null) {
				getWinProjMgr().mapActivity.remove("delProxyFilesForRole");
			}
			getWinProjMgr().mapActivity.put("delProxyFilesForRole", saInfo);

		} catch (Exception e) {
			// Ignore exceptions
		}
	}

	/** Removes SA settings from package.xml */
	private void removeProxyProperties(String propName) {
		try {
			Document packageFileDoc = getWinProjMgr().getPackageFileDoc();
			if (packageFileDoc != null) {
				String inputEndPointExpr = String.format(propName,
						this.getName());
				ParserXMLUtility.deleteElement(packageFileDoc,
						inputEndPointExpr);
			}
		} catch (Exception e) {
			// Die silently , there is nothing much that can be done at this
			// point
		}
	}

	// This method is used to enable load balancing in proxy when SSL is already
	// configured.
	private void setLBInProxy(boolean enableLB)
			throws WindowsAzureInvalidProjectOperationException {
		WindowsAzureEndpoint endPoint = getSslOffloadingInputEndpoint();
		WindowsAzureEndpoint sslRedirectEndPoint = getSslOffloadingRedirectionEndpoint();
		// Add startup command
		String saInternalEndPointName = getSslOffloadingInternalEndpoint()
				.getName();
		String startUpCmd = String
				.format(WindowsAzureConstants.SSL_TASK_CMD_VALUE,
						endPoint.getName(), saInternalEndPointName, enableLB,
						getSslOffloadingCert().getFingerPrint(),
						WindowsAzureConstants.SSL_STORE_NAME);

		if (sslRedirectEndPoint != null) {
			startUpCmd += " " + sslRedirectEndPoint.getName();
		}
		addOrUpdateStartUpCmdForProxy(startUpCmd, true);
	}

	/** This API is for disabling session affinity */
	private void disableSessionAffinity()
			throws WindowsAzureInvalidProjectOperationException {
		WindowsAzureEndpoint endPoint = getSslOffloadingInputEndpoint();
		if (endPoint != null) {
			setLBInProxy(false);
		} else {
			removeProxyFilesFromPrj();
			removeProxySettingsFromDefDoc(getSessionAffinityInputEndpoint(),
					getSessionAffinityInternalEndpoint(),
					getSslOffloadingRedirectionEndpoint());
		}

		// Remove properties from package.xml
		removeProxyProperties(WindowsAzureConstants.SA_INPUT_ENDPOINT);
		removeProxyProperties(WindowsAzureConstants.SA_INTERNAL_ENDPOINT);
	}

	/** This API is for disabling ssl configuration */
	private void disableSslOffloading()
			throws WindowsAzureInvalidProjectOperationException {
		WindowsAzureEndpoint proxyInputEP = getSessionAffinityInputEndpoint();
		WindowsAzureEndpoint sslRedirectInputEP = getSslOffloadingRedirectionEndpoint();

		if (proxyInputEP != null) {
			WindowsAzureEndpoint saInternalEP = getSessionAffinityInternalEndpoint();
			boolean enableLoadBalancing = true;

			String startUpCmd = String.format(
					WindowsAzureConstants.TASK_CMD_VALUE,
					proxyInputEP.getName(), saInternalEP.getName(),
					enableLoadBalancing);
			addOrUpdateStartUpCmdForProxy(startUpCmd, true);

			// Remove redirect endpoint if exists
			if (sslRedirectInputEP != null) {
				sslRedirectInputEP.delete();
			}

			// update protocol and remove certificate
			proxyInputEP.setProtocol("tcp");
			proxyInputEP.removeAttribute("certificate");

		} else {
			proxyInputEP = getSslOffloadingInputEndpoint();

			removeProxyFilesFromPrj();
			removeProxySettingsFromDefDoc(proxyInputEP,
					getSslOffloadingInternalEndpoint(), sslRedirectInputEP);
		}

		// remove properties from package.xml
		removeProxyProperties(WindowsAzureConstants.SSL_INPUT_ENDPOINT);
		removeProxyProperties(WindowsAzureConstants.SSL_INTERNAL_ENDPOINT);
		removeProxyProperties(WindowsAzureConstants.SSL_CERT_NAME);
		removeProxyProperties(WindowsAzureConstants.SSL_CERT_FINGERPRINT);
		removeProxyProperties(WindowsAzureConstants.SSL_REDIRECTION_ENDPOINT);

	}

	/**
	 * Get ARR IIS port. By default this returns 31221 if not available then
	 * returns next available one
	 */
	private int getARRPort()
			throws WindowsAzureInvalidProjectOperationException {
		int port = WindowsAzureConstants.IIS_ARR_PORT;

		while (!winProjMgr.isValidPort(String.valueOf(port),
				WindowsAzureEndpointType.Input)) {
			port++;
		}
		return port;
	}

	/**
	 * This method suffixes a string to the name and checks if that name is
	 * available for endpoint
	 */
	private String generateEndPointName(String name, String suffix)
			throws WindowsAzureInvalidProjectOperationException {
		StringBuilder saInternalEndPoint = new StringBuilder(name);
		saInternalEndPoint.append(suffix);

		int sufIncrement = 1;
		while (!winProjMgr.isAvailableRoleName(saInternalEndPoint.toString())) {
			saInternalEndPoint.append(sufIncrement);
			sufIncrement++;
		}
		return saInternalEndPoint.toString();
	}

	/** Returns Session Affinity input endpoint */
	public WindowsAzureEndpoint getSessionAffinityInputEndpoint()
			throws WindowsAzureInvalidProjectOperationException {
		return getProxyEndpoint(WindowsAzureConstants.SA_INPUT_ENDPOINT);
	}

	/** Returns session affinity internal endpoint */
	public WindowsAzureEndpoint getSessionAffinityInternalEndpoint()
			throws WindowsAzureInvalidProjectOperationException {
		return getProxyEndpoint(WindowsAzureConstants.SA_INTERNAL_ENDPOINT);
	}

	/** Returns SSL Offloading input endpoint */
	public WindowsAzureEndpoint getSslOffloadingInputEndpoint()
			throws WindowsAzureInvalidProjectOperationException {
		return getProxyEndpoint(WindowsAzureConstants.SSL_INPUT_ENDPOINT);
	}

	/** Returns SSL Offloading internal endpoint */
	public WindowsAzureEndpoint getSslOffloadingInternalEndpoint()
			throws WindowsAzureInvalidProjectOperationException {
		return getProxyEndpoint(WindowsAzureConstants.SSL_INTERNAL_ENDPOINT);
	}

	/** Returns SSL Offloading redirection endpoint */
	public WindowsAzureEndpoint getSslOffloadingRedirectionEndpoint()
			throws WindowsAzureInvalidProjectOperationException {
		return getProxyEndpoint(WindowsAzureConstants.SSL_REDIRECTION_ENDPOINT);
	}

	/** Deletes SSL Offloading redirection endpoint */
	public void deleteSslOffloadingRedirectionEndpoint()
			throws WindowsAzureInvalidProjectOperationException {
		WindowsAzureEndpoint sslRedirectEP = getProxyEndpoint(WindowsAzureConstants.SSL_REDIRECTION_ENDPOINT);

		if (sslRedirectEP != null) {
			// Remove from package.xml
			removeProxyProperties(WindowsAzureConstants.SSL_REDIRECTION_ENDPOINT);

			// update startup command
			WindowsAzureEndpoint sslInputEndpoint = getSslOffloadingInputEndpoint();
			WindowsAzureEndpoint sslInternalEndpoint = getSslOffloadingInternalEndpoint();
			WindowsAzureCertificate sslCert = getSslOffloadingCert();
			boolean enableLoadBalancing = false;

			if (getSessionAffinityInputEndpoint() != null) {
				enableLoadBalancing = true;
			}

			String startUpCmd = String.format(
					WindowsAzureConstants.SSL_TASK_CMD_VALUE,
					sslInputEndpoint.getName(), sslInternalEndpoint.getName(),
					enableLoadBalancing, sslCert.getFingerPrint(),
					WindowsAzureConstants.SSL_STORE_NAME);

			addOrUpdateStartUpCmdForProxy(startUpCmd, true);

			// delete redirection endpoint
			sslRedirectEP.delete();
		}
	}

	/** Returns SSL certificate associated with SSL proxy configuration */
	public WindowsAzureCertificate getSslOffloadingCert()
			throws WindowsAzureInvalidProjectOperationException {

		// Get values from package.xml
		String fingerPrint = getPropertyAttrValue(WindowsAzureConstants.SSL_CERT_FINGERPRINT);
		String name = getPropertyAttrValue(WindowsAzureConstants.SSL_CERT_NAME);

		if (fingerPrint != null && name != null)
			return new WindowsAzureCertificate(name, fingerPrint);
		else
			return null;
	}

	/** Updates SSLOffloading certification information */
	public void setSslOffloadingCert(WindowsAzureCertificate sslCert)
			throws WindowsAzureInvalidProjectOperationException {

		WindowsAzureEndpoint sslInputEndpoint = getSslOffloadingInputEndpoint();
		WindowsAzureEndpoint sslInternalEndpoint = getSslOffloadingInternalEndpoint();
		WindowsAzureEndpoint sslRedirectEndpoint = getSslOffloadingRedirectionEndpoint();
		boolean enableLoadBalancing = false;

		if (getSessionAffinityInputEndpoint() != null) {
			enableLoadBalancing = true;
		}

		addPropertiesInPackage(WindowsAzureConstants.SSL_CERT_NAME,
				WindowsAzureConstants.SSL_CERT_NAME_PROP, sslCert.getName());
		addPropertiesInPackage(WindowsAzureConstants.SSL_CERT_FINGERPRINT,
				WindowsAzureConstants.SSL_CERT_FINGERPRINT_PROP,
				sslCert.getFingerPrint());

		String startUpCmd = String.format(
				WindowsAzureConstants.SSL_TASK_CMD_VALUE,
				sslInputEndpoint.getName(), sslInternalEndpoint.getName(),
				enableLoadBalancing, sslCert.getFingerPrint(),
				WindowsAzureConstants.SSL_STORE_NAME);

		if (sslRedirectEndpoint != null) {
			startUpCmd += " " + startUpCmd;
		}

		addOrUpdateStartUpCmdForProxy(startUpCmd, true);
		sslInputEndpoint.setCertificate(sslCert.getName());

	}

	/**
	 * Returns endpoint associated with session affinity or ssl configuration
	 * Can be used in a generic manner as well.
	 * */
	private WindowsAzureEndpoint getProxyEndpoint(String endPointProp)
			throws WindowsAzureInvalidProjectOperationException {
		return getEndpoint(getPropertyAttrValue(endPointProp));
	}

	/**
	 * This API fetches property element information from package.xml
	 * 
	 * @param attrName
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	private String getPropertyAttrValue(String attrName)
			throws WindowsAzureInvalidProjectOperationException {
		String propertyValue = null;
		try {
			Document packageFileDoc = getWinProjMgr().getPackageFileDoc();
			if (packageFileDoc != null) {
				XPath xPath = XPathFactory.newInstance().newXPath();
				String endPointExpr = null;
				endPointExpr = String.format(attrName, this.getName());

				Element propEndPoint = (Element) xPath.evaluate(endPointExpr,
						packageFileDoc, XPathConstants.NODE);
				if (propEndPoint != null) {
					propertyValue = propEndPoint.getAttribute("value");
				}
			}
		} catch (Exception e) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Internal error occurred while fetching property info from package.xml",
					e);
		}

		return propertyValue;
	}

	/**
	 * This API changes session affinity settings in startup task and
	 * package.xml if session affinity end point changes
	 */
	protected void reconfigureSessionAffinity(WindowsAzureEndpoint value,
			String newName) throws WindowsAzureInvalidProjectOperationException {
		if (value == null) {
			throw new IllegalArgumentException(
					WindowsAzureConstants.EXCP_EMPTY_OR_INVALID_ENDPOINT);
		}

		String endPointName = null;
		if (value.getEndPointType() == WindowsAzureEndpointType.Input) {
			endPointName = getSessionAffinityInternalEndpoint().getName();
		} else {
			endPointName = getSessionAffinityInputEndpoint().getName();
		}
		Document packageFileDoc = getWinProjMgr().getPackageFileDoc();
		try {
			if (packageFileDoc != null) {
				XPath xPath = XPathFactory.newInstance().newXPath();
				String endPointExpr = null;
				// Change in startup task
				if (value.getEndPointType() == WindowsAzureEndpointType.Input) {
					endPointExpr = String.format(
							WindowsAzureConstants.SA_INPUT_ENDPOINT,
							this.getName());
					changeEndPointInfoInStartupTask(newName, endPointName, true);
				} else if (value.getEndPointType() == WindowsAzureEndpointType.Internal) {
					endPointExpr = String.format(
							WindowsAzureConstants.SA_INTERNAL_ENDPOINT,
							this.getName());
					changeEndPointInfoInStartupTask(endPointName, newName, true);
				}
				// Change in package.xml
				Element propEndPoint = (Element) xPath.evaluate(endPointExpr,
						packageFileDoc, XPathConstants.NODE);
				if (propEndPoint != null)
					propEndPoint.setAttribute("value", newName);
			}
		} catch (Exception e) {
			throw new WindowsAzureInvalidProjectOperationException(
					WindowsAzureConstants.EXCP_SA_NAME_CHANGE, e);
		}

	}

	/**
	 * Gets the list of components that are associated with this role.
	 * 
	 * @return list of components
	 * @throws WindowsAzureInvalidProjectOperationException .
	 */
	public List<WindowsAzureRoleComponent> getComponents()
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
						winCompList.add(getComponentObjFromEle(compEle,
								compEle.getAttribute(WindowsAzureConstants.ATTR_TYPE)));
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
	 * 
	 * @param name
	 *            is importas attribute of component
	 * @return component element
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	private Element addComponentElement()
			throws WindowsAzureInvalidProjectOperationException {
		try {
			Document doc = getWinProjMgr().getPackageFileDoc();
			XPath xPath = XPathFactory.newInstance().newXPath();
			String expr = String.format(WindowsAzureConstants.WA_PACK_NAME,
					getName());
			Element role = (Element) xPath.evaluate(expr, doc,
					XPathConstants.NODE);
			Element eleComp = doc.createElement("component");
			return (Element) role.appendChild(eleComp);
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception while creating new component", ex);
		}
	}

	/**
	 * Create and associate component element with role
	 * 
	 * @param name
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public WindowsAzureRoleComponent addComponent(String attr_name, String value)
			throws WindowsAzureInvalidProjectOperationException {
		try {
			List<WindowsAzureRoleComponent> comps = getComponents();
			WindowsAzureRoleComponent winComp = null;
			Element comp = addComponentElement();
			if (attr_name.equalsIgnoreCase(WindowsAzureConstants.ATTR_IMPORTAS)) {
				comp.setAttribute(WindowsAzureConstants.ATTR_IMPORTAS, value);
				winComp = new WindowsAzureRoleComponent(winProjMgr, this);
				winComp.setDeployName(value);
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
	 * Method swaps component up or down according to direction parameter.
	 * 
	 * @param index
	 * @param direction
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public void swapCmpnt(int index, String direction)
			throws WindowsAzureInvalidProjectOperationException {
		if (direction.isEmpty() || direction == null || index < 0) {
			throw new IllegalArgumentException(
					WindowsAzureConstants.INVALID_ARG);
		}
		String cmpntStr = String.format(WindowsAzureConstants.COMPONENT,
				getName());
		XPath xPath = XPathFactory.newInstance().newXPath();
		Document pacDoc = winProjMgr.getPackageFileDoc();
		try {
			NodeList cmpNodes = (NodeList) xPath.evaluate(cmpntStr, pacDoc,
					XPathConstants.NODESET);
			// move up
			if (direction.equalsIgnoreCase("up")) {
				cmpNodes.item(index)
						.getParentNode()
						.insertBefore(cmpNodes.item(index),
								cmpNodes.item(index - 1));
				Collections.swap(winCompList, index, index - 1);
			} else {
				// move down
				cmpNodes.item(index)
						.getParentNode()
						.insertBefore(cmpNodes.item(index),
								cmpNodes.item(index + 2));
				Collections.swap(winCompList, index, index + 1);
			}
		} catch (Exception e) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception while swapping components", e);
		}
	}

	/**
	 * Method to determine if a component is part of a server, JDK or
	 * application configuration
	 * 
	 * @param envName
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException .
	 */
	public boolean getIsEnvPreconfigured(String envName)
			throws WindowsAzureInvalidProjectOperationException {
		boolean preConFig = false;
		if (envName == null || envName.isEmpty()) {
			throw new IllegalArgumentException(
					WindowsAzureConstants.INVALID_ARG);
		}
		try {
			// Check all components associated with role
			List<WindowsAzureRoleComponent> compList = getComponents();
			for (int i = 0; i < compList.size(); i++) {
				WindowsAzureRoleComponent comp = compList.get(i);
				if (comp.getDeployDir().contains("%" + envName + "%")
						|| comp.getImportPath().contains("%" + envName + "%")) {
					preConFig = true;
					break;
				}
			}
			if (!preConFig) {
				// check all env variables associated with role
				Map<String, String> envMap = getRuntimeEnv();
				Collection<String> col = envMap.values();
				for (Iterator<String> iterator = col.iterator(); iterator
						.hasNext();) {
					String envVal = iterator.next();
					if (envVal.contains("%" + envName + "%")) {
						preConFig = true;
						break;
					}
				}
			}
		} catch (Exception e) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception occurred in", e);
		}
		return preConFig;
	}

	/**
	 * Check the deployment name is valid or not
	 * 
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
			for (Iterator<WindowsAzureRoleComponent> it = components.iterator(); it
					.hasNext();) {
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
	 * This API exposes the source path of the JDK if one is configured,
	 * depending on the presence of Xpath component[@type=jdk.deploy] inside
	 * the appropriate <workerrole> element in package.xml. If no JDK
	 * configured, the API shall return NULL. The return string comes directly
	 * from @importsrc
	 * 
	 * @return source path of JDK
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public String getJDKSourcePath()
			throws WindowsAzureInvalidProjectOperationException {
		String sourcePath = null;
		try {
			XPath xPath = XPathFactory.newInstance().newXPath();
			Document doc = winProjMgr.getPackageFileDoc();
			String expr = String.format(WindowsAzureConstants.COMPONENT_TYPE,
					getName(), "jdk.deploy");
			Element component = (Element) xPath.evaluate(expr, doc,
					XPathConstants.NODE);
			if (component != null) {
				sourcePath = component
						.getAttribute(WindowsAzureConstants.ATTR_IPATH);
				String importMethod = component.getAttribute(WindowsAzureConstants.ATTR_IMETHOD);
				if (!importMethod.equals(WindowsAzureRoleComponentImportMethod.none.toString())
						&& sourcePath.isEmpty()) {
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
	 * Method creates JAVA_HOME value according to JDK path.
	 * 
	 * @param path
	 * @param templateFile
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public static String constructJdkHome(String path, File templateFile)
			throws WindowsAzureInvalidProjectOperationException {
		String envVal = "";
		try {
			// parse template file and find componentset name
			XPath xPath = XPathFactory.newInstance().newXPath();
			Document compDoc = ParserXMLUtility.parseXMLFile(templateFile
					.getAbsolutePath());
			String expr = String.format(
					WindowsAzureConstants.TEMP_COMPONENTSET, "JDK");
			Element compSet = (Element) xPath.evaluate(expr, compDoc,
					XPathConstants.NODE);
			NodeList nodelist = compSet.getChildNodes();
			if (compSet != null) {
				for (int i = 0; i < nodelist.getLength(); i++) {
					Node compNode = (Node) nodelist.item(i);
					if (!compNode.hasAttributes()) {
						continue;
					}
					Element compEle = (Element) compNode;
					if (compEle.getNodeName().equalsIgnoreCase("startupenv")
							&& compEle.getAttribute("type").equalsIgnoreCase(
									"jdk.home")) {
						String jdkDirName = new File(path).getName();
						envVal = compEle.getAttribute("value");
						envVal = envVal.replace("${placeholder}", jdkDirName);
						// if path empty then remove'\'
						if (path.isEmpty()) {
							envVal = envVal.substring(0, envVal.lastIndexOf("%") + 1);
						}
					}
				}
			}
		} catch (Exception e) {
			throw new WindowsAzureInvalidProjectOperationException(
					WindowsAzureConstants.EXCP, e);
		}
		return envVal;
	}

	/**
	 * Method creates server home value according to server name and path.
	 * 
	 * @param name
	 * @param path
	 * @param templateFile
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public static String constructServerHome(String name, String path,
			File templateFile)
			throws WindowsAzureInvalidProjectOperationException {
		String envVal = "";
		try {
			// parse template file and find componentset name
			XPath xPath = XPathFactory.newInstance().newXPath();
			Document compDoc = ParserXMLUtility.parseXMLFile(templateFile
					.getAbsolutePath());
			String expr = String.format(WindowsAzureConstants.TEMP_SERVER_COMP,
					"server", name);
			Element compSet = (Element) xPath.evaluate(expr, compDoc,
					XPathConstants.NODE);
			if (compSet != null) {
				NodeList nodelist = compSet.getChildNodes();
				for (int i = 0; i < nodelist.getLength(); i++) {
					Node compNode = (Node) nodelist.item(i);
					if (!compNode.hasAttributes()) {
						continue;
					}
					Element compEle = (Element) compNode;
					if (compEle.getNodeName().equalsIgnoreCase("startupenv")
							&& compEle.getAttribute("type").equalsIgnoreCase(
									"server.home")) {
						String srvDirName = new File(path).getName();
						envVal = compEle.getAttribute("value");
						envVal = envVal.replace("${placeholder}",
								"%DEPLOYROOT%\\" + srvDirName);
						// if path empty then remove'\'
						if (path.isEmpty()) {
							envVal = envVal.substring(0, envVal.lastIndexOf("%") + 1);
						}
					}
				}
			}
		} catch (Exception e) {
			throw new WindowsAzureInvalidProjectOperationException(
					WindowsAzureConstants.EXCP, e);
		}
		return envVal;
	}

	/**
	 * This API sets the JDK source path, adding the JDK configuration from the
	 * template file (e.g. componentsets.xml ) if its not in package.xml yet.
	 * Only one JDK can be configured per role. When set to NULL, all
	 * <component> and <startupenv> XML with @type starting with the substring
	 * server. shall be removed from <workerrole>
	 * 
	 * @param path
	 * @param templateFile
	 * @throws WindowsAzureInvalidProjectOperationException .
	 */
	public void setJDKSourcePath(String path, File templateFile, String jdkName)
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

			// parse template file and find componentset name
			Document compDoc = ParserXMLUtility.parseXMLFile(templateFile
					.getAbsolutePath());
			// check for third party JDK
			if (jdkName != null && !jdkName.isEmpty()) {
				expr = String.format(WindowsAzureConstants.TEMP_SERVER_COMP,
						"JDK", jdkName);
			} else {
				expr = String.format(WindowsAzureConstants.TEMP_COMPONENTSET,
						"JDK");
			}
			Element compSet = (Element) xPath.evaluate(expr, compDoc,
					XPathConstants.NODE);
			NodeList nodelist = compSet.getChildNodes();
			// Adding JDK component
			if (component == null) {
				if (compSet != null) {
					String parentNode = String.format(
							WindowsAzureConstants.WA_PACK_ROLE, getName());
					Element role = (Element) xPath.evaluate(parentNode, doc,
							XPathConstants.NODE);
					Node preNode = role.getFirstChild();
					Element ele = null;
					// Iterate on child nodes, if it is startupenv \ component
					// add it in package.xml
					for (int i = 0; i < nodelist.getLength(); i++) {
						Node compNode = (Node) nodelist.item(i);
						if (!compNode.hasAttributes()) {
							continue;
						}
						Element compEle = (Element) compNode;
						if (compEle.getNodeName()
								.equalsIgnoreCase("startupenv")) {
							ele = doc.createElement("startupenv");
							String jdkDirName = new File(path).getName();
							NamedNodeMap map = compEle.getAttributes();
							for (int j = 0; j < map.getLength(); j++) {
								ele.setAttribute(map.item(j).getNodeName(), map
										.item(j).getNodeValue());
							}
							String envVal = compEle.getAttribute("value");
							envVal = envVal.replace("${placeholder}",
									jdkDirName);
							// if path empty then remove'\'
							if (compEle.getAttribute("type").equalsIgnoreCase("jdk.home")
									&& path.isEmpty()) {
								envVal = envVal.substring(0, envVal.lastIndexOf("%") + 1);
							}
							ele.setAttribute("value", envVal);

							preNode = role.insertBefore(ele, preNode);
							getRuntimeEnv().put(compEle.getAttribute("name"),
									envVal);
						} else if (compEle.getNodeName().equalsIgnoreCase(
								"component")) {
							ele = doc.createElement("component");
							NamedNodeMap map = compEle.getAttributes();
							for (int j = 0; j < map.getLength(); j++) {
								ele.setAttribute(map.item(j).getNodeName(), map
										.item(j).getNodeValue());
							}
							ele.setAttribute(WindowsAzureConstants.ATTR_IPATH,
									path);
							if (path.isEmpty()) {
								ele.setAttribute(
										WindowsAzureConstants.ATTR_IMETHOD,
										WindowsAzureRoleComponentImportMethod.none.toString());
							}
							preNode = role.insertBefore(ele, preNode);

							WindowsAzureRoleComponent comp = new WindowsAzureRoleComponent(
									winProjMgr, this, compEle.getAttribute(WindowsAzureConstants.ATTR_TYPE));
							comp.setImportPath(path);
							comp.setType(compEle
									.getAttribute(WindowsAzureConstants.ATTR_TYPE));
							comp.setDeployDir(compEle
									.getAttribute(WindowsAzureConstants.ATTR_DDIR));
							if (path.isEmpty()) {
								comp.setImportMethod(WindowsAzureRoleComponentImportMethod.none);
							} else {
								comp.setImportMethod(WindowsAzureRoleComponentImportMethod.valueOf(compEle
										.getAttribute(WindowsAzureConstants.ATTR_IMETHOD)));
							}
							// add JDK component at 0th position
							comps.add(0, comp);
						}

						preNode = preNode.getNextSibling();
					}
				}
			} else {
				for (int i = 0; i < comps.size(); i++) {
					if (comps.get(i).getType().equalsIgnoreCase("jdk.deploy")) {
						comps.get(i).setImportPath(path);
					}
				}
				// If JDK path is updated then update environment variable as
				// well
				expr = String.format(WindowsAzureConstants.WA_PACK_SENV_NAME,
						getName(), WindowsAzureConstants.JAVA_HOME_ENV_VAR);
				Element strenv = (Element) xPath.evaluate(expr, doc,
						XPathConstants.NODE);
				String jdkDirName = new File(path).getName();
				String envVal = "";
				for (int i = 0; i < nodelist.getLength(); i++) {
					Node compNode = (Node) nodelist.item(i);
					if (!compNode.hasAttributes()) {
						continue;
					}
					Element compEle = (Element) compNode;
					if (compEle.getNodeName().equalsIgnoreCase("startupenv")
							&& compEle.getAttribute("type").equalsIgnoreCase(
									"jdk.home")) {

						envVal = compEle.getAttribute("value");
						envVal = envVal.replace("${placeholder}", jdkDirName);
					}
				}
				strenv.setAttribute(WindowsAzureConstants.ATTR_VALUE, envVal);
				/*
				 * Get previously added environment variables.
				 */
				envVarMap = getRuntimeEnv();
				envVarMap.put(WindowsAzureConstants.JAVA_HOME_ENV_VAR, envVal);
			}
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception while geting setJDKSourcePath", ex);
		}

	}

	/**
	 * This API sets the Server source path and name, finding the right
	 * <componentset> based on the provided name and @type=server. It adds the
	 * Server configuration from the template file if its not in package.xml
	 * yet (or replacing whatever is there). Only one server can be configured
	 * per role. When set to NULL, all <component> and <startupenv> XML with @type
	 * starting with the substring server. shall be removed from <workerrole>
	 * 
	 * @param name
	 * @param path
	 * @param templateFile
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public void setServer(String name, String path, File templateFile)
			throws WindowsAzureInvalidProjectOperationException {
		if (path == null || templateFile == null) {
			throw new IllegalArgumentException();
		}

		try {
			// Get instance value before changing server settings
			String instance = getInstances();
			if (name == null) {
				// unassociate the server configuration from role
				remAssocFromRole("server.");
				setInstances(instance);
				return;
			}

			// parse component.xml and go the selected server
			XPath xPath = XPathFactory.newInstance().newXPath();
			Document pacDoc = winProjMgr.getPackageFileDoc();
			Document compDoc = ParserXMLUtility.parseXMLFile(templateFile
					.getAbsolutePath());
			String expr = String.format(WindowsAzureConstants.TEMP_SERVER_COMP,
					"server", name);
			Element compSet = (Element) xPath.evaluate(expr, compDoc,
					XPathConstants.NODE);

			String parentNode = String.format(
					WindowsAzureConstants.WA_PACK_ROLE, getName());
			Element role = (Element) xPath.evaluate(parentNode, pacDoc,
					XPathConstants.NODE);

			String jdkDply = String.format(
					WindowsAzureConstants.COMPONENT_TYPE, getName(),
					"jdk.deploy");
			Node jdkNode = (Node) xPath.evaluate(jdkDply, pacDoc,
					XPathConstants.NODE);
			if (compSet != null) {

				// set server name in property
				expr = String.format(WindowsAzureConstants.SERVER_PROP_PATH,
						getName());
				Element property = (Element) xPath.evaluate(expr, pacDoc,
						XPathConstants.NODE);
				if (property == null) {
					// find parent and append child
					Element projProper = (Element) xPath.evaluate(
							WindowsAzureConstants.PROJ_PROPERTY, pacDoc,
							XPathConstants.NODE);
					property = pacDoc.createElement("property");
					property.setAttribute(WindowsAzureConstants.ATTR_NAME,
							String.format(
									WindowsAzureConstants.SERVER_PROP_NAME,
									getName()));
					property.setAttribute(WindowsAzureConstants.ATTR_VALUE,
							name);
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
						String type = compEle.getAttribute("type");
						envVal = envVal.replace("${placeholder}",
								"%DEPLOYROOT%\\" + jdkDirName);
						if (type.equalsIgnoreCase("server.home")
								&& path.isEmpty()) {
							envVal = envVal.substring(0, envVal.lastIndexOf("%") + 1);
						}
						ele = pacDoc.createElement("startupenv");
						ele.setAttribute("name", compEle.getAttribute("name"));
						ele.setAttribute("value", envVal);
						ele.setAttribute("type", type);
						if (jdkNode != null) {
							role.insertBefore(ele, jdkNode);
						} else {
							role.appendChild(ele);
						}
						getRuntimeEnv().put(compEle.getAttribute("name"),
								envVal);
					} else if (compEle.getNodeName().equalsIgnoreCase(
							"component")) {
						ele = pacDoc.createElement("component");
						String type = compEle
								.getAttribute(WindowsAzureConstants.ATTR_TYPE);
						if (type.equalsIgnoreCase("server.deploy")) {
							NamedNodeMap map = compEle.getAttributes();
							for (int j = 0; j < map.getLength(); j++) {
								ele.setAttribute(map.item(j).getNodeName(), map
										.item(j).getNodeValue());
							}
							ele.setAttribute(WindowsAzureConstants.ATTR_IPATH, path);
							if (path.isEmpty()) {
								ele.setAttribute(WindowsAzureConstants.ATTR_IMETHOD,
										WindowsAzureRoleComponentImportMethod.none.toString());
							}
							Node jdkNxtNode = jdkNode.getNextSibling();
							if (jdkNxtNode == null) {
								role.appendChild(ele);
							} else {
								role.insertBefore(ele, jdkNxtNode);
							}
							// Add server.deploy at 1st position
							WindowsAzureRoleComponent cmpnt = getComponentObjFromEle(ele,
									compEle.getAttribute(WindowsAzureConstants.ATTR_TYPE));
							if (path.isEmpty()) {
								cmpnt.setImportMethod(WindowsAzureRoleComponentImportMethod.none);
							}
							getComponents().add(1, cmpnt);
						} else if (type.equalsIgnoreCase("server.start")) {
							NamedNodeMap map = compEle.getAttributes();
							for (int j = 0; j < map.getLength(); j++) {
								ele.setAttribute(map.item(j).getNodeName(), map
										.item(j).getNodeValue());
							}

							String existingServerApp = String.format(
									WindowsAzureConstants.SERVER_TYPE,
									getName(), "server.app");
							NodeList existingSerAppNodeList = (NodeList) xPath
									.evaluate(existingServerApp, pacDoc,
											XPathConstants.NODESET);

							if (existingSerAppNodeList == null
									|| existingSerAppNodeList.getLength() == 0) {
								/*
								 * If no application is present in package.xml
								 * no need to worry about the order, just append
								 * it at end of the role
								 */
								role.appendChild(ele);
							} else {
								expr = String.format(
										WindowsAzureConstants.SERVER_APP,
										"server", getServerName());
								Element appNodeInCompSet = (Element) xPath
										.evaluate(expr, compDoc,
												XPathConstants.NODE);

								// depend on position of server.app in component
								// set

								if (appNodeInCompSet
										.compareDocumentPosition(compEle) == 2) {
									Node firstAppNode = existingSerAppNodeList
											.item(0);
									// server.app node is before server.start
									role.insertBefore(ele, firstAppNode);
								} else {
									Node lastAppNode = existingSerAppNodeList
											.item(existingSerAppNodeList
													.getLength() - 1);
									role.insertBefore(ele,
											lastAppNode.getNextSibling());
								}

							}
							// Add server.start at 2nd position
							getComponents().add(2, getComponentObjFromEle(ele,
									compEle.getAttribute(WindowsAzureConstants.ATTR_TYPE)));
						}
					}
				}
			}
			setInstances(instance);
		} catch (Exception e) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception while setServer", e);
		}
	}

	protected WindowsAzureRoleComponent getComponentObjFromEle(Element compEle, String type)
			throws WindowsAzureInvalidProjectOperationException {
		try {
			WindowsAzureRoleComponent compobj;
			if (type.isEmpty()) {
				compobj = new WindowsAzureRoleComponent(
						winProjMgr, this);
			} else {
				compobj = new WindowsAzureRoleComponent(
						winProjMgr, this, type);
			}
			if (compEle.hasAttribute(WindowsAzureConstants.ATTR_IMPORTAS)
					&& (!compEle.getAttribute(
							WindowsAzureConstants.ATTR_IMPORTAS).isEmpty())) {
				compobj.setDeployName(compEle
						.getAttribute(WindowsAzureConstants.ATTR_IMPORTAS));
			}

			if (compEle.hasAttribute(WindowsAzureConstants.ATTR_IPATH)) {
				compobj.setImportPath(compEle
						.getAttribute(WindowsAzureConstants.ATTR_IPATH));
			}

			if (compEle.hasAttribute(WindowsAzureConstants.ATTR_TYPE)) {
				compobj.setType(compEle
						.getAttribute(WindowsAzureConstants.ATTR_TYPE));
			}

			if (compEle.hasAttribute(WindowsAzureConstants.ATTR_IMETHOD)) {
				compobj.setImportMethod(WindowsAzureRoleComponentImportMethod.valueOf(compEle
						.getAttribute(WindowsAzureConstants.ATTR_IMETHOD)));
			}

			if (compEle.hasAttribute(WindowsAzureConstants.ATTR_CLOUD_UPLOAD)) {
				compobj.setCloudUploadMode(WARoleComponentCloudUploadMode
						.valueOf(compEle.getAttribute(
								WindowsAzureConstants.ATTR_CLOUD_UPLOAD)
								.toLowerCase()));
			}

			if (compEle.hasAttribute(WindowsAzureConstants.ATTR_DDIR)) {
				compobj.setDeployDir(compEle
						.getAttribute(WindowsAzureConstants.ATTR_DDIR));
			}

			if (compEle.hasAttribute(WindowsAzureConstants.ATTR_DMETHOD)) {
				compobj.setDeployMethod(WindowsAzureRoleComponentDeployMethod
						.valueOf(compEle.getAttribute(
								WindowsAzureConstants.ATTR_DMETHOD)
								.toLowerCase()));
			}
			if (compEle.hasAttribute(WindowsAzureConstants.ATTR_CURL)) {
				compobj.setCloudDownloadURL(compEle
						.getAttribute(WindowsAzureConstants.ATTR_CURL));
			}

			if (compEle.hasAttribute(WindowsAzureConstants.ATTR_CKEY)) {
				compobj.setCloudKey(compEle
						.getAttribute(WindowsAzureConstants.ATTR_CKEY));
			}

			if (compEle.hasAttribute(WindowsAzureConstants.ATTR_CMTHD)) {
				compobj.setCloudMethod(WindowsAzureRoleComponentCloudMethod.valueOf(compEle
						.getAttribute(WindowsAzureConstants.ATTR_CMTHD)));
			}
			return compobj;
		} catch (Exception e) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception in getComponentObjFromEle", e);
		}
	}

	protected void remAssocFromRole(String type)
			throws WindowsAzureInvalidProjectOperationException {
		// find all components and env have type server...
		try {
			XPath xPath = XPathFactory.newInstance().newXPath();
			Document doc = winProjMgr.getPackageFileDoc();
			if (type.equalsIgnoreCase("server.")) {
				// remove server property
				String exprPro = String.format(
						WindowsAzureConstants.SERVER_PROP_PATH, getName());
				Element property = (Element) xPath.evaluate(exprPro, doc,
						XPathConstants.NODE);
				if (property != null) {
					property.getParentNode().removeChild(property);
				}
				// Below condition will hold true only in case of liberty server
				// liberty has third startupenv without type attribute.
				// Hence delete it checking its name and value.
				String startUpName = "JVM_ARGS";
				String startUpExpr = String.format(WindowsAzureConstants.WA_PACK_SENV_NAME_VALUE,
						getName(), startUpName, "-DdefaultHostName=*");
				Element element = (Element) xPath.evaluate(startUpExpr, doc, XPathConstants.NODE);
				if (element != null) {
					renameRuntimeEnv(startUpName, "");
				}
			}

			// remove component and start env
			String expr = String.format(WindowsAzureConstants.SERVER_ASSO,
					getName(), type);
			NodeList nodelist = (NodeList) xPath.evaluate(expr, doc,
					XPathConstants.NODESET);
			for (int i = 0; i < nodelist.getLength(); i++) {
				if (nodelist.item(i).getNodeName()
						.equalsIgnoreCase("startupenv")) {
					// remove start env
					Element ele = (Element) nodelist.item(i);
					renameRuntimeEnv(ele.getAttribute("name"), "");
					// envVarMap.remove(ele.getAttribute("name"));
					// nodelist.item(i).getParentNode().removeChild(nodelist.item(i));
				}
				if (nodelist.item(i).getNodeName()
						.equalsIgnoreCase("component")) {
					Element ele = (Element) nodelist.item(i);
					String componenttype = ele.getAttribute("type");
					if (!"server.app".equalsIgnoreCase(componenttype)) {
						WindowsAzureRoleComponent waComp = null;
						for (int j = 0; j < getComponents().size(); j++) {
							waComp = getComponents().get(j);
							if (waComp
									.getDeployName()
									.equalsIgnoreCase(
											ele.getAttribute(WindowsAzureConstants.ATTR_IMPORTAS))) {
								if (waComp.getDeployName().isEmpty()) {
									if (waComp
											.getImportPath()
											.equalsIgnoreCase(
													ele.getAttribute(WindowsAzureConstants.ATTR_IPATH))) {
										break;
									}
								} else {
									break;
								}
							}
						}
						if (waComp != null) {
							getComponents().remove(waComp);
						}
						nodelist.item(i).getParentNode()
								.removeChild(nodelist.item(i));
					}
				}
			}
		} catch (Exception e) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception while removing server association from role", e);
		}
	}

	/**
	 * This function expose the source path and the name of the server if one is
	 * configured, depending on the presence of the special Ant property storing
	 * the name of the server and then the presence of
	 * component[@type=server.deploy] inside the appropriate <workerrole>
	 * element. If no server is configured, return NULL.
	 * 
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public String getServerSourcePath()
			throws WindowsAzureInvalidProjectOperationException {
		try {
			String srcPath = null;

			XPath xPath = XPathFactory.newInstance().newXPath();
			Document doc = winProjMgr.getPackageFileDoc();
			String expr = String.format(WindowsAzureConstants.SERVER_TYPE,
					getName(), "server.deploy");
			Element node = (Element) xPath.evaluate(expr, doc,
					XPathConstants.NODE);
			if (node != null) {
				srcPath = node.getAttribute(WindowsAzureConstants.ATTR_IPATH);
				String importMethod = node.getAttribute(WindowsAzureConstants.ATTR_IMETHOD);
				if (!importMethod.equals(WindowsAzureRoleComponentImportMethod.none.toString())
						&& srcPath.isEmpty()) {
					srcPath = null;
				}
			}
			return srcPath;
		} catch (Exception e) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception in getServerSourcePath", e);
		}
	}

	/**
	 * This function expose the source path and the name of the server if one is
	 * configured, depending on the presence of the special Ant property storing
	 * the name of the server and then the presence of
	 * component[@type=server.deploy] inside the appropriate <workerrole>
	 * element. If no server is configured .
	 * 
	 * @param path
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public void setServerSourcePath(String name, String path, File templateFile)
			throws WindowsAzureInvalidProjectOperationException {
		if (path == null) {
			throw new IllegalArgumentException();
		}
		try {
			XPath xPath = XPathFactory.newInstance().newXPath();
			Document doc = winProjMgr.getPackageFileDoc();
			String expr = String.format(
					WindowsAzureConstants.WA_PACK_SENV_TYPE, getName(),
					"server.home");
			Element envNameEle = (Element) xPath.evaluate(expr, doc,
					XPathConstants.NODE);
			if (envNameEle == null) {
				throw new WindowsAzureInvalidProjectOperationException(
						"server is not configured");
			}
			// find ${placeholder} value from templateFile
			Document compDoc = ParserXMLUtility.parseXMLFile(templateFile
					.getAbsolutePath());
			expr = String.format(WindowsAzureConstants.SERVER_HOME, "server",
					name);
			Element serHome = (Element) xPath.evaluate(expr, compDoc,
					XPathConstants.NODE);
			String envVal = "";
			if (serHome != null) {
				envVal = serHome.getAttribute(WindowsAzureConstants.ATTR_VALUE);
				envVal = envVal.replace("${placeholder}", "%DEPLOYROOT%\\"
						+ new File(path).getName());
			}

			envNameEle.setAttribute(WindowsAzureConstants.ATTR_VALUE, envVal);
			String envName = envNameEle
					.getAttribute(WindowsAzureConstants.ATTR_NAME);
			getRuntimeEnv().put(envName, envVal);
			for (int i = 0; i < getComponents().size(); i++) {
				if (winCompList.get(i).getType()
						.equalsIgnoreCase("server.deploy")) {
					getComponents().get(i).setImportPath(path);
					if (path.isEmpty()) {
						getComponents().get(i).setImportMethod
						(WindowsAzureRoleComponentImportMethod.none);
					}
				}
			}

		} catch (Exception e) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception in setServerSourcePath", e);
		}

	}

	/**
	 * This function expose the source path and the name of the server if one is
	 * configured, depending on the presence of the special Ant property storing
	 * the name of the server and then the presence of
	 * component[@type=server.deploy] inside the appropriate <workerrole>
	 * element. If no server is configured, return NULL.
	 * 
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public String getServerName()
			throws WindowsAzureInvalidProjectOperationException {
		try {
			String srcPath = null;
			XPath xPath = XPathFactory.newInstance().newXPath();
			Document doc = winProjMgr.getPackageFileDoc();
			String expr = String.format(WindowsAzureConstants.SERVER_PROP_PATH,
					getName());
			Element property = (Element) xPath.evaluate(expr, doc,
					XPathConstants.NODE);
			if (property != null) {
				srcPath = property
						.getAttribute(WindowsAzureConstants.ATTR_VALUE);
			}
			return srcPath;
		} catch (Exception e) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception in getServerName", e);
		}
	}

	/**
	 * This finds the appropriate <component> template (@type=server.app)
	 * inside the provided componentsets.xml file, copies it over right before
	 * the <component> element with @type=server.start in package.xml, and
	 * sets its @importsrc, @importmethod (auto or copy), and @importas, as
	 * figured out by the plugin depending on the user input.
	 * 
	 * @param importSrc
	 * @param importAs
	 * @param importMethod
	 * @param templateFile
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public void addServerApplication(String importSrc, String importAs,
			String importMethod, File templateFile, boolean needCldAttr)
			throws WindowsAzureInvalidProjectOperationException {
		if (importSrc == null || importAs == null || importMethod == null
				|| templateFile == null || importSrc.isEmpty()
				|| importAs.isEmpty() || importMethod.isEmpty()) {
			throw new IllegalArgumentException();
		}
		try {
			List<WindowsAzureRoleComponent> comps = getComponents();
			// find appropriate component node in template file
			XPath xPath = XPathFactory.newInstance().newXPath();
			Document compDoc = ParserXMLUtility.parseXMLFile(templateFile
					.getAbsolutePath());
			String expr = String.format(WindowsAzureConstants.SERVER_APP,
					"server", getServerName());
			Element comp = (Element) xPath.evaluate(expr, compDoc,
					XPathConstants.NODE);

			String compServerStart = String.format(
					WindowsAzureConstants.SERVER_START, "server",
					getServerName());
			Element compServerStartElement = (Element) xPath.evaluate(
					compServerStart, compDoc, XPathConstants.NODE);

			// copy the node in package.xml file
			Document packageDoc = winProjMgr.getPackageFileDoc();
			String parentNode = String.format(
					WindowsAzureConstants.WA_PACK_ROLE, getName());
			Element role = (Element) xPath.evaluate(parentNode, packageDoc,
					XPathConstants.NODE);

			String serverStart = String.format(
					WindowsAzureConstants.SERVER_TYPE, getName(),
					"server.start");
			Element serStartNode = (Element) xPath.evaluate(serverStart,
					packageDoc, XPathConstants.NODE);

			String existingServerApp = String.format(
					WindowsAzureConstants.SERVER_TYPE, getName(), "server.app");
			NodeList existingSerAppNodeList = (NodeList) xPath.evaluate(
					existingServerApp, packageDoc, XPathConstants.NODESET);

			Element app = packageDoc.createElement("component");
			if (comp != null) {
				NamedNodeMap map = comp.getAttributes();
				for (int j = 0; j < map.getLength(); j++) {
					app.setAttribute(map.item(j).getNodeName(), map.item(j)
							.getNodeValue());
				}
			} else {
				app.setAttribute(WindowsAzureConstants.ATTR_DDIR,
						"%SERVER_APPS_LOCATION%");
				app.setAttribute(WindowsAzureConstants.ATTR_DMETHOD, "copy");
				app.setAttribute(WindowsAzureConstants.ATTR_TYPE, "server.app");
			}
			app.setAttribute(WindowsAzureConstants.ATTR_IPATH, importSrc);
			app.setAttribute(WindowsAzureConstants.ATTR_IMPORTAS, importAs);
			app.setAttribute(WindowsAzureConstants.ATTR_IMETHOD, importMethod);
			if (needCldAttr) {
				app.setAttribute(WindowsAzureConstants.ATTR_CLOUD_UPLOAD,
						WARoleComponentCloudUploadMode.always.toString());
				app.setAttribute(WindowsAzureConstants.ATTR_CURL, "auto");
				app.setAttribute(WindowsAzureConstants.ATTR_CMTHD, "copy");
			}

			if (existingSerAppNodeList == null
					|| existingSerAppNodeList.getLength() == 0) {
				if (compServerStartElement != null) {
					/*
					 * server is configured.
					 * check position of server.app in component set
					 */
					if (comp.compareDocumentPosition(compServerStartElement) == 4) {
						// server.app node is before server.start
						role.insertBefore(app, serStartNode);
					} else {
						role.insertBefore(app, serStartNode.getNextSibling());
					}
				} else {
					// server is not configured yet, hence insert app node before workerrole node
					role.insertBefore(app, null);
				}
			} else {
				// server.app exists, hence just append new app to existing apps
				Node lastAppNode = existingSerAppNodeList
						.item(existingSerAppNodeList.getLength() - 1);
				role.insertBefore(app, lastAppNode.getNextSibling());
			}
			if (comp != null) {
				comps.add(getComponentObjFromEle(app,
						comp.getAttribute(WindowsAzureConstants.ATTR_TYPE)));
			} else {
				comps.add(getComponentObjFromEle(app, "server.app"));
			}
		} catch (Exception e) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception in addServerApplication", e);
		}
	}

	/***
	 * This API returns the names of the applications configured for this role,
	 * based on an XPath like this, in the context of the appropriate
	 * <workerrole> in package.xml: component[@type="server.app"]/@importas
	 * 
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public List<WindowsAzureRoleComponent> getServerApplications()
			throws WindowsAzureInvalidProjectOperationException {

		try {
			ArrayList<WindowsAzureRoleComponent> serverComp = new ArrayList<WindowsAzureRoleComponent>();
			for (int i = 0; i < getComponents().size(); i++) {
				if (getComponents().get(i).getType()
						.equalsIgnoreCase("server.app")) {
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
	 * This API will remove an application from package.xml. This deletes the
	 * <component> (@type=server.app and @name=?) inside the appropriate
	 * <workerrole> element in package.xml.
	 * 
	 * @throws WindowsAzureInvalidProjectOperationException
	 * 
	 */
	public void removeServerApplication(String importAs)
			throws WindowsAzureInvalidProjectOperationException {
		if (importAs == null || importAs.isEmpty()) {
			throw new IllegalArgumentException();
		}
		try {
			XPath xPath = XPathFactory.newInstance().newXPath();
			Document doc = winProjMgr.getPackageFileDoc();
			String expr = String.format(
					WindowsAzureConstants.COMPONENT_TYPE_IMPORTAS, getName(),
					"server.app", importAs);
			Node comp = (Node) xPath.evaluate(expr, doc, XPathConstants.NODE);
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
	 * 
	 * @param name
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	protected void setVarInDefFile(String name)
			throws WindowsAzureInvalidProjectOperationException {
		// String value = "";
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException(
					WindowsAzureConstants.INVALID_ARG);
		}

		try {
			XPath xPath = XPathFactory.newInstance().newXPath();
			Document doc = winProjMgr.getdefinitionFileDoc();

			// Find Startup tag
			String startupWa = String.format(
					WindowsAzureConstants.STARTUP_WR_NAME, getName());
			String parentNode = String.format(WindowsAzureConstants.WR_NAME,
					getName());
			ParserXMLUtility.updateOrCreateElement(doc, startupWa, parentNode,
					"Startup", true, null);

			// find task Tag
			String taskCmd = String.format(
					WindowsAzureConstants.STARTUP_WITH_STARTUP_CMD, getName());
			Map<String, String> attr = new HashMap<String, String>();
			attr.put("commandLine", WindowsAzureConstants.TASK_CMD_LINE);
			ParserXMLUtility.updateOrCreateElement(doc, taskCmd, startupWa,
					"Task", false, attr);

			// environment tag
			String expr = String.format(WindowsAzureConstants.ENVIRONMENT,
					getName());
			ParserXMLUtility.updateOrCreateElement(doc, expr, taskCmd,
					"Environment", true, null);

			Element env = (Element) xPath.evaluate(expr, doc,
					XPathConstants.NODE);

			// find Variable tag, if not present create new
			Element var = (Element) xPath.evaluate(
					String.format("./Variable[@name='%s']", name), env,
					XPathConstants.NODE);
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
	 * 
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException .
	 */
	public String getRuntimeEnvType(String varName)
			throws WindowsAzureInvalidProjectOperationException {
		if (varName == null || varName.isEmpty()) {
			throw new IllegalArgumentException(
					WindowsAzureConstants.INVALID_ARG);
		}
		try {
			XPath xPath = XPathFactory.newInstance().newXPath();
			Document doc = getWinProjMgr().getPackageFileDoc();
			String expr = String
					.format(WindowsAzureConstants.WA_PACK_SENV_NAME, getName(),
							varName)
					+ "/@type";
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
	 * API to find the name of the environment variable set in the role
	 * according to its type.
	 * 
	 * @param type
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public String getRuntimeEnvName(String type)
			throws WindowsAzureInvalidProjectOperationException {
		if (type == null || type.isEmpty()) {
			throw new IllegalArgumentException(
					WindowsAzureConstants.INVALID_ARG);
		}
		try {
			XPath xPath = XPathFactory.newInstance().newXPath();
			Document doc = getWinProjMgr().getPackageFileDoc();
			String expr = String.format(
					WindowsAzureConstants.WA_PACK_SENV_TYPE, getName(), type)
					+ "/@name";
			String name = xPath.evaluate(expr, doc);
			if (name.isEmpty()) {
				name = null;
			}
			return name;
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
			// check first the env is already present, if yes. edit the same
			String env = String.format(WindowsAzureConstants.WA_PACK_SENV_NAME,
					getName(), varName);
			Element envNode = (Element) xPath.evaluate(env, doc,
					XPathConstants.NODE);

			if (envNode == null) {
				throw new WindowsAzureInvalidProjectOperationException(varName
						+ " Runtime variable is not present");
			}
			envNode.setAttribute(WindowsAzureConstants.ATTR_TYPE, envType);
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					WindowsAzureConstants.EXCP, ex);
		}

	}

	/**
	 * This API to set cache memory size (and enable caching)
	 * 
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
			// if value is 0 then disable all cache.
			if (isCachingEnable()) {
				disableCache();
				setCacheStorageAccountName("");
				setCacheStorageAccountKey("");
				setCacheStorageAccountUrl("");
			}
		} else {
			try {
				// check cache is already exist...
				if (!isCachingEnable()) {
					// enable Cache
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
			String expr = String.format(
					WindowsAzureConstants.CONFIG_SETTING_ROLE, getName(),
					settingName);
			ele = (Element) xPath.evaluate(expr, doc, XPathConstants.NODE);
		} catch (Exception ex) {
			ele = null;
		}
		return ele;
	}

	/**
	 * This API will remove all setting from CSDEF: For corresponding WorkerRole
	 * element, in Imports, ensure presence of <Import moduleName="Caching" />
	 * CSCFG: For corresponding Role element, in ConfigurationSettings, ensure
	 * presence of: <Setting
	 * name="Microsoft.WindowsAzure.Plugins.Caching.CacheSizePercentage"
	 * value="value" /> <Setting
	 * name="Microsoft.WindowsAzure.Plugins.Caching.Loglevel" value="" />
	 * <Setting name=
	 * "Microsoft.WindowsAzure.Plugins.Caching.ConfigStoreConnectionString"
	 * value="UseDevelopmentStorage=true" /> <Setting
	 * name="Microsoft.WindowsAzure.Plugins.Caching.NamedCaches" value=
	 * "{&quot;caches&quot;:[{&quot;name&quot;:&quot;default&quot;,&quot;policy&quot;:{&quot;expiration&quot;:{&quot;defaultTTL&quot;:10,&quot;isExpirable&quot;:true,&quot;type&quot;:1}}}]}"
	 * />*(The goo inside is HTML-encoded JSON)
	 * 
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

			// Remove all memcache including default
			// Get all cache from role and remove all by iterating on map

			List<WindowsAzureNamedCache> list = new ArrayList<WindowsAzureNamedCache>();
			for (WindowsAzureNamedCache cache : mapcache.values()) {
				list.add(cache);
			}
			for (int i = 0; i < list.size(); i++) {
				list.get(i).delete();
			}
			getLocalStorage(WindowsAzureConstants.CACHE_LS_NAME).delete();

			// Remove entries from CSDFG file
			Document doc = getWinProjMgr().getConfigFileDoc();
			String expr = String.format(
					WindowsAzureConstants.CONFIG_SETTING_ROLE, getName(),
					WindowsAzureConstants.SET_CACHESIZEPER);
			ParserXMLUtility.deleteElement(doc, expr);

			expr = String.format(WindowsAzureConstants.CONFIG_SETTING_ROLE,
					getName(), WindowsAzureConstants.SET_CONFIGCONN);
			ParserXMLUtility.deleteElement(doc, expr);

			expr = String.format(WindowsAzureConstants.CONFIG_SETTING_ROLE,
					getName(), WindowsAzureConstants.SET_DIAGLEVEL);
			ParserXMLUtility.deleteElement(doc, expr);

			expr = String.format(WindowsAzureConstants.CONFIG_SETTING_ROLE,
					getName(), WindowsAzureConstants.SET_NAMEDCACHE);
			ParserXMLUtility.deleteElement(doc, expr);

		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					WindowsAzureConstants.EXCP, ex);
		}

	}

	/**
	 * This API will add import statement, local storege and endpoint in csdef
	 * file and settings tags in cscfg file.
	 * 
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	private void enableCache()
			throws WindowsAzureInvalidProjectOperationException {
		try {
			// add import
			Document doc = winProjMgr.getdefinitionFileDoc();
			String expr = String.format(WindowsAzureConstants.IMPORT_NANE,
					getName(), "Caching");
			String parentNodeExpr = String.format(WindowsAzureConstants.IMPORT,
					getName());
			String eleImports = ParserXMLUtility.getExpressionValue(doc,
					parentNodeExpr);

			HashMap<String, String> map = new HashMap<String, String>();
			if (eleImports == null || eleImports.isEmpty()) {
				String parentNode = String.format(
						WindowsAzureConstants.WR_NAME, getName());
				ParserXMLUtility.updateOrCreateElement(doc, parentNodeExpr,
						parentNode, "Imports", false, map);
			}
			map = new HashMap<String, String>();
			map.put("moduleName", "Caching");
			ParserXMLUtility.updateOrCreateElement(doc, expr, parentNodeExpr,
					"Import", false, map);

			// add local storage
			addLocalStorage(WindowsAzureConstants.CACHE_LS_NAME, 20000, false,
					WindowsAzureConstants.CACHE_LS_PATH);

			// add setting tags
			doc = winProjMgr.getConfigFileDoc();
			expr = String.format(WindowsAzureConstants.CONFIG_SETTING_ROLE,
					getName(), WindowsAzureConstants.SET_CACHESIZEPER);
			parentNodeExpr = String.format(
					WindowsAzureConstants.CONFIG_ROLE_SET, getName());

			String configSet = ParserXMLUtility.getExpressionValue(doc,
					parentNodeExpr);
			if (configSet == null || configSet.isEmpty()) {
				map.clear();
				String parentNode = String.format(
						WindowsAzureConstants.ROLE_NAME, getName());
				ParserXMLUtility.updateOrCreateElement(doc, parentNodeExpr,
						parentNode, "ConfigurationSettings", false, map);
			}

			map.clear();
			map.put("name", WindowsAzureConstants.SET_CACHESIZEPER);
			map.put("value", "");
			ParserXMLUtility.updateOrCreateElement(doc, expr, parentNodeExpr,
					"Setting", false, map);

			expr = String.format(WindowsAzureConstants.CONFIG_SETTING_ROLE,
					getName(), WindowsAzureConstants.SET_DIAGLEVEL);
			map.clear();
			map.put("name", WindowsAzureConstants.SET_DIAGLEVEL);
			map.put("value", "1");
			ParserXMLUtility.updateOrCreateElement(doc, expr, parentNodeExpr,
					"Setting", false, map);

			expr = String.format(WindowsAzureConstants.CONFIG_SETTING_ROLE,
					getName(), WindowsAzureConstants.SET_CONFIGCONN);
			map.clear();
			map.put("name", WindowsAzureConstants.SET_CONFIGCONN);
			map.put("value", WindowsAzureConstants.SET_CONFIGCONN_VAL);
			ParserXMLUtility.updateOrCreateElement(doc, expr, parentNodeExpr,
					"Setting", false, map);

			// add Internal endpoint
			String newCache = JSONHelper.createObject();
			// Add setting in config file
			parentNodeExpr = String.format(
					WindowsAzureConstants.CONFIG_ROLE_SET, getName());
			expr = String.format(WindowsAzureConstants.CONFIG_SETTING_ROLE,
					getName(), WindowsAzureConstants.SET_NAMEDCACHE);
			map.clear();
			map.put("name", WindowsAzureConstants.SET_NAMEDCACHE);
			map.put("value", newCache);
			ParserXMLUtility.updateOrCreateElement(doc, expr, parentNodeExpr,
					"Setting", false, map);
			addNamedCache("default", 11211);

		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					WindowsAzureConstants.EXCP, ex);
		}

	}

	/**
	 * This method is to tell whether caching is enable or not.
	 * 
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	protected boolean isCachingEnable()
			throws WindowsAzureInvalidProjectOperationException {
		boolean isEnable = false;
		Element eleCache = getImportEle("Caching");
		if (eleCache != null) {
			isEnable = true;
		}
		return isEnable;
	}

	private Element getImportEle(String moduleName)
			throws WindowsAzureInvalidProjectOperationException {
		Element eleImport = null;
		try {
			Document doc = getWinProjMgr().getdefinitionFileDoc();
			XPath xPath = XPathFactory.newInstance().newXPath();
			String expr = String.format(WindowsAzureConstants.IMPORT_NANE,
					getName(), moduleName);
			eleImport = (Element) xPath
					.evaluate(expr, doc, XPathConstants.NODE);
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					WindowsAzureConstants.EXCP, ex);
		}
		return eleImport;
	}

	/**
	 * API to read Cache memory setting (and to determine if caching is enabled
	 * if this returns > 0).
	 * 
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
				String expr = String.format(
						WindowsAzureConstants.CONFIG_SETTING_ROLE_VAL,
						getName(), WindowsAzureConstants.SET_CACHESIZEPER);
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
	 * set the storage account name associated with the cache. The information
	 * is persisted on the plug-in property in package.xml named
	 * project.workerrole1.cachestorageaccount.name. It also written to the
	 * CSCFG but what exactly is put into the CSCFG depends on whether the
	 * project is built for the cloud or for the emulator. The basic idea is
	 * that as long as it's for Emulator, dev storage is enabled in the CSCFG
	 * (regardless of storage account settings), and if it's for the Cloud, then
	 * the account credentials are enabled in the CSCFG.
	 * 
	 * @param name
	 *            .
	 * @throws WindowsAzureInvalidProjectOperationException .
	 */
	public void setCacheStorageAccountName(String name)
			throws WindowsAzureInvalidProjectOperationException {
		setProperty(WindowsAzureConstants.CACHE_ST_ACC_NAME_PROP, name);
		// sets entry in cscfg
		setCacheSettingInCscfg(name, getCacheStorageAccountKey(),
				getCacheStorageAccountUrl());
	}

	/**
	 * Sets the storage account name from the plugin property and sets setting
	 * value in config depending upon the package type.
	 * 
	 * @param key
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public void setCacheStorageAccountKey(String key)
			throws WindowsAzureInvalidProjectOperationException {
		setProperty(WindowsAzureConstants.CACHE_ST_ACC_KEY_PROP, key);
		// sets entry in cscfg
		setCacheSettingInCscfg(getCacheStorageAccountName(), key,
				getCacheStorageAccountUrl());
	}

	/**
	 * Sets the storage account blob endpoint url from the plugin property and
	 * sets setting value in config depending upon the package type.
	 * 
	 * @param url
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public void setCacheStorageAccountUrl(String url)
			throws WindowsAzureInvalidProjectOperationException {
		setProperty(WindowsAzureConstants.CACHE_ST_ACC_URL_PROP, url);
		setCacheSettingInCscfg(getCacheStorageAccountName(),
				getCacheStorageAccountKey(), url);
	}

	/**
	 * Utility method to set role property in package.xml.
	 * 
	 * @param property
	 * @param value
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public void setProperty(String property, String value)
			throws WindowsAzureInvalidProjectOperationException {
		try {
			String propName = String.format(property, getName());
			if (value == null || value.isEmpty()) {
				// remove property from package.xml
				ParserXMLUtility
						.deleteElement(winProjMgr.getPackageFileDoc(), String
								.format(WindowsAzureConstants.ROLE_PROP,
										propName));
			} else {
				// add entry in package.xml
				addPropertyInPackageXML(propName, value);
			}
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					WindowsAzureConstants.EXCP, ex);
		}
	}

	/**
	 * Utility method to get role property from package.xml.
	 * 
	 * @param property
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public String getProperty(String property)
			throws WindowsAzureInvalidProjectOperationException {
		String propVal = null;
		try {
			Document packageFileDoc = getWinProjMgr().getPackageFileDoc();
			if (packageFileDoc != null) {
				String propName = String.format(property, getName());
				String nodeExpr = String.format(
						WindowsAzureConstants.ROLE_PROP_VAL, propName);
				XPath xPath = XPathFactory.newInstance().newXPath();
				propVal = xPath.evaluate(nodeExpr, packageFileDoc);
			}
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					WindowsAzureConstants.EXCP, ex);
		}
		return propVal;
	}

	/**
	 * Sets third party JDK name in the plugin property.
	 * 
	 * @param value
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public void setJDKCloudName(String value)
			throws WindowsAzureInvalidProjectOperationException {
		setProperty(WindowsAzureConstants.THRD_PARTY_JDK_NAME, value);
	}

	/**
	 * Returns third party JDK name from the plugin property.
	 * 
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public String getJDKCloudName()
			throws WindowsAzureInvalidProjectOperationException {
		String jdkCloudName = getProperty(WindowsAzureConstants.THRD_PARTY_JDK_NAME);
		return jdkCloudName;
	}

	/**
	 * Sets third party server name in the plugin property.
	 * @param value
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public void setServerCloudName(String value)
			throws WindowsAzureInvalidProjectOperationException {
		setProperty(WindowsAzureConstants.THRD_PARTY_SRV_NAME, value);
	}

	/**
	 * Returns third party server name from the plugin property.
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public String getServerCloudName()
			throws WindowsAzureInvalidProjectOperationException {
		String srvCloudName = getProperty(WindowsAzureConstants.THRD_PARTY_SRV_NAME);
		return srvCloudName;
	}

	/**
	 * Sets the JDK Cloud Home in the plugin property.
	 * 
	 * @param value
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public void setJDKCloudHome(String value)
			throws WindowsAzureInvalidProjectOperationException {
		setProperty(WindowsAzureConstants.JDK_CLOUD_HOME, value);
	}

	/**
	 * Returns the JDK Cloud Home from the plugin property.
	 * 
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public String getJDKCloudHome()
			throws WindowsAzureInvalidProjectOperationException {
		String jdkCloudHome = getProperty(WindowsAzureConstants.JDK_CLOUD_HOME);
		return jdkCloudHome;
	}

	/**
	 * Sets the JDK Local Home in the plugin property.
	 * 
	 * @param value
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public void setJDKLocalHome(String value)
			throws WindowsAzureInvalidProjectOperationException {
		setProperty(WindowsAzureConstants.JDK_LOCAL_HOME, value);
	}

	/**
	 * Returns the JDK local Home from the plugin property.
	 * 
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public String getJDKLocalHome()
			throws WindowsAzureInvalidProjectOperationException {
		String jdkLocalHome = getProperty(WindowsAzureConstants.JDK_LOCAL_HOME);
		return jdkLocalHome;
	}

	/**
	 * Sets the Server Cloud Home in the plugin property.
	 * 
	 * @param value
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public void setServerCloudHome(String value)
			throws WindowsAzureInvalidProjectOperationException {
		setProperty(WindowsAzureConstants.SRV_CLOUD_HOME, value);
	}

	/**
	 * Returns the Server Cloud Home from the plugin property.
	 * 
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public String getServerCloudHome()
			throws WindowsAzureInvalidProjectOperationException {
		String srvCloudHome = getProperty(WindowsAzureConstants.SRV_CLOUD_HOME);
		return srvCloudHome;
	}

	/**
	 * Sets the Server Local Home in the plugin property.
	 * 
	 * @param value
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public void setServerLocalHome(String value)
			throws WindowsAzureInvalidProjectOperationException {
		setProperty(WindowsAzureConstants.SRV_LOCAL_HOME, value);
	}

	/**
	 * Returns the Server local Home from the plugin property.
	 * 
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public String getServerLocalHome()
			throws WindowsAzureInvalidProjectOperationException {
		String srvLocalHome = getProperty(WindowsAzureConstants.SRV_LOCAL_HOME);
		return srvLocalHome;
	}

	/**
	 * This API sets cache setting in config file. If package type is local then
	 * the the value will be UseDevelopmentStorage=true else
	 * "DefaultEndpointsProtocol=https;AccountName=name;AccountKey=key".
	 * 
	 * @param name
	 * @param value
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	protected void setCacheSettingInCscfg(String name, String value, String url)
			throws WindowsAzureInvalidProjectOperationException {
		try {
			Document doc = winProjMgr.getConfigFileDoc();
			String expr = String.format(
					WindowsAzureConstants.CONFIG_SETTING_ROLE, getName(),
					WindowsAzureConstants.SET_CONFIGCONN);
			String parentNodeExpr = String.format(
					WindowsAzureConstants.CONFIG_ROLE_SET, getName());
			HashMap<String, String> map = new HashMap<String, String>();
			if (isCachingEnable()) {
				if (winProjMgr.getPackageType() == WindowsAzurePackageType.CLOUD
						&& (name == null || name.isEmpty())
						&& (value == null || value.isEmpty())
						&& (url == null || url.isEmpty())) {
					// Donot do any thing.
				} else if (winProjMgr.getPackageType() == WindowsAzurePackageType.LOCAL
						|| name == null
						|| value == null
						|| url == null
						|| name.isEmpty() || value.isEmpty() || url.isEmpty()) {
					map.clear();
					map.put(WindowsAzureConstants.ATTR_NAME,
							WindowsAzureConstants.SET_CONFIGCONN);
					map.put(WindowsAzureConstants.ATTR_VALUE,
							WindowsAzureConstants.SET_CONFIGCONN_VAL);
					ParserXMLUtility.updateOrCreateElement(doc, expr,
							parentNodeExpr, "Setting", false, map);
				} else if (name != null && value != null && url != null) {
					String val = String.format(
							WindowsAzureConstants.SET_CONFIGCONN_VAL_CLOULD,
							url, name, value);
					map.clear();
					map.put(WindowsAzureConstants.ATTR_NAME,
							WindowsAzureConstants.SET_CONFIGCONN);
					map.put(WindowsAzureConstants.ATTR_VALUE, val);
					ParserXMLUtility.updateOrCreateElement(doc, expr,
							parentNodeExpr, "Setting", false, map);
				}
			}
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					WindowsAzureConstants.EXCP, ex);
		}
	}

	/**
	 * Gets the storage account key from the plugin property.
	 * 
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public String getCacheStorageAccountKey()
			throws WindowsAzureInvalidProjectOperationException {
		String key = getProperty(WindowsAzureConstants.CACHE_ST_ACC_KEY_PROP);
		return key;
	}

	/**
	 * Gets the storage account name from the plug-in property.
	 * 
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public String getCacheStorageAccountName()
			throws WindowsAzureInvalidProjectOperationException {
		String name = getProperty(WindowsAzureConstants.CACHE_ST_ACC_NAME_PROP);
		return name;
	}

	/**
	 * Gets the storage account blob endpoint URL from the plugin property.
	 * 
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public String getCacheStorageAccountUrl()
			throws WindowsAzureInvalidProjectOperationException {
		String url = getProperty(WindowsAzureConstants.CACHE_ST_ACC_URL_PROP);
		return url;
	}

	/**
	 * This API will set property in package.xml in waprojectproperties target.
	 * 
	 * @param name
	 * @param value
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	private void addPropertyInPackageXML(String name, String value)
			throws WindowsAzureInvalidProjectOperationException {
		Document packageFileDoc;
		try {
			packageFileDoc = getWinProjMgr().getPackageFileDoc();
			if (packageFileDoc != null) {
				String nodeExpr = String.format(
						WindowsAzureConstants.ROLE_PROP, name);
				String parentNodeExpr = WindowsAzureConstants.PROJ_PROPERTY;

				HashMap<String, String> nodeAttr = new HashMap<String, String>();
				nodeAttr.put(WindowsAzureConstants.ATTR_NAME, name);
				nodeAttr.put(WindowsAzureConstants.ATTR_VALUE, value);
				ParserXMLUtility.updateOrCreateElement(packageFileDoc,
						nodeExpr, parentNodeExpr,
						WindowsAzureConstants.PROJ_PROPERTY_ELEMENT_NAME,
						false, nodeAttr);
			}
		} catch (WindowsAzureInvalidProjectOperationException ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					WindowsAzureConstants.EXCP, ex);
		}
	}

	/**
	 * This function will add a new named cache. Throw if
	 * getCacheMemoryPercent() returns 0, or name is null or in use by another
	 * cache (case insensitive comparison) or port is not between 1-65535, or
	 * already in use by anything else Create internal endpoint named
	 * memcache_name and the supplied port Inject the new cache object into
	 * the JSON with the specified name and return a WindowsAzureNamedCache
	 * object from the API.
	 * 
	 * @param name
	 * @param port
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */

	public WindowsAzureNamedCache addNamedCache(String name, int port)
			throws WindowsAzureInvalidProjectOperationException {
		if (name == null || name.isEmpty() || port < 1 || port > 65535) {
			throw new IllegalArgumentException(
					WindowsAzureConstants.INVALID_ARG);
		}

		if (!isCachingEnable()) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Caching is not enabled");
		}

		if (getNamedCaches().keySet().contains(name)) {
			throw new WindowsAzureInvalidProjectOperationException(name
					+ " already exist");
		}
		//
		// if(!getWinProjMgr().isValidPort(String.valueOf(port),
		// WindowsAzureEndpointType.Internal)) {
		// throw new WindowsAzureInvalidProjectOperationException(port +
		// " already exist");
		// }

		WindowsAzureNamedCache cache = null;
		try {
			Document doc = getWinProjMgr().getConfigFileDoc();

			// Create JSON Object
			String expr = String.format(
					WindowsAzureConstants.CONFIG_SETTING_ROLE_VAL, getName(),
					WindowsAzureConstants.SET_NAMEDCACHE);
			String encodedCache = ParserXMLUtility
					.getExpressionValue(doc, expr);
			String newEncCompCache = JSONHelper.addCache(encodedCache, name, 0,
					10, 1);
			ParserXMLUtility.setExpressionValue(doc, expr, newEncCompCache);

			String newCache = JSONHelper.getCaches(newEncCompCache).get(name);
			// Add endpoint
			addEndpoint("memcache_" + name, WindowsAzureEndpointType.Internal,
					String.valueOf(port), "");

			cache = getWindowsCacheObjFromEle(newCache);
			cacheMap.put(name, cache);

		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					WindowsAzureConstants.EXCP, ex);
		}
		return cache;
	}

	/**
	 * Creates a WindowsAzureNamedCache object with given encoded string.
	 * 
	 * @param str
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	private WindowsAzureNamedCache getWindowsCacheObjFromEle(String str)
			throws WindowsAzureInvalidProjectOperationException {

		try {
			WindowsAzureNamedCache waCache = new WindowsAzureNamedCache(this,
					getWinProjMgr());
			waCache.setName(JSONHelper.getParamVal(str, "name"));

			String type = JSONHelper.getParamVal(str, "policy.expiration.type");
			waCache.setExpirationPolicy(WindowsAzureCacheExpirationPolicy
					.values()[Integer.parseInt(type)]);

			if (JSONHelper.getParamVal(str, "secondaries").equals("1")) {
				waCache.setBackups(true);
			} else {
				waCache.setBackups(false);
			}

			waCache.setMinutesToLive(Integer.parseInt(JSONHelper.getParamVal(
					str, "policy.expiration.defaultTTL")));
			return waCache;
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					WindowsAzureConstants.EXCP, ex);
		}
	}

	/**
	 * This API will return the collection of caches associated with this cache
	 * 
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
			String expr = String.format(
					WindowsAzureConstants.CONFIG_SETTING_ROLE_VAL, getName(),
					WindowsAzureConstants.SET_NAMEDCACHE);
			String encodedCache = ParserXMLUtility
					.getExpressionValue(doc, expr);
			if (!encodedCache.isEmpty()) {
				Map<String, String> encodedMap = JSONHelper
						.getCaches(encodedCache);
				Set<Entry<String, String>> set = encodedMap.entrySet();
				for (Entry<String, String> entry : set) {
					cacheMap.put(entry.getKey(),
							getWindowsCacheObjFromEle(entry.getValue()));
				}
			}
			return cacheMap;
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					WindowsAzureConstants.EXCP, ex);
		}
	}

	/**
	 * This API will return url attribute of server deploy component if server
	 * is not set will throw exception if the attribute is not set returns empty
	 * string
	 * 
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public String getServerCloudURL()
			throws WindowsAzureInvalidProjectOperationException {
		try {
			if (getServerSourcePath() == null) {
				throw new WindowsAzureInvalidProjectOperationException(
						"Server is not configured");
			} else {
				String url = null;
				XPath xPath = XPathFactory.newInstance().newXPath();
				Document doc = winProjMgr.getPackageFileDoc();
				String expr = String.format(WindowsAzureConstants.SERVER_TYPE,
						getName(), "server.deploy");
				Element node = (Element) xPath.evaluate(expr, doc,
						XPathConstants.NODE);
				if (node != null) {
					url = node.getAttribute(WindowsAzureConstants.ATTR_CURL);
				}
				return url;
			}
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException("", ex);
		}
	}

	/**
	 * This API will set url attribute of server deploy component if server is
	 * not set will throw exception if the url is null or empty, api will remove
	 * the attribute from xml
	 * 
	 * @param url
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public void setServerCloudURL(String url)
			throws WindowsAzureInvalidProjectOperationException {
		try {
			if (getServerSourcePath() == null) {
				throw new WindowsAzureInvalidProjectOperationException(
						"Server is not configured");
			} else {
				List<WindowsAzureRoleComponent> compList = getComponents();
				for (int i = 0; i < compList.size(); i++) {
					WindowsAzureRoleComponent comp = compList.get(i);
					if (comp.getType().equalsIgnoreCase("server.deploy")) {
						if (url == null || url.isEmpty()) {
							comp.setCloudDownloadURL("");
							comp.setCloudMethod(null);
						} else {
							comp.setCloudDownloadURL(url);
							comp.setCloudMethod(WindowsAzureRoleComponentCloudMethod.unzip);
						}
					}
				}
			}
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException("", ex);
		}
	}

	/**
	 * This API will return key attribute of server deploy component if server
	 * is not set will throw exception if the attribute is not set returns empty
	 * string
	 * 
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public String getServerCloudKey()
			throws WindowsAzureInvalidProjectOperationException {
		try {
			if (getServerSourcePath() == null) {
				throw new WindowsAzureInvalidProjectOperationException(
						"Server is not configured");
			} else {
				String key = null;
				XPath xPath = XPathFactory.newInstance().newXPath();
				Document doc = winProjMgr.getPackageFileDoc();
				String expr = String.format(WindowsAzureConstants.SERVER_TYPE,
						getName(), "server.deploy");
				Element node = (Element) xPath.evaluate(expr, doc,
						XPathConstants.NODE);
				if (node != null) {
					key = node.getAttribute(WindowsAzureConstants.ATTR_CKEY);
				}
				return key;
			}
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException("", ex);
		}
	}

	/**
	 * This API will set key attribute of server deploy component if server is
	 * not set will throw exception if the key is null or empty, api will remove
	 * the attribute from xml
	 * 
	 * @param url
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public void setServerCloudKey(String key)
			throws WindowsAzureInvalidProjectOperationException {
		try {
			if (getServerSourcePath() == null) {
				throw new WindowsAzureInvalidProjectOperationException(
						"Server is not configured");
			} else {
				List<WindowsAzureRoleComponent> compList = getComponents();
				for (int i = 0; i < compList.size(); i++) {
					WindowsAzureRoleComponent comp = compList.get(i);
					if (comp.getType().equalsIgnoreCase("server.deploy")) {
						if (key == null || key.isEmpty()) {
							comp.setCloudKey("");
						} else {
							comp.setCloudKey(key);
						}
					}
				}
			}
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException("", ex);
		}
	}

	/**
	 * This API returns cloudUpload attribute of server deploy component if
	 * server is not set will throw exception if the attribute is not set
	 * returns null
	 * 
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public WARoleComponentCloudUploadMode getServerCloudUploadMode()
			throws WindowsAzureInvalidProjectOperationException {
		try {
			if (getServerSourcePath() == null) {
				throw new WindowsAzureInvalidProjectOperationException(
						"Server is not configured");
			} else {
				XPath xPath = XPathFactory.newInstance().newXPath();
				Document doc = winProjMgr.getPackageFileDoc();
				String expr = String.format(WindowsAzureConstants.SERVER_TYPE,
						getName(), "server.deploy");
				Element node = (Element) xPath.evaluate(expr, doc,
						XPathConstants.NODE);
				if (node != null) {
					String attrValue = node
							.getAttribute(WindowsAzureConstants.ATTR_CLOUD_UPLOAD);
					if (attrValue != null && attrValue.length() > 0)
						return WARoleComponentCloudUploadMode
								.valueOf(node
										.getAttribute(
												WindowsAzureConstants.ATTR_CLOUD_UPLOAD)
										.toLowerCase());
				}
				return null;
			}
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException("", ex);
		}
	}

	/**
	 * This API will set cloudUpload attribute of server deploy component if
	 * server is not set will throw exception if cloudUpload is null or empty,
	 * api will remove the attribute from xml
	 * 
	 * @param cloudUpload
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public void setServerCloudUploadMode(
			WARoleComponentCloudUploadMode cloudUpload)
			throws WindowsAzureInvalidProjectOperationException {
		try {
			if (getServerSourcePath() == null) {
				throw new WindowsAzureInvalidProjectOperationException(
						"Server is not configured");
			} else {
				List<WindowsAzureRoleComponent> compList = getComponents();
				for (int i = 0; i < compList.size(); i++) {
					WindowsAzureRoleComponent comp = compList.get(i);
					if (comp.getType().equalsIgnoreCase("server.deploy")) {
						if (cloudUpload == null) {
							comp.setCloudUploadMode(null);
						} else {
							comp.setCloudUploadMode(cloudUpload);
						}
					}
				}
			}
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException("", ex);
		}
	}

	/**
	 * This API will return url attribute of jdk deploy component if jdk is not
	 * set will throw exception if the attribute is not set returns empty string
	 * 
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public String getJDKCloudURL()
			throws WindowsAzureInvalidProjectOperationException {
		try {
			if (getJDKSourcePath() == null) {
				throw new WindowsAzureInvalidProjectOperationException(
						"JDK is not configured");
			} else {
				String url = null;
				XPath xPath = XPathFactory.newInstance().newXPath();
				Document doc = winProjMgr.getPackageFileDoc();
				String expr = String.format(WindowsAzureConstants.SERVER_TYPE,
						getName(), "jdk.deploy");
				Element node = (Element) xPath.evaluate(expr, doc,
						XPathConstants.NODE);
				if (node != null) {
					url = node.getAttribute(WindowsAzureConstants.ATTR_CURL);
				}
				return url;
			}
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException("", ex);
		}
	}

	/**
	 * This API will set url attribute of jdk deploy component if jdk is not set
	 * will throw exception if the url is null or empty, api will remove the
	 * attribute from xml
	 * 
	 * @param url
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public void setJDKCloudURL(String url)
			throws WindowsAzureInvalidProjectOperationException {
		try {
			if (getJDKSourcePath() == null) {
				throw new WindowsAzureInvalidProjectOperationException(
						"JDK is not configured");
			} else {
				List<WindowsAzureRoleComponent> compList = getComponents();
				for (int i = 0; i < compList.size(); i++) {
					WindowsAzureRoleComponent comp = compList.get(i);
					if (comp.getType().equalsIgnoreCase("jdk.deploy")) {
						if (url == null || url.isEmpty()) {
							comp.setCloudDownloadURL("");
							comp.setCloudMethod(null);
						} else {
							comp.setCloudDownloadURL(url);
							comp.setCloudMethod(WindowsAzureRoleComponentCloudMethod.unzip);
						}
					}
				}
			}
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException("", ex);
		}
	}

	/**
	 * This API will return key attribute of jdk deploy component if jdk is not
	 * set will throw exception if the attribute is not set returns empty string
	 * 
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public String getJDKCloudKey()
			throws WindowsAzureInvalidProjectOperationException {
		try {
			if (getJDKSourcePath() == null) {
				throw new WindowsAzureInvalidProjectOperationException(
						"Server is not configured");
			} else {
				String key = null;
				XPath xPath = XPathFactory.newInstance().newXPath();
				Document doc = winProjMgr.getPackageFileDoc();
				String expr = String.format(WindowsAzureConstants.SERVER_TYPE,
						getName(), "jdk.deploy");
				Element node = (Element) xPath.evaluate(expr, doc,
						XPathConstants.NODE);
				if (node != null) {
					key = node.getAttribute(WindowsAzureConstants.ATTR_CKEY);
				}
				return key;
			}
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException("", ex);
		}
	}

	/**
	 * This API will set key attribute of jdk deploy component if jdk is not set
	 * will throw exception if the key is null or empty, api will remove the
	 * attribute from xml
	 * 
	 * @param url
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public void setJDKCloudKey(String key)
			throws WindowsAzureInvalidProjectOperationException {
		try {
			if (getJDKSourcePath() == null) {
				throw new WindowsAzureInvalidProjectOperationException(
						"Server is not configured");
			} else {
				List<WindowsAzureRoleComponent> compList = getComponents();
				for (int i = 0; i < compList.size(); i++) {
					WindowsAzureRoleComponent comp = compList.get(i);
					if (comp.getType().equalsIgnoreCase("jdk.deploy")) {
						if (key == null || key.isEmpty()) {
							comp.setCloudKey("");
						} else {
							comp.setCloudKey(key);
						}
					}
				}
			}
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException("", ex);
		}
	}

	/**
	 * This API will return cloudUpload attribute of jdk deploy component if jdk
	 * is not set will throw exception if the attribute is not set returns null
	 * 
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public WARoleComponentCloudUploadMode getJDKCloudUploadMode()
			throws WindowsAzureInvalidProjectOperationException {
		try {
			if (getJDKSourcePath() == null) {
				throw new WindowsAzureInvalidProjectOperationException(
						"JDK is not configured");
			} else {
				XPath xPath = XPathFactory.newInstance().newXPath();
				Document doc = winProjMgr.getPackageFileDoc();
				String expr = String.format(WindowsAzureConstants.SERVER_TYPE,
						getName(), "jdk.deploy");
				Element node = (Element) xPath.evaluate(expr, doc,
						XPathConstants.NODE);
				if (node != null) {
					String attrValue = node
							.getAttribute(WindowsAzureConstants.ATTR_CLOUD_UPLOAD);
					if (attrValue != null && attrValue.length() > 0)
						return WARoleComponentCloudUploadMode
								.valueOf(node
										.getAttribute(
												WindowsAzureConstants.ATTR_CLOUD_UPLOAD)
										.toLowerCase());
				}
				return null;
			}
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException("", ex);
		}
	}

	/**
	 * This API will set cloudUpload attribute of jdk deploy component if jdk is
	 * not set will throw exception if cloudUpload is null or empty, api will
	 * remove the attribute from xml
	 * 
	 * @param cloudUpload
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public void setJDKCloudUploadMode(WARoleComponentCloudUploadMode cloudUpload)
			throws WindowsAzureInvalidProjectOperationException {
		try {
			if (getJDKSourcePath() == null) {
				throw new WindowsAzureInvalidProjectOperationException(
						"JDK is not configured");
			} else {
				List<WindowsAzureRoleComponent> compList = getComponents();
				for (int i = 0; i < compList.size(); i++) {
					WindowsAzureRoleComponent comp = compList.get(i);
					if (comp.getType().equalsIgnoreCase("jdk.deploy")) {
						if (cloudUpload == null) {
							comp.setCloudUploadMode(null);
						} else {
							comp.setCloudUploadMode(cloudUpload);
						}
					}
				}
			}
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException("", ex);
		}
	}

	/**
	 * This API will set cloudaltsrc attribute of jdk deploy component if jdk is
	 * not set will throw exception if cloudaltsrc is null, api will remove the
	 * attribute from xml
	 * 
	 * @param url
	 */
	public void setJdkCldAltSrc(String url)
			throws WindowsAzureInvalidProjectOperationException {
		try {
			if (getJDKSourcePath() == null) {
				throw new WindowsAzureInvalidProjectOperationException(
						"JDK is not configured");
			} else {
				List<WindowsAzureRoleComponent> compList = getComponents();
				for (int i = 0; i < compList.size(); i++) {
					WindowsAzureRoleComponent comp = compList.get(i);
					if (comp.getType().equalsIgnoreCase("jdk.deploy")) {
						comp.setCloudAltSrc(url);
					}
				}
			}
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException("", ex);
		}
	}

	public void setServerCldAltSrc(String url)
			throws WindowsAzureInvalidProjectOperationException {
		try {
			if (getServerSourcePath() == null) {
				throw new WindowsAzureInvalidProjectOperationException(
						"Server is not configured");
			} else {
				List<WindowsAzureRoleComponent> compList = getComponents();
				for (int i = 0; i < compList.size(); i++) {
					WindowsAzureRoleComponent comp = compList.get(i);
					if (comp.getType().equalsIgnoreCase("server.deploy")) {
						comp.setCloudAltSrc(url);
					}
				}
			}
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException("", ex);
		}
	}

	/**
	 * This API will set cloud value attribute of jdk home if jdk is not set
	 * will throw exception if value is null, api will remove the attribute from
	 * xml
	 * 
	 * @param value
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public void setJdkCloudValue(String value)
			throws WindowsAzureInvalidProjectOperationException {
		try {
			if (getJDKSourcePath() == null) {
				throw new WindowsAzureInvalidProjectOperationException(
						"JDK is not configured");
			} else {
				XPath xPath = XPathFactory.newInstance().newXPath();
				Document doc = winProjMgr.getPackageFileDoc();
				String expr = String.format(
						WindowsAzureConstants.WA_PACK_SENV_NAME, getName(),
						WindowsAzureConstants.JAVA_HOME_ENV_VAR);
				Element strenv = (Element) xPath.evaluate(expr, doc,
						XPathConstants.NODE);
				if (value == null) {
					strenv.removeAttribute(WindowsAzureConstants.ATTR_CLD_VAL);
				} else {
					strenv.setAttribute(WindowsAzureConstants.ATTR_CLD_VAL,
							value);
				}
			}
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException("", ex);
		}
	}

	/**
	 * This API will set cloud value attribute of server home if server is not set
	 * will throw exception if value is null, api will remove the attribute from xml
	 * @param value
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public void setServerCloudValue(String value)
			throws WindowsAzureInvalidProjectOperationException {
		try {
			if (getServerName() == null) {
				throw new WindowsAzureInvalidProjectOperationException(
						"Server is not configured");
			} else {
				XPath xPath = XPathFactory.newInstance().newXPath();
				Document doc = winProjMgr.getPackageFileDoc();
				String expr = String.format(
						WindowsAzureConstants.WA_PACK_SENV_TYPE, getName(),
						"server.home");
				Element strenv = (Element) xPath.evaluate(expr, doc,
						XPathConstants.NODE);
				if (value == null) {
					strenv.removeAttribute(WindowsAzureConstants.ATTR_CLD_VAL);
				} else {
					strenv.setAttribute(WindowsAzureConstants.ATTR_CLD_VAL, value);
				}
			}
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException("", ex);
		}
	}

	public Map<String, WindowsAzureCertificate> getCertificates()
			throws WindowsAzureInvalidProjectOperationException {
		try {
			if (certMap.isEmpty()) {
				XPath xPath = XPathFactory.newInstance().newXPath();
				Document doc = getWinProjMgr().getConfigFileDoc();
				String expr = String.format(WindowsAzureConstants.CERT_LIST,
						getName());
				NodeList certList = (NodeList) xPath.evaluate(expr, doc,
						XPathConstants.NODESET);
				for (int i = 0; i < certList.getLength(); i++) {
					Element ele = (Element) certList.item(i);
					String name = ele
							.getAttribute(WindowsAzureConstants.ATTR_NAME);
					String thumb = ele
							.getAttribute(WindowsAzureConstants.ATTR_THUMB);
					WindowsAzureCertificate certObj = new WindowsAzureCertificate(
							winProjMgr, this, name, thumb);
					certMap.put(name, certObj);
				}
			}
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception while getting certificates", ex);
		}
		return certMap;
	}

	public WindowsAzureCertificate addCertificate(String name,
			String fingerprint)
			throws WindowsAzureInvalidProjectOperationException {
		if (name == null || name.isEmpty() || fingerprint == null
				|| fingerprint.isEmpty()) {
			throw new IllegalArgumentException();
		}

		WindowsAzureCertificate certToInsert = new WindowsAzureCertificate(
				getWinProjMgr(), this, name, fingerprint);

		for (Iterator<Entry<String, WindowsAzureCertificate>> iterator = getCertificates()
				.entrySet().iterator(); iterator.hasNext();) {
			WindowsAzureCertificate cert = iterator.next().getValue();
			if (cert.getName().equalsIgnoreCase(name)
					|| cert.getFingerPrint().equalsIgnoreCase(fingerprint)) {
				throw new WindowsAzureInvalidProjectOperationException(
						"This certificate with same name or fingerprint already exists");
			}
		}

		try {
			// add entry in cscfg
			Document doc = getWinProjMgr().getConfigFileDoc();
			XPath xPath = XPathFactory.newInstance().newXPath();

			String expr1 = String.format(WindowsAzureConstants.ROLE_NAME,
					getName());
			Node role;
			role = (Node) xPath.evaluate(expr1, doc, XPathConstants.NODE);
			boolean flag = false;
			for (Node child = role.getFirstChild(); child != null; child = child
					.getNextSibling()) {
				if (child.getNodeName().equalsIgnoreCase("Certificates")) {
					flag = true;
				}
			}
			// if Certificates tag doesn't exist, create new tag
			if (!flag) {
				Element eleCerts = doc.createElement("Certificates");
				role.appendChild(eleCerts);
			}
			// traverse to Certificates
			String expr = String.format(WindowsAzureConstants.CERT_ROLE,
					getName());
			Node certs;
			certs = (Node) xPath.evaluate(expr, doc, XPathConstants.NODE);
			// create Certificate tag
			Element cert = doc.createElement("Certificate");
			cert.setAttribute(WindowsAzureConstants.ATTR_NAME, name);
			cert.setAttribute(WindowsAzureConstants.ATTR_THUMB, fingerprint);
			cert.setAttribute("thumbprintAlgorithm", "sha1");
			// append Certificate tag
			certs.appendChild(cert);

			// add entry in csdef
			Document defDoc = getWinProjMgr().getdefinitionFileDoc();
			String expr_workRole = String.format(WindowsAzureConstants.WR_NAME,
					getName());
			Node workRole;
			workRole = (Node) xPath.evaluate(expr_workRole, defDoc,
					XPathConstants.NODE);
			boolean flag1 = false;
			for (Node child = workRole.getFirstChild(); child != null; child = child
					.getNextSibling()) {
				if (child.getNodeName().equalsIgnoreCase("Certificates")) {
					flag1 = true;
				}
			}
			// if endpoint tag doesn't exist, create new endpoint tag
			if (!flag1) {
				Element eleCerts = defDoc.createElement("Certificates");
				workRole.appendChild(eleCerts);
			}
			String exprCerts = String.format(WindowsAzureConstants.WR_CERTS,
					getName());
			Node work_certs;
			work_certs = (Node) xPath.evaluate(exprCerts, defDoc,
					XPathConstants.NODE);
			// create Certificate tag
			Element work_cert = defDoc.createElement("Certificate");
			work_cert.setAttribute(WindowsAzureConstants.ATTR_NAME, name);
			work_cert.setAttribute("storeLocation", "LocalMachine");
			work_cert.setAttribute("storeName", "My");
			// append Certificate tag
			work_certs.appendChild(work_cert);
			getCertificates();
			certMap.put(name, certToInsert);
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					WindowsAzureConstants.EXCP_ADD_ENDPOINT, ex);
		}
		return certToInsert;
	}

	public WindowsAzureCertificate getCertificate(String certName)
			throws WindowsAzureInvalidProjectOperationException {
		WindowsAzureCertificate cert = null;
		Map<String, WindowsAzureCertificate> certList = this.getCertificates();
		if (certList != null) {
			for (Iterator<Entry<String, WindowsAzureCertificate>> iterator = certList
					.entrySet().iterator(); iterator.hasNext();) {
				WindowsAzureCertificate certTemp = iterator.next().getValue();
				if (certTemp.getName().equalsIgnoreCase(certName)) {
					cert = certTemp;
				}
			}
		}
		return cert;
	}
}
