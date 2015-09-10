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

import java.net.URL;
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
import com.microsoftopentechnologies.azurecommons.model.RoleAndEndpoint;
import com.microsoftopentechnologies.azurecommons.roleoperations.WARLoadBalanceUtilMethods;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
import com.persistent.util.WAEclipseHelper;
/**
 * Property page for Load Balancing that is sticky session.
 */
public class WARLoadBalance extends PropertyPage {

    private WindowsAzureProjectManager waProjManager;
    private WindowsAzureRole waRole;
    private Button btnSsnAffinity;
    private Label lblEndptToUse;
    private Combo comboEndpt;
    private Group grpSessionAff;
    private List<WindowsAzureEndpoint> endpointsList;
    private WindowsAzureEndpoint stcSelEndpoint;
    private boolean isPageDisplayed = false;

    @Override
    public String getTitle() {
    	if (!isPageDisplayed) {
    		return super.getTitle();
    	}
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
                    isEditableEndpointCombo(endPt);
                }
            }
        } catch (Exception e) {
        	PluginUtil.displayErrorDialogAndLog(
        			getShell(),
        			Messages.adRolErrTitle,
        			Messages.dlgDbgErr, e);
        }
        return super.getTitle();
    }
    
    private void isEditableEndpointCombo(WindowsAzureEndpoint endPt)
    		throws WindowsAzureInvalidProjectOperationException {
    	if (endPt.equals(waRole.getSslOffloadingInputEndpoint())) {
    		comboEndpt.setEnabled(false);
    	} else {
    		comboEndpt.setEnabled(true);
    	}
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
                    isEditableEndpointCombo(ssnAffEndpt);
                }

                boolean enabled = btnSsnAffinity.getSelection();
                lblEndptToUse.setEnabled(enabled);
                comboEndpt.setEnabled(enabled);
        } catch (WindowsAzureInvalidProjectOperationException ex) {
        	PluginUtil.displayErrorDialogAndLog(
        			this.getShell(),
        			Messages.adRolErrTitle,
        			Messages.adRolErrMsgBox1
        			+ Messages.adRolErrMsgBox2, ex);
        }
        isPageDisplayed = true;
        return container;
    }

    /**
     * Enable session affinity components.
     * @param value
     */
    private void enableSessionAffComp(boolean value) {
        btnSsnAffinity.setEnabled(value);
        lblEndptToUse.setEnabled(value);
        comboEndpt.setEnabled(value);
        //lblNote.setEnabled(value);
    }

    /**
     * Method creates UI controls for session affinity.
     * @param container
     */
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
                        	PluginUtil.displayErrorDialogAndLog(
                        			getShell(),
                        			Messages.adRolErrTitle,
                        			Messages.adRolErrMsgBox1
                        			+ Messages.adRolErrMsgBox2, ex);
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
                	PluginUtil.displayErrorDialogAndLog(
                			getShell(),
                			Messages.adRolErrTitle,
                			Messages.adRolErrMsgBox1
                			+ Messages.adRolErrMsgBox2, e);
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
            		/*
            		 * only logging the error in log file
            		 * not showing anything to end user.
            		 */
            		Activator.getDefault().log(
            				Messages.lnkOpenErrMsg, ex);
            	}
            }
        });
    }

    /**
     * Enable session affinity.
     */
    protected void enableSessionAff() {
        try {
        	WindowsAzureEndpoint endpt = null;
        	populateEndPointList();
        	endpt = WARLoadBalanceUtilMethods.findInputEndpt(waRole, endpointsList);

            if (endpt == null) {
                boolean choice = MessageDialog.openConfirm(getShell(),
                		Messages.lbCreateEndptTtl,
                		Messages.lbCreateEndptMsg);
                if (choice) {
                    WindowsAzureEndpoint newEndpt = createEndpt();
                    populateEndPointList();
                    comboEndpt.setText(String.format(Messages.dbgEndPtStr,
                            newEndpt.getName(),
                            newEndpt.getPort(),
                            newEndpt.getPrivatePort()));
                    waRole.setSessionAffinityInputEndpoint(newEndpt);
                    isEditableEndpointCombo(newEndpt);
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
                isEditableEndpointCombo(endpt);
            }
        } catch (WindowsAzureInvalidProjectOperationException e) {
        	PluginUtil.displayErrorDialogAndLog(
        			this.getShell(),
        			Messages.adRolErrTitle,
        			Messages.adRolErrMsgBox1
        			+ Messages.adRolErrMsgBox2, e);
        }
    }

    /**
     * Method creates endpoint associated with session affinity.
     * @return
     * @throws WindowsAzureInvalidProjectOperationException
     */
    private WindowsAzureEndpoint createEndpt()
    		throws WindowsAzureInvalidProjectOperationException {
    	RoleAndEndpoint obj = WARLoadBalanceUtilMethods.createEndpt(waRole, waProjManager);
    	WindowsAzureEndpoint endpt = obj.getEndPt();
    	waRole = obj.getRole();
    	return endpt;
    }

    /**
     * Populates endpoints having type input in combo box.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    private void populateEndPointList()
    		throws WindowsAzureInvalidProjectOperationException {
        endpointsList = waRole.getEndpoints();
        comboEndpt.removeAll();
        for (WindowsAzureEndpoint endpoint : endpointsList) {
            if (endpoint.getEndPointType().
                    equals(WindowsAzureEndpointType.Input)
                    && endpoint.getPrivatePort() != null
                    && !endpoint.equals(waRole.getDebuggingEndpoint())) {
                  comboEndpt.add(String.format(Messages.dbgEndPtStr,
                          endpoint.getName(),
                          endpoint.getPort(),
                          endpoint.getPrivatePort()));
            }
        }
    }

    @Override
    public boolean performOk() {
    	if (!isPageDisplayed) {
    		return super.performOk();
    	}
        boolean okToProceed = true;
        try {
            if (!Activator.getDefault().isSaved()) {
                waProjManager.save();
                Activator.getDefault().setSaved(true);
            }
            WAEclipseHelper.refreshWorkspace(
            		Messages.rolsRefTitle, Messages.rolsRefMsg);
        } catch (WindowsAzureInvalidProjectOperationException e) {
        	PluginUtil.displayErrorDialogAndLog(
        			this.getShell(),
        			Messages.adRolErrTitle,
        			Messages.adRolErrMsgBox1
        			+ Messages.adRolErrMsgBox2, e);
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
        WindowsAzureEndpoint selectedEndpoint = null;
        try {
            selectedEndpoint = WARLoadBalanceUtilMethods.
            		getStickySelectedEndpoint(waRole,
            				comboEndpt.getText());
        } catch (WindowsAzureInvalidProjectOperationException e) {
        	PluginUtil.displayErrorDialogAndLog(
        			getShell(),
        			Messages.adRolErrTitle,
        			Messages.dlgDbgErr, e);
        }
        return selectedEndpoint;
    }
}
