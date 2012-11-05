/**
 * Copyright 2012 Persistent Systems Ltd.
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
package com.persistent.winazureroles;

import java.util.HashMap;
import java.util.Iterator;
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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

import waeclipseplugin.Activator;

import com.interopbridges.tools.windowsazure.WindowsAzureCacheExpirationPolicy;
import com.interopbridges.tools.windowsazure.WindowsAzureEndpointType;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureNamedCache;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
import com.persistent.util.WAEclipseHelper;

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
	private Label accKeyLbl;
	private Label accNameLbl;
	private Label crdntlLbl;
	private Text txtAccKey;
	private Text txtAccName;
	private TableViewer tableViewer;
	private Map<String, WindowsAzureNamedCache> mapCache;
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
	/**
	 * End point range's minimum value.
	 */
	private final static int RANGE_MIN = 1;
	/**
	 * End point range's maximum value.
	 */
	private final static int RANGE_MAX = 65535;
	private boolean isPageDisplayed = false;
	/**
	 * Boolean field to track whether
	 * "Storage Account Information Missing" warning is displayed
	 * and traversed to another property page
	 * by clicking on OK button.
	 */
	private boolean warnDisplayed = false;
	/**
	 * Default time to live value.
	 */
	private final static int DEFAULT_TTL = 10;

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
			enableCachingPage();
			cacheCheck.setSelection(true);
			txtCache.setText(String.format("%s%s",
					cachePercent, "%"));
			cacheScale.setSelection(cachePercent);
			txtHostName.setText(String.format("%s%s%s",
					Messages.dlgDbgLclHost, "_",
					wARole.getName().toLowerCase()));
			try {
				String accName = wARole.
						getCacheStorageAccountName();
				String accKey = wARole.
						getCacheStorageAccountKey();
				if (accName != null) {
					txtAccName.setText(accName);
				}
				if (accKey != null) {
					txtAccKey.setText(accKey);
				}
			} catch (WindowsAzureInvalidProjectOperationException e) {
				PluginUtil.displayErrorDialogAndLog(
						getShell(),
						Messages.cachErrTtl,
						Messages.getStrAccErrMsg, e);
			}

		} else {
			cacheCheck.setSelection(false);
			disableCachingPage();
		}
		if (tableViewer != null) {
			tableViewer.refresh();
		}
		/*
		 * As soon as we come to Caching page
		 * set value to false, as user may traverse to other page
		 * and come back to caching page again
		 * and then click on OK 
		 */
		warnDisplayed = false;
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
					
					enableCachingPage();
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
				} else {
					disableCachingPage();
					/* Set cache memory percent to 0
					 *  to disable cache.
					 *  Also set storage account name
					 *  and key to empty.
					 */
					setCachPerMem(0);
					try {
						wARole.setCacheStorageAccountName("");
						wARole.setCacheStorageAccountKey("");
					} catch (WindowsAzureInvalidProjectOperationException e) {
						PluginUtil.displayErrorDialogAndLog(
								getShell(),
								Messages.cachErrTtl,
								Messages.setAccErrMsg, e);
					}
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
		cacheScale.setMinimum(15);
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
							&& cacheVal >= 15
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
				Entry<String, WindowsAzureNamedCache> cachEntry =
						getSelNamedCache();
				if (cachEntry.getKey().
						equalsIgnoreCase(Messages.dfltCachName)) {
					btnRemove.setEnabled(false);
				} else {
					btnRemove.setEnabled(true);
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
			boolean choice = MessageDialog.openQuestion(new Shell(),
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

		accNameLbl = new Label(strGrp, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		accNameLbl.setText(Messages.nameLbl);
		accNameLbl.setLayoutData(gridData);

		txtAccName = new Text(strGrp, SWT.LEFT | SWT.BORDER);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.widthHint = 200;
		gridData.horizontalIndent = 30;
		txtAccName.setLayoutData(gridData);
		txtAccName.addFocusListener(new FocusListener() {
			private String oldTxt = "";
			@Override
			public void focusLost(FocusEvent arg0) {
				if (!txtAccName.getText().equals(oldTxt)) {
					try {
						wARole.
						setCacheStorageAccountName(
								txtAccName.getText().trim());
					} catch (WindowsAzureInvalidProjectOperationException e) {
						PluginUtil.displayErrorDialogAndLog(
								getShell(),
								Messages.cachErrTtl,
								Messages.setAccNmErrMsg, e);
					}
				}
			}

			@Override
			public void focusGained(FocusEvent arg0) {
				oldTxt = txtAccName.getText();
			}
		});

		accKeyLbl = new Label(strGrp, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalIndent = 3;
		accKeyLbl.setText(Messages.keyLbl);
		accKeyLbl.setLayoutData(gridData);

		txtAccKey = new Text(strGrp, SWT.LEFT | SWT.BORDER);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.widthHint = 200;
		gridData.verticalIndent = 3;
		gridData.horizontalIndent = 30;
		txtAccKey.setLayoutData(gridData);
		txtAccKey.addFocusListener(new FocusListener() {
			private String oldTxt = "";
			@Override
			public void focusLost(FocusEvent arg0) {
				if (!txtAccKey.getText().
						equals(oldTxt)) {
					try {
						wARole.
						setCacheStorageAccountKey(
								txtAccKey.getText().trim());
					} catch (WindowsAzureInvalidProjectOperationException e) {
						PluginUtil.displayErrorDialogAndLog(
								getShell(),
								Messages.cachErrTtl,
								Messages.setAccKyErrMsg, e);
					}
				}
			}

			@Override
			public void focusGained(FocusEvent arg0) {
				oldTxt = txtAccKey.getText();
			}
		});
	}

	/**
	 * Disable components on caching page.
	 */
	private void disableCachingPage() {
		enblCacheNote.setEnabled(false);
		scaleLbl.setEnabled(false);
		cacheScale.setEnabled(false);
		cacheScale.setSelection(cacheScale.getMinimum());
		txtCache.setText("");
		txtCache.setEnabled(false);
		txtHostName.setText("");
		txtHostName.setEnabled(false);
		tblCache.setEnabled(false);
		btnRemove.setEnabled(false);
		btnAdd.setEnabled(false);
		btnEdit.setEnabled(false);
		explNtLbl.setEnabled(false);
		hostNameLbl.setEnabled(false);
		strGrp.setEnabled(false);
		accKeyLbl.setEnabled(false);
		accNameLbl.setEnabled(false);
		crdntlLbl.setEnabled(false);
		txtAccKey.setText("");
		txtAccKey.setEnabled(false);
		txtAccName.setText("");
		txtAccName.setEnabled(false);
	}

	/**
	 * Enable components on caching page.
	 */
	private void enableCachingPage() {
		enblCacheNote.setEnabled(true);
		scaleLbl.setEnabled(true);
		cacheScale.setEnabled(true);
		txtCache.setEnabled(true);
		tblCache.setEnabled(true);
		btnAdd.setEnabled(true);
		explNtLbl.setEnabled(true);
		txtHostName.setEnabled(true);
		hostNameLbl.setEnabled(true);
		strGrp.setEnabled(true);
		accKeyLbl.setEnabled(true);
		accNameLbl.setEnabled(true);
		crdntlLbl.setEnabled(true);
		txtAccKey.setEnabled(true);
		txtAccName.setEnabled(true);
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
			Boolean isValidPort = false;
			String portTxt = modifiedVal.toString();
			if (portTxt.isEmpty()) {
				isValidPort = false;
			} else {
				try {
					int portNum = Integer.parseInt(portTxt);
					if (RANGE_MIN <= portNum
							&& portNum <= RANGE_MAX) {
						/*
						 * Check whether end point of same name or port
						 * exist already.
						 */
						Boolean isValidEp = wARole.isValidEndpoint(
								String.format("%s%s", Messages.cachEndPtName,
										cache.getName()),
										WindowsAzureEndpointType.Internal,
										portTxt, "");
						if (isValidEp) {
							isValidPort = true;
						} else {
							PluginUtil.displayErrorDialog(
									getShell(),
									Messages.cachPortErrTtl,
									Messages.dlgPortInUse);
							return;
						}
					} else {
						isValidPort = false;
					}
				} catch (NumberFormatException e) {
					isValidPort = false;
				}
			}
			if (isValidPort) {
				cache.getEndpoint().setPrivatePort(portTxt);
			} else {
				PluginUtil.displayErrorDialog(
						getShell(),
						Messages.cachPortErrTtl,
						Messages.rngErrMsg);
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
			Boolean isVallidMtl = false;
			int mtl = 0;
			String mtlTxt = modifiedVal.toString();
			if (mtlTxt.isEmpty()) {
				isVallidMtl = false;
			} else {
				try {
					mtl = Integer.parseInt(mtlTxt);
					if (mtl > 0) {
						isVallidMtl = true;
					} else {
						isVallidMtl = false;
					}
				} catch (NumberFormatException e) {
					isVallidMtl = false;
				}
			}
			if (isVallidMtl) {
				cache.setMinutesToLive(mtl);
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
			// NEVER_EXPIRES
			if (modifiedVal.toString().equals("0")) {
				cache.setExpirationPolicy(
						WindowsAzureCacheExpirationPolicy.
						NEVER_EXPIRES);
				/*
				 * If expiration policy is set to
				 * NEVER_EXPIRES then set MTL to zero
				 */
				cache.setMinutesToLive(0);
			}
			// ABSOLUTE
			else if (modifiedVal.toString().equals("1")) {
				cache.setExpirationPolicy(
						WindowsAzureCacheExpirationPolicy.
						ABSOLUTE);
				if (cache.getMinutesToLive() == 0) {
					cache.setMinutesToLive(DEFAULT_TTL);
				}
			}
			// SLIDING_WINDOW
			else if (modifiedVal.toString().equals("2")) {
				cache.setExpirationPolicy(
						WindowsAzureCacheExpirationPolicy.
						SLIDING_WINDOW);
				if (cache.getMinutesToLive() == 0) {
					cache.setMinutesToLive(DEFAULT_TTL);
				}
			}
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
			if (modifiedVal.toString().isEmpty()) {
				PluginUtil.displayErrorDialog(getShell(),
						Messages.cachNameErrTtl,
						Messages.cachNameEmpMsg);
			} else {
				StringBuffer strBfr =
						new StringBuffer(modifiedVal.toString());
				boolean isValidName = true;
				/*
				 * Check cache name contains alphanumeric characters,
				 * underscore and starts with alphabet only.
				 */
				if (WAEclipseHelper.
						isAlphaNumericUnderscore(
								modifiedVal.toString())) {
					for (Iterator<String> iterator =
							mapCache.keySet().iterator();
							iterator.hasNext();) {
						String key = iterator.next();
						if (key.equalsIgnoreCase(strBfr.toString())) {
							isValidName = false;
							break;
						}
					}
					if (isValidName
							|| modifiedVal.toString().equalsIgnoreCase(
									cache.getName())) {
						cache.setName(modifiedVal.toString());
					} else {
						PluginUtil.displayErrorDialog(getShell(),
								Messages.cachNameErrTtl,
								Messages.cachNameErrMsg);
					}
				} else {
					PluginUtil.displayErrorDialog(getShell(),
							Messages.cachNameErrTtl,
							Messages.chNameAlphNuMsg);
				}
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
		boolean cachPerValid = true;
		boolean accValid = true;
		// Check caching is enabled
		if (cacheCheck.getSelection()) {
			/* Check cache memory size 
			 * is set to valid value or not
			 */
			if (isCachPerValid) {
				cachPerValid = true;
				setErrorMessage(null);
				/* Check account name or key is empty,
				 * if then display confirmation dialog.
				 */
				if (txtAccName.getText().isEmpty()
						|| txtAccKey.getText().isEmpty()) {
					boolean choice = MessageDialog.openConfirm(
							new Shell(), Messages.cachWarnTtl,
							Messages.cachWarnMsg);
					if (choice) {
						accValid = true;
						// warning displayed & traversing to other page
						warnDisplayed = true;
					} else {
						accValid = false;
						// warning displayed & not traversing to other page
						warnDisplayed = false;
					}
				}
			} else {
				cachPerValid = false;
				setErrorMessage(Messages.cachPerErrMsg);
			}
		}
		if (cachPerValid
				&& accValid) {
			setErrorMessage(null);
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
		boolean cachPerValid = true;
		boolean accValid = true;
		// Check caching is enabled
		if (cacheCheck.getSelection()) {
			/* Check cache memory size
			 * is set to valid value or not
			 */
			if (isCachPerValid) {
				cachPerValid = true;
				/* Check we are on Caching property page or not
				 * if yes then check account name or key is empty,
				 * if yes then display confirmation dialog.
				 * Logic to avoid displaying warning in case
				 * of super.performOk() call
				 * i.e when current selected
				 * property page is not Caching
				 */
				if (!warnDisplayed && (txtAccName.getText().isEmpty()
						|| txtAccKey.getText().isEmpty())) {
					boolean choice = MessageDialog.openConfirm(
							new Shell(), Messages.cachWarnTtl,
							Messages.cachWarnMsg);
					if (choice) {
						accValid = true;
					} else {
						accValid = false;
					}
				}
			} else {
				cachPerValid = false;
				PluginUtil.displayErrorDialog(
						this.getShell(),
						Messages.cachPerErrTtl,
						Messages.cachPerErrMsg);
			}
		}
		if (cachPerValid
				&& accValid) {
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