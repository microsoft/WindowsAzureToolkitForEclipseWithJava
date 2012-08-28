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
package com.persistent.ui.propertypage;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

import waeclipseplugin.Activator;

import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzurePackageType;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.persistent.util.WAEclipseHelper;
import com.persistent.util.MessageUtil;

/**
 *
 * This class creates Windows Azure Property Page
 *
 */
public class WAWinAzurePropertyPage extends PropertyPage {
    private Text  txtServiceName;
    private WindowsAzureProjectManager waProjManager;
    private IProject selProject;
    private String errorTitle;
    private String errorMessage;
    private Combo comboType;
    private static String[] arrType = {Messages.proPageBFEmul,
        Messages.proPageBFCloud };
    /**
     * Draw controls for property page.
     *
     *  @param parent
     *
     *  @return Control on which controls(button/label) are drawn
     */
    @Override
    protected Control createContents(Composite parent) {
        noDefaultAndApplyButton();
        //display help content
        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
                "com.persistent.winazure.eclipseplugin."
        + "windows_azure_project_project_property");
        loadProject();
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        container.setLayout(gridLayout);

        Label lblServiceName = new Label(container, SWT.LEFT);
        lblServiceName.setText(Messages.proPageServName);

        txtServiceName = new Text(container, SWT.SINGLE | SWT.BORDER);
        try {
            txtServiceName.setText(waProjManager.getServiceName());
        } catch (WindowsAzureInvalidProjectOperationException e) {
            errorTitle = Messages.proPageErrTitle;
            errorMessage = Messages.proPageErrMsgBox1
            + Messages.proPageErrMsgBox2;
            MessageUtil.displayErrorDialog(this.getShell(), errorTitle,
                    errorMessage);
            Activator.getDefault().log(errorMessage, e);
        }
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        txtServiceName.setLayoutData(gridData);

        Label lblbuildFor = new Label(container, SWT.LEFT);
        lblbuildFor.setText(Messages.proPageBldForLbl);

        comboType = new Combo(container, SWT.READ_ONLY);
        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        comboType.setLayoutData(gridData);
        comboType.setItems(arrType);
        WindowsAzurePackageType type;
        try {
            type = waProjManager.getPackageType();
            if (type.equals(WindowsAzurePackageType.LOCAL)) {
                comboType.setText(arrType[0]);
            } else {
                comboType.setText(arrType[1]);
            }
        } catch (WindowsAzureInvalidProjectOperationException e) {
            errorTitle = Messages.proPageErrTitle;
            errorMessage = Messages.proPageErrMsgBox1
            +
            Messages.proPageErrMsgBox2;
            MessageUtil.displayErrorDialog(this.getShell(), errorTitle,
                    errorMessage);
            Activator.getDefault().log(errorMessage, e);
        }
        return container;
    }

    /**
     * This method loads the projects available in workspace.
     * selProject variable will contain value of current selected project.
     */
    private void loadProject() {
    	selProject = WAEclipseHelper.getSelectedProject();
    	String path = selProject.getLocation().toPortableString();
    	File projDirPath = new File(path);
        try {
            waProjManager = WindowsAzureProjectManager.
            load(projDirPath);
        } catch (WindowsAzureInvalidProjectOperationException e) {
            errorTitle = Messages.proPageErrTitle;
            errorMessage = Messages.proPageErrMsgBox1
            +
            Messages.proPageErrMsgBox2;
            MessageUtil.displayErrorDialog(this.getShell(), errorTitle,
                    errorMessage);
            Activator.getDefault().log(Messages.remAccErProjLoad, e);
        }
    }

    @Override
    public boolean performOk() {
        try {
            loadProject();
            String serviceName = txtServiceName.getText();
            if (waProjManager.isValidServiceName(serviceName)) {
                waProjManager.setServiceName(serviceName);
            }
            String type = comboType.getText();
            if (type.equalsIgnoreCase(Messages.proPageBFEmul)) {
                waProjManager.setPackageType(WindowsAzurePackageType.LOCAL);
            } else {
                waProjManager.setPackageType(WindowsAzurePackageType.CLOUD);
            }
            waProjManager.save();
            WAEclipseHelper.refreshWorkspace(
            		Messages.proPageRefWarn, Messages.proPageRefMsg);
        } catch (WindowsAzureInvalidProjectOperationException e) {
            errorTitle = Messages.proPageErrTitle;
            errorMessage = Messages.proPageErrMsgBox1
            +
            Messages.proPageErrMsgBox2;
            MessageUtil.displayErrorDialog(this.getShell(), errorTitle,
                    errorMessage);
            Activator.getDefault().log(errorMessage, e);
        }
        return super.performOk();
    }
}
