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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.gigaspaces.azure.model.CreateStorageServiceInput;
import com.gigaspaces.azure.model.Location;
import com.gigaspaces.azure.model.Locations;
import com.gigaspaces.azure.util.UIUtils;
import com.persistent.util.MessageUtil;

public class NewStorageAccountDialog extends WADialog {
	
	private Text storageAccountTxt;
	private Combo locationComb;
	private Text descriptionTxt;
	private ProgressBar bar;
	boolean valid = false;
	private String storageAccountNameToCreate;
	private String storageAccountLocation;
	private String defaultLocation;

	
	private final static String STORAGE_ACCOUNT_NAME_PATTERN = "^[a-z0-9]+$";

	public NewStorageAccountDialog(Shell parentShell) {
		super(parentShell);
	}
	
	public void setDefaultLocation(final String location) {
		this.defaultLocation = location;
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		bar = new ProgressBar(parent, SWT.FILL);
		GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,Messages.pluginPrefix + Messages.newStorageAccountHelp);
		
		gridData.horizontalIndent = 10;
		gridData.grabExcessHorizontalSpace = true;

		bar.setLayoutData(gridData);
		bar.setMaximum(60000);
		bar.setSelection(0);
		bar.setVisible(false);

		Control control = super.createButtonBar(parent);

		Button okButton = getButton(IDialogConstants.OK_ID);
		if (okButton != null) {
			okButton.setText("OK");

			okButton.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(final SelectionEvent e) {

					final CreateStorageServiceInput body = new CreateStorageServiceInput(
							storageAccountTxt.getText(), storageAccountTxt
									.getText(), locationComb.getText());

					body.setDescription(descriptionTxt.getText());
					
					storageAccountNameToCreate = storageAccountTxt.getText();
					storageAccountLocation = locationComb.getText();

					boolean isNameAvailable = false;
					try {
						isNameAvailable = WizardCacheManager.isStorageAccountNameAvailable(storageAccountNameToCreate);
						if (isNameAvailable) {
							WizardCacheManager.createStorageServiceMock(storageAccountNameToCreate, storageAccountLocation, descriptionTxt.getText());
							valid = true;
							close();
						} else {
							MessageUtil.displayErrorDialog(getShell(), "DNS Conflict", Messages.storageAccountConflictError);
							storageAccountTxt.setFocus();
							storageAccountTxt.selectAll();
						}
					} catch (final Exception e1) {
						MessageUtil.displayErrorDialogAndLog(getShell(), "Error", e1.getMessage(), e1);
					}
					
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}
		return control;
	}

	@Override
	public boolean close() {
		if (this.getReturnCode() == 1) {
			valid = true;
		}

		if (valid == true) {
			valid = super.close();
		}

		return valid;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(Messages.storageNew);
		setMessage(Messages.storageCreateNew);

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

		Label hostedServiceLbl = new Label(container, SWT.LEFT);
		hostedServiceLbl.setText(Messages.storageAccountLbl);
		gridData = new GridData();
		gridData.widthHint = 250;
		gridData.horizontalIndent = 10;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.BEGINNING;

		storageAccountTxt = new Text(container, SWT.BORDER);
		storageAccountTxt.addModifyListener(new ValidateInputCompletion());

		storageAccountTxt.setLayoutData(gridData);

		Label locationLbl = new Label(container, SWT.LEFT);
		locationLbl.setText(Messages.hostedLocationLbl);

		locationComb = new Combo(container, SWT.READ_ONLY);
		locationComb.setLayoutData(gridData);
		populateLocations();
		locationComb.addModifyListener(new ValidateInputCompletion());
		Label descriptionLbl = new Label(container, SWT.LEFT);
		descriptionLbl.setText(Messages.hostedLocDescLbl);

		descriptionTxt = new Text(container, SWT.BORDER);
		descriptionTxt.setLayoutData(gridData);

		validateDialog();

		return super.createDialogArea(parent);
	}

	private void populateLocations() {
		Locations items = WizardCacheManager.getLocation();
		locationComb.removeAll();

		for (Location location : items) {
			locationComb.add(location.getName());
			locationComb.setData(location.getName(), location);
		}
		
		// default location will exist if the user has created a hosted servicebefore creating the storage account
		if (defaultLocation != null) {
			int selection = UIUtils.findSelectionByText(defaultLocation, locationComb);
			if (selection != -1) {
				locationComb.select(selection);
			}
			else {
				locationComb.select(0);
			}			
		}
	}

	@Override
	protected boolean validateDialog() {

		String host = storageAccountTxt.getText();
		String location = locationComb.getText();

		boolean legalName = validateStorageAccountName(host);
		if (!legalName) {
			setErrorMessage(Messages.wrongStorageName);
			return false;
		}
		
		if (host == null || host.isEmpty()) {
			setErrorMessage(Messages.storageAccountIsNullError);
			return false;
		}

		if (location == null || location.isEmpty()) {
			setErrorMessage(Messages.hostedLocNotSelectedError);
			return false;
		}

		setMessage(Messages.storageCreateNew);
		return true;
	}

	private boolean validateStorageAccountName(String host) {
		
		if (host.length() < 3 || host.length() > 24) {
			return false;
		}
		if (!host.matches(STORAGE_ACCOUNT_NAME_PATTERN)) {
			return false;
		}
		return true;
	}

	@Override
	protected void createButtonClicked() {
	}

	public String getStorageAccountName() {
		return storageAccountNameToCreate;
	}
	
	public String getLocation() {
		return storageAccountLocation; 
	}


}
