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
package com.persistent.ui.propertypage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

import com.interopbridges.tools.windowsazure.OSFamilyType;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzurePackageType;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.microsoftopentechnologies.azurecommons.propertypage.Azure;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
import com.persistent.util.WAEclipseHelper;

/**
 * This class creates Azure Property Page.
 */
public class WAWinAzurePropertyPage extends PropertyPage {
    private Text txtServiceName;
    private WindowsAzureProjectManager waProjManager;
    private IProject selProject;
    private String errorMessage;
    private Combo comboType;
    private Combo targetOSComboType;
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
            errorMessage = Messages.proPageErrMsgBox1
            + Messages.proPageErrMsgBox2;
            PluginUtil.displayErrorDialogAndLog(this.getShell(),
            		Messages.proPageErrTitle,
            		errorMessage, e);
        }
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = SWT.FILL;
        txtServiceName.setLayoutData(gridData);

        Label lblbuildFor = new Label(container, SWT.LEFT);
        lblbuildFor.setText(Messages.proPageBldForLbl);

        comboType = new Combo(container, SWT.READ_ONLY);
        comboType.setLayoutData(gridData);
        comboType.setItems(arrType);

        Label lblTargetOS = new Label(container, SWT.LEFT);
        lblTargetOS.setText(Messages.proPageTgtOSLbl);

        targetOSComboType = new Combo(container, SWT.READ_ONLY);
        targetOSComboType.setLayoutData(gridData);

        List<String> osNames = new ArrayList<String>();
		for (OSFamilyType osType : OSFamilyType.values()) {
			osNames.add(osType.getName());
		}

        targetOSComboType.setItems(osNames.toArray(new String[osNames.size()]));

        WindowsAzurePackageType type;
        try {
            type = waProjManager.getPackageType();
            if (type.equals(WindowsAzurePackageType.LOCAL)) {
                comboType.setText(arrType[0]);
            } else {
                comboType.setText(arrType[1]);
            }
            //Set current value for target OS
            targetOSComboType.setText(waProjManager.getOSFamily().getName());
        } catch (WindowsAzureInvalidProjectOperationException e) {
            errorMessage = Messages.proPageErrMsgBox1
            +
            Messages.proPageErrMsgBox2;
           PluginUtil.displayErrorDialogAndLog(this.getShell(),
        		   Messages.proPageErrTitle,
        		   errorMessage, e);
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
            errorMessage = Messages.proPageErrMsgBox1
            +
            Messages.proPageErrMsgBox2;
            PluginUtil.displayErrorDialogAndLog(this.getShell(),
            		Messages.proPageErrTitle, errorMessage, e);
        }
    }

    @Override
    public boolean performOk() {
    	try {
    		loadProject();
    		waProjManager = Azure.performOK(waProjManager,
    				txtServiceName.getText(),
    				comboType.getText(),
    				targetOSComboType.getText());
    		waProjManager.save();
    		WAEclipseHelper.refreshWorkspace(
    				Messages.proPageRefWarn, Messages.proPageRefMsg);
    	} catch (WindowsAzureInvalidProjectOperationException e) {
    		errorMessage = Messages.proPageErrMsgBox1
    				+
    				Messages.proPageErrMsgBox2;
    		PluginUtil.displayErrorDialogAndLog(this.getShell(),
    				Messages.proPageErrTitle,
    				errorMessage, e);
    	}
    	return super.performOk();
    }
}
