/**
* Copyright 2014 Microsoft Open Technologies, Inc.
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

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import waeclipseplugin.Activator;

import com.interopbridges.tools.windowsazure.WARoleComponentCloudUploadMode;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.microsoftopentechnologies.wacommon.storageregistry.StorageAccount;
import com.microsoftopentechnologies.wacommon.storageregistry.StorageAccountRegistry;
import com.microsoftopentechnologies.wacommon.storageregistry.StorageRegistryUtilMethods;
import com.persistent.ui.preference.StorageAccountsPreferencePage;
import com.persistent.ui.projwizard.WAProjectWizard;

/**
 * Class creates all the UI components
 * required on JDK, Server and Application tabs
 * for azure deployment project creation wizard
 * and server configuration property page.
 */
public class JdkSrvConfig {
	// Variables for JDK Download group
	private static Group dlJdkGrp;
	private static Text txtUrl;
	private static Combo cmbStrgAccJdk;
	private static Link accLinkJdk;
	private static Button dlRdCldBtn;
	private static Button autoDlRdCldBtn;
	private static Button thrdPrtJdkBtn;
	private static Combo thrdPrtJdkCmb;
	private static Link thrdPrtJdkLink;
	private static Label lblUrl;
	private static Label lblKey;
	private static Label lblDlNoteUrl;
	private static Label lblJavaHome;
	private static Text txtJavaHome;
	// Variables for Server Download group
	private static Group dlSrvGrp;
	private static Text txtUrlSrv;
	private static Combo cmbStrgAccSrv;
	private static Link accLinkSrv;
	private static Button dlRdCldBtnSrv;
	private static Button autoDlRdCldBtnSrv;
	private static Label lblUrlSrv;
	private static Label lblKeySrv;
	private static Label lblDlNoteUrlSrv;
	private static Label lblHomeDir;
	private static Text txtHomeDir;
	// Variables for JDK group
	private static Button jdkCheckBtn;
	private static Button btnJdkLoc;
	private static Text txtJdk;
	// Variables for Server group
	private static Button serCheckBtn;
	private static Text txtDir;
	private static Button btnSrvLoc;
	private static Label lblSelect;
	private static Combo comboServer;
	private static Link custLink;
	// Variables for Application table
	private static Table tblApp;
	private static Button btnRemove;
	private static Button btnAdd;
	private static TableViewer tableViewer;
	private static TableColumn colName;
	public static final String JDK_TXT = "JDK";
	public static final String SRV_TXT = "SERVER";
	private static String[] accNames = getStrgAccoNamesAsPerTab(null, false);
	public static final String NONE_TXT = "(none)";
	public static final String AUTO_TXT = "(auto)";
	public static final String FWD_SLASH = "/";
	protected static File cmpntFile = new File(WAEclipseHelper.getTemplateFile(Messages.cmpntFileName));

	/*
	 * Getter methods for UI components.
	 */
	public static String[] getAccNames() {
		return accNames;
	}

	public static Button getJdkCheckBtn() {
		return jdkCheckBtn;
	}

	public static void setTxtUrl(Text txtUrl) {
		JdkSrvConfig.txtUrl = txtUrl;
	}

	public static void setTxtUrlSrv(Text txtUrlSrv) {
		JdkSrvConfig.txtUrlSrv = txtUrlSrv;
	}

	public static void setCmbStrgAccJdk(Combo cmbStrgAccJdk) {
		JdkSrvConfig.cmbStrgAccJdk = cmbStrgAccJdk;
	}

	public static void setCmbStrgAccSrv(Combo cmbStrgAccSrv) {
		JdkSrvConfig.cmbStrgAccSrv = cmbStrgAccSrv;
	}

	public static Button getBtnJdkLoc() {
		return btnJdkLoc;
	}

	public static Text getTxtJdk() {
		return txtJdk;
	}

	public static Button getSerCheckBtn() {
		return serCheckBtn;
	}

	public static Button getBtnSrvLoc() {
		return btnSrvLoc;
	}

	public static Text getTxtDir() {
		return txtDir;
	}

	public static Combo getComboServer() {
		return comboServer;
	}

	public static Link getCustLink() {
		return custLink;
	}

	public static Table getTblApp() {
		return tblApp;
	}

	public static Button getBtnRemove() {
		return btnRemove;
	}

	public static Button getBtnAdd() {
		return btnAdd;
	}

	public static TableViewer getTableViewer() {
		return tableViewer;
	}

	public static TableColumn getColName() {
		return colName;
	}

	public static Text getTxtUrl() {
		return txtUrl;
	}

	public static Button getDlRdCldBtn() {
		return dlRdCldBtn;
	}

	public static Button getAutoDlRdCldBtn() {
		return autoDlRdCldBtn;
	}

	public static Button getDlRdCldBtnSrv() {
		return dlRdCldBtnSrv;
	}

	public static Button getAutoDlRdCldBtnSrv() {
		return autoDlRdCldBtnSrv;
	}

	public static Text getTxtUrlSrv() {
		return txtUrlSrv;
	}

	public static Link getAccLinkJdk() {
		return accLinkJdk;
	}

	public static Link getAccLinkSrv() {
		return accLinkSrv;
	}

	public static Label getLblDlNoteUrl() {
		return lblDlNoteUrl;
	}

	public static Combo getCmbStrgAccJdk() {
		return cmbStrgAccJdk;
	}

	public static Combo getCmbStrgAccSrv() {
		return cmbStrgAccSrv;
	}

	public static Label getLblDlNoteUrlSrv() {
		return lblDlNoteUrlSrv;
	}

	public static Text getTxtJavaHome() {
		return txtJavaHome;
	}

	public static Text getTxtHomeDir() {
		return txtHomeDir;
	}

	public static Combo getThrdPrtJdkCmb() {
		return thrdPrtJdkCmb;
	}

	public static Button getThrdPrtJdkBtn() {
		return thrdPrtJdkBtn;
	}

	public static Link getThrdPrtJdkLink() {
		return thrdPrtJdkLink;
	}

	/**
	 * Method creates all components
	 * required for JDK tab.
	 * @param parent
	 * @return
	 */
	public static Control createJDKGrp(Composite parent) {
		// JDK container
		Composite containerJDK = createContainer(parent);
		Group jdkEmlGrp = createGroup(
				containerJDK, 3, Messages.emltrGrp);

		// JDK Checkbox
		jdkCheckBtn = createCheckButton(jdkEmlGrp,
				Messages.dplPageJdkChkBtn);

		// JDK Directory
		txtJdk = new Text(jdkEmlGrp, SWT.LEFT | SWT.BORDER);
		GridData groupGridData = new GridData();
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.horizontalSpan = 2;
		groupGridData.horizontalIndent = 20;
		groupGridData.widthHint = 330;
		groupGridData.horizontalAlignment = SWT.FILL;
		txtJdk.setLayoutData(groupGridData);

		// JDK Browse button
		btnJdkLoc = new Button(jdkEmlGrp, SWT.PUSH | SWT.CENTER);
		groupGridData = new GridData();
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridData.grabExcessHorizontalSpace = true;
		btnJdkLoc.setText(Messages.dbgBrowseBtn);
		btnJdkLoc.setLayoutData(groupGridData);

		// JDK's Deploy from download group
		createDownloadJdkGrp(containerJDK);

		return containerJDK;
	}

	/**
	 * Method creates all components
	 * required for Server tab.
	 * @param parent
	 * @return
	 */
	public static Control createServerGrp(Composite parent) {
		// Server container
		Composite containerSrv = createContainer(parent);
		Group srvEmGrp = createGroup(
				containerSrv, 3, Messages.emltrGrp);

		// Server checkbox
		serCheckBtn = createCheckButton(srvEmGrp,
				Messages.dplPageSerChkBtn);

		txtDir = new Text(srvEmGrp, SWT.LEFT | SWT.BORDER);
		GridData groupGridData = new GridData();
		groupGridData.horizontalSpan = 2;
		groupGridData.horizontalIndent = 20;
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.widthHint = 330;
		txtDir.setLayoutData(groupGridData);

		btnSrvLoc = new Button(srvEmGrp, SWT.PUSH | SWT.CENTER);
		groupGridData = new GridData();
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridData.widthHint = 50;
		btnSrvLoc.setText(Messages.dbgBrowseBtn);
		btnSrvLoc.setLayoutData(groupGridData);

		// Server dropdown
		lblSelect = new Label(srvEmGrp, SWT.LEFT);
		groupGridData = new GridData();
		groupGridData.horizontalIndent = 20;
		groupGridData.horizontalAlignment = SWT.FILL;
		lblSelect.setText(Messages.dplDlgSelLbl);
		lblSelect.setLayoutData(groupGridData);

		comboServer = createCombo(srvEmGrp, false);

		custLink = createLink(srvEmGrp,
				Messages.dplDlgSerBtn, false);

		// Server's Deploy from download group
		createDownloadSrvGrp(containerSrv);

		return containerSrv;

	}

	/**
	 * Method creates all components
	 * required for Application tab.
	 * @param parent
	 * @return
	 */
	public static Control createAppTbl(Composite parent) {
		Composite appContainer = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        appContainer.setLayout(gridLayout);
        appContainer.setLayoutData(gridData);

		tblApp = new Table(appContainer, SWT.BORDER
				| SWT.FULL_SELECTION);
		tblApp.setHeaderVisible(true);
		tblApp.setLinesVisible(true);
		gridData = new GridData();
		gridData.heightHint = 300;
		gridData.horizontalIndent = 2;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;

		tblApp.setLayoutData(gridData);

		colName = new TableColumn(tblApp, SWT.FILL);
		colName.setText(Messages.dplPageNameLbl);
		colName.setWidth(350);

		tableViewer = new TableViewer(tblApp);
		//Composite for buttons
		final Composite containerRoleBtn =
				new Composite(appContainer, SWT.NONE);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.END;
		gridData.verticalAlignment = GridData.BEGINNING;
		containerRoleBtn.setLayout(gridLayout);
		containerRoleBtn.setLayoutData(gridData);

		btnAdd = new Button(containerRoleBtn, SWT.PUSH);
		btnAdd.setText(Messages.rolsAddBtn);
		gridData = new GridData();
		gridData.widthHint = 95;
		gridData.horizontalAlignment = SWT.FILL;
		btnAdd.setLayoutData(gridData);

		btnRemove = new Button(containerRoleBtn, SWT.PUSH);
		btnRemove.setText(Messages.rolsRemoveBtn);
		btnRemove.setEnabled(false);
		gridData = new GridData();
		gridData.widthHint = 95;
		gridData.horizontalAlignment = SWT.FILL;
		btnRemove.setLayoutData(gridData);

		return appContainer;
	}

	/**
	 * Method creates JDK's deploy from download group.
	 * @param parent
	 */
	public static void createDownloadJdkGrp(Composite parent) {
		dlJdkGrp = createGroup(parent, 3, Messages.dlgDownloadGrp);
		autoDlRdCldBtn = createRadioButton(dlJdkGrp,
				Messages.autoDlJdkCldRdBtnLbl);
		thrdPrtJdkBtn = createRadioButton(dlJdkGrp,
				Messages.thrdPrtJdkLbl);
		thrdPrtJdkCmb = createThirdPartyJdkCombo(dlJdkGrp);
		thrdPrtJdkLink = createThirdPartyJdkLink(
				dlJdkGrp, Messages.dplDlgSerBtn);
		dlRdCldBtn = createRadioButton(dlJdkGrp,
				Messages.cldRdBtnLbl);
		lblUrl = createUrlComponentLbl(dlJdkGrp);
		txtUrl = createUrlComponentTxt(dlJdkGrp);
		lblDlNoteUrl = createDlNoteLabel(dlJdkGrp,
				Messages.dlgDlNtLblUrl);
		lblKey = createComponentLbl(dlJdkGrp,
				Messages.dlgDlStrgAcc);
		cmbStrgAccJdk = createCombo(dlJdkGrp, true);
		accLinkJdk = createLink(dlJdkGrp, Messages.linkLblAcc, true);
		lblJavaHome = createComponentLbl(dlJdkGrp,
				Messages.lblJavaHome);
		txtJavaHome = createComponentTxt(dlJdkGrp);
		new Link(dlJdkGrp, SWT.NO);
		setEnableDlGrp(false, false);
	}

	/**
	 * Method creates Server's deploy from download group.
	 * @param parent
	 */
	public static void createDownloadSrvGrp(Composite parent) {
		dlSrvGrp = createGroup(parent, 3, Messages.dlgDownloadGrp);
		autoDlRdCldBtnSrv = createRadioButton(dlSrvGrp,
				Messages.autoDlSrvCldRdBtnLbl);
		dlRdCldBtnSrv = createRadioButton(dlSrvGrp,
				Messages.cldRdBtnLbl);
		lblUrlSrv = createUrlComponentLbl(dlSrvGrp);
		txtUrlSrv = createUrlComponentTxt(dlSrvGrp);
		lblDlNoteUrlSrv = createDlNoteLabel(dlSrvGrp,
				Messages.dlNtLblUrlSrv);
		lblKeySrv = createComponentLbl(dlSrvGrp,
				Messages.dlgDlStrgAcc);
		cmbStrgAccSrv = createCombo(dlSrvGrp, true);
		accLinkSrv = createLink(dlSrvGrp, Messages.linkLblAcc, true);
		lblHomeDir = createComponentLbl(dlSrvGrp,
				Messages.lblHmDir);
		txtHomeDir = createComponentTxt(dlSrvGrp);
		new Link(dlSrvGrp, SWT.NO);
		setEnableDlGrpSrv(false, false);
	}

	/**
	 * Method creates container.
	 * @param parent
	 * @return
	 */
	public static Composite createContainer(Composite parent){
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		container.setLayout(gridLayout);
		container.setLayoutData(gridData);
		return container;
	}

	/**
	 * Method creates check box.
	 * @param parent
	 * @param lblText
	 * @return
	 */
	public static Button createCheckButton(Composite parent,
			String lblText) {
		Button checkBtn = new Button(parent, SWT.CHECK);
		GridData groupGridData = new GridData();
		groupGridData = new GridData();
		groupGridData.horizontalSpan = 3;
		groupGridData.horizontalAlignment = SWT.FILL;
		checkBtn.setText(lblText);
		checkBtn.setLayoutData(groupGridData);
		return checkBtn;
	}

	/**
	 * Method creates deploy from download group.
	 * @param parent
	 * @return
	 */
	public static Group createGroup(Composite parent,
			int numCol, String text) {
		Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
		GridLayout groupGridLayout = new GridLayout();
		GridData groupGridData = new GridData();
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.verticalIndent = 15;
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridData.horizontalSpan = 3;
		groupGridLayout.numColumns = numCol;
		groupGridLayout.verticalSpacing = 10;
		group.setText(text);
		group.setLayout(groupGridLayout);
		group.setLayoutData(groupGridData);
		return group;
	}

	/**
	 * Method creates deploy from download group's check box.
	 * @param parent
	 */
	public static Button createDplFrmDlChk(Composite parent) {
		Button chkButton = new Button(parent, SWT.CHECK);
		GridData groupGridData = new GridData();
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridData.horizontalSpan = 2;
		groupGridData.grabExcessHorizontalSpace = true;
		chkButton.setText(Messages.dlgDlChkTxt);
		chkButton.setSelection(false);
		chkButton.setLayoutData(groupGridData);
		return chkButton;
	}

	/**
	 * Method creates radio button.
	 * @param parent
	 * @param text
	 * @return
	 */
	public static Button createRadioButton(Composite parent,
			String text) {
		Button radioBtn = new Button(parent, SWT.RADIO);
		GridData groupGridData = new GridData();
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridData.horizontalSpan = 3;
		groupGridData.grabExcessHorizontalSpace = true;
		radioBtn.setText(text);
		radioBtn.setLayoutData(groupGridData);
		return radioBtn;
	}

	/**
	 * Method creates read only combo box.
	 * @param parent
	 * @param lowerCmb
	 * @return
	 */
	public static Combo createCombo(Composite parent,
			boolean lowerCmb) {
		Combo combo = new Combo(parent, SWT.READ_ONLY);
		GridData groupGridData = new GridData();
		if (lowerCmb) {
			groupGridData.widthHint = 300;
			groupGridData.verticalIndent = 10;
			groupGridData.horizontalIndent = 10;
		} else {
			groupGridData.grabExcessHorizontalSpace = true;
			groupGridData.horizontalAlignment = SWT.FILL;
		}
		combo.setLayoutData(groupGridData);
		return combo;
	}

	/**
	 * Method creates third party JDK combo box.
	 * @param parent
	 * @return
	 */
	public static Combo createThirdPartyJdkCombo(Composite parent) {
		Combo combo = new Combo(parent, SWT.READ_ONLY);
		GridData groupGridData = new GridData();
		groupGridData.horizontalSpan = 2;
		groupGridData.horizontalIndent = 17;
		groupGridData.widthHint = 382;
		combo.setLayoutData(groupGridData);
		return combo;
	}

	/**
	 * Method creates link.
	 * @param parent
	 * @param text
	 * @param lower
	 * @return
	 */
	public static Link createLink(Composite parent, String text,
			boolean lower) {
		Link link = new Link(parent, SWT.LEFT);
		GridData gridData = new GridData();
		if (lower) {
			gridData.horizontalIndent = 10;
			gridData.verticalIndent = 10;
		} else {
			gridData.horizontalIndent = 15;
			gridData.horizontalAlignment = SWT.FILL;
			gridData.grabExcessHorizontalSpace = true;
		}
		link.setLayoutData(gridData);
		link.setText(text);
		return link;
	}

	/**
	 * Method creates third party JDK link.
	 * @param parent
	 * @param text
	 * @return
	 */
	public static Link createThirdPartyJdkLink(Composite parent,
			String text) {
		Link link = new Link(parent, SWT.LEFT);
		GridData gridData = new GridData();
		gridData.horizontalIndent = 10;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		link.setLayoutData(gridData);
		link.setText(text);
		return link;
	}

	/**
	 * Method creates deploy from download group's URL component label.
	 * @param group
	 * @return
	 */
	public static Label createUrlComponentLbl(Composite group) {
		Label label = new Label(group, SWT.LEFT);
		GridData groupGridData = new GridData();
		label.setText(Messages.dlgDlUrlLbl);
		label.setLayoutData(groupGridData);
		return label;
	}

	/**
	 * Method creates deploy from download group's URL component text.
	 * @param group
	 * @return
	 */
	public static Text createUrlComponentTxt(Composite group) {
		Text text = new Text(group, SWT.LEFT | SWT.BORDER);
		GridData groupGridData = new GridData();
		groupGridData.horizontalIndent = 10;
		groupGridData.widthHint = 400;
		groupGridData.horizontalSpan = 2;
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridData.grabExcessHorizontalSpace = true;
		text.setLayoutData(groupGridData);
		return text;
	}

	/**
	 * Method creates deploy from download
	 * group's Access key component label.
	 * @param group
	 * @return
	 */
	public static Label createComponentLbl(Composite group, String lblStr) {
		Label label = new Label(group, SWT.LEFT);
		GridData groupGridData = new GridData();
		groupGridData.verticalIndent = 10;
		label.setText(lblStr);
		label.setLayoutData(groupGridData);
		return label;
	}

	/**
	 * Method creates deploy from download
	 * group's Access key component text.
	 * @param group
	 * @return
	 */
	public static Text createComponentTxt(Composite group) {
		Text text = new Text(group, SWT.LEFT | SWT.BORDER);
		GridData groupGridData = new GridData();
		groupGridData.horizontalIndent = 10;
		groupGridData.verticalIndent = 10;
		groupGridData.widthHint = 314;
		text.setLayoutData(groupGridData);
		return text;
	}

	/**
	 * Method creates deploy from download group's note.
	 * @param group
	 * @param text
	 * @return
	 */
	public static Label createDlNoteLabel(Composite group, String text) {
		// creating a temporary label and setting it to not visible because
		// note's indentation is not working properly in different
		// resolutions.So added this
		// dummy label for indentation to work properly.
		Label lblTemp = new Label(group, SWT.LEFT);
		GridData groupGridData = new GridData();
		lblTemp.setText("Label");
		lblTemp.setLayoutData(groupGridData);
		lblTemp.setVisible(false);

		Label label = new Label(group, SWT.LEFT);
		groupGridData = new GridData();
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridData.horizontalIndent = 10;
		groupGridData.horizontalSpan = 2;
		groupGridData.grabExcessHorizontalSpace = true;
		label.setText(text);
		label.setLayoutData(groupGridData);
		return label;
	}

	/**
	 * Enable or disable components of JDK group according to status.
	 * @param status
	 */
	public static void setEnableJDK(boolean status){
		jdkCheckBtn.setSelection(status);
		txtJdk.setEnabled(status);
		btnJdkLoc.setEnabled(status);
		if (!status) {
			txtJdk.setText("");
		}
	}

	/**
	 * Enable or disable components of Server group according to status.
	 * @param status
	 */
	public static void setEnableServer(boolean status) {
		serCheckBtn.setEnabled(status);
		comboServer.setEnabled(status);
		lblSelect.setEnabled(status);
		custLink.setEnabled(status);
		btnSrvLoc.setEnabled(status);
		txtDir.setEnabled(status);
		tblApp.setEnabled(status);
		btnAdd.setEnabled(status);
		if (!status) {
			serCheckBtn.setSelection(status);
			comboServer.removeAll();
			txtDir.setText("");
			btnRemove.setEnabled(status);
		}
	}

	/**
	 * Enable or disable components of
	 * JDK download group according to status.
	 * @param status
	 */
	public static void setEnableDlGrp(boolean status, boolean applyAutoUlParams) {
		dlRdCldBtn.setEnabled(status);
		autoDlRdCldBtn.setEnabled(status);
		thrdPrtJdkBtn.setEnabled(status);
		cmbStrgAccJdk.setEnabled(status);
		lblKey.setEnabled(status);
		lblUrl.setEnabled(status);
		lblDlNoteUrl.setEnabled(status);
		lblJavaHome.setEnabled(status);
		txtUrl.setEnabled(status);

		if (status && applyAutoUlParams) {
			// Always disable and auto-generate JDK url and derive Java home.
			txtUrl.setEditable(false);
			txtJavaHome.setEnabled(!status);
		} else {
			txtUrl.setEditable(true);
			txtJavaHome.setEnabled(status);
		}

		if (!status) {
			dlRdCldBtn.setSelection(false);
			autoDlRdCldBtn.setSelection(false);
			txtUrl.setText("");
			cmbStrgAccJdk.removeAll();
			txtJavaHome.setText("");
			lblDlNoteUrl.setText(Messages.dlgDlNtLblUrl);
			JdkSrvConfigListener.enableThirdPartyJdkCombo(false);
		} else {
			cmbStrgAccJdk = JdkSrvConfig.populateStrgAccComboBox(
					cmbStrgAccJdk.getText(),
					cmbStrgAccJdk,
					JDK_TXT, false);
		}
	}

	/**
	 * Enable both radio buttons of JDK
	 * cloud deployment and select local one.
	 * @param defaultSelectButton
	 */
	public static void enableJdkRdButtons(Button defaultSelectButton) {
		dlRdCldBtn.setEnabled(true);
		autoDlRdCldBtn.setEnabled(true);
		thrdPrtJdkBtn.setEnabled(true);
		defaultSelectButton.setSelection(true);
	}

	/**
	 * Enable or disable components of
	 * Server download group according to status.
	 * @param status
	 */
	public static void setEnableDlGrpSrv(boolean status, boolean applyAutoUlParams) {
		dlRdCldBtnSrv.setEnabled(status);
		autoDlRdCldBtnSrv.setEnabled(status);
		cmbStrgAccSrv.setEnabled(status);
		lblKeySrv.setEnabled(status);
		lblUrlSrv.setEnabled(status);
		lblDlNoteUrlSrv.setEnabled(status);
		lblHomeDir.setEnabled(status);
		txtUrlSrv.setEnabled(status);

		if (status && applyAutoUlParams) {
			txtUrlSrv.setEditable(false);
			txtHomeDir.setEnabled(!status);
		} else {
			txtUrlSrv.setEditable(true);
			txtHomeDir.setEnabled(status);
		}
		if (!status) {
			dlRdCldBtnSrv.setSelection(false);
			autoDlRdCldBtnSrv.setSelection(false);
			txtUrlSrv.setText("");
			cmbStrgAccSrv.removeAll();
			txtHomeDir.setText("");
			lblDlNoteUrlSrv.setText(Messages.dlNtLblUrlSrv);
		} else {
			cmbStrgAccSrv = JdkSrvConfig.populateStrgAccComboBox(
					cmbStrgAccSrv.getText(),
					cmbStrgAccSrv,
					SRV_TXT, false);
		}
	}

	/**
	 * Enable both radio buttons of server
	 * cloud deployment and select local one.
	 * @param defaultSelectButton
	 */
	public static void enableSrvRdButtons(Button defaultSelectButton) {
		dlRdCldBtnSrv.setEnabled(true);
		autoDlRdCldBtnSrv.setEnabled(true);
		defaultSelectButton.setSelection(true);
	}

	/**
	 * Method initializes storage account list
	 * and populates in combo box.
	 * @param valToSet
	 * @param combo
	 * @param tabControl
	 * @param needAuto
	 * If its caching page, we need auto even though
	 * tabControl is null
	 * @return
	 */
	public static Combo populateStrgAccComboBox(
			String valToSet, Combo combo,
			String tabControl, boolean needAuto) {
		accNames = getStrgAccoNamesAsPerTab(tabControl, needAuto);
		combo.setItems(accNames);
		/*
		 * If value to set is not present
		 * then set it to none.
		 */
		if (valToSet == null
				||  valToSet.isEmpty()
				|| !Arrays.asList(accNames).
				contains(valToSet)) {
			combo.setText(accNames[0]);
		} else {
			combo.setText(valToSet);
		}
		return combo;
	}

	/**
	 * Method prepares storage account name list
	 * as per current tab.
	 * If no tab = null, then page is other than serverconfiguration
	 * then add (none)
	 * If tab is JDK or server, then add (none) or (auto)
	 * as per selection of radio buttons.
	 * If auto upload or third party JDK radio button selected --> Add (auto)
	 * if not --> Add (none)
	 * @param tabControl
	 * @param needAuto
	 * If its caching page, we need auto even though
	 * tabControl is null
	 * @return
	 */
	private static String[] getStrgAccoNamesAsPerTab(String tabControl,
			boolean needAuto) {
		if (tabControl == null) {
			if (needAuto) {
				accNames = StorageRegistryUtilMethods.
						getStorageAccountNames(true);
			} else {
				accNames = StorageRegistryUtilMethods.
						getStorageAccountNames(false);
			}
		} else {
			// For JDK
			if (JDK_TXT.equals(tabControl)) {
				/*
				 * (auto) storage account is needed for
				 * auto upload as well as third party JDK
				 */
				accNames = StorageRegistryUtilMethods.
						getStorageAccountNames(autoDlRdCldBtn.getSelection()
								|| thrdPrtJdkBtn.getSelection());
			} else if (SRV_TXT.equals(tabControl)) {
				accNames = StorageRegistryUtilMethods.
						getStorageAccountNames(autoDlRdCldBtnSrv.getSelection());
			}
		}
		return accNames;
	}

	/**
	 * Listener for Accounts link.
	 * Method will open storage accounts preference page
	 * and will update storage account combo box.
	 * @param btn
	 * @param combo
	 * @param tabControl
	 * @return
	 */
	public static Combo openAccLink(Button btn, Combo combo, String tabControl) {
		Combo updatedCmb = combo;
		Object storageAcc = new StorageAccountsPreferencePage();
		WAEclipseHelper.
		openPropertyPageDialog(
				com.persistent.util.Messages.cmhIdStrgAcc,
				com.persistent.util.Messages.cmhLblStrgAcc,
				storageAcc);
		/*
		 * Update data in every case.
		 * No need to check which button (OK/Cancel)
		 * has been pressed as change is permanent
		 * even though user presses cancel
		 * according to functionality.
		 */
		/*
		 * store old value which was selected
		 * previously so that we can populate
		 * the same later.
		 */
		if (btn.getSelection()) {
			String cmbName = combo.getText();
			String accPgName =
					StorageAccountsPreferencePage.getSelIndexValue();
			String finalNameToSet;
			/*
			 * If row selected on preference page.
			 * set combo box to it always.
			 * Else keep combo box's previous value
			 * as it is.
			 */
			if (accPgName != NONE_TXT
					&& accPgName != AUTO_TXT) {
				finalNameToSet = accPgName;
			} else {
				finalNameToSet = cmbName;
			}
			// update storage account combo box
			if (finalNameToSet.equals(AUTO_TXT)) {
				updatedCmb = JdkSrvConfig.
						populateStrgAccComboBox(
								finalNameToSet,
								combo,
								tabControl, true);
			} else {
				updatedCmb = JdkSrvConfig.
						populateStrgAccComboBox(
								finalNameToSet,
								combo,
								tabControl, false);
			}
		}
		return updatedCmb;
	}

	/**
	 * Listener for server's Accounts link.
	 * Method will update storage account combo box
	 * of JDK as well if cloud radio button is selected.
	 */
	public static void accountsLinkOfSrvClicked() {
		cmbStrgAccSrv = openAccLink(dlRdCldBtnSrv.getSelection() ? dlRdCldBtnSrv
				: autoDlRdCldBtnSrv,
				cmbStrgAccSrv, SRV_TXT);
		/*
		 * Always update JDK combo box
		 * as at least one radio button
		 * would be always selected.
		 */
		cmbStrgAccJdk = populateStrgAccComboBox(
				cmbStrgAccJdk.getText(),
				cmbStrgAccJdk,
				JDK_TXT, false);
		/*
		 * If JDK auto button is selected then
		 * update JDK URL as we may have set
		 * combo box to (auto) by removing
		 * selected storage account from registry.
		 */
		if (autoDlRdCldBtn.getSelection()
				|| thrdPrtJdkBtn.getSelection()) {
			updateJDKDlURL();
		}
	}

	/**
	 * Listener for JDK's Accounts link.
	 * Method will update storage account combo box
	 * of server as well if cloud radio button is selected.
	 */
	public static void accountsLinkOfJdkClicked() {
		cmbStrgAccJdk = openAccLink(dlRdCldBtn.getSelection() ? dlRdCldBtn
				: autoDlRdCldBtn.getSelection() ? autoDlRdCldBtn
						: thrdPrtJdkBtn,
						cmbStrgAccJdk,
						JDK_TXT);
		/*
		 * If server auto or deploy button selected then
		 * update server combo box as well even though
		 * link on JDK tab is clicked.
		 */
		if (dlRdCldBtnSrv.getSelection() || autoDlRdCldBtnSrv.getSelection()) {
			cmbStrgAccSrv = populateStrgAccComboBox(
					cmbStrgAccSrv.getText(),
					cmbStrgAccSrv,
					SRV_TXT, false);
			/*
			 * If server auto button is selected then
			 * update server URL as we may have set
			 * combo box to (auto) by removing
			 * selected storage account from registry.
			 */
			if (autoDlRdCldBtnSrv.getSelection()) {
				updateServerDlURL();
			}
		}
	}

	/**
	 * Listener for URL text box's text change.
	 * @param url
	 * @param nameInUrl
	 * @param combo
	 * @return
	 */
	public static Combo urlModifyListner(String url,
			String nameInUrl, Combo combo) {
		String endpoint = StorageRegistryUtilMethods.
				getServiceEndpoint(url);
		String accNameToSet = accNames[0];
		if (nameInUrl != null
				&& !nameInUrl.isEmpty()
				&& endpoint != null) {
			// check storage account name present in list
			if (Arrays.asList(accNames).contains(nameInUrl)) {
				/*
				 * check endpoint of storage account from list
				 * and from URL matches then
				 * only select storage account otherwise select none.
				 */
				int index = Arrays.asList(accNames).indexOf(nameInUrl);
				String endpointInReg = StorageRegistryUtilMethods.
						getServiceEndpoint(StorageAccountRegistry.
								getStrgList().get(index - 1).getStrgUrl());
				if (endpoint.equalsIgnoreCase(endpointInReg)) {
					accNameToSet = nameInUrl;
				}
			} else if (StorageRegistryUtilMethods.
					isDuplicatePresent()) {
				/*
				 * If accounts with same name but
				 * different service URL exists
				 * then check concatenation of account name
				 * and service endpoint exists in list.
				 */
				String accAndUrl = StorageRegistryUtilMethods.
						getAccNmSrvcUrlToDisplay(nameInUrl, endpoint);
				if (Arrays.asList(accNames).contains(accAndUrl)) {
					accNameToSet = accAndUrl;
				}
			}
		}
		combo.setText(accNameToSet);
		return combo;
	}

	/**
	 * Listener for storage account combo box.
	 * @param combo
	 * @param urlTxt
	 * @param tabControl
	 * @return
	 */
	public static Text cmbBoxListener(
			Combo combo, Text urlTxt, String tabControl) {
		int index = combo.getSelectionIndex();
		String url = urlTxt.getText().trim();
		// check value is not none and auto.
		if (index > 0) {
			String newUrl = StorageAccountRegistry.
					getStrgList().get(index - 1).getStrgUrl();

			// For JDK tab and auto upload option selected
			if (tabControl != null && JDK_TXT.equals(tabControl)) {
				if (autoDlRdCldBtn.getSelection()) {
					urlTxt.setText(prepareCloudBlobURL(txtJdk.getText() , newUrl));
					return urlTxt;
				} else if (thrdPrtJdkBtn.getSelection()) {
					urlTxt.setText(prepareUrlForThirdPartyJdk(
							thrdPrtJdkCmb.getText(), newUrl));
					return urlTxt;
				}
			}

			// For Server and auto upload option selected
			if (tabControl != null && SRV_TXT.equals(tabControl)
					&& autoDlRdCldBtnSrv.getSelection()) {
				String value = prepareCloudBlobURL(txtDir.getText() , newUrl);
				urlTxt.setText(value);
				return urlTxt;
			}
			/*
			 * If URL is blank and new storage account selected
			 * then auto generate with storage accounts URL.
			 */
			if (url.isEmpty()) {
				urlTxt.setText(newUrl);
			} else {
				/*
				 * If storage account in combo box and URL
				 * are in sync then update
				 * corresponding portion of the URL
				 * with the URI of the newly selected storage account
				 * (leaving the container and blob name unchanged.
				 */
				String oldVal = StorageRegistryUtilMethods.
						getSubStrAccNmSrvcUrlFrmUrl(url);
				String newVal = StorageRegistryUtilMethods.
						getSubStrAccNmSrvcUrlFrmUrl(newUrl);
				urlTxt.setText(url.replaceFirst(oldVal, newVal));
			}
		} else if (index == 0) {
			// index = 0 means none or auto is selected
			// For JDK tab and auto upload option selected
			if (tabControl != null && JDK_TXT.equals(tabControl)
					&& (autoDlRdCldBtn.getSelection()
							|| thrdPrtJdkBtn.getSelection())) {
				urlTxt.setText(AUTO_TXT);
				return urlTxt;
			}
			// For Server and auto upload option selected
			if (tabControl != null && SRV_TXT.equals(tabControl)
					&& autoDlRdCldBtnSrv.getSelection()) {
				urlTxt.setText(AUTO_TXT);
				return urlTxt;
			}
		}
		return urlTxt;
	}

	/**
	 * This API appends eclipse container name and filename to url
	 * in order to construct blob url.
	 * @param filePath
	 * @param newUrl
	 * @return
	 */
	public static String prepareCloudBlobURL(String filePath,
			String newUrl) {
		if ((filePath == null || filePath.length() == 0)
			|| (newUrl == null || newUrl.length() == 0)) {
			return "";
		}

		File jdkPath = new File(filePath);
		return new StringBuilder(newUrl).append(Messages.eclipseDeployContainer)
											.append(FWD_SLASH)
											.append(jdkPath.getName().trim().replaceAll("\\s+", "-"))
											.append(".zip").toString();
	}

	/**
	 * This API appends eclipse container name and filename to url
	 * in order to construct blob url.
	 * @param asName
	 * @param url
	 * @return
	 */
	public static String prepareUrlForApp(String asName,
			String url) {
		if ((asName == null || asName.length() == 0)
				|| (url == null || url.length() == 0)) {
			return "";
		}

		return new StringBuilder(url)
		.append(Messages.eclipseDeployContainer)
		.append(FWD_SLASH)
		.append(asName).toString();
	}

	/**
	 * Method prepares third party JDK URL
	 * by appending eclipse container name and
	 * filename from third party URL.
	 * @param url
	 * @return
	 */
	public static String prepareUrlForThirdPartyJdk(String jdkName, String url) {
		String finalUrl = "";
		try {
			String cloudValue = WindowsAzureProjectManager.
					getCloudValue(jdkName, cmpntFile);
			String dirName = cloudValue.substring(cloudValue.lastIndexOf("\\") + 1,
					cloudValue.length());
			finalUrl = new StringBuilder(url)
			.append(Messages.eclipseDeployContainer)
			.append(FWD_SLASH)
			.append(dirName)
			.append(".zip").toString();
		} catch (Exception ex) {
			Activator.getDefault().log(ex.getMessage());
		}
		return finalUrl;
	}

	/**
	 * Method returns access key from storage registry
	 * according to account name selected in combo box.
	 * @param combo
	 * @return
	 */
	public static String getAccessKey(Combo combo) {
		String key = "";
		// get access key.
		int strgAccIndex = combo.getSelectionIndex();
		if (strgAccIndex > 0
				&& !combo.getText().isEmpty()) {
			key = StorageAccountRegistry.
					getStrgList().get(strgAccIndex - 1).
					getStrgKey();
		}
		return key;
	}

	/**
	 * Method returns blob endpoint URL from storage registry
	 * according to account name selected in combo box.
	 * @param combo
	 * @return
	 */
	public static String getBlobEndpointUrl(Combo combo) {
		String url = "";
		int strgAccIndex = combo.getSelectionIndex();
		if (strgAccIndex > 0
				&& !combo.getText().isEmpty()) {
			url = StorageAccountRegistry.
					getStrgList().get(strgAccIndex - 1).
					getStrgUrl();
		}
		return url;
	}

	/**
	 * Method returns URL from storage registry
	 * according to account name selected in combo box.
	 * @param combo
	 * @return
	 */
	public static String getUrl(Combo combo) {
		int index = combo.getSelectionIndex();
		String url = "";
		if (index != 0) {
			url = StorageAccountRegistry.
					getStrgList().get(index - 1).getStrgUrl();
		}
		return url;
	}

	/**
	 * Method populates storage account name associated
	 * with the component's access key.
	 * @param key
	 * @param combo
	 * @return
	 */
	public static Combo populateStrgNameAsPerKey(String key,
			Combo combo) {
		boolean isSet = false;
		String accName = accNames[0];
		if (key != null) {
			// get index of account which has matching access key
			int index = StorageRegistryUtilMethods.
					getStrgAccIndexAsPerKey(key);
			if (index >= 0) {
				StorageAccount account = StorageAccountRegistry.
						getStrgList().get(index);
				accName = account.getStrgName();
				// check storage account name present in list
				if (Arrays.asList(accNames).contains(accName)) {
					isSet = true;
				} else if (StorageRegistryUtilMethods.
						isDuplicatePresent()) {
					/*
					 * If accounts with same name but
					 * different service URL exists
					 * then check concatenation of account name
					 * and service endpoint exists in list.
					 */
					String endpoint = StorageRegistryUtilMethods.
							getServiceEndpoint(account.getStrgUrl());
					String accAndUrl = StorageRegistryUtilMethods.
							getAccNmSrvcUrlToDisplay(accName, endpoint);
					if (Arrays.asList(accNames).contains(accAndUrl)) {
						accName = accAndUrl;
						isSet = true;
					}
				}
			}
		}
		if (isSet) {
			combo.setText(accName);
		} else {
			combo.setText(accNames[0]);
		}
		return combo;
	}

	/**
	 * API to determine if storage account is selected or not in JDK tab
	 * @return true if storage account is selected in JDK tab else false.
	 */
	public static boolean isSASelectedForJDK() {
		return !NONE_TXT.equals(cmbStrgAccJdk.getText());
	}

	/**
	 * API to determine if storage account is selected or not in Server tab
	 * @return true if storage account is selected in Server tab else false.
	 */
	public static boolean isSASelectedForSrv() {
		 return !NONE_TXT.equals(cmbStrgAccSrv.getText());
	}

	/**
	 * Method will check if JDK storage account combo box
	 * is set to valid value other than none
	 * then while selecting auto upload option
	 * for server, it will populate
	 * storage account name selected for JDK
	 * in server combo box.
	 */
	public static void populateDefaultStrgAccForSrvAuto() {
		int jdkIndex = cmbStrgAccJdk.getSelectionIndex();
		int srvIndex = cmbStrgAccSrv.getSelectionIndex();
		/*
		 * JDK storage account combo box is enabled
		 * and account selected is other than (none).
		 * Also check storage account for server
		 * is not specified already then only change.
		 */
		if (jdkIndex > 0
				&& !(srvIndex > 0)) {
			cmbStrgAccSrv.select(jdkIndex);
		}
	}

	/**
	 * Method to update JDK cloud URL.
	 * Will get updated as per storage account
	 * combo box and radio button selection.
	 */
	public static void updateJDKDlURL() {
		if (isSASelectedForJDK()) {
			setTxtUrl(cmbBoxListener(
					getCmbStrgAccJdk(),
					getTxtUrl(), JDK_TXT));
		} else if (!getDlRdCldBtn().getSelection()) {
			getTxtUrl().setText("");
		}
	}

	/**
	 * Method to update server cloud URL.
	 * Will get updated as per storage account
	 * combo box and radio button selection.
	 */
	public static void updateServerDlURL() {
		if (isSASelectedForSrv()) {
			setTxtUrlSrv(cmbBoxListener(
					getCmbStrgAccSrv(),
					getTxtUrlSrv(),
					SRV_TXT));
		} else if (!getDlRdCldBtnSrv().getSelection()) {
			getTxtUrlSrv().setText("");
		}
	}

	/**
	 * Utility method to update note below text box for JDK.
	 */
	public static void updateJDKDlNote(String label) {
		// Update note below URL text box
		String jdkPath = getTxtJdk().
				getText();
		File file = new File(jdkPath);
		if (!jdkPath.isEmpty() && file.exists()) {
			String dirName = file.getName();
			getLblDlNoteUrl().setText(String.format(
					label, dirName));
		}
	}

	/**
	 * Utility method to update note below text box for Server.
	 */
	public static void updateSrvDlNote(String label) {
		// Update note below server URL text box
		String srvPath = getTxtDir().getText();
		File file = new File(srvPath);
		if (!srvPath.isEmpty() && file.exists()) {
			String dirName = file.getName();
			getLblDlNoteUrlSrv().setText(String.format(
					label, dirName));
		}
	}

	/**
	 * Utility method to update java home value.
	 */
	public static void updateJDKHome(WindowsAzureRole role) {
		try {
			String jdkPath = getTxtJdk().getText();
			String jdkHome = role.constructJdkHome(jdkPath,
					cmpntFile);
			getTxtJavaHome().setText(jdkHome);
		} catch (WindowsAzureInvalidProjectOperationException e) {
			Activator.getDefault().log(e.getMessage());
		}
	}

	/**
	 * Utility method to update server home value.
	 */
	public static void updateServerHome(WindowsAzureRole role) {
		// set server home directory text box value
		String srvPath = getTxtDir().getText();
		try {
			String srvHome = role.constructServerHome(
					getComboServer().getText(),
					srvPath,
					cmpntFile);
			getTxtHomeDir().setText(srvHome);
		} catch (WindowsAzureInvalidProjectOperationException e) {
			Activator.getDefault().log(e.getMessage());
		}
	}

	/**
	 * Return whether Server auto download group
	 * check box is checked or not.
	 * @return
	 */
	public static boolean isSrvAutoUploadChecked() {
		return getAutoDlRdCldBtnSrv().getSelection();
	}

	/**
	 * Return whether Server download group
	 * check box is checked or not.
	 * @return
	 */
	public static boolean isSrvDownloadChecked() {
		return getDlRdCldBtnSrv().getSelection();
	}

	/**
	 * Listener for JDK browse button it is used in file system button.
	 * It will open the file system location.
	 */
	public static String utilJdkBrowseBtnListener(String label) {
		String directory = null;
		try {
			String oldTxt = getTxtJdk().getText();
			String path = WAEclipseHelper.
					jdkDefaultDirectory(oldTxt);
			DirectoryDialog dialog =
					new DirectoryDialog(new Shell());
			if (path != null) {
				File file = new File(path);
				if (!path.isEmpty()
						&& file.exists()
						&& file.isDirectory()) {
					dialog.setFilterPath(path);
				}
			}

			directory = dialog.open();
			if (directory != null
					&& !directory.equalsIgnoreCase(oldTxt)) {
				getTxtJdk().setText(directory);
				// Update note below URL text box
				if (getDlRdCldBtn().getSelection()) {
					String dirName = new File(directory).getName();
					getLblDlNoteUrl().
					setText(String.format(
							label, dirName));
				}
			}
		} catch (Exception e) {
			Activator.getDefault().log(e.getMessage(), e);
		}
		return directory;
	}

	/**
	 * Server directory browse button listener.
	 * @param label
	 * @return
	 */
	public static String utilSerBrowseBtnListener(String label) {
		String directory = null;
		try {
			String path = getTxtDir().getText();
			DirectoryDialog dialog =
					new DirectoryDialog(new Shell());
			if (path != null) {
				File file = new File(path);
				if (!path.isEmpty()
						&& file.exists()
						&& file.isDirectory()) {
					dialog.setFilterPath(path);
				}
			}
			directory = dialog.open();
			if (directory != null) {
				getTxtDir().setText(directory);
				// Update note below server URL text box
				if (isSrvDownloadChecked()) {
					String dirName = new File(directory).getName();
					getLblDlNoteUrlSrv().
					setText(String.format(
							label, dirName));
				}
				// Auto detect server family
				String serverName = WAEclipseHelper.
						detectServer(new File(directory));
				if (serverName != null
						&& !serverName.isEmpty()) {
					getComboServer().
					setText(serverName);
				} else {
					getComboServer().
					clearSelection();
				}
			}
		} catch (Exception e) {
			Activator.getDefault().log(e.getMessage(), e);
		}
		return directory;
	}

	/**
	 * Customize Link listener. This will close the wizard
	 * or property page and open the
	 * componentssets.xml in default editor.
	 */
	public static void custLinkListener(
			String ttl, String msg,
			boolean isWizard, Shell shell,
			Object pageObj, File file) {
		boolean choice = MessageDialog.openConfirm(shell,
				ttl, msg);
		if (choice) {
			try {
				if (isWizard) {
					WAProjectWizard wiz =
							(WAProjectWizard) pageObj;
					wiz.getShell().close();
				} else {
					shell.close();
				}
				if (file.exists() && file.isFile()) {
					IFileStore store = EFS.getLocalFileSystem().
							getStore(file.toURI());
					IWorkbenchPage benchPage = PlatformUI.
							getWorkbench().getActiveWorkbenchWindow()
							.getActivePage();
					IDE.openEditorOnFileStore(benchPage, store);
				}
			} catch (PartInitException e) {
				Activator.getDefault().log(e.getMessage(), e);
			}
		}
	}

	/**
	 * Returns true if auto upload is selected for JDK else false.
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public static boolean isJDKAutoUploadPrevSelected(WindowsAzureRole role)
			throws WindowsAzureInvalidProjectOperationException {
		WARoleComponentCloudUploadMode uploadMode =
				role.getJDKCloudUploadMode();
		if (uploadMode != null
				&& uploadMode.equals(WARoleComponentCloudUploadMode.auto)) {
			return true;
		}
		return false;
	}

	/**
	 * Returns true if auto upload is selected for Server else false.
	 * @return
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public static boolean isServerAutoUploadPrevSelected(WindowsAzureRole role)
			throws WindowsAzureInvalidProjectOperationException {
		WARoleComponentCloudUploadMode uploadMode =
				role.getServerCloudUploadMode();
		if (uploadMode != null
				&& uploadMode.equals(WARoleComponentCloudUploadMode.auto)) {
			return true;
		}
		return false;
	}

	/**
	 * Display third party JDK license agreement.
	 * @return : boolean
	 * true : license accepted "Accept" button pressed
	 * false : license not accepted "Cancel" button pressed
	 */
	public static boolean createAccLicenseAggDlg() {
		boolean licenseAccepted = false;
		AcceptLicenseAgreementDlg dlg =
				new AcceptLicenseAgreementDlg(new Shell());
		int btnId = dlg.open();
		if (btnId == Window.OK) {
			licenseAccepted = true;
		}
		return licenseAccepted;
	}
}
