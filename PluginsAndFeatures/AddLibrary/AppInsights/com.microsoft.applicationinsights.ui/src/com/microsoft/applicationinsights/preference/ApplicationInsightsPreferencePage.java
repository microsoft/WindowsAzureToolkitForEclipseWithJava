/**
 * Copyright Microsoft Corp.
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
package com.microsoft.applicationinsights.preference;

import java.util.ArrayList;
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

import com.microsoft.applicationinsights.ui.config.AIResourceChangeListener;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;

/**
 * Class for Application Insights preference page.
 * Creates UI components and their listeners.
 */
public class ApplicationInsightsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private Table table;
	private TableViewer tableViewer;
	private Button btnAdd;
	private Button btnDetails;
	private Button btnRemove;
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
		createApplicationInsightsResourceTable(composite);
		return null;
	}

	public void createApplicationInsightsResourceTable(Composite parent) {
		table = new Table(parent, SWT.BORDER
				| SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		GridData gridData = new GridData();
		gridData.heightHint = 360;
		gridData.verticalIndent = 15;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = false;
		table.setLayoutData(gridData);

		TableColumn strNameCol = new TableColumn(table, SWT.FILL);
		strNameCol.setText(Messages.resrcName);
		strNameCol.setWidth(150);

		TableColumn strUrlDisCol = new TableColumn(table, SWT.FILL);
		strUrlDisCol.setText(Messages.instrKey);
		strUrlDisCol.setWidth(190);

		tableViewer = new TableViewer(table);
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
				ApplicationInsightsPageTableElement rowElement =
						(ApplicationInsightsPageTableElement) element;
				String result = "";
				switch (i) {
				case 0:
					result = rowElement.getResourceName();
					break;

				case 1:
					result = rowElement.getInstrumentationKey();
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

		Composite containerButtons = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.END;
		gridData.verticalAlignment = GridData.BEGINNING;
		gridData.verticalIndent = 15;
		containerButtons.setLayout(gridLayout);
		containerButtons.setLayoutData(gridData);

		btnAdd = new Button(containerButtons, SWT.PUSH);
		btnAdd.setText(Messages.btnAddLbl);
		gridData = new GridData();
		gridData.widthHint = 70;
		btnAdd.setLayoutData(gridData);
		btnAdd.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				addButtonListener();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		btnDetails = new Button(containerButtons, SWT.PUSH);
		btnDetails.setEnabled(false);
		btnDetails.setText(Messages.btnDtlsLbl);
		gridData = new GridData();
		gridData.widthHint = 70;
		btnDetails.setLayoutData(gridData);
		btnDetails.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				detailsButtonListener();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		btnRemove = new Button(containerButtons, SWT.PUSH);
		btnRemove.setEnabled(false);
		btnRemove.setText(Messages.btnRmvLbl);
		gridData = new GridData();
		gridData.widthHint = 70;
		btnRemove.setLayoutData(gridData);
		btnRemove.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				removeButtonListener();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		table.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				selIndex = tableViewer.getTable().getSelectionIndex();
				btnDetails.setEnabled(true);
				btnRemove.setEnabled(true);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
			}
		});

		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(
					SelectionChangedEvent selectionchangedevent) {
				if (selectionchangedevent.getSelection().isEmpty()) {
					btnDetails.setEnabled(false);
					btnRemove.setEnabled(false);
				}
			}
		});
	}

	/**
	 * Method opens dialog to add existing application insights resource in list.
	 */
	protected void addButtonListener() {
		ApplicationInsightsAddDialog dialog = new ApplicationInsightsAddDialog(getShell());
		dialog.open();
		tableViewer.refresh();
	}

	/**
	 * Method opens dialog to show details of application insights resource.
	 */
	protected void detailsButtonListener() {
		int index = tableViewer.getTable().getSelectionIndex();
		ApplicationInsightsResource resource =
				ApplicationInsightsResourceRegistry.getAppInsightsResrcList().get(index);
		ApplicationInsightsDetailsDialog dialog =
				new ApplicationInsightsDetailsDialog(getShell(), resource);
		dialog.open();
	}

	/**
	 * Method removes application insight resource from local cache.
	 */
	protected void removeButtonListener() {
		int curSelIndex = tableViewer.getTable().getSelectionIndex();
		if (curSelIndex > -1) {
			String keyToRemove = ApplicationInsightsResourceRegistry.getKeyAsPerIndex(curSelIndex);
			String projName = AIResourceChangeListener.getProjectNameAsPerKey(keyToRemove);
			if (projName != null && !projName.isEmpty()) {
				PluginUtil.displayErrorDialog(new Shell(), Messages.appTtl,
						String.format(Messages.rsrcUseMsg, projName));
			} else {
				boolean choice = MessageDialog.openConfirm(new Shell(),
						Messages.appTtl, Messages.rsrcRmvMsg);
				if (choice) {
					ApplicationInsightsResourceRegistry.getAppInsightsResrcList().remove(curSelIndex);
					ApplicationInsightsPreferences.save();
					tableViewer.refresh();
					selIndex = -1;
				}
			}
		}
	}

	private Object[] getTableContent() {
		ApplicationInsightsPreferences.load();
		ApplicationInsightsPageTableElements elements = getPrefPageTableElements();
		return elements.getElements().toArray();
	}

	public static ApplicationInsightsPageTableElements getPrefPageTableElements() {
		List<ApplicationInsightsResource> resourceList =
				ApplicationInsightsResourceRegistry.getAppInsightsResrcList();
		List<ApplicationInsightsPageTableElement> tableRowElements =
				new ArrayList<ApplicationInsightsPageTableElement>();
		for (ApplicationInsightsResource resource : resourceList) {
			if (resource != null) {
				ApplicationInsightsPageTableElement ele = new ApplicationInsightsPageTableElement();
				ele.setResourceName(resource.getResourceName());
				ele.setInstrumentationKey(resource.getInstrumentationKey());
				tableRowElements.add(ele);
			}
		}
		ApplicationInsightsPageTableElements elements =
				new ApplicationInsightsPageTableElements();
		elements.setElements(tableRowElements);
		return elements;
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

	public static int getSelIndex() {
		return selIndex;
	}
}
