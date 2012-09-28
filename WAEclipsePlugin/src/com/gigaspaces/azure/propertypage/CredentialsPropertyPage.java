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

package com.gigaspaces.azure.propertypage;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

import com.gigaspaces.azure.model.Subscription;
import com.gigaspaces.azure.runnable.CacheAccountWithProgressWindow;
import com.gigaspaces.azure.util.PublishData;
import com.gigaspaces.azure.util.PublishProfile;
import com.gigaspaces.azure.wizards.WizardCacheManager;
import com.persistent.util.MessageUtil;

public class CredentialsPropertyPage extends PropertyPage {

	private static PublishData publishData;
	
	private static boolean edit;

	private static boolean add;
	
	private Text txtThumbPrint;
	private Text txtSubscriptionId;
	
	public static void setPublishData(PublishData si) {
		publishData = si;
	}

	public static void setEdit(boolean bool) {
		edit = bool;
	}
	
	public static void setAdd(boolean bool) {
		add = bool;
	}

	
	@Override
	protected Control createContents(Composite composite) {
		noDefaultAndApplyButton();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite,com.gigaspaces.azure.wizards.Messages.pluginPrefix + com.gigaspaces.azure.wizards.Messages.credentialsHelp);

		Composite container = new Composite(composite, SWT.NONE);
		
		
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		container.setLayout(gridLayout);
		container.setLayoutData(gridData);

		createSubscriptionIdComponent(container);
		createThumbprintComponent(container);
		return container;
	}

	private void createThumbprintComponent(Composite container) {
		Label lblThumbprint = new Label(container, SWT.LEFT);
		lblThumbprint.setText(Messages.credentialDlgThumbprint);
		GridData gridData = new GridData();
		gridData.heightHint = 20;
		gridData.horizontalIndent = 5;
		lblThumbprint.setLayoutData(gridData);

		txtThumbPrint = new Text(container, SWT.SINGLE | SWT.BORDER);
		gridData = new GridData();
		gridData.widthHint = 275;
		gridData.horizontalAlignment = GridData.END;
		gridData.grabExcessHorizontalSpace = true;
		txtThumbPrint.setLayoutData(gridData);

		if (publishData != null) {
			txtThumbPrint.setText(publishData.getThumbprint());
		}

		txtThumbPrint.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent event) {
			}
		});

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
		String thumbprint = txtThumbPrint.getText();
		
		if ((subsciptionId != null && !subsciptionId.isEmpty()) && (thumbprint != null && !thumbprint.isEmpty())) {			
			PublishData publishDataToCache = null;
			if (edit) {
				publishDataToCache = handleEdit(subsciptionId, thumbprint);
			}
			if (add){
				publishDataToCache = handleAdd(subsciptionId, thumbprint);
			}
			if (publishDataToCache != null) {
				String messageInCaseOfError = Messages.loadingCredentialsError;
				CacheAccountWithProgressWindow settings = new CacheAccountWithProgressWindow(publishDataToCache, Display.getDefault().getActiveShell(), messageInCaseOfError);
				Display.getDefault().syncExec(settings);
				if (settings.isCompletedSuccesfully() && edit) {
					WizardCacheManager.removeSubscription(subsciptionId,publishData.getThumbprint());
				}
			}
		}
		return super.performOk();
	}

	private PublishData handleAdd(String subsciptionId, String thumbprint) {

		PublishData pd = WizardCacheManager.findPublishDataByThumbprint(thumbprint);
		if (pd == null) {
			pd = createPublishData(subsciptionId, thumbprint);
			return pd;
		}
		if (!doesSubscriptionExist(pd, subsciptionId)) {
			Subscription s = new Subscription();
			s.setSubscriptionID(subsciptionId);
			pd.getPublishProfile().getSubscriptions().add(s);
		} 
		else {
			return null; // TODO - bad practice to return null as a valid return value.
		}
		return pd;
	}

	private PublishData handleEdit(String subsciptionId, String thumbprint) {
		PublishData pd = WizardCacheManager.findPublishDataByThumbprint(thumbprint);

		if (pd == null) {
			pd = createPublishData(subsciptionId, thumbprint);
			return pd;
		} 
		if (!doesSubscriptionExist(pd, subsciptionId)) {
			Subscription s = new Subscription();
			s.setSubscriptionID(subsciptionId);
			pd.getPublishProfile().getSubscriptions().add(s);
		} 
		else {
			return null; // TODO - bad practice to return null as a valid return value.
		}
		return pd;
	}

	private PublishData createPublishData(String subsciptionId,String thumbprint) {
		PublishData pd = new PublishData();
		pd.setPublishProfile(new PublishProfile());
		pd.getPublishProfile().setThumbprint(thumbprint);

		List<Subscription> subs = new ArrayList<Subscription>();

		Subscription s = new Subscription();
		s.setSubscriptionID(subsciptionId);
		subs.add(s);
		pd.getPublishProfile().setSubscriptions(subs);
		return pd;
	}
	
	private boolean doesSubscriptionExist(PublishData publishData, String subsciptionId) {
		
		List<Subscription> subs = publishData.getPublishProfile().getSubscriptions();

		for (int i = 0; i < subs.size(); i++) {
			Subscription s = subs.get(i);

			if (s.getSubscriptionID().equals(subsciptionId)) {
				return true;
			}
		}

		return false;
	}
	
	public void testErrorMessage() {
		MessageUtil.displayErrorDialog(this.getShell(), "asdasd", "sadasd");
	}
}
