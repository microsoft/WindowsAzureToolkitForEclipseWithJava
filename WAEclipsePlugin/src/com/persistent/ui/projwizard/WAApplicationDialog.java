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
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import waeclipseplugin.Activator;

import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.interopbridges.tools.windowsazure.WindowsAzureRoleComponent;
import com.persistent.util.MessageUtil;
import com.persistent.util.ProjectNatureHelper;
import com.persistent.util.ProjectNatureHelper.ProjExportType;
import com.persistent.winazureroles.WAServerConfiguration;

public class WAApplicationDialog extends TitleAreaDialog {
    private Button fileRadioBtn;
    private Button projRadioBtn;
    private Button browseBtn;
    private Combo projCombo;
    private Text fileTxt;
    private Text asNameTxt;
    private Label projLbl;
    private Label nameLbl;
    private String errorTitle;
    private String errorMessage;
    private Button okButton;
    private WindowsAzureRole windowsAzureRole;
    private WADeployPage depPage;
    private WAServerConfiguration serverConf;


    public WAApplicationDialog(Shell parentShell,
    WADeployPage page,
    WindowsAzureRole role,
    WAServerConfiguration conf) {
        super(parentShell);
        this.depPage = page;
        this.windowsAzureRole = role;
        this.serverConf = conf;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.appDlgTxt);
        newShell.setLocation(250, 250);
    }


    @Override
    protected Control createDialogArea(Composite parent) {
        setTitle(Messages.appDlgTxt);
        setMessage(Messages.appDlgMsg);
        // display help contents
        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
                "com.persistent.winazure.eclipseplugin."
        + "windows_azure_addapp_dialog");
        Activator.getDefault().setSaved(false);
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        GridData gridData = new GridData();
        gridLayout.numColumns = 3;
        gridLayout.marginBottom = 10;
        gridData.verticalIndent = 10;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        container.setLayout(gridLayout);
        container.setLayoutData(gridData);
        createAppFileCmpnt(container);
        createAppProjCmpnt(container);
        return super.createDialogArea(parent);
    }

    private void createAppFileCmpnt(Composite container) {
        fileRadioBtn = new Button(container, SWT.RADIO);
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalIndent = 10;
        gridData.horizontalSpan = 3;
        gridData.horizontalAlignment = SWT.FILL;
        fileRadioBtn.setText(Messages.appDlgFileBtnTxt);
        fileRadioBtn.setLayoutData(gridData);
        fileRadioBtn.setSelection(true);
        fileRadioBtn.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                projRadioBtn.setSelection(false);
                projCombo.setEnabled(false);
                asNameTxt.setEnabled(false);
                nameLbl.setEnabled(false);
                fileRadioBtn.setSelection(true);
                fileTxt.setEnabled(true);
                fileTxt.setText("");
                browseBtn.setEnabled(true);
                if (okButton != null) {
                    okButton.setEnabled(false);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
            }
        });

        fileTxt = new Text(container, SWT.LEFT | SWT.BORDER);
        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalIndent = 20;
        gridData.horizontalSpan = 2;
        gridData.widthHint = 320;
        gridData.horizontalAlignment = SWT.FILL;
        fileTxt.setLayoutData(gridData);
        fileTxt.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent arg0) {
                if (fileTxt.getText().isEmpty()) {
                    if (okButton != null) {
                        okButton.setEnabled(false);
                    }
                } else {
                    if (okButton != null) {
                        okButton.setEnabled(true);
                    }
                }
            }
        });

        browseBtn = new Button(container, SWT.PUSH | SWT.CENTER | SWT.END);
        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = SWT.FILL;
        browseBtn.setText(Messages.dplPageBtnTxt);
        browseBtn.setLayoutData(gridData);
        browseBtn.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                browseBtnListener();
            }
            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
            }
        });
    }

    private void createAppProjCmpnt(Composite container) {
        projRadioBtn = new Button(container, SWT.RADIO);
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalIndent = 10;
        gridData.horizontalSpan = 3;
        gridData.horizontalAlignment = SWT.FILL;
        projRadioBtn.setText(Messages.appDlgProjBtnTxt);
        projRadioBtn.setSelection(false);
        projRadioBtn.setLayoutData(gridData);
        projRadioBtn.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                fileRadioBtn.setSelection(false);
                projRadioBtn.setSelection(true);
                fileTxt.setEnabled(false);
                browseBtn.setEnabled(false);
                projCombo.setEnabled(true);
                asNameTxt.setEnabled(true);
                nameLbl.setEnabled(true);
                projCombo.setItems(getProjects());
                if (okButton != null) {
                    okButton.setEnabled(false);
                }
            }
            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
            }
        });

        projCombo = new Combo(container, SWT.READ_ONLY);
        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalIndent = 20;
        gridData.horizontalSpan = 2;
        gridData.horizontalAlignment = SWT.FILL;
        projCombo.setLayoutData(gridData);
        projCombo.setEnabled(false);
        projCombo.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                if (projCombo.getText().isEmpty()
                		|| asNameTxt.getText().isEmpty()) {
                    if (okButton != null) {
                        okButton.setEnabled(false);
                    } else {
                        if (okButton != null) {
                            okButton.setEnabled(true);
                        }
                    }
                }
                IWorkspace workspace = ResourcesPlugin.getWorkspace();
                IWorkspaceRoot root = workspace.getRoot();
                IProject proj = root.getProject(projCombo.getText());
                asNameTxt.setText(getAsNameFrmProject(
                		proj.getLocation().toOSString()));
            }
            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
            }
        });


        projLbl = new Label(container, SWT.LEFT);
        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = SWT.FILL;
        projLbl.setLayoutData(gridData);

        nameLbl = new Label(container, SWT.LEFT);
        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalIndent = 20;
        gridData.horizontalAlignment = SWT.FILL;
        nameLbl.setText(Messages.appDlgProjLbl);
        nameLbl.setLayoutData(gridData);
        nameLbl.setEnabled(false);

        asNameTxt = new Text(container, SWT.LEFT | SWT.BORDER);
        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.widthHint = 240;
        gridData.horizontalAlignment = SWT.FILL;
        asNameTxt.setLayoutData(gridData);
        asNameTxt.setEnabled(false);
        asNameTxt.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent arg0) {
                if (asNameTxt.getText().isEmpty()
                		|| projCombo.getText().isEmpty()) {
                    if (okButton != null) {
                        okButton.setEnabled(false);
                    }
                } else {
                    if (okButton != null) {
                        okButton.setEnabled(true);
                    }
                }
            }
        });

    }
    private void browseBtnListener() {
        FileDialog dialog = new FileDialog(this.getShell(), SWT.MULTI);
        String[] extensions = new String [3];
        extensions[0] = "*.WAR";
        extensions[1] = "*.JAR";
        extensions[2] = "*.EAR";
        dialog.setFilterExtensions(extensions);
        String file = dialog.open();
        if (file != null) {
            fileTxt.setText(file);
        }
    }

    private String[] getProjects() {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        String []projects = null;
        ArrayList<String> projList = new ArrayList<String>();
        try {
            for (IProject wRoot : root.getProjects()) {
                if (wRoot.isOpen()
                        && !wRoot.hasNature("com.persistent.ui.projectnature")) {
                    projList.add(wRoot.getProject().getName());
                }
            }
            projects = new String[projList.size()];
            projects = projList.toArray(projects);
        } catch (Exception e) {
            errorTitle = Messages.projSelTtl;
            errorMessage = Messages.projSelMsg;
            MessageUtil.displayErrorDialog(this.getShell(), errorTitle,
                    errorMessage);
            Activator.getDefault().log(errorMessage, e);
        }
        return projects;
    }

    @Override
    protected void okPressed() {
        boolean isValid = true;
        if ((fileRadioBtn.getSelection() && !fileTxt.getText().isEmpty())) {
            File file = new File(fileTxt.getText());
            if (!file.exists()) {
                isValid = false;
                errorTitle = Messages.appDlgInvFileTtl;
                errorMessage = Messages.appDlgInvFileMsg;
                MessageUtil.displayErrorDialog(this.getShell(), errorTitle,
                        errorMessage);
            }
        } else if (projRadioBtn.getSelection()
        		&& !asNameTxt.getText().isEmpty()
        		&& !projCombo.getText().isEmpty()) {
            boolean isValidName = true;
            try {
                isValidName = windowsAzureRole.
                		isValidDeployName(asNameTxt.getText());
            } catch (Exception e) {
                isValidName = false;
            }
            if (!isValidName) {
                isValid = false;
                errorTitle = Messages.appDlgInvNmeTtl;
                errorMessage = Messages.appDlgInvNmeMsg;
                MessageUtil.displayErrorDialog(this.getShell(), errorTitle,
                        errorMessage);
            }
        }
        if (isValid) {
            ArrayList<String> cmpList = null;
            if (depPage == null) {
                cmpList = serverConf.getAppsAsNames();
            } else {
                cmpList = depPage.getAppsAsNames();
            }

            if (fileRadioBtn.getSelection()) {
                if (cmpList.contains(new File(fileTxt.getText()).getName())) {
                    errorTitle = Messages.appDlgDupNmeTtl;
                    errorMessage = Messages.appDlgDupNmeMsg;
                    MessageUtil.displayErrorDialog(this.getShell(), errorTitle,
                            errorMessage);
                    return;
                }
                try {
                	List<WindowsAzureRoleComponent> components =
                			windowsAzureRole.getComponents();
                	for (int i = 0; i < components.size(); i++) {
                		if (components.get(i).getDeployName().
                				equalsIgnoreCase(new File(fileTxt.getText()).getName())) {
                			errorTitle = Messages.appDlgDupNmeTtl;
                			errorMessage = Messages.appDlgDupNmeMsg;
                			MessageUtil.displayErrorDialog(this.getShell(),
                					errorTitle, errorMessage);
                			return;
                		}
                	}
                } catch (WindowsAzureInvalidProjectOperationException e) {
                    errorTitle = Messages.addAppErrTtl;
                    errorMessage = Messages.addAppErrMsg;
                    Activator.getDefault().log(errorMessage, e);
                    MessageUtil.displayErrorDialog(this.getShell(), errorTitle,
                            errorMessage);
                }

                if (depPage == null) {
                    serverConf.addToAppList(fileTxt.getText(),
                    		new File(fileTxt.getText()).getName(),
                    		"copy");
                } else {
                    depPage.addToAppList(fileTxt.getText(),
                    		new File(fileTxt.getText()).getName(),
                    		"copy");
                }


            } else if (projRadioBtn.getSelection()) {
                IWorkspace workspace = ResourcesPlugin.getWorkspace();
                IWorkspaceRoot root = workspace.getRoot();
                IProject proj = root.getProject(projCombo.getText());
                if (cmpList.contains(new File(asNameTxt.getText()).getName())) {
                    errorTitle = Messages.appDlgDupNmeTtl;
                    errorMessage = Messages.appDlgDupNmeMsg;
                    MessageUtil.displayErrorDialog(this.getShell(), errorTitle,
                            errorMessage);
                    return;
                }
                if (depPage == null) {
                     serverConf.addToAppList(proj.getLocation().toOSString(),
                    		 asNameTxt.getText(), "auto");
                } else {
                     depPage.addToAppList(proj.getLocation().toOSString(),
                    		 asNameTxt.getText(), "auto");
                }
            }
            super.okPressed();
        }
    }

    @Override
    protected Control createButtonBar(Composite parent) {
        Control ctrl = super.createButtonBar(parent);
        okButton = getButton(IDialogConstants.OK_ID);
        okButton.setEnabled(false);
        return ctrl;
    }

    private String getAsNameFrmProject(String path) {
        String name = "";
        ProjExportType type = ProjectNatureHelper
                .getProjectNature(getProjectFrmPath(path));
        File file = new File(path);
        if (file.exists() && file.isDirectory()) {
            name = file.getName();
            if (type.equals(ProjExportType.EAR)) {
                name = name.concat(".ear");
            } else if (type.equals(ProjExportType.WAR)) {
                name = name.concat(".war");
            } else {
                name = name.concat(".jar");
            }
        }

        return name;
    }

    private IProject getProjectFrmPath(String path) {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        IProject project = null;
        ArrayList<IProject> projList = new ArrayList<IProject>();
        try {
            for (IProject wRoot : root.getProjects()) {
                if (wRoot.isOpen()
                        && !wRoot.hasNature("com.persistent.ui.projectnature")) {
                    projList.add(wRoot);
                }
            }
            IProject[] arr = new IProject[projList.size()];
            arr = projList.toArray(arr);
            for (int i = 0; i < arr.length; i++) {
                if (arr[i].getLocation().toOSString().equalsIgnoreCase(path)) {
                    project = arr[i];
                }
            }
        } catch (Exception e) {
            errorTitle = Messages.projSelTtl;
            errorMessage = Messages.projSelMsg;
            MessageUtil.displayErrorDialog(this.getShell(), errorTitle,
                    errorMessage);
            Activator.getDefault().log(errorMessage, e);
        }
        return project;
    }
}
