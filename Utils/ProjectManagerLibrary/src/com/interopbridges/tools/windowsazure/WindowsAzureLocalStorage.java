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

/**
 * This class is for representing a local storage resource.
 */
public class WindowsAzureLocalStorage {

	/**
	 * name variable is represents name attribute in LocalStorage tag in
	 * ServiceDefinition.csdef.
	 */
	private String name = "";

	/**
	 * size variable is represents sizeInMB attribute in LocalStorage tag in
	 * ServiceDefinition.csdef.
	 */
	private int size;

	/**
	 * cleanOnRecycle variable is represents cleanOnRoleRecycle attribute in
	 * LocalStorage tag in ServiceDefinition.csdef.
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
	private WindowsAzureProjectManager wProj = null;

	/**
	 * wRole Variable is to represent corresponding object of WindowsAzureRole.
	 */
	private WindowsAzureRole wRole = null;

	/**
	 * lsVarList contsins list of all env variable related to local storage
	 * WindowsAzureRole.
	 */

	protected WindowsAzureLocalStorage(WindowsAzureProjectManager waProj,
			WindowsAzureRole waRole) {
		wProj = waProj;
		wRole = waRole;
	}

	private Element getLocalStorageNode()
			throws WindowsAzureInvalidProjectOperationException {
		try {
			XPath xPath = XPathFactory.newInstance().newXPath();
			Document doc = wProj.getdefinitionFileDoc();
			String expr = String.format(WindowsAzureConstants.LS_NAME,
					wRole.getName(), this.getName());
			Element locSt = (Element) xPath.evaluate(expr, doc,
					XPathConstants.NODE);
			if (locSt == null) {
				throw new WindowsAzureInvalidProjectOperationException(
						"Exception while geting Local storege node");
			}
			return locSt;
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception while geting Local storege node", ex);
		}
	}

	/**
	 * This API is to returns the value of the name attribute of the
	 * corresponding <LocalStorage> XML node.
	 * 
	 * @return name .
	 */
	public String getName() {
		return name;
	}

	/**
	 * This API changes the name. Throws an exception if the name is null or
	 * blank, duplicated, or otherwise invalid .
	 * 
	 * @param lsName
	 *            .
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public void setName(String lsName)
			throws WindowsAzureInvalidProjectOperationException {
		if ((null == lsName) || (lsName.isEmpty())) {
			throw new IllegalArgumentException(
					WindowsAzureConstants.INVALID_ARG);
		}
		if (isCachingLocalStorage()) {
			throw new WindowsAzureInvalidProjectOperationException(
					"This local storage is assoiciated with caching");
		}
		try {
			if (this.name.isEmpty()) {
				this.name = lsName;
			}
			Element lsEle = getLocalStorageNode();
			lsEle.setAttribute(WindowsAzureConstants.ATTR_NAME, lsName);

			// change in corresponding <RoleInstanceValue>
			XPath xPath = XPathFactory.newInstance().newXPath();
			Document doc = wProj.getdefinitionFileDoc();
			String expr = String.format(
					WindowsAzureConstants.ROLE_INSTANCE_NODE, wRole.getName(),
					getPathEnv(), getName());
			Element eleRInstance = (Element) xPath.evaluate(expr, doc,
					XPathConstants.NODE);
			if (eleRInstance != null) {
				eleRInstance.setAttribute("xpath", String.format(
						WindowsAzureConstants.ROLE_INSTANCE_PATH, lsName));
			}
			WindowsAzureLocalStorage obj = wRole.locStoMap.get(this.name);
			wRole.locStoMap.remove(this.name);
			wRole.locStoMap.put(lsName, obj);
			this.name = lsName;
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception in setName", ex);
		}
	}

	/**
	 * This API returns the value of the size attribute of the corresponding
	 * <LocalStorage> XML node.
	 * 
	 * @return size .
	 */
	public int getSize() {
		return size;
	}

	/**
	 * This API sets the size. Must be no less than 1. It may be greater than
	 * the maximum size allowed for the role.
	 * 
	 * @param lsSize
	 *            .
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public void setSize(int lsSize)
			throws WindowsAzureInvalidProjectOperationException {
		if (lsSize < 1) {
			throw new IllegalArgumentException(
					WindowsAzureConstants.INVALID_ARG);
		}
		try {
			Element lsEle = getLocalStorageNode();
			lsEle.setAttribute(WindowsAzureConstants.ATTR_SIZEINMB,
					String.valueOf(lsSize));
			this.size = lsSize;
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception in setSize", ex);
		}
	}

	/**
	 * This API returns True if the cleanOnRecycle attribute of the
	 * corresponding <LocalStorage> XML node is True, else False.
	 * 
	 * @return cleanOnRecycle .
	 */
	public Boolean getCleanOnRecycle() {
		return cleanOnRecycle;
	}

	/**
	 * This API sets the cleanOnRecycle attribute.
	 * 
	 * @param lsCleanOnRecycle
	 *            .
	 * @throws WindowsAzureInvalidProjectOperationException .
	 */
	public void setCleanOnRecycle(Boolean lsCleanOnRecycle)
			throws WindowsAzureInvalidProjectOperationException {
		try {
			Element lsEle = getLocalStorageNode();
			lsEle.setAttribute(WindowsAzureConstants.ATTR_CLE_ON_ROLE_RECYCLE,
					String.valueOf(lsCleanOnRecycle));
			this.cleanOnRecycle = lsCleanOnRecycle;
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception in setCleanOnRecycle", ex);
		}
	}

	/**
	 * this API Returns the name of the associated path variable, or null if
	 * none.
	 * 
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException .
	 */
	public String getPathEnv()
			throws WindowsAzureInvalidProjectOperationException {
		try {
			XPath xPath = XPathFactory.newInstance().newXPath();
			Document doc = wProj.getdefinitionFileDoc();
			String expr = String.format(WindowsAzureConstants.VAR_LS_ENV_NAME,
					wRole.getName(), getName());
			pathEnv = xPath.evaluate(expr, doc);
			return pathEnv;
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception in getPathEnv", ex);
		}
	}

	/**
	 * this API sets the name of the associated path variable, or null if none.
	 * 
	 * @param pathEnv
	 * @throws WindowsAzureInvalidProjectOperationException .
	 */
	public void setPathEnv(String pathEnv)
			throws WindowsAzureInvalidProjectOperationException {

		if (pathEnv == null) {
			throw new IllegalArgumentException(
					WindowsAzureConstants.INVALID_ARG);
		}
		try {
			if (pathEnv.isEmpty()) {
				// delete path environment variable if exist
				if (!getPathEnv().isEmpty()) {
					deleteLsEnv(getPathEnv());
				}
			} else {
				// Check the path variable already exist,
				// if path is exist throws exception
				if (wRole.getLsEnv().contains(pathEnv)
						&& (!getPathEnv().equals(pathEnv))) {
					throw new WindowsAzureInvalidProjectOperationException(
							"Path Already exist");
				}
				if (!getPathEnv().isEmpty()) {
					deleteLsEnv(getPathEnv());
				}
				// set path environment variable
				wRole.setVarInDefFile(pathEnv);
				wRole.lsVarList.add(pathEnv);
				XPath xPath = XPathFactory.newInstance().newXPath();
				Document doc = wProj.getdefinitionFileDoc();
				String expr = String.format(
						WindowsAzureConstants.ROLE_INSTANCE_NODE,
						wRole.getName(), pathEnv, getName());
				Element eleRInstance = (Element) xPath.evaluate(expr, doc,
						XPathConstants.NODE);
				if (eleRInstance == null) {
					// Create <RoleInstanceValue> node and
					// append to path variable node
					expr = String.format(
							WindowsAzureConstants.VAR_WITH_SPECIFIC_NAME,
							wRole.getName(), pathEnv);
					Element eleVar = (Element) xPath.evaluate(expr, doc,
							XPathConstants.NODE);
					eleRInstance = doc.createElement("RoleInstanceValue");
					eleVar.appendChild(eleRInstance);
				}
				eleRInstance.setAttribute("xpath", String.format(
						WindowsAzureConstants.ROLE_INSTANCE_PATH, getName()));
			}
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					"Exception in setPathEnv", ex);
		}
		WindowsAzureLocalStorage.pathEnv = pathEnv;
	}

	/**
	 * This API Deletes the corresponding <LocalStorage> XML from CSDEF. If the
	 * parent <LocalResources> element is empty as a result, then delete that as
	 * well.
	 * 
	 * @throws WindowsAzureInvalidProjectOperationException .
	 */
	public void delete() throws WindowsAzureInvalidProjectOperationException {
		// if caching is associated,This Local storage can not be deleted
		if (isCachingLocalStorage()) {
			throw new WindowsAzureInvalidProjectOperationException(
					"This local storage is assoiciated with caching");
		}

		// delete corresponding environment variable
		if (!getPathEnv().isEmpty()) {
			deleteLsEnv(getPathEnv());
		}
		wRole.locStoMap.remove(getName());
		Element lsEle = getLocalStorageNode();
		lsEle.getParentNode().removeChild(lsEle);
	}

	public void deleteLsEnv(String varName)
			throws WindowsAzureInvalidProjectOperationException {
		if ((null == varName) || varName.isEmpty()) {
			throw new IllegalArgumentException(
					WindowsAzureConstants.INVALID_ARG);
		}
		try {
			XPath xPath = XPathFactory.newInstance().newXPath();
			Document doc = wProj.getdefinitionFileDoc();
			String expr = String.format(
					WindowsAzureConstants.VAR_WITH_SPECIFIC_NAME,
					wRole.getName(), varName);
			Element var = (Element) xPath.evaluate(expr, doc,
					XPathConstants.NODE);
			if (var == null) {
				throw new WindowsAzureInvalidProjectOperationException(varName
						+ " variable does not exist");
			}
			wRole.lsVarList.remove(varName);
			var.getParentNode().removeChild(var);
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					WindowsAzureConstants.EXCP, ex);
		}
	}

	/**
	 * This API is return true in case LS is associated with caching (having
	 * name "Microsoft.WindowsAzure.Plugins.Caching.FileStore") and name caching
	 * is enabled
	 */
	public boolean isCachingLocalStorage()
			throws WindowsAzureInvalidProjectOperationException {
		boolean isCachingAssociated = false;
		try {
			if (getName().equalsIgnoreCase(WindowsAzureConstants.CACHE_LS_NAME)
					&& !wRole.getNamedCaches().isEmpty()) {
				isCachingAssociated = true;
			}
		} catch (Exception ex) {
			throw new WindowsAzureInvalidProjectOperationException(
					WindowsAzureConstants.EXCP, ex);
		}
		return isCachingAssociated;
	}
}
