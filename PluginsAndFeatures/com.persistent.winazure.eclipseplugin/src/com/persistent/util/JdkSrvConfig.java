/**
 * Copyright 2013 Persistent Systems Ltd.
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

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.microsoftopentechnologies.wacommon.storageregistry.StorageAccount;
import com.microsoftopentechnologies.wacommon.storageregistry.StorageAccountRegistry;
import com.microsoftopentechnologies.wacommon.storageregistry.StorageRegistryUtilMethods;
import com.persistent.ui.preference.StorageAccountsPreferencePage;

/**
 * Class creates all the UI components
 * required on JDK, Server and Application tabs
 * for windows azure deployment project creation wizard
 * and server configuration property page.
 */
public class JdkSrvConfig {
	// Variables for JDK Download group
	private static Group dlJdkGrp;
	private static Text txtUrl;
	private static Combo cmbStrgAccJdk;
	private static Link accLinkJdk;
	private static Button dlRdLocBtn;
	private static Button dlRdCldBtn;
	private static Button autoDlRdCldBtn;
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
	private static Button dlRdLocBtnSrv;
	private static Button dlRdCldBtnSrv;
	private static Button autoDlRdCldBtnSrv;
	private static Label lblUrlSrv;
	private static Label lblKeySrv;
	private static Label lblDlNoteUrlSrv;
	private static Label lblHomeDir;
	private static Text txtHomeDir;
	// Variables for JDK group
	private static Button jdkCheckBtn;
	private static Label lblJdkLoc;
	private static Button btnJdkLoc;
	private static Text txtJdk;
	// Variables for Server group
	private static Button serCheckBtn;
	private static Label lblDir;
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
	private static String[] accNames = StorageRegistryUtilMethods.
			getStorageAccountNames();
	public static final String NONE_TXT = "(none)";
	public static final String FWD_SLASH = "/";

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

	public static Button getDlRdLocBtn() {
		return dlRdLocBtn;
	}

	public static Button getDlRdCldBtn() {
		return dlRdCldBtn;
	}
	
	public static Button getAutoDlRdCldBtn() {
		return autoDlRdCldBtn;
	}

	public static Button getDlRdLocBtnSrv() {
		return dlRdLocBtnSrv;
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

	/**
	 * Method creates all components
	 * required for JDK tab.
	 * @param parent
	 * @return
	 */
	public static Control createJDKGrp(Composite parent) {
		// JDK container
		Composite containerJDK = createContainer(parent);

		// JDK Checkbox
		jdkCheckBtn = createCheckButton(containerJDK,
				Messages.dplPageJdkChkBtn);

		// JDK Directory
		lblJdkLoc = new Label(containerJDK, SWT.LEFT);
		GridData groupGridData = new GridData();
		groupGridData.horizontalAlignment = SWT.FILL;
		lblJdkLoc.setText(Messages.dplDlgJdkLbl);
		lblJdkLoc.setLayoutData(groupGridData);

		txtJdk = new Text(containerJDK, SWT.LEFT | SWT.BORDER);
		groupGridData = new GridData();
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.widthHint = 330;
		groupGridData.horizontalAlignment = SWT.FILL;
		txtJdk.setLayoutData(groupGridData);

		// JDK Browse button
		btnJdkLoc = new Button(containerJDK, SWT.PUSH | SWT.CENTER);
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

		// Server checkbox
		serCheckBtn = createCheckButton(containerSrv,
				Messages.dplPageSerChkBtn);

		// Server directory
		lblDir = new Label(containerSrv, SWT.LEFT);
		GridData groupGridData = new GridData();
		lblDir.setText(Messages.dplDlgJdkLbl);
		lblDir.setLayoutData(groupGridData);

		txtDir = new Text(containerSrv, SWT.LEFT | SWT.BORDER);
		groupGridData = new GridData();
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.widthHint = 330;
		txtDir.setLayoutData(groupGridData);

		btnSrvLoc = new Button(containerSrv, SWT.PUSH | SWT.CENTER);
		groupGridData = new GridData();
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridData.widthHint = 95;
		btnSrvLoc.setText(Messages.dbgBrowseBtn);
		btnSrvLoc.setLayoutData(groupGridData);

		// Server dropdown
		lblSelect = new Label(containerSrv, SWT.LEFT);
		groupGridData = new GridData();
		groupGridData.horizontalAlignment = SWT.FILL;
		lblSelect.setText(Messages.dplDlgSelLbl);
		lblSelect.setLayoutData(groupGridData);

		comboServer = createCombo(containerSrv, false);

		custLink = createLink(containerSrv,
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
		gridData.heightHint = 270;
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
		dlJdkGrp = createGroup(parent);
		dlRdLocBtn = createRadioButton(dlJdkGrp,
				Messages.jdkLocRdBtnLbl);
		autoDlRdCldBtn = createRadioButton(dlJdkGrp,
				Messages.autoDlJdkCldRdBtnLbl);
		dlRdCldBtn = createRadioButton(dlJdkGrp,
				Messages.jdkCldRdBtnLbl);
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
		dlSrvGrp = createGroup(parent);
		dlRdLocBtnSrv = createRadioButton(dlSrvGrp,
				Messages.srvLocRdBtnLbl);
		autoDlRdCldBtnSrv = createRadioButton(dlSrvGrp,
				Messages.autoDlSrvCldRdBtnLbl);
		dlRdCldBtnSrv = createRadioButton(dlSrvGrp,
				Messages.srvCldRdBtnLbl);
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
	public static Group createGroup(Composite parent) {
		Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
		GridLayout groupGridLayout = new GridLayout();
		GridData groupGridData = new GridData();
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.verticalIndent = 15;
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridData.horizontalSpan = 3;
		groupGridLayout.numColumns = 3;
		groupGridLayout.verticalSpacing = 10;
		group.setText(Messages.dlgDownloadGrp);
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
		lblJdkLoc.setEnabled(status);
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
		lblDir.setEnabled(status);
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
		dlRdLocBtn.setEnabled(status);
		cmbStrgAccJdk.setEnabled(status);
		lblKey.setEnabled(status);
		lblUrl.setEnabled(status);
		lblDlNoteUrl.setEnabled(status);
		lblJavaHome.setEnabled(status);
		txtUrl.setEnabled(status);
		
		if (status && applyAutoUlParams) { // Always disable and auto-generate JDK url and derive Java home.  
			txtUrl.setEditable(false);
			txtJavaHome.setEnabled(!status);
		} else {
			txtUrl.setEditable(true);
			txtJavaHome.setEnabled(status);
		}
		
		if (!status) {
			dlRdCldBtn.setSelection(false);
			autoDlRdCldBtn.setSelection(false);
			dlRdLocBtn.setSelection(false);
			txtUrl.setText("");
			cmbStrgAccJdk.removeAll();
			txtJavaHome.setText("");
			lblDlNoteUrl.setText(Messages.dlgDlNtLblUrl);
		} else {
			cmbStrgAccJdk = JdkSrvConfig.populateStrgAccComboBox(cmbStrgAccJdk.getText(),cmbStrgAccJdk);
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
		dlRdLocBtn.setEnabled(true);
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
		dlRdLocBtnSrv.setEnabled(status);
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
			dlRdLocBtnSrv.setSelection(false);
			txtUrlSrv.setText("");
			cmbStrgAccSrv.removeAll();
			txtHomeDir.setText("");
			lblDlNoteUrlSrv.setText(Messages.dlNtLblUrlSrv);
		} else {
			cmbStrgAccSrv = JdkSrvConfig.populateStrgAccComboBox(cmbStrgAccSrv.getText(),cmbStrgAccSrv);
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
		dlRdLocBtnSrv.setEnabled(true);
		defaultSelectButton.setSelection(true);
	}

	/**
	 * Method initializes storage account list
	 * and populates in combo box.
	 * @param valToSet : value to set in combo box
	 */
	public static Combo populateStrgAccComboBox(
			String valToSet, Combo combo) {
		accNames = StorageRegistryUtilMethods.
				getStorageAccountNames();
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
	 * Listener for Accounts link.
	 * Method will open storage accounts preference page
	 * and will update storage account combo box.
	 * @param btn
	 * @param combo
	 * @return
	 */
	public static Combo openAccLink(Button btn, Combo combo) {
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
			String oldName = combo.getText();
			// update storage account combo box
			updatedCmb = JdkSrvConfig.
					populateStrgAccComboBox(oldName, combo);
		}
		return updatedCmb;
	}

	/**
	 * Listener for server's Accounts link.
	 * Method will update storage account combo box
	 * of JDK as well if cloud radio button is selected.
	 */
	public static void accountsLinkOfSrvClicked() {
		cmbStrgAccSrv = openAccLink(dlRdCldBtnSrv.getSelection()? dlRdCldBtnSrv : autoDlRdCldBtnSrv,
				cmbStrgAccSrv);
		if (dlRdCldBtn.getSelection() || autoDlRdCldBtn.getSelection()) {
			cmbStrgAccJdk = populateStrgAccComboBox(
					cmbStrgAccJdk.getText(),
					cmbStrgAccJdk);
		}
	}

	/**
	 * Listener for JDK's Accounts link.
	 * Method will update storage account combo box
	 * of server as well if cloud radio button is selected.
	 */
	public static void accountsLinkOfJdkClicked() {
		cmbStrgAccJdk = openAccLink(dlRdCldBtn.getSelection() ? dlRdCldBtn : autoDlRdCldBtn,
				cmbStrgAccJdk);
		if (dlRdCldBtnSrv.getSelection() || autoDlRdCldBtnSrv.getSelection()) {
			cmbStrgAccSrv = populateStrgAccComboBox(
					cmbStrgAccSrv.getText(),
					cmbStrgAccSrv);
		}
	}

	/**
	 * Listener for URL text box's text change.
	 * @param url
	 * @param nameInUrl
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
	 * @param isCmbSetNone
	 */
	public static Text cmbBoxListener(
			Combo combo, Text urlTxt, String tabControl) {
		int index = combo.getSelectionIndex();
		String url = urlTxt.getText().trim();
		// check value is not none.
		if (index > 0) {
			String newUrl = StorageAccountRegistry.
					getStrgList().get(index - 1).getStrgUrl();
			
			// For JDK
			if(tabControl != null && "JDK".equals(tabControl) 
					&& autoDlRdCldBtn.getSelection()) {
				String value = prepareCloudBlobURL(txtJdk.getText() , newUrl);
				urlTxt.setText(value);
				return urlTxt;
			}
			
			// For Server
			if(tabControl != null && "SERVER".equals(tabControl) 
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
		}
		return urlTxt;
	}

	/**
	 * This API appends eclipse container name and filename to url
	 * @param text
	 * @param newUrl
	 * @return
	 */
	private static String prepareCloudBlobURL(String filePath, String newUrl) {
		if ( (filePath == null || filePath.length() == 0)
			|| ( newUrl == null || newUrl.length() == 0)) {
			return "";
		}

		File jdkPath = new File(filePath);
		return new StringBuilder(newUrl).append(Messages.eclipseDeployContainer)
											.append(FWD_SLASH)
											.append(jdkPath.getName().trim().replaceAll("\\s+", "-"))
											.append(".zip").toString();
	}

	/**
	 * Method returns access key from storage registry
	 * according to account name selected in combo box.
	 * @param combo
	 * @return
	 */
	public static String getAccessKey(Combo combo) {
		String key = "";
		// set access key.
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
}
