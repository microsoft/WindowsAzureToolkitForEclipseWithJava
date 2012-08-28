/**
* Copyright 2011 Persistent Systems Ltd.
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.persistent.util.WAEclipseHelper;
import com.persistent.util.MessageUtil;

/**
 * Property page for windows azure role.
 *
 */
public class WARGeneral extends PropertyPage {

    private Text txtRoleName;
    private Combo comboVMSize;
    private Text txtNoOfInstances;
    private String[] arrVMSize = {"ExtraLarge", "Large", "Medium", "Small",
    "ExtraSmall"};
    private boolean isValidRoleName = false;
    private WindowsAzureProjectManager waProjManager;
    private WindowsAzureRole windowsAzureRole;
    private String errorTitle;
    private String errorMessage;

    /**
     * Creates components for role name, VM size and number of instances.
     *
     * @param parent : parent composite.
     * @return control
     */
    @Override
    protected Control createContents(Composite parent) {
        noDefaultAndApplyButton();
        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
                "com.persistent.winazure.eclipseplugin."
                + "windows_azure_project_roles");
        waProjManager = Activator.getDefault().getWaProjMgr();
        windowsAzureRole = Activator.getDefault().getWaRole();
        Activator.getDefault().setSaved(false);

        Composite container = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        container.setLayout(gridLayout);
        container.setLayoutData(gridData);

        createRoleNameComponent(container);
        createVMSizeComponent(container);
        createNoOfInstancesComponent(container);

        try {
            if (Activator.getDefault().isEdit()) {
                showContents();
            } else {
                txtRoleName.setText(windowsAzureRole.getName());
                windowsAzureRole.setInstances(txtNoOfInstances.getText());
                windowsAzureRole.setVMSize(comboVMSize.getText());
            }
        } catch (Exception ex) {
            errorTitle = Messages.adRolErrTitle;
            errorMessage = Messages.adRolErrMsgBox1
                    + Messages.adRolErrMsgBox2;
            MessageUtil.displayErrorDialog(this.getShell(), errorTitle,
                    errorMessage);
            Activator.getDefault().log(errorMessage, ex);
        }
        return container;
    }

    /**
     * Creates label and text box for role name,
     * and adds modify listener to text box.
     *
     * @param container
     */
    private void createRoleNameComponent(Composite container) {
        Label lblRoleName = new Label(container, SWT.LEFT);
        lblRoleName.setText(Messages.dlgRoleTxt);
        GridData gridData = new GridData();
        gridData.heightHint = 20;
        gridData.horizontalIndent = 5;
        lblRoleName.setLayoutData(gridData);

        txtRoleName = new Text(container, SWT.SINGLE | SWT.BORDER);
        gridData = new GridData();
        gridData.widthHint = 275;
        gridData.horizontalAlignment = GridData.END;
        gridData.grabExcessHorizontalSpace = true;
        txtRoleName.setLayoutData(gridData);
        txtRoleName.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent event) {
                roleNameModifyListener(event);
            }
        });
    }

    /**
     * Creates label and combo box for VM size,
     * and adds selection listener to combo.
     *
     * @param container
     */
    private void createVMSizeComponent(Composite container) {
        Label lblVMSize = new Label(container, SWT.LEFT);
        lblVMSize.setText(Messages.dlgVMSize);
        GridData gridData = new GridData();
        gridData.heightHint = 20;
        gridData.horizontalIndent = 5;
        lblVMSize.setLayoutData(gridData);

        comboVMSize = new Combo(container, SWT.READ_ONLY);
        gridData = new GridData();
        gridData.widthHint = 260;
        gridData.horizontalIndent = 5;
        gridData.horizontalAlignment = GridData.END;
        gridData.grabExcessHorizontalSpace = true;
        comboVMSize.setLayoutData(gridData);
        comboVMSize.setItems(arrVMSize);
        comboVMSize.setText(arrVMSize[3]);
        comboVMSize.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                vmSizeSelectionListener();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {

            }
        });
    }

    /**
     * Creates label and text box for no. of instances,
     * and adds modify listener to text box.
     *
     * @param container
     */
    private void createNoOfInstancesComponent(Composite container) {
        Label lblNoOfInstances = new Label(container, SWT.LEFT);
        lblNoOfInstances.setText(Messages.dlgInstnces);
        GridData gridData = new GridData();
        gridData.heightHint = 20;
        gridData.horizontalIndent = 5;
        lblNoOfInstances.setLayoutData(gridData);

        txtNoOfInstances = new Text(container, SWT.SINGLE | SWT.BORDER);
        gridData = new GridData();
        gridData.widthHint = 275;
        gridData.horizontalAlignment = GridData.END;
        gridData.grabExcessHorizontalSpace = true;
        txtNoOfInstances.setLayoutData(gridData);
        txtNoOfInstances.setText("1");
        txtNoOfInstances.addFocusListener(new FocusListener() {

            @Override
            public void focusLost(FocusEvent arg0) {
                instancesModifyListener();
            }

            @Override
            public void focusGained(FocusEvent arg0) {
                setValid(true);
            }
        });
    }

    /**
     * Modify listener for number of instances textbox.
     */
    protected void instancesModifyListener() {
        setValid(true);
        String noOfInstances = txtNoOfInstances.getText();
        boolean isValidinstances = true;
        try {
            // validate noOfInstances field
            int instances = Integer.parseInt(noOfInstances);
            if (instances < 1) {
                isValidinstances = false;
            }
        } catch (NumberFormatException ex) {
            isValidinstances = false;
        }
        try {
            if (noOfInstances.isEmpty() || !isValidinstances) {
                setValid(false);
                errorTitle = Messages.dlgInvldInst1;
                errorMessage = Messages.dlgInvldInst2;
                MessageUtil.displayErrorDialog(getShell(), errorTitle,
                        errorMessage);
            } else {
                windowsAzureRole.setInstances(txtNoOfInstances.getText());
            }
        } catch (Exception ex) {
            errorTitle = Messages.adRolErrTitle;
            errorMessage = Messages.adRolErrMsgBox1
                    + Messages.adRolErrMsgBox2;
            MessageUtil.displayErrorDialog(getShell(), errorTitle,
                    errorMessage);
            Activator.getDefault().log(errorMessage, ex);
        }
    }

    /**
     * Listener for VM size combo box.
     */
    private void vmSizeSelectionListener() {
        try {
            windowsAzureRole.setVMSize(comboVMSize.getText());
        } catch (WindowsAzureInvalidProjectOperationException e) {
            errorTitle = Messages.adRolErrTitle;
            errorMessage = Messages.adRolErrMsgBox1
                    + Messages.adRolErrMsgBox2;
            MessageUtil.displayErrorDialog(getShell(), errorTitle,
                    errorMessage);
            Activator.getDefault().log(errorMessage, e);
        }
    }


    /**
     * Modify listener for role name textbox.
     *
     * @param event : ModifyEvent
     */
    private void roleNameModifyListener(ModifyEvent event) {
        setValid(true);
        String roleName = ((Text) event.widget).getText();
        try {
            if (roleName.equalsIgnoreCase(
                    windowsAzureRole.getName())) {
                isValidRoleName = true;
            } else {
                isValidRoleName = waProjManager
                        .isAvailableRoleName(roleName);
            }
            if (isValidRoleName) {
                windowsAzureRole.setName(txtRoleName.getText());
            } else {
                setValid(false);
                errorTitle = Messages.dlgInvldRoleName1;
                errorMessage = Messages.dlgInvldRoleName2;
                MessageUtil.displayErrorDialog(getShell(), errorTitle,
                        errorMessage);
            }
        } catch (WindowsAzureInvalidProjectOperationException e) {
            errorTitle = Messages.adRolErrTitle;
            errorMessage = Messages.adRolErrMsgBox1
                    + Messages.adRolErrMsgBox2;
            MessageUtil.displayErrorDialog(getShell(),
                    errorTitle, errorMessage);
            Activator.getDefault().log(errorMessage, e);
        }
    }

    /**
     * Populates the data in case of edit operation of role.
     */
    private void showContents() {
        String vmSize = "";
        txtRoleName.setText(windowsAzureRole.getName());
        vmSize = windowsAzureRole.getVMSize();
        int index = 3;
        for (int i = 0; i < arrVMSize.length; i++) {
            if (vmSize.equalsIgnoreCase(arrVMSize[i])) {
                index = i;
            }
        }
        comboVMSize.setText(arrVMSize[index]);
        try {
            txtNoOfInstances.setText(windowsAzureRole.getInstances());
        } catch (WindowsAzureInvalidProjectOperationException e) {
            errorTitle = Messages.adRolErrTitle;
            errorMessage = Messages.adRolErrMsgBox1
                    + Messages.adRolErrMsgBox2;
            MessageUtil.displayErrorDialog(this.getShell(), errorTitle,
                    errorMessage);
            Activator.getDefault().log(errorMessage, e);
        }
    }

    @Override
    public boolean performOk() {
        boolean okToProceed = true;
        try {
            if (!Activator.getDefault().isSaved()) {
                waProjManager.save();
                Activator.getDefault().setSaved(true);
            }
            WAEclipseHelper.refreshWorkspace(
            Messages.rolsRefTitle, Messages.rolsRefMsg);
        } catch (WindowsAzureInvalidProjectOperationException e) {
            errorTitle = Messages.adRolErrTitle;
            errorMessage = Messages.adRolErrMsgBox1
                    + Messages.adRolErrMsgBox2;
            MessageUtil.displayErrorDialog(this.getShell(), errorTitle,
                    errorMessage);
            Activator.getDefault().log(errorMessage, e);
            okToProceed = false;
        }
        if (okToProceed) {
            okToProceed = super.performOk();
        }
        return okToProceed;
    }

}
