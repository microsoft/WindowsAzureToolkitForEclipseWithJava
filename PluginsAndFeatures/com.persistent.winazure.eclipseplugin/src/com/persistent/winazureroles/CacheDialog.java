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
package com.persistent.winazureroles;

import java.io.FileInputStream;
import java.net.URL;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import waeclipseplugin.Activator;

import com.interopbridges.tools.windowsazure.WindowsAzureCacheExpirationPolicy;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureNamedCache;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.microsoftopentechnologies.azurecommons.exception.AzureCommonsException;
import com.microsoftopentechnologies.azurecommons.roleoperations.CacheDialogUtilMethods;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
/**
 * Class creates UI controls and respective listeners
 * for Named Cache dialog.
 */
public class CacheDialog extends TitleAreaDialog {
	private WindowsAzureRole windowsAzureRole;
	private Map<String, WindowsAzureNamedCache> cacheMap;
	private boolean isEdit;
	private String cacheName;
	private Text txtCacheName;
	private Label lblName;
	private Text txtPortNum;
	private Label lblPortNum;
	private Label lblPolicy;
	private Combo comboExpPolicy;
	private Label minLive; 
	private Text txtMinLive;
	private Button backupCheck;
	private Label backupLbl;
	/**
	 * Cache expiration policies.
	 */
	private static String[] arrType = {Messages.expPolNvrExp,
		Messages.expPolAbs,
		Messages.expPolSlWn};

	/**
	 * Constructor to be called for add cache.
	 * @param parentShell
	 * @param mapCache : map containing all cache entries.
	 */
	public CacheDialog(Shell parentShell,
			Map<String, WindowsAzureNamedCache> mapCache) {
		super(parentShell);
		windowsAzureRole = Activator.getDefault().getWaRole();
		cacheMap = mapCache;
	}

	/**
	 * Constructor to be called for edit cache.
	 * @param parentShell
	 * @param mapCache : map containing all cache entries.
	 * @param windowsAzureRole
	 * @param key
	 */
	public CacheDialog(Shell parentShell,
			Map<String, WindowsAzureNamedCache> mapCache,
			WindowsAzureRole windowsAzureRole,
			String key) {
		super(parentShell);
		this.cacheMap = mapCache;
		this.windowsAzureRole = windowsAzureRole;
		this.isEdit = true;
		this.cacheName = key;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.cacheTtl);
		newShell.setLocation(250, 250);
		Image image;
		try {
			URL imgUrl = Activator.getDefault().
					getBundle().getEntry(Messages.cachDlgImg);
			URL imgFileURL = FileLocator.toFileURL(imgUrl);
			URL path = FileLocator.resolve(imgFileURL);
			String imgpath = path.getFile();
			image = new Image(null, new FileInputStream(imgpath));
			setTitleImage(image);
		} catch (Exception e) {
			PluginUtil.displayErrorDialogAndLog(getShell(),
					Messages.genErrTitle,
					Messages.lclDlgImgErr, e);
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(Messages.cachTxt);
		setMessage(Messages.cachMsg);
		// Display help contents
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				"com.persistent.winazure.eclipseplugin."
						+ "windows_azure_cache_dialog");
		Activator.getDefault().setSaved(false);
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		GridData gridData = new GridData();
		gridLayout.marginBottom = 50;
		gridData.widthHint = 550;
		gridData.verticalIndent = 10;
		container.setLayout(gridLayout);
		container.setLayoutData(gridData);

		// Create UI components of named cache
		createName(container);
		createPortNum(container);
		createExpPolicy(container);
		createMinToLive(container);
		createBackupCheck(container);

		// If Edit button is pressed populate data of selected resource
		if (isEdit) {
			populateData();
		}

		return super.createDialogArea(parent);
	}

	/**
	 * Populates the cache name and value text fields with the corresponding
	 * attributes of named cache resource selected for editing.
	 */
	private void populateData() {
		try {
			WindowsAzureNamedCache cache = cacheMap.get(cacheName);
			txtCacheName.setText(cache.getName());
			/*
			 * Disable cache name text box if default cache
			 * is selected for editing
			 * as renaming default cache is not allowed.
			 */
			if (cache.getName().
					equalsIgnoreCase(Messages.
							dfltCachName)) {
				txtCacheName.setEnabled(false);
			}
			txtPortNum.setText(cache.getEndpoint().
					getPrivatePort());
			comboExpPolicy.setText(CacheDialogUtilMethods.getExpPolStr(cache));
			/*
			 * Check if expiration policy is NEVER_EXPIRES
			 * then disable minutes to live text box
			 * and set value to N/A
			 */
			if (cache.getExpirationPolicy().
					equals(WindowsAzureCacheExpirationPolicy.
							NEVER_EXPIRES)) {
				txtMinLive.setText(Messages.dlgDbgNA);
				txtMinLive.setEnabled(false);
			} else {
				txtMinLive.setText(Integer.toString(cache.
						getMinutesToLive()));
			}
			backupCheck.setSelection(cache.getBackups());
		} catch (WindowsAzureInvalidProjectOperationException e) {
			PluginUtil.displayErrorDialogAndLog(getShell(),
					Messages.cachErrTtl,
					Messages.cachGetErMsg, e);
		}
	}

	/**
	 * Creates the backup check box component.
	 * @param container
	 */
	private void createBackupCheck(Composite container) {
		backupCheck = new Button(container, SWT.CHECK);
		GridData gridData = new GridData();
		gridData.verticalIndent = 10;
		gridData.horizontalIndent = 10;
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		backupCheck.setText(Messages.backupLbl1);
		backupCheck.setLayoutData(gridData);
		backupCheck.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				/*
				 * If user selects backup option
				 * then check virtual machine instances > 2
				 * otherwise give warning
				 */
				if (backupCheck.getSelection()) {
					int vmCnt = 0;
					try {
						vmCnt = Integer.parseInt(
								windowsAzureRole.getInstances());
					} catch (Exception e) {
						PluginUtil.displayErrorDialogAndLog(
								getShell(),
								Messages.genErrTitle,
								Messages.vmInstGetErMsg, e);
					}
					if (vmCnt < 2) {
						/*
						 * If virtual machine instances < 2
						 * then make back up check-box unchecked.
						 */
						backupCheck.setSelection(false);
						MessageDialog.openWarning(
								getShell(), Messages.backWarnTtl,
								Messages.backWarnMsg);
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		backupLbl = new Label(container, SWT.RIGHT);
		gridData = new GridData();
		gridData.horizontalIndent = 25;
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		backupLbl.setText(Messages.backupLbl2);
		backupLbl.setLayoutData(gridData);
	}

	/**
	 * Creates the minutes to live component.
	 * @param container
	 */
	private void createMinToLive(Composite container) {
		minLive = new Label(container, SWT.LEFT);
		GridData gridData = new GridData();
		gridData.horizontalIndent = 10;
		gridData.verticalIndent = 5;
		minLive.setText(Messages.minLiveLbl);
		minLive.setLayoutData(gridData);

		txtMinLive = new Text(container, SWT.LEFT |  SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalIndent = 30;
		gridData.verticalIndent = 5;
		gridData.widthHint = 343;
		gridData.horizontalAlignment = GridData.END;
		gridData.grabExcessHorizontalSpace = true;
		txtMinLive.setLayoutData(gridData);
	}

	/**
	 * Creates the expiration policy component.
	 * @param container
	 */
	private void createExpPolicy(Composite container) {
		lblPolicy = new Label(container, SWT.LEFT);
		GridData gridData = new GridData();
		gridData.horizontalIndent = 10;
		gridData.verticalIndent = 5;
		lblPolicy.setText(Messages.expLbl);
		lblPolicy.setLayoutData(gridData);

		comboExpPolicy = new Combo(container, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.horizontalIndent = 30;
		gridData.verticalIndent = 5;
		gridData.widthHint = 327;
		gridData.horizontalAlignment = GridData.END;
		gridData.grabExcessHorizontalSpace = true;
		comboExpPolicy.setLayoutData(gridData);
		comboExpPolicy.setItems(arrType);
		comboExpPolicy.setText(arrType[1]);
		comboExpPolicy.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				/*
				 * Check if expiration policy is NEVER_EXPIRES
				 * then disable minutes to live text box
				 * and set value to N/A
				 * else enable text box
				 * and set value to default i.e 10
				 */
				if (comboExpPolicy.getText().
						equals(Messages.expPolNvrExp)) {
					txtMinLive.setText(Messages.dlgDbgNA);
					txtMinLive.setEnabled(false);
				} else {
					txtMinLive.setEnabled(true);
					txtMinLive.setText("");
				}
			}
		});
	}

	/**
	 * Creates the port number component.
	 * @param container
	 */
	private void createPortNum(Composite container) {
		lblPortNum = new Label(container, SWT.LEFT);
		GridData gridData = new GridData();
		gridData.horizontalIndent = 10;
		gridData.verticalIndent = 5;
		lblPortNum.setText(Messages.portNumLbl);
		lblPortNum.setLayoutData(gridData);

		txtPortNum = new Text(container, SWT.LEFT | SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalIndent = 30;
		gridData.verticalIndent = 5;
		gridData.widthHint = 343;
		gridData.horizontalAlignment = GridData.END;
		gridData.grabExcessHorizontalSpace = true;
		txtPortNum.setLayoutData(gridData);
	}

	/**
	 * Creates the cache name component.
	 * @param container
	 */
	private void createName(Composite container) {
		lblName = new Label(container, SWT.LEFT);
		GridData gridData = new GridData();
		gridData.horizontalIndent = 10;
		gridData.verticalIndent = 5;
		lblName.setText(Messages.adRolName);
		lblName.setLayoutData(gridData);

		txtCacheName = new Text(container, SWT.LEFT | SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalIndent = 30;
		gridData.verticalIndent = 5;
		gridData.widthHint = 343;
		gridData.horizontalAlignment = GridData.END;
		gridData.grabExcessHorizontalSpace = true;
		txtCacheName.setLayoutData(gridData);
	}

	@Override
	protected void okPressed() {
		boolean okToProceed = true;
		String cachNameTxt = txtCacheName.getText().trim();
		String port = txtPortNum.getText().trim();
		String mtl = txtMinLive.getText().trim();
		try {
			// Check values for all fields are specified
			if (!txtCacheName.getText().equals("")
					&& !txtPortNum.getText().equals("")
					&& !txtMinLive.getText().equals("")) {
				/*
				 * Check if expiration policy is NEVER_EXPIRES
				 * then only validate cache name and port number
				 * else all three fields
				 * name, port, minutes to live.
				 */
				if (comboExpPolicy.getText().
						equals(Messages.expPolNvrExp)) {
					okToProceed = isValidName(cachNameTxt)
							&& validatePort(port);
				} else {
					okToProceed = isValidName(cachNameTxt)
							&& validatePort(port)
							&& validateMtl(mtl);
				}
			} else {
				okToProceed = false;
				PluginUtil.displayErrorDialog(getShell(),
						Messages.cachErrTtl,
						Messages.cachSpcfyAll);
			}

			// Edit case
			if (isEdit && okToProceed) {
				WindowsAzureNamedCache namedCache =
						cacheMap.get(cacheName);
				/*
				 * If cache name is edited
				 * then set name to newer value.
				 */
				if (!namedCache.getName()
						.equals(cachNameTxt)) {
					namedCache.setName(cachNameTxt);
					cacheName = cachNameTxt;
				}
				namedCache.getEndpoint().setPrivatePort(port);
				setCacheAttributes(namedCache);
			} else if (!isEdit && okToProceed) {
				WindowsAzureNamedCache namedCache = 
						windowsAzureRole.addNamedCache(
								cachNameTxt,
								Integer.parseInt(port));
				setCacheAttributes(namedCache);
			}
		} catch (Exception e) {
			okToProceed = false;
			PluginUtil.displayErrorDialogAndLog(getShell(),
					Messages.cachErrTtl,
					Messages.cachSetErrMsg, e);
		}
		if (okToProceed) {
			super.okPressed();
		}
	}

	/**
	 * Validates the name of cache.
	 * @param name
	 * @return Boolean
	 */
	private Boolean isValidName(String name) {
		boolean retVal = true;
		try {
			retVal = CacheDialogUtilMethods.isValidName(name,
					cacheMap, isEdit, cacheName);
		} catch (AzureCommonsException e) {
			retVal = false;
			PluginUtil.displayErrorDialogAndLog(getShell(),
					Messages.genErrTitle,
					e.getMessage(), e);
		}
		return retVal;
	}

	/**
	 * Validates the Minutes to live attribute of named cache.
	 * Value must be numeric and should be at least 0
	 * @param minToLive
	 * @return Boolean
	 */
	private Boolean validateMtl(String minToLive) {
		Boolean isVallidMtl = CacheDialogUtilMethods.
				validateMtl(minToLive);
		if (!isVallidMtl) {
			PluginUtil.displayErrorDialog(getShell(),
					Messages.cachMtlErrTtl,
					Messages.cachMtlErrMsg);
		}
		return isVallidMtl;
	}

	/**
	 * Validates the port number of named cache.
	 * Positive integer between 1 to 65535 is allowed.
	 * @param port
	 * @return Boolean
	 */
	private Boolean validatePort(String port) {
		Boolean isValidPortRng = false;
		try {
			isValidPortRng = CacheDialogUtilMethods.validatePort(port, cacheMap, cacheName, isEdit,
					txtCacheName.getText(), windowsAzureRole);
		} catch (AzureCommonsException e) {
			PluginUtil.displayErrorDialogAndLog(getShell(), Messages.genErrTitle, e.getMessage(), e);
		}
		return isValidPortRng;
	}

	/**
	 * Function sets values of named cache attributes
	 * like backup option, expiration policy and minutes to live.
	 * @param namedCache
	 */
	private void setCacheAttributes(WindowsAzureNamedCache namedCache) {
		String expPolCmbTxt = comboExpPolicy.getText();
		/*
		 * Mapping of expiration policies shown on UI
		 * to actual values stored in project manager object
		 */
		WindowsAzureCacheExpirationPolicy expPol =
				CacheDialogUtilMethods.getExpPolObject(expPolCmbTxt);
		try {
			CacheDialogUtilMethods.setCacheAttributes(namedCache,
					expPolCmbTxt,
					backupCheck.getSelection(),
					expPol,
					txtMinLive.getText().trim());
		} catch (Exception e) {
			PluginUtil.displayErrorDialogAndLog(getShell(),
					Messages.cachErrTtl,
					Messages.cachSetErrMsg, e);
		}
	}
}
