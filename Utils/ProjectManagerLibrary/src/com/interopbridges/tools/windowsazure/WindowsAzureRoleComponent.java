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

public class WindowsAzureRoleComponent {

	/**
	 * wProj Variable is to represent corresponding object of
	 * WindowsAzureProjectManager.
	 */
	private WindowsAzureProjectManager wProj = null;

	/**
	 * wRole Variable is to represent corresponding object of WindowsAzureRole.
	 */
	private WindowsAzureRole wRole = null;

	/**
	 * importMethod variable represents importMethod attribute in Component tag
	 * in package.xml.
	 */
	private WindowsAzureRoleComponentImportMethod importMethod = null;
	private String cloudAltSrc = null;
	/**
	 * cloudUploadMode variable represents cloudUpload attribute in Component
	 * tag in package.xml.
	 */
	private WARoleComponentCloudUploadMode cloudUploadMode = null;
	/**
	 * importPath variable represents importPath attribute in Component tag in
	 * package.xml.
	 */
	private String importPath = "";

	/**
	 * deployMethod variable represents deployMethod attribute in Component tag
	 * in package.xml.
	 */
	private WindowsAzureRoleComponentDeployMethod deployMethod = null;

	/**
	 * deployDir variable represents deployDir attribute in Component tag in
	 * package.xml.
	 */
	private String deployDir = "";

	/**
	 * deployName variable represents importas attribute in Component tag in
	 * package.xml.
	 */
	private String deployName = "";

	/**
	 * type variable is to determine if a component is part of a server, JDK or
	 * application configuration, based on its @type.
	 */
	private String type = "";

	/**
	 * cloudkey variable represents cloudkey attribute in Component tag in
	 * package.xml.
	 */
	private String cloudkey = null;

	/**
	 * cloudurl variable represents cloudurl attribute in Component tag in
	 * package.xml
	 */
	private String cloudurl = null;

	/**
	 * cloudmethod variable represents cloudmethod attribute in Component tag in
	 * package.xml
	 */
	private WindowsAzureRoleComponentCloudMethod cloudmethod = WindowsAzureRoleComponentCloudMethod.none;

	/**
	 * Constructor to initialize projectManager and Role instances.
	 * 
	 * @param waProj
	 * @param waRole
	 */
	public WindowsAzureRoleComponent(WindowsAzureProjectManager waProj,
			WindowsAzureRole waRole) {
		wProj = waProj;
		wRole = waRole;
	}

	/**
	 * Constructor to initialize project manager, Role object and type of component.
	 * @param waProj
	 * @param waRole
	 * @param type
	 */
	public WindowsAzureRoleComponent(WindowsAzureProjectManager waProj,
			WindowsAzureRole waRole, String type) {
		wProj = waProj;
		wRole = waRole;
		this.type = type;
	}

	/**
	 * This method is to find corresponding Component tag in package.xml.
	 * 
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
				// In case deployment name is empty, there can be two componetns
				// having same IPATH
				// In case deployname is specified and in other where deployname
				// is empty.
				// This component can be placed in 2 ways...
				// Either importas attribute is not present or value importas
				// attribute is empty

				// Case 1: importas attribute is not present
				/*
				 * To take care of scenario when both,
				 * JDK and server does not have local path specified,
				 * rely on component type to identify element.
				 */
				if (this.importPath.isEmpty()) {
					expr = String.format(WindowsAzureConstants.COMPONENT_TYPE_PATH,
							wRole.getName(), this.type, this.importPath);
				} else {
					expr = String.format(
							WindowsAzureConstants.COMPONENT_IPATH_NAME,
							wRole.getName(), this.importPath);
				}
				component = (Element) xPath.evaluate(expr, doc,
						XPathConstants.NODE);

				if (component == null) {
					// case 2: Value importas attribute is empty
					expr = String
							.format(WindowsAzureConstants.COMPONENT_IPATH_NAME_IAS_EMPTY,
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
	 * 
	 * @return
	 */
	public WindowsAzureRoleComponentImportMethod getImportMethod() {
		return importMethod;
	}

	/**
	 * This API sets the importMethod attribute in Component tag in package.xml.
	 * Throws an exception if the name is null,
	 * 
	 * @param importMethod
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public void setImportMethod(
			WindowsAzureRoleComponentImportMethod importMethod)
			throws WindowsAzureInvalidProjectOperationException {
		if (null == importMethod) {
			throw new IllegalArgumentException(
					WindowsAzureConstants.INVALID_ARG);
		}
		try {
			Element component = getComponentNode();
			component.setAttribute(WindowsAzureConstants.ATTR_IMETHOD,
					importMethod.toString());
			this.importMethod = importMethod;
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception in setImportMethod", ex);
		}
	}

	/**
	 * This API is to returns the value of the cloudUpload attribute
	 * 
	 * @return
	 */
	public WARoleComponentCloudUploadMode getCloudUploadMode() {
		return cloudUploadMode;
	}

	/**
	 * This API sets the cloudUpload attribute in Component tag in package.xml.
	 * 
	 * @param cloudUploadMode
	 *            sets the attribute in package.xml if null then removes the
	 *            attribute
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public void setCloudUploadMode(
			WARoleComponentCloudUploadMode cloudUploadMode)
			throws WindowsAzureInvalidProjectOperationException {

		this.cloudUploadMode = cloudUploadMode;

		try {
			Element component = getComponentNode();
			if (null == cloudUploadMode) {
				component
						.removeAttribute(WindowsAzureConstants.ATTR_CLOUD_UPLOAD);
			} else {
				component.setAttribute(WindowsAzureConstants.ATTR_CLOUD_UPLOAD,
						cloudUploadMode.toString());
			}
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception in setCloudUploadMode", ex);
		}
	}

	/**
	 * This API sets the cloudaltsrc attribute in Component tag in package.xml.
	 * 
	 * @param url
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public void setCloudAltSrc(String url)
			throws WindowsAzureInvalidProjectOperationException {
		try {
			Element component = getComponentNode();
			if (url == null || url.isEmpty()) {
				component
						.removeAttribute(WindowsAzureConstants.ATTR_CLD_ALT_SRC);
			} else {
				component.setAttribute(WindowsAzureConstants.ATTR_CLD_ALT_SRC,
						url);
			}
			this.cloudAltSrc = url;
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception in setCloudAltSrc", ex);
		}
	}

	/**
	 * This API is to returns the value of the import from attribute
	 * 
	 * @return
	 */
	public String getImportPath() {
		return importPath;
	}

	/**
	 * This API sets the importfrom attribute in Component tag in package.xml.
	 * 
	 * @param importPath
	 *            should be not null.
	 * @throws WindowsAzureInvalidProjectOperationException. If
	 *             there is any internal problem.
	 */
	public void setImportPath(String importPath)
			throws WindowsAzureInvalidProjectOperationException {
		if (null == importPath) {
			throw new IllegalArgumentException(
					WindowsAzureConstants.INVALID_ARG);
		}
		try {
			if (this.deployName.isEmpty() && (this.importPath.isEmpty())) {
				this.importPath = importPath;
			}
			Element component = getComponentNode();
			component
					.setAttribute(WindowsAzureConstants.ATTR_IPATH, importPath);
			this.importPath = importPath;
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception in setImportPath", ex);
		}
	}

	/**
	 * This API is to returns the value of the deploymethod attribute
	 * 
	 * @return
	 */
	public WindowsAzureRoleComponentDeployMethod getDeployMethod() {
		return deployMethod;
	}

	/**
	 * This API sets the deploymethod attribute in Component tag in package.xml.
	 * 
	 * @param deployMethod
	 * @throws WindowsAzureInvalidProjectOperationException. If
	 *             there is any internal problem.
	 */
	public void setDeployMethod(
			WindowsAzureRoleComponentDeployMethod deployMethod)
			throws WindowsAzureInvalidProjectOperationException {
		if (null == deployMethod) {
			throw new IllegalArgumentException(
					WindowsAzureConstants.INVALID_ARG);
		}
		try {
			Element component = getComponentNode();
			component.setAttribute(WindowsAzureConstants.ATTR_DMETHOD,
					deployMethod.toString());
			this.deployMethod = deployMethod;
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception in setDeployMethod", ex);
		}
	}

	/**
	 * This API is to returns the value of the deploydir attribute.
	 * 
	 * @return
	 */
	public String getDeployDir() {
		return deployDir;
	}

	/**
	 * This API sets the deploydir attribute in Component tag in package.xml.
	 * 
	 * @param deployDir
	 * @throws WindowsAzureInvalidProjectOperationException. If
	 *             there is any internal problem.
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
	 * This API is to returns the value of the importas attribute in package.xml
	 * 
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
	public void setDeployName(String deployName)
			throws WindowsAzureInvalidProjectOperationException {
		if (null == deployName) {
			throw new IllegalArgumentException(
					WindowsAzureConstants.INVALID_ARG);
		}
		try {
			if (this.deployName.isEmpty() && (this.importPath.isEmpty())) {
				this.deployName = deployName;
			}
			Element component = getComponentNode();
			component.setAttribute(WindowsAzureConstants.ATTR_IMPORTAS,
					deployName);
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
		if (null == type) {
			throw new IllegalArgumentException(
					WindowsAzureConstants.INVALID_ARG);
		}
		try {
			if (this.type.isEmpty()) {
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
			String type = component
					.getAttribute(WindowsAzureConstants.ATTR_TYPE);
			if (type.toLowerCase().startsWith("server.")
					|| type.toLowerCase().startsWith("jdk.")) {
				isPreconfig = true;
			}
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception in getIsPreconfigured", ex);
		}
		return isPreconfig;

	}

	/**
	 * Gets the cloudurl attribute of the corresponding <component> tag in
	 * package.xml
	 * 
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public String getCloudDownloadURL()
			throws WindowsAzureInvalidProjectOperationException {
		if (cloudurl == null) {
			Element component = getComponentNode();
			String curl = component
					.getAttribute(WindowsAzureConstants.ATTR_CURL);
			if (!curl.isEmpty()) {
				cloudurl = curl;
			}
		}
		return cloudurl;
	}

	/**
	 * Sets the cloudurl attribute of the corresponding <component> tag in
	 * package.xml
	 * 
	 * @param url
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public void setCloudDownloadURL(String url)
			throws WindowsAzureInvalidProjectOperationException {
		try {
			if (null == url || url.isEmpty()) {
				Element component = getComponentNode();
				component.removeAttribute(WindowsAzureConstants.ATTR_CURL);
			} else {
				Element component = getComponentNode();
				component.setAttribute(WindowsAzureConstants.ATTR_CURL, url);
			}
			this.cloudurl = url;
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception in setCloudDownloadURL", ex);
		}
	}

	/**
	 * Gets the cloudmethod attribute of the corresponding <component> tag in
	 * package.xml
	 */
	public WindowsAzureRoleComponentCloudMethod getCloudMethod()
			throws WindowsAzureInvalidProjectOperationException {
		Element component = getComponentNode();
		String cmeth = component.getAttribute(WindowsAzureConstants.ATTR_CMTHD);
		if (!cmeth.isEmpty()) {
			cloudmethod = WindowsAzureRoleComponentCloudMethod.valueOf(cmeth);
		}
		return cloudmethod;
	}

	/**
	 * Sets the cloudmethod attribute of the corresponding <component> tag in
	 * package.xml
	 * 
	 * @param waCloudMethod
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public void setCloudMethod(
			WindowsAzureRoleComponentCloudMethod waCloudMethod)
			throws WindowsAzureInvalidProjectOperationException {

		try {
			Element component = getComponentNode();
			if (waCloudMethod == null
					|| WindowsAzureRoleComponentCloudMethod.none == waCloudMethod) {
				component.removeAttribute(WindowsAzureConstants.ATTR_CMTHD);
			} else {
				component.setAttribute(WindowsAzureConstants.ATTR_CMTHD,
						waCloudMethod.toString());
			}
			this.cloudmethod = waCloudMethod;
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception in setCloudMethod", ex);
		}
	}

	/*
	 * Gets the cloudkey attribute of the corresponding <component> tag in
	 * package.xml.
	 */
	public String getCloudKey()
			throws WindowsAzureInvalidProjectOperationException {
		if (cloudkey == null || cloudkey.isEmpty()) {
			Element component = getComponentNode();
			String ckey = component
					.getAttribute(WindowsAzureConstants.ATTR_CKEY);
			if (!ckey.isEmpty()) {
				cloudkey = ckey;
			}
		}
		return cloudkey;
	}

	/**
	 * Sets the cloudkey attribute of the corresponding <component> tag in
	 * package.xml.
	 * 
	 * @param cloudkey
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public void setCloudKey(String cloudkey)
			throws WindowsAzureInvalidProjectOperationException {

		try {
			Element component = getComponentNode();
			if (null == cloudkey || cloudkey.isEmpty()) {
				component.removeAttribute(WindowsAzureConstants.ATTR_CKEY);
			} else {
				component.setAttribute(WindowsAzureConstants.ATTR_CKEY,
						cloudkey);
			}
			this.cloudkey = cloudkey;
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception in setCloudKey", ex);
		}
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
