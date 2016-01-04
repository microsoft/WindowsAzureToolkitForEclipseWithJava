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
package com.microsoftopentechnologies.wacommon.commoncontrols;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.microsoft.applicationinsights.management.rest.ApplicationInsightsManagementClient;
import com.microsoft.applicationinsights.management.rest.model.ResourceGroup;
import com.microsoft.applicationinsights.management.rest.model.Subscription;
import com.microsoftopentechnologies.azuremanagementutil.rest.AzureApplicationInsightsServices;
import com.microsoftopentechnologies.wacommon.Activator;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
/**
 * Class creates new resource group.
 */
public class NewResourceGroupDialog extends TitleAreaDialog {
	ApplicationInsightsManagementClient client;
	String subscription;
	Button okButton;
	Text txtName;
	Combo subscriptionCombo;
	Combo locationCombo;
	AzureApplicationInsightsServices instance = AzureApplicationInsightsServices.getInstance();
	Map<String, String> subMap = new HashMap<String, String>();
	static ResourceGroup group;

	public NewResourceGroupDialog(Shell parentShell,
			ApplicationInsightsManagementClient client,
			String subscription) {
		super(parentShell);
		this.client = client;
		this.subscription = subscription;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.newResGrpTtl);
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		Control ctrl = super.createButtonBar(parent);
		okButton = getButton(IDialogConstants.OK_ID);
		populateValues();
		return ctrl;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(Messages.newResGrpTtl);
		setMessage(Messages.newResGrpMsg);
		setHelpAvailable(false);

		Composite container = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		GridData gridData = new GridData();
		gridLayout.numColumns = 2;
		gridLayout.marginBottom = 10;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		container.setLayout(gridLayout);
		container.setLayoutData(gridData);
		createNameCmpnt(container);
		createSubCmpnt(container);
		createLocationCmpnt(container);

		return super.createDialogArea(parent);
	}

	private void populateValues() {
		try {
			if (client != null) {
				List<Subscription> subList = instance.getSubscriptions(client);
				// check at least single subscription is associated with the account
				if (subList.size() > 0) {
					for (Subscription sub : subList) {
						subMap.put(sub.getId(), sub.getName());
					}
					Collection<String> values = subMap.values();
					String[] subNameArray = values.toArray(new String[values.size()]);

					subscriptionCombo.setItems(subNameArray);

					// To do - Get list of locations available for subscription.
					// For now pulling value using application insights function
					List<String> regionList = instance.getAvailableGeoLocations(client);
					String[] regionArray = regionList.toArray(new String[regionList.size()]);
					locationCombo.setItems(regionArray);
					locationCombo.setText(regionArray[0]);

					/*
					 * If subscription name is there,
					 * dialog invoked from application insights dialog,
					 * hence disable subscription combo.
					 */
					if (subscription != null && !subscription.isEmpty()) {
						subscriptionCombo.setEnabled(false);
						subscriptionCombo.setText(subscription);
					} else {
						subscriptionCombo.setText(subNameArray[0]);
					}
				}
			}
			enableOkBtn();
		} catch (Exception ex) {
			Activator.getDefault().log(Messages.getValuesErrMsg, ex);
		}
	}

	private void createNameCmpnt(Composite container) {
		Label lblName = new Label(container, SWT.LEFT);
		GridData gridData = gridDataForLbl();
		lblName.setLayoutData(gridData);
		lblName.setText(Messages.name);

		txtName = new Text(container, SWT.LEFT | SWT.BORDER);
		gridData = gridDataForText(180);
		txtName.setLayoutData(gridData);
		txtName.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent arg0) {
				enableOkBtn();
			}
		});
	}

	private void createSubCmpnt(Composite container) {
		Label lblName = new Label(container, SWT.LEFT);
		GridData gridData = gridDataForLbl();
		lblName.setLayoutData(gridData);
		lblName.setText(Messages.sub);

		subscriptionCombo = new Combo(container, SWT.READ_ONLY);
		gridData = gridDataForText(180);
		subscriptionCombo.setLayoutData(gridData);

		// To do - listener for subscription combo.
		// Needs to be added to fetch location values as per subscription.
	}

	private void createLocationCmpnt(Composite container) {
		Label lblName = new Label(container, SWT.LEFT);
		GridData gridData = gridDataForLbl();
		lblName.setLayoutData(gridData);
		lblName.setText(Messages.location);

		locationCombo = new Combo(container, SWT.READ_ONLY);
		gridData = gridDataForText(180);
		locationCombo.setLayoutData(gridData);
	}

	/**
	 * Method creates grid data for label field.
	 * @return
	 */
	private GridData gridDataForLbl() {
		GridData gridData = new GridData();
		gridData.horizontalIndent = 5;
		gridData.verticalIndent = 10;
		return gridData;
	}

	/**
	 * Method creates grid data for text field.
	 * @return
	 */
	private GridData gridDataForText(int width) {
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.END;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.widthHint = width;
		gridData.verticalIndent = 10;
		gridData.grabExcessHorizontalSpace = true;
		return gridData;
	}

	/**
	 * Method enables or disables OK button.
	 * Disable OK button if resource group name/location/subscription is not specified.
	 */
	private void enableOkBtn() {
		if (okButton != null) {
			if (txtName.getText().trim().isEmpty()
					|| subscriptionCombo.getText().isEmpty()
					|| locationCombo.getText().isEmpty()) {
				okButton.setEnabled(false);
				if (subscriptionCombo.getText().isEmpty() || subscriptionCombo.getItemCount() <= 0) {
					setErrorMessage(Messages.noSubErrMsg);
				} else {
					setErrorMessage(null);
				}
			} else {
				okButton.setEnabled(true);
				setErrorMessage(null);
			}
		}
	}

	private String findKeyAsPerValue(String subName) {
		String key = "";
		for (Map.Entry<String, String> entry : subMap.entrySet()) {
			if (entry.getValue().equalsIgnoreCase(subName)) {
				key = entry.getKey();
				break;
			}
		}
		return key;
	}

	@Override
	protected void okPressed() {
		boolean isValid = false;
		try {
			PluginUtil.showBusy(true, getShell());
			String subId = findKeyAsPerValue(subscriptionCombo.getText());
			group = instance.createResourceGroup(client, subId,
					txtName.getText().trim(), locationCombo.getText());
			isValid = true;
		} catch (java.net.SocketTimeoutException e) {
			PluginUtil.showBusy(false, getShell());
			PluginUtil.displayErrorDialogAndLog(getShell(), Messages.newResGrpTtl,
					Messages.timeOutErr, e);
		} catch (Exception ex) {
			PluginUtil.showBusy(false, getShell());
			PluginUtil.displayErrorDialogAndLog(getShell(),
					Messages.newResGrpTtl, Messages.newResErrMsg, ex);
		}
		if (isValid) {
			PluginUtil.showBusy(false, getShell());
			super.okPressed();
		}
	}

	public static ResourceGroup getResourceGroup() {
		return group;
	}
}
