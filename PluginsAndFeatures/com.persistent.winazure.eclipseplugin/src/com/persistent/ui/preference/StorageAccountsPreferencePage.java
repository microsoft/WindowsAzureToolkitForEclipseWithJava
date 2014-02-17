/**
* Copyright 2014 Microsoft Open Technologies, Inc.
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
package com.persistent.ui.preference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.gigaspaces.azure.util.MethodUtils;
import com.gigaspaces.azure.util.UIUtils;
import com.microsoftopentechnologies.wacommon.commoncontrols.ImportSubscriptionDialog;
import com.microsoftopentechnologies.wacommon.storageregistry.PreferenceUtilStrg;
import com.microsoftopentechnologies.wacommon.storageregistry.StorageAccount;
import com.microsoftopentechnologies.wacommon.storageregistry.StorageAccountRegistry;
import com.microsoftopentechnologies.wacommon.storageregistry.StorageRegistryUtilMethods;

public class StorageAccountsPreferencePage
extends PreferencePage implements IWorkbenchPreferencePage {
	private Button btnImpFrmPubSetFile;
	private Table tblStorage;
	private TableViewer tableViewer;
	private Button btnAddStorage;
	private Button btnRemoveStorage;
	private Button btnEditStorage;
	public static int selIndex = -1;

	@Override
	public void init(IWorkbench arg0) {
	}

	@Override
	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		composite.setLayout(gridLayout);
		composite.setLayoutData(gridData);
		// create Import From Publish settings file button
		createImportBtnCmpnt(composite);
		createStorageTable(composite);
		return null;
	}

	/**
	 * Method creates Import From Publish Settings File
	 * button and add listener to it.
	 * @param parent
	 */
	private void createImportBtnCmpnt(Composite parent) {
		btnImpFrmPubSetFile = UIUtils.
				createImportFromPublishSettingsFileBtn(parent, 2);
		btnImpFrmPubSetFile.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				ImportSubscriptionDialog dlg = new
						ImportSubscriptionDialog(new Shell());
				dlg.open();
				String fileName = ImportSubscriptionDialog.
						getPubSetFilePath();
				MethodUtils.handleFile(fileName, tableViewer);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {

			}
		});
	}

	/**
	 * Method creates storage account table.
	 * @param composite
	 */
	private void createStorageTable(Composite composite) {
		tblStorage = new Table(composite, SWT.BORDER
				| SWT.FULL_SELECTION);
		tblStorage.setHeaderVisible(true);
		tblStorage.setLinesVisible(true);

		GridData gridData = new GridData();
		gridData.heightHint = 360;
		gridData.verticalIndent = 15;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = false;
		tblStorage.setLayoutData(gridData);

		TableColumn strNameCol = new TableColumn(tblStorage,
				SWT.FILL);
		strNameCol.setText(Messages.strColName);
		strNameCol.setWidth(150);

		TableColumn strUrlDisCol = new TableColumn(tblStorage, SWT.FILL);
		strUrlDisCol.setText(Messages.strColSrvEnpt);
		strUrlDisCol.setWidth(190);

		tableViewer = new TableViewer(tblStorage);
		tableViewer.setContentProvider(new IStructuredContentProvider() {

			@Override
			public void inputChanged(Viewer viewer,
					Object obj, Object obj1) {
			}

			@Override
			public void dispose() {
			}

			@Override
			public Object[] getElements(Object obj) {
				return getTableContent();
			}
		});

		tableViewer.setLabelProvider(new ITableLabelProvider() {

			@Override
			public void removeListener(
					ILabelProviderListener ilabelproviderlistener) {
			}

			@Override
			public boolean isLabelProperty(Object element, String s) {
				return false;
			}

			@Override
			public void dispose() {
			}

			@Override
			public void addListener(
					ILabelProviderListener ilabelproviderlistener) {
			}

			@Override
			public String getColumnText(Object element, int i) {
				StorageAccPrefPageTableElement rowElement =
						(StorageAccPrefPageTableElement) element;
				String result = "";
				switch (i) {
				case 0:
					result = rowElement.getStorageName();
					break;

				case 1:
					/*
					 * Show only endpoint service URL part.
					 */
					result = StorageRegistryUtilMethods.
					getServiceEndpoint(rowElement.getStorageUrl());
					break;

				default:
					break;
				}
				return result;
			}

			@Override
			public Image getColumnImage(Object element, int i) {
				return null;
			}
		});

		tableViewer.setInput(getTableContent());

		Composite containerButtons = new Composite(composite, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.END;
		gridData.verticalAlignment = GridData.BEGINNING;
		gridData.verticalIndent = 15;
		containerButtons.setLayout(gridLayout);
		containerButtons.setLayoutData(gridData);

		btnAddStorage = new Button(containerButtons, SWT.PUSH);
		btnAddStorage.setText(Messages.addBtnText);
		gridData = new GridData();
		gridData.widthHint = 70;
		btnAddStorage.setLayoutData(gridData);

		btnAddStorage.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				addButtonListener();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		btnEditStorage = new Button(containerButtons, SWT.PUSH);
		btnEditStorage.setEnabled(false);
		btnEditStorage.setText(Messages.editBtnText);
		gridData = new GridData();
		gridData.widthHint = 70;
		btnEditStorage.setLayoutData(gridData);

		tblStorage.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				selIndex = tableViewer.getTable().getSelectionIndex();
				btnEditStorage.setEnabled(true);
				btnRemoveStorage.setEnabled(true);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
			}
		});

		btnEditStorage.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				editButtonListener();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		btnRemoveStorage = new Button(containerButtons, SWT.PUSH);
		btnRemoveStorage.setEnabled(false);
		btnRemoveStorage.setText(Messages.removeBtnText);
		gridData = new GridData();
		gridData.widthHint = 70;
		btnRemoveStorage.setLayoutData(gridData);

		btnRemoveStorage.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				removeButtonListener();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		tableViewer
		.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(
					SelectionChangedEvent selectionchangedevent) {
				if (selectionchangedevent.getSelection().isEmpty()) {
					btnEditStorage.setEnabled(false);
					btnRemoveStorage.setEnabled(false);
				}
			}
		});
	}

	/**
	 * Method removes selected storage account from list.
	 */
	protected void removeButtonListener() {
		int curSelIndex = tableViewer.getTable().getSelectionIndex();
		if (curSelIndex > -1) {
			boolean choice = MessageDialog.openConfirm(new Shell(),
					Messages.accRmvTtl, Messages.accRmvMsg);
			if (choice) {
				StorageAccountRegistry.getStrgList().remove(curSelIndex);
				PreferenceUtilStrg.save();
				tableViewer.refresh();
				/*
				 * Do not remember selection index when element is removed.
				 * As after remove no row is selected in table
				 * and we don't want to set combo box to element
				 * which is next to removed element.
				 */
				selIndex = -1;
			}
		}
	}

	/**
	 * Method opens dialog to edit access key of storage account.
	 */
	protected void editButtonListener() {
		int index = tableViewer.getTable().getSelectionIndex();
		StorageAccount accToEdit =
				StorageAccountRegistry.getStrgList().get(index);
		StorageAccountDialog dlg =
				new StorageAccountDialog(getShell(), accToEdit);
		if (dlg.open() == 0) {
			PreferenceUtilStrg.save();
		}
	}

	/**
	 * Method opens dialog to add storage account in list.
	 */
	protected void addButtonListener() {
		StorageAccountDialog dlg = new StorageAccountDialog(getShell());
		if (dlg.open() == 0) {
			PreferenceUtilStrg.save();
			tableViewer.refresh();
		}
	}

	/**
	 * Method prepares storage account list to show in table.
	 * @return
	 */
	private Object[] getTableContent() {
		// loads data from preference file.
		PreferenceUtilStrg.load();
		List<StorageAccount> strgList =
				StorageAccountRegistry.getStrgList();
		List<StorageAccPrefPageTableElement> tableRowElements =
				new ArrayList<StorageAccPrefPageTableElement>();
		for (StorageAccount storageAcc : strgList) {
			if (storageAcc != null) {
				StorageAccPrefPageTableElement ele =
						new StorageAccPrefPageTableElement();
				ele.setStorageName(storageAcc.getStrgName());
				ele.setStorageUrl(storageAcc.getStrgUrl());
				tableRowElements.add(ele);
			}
		}
		StorageAccPrefPageTableElements elements =
				new StorageAccPrefPageTableElements();
		elements.setElements(tableRowElements);
		return elements.getElements().toArray();
	}

	@Override
	public void setVisible(boolean visible) {
		// reload information if its new eclipse session.
		MethodUtils.loadSubInfoFirstTime(tableViewer);
		selIndex = -1;
		super.setVisible(visible);
	}

	public static String getSelIndexValue() {
		return Arrays.asList(StorageRegistryUtilMethods.
				getStorageAccountNames(false)).get(selIndex + 1);
	}

	@Override
	public boolean performCancel() {
		/*
		 * Do not remember selection index if cancel
		 * is pressed.
		 */
		selIndex = -1;
		return true;
	}
}
