/**
 * Copyright 2012 Persistent Systems Ltd.
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
package com.persistent.ui.projwizard;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import waeclipseplugin.Activator;

import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.persistent.util.AppCmpntParam;
import com.persistent.util.WAEclipseHelper;

/**
 * This class creates page for configuring JDK
 * and server with application.
 */
public class WADeployPage extends WizardPage {
	private Text txtDir;
	private Text txtJdk;
	private Button btnZipLoc;
	private Combo comboServer;
	private Group serverGrp;
	private Group jdkGrp;
	private WindowsAzureRole waRole;
	private Button jdkCheckBtn;
	private Label lblJdkLoc;
	private Button btnJdkLoc;
	private Button serCheckBtn;
	private Label lblSelect;
	private Link custLink;
	private Label lblDir;
	private Label lblApp;
	private Table tblApp;
	private Button btnRemove;
	private Button btnAdd;
	private TableViewer tableViewer;
	private TableColumn colName;
	private Object pageObj;
	private boolean isWizard;
	private ArrayList<AppCmpntParam> appList = new ArrayList<AppCmpntParam>();

	/**
	 * Constructor with page name.
	 * @param pageName : name of page
	 * @param waprojMgr : WindowsAzureProjectManager
	 * @param role : WindowsAzureRole
	 * @param pageObj
	 * @param isWizard
	 */
	protected WADeployPage(String pageName,
			WindowsAzureProjectManager waprojMgr,
			WindowsAzureRole role, Object pageObj,
			boolean isWizard) {
		super(pageName);
		this.waRole = role;
		this.pageObj = pageObj;
		this.isWizard = isWizard;
		setTitle(Messages.wizPageTitle);
		setDescription(Messages.dplPageMsg);
		setPageComplete(true);
		if (!Activator.getDefault().isContextMenu()) {
			try {
				AppCmpntParam acp = new AppCmpntParam();
				acp.setImpAs(waRole.getComponents().
						get(0).getDeployName());
				appList.add(acp);
			} catch (WindowsAzureInvalidProjectOperationException e) {
				Activator.getDefault().log(e.getMessage());
			}
		}
	}

	@Override
	public void createControl(Composite parent) {
		// display help contents
		PlatformUI
				.getWorkbench()
				.getHelpSystem()
		.setHelp(parent.getShell(),
						"com.persistent.winazure.eclipseplugin."
								+ "windows_azure_project");
		GridLayout gridLayout = new GridLayout();
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(gridLayout);
		container.setLayoutData(gridData);
		createJDKGrp(container);
		createServerGrp(container);
		if (Activator.getDefault().isContextMenu()) {
			jdkCheckBtn.setSelection(true);
			txtJdk.setText(WAEclipseHelper.
					jdkDefaultDirectory(null));
			txtJdk.setEnabled(true);
			lblJdkLoc.setEnabled(true);
			btnJdkLoc.setEnabled(true);
			enableServerGrp();

			String file = WAEclipseHelper.getTemplateFile();
			File cmpntFile = new File(file);
			try {
				String[] servList = WindowsAzureProjectManager.
						getServerTemplateNames(cmpntFile);
				Arrays.sort(servList);
				comboServer.setItems(servList);
			} catch (WindowsAzureInvalidProjectOperationException e) {
				Activator.getDefault().log(e.getMessage());
			}
			serCheckBtn.setSelection(true);
			handlePageComplete();
		}
		setControl(container);
	}

	/**
	 * Creates the size component.
	 * @param parent : parent container
	 */
	private void createJDKGrp(Composite parent) {
		jdkGrp = new Group(parent, SWT.SHADOW_ETCHED_IN);
		GridLayout groupGridLayout = new GridLayout();
		GridData groupGridData = new GridData();
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.horizontalIndent = 10;
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridLayout.numColumns = 3;
		groupGridLayout.verticalSpacing = 10;
		jdkGrp.setText(Messages.dplPageJDKGrp);
		jdkGrp.setLayout(groupGridLayout);
		jdkGrp.setLayoutData(groupGridData);

		// JDK Checkbox
		jdkCheckBtn = new Button(jdkGrp, SWT.CHECK);
		groupGridData = new GridData();
		groupGridData.horizontalSpan = 3;
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridData.grabExcessHorizontalSpace = true;
		jdkCheckBtn.setText(Messages.dplPageJdkChkBtn);
		jdkCheckBtn.setSelection(false);
		jdkCheckBtn.setLayoutData(groupGridData);
		jdkCheckBtn.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (jdkCheckBtn.getSelection()) {
					jdkCheckBtn.setSelection(true);
					lblJdkLoc.setEnabled(true);
					txtJdk.setEnabled(true);
					txtJdk.setText(WAEclipseHelper.
							jdkDefaultDirectory(null));
					btnJdkLoc.setEnabled(true);
					serCheckBtn.setEnabled(true);
					handlePageComplete();
				} else {
					serCheckBtn.setSelection(false);
					disableServerGrp();
					serCheckBtn.setEnabled(true);

					jdkCheckBtn.setSelection(false);
					lblJdkLoc.setEnabled(false);
					txtJdk.setText("");
					txtJdk.setEnabled(false);
					btnJdkLoc.setEnabled(false);
					serCheckBtn.setEnabled(false);
					disableServerGrp();
					handlePageComplete();
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		// JDK Directory textbox
		lblJdkLoc = new Label(jdkGrp, SWT.LEFT);
		groupGridData = new GridData();
		lblJdkLoc.setText(Messages.dplPageJdkLbl);
		lblJdkLoc.setEnabled(false);
		lblJdkLoc.setLayoutData(groupGridData);

		txtJdk = new Text(jdkGrp, SWT.LEFT | SWT.BORDER);
		groupGridData = new GridData();
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridData.widthHint = 270;
		groupGridData.grabExcessHorizontalSpace = true;
		txtJdk.setEnabled(false);
		txtJdk.setLayoutData(groupGridData);
		txtJdk.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				handlePageComplete();
			}
		});

		btnJdkLoc = new Button(jdkGrp, SWT.PUSH);
		groupGridData = new GridData();
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridData.grabExcessHorizontalSpace = true;
		btnJdkLoc.setText(Messages.dplPageBtnTxt);
		btnJdkLoc.setLayoutData(groupGridData);
		btnJdkLoc.setEnabled(false);
		btnJdkLoc.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				jdkBrowseBtnListener();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
	}

	/**
	 * Creates the server components.
	 * @param parent : parent container
	 */
	private void createServerGrp(Composite parent) {
		serverGrp = new Group(parent, SWT.SHADOW_ETCHED_IN);
		GridLayout groupGridLayout = new GridLayout();
		GridData groupGridData = new GridData();
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.horizontalIndent = 10;
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridLayout.numColumns = 3;
		groupGridLayout.verticalSpacing = 10;
		serverGrp.setText(Messages.dplPageSerTxt);
		serverGrp.setLayout(groupGridLayout);
		serverGrp.setLayoutData(groupGridData);

		// Server checkbox
		serCheckBtn = new Button(serverGrp, SWT.CHECK);
		groupGridData = new GridData();
		groupGridData.horizontalSpan = 3;
		groupGridData.horizontalAlignment = SWT.FILL;
		serCheckBtn.setText(Messages.dplPageSerChkBtn);
		serCheckBtn.setLayoutData(groupGridData);
		serCheckBtn.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (serCheckBtn.getSelection()) {
					serCheckBtn.setSelection(true);
					enableServerGrp();
					String file = WAEclipseHelper.getTemplateFile();
					File cmpntFile = new File(file);
					try {
						String[] servList =
								WindowsAzureProjectManager.
								getServerTemplateNames(cmpntFile);
						Arrays.sort(servList);
						comboServer.setItems(servList);
					} catch (WindowsAzureInvalidProjectOperationException e) {
						Activator.getDefault().log(e.getMessage());
					}
					handlePageComplete();
				} else {
					if (appList.isEmpty()) {
						serCheckBtn.setSelection(false);
						disableServerGrp();
						serCheckBtn.setEnabled(true);
						handlePageComplete();
					} else {
						serCheckBtn.setSelection(false);
						disableServerGrp();
						serCheckBtn.setEnabled(true);
						handlePageComplete();
					}
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		// Server directory selector
		lblDir = new Label(serverGrp, SWT.LEFT);
		groupGridData = new GridData();
		lblDir.setText(Messages.dplPageJdkLbl);
		lblDir.setLayoutData(groupGridData);

		txtDir = new Text(serverGrp, SWT.LEFT | SWT.BORDER);
		groupGridData = new GridData();
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridData.grabExcessHorizontalSpace = true;
		txtDir.setLayoutData(groupGridData);
		txtDir.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				handlePageComplete();
			}
		});

		btnZipLoc = new Button(serverGrp, SWT.PUSH);
		groupGridData = new GridData();
		groupGridData.horizontalAlignment = SWT.FILL;
		btnZipLoc.setText(Messages.dplPageBtnTxt);
		btnZipLoc.setLayoutData(groupGridData);
		btnZipLoc.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				serBrowseBtnListener();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		// Server family selector
		lblSelect = new Label(serverGrp, SWT.LEFT);
		groupGridData = new GridData();
		groupGridData.horizontalAlignment = SWT.FILL;
		lblSelect.setText(Messages.dplPageSelLbl);
		lblSelect.setLayoutData(groupGridData);

		comboServer = new Combo(serverGrp, SWT.READ_ONLY);
		groupGridData = new GridData();
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.horizontalAlignment = SWT.FILL;
		comboServer.setLayoutData(groupGridData);
		comboServer.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				handlePageComplete();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		custLink = new Link(serverGrp, SWT.CENTER);
		groupGridData = new GridData();
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.horizontalIndent = 10;
		custLink.setText(Messages.dplPageSerBtn);
		custLink.setLayoutData(groupGridData);
		custLink.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				serBtnListener();
			}
		});

		// Applications selector
		lblApp = new Label(serverGrp, SWT.LEFT);
		groupGridData = new GridData();
		groupGridData.horizontalSpan = 3;
		lblApp.setText(Messages.dplPageAppLbl);
		lblApp.setLayoutData(groupGridData);
		createAppTbl(serverGrp);
		if (jdkCheckBtn.getSelection()) {
			enableServerGrp();
			serCheckBtn.setSelection(false);
		} else {
			disableServerGrp();
		}

	}

	/**
	 * Listener for browse button it is used in file system button.
	 * It will open the file system location.
	 */
	protected void jdkBrowseBtnListener() {
		try {
			String path = WAEclipseHelper.
					jdkDefaultDirectory(txtJdk.getText());
			DirectoryDialog dialog =
					new DirectoryDialog(this.getShell());
			if (path != null) {
				File file = new File(path);
				if (!path.isEmpty()
						&& file.exists()
						&& file.isDirectory()) {
					dialog.setFilterPath(path);
				}
			}

			String directory = dialog.open();
			if (directory != null) {
				txtJdk.setText(directory);
			}
		} catch (Exception e) {
			Activator.getDefault().log(e.getMessage(), e);
		}
	}

	/**
	 * Listener for browse button it is used in file system button.
	 * It will open the file system location.
	 */
	protected void serBrowseBtnListener() {
		try {
			String path = txtDir.getText();
			DirectoryDialog dialog =
					new DirectoryDialog(this.getShell());
			if (path != null) {
				File file = new File(path);
				if (!path.isEmpty()
						&& file.exists()
						&& file.isDirectory()) {
					dialog.setFilterPath(path);
				}
			}
			String directory = dialog.open();
			if (directory != null) {
				txtDir.setText(directory);
				// Autodetect server family
				String serverName = WAEclipseHelper.
						detectServer(new File(directory));
				if (serverName != null
						&& !serverName.isEmpty()) {
					comboServer.setText(serverName);
				} else {
					comboServer.clearSelection();
				}
			}

			handlePageComplete();
		} catch (Exception e) {
			Activator.getDefault().log(e.getMessage(), e);
		}
	}

	/**
	 * Creates the application table component.
	 * @param parent : container
	 */
	private void createAppTbl(Composite parent) {
		tblApp = new Table(parent, SWT.MULTI | SWT.BORDER
				| SWT.FULL_SELECTION);
		tblApp.setHeaderVisible(true);
		tblApp.setLinesVisible(true);
		GridData gridData = new GridData();
		gridData.heightHint = 75;
		gridData.horizontalIndent = 2;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = false;

		GridLayout gridLayoutTable = new GridLayout();
		tblApp.setLayout(gridLayoutTable);
		tblApp.setLayoutData(gridData);

		colName = new TableColumn(tblApp, SWT.FILL);
		colName.setText(Messages.dplPageNameLbl);
		colName.setWidth(350);

		tableViewer = new TableViewer(tblApp);
		tableViewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public void inputChanged(Viewer arg0,
					Object arg1, Object arg2) {
			}

			@Override
			public void dispose() {
			}

			@Override
			public Object[] getElements(Object arg0) {
				return getAppsAsNames().toArray();
			}
		});

		tableViewer.setLabelProvider(new ITableLabelProvider() {
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
				if (colIndex == 0) {
					result = element.toString();
				}
				return result;
			}
			@Override
			public Image getColumnImage(Object arg0, int arg1) {
				return null;
			}
		});

		tableViewer.setInput(getAppsAsNames());


		// Composite for buttons
		final Composite containerRoleBtn =
				new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		GridData cntGridData = new GridData();
		cntGridData.verticalAlignment = SWT.FILL;
		containerRoleBtn.setLayout(gridLayout);
		containerRoleBtn.setLayoutData(cntGridData);

		btnAdd = new Button(containerRoleBtn, SWT.PUSH | SWT.CENTER);
		btnAdd.setText(Messages.dplPageAddBtn);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.widthHint = 90;
		btnAdd.setLayoutData(gridData);

		// Add selection listener for Add Button
		btnAdd.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				addButtonListener(containerRoleBtn);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		btnRemove = new Button(containerRoleBtn, SWT.PUSH | SWT.CENTER);
		btnRemove.setText(Messages.dplPageRmvBtn);
		btnRemove.setEnabled(false);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		btnRemove.setLayoutData(gridData);
		tblApp.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				btnRemove.setEnabled(true);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {

			}
		});
		// Add selection listener for Remove Button
		btnRemove.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				removeButtonListener();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {

			}
		});
	}

	/**
	 * Add Application button listener.
	 * @param parent : parent container
	 */
	private void addButtonListener(Composite parent) {
		WAApplicationDialog dialog = new WAApplicationDialog(getShell(),
				this, waRole, null);
		dialog.open();
		tableViewer.refresh();

	}

	/**
	 * Remove application button listener.
	 */
	private void removeButtonListener() {
		int selIndex = tableViewer.getTable().getSelectionIndex();
		if (selIndex > -1) {
			try {
				appList.remove(selIndex);
				tableViewer.refresh();
			} catch (Exception e) {
				Activator.getDefault().log(e.getMessage(), e);
			}
		}
	}

	/**
	 * Enables the server group component.
	 */
	private void enableServerGrp() {
		serCheckBtn.setEnabled(true);
		comboServer.setEnabled(true);
		lblSelect.setEnabled(true);
		custLink.setEnabled(true);
		lblDir.setEnabled(true);
		btnZipLoc.setEnabled(true);
		txtDir.setEnabled(true);
		lblApp.setEnabled(true);
		tblApp.setEnabled(true);
		btnAdd.setEnabled(true);
	}

	/**
	 * Disables the server group component if check box is not selected.
	 */
	private void disableServerGrp() {
		serCheckBtn.setEnabled(false);
		serCheckBtn.setSelection(false);
		comboServer.setEnabled(false);
		lblSelect.setEnabled(false);
		custLink.setEnabled(false);
		lblDir.setEnabled(false);
		btnZipLoc.setEnabled(false);
		txtDir.setEnabled(false);
		txtDir.setText("");
		comboServer.removeAll();
		lblApp.setEnabled(false);
		tblApp.setEnabled(false);
		btnAdd.setEnabled(false);
		btnRemove.setEnabled(false);
	}

	/**
	 * Handles the page complete event of deploy page.
	 */
	private void handlePageComplete() {
		boolean isJdkValid = false;
		if (jdkCheckBtn.getSelection()) {
			if (txtJdk.getText().isEmpty()) {
				setPageComplete(false);
			} else {
				File file = new File(txtJdk.getText());
				if (!file.exists()) {
					setErrorMessage(Messages.dplWrngJdkMsg);
					setPageComplete(false);
				} else {
					setErrorMessage(null);
					setPageComplete(true);
					isJdkValid = true;
				}
			}
			if (isJdkValid && serCheckBtn.getSelection()) {
				if (comboServer.getText().isEmpty()) {
					setErrorMessage(Messages.dplEmtSerMsg);
					setPageComplete(false);
				} else if (txtDir.getText().isEmpty()) {
					setErrorMessage(Messages.dplEmtSerPtMsg);
					setPageComplete(false);
				} else if (!(new File(txtDir.
						getText()).exists())) {
					setErrorMessage(Messages.dplWrngSerMsg);
					setPageComplete(false);
				} else {
					setErrorMessage(null);
					setPageComplete(true);
				}
			}
		} else {
			setPageComplete(true);
		}
	}

	/**
	 * Server button listener. This will close the wizard and open the
	 * componentssets.xml in default editor.
	 */
	private void serBtnListener() {
		boolean choice = MessageDialog.openConfirm(getShell(),
				Messages.dplSerBtnTtl, Messages.dplSerBtnMsg);
		if (choice) {
			try {
				if (isWizard) {
					WAProjectWizard wiz =
							(WAProjectWizard) pageObj;
					wiz.getShell().close();
				}
				String file = WAEclipseHelper.getTemplateFile();
				File cmpntFile = new File(file);
				if (cmpntFile.exists() && cmpntFile.isFile()) {
					IFileStore store = EFS.getLocalFileSystem().
							getStore(cmpntFile.toURI());
					IWorkbenchPage benchPage = PlatformUI.
							getWorkbench().getActiveWorkbenchWindow()
							.getActivePage();
					IDE.openEditorOnFileStore(benchPage, store);
				}
			} catch (PartInitException e) {
				Activator.getDefault().log(e.getMessage(), e);
			}
		}
	}

	/**
	 * Gives JDK location specified by user.
	 * @return JDK home location
	 */
	public String getJdkLoc() {
		return txtJdk.getText();
	}

	/**
	 * Gives server installation location specified by user.
	 * @return server home location
	 */
	public String getServerLoc() {
		return txtDir.getText();
	}

	/**
	 * Gives server selected by user.
	 * @return serverName
	 */
	public String getServerName() {
		return comboServer.getText();
	}

	/**
	 * @return whether JDK check box is checked or not.
	 */
	public String isJDKChecked() {
		String checked = "false";
		if (jdkCheckBtn.getSelection()) {
			checked = "true";
		}
		return checked;
	}

	/**
	 * @return whether Server check box is checked or not.
	 */
	public String isServerChecked() {
		String checked = "false";
		if (serCheckBtn.getSelection()) {
			checked = "true";
		}
		return checked;
	}

	/**
	 * Adds the application to the application list.
	 * @param src : import source location
	 * @param name : import as name
	 * @param method : import method
	 */
	public void addToAppList(String src, String name, String method) {
		AppCmpntParam param = new AppCmpntParam();
		param.setImpSrc(src);
		param.setImpAs(name);
		param.setImpMethod(method);
		appList.add(param);
	}

	/**
	 * @return added application Asnames which is to be set in table.
	 */
	public ArrayList<String> getAppsAsNames() {
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < appList.size(); i++) {
			list.add(appList.get(i).getImpAs());
		}
		return list;
	}

	/**
	 * @return applist
	 */
	public ArrayList<AppCmpntParam> getAppsList() {
		return appList;
	}

}
