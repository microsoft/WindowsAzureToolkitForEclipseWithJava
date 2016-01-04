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
package com.microsoft.applicationinsights.preference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.microsoft.applicationinsights.management.rest.ApplicationInsightsManagementClient;
import com.microsoft.applicationinsights.management.rest.model.Resource;
import com.microsoft.applicationinsights.management.rest.model.ResourceGroup;
import com.microsoft.applicationinsights.management.rest.model.Subscription;
import com.microsoft.applicationinsights.ui.activator.Activator;
import com.microsoft.applicationinsights.util.AILibraryUtil;
import com.microsoftopentechnologies.azuremanagementutil.rest.AzureApplicationInsightsServices;
import com.microsoftopentechnologies.wacommon.commoncontrols.NewResourceGroupDialog;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
/**
 * Class is intended for creating new application insights resources
 * remotely in the cloud.
 */
public class ApplicationInsightsNewDialog extends TitleAreaDialog  {
	Text txtName;
	Combo subscription;
	Combo resourceGrp;
	Combo region;
	Button okButton;
	Button newBtn;
	ApplicationInsightsManagementClient client;
	AzureApplicationInsightsServices instance = AzureApplicationInsightsServices.getInstance();
	Map<String, String> subMap = new HashMap<String, String>();
	String currentSub;
	static ApplicationInsightsResource resourceToAdd;

	public ApplicationInsightsNewDialog(Shell parentShell, ApplicationInsightsManagementClient client) {
		super(parentShell);
		this.client = client;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.appTtl);
		Image image = AILibraryUtil.getImage();
		if (image != null) {
			setTitleImage(image);
		}
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		Control ctrl = super.createButtonBar(parent);
		okButton = getButton(IDialogConstants.OK_ID);
		// populate values after button bar has been created, in order to enable or disable OK button.
		populateValues();
		return ctrl;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(Messages.newKeyTtl);
		setMessage(Messages.newKeyMsg);
		setHelpAvailable(false);

		Composite container = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		GridData gridData = new GridData();
		gridLayout.numColumns = 3;
		gridLayout.marginBottom = 10;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		container.setLayout(gridLayout);
		container.setLayoutData(gridData);

		createNameCmpnt(container);
		createSubCmpnt(container);
		createResourceGroupCmpnt(container);
		createRegionCmpnt(container);

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
					Set<String> keySet = subMap.keySet();
					String[] subKeyArray = keySet.toArray(new String[keySet.size()]);

					subscription.setItems(subNameArray);
					subscription.setText(subNameArray[0]);
					currentSub = subNameArray[0];

					populateResourceGroupValues(subKeyArray[0], "");
				}

				List<String> regionList = instance.getAvailableGeoLocations(client);
				String[] regionArray = regionList.toArray(new String[regionList.size()]);
				region.setItems(regionArray);
				region.setText(regionArray[0]);
			}
			enableOkBtn();
		} catch (Exception ex) {
			Activator.getDefault().log(Messages.getValuesErrMsg, ex);
		}
	}

	private void populateResourceGroupValues(String subId, String valtoSet) {
		try {
			List<ResourceGroup> groupList = instance.getResourceGroups(client, subId);
			if (groupList.size() > 0) {
				List<String> groupStringList = new ArrayList<String>();
				for (ResourceGroup group : groupList) {
					groupStringList.add(group.getName());
				}
				String[] groupArray = groupStringList.toArray(new String[groupStringList.size()]);
				resourceGrp.removeAll();
				resourceGrp.setItems(groupArray);
				if (valtoSet.isEmpty() || !groupStringList.contains(valtoSet)) {
					resourceGrp.setText(groupArray[0]);
				} else {
					resourceGrp.setText(valtoSet);
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

		new Link(container, SWT.NO);
	}

	private void createSubCmpnt(Composite container) {
		Label lblName = new Label(container, SWT.LEFT);
		GridData gridData = gridDataForLbl();
		lblName.setLayoutData(gridData);
		lblName.setText(Messages.sub);

		subscription = new Combo(container, SWT.READ_ONLY);
		gridData = gridDataForText(180);
		subscription.setLayoutData(gridData);

		subscription.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String newSub = subscription.getText();
				String prevResGrpVal = resourceGrp.getText();
				String key = findKeyAsPerValue(newSub);
				if (key != null && !key.isEmpty()) {
					if (currentSub.equalsIgnoreCase(newSub)) {
						populateResourceGroupValues(key, prevResGrpVal);
					} else {
						populateResourceGroupValues(key, "");
					}
					currentSub = newSub;
				} else {
					Activator.getDefault().log(Messages.getSubIdErrMsg);
				}
				enableOkBtn();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {

			}
		});

		new Link(container, SWT.NO);
	}

	private void createResourceGroupCmpnt(Composite container) {
		Label lblName = new Label(container, SWT.LEFT);
		GridData gridData = gridDataForLbl();
		lblName.setLayoutData(gridData);
		lblName.setText(Messages.resGrp);

		resourceGrp = new Combo(container, SWT.READ_ONLY);
		gridData = gridDataForText(180);
		resourceGrp.setLayoutData(gridData);

		newBtn =  new Button(container, SWT.PUSH);
		newBtn.setText(Messages.btnNewLbl);
		gridData = gridDataForText(40);
		newBtn.setLayoutData(gridData);

		newBtn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String subTxt = subscription.getText();
				NewResourceGroupDialog dialog = new NewResourceGroupDialog(
						getShell(), client, subTxt);
				int result = dialog.open();
				if (result == Window.OK) {
					ResourceGroup group = NewResourceGroupDialog.getResourceGroup();
					if (group != null) {
						populateResourceGroupValues(findKeyAsPerValue(subTxt), group.getName());
					}
				}
			}
		});
	}

	private void createRegionCmpnt(Composite container) {
		Label lblName = new Label(container, SWT.LEFT);
		GridData gridData = gridDataForLbl();
		lblName.setLayoutData(gridData);
		lblName.setText(Messages.region);

		region = new Combo(container, SWT.READ_ONLY);
		gridData = gridDataForText(180);
		region.setLayoutData(gridData);

		new Link(container, SWT.NO);
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
	 * Enable or disable OK button as per text selected in combo box or text box.
	 * New… button to create resource group will be disabled
	 * if no subscription is selected/exists.
	 */
	private void enableOkBtn() {
		if (okButton != null) {
			if (txtName.getText().trim().isEmpty()
					|| subscription.getText().isEmpty()
					|| resourceGrp.getText().isEmpty()
					|| region.getText().isEmpty()) {
				okButton.setEnabled(false);
				if (subscription.getText().isEmpty() || subscription.getItemCount() <= 0) {
					setErrorMessage(Messages.noSubErrMsg);
					newBtn.setEnabled(false);
				} else if (resourceGrp.getText().isEmpty() || resourceGrp.getItemCount() <= 0) {
					setErrorMessage(Messages.noResGrpErrMsg);
					newBtn.setEnabled(true);
				} else {
					setErrorMessage(null);
					newBtn.setEnabled(true);
				}
			} else {
				okButton.setEnabled(true);
				setErrorMessage(null);
				newBtn.setEnabled(true);
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
			String subId = findKeyAsPerValue(subscription.getText());
			Resource resource = instance.createApplicationInsightsResource(client,
					subId, resourceGrp.getText(),
					txtName.getText(), region.getText());
			resourceToAdd = new ApplicationInsightsResource(
					resource.getName(), resource.getInstrumentationKey(),
					subscription.getText(), subId, resource.getLocation(),
					resource.getResourceGroup(), true);
			isValid = true;
		} catch (java.net.SocketTimeoutException e) {
			PluginUtil.showBusy(false, getShell());
			PluginUtil.displayErrorDialogAndLog(getShell(), Messages.appTtl,
					Messages.timeOutErr1, e);
		} catch (Exception ex) {
			PluginUtil.showBusy(false, getShell());
			PluginUtil.displayErrorDialogAndLog(getShell(),
					Messages.appTtl, Messages.resCreateErrMsg, ex);
		}
		if (isValid) {
			PluginUtil.showBusy(false, getShell());
			super.okPressed();
		}
	}

	public static ApplicationInsightsResource getResource() {
		return resourceToAdd;
	}
}
