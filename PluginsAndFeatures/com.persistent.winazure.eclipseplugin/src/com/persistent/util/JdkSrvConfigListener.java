/**
* Copyright 2015 Microsoft Open Technologies, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*	 http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/
package com.persistent.util;

import java.io.File;
import java.util.Arrays;

import waeclipseplugin.Activator;

import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.microsoftopentechnologies.azurecommons.storageregistry.StorageRegistryUtilMethods;
import com.microsoftopentechnologies.azurecommons.util.WAEclipseHelperMethods;
import com.persistent.winazureroles.Messages;
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
	public static String jdkChkBoxChecked(
			String depJdkName) {
		// Pre-populate with auto-discovered JDK if any
		String jdkDefaultDir =
				WAEclipseHelperMethods.jdkDefaultDirectory(null);
		getTxtJdk().setText(jdkDefaultDir);
		setEnableJDK(true);
		getSerCheckBtn().setEnabled(true);
		if (getAutoDlRdCldBtn().getSelection()) {
			configureAutoUploadJDKSettings(Messages.dlNtLblDir);
		} else if (getThrdPrtJdkBtn().getSelection()) {
			thirdPartyJdkBtnSelected(Messages.dlNtLblDir);
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
		getLblDlNoteUrl().setText(Messages.dlgDlNtLblUrl);
		if (getAutoDlRdCldBtn().getSelection()) {
			getSerCheckBtn().setSelection(false);
			setEnableServer(false);
			setEnableDlGrpSrv(false, false);
			configureAutoUploadJDKSettings(Messages.dlNtLblDir);
		} else {
			// incase of third party and custom download just change text
			getAutoDlRdCldBtn().setText(com.persistent.util.Messages.noJdkDplyLbl);
		}
	}

	/**
	 * Method is used when JDK directory text is modified.
	 * @param role
	 * @param label
	 */
	public static void modifyJdkText(String label) {
		// update only for auto upload not for third party JDK.
		if (getAutoDlRdCldBtn().getSelection()) {
			setTxtUrl(cmbBoxListener(
					getCmbStrgAccJdk(),
					getTxtUrl(), "JDK"));
			updateJDKDlNote(label);
			updateJDKHome(getTxtJdk().getText());
		}
	}

	/**
	 * Method is used when focus is lost
	 * from JDK directory text box.
	 * @param jdkPath
	 */
	public static void focusLostJdkText(String jdkPath) {
		// Update note below JDK URL text box
		File file = new File(jdkPath);
		if (getDlRdCldBtn().getSelection()
				&& !jdkPath.isEmpty()
				&& file.exists()) {
			String dirName = file.getName();
			getLblDlNoteUrl().
			setText(String.format(
					Messages.dlNtLblDir, dirName));
		} else {
			getLblDlNoteUrl().
			setText(Messages.dlgDlNtLblUrl);
		}
	}

	/**
	 * Method is used when JDK's deploy from custom download
	 * radio button is selected.
	 * @param role
	 */
	public static void jdkDeployBtnSelected() {
		// deploy radio button selected
		setEnableDlGrp(true, false);
		updateJDKDlNote(Messages.dlNtLblDir);
		updateJDKHome(getTxtJdk().getText());
		enableThirdPartyJdkCombo(false);
		enableServerCloudBtns(true);
	}

	/**
	 * Method is used when third party JDK
	 * radio button is selected.
	 * @param role
	 * @param label
	 */
	public static void thirdPartyJdkBtnSelected(String label) {
		setEnableDlGrp(true, true);
		enableThirdPartyJdkCombo(true);
		thirdPartyComboListener();
		updateJDKDlNote(label);
		enableServerCloudBtns(true);
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
	public static void srvChkBoxChecked(String label) {
		setEnableServer(true);
		try {
			String[] servList =
					WindowsAzureProjectManager.
					getServerTemplateNames(cmpntFile);
			Arrays.sort(servList);
			getComboServer().setItems(servList);
			if (getAutoDlRdCldBtnSrv().getSelection()) {
				configureAutoUploadServerSettings(label);
			} else if (getThrdPrtSrvBtn().getSelection()) {
				thirdPartySrvBtnSelected(label);
			} else {
				srvDeployBtnSelected(label);
			}
		} catch (WindowsAzureInvalidProjectOperationException e) {
			Activator.getDefault().log(e.getMessage());
		}
	}

	/**
	 * Method is used when Server check box is unchecked.
	 */
	public static void srvChkBoxUnChecked() {
		/*
		 * If first radio button was selected
		 * and server check box has been unchecked
		 * then disable complete server group
		 * else just change text of first radio button.
		 */
		if (getAutoDlRdCldBtnSrv().getSelection()) {
			setEnableDlGrpSrv(false, false);
			getThrdPrtSrvBtn().setEnabled(true);
			enableApplicationTab(false);
		} else {
			getAutoDlRdCldBtnSrv().setText(com.persistent.util.Messages.noSrvDplyLbl);
		}
		/*
		 * As local server has been removed,
		 * show all third party servers.
		 */
		if (getThrdPrtSrvBtn().getSelection()) {
			String cloudSrv = getThrdPrtSrvCmb().getText();
			showThirdPartySrvNames(true, "", "");
			getThrdPrtSrvCmb().setText(cloudSrv);
		} else if (getDlRdCldBtnSrv().getSelection()) {
			getThrdPrtSrvBtn().setEnabled(true);
			getAutoDlRdCldBtnSrv().setSelection(true);
			setEnableDlGrpSrv(false, false);
			enableApplicationTab(false);
		}
		setEnableServer(false);
		getSerCheckBtn().setEnabled(true);
	}

	/**
	 * Method is used when Server directory text is modified.
	 * @param role
	 * @param label
	 */
	public static void modifySrvText(String label) {
		if (isSrvAutoUploadChecked()) {
			setTxtUrlSrv(cmbBoxListener(
					getCmbStrgAccSrv(),
					getTxtUrlSrv(), "SERVER"));
			updateServerHome(getTxtDir().getText());
		} else if(getDlRdCldBtnSrv().getSelection()
				&& !getTxtUrlSrv().getText().isEmpty()) {
			modifySrvUrlText();
		}
		updateSrvDlNote(label);
	}

	/**
	 * Method is used when server's deploy from download
	 * radio button is selected.
	 * @param role
	 * @param label
	 */
	public static void srvDeployBtnSelected(String label) {
		// server deploy radio button selected
		setEnableDlGrpSrv(true, false);
		updateSrvDlNote(label);
		updateServerHome(getTxtDir().getText());
		enableThirdPartySrvCombo(false);
		enableApplicationTab(true);
	}
	
	/**
	 * Method is used when third party JDK
	 * radio button is selected.
	 * @param role
	 * @param label
	 */
	public static void thirdPartySrvBtnSelected(String label) {
		setEnableDlGrpSrv(true, true);
		enableThirdPartySrvCombo(true);
		thirdPartySrvComboListener();
		updateSrvDlNote(label);
		enableApplicationTab(true);
	}

	/**
	 * Method is used when server URL text is modified.
	 * @param role
	 * @param label
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
	 * @param role
	 * @param label
	 */
	public static void configureAutoUploadServerSettings(String label) {
		if (getSerCheckBtn().getSelection()) {
			setEnableDlGrpSrv(true, true);
			populateDefaultStrgAccForSrvAuto();
			updateServerDlURL();
			updateSrvDlNote(label);
			updateServerHome(getTxtDir().getText());
			enableThirdPartySrvCombo(false);
			enableApplicationTab(true);
		} else {
			setEnableDlGrpSrv(false, false);
			enableApplicationTab(false);
		}
	}

	/**
	 * Method used when JDK auto upload/no JDK deployment
	 * radio button selected.
	 * @param role
	 * @param label
	 */
	public static void configureAutoUploadJDKSettings(
			String label) {
		if (getJdkCheckBtn().getSelection()) {
			setEnableDlGrp(true, true);
			updateJDKDlURL();
			updateJDKDlNote(label);
			updateJDKHome(getTxtJdk().getText());
			enableThirdPartyJdkCombo(false);
			enableServerCloudBtns(true);
		} else {
			JdkSrvConfig.setEnableServer(false);
			JdkSrvConfig.setEnableDlGrpSrv(false, false);
			setEnableDlGrp(false, false);
			enableServerCloudBtns(false);
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
					getThrdPrtJdkCmb().setText(thrdPrtJdkArr[0]);
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
					getAutoDlRdCldBtnSrv().setSelection(true);
					configureAutoUploadServerSettings(
							com.persistent.ui.projwizard.Messages.dlNtLblDirSrv);
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
