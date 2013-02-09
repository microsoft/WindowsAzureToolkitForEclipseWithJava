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
	private static Text txtKey;
	private static Button dlCheckBtn;
	private static Label lblUrl;
	private static Label lblKey;
	private static Label lblDlNote;
	private static Label lblDlNoteUrl;
	private static Label lblJavaHome;
	private static Text txtJavaHome;
	// Variables for Server Download group
	private static Group dlSrvGrp;
	private static Text txtUrlSrv;
	private static Text txtKeySrv;
	private static Button dlCheckBtnSrv;
	private static Label lblUrlSrv;
	private static Label lblKeySrv;
	private static Label lblDlNoteSrv;
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

	/*
	 * Getter methods for UI components.
	 */

	public static Button getJdkCheckBtn() {
		return jdkCheckBtn;
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

	public static Button getDlCheckBtn() {
		return dlCheckBtn;
	}

	public static Text getTxtUrl() {
		return txtUrl;
	}

	public static Text getTxtKey() {
		return txtKey;
	}

	public static Button getDlCheckBtnSrv() {
		return dlCheckBtnSrv;
	}

	public static Text getTxtUrlSrv() {
		return txtUrlSrv;
	}

	public static Text getTxtKeySrv() {
		return txtKeySrv;
	}
	
	public static Label getLblDlNoteUrl() {
		return lblDlNoteUrl;
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

		comboServer = new Combo(containerSrv, SWT.READ_ONLY);
		groupGridData = new GridData();
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.horizontalAlignment = SWT.FILL;
		comboServer.setLayoutData(groupGridData);

		custLink = new Link (containerSrv, SWT.CENTER);
		groupGridData = new GridData();
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.horizontalIndent = 15;
		custLink.setText(Messages.dplDlgSerBtn);
		custLink.setLayoutData(groupGridData);

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
		dlCheckBtn = createDplFrmDlChk(dlJdkGrp);
		lblUrl = createUrlComponentLbl(dlJdkGrp);
		txtUrl = createUrlComponentTxt(dlJdkGrp);
		lblDlNoteUrl = createDlNoteLabel(dlJdkGrp,
				Messages.dlgDlNtLblUrl);
		lblKey = createComponentLbl(dlJdkGrp,
				Messages.dlgDlAccessKey);
		txtKey = createComponentTxt(dlJdkGrp);
		lblDlNote = createDlNoteLabel(dlJdkGrp,
				Messages.dlgDlNoteLbl);
		lblJavaHome = createComponentLbl(dlJdkGrp,
				Messages.lblJavaHome);
		txtJavaHome = createComponentTxt(dlJdkGrp);
		setEnableDlGrp(false);
	}

	/**
	 * Method creates Server's deploy from download group.
	 * @param parent
	 */
	public static void createDownloadSrvGrp(Composite parent) {
		dlSrvGrp = createGroup(parent);
		dlCheckBtnSrv = createDplFrmDlChk(dlSrvGrp);
		lblUrlSrv = createUrlComponentLbl(dlSrvGrp);
		txtUrlSrv = createUrlComponentTxt(dlSrvGrp);
		lblDlNoteUrlSrv = createDlNoteLabel(dlSrvGrp,
				Messages.dlgDlNtLblUrl);
		lblKeySrv = createComponentLbl(dlSrvGrp,
				Messages.dlgDlAccessKey);
		txtKeySrv = createComponentTxt(dlSrvGrp);
		lblDlNoteSrv = createDlNoteLabel(dlSrvGrp,
				Messages.dlgDlNoteLbl);
		lblHomeDir = createComponentLbl(dlSrvGrp,
				Messages.lblHmDir);
		txtHomeDir = createComponentTxt(dlSrvGrp);
		setEnableDlGrpSrv(false);
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
		groupGridLayout.numColumns = 2;
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
		groupGridData.widthHint = 400;
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridData.grabExcessHorizontalSpace = true;
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
	public static void setEnableDlGrp(boolean status) {
		dlCheckBtn.setEnabled(status);
		dlCheckBtn.setSelection(status);
		txtUrl.setEnabled(status);
		txtKey.setEnabled(status);
		lblKey.setEnabled(status);
		lblUrl.setEnabled(status);
		lblDlNote.setEnabled(status);
		lblDlNoteUrl.setEnabled(status);
		lblJavaHome.setEnabled(status);
		txtJavaHome.setEnabled(status);
		if (!status) {
			txtUrl.setText("");
			txtKey.setText("");
			txtJavaHome.setText("");
		}
	}

	/**
	 * Enable or disable components of
	 * Server download group according to status.
	 * @param status
	 */
	public static void setEnableDlGrpSrv(boolean status) {
		dlCheckBtnSrv.setEnabled(status);
		dlCheckBtnSrv.setSelection(status);
		txtUrlSrv.setEnabled(status);
		txtKeySrv.setEnabled(status);
		lblKeySrv.setEnabled(status);
		lblUrlSrv.setEnabled(status);
		lblDlNoteSrv.setEnabled(status);
		lblDlNoteUrlSrv.setEnabled(status);
		lblHomeDir.setEnabled(status);
		txtHomeDir.setEnabled(status);
		if (!status) {
			txtUrlSrv.setText("");
			txtKeySrv.setText("");
			txtHomeDir.setText("");
		}
	}
}
