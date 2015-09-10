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

package com.microsoftopentechnologies.azurecommons.startup;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.interopbridges.tools.windowsazure.WARoleComponentCloudUploadMode;
import com.interopbridges.tools.windowsazure.WindowsAzurePackageType;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.interopbridges.tools.windowsazure.WindowsAzureRoleComponent;
import com.interopbridges.tools.windowsazure.WindowsAzureRoleComponentCloudMethod;
import com.microsoftopentechnologies.azurecommons.messagehandler.PropUtil;
import com.microsoftopentechnologies.azurecommons.storageregistry.StorageAccount;
import com.microsoftopentechnologies.azurecommons.storageregistry.StorageAccountRegistry;
import com.microsoftopentechnologies.azurecommons.storageregistry.StorageRegistryUtilMethods;
import com.microsoftopentechnologies.azurecommons.util.WAEclipseHelperMethods;
import com.microsoftopentechnologies.azurecommons.wacommonutil.PreferenceSetUtil;

public class WAStartUpUtilMethods {
	private static String expStrgReg = PropUtil.getValueFromFile("expStrgReg");
	private static String typeJdkDply = PropUtil.getValueFromFile("typeJdkDply");
	private static String typeSrvDply = PropUtil.getValueFromFile("typeSrvDply");
	private final static String AUTO = PropUtil.getValueFromFile("methodAuto");
	private static String typeSrvApp = PropUtil.getValueFromFile("typeSrvApp");
	private static String approot = PropUtil.getValueFromFile("approot");
	private static String expLocToAuto = PropUtil.getValueFromFile("expLocToAuto");

	/**
	 * Storage account registry project open logic.
	 * Plugin needs to detect and aggregate the information
	 * about the different storage accounts used by the components.
	 * If account is not there,
	 * then add the storage account, with the key to the registry.
	 * If it's there,
	 * but the access key is different, then update component's cloud key
	 * with the key from the registry.
	 * @param projMngr
	 * @return
	 * @throws Exception 
	 */
	public static WindowsAzureProjectManager initializeStorageAccountRegistry(
			WindowsAzureProjectManager projMngr, String prefFilePath) throws Exception {
		try {
			List<StorageAccount> strgAccList = StorageAccountRegistry.getStrgList();
			// get number of roles in one project
			List<WindowsAzureRole> roleList = projMngr.getRoles();
			for (int i = 0; i < roleList.size(); i++) {
				WindowsAzureRole role = roleList.get(i);
				// check for caching storage account name and key given
				String key = role.getCacheStorageAccountKey();
				String name = role.getCacheStorageAccountName();
				if (key != null
						&& name != null
						&& !key.isEmpty()
						&& !name.isEmpty()
						&& WAEclipseHelperMethods.isLowerCaseAndInteger(name)) {
					StorageAccount account = new StorageAccount(name,
							key,
							PreferenceSetUtil.getSelectedBlobServiceURL(name, prefFilePath));
					if (strgAccList.contains(account)) {
						int index = strgAccList.indexOf(account);
						String keyInReg = strgAccList.get(index).getStrgKey();
						if (!key.equals(keyInReg)) {
							// update key of component
							role.setCacheStorageAccountKey(keyInReg);
						}
					} else {
						// add account in registry.
						strgAccList.add(account);
					}
				}

				// get list of components in one role.
				List<WindowsAzureRoleComponent> cmpnntsList =
						role.getComponents();
				for (int j = 0; j < cmpnntsList.size(); j++) {
					WindowsAzureRoleComponent component =
							cmpnntsList.get(j);
					// check cloud URL is set or not
					String url = component.getCloudDownloadURL();
					if (url != null
							&& !url.isEmpty()) {
						try {
							new URL(url);
							String accessKey = component.getCloudKey();
							/*
							 * check cloud key is set or not
							 * if not then URL is publicly accessible
							 * hence do not add that account in registry.
							 */
							if (accessKey != null
									&& !accessKey.isEmpty()) {
								StorageAccount account = new StorageAccount(
										StorageRegistryUtilMethods.
										getAccNameFromUrl(url),
										accessKey,
										StorageRegistryUtilMethods.
										getServiceEndpointUrl(url));
								if (strgAccList.contains(account)) {
									int index = strgAccList.indexOf(account);
									String keyInReg = strgAccList.get(index).getStrgKey();
									if (!accessKey.equals(keyInReg)) {
										// update key of component
										component.setCloudKey(keyInReg);
									}
								} else {
									// add account in registry.
									strgAccList.add(account);
								}
							}
						} catch(MalformedURLException e) {
						}
					}
				}
			}
			StorageAccountRegistry.editUrlsAsPerCloud();
		} catch (Exception e) {
			throw new Exception(expStrgReg, e);
		}
		return projMngr;
	}

	/**
	 * Change include in package deployment to
	 * auto upload with auto storage selected.
	 * Need these changes while importing old project
	 * in new plugin.
	 * @param projMngr
	 * @return
	 * @throws Exception 
	 */
	public static WindowsAzureProjectManager changeLocalToAuto(
			WindowsAzureProjectManager projMngr, String projName, File cmpntFile) throws Exception {
		try {
			// get number of roles in one project
			List<WindowsAzureRole> roleList = projMngr.getRoles();
			for (int i = 0; i < roleList.size(); i++) {
				WindowsAzureRole role = roleList.get(i);
				// get list of components in one role.
				List<WindowsAzureRoleComponent> cmpnntsList =
						role.getComponents();
				for (int j = 0; j < cmpnntsList.size(); j++) {
					WindowsAzureRoleComponent component =
							cmpnntsList.get(j);
					String type = component.getType();
					String key = component.getCloudKey();
					String url = component.getCloudDownloadURL();
					/*
					 * check component is JDK or server
					 * and cloud URL and key is not specified
					 * i.e. deployment is for local.
					 */
					if ((type.equals(typeJdkDply)
							|| type.equals(typeSrvDply))
							&& (key == null || key.isEmpty())
							&& (url == null || url.isEmpty())) {
						component.setCloudDownloadURL(AUTO);
						component.setCloudUploadMode(
								WARoleComponentCloudUploadMode.auto);
						component.setCloudMethod(
								WindowsAzureRoleComponentCloudMethod.unzip);
						// store home properties
						/*
						 * For auto upload cloud
						 * and local home property will be same.
						 * So just check package type, construct
						 * home value and set.
						 */
						if (projMngr.getPackageType().
								equals(WindowsAzurePackageType.LOCAL)) {
							if (type.equals(typeJdkDply)) {
								role.setJDKCloudHome(
										WindowsAzureRole.constructJdkHome(
												component.getImportPath(),
												cmpntFile));
							} else if (type.equals(typeSrvDply)) {
								role.setServerCloudHome(
										WindowsAzureRole.constructServerHome(role.getServerName(),
												component.getImportPath(),
												cmpntFile));
							}
						} else {
							if (type.equals(typeJdkDply)) {
								role.setJDKLocalHome(
										WindowsAzureRole.constructJdkHome(
												component.getImportPath(),
												cmpntFile));
							} else if (type.equals(typeSrvDply)) {
								role.setServerLocalHome(
										WindowsAzureRole.constructServerHome(role.getServerName(),
												component.getImportPath(), cmpntFile));
							}
						}
					} else if (type.equals(typeSrvApp)) {
						String approotPathSubStr = String.format("%s%s%s%s",
								projName,
								File.separator,
								role.getName(),
								approot);
						String impSrc = component.getImportPath();
						if (impSrc != null
								&& !impSrc.isEmpty()
								&& !impSrc.contains(approotPathSubStr)) {
							component.setCloudUploadMode(WARoleComponentCloudUploadMode.always);
							component.setCloudDownloadURL(AUTO);
							component.setCloudMethod(
									WindowsAzureRoleComponentCloudMethod.copy);
						}
					}
				}
			}
		} catch (Exception e) {
			throw new Exception(expLocToAuto, e);
		}
		return projMngr;
	}
}
