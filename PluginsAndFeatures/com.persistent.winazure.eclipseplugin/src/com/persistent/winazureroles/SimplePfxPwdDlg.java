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

import com.microsoftopentechnologies.wacommon.utils.CerPfxUtil;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;

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
