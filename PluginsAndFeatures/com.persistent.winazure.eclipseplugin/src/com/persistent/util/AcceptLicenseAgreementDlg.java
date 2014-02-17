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
	protected static File cmpntFile = new
			File(WAEclipseHelper.getTemplateFile(Messages.cmpntFileName));

	protected AcceptLicenseAgreementDlg(Shell parentShell) {
		super(parentShell);
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
		String jdkName = JdkSrvConfig.getThrdPrtJdkCmb().getText();
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(1, false);
		container.setLayout(gridLayout);
		GridData gridData = new GridData();
		gridData.horizontalIndent = 5;
		container.setLayoutData(gridData);
		Label lblName = new Label(container, SWT.LEFT);
		lblName.setText(String.format(Messages.aggMsg, jdkName));
		String url = "";
		try {
			url = WindowsAzureProjectManager.
					getLicenseUrl(jdkName, cmpntFile);
		} catch (WindowsAzureInvalidProjectOperationException e) {
			Activator.getDefault().log(e.getMessage(), e);
		}
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
