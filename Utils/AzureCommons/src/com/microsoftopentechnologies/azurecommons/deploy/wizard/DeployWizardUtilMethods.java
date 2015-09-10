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

package com.microsoftopentechnologies.azurecommons.deploy.wizard;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.interopbridges.tools.windowsazure.WindowsAzureCertificate;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.interopbridges.tools.windowsazure.WindowsAzureRoleComponent;
import com.microsoft.windowsazure.management.compute.models.ServiceCertificateListResponse.Certificate;
import com.microsoftopentechnologies.azurecommons.deploy.model.AutoUpldCmpnts;
import com.microsoftopentechnologies.azurecommons.exception.AzureCommonsException;
import com.microsoftopentechnologies.azurecommons.messagehandler.PropUtil;
import com.microsoftopentechnologies.azurecommons.wacommonutil.CerPfxUtil;

public class DeployWizardUtilMethods {
	static String autoUploadEr = PropUtil.getValueFromFile("autoUploadEr");
	static String dashAuto = "-auto";
	static String auto = PropUtil.getValueFromFile("methodAuto");
	static String certUploadEr = PropUtil.getValueFromFile("certUploadEr");
	static String dfltThmbprnt = PropUtil.getValueFromFile("dfltThmbprnt");
	static final String BASE_PATH = "${basedir}";

	/**
	 * Method restores caching properties which are updated before build
	 * to original state i.e. again updates storage account name to "auto"
	 * and removes key property.
	 * @param projMngr
	 * @return
	 * @throws AzureCommonsException 
	 */
	public static WindowsAzureProjectManager addAutoSettingsForCache(
			WindowsAzureProjectManager projMngr,
			List<String> roleMdfdCache) throws AzureCommonsException {
		try {
			// get number of roles in one project
			List<WindowsAzureRole> roleList = projMngr.getRoles();
			for (int i = 0; i < roleList.size(); i++) {
				WindowsAzureRole role = roleList.get(i);
				if (roleMdfdCache.contains(role.getName())) {
					role.setCacheStorageAccountName(dashAuto);
					role.setCacheStorageAccountKey("");
					role.setCacheStorageAccountUrl("");
				}
			}
		} catch (Exception e) {
			throw new AzureCommonsException(autoUploadEr, e);
		}
		return projMngr;
	}

	/**
	 * Method restores components which are updated before build
	 * to original state i.e. again updates cloudurl to "auto"
	 * and removes cloudkey attribute.
	 * @param projMngr
	 * @return
	 * @throws AzureCommonsException 
	 */
	public static WindowsAzureProjectManager addAutoCloudUrl(
			WindowsAzureProjectManager projMngr,
			List<AutoUpldCmpnts> mdfdCmpntList) throws AzureCommonsException {
		try {
			// get number of roles in one project
			List<WindowsAzureRole> roleList = projMngr.getRoles();
			for (int i = 0; i < roleList.size(); i++) {
				WindowsAzureRole role = roleList.get(i);
				AutoUpldCmpnts obj = new AutoUpldCmpnts(role.getName());
				// check list has entry with this role name
				if (mdfdCmpntList.contains(obj)) {
					// get list of components
					List<WindowsAzureRoleComponent> cmpnntsList =
							role.getComponents();
					// get indices of components which needs to be updated.
					int index = mdfdCmpntList.indexOf(obj);
					AutoUpldCmpnts presentObj =
							mdfdCmpntList.get(index);
					List<Integer> indices = presentObj.getCmpntIndices();
					// iterate over indices and update respective components.
					for (int j = 0; j < indices.size(); j++) {
						WindowsAzureRoleComponent cmpnt =
								cmpnntsList.get(indices.get(j));
						cmpnt.setCloudDownloadURL(auto);
						cmpnt.setCloudKey("");
					}
				}
			}
		} catch (Exception e) {
			throw new AzureCommonsException(autoUploadEr, e);
		}
		return projMngr;
	}

	/**
	 * If remote desktop is enabled
	 * then method checks whether
	 * its using sample certificate or not.
	 * @param waProjManager
	 * @return
	 * @throws AzureCommonsException 
	 */
	public static boolean checkRDPUsesSampleCert(WindowsAzureProjectManager waProjManager,
			String projLocation) throws AzureCommonsException {
		Boolean usesSampleCert = false;
		try {
			if (waProjManager.getRemoteAccessAllRoles()) {
				/*
				 * Check if sample certificate is used or
				 * custom one.
				 */
				String certPath = waProjManager.getRemoteAccessCertificatePath();
				if (certPath.startsWith(BASE_PATH)) {
					certPath = certPath.substring(certPath.indexOf("}") + 1
							, certPath.length());
					certPath = String.format("%s%s",
							projLocation,
							certPath);
				}
				String thumbprint = CerPfxUtil.getThumbPrint(certPath);
				if (thumbprint.equals(dfltThmbprnt)) {
					usesSampleCert = true;
				}
			}
		} catch (Exception e) {
			throw new AzureCommonsException(certUploadEr, e);
		}
		return usesSampleCert;
	}

	/**
	 * Certificate is not present on cloud
	 * but check whether its already
	 * been added to certToUpload list.
	 * To avoid unnecessary PFX password prompt
	 * invocation.
	 * @param certToUpload
	 * @param pmlCert
	 * @return
	 */
	public static boolean isDuplicateThumbprintCert(
			List<WindowsAzureCertificate> certToUpload,
			WindowsAzureCertificate pmlCert) {
		boolean alreadyAdded = false;
		for (int j = 0; j < certToUpload.size(); j++) {
			WindowsAzureCertificate certObj = certToUpload.get(j);
			if (certObj.getFingerPrint().
					equalsIgnoreCase(pmlCert.getFingerPrint())) {
				alreadyAdded = true;
				break;
			}
		}
		return alreadyAdded;
	}

	/**
	 * Prepares list of certificates which needs to be
	 * uploaded to cloud service by comparing
	 * certificates present in particular cloud service
	 * with the certificates configured in selected project.
	 * @param projMngr
	 * @return
	 */
	public static List<WindowsAzureCertificate> handleCertUpload(WindowsAzureProjectManager projMngr,
			List<Certificate> cloudCertList)
					throws AzureCommonsException {
		List<WindowsAzureCertificate> certToUpload =
				new ArrayList<WindowsAzureCertificate>();
		try {
			List<WindowsAzureRole> roleList = projMngr.getRoles();
			// iterate over roles
			for (int i = 0; i < roleList.size(); i++) {
				WindowsAzureRole role = roleList.get(i);
				Map<String, WindowsAzureCertificate> pmlCertList = role.getCertificates();
				for (Iterator<Entry<String, WindowsAzureCertificate>> iterator =
						pmlCertList.entrySet().iterator();
						iterator.hasNext();) {
					WindowsAzureCertificate pmlCert = iterator.next().getValue();
					/*
					 * No certificate present on cloud as REST API returned null
					 * Need to upload each certificate
					 */
					if (cloudCertList == null
							|| cloudCertList.isEmpty()) {
						if (!isDuplicateThumbprintCert(certToUpload, pmlCert)) {
							certToUpload.add(pmlCert);
						}
					} else {
						/*
						 * Check certificate is present on cloud
						 * or not.
						 */
						boolean isPresent = false;
						for (int j = 0; j < cloudCertList.size(); j++) {
							Certificate cloudCert = cloudCertList.get(j);
							if (cloudCert.getThumbprint().
									equalsIgnoreCase(pmlCert.getFingerPrint())) {
								isPresent = true;
								break;
							}
						}
						if (!isPresent
								&& !isDuplicateThumbprintCert(certToUpload, pmlCert)) {
							certToUpload.add(pmlCert);
						}
					}
				}
			}
		} catch (Exception e) {
			throw new AzureCommonsException(certUploadEr, e);
		}
		return certToUpload;
	}
}
