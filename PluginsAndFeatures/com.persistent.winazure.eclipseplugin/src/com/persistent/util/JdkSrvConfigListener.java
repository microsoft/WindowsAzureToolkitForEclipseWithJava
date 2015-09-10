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
package com.persistent.util;

import java.util.Arrays;

import waeclipseplugin.Activator;

import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.microsoftopentechnologies.azurecommons.storageregistry.StorageRegistryUtilMethods;
import com.microsoftopentechnologies.azurecommons.util.WAEclipseHelperMethods;
/**
 * Class has utility methods
 * required for UI components listeners of
 * JDK, Server and Application tabs
 * for azure deployment project creation wizard
 * and server configuration property page.
 */
public class JdkSrvConfigListener extends JdkSrvConfig {
	/**
	 * Method is used when JDK check box is checked.
	 * @return
	 */
	public static String jdkChkBoxChecked(String depJdkName) {
		// Pre-populate with auto-discovered JDK if any
		String jdkDefaultDir =
				WAEclipseHelperMethods.jdkDefaultDirectory(null);
		getTxtJdk().setText(jdkDefaultDir);
		setEnableJDK(true);
		getSerCheckBtn().setEnabled(true);
		if (getAutoDlRdCldBtn().getSelection()) {
			configureAutoUploadJDKSettings();
		} else if (getThrdPrtJdkBtn().getSelection()) {
			thirdPartyJdkBtnSelected();
		} else {
			jdkDeployBtnSelected();
		}
		return jdkDefaultDir;
	}
	
	/**
	 * Method is used when JDK check box is unchecked.
	 */
	public static void jdkChkBoxUnChecked() {
		setEnableJDK(false);
		getLblDlNoteUrl().setText(com.persistent.ui.projwizard.Messages.dlgDlNtLblUrl);
		if (getAutoDlRdCldBtn().getSelection()) {
			getSerCheckBtn().setSelection(false);
			setEnableServer(false);
			setEnableDlGrpSrv(false, false);
			configureAutoUploadJDKSettings();
		} else {
			// incase of third party and custom download just change text
			getAutoDlRdCldBtn().setText(com.persistent.util.Messages.noJdkDplyLbl);
		}
	}

	/**
	 * Method is used when JDK directory text is modified.
	 */
	public static void modifyJdkText() {
		// update only for auto upload not for third party JDK.
		if (getAutoDlRdCldBtn().getSelection()) {
			setTxtUrl(cmbBoxListener(
					getCmbStrgAccJdk(),
					getTxtUrl(), "JDK"));
			updateJDKHome(getTxtJdk().getText());
		} else if(getDlRdCldBtn().getSelection()
				&& !getTxtUrl().getText().isEmpty()) {
			modifyJdkUrlText();
		}
		updateJDKDlNote();
	}

	/**
	 * Method is used when JDK's deploy from custom download
	 * radio button is selected.
	 */
	public static void jdkDeployBtnSelected() {
		// deploy radio button selected
		setEnableDlGrp(true, false);
		updateJDKDlNote();
		updateJDKHome(getTxtJdk().getText());
		enableThirdPartyJdkCombo(false);
		getSerCheckBtn().setEnabled(true);
	}

	/**
	 * Method is used when third party JDK
	 * radio button is selected.
	 */
	public static void thirdPartyJdkBtnSelected() {
		setEnableDlGrp(true, true);
		enableThirdPartyJdkCombo(true);
		thirdPartyComboListener();
		updateJDKDlNote();
		getSerCheckBtn().setEnabled(true);
	}

	/**
	 * Method is used when JDK URL text is modified.
	 */
	public static void modifyJdkUrlText() {
		/*
		 * Extract storage account name
		 * and service endpoint from URL
		 * entered by user.
		 */
		String url = getTxtUrl().getText().trim();
		String nameInUrl =
				StorageRegistryUtilMethods.getAccNameFromUrl(
						url);
		setCmbStrgAccJdk(
				urlModifyListner(url, nameInUrl,
						getCmbStrgAccJdk()));
		/*
		 * update JAVA_HOME accordingly
		 */
		if (WAEclipseHelperMethods.isBlobStorageUrl(url) && url.endsWith(".zip")) {
			url = url.substring(0, url.indexOf(".zip"));
			updateJDKHome(url);
		}
	}

	/**
	 * Method is used when accounts link on JDK tab is clicked.
	 */
	public static void jdkAccLinkClicked() {
		accountsLinkOfJdkClicked();
		updateJDKDlURL();
	}

	/**
	 * Method is used when Server check box is checked.
	 */
	public static void srvChkBoxChecked() {
		setEnableServer(true);
		try {
			String[] servList =
					WindowsAzureProjectManager.
					getServerTemplateNames(cmpntFile);
			Arrays.sort(servList);
			getComboServer().setItems(servList);
			// select third party server button.
			getThrdPrtSrvBtn().setSelection(true);
			if (getAutoDlRdCldBtnSrv().getSelection()) {
				configureAutoUploadServerSettings();
			} else if (getThrdPrtSrvBtn().getSelection()) {
				thirdPartySrvBtnSelected();
			} else {
				srvDeployBtnSelected();
			}
			checkSDKPresenceAndEnableServer();
		} catch (WindowsAzureInvalidProjectOperationException e) {
			Activator.getDefault().log(e.getMessage());
		}
	}

	/**
	 * Method is used when Server check box is unchecked.
	 */
	public static void srvChkBoxUnChecked() {
		setEnableServer(false);
		enableApplicationTab(false);
		setEnableDlGrpSrv(false, false);
		getSerCheckBtn().setEnabled(true);
	}

	/**
	 * Method is used when Server directory text is modified.
	 */
	public static void modifySrvText() {
		if (isSrvAutoUploadChecked()) {
			setTxtUrlSrv(cmbBoxListener(
					getCmbStrgAccSrv(),
					getTxtUrlSrv(), "SERVER"));
			updateServerHome(getTxtDir().getText());
		} else if(getDlRdCldBtnSrv().getSelection()
				&& !getTxtUrlSrv().getText().isEmpty()) {
			modifySrvUrlText();
		}
		updateSrvDlNote();
	}

	/**
	 * Method is used when server's deploy from download
	 * radio button is selected.
	 */
	public static void srvDeployBtnSelected() {
		// server deploy radio button selected
		setEnableDlGrpSrv(true, false);
		checkSDKPresenceAndEnableServer();
		updateSrvDlNote();
		updateServerHome(getTxtDir().getText());
		enableThirdPartySrvCombo(false);
		enableApplicationTab(true);
		enforceSameLocalCloudServer();
	}
	
	/**
	 * Method is used when third party JDK
	 * radio button is selected.
	 */
	public static void thirdPartySrvBtnSelected() {
		setEnableDlGrpSrv(true, true);
		checkSDKPresenceAndEnableServer();
		enableThirdPartySrvCombo(true);
		thirdPartySrvComboListener();
		updateSrvDlNote();
		enableApplicationTab(true);
	}

	/**
	 * Method is used when server URL text is modified.
	 */
	public static void modifySrvUrlText() {
		/*
		 * Extract storage account name
		 * and service endpoint from URL
		 * entered by user.
		 */
		String url = getTxtUrlSrv().getText().trim();
		String nameInUrl =
				StorageRegistryUtilMethods.
				getAccNameFromUrl(
						url);
		setCmbStrgAccSrv(
				urlModifyListner(url, nameInUrl,
						getCmbStrgAccSrv()));
		/*
		 * update home directory for server accordingly
		 */
		if (WAEclipseHelperMethods.isBlobStorageUrl(url) && url.endsWith(".zip")) {
			url = url.substring(0, url.indexOf(".zip"));
			updateServerHome(url);
		}
	}

	/**
	 * Method is used when accounts link on server tab is clicked.
	 */
	public static void srvAccLinkClicked() {
		accountsLinkOfSrvClicked();
		updateServerDlURL();
		if (getThrdPrtSrvBtn().getSelection()) {
			updateServerHomeForThirdParty();
		}
	}

	/**
	 * Method used when server auto upload radio
	 * button selected.
	 */
	public static void configureAutoUploadServerSettings() {
		setEnableDlGrpSrv(true, true);
		enableLocalServerPathCmpnts(true);
		populateDefaultStrgAccForSrvAuto();
		updateServerDlURL();
		updateSrvDlNote();
		updateServerHome(getTxtDir().getText());
		enableThirdPartySrvCombo(false);
		enableApplicationTab(true);
		enforceSameLocalCloudServer();
	}

	/**
	 * Method used when JDK auto upload/no JDK deployment
	 * radio button selected.
	 */
	public static void configureAutoUploadJDKSettings() {
		if (getJdkCheckBtn().getSelection()) {
			setEnableDlGrp(true, true);
			updateJDKDlURL();
			updateJDKDlNote();
			updateJDKHome(getTxtJdk().getText());
			enableThirdPartyJdkCombo(false);
			getSerCheckBtn().setEnabled(true);
		} else {
			setEnableServer(false);
			setEnableDlGrpSrv(false, false);
			setEnableDlGrp(false, false);
			enableApplicationTab(false);
		}
	}

	/**
	 * Enable or disable third party JDK
	 * related components.
	 * @param status
	 */
	public static void enableThirdPartyJdkCombo(Boolean status) {
		getThrdPrtJdkCmb().setEnabled(status);
		getThrdPrtJdkBtn().setSelection(status);
	}
	
	/**
	 * Enable or disable third party JDK
	 * related components.
	 * @param status
	 */
	public static void enableThirdPartySrvCombo(Boolean status) {
		getThrdPrtSrvCmb().setEnabled(status);
		getThrdPrtSrvBtn().setSelection(status);
	}

	/**
	 * Method decides whether to
	 * show third party JDK names or not.
	 * @param status
	 */
	public static void showThirdPartyJdkNames(Boolean status, String depJdkName) {
		if (status) {
			try {
				String [] thrdPrtJdkArr = WindowsAzureProjectManager.
						getThirdPartyJdkNames(cmpntFile, depJdkName);
				// check at least one element is present
				if (thrdPrtJdkArr.length >= 1) {
					getThrdPrtJdkCmb().setItems(thrdPrtJdkArr);
					String valueToSet = "";
					valueToSet = WindowsAzureProjectManager.getFirstDefaultThirdPartyJdkName(cmpntFile);
					if (valueToSet.isEmpty()) {
						valueToSet = thrdPrtJdkArr[0];
					}
					getThrdPrtJdkCmb().setText(valueToSet);
				}
			} catch (WindowsAzureInvalidProjectOperationException e) {
				Activator.getDefault().log(e.getMessage());
			}
		} else {
			getThrdPrtJdkCmb().removeAll();
			getThrdPrtJdkCmb().setText("");
		}
	}

	/**
	 * Method decides whether to
	 * show third party JDK names or not.
	 * @param status
	 */
	public static void showThirdPartySrvNames(Boolean status, String localSrvName, String depSrvName) {
		if (status) {
			try {
				String [] thrdPrtSrvArr;
				if (localSrvName.isEmpty()) {
					thrdPrtSrvArr = WindowsAzureProjectManager.
							getAllThirdPartySrvNames(cmpntFile, depSrvName);
				} else {
					thrdPrtSrvArr = WindowsAzureProjectManager.getThirdPartySrvNames(
							cmpntFile, localSrvName, depSrvName);
				}
				// check at least one element is present else disable
				if (thrdPrtSrvArr.length >= 1) {
					getThrdPrtSrvCmb().setItems(thrdPrtSrvArr);
					String valueToSet = "";
					if (localSrvName.isEmpty()) {
						valueToSet = WindowsAzureProjectManager.
								getFirstDefaultThirdPartySrvName(cmpntFile);
					} else {
						valueToSet = WindowsAzureProjectManager.
								getDefaultThirdPartySrvName(cmpntFile, localSrvName);
					}
					if (valueToSet.isEmpty()) {
						valueToSet = thrdPrtSrvArr[0];
					}
					getThrdPrtSrvCmb().setText(valueToSet);
				}
			} catch (WindowsAzureInvalidProjectOperationException e) {
				Activator.getDefault().log(e.getMessage());
			}
		} else {
			getThrdPrtSrvCmb().removeAll();
			getThrdPrtSrvCmb().setText("");
		}
	}

	/**
	 * Listener for third party JDK name combo box.
	 * Updates URL and java home.
	 */
	public static void thirdPartyComboListener() {
		updateJDKDlURL();
		try {
			getTxtJavaHome().setText(WindowsAzureProjectManager.
					getCloudValue(getThrdPrtJdkCmb().getText(),
							cmpntFile));
		} catch (WindowsAzureInvalidProjectOperationException e) {
			Activator.getDefault().log(e.getMessage());
		}
	}
	
	/**
	 * Listener for third party JDK name combo box.
	 * Updates URL and java home.
	 */
	public static void thirdPartySrvComboListener() {
		updateServerDlURL();
		try {
			getTxtHomeDir().setText(WindowsAzureProjectManager.getThirdPartyServerHome(
					getThrdPrtSrvCmb().getText(), cmpntFile));
		} catch (WindowsAzureInvalidProjectOperationException e) {
			Activator.getDefault().log(e.getMessage());
		}
	}

	public static void disableRemoveButton() {
		if (JdkSrvConfig.getTblApp().getItemCount() == 0) {
			// table is empty i.e. number of rows = 0
			JdkSrvConfig.getBtnRemove().setEnabled(false);
		}
	}

	public static void enforceSameLocalCloudServer() {
		try {
			String srvName = getComboServer().getText();
			String cloudSrv = "";
			if (getThrdPrtSrvBtn().getSelection()) {
				cloudSrv = getThrdPrtSrvCmb().getText();
			}
			if (cloudSrv.isEmpty()) {
				/*
				 * user first selects a local server and
				 * third party radio button is not selected.
				 */
				populateServerNames(srvName);
			} else {
				if (WindowsAzureProjectManager.checkCloudAndLocalFamilyAreEqual(
						cmpntFile, srvName, cloudSrv)) {
					/*
					 * user first selects the cloud server
					 * and then a local server that is compatible with cloud server. 
					 */
					showThirdPartySrvNames(true, srvName, "");
					getThrdPrtSrvCmb().setText(cloudSrv);
				} else {
					/*
					 * user first selects the cloud server
					 * and then a local server that is different from cloud server. 
					 */
					populateServerNames(srvName);
					if (getThrdPrtSrvCmb().getItemCount() <= 0) {
						// if no third party servers available
						getAutoDlRdCldBtnSrv().setSelection(true);
						configureAutoUploadServerSettings();
					} else {
						thirdPartySrvBtnSelected();
					}
				}
			}
		} catch (WindowsAzureInvalidProjectOperationException e) {
			Activator.getDefault().log(e.getMessage());
		}
	}
	
	private static void populateServerNames(String srvName) {
		showThirdPartySrvNames(false, "", "");
		showThirdPartySrvNames(true, srvName, "");
		if (getThrdPrtSrvCmb().getItemCount() <= 0) {
			// if no third party servers available
			enableThirdPartySrvCombo(false);
			getThrdPrtSrvBtn().setEnabled(false);
		} else {
			getThrdPrtSrvBtn().setEnabled(true);
		}
	}
}
