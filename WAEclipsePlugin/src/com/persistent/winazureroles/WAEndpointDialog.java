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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import waeclipseplugin.Activator;

import com.interopbridges.tools.windowsazure.WindowsAzureEndpoint;
import com.interopbridges.tools.windowsazure.WindowsAzureEndpointType;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.persistent.util.MessageUtil;

/**
 *
 * This class creates a dialog to edit/add an Endpoint.
 *
 */
public class WAEndpointDialog extends org.eclipse.jface.dialogs.Dialog {

    private Text txtName;
    private Combo comboType;
    private Text txtPublicPort;
    private Text txtPrivatePort;
    private Label lblPublicPort;
    private WindowsAzureRole windowsAzureRole;
    private WindowsAzureEndpoint waEndpt;
    private static String[] arrType = { WindowsAzureEndpointType.Input.toString(),
                                        WindowsAzureEndpointType.Internal.toString(),
                                        WindowsAzureEndpointType.InstanceInput.toString() };

    private static String errorTitle;
    private static String errorMessage;
    private boolean isSamePort;
    private boolean isEditEndpt = false;

    //Constructor to be called while adding an endpoint
    public WAEndpointDialog(Shell parent,
    		WindowsAzureRole windowsAzureRole) {
        super(parent);
        this.windowsAzureRole = windowsAzureRole;
    }

    //Constructor to be called while editing an endpoint
    public WAEndpointDialog(Shell parent, WindowsAzureRole windowsAzureRole,
            WindowsAzureEndpoint waEndpoint, boolean isEditEndpt) {
        super(parent);
        this.isEditEndpt = isEditEndpt;
        this.waEndpt = waEndpoint;
        this.windowsAzureRole = windowsAzureRole;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        String title = Messages.adRolEndPtTitle;
        if (isEditEndpt) {
            title = Messages.endptEditTitle;
        }
        newShell.setText(title);
    }

    protected Control createContents(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        container.setLayout(gridLayout);
        GridData gridData = new GridData();
        gridData.widthHint = 200;
        container.setLayoutData(gridData);

        Label lblName = new Label(container, SWT.LEFT);
        gridData = new GridData();
        gridData.horizontalIndent = 10;
        lblName.setLayoutData(gridData);
        lblName.setText(Messages.adRolName);

        txtName = new Text(container, SWT.SINGLE | SWT.BORDER);
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.END;
        gridData.grabExcessHorizontalSpace = true;
        gridData.widthHint = 75;
        txtName.setLayoutData(gridData);

        createEndptTypeComponent(container);

        createPublicPortComponent(container);

        Label lblPrivatePort = new Label(container, SWT.LEFT);
        gridData = new GridData();
        gridData.horizontalIndent = 10;
        lblPrivatePort.setLayoutData(gridData);
        lblPrivatePort.setText(Messages.adRolPrivatePort);

        txtPrivatePort = new Text(container, SWT.SINGLE | SWT.BORDER);
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.END;
        gridData.grabExcessHorizontalSpace = true;
        gridData.widthHint = 75;
        txtPrivatePort.setLayoutData(gridData);

        if (isEditEndpt) {
            txtName.setText(waEndpt.getName());
            txtPrivatePort.setText(waEndpt.getPrivatePort());
            txtPublicPort.setText(waEndpt.getPort());
            comboType.setText(waEndpt.getEndPointType().toString());
            if (comboType.getText().equalsIgnoreCase(
                    WindowsAzureEndpointType.Internal.toString())) {
                txtPublicPort.setEnabled(false);
                lblPublicPort.setEnabled(false);
                txtPublicPort.setText("");
            }
        }
        return super.createContents(parent);
    }

    /**
     * Creates an endpoint type component consisting of label and combo box.
     * Also adds a selection listener to combo box.
     *
     * @param container
     */
    private void createEndptTypeComponent(Composite container) {
        Label lblType = new Label(container, SWT.LEFT);
        GridData gridData = new GridData();
        gridData.horizontalIndent = 10;
        lblType.setLayoutData(gridData);
        lblType.setText(Messages.adRolType);

        comboType = new Combo(container, SWT.READ_ONLY);
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.END;
        gridData.grabExcessHorizontalSpace = true;
        gridData.widthHint = 60;
        comboType.setLayoutData(gridData);
        comboType.setItems(arrType);
        comboType.setText(arrType[0]);

        final Combo comboTemp = comboType;

        comboType.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (comboTemp.getText().equalsIgnoreCase(
                        WindowsAzureEndpointType.Internal.toString())) {
                    txtPublicPort.setText("");
                    txtPublicPort.setEnabled(false);
                    lblPublicPort.setEnabled(false);
                } else if (comboTemp.getText().equalsIgnoreCase(
                        WindowsAzureEndpointType.Input.toString())){
                    txtPublicPort.setEnabled(true);
                    lblPublicPort.setEnabled(true);
                        String port = txtPublicPort.getText();
                        if (port.contains("-")) {
                            txtPublicPort.setText(port.split("-")[0]);
                        } else {
                            try {
                                Integer.parseInt(port);
                                } catch (Exception e) {
                                    port = txtPrivatePort.getText();
                                }
                            txtPublicPort.setText(port);
                        }
                }  else {
                    txtPublicPort.setEnabled(true);
                    lblPublicPort.setEnabled(true);
                      if (!txtPublicPort.getText().isEmpty()
                    		  && txtPrivatePort.getText().isEmpty()) {
                          if (!txtPublicPort.getText().contains("-")) {
                              txtPublicPort.setText(txtPublicPort.
                            		  getText().concat("-").
                            		  concat(txtPublicPort.getText()));
                           }
                          txtPrivatePort.setText(txtPublicPort.
                        		  getText().split("-")[0]);
                      } else if (txtPublicPort.getText().isEmpty()) {
                    	  txtPublicPort.setText(txtPrivatePort.getText());
                      }
                }
            }
        });
    }

    /**
     * Creates a public port component consisting of label and text box.
     * Also adds a focus listener to the text box.
     *
     * @param container
     */
    private void createPublicPortComponent(Composite container) {
        lblPublicPort = new Label(container, SWT.LEFT);
        GridData gridData = new GridData();
        gridData.horizontalIndent = 10;
        lblPublicPort.setLayoutData(gridData);
        lblPublicPort.setText(Messages.adRolPubPort);

        txtPublicPort = new Text(container, SWT.SINGLE | SWT.BORDER);
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.END;
        gridData.grabExcessHorizontalSpace = true;
        gridData.widthHint = 75;
        txtPublicPort.setLayoutData(gridData);

        final Combo comboTemp = comboType;
        txtPublicPort.addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent event) {
                if (!comboTemp.getText().equalsIgnoreCase(
                        WindowsAzureEndpointType.Internal.toString())
                        && (txtPrivatePort.getText().isEmpty()
                        || isSamePort)) {
                    if (txtPublicPort.getText().contains("-")) {
                    txtPrivatePort.setText(txtPublicPort.
                    		getText().split("-")[0]);
                    } else {
                    txtPrivatePort.setText(txtPublicPort.getText());
                    }
                }
            }
            @Override
            public void focusGained(FocusEvent event) {
                String oldPort = txtPublicPort.getText();
                isSamePort = false;
                if (txtPrivatePort.getText().equals(oldPort)) {
                    isSamePort = true;
                }
            }
        });
    }

    @Override
    protected void okPressed() {
        boolean okToProceed = true;
        try {
            if (isEditEndpt) {
                //if its an edit an endpoint scenario
                okToProceed = editEndpt();
            } else {
                //for add an endpoint scenario
                boolean isValid = windowsAzureRole.isAvailableEndpointName(
                        txtName.getText());
                if (isValid) {
                    boolean isValidendpoint = windowsAzureRole.isValidEndpoint(
                            txtName.getText(),
                            WindowsAzureEndpointType.valueOf(comboType.getText()),
                            txtPrivatePort.getText(), txtPublicPort.getText());
                    if (isValidendpoint) {
                        windowsAzureRole.addEndpoint(txtName.getText(),
                        WindowsAzureEndpointType.valueOf(comboType.getText()),
                        txtPrivatePort.getText(), txtPublicPort.getText());
                    } else {
                        errorTitle = Messages.dlgInvldPort;
                        errorMessage = Messages.dlgPortInUse;
                        MessageUtil.displayErrorDialog(this.getShell(),
                                errorTitle, errorMessage);
                        okToProceed = false;
                    }
                } else {
                    errorTitle = Messages.dlgInvdEdPtName1;
                    errorMessage = Messages.dlgInvdEdPtName2;
                    MessageUtil.displayErrorDialog(this.getShell(),
                            errorTitle, errorMessage);
                    okToProceed = false;
                }
            }
            } catch (WindowsAzureInvalidProjectOperationException e) {
                errorTitle = Messages.rolsErr;
                errorMessage = Messages.adRolErrMsgBox1
                		+ Messages.adRolErrMsgBox2;
                MessageUtil.displayErrorDialog(this.getShell(),
                        errorTitle, errorMessage);
                Activator.getDefault().log(errorMessage, e);
            }
        if (okToProceed) {
            super.okPressed();
        }
    }

    /**
     * This method edits an endpoint.
     * For editing it also validates endpoint name and ports.
     *
     * @return retVal : false if any error occurs.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    private boolean editEndpt()
            throws WindowsAzureInvalidProjectOperationException {
        boolean retVal = true;
        String oldEndptName = waEndpt.getName();
        if (!oldEndptName.equalsIgnoreCase(txtName.getText())) {
            //validate endpoint name
            boolean isValid = windowsAzureRole.isAvailableEndpointName(
                    txtName.getText());
            if (!isValid) {
                //if name is not valid
                errorTitle = Messages.dlgInvdEdPtName1;
                errorMessage = Messages.dlgInvdEdPtName2;
                MessageUtil.displayErrorDialog(this.getShell(),
                        errorTitle, errorMessage);
                retVal = false;
            }
        }
        if (retVal) {
            retVal = validatePorts(oldEndptName);
        }
        return retVal;
    }

    /**
     * Validates public and private ports.
     * And also makes changes corresponding to the debug endpoint.
     *
     * @param oldEndptName : old name of the endpoint.
     * @return retVal : false if any error occurs.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    private boolean validatePorts(String oldEndptName)
            throws WindowsAzureInvalidProjectOperationException {
        boolean retVal = true;
        WindowsAzureEndpointType oldType = waEndpt.getEndPointType();
        WindowsAzureEndpoint debugEndpt = windowsAzureRole
                .getDebuggingEndpoint();
        WindowsAzureEndpoint stickyEndpt = windowsAzureRole
                .getSessionAffinityInputEndpoint();
        WindowsAzureEndpoint stickyIntEndpt = windowsAzureRole
                .getSessionAffinityInternalEndpoint();
        String stcEndptName = "";
        String dbgEndptName = "";
        String stcIntEndptName = "";
        if (debugEndpt != null) {
            //get the debugging endpoint name
            dbgEndptName = debugEndpt.getName();
        }
        if (stickyEndpt != null) {
            stcEndptName = stickyEndpt.getName();
            stcIntEndptName = stickyIntEndpt.getName();
        }
        //validate ports
        boolean isValidendpoint = windowsAzureRole.isValidEndpoint(
                oldEndptName,
                WindowsAzureEndpointType.valueOf(comboType.getText()),
                txtPrivatePort.getText(), txtPublicPort.getText());
        if (isValidendpoint) {
            if (oldEndptName.equalsIgnoreCase(dbgEndptName)) {
                retVal = handleChangeForDebugEndpt(oldType);
            }
            if (oldEndptName.equalsIgnoreCase(stcEndptName)) {
                retVal = handleChangeForStickyEndpt(oldType);
            }
            if (oldEndptName.equalsIgnoreCase(stcIntEndptName)) {
                retVal = handleChangeForStickyEndpt(oldType);
            }
            if (retVal) {
                //set the new values in the endpoint object.
                waEndpt.setEndPointType(WindowsAzureEndpointType
                        .valueOf(comboType.getText()));
                waEndpt.setName(txtName.getText());
                if (comboType.getText().equalsIgnoreCase(
                        WindowsAzureEndpointType.Input.toString())
                        || comboType.getText().equalsIgnoreCase(
                                WindowsAzureEndpointType.InstanceInput.toString())) {
                    waEndpt.setPort(txtPublicPort.getText());
                }
                waEndpt.setPrivatePort(txtPrivatePort.getText());
            }
        } else {
            errorTitle = Messages.dlgInvldPort;
            errorMessage = Messages.dlgPortInUse;
            MessageUtil.displayErrorDialog(this.getShell(),
                    errorTitle, errorMessage);
            retVal = false;
        }
        return retVal;
    }

    /**
     * Disables the debugging if debug endpoint's type is changed to 'Internal',
     * and if private port is modified then assigns the new debugging port
     * by setting the modified endpoint as a debugging endpoint.
     *
     * @param oldType : old type of the endpoint.
     * @return retVal : false if any error occurs.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    private boolean handleChangeForDebugEndpt(WindowsAzureEndpointType oldType)
            throws WindowsAzureInvalidProjectOperationException {
        boolean retVal = true;
        if (oldType.equals(WindowsAzureEndpointType.Input)
                && comboType.getText().equalsIgnoreCase(
                WindowsAzureEndpointType.Internal.toString())) {
            StringBuffer msg = new StringBuffer(Messages.dlgEPDel);
            msg.append(Messages.dlgEPChangeType);
            msg.append(Messages.dlgEPDel2);
            boolean choice = MessageDialog.openQuestion(new Shell(),
                    Messages.dlgTypeTitle, msg.toString());
            if (choice) {
                waEndpt.setEndPointType(WindowsAzureEndpointType
                        .valueOf(comboType.getText()));
                windowsAzureRole.setDebuggingEndpoint(null);
            } else {
                retVal = false;
            }
        } else if (!waEndpt.getPrivatePort().equalsIgnoreCase(
                txtPrivatePort.getText())) {
            boolean isSuspended = windowsAzureRole.getStartSuspended();
            windowsAzureRole.setDebuggingEndpoint(null);
            waEndpt.setPrivatePort(txtPrivatePort.getText());
            windowsAzureRole.setDebuggingEndpoint(waEndpt);
            windowsAzureRole.setStartSuspended(isSuspended);
        }
        return retVal;
    }

    /**
     * Disables the session affinity if endpoint's type is changed to 'Internal'.
     *
     * @param oldType : old type of the end point.
     * @return retVal : false if any error occurs.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    private boolean handleChangeForStickyEndpt(WindowsAzureEndpointType oldType)
            throws WindowsAzureInvalidProjectOperationException {
        boolean retVal = true;
        return retVal;
    }
}
