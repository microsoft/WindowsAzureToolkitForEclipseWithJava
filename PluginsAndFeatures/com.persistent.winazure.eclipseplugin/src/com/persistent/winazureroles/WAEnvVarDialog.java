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

import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;

/**
 * Dialog which adds/edits an environment variable.
 *
 */
public class WAEnvVarDialog extends Dialog {

    private Text txtName;
    private Text txtValue;
    private Map<String, String> mapEnvVar;
    private WindowsAzureRole waRole;
    private boolean isEditVariable = false;
    private String varName;
    private Button okButton;

    /**
     * Constructor to be called for adding an environment variable.
     *
     * @param parentShell
     * @param mapEnvVar : map containing all environment variables.
     * @param windowsAzureRole
     */
    protected WAEnvVarDialog(Shell parentShell,
    		Map<String, String> mapEnvVar,
            WindowsAzureRole windowsAzureRole) {
        super(parentShell);
        this.mapEnvVar = mapEnvVar;
        this.waRole = windowsAzureRole;
    }

    /**
     * Constructor to be called for editing an environment variable.
     *
     * @param parentShell
     * @param mapEnvVar : map containing all environment variables.
     * @param windowsAzureRole
     * @param key
     */
    public WAEnvVarDialog(Shell parentShell,
    		Map<String, String> mapEnvVar,
            WindowsAzureRole windowsAzureRole,
            String key) {
        super(parentShell);
        this.mapEnvVar = mapEnvVar;
        this.waRole = windowsAzureRole;
        this.isEditVariable = true;
        this.varName = key;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        String title = Messages.evNewTitle;
        if (isEditVariable) {
            title = Messages.evEditTitle;
        }
        newShell.setText(title);
    }

    @Override
    protected Control createButtonBar(Composite parent) {
        GridData gridData = new GridData();
        gridData.verticalAlignment = SWT.FILL;
        gridData.horizontalAlignment = SWT.FILL;
        parent.setLayoutData(gridData);
        Control ctrl = super.createButtonBar(parent);
        okButton = getButton(IDialogConstants.OK_ID);
        if (!isEditVariable) {
            okButton.setEnabled(false);
        }
        return ctrl;
    }

    protected Control createContents(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        container.setLayout(gridLayout);
        GridData gridData = new GridData();
        gridData.widthHint = 400;
        container.setLayoutData(gridData);

        createNameComponent(container);
        createValueComponent(container);

        if (isEditVariable) {
            // populate the environment variable name and it's value
            // in case of this dialog is opened for editing the variable.
            populateData();
        }

        return super.createContents(parent);
    }

    /**
     * Populates the variable name and value text fields with the corresponding
     * attributes of environment variable selected for editing.
     *
     */
    private void populateData() {
        try {
            txtName.setText(varName);
            txtValue.setText(mapEnvVar.get(varName));
        } catch (Exception ex) {
        	PluginUtil.displayErrorDialogAndLog(
        			this.getShell(),
        			Messages.rolsErr,
        			Messages.adRolErrMsgBox1
        			+ Messages.adRolErrMsgBox2, ex);
        }
    }

    /**
     * Creates label and text box for variable's name.
     *
     * @param container
     */
    private void createNameComponent(Composite container) {
        Label lblName = new Label(container, SWT.LEFT);
        GridData gridData = new GridData();
        gridData.horizontalIndent = 10;
        gridData.verticalIndent =10;
        lblName.setLayoutData(gridData);
        lblName.setText(Messages.evVarName);

        txtName = new Text(container, SWT.SINGLE | SWT.BORDER);
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.END;
        gridData.verticalIndent = 10;
        gridData.grabExcessHorizontalSpace = true;
        gridData.widthHint = 250;
        txtName.setLayoutData(gridData);
        txtName.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent event) {
                if (okButton != null) {
                    if (txtName.getText().trim().isEmpty()) {
                        okButton.setEnabled(false);
                    } else {
                        okButton.setEnabled(true);
                    }
                }
            }
        });
    }

    /**
     * Creates label and text box for variable's value.
     *
     * @param container
     */
    private void createValueComponent(Composite container) {
        Label lblValue = new Label(container, SWT.LEFT);
        GridData gridData = new GridData();
        gridData.horizontalIndent = 10;
        gridData.verticalIndent = 5;
        lblValue.setLayoutData(gridData);
        lblValue.setText(Messages.evVarValue);

        txtValue = new Text(container, SWT.SINGLE | SWT.BORDER);
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.END;
        gridData.verticalIndent = 5;
        gridData.grabExcessHorizontalSpace = true;
        gridData.widthHint = 250;
        txtValue.setLayoutData(gridData);
    }

    @Override
    protected void okPressed() {
        boolean retVal = true;
        try {
            retVal = validateName();
            if (retVal) {
                String value = txtValue.getText().trim();
                String name = txtName.getText().trim();
                name = name.replaceAll("[\\s]+", "_");
                if (isEditVariable
                        && !varName.equals(txtName.getText())) {
                    // Here the comparison is case sensitive to handle
                    // the scenario where user edits the name such that
                    // only case (upper/lower) of the letters changes.

                    // renames the variable name
                    waRole.renameRuntimeEnv(varName, name);
                }
                waRole.setRuntimeEnv(name, value);
            }
        } catch (Exception ex) {
            PluginUtil.displayErrorDialogAndLog(
            		this.getShell(),
            		Messages.rolsErr,
            		Messages.adRolErrMsgBox1
                    + Messages.adRolErrMsgBox2, ex);
            retVal = false;
        }
        if (retVal) {
            super.okPressed();
        }
    }

    /**
     * Validates the variable name so that it should not be empty
     * or an existing one.
     *
     * @return true if the variable name is valid, else false
     */
    private boolean validateName() {
        boolean retVal = true;
        String name = txtName.getText();
        boolean isValidName = true;
        for (Iterator<String> iterator = mapEnvVar.keySet().iterator();
                iterator.hasNext();) {
            String key = iterator.next();
            if (key.trim().equalsIgnoreCase(name.trim())) {
                isValidName = false;
                break;
            }
        }
        try {
        if (!isValidName
            && !(isEditVariable
                    && varName.equalsIgnoreCase(txtName.getText()))
                    || waRole.getLsEnv().contains(name)) {
            // Here the comparison is case-insensitive to avoid this message,
            // when user only changes case (upper/lower) of the name.
            PluginUtil.displayErrorDialog(
            		getShell(),
            		Messages.evInUseTitle,
            		Messages.evInUseMsg);
            retVal = false;
        }
        } catch (WindowsAzureInvalidProjectOperationException e) {
        	PluginUtil.displayErrorDialogAndLog(
        			this.getShell(),
        			Messages.envErrTtl,
        			Messages.envValMsg, e);
        	retVal = false;
        }
        return retVal;
    }
}
