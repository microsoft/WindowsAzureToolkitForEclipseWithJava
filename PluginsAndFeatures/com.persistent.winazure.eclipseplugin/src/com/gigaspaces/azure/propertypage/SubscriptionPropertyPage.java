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

package com.gigaspaces.azure.propertypage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.dialogs.PropertyPage;

import com.gigaspaces.azure.model.Subscription;
import com.gigaspaces.azure.runnable.LoadAccountWithProgressWindow;
import com.gigaspaces.azure.util.PreferenceUtil;
import com.gigaspaces.azure.util.PublishData;
import com.gigaspaces.azure.wizards.WizardCacheManager;

public class SubscriptionPropertyPage extends PropertyPage {

	private Table tblSubscriptions;
	private TableViewer tableViewer;
	private Button btnAddSubscription;
	private Button btnRemoveSubscription;
	private Button btnEditSubscription;

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

		createSubscriptionTable(composite);

		return composite;
	}

	private void createSubscriptionTable(Composite composite) {
		tblSubscriptions = new Table(composite, SWT.MULTI | SWT.BORDER
				| SWT.FULL_SELECTION);

		tblSubscriptions.setHeaderVisible(true);

		tblSubscriptions.setLinesVisible(true);

		GridData gridData = new GridData();
		gridData.heightHint = 75;
		gridData.horizontalIndent = 3;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = false;

		GridLayout gridLayoutTable = new GridLayout();
		gridLayoutTable.numColumns = 3;
		gridLayoutTable.marginRight = 0;
		tblSubscriptions.setLayout(gridLayoutTable);
		tblSubscriptions.setLayoutData(gridData);

		TableColumn subscriptionNameCol = new TableColumn(tblSubscriptions,
				SWT.FILL);

		subscriptionNameCol.setText(Messages.subscriptionColName);
		subscriptionNameCol.setWidth(120);

		TableColumn subscriptionIdCol = new TableColumn(tblSubscriptions,
				SWT.FILL);
		subscriptionIdCol.setText(Messages.subscriptionIdColName);
		subscriptionIdCol.setWidth(180);

		TableColumn thumbprintCol = new TableColumn(tblSubscriptions, SWT.FILL);
		thumbprintCol.setText(Messages.thumbprintColName);
		thumbprintCol.setWidth(180);

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
		containerButtons.setLayout(gridLayout);
		containerButtons.setLayoutData(gridData);

		btnAddSubscription = new Button(containerButtons, SWT.PUSH);
		btnAddSubscription.setText(Messages.addBtnText);
		gridData = new GridData();
		gridData.widthHint = 80;
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

		btnEditSubscription = new Button(containerButtons, SWT.PUSH);
		btnEditSubscription.setEnabled(false);
		btnEditSubscription.setText(Messages.editBtnText);
		gridData = new GridData();
		gridData.widthHint = 80;
		btnEditSubscription.setLayoutData(gridData);

		tblSubscriptions.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				btnEditSubscription.setEnabled(true);
				btnRemoveSubscription.setEnabled(true);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
			}
		});

		btnEditSubscription.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				editButtonListener();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		btnRemoveSubscription = new Button(containerButtons, SWT.PUSH);
		btnRemoveSubscription.setEnabled(false);
		btnRemoveSubscription.setText(Messages.emoveBtnText);
		gridData = new GridData();
		gridData.widthHint = 80;
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
							btnEditSubscription.setEnabled(false);
							btnRemoveSubscription.setEnabled(false);
						}
					}
				});
	}

	protected void removeButtonListener() {

		int index = tableViewer.getTable().getSelectionIndex();
		String id = tableViewer.getTable().getItem(index).getText(1);
		String thumbprint = tableViewer.getTable().getItem(index).getText(2);

		WizardCacheManager.removeSubscription(id, thumbprint);
		PreferenceUtil.save();
		tableViewer.refresh();
	}

	protected void editButtonListener() {

		int index = tableViewer.getTable().getSelectionIndex();

		String thumbprint = tableViewer.getTable().getItem(index).getText(2);
		String id = tableViewer.getTable().getItem(index).getText(1);

		PublishData pd = WizardCacheManager.findPublishDataByThumbprint(thumbprint);

		WizardCacheManager.changeCurrentSubscription(pd, id);

		CredentialsPropertyPage.setAdd(false);
		CredentialsPropertyPage.setEdit(true);
		
		if (openPropertyDialog(pd) == Window.OK) {
			PreferenceUtil.save();
			tableViewer.refresh();
		}
	}

	protected void addButtonListener() {
		
		CredentialsPropertyPage.setAdd(true);
		CredentialsPropertyPage.setEdit(false);
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
		List<SubscriptionPropertyPageTableElement> tableRowElements = new ArrayList<SubscriptionPropertyPageTableElement>();
		for (PublishData pd : publishDatas) {
			for (Subscription sub : pd.getPublishProfile().getSubscriptions()) {
				SubscriptionPropertyPageTableElement el = new SubscriptionPropertyPageTableElement();
				el.setPublishDataThumbprint(pd.getThumbprint());
				el.setSubscriptionId(sub.getId());
				el.setSubscriptionName(sub.getName());
				if (!tableRowElements.contains(el)) {
					tableRowElements.add(el);
				}
			}
		}
		SubscriptionPropertyPageTableElements elements = new SubscriptionPropertyPageTableElements();
		elements.setElements(tableRowElements);

		return elements.getElements().toArray();
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
		if (PreferenceUtil.isLoaded()  == false) {
			Display.getDefault().syncExec(new LoadAccountWithProgressWindow(null, getShell()));			
		}
		PreferenceUtil.setLoaded(true);
		tableViewer.refresh();
		super.setVisible(visible);
	}		
}
