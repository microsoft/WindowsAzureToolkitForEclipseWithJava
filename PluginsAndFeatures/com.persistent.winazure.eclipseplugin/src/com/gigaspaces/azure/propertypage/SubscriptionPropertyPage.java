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
package com.gigaspaces.azure.propertypage;

import java.util.Collection;

import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.jface.window.Window;
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
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.dialogs.PropertyPage;

import com.gigaspaces.azure.util.MethodUtils;
import com.gigaspaces.azure.util.PreferenceUtil;
import com.gigaspaces.azure.util.UIUtils;
import com.gigaspaces.azure.wizards.WizardCacheManager;
import com.microsoftopentechnologies.azurecommons.deploy.propertypages.SubscriptionPropertyPageTableElement;
import com.microsoftopentechnologies.azurecommons.deploy.propertypages.SubscriptionPropertyPageUtilMethods;
import com.microsoftopentechnologies.azurecommons.deploy.util.PublishData;
import com.microsoftopentechnologies.wacommon.commoncontrols.ImportSubscriptionDialog;

public class SubscriptionPropertyPage extends PropertyPage {

	private Table tblSubscriptions;
	private TableViewer tableViewer;
	private Button btnAddSubscription;
	private Button btnRemoveSubscription;
	private Button btnImpFrmPubSetFile;

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

		createSubscriptionTable(composite);

		return composite;
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
						ImportSubscriptionDialog(getShell());
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

	private void createSubscriptionTable(Composite composite) {
		tblSubscriptions = new Table(composite, SWT.MULTI | SWT.BORDER
				| SWT.FULL_SELECTION);

		tblSubscriptions.setHeaderVisible(true);

		tblSubscriptions.setLinesVisible(true);

		GridData gridData = new GridData();
		gridData.heightHint = 75;
		gridData.horizontalIndent = 3;
		gridData.verticalIndent = 15;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = false;

		GridLayout gridLayoutTable = new GridLayout();
		gridLayoutTable.numColumns = 2;
		gridLayoutTable.marginRight = 0;
		tblSubscriptions.setLayout(gridLayoutTable);
		tblSubscriptions.setLayoutData(gridData);

		TableColumn subscriptionNameCol = new TableColumn(tblSubscriptions,
				SWT.FILL);

		subscriptionNameCol.setText(Messages.subscriptionColName);
		subscriptionNameCol.setWidth(160);

		TableColumn subscriptionIdCol = new TableColumn(tblSubscriptions,
				SWT.FILL);
		subscriptionIdCol.setText(Messages.subscriptionIdColName);
		subscriptionIdCol.setWidth(300);

		tableViewer = new TableViewer(tblSubscriptions);
		tableViewer.setContentProvider(new IStructuredContentProvider() {

			@Override
			public void inputChanged(Viewer viewer, Object obj, Object obj1) {
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

				SubscriptionPropertyPageTableElement rowElement = (SubscriptionPropertyPageTableElement) element;

				String result = ""; //$NON-NLS-1$

				switch (i) {
				case 0:
					result = rowElement.getSubscriptionName();
					break;

				case 1:
					result = rowElement.getSubscriptionId();
					break;
				case 2:
					result = rowElement.getPublishDataThumbprint();
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

		btnAddSubscription = new Button(containerButtons, SWT.PUSH);
		btnAddSubscription.setText(Messages.addBtnText);
		gridData = new GridData();
		gridData.widthHint = 70;
		btnAddSubscription.setLayoutData(gridData);

		btnAddSubscription.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				addButtonListener();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		tblSubscriptions.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				btnRemoveSubscription.setEnabled(true);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
			}
		});

		btnRemoveSubscription = new Button(containerButtons, SWT.PUSH);
		btnRemoveSubscription.setEnabled(false);
		btnRemoveSubscription.setText(Messages.emoveBtnText);
		gridData = new GridData();
		gridData.widthHint = 70;
		btnRemoveSubscription.setLayoutData(gridData);

		btnRemoveSubscription.addSelectionListener(new SelectionListener() {

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
							btnRemoveSubscription.setEnabled(false);
						}
					}
				});
	}

	protected void removeButtonListener() {

		int index = tableViewer.getTable().getSelectionIndex();
		String id = tableViewer.getTable().getItem(index).getText(1);

		WizardCacheManager.removeSubscription(id);
		/*
		 * Remove from map as well,
		 * Do not put this line inside removeSubscription method
		 * as method gets called at removeDuplicateSubscriptions
		 * also to update Publish data cache.
		 */
		WizardCacheManager.getPublishSettingsPerSubscription().remove(id);
		PreferenceUtil.save();
		tableViewer.refresh();
	}

	protected void addButtonListener() {
		if (openPropertyDialog(null) == Window.OK) {
			PreferenceUtil.save();
			tableViewer.refresh();
		}
	}

	protected int openPropertyDialog(PublishData pd) {
		int retVal = Window.CANCEL;
		try {
			// Create the nodes
			IPreferenceNode subscriptionGeneral = new PreferenceNode(
					Messages.credentialsPageId, Messages.credentialsNodeText,
					null, CredentialsPropertyPage.class.toString());

			CredentialsPropertyPage.setPublishData(null);

			if (pd != null) {
				CredentialsPropertyPage.setPublishData(pd);
			}

			SelectionProvider selProvider = new SelectionProvider();

			PropertyDialogAction action = new PropertyDialogAction(
					new IShellProvider() {

						@Override
						public Shell getShell() {
							return new Shell();
						}
					}, selProvider);

			StructuredSelection selection = new StructuredSelection(
					subscriptionGeneral);

			selProvider.setSelection(selection);

			PreferenceDialog dlg = action.createDialog();

			String dlgTitle = String.format(Messages.credentialsDlgTitle);

			dlg.getShell().setText(dlgTitle);

			retVal = dlg.open();

		} catch (Exception ex) {
		}
		return retVal;
	}

	private Object[] getTableContent() {
		Collection<PublishData> publishDatas = WizardCacheManager.getPublishDatas();
		return SubscriptionPropertyPageUtilMethods.getTableContent(publishDatas);
	}

	private static class SelectionProvider implements ISelectionProvider {
		ISelection sel;

		@Override
		public void addSelectionChangedListener(ISelectionChangedListener arg0) {

		}

		@Override
		public ISelection getSelection() {
			return sel;
		}

		@Override
		public void removeSelectionChangedListener(
				ISelectionChangedListener arg0) {

		}

		@Override
		public void setSelection(ISelection selection) {
			sel = selection;
		}

	}

	@Override
	public void setVisible(boolean visible) {
		// reload information if its new eclipse session.
		MethodUtils.loadSubInfoFirstTime(tableViewer);
		super.setVisible(visible);
	}
}
