/**
* Copyright Microsoft Corp.
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
package com.gigaspaces.azure.propertypage;

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
