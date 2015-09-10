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
package com.gigaspaces.azure.views;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.microsoftopentechnologies.azurecommons.deploy.DeploymentEventArgs;
import com.microsoftopentechnologies.azurecommons.deploy.DeploymentEventListener;

import waeclipseplugin.Activator;

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

		Link link = new Link(table, SWT.LEFT);
		link.setText(String.format("%s%s%s%s", "  ", "<a>", Messages.runStatusVisible, "</a>"));
		rows.put(key, new TableRowDescriptor(item, bar, link));
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
											row.getProgressBar().setVisible(false);
											/*
											 * Need link only if service is running.
											 */
											if (args.getDeployMessage().equalsIgnoreCase(Messages.runStatus)) {
												TableEditor editor = new TableEditor(table);
												editor.grabHorizontal = editor.grabVertical = true;
												editor.setEditor(row.getLink(), row.getItem(), 1);
												row.getLink().setVisible(true);
												row.getLink().addSelectionListener(new SelectionAdapter() {
													@Override
													public void widgetSelected(SelectionEvent event) {
														try {
															PlatformUI.getWorkbench().getBrowserSupport().
															getExternalBrowser().openURL(new URL(args.getDeploymentURL()));
														} catch (Exception e) {
														}
													}
												});
											} else {
												row.getItem().setText(1, args.getDeployMessage());
											}
										}
									}
								}
							});
						}
					}
				});
	}

}
