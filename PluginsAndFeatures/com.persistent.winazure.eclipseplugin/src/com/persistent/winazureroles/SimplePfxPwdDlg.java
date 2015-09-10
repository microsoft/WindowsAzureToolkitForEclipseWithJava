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
package com.persistent.winazureroles;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
import com.microsoftopentechnologies.azurecommons.wacommonutil.CerPfxUtil;

public class SimplePfxPwdDlg extends org.eclipse.jface.dialogs.Dialog {
	private Text txtPwd;
	private String pfxPath; 
	private String password;
	private Button okButton;

	protected SimplePfxPwdDlg(Shell parentShell, String path) {
		super(parentShell);
		this.pfxPath = path;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.certPwd);
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		Control ctrl = super.createButtonBar(parent);
		okButton = getButton(IDialogConstants.OK_ID);
		okButton.setEnabled(false);
		return ctrl;
	}

	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(1, false);
		container.setLayout(gridLayout);
		GridData gridData = new GridData();
		gridData.widthHint = 400;
		container.setLayoutData(gridData);

		Label lblName = new Label(container, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalIndent = 5;
		gridData.verticalIndent = 10;
		lblName.setLayoutData(gridData);
		lblName.setText(Messages.enterPfxPwd);

		txtPwd = new Text(container, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalIndent = 10;
		gridData.grabExcessHorizontalSpace = true;
		txtPwd.setLayoutData(gridData);
		txtPwd.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent event) {
				enableOkBtn();
			}
		});

		return super.createContents(parent);
	}

	/**
	 * Method enables or disables OK button.
	 * Disable OK button password is empty.
	 */
	private void enableOkBtn() {
		if (okButton != null) {
			if (txtPwd.getText().trim().isEmpty()) {
				okButton.setEnabled(false);
			} else {
				okButton.setEnabled(true);
			}
		}
	}

	@Override
	protected void okPressed() {
		boolean isValid = false;
		if (CerPfxUtil.validatePfxPwd(pfxPath, txtPwd.getText().trim())) {
			isValid = true;
			password = txtPwd.getText().trim();
		} else {
			PluginUtil.displayErrorDialog(getShell(),
					com.gigaspaces.azure.wizards.Messages.error,
					com.gigaspaces.azure.wizards.Messages.invalidPfxPwdMsg);
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
}
