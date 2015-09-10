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

public class WindowsAzureCertificate {

	private String name;
	private String fingerPrint;
	private WindowsAzureProjectManager wProj = null;
	private WindowsAzureRole wRole = null;

	public WindowsAzureCertificate() {
		super();
	}

	public WindowsAzureCertificate(String name, String fingerPrint) {
		super();
		this.name = name;
		this.fingerPrint = fingerPrint;
	}

	protected WindowsAzureCertificate(WindowsAzureProjectManager waProj,
			WindowsAzureRole waRole, String name, String fingerPrint) {
		super();
		this.wProj = waProj;
		this.wRole = waRole;
		this.name = name;
		this.fingerPrint = fingerPrint;
	}

	private Element getCscfgCertNode()
			throws WindowsAzureInvalidProjectOperationException {
		try {
			XPath xPath = XPathFactory.newInstance().newXPath();
			Document doc = wProj.getConfigFileDoc();
			String expr = String.format(WindowsAzureConstants.CERT_ROLE_NAME,
					wRole.getName(), this.getName());
			Element certNd = (Element) xPath.evaluate(expr, doc,
					XPathConstants.NODE);
			if (certNd == null) {
				throw new WindowsAzureInvalidProjectOperationException(
						"Exception while geting Certificate node");
			}
			return certNd;
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception while geting Certificate node", ex);
		}
	}

	private Element getCsdefCertNode()
			throws WindowsAzureInvalidProjectOperationException {
		try {
			XPath xPath = XPathFactory.newInstance().newXPath();
			Document doc = wProj.getdefinitionFileDoc();
			String expr = String.format(WindowsAzureConstants.WR_CERT,
					wRole.getName(), this.getName());
			Element ele = (Element) xPath.evaluate(expr, doc,
					XPathConstants.NODE);
			if (ele == null) {
				throw new WindowsAzureInvalidProjectOperationException(
						"Exception while geting Certificate node");
			}
			return ele;
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception while geting Certificate node", ex);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String certName)
			throws WindowsAzureInvalidProjectOperationException {
		if (certName == null || certName.isEmpty()) {
			throw new IllegalArgumentException(
					WindowsAzureConstants.INVALID_ARG);
		}
		if (isRemoteAccess()) {
			throw new WindowsAzureInvalidProjectOperationException(
					"This certificate is assoiciated with remote access");
		}
		try {
			if (this.name.isEmpty()) {
				this.name = certName;
			}
			// change in cscfg
			Element certEle = getCscfgCertNode();
			certEle.setAttribute(WindowsAzureConstants.ATTR_NAME, certName);

			// change in csdef
			Element ele = getCsdefCertNode();
			ele.setAttribute(WindowsAzureConstants.ATTR_NAME, certName);

			WindowsAzureCertificate obj = wRole.certMap.get(this.name);
			wRole.certMap.remove(this.name);
			wRole.certMap.put(certName, obj);
			this.name = certName;
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception in setName", ex);
		}
	}

	public String getFingerPrint() {
		return fingerPrint;
	}

	public void setFingerPrint(String fingerPrint)
			throws WindowsAzureInvalidProjectOperationException {
		try {
			Element ele = getCscfgCertNode();
			ele.setAttribute(WindowsAzureConstants.ATTR_THUMB, fingerPrint);
			this.fingerPrint = fingerPrint;
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception in setFingerPrint", ex);
		}
	}

	public boolean isRemoteAccess()
			throws WindowsAzureInvalidProjectOperationException {
		boolean isRemoteAssociated = false;
		try {
			if (wProj.getRemoteAccessAllRoles()
					&& getName().equalsIgnoreCase(
							WindowsAzureConstants.REMOTEACCESS_FINGERPRINT)
					&& getFingerPrint().equalsIgnoreCase(wRole.getThumbprint())) {
				isRemoteAssociated = true;
			}
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					WindowsAzureConstants.EXCP, ex);
		}
		return isRemoteAssociated;
	}

	public boolean isSSLCert()
			throws WindowsAzureInvalidProjectOperationException {
		boolean isSslCert = false;
		WindowsAzureCertificate sslCert = wRole.getSslOffloadingCert();
		if (sslCert != null && sslCert.getName().equalsIgnoreCase(getName())
				&& sslCert.getFingerPrint().equalsIgnoreCase(getFingerPrint())) {
			isSslCert = true;
		}
		return isSslCert;
	}

	public void delete() throws WindowsAzureInvalidProjectOperationException {
		if (isRemoteAccess()) {
			throw new WindowsAzureInvalidProjectOperationException(
					"This certificate is assoiciated with remote access");
		}
		// remove from map
		wRole.certMap.remove(getName());
		// Remove from cscfg
		Element certEle = getCscfgCertNode();
		Node parentNode = certEle.getParentNode();
		parentNode.removeChild(certEle);
		removeParentNodeIfNeeded(parentNode);
		// Remove from csdef
		Element certCsdefEle = getCsdefCertNode();
		Node parentCsdefNode = certCsdefEle.getParentNode();
		parentCsdefNode.removeChild(certCsdefEle);
		removeParentNodeIfNeeded(parentCsdefNode);
	}

	private void removeParentNodeIfNeeded(Node parentNode) {
		Boolean hasCertChild = false;
		for (Node child = parentNode.getFirstChild(); child != null; child = child
				.getNextSibling()) {
			if (child.getNodeName().equalsIgnoreCase("Certificate")) {
				hasCertChild = true;
				break;
			}
		}
		/*
		 * if it was last certificate which got removed then remove Certificates
		 * tag as well.
		 */
		if (!hasCertChild) {
			parentNode.getParentNode().removeChild(parentNode);
		}
	}
}
