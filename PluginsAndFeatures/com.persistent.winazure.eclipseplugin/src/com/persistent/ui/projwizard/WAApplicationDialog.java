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
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
import com.persistent.util.ProjectNatureHelper;
import com.persistent.util.ProjectNatureHelper.ProjExportType;
import com.persistent.winazureroles.WAServerConfiguration;

/**
 * Creates dialog for adding applications from workspace
 * or through file system. 
 */
public class WAApplicationDialog extends TitleAreaDialog {
    private Button fileRadioBtn;
    private Button projRadioBtn;
    private Button browseBtn;
    private Combo projCombo;
    private Text fileTxt;
    private Text asNameTxt;
    private Label projLbl;
    private Label nameLbl;
    private Button okButton;
    private WindowsAzureRole windowsAzureRole;
    private WATabPage depPage;
    private WAServerConfiguration serverConf;

    /**
     * Constructor.
     * @param parentShell
     * @param waDeployPage : object of WADeployPage
     * @param role : WindowsAzureRole
     * @param conf
     */
    public WAApplicationDialog(Shell parentShell,
    		WATabPage waDeployPage,
    WindowsAzureRole role,
    WAServerConfiguration conf) {
        super(parentShell);
        this.depPage = waDeployPage;
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

    /**
     * Method creates UI controls of application file selection
     * using file system.
     * @param container
     */
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
            	/*
            	 * If user is again selecting File radio button
            	 * which was already selected,
            	 * and some file path was present in text box
            	 * then do not make text empty and keep OK enabled.
            	 */
            	if (fileRadioBtn.getSelection()
            			&& !fileTxt.getText().equals("")) {
            		if (okButton != null) {
            			okButton.setEnabled(true);
            		}
            	} else {
            		projRadioBtn.setSelection(false);
            		projCombo.setEnabled(false);
            		asNameTxt.setText("");
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

    /**
     * Method creates UI controls of application project selection
     * using workspace.
     * @param container
     */
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
            	/*
            	 * If user is again selecting Project radio button
            	 * which was already selected,
            	 * and some value present in combo box and text box
            	 * then do not make text empty and keep OK enabled.
            	 */
            	if (projRadioBtn.getSelection()
            			&& !projCombo.getText().equals("")
            			&& !asNameTxt.getText().equals("")) {
            		if (okButton != null) {
            			okButton.setEnabled(true);
            		}
            	} else {
            		fileRadioBtn.setSelection(false);
            		projRadioBtn.setSelection(true);
            		fileTxt.setEnabled(false);
            		browseBtn.setEnabled(false);
            		projCombo.setEnabled(true);
            		asNameTxt.setEnabled(true);
            		asNameTxt.setText("");
            		nameLbl.setEnabled(true);
            		projCombo.setItems(getProjects());
            		if (okButton != null) {
            			okButton.setEnabled(false);
            		}
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

    /**
     * Browse button for application file selection using file system.
     * Only JAR, WAR and EAR files can be selected.
     */
    private void browseBtnListener() {
        FileDialog dialog = new FileDialog(this.getShell());
        String[] extensions = {"*.war", "*.WAR", "*.jar", "*.JAR", "*.ear", "*.EAR"};
        dialog.setFilterExtensions(extensions);
        String file = dialog.open();
        if (file != null) {
            fileTxt.setText(file);
        }
    }

    /**
     * Method returns array of project names
     * which are open and has azure nature.
     * @return String[]
     */
    private String[] getProjects() {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        String[] projects = null;
        ArrayList<String> projList = new ArrayList<String>();
        try {
            for (IProject wRoot : root.getProjects()) {
                if (wRoot.isOpen()
                        && !wRoot.hasNature(Messages.waProjNature)) {
                    projList.add(wRoot.getProject().getName());
                }
            }
            projects = new String[projList.size()];
            projects = projList.toArray(projects);
        } catch (Exception e) {
            PluginUtil.displayErrorDialogAndLog(this.getShell(),
            		Messages.projSelTtl,
            		Messages.projSelMsg, e);
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
                PluginUtil.displayErrorDialog(this.getShell(),
                		Messages.appDlgInvFileTtl,
                		Messages.appDlgInvFileMsg);
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
                PluginUtil.displayErrorDialog(this.getShell(),
                		Messages.appDlgInvNmeTtl,
                		Messages.appDlgInvNmeMsg);
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
                    PluginUtil.displayErrorDialog(this.getShell(),
                    		Messages.appDlgDupNmeTtl,
                    		Messages.appDlgDupNmeMsg);
                    return;
                }
                try {
                	List<WindowsAzureRoleComponent> components =
                			windowsAzureRole.getComponents();
                	for (int i = 0; i < components.size(); i++) {
                		if (components.get(i).getDeployName().
                				equalsIgnoreCase(new File(
                						fileTxt.getText()).getName())) {
                			PluginUtil.displayErrorDialog(this.getShell(),
                					Messages.appDlgDupNmeTtl,
                					Messages.appDlgDupNmeMsg);
                			return;
                		}
                	}
                } catch (WindowsAzureInvalidProjectOperationException e) {
                    PluginUtil.displayErrorDialogAndLog(this.getShell(),
                    		Messages.addAppErrTtl,
                    		Messages.addAppErrMsg, e);
                }

                if (depPage == null) {
                    serverConf.addToAppList(fileTxt.getText(),
                    		new File(fileTxt.getText()).getName(),
                    		Messages.methodCopy);
                } else {
                    depPage.addToAppList(fileTxt.getText(),
                    		new File(fileTxt.getText()).getName(),
                    		Messages.methodCopy);
                }


            } else if (projRadioBtn.getSelection()) {
                IWorkspace workspace = ResourcesPlugin.getWorkspace();
                IWorkspaceRoot root = workspace.getRoot();
                IProject proj = root.getProject(projCombo.getText());
                if (cmpList.contains(new File(asNameTxt.getText()).getName())) {
                	PluginUtil.displayErrorDialog(this.getShell(),
                			Messages.appDlgDupNmeTtl,
                			Messages.appDlgDupNmeMsg);
                    return;
                }
                if (depPage == null) {
                     serverConf.addToAppList(proj.getLocation().toOSString(),
                    		 asNameTxt.getText(),
                    		 Messages.methodAuto);
                } else {
                     depPage.addToAppList(proj.getLocation().toOSString(),
                    		 asNameTxt.getText(),
                    		 Messages.methodAuto);
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

    /**
     * Method checks type of project
     * and returns corresponding as name with
     * proper extension.
     * @param path
     * @return String
     */
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

    /**
     * Method returns project having specified from path.
     * @param path
     * @return IProject
     */
    private IProject getProjectFrmPath(String path) {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        IProject project = null;
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
            for (int i = 0; i < arr.length; i++) {
                if (arr[i].getLocation().
                		toOSString().equalsIgnoreCase(path)) {
                    project = arr[i];
                }
            }
        } catch (Exception e) {
            PluginUtil.displayErrorDialogAndLog(this.getShell(),
            		Messages.projSelTtl,
            		Messages.projSelMsg, e);
        }
        return project;
    }
}
