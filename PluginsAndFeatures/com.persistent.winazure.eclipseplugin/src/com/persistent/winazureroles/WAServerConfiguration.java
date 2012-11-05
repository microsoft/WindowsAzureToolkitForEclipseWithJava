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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.ide.IDE;

import waeclipseplugin.Activator;

import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.interopbridges.tools.windowsazure.WindowsAzureRoleComponent;
import com.interopbridges.tools.windowsazure.WindowsAzureRoleComponentImportMethod;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
import com.persistent.ui.projwizard.WAApplicationDialog;
import com.persistent.util.AppCmpntParam;
import com.persistent.util.ProjectNatureHelper;
import com.persistent.util.WAEclipseHelper;

/**
 * Property page for Server Configuration.
 */
public class WAServerConfiguration extends PropertyPage {
	private WindowsAzureProjectManager waProjManager;
	private WindowsAzureRole windowsAzureRole;
	private Text txtDir;
	private Text txtJdk;
	private Button btnSrvLoc;
	private Combo comboServer;
	private Group serverGrp;
	private Group jdkGrp;
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
    private ArrayList<AppCmpntParam> appList = new ArrayList<AppCmpntParam>();
	private File cmpntFile = new File(WAEclipseHelper.getTemplateFile());
	private IWorkspace workspace = ResourcesPlugin.getWorkspace();
	private IWorkspaceRoot root = workspace.getRoot();
	private ArrayList<String> fileToDel = new ArrayList<String>();
	private String finalSrvPath;
	private WindowsAzureRoleComponentImportMethod finalImpMethod;
	private String finalAsName;
	private boolean isPageDisplayed = false;

	@Override
	public String getTitle() {
		if (!isPageDisplayed) {
			return super.getTitle();
		}
		// Check JDK is already enabled or not
		// and if enabled show appropriate values on property page
		String jdkSrcPath = null;
		try {
			jdkSrcPath = windowsAzureRole.getJDKSourcePath();
		} catch (WindowsAzureInvalidProjectOperationException e) {
			PluginUtil.displayErrorDialogAndLog(getShell(),
					Messages.jdkPathErrTtl,
					Messages.getJdkErrMsg, e);
		}
		if (jdkSrcPath == null) {
			disableJDKGrp();
		} else {
			enableJDKGrp();
			txtJdk.setText(jdkSrcPath);
		}

		// Check Server is already enabled or not
		// and if enabled show appropriate values on property page
		String srvSrcPath = null;
		String srvName = null;
		try {
			srvSrcPath = windowsAzureRole.getServerSourcePath();
			srvName = windowsAzureRole.getServerName();
		} catch (WindowsAzureInvalidProjectOperationException e) {
			PluginUtil.displayErrorDialogAndLog(getShell(),
					Messages.srvErrTtl,
					Messages.getSrvBothErrMsg, e);
		}
		if (srvSrcPath != null && srvName != null) {
			serCheckBtn.setSelection(true);
			enableServerGrp();
			comboServer.setText(srvName);
			txtDir.setText(srvSrcPath);
		} else {
			disableServerGrp();
		}

		if (jdkCheckBtn.getSelection()) {
			serCheckBtn.setEnabled(true);
		} else {
			disableServerGrp();
		}

		if (tableViewer != null) {
			tableViewer.refresh();
		}

		return super.getTitle();
	}

	/**
	 * Create Server Configuration page and buttons associated with it.
	 *
	 * @param parent : parent composite.
	 * @return control
	 */
	@Override
	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				"com.persistent.winazure.eclipseplugin."
						+ "windows_azure_serverconfiguration_page");
		waProjManager = Activator.getDefault().getWaProjMgr();
		windowsAzureRole = Activator.getDefault().getWaRole();
		Activator.getDefault().setSaved(false);

		Composite container = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		container.setLayout(gridLayout);
		container.setLayoutData(gridData);

		createJDKGrp(container);
		createServerGrp(container);
		isPageDisplayed = true;
		return container;
	}

	/** Sets the JDK.
	 * @param directory
	 * @throws WindowsAzureInvalidProjectOperationException 
	 */
	private void setJDK(String jdkPath)
			throws WindowsAzureInvalidProjectOperationException {
		if (jdkPath != null && !jdkPath.isEmpty()) {
			File jdkFile = new File(jdkPath);
			if (jdkFile.exists() && jdkFile.isDirectory()) {
				windowsAzureRole.setJDKSourcePath(jdkPath,
						cmpntFile);
				/*
				 * Remove error displayed on the
				 * top of property page if any
				 * when path is set to correct value.
				 */
				setErrorMessage(null);
				if (!fileToDel.contains("jdk")) {
					fileToDel.add("jdk");
				}
			}
		}
	}

	/**
	 * Creates the JDK component.
	 *
	 * @param parent Parent container
	 */
	private void createJDKGrp(Composite parent) {

		jdkGrp =  new Group(parent, SWT.SHADOW_ETCHED_IN);
		GridLayout groupGridLayout = new GridLayout();
		GridData groupGridData = new GridData();
		groupGridData.horizontalSpan = 3;
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.horizontalIndent = 3;
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridData.widthHint = 550;
		groupGridLayout.numColumns = 3;
		groupGridLayout.verticalSpacing = 10;
		jdkGrp.setText(Messages.dplDlgJDKGrp);
		jdkGrp.setLayout(groupGridLayout);
		jdkGrp.setLayoutData(groupGridData);

		// JDK Checkbox
		jdkCheckBtn = new Button(jdkGrp, SWT.CHECK);
		groupGridData = new GridData();
		groupGridData.horizontalSpan = 3;
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridData.grabExcessHorizontalSpace  = true;
		jdkCheckBtn.setText(Messages.dplPageJdkChkBtn);
		jdkCheckBtn.setLayoutData(groupGridData);

		// JDK Directory
		lblJdkLoc = new Label(jdkGrp, SWT.LEFT);
		groupGridData = new GridData();
		groupGridData.horizontalAlignment = SWT.FILL;
		lblJdkLoc.setText(Messages.dplDlgJdkLbl);
		lblJdkLoc.setLayoutData(groupGridData);

		txtJdk = new Text(jdkGrp, SWT.LEFT | SWT.BORDER);
		groupGridData = new GridData();
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.widthHint = 330;
		groupGridData.horizontalAlignment = SWT.FILL;
		txtJdk.setLayoutData(groupGridData);
		txtJdk.addFocusListener(new FocusListener() {
			private String oldTxt = "";
			@Override
			public void focusLost(FocusEvent arg0) {
				try {
					if (!txtJdk.getText().
							equalsIgnoreCase(oldTxt)) {
						setJDK(txtJdk.getText());
					}
				} catch (WindowsAzureInvalidProjectOperationException e) {
					PluginUtil.displayErrorDialogAndLog(getShell(),
							Messages.jdkPathErrTtl,
							Messages.setJdkErrMsg, e);
				}
			}

			@Override
			public void focusGained(FocusEvent arg0) {
				oldTxt = txtJdk.getText();
			}
		});

		// JDK Browse button
		btnJdkLoc = new Button(jdkGrp, SWT.PUSH | SWT.CENTER);
		groupGridData = new GridData();
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridData.grabExcessHorizontalSpace = true;
		btnJdkLoc.setText(Messages.dplDlgBtnTxt);
		btnJdkLoc.setLayoutData(groupGridData);

		jdkCheckBtn.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (jdkCheckBtn.getSelection()) {
					try {
						// Pre-populate with auto-discovered JDK if any
						String jdkDefaultDir =
								WAEclipseHelper.jdkDefaultDirectory(null);
						setJDK(jdkDefaultDir);
						txtJdk.setText(jdkDefaultDir);
					} catch (WindowsAzureInvalidProjectOperationException e) {
						PluginUtil.displayErrorDialogAndLog(
								getShell(),
								Messages.jdkPathErrTtl,
								Messages.jdkDirErrMsg, e);
					}
					enableJDKGrp();
					serCheckBtn.setEnabled(true);
				} else {
					disableJDKGrp();
					
					// Remove server setting
					updateServer(null, null, cmpntFile);

					try {
						windowsAzureRole.setJDKSourcePath(null, cmpntFile);
					} catch (WindowsAzureInvalidProjectOperationException e) {
						PluginUtil.displayErrorDialogAndLog(
								getShell(),
								Messages.jdkPathErrTtl,
								Messages.setJdkErrMsg, e);
					}
					/*
					 * Remove error displayed on the top
					 * of property page if any
					 * when path is set to correct value.
					 */
					setErrorMessage(null);
					disableServerGrp();

					if (!fileToDel.contains("jdk")) {
						fileToDel.add("jdk");
					}
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

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
	 *
	 * @param parent Parent container
	 */
	private void createServerGrp(Composite parent) {
		serverGrp =  new Group(parent, SWT.SHADOW_ETCHED_IN);
		GridLayout groupGridLayout = new GridLayout();
		GridData groupGridData = new GridData();
		groupGridData.horizontalSpan = 3;
		/*
		 * width is not specified for server grid
		 * as it will automatically adjust
		 * and avoid scenario of browse
		 * button getting attached to the wall of pane.
		 */
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.horizontalIndent = 3;
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridLayout.numColumns = 3;
		groupGridLayout.verticalSpacing = 5;
		serverGrp.setText(Messages.dplDlgSerTxt);
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
					try {
						String[] servList = WindowsAzureProjectManager.
								getServerTemplateNames(cmpntFile);
						Arrays.sort(servList);
						comboServer.setItems(servList);
					} catch (WindowsAzureInvalidProjectOperationException e) {
						PluginUtil.displayErrorDialogAndLog(
								getShell(),
								Messages.srvErrTtl,
								Messages.getSrvNmErrMsg, e);
					}
				} else {
					disableServerGrp();
					// Remove server setting
					updateServer(null, null, cmpntFile);
					serCheckBtn.setEnabled(true);
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		// Server directory
		lblDir = new Label(serverGrp, SWT.LEFT);
		groupGridData = new GridData();
		lblDir.setText(Messages.dplDlgJdkLbl);
		lblDir.setLayoutData(groupGridData);

		txtDir = new Text(serverGrp, SWT.LEFT | SWT.BORDER);
		groupGridData = new GridData();
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridData.grabExcessHorizontalSpace = true;
		txtDir.setLayoutData(groupGridData);
		txtDir.addFocusListener(new FocusListener() {
			private String oldTxt = "";
			@Override
			public void focusLost(FocusEvent arg0) {
				if (!comboServer.getText().isEmpty()
						&& !txtDir.getText().equalsIgnoreCase(oldTxt)) {
					File srvPath = new File(txtDir.getText());
					if (srvPath.exists()
							&& srvPath.isDirectory()) {
						// Server auto-detection`
						String serverName =
								WAEclipseHelper.detectServer(srvPath);
						if (serverName != null) {
							comboServer.setText(serverName);
						} else {
							comboServer.clearSelection();
						}
						updateServer(serverName,
								txtDir.getText(), cmpntFile);
					}
				}
			}

			@Override
			public void focusGained(FocusEvent arg0) {
				oldTxt = txtDir.getText();
			}
		});

		btnSrvLoc = new Button(serverGrp, SWT.PUSH | SWT.CENTER);
		groupGridData = new GridData();
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridData.grabExcessHorizontalSpace = true;
		btnSrvLoc.setText(Messages.dplDlgBtnTxt);
		btnSrvLoc.setLayoutData(groupGridData);
		btnSrvLoc.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				serBrowseBtnListener();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		// Server dropdown
		lblSelect = new Label(serverGrp, SWT.LEFT);
		groupGridData = new GridData();
		groupGridData.horizontalAlignment = SWT.FILL;
		lblSelect.setText(Messages.dplDlgSelLbl);
		lblSelect.setLayoutData(groupGridData);

		comboServer = new Combo(serverGrp, SWT.READ_ONLY);
		groupGridData = new GridData();
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.horizontalAlignment = SWT.FILL;
		comboServer.setLayoutData(groupGridData);
		try {
			String [] servList = WindowsAzureProjectManager.
					getServerTemplateNames(cmpntFile);
			Arrays.sort(servList);
			comboServer.setItems(servList);
		} catch (WindowsAzureInvalidProjectOperationException e) {
			PluginUtil.displayErrorDialogAndLog(
					getShell(),
					Messages.srvErrTtl,
					Messages.getSrvNmErrMsg, e);
		}

		comboServer.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				updateServer(comboServer.getText(),
						txtDir.getText(),
						cmpntFile);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		custLink = new Link (serverGrp, SWT.CENTER);
		groupGridData = new GridData();
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.horizontalIndent = 10;
		custLink.setText(Messages.dplDlgSerBtn);
		custLink.setLayoutData(groupGridData);
		custLink.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				custLinkListener();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		// Applications list
		lblApp = new Label(serverGrp, SWT.LEFT);
		groupGridData = new GridData();
		groupGridData.horizontalSpan = 3;
		lblApp.setText(Messages.lblApp);
		lblApp.setLayoutData(groupGridData);
		createAppTbl(serverGrp);
	}

	/**
	 * Updates server settings when UI controls change.
	 * @param newName
	 * @param newPath
	 * @param componentFile
	 */
	private void updateServer(String newName,
			String newPath,
			File componentFile) {
		try {
			String oldName = windowsAzureRole.getServerName();
			String oldPath = windowsAzureRole.getServerSourcePath();
			String path = newPath;

			if (newName != null
					&& path != null
					&& newName.equalsIgnoreCase(oldName)
					&& path.equalsIgnoreCase(oldPath)) {
				return;
			}

			// Remove old server from approot
			if (windowsAzureRole.getServerName() != null
					&&  !fileToDel.contains("srv")) {
				fileToDel.add("srv");
				getPrevCmpntVal();
			}

			if (path == null
					|| path.isEmpty()) {
				path = Messages.dummySrvPath;
			}

			// Remove the current server if any
			windowsAzureRole.setServer(null,
					Messages.dummySrvPath,
					componentFile);

			// Add the new server if desired
			if (newName != null
					&& path != null) {
				windowsAzureRole.setServer(newName,
						path, componentFile);
			}
			/*
			 * Remove error displayed on the top of
			 * property page if any
			 * when server is set to correct value.
			 */
			setErrorMessage(null);
		}
		catch (WindowsAzureInvalidProjectOperationException e) {
			PluginUtil.displayErrorDialogAndLog(
					getShell(),
					Messages.srvErrTtl,
					Messages.setSrvNmErrMsg, e);
		}
	}

	/**
	 * Creates the application table component.
	 * @param parent : container
	 */
	private void createAppTbl(Composite parent) {
		tblApp = new Table(parent, SWT.BORDER
				| SWT.FULL_SELECTION);
		tblApp.setHeaderVisible(true);
		tblApp.setLinesVisible(true);
		GridData gridData = new GridData();
		gridData.heightHint = 90;
		gridData.horizontalIndent = 2;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = false;


		GridLayout gridLayoutTable = new GridLayout();
		tblApp.setLayout(gridLayoutTable);
		tblApp.setLayoutData(gridData);

		colName = new TableColumn(tblApp, SWT.FILL);
		colName.setText(Messages.dplPageNameLbl);
		colName.setWidth(420);


		tableViewer = new TableViewer(tblApp);
		tableViewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
			}

			@Override
			public void dispose() {
			}

			@Override
			public Object[] getElements(Object arg0) {
				List<WindowsAzureRoleComponent> srvApp1 = null;
				// Get previously added sever applications
				try {
					srvApp1 = windowsAzureRole.getServerApplications();
				} catch (WindowsAzureInvalidProjectOperationException e) {
					PluginUtil.displayErrorDialogAndLog(
							getShell(),
							Messages.srvErrTtl,
							Messages.getSrvAppErrMsg, e);
				}
				/* Display existing server
				 * applications in Applications table
				 */
				ArrayList<String> appNames = new ArrayList<String>();
				for (int i = 0; i < srvApp1.size(); i++) {
					WindowsAzureRoleComponent cmpnt = srvApp1.get(i);
					appNames.add(cmpnt.getDeployName());
				}
				return (appNames.toArray());
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

		try {
			tableViewer.setInput(windowsAzureRole.getServerApplications());
		} catch (WindowsAzureInvalidProjectOperationException e) {
			PluginUtil.displayErrorDialogAndLog(
					getShell(),
					Messages.srvErrTtl,
					Messages.getSrvAppErrMsg, e);
		}

		//Composite for buttons
		final Composite containerRoleBtn =
				new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		GridData cntGridData = new GridData();
		cntGridData.verticalAlignment = SWT.FILL;
		containerRoleBtn.setLayout(gridLayout);
		containerRoleBtn.setLayoutData(cntGridData);

		btnAdd = new Button(containerRoleBtn, SWT.PUSH);
		btnAdd.setText(Messages.rolsAddBtn);
		gridData = new GridData();
		gridData.widthHint = 95;
		gridData.horizontalAlignment = SWT.FILL;
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

		btnRemove = new Button(containerRoleBtn, SWT.PUSH);
		btnRemove.setText(Messages.rolsRemoveBtn);
		btnRemove.setEnabled(false);
		gridData = new GridData();
		gridData.widthHint = 95;
		gridData.horizontalAlignment = SWT.FILL;
		btnRemove.setLayoutData(gridData);

		/* Enable edit and remove button only when
		 * entry from table is selected.
		 */
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

	@Override
	public boolean okToLeave() {
		boolean okToProceed = false;
		boolean isJdkValid = true;
		boolean isSrvValid = true;
		// Validation for JDK
		if (jdkCheckBtn.getSelection()) {
			if (txtJdk.getText().isEmpty()) {
				isJdkValid = false;
				setErrorMessage(Messages.jdkPathErrMsg);
			} else {
				File file = new File(txtJdk.getText());
				if (file.exists()
						&& file.isDirectory()) {
					isJdkValid = true;
					setErrorMessage(null);
				} else {
					isJdkValid = false;
					setErrorMessage(Messages.jdkPathErrMsg);
				}
			}
		}

		// Validation for Server
		if (isJdkValid && serCheckBtn.getSelection()) {
			if (comboServer.getText().isEmpty()) {
				setErrorMessage(Messages.dplEmtSerMsg);
				isSrvValid = false;
			} else if (txtDir.getText().isEmpty()) {
				setErrorMessage(Messages.dplWrngSerMsg);
				isSrvValid = false;
			} else if ((new File(txtDir.getText()).exists())
					&& (new File(txtDir.getText()).isDirectory())) {
				isSrvValid = true;
				setErrorMessage(null);
			} else {
				setErrorMessage(Messages.dplWrngSerMsg);
				isSrvValid = false;
			}
		}
		if (isJdkValid && isSrvValid) {
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
		boolean isJdkValid = true;
		boolean isSrvValid = true;
		// Validation for JDK
		if (jdkCheckBtn.getSelection()) {
			if (txtJdk.getText().isEmpty()) {
				isJdkValid = false;
				PluginUtil.displayErrorDialogAndLog(getShell(),
						Messages.jdkPathErrTtl,
						Messages.jdkPathErrMsg, null);
			} else {
				File file = new File(txtJdk.getText());
				if (file.exists() && file.isDirectory()) {
					isJdkValid = true;

				} else {
					isJdkValid = false;
					PluginUtil.displayErrorDialogAndLog(
							getShell(),
							Messages.jdkPathErrTtl,
							Messages.jdkPathErrMsg, null);
				}
			}
		}

		// Validation for Server
		if (isJdkValid && serCheckBtn.getSelection()) {
			if (comboServer.getText().isEmpty()) {
				isSrvValid = false;
				PluginUtil.displayErrorDialogAndLog(
						getShell(),
						Messages.srvErrTtl,
						Messages.dplEmtSerMsg, null);
			} else if (txtDir.getText().isEmpty()) {
				isSrvValid = false;
				PluginUtil.displayErrorDialogAndLog(
						getShell(),
						Messages.srvErrTtl,
						Messages.dplWrngSerMsg, null);
			} else if ((new File(txtDir.getText()).exists())
					&& (new File(txtDir.getText()).isAbsolute())) {
				isSrvValid = true;
			} else {
				isSrvValid = false;
				PluginUtil.displayErrorDialogAndLog(
						getShell(),
						Messages.srvErrTtl,
						Messages.dplWrngSerMsg, null);
			}
		}

		if (isJdkValid && isSrvValid) {
			okToProceed = true;
		}
		if (okToProceed) {
			try {
				if (!Activator.getDefault().isSaved()) {
					waProjManager.save();
					/*
					 * Delete files from approot,
					 * whose entry from component table is removed.
					 */
					if (!fileToDel.isEmpty()) {
						for (int i = 0; i < fileToDel.size(); i++) {
							String str = fileToDel.get(i);
							if (str.equalsIgnoreCase("jdk")) {
								deleteJdkDir();
							} else if(str.equalsIgnoreCase("srv")) {
								deleteServerFile();
							} else {
								File file = new File(str);
								if (file.exists()) {
									file.delete();
								}
							}
						}
					}
					fileToDel.clear();
					Activator.getDefault().setSaved(true);
				}
				WAEclipseHelper.refreshWorkspace(
						Messages.rfrshErrTtl,
						Messages.rfrshErrMsg);
				okToProceed = super.performOk();
			} catch (WindowsAzureInvalidProjectOperationException e) {
				okToProceed = false;
				PluginUtil.displayErrorDialogAndLog(
						getShell(),
						Messages.adRolErrTitle,
						Messages.adRolErrMsgBox1
						+ Messages.adRolErrMsgBox2, e);
			}
		}
		return okToProceed;
	}

	/**
	 * Listener for browse button it is used in file system button.
	 * It will open the file system location.
	 */
	private void jdkBrowseBtnListener() {
		try {
			String oldTxt = txtJdk.getText();
			String path = WAEclipseHelper.
					jdkDefaultDirectory(oldTxt);
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
			if (directory != null
					&& !directory.equalsIgnoreCase(oldTxt)) {
				txtJdk.setText(directory);
				setJDK(directory);
			}
		}  catch (WindowsAzureInvalidProjectOperationException e) {
			PluginUtil.displayErrorDialogAndLog(
					getShell(),
					Messages.jdkPathErrTtl,
					Messages.setJdkErrMsg, e);
		}
	}

	/**
	 * Enables the JDK group component.
	 */
	private void enableJDKGrp() {
		jdkCheckBtn.setSelection(true);
		lblJdkLoc.setEnabled(true);
		txtJdk.setEnabled(true);
		btnJdkLoc.setEnabled(true);
	}

	/**
	 * Disables the JDK group component.
	 */
	private void disableJDKGrp() {
		jdkCheckBtn.setSelection(false);
		lblJdkLoc.setEnabled(false);
		txtJdk.setText("");
		txtJdk.setEnabled(false);
		btnJdkLoc.setEnabled(false);
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
		btnSrvLoc.setEnabled(true);
		txtDir.setEnabled(true);
		lblApp.setEnabled(true);
		tblApp.setEnabled(true);
		btnAdd.setEnabled(true);
	}

	/**
	 * Disables the server group component.
	 */
	private void disableServerGrp() {
		serCheckBtn.setEnabled(false);
		serCheckBtn.setSelection(false);
		comboServer.removeAll();
		comboServer.setEnabled(false);
		lblSelect.setEnabled(false);
		custLink.setEnabled(false);
		lblDir.setEnabled(false);
		btnSrvLoc.setEnabled(false);
		txtDir.setText("");
		txtDir.setEnabled(false);
		lblApp.setEnabled(false);
		tblApp.setEnabled(false);
		btnAdd.setEnabled(false);
		btnRemove.setEnabled(false);
	}

	/**
	 * Customize Link listener.
	 * This will close the property page and will open the
	 * componentssets.xml in default editor.
	 */
	private void custLinkListener() {
		boolean choice = MessageDialog.openConfirm(getShell(),
				Messages.dplSerBtnTtl, Messages.dplSerBtnMsg);
		if (choice) {
			// To close property page shell
			this.getShell().close();
			if (cmpntFile.exists() && cmpntFile.isFile()) {
				IFileStore store = EFS.getLocalFileSystem().
						getStore(cmpntFile.toURI());
				IWorkbenchPage benchPage = PlatformUI.getWorkbench().
						getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditorOnFileStore(benchPage, store);
				} catch (PartInitException e) {
					PluginUtil.displayErrorDialogAndLog(
							getShell(),
							Messages.srvErrTtl,
							Messages.fileOpenErrMsg, e);
				}
			}
		}
	}

	/**
	 * Listener for browse button it is used in file system button.
	 * It will open the file system location.
	 */
	private void serBrowseBtnListener() {
		String oldTxt = txtDir.getText();
		DirectoryDialog dialog = new DirectoryDialog(this.getShell());
		if (oldTxt != null) {
			File file = new File(oldTxt);
			if (!oldTxt.isEmpty()
					&& file.exists()
					&& file.isDirectory()) {
				dialog.setFilterPath(oldTxt);
			}
		}

		String directory = dialog.open();
		if (directory != null) {
			txtDir.setText(directory);

			// Auto detect server family
			String serverName = WAEclipseHelper.
					detectServer(new File(directory));
			if (serverName != null && !serverName.isEmpty()) {
				comboServer.setText(serverName);
			} else {
				comboServer.clearSelection();
			}

			/*
			 * Check server configured previously
			 * and now server name is changed.
			 */
			updateServer(comboServer.getText(),
					txtDir.getText(), cmpntFile);
		}
	}

	/**
	 * To Add the application to the application list.
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
	 * @return list of added application AsNames
	 * which is to be set in application table.
	 */
	public ArrayList<String> getAppsAsNames() {
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < appList.size(); i++) {
			list.add(appList.get(i).getImpAs());
		}
		return list;
	}

	/**
	 * @return appList
	 */
	public ArrayList<AppCmpntParam> getAppsList () {
		return appList;
	}

	/**
	 * Add Application button listener.
	 * @param parent : parent container
	 */
	private void addButtonListener(Composite parent) {
		WAApplicationDialog dialog = new WAApplicationDialog(getShell(),
				null, windowsAzureRole, this);
		dialog.open();
		List<WindowsAzureRoleComponent> srvApp = null;
		try {
			if (!getAppsAsNames().isEmpty()) {
				AppCmpntParam app = getAppsList().get(getAppsList().size() - 1);
				String impSrc = app.getImpSrc();
				String impAs = app.getImpAs();

				try {
					srvApp = windowsAzureRole.getServerApplications();
				} catch (WindowsAzureInvalidProjectOperationException e) {
					PluginUtil.displayErrorDialogAndLog(
							getShell(),
							Messages.srvErrTtl,
							Messages.getSrvAppErrMsg, e);
				}
				if (srvApp.size() == 0) {
					windowsAzureRole.addServerApplication(impSrc, impAs,
							app.getImpMethod(), cmpntFile);
				} else {
				    boolean isExist = false;
					for (int i = 0; i < srvApp.size(); i++) {
						WindowsAzureRoleComponent c = srvApp.get(i);
						if (impAs.equalsIgnoreCase(c.getDeployName())
								&& impSrc.equalsIgnoreCase(c.getImportPath())) {
						    isExist = true;
						    break;
						}
					}
					if (!isExist) {
                        windowsAzureRole.addServerApplication(impSrc, impAs,
                                app.getImpMethod(), cmpntFile);
					}
				}
			}
			tableViewer.refresh();
		} catch (WindowsAzureInvalidProjectOperationException e) {
			PluginUtil.displayErrorDialogAndLog(
					getShell(),
					Messages.srvErrTtl,
					Messages.addSrvAppErrMsg, e);
		}
	}

	/**
	 * Remove application button listener.
	 */
	private void removeButtonListener() {
		int selIndex = tableViewer.getTable().getSelectionIndex();
		if (selIndex > -1) {
			try {
				boolean choice = MessageDialog.openQuestion(new Shell(),
						Messages.appRmvTtl, Messages.appRmvMsg);
				if (choice) {
					String cmpntName = tableViewer.
							getTable().getItem(selIndex).getText().toString();
					String cmpntPath = String.format("%s%s%s%s%s",
							root.getProject(waProjManager.getProjectName()).getLocation(),
							"\\", windowsAzureRole.getName(),
							Messages.approot, cmpntName);
					windowsAzureRole.removeServerApplication(cmpntName);
					if (!fileToDel.contains(cmpntPath)) {
						fileToDel.add(cmpntPath);
					}
					tableViewer.refresh();
				}
			} catch (Exception e) {
				PluginUtil.displayErrorDialogAndLog(
						getShell(),
						Messages.srvErrTtl,
						Messages.rmvSrvAppErrMsg, e);
			}
		}
	}

	/**
	 * To delete jdk directory which is present inside approot
	 * if JDK source path is modified.
	 */
	private void deleteJdkDir() {
		String jdkPath = "";
		try {
			jdkPath = String.format("%s%s%s%s%s",
					root.getProject(waProjManager.getProjectName()).getLocation(),
					"\\", windowsAzureRole.getName(),
					Messages.approot, "jdk");
		} catch (WindowsAzureInvalidProjectOperationException e1) {
			PluginUtil.displayErrorDialogAndLog(
					getShell(),
					Messages.jdkPathErrTtl,
					Messages.jdkDirErrMsg, e1);
		}
		File jdkFile = new File(jdkPath);
		if (jdkFile.exists()) {
			WAEclipseHelper.deleteDirectory(jdkFile);
			WAEclipseHelper.refreshWorkspace(
					Messages.rfrshErrTtl,
					Messages.rfrshErrMsg);
		}
	}

	/**
	 *  To delete zip file or directory of
	 *  server which is present inside approot
	 *  if server name or source path is modified.
	 */
	private void deleteServerFile() {
		File srvFile = null;
		try {
			srvFile = new File(String.format("%s%s%s%s%s",
					root.getProject(waProjManager.getProjectName()).getLocation(),
					"\\", windowsAzureRole.getName(),
					Messages.approot, ProjectNatureHelper.
					getAsName(finalSrvPath, finalImpMethod, finalAsName)));
		} catch (WindowsAzureInvalidProjectOperationException e) {
			PluginUtil.displayErrorDialogAndLog(
					getShell(),
					Messages.genErrTitle,
					Messages.srvFileErr, e);
		}
		if (srvFile.exists()) {
			if (srvFile.isFile()) {
				srvFile.delete();
			} else if (srvFile.isDirectory()) {
				WAEclipseHelper.deleteDirectory(srvFile);
			}
			WAEclipseHelper.refreshWorkspace(
					Messages.rfrshErrTtl,
					Messages.rfrshErrMsg);
		}
	}

	/**
	 * Method stores previous values of server component
	 * while editing sever configuration.
	 * It is used while deleting server entry from approot.
	 */
	private void getPrevCmpntVal() {
		List<WindowsAzureRoleComponent> listComponents = null;
		try {
			listComponents = windowsAzureRole.getComponents();
		} catch (WindowsAzureInvalidProjectOperationException e) {
			PluginUtil.displayErrorDialogAndLog(
					getShell(),
					Messages.cmpntSetErrTtl,
					Messages.cmpntgetErrMsg, e);
		}
		for (int i = 0; i < listComponents.size(); i++) {
			WindowsAzureRoleComponent cmp = listComponents.get(i);
			if (cmp.getType().equalsIgnoreCase(
					Messages.typeSrvDply)) {
				finalSrvPath = cmp.getImportPath();
				finalImpMethod = cmp.getImportMethod();
				finalAsName = cmp.getDeployName();
			}
		}
	}
}
