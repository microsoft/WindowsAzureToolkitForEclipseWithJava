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

import java.io.File;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import waeclipseplugin.Activator;

import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.persistent.util.MessageUtil;
import com.persistent.util.WAEclipseHelper;

/**
 * This class creates Roles table and buttons.
 */
public class WARoles {
    private static Table tblRoles;
    private static Button btnAddRole;
    private static Button btnEditRole;
    private static Button btnRemoveRole;
    private static TableViewer tableViewer;
    private static List<WindowsAzureRole> listRoles = null;
    private static WindowsAzureProjectManager waProjManager;
    private static boolean isWizard;
    private static String errorTitle;
    private static String errorMessage;

    /**
     * This method draws the controls.
     *
     * @param composite
     * @param isWizard
     */
    public static void displayRoles(Composite composite,
    		boolean isWizard) {

        final Composite parent = composite;
        WARoles.isWizard = isWizard;
        //Roles table
        tblRoles = new Table(composite, SWT.BORDER |
                SWT.FULL_SELECTION);
        tblRoles.setHeaderVisible(true);
        tblRoles.setLinesVisible(true);
        GridData gridData = new GridData();
        gridData.heightHint = 75;
        gridData.horizontalIndent = 3;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = false;

        // Changes for the extra column which was being displayed
        //on project property.
        GridLayout gridLayoutTable = new GridLayout();
        gridLayoutTable.numColumns = 3;
        gridLayoutTable.marginRight = 0;
        tblRoles.setLayout(gridLayoutTable);
        tblRoles.setLayoutData(gridData);

        TableColumn colName = new TableColumn(tblRoles, SWT.FILL);
        colName.setText(Messages.rolsName);
        colName.setWidth(180);

        TableColumn colVMSize = new TableColumn(tblRoles, SWT.FILL);
        colVMSize.setText(Messages.rolsVMSize);
        colVMSize.setWidth(90);

        TableColumn colInstances = new TableColumn(tblRoles, SWT.FILL);
        colInstances.setText(Messages.rolsInstances);
        colInstances.setWidth(80);
        loadProject();

        tableViewer = new TableViewer(tblRoles);
        tableViewer.setContentProvider(new IStructuredContentProvider() {

            @Override
            public void inputChanged(Viewer arg0, Object arg1, Object arg2) {

            }

            @Override
            public void dispose() {

            }

            @Override
            public Object[] getElements(Object arg0) {
                return getRoles().toArray();
            }
        });

        tableViewer.setLabelProvider(new ITableLabelProvider() {

            @Override
            public void removeListener(ILabelProviderListener arg0) {

            }

            @Override
            public boolean isLabelProperty(Object arg0, String arg1) {
                return false;
            }

            @Override
            public void dispose() {

            }

            @Override
            public void addListener(ILabelProviderListener arg0) {

            }

            @Override
            public String getColumnText(Object element, int colIndex) {
                WindowsAzureRole winAzureRole = (WindowsAzureRole) element;
                String result = "";
                try {
                switch(colIndex) {
                    case 0:
                        result = winAzureRole.getName();
                        break;
                    case 1:
                        result = winAzureRole.getVMSize();
                        break;
                    case 2:
                        result = winAzureRole.getInstances();
                        break;
                    default:
                    	break;
                    }
                } catch (WindowsAzureInvalidProjectOperationException e) {
                    //display error message if any exception occurs while
                    //reading role data
                    errorTitle = Messages.rolsErr;
                    errorMessage = Messages.adRolErrMsgBox1
                    + Messages.adRolErrMsgBox2;
                    MessageUtil.displayErrorDialog(parent.getShell(),
                            errorTitle, errorMessage);
                    Activator.getDefault().log(errorMessage, e);
                }
                return result;
            }

            @Override
            public Image getColumnImage(Object arg0, int arg1) {
                return null;
            }
        });

        tableViewer.setInput(getRoles().toArray());

        //Composite for buttons
        Composite containerRoleBtn = new Composite(composite, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        containerRoleBtn.setLayout(gridLayout);

        btnAddRole = new Button(containerRoleBtn, SWT.PUSH);
        btnAddRole.setText(Messages.rolsAddBtn);
        gridData = new GridData();
        gridData.widthHint = 80;
        btnAddRole.setLayoutData(gridData);

        //Add selection listener for Add Button
        btnAddRole.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                addButtonListener(parent);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {

            }
        });

        btnEditRole = new Button(containerRoleBtn, SWT.PUSH);
        btnEditRole.setText(Messages.rolsEditBtn);
        btnEditRole.setEnabled(false);
        gridData = new GridData();
        gridData.widthHint = 80;
        btnEditRole.setLayoutData(gridData);
        tblRoles.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                btnEditRole.setEnabled(true);
                btnRemoveRole.setEnabled(true);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {


            }
        });

        //Add selection listener for edit button
        btnEditRole.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                editButtonListener(parent);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {

            }
        });

        btnRemoveRole = new Button(containerRoleBtn, SWT.PUSH);
        btnRemoveRole.setText(Messages.rolsRemoveBtn);
        btnRemoveRole.setEnabled(false);
        gridData = new GridData();
        gridData.widthHint = 80;
        btnRemoveRole.setLayoutData(gridData);
        //Add selection listener for Remove Button
        btnRemoveRole.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                removeButtonListener(parent);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {

            }
        });
    }

    /**
     * Listener for remove Button.
     *
     * @param composite
     */
    protected static void removeButtonListener(Composite composite) {
        try {
            int selIndex = tableViewer.getTable().getSelectionIndex();
            if (selIndex > -1) {
                boolean choice =
                		MessageDialog.openQuestion(composite.getShell(),
                        Messages.rolsDelTitle, Messages.rolsDelMsg);
                if (choice) {
                	/*
                	 * If the role selected for deletion is the last role,
                	 * then do not delete it and give error message.
                	 */
                	if (listRoles.size() == 1) {
                		MessageUtil.displayErrorDialog(
                				composite.getShell(),
                                Messages.rolsDelTitle,
                                Messages.lastRolDelMsg);
                	} else {
                		WindowsAzureRole windowsAzureRole =
                				listRoles.get(selIndex);
                		windowsAzureRole.delete();
                		waProjManager.save();
                		tableViewer.refresh();
                		WAEclipseHelper.refreshWorkspace(
                				Messages.rolsRefTitle,
                				Messages.rolsRefMsg);
                	}
                }
            }
        } catch (WindowsAzureInvalidProjectOperationException e) {
            errorTitle = Messages.rolsErr;
            errorMessage = Messages.adRolErrMsgBox1
            + Messages.adRolErrMsgBox2;
            MessageUtil.displayErrorDialog(composite.getShell(),
                    errorTitle, errorMessage);
            Activator.getDefault().log(errorMessage, e);
        }
    }

    /**
     * Listener for Edit Button.
     *
     * @param composite
     */
    protected static void editButtonListener(final Composite composite) {
        int selIndex = tableViewer.getTable().getSelectionIndex();
        if (selIndex > -1) {
            try {
                loadProject();
                WindowsAzureRole windowsAzureRole = listRoles.get(
                        tableViewer.getTable().getSelectionIndex());

                Activator.getDefault().setWaProjMgr(waProjManager);
                Activator.getDefault().setWaRole(windowsAzureRole);
                Activator.getDefault().setEdit(true);

                int btnID = WAEclipseHelper.
                		openRolePropertyDialog(windowsAzureRole,
                		Messages.cmhIdGeneral);
                if (btnID == Window.OK) {
                    tableViewer.refresh();
                }
                WAEclipseHelper.refreshWorkspace(
                		Messages.rolsRefTitle, Messages.rolsRefMsg);
            } catch (Exception ex) {
                errorTitle = Messages.rolsDlgErr;
                errorMessage = Messages.rolsDlgErrMsg;
                MessageUtil.displayErrorDialog(new Shell(),
                        errorTitle, errorMessage);
                Activator.getDefault().log(errorMessage, ex);
            }
        }
    }

    /**
     * This method loads the project data.
     *
     * @param composite
     */
    private static void loadProject() {
    	IProject selProject = WAEclipseHelper.getSelectedProject();
    	String path = "";
    	if (isWizard) {
    		path = System.getProperty(Messages.rolsTmpDir);
    		StringBuffer strBfr = new StringBuffer(path);
    		path = strBfr.append(File.separator).append(
    				Messages.rolsTmpProj).toString();
    	} else {
    		path = selProject.getLocation().toPortableString();
    	}
    	File projDirPath = new File(path);
        try {
            waProjManager = WindowsAzureProjectManager.
            load(projDirPath);
            listRoles = waProjManager.getRoles();
        } catch (WindowsAzureInvalidProjectOperationException e) {
            errorTitle = Messages.rolsErr;
            errorMessage = Messages.adRolErrMsgBox1
            + Messages.adRolErrMsgBox2;
            MessageUtil.displayErrorDialog(new Shell(), errorTitle ,
                    errorMessage);
            Activator.getDefault().log(errorMessage, e);
        }
    }

    /**
     * Listener for Add Button.
     *
     * @param composite
     */
    private static void addButtonListener(final Composite composite) {
        try {
            loadProject();
            StringBuffer strBfr = new StringBuffer(
                    Messages.dlgWorkerRole1);
            int roleNo = 2;
            while (!waProjManager
                    .isAvailableRoleName(strBfr.toString())) {
                strBfr.delete(10, strBfr.length());
                strBfr.append(roleNo++);
            }
            String strKitLoc = String.format("%s%s%s%s%s%s",
					Platform.getInstallLocation().
					getURL().getPath().toString(),
					File.separator, Messages.pluginFolder,
					File.separator, Messages.pluginId,
					Messages.pWizStarterKit);
            WindowsAzureRole windowsAzureRole = waProjManager
                    .addRole(strBfr.toString(), strKitLoc);
            windowsAzureRole.setInstances(Messages.rolsNoOfInst);
            windowsAzureRole.setVMSize(Messages.rolsVMSmall);
            Activator.getDefault().setWaProjMgr(waProjManager);
            Activator.getDefault().setWaRole(windowsAzureRole);
            Activator.getDefault().setEdit(false);
            /*
             * Check whether user has pressed OK or Cancel button.
             * If OK : Refresh roles table so that newly added role is visible
             * else CANCEL : remove added role from list of roles.
             */
            int btnID = WAEclipseHelper.
            		openRolePropertyDialog(windowsAzureRole,
            				Messages.cmhIdGeneral);
            if (btnID == Window.OK) {
            	tableViewer.refresh();
            } else {
            	getRoles().remove(windowsAzureRole);
            }
            WAEclipseHelper.refreshWorkspace(
            		Messages.rolsRefTitle, Messages.rolsRefMsg);
        } catch (Exception ex) {
            errorTitle = Messages.rolsDlgErr;
            errorMessage = Messages.rolsDlgErrMsg;
            MessageUtil.displayErrorDialog(new Shell(),
                    errorTitle, errorMessage);
            Activator.getDefault().log(errorMessage, ex);
        }
    }

    /**
     * Method returns list of windows azure roles.
     * @return List<WindowsAzureRole>
     */
    public static List<WindowsAzureRole> getRoles() {
        return listRoles;
    }

    /**
     * Method saves PML object.
     */
    public static void performSave() {
            try {
                waProjManager.save();
            } catch (WindowsAzureInvalidProjectOperationException e) {
                errorTitle = Messages.rolsErr;
                errorMessage = Messages.adRolErrMsgBox1
                + Messages.adRolErrMsgBox2;
                MessageUtil.displayErrorDialog(new Shell(), errorTitle,
                        errorMessage);
                Activator.getDefault().log(errorMessage, e);
            }
    }
}
