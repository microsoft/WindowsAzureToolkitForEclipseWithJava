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

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Class representing Azure endpoint.
 */
public class WindowsAzureEndpoint {
	private WindowsAzureProjectManager winProjMgr;
	private WindowsAzureRole wRole;
	private String name = "";
	private String port = "";
	private String localPort = "";
	private String minPort = "";
	private String maxPort = "";
	private String protocol = "";
	private String certificate = "";

	public WindowsAzureEndpoint(WindowsAzureProjectManager winProMgr,
			WindowsAzureRole winRole) {
		winProjMgr = winProMgr;
		wRole = winRole;
	}

	/**
	 * Get instance of ProjectManager Class.
	 * 
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
	 * 
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	private Node getThisEndPointNode()
			throws WindowsAzureInvalidProjectOperationException {
		try {
			Node epNode = null;
			if (!getName().isEmpty()) {
				XPath xPath = XPathFactory.newInstance().newXPath();
				Document doc = getWindowsAzureProjMgr().getdefinitionFileDoc();
				String expr = "";

				if (getEndPointType().equals(WindowsAzureEndpointType.Input)) {
					expr = String.format(WindowsAzureConstants.INPUT_ENDPOINT,
							wRole.getName(), getName());
				} else if (getEndPointType().equals(
						WindowsAzureEndpointType.Internal)) {
					expr = String.format(
							WindowsAzureConstants.INTERNAL_ENDPOINT,
							wRole.getName(), getName());
				} else {
					expr = String.format(
							WindowsAzureConstants.INSTANCE_ENDPOINT,
							wRole.getName(), getName());
				}
				epNode = (Node) xPath.evaluate(expr, doc, XPathConstants.NODE);
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

			// if(isCachingEndPoint()) {
			// throw new WindowsAzureInvalidProjectOperationException(
			// "Endpoint is associated with caching");
			// }

			WindowsAzureEndpoint saEndPt = wRole
					.getSessionAffinityInputEndpoint();
			if (saEndPt != null
					&& getName().equalsIgnoreCase(saEndPt.getName()))
				wRole.reconfigureSessionAffinity(wRole.getEndpoint(getName()),
						endPointName);
			Node epNode = getThisEndPointNode();
			epNode.getAttributes().getNamedItem("name")
					.setNodeValue(endPointName);
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
	 * Gets the protocol.
	 * 
	 * @return
	 */

	public String getProtocol() {
		return protocol;
	}

	/**
	 * Gets the certificate.
	 * 
	 * @return
	 */

	public String getCertificate() {
		return certificate;
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
			} else if (getEndPointType().equals(
					WindowsAzureEndpointType.InstanceInput)) {
				this.minPort = endPointPort;
				this.maxPort = endPointPort;
				if (endPointPort.contains("-")) {
					String[] ports = endPointPort.split("-");
					this.minPort = ports[0];
					this.maxPort = ports[1];
				}
				epProt = String.format("%s-%s", minPort, maxPort);
				String expr = "./AllocatePublicPortFrom/FixedPortRange";
				XPath xPath = XPathFactory.newInstance().newXPath();
				Element eleFxdPortRan = (Element) xPath.evaluate(expr,
						getThisEndPointNode(), XPathConstants.NODE);
				eleFxdPortRan.setAttribute(WindowsAzureConstants.ATTR_MINPORT,
						this.minPort);
				eleFxdPortRan.setAttribute(WindowsAzureConstants.ATTR_MAXPORT,
						this.maxPort);
				this.port = epProt;
			}
		} catch (Exception e) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception in setPort ", e);
		}

	}

	/**
	 * Sets the EndpointName protocol.
	 * 
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public void setProtocol(String protocol)
			throws WindowsAzureInvalidProjectOperationException {
		if ((protocol == null) || (protocol.isEmpty())) {
			throw new IllegalArgumentException(
					WindowsAzureConstants.EXCP_EMPTY_PROTOCOL);
		}

		try {
			Node epNode = getThisEndPointNode();
			epNode.getAttributes().getNamedItem("protocol")
					.setNodeValue(protocol);
			this.protocol = protocol;

		} catch (Exception e) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception in setProtocol ", e);
		}
	}

	/**
	 * Sets the Endpoint certificate.
	 * 
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public void setCertificate(String certificate)
			throws WindowsAzureInvalidProjectOperationException {

		if ((certificate == null) || (certificate.isEmpty())) {
			throw new IllegalArgumentException(
					WindowsAzureConstants.EXCP_EMPTY_CERT);
		}

		try {
			Node epNode = getThisEndPointNode();
			Node certAttr = epNode.getAttributes().getNamedItem("certificate");
			if (certAttr != null)
				certAttr.setNodeValue(certificate);
			else
				((Element) epNode).setAttribute("certificate", certificate);

			this.certificate = certificate;
		} catch (Exception e) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception in setCertificate ", e);
		}
	}

	/**
	 * Removes Endpoint attribute.
	 * 
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public void removeAttribute(String attributeName)
			throws WindowsAzureInvalidProjectOperationException {
		try {
			Node epNode = getThisEndPointNode();
			Node attrNode = epNode.getAttributes().getNamedItem(attributeName);

			if (attrNode != null) {
				((Element) epNode).removeAttribute(attributeName);
			}
		} catch (Exception e) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception while removing attribute ", e);
		}
	}

	/**
	 * Sets the Input or Instance Local Port.
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
		String port = localPort;
		if (port != null && port.equals("*")) {
			port = null;
		}
		return port;
	}

	protected String getPrivatePortWrapper() {
		String port = getPrivatePort();
		if (port == null) {
			port = "*";
		}
		return port;
	}

	/**
	 * Sets the private EndpointName port.
	 * 
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public void setPrivatePort(String endPointPort)
			throws WindowsAzureInvalidProjectOperationException {
		if ((getEndPointType().equals(WindowsAzureEndpointType.InstanceInput))
				&& ((endPointPort == null) || (endPointPort.isEmpty()))) {
			throw new IllegalArgumentException(
					WindowsAzureConstants.EXCP_EMPTY_PRIVATE_PORT);
		}
		try {
			Element epNode = (Element) getThisEndPointNode();
			if (getEndPointType().equals(WindowsAzureEndpointType.Input)
					|| (getEndPointType()
							.equals(WindowsAzureEndpointType.InstanceInput))) {
				if (endPointPort == null) {
					endPointPort = "*";
				}
				setLocalPort(endPointPort);
			} else {
				// internal endpoint
				boolean hasFIxedChild = false;
				for (Node child = epNode.getFirstChild(); child != null; child = child
						.getNextSibling()) {
					if (child.getNodeName().equalsIgnoreCase("FixedPort")) {
						hasFIxedChild = true;
						Element eleChild = (Element) child;
						if (endPointPort == null) {
							// Remove fixed port
							eleChild.getParentNode().removeChild(eleChild);
						} else if (endPointPort.contains("-")) {
							// create internal input range
							Element eleFxdPortRan = winProjMgr
									.getdefinitionFileDoc().createElement(
											"FixedPortRange");
							String[] ports = endPointPort.split("-");
							String minPort = ports[0];
							String maxPort = ports[1];
							eleFxdPortRan
									.setAttribute(
											WindowsAzureConstants.ATTR_MINPORT,
											minPort);
							eleFxdPortRan
									.setAttribute(
											WindowsAzureConstants.ATTR_MAXPORT,
											maxPort);
							eleChild.getParentNode().appendChild(eleFxdPortRan);

							// Remove fixed port
							eleChild.getParentNode().removeChild(eleChild);
						} else { // Just update port
							eleChild.setAttribute("port", endPointPort);
						}
					} else if (child.getNodeName().equalsIgnoreCase(
							"FixedPortRange")) {
						hasFIxedChild = true;
						Element eleChild = (Element) child;
						if (endPointPort == null) {
							// Removing internal port range
							eleChild.getParentNode().removeChild(eleChild);
						} else if (endPointPort.contains("-")) {
							// updates port range values
							String[] ports = endPointPort.split("-");
							String minPort = ports[0];
							String maxPort = ports[1];
							eleChild.setAttribute(
									WindowsAzureConstants.ATTR_MINPORT, minPort);
							eleChild.setAttribute(
									WindowsAzureConstants.ATTR_MAXPORT, maxPort);
						} else {
							// create internal fixed port
							Element eleFxdPort = winProjMgr
									.getdefinitionFileDoc().createElement(
											"FixedPort");
							eleFxdPort.setAttribute("port", endPointPort);
							eleChild.getParentNode().appendChild(eleFxdPort);

							// Removing internal port range
							eleChild.getParentNode().removeChild(eleChild);
						}
					}
				}
				/*
				 * no child node i.e. (auto) was selected previously and if
				 * again auto is coming then dont do any thing.
				 */
				if (!hasFIxedChild && endPointPort != null) {
					if (endPointPort.contains("-")) {
						// create internal input range
						Element eleFxdPortRan = winProjMgr
								.getdefinitionFileDoc().createElement(
										"FixedPortRange");
						String[] ports = endPointPort.split("-");
						String minPort = ports[0];
						String maxPort = ports[1];
						eleFxdPortRan.setAttribute(
								WindowsAzureConstants.ATTR_MINPORT, minPort);
						eleFxdPortRan.setAttribute(
								WindowsAzureConstants.ATTR_MAXPORT, maxPort);
						epNode.appendChild(eleFxdPortRan);
					} else {
						// create internal fixed port
						Element eleFxdPort = winProjMgr.getdefinitionFileDoc()
								.createElement("FixedPort");
						eleFxdPort.setAttribute("port", endPointPort);
						epNode.appendChild(eleFxdPort);
					}
				}
			}
			this.localPort = endPointPort;
		} catch (Exception e) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception occurred", e);
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
			String expr = String.format(WindowsAzureConstants.ENDPOINT
					+ "/*[@name='" + getName() + "']", wRole.getName());
			Node epNode = (Node) xPath.evaluate(expr, doc, XPathConstants.NODE);
			if (epNode.getNodeName().equalsIgnoreCase("InternalEndpoint")) {
				type = WindowsAzureEndpointType.Internal;
			} else if (epNode.getNodeName().equalsIgnoreCase("InputEndpoint")) {
				type = WindowsAzureEndpointType.Input;
			} else {
				type = WindowsAzureEndpointType.InstanceInput;
			}
		} catch (Exception e) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception occurred", e);
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

				if (getEndPointType().equals(
						WindowsAzureEndpointType.InstanceInput)) {
					XPath xPath = XPathFactory.newInstance().newXPath();
					String VarExpr = String.format(
							WindowsAzureConstants.VAR_WITH_SPECIFIC_NAME,
							wRole.getName(), getName() + "_PUBLICPORT");
					Element eleVar = (Element) xPath.evaluate(VarExpr, doc,
							XPathConstants.NODE);
					if (eleVar != null) {
						eleVar.getParentNode().removeChild(eleVar);
					}
				}

				if (type == WindowsAzureEndpointType.Input) {
					// check if endpoint is associated with session affinity, if
					// yes then throw error
					WindowsAzureEndpoint saEndPt = wRole
							.getSessionAffinityInternalEndpoint();
					if (saEndPt != null
							&& getName().equalsIgnoreCase(saEndPt.getName()))
						throw new WindowsAzureInvalidProjectOperationException(
								WindowsAzureConstants.EXCP_SA_ENDPOINT_TYPE_CHANGE);
					String newPubPort = "";
					if (getEndPointType() == WindowsAzureEndpointType.Internal) {
						// while changing internal to input, private and public
						// port
						// are similar
						newPubPort = getPrivatePortWrapper();
						if (newPubPort.contains("-")) {
							newPubPort = (newPubPort.split("-"))[0];
						}
					} else {
						// old type is instance
						newPubPort = getPort();
						if (newPubPort.contains("-")) {
							newPubPort = (newPubPort.split("-"))[0];
						}
					}
					// CreateInputNode
					Element eleInputNode = doc.createElement("InputEndpoint");
					eleInputNode.setAttribute("name", getName());
					eleInputNode.setAttribute("port", newPubPort);
					eleInputNode.setAttribute("localPort",
							getPrivatePortWrapper());
					eleInputNode.setAttribute("protocol", "tcp");
					Node internalNode = getThisEndPointNode();
					internalNode.getParentNode().appendChild(eleInputNode);
					// remove internal node
					internalNode.getParentNode().removeChild(internalNode);
					// set the port to update the corresponding cache of port
					setPort(newPubPort);
				}
				if (type == WindowsAzureEndpointType.Internal) {
					// check if endpoint is associated with session affinity ,
					// if yes then throw error
					WindowsAzureEndpoint saEndPt = wRole
							.getSessionAffinityInputEndpoint();
					if (saEndPt != null
							&& getName().equalsIgnoreCase(saEndPt.getName()))
						throw new WindowsAzureInvalidProjectOperationException(
								WindowsAzureConstants.EXCP_SA_ENDPOINT_TYPE_CHANGE);
					// change in cache
					Element eleInternalNode = doc
							.createElement("InternalEndpoint");
					eleInternalNode.setAttribute("name", getName());
					eleInternalNode.setAttribute("protocol", "tcp");
					String priPort = getPrivatePortWrapper();
					if (priPort != null && !priPort.isEmpty()
							&& !priPort.equals("*")) {
						Element eleFixedPort = doc.createElement("FixedPort");
						eleFixedPort.setAttribute("port",
								getPrivatePortWrapper());
						eleInternalNode.appendChild(eleFixedPort);
					}
					Node currNode = getThisEndPointNode();
					currNode.getParentNode().appendChild(eleInternalNode);
					currNode.getParentNode().removeChild(currNode);
					/*
					 * When type is changed to Internal, set public port to
					 * empty.
					 */
					this.port = "";
				}
				if (type == WindowsAzureEndpointType.InstanceInput) {
					WindowsAzureEndpoint saEndPt = wRole
							.getSessionAffinityInternalEndpoint();
					if (saEndPt != null
							&& getName().equalsIgnoreCase(saEndPt.getName()))
						throw new WindowsAzureInvalidProjectOperationException(
								WindowsAzureConstants.EXCP_SA_ENDPOINT_TYPE_CHANGE);

					String newPubPort = "";
					if (getEndPointType() == WindowsAzureEndpointType.Internal) {
						// while changing internal to instance, private and
						// public port
						// are similar
						newPubPort = getPrivatePortWrapper();
						if (newPubPort.contains("-")) {
							newPubPort = (newPubPort.split("-"))[0];
						}
					} else {
						// old type is input
						newPubPort = getPort();
					}
					if (!newPubPort.contains("-")) {
						newPubPort = String.format("%s-%s", newPubPort,
								newPubPort);
					}

					// CreateInputNode
					Element eleInstanceNode = doc
							.createElement("InstanceInputEndpoint");
					eleInstanceNode.setAttribute(
							WindowsAzureConstants.ATTR_NAME, getName());
					eleInstanceNode.setAttribute("protocol", "tcp");
					eleInstanceNode.setAttribute("localPort",
							getPrivatePortWrapper());

					Element eleAllPubPort = doc
							.createElement("AllocatePublicPortFrom");
					Element eleFxdPortRan = doc.createElement("FixedPortRange");
					eleFxdPortRan.setAttribute(
							WindowsAzureConstants.ATTR_MINPORT, minPort);
					eleFxdPortRan.setAttribute(
							WindowsAzureConstants.ATTR_MAXPORT, maxPort);

					eleAllPubPort.appendChild(eleFxdPortRan);
					eleInstanceNode.appendChild(eleAllPubPort);

					wRole.setVarInDefFile(getName() + "_PUBLICPORT");
					XPath xPath = XPathFactory.newInstance().newXPath();
					String VarExpr = String.format(
							WindowsAzureConstants.VAR_WITH_SPECIFIC_NAME,
							wRole.getName(), getName() + "_PUBLICPORT");
					Element eleVar = (Element) xPath.evaluate(VarExpr, doc,
							XPathConstants.NODE);
					Element insval = doc.createElement("RoleInstanceValue");
					String val = String.format(
							WindowsAzureConstants.EP_INSTANCE_VAR, getName());
					insval.setAttribute("xpath", val);
					eleVar.appendChild(insval);

					Node currNode = getThisEndPointNode();
					currNode.getParentNode().appendChild(eleInstanceNode);
					// remove internal node
					currNode.getParentNode().removeChild(currNode);
					// Update cache
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

			if (getEndPointType()
					.equals(WindowsAzureEndpointType.InstanceInput)) {
				XPath xPath = XPathFactory.newInstance().newXPath();
				Document doc = getWindowsAzureProjMgr().getdefinitionFileDoc();
				String VarExpr = String.format(
						WindowsAzureConstants.VAR_WITH_SPECIFIC_NAME,
						wRole.getName(), getName() + "_PUBLICPORT");
				Element eleVar = (Element) xPath.evaluate(VarExpr, doc,
						XPathConstants.NODE);
				if (eleVar != null) {
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
	 * This API will return true if this endpoint is associated with azure
	 * cache.
	 * 
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public boolean isCachingEndPoint()
			throws WindowsAzureInvalidProjectOperationException {
		boolean isCachingAssociated = false;
		try {

			if (getName().startsWith("memcache_")
					&& wRole.getNamedCaches()
							.keySet()
							.contains(
									getName().subSequence("memcache_".length(),
											getName().length()))) {
				isCachingAssociated = true;
			}
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					WindowsAzureConstants.EXCP);
		}

		return isCachingAssociated;
	}

	public boolean isStickySessionEndpoint()
			throws WindowsAzureInvalidProjectOperationException {
		boolean isStickyAssociated = false;
		try {
			WindowsAzureEndpoint stickyEndpt = wRole
					.getSessionAffinityInputEndpoint();
			WindowsAzureEndpoint stickyIntEndpt = wRole
					.getSessionAffinityInternalEndpoint();
			String stcEndptName = "";
			String stcIntEndptName = "";
			if (stickyEndpt != null) {
				stcEndptName = stickyEndpt.getName();
				stcIntEndptName = stickyIntEndpt.getName();
			}
			if (!stcEndptName.isEmpty()
					&& !stcIntEndptName.isEmpty()
					&& (getName().equalsIgnoreCase(stcEndptName) || getName()
							.equalsIgnoreCase(stcIntEndptName))) {
				isStickyAssociated = true;
			}
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					WindowsAzureConstants.EXCP);
		}
		return isStickyAssociated;
	}

	public boolean isSSLEndpoint()
			throws WindowsAzureInvalidProjectOperationException {
		boolean isSSLAssociated = false;
		try {
			WindowsAzureEndpoint sslEndpt = wRole
					.getSslOffloadingInputEndpoint();
			WindowsAzureEndpoint sslIntEndpt = wRole
					.getSslOffloadingInternalEndpoint();
			String sslEndptName = "";
			String sslIntEndptName = "";
			if (sslEndpt != null) {
				sslEndptName = sslEndpt.getName();
				sslIntEndptName = sslIntEndpt.getName();
			}
			if (!sslEndptName.isEmpty()
					&& !sslIntEndptName.isEmpty()
					&& (getName().equalsIgnoreCase(sslEndptName) || getName()
							.equalsIgnoreCase(sslIntEndptName))) {
				isSSLAssociated = true;
			}
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					WindowsAzureConstants.EXCP);
		}
		return isSSLAssociated;
	}

	public boolean isSSLRedirectEndPoint()
			throws WindowsAzureInvalidProjectOperationException {
		boolean isSSLRedirectEP = false;
		try {
			WindowsAzureEndpoint sslRedirectEndpt = wRole
					.getSslOffloadingRedirectionEndpoint();
			if (sslRedirectEndpt != null
					&& (getName().equalsIgnoreCase(sslRedirectEndpt.getName()))) {
				isSSLRedirectEP = true;
			}
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					WindowsAzureConstants.EXCP);
		}
		return isSSLRedirectEP;
	}
}
