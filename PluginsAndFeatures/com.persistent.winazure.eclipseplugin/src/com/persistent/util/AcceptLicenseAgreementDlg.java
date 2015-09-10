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
package com.persistent.util;

import java.io.File;
import java.net.URL;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;

import waeclipseplugin.Activator;

import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;

public class AcceptLicenseAgreementDlg extends Dialog {
	private Button acceptBtn;
	private Link urlLink;
	/*
	 * isForJdk : true = License dialog for JDK
	 * isForJdk : false = License dialog for server
	 */
	private boolean isForJdk;
	protected static File cmpntFile = new
			File(WAEclipseHelper.getTemplateFile(Messages.cmpntFileName));

	protected AcceptLicenseAgreementDlg(Shell parentShell, boolean isForJdk) {
		super(parentShell);
		this.isForJdk = isForJdk;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.aggTtl);
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		Control ctrl = super.createButtonBar(parent);
		acceptBtn = getButton(IDialogConstants.OK_ID);
		if (acceptBtn != null) {
			acceptBtn.setText(Messages.acptBtn);
		}
		return ctrl;
	}

	protected Control createContents(Composite parent) {
		String name = "";
		String url = "";
		try {
			if (isForJdk) {
				name = JdkSrvConfig.getThrdPrtJdkCmb().getText();
				url = WindowsAzureProjectManager.
						getLicenseUrl(name, cmpntFile);
			} else {
				name = JdkSrvConfig.getThrdPrtSrvCmb().getText();
				url = WindowsAzureProjectManager.
						getThirdPartyServerLicenseUrl(name, cmpntFile);
			}
		} catch (WindowsAzureInvalidProjectOperationException e) {
			Activator.getDefault().log(e.getMessage());
		}
		
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(1, false);
		container.setLayout(gridLayout);
		GridData gridData = new GridData();
		gridData.horizontalIndent = 5;
		container.setLayoutData(gridData);
		Label lblName = new Label(container, SWT.LEFT);
		lblName.setText(String.format(Messages.aggMsg, name));
		
		urlLink = new Link(container, SWT.LEFT);
		urlLink.setText(String.format(Messages.aggLnk, url, url));
		gridData = new GridData();
		gridData.horizontalIndent = 10;
		urlLink.setLayoutData(gridData);
		urlLink.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				try {
					PlatformUI.getWorkbench().getBrowserSupport().
					getExternalBrowser().openURL(new URL(event.text));
				} catch (Exception ex) {
					/*
					 * only logging the error in log file
					 * not showing anything to end user
					 */
					Activator.getDefault().log(
							com.persistent.ui.projwizard.Messages.lnkOpenErrMsg,
							ex);
				}
			}
		});
		return super.createContents(parent);
	}
}
