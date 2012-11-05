/**
 * Copyright 2011 Persistent Systems Ltd.
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
package com.persistent.winazureroles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.interopbridges.tools.windowsazure.WindowsAzureEndpoint;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;

/**
 * This Class provides the debug option to the user
 * to enable/disable project debugging.
 */
public class DebugConfigurationDialog extends TrayDialog {

    private Label hostLabel;
    private Text projText;
    private Text hostText;
    private Button emulatorCheckBox;
    private Button cloudCheckBox;
    private Button okButton;
    private WindowsAzureRole debugRole;
    private WindowsAzureEndpoint debugEndpoint;
    private Map<String,String> paramMap;
    private List<String> configList = new ArrayList<String>();
    private Label staticMsgLabel;

    /**
     * Constructor.
     * @param parentShell
     * @param role
     * @param endPoint
     * @param map
     */
    public DebugConfigurationDialog(Shell parentShell,
    		WindowsAzureRole role,
    		WindowsAzureEndpoint endPoint,
    		Map<String,String> map) {
        super(parentShell);
        debugRole = role;
        debugEndpoint = endPoint;
        paramMap = map;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.dbgTitle);
        newShell.setLocation(300, 300);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        // display help contents
                PlatformUI
                .getWorkbench()
                .getHelpSystem()
                .setHelp(getParentShell(),
                  "com.persistent.winazure.eclipseplugin." +
                  "windows_azure_debug_config");
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        GridData gridData = new GridData();
        gridLayout.numColumns = 2;
        gridData.verticalIndent = 10;
        gridData.grabExcessHorizontalSpace = true;
        container.setLayout(gridLayout);
        container.setLayoutData(gridData);

        // Different UI components creation for debug
        createProjComp(container);

        Label debugLabel = new Label(container, SWT.LEFT);
        debugLabel.setText(Messages.dbgDebugLbl);
        gridData = new GridData();
        gridData.horizontalSpan = 2;
        gridData.verticalIndent = 7;
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.horizontalIndent = 10;
        debugLabel.setLayoutData(gridData);

        //Emulator check box
        createEmulChkBox(container);

        //cloud check box
        createCloudChkBox(container);

       //host label and text box
        hostLabel = new Label(container, SWT.LEFT);
        hostLabel.setText(Messages.dbgHostLbl);
        gridData = new GridData();
        gridData.horizontalSpan = 2;
        gridData.verticalIndent = 5;
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.horizontalIndent = 28;
        hostLabel.setLayoutData(gridData);
        hostLabel.setEnabled(false);

        hostText = new Text(container, SWT.LEFT | SWT.BORDER);
        gridData = new GridData();
        gridData.horizontalIndent = 28;
        gridData.verticalIndent = 5;
        gridData.horizontalSpan = 2;
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = SWT.FILL;
        hostText.setLayoutData(gridData);
        hostText.setEnabled(false);
        hostText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent arg0) {
                okButton.setEnabled(getOKStatus());
            }
        });

        Label noteLabel = new Label(container, SWT.LEFT);
        noteLabel.setText(Messages.dbgNoteLbl);
        gridData = new GridData();
        gridData.horizontalSpan = 2;
        gridData.verticalIndent = 5;
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.horizontalIndent = 28;
        noteLabel.setLayoutData(gridData);

        return super.createDialogArea(parent);
    }

    /**
     * Creates check box for cloud debug config.
     *
     * @param container
     */
    private void createCloudChkBox(Composite container) {
        cloudCheckBox = new Button(container, SWT.CHECK);
        cloudCheckBox.setText(Messages.dbgCldChkBox);
        GridData gridData = new GridData();
        gridData.horizontalSpan = 2;
        gridData.verticalIndent = 5;
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.horizontalIndent = 10;
        cloudCheckBox.setLayoutData(gridData);
        cloudCheckBox.setSelection(false);
        cloudCheckBox.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                if (cloudCheckBox.getSelection()) {
                    hostLabel.setEnabled(true);
                    hostText.setEnabled(true);
                } else {
                    hostLabel.setEnabled(false);
                    hostText.setEnabled(false);
                }
                okButton.setEnabled(getOKStatus());
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });
    }

    /**
     * Creates check box for emulator debug config.
     *
     * @param container
     */
    private void createEmulChkBox(Composite container) {
        emulatorCheckBox = new Button(container, SWT.CHECK);
        emulatorCheckBox
                .setText(Messages.dbgEmuChkBox);
        GridData gridData = new GridData();
        gridData.horizontalSpan = 2;
        gridData.verticalIndent = 5;
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.horizontalIndent = 10;
        emulatorCheckBox.setLayoutData(gridData);
        emulatorCheckBox.setSelection(true);
        emulatorCheckBox.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                okButton.setEnabled(getOKStatus());
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });
    }

    /**
     * Creates a project component which includes label, textbox
     * and browse button to select project.
     *
     * @param container
     */
    private void createProjComp(Composite container) {
        Label projectLabel = new Label(container, SWT.LEFT);
        projectLabel.setText(Messages.dbgProjLbl);
        GridData gridData = new GridData();
        gridData.horizontalSpan = 2;
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.horizontalIndent = 10;
        projectLabel.setLayoutData(gridData);

        projText = new Text(container, SWT.LEFT | SWT.BORDER);
        gridData = new GridData();
        gridData.horizontalIndent = 10;
        gridData.verticalIndent = 5;
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = SWT.FILL;
        projText.setLayoutData(gridData);
        projText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent arg0) {
                okButton.setEnabled(getOKStatus());
            }
        });

        //browse button for project selection
        Button browseButton = new Button(container, SWT.PUSH | SWT.CENTER);
        browseButton.setText(Messages.dbgBrowseBtn);
        gridData = new GridData();
        gridData.verticalIndent = 3;
        gridData.horizontalIndent = 3;
        browseButton.setLayoutData(gridData);
        browseButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                String projectName = browseBtnListener();
                projText.setText(projectName);

            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });
    }

    @Override
    protected void okPressed() {

        if (!projText.getText().isEmpty()) {
            IProject project;
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            IWorkspaceRoot root = workspace.getRoot();
            project = root.getProject(projText.getText());
            try {
                //check for project exists/open/JAVA nature
                if (!project.exists()
                        || !project.isOpen()
                        || !project.hasNature(
                                Messages.natJava)) {
                    PluginUtil.displayErrorDialog(
                    		this.getShell(),
                    		Messages.dbgInvdProjTitle,
                    		Messages.dbgInvdProjMsg);
                    return;
                }
            } catch (Exception e) {
            	PluginUtil.displayErrorDialogAndLog(
            			this.getShell(),
            			Messages.dbgProjErrTitle,
            			Messages.dbgProjErr, e);
            }

        }

        try {
            //method which pass the required parameters for creating new
            //debug launch configuration.
            createLaunchConfigParams();
        } catch (Exception e) {
        	PluginUtil.displayErrorDialogAndLog(
        			this.getShell(),
        			Messages.dlgDbgConfErrTtl,
        			Messages.dlgDbgConfErr, e);
        }
        ConfirmDebugDialog dialog = new ConfirmDebugDialog(getParentShell());
        dialog.open();
        super.okPressed();
    }

    @Override
    protected Control createButtonBar(Composite parent) {

        Label separator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
        separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Control ctrl = super.createButtonBar(parent);
        okButton = getButton(IDialogConstants.OK_ID);
        okButton.setEnabled(false);
        return ctrl;
    }


    /**
     * Listener for Browse Button.
     * @return projName
     */
    private String browseBtnListener() {
        ElementListSelectionDialog dialog = new ElementListSelectionDialog(
                this.getShell(), new WorkbenchLabelProvider());
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        String projName = "";
        IProject proj;

        ArrayList<IProject> projList = new ArrayList<IProject>();
        try {
            for (IProject wRoot : root.getProjects()) {
                if (wRoot.isOpen()
                        && wRoot.hasNature(Messages.natJava)) {
                    projList.add(wRoot);
                }
            }

            IProject[] arr = new IProject[projList.size()];
            arr = projList.toArray(arr);
            dialog.setTitle(Messages.dbgProjSelTitle);
            dialog.setMessage(Messages.dbgProjSelMsg);
            dialog.setElements(arr);
            dialog.open();
            Object [] obj = dialog.getResult();

            if (obj != null && obj.length > 0) {
                proj = (IProject) obj[0];
                if (proj.isOpen()) {
                    projName = proj.getProject().getName();
                }
            }

        } catch (Exception e) {
        	PluginUtil.displayErrorDialogAndLog(
        			this.getShell(),
        			Messages.dbgProjErrTitle,
        			Messages.dbgProjErr, e);
        }
        return projName;
    }

    /**
     * This method returns the status of the OK button.
     * @return OK button status
     */
    private boolean getOKStatus() {
        boolean retVal = true;
        if (projText.getText().isEmpty()
                || (cloudCheckBox.getSelection() && hostText.getText()
                        .isEmpty())
                || (!cloudCheckBox.getSelection() && !emulatorCheckBox
                        .getSelection())) {
            retVal = false;
        }
        return retVal;
    }


    /**
     * This method prepares and pass the required parameters for
     * creating a new debug launch configuration.
     * @throws CoreException
     */
    protected void createLaunchConfigParams()
    		throws CoreException {
        if (emulatorCheckBox.getSelection()) {
            String configName = String.format("%s%s)",
                    Messages.dlgDbgConfEmul,
                    debugRole.getName());
            ILaunchManager manager = DebugPlugin.
                    getDefault().getLaunchManager();
            int iterate = 1;
            String tempConfigName = configName;
            while (manager.isExistingLaunchConfigurationName(tempConfigName)) {
                tempConfigName = String.format("%s (%s)", configName,
                        String.valueOf(iterate));
                iterate++;
            }
            paramMap.put(Messages.dlgDbgEmuChkd , "true");
            paramMap.put(Messages.dlgDbgEmuConf, tempConfigName);
            paramMap.put(Messages.dlgDbgEmuProj, projText.getText());
            paramMap.put(Messages.dlgDbgEmuHost,
                    Messages.dlgDbgLclHost);
            paramMap.put(Messages.dlgDbgEmuPort,
                    debugEndpoint.getPrivatePort());
            configList.add(tempConfigName);
        }
        if (cloudCheckBox.getSelection()) {
            String configName = String.format("%s%s)",
                    Messages.dlgDbgConfCloud,
                    debugRole.getName());
            ILaunchManager manager = DebugPlugin.
                    getDefault().getLaunchManager();
            int iterate = 1;
            String tempConfigName = configName;
            while (manager.isExistingLaunchConfigurationName(tempConfigName)) {
                tempConfigName = String.format("%s (%s)", configName,
                        String.valueOf(iterate));
                iterate++;
            }
            paramMap.put(Messages.dlgDbgCldChkd , "true");
            paramMap.put(Messages.dlgDbgCldConf, tempConfigName);
            paramMap.put(Messages.dlgDbgCldProj, projText.getText());
            paramMap.put(Messages.dlgDbgCldHost, hostText.getText());
            paramMap.put(Messages.dlgDbgCldPort, debugEndpoint.getPort());
            configList.add(tempConfigName);
        }

    }


    /**
     * Private class for displaying the confirmation message dialog to the user
     * with created launch configuration names.
     */
    private class ConfirmDebugDialog extends Dialog {
        @Override
        protected Control createButtonBar(Composite parent) {
            Control ctrl = super.createButtonBar(parent);
            Button okBtn = getButton(IDialogConstants.OK_ID);
            okBtn.setVisible(false);
            Button cancel = getButton(IDialogConstants.CANCEL_ID);
            cancel.setText(IDialogConstants.OK_LABEL);
            return ctrl;
        }

        /**
         * Constructor.
         * @param parentShell
         */
        public ConfirmDebugDialog(Shell parentShell) {
            super(parentShell);
        }

        @Override
        protected Control createContents(Composite parent) {
            staticMsgLabel = new Label(parent, SWT.LEFT);
            GridData gridData = new GridData();
            gridData.horizontalIndent = 15;
            gridData.verticalIndent = 20;
            staticMsgLabel.setLayoutData(gridData);
            staticMsgLabel.setText(Messages.dlgConfDbgDlgMsg);
            gridData = new GridData();
            gridData.horizontalIndent = 23;
            for (String str : configList) {
                Label lblConfirm = new Label(parent, SWT.LEFT);
                lblConfirm.setLayoutData(gridData);
                lblConfirm.setText(String.format("-   %s", str));
            }
           return super.createContents(parent);
        }

        @Override
        protected void configureShell(Shell newShell) {
            newShell.setText(Messages.dlgConfDbgTitle);
            newShell.setLocation(300, 300);
            newShell.setSize(425, 230);
            super.configureShell(newShell);
        }
    }
}
