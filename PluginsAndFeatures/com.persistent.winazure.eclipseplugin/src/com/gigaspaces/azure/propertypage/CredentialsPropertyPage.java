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
 */package com.gigaspaces.azure.propertypage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

import com.gigaspaces.azure.runnable.CacheAccountWithProgressWindow;
import com.gigaspaces.azure.wizards.WizardCacheManager;
import com.microsoftopentechnologies.azurecommons.deploy.propertypages.CredentialsPropertyPageUtilMethods;
import com.microsoftopentechnologies.azurecommons.deploy.util.PublishData;

public class CredentialsPropertyPage extends PropertyPage {

	private static PublishData publishData;
	private Text txtSubscriptionId;
	public static void setPublishData(PublishData si) {
		publishData = si;
	}

	@Override
	protected Control createContents(Composite composite) {
		noDefaultAndApplyButton();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite,
				com.gigaspaces.azure.wizards.Messages.pluginPrefix
				+ com.gigaspaces.azure.wizards.Messages.credentialsHelp);

		Composite container = new Composite(composite, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		container.setLayout(gridLayout);
		container.setLayoutData(gridData);

		createSubscriptionIdComponent(container);
		return container;
	}

	private void createSubscriptionIdComponent(Composite container) {
		Label lblSubscriptionId = new Label(container, SWT.LEFT);
		lblSubscriptionId.setText(Messages.credentialDlgSubscriptionId);
		GridData gridData = new GridData();
		gridData.heightHint = 20;
		gridData.horizontalIndent = 5;
		lblSubscriptionId.setLayoutData(gridData);

		txtSubscriptionId = new Text(container, SWT.SINGLE | SWT.BORDER);
		gridData = new GridData();
		gridData.widthHint = 275;
		gridData.horizontalAlignment = GridData.END;
		gridData.grabExcessHorizontalSpace = true;
		txtSubscriptionId.setLayoutData(gridData);
		txtSubscriptionId.setEnabled(publishData == null);

		if (publishData != null) {
			txtSubscriptionId.setText(publishData.getCurrentSubscription().getSubscriptionID());
		}

	}

	@Override
	public boolean performOk() {
		String subsciptionId = txtSubscriptionId.getText();
		if (subsciptionId != null && !subsciptionId.isEmpty()) {
			PublishData publishDataToCache = null;
			publishDataToCache = CredentialsPropertyPageUtilMethods.
					handleAddAndEdit(subsciptionId,
							WizardCacheManager.getPublishDataList());
			if (publishDataToCache != null) {
				String messageInCaseOfError = Messages.loadingCredentialsError;
				CacheAccountWithProgressWindow settings = new CacheAccountWithProgressWindow(null, publishDataToCache, Display.getDefault().getActiveShell(), messageInCaseOfError);
				Display.getDefault().syncExec(settings);
			}
		}
		return super.performOk();
	}
}
