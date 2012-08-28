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

package com.gigaspaces.azure.views;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import waeclipseplugin.Activator;
import com.gigaspaces.azure.deploy.DeploymentEventArgs;
import com.gigaspaces.azure.deploy.DeploymentEventListener;

public class WindowsAzureActivityLogView extends ViewPart {

	private TableViewer viewer;
	private Table table;
	private SimpleDateFormat dateFormat = new SimpleDateFormat(
			"MM/dd/yyyy hh:mm:ss", Locale.getDefault()); 

	private HashMap<String, TableRowDescriptor> rows = new HashMap<String, TableRowDescriptor>();

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		viewer = new TableViewer(createTable(parent));

		// Create the help context id for the viewer's control
		PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(viewer.getControl(),
						"com.gigaspaces.azrue.views.waactivitylogview"); 

		registerDeploymentListener();
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	private Table createTable(Composite parent) {
		table = new Table(parent, SWT.BORDER);

		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		for (int i = 0; i < 3; i++) {
			new TableColumn(table, SWT.NONE);
		}

		TableColumnLayout layout = new TableColumnLayout();
		parent.setLayout(layout);

		table.getColumn(0).setText(Messages.desc);
		table.getColumn(1).setText(Messages.status);
		table.getColumn(2).setText(Messages.startTime);

		layout.setColumnData(table.getColumn(0), new ColumnWeightData(65));
		layout.setColumnData(table.getColumn(1), new ColumnWeightData(20));
		layout.setColumnData(table.getColumn(2), new ColumnWeightData(15));

		return table;
	}

	public void addDeployment(String key, String description, Date startDate) {

		if (rows.containsKey(key)) {
			rows.remove(key);
		}

		TableItem item = new TableItem(table, SWT.NONE);
		item.setText(new String[] { description, null,
				dateFormat.format(startDate) });

		ProgressBar bar = new ProgressBar(table, SWT.NONE);

		bar.setSelection(0);
		TableEditor editor = new TableEditor(table);
		editor.grabHorizontal = editor.grabVertical = true;
		editor.setEditor(bar, item, 1);

		rows.put(key, new TableRowDescriptor(item, bar));
	}

	public void registerDeploymentListener() {
		Activator.getDefault().addDeploymentEventListener(
				new DeploymentEventListener() {

					@Override
					public void onDeploymentStep(final DeploymentEventArgs args) {						
						if (rows.containsKey(args.getId())) {

							Display.getDefault().asyncExec(new Runnable() {

								@Override
								public void run() {

									TableRowDescriptor row = rows.get(args.getId());
									
									if (!row.getProgressBar().isDisposed()) {
										row.getProgressBar().setSelection(row.getProgressBar().getSelection() + args.getDeployCompleteness());
										if (row.getProgressBar().getMaximum() <= (row.getProgressBar().getSelection())) {
											row.getItem().setText(1,args.getDeployMessage());
											row.getProgressBar().setVisible(false);
										}
									}
								}
							});
						}
					}
				});
	}

}
