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
package com.microsoft.azureexplorer.forms;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.microsoftopentechnologies.tooling.msservices.model.storage.ClientStorageAccount;
import com.microsoftopentechnologies.tooling.msservices.model.storage.TableEntity;

public class TableEntityForm extends Dialog {
	private Button addPropertyButton;

	private TableEntity tableEntity;
	private ClientStorageAccount storageAccount;
	private Runnable onFinish;
	private String tableName;
	private List<TableEntity> tableEntityList;

	public TableEntityForm(Shell parentShell, String name) {
		super(parentShell);
		parentShell.setText(name);
	}

	@Override
	protected Control createContents(Composite parent) {
		addPropertyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		return super.createContents(parent);
	}

	public void setTableEntity(TableEntity tableEntity) {
		this.tableEntity = tableEntity;


	}

	@Override
	protected void okPressed() {
		super.okPressed();
	}

	public void setStorageAccount(ClientStorageAccount storageAccount) {
		this.storageAccount = storageAccount;
	}

	public void setOnFinish(Runnable onFinish) {
		this.onFinish = onFinish;
	}

	public TableEntity getTableEntity() {
		return tableEntity;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void setTableEntityList(List<TableEntity> tableEntityList) {
		this.tableEntityList = tableEntityList;
	}
}
