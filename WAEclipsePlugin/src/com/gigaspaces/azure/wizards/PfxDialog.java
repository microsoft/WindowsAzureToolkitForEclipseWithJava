/*******************************************************************************
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.gigaspaces.azure.wizards;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import waeclipseplugin.Activator;

public class PfxDialog extends WADialog {
	private Text subscriptionIdTxt;
	private Text pfxPasswordTxt;
	private String subscriptionId;
	private String pfxPassword;
	private File pfxFile;
	public PfxDialog(File file,Shell parentShell) {
		super(parentShell);
		this.pfxFile = file;
	}

	@Override
	protected void configureShell(Shell newShell) {		
		super.configureShell(newShell);
		newShell.setText(Messages.subscriptionCert);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(Messages.subscriptionCert);
		setMessage(Messages.enterSubscId);

		Composite container = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		GridData gridData = new GridData();
		gridData.widthHint = 30;
		gridLayout.numColumns = 2;
		gridData.verticalIndent = 10;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		container.setLayout(gridLayout);
		container.setLayoutData(gridData);

		Label subscriptionIdLbl = new Label(container, SWT.LEFT);
		subscriptionIdLbl.setText(Messages.subscriptionIdLbl);
		gridData = new GridData();
		gridData.widthHint = 250;
		// gridData.heightHint = 23;
		gridData.horizontalIndent = 10;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.BEGINNING;

		subscriptionIdTxt = new Text(container, SWT.BORDER);
		subscriptionIdTxt.addModifyListener(new ValidateInputCompletion());

		subscriptionIdTxt.setLayoutData(gridData);

		Label pfxPasswordLbl = new Label(container, SWT.LEFT);
		pfxPasswordLbl.setText(Messages.pfxFilePasswordLbl);

		pfxPasswordTxt = new Text(container,SWT.BORDER | SWT.PASSWORD);
		pfxPasswordTxt.addModifyListener(new ValidateInputCompletion());
		pfxPasswordTxt.setLayoutData(gridData);

		validateDialog();

		return super.createDialogArea(parent);
	}

	@Override
	protected boolean validateDialog() {
		subscriptionId = subscriptionIdTxt.getText();

		if (subscriptionId == null || subscriptionId.isEmpty()) {
			subscriptionId = null;
			setErrorMessage(Messages.subscriptionIdIsNull);
			return false;
		}

		if (Pattern
				.matches(
						Messages.subsriptionIdPattern,
						subscriptionId) == false) {
			subscriptionId = null;
			setErrorMessage(Messages.invalidSubsriptionId);
			return false;
		}

		pfxPassword = pfxPasswordTxt.getText();

		if (pfxPassword == null || pfxPassword.isEmpty()) {
			pfxPassword = null;
			setErrorMessage(Messages.passwordIsNull);
			return false;
		}

		try {
			KeyStore ks= KeyStore.getInstance(Messages.keyStore);
			FileInputStream input = new FileInputStream(pfxFile);
			ks.load(input, pfxPassword.toCharArray());			
		} catch (KeyStoreException e) {
			Activator.getDefault().log(Messages.error,e);
		} catch (FileNotFoundException e) {
			Activator.getDefault().log(Messages.error,e);
		} catch (NoSuchAlgorithmException e) {
			pfxPassword = null;
			setErrorMessage(Messages.invalidCertPassword);
			return false;
		} catch (CertificateException e) {
			pfxPassword = null;
			setErrorMessage(Messages.invalidCertPassword);
			return false;
		} catch (IOException e) {
			pfxPassword = null;
			setErrorMessage(Messages.invalidCertPassword);
			return false;
		}
		
		setMessage(Messages.subscriptionCert);
		return true;
	}

	@Override
	protected void createButtonClicked() {
		// TODO Auto-generated method stub

	}

	public String getSubscriptionId() {
		return subscriptionId;
	}

	public String getPfxPassword() {
		return pfxPassword;
	}

}
