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
package com.persistent.ui.preference;

import java.io.FileInputStream;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import waeclipseplugin.Activator;

import com.gigaspaces.azure.wizards.NewStorageAccountDialog;
import com.gigaspaces.azure.wizards.WizardCacheManager;
import com.microsoftopentechnologies.azuremanagementutil.model.StorageService;
import com.microsoftopentechnologies.azurecommons.preference.PreferenceUtilMethods;
import com.microsoftopentechnologies.azurecommons.storageregistry.StorageAccount;
import com.microsoftopentechnologies.azurecommons.storageregistry.StorageRegistryUtilMethods;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
import com.microsoftopentechnologies.azurecommons.wacommonutil.PreferenceSetUtil;

public class StorageAccountDialog extends TitleAreaDialog {
	private Text txtName;
	private Text txtKey;
	private Text txtUrl;
	private boolean isEdit;
	StorageAccount account;
	private Button okButton;
	private Button newStrAccBtn;

	public StorageAccountDialog(Shell parentShell) {
		super(parentShell);
	}

	public StorageAccountDialog(Shell parentShell, StorageAccount accToEdit) {
		super(parentShell);
		this.isEdit = true;
		this.account = accToEdit;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		String title = Messages.addStrTtl;
		if (isEdit) {
			title = Messages.edtStrTtl;
		}
		newShell.setText(title);
		Image image;
		try {
			URL imgUrl = Activator.getDefault().
					getBundle().getEntry(Messages.strAccDlgImg);
			URL imgFileURL = FileLocator.toFileURL(imgUrl);
			URL path = FileLocator.resolve(imgFileURL);
			String imgpath = path.getFile();
			image = new Image(null, new FileInputStream(imgpath));
			setTitleImage(image);
		} catch (Exception e) {
			PluginUtil.displayErrorDialogAndLog(getShell(),
					Messages.errTtl,
					com.persistent.winazureroles.Messages.lclDlgImgErr, e);
		}
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		Control ctrl = super.createButtonBar(parent);
		okButton = getButton(IDialogConstants.OK_ID);
		if (!isEdit) {
			okButton.setEnabled(false);
		}
		return ctrl;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(Messages.strTxt);
		setMessage(Messages.strNmMsg);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				"com.persistent.winazure.eclipseplugin."
						+ "storage_account_dialog");
		Activator.getDefault().setSaved(false);
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
		createAccessKeyCmpnt(container);
		createURLCmpnt(container);

		if (isEdit) {
			txtName.setEditable(false);
			txtUrl.setEditable(false);
			txtName.setText(account.getStrgName());
			txtKey.setText(account.getStrgKey());
			txtUrl.setText(account.getStrgUrl());
		} else {
			txtUrl.setText(constructURL(""));
		}
		return super.createDialogArea(parent);
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
	 * Create URL UI components and its listeners.
	 * @param container
	 */
	private void createURLCmpnt(Composite container) {
		Label lblUrl = new Label(container, SWT.LEFT);
		GridData gridData = gridDataForLbl();
		lblUrl.setText(Messages.strUrlTxt);
		lblUrl.setLayoutData(gridData);

		txtUrl = new Text(container, SWT.LEFT | SWT.BORDER);
		gridData = gridDataForText(180);
		txtUrl.setLayoutData(gridData);

		txtUrl.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent arg0) {
				if (isEdit) {
					setMessage("");
				} else {
					setMessage(Messages.strUrlMsg);
				}
			}

			@Override
			public void focusLost(FocusEvent arg0) {
			}
		});

		txtUrl.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent event) {
				enableOkBtn();
				/*
				 * Modify storage account name,
				 * if storage account name from URL
				 * is modified.
				 */
				if (!isEdit) {
					String url = txtUrl.getText();
					String nameInUrl = StorageRegistryUtilMethods.
							getAccNameFromUrl(url);
					if (nameInUrl != null
							&& !nameInUrl.equalsIgnoreCase(
									txtName.getText().trim())) {
						txtName.setText(nameInUrl);
					}
				}
			}
		});

		new Link(container, SWT.NO);
	}

	/**
	 * Create access key UI components and its listeners.
	 * @param container
	 */
	private void createAccessKeyCmpnt(Composite container) {
		Label lblKey = new Label(container, SWT.LEFT);
		GridData gridData = gridDataForLbl();
		lblKey.setLayoutData(gridData);
		lblKey.setText(Messages.strAccKeyTxt);

		txtKey = new Text(container, SWT.LEFT | SWT.BORDER);
		gridData = gridDataForText(180);
		txtKey.setLayoutData(gridData);

		txtKey.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent arg0) {
				setMessage(Messages.strKeyMsg);
			}

			@Override
			public void focusLost(FocusEvent arg0) {
			}
		});

		txtKey.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent event) {
				enableOkBtn();
			}
		});

		new Link(container, SWT.NO);
	}

	/**
	 * Create storage account name UI components and its listeners.
	 * @param container
	 */
	private void createNameCmpnt(Composite container) {
		Label lblName = new Label(container, SWT.LEFT);
		GridData gridData = gridDataForLbl();
		lblName.setLayoutData(gridData);
		lblName.setText(Messages.strDlgNmTxt);

		txtName = new Text(container, SWT.LEFT | SWT.BORDER);
		gridData = gridDataForText(180);
		txtName.setLayoutData(gridData);

		txtName.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent arg0) {
				if (isEdit) {
					setMessage("");
				} else {
					setMessage(Messages.strNmMsg);
				}
			}

			@Override
			public void focusLost(FocusEvent arg0) {
			}
		});

		txtName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent event) {
				enableOkBtn();
				/*
				 * update URL text as per name text.
				 */
				if (!isEdit) {
					String name = txtName.getText().trim();
					String url = txtUrl.getText().trim();
					if (name.isEmpty() && url.isEmpty()) {
						txtUrl.setText(constructURL(""));
					} else {
						syncUpAccNameAndNameInUrl(name, url);
					}
				}
			}
		});

		newStrAccBtn =  new Button(container, SWT.PUSH);
		newStrAccBtn.setText("New...");
		gridData = gridDataForText(40);
		newStrAccBtn.setLayoutData(gridData);
		/*
		 * If any subscription is present then
		 * only enable New... button.
		 */
		if (WizardCacheManager.
				getCurrentPublishData() != null) {
			newStrAccBtn.setEnabled(true);
		} else {
			newStrAccBtn.setEnabled(false);
		}
		newStrAccBtn.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
					NewStorageAccountDialog storageAccountDialog =
							new NewStorageAccountDialog(getShell(), "");
					int result = storageAccountDialog.open();
					// populate data in storage registry dialog
					if (result == Window.OK) {
						StorageService service = NewStorageAccountDialog.getStorageService();
						if (service != null) {
							txtName.setText(service.getServiceName());
							txtKey.setText(service.getPrimaryKey());
							txtUrl.setText(service.getStorageAccountProperties().getEndpoints().get(0).toString());
						}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
	}

	/**
	 * Method is to update blob URL text
	 * in order to keep storage account name and
	 * storage account name's substring from URL
	 * in sync.
	 * @param name
	 * @param url
	 */
	private void syncUpAccNameAndNameInUrl(String name, String url) {
		String nameInUrl = StorageRegistryUtilMethods.
				getAccNameFromUrl(url);
		if (nameInUrl != null
				&& !name.equalsIgnoreCase(nameInUrl)) {
			String rplcNameInUrl = "//" + nameInUrl;
			String rplcName = "//" + name;
			txtUrl.setText(url.replaceFirst(rplcNameInUrl, rplcName));
		}
	}

	/**
	 * Method enables or disables OK button.
	 * Disable OK button if PFX path or password are empty.
	 */
	private void enableOkBtn() {
		if (okButton != null) {
			if (txtName.getText().trim().isEmpty()
					|| txtKey.getText().trim().isEmpty()
					|| txtUrl.getText().trim().isEmpty()) {
				okButton.setEnabled(false);
			} else {
				okButton.setEnabled(true);
			}
		}
	}

	@Override
	protected void okPressed() {
		boolean isValid = false;
		String error = PreferenceUtilMethods.
				storageAccDlgOkPressed(isEdit,
						txtKey.getText().trim(),
						txtName.getText().trim(),
						txtUrl.getText().trim(), account);
		if (error != null && error.isEmpty()) {
			isValid = true;
		} else {
			PluginUtil.displayErrorDialog(getShell(),
					Messages.errTtl, error);
		}
		if (isValid) {
			super.okPressed();
		}
	}

	/**
	 * Method constructs URL as per preference sets file.
	 * @param storageName
	 * @return
	 */
	private String constructURL(String storageName) {
		String url = "";
		try {
			url = PreferenceSetUtil.
					getSelectedBlobServiceURL(storageName, PluginUtil.getPrefFilePath());
		} catch (Exception e) {
			Activator.getDefault().log(Messages.errTtl, e);
		}
		return url;
	}
}
