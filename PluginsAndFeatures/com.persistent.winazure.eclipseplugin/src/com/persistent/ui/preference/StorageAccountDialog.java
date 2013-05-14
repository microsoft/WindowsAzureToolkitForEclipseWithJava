/**
 * Copyright 2013 Persistent Systems Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.persistent.ui.preference;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import waeclipseplugin.Activator;

import com.microsoftopentechnologies.wacommon.storageregistry.StorageAccount;
import com.microsoftopentechnologies.wacommon.storageregistry.StorageAccountRegistry;
import com.microsoftopentechnologies.wacommon.storageregistry.StorageRegistryUtilMethods;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
import com.microsoftopentechnologies.wacommon.utils.PreferenceSetUtil;
import com.persistent.util.WAEclipseHelper;

public class StorageAccountDialog extends TitleAreaDialog {
	private Text txtName;
	private Text txtKey;
	private Text txtUrl;
	private boolean isEdit;
	StorageAccount account;
	private Button okButton;

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
		gridLayout.numColumns = 2;
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
	private GridData gridDataForText() {
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.END;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.widthHint = 180;
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
		gridData = gridDataForText();
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
		gridData = gridDataForText();
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
		gridData = gridDataForText();
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
		// edit scenario.
		if (isEdit) {
			// check access key is changed, then edit else not.
			String newKey = txtKey.getText().trim();
			if (!account.getStrgKey().equals(newKey)) {
				if (newKey.contains(" ")) {
					isValid = false;
					PluginUtil.displayErrorDialog(getShell(),
							Messages.keyErrTtl,
							Messages.keyErrMsg);
				} else {
					isValid = true;
					StorageAccountRegistry.
					editAccountAccessKey(account, newKey);
				}
			} else {
				isValid = true;
			}
		} else {
			// add scenario.
			// validate account name
			if (validateName()) {
				// validate URL
				String name = txtName.getText().trim();
				try {
					String url = txtUrl.getText().trim();
					// append '/' if not present.
					if (!url.endsWith("/")) {
						url = url + "/";
					}
					if (url.equalsIgnoreCase(name + Messages.blobEnPt)) {
						url = String.format("%s%s%s",
								Messages.http,
								name,
								Messages.blobEnPt);
					}
					new URL(url);
					if (url.startsWith(Messages.http + name + '.')
							|| url.startsWith(Messages.https + name + '.')) {
						// validate access key
						String key = txtKey.getText().trim();
						if (key.contains(" ")) {
							isValid = false;
							PluginUtil.displayErrorDialog(getShell(),
									Messages.keyErrTtl,
									Messages.keyErrMsg);
						} else {
							// check account does not exist previously
							StorageAccount account = new StorageAccount(
									txtName.getText().trim(),
									key,
									url);
							if (!StorageAccountRegistry.getStrgList().contains(account)) {
								StorageAccountRegistry.addAccount(account);
								isValid = true;
							} else {
								isValid = false;
								PluginUtil.displayErrorDialog(getShell(),
										Messages.errTtl,
										Messages.urlPreErrMsg);
							}
						}
					} else {
						isValid = false;
						PluginUtil.displayErrorDialog(getShell(),
								Messages.errTtl, Messages.urlErMsg);
					}
				} catch (MalformedURLException e) {
					isValid = false;
					PluginUtil.displayErrorDialog(getShell(),
							Messages.errTtl, Messages.urlErMsg);
				}
			} else {
				isValid = false;
			}
		}
		if (isValid) {
			super.okPressed();
		}
	}

	/**
	 * Method validates storage account name.
	 * @return
	 */
	private boolean validateName() {
		Boolean isVallidName = false;
		String name = txtName.getText().trim();
		if (WAEclipseHelper.isLowerCaseAndInteger(name)) {
			if (name.length() >= 3
					&& name.length() <= 24) {
				isVallidName = true;
			} else {
				isVallidName = false;
				PluginUtil.displayErrorDialog(getShell(),
						Messages.errTtl, Messages.namelnErMsg);
			}
		} else {
			isVallidName = false;
			PluginUtil.displayErrorDialog(getShell(),
					Messages.errTtl, Messages.nameRxErMsg);
		}
		return isVallidName;
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
					getSelectedBlobServiceURL(storageName);
		} catch (Exception e) {
			Activator.getDefault().log(Messages.errTtl, e);
		}
		return url;
	}
}
