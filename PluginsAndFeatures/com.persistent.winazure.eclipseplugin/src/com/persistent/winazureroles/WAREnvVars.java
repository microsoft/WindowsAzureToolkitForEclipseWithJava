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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

import waeclipseplugin.Activator;

import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
import com.persistent.util.WAEclipseHelper;
/**
 * Property page for Environment variables table.
 */
public class WAREnvVars extends PropertyPage {

    private WindowsAzureProjectManager waProjManager;
    private WindowsAzureRole windowsAzureRole;
    private Table tblEnvVariables;
    private TableViewer tblViewer;
    private Map<String, String> mapEnvVar;
    private Button btnEdit;
    private Button btnRemove;
    private boolean isPageDisplayed = false;

    @Override
    public String getTitle() {
    	if (isPageDisplayed
    			&& tblViewer != null) {
    		tblViewer.refresh();
    	}
        return super.getTitle();
    }

    @Override
    protected Control createContents(Composite parent) {
        noDefaultAndApplyButton();
        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
                "com.persistent.winazure.eclipseplugin."
                + "windows_azure_envvar_page");
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

        tblEnvVariables = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
        tblEnvVariables.setHeaderVisible(true);
        tblEnvVariables.setLinesVisible(true);
        gridData = new GridData();
        gridData.heightHint = 380;
        gridData.horizontalIndent = 3;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        tblEnvVariables.setLayoutData(gridData);

        TableColumn colName = new TableColumn(tblEnvVariables, SWT.FILL);
        colName.setText(Messages.evColName);
        colName.setWidth(150);

        TableColumn colValue = new TableColumn(tblEnvVariables, SWT.FILL);
        colValue.setText(Messages.evColValue);
        colValue.setWidth(325);

        tblEnvVariables.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                btnEdit.setEnabled(true);
                btnRemove.setEnabled(true);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {

            }
        });

        // container for add/remove/remove buttons
        Composite containerButtons = new Composite(container, SWT.NONE);
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
        isPageDisplayed = true;
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
     * Listener for add button, which launches a dialog to add
     * an environment variable.
     */
    protected void addBtnListener() {
        try {
            WAEnvVarDialog dialog = new WAEnvVarDialog(getShell(),
                    mapEnvVar, windowsAzureRole);
            dialog.open();
            tblViewer.refresh();
        } catch (Exception ex) {
        	PluginUtil.displayErrorDialogAndLog(
        			getShell(),
        			Messages.evLaunchErrTtl,
        			Messages.evErrLaunchMsg1
        			+ Messages.evErrLaunchMsg2, ex);
        }
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
     * Listener for edit button, which launches a dialog to edit
     * an environment variable.
     */
    @SuppressWarnings("unchecked")
	protected void editBtnListener() {
        int selIndex = tblViewer.getTable().getSelectionIndex();
        if (selIndex > -1) {
            try {
                Entry<String, String> mapEntry =
                        (Entry<String, String>) tblViewer.getTable().getItem(
                        selIndex).getData();
                WAEnvVarDialog dialog = new WAEnvVarDialog(getShell(),
                        mapEnvVar, windowsAzureRole, mapEntry.getKey());
                dialog.open();
                tblViewer.refresh();
            } catch (Exception ex) {
            	PluginUtil.displayErrorDialogAndLog(
            			getShell(),
            			Messages.evLaunchErrTtl,
            			Messages.evErrLaunchMsg1
            			+ Messages.evErrLaunchMsg2, ex);
            }
        }
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
     * Listener for remove button, which removes the
     * environment variable from the role.
     */
    @SuppressWarnings("unchecked")
	protected void removeBtnListener() {
        int selIndex = tblViewer.getTable().getSelectionIndex();
        if (selIndex > -1) {
            try {
                Entry<String, String> mapEntry =
                        (Entry<String, String>) tblViewer.getTable().getItem(
                                selIndex).getData();
                // Check environment variable is associated with component
                if (windowsAzureRole.getIsEnvPreconfigured(mapEntry.getKey())) {
                	PluginUtil.displayErrorDialog(getShell(),
                			Messages.jdkDsblErrTtl,
                			Messages.envJdkDslErrMsg);
                } else {
                    boolean choice = MessageDialog.openQuestion(new Shell(),
                            Messages.evRemoveTtl, Messages.evRemoveMsg);
                    if (choice) {
                        /*
                         * to delete call rename with
                         * newName(second param) as empty
                         */
                        windowsAzureRole.
                        renameRuntimeEnv(mapEntry.getKey(), "");
                        tblViewer.refresh();
                    }
                }
            } catch (Exception ex) {
            	PluginUtil.displayErrorDialogAndLog(
            			this.getShell(),
            			Messages.adRolErrTitle,
            			Messages.adRolErrMsgBox1
            			+ Messages.adRolErrMsgBox2, ex);
            }
        }
    }

    /**
     * Creates a table viewer for environment variables table,
     * which a) sets the input data for table
     *       b) enables in-place modification.
     *
     */
    private void createTableViewer() {
        tblViewer = new TableViewer(tblEnvVariables);

        tblViewer.setUseHashlookup(true);
        tblViewer.setColumnProperties(new String[] {
                Messages.evColName,
                Messages.evColValue });

        CellEditor[] editors = new CellEditor[2];

        editors[0] = new TextCellEditor(tblEnvVariables);
        editors[1] = new TextCellEditor(tblEnvVariables);

        tblViewer.setCellEditors(editors);
        tblViewer.setContentProvider(new EnvVarContentProvider());
        tblViewer.setLabelProvider(new EnvVarLabelProvider());
        tblViewer.setCellModifier(new EnvVarCellModifier());

        try {
            mapEnvVar = windowsAzureRole.getRuntimeEnv();
            tblViewer.setInput(mapEnvVar.entrySet().toArray());
        } catch (Exception ex) {
        	PluginUtil.displayErrorDialogAndLog(
        			getShell(),
        			Messages.rolsErr,
        			Messages.adRolErrMsgBox1
        			+ Messages.adRolErrMsgBox2, ex);
        }
    }

    /**
     * Content provider class for environment variables table,
     * which sets the input for the table.
     *
     */
    private class EnvVarContentProvider implements IStructuredContentProvider {

        @Override
        public void inputChanged(Viewer viewer,
                Object oldInput, Object newInput) {

        }

        @Override
        public void dispose() {

        }

        @Override
        public Object[] getElements(Object arg0) {
            if (mapEnvVar == null) {
                mapEnvVar = new HashMap<String, String>();
            }
            Map<String, String> treeMap =
            		new TreeMap<String, String>(mapEnvVar);
            return treeMap.entrySet().toArray();
        }
    }

    /**
     * Label provider class for environment variables table,
     * to set column data.
     *
     */
    private class EnvVarLabelProvider implements ITableLabelProvider {

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

        @SuppressWarnings("unchecked")
		@Override
        public String getColumnText(Object element, int colIndex) {
            String result = "";
            try {
                Entry<String, String> mapEntry =
                		(Entry<String, String>) element;
                switch (colIndex) {
                case 0:
                    result = mapEntry.getKey();
                    break;
                case 1:
                    result = mapEntry.getValue();
                    break;
                default:
                    // This switch is for column indices. Here the switch always
                    // have an valid index so default will never be reached.
                }
            } catch (Exception ex) {
            	PluginUtil.displayErrorDialogAndLog(
            			getShell(),
            			Messages.adRolErrTitle,
            			Messages.adRolErrMsgBox1
            			+ Messages.adRolErrMsgBox2, ex);
            }
            return result;
        }

        @Override
        public Image getColumnImage(Object arg0, int arg1) {
            return null;
        }
    }

    /**
     * Cell modifier class for environment variables table,
     * which implements the in-place editing for cells.
     *
     */
    private class EnvVarCellModifier implements ICellModifier {
        @SuppressWarnings("unchecked")
		@Override
        public void modify(Object variable, String columnName,
                Object modifiedVal) {
            try {
                TableItem tblItem = (TableItem) variable;
                Entry<String, String> mapEntry =
                        (Entry<String, String>) tblItem.getData();
                if (columnName.equals(Messages.evColName)) {
                    String modifiedName = modifiedVal.toString();
                    boolean isValidName = true;
                    for (Iterator<String> iterator =
                    		mapEnvVar.keySet().iterator();
                            iterator.hasNext();) {
                        String key = iterator.next();
                        if (key.equalsIgnoreCase(modifiedName)) {
                            isValidName = false;
                            break;
                        }
                    }
                    if (modifiedName.isEmpty()) {
                    	PluginUtil.displayErrorDialog(
                    			getShell(),
                    			Messages.evNameEmptyTtl,
                    			Messages.evNameEmptyMsg);
                    } else if (!isValidName
                        && !modifiedName.equalsIgnoreCase(mapEntry.getKey())
                        || windowsAzureRole.getLsEnv().contains(modifiedName)) {
                    	PluginUtil.displayErrorDialog(getShell(),
                    			Messages.evInUseTitle,
                    			Messages.evInUseMsg);
                    } else {
                        String name = modifiedName.trim();
                        name = name.replaceAll("[\\s]+", "_");
                        windowsAzureRole.renameRuntimeEnv(mapEntry.getKey(),
                                name);
                    }
                } else if (columnName.equals(Messages.evColValue)) {
                    windowsAzureRole.setRuntimeEnv(mapEntry.getKey(),
                            modifiedVal.toString().trim());
                }
                tblViewer.refresh();
            } catch (Exception ex) {
            	PluginUtil.displayErrorDialogAndLog(
            			getShell(),
            			Messages.adRolErrTitle,
            			Messages.adRolErrMsgBox1
            			+ Messages.adRolErrMsgBox2, ex);
            }
        }

        @SuppressWarnings("unchecked")
		public Object getValue(Object element, String property) {
            Object result = null;
            try {
                Entry<String, String> mapEntry =
                		(Entry<String, String>) element;
                if (property.equals(Messages.evColName)) {
                    result = mapEntry.getKey();
                } else if (property.equals(Messages.evColValue)) {
                    result = mapEntry.getValue();
                }
            } catch (Exception ex) {
            	PluginUtil.displayErrorDialogAndLog(
            			getShell(),
            			Messages.adRolErrTitle,
            			Messages.adRolErrMsgBox1
            			+ Messages.adRolErrMsgBox2, ex);
            }

            return result;
        }

        /**
         * Determines whether a particular cell can be modified or not.
         */
        @Override
        public boolean canModify(Object element, String property) {
            return true;
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
}
