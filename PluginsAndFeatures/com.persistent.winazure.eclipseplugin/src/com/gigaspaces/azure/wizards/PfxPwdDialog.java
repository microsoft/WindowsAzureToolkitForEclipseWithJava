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
import java.security.cert.X509Certificate;

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

import com.interopbridges.tools.windowsazure.WindowsAzureCertificate;
import com.microsoftopentechnologies.wacommon.utils.CerPfxUtil;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
import com.persistent.util.WAEclipseHelper;

/**
 * Creates dialog for accepting pfx password.
 */
public class PfxPwdDialog extends Dialog {
	private Text txtPfxPath;
	private Button browseBtn;
	private Text passwordField;
	private Label lblPassword;
	private WindowsAzureCertificate cert;
	public String password;
	public String pfxPath;
	private Button okButton;
	private String projPath = WAEclipseHelper.getSelectedProject().getLocation().toOSString();

	/**
	 * Constructor.
	 * @param parentShell
	 * @param certName
	 */
	protected PfxPwdDialog(Shell parentShell,
			WindowsAzureCertificate cert) {
		super(parentShell);
		this.cert = cert;
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
		okButton.setEnabled(false);
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
		lblMsg.setText(String.format(Messages.pfxInputMsg, cert.getName()));
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.horizontalSpan = 2;
		gridData.horizontalIndent = 5;
		lblMsg.setLayoutData(gridData);

		Label lblMsgNxt = new Label(container, SWT.LEFT);
		lblMsgNxt.setText(Messages.pfxRadioYes);
		lblMsgNxt.setLayoutData(gridData);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.verticalIndent = 15;
		gridData.horizontalIndent = 5;
		lblMsgNxt.setLayoutData(gridData);
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
		gridData.horizontalIndent = 5;
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
		String path = new File(projPath).getParent();
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
		gridData.horizontalIndent = 5;
		gridData.horizontalSpan = 2;
		lblPassword.setText(Messages.certPwdLbl);
		lblPassword.setLayoutData(gridData);

		passwordField = new Text(container,
				SWT.BORDER | SWT.PASSWORD | SWT.LEFT);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.horizontalSpan = 2;
		gridData.horizontalIndent = 5;
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
		String path = txtPfxPath.getText().trim();
		File file = new File(path);
		/*
		 * Check validity of PFX file,
		 * If it is directory or does not ends with .pfx
		 * then give error.
		 */
		if (file.exists() && file.getPath().endsWith(".pfx")) {
			String pwdTxt = passwordField.getText().trim();
			/*
			 * Validate password entered for PFX file.
			 */
			if (CerPfxUtil.validatePfxPwd(path, pwdTxt)) {
				try {
					// check pfx and cer file's thumbprint matches
					X509Certificate pfxCert = CerPfxUtil.getCert(path, pwdTxt);
					if (CerPfxUtil.getThumbPrint(pfxCert).
							equalsIgnoreCase(cert.getFingerPrint())) {
						password = pwdTxt;
						pfxPath = path;
						isValid = true;
					} else {
						PluginUtil.displayErrorDialog(getShell(),
								Messages.error,
								String.format(Messages.unMatchMsg,
										cert.getName()));
					}
				} catch (Exception e) {
				}
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
		if (isValid) {
			super.okPressed();
		}
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
}
