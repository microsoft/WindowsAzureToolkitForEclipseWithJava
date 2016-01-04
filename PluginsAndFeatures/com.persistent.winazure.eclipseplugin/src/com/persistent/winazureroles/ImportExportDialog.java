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

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import waeclipseplugin.Activator;

import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.interopbridges.tools.windowsazure.WindowsAzureRoleComponent;
import com.interopbridges.tools.windowsazure.WindowsAzureRoleComponentDeployMethod;
import com.interopbridges.tools.windowsazure.WindowsAzureRoleComponentImportMethod;
import com.microsoftopentechnologies.azurecommons.roleoperations.ImportExportDialogUtilMethods;
import com.microsoftopentechnologies.azurecommons.storageregistry.StorageRegistryUtilMethods;
import com.microsoftopentechnologies.azurecommons.util.WAEclipseHelperMethods;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
import com.persistent.util.JdkSrvConfig;
import com.persistent.util.ProjectNatureHelper;
import com.persistent.util.ProjectNatureHelper.ProjExportType;
/**
 * Class creates UI controls and respective listeners
 * for add or edit component dialog.
 */
public class ImportExportDialog extends TitleAreaDialog {
	private Text txtFromPath;
	private Text txtUrl;
	private Combo comboStrgAcc;
	private Text txtName;
	private Text txtToDir;
	private Combo comboImport;
	private Combo comboDeploy;
	private Combo comboCloud;
	private boolean isEdit;
	private Button dlCheckBtn;
	private Label lblUrl;
	private Label lblStrgAcc;
	private Label lblDlMethod;
	private WindowsAzureRole windowsAzureRole;
	private WindowsAzureRoleComponent winAzureRoleCmpnt;
	private WindowsAzureProjectManager winAzureProjMgr;
	private static final String BASE_PATH = "${basedir}" + File.separator + "..";
	public ArrayList<String> cmpList = new ArrayList<String>();
	private String[] cloudMethods = {"same", "unzip",
	"copy"};
	private Link accLink;

	/**
	 * Constructor to be called for adding component.
	 *
	 * @param parentShell
	 * @param waProjManager
	 */
	public ImportExportDialog(Shell parentShell,
			WindowsAzureProjectManager waProjManager) {
		super(parentShell);
		this.winAzureProjMgr = waProjManager;
		this.windowsAzureRole = Activator.getDefault().getWaRole();
	}

	/**
	 * Constructor to be called for editing an existing component.
	 * @param parentShell
	 * @param windowsAzureRole
	 * @param component component to be edited
	 * @param waProjManager
	 */
	public ImportExportDialog(Shell parentShell,
			WindowsAzureRole windowsAzureRole,
			WindowsAzureRoleComponent component,
			WindowsAzureProjectManager waProjManager) {
		super(parentShell);
		this.windowsAzureRole = windowsAzureRole;
		this.winAzureRoleCmpnt = component;
		this.winAzureProjMgr = waProjManager;
		this.isEdit = true;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		if (isEdit) {
			newShell.setText(Messages.dlgImpEdtShlTtl);
		} else {
			newShell.setText(Messages.dlgImpShellTtl);
		}
		newShell.setLocation(250, 250);
		Image image;
		try {
			URL imgUrl = Activator.getDefault().getBundle()
					.getEntry(Messages.lclDlgImg);
			URL imgFileURL = FileLocator.toFileURL(imgUrl);
			URL path = FileLocator.resolve(imgFileURL);
			String imgpath = path.getFile();
			image = new Image(null, new FileInputStream(imgpath));
			setTitleImage(image);
		} catch (Exception e) {
			PluginUtil.displayErrorDialogAndLog(
					getShell(),
					Messages.genErrTitle,
					Messages.lclDlgImgErr, e);
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(Messages.dlgImpTtl);
		setMessage(Messages.dlgImpMsg);
		// display help contents
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				"com.persistent.winazure.eclipseplugin."
		+ "windows_azure_importexport_dialog");
		Activator.getDefault().setSaved(false);
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		container.setLayout(gridLayout);
		container.setLayoutData(gridData);
		createImportGrp(container);
		createDeployGrp(container);
		createDownloadGrp(container);
		try {
			for (int i = 0; i < windowsAzureRole.
					getComponents().size(); i++) {
				WindowsAzureRoleComponent cmpnt =
						windowsAzureRole.getComponents().get(i);
				cmpList.add(ProjectNatureHelper.
						getAsName(cmpnt.getImportPath(),
						cmpnt.getImportMethod(),
						cmpnt.getDeployName()).toLowerCase());
			}
		} catch (WindowsAzureInvalidProjectOperationException e) {
			PluginUtil.displayErrorDialogAndLog(
					this.getShell(),
					Messages.cmpntSetErrTtl,
					Messages.cmpntgetErrMsg, e);
		}
		if (isEdit) {
			populateData();
		}
		return super.createDialogArea(parent);
	}

	/**
	 * Method creates deploy from download
	 * group and its components.
	 * @param parent
	 */
	private void createDownloadGrp(Composite parent) {
		Group dlGrp = new Group(parent, SWT.SHADOW_ETCHED_IN);
		GridLayout groupGridLayout = new GridLayout();
		GridData groupGridData = new GridData();
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.horizontalIndent = 10;
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridLayout.numColumns = 3;
		groupGridLayout.verticalSpacing = 10;
		dlGrp.setText(Messages.dlgDownloadGrp);
		dlGrp.setLayout(groupGridLayout);
		dlGrp.setLayoutData(groupGridData);
		createDplFrmDlChk(dlGrp);
		createUrlComponent(dlGrp);
		createCloudMethodComponent(dlGrp);
		createStrgAccComponent(dlGrp);
		setEnableDlGrp(false);
	}

	/**
	 * Method prepares and returns grid data object
	 * as per type of UI component combo box or text box.
	 * @param isCombo
	 * @return
	 */
	private GridData prepareGridDataDnldGrp(boolean isCombo) {
		GridData gridData = new GridData();
		gridData.horizontalIndent = 2;
		if (isCombo) {
			gridData.widthHint = 310;
		} else {
			gridData.widthHint = 300;
			gridData.horizontalSpan = 2;
			gridData.horizontalAlignment = SWT.FILL;
			gridData.grabExcessHorizontalSpace = true;
		}
		return gridData;
	}

	/**
	 * Method creates UI components related to
	 * URL and their listeners.
	 * @param dlGrp
	 */
	private void createUrlComponent(Composite dlGrp) {
		lblUrl = JdkSrvConfig.createUrlComponentLbl(dlGrp);
		txtUrl = new Text(dlGrp, SWT.LEFT | SWT.BORDER);
		txtUrl.setLayoutData(prepareGridDataDnldGrp(false));

		txtUrl.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent arg0) {
				/*
				 * Extract storage account name
				 * and service endpoint from URL
				 * entered by user.
				 */
				String url = txtUrl.getText().trim();
				String nameInUrl =
						StorageRegistryUtilMethods.getAccNameFromUrl(
								url);
				/*
				 * If matching account name found then set to that
				 * else set to none.
				 */
				comboStrgAcc = JdkSrvConfig.
						urlModifyListner(url, nameInUrl, comboStrgAcc);
			}
		});
	}

	/**
	 * Method creates deploy from download
	 * group's check box component.
	 * @param parent
	 */
	private void createDplFrmDlChk(Composite parent) {
		dlCheckBtn = JdkSrvConfig.createDplFrmDlChk(parent);
		dlCheckBtn.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (dlCheckBtn.getSelection()) {
					setEnableDlGrp(true);
				} else {
					setEnableDlGrp(false);
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		new Link(parent, SWT.NO);
	}

	/**
	 * Method creates deploy from download
	 * group's cloud method component.
	 * @param group
	 */
	private void createCloudMethodComponent(Composite group) {
		lblDlMethod = new Label(group, SWT.LEFT);
		lblDlMethod.setText(Messages.dlgImpMthLbl);

		comboCloud = new Combo(group, SWT.READ_ONLY);
		comboCloud.setLayoutData(prepareGridDataDnldGrp(true));
		comboCloud.setItems(cloudMethods);
		comboCloud.setText(cloudMethods[0]);

		new Link(group, SWT.NO);
	}

	/**
	 * Method creates deploy from download
	 * group's Access key component.
	 * @param group
	 */
	private void createStrgAccComponent(Composite group) {
		lblStrgAcc = new Label(group, SWT.LEFT);
		lblStrgAcc.setText(Messages.dlgDlStrgAcc);

		comboStrgAcc = new Combo(group, SWT.READ_ONLY);
		comboStrgAcc.setLayoutData(prepareGridDataDnldGrp(true));
		comboStrgAcc.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				txtUrl = JdkSrvConfig.
				cmbBoxListener(comboStrgAcc, txtUrl, null);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		accLink = JdkSrvConfig.createLink(group, Messages.linkLblAcc, false);
		accLink.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				comboStrgAcc = JdkSrvConfig.
						openAccLink(dlCheckBtn, comboStrgAcc, null);
				txtUrl = JdkSrvConfig.
						cmbBoxListener(comboStrgAcc, txtUrl, null);
			}
		});
	}

	/**
	 * Method populates values of deploy from download group
	 * that is URL, cloud method and access key.
	 */
	private void updateClouldDlGroup() {
		String url;
		try {
			url = winAzureRoleCmpnt.getCloudDownloadURL();
			if (url == null || url.isEmpty()) {
				setEnableDlGrp(false);
			} else {
				setEnableDlGrp(true);
				dlCheckBtn.setSelection(true);
				txtUrl.setText(url);
				if (winAzureRoleCmpnt.getCloudMethod() != null) {
					switch (winAzureRoleCmpnt.getCloudMethod()) {
					case none:
						comboCloud.setText(cloudMethods[0]);
						break;
					case  unzip:
						comboCloud.setText(cloudMethods[1]);
						break;
					default:
						comboCloud.setText(cloudMethods[2]);
					}
				}
				/*
				 * Find storage account name
				 * associated with the component's access key
				 * and populate it.
				 */
				String accessKey = winAzureRoleCmpnt.getCloudKey();
				comboStrgAcc = JdkSrvConfig.
				populateStrgNameAsPerKey(accessKey, comboStrgAcc);
			}
		} catch (Exception e) {
			PluginUtil.displayErrorDialogAndLog(
					this.getShell(),
					Messages.genErrTitle,
					Messages.urlKeyGetErMsg, e);
		}
	}

	/**
	 * Method validates URL and access key given
	 * for deploy from download group.
	 * @return
	 */
	private boolean validateDlGroup() {
		boolean isValidUrl = ImportExportDialogUtilMethods.
				validateDlGroup(
						dlCheckBtn.getSelection(),
						txtUrl.getText().trim());
		if (!isValidUrl) {
			PluginUtil.displayErrorDialog(
					this.getShell(),
					Messages.dlgDlUrlErrTtl,
					Messages.dlgDlUrlErrMsg);
		}
		return isValidUrl;
	}

	@Override
	protected void okPressed() {
		if (validateData()) {
			try {
				IWorkspace workspace =
						ResourcesPlugin.getWorkspace();
				IWorkspaceRoot root = workspace.getRoot();
				String newMethod = comboImport.getText();
				// if UI shows import method as war, jar or ear then internally its auto
				if (comboImport.getText().equalsIgnoreCase("WAR")
						|| comboImport.getText().equalsIgnoreCase("JAR")
						|| comboImport.getText().equalsIgnoreCase("EAR")) {
					newMethod = "auto";
				}

				if (isEdit) {
					/* If project manager object returns
					 * import method as null then in UI its none
					 * and if From Path as empty then its .\ i.e approot
					 */
					WindowsAzureRoleComponentImportMethod oldImpMethod =
							winAzureRoleCmpnt.getImportMethod();
					if (oldImpMethod == null) {
						oldImpMethod = WindowsAzureRoleComponentImportMethod.none;
					}
					String oldPath = winAzureRoleCmpnt.getImportPath();
					if (oldPath.isEmpty()) {
						oldPath = ".\\";
					}
					/* To get exported component's As name using getAsName method
					 * when component has empty As name
					 */
					String oldAsName = winAzureRoleCmpnt.getDeployName();
					if (oldAsName.isEmpty()) {
						oldAsName = ProjectNatureHelper.
								getAsName(oldPath, oldImpMethod, oldAsName);
					}

					/* if import method or from path is changed
					 * then delete exported cmpnt file from approot
					 */
					if (!newMethod.equalsIgnoreCase(oldImpMethod.name())
							|| !txtFromPath.getText().equalsIgnoreCase(oldPath)) {
						String cmpntPath = String.format("%s%s%s%s%s",
								root.getProject(winAzureProjMgr.getProjectName()).getLocation(),
								File.separator, windowsAzureRole.getName(),
								Messages.approot, oldAsName);
						File file = new File(cmpntPath);
						if (file.exists()) {
							if (file.isFile()) {
								file.delete();
							} else if (file.isDirectory()) {
								WAEclipseHelperMethods.deleteDirectory(file);
							}
						}
					}
					/* if import as is changed while import method
					 * and from path is same
					 * then rename exported cmpnt file from approot
					 */
					if (!txtName.getText().equalsIgnoreCase(winAzureRoleCmpnt.getDeployName())) {
						String cmpntPath = String.format("%s%s%s%s%s",
								root.getProject(winAzureProjMgr.getProjectName()).getLocation(),
								File.separator, windowsAzureRole.getName(),
								Messages.approot, oldAsName);
						File file = new File(cmpntPath);
						if (file.exists()) {
							String dest = String.format("%s%s%s%s%s",
									root.getProject(winAzureProjMgr.getProjectName()).getLocation(),
									File.separator, windowsAzureRole.getName(),
									Messages.approot, txtName.getText());
							file.renameTo(new File(dest));
						}
					}
				} else {
					// Error if duplicate file component entry is added
					if (cmpList.contains(ProjectNatureHelper.getAsName(txtFromPath.getText(),
							WindowsAzureRoleComponentImportMethod.valueOf(newMethod),
							txtName.getText()).toLowerCase())) {
						PluginUtil.displayErrorDialog(
								this.getShell(),
								Messages.dlgImpInvDplTtl,
								Messages.dlgImpInvDplMsg);
						return;
					}
					if (!txtName.getText().isEmpty()) {
						winAzureRoleCmpnt = windowsAzureRole.
								addComponent("importas", txtName.getText().trim());
					} else {
						winAzureRoleCmpnt = windowsAzureRole.
								addComponent("importsrc", txtFromPath.getText().trim());
					}
				}
				winAzureRoleCmpnt = ImportExportDialogUtilMethods.okPressedPart2(
						winAzureRoleCmpnt,
						txtToDir.getText().trim(),
						comboDeploy.getText(),
						txtName.getText().trim(),
						comboImport.getText(),
						txtUrl.getText().trim(),
						cloudMethods,
						comboCloud.getText(),
						JdkSrvConfig.getAccessKey(comboStrgAcc));

				if (!txtFromPath.getText().startsWith(BASE_PATH)
						&& txtFromPath.getText().
						contains(root.getLocation().toOSString())) {
					String wrkSpcPath = root.getLocation().toOSString();
					String replaceString = txtFromPath.getText().trim();
					String subString = txtFromPath.getText().substring(
							txtFromPath.getText().indexOf(wrkSpcPath),
							wrkSpcPath.length());
					txtFromPath.setText(replaceString.
							replace(subString, BASE_PATH));
				}
				if (!txtFromPath.getText().isEmpty()
						|| !txtFromPath.getText().equalsIgnoreCase("\\.")) {
					winAzureRoleCmpnt.setImportPath(
							txtFromPath.getText().trim());
				}
				super.okPressed();
			} catch (WindowsAzureInvalidProjectOperationException e) {
				PluginUtil.displayErrorDialogAndLog(
						this.getShell(),
						Messages.cmpntSetErrTtl,
						Messages.cmpntRmvErrMsg, e);
			}
		}
	}

	/**
	 * Populates the corresponding values of selected
	 * component for editing.
	 */
	private void populateData() {
		if (winAzureRoleCmpnt.getImportPath().isEmpty()
				|| winAzureRoleCmpnt.getImportPath() == null) {
			txtFromPath.setText(".\\");
		} else {
			txtFromPath.setText(winAzureRoleCmpnt.getImportPath());
		}
		if (winAzureRoleCmpnt.getDeployName().isEmpty()
				|| winAzureRoleCmpnt.getDeployName() == null) {
			txtName.setText("");
		} else {
			txtName.setText(winAzureRoleCmpnt.getDeployName());
		}
		if (winAzureRoleCmpnt.getDeployDir().isEmpty()
				|| winAzureRoleCmpnt.getDeployDir() == null) {
			txtToDir.setText(".\\");
		} else {
			txtToDir.setText(winAzureRoleCmpnt.getDeployDir());
		}

		cmpList.remove(ProjectNatureHelper.
				getAsName(winAzureRoleCmpnt.getImportPath(),
						winAzureRoleCmpnt.getImportMethod(),
						winAzureRoleCmpnt.getDeployName()).toLowerCase());

		updateImportMethodCombo(txtFromPath.getText());
		updateDeployMethodCombo();
		updateClouldDlGroup();
	}

	/**
	 * Creates the import group.
	 * @param parent Parent container
	 */
	private void createImportGrp(Composite parent) {
		Group importGrp = new Group(parent, SWT.SHADOW_ETCHED_IN);
		GridLayout groupGridLayout = new GridLayout();
		GridData groupGridData = new GridData();
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.horizontalIndent = 10;
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridLayout.numColumns = 4;
		groupGridLayout.verticalSpacing = 10;
		importGrp.setText(Messages.dlgImpImpGrp);
		importGrp.setLayout(groupGridLayout);
		importGrp.setLayoutData(groupGridData);
		createFrmPathComponent(importGrp);
		createWrkSpcButton(importGrp);
		createFileButton(importGrp);
		createDirButton(importGrp);
		createImportComponent(importGrp);
		createAsNameComponent(importGrp);
	}

	/**
	 * Creates the deploy group.
	 * @param parent Parent container
	 */
	private void createDeployGrp(Composite parent) {
		Group deployGrp = new Group(parent, SWT.SHADOW_ETCHED_IN);
		GridLayout groupGridLayout = new GridLayout();
		GridData groupGridData = new GridData();
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridData.horizontalIndent = 10;
		groupGridData.verticalIndent = 10;
		groupGridLayout.numColumns = 2;
		groupGridLayout.verticalSpacing = 10;
		deployGrp.setText(Messages.dlgImpDplGrp);
		deployGrp.setLayout(groupGridLayout);
		deployGrp.setLayoutData(groupGridData);
		createDplMethodComponent(deployGrp);
		createToDirComponent(deployGrp);
		createNoteLabel(deployGrp);
	}

	/**
	 * Listener for workspace Button.
	 */
	private void workspaceBtnListener() {
		String oldPath = txtFromPath.getText();
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(
				this.getShell(), new WorkbenchLabelProvider());
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		String projPath = "";
		IProject proj;
		ArrayList<IProject> projList = new ArrayList<IProject>();
		try {
			for (IProject wRoot : root.getProjects()) {
				if (wRoot.isOpen()
						&& !wRoot.hasNature(Messages.waProjNature)) {
					projList.add(wRoot);
				}
			}
			IProject[] arr = new IProject[projList.size()];
			arr = projList.toArray(arr);
			dialog.setTitle(Messages.dlgImpPrjTtl);
			dialog.setMessage(Messages.dlgImpPrjMsg);
			dialog.setElements(arr);
			dialog.open();
			Object[] obj = dialog.getResult();
			if (obj != null && obj.length > 0) {
				proj = (IProject) obj[0];
				if (proj.isOpen()) {
					projPath = proj.getLocation().toOSString();
					if (isWorkspaceProj(winAzureProjMgr.getProjectName())
							&& isWorkspaceProj(proj.getName())) {
						if (projPath.contains(root.getLocation().toOSString())) {
							String wrkSpcPath = root.getLocation().toOSString();
							String replaceString = projPath;
							String subString = projPath.substring(
									projPath.indexOf(wrkSpcPath),
									wrkSpcPath.length());
							projPath = replaceString.replace(subString,
									BASE_PATH);
						}
					}
					txtFromPath.setText(projPath);
					/*
					 * If new from path is selected then
					 * remove previous As name text.
					 */
					if (!oldPath.equals(projPath)) {
						txtName.setText("");
					}
					updateImportMethodCombo(projPath);
					updateDeployMethodCombo();
				}
			}
		} catch (Exception e) {
			PluginUtil.displayErrorDialogAndLog(
					this.getShell(),
					Messages.prjSelErr,
					Messages.prjSelMsg, e);
		}
	}

	/**
	 * Listener for directory button it is used for selecting any
	 * directory/folder. It will open the file system location.
	 */
	protected void dirBtnListener() {
		try {
			String oldPath = txtFromPath.getText();
			DirectoryDialog dialog = new DirectoryDialog(this.getShell());
			String directory = dialog.open();
			if (directory != null) {
				IWorkspace workspace = ResourcesPlugin.getWorkspace();
				IWorkspaceRoot root = workspace.getRoot();

				if (isWorkspaceProj(winAzureProjMgr.getProjectName())
						&& directory.contains(root.getLocation().toOSString())) {
					String wrkSpcPath = root.getLocation().toOSString();
					String replaceString = directory;
					String subString = directory.substring(
							directory.indexOf(wrkSpcPath), wrkSpcPath.length());
					directory = replaceString.replace(subString, BASE_PATH);

				}
				txtFromPath.setText(directory);
				/*
				 * If new from path is selected then
				 * remove previous As name text.
				 */
				if (!oldPath.equals(directory)) {
					txtName.setText("");
				}
				updateImportMethodCombo(directory);
				updateDeployMethodCombo();
			}
		} catch (WindowsAzureInvalidProjectOperationException e) {
			Activator.getDefault().log(e.getMessage(), e);
		}
	}

	/**
	 * Listener for file button it is used for selecting file.
	 *  It will open the file system location.
	 */
	protected void fileBtnListener() {
		try {
			String oldPath = txtFromPath.getText();
			FileDialog dialog = new FileDialog(this.getShell());
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceRoot root = workspace.getRoot();
			String wrkSpcPath = root.getLocation().toOSString();
			//Setting the default path to workspace
			dialog.setFilterPath(wrkSpcPath);
			String selFile = dialog.open();
			if (selFile != null) {
				File file = new File(selFile);
				if (file.exists()) {
					if (isWorkspaceProj(winAzureProjMgr.getProjectName())
							&& selFile.contains(wrkSpcPath)) {
						String replaceString = selFile;
						String subString = selFile.substring(
								selFile.indexOf(wrkSpcPath),
								wrkSpcPath.length());
						selFile = replaceString.replace(subString, BASE_PATH);
					}
					txtFromPath.setText(selFile);
					/*
					 * If new from path is selected then
					 * remove previous As name text.
					 */
					if (!oldPath.equals(selFile)) {
						txtName.setText("");
					}
					updateImportMethodCombo(selFile);
					updateDeployMethodCombo();
				}
			}
		} catch (WindowsAzureInvalidProjectOperationException e) {
			Activator.getDefault().log(e.getMessage(), e);
		}
	}

	/**
	 * Creates the FromPath component.
	 * @param group Group under which this component
	 * need to be added
	 */
	private void createFrmPathComponent(Composite group) {
		Label lblFrmPath = new Label(group, SWT.LEFT);
		GridData groupGridData = new GridData();
		lblFrmPath.setText(Messages.dlgImpFrmPtLbl);
		lblFrmPath.setLayoutData(groupGridData);

		txtFromPath = new Text(group, SWT.LEFT | SWT.BORDER);
		groupGridData = new GridData();
		groupGridData.horizontalIndent = 33;
		groupGridData.horizontalSpan = 3;
		groupGridData.widthHint = 400;
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridData.grabExcessHorizontalSpace = true;
		txtFromPath.setLayoutData(groupGridData);
		txtFromPath.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent arg0) {
				setMessage(Messages.dlgImpMsg);
				if (!txtFromPath.getText().isEmpty()) {
					updateImportMethodCombo(txtFromPath.getText());
					updateDeployMethodCombo();
				}
			}
			@Override
			public void focusGained(FocusEvent arg0) {
				setMessage(Messages.dlgImpTxtPtTip);
			}
		});
	}

	/**
	 * Creates workspace button.
	 * @param group Group under which this component
	 * need to be added.
	 */
	private void createWrkSpcButton(Composite group) {
		Button btnWrkSpc = new Button(group, SWT.PUSH | SWT.CENTER);
		GridData groupGridData = new GridData();
		groupGridData.horizontalIndent = 100;
		groupGridData.horizontalSpan = 2;
		groupGridData.widthHint = 100;
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.horizontalAlignment = SWT.FILL;
		btnWrkSpc.setText(Messages.dlgImpWrkBtn);
		btnWrkSpc.setLayoutData(groupGridData);
		btnWrkSpc.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				workspaceBtnListener();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
	}

	/**
	 * Creates file button.
	 * @param group Group under which this component
	 * need to be added.
	 */
	private void createFileButton(Composite group) {
		Button btnFileSys = new Button(group, SWT.PUSH | SWT.CENTER);
		GridData groupGridData = new GridData();
		groupGridData.widthHint = 100;
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.horizontalAlignment = SWT.FILL;
		btnFileSys.setText(Messages.dlgImpFileBtn);
		btnFileSys.setLayoutData(groupGridData);
		btnFileSys.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				fileBtnListener();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
	}

	/**
	 * Creates Directory button.
	 * @param group Group under which this component
	 * need to be added.
	 */
	private void createDirButton(Composite group) {
		Button btnFileSys = new Button(group, SWT.PUSH | SWT.CENTER);
		GridData groupGridData = new GridData();
		groupGridData.widthHint = 100;
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.horizontalAlignment = SWT.FILL;
		btnFileSys.setText(Messages.dlgImpDirBtn);
		btnFileSys.setLayoutData(groupGridData);
		btnFileSys.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				dirBtnListener();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
	}

	/**
	 * Create combo for import methods.
	 * @param group Group under which this component
	 * need to be added.
	 */
	private void createImportComponent(Composite group) {
		Label lblMethod = new Label(group, SWT.LEFT);
		GridData groupGridData = new GridData();
		lblMethod.setText(Messages.dlgImpMthLbl);
		lblMethod.setLayoutData(groupGridData);

		comboImport = new Combo(group, SWT.READ_ONLY);
		groupGridData = new GridData();
		groupGridData.horizontalIndent = 33;
		groupGridData.horizontalSpan = 3;
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.horizontalAlignment = SWT.FILL;
		comboImport.setLayoutData(groupGridData);
		String[] impMethods = {"WAR", "JAR",
				"EAR", "copy", "zip", "none"};
		comboImport.setItems(impMethods);
		comboImport.setText(impMethods[1]);
		comboImport.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (!txtFromPath.getText().isEmpty()
						&& !txtName.getText().isEmpty()) {
					String oldVal = txtName.getText();
					String newVal = getAsName();
					if (!oldVal.equals(newVal)) {
						txtName.setText("");
					}
				}
				updateDeployMethodCombo();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
	}

	/**
	 *Creates deployment name component.
	 * @param group Group under which this component
	 * need to be added.
	 */
	private void createAsNameComponent(Composite group) {
		Label lblName = new Label(group, SWT.LEFT);
		GridData groupGridData = new GridData();
		lblName.setText(Messages.dlgImpNameLbl);
		lblName.setLayoutData(groupGridData);

		txtName = new Text(group, SWT.LEFT | SWT.BORDER);
		groupGridData = new GridData();
		groupGridData.horizontalIndent = 33;
		groupGridData.horizontalSpan = 3;
		groupGridData.widthHint = 400;
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.horizontalAlignment = SWT.FILL;
		txtName.setLayoutData(groupGridData);
		txtName.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent arg0) {
				setMessage(Messages.dlgImpMsg);
			}
			@Override
			public void focusGained(FocusEvent arg0) {
				setMessage(Messages.dlgImpTxtNmTip);
				if (!txtFromPath.getText().isEmpty()
						&& txtName.getText().isEmpty()) {
					txtName.setText(getAsName());
				}
			}
		});
	}

	/**
	 * Method returns As Name according to
	 * import method.
	 * @return
	 */
	public String getAsName() {
		File file = new File(ProjectNatureHelper.
				convertPath(txtFromPath.getText()));
		String name = ImportExportDialogUtilMethods.
				getAsName(file, comboImport.getText());
		return name;
	}

	/**
	 * Creates combo for deployment methods.
	 * @param group Group under which this component
	 * need to be added.
	 */
	private void createDplMethodComponent(Composite group) {
		Label lblDplMethod = new Label(group, SWT.LEFT);
		GridData groupGridData = new GridData();
		lblDplMethod.setText(Messages.dlgImpMthLbl);
		lblDplMethod.setLayoutData(groupGridData);

		comboDeploy = new Combo(group, SWT.READ_ONLY);
		groupGridData = new GridData();
		groupGridData.horizontalIndent = 25;
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.horizontalAlignment = SWT.FILL;
		comboDeploy.setLayoutData(groupGridData);
		String[] dplMethods = ImportExportDialogUtilMethods.convertObjToStr(
				WindowsAzureRoleComponentDeployMethod.values());
		comboDeploy.setItems(dplMethods);
		comboDeploy.setText(dplMethods[0]);
		comboDeploy.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent arg0) {
				if (comboDeploy.getText().
						equalsIgnoreCase(
								WindowsAzureRoleComponentDeployMethod.none.name())) {
					txtToDir.setEnabled(false);
				} else {
					txtToDir.setEnabled(true);
				}
			}
		});
	}

	/**
	 * Creates to directory component.
	 * @param group Group under which this component
	 * need to be added.
	 */
	private void createToDirComponent(Composite group) {
		Label lblToDplPath = new Label(group, SWT.LEFT);
		GridData groupGridData = new GridData();
		lblToDplPath.setText(Messages.dlgImpToDirLbl);
		lblToDplPath.setLayoutData(groupGridData);

		txtToDir = new Text(group, SWT.LEFT | SWT.BORDER);
		groupGridData = new GridData();
		groupGridData.horizontalIndent = 25;
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridData.widthHint = 400;
		groupGridData.grabExcessHorizontalSpace = true;
		txtToDir.setLayoutData(groupGridData);
		txtToDir.setText(".\\");
		txtToDir.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent arg0) {
				setMessage(Messages.dlgImpMsg);
			}
			@Override
			public void focusGained(FocusEvent arg0) {
				setMessage(Messages.dlgImpTxtDirTip);
			}
		});
	}

	/**
	 * Creates note label.
	 * @param group Group under which this component
	 * need to be added.
	 */
	private void createNoteLabel(Composite group) {
		// creating a temporary label and setting it to not visible because
		// note's indentation is not working properly in different
		// resolutions.So added this
		// dummy label for indentation to work properly.
		Label lblTemp = new Label(group, SWT.LEFT);
		GridData groupGridData = new GridData();
		lblTemp.setText(Messages.dlgImpTmpLbl);
		lblTemp.setLayoutData(groupGridData);
		lblTemp.setVisible(false);

		Label lblNote = new Label(group, SWT.LEFT);
		groupGridData = new GridData();
		groupGridData.horizontalIndent = 25;
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridData.grabExcessHorizontalSpace = true;
		lblNote.setText(Messages.dlgImpNoteLbl);
		lblNote.setLayoutData(groupGridData);
	}

	/**
	 * This method used for updating the import
	 * method combo box values.Values gets changed on user input.
	 * @param impPath import file path
	 */
	private void updateImportMethodCombo(String impPath) {
		String path = impPath;
		comboImport.removeAll();
		comboImport.add(WindowsAzureRoleComponentImportMethod.copy.name());
		if (path.startsWith(BASE_PATH)) {
			path = ProjectNatureHelper.convertPath(path);
		}
		String nature = findSrcPathNature(path);
		if (nature.equalsIgnoreCase(Messages.proj)) {
			ProjExportType type = ProjectNatureHelper.getProjectNature(
					ProjectNatureHelper.findProjectFromWorkSpace(path));
			comboImport.add(type.name());
			comboImport.setText(type.name());
		} else if (nature.equalsIgnoreCase(Messages.dir)) {
			comboImport.add(WindowsAzureRoleComponentImportMethod.zip.name());
			comboImport.add(WindowsAzureRoleComponentImportMethod.none.name());
			comboImport.setText(WindowsAzureRoleComponentImportMethod.zip.name());
		} else if (nature.equalsIgnoreCase(Messages.file)) {
			comboImport.add(WindowsAzureRoleComponentImportMethod.none.name());
			if (path.endsWith(".zip")) {
				comboImport.setText(WindowsAzureRoleComponentImportMethod.copy.name());
			} else {
				comboImport.add(WindowsAzureRoleComponentImportMethod.zip.name());
				comboImport.setText(WindowsAzureRoleComponentImportMethod.copy.name());
			}
		} else {
			comboImport.add(WindowsAzureRoleComponentImportMethod.none.name());
			comboImport.remove(WindowsAzureRoleComponentImportMethod.copy.name());
			comboImport.setText(WindowsAzureRoleComponentImportMethod.none.name());
		}
		if (isEdit
				&& (txtFromPath.getText().equalsIgnoreCase(winAzureRoleCmpnt.getImportPath())
						|| txtFromPath.getText().equalsIgnoreCase("\\."))) {
			if (winAzureRoleCmpnt.getImportMethod() != null) {
				comboImport.setText(winAzureRoleCmpnt.getImportMethod().name());
			}
		}
	}


	/**
	 * This method is used for updating deploy combo
	 * box values.
	 */
	private void updateDeployMethodCombo() {
		/* If project manager object returns
		 * From Path as empty then its .\ i.e approot
		 */
		String oldPath = "";
		if (isEdit) {
			oldPath = winAzureRoleCmpnt.getImportPath();
			if (oldPath.isEmpty()) {
				oldPath = ".\\";
			}
		}

		comboDeploy.removeAll();
		comboDeploy.add(WindowsAzureRoleComponentDeployMethod.copy.name());
		comboDeploy.add(WindowsAzureRoleComponentDeployMethod.none.name());
		String impTxt = comboImport.getText();
		if (impTxt.equalsIgnoreCase("EAR")
				|| impTxt.equalsIgnoreCase("WAR")
				|| impTxt.equalsIgnoreCase("JAR")) {
			comboDeploy.setText(WindowsAzureRoleComponentImportMethod.copy.name());
		} else if (impTxt.equalsIgnoreCase(WindowsAzureRoleComponentImportMethod.zip
				.name())) {
			comboDeploy.add(WindowsAzureRoleComponentDeployMethod.unzip.name());
			comboDeploy.setText(WindowsAzureRoleComponentDeployMethod.unzip.name());
		} else if (impTxt.equalsIgnoreCase(WindowsAzureRoleComponentImportMethod.copy.name())
				|| impTxt.equalsIgnoreCase(WindowsAzureRoleComponentImportMethod.none.name())) {
			File file = new File(ProjectNatureHelper.
					convertPath(txtFromPath.getText()));
			if (!file.getAbsolutePath().endsWith(".zip")) {
				comboDeploy.add(WindowsAzureRoleComponentDeployMethod.exec.name());
				comboDeploy.setText(WindowsAzureRoleComponentDeployMethod.copy.name());
			} else if (file.exists()
					&& file.isFile()
					&& file.getAbsolutePath().endsWith(".zip")) {
				comboDeploy.add(WindowsAzureRoleComponentDeployMethod.unzip.name());
				comboDeploy.setText(WindowsAzureRoleComponentDeployMethod.unzip.name());
			}
			else if (file.exists()) {
				comboDeploy.setText(WindowsAzureRoleComponentDeployMethod.copy.name());
			} else {
				comboDeploy.setText(WindowsAzureRoleComponentDeployMethod.none.name());
			}
			if (impTxt.equalsIgnoreCase(WindowsAzureRoleComponentImportMethod.none.name())) {
				comboDeploy.setText(WindowsAzureRoleComponentDeployMethod.none.name());
			}
		}

		else {
			comboDeploy.setText(WindowsAzureRoleComponentDeployMethod.copy.name());
		}
		if (isEdit
				&& txtFromPath.getText().equalsIgnoreCase(oldPath)) {
			if (winAzureRoleCmpnt.getDeployMethod() != null) {
				comboDeploy.setText(winAzureRoleCmpnt.getDeployMethod().name());
			} else if (winAzureRoleCmpnt.getDeployMethod() == null
					|| winAzureRoleCmpnt.getDeployMethod().toString().isEmpty()) {
				comboDeploy.setText(WindowsAzureRoleComponentDeployMethod.none.name());
			}
		}
	}

	/**
	 * This method determines the nature of the import source.
	 * @param srcPath import path
	 * @return nature of resource(file,folder,project)
	 */
	private String findSrcPathNature(String srcPath) {
		String path = srcPath;
		String nature = "";
		if (path.startsWith(BASE_PATH)) {
			path = ProjectNatureHelper.convertPath(path);
		}
		if (path.equalsIgnoreCase(".\\")) {
			return nature;
		}
		File file = new File(path);
		if (!file.exists()) {
			if (!path.isEmpty()) {
				txtName.setText("");
			}
			return "";
		} else if (file.isDirectory()) {
			IProject project = ProjectNatureHelper.
					findProjectFromWorkSpace(path);
			if (project == null) {
				nature = Messages.dir;
			} else {
				nature = Messages.proj;
			}
		} else {
			//consider it as file
			nature = Messages.file;
		}
		return nature;
	}

	/**
	 * This method validated the data entered by user.
	 * @return true if all data is valid else return false
	 */
	private boolean validateData() {
		boolean isValidPath = true;
		String path = txtFromPath.getText();
		if ((path.isEmpty()
				|| txtFromPath.getText().equalsIgnoreCase(".\\"))
				&& txtName.getText().isEmpty()) {
			isValidPath = false;
			PluginUtil.displayErrorDialog(
					this.getShell(),
					Messages.impExpErrTtl,
					Messages.impExpErrMsg);
			return isValidPath;
		} else if (path.isEmpty() && !txtName.getText().isEmpty()) {
			isValidPath = true;
		}
		if (path.startsWith(BASE_PATH)) {
			path = ProjectNatureHelper.convertPath(path);
		}
		File file = new File(path);
		if (!path.isEmpty() && !file.exists()) {
			isValidPath = false;
			PluginUtil.displayErrorDialog(
					this.getShell(),
					Messages.dlgImpInvPthTtl,
					Messages.dlgImpInvPthMsg);
			return isValidPath;
		}
		boolean isvalidname = false;

		WindowsAzureRoleComponentImportMethod newMethod = null;
		// if UI shows import method as war, jar or ear then internally its auto
		if (comboImport.getText().equalsIgnoreCase("WAR")
				|| comboImport.getText().equalsIgnoreCase("JAR")
				|| comboImport.getText().equalsIgnoreCase("EAR")) {
			newMethod = WindowsAzureRoleComponentImportMethod.auto;
		} else {
			newMethod = WindowsAzureRoleComponentImportMethod.
					valueOf(comboImport.getText());
		}
		String name = ProjectNatureHelper.getAsName(txtFromPath.getText(),
				newMethod, txtName.getText());

		if ((isEdit) && (winAzureRoleCmpnt.getDeployName().
				equalsIgnoreCase(name))) {
			isvalidname = true;
		} else {
			try {
					isvalidname = windowsAzureRole.
							isValidDeployName(name);
				} catch (Exception e) {
				isvalidname = false;
			}
		}
		if (!isvalidname) {
			PluginUtil.displayErrorDialog(
					this.getShell(),
					Messages.dlgImpInvDplTtl,
					Messages.dlgImpInvDplMsg);
		}
		boolean isValidDlGrp = validateDlGroup();

		return isValidPath && isvalidname && isValidDlGrp;
	}

	/**
	 * This method is used for evaluating that the project
	 * is in workspace or not to populate the relative path
	 * of project. If that project is in same workspace then
	 * this method will return true so we have to make the path
	 * relative else we have to display absolute path.
	 * @param prjName project name
	 * @return: true if project is in workspace else false
	 */
	private boolean isWorkspaceProj(String prjName) {
		boolean isWrkspcProj = false;
		try {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceRoot root = workspace.getRoot();
			IPath location = root.getProject(prjName).getLocation();
			if (location != null) {
				String projPath = location.toOSString();
				if (projPath.contains(root.getLocation().toOSString())) {
					isWrkspcProj = true;
				}
			}
		} catch (Exception e) {
			Activator.getDefault().log(e.getMessage(), e);
		}
		return isWrkspcProj;
	}

	/**
	 * Enable or disable components of
	 * download group according to status.
	 * @param status
	 */
	private void setEnableDlGrp(boolean status) {
		txtUrl.setEnabled(status);
		comboCloud.setEnabled(status);
		lblStrgAcc.setEnabled(status);
		comboStrgAcc.setEnabled(status);
		lblDlMethod.setEnabled(status);
		lblUrl.setEnabled(status);
		if (!status) {
			txtUrl.setText("");
			comboStrgAcc.removeAll();
			comboCloud.removeAll();
		} else {
			comboCloud.setItems(cloudMethods);
			comboCloud.setText(cloudMethods[0]);
			comboStrgAcc = JdkSrvConfig.
					populateStrgAccComboBox("", comboStrgAcc, null, false);
		}
	}
}