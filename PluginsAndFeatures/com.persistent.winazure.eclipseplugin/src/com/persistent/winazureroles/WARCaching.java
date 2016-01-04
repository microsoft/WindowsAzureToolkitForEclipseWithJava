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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

import com.interopbridges.tools.windowsazure.WindowsAzureCacheExpirationPolicy;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureNamedCache;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.microsoftopentechnologies.azurecommons.exception.AzureCommonsException;
import com.microsoftopentechnologies.azurecommons.roleoperations.WARCachingUtilMethods;
import com.microsoftopentechnologies.azurecommons.storageregistry.StorageAccountRegistry;
import com.microsoftopentechnologies.azurecommons.storageregistry.StorageRegistryUtilMethods;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
import com.persistent.util.JdkSrvConfig;
import com.persistent.util.WAEclipseHelper;

import waeclipseplugin.Activator;

/**
 * Property page for Caching.
 */

public class WARCaching extends PropertyPage {
	private WindowsAzureProjectManager waProjManager;
	private WindowsAzureRole wARole;
	private Button cacheCheck;
	private Label enblCacheNote;
	private Label scaleLbl;
	private Scale cacheScale;
	private Text txtCache;
	private Text txtHostName;
	private Table tblCache;
	private Button btnRemove;
	private Button btnAdd;
	private Button btnEdit;
	private Label explNtLbl;
	private Label hostNameLbl;
	private Group strGrp;
	private Combo comboStrgAcc;
	private Link accLink;
	private Label crdntlLbl;
	private TableViewer tableViewer;
	private Map<String, WindowsAzureNamedCache> mapCache;
	private final String dashAuto = "-auto";
	/**
	 * Array to store and display
	 * expiration policy types in table column.
	 */
	private static String[] arrExpPolType = {Messages.expPolNvrExp,
		Messages.expPolAbs,
		Messages.expPolSlWn};
	/**
	 * Array to store and display
	 * backup option in table column.
	 */
	private static String[] arrBackOpt = {Messages.cachBckYes,
		Messages.cachBckNo};
	/**
	 * Default cache memory size.
	 */
	private final static int CACH_DFLTVAL = 30;
	/**
	 * Boolean field to track whether
	 * cache memory size is set to valid value or not.
	 */
	private Boolean isCachPerValid = true;
	private boolean isPageDisplayed = false;

	@Override
	public String getTitle() {
		if (!isPageDisplayed) {
			return super.getTitle();
		}
		/* Check Cache memory setting is present or not
		 * and if enabled show appropriate values on property page */
		int cachePercent = 0;
		try {
			cachePercent = wARole.getCacheMemoryPercent();
		} catch (WindowsAzureInvalidProjectOperationException e) {
			PluginUtil.displayErrorDialogAndLog(
					getShell(),
					Messages.cachErrTtl,
					Messages.getCachMemErMsg, e);
		}
		if (cachePercent > 0) {
			setEnableCaching(true);
			cacheCheck.setSelection(true);
			txtCache.setText(String.format("%s%s",
					cachePercent, "%"));
			cacheScale.setSelection(cachePercent);
			txtHostName.setText(String.format("%s%s%s",
					Messages.dlgDbgLclHost, "_",
					wARole.getName().toLowerCase()));
			try {
				String accKey = wARole.
						getCacheStorageAccountKey();
				comboStrgAcc = JdkSrvConfig.
						populateStrgNameAsPerKey(accKey, comboStrgAcc);
			} catch (WindowsAzureInvalidProjectOperationException e) {
				PluginUtil.displayErrorDialogAndLog(
						getShell(),
						Messages.cachErrTtl,
						Messages.getStrAccErrMsg, e);
			}

		} else {
			cacheCheck.setSelection(false);
			setEnableCaching(false);
		}
		if (tableViewer != null) {
			tableViewer.refresh();
		}
		return super.getTitle();
	}

	/**
	 * Create check box to enable/disable caching,
	 * scale and synchronized text box to accept values,
	 * cache table, account name and key text boxes, add button
	 * to launch a dialog to create cache configurations.
	 *
	 * @param parent : parent composite.
	 * @return control
	 */
	@Override
	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				"com.persistent.winazure.eclipseplugin."
						+ "windows_azure_caching_page");
		waProjManager = Activator.getDefault().getWaProjMgr();
		wARole = Activator.getDefault().getWaRole();
		Activator.getDefault().setSaved(false);

		Composite container = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		container.setLayout(gridLayout);
		container.setLayoutData(gridData);

		// Create cache check box which enables caching.
		createCacheCheckButton(container);

		// Create label of enable cache note and size
		enblCacheNote = new Label(container, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalIndent = 4;
		gridData.horizontalIndent = 5;
		gridData.horizontalSpan = 3;
		enblCacheNote.setText(Messages.enblCachNtLbl);
		enblCacheNote.setLayoutData(gridData);

		// Create scale and its associated components
		createScale(container);

		// Create cache table
		createCacheTable(container);

		// Create explanatory note
		createExplanatoryNote(container);

		// Create storage group
		createStorageGroup(container);
		isPageDisplayed = true;
		return container;
	}

	/**
	 * Creates the cache check button and adds selection listener to it.
	 * This check box enables/disables the caching.
	 * @param container
	 */
	private void createCacheCheckButton(Composite container) {
		cacheCheck = new Button(container, SWT.CHECK);
		GridData gridData = new GridData();
		gridData.verticalIndent = 5;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 3;
		cacheCheck.setText(Messages.cachChkBoxTxt);
		cacheCheck.setLayoutData(gridData);

		cacheCheck.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (cacheCheck.getSelection()) {
					if(Messages.txtExtraSmallVM.equals(wARole.getVMSize())) {
						PluginUtil.displayErrorDialog(getShell(),Messages.cacheConfTitle,
		                         Messages.cacheConfErrMsg);
						cacheCheck.setSelection(false);
						return;
					}
					setEnableCaching(true);
					/* Set cache memory percent
					 * to default value 30
					 */
					setCachPerMem(CACH_DFLTVAL);
					cacheScale.setSelection(CACH_DFLTVAL);
					txtCache.setText(String.format("%s%s",
							CACH_DFLTVAL, "%"));
					txtHostName.setText(String.format("%s%s%s",
							Messages.dlgDbgLclHost, "_",
							wARole.getName().toLowerCase()));
					setName(dashAuto);
				} else {
					setEnableCaching(false);
					/* Set cache memory percent to 0
					 *  to disable cache.
					 *  Also set storage account name
					 *  and key to empty.
					 */
					setCachPerMem(0);
					setName("");
					setKey("");
					setBlobUrl("");
				}
				/*
				 *  Necessary to refresh table
				 *  to show or remove default
				 *  named cache added by PML.
				 */
				tableViewer.refresh();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
	}

	/**
	 * Create the scale for adjusting cache size with synchronized text box.
	 * @param container
	 */
	private void createScale(Composite container) {
		scaleLbl = new Label(container, SWT.LEFT);
		GridData gridData = new GridData();
		gridData.verticalIndent = 6;
		gridData.horizontalIndent = 5;
		scaleLbl.setText(Messages.cachScaleLbl);
		scaleLbl.setLayoutData(gridData);

		cacheScale = new Scale(container, SWT.HORIZONTAL);
		gridData = new GridData();
		gridData.verticalIndent = 6;
		gridData.horizontalIndent = 135;
		cacheScale.setMinimum(10);
		cacheScale.setMaximum(100);
		cacheScale.setIncrement(1);
		cacheScale.setPageIncrement(10);
		cacheScale.setLayoutData(gridData);

		/**
		 * Displays scale's position in synchronized cache text box.
		 * Listener handles keyboard's keys
		 * (up/down or right/left arrows) press event
		 * and mouse's drag, move events
		 */
		cacheScale.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				txtCache.setText(String.format("%s%s",
						cacheScale.getSelection(), "%"));
				setCachPerMem(cacheScale.getSelection());
			}
		});

		txtCache = new Text(container, SWT.RIGHT | SWT.BORDER);
		gridData = new GridData();
		gridData.widthHint = 56;
		gridData.verticalIndent = 6;
		txtCache.setLayoutData(gridData);

		/**
		 * Adjusts scales's position, according to value
		 * entered in the synchronized cache text box.
		 */
		txtCache.addFocusListener(new FocusListener() {
			private String oldTxt = "";
			@Override
			public void focusLost(FocusEvent arg0) {
				if (!txtCache.getText().equals(oldTxt)) {
					int cacheVal = 0;
					Boolean isNumber = true;
					try {
						/*
						 * As '%' is allowed in user's input,
						 * check if '%' is present already
						 * then ignore '%' and take only numeric value
						 */
						if (txtCache.getText().endsWith("%")) {
							cacheVal = Integer.parseInt(
									txtCache.getText().trim().substring(0,
											txtCache.getText().length() - 1));
						} else {
							cacheVal = Integer.parseInt(txtCache.getText().trim());
							txtCache.setText(
									String.format("%s%s", cacheVal, "%"));
						}
					} catch (NumberFormatException e) {
						/*
						 * User has given alphabet
						 * or special character as input
						 * for cache memory size.
						 */
						isNumber = false;
					}

					/*
					 * Check cache memory size input
					 * is numeric and has value > 0
					 */
					if (isNumber
							&& cacheVal >= 10
							&& cacheVal <= 100) {
						setCachPerMem(cacheVal);
						cacheScale.setSelection(cacheVal);
					} else {
						/*
						 * User has given zero
						 * or negative number as input.
						 */
						isCachPerValid = false;
					}
				}
			}

			@Override
			public void focusGained(FocusEvent arg0) {
				oldTxt = txtCache.getText();
			}
		});
	}

	/**
	 * Create cache table.
	 * @param container
	 */
	private void createCacheTable(Composite container) {
		tblCache = new Table(container, SWT.BORDER
				| SWT.FULL_SELECTION);
		tblCache.setHeaderVisible(true);
		tblCache.setLinesVisible(true);
		GridData gridData = new GridData();
		gridData.heightHint = 90;
		gridData.horizontalIndent = 5;
		gridData.verticalIndent = 5;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = false;
		tblCache.setLayoutData(gridData);

		// Create columns
		TableColumn cacheName = new TableColumn(tblCache, SWT.FILL);
		cacheName.setText(Messages.colChName);
		cacheName.setWidth(90);

		TableColumn colBckp = new TableColumn(tblCache, SWT.FILL);
		colBckp.setText(Messages.colBkps);
		colBckp.setWidth(100);

		TableColumn colExprtn = new TableColumn(tblCache, SWT.FILL);
		colExprtn.setText(Messages.colExp);
		colExprtn.setWidth(105);

		TableColumn colMinToLive =
				new TableColumn(tblCache, SWT.FILL);
		colMinToLive.setText(Messages.colMinToLive);
		colMinToLive.setWidth(95);

		TableColumn colPort = new
				TableColumn(tblCache, SWT.FILL);
		colPort.setText(Messages.colPort);
		colPort.setWidth(60);

		/* Enable edit and remove button only when
		 * entry from table is selected.
		 */
		tblCache.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				btnEdit.setEnabled(true);
				/*
				 * Disable remove button if default cache
				 * is selected for removal
				 * as removal of default cache is not allowed.
				 */
				try {
					Entry<String, WindowsAzureNamedCache> cachEntry =
							getSelNamedCache();
					if (cachEntry.getKey().
							equalsIgnoreCase(Messages.dfltCachName)) {
						btnRemove.setEnabled(false);
					} else {
						btnRemove.setEnabled(true);
					}
				} catch (Exception e) {
					btnRemove.setEnabled(false);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {

			}
		});

		//Composite for buttons
		final Composite containerButtons =
				new Composite(container, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		GridData cntGridData = new GridData();
		cntGridData.verticalAlignment = SWT.FILL;
		containerButtons.setLayout(gridLayout);
		containerButtons.setLayoutData(cntGridData);

		createAddButton(containerButtons);
		createEditButton(containerButtons);
		createRemoveButton(containerButtons);

		createTableViewer();

		tableViewer.setContentProvider(new EPTableContentProvider());
		tableViewer.setLabelProvider(new EPTableLabelProvider());
		tableViewer.setCellModifier(new CellModifier());

		// Display previously added named caches if any
		try {
			mapCache = wARole.getNamedCaches();
			tableViewer.setInput(mapCache.entrySet().toArray());
		} catch (WindowsAzureInvalidProjectOperationException e) {
			PluginUtil.displayErrorDialogAndLog(
					getShell(),
					Messages.cachErrTtl,
					Messages.cachGetErMsg, e);
		}
	}

	/**
	 * Creates 'Add' button and adds selection listener to it.
	 *
	 * @param containerButtons
	 */
	private void createAddButton(Composite containerButtons) {
		btnAdd =
				new Button(containerButtons, SWT.PUSH);
		btnAdd.setText(Messages.rolsAddBtn);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalIndent = 5;
		gridData.widthHint = 65;
		btnAdd.setLayoutData(gridData);
		btnAdd.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				addBtnListener();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {

			}
		});
	}

	/**
	 * Creates 'Edit' button and adds selection listener to it.
	 *
	 * @param containerButtons
	 */
	private void createEditButton(Composite containerButtons) {
		btnEdit = new Button(containerButtons, SWT.PUSH);
		btnEdit.setText(Messages.rolsEditBtn);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		btnEdit.setLayoutData(gridData);
		btnEdit.setEnabled(false);
		btnEdit.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				editBtnListener();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {

			}
		});
	}

	/**
	 * Creates 'Remove' button and adds selection listener to it.
	 *
	 * @param containerButtons
	 */
	private void createRemoveButton(Composite containerButtons) {
		btnRemove = new Button(containerButtons, SWT.PUSH);
		btnRemove.setText(Messages.dlgBtnRemove);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		btnRemove.setLayoutData(gridData);
		btnRemove.setEnabled(false);
		btnRemove.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				removeBtnListener();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {

			}
		});
	}

	/**
	 * Listener method for add button which opens a dialog
	 * to add new cache entry in cache table.
	 */
	protected void addBtnListener() {
		CacheDialog dialog = new CacheDialog(this.getShell(), mapCache);
		dialog.open();
		tableViewer.refresh();
	}

	/**
	 * Listener method for edit button which opens a dialog
	 * to edit cache entry.
	 */
	protected void editBtnListener() {
		Entry<String, WindowsAzureNamedCache>
		cachEntry = getSelNamedCache();
		CacheDialog dialog =
				new CacheDialog(getShell(),
						mapCache , wARole,
						cachEntry.getKey());
		dialog.open();
		tableViewer.refresh();
	}

	/**
	 * Listener method for remove button which
	 * deletes the selected cache entry.
	 */
	protected void removeBtnListener() {
		try {
			boolean choice = MessageDialog.openQuestion(getShell(),
					Messages.cachRmvTtl, Messages.cachRmvMsg);
			if (choice) {
				Entry<String, WindowsAzureNamedCache>
				cachEntry = getSelNamedCache();
				WindowsAzureNamedCache cachToDel =
						cachEntry.getValue();
				cachToDel.delete();
				tableViewer.refresh();
			}
		} catch (WindowsAzureInvalidProjectOperationException e) {
			PluginUtil.displayErrorDialogAndLog(
					getShell(),
					Messages.cachErrTtl,
					Messages.cachDelErMsg, e);
		}
	}

	/**
	 * Create explanatory note.
	 * @param container
	 */
	private void createExplanatoryNote(Composite container) {
		explNtLbl = new Label(container, SWT.LEFT);
		GridData gridData = new GridData();
		gridData.verticalIndent = 6;
		gridData.horizontalIndent = 5;
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		explNtLbl.setText(Messages.cachNtLbl);
		explNtLbl.setLayoutData(gridData);

		hostNameLbl = new Label(container, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalIndent = 5;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		hostNameLbl.setText(Messages.hostLbl);
		hostNameLbl.setLayoutData(gridData);

		txtHostName = new Text(container, SWT.BORDER | SWT.READ_ONLY);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		txtHostName.setLayoutData(gridData);
	}

	/**
	 * Create Storage group.
	 * @param container
	 */
	private void createStorageGroup(Composite container) {
		strGrp =  new Group(container, SWT.SHADOW_ETCHED_IN);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		GridData gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalIndent = 5;
		gridData.verticalIndent = 3;
		gridData.horizontalAlignment = SWT.FILL;
		strGrp.setText(Messages.strGrp);
		strGrp.setLayout(gridLayout);
		strGrp.setLayoutData(gridData);

		crdntlLbl = new Label(strGrp, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.horizontalSpan = 2;
		gridData.verticalIndent = 3;
		crdntlLbl.setText(Messages.crdntlLbl);
		crdntlLbl.setLayoutData(gridData);

		comboStrgAcc = new Combo(strGrp, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.widthHint = 350;
		comboStrgAcc.setLayoutData(gridData);
		comboStrgAcc.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setCacheNameKey();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		accLink = JdkSrvConfig.createLink(strGrp,
				Messages.linkLblAcc, false);
		accLink.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				comboStrgAcc = JdkSrvConfig.
						openAccLink(cacheCheck,
								comboStrgAcc, null);
				setCacheNameKey();
			}
		});
	}

	/**
	 * Method sets cache name and key
	 * as per storage account combo box value.
	 */
	private void setCacheNameKey() {
		String key = JdkSrvConfig.getAccessKey(comboStrgAcc);
		String url = JdkSrvConfig.getBlobEndpointUrl(comboStrgAcc);
		if (key.isEmpty()) {
			// auto is selected
			setName(dashAuto);
		} else {
			String name = StorageAccountRegistry.
					getStrgList().get(StorageRegistryUtilMethods.
							getStrgAccIndexAsPerKey(key)).getStrgName();
			setName(name);
		}
		setKey(key);
		setBlobUrl(url);
	}

	/**
	 * Method sets azure role's
	 * cache storage account key.
	 * @param key
	 */
	private void setKey(String key) {
		try {
			wARole.
			setCacheStorageAccountKey(key);
		} catch (WindowsAzureInvalidProjectOperationException e) {
			PluginUtil.displayErrorDialogAndLog(
					getShell(),
					Messages.cachErrTtl,
					Messages.setAccKyErrMsg, e);
		}
	}

	/**
	 * Method sets azure role's
	 * cache storage account name.
	 * @param name
	 */
	private void setName(String name) {
		try {
			wARole.
			setCacheStorageAccountName(name);
		} catch (WindowsAzureInvalidProjectOperationException e) {
			PluginUtil.displayErrorDialogAndLog(
					getShell(),
					Messages.cachErrTtl,
					Messages.setAccNmErrMsg, e);
		}
	}

	/**
	 * Method sets azure role's
	 * cache storage account blob endpoint url.
	 * @param url
	 */
	private void setBlobUrl(String url) {
		try {
			wARole.setCacheStorageAccountUrl(url);
		} catch (WindowsAzureInvalidProjectOperationException e) {
			PluginUtil.displayErrorDialogAndLog(
					getShell(),
					Messages.cachErrTtl,
					Messages.setAccUrlErrMsg, e);
		}
	}

	/**
	 * Method enables or disables
	 * UI components on caching page.
	 * @param status
	 */
	private void setEnableCaching(boolean status) {
		enblCacheNote.setEnabled(status);
		scaleLbl.setEnabled(status);
		cacheScale.setEnabled(status);
		txtCache.setEnabled(status);
		tblCache.setEnabled(status);
		btnAdd.setEnabled(status);
		explNtLbl.setEnabled(status);
		txtHostName.setEnabled(status);
		hostNameLbl.setEnabled(status);
		comboStrgAcc.setEnabled(status);
		crdntlLbl.setEnabled(status);
		if (status) {
			comboStrgAcc = JdkSrvConfig.
					populateStrgAccComboBox("",
							comboStrgAcc, null, true);
		} else {
			cacheScale.
			setSelection(cacheScale.getMinimum());
			txtCache.setText("");
			comboStrgAcc.removeAll();
			txtHostName.setText("");
			btnRemove.setEnabled(status);
			btnEdit.setEnabled(status);
		}
	}

	/**
	 * Create TableViewer for cache table.
	 */
	private void createTableViewer() {
		tableViewer = new TableViewer(tblCache);

		tableViewer.setUseHashlookup(true);
		tableViewer.setColumnProperties(new String[] {
				Messages.colChName,
				Messages.colBkps,
				Messages.colExp,
				Messages.colMinToLive,
				Messages.colPort });

		CellEditor[] editors = new CellEditor[5];

		editors[0] = new TextCellEditor(tblCache);
		editors[1] = new ComboBoxCellEditor(tblCache, arrBackOpt,
				SWT.READ_ONLY);
		editors[2] = new ComboBoxCellEditor(tblCache, arrExpPolType,
				SWT.READ_ONLY);
		editors[3] = new TextCellEditor(tblCache);
		editors[4] = new TextCellEditor(tblCache);

		tableViewer.setCellEditors(editors);
	}

	/**
	 * Content provider class for cache table,
	 * which determines the input for the table.
	 *
	 */
	private class EPTableContentProvider implements IStructuredContentProvider {

		@Override
		public void inputChanged(Viewer viewer,
				Object oldInput, Object newInput) {

		}

		@Override
		public void dispose() {

		}

		@Override
		public Object[] getElements(Object arg0) {
			if (mapCache == null) {
				mapCache = new HashMap<String,
						WindowsAzureNamedCache>();
			}
			try {
				mapCache = wARole.getNamedCaches();
			} catch (WindowsAzureInvalidProjectOperationException e) {
				PluginUtil.displayErrorDialogAndLog(
						getShell(),
						Messages.cachErrTtl,
						Messages.cachGetErMsg, e);
			}
			Map<String, WindowsAzureNamedCache> treeMap = new 
					TreeMap<String, WindowsAzureNamedCache>(mapCache);
			return treeMap.entrySet().toArray();
		}
	}

	/**
	 * Label provider class for cache table,
	 * to provide column names.
	 *
	 */
	private class EPTableLabelProvider implements ITableLabelProvider {

		@Override
		public void removeListener(ILabelProviderListener arg0) {

		}

		@Override
		public boolean isLabelProperty(Object arg0, String arg1) {
			return false;
		}

		@Override
		public void dispose() {

		}

		@Override
		public void addListener(ILabelProviderListener arg0) {

		}

		@Override
		public String getColumnText(Object element, int colIndex) {
			String result = "";
			try {
				@SuppressWarnings("unchecked")
				Entry<String, WindowsAzureNamedCache> cacheEntry =
				(Entry<String, WindowsAzureNamedCache>) element;
				WindowsAzureNamedCache cache = cacheEntry.getValue();
				switch (colIndex) {
				case 0:
					result = cache.getName();
					break;
				case 1:
					if (cache.getBackups()) {
						result = Messages.cachBckYes;
					} else {
						result = Messages.cachBckNo;
					}
					break;
				case 2:
					result = getExpPolStr(cache);
					break;
				case 3:
					/*
					 * If expiration policy is NEVER_EXPIRES
					 * then show N/A for minutes to Live column
					 */
					if (cache.getExpirationPolicy().
							equals(WindowsAzureCacheExpirationPolicy.
									NEVER_EXPIRES)) {
						result = Messages.dlgDbgNA;
					} else {
						result = Integer.toString(
								cache.getMinutesToLive());
					}
					break;
				case 4:
					result = cache.getEndpoint().
					getPrivatePort();
					break;
				default:
					break;
				}
			} catch (Exception e) {
				PluginUtil.displayErrorDialogAndLog(
						getShell(),
						Messages.cachErrTtl,
						Messages.cachGetErMsg, e);
			}
			return result;
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}

	/**
	 * Cell modifier class for cache table,
	 * which implements the in-place editing for cells.
	 */
	private class CellModifier implements ICellModifier {
		@Override
		public void modify(Object waCache, String columnName,
				Object modifiedVal) {
			TableItem tblItem = (TableItem) waCache;
			@SuppressWarnings("unchecked")
			Entry<String, WindowsAzureNamedCache> cacheEntry =
			(Entry<String, WindowsAzureNamedCache>) tblItem.getData();
			WindowsAzureNamedCache cache  = cacheEntry.getValue();
			try {
				if (columnName.equals(Messages.colChName)) {
					modifyCacheName(cache, modifiedVal);
				} else if (columnName.equals(Messages.colBkps)) {
					modifyBackup(cache, modifiedVal);
				} else if (columnName.equals(Messages.colExp)) {
					modifyExpirationPol(cache, modifiedVal);
				} else if (columnName.equals(Messages.colMinToLive)) {
					modifyMinToLive(cache, modifiedVal);
				} else if (columnName.equals(Messages.colPort)) {
					modifyPort(cache, modifiedVal);
				}
			} catch (Exception e) {
				PluginUtil.displayErrorDialogAndLog(
						getShell(),
						Messages.cachErrTtl,
						Messages.cachSetErrMsg, e);
			}
			tableViewer.refresh();
		}

		/**
		 * Handles the modification of named cache port.
		 * @param cache : the cache being modified
		 * @param modifiedVal : new value for port
		 * @throws WindowsAzureInvalidProjectOperationException
		 */
		private void modifyPort(WindowsAzureNamedCache cache,
				Object modifiedVal)
						throws WindowsAzureInvalidProjectOperationException {
			try {
				cache = WARCachingUtilMethods.modifyPort(cache, modifiedVal, wARole);
			} catch (AzureCommonsException e1) {
				PluginUtil.displayErrorDialogAndLog(
						getShell(),
						Messages.cachPortErrTtl,
						e1.getMessage(), e1);
			}
		}

		/**
		 * Handles the modification of named cache minutes to live.
		 * @param cache : the cache being modified
		 * @param modifiedVal : new value for minutes to live
		 * @throws WindowsAzureInvalidProjectOperationException
		 */
		private void modifyMinToLive(WindowsAzureNamedCache cache,
				Object modifiedVal)
						throws WindowsAzureInvalidProjectOperationException {
			String mtlTxt = modifiedVal.toString();
			Boolean isVallidMtl = WARCachingUtilMethods.validateMtl(mtlTxt);
			if (isVallidMtl) {
				cache.setMinutesToLive(Integer.parseInt(mtlTxt));
			} else {
				PluginUtil.displayErrorDialog(
						getShell(),
						Messages.cachMtlErrTtl,
						Messages.cachMtlErrMsg);
			}
		}

		/**
		 * Handles the modification of named cache expiration policy.
		 * @param cache : the cache being modified
		 * @param modifiedVal : new value for expiration policy
		 * @throws WindowsAzureInvalidProjectOperationException
		 */
		private void modifyExpirationPol(WindowsAzureNamedCache cache,
				Object modifiedVal)
						throws WindowsAzureInvalidProjectOperationException {
			cache = WARCachingUtilMethods.
					modifyExpirationPol(cache, modifiedVal.toString());
		}

		/**
		 * Handles the modification of named cache backup option.
		 * @param cache : the cache being modified
		 * @param modifiedVal : new value for backup option
		 * @throws WindowsAzureInvalidProjectOperationException
		 */
		private void modifyBackup(WindowsAzureNamedCache cache,
				Object modifiedVal)
						throws WindowsAzureInvalidProjectOperationException {
			if (modifiedVal.toString().equals("0")) {
				/*
				 * If user selects backup option
				 * then check virtual machine instances > 2
				 * otherwise give warning
				 */
				int vmCnt = 0;
				try {
					vmCnt = Integer.parseInt(
							wARole.getInstances());
				} catch (Exception e) {
					PluginUtil.displayErrorDialogAndLog(
							getShell(),
							Messages.genErrTitle,
							Messages.vmInstGetErMsg,
							e);
				}
				if (vmCnt < 2) {
					/*
					 * If virtual machine instances < 2
					 * then set back up to false.
					 */
					cache.setBackups(false);
					MessageDialog.openWarning(
							getShell(), Messages.backWarnTtl,
							Messages.backWarnMsg);
				} else {
					cache.setBackups(true);
				}
			} else if (modifiedVal.toString().equals("1")) {
				cache.setBackups(false);
			}
		}

		/**
		 * Handles the modification of named cache name.
		 * @param cache : the cache being modified
		 * @param modifiedVal : new value for cache name
		 * @throws WindowsAzureInvalidProjectOperationException
		 */
		private void modifyCacheName(WindowsAzureNamedCache cache,
				Object modifiedVal)
						throws WindowsAzureInvalidProjectOperationException {
			try {
				cache = WARCachingUtilMethods.modifyCacheName(cache, modifiedVal, mapCache);
			} catch (AzureCommonsException e) {
				PluginUtil.displayErrorDialogAndLog(getShell(),
						Messages.cachNameErrTtl,
						e.getMessage(), e);
			}
		}

		/**
		 * Determines whether a particular cell can be modified or not.
		 */
		@Override
		public boolean canModify(Object element, String property) {
			boolean retVal = true;
			@SuppressWarnings("unchecked")
			Entry<String, WindowsAzureNamedCache> entry =
			(Entry<String, WindowsAzureNamedCache>) element;
			/*
			 * If default cache is selected for in place editing
			 * of cache name then don't allow
			 * as renaming default cache is not allowed.
			 */
			if (property.equals(Messages.colChName)
					&& entry.getValue().getName().equalsIgnoreCase(
							Messages.dfltCachName)) {
				retVal = false;
			}
			/*
			 * If expiration policy is NEVER_EXPIRES
			 * then in place editing of column minutes to live
			 * is not allowed.
			 */
			else if (property.equals(Messages.colMinToLive)
					&& entry.getValue().getExpirationPolicy().
					equals(WindowsAzureCacheExpirationPolicy.
							NEVER_EXPIRES)) {
				retVal = false;
			}
			return retVal;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Object getValue(Object element, String property) {
			Object result = null;
			Entry<String, WindowsAzureNamedCache> cacheEntry =
					(Entry<String, WindowsAzureNamedCache>) element;
			WindowsAzureNamedCache cache = cacheEntry.getValue();
			if (property.equals(Messages.colChName)) {
				result = cache.getName();
			} else if (property.equals(Messages.colBkps)) {
				if (cache.getBackups()) {
					result = 0;
				} else {
					result = 1;
				}
			} else if (property.equals(Messages.colExp)) {
				WindowsAzureCacheExpirationPolicy 
				policy = cache.getExpirationPolicy();
				if (policy.equals(
						WindowsAzureCacheExpirationPolicy.
						NEVER_EXPIRES)) {
					result = 0;
				} else if (policy.equals(
						WindowsAzureCacheExpirationPolicy.
						ABSOLUTE)) {
					result = 1;
				} else if (policy.equals(
						WindowsAzureCacheExpirationPolicy.
						SLIDING_WINDOW)) {
					result = 2;
				}
			} else if (property.equals(Messages.colMinToLive)) {
				/*
				 * The return value of getValue() is an object,
				 * but if we are using a TextCellEditor,
				 * it has to return a String.
				 * Hence integer to String conversion is necessary.
				 */
				result = Integer.toString(cache.
						getMinutesToLive());
			} else if (property.equals(Messages.colPort)) {
				try {
					result = cache.getEndpoint().getPrivatePort();
				} catch (WindowsAzureInvalidProjectOperationException e) {
					PluginUtil.displayErrorDialogAndLog(
							getShell(),
							Messages.cachErrTtl,
							Messages.cachGetErMsg,
							e);
				}
			}
			return result;
		}
	}

	@Override
	public boolean okToLeave() {
		boolean okToProceed = false;
		// Check caching is enabled
		if (cacheCheck.getSelection()) {
			/* Check cache memory size 
			 * is set to valid value or not
			 */
			if (isCachPerValid) {
				okToProceed = true;
				setErrorMessage(null);
			} else {
				setErrorMessage(Messages.cachPerErrMsg);
			}
		} else {
			okToProceed = true;
		}
		boolean retVal = false;
		if (okToProceed) {
			retVal = super.okToLeave();
		}
		return retVal;
	}

	@Override
	public boolean performOk() {
		if (!isPageDisplayed) {
			return super.performOk();
		}

		boolean okToProceed = false;
		// Check caching is enabled
		if (cacheCheck.getSelection()) {
			/* Check cache memory size
			 * is set to valid value or not
			 */
			if (isCachPerValid) {
				okToProceed = true;
			} else {
				PluginUtil.displayErrorDialog(
						this.getShell(),
						Messages.cachPerErrTtl,
						Messages.cachPerErrMsg);
			}
		} else {
			okToProceed = true;
		}
		if (okToProceed) {
			try {
				if (!Activator.getDefault().isSaved()) {
					waProjManager.save();
					Activator.getDefault().setSaved(true);
				}
				WAEclipseHelper.refreshWorkspace(
						Messages.rolsRefTitle, Messages.rolsRefMsg);
				okToProceed = super.performOk();
			} catch (WindowsAzureInvalidProjectOperationException e) {
				okToProceed = false;
				PluginUtil.displayErrorDialogAndLog(
						this.getShell(),
						Messages.adRolErrTitle,
						Messages.adRolErrMsgBox1
						+ Messages.adRolErrMsgBox2, e);
			}
		}
		return okToProceed;
	}

	private void setCachPerMem(int cachVal) {
		try {
			wARole.setCacheMemoryPercent(cachVal);
			isCachPerValid = true;
			setErrorMessage(null);
		} catch (Exception e) {
			/*
			 * User has given input
			 * cachVal < 0 or cachVal > 100
			 */
			isCachPerValid = false;
		}
	}

	/**
	 * Function returns entry of cache element
	 * which is currently selected in table.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Entry<String, WindowsAzureNamedCache> getSelNamedCache() {
		int selIndex = tableViewer.getTable().getSelectionIndex();
		Entry<String, WindowsAzureNamedCache> cachEntry = null;
		if (selIndex > -1) {
			try {
				cachEntry = (Entry<String, WindowsAzureNamedCache>)
						tableViewer.getTable().
						getItem(selIndex).getData();
			} catch (Exception e) {
				PluginUtil.displayErrorDialogAndLog(
						getShell(),
						Messages.cachErrTtl,
						Messages.cachSetErrMsg,
						e);
			}
		}
		return cachEntry;
	}

	/**
	 * Mapping of expiration policies stored in project manager object
	 * to values shown on UI.
	 * @param cache
	 * @return
	 */
	private String getExpPolStr(WindowsAzureNamedCache cache) {
		String expPolStr;
		if (cache.getExpirationPolicy().
				equals(WindowsAzureCacheExpirationPolicy.
						NEVER_EXPIRES)) {
			expPolStr = Messages.expPolNvrExp;
		} else if (cache.getExpirationPolicy().
				equals(WindowsAzureCacheExpirationPolicy.
						ABSOLUTE)) {
			expPolStr = Messages.expPolAbs;
		} else {
			expPolStr = Messages.expPolSlWn;
		}
		return expPolStr;
	}
}