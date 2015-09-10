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
