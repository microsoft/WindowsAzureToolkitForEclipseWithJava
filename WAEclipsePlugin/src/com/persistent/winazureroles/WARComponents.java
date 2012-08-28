/**
* Copyright 2012 Persistent Systems Ltd.
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

import waeclipseplugin.Activator;

import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.interopbridges.tools.windowsazure.WindowsAzureRoleComponent;
import com.interopbridges.tools.windowsazure.WindowsAzureRoleComponentImportMethod;
import com.persistent.util.WAEclipseHelper;
import com.persistent.util.MessageUtil;
import com.persistent.util.ProjectNatureHelper;
import com.persistent.util.ProjectNatureHelper.ProjExportType;

/**
 * Property page for Components table.
 *
 */

public class WARComponents extends PropertyPage {

    private WindowsAzureProjectManager waProjManager;
    private WindowsAzureRole windowsAzureRole;
    private Table tblComponents;
    private TableViewer tblViewer;
    private static List<WindowsAzureRoleComponent> listComponents;
    private String errorTitle;
    private String errorMessage;
    private Button btnEdit;
    private Button btnRemove;
    private ArrayList<File> fileToDel = new ArrayList<File>();

    @Override
    public String getTitle() {
    	if (tblViewer != null) {
    		tblViewer.refresh();
    	}
    	return super.getTitle();
    }

    /**
     * Create components table and buttons associated with it.
     *
     * @param parent : parent composite.
     * @return control
     */
    @Override
    protected Control createContents(Composite parent) {
        noDefaultAndApplyButton();
        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
                "com.persistent.winazure.eclipseplugin."
                + "windows_azure_components_page");
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

        tblComponents = new Table(container, SWT.MULTI | SWT.BORDER
                | SWT.FULL_SELECTION);
        tblComponents.setHeaderVisible(true);
        tblComponents.setLinesVisible(true);
        gridData = new GridData();
        gridData.heightHint = 380;
        gridData.horizontalIndent = 3;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        tblComponents.setLayoutData(gridData);

        TableColumn colImprt = new TableColumn(tblComponents, SWT.FILL);
        colImprt.setText(Messages.cmpntsImprt);
        colImprt.setWidth(60);

        TableColumn colFrm = new TableColumn(tblComponents, SWT.FILL);
        colFrm.setText(Messages.cmpntsFrm);
        colFrm.setWidth(100);

        TableColumn colAs =
                new TableColumn(tblComponents, SWT.FILL);
        colAs.setText(Messages.cmpntsAs);
        colAs.setWidth(100);

        TableColumn colDply =
                new TableColumn(tblComponents, SWT.FILL);
        colDply.setText(Messages.cmpntsDply);
        colDply.setWidth(60);

        TableColumn colTo =
                new TableColumn(tblComponents, SWT.FILL);
        colTo.setText(Messages.cmpntsTo);
        colTo.setWidth(150);

        tblComponents.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                btnEdit.setEnabled(true);
                btnRemove.setEnabled(true);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {

            }
        });

        Composite containerButtons =
                new Composite(container, SWT.NONE);
        gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        gridData = new GridData();
        gridData.horizontalAlignment = SWT.END;
        gridData.verticalAlignment = GridData.BEGINNING;
        containerButtons.setLayout(gridLayout);
        containerButtons.setLayoutData(gridData);

        createAddButton(containerButtons);
        createEditButton(containerButtons);
        createRemoveButton(containerButtons);
        createTableViewer();
        Label note = new Label(parent, SWT.LEFT);
        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.horizontalIndent = 8;
        note.setText(Messages.cmpntNote);
        note.setLayoutData(gridData);

        return container;
    }

    /**
     * Creates 'Add' button and adds selection listener to it.
     *
     * @param containerButtons
     */
    private void createAddButton(Composite containerButtons) {
        Button btnAdd = new Button(containerButtons, SWT.PUSH);
        btnAdd.setText(Messages.rolsAddBtn);
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        btnAdd.setLayoutData(gridData);
        btnAdd.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                addBtnListener();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {

            }
        });
    }

    /**
     * Creates 'Edit' button and adds selection listener to it.
     *
     * @param containerButtons
     */
    private void createEditButton(Composite containerButtons) {
        btnEdit = new Button(containerButtons, SWT.PUSH);
        btnEdit.setText(Messages.rolsEditBtn);
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        btnEdit.setLayoutData(gridData);
        btnEdit.setEnabled(false);
        btnEdit.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                editBtnListener();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {

            }
        });
    }

    /**
     * Creates 'Remove' button and adds selection listener to it.
     *
     * @param containerButtons
     */
    private void createRemoveButton(Composite containerButtons) {
        btnRemove = new Button(containerButtons, SWT.PUSH);
        btnRemove.setText(Messages.dlgBtnRemove);
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        btnRemove.setLayoutData(gridData);
        btnRemove.setEnabled(false);
        btnRemove.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                removeBtnListener();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {

            }
        });
    }

    /**
     * Content provider class for components table,
     * which determines the input for the table.
     *
     */
    private class CmptTableContentProvider implements IStructuredContentProvider {

        @Override
        public void inputChanged(Viewer viewer,
                Object oldInput, Object newInput) {

        }

        @Override
        public void dispose() {

        }

        @Override
        public Object[] getElements(Object arg0) {
            if (listComponents == null) {
                listComponents = new ArrayList<WindowsAzureRoleComponent>();
            }
            List<WindowsAzureRoleComponent> arrList =
                    new ArrayList<WindowsAzureRoleComponent>(listComponents);
            return arrList.toArray();

        }
    }

    /**
     * Label provider class for components table,
     * to provide column names.
     *
     */
    private class CmptTableLabelProvider implements ITableLabelProvider {

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
            WindowsAzureRoleComponent component =
                    (WindowsAzureRoleComponent) element;
            String result = "";
            switch (colIndex) {
            case 0:
                if (component.getImportMethod() != null) {
                    if (component.getImportMethod().
                            equals(WindowsAzureRoleComponentImportMethod.auto)) {
                        String path = component.getImportPath();
                        IWorkspace workspace = ResourcesPlugin.getWorkspace();
                        IWorkspaceRoot root = workspace.getRoot();
                        if (path.contains("\\")) {
                            path = path.substring(path.lastIndexOf('\\'),
                            		path.length());
                            IProject proj = root.getProject(path);
                            ProjExportType type = ProjectNatureHelper.
                            		getProjectNature(proj);
                            result = type.toString();
                        }
                    } else {
                            result = component.getImportMethod().toString();
                        }
                } else {
                    result = WindowsAzureRoleComponentImportMethod.none.name();
                }
                break;
            case 1:
                if (component.getImportPath().isEmpty()
                		|| component.getImportPath() == null) {
                    result = ".\\";
                } else {
                result = component.getImportPath();
                }
                break;
            case 2:
                result = component.getDeployName();
                break;
            case 3:
                if (component.getDeployMethod() != null) {
                    result = component.getDeployMethod().toString();
                } else {
                    result = WindowsAzureRoleComponentImportMethod.none.name();
                }
                break;
            case 4:
                if (component.getDeployDir().isEmpty()
                		|| component.getDeployDir() == null) {
                    result = ".\\";
                } else {
                result = component.getDeployDir();
                }
                break;
             default:
                 break;
            }
            return result;
        }

        @Override
        public Image getColumnImage(Object arg0, int arg1) {
            return null;
        }
    }

    private class CellModifier implements ICellModifier {

        @Override
        public boolean canModify(Object element, String property) {
            boolean retVal = true;
            if ((property.equals(Messages.cmpntsImprt))
                    || (property.equals(Messages.cmpntsFrm))
                    || (property.equals(Messages.cmpntsAs))
                    || (property.equals(Messages.cmpntsDply))
                    || (property.equals(Messages.cmpntsTo))) {
                retVal = false;
            }
            return retVal;
        }

        @Override
        public Object getValue(Object element, String property) {
            Object result = null;
            WindowsAzureRoleComponent component =
            		(WindowsAzureRoleComponent) element;

            if (property.equals(Messages.cmpntsImprt)) {
                if (component.getImportMethod() != null) {
                    result = component.getImportMethod().toString();
                }
            } else if (property.equals(Messages.cmpntsFrm)) {
                result = component.getImportPath();
            } else if (property.equals(
                    Messages.cmpntsAs)) {
                result = component.getDeployName();
            } else if (property.equals(
                    Messages.cmpntsDply)) {
                if (component.getDeployMethod() != null) {
                    result = component.getDeployMethod().toString();
                }
            }  else if (property.equals(
                    Messages.cmpntsTo)) {
                result = component.getDeployDir();
            }
            return result;
        }

        @Override
        public void modify(Object arg0, String arg1, Object arg2) {
        	tblViewer.refresh();
        }

    }

    /**
     * Listener method for add button which opens a dialog
     * to add a component.
     */
    protected void addBtnListener() {
        ImportExportDialog dialog = new
        		ImportExportDialog(this.getShell(), waProjManager);
        dialog.open();
        tblViewer.refresh(true);
    }

    /**
     * Listener method for edit button which opens a dialog
     * to edit a component.
     */
    protected void editBtnListener() {
        int selIndex = tblViewer.getTable().getSelectionIndex();
        if (selIndex > -1) {
        WindowsAzureRoleComponent component = listComponents.get(selIndex);
        ImportExportDialog dialog = new ImportExportDialog(this.getShell(),
                 windowsAzureRole , component, waProjManager);
        dialog.open();
        tblViewer.refresh(true);
      }
    }

    /**
     * Listener method for remove button which
     * deletes the selected component.
     */
    protected void removeBtnListener() {
        int selIndex = tblViewer.getTable().getSelectionIndex();
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        WindowsAzureRoleComponent component = listComponents.get(selIndex);
        if (selIndex > -1) {
        	try {
        		/* First condition: Checks component is part of a JDK, server configuration
        		 * Second condition: For not showing error message 
        		 * "Disable Server JDK Configuration" while removing server application 
        		 * when server or JDK  is already disabled.
        		 */
        		if (component.getIsPreconfigured()
        				&& (!(component.getType().equals("server.app") 
        						&& windowsAzureRole.getServerName() == null))) {
        			errorTitle = Messages.jdkDsblErrTtl;
        			errorMessage = Messages.jdkDsblErrMsg;
        			MessageUtil.displayErrorDialog(getShell(),
        					errorTitle, errorMessage);
        		}
                else {
                    boolean choice = MessageDialog.openQuestion(new Shell(),
                            Messages.cmpntRmvTtl, Messages.cmpntRmvMsg);
                    if (choice) {
                        String cmpntPath = String.format("%s%s%s%s%s",
                                root.getProject(waProjManager.
                                		getProjectName()).getLocation(),
                                "\\", windowsAzureRole.getName(), "\\approot\\",
                                component.getDeployName());
                        File file = new File(cmpntPath);
                        // Check import source is equal to approot
                        if (component.getImportPath().isEmpty()
                                && file.exists()) {
                            MessageDialog dialog = new MessageDialog(new Shell(),
                                    Messages.cmpntSrcRmvTtl,
                                    null, Messages.cmpntSrcRmvMsg,
                                    MessageDialog.QUESTION_WITH_CANCEL,
                                    new String[]{
                                IDialogConstants.YES_LABEL,
                                IDialogConstants.NO_LABEL,
                                IDialogConstants.CANCEL_LABEL}, 0);
                            switch(dialog.open()) {
                            case 0:
                                //yes
                                component.delete();
                                tblViewer.refresh();
                                fileToDel.add(file);
                                break;
                            case 1:
                                //no
                                component.delete();
                                tblViewer.refresh();
                                break;
                            case 2:
                                //cancel
                                break;
                            default:
                                break;
                            }
                        }
                        else {
                        	component.delete();
                        	tblViewer.refresh();
                        	fileToDel.add(file);
                        }
                    }
                }
            } catch (WindowsAzureInvalidProjectOperationException e) {
                errorTitle = Messages.cmpntSetErrTtl;
                errorMessage = Messages.cmpntRmvErrMsg;
                MessageUtil.displayErrorDialog(getShell(),
                        errorTitle, errorMessage);
                Activator.getDefault().log(errorMessage, e);
            }
        }

    }


    /**
     * Create TableViewer for components table.
     */
    private void createTableViewer() {
        tblViewer = new TableViewer(tblComponents);

        tblViewer.setUseHashlookup(true);
        tblViewer.setColumnProperties(new String[] {
                Messages.cmpntsImprt,
                Messages.cmpntsFrm,
                Messages.cmpntsAs,
                Messages.cmpntsDply,
                Messages.cmpntsTo });

        CellEditor[] editors = new CellEditor[5];

        editors[0] = new TextCellEditor(tblComponents);
        editors[1] = new TextCellEditor(tblComponents);
        editors[2] = new TextCellEditor(tblComponents);
        editors[3] = new TextCellEditor(tblComponents);
        editors[4] = new TextCellEditor(tblComponents);

        tblViewer.setCellEditors(editors);
        tblViewer.setContentProvider( new CmptTableContentProvider());
        tblViewer.setLabelProvider(new CmptTableLabelProvider());
        tblViewer.setCellModifier(new CellModifier());

        try {
            listComponents = windowsAzureRole.getComponents();
            tblViewer.setInput(listComponents.toArray());
        } catch (Exception e) {
            errorTitle = Messages.cmpntSetErrTtl;
            errorMessage = Messages.cmpntgetErrMsg;
            MessageUtil.displayErrorDialog(getShell(),
                    errorTitle, errorMessage);
            Activator.getDefault().log(errorMessage, e);
        }
    }

    @Override
    public boolean performOk() {
        boolean okToProceed = true;
        try {
        	if (!Activator.getDefault().isSaved()) {
        		waProjManager.save();
        		// Delete files from approot, whose entry from component table is removed
        		if (!fileToDel.isEmpty()) {
        			for (int i = 0; i < fileToDel.size(); i++) {
        				File file = fileToDel.get(i);
        				if (file.exists()) {
        					if (file.isDirectory()) {
        						WAEclipseHelper.deleteDirectory(file);
        					} else {
        						file.delete();
        					}
        				}
        			}
        		}
        		fileToDel.clear();
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
