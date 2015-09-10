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
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

import com.interopbridges.tools.windowsazure.WindowsAzureCertificate;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.microsoftopentechnologies.azurecommons.util.WAEclipseHelperMethods;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
import com.persistent.util.WAEclipseHelper;

import waeclipseplugin.Activator;

public class WARCertificates extends PropertyPage{
	private WindowsAzureProjectManager waProjManager;
	private WindowsAzureRole windowsAzureRole;
	private Table tblCertificates;
	private TableViewer tblViewer;
	private Map<String, WindowsAzureCertificate> mapCert;
	private Button btnRemove;
	private boolean isPageDisplayed = false;
	public static String certSelected = "";

	@Override
	public String getTitle() {
		if (isPageDisplayed
				&& tblViewer != null) {
			tblViewer.refresh();
		}
		return super.getTitle();
	}

	@Override
	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				"com.persistent.winazure.eclipseplugin."
						+ "windows_azure_certificates_page");
		waProjManager = Activator.getDefault().getWaProjMgr();
		windowsAzureRole = Activator.getDefault().getWaRole();
		Activator.getDefault().setSaved(false);

		Composite container = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		container.setLayout(gridLayout);
		container.setLayoutData(gridData);

		tblCertificates = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
		tblCertificates.setHeaderVisible(true);
		tblCertificates.setLinesVisible(true);
		gridData = new GridData();
		gridData.heightHint = 380;
		gridData.horizontalIndent = 3;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		tblCertificates.setLayoutData(gridData);

		TableColumn colName = new TableColumn(tblCertificates, SWT.FILL);
		colName.setText(Messages.evColName);
		colName.setWidth(150);

		TableColumn colValue = new TableColumn(tblCertificates, SWT.FILL);
		colValue.setText(Messages.colThumb);
		colValue.setWidth(325);

		tblCertificates.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				btnRemove.setEnabled(true);
				decideCurSelectedCert();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {

			}
		});

		// container for add/remove/remove buttons
		Composite containerButtons = new Composite(container, SWT.NONE);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.END;
		gridData.verticalAlignment = GridData.BEGINNING;
		containerButtons.setLayout(gridLayout);
		containerButtons.setLayoutData(gridData);

		createAddButton(containerButtons);
		createRemoveButton(containerButtons);
		createTableViewer();
		isPageDisplayed = true;
		return container;
	}

	private void createAddButton(Composite containerButtons) {
		Button btnAdd = new Button(containerButtons, SWT.PUSH);
		btnAdd.setText(Messages.rolsAddBtn);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
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

	protected void addBtnListener(){
		CertificateDialog dialog =
				new CertificateDialog(this.getShell(), mapCert, windowsAzureRole);
		if (dialog.open() == 0) {
			tblViewer.refresh(true);
			String name = CertificateDialog.getNewlyAddedCert();
			TableItem[] items = tblCertificates.getItems();
			for (int i = 0; i < items.length; i++) {
				TableItem tableItem = items[i];
				if (tableItem.getText().equalsIgnoreCase(name)) {
					tblCertificates.select(i);
					break;
				}
			}
			decideCurSelectedCert();
		}
	}

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

	@SuppressWarnings("unchecked")
	protected void removeBtnListener() {
		int selIndex = tblViewer.getTable().getSelectionIndex();
		if (selIndex > -1) {
			try {
				Entry<String, WindowsAzureCertificate> certEntry =
						(Entry<String, WindowsAzureCertificate>)
						tblViewer.getTable().getItem(selIndex).getData();
				WindowsAzureCertificate delCert = certEntry.getValue();
				if (delCert.isRemoteAccess()
						&& delCert.isSSLCert()) {
					String temp = String.format("%s%s%s",
							Messages.sslTtl,
							" and ",
							com.persistent.util.Messages.cmhLblRmtAces);
					PluginUtil.displayErrorDialog(getShell(),
							Messages.certRmTtl,
							String.format(Messages.certComMsg,
									temp,
									temp));
				} else if (delCert.isRemoteAccess()) {
					PluginUtil.displayErrorDialog(getShell(),
							Messages.certRmTtl,
							String.format(Messages.certComMsg,
									com.persistent.util.Messages.cmhLblRmtAces,
									com.persistent.util.Messages.cmhLblRmtAces));
				} else if (delCert.isSSLCert()) {
					PluginUtil.displayErrorDialog(getShell(),
							Messages.certRmTtl,
							String.format(Messages.certComMsg,
									Messages.sslTtl,
									Messages.sslTtl));
				} else {
					boolean choice = MessageDialog.openConfirm(getShell(),
							Messages.certRmTtl,
							String.format(Messages.certRmMsg,
									delCert.getName()));
					if (choice) {
						delCert.delete();
						tblViewer.refresh();
						if (tblCertificates.getItemCount() == 0) {
							// table is empty i.e. number of rows = 0
							btnRemove.setEnabled(false);
						}
						certSelected = "";
					}
				}
			} catch (WindowsAzureInvalidProjectOperationException e) {
				PluginUtil.displayErrorDialogAndLog(getShell(),
						Messages.certErrTtl,
						Messages.certErrMsg, e);
			}
		}
	}

	private void createTableViewer() {
		tblViewer = new TableViewer(tblCertificates);

		tblViewer.setUseHashlookup(true);
		tblViewer.setColumnProperties(new String[] {
				Messages.evColName,
				Messages.colThumb});

		CellEditor[] editors = new CellEditor[2];
		editors[0] = new TextCellEditor(tblCertificates);
		editors[1] = new TextCellEditor(tblCertificates);

		tblViewer.setCellEditors(editors);
		tblViewer.setContentProvider(new CertContentProvider());
		tblViewer.setLabelProvider(new CertLabelProvider());
		tblViewer.setCellModifier(new CertCellModifier());

		try {
			mapCert = windowsAzureRole.getCertificates();
			tblViewer.setInput(mapCert.entrySet().toArray());
		} catch (Exception e) {
			PluginUtil.displayErrorDialogAndLog(getShell(),
					Messages.certErrTtl,
					Messages.certErrMsg, e);
		}
	}

	private class CertContentProvider
	implements IStructuredContentProvider {

		@Override
		public void inputChanged(Viewer viewer,
				Object oldInput, Object newInput) {
		}

		@Override
		public void dispose() {

		}

		@Override
		public Object[] getElements(Object arg0) {
			if (mapCert == null) {
				mapCert = new HashMap<String, WindowsAzureCertificate>();
			}
			Map<String, WindowsAzureCertificate> treeMap =
					new TreeMap<String,
					WindowsAzureCertificate>(mapCert);
			return treeMap.entrySet().toArray();
		}
	}

	private class CertLabelProvider implements ITableLabelProvider {

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

		@SuppressWarnings("unchecked")
		@Override
		public String getColumnText(Object element, int colIndex) {
			Entry<String, WindowsAzureCertificate> certEntry =
					(Entry<String, WindowsAzureCertificate>) element;
			WindowsAzureCertificate cert = certEntry.getValue();
			String result = "";
			switch (colIndex) {
			case 0:
				result = cert.getName();
				break;
			case 1:
				result = cert.getFingerPrint();
				break;
			default:
				break;
			}
			return result;
		}

		@Override
		public Image getColumnImage(Object arg0, int arg1) {
			return null;
		}
	}
	private class CertCellModifier implements ICellModifier {

		@Override
		public boolean canModify(Object element, String property) {
			boolean retVal = true;
			@SuppressWarnings("unchecked")
			Entry<String, WindowsAzureCertificate> entry =
			(Entry<String, WindowsAzureCertificate>) element;
			try {
				if (entry.getValue().isRemoteAccess()
						|| entry.getValue().isSSLCert()) {
					retVal = false;
				}
			} catch (WindowsAzureInvalidProjectOperationException e) {
				PluginUtil.displayErrorDialogAndLog(
						getShell(),
						Messages.certErrTtl,
						Messages.certErrMsg, e);
			}
			return retVal;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Object getValue(Object element, String property) {
			Object result = null;
			Entry<String, WindowsAzureCertificate> certEntry =
					(Entry<String, WindowsAzureCertificate>) element;
			WindowsAzureCertificate cert = certEntry.getValue();
			if (property.equals(Messages.evColName)) {
				result = cert.getName();
			} else if (property.equals(Messages.colThumb)) {
				result = cert.getFingerPrint();
			}
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void modify(Object waCert, String columnName,
				Object modifiedVal) {
			TableItem tblItem = (TableItem) waCert;
			Entry<String, WindowsAzureCertificate> certEntry =
					(Entry<String, WindowsAzureCertificate>) tblItem.getData();
			WindowsAzureCertificate cert = certEntry.getValue();
			try {
				if (columnName.equals(Messages.evColName)) {
					modifyName(cert, modifiedVal);
				} else if (columnName.equals(Messages.colThumb)) {
					modifyThumb(cert, modifiedVal);
				}
			} catch (Exception e) {
				PluginUtil.displayErrorDialogAndLog(
						getShell(),
						Messages.certErrTtl,
						Messages.certErrMsg, e);
			}
			tblViewer.refresh();
		}

		private void modifyName(WindowsAzureCertificate cert, Object modifiedVal)
				throws WindowsAzureInvalidProjectOperationException {
			String modifiedTxt = modifiedVal.toString().trim();
			if (modifiedTxt.isEmpty()) {
				PluginUtil.displayErrorDialog(
						getShell(),
						Messages.certErrTtl,
						Messages.certInvMsg);
			} else {
				if (WAEclipseHelperMethods.isAlphaNumericUnderscore(modifiedTxt)) {
					boolean isValidName = true;
					for (Iterator<String> iterator =
							mapCert.keySet().iterator();
							iterator.hasNext();) {
						String key = iterator.next();
						if (key.equalsIgnoreCase(modifiedTxt)) {
							isValidName = false;
							break;
						}
					}
					if (isValidName || modifiedTxt.equalsIgnoreCase(
							cert.getName())) {
						cert.setName(modifiedTxt);
					} else {
						PluginUtil.displayErrorDialog(
								getShell(),
								Messages.certErrTtl,
								Messages.certAddErrMsg);
					}
				} else {
					PluginUtil.displayErrorDialog(
							getShell(),
							Messages.certErrTtl,
							Messages.certRegMsg);
				}
			}
		}

		private void modifyThumb(WindowsAzureCertificate cert, Object modifiedVal)
				throws WindowsAzureInvalidProjectOperationException {
			String modifiedTxt = modifiedVal.toString().trim();
			if (modifiedTxt.isEmpty()) {
				PluginUtil.displayErrorDialog(
						getShell(),
						Messages.certErrTtl,
						Messages.certInvMsg);
			} else {
				boolean isValidName = true;
				for (Iterator<Entry<String, WindowsAzureCertificate>> iterator =
						mapCert.entrySet().iterator();
						iterator.hasNext();) {
					WindowsAzureCertificate certObj = iterator.next().getValue();
					if (certObj.getFingerPrint().equalsIgnoreCase(modifiedTxt)) {
						isValidName = false;
						break;
					}
				}
				if (isValidName || modifiedTxt.equalsIgnoreCase(
						cert.getFingerPrint())) {
					cert.setFingerPrint(modifiedTxt.toUpperCase());
				} else {
					PluginUtil.displayErrorDialog(
							getShell(),
							Messages.certErrTtl,
							Messages.certAddErrMsg);
				}
			}
		}
	}

	@Override
	public boolean performOk() {
		if (!isPageDisplayed) {
			return super.performOk();
		}
		boolean okToProceed = true;
		try {
			if (!Activator.getDefault().isSaved()) {
				waProjManager.save();
				Activator.getDefault().setSaved(true);
			}
			WAEclipseHelper.refreshWorkspace(
					Messages.rolsRefTitle, Messages.rolsRefMsg);
		} catch (WindowsAzureInvalidProjectOperationException e) {
			PluginUtil.displayErrorDialogAndLog(
					this.getShell(),
					Messages.adRolErrTitle,
					Messages.adRolErrMsgBox1
					+ Messages.adRolErrMsgBox2, e);
			okToProceed = false;
		}
		if (okToProceed) {
			okToProceed = super.performOk();
		}
		return okToProceed;
	}

	public static String getSelCertName() {
		return certSelected;
	}

	@Override
	public boolean performCancel() {
		/*
		 * Do not remember selection index if cancel
		 * is pressed.
		 */
		certSelected = "";
		return true;
	}

	private void decideCurSelectedCert() {
		int selIndex = tblViewer.getTable().getSelectionIndex();
		if (selIndex > -1) {
			@SuppressWarnings("unchecked")
			Entry<String, WindowsAzureCertificate> certEntry =
			(Entry<String, WindowsAzureCertificate>)
			tblViewer.getTable().getItem(selIndex).getData();
			certSelected = certEntry.getValue().getName();
		} else {
			certSelected = "";
		}
	}
}
