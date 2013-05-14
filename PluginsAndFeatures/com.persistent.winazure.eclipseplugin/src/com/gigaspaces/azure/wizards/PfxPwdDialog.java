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
package com.gigaspaces.azure.wizards;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.security.KeyStore;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.microsoftopentechnologies.wacommon.utils.PluginUtil;

/**
 * Creates dialog for accepting pfx password.
 */
public class PfxPwdDialog extends Dialog {
	private Button yesRadioBtn;
	private Button noRadioBtn;
	private Text txtPfxPath;
	private Button browseBtn;
	private Text passwordField;
	private Label lblPassword;
	private String certPath;
	public String password;
	public String pfxPath;
	private Button okButton;
	private boolean isNewService;

	/**
	 * Constructor.
	 * @param parentShell
	 * @param pfxPath
	 */
	protected PfxPwdDialog(Shell parentShell,
			String certPath, boolean isNewService) {
		super(parentShell);
		this.certPath = certPath;
		this.isNewService = isNewService;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.pfxInputTtl);
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		Control ctrl = super.createButtonBar(parent);
		okButton = getButton(IDialogConstants.OK_ID);
		return ctrl;
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		container.setLayout(gridLayout);
		GridData gridData = new GridData();
		gridData.widthHint = 400;
		container.setLayoutData(gridData);

		createLabelComponent(container);
		createNoRadioBtn(container);
		createYesRadioBtn(container);
		createPfxPathTxtBox(container);
		createPasswordComponent(container);

		return super.createContents(parent);
	}

	/**
	 * Create labels on dialog.
	 * @param container
	 */
	private void createLabelComponent(Composite container) {
		Label lblMsg = new Label(container, SWT.LEFT);
		lblMsg.setText(Messages.pfxInputMsg);
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.horizontalSpan = 2;
		gridData.horizontalIndent = 5;
		lblMsg.setLayoutData(gridData);

		Label lblQuestion = new Label(container, SWT.LEFT);
		lblQuestion.setText(Messages.pfxInptQueMsg);
		gridData.verticalIndent = 10;
		lblQuestion.setLayoutData(gridData);
	}

	/**
	 * Method creates No radio button and its listener.
	 * @param container
	 */
	private void createNoRadioBtn(Composite container) {
		noRadioBtn = new Button(container, SWT.RADIO);
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalIndent = 5;
		gridData.verticalIndent = 10;
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = SWT.FILL;
		noRadioBtn.setText(Messages.pfxRadioNo);
		noRadioBtn.setLayoutData(gridData);
		noRadioBtn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				enablePfxCmpnts(false);
				okButton.setEnabled(true);
			}
		});

		if (isNewService) {
			noRadioBtn.setEnabled(false);
		}
	}

	/**
	 * Method creates Yes radio button and its listener.
	 * @param container
	 */
	private void createYesRadioBtn(Composite container) {
		yesRadioBtn = new Button(container, SWT.RADIO);
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalIndent = 5;
		gridData.verticalIndent = 10;
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = SWT.FILL;
		yesRadioBtn.setText(Messages.pfxRadioYes);
		yesRadioBtn.setLayoutData(gridData);
		yesRadioBtn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				enablePfxCmpnts(true);
				okButton.setEnabled(false);
				String path = getPfxPathAsPerCert();
				if (path != null && !path.isEmpty()) {
					txtPfxPath.setText(path);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
	}

	/**
	 * Method creates PFX file path text box and its listener.
	 * @param container
	 */
	private void createPfxPathTxtBox(Composite container) {
		txtPfxPath = new Text(container,
				SWT.BORDER | SWT.LEFT);
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalIndent = 5;
		gridData.widthHint = 300;
		gridData.horizontalIndent = 25;
		txtPfxPath.setLayoutData(gridData);
		txtPfxPath.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent arg0) {
				enableOkBtn();
			}
		});

		browseBtn = new Button(container,
				SWT.PUSH | SWT.CENTER | SWT.END);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalIndent = 5;
		browseBtn.setText(Messages.browseBtn);
		browseBtn.setLayoutData(gridData);
		browseBtn.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				browseBtnListener();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
	}

	/**
	 * Method gets called when user clicks on Browse button.
	 * Opens dialog for selecting PFX files.
	 * Default location is of
	 * certificate file's parent directory.
	 */
	private void  browseBtnListener() {
		FileDialog dialog = new FileDialog(this.getShell());
		String [] extensions = {"*.pfx", "*.*"};
		dialog.setFilterExtensions(extensions);
		String path = new File(certPath).getParent();
		// Go to custom certificate location
		dialog.setFilterPath(path);
		String pfxPathDlg = dialog.open();
		if (pfxPathDlg != null) {
			txtPfxPath.setText(pfxPathDlg);
		}
	}

	/**
	 * Method creates password text box and its listener.
	 * @param container
	 */
	private void createPasswordComponent(Composite container) {
		lblPassword = new Label(container, SWT.LEFT);
		GridData  gridData = new GridData();
		gridData.verticalIndent = 10;
		gridData.horizontalIndent = 25;
		gridData.horizontalSpan = 2;
		lblPassword.setText(Messages.certPwdLbl);
		lblPassword.setLayoutData(gridData);

		passwordField = new Text(container,
				SWT.BORDER | SWT.PASSWORD | SWT.LEFT);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.horizontalSpan = 2;
		gridData.horizontalIndent = 25;
		gridData.verticalIndent = 5;
		passwordField.setLayoutData(gridData);
		passwordField.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent event) {
				enableOkBtn();
			}
		});
	}

	@Override
	protected void okPressed() {
		boolean isValid = false;
		/*
		 * No radio button : No need to upload certificate,
		 * hence make path and password empty.
		 */
		if (noRadioBtn.getSelection()) {
			password = "";
			pfxPath = "";
			isValid = true;
		} else {
			String path = txtPfxPath.getText().trim();
			File file = new File(path);
			/*
			 * Check validity of PFX file,
			 * If it is directory or does not ends with .pfx
			 * then give error.
			 */
			if (file.exists() && file.getPath().endsWith(".pfx")) {
				/*
				 * Validate password entered for PFX file.
				 */
				if (validatePwd()) {
					password = passwordField.getText().trim();
					pfxPath = path;
					isValid = true;
				} else {
					PluginUtil.displayErrorDialog(getShell(),
							Messages.error,
							Messages.invalidPfxPwdMsg);
				}
			} else {
				PluginUtil.displayErrorDialog(getShell(),
						Messages.error,
						Messages.invalidfPfxFile);
			}
		}
		if (isValid) {
			super.okPressed();
		}
	}

	/**
	 * Validate pfx password entered for pfx file.
	 * @return boolean
	 */
	private boolean validatePwd() {
		boolean retval = true;
		try {
			KeyStore ks = KeyStore.getInstance(Messages.keyStore);
			FileInputStream input = new FileInputStream(
					new File(txtPfxPath.getText()));
			ks.load(input, passwordField.
					getText().trim().toCharArray());
		} catch (Exception e) {
			retval = false;
		}
		return retval;
	}

	/**
	 * Return password value.
	 * @return
	 */
	public String getPwd() {
		return password;
	}

	/**
	 * Return PFX path value.
	 * @return
	 */
	public String getPfxPath() {
		return pfxPath;
	}

	/**
	 * Enable or disable PFX path
	 * and password components.
	 * @param state
	 */
	private void enablePfxCmpnts(boolean state) {
		txtPfxPath.setEnabled(state);
		browseBtn.setEnabled(state);
		passwordField.setEnabled(state);
		lblPassword.setEnabled(state);
		if (!state) {
			txtPfxPath.setText("");
			passwordField.setText("");
		}
	}

	/**
	 * Method enables or disables OK button.
	 * Disable OK button if PFX path or password are empty.
	 */
	private void enableOkBtn() {
		if (okButton != null) {
			if (passwordField.getText().trim().isEmpty()
					|| txtPfxPath.getText().trim().isEmpty()) {
				okButton.setEnabled(false);
			} else {
				okButton.setEnabled(true);
			}
		}
	}

	/**
	 * Find and populate path of PFX file
	 * as per certificate file.
	 * @return
	 */
	private String getPfxPathAsPerCert() {
		String pfxPathPopulate = "";
		/*
		 * Find name and parent file of certificate file.
		 */
		File certFile = new File(certPath);
		File certFileParent = certFile.getParentFile();
		String certName = certFile.getName().substring(
				0, certFile.getName().
				lastIndexOf('.'));
		/*
		 * Search pfx files in parent certificate folder
		 * and store all of them in array.
		 */
		File[] pfxArr = certFileParent.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File certFileParent,
					String name) {
				return name.endsWith(".pfx");
			}
		});
		/*
		 * Iterate over all pfx files
		 * and search for pfx file having
		 * same name as that of
		 * cer file.
		 * If found populate path of pfx file
		 * else not.
		 */
		for (int i = 0; i < pfxArr.length; i++) {
			try {
				String path = pfxArr[i].getPath();
				String pfxName = pfxArr[i].getName().substring(0,
						pfxArr[i].getName().lastIndexOf('.'));
				if (certName.equals(pfxName)) {
					pfxPathPopulate = path;
					break;
				}
			}  catch (Exception e) {
				pfxPathPopulate = "";
			}
		}
		return pfxPathPopulate;
	}
}
