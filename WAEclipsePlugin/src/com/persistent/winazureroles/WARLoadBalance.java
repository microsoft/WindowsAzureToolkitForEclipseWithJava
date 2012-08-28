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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

import waeclipseplugin.Activator;

import com.interopbridges.tools.windowsazure.WindowsAzureEndpoint;
import com.interopbridges.tools.windowsazure.WindowsAzureEndpointType;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.persistent.util.WAEclipseHelper;
import com.persistent.util.MessageUtil;

public class WARLoadBalance extends PropertyPage {

    private WindowsAzureProjectManager waProjManager;
    private WindowsAzureRole waRole;
    private Button btnSsnAffinity;
    private Label lblEndptToUse;
    private Combo comboEndpt;
    private Group grpSessionAff;
    private List<WindowsAzureEndpoint> endpointsList;
    private String errorTitle;
    private String errorMessage;
    private WindowsAzureEndpoint stcSelEndpoint;

    @Override
    public String getTitle() {
        try {
            if (waRole != null) {
                WindowsAzureEndpoint endPt =
                waRole.getSessionAffinityInputEndpoint();
                if (endPt == null) {
                    btnSsnAffinity.setSelection(false);
                    lblEndptToUse.setEnabled(false);
                    comboEndpt.setEnabled(false);
                    comboEndpt.removeAll();

                } else {
                    populateEndPointList();
                    comboEndpt.setText(String.format(Messages.dbgEndPtStr,
                            endPt.getName(),
                            endPt.getPort(),
                            endPt.getPrivatePort()));
                }
            }
        } catch (Exception e) {
            errorTitle = Messages.adRolErrTitle;
            errorMessage = Messages.dlgDbgErr;
            MessageUtil.displayErrorDialog(getShell(),
               errorTitle, errorMessage);
            Activator.getDefault().log(errorMessage, e);
        }
        return super.getTitle();
    }

    @Override
    protected Control createContents(Composite parent) {
        noDefaultAndApplyButton();
        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
                "com.persistent.winazure.eclipseplugin."
                + "windows_azure_loadbalance_page");
        waProjManager = Activator.getDefault().getWaProjMgr();
        waRole = Activator.getDefault().getWaRole();
        Activator.getDefault().setSaved(false);

        Composite container = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        container.setLayout(gridLayout);
        container.setLayoutData(gridData);

        createSessionAffinityComponent(container);
        try {
               enableSessionAffComp(true);
               WindowsAzureEndpoint ssnAffEndpt =
                        waRole.getSessionAffinityInputEndpoint();
                if (ssnAffEndpt != null) {
                    btnSsnAffinity.setSelection(true);
                    populateEndPointList();
                    comboEndpt.setText(String.format(Messages.dbgEndPtStr,
                            ssnAffEndpt.getName(),
                            ssnAffEndpt.getPort(),
                            ssnAffEndpt.getPrivatePort()));
                }

                boolean enabled = btnSsnAffinity.getSelection();
                lblEndptToUse.setEnabled(enabled);
                comboEndpt.setEnabled(enabled);
        } catch (WindowsAzureInvalidProjectOperationException ex) {
            errorTitle = Messages.adRolErrTitle;
            errorMessage = Messages.adRolErrMsgBox1
                    + Messages.adRolErrMsgBox2;
            MessageUtil.displayErrorDialog(this.getShell(), errorTitle,
                    errorMessage);
            Activator.getDefault().log(errorMessage, ex);
        }
        return container;
    }

    private void enableSessionAffComp(boolean value) {
        btnSsnAffinity.setEnabled(value);
        lblEndptToUse.setEnabled(value);
        comboEndpt.setEnabled(value);
        //lblNote.setEnabled(value);
    }

    private void createSessionAffinityComponent(Composite container) {
        grpSessionAff = new Group(container, SWT.None);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        grpSessionAff.setLayout(gridLayout);
        grpSessionAff.setLayoutData(gridData);
        grpSessionAff.setText(Messages.lbSsnAff);

        btnSsnAffinity = new Button(grpSessionAff, SWT.CHECK);
        btnSsnAffinity.setText(Messages.lbEnableSsnAff);
        gridData = new GridData();
        gridData.horizontalSpan = 2;
        btnSsnAffinity.setLayoutData(gridData);
        btnSsnAffinity.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                if (event.getSource() instanceof Button) {
                    Button checkBox = (Button) event.getSource();

                    if (checkBox.getSelection()) {
                        lblEndptToUse.setEnabled(checkBox.getSelection());
                        comboEndpt.setEnabled(checkBox.getSelection());
                        enableSessionAff();
                    }
                    else {
                        try {
                            comboEndpt.removeAll();
                            lblEndptToUse.setEnabled(checkBox.getSelection());
                            comboEndpt.setEnabled(checkBox.getSelection());
                            waRole.setSessionAffinityInputEndpoint(null);
                        } catch (WindowsAzureInvalidProjectOperationException ex) {
                            errorTitle = Messages.adRolErrTitle;
                            errorMessage = Messages.adRolErrMsgBox1
                                    + Messages.adRolErrMsgBox2;
                            MessageUtil.displayErrorDialog(getShell(),
                            errorTitle, errorMessage);
                            Activator.getDefault().log(errorMessage, ex);
                        }
                    }
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {

            }
        });

        lblEndptToUse = new Label(grpSessionAff, SWT.LEFT);
        gridData = new GridData();
        gridData.horizontalIndent = 17;
        lblEndptToUse.setLayoutData(gridData);
        lblEndptToUse.setText(Messages.lbEndptToUse);

        comboEndpt = new Combo(grpSessionAff, SWT.READ_ONLY);
        gridData = new GridData();
        gridData.widthHint = 260;
        gridData.heightHint = 12;
        gridData.horizontalAlignment = GridData.END;
        gridData.grabExcessHorizontalSpace = true;
        comboEndpt.setLayoutData(gridData);
        comboEndpt.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                setValid(true);
                try {
                    if (btnSsnAffinity.getSelection()) {
                        stcSelEndpoint = getStickySelectedEndpoint();
                        if (!stcSelEndpoint.equals(waRole.
                        getSessionAffinityInputEndpoint())) {
                        waRole.setSessionAffinityInputEndpoint(null);
                        waRole.setSessionAffinityInputEndpoint(stcSelEndpoint);
                        }
                    }
                } catch (WindowsAzureInvalidProjectOperationException e) {
                    errorTitle = Messages.adRolErrTitle;
                    errorMessage = Messages.adRolErrMsgBox1
                            + Messages.adRolErrMsgBox2;
                    MessageUtil.displayErrorDialog(getShell(), errorTitle,
                            errorMessage);
                    Activator.getDefault().log(errorMessage, e);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
            }
        });
        Link linkNote = new Link(grpSessionAff, SWT.LEFT);
        linkNote.setText(Messages.lbNote);
        gridData = new GridData();
        gridData.horizontalSpan = 2;
        gridData.verticalIndent = 5;
        linkNote.setLayoutData(gridData);
        linkNote.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
            	try {
            		PlatformUI.getWorkbench().getBrowserSupport().
            		getExternalBrowser().openURL(new URL(event.text));
            	}
            	catch (Exception ex) {
            		//only logging the error in log file not showing anything to end user
            		Activator.getDefault().log(errorMessage, ex);
            	}
            }
        });
    }

    protected void enableSessionAff() {
        try {

             WindowsAzureEndpoint endpt = null;
            populateEndPointList();
            endpt = findInputEndpt();

            if (endpt == null) {
                errorTitle = Messages.lbCreateEndptTtl;
                errorMessage = Messages.lbCreateEndptMsg;
                boolean choice = MessageDialog.openConfirm(getShell(),
                        errorTitle, errorMessage);
                if (choice) {
                    WindowsAzureEndpoint newEndpt = createEndpt();
                    populateEndPointList();
                    comboEndpt.setText(String.format(Messages.dbgEndPtStr,
                            newEndpt.getName(),
                            newEndpt.getPort(),
                            newEndpt.getPrivatePort()));
                    waRole.setSessionAffinityInputEndpoint(newEndpt);
                } else {
                    btnSsnAffinity.setSelection(false);
                    lblEndptToUse.setEnabled(false);
                    comboEndpt.setEnabled(false);
                }
            } else {
                comboEndpt.setText(String.format(Messages.dbgEndPtStr,
                        endpt.getName(),
                        endpt.getPort(),
                        endpt.getPrivatePort()));
                waRole.setSessionAffinityInputEndpoint(endpt);
            }
        } catch (WindowsAzureInvalidProjectOperationException e) {
            errorTitle = Messages.adRolErrTitle;
            errorMessage = Messages.adRolErrMsgBox1
                    + Messages.adRolErrMsgBox2;
            MessageUtil.displayErrorDialog(this.getShell(), errorTitle,
                    errorMessage);
            Activator.getDefault().log(errorMessage, e);
        }
    }

    private WindowsAzureEndpoint createEndpt()
    		throws WindowsAzureInvalidProjectOperationException {
        WindowsAzureEndpoint endpt = null;
        StringBuffer endptName = new StringBuffer(Messages.lbHttpEndpt);
        int index = 1;
        int localPort = 8080;
        while (!waRole.isAvailableEndpointName(endptName.toString())) {
            endptName.insert(4, index++);
        }

        while (!waProjManager.isValidPort(String.valueOf(localPort),
                WindowsAzureEndpointType.Input)) {
            localPort++;
        }
        endpt = waRole.addEndpoint(endptName.toString(),
                WindowsAzureEndpointType.Input,
                String.valueOf(localPort),
                "80");
        return endpt;
    }

    private void populateEndPointList()
    		throws WindowsAzureInvalidProjectOperationException {
        endpointsList = waRole.getEndpoints();
        comboEndpt.removeAll();
        for (WindowsAzureEndpoint endpoint : endpointsList) {
            if (endpoint.getEndPointType().
                    equals(WindowsAzureEndpointType.Input)
                    && !endpoint.equals(waRole.getDebuggingEndpoint())) {
                  comboEndpt.add(String.format(Messages.dbgEndPtStr,
                          endpoint.getName(),
                          endpoint.getPort(),
                          endpoint.getPrivatePort()));
            }
        }
    }

    private WindowsAzureEndpoint findInputEndpt()
    		throws WindowsAzureInvalidProjectOperationException {
        WindowsAzureEndpoint endpt = null;
        boolean isFirst = true;
        for (WindowsAzureEndpoint endpoint : endpointsList) {
            if (endpoint.getEndPointType().
                    equals(WindowsAzureEndpointType.Input)
                    && !endpoint.equals(waRole.getDebuggingEndpoint())) {
                if (isFirst) {
                    endpt = endpoint;
                    isFirst = false;
                }
                if (endpoint.getPort().equalsIgnoreCase("80")) {
                    endpt = endpoint;
                    break;
                }
            }
        }
        return endpt;
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

    /**
     * This method returns the selected endpoint name
     * which was selected for sticky session.
     * @return selectedEndpoint
     */
    private WindowsAzureEndpoint getStickySelectedEndpoint() {
        List<WindowsAzureEndpoint> endpointsList;
        WindowsAzureEndpoint selectedEndpoint = null;
        try {
            endpointsList = new ArrayList<WindowsAzureEndpoint>(
                    waRole.getEndpoints());
            for (WindowsAzureEndpoint endpoint : endpointsList) {
                if (comboEndpt.getText().equals(
                        String.format(Messages.dbgEndPtStr,
                                endpoint.getName(),
                                endpoint.getPort(),
                                endpoint.getPrivatePort()))) {
                    selectedEndpoint = endpoint;
                }
            }
        } catch (WindowsAzureInvalidProjectOperationException e) {
             errorTitle = Messages.adRolErrTitle;
             errorMessage = Messages.dlgDbgErr;
             MessageUtil.displayErrorDialog(getShell(),
                errorTitle, errorMessage);
             Activator.getDefault().log(errorMessage, e);
        }
        return selectedEndpoint;
    }
}
