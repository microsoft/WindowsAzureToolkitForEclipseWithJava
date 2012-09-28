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
import org.eclipse.jface.viewers.ComboBoxCellEditor;
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
import com.interopbridges.tools.windowsazure.WindowsAzureLocalStorage;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.persistent.util.WAEclipseHelper;
import com.persistent.util.MessageUtil;

/**
 * Property page for Local Storage Resources table.
 */
public class WARLocalStorage extends PropertyPage {

    private WindowsAzureProjectManager waProjManager;
    private WindowsAzureRole windowsAzureRole;
    private Table tblResources;
    private TableViewer tblViewer;
    private static String[] arrType = {
        Messages.lclStgClean, Messages.lclStgPsv};
    private String errorTitle;
    private String errorMessage;
    private Map<String, WindowsAzureLocalStorage> mapLclStg;
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

    /**
     * Create local storage resources table and buttons associated with it.
     *
     * @param parent : parent composite.
     * @return control
     */
    @Override
    protected Control createContents(Composite parent) {
        noDefaultAndApplyButton();
        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
                "com.persistent.winazure.eclipseplugin."
                + "windows_azure_localstorage_page");
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

        tblResources = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
        tblResources.setHeaderVisible(true);
        tblResources.setLinesVisible(true);
        gridData = new GridData();
        gridData.heightHint = 380;
        gridData.horizontalIndent = 3;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        tblResources.setLayoutData(gridData);

        TableColumn colName = new TableColumn(tblResources, SWT.FILL);
        colName.setText(Messages.lclStgRname);
        colName.setWidth(140);

        TableColumn colSize =
                new TableColumn(tblResources, SWT.FILL);
        colSize.setText(Messages.lclStgSize);
        colSize.setWidth(70);

        TableColumn colRecycle = new TableColumn(tblResources, SWT.FILL);
        colRecycle.setText(Messages.lclStgRcl);
        colRecycle.setWidth(75);

        TableColumn colPathVar =
                new TableColumn(tblResources, SWT.FILL);
        colPathVar.setText(Messages.lclStgPath);
        colPathVar.setWidth(185);

        tblResources.addSelectionListener(new SelectionListener() {

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
     * Content provider class for local storage table,
     * which determines the input for the table.
     *
     */
    private class LclStgTableContentProvider
    implements IStructuredContentProvider {

        @Override
        public void inputChanged(Viewer viewer,
                Object oldInput, Object newInput) {
        }

        @Override
        public void dispose() {

        }

        @Override
        public Object[] getElements(Object arg0) {
            if (mapLclStg == null) {
                mapLclStg = new HashMap<String, WindowsAzureLocalStorage>();
            }
            Map<String, WindowsAzureLocalStorage> treeMap =
            		new TreeMap<String,
            		WindowsAzureLocalStorage>(mapLclStg);
            return treeMap.entrySet().toArray();
        }
    }

    /**
     * Label provider class for local storage table,
     * to provide column names.
     */
    private class LclStgTableLabelProvider implements ITableLabelProvider {

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
            Entry<String, WindowsAzureLocalStorage> lclStgEntry =
                    (Entry<String, WindowsAzureLocalStorage>) element;
            WindowsAzureLocalStorage lclStg = lclStgEntry.getValue();
            String result = "";
            switch (colIndex) {
            case 0:
                result = lclStg.getName();
                break;
            case 1:
                result = String.valueOf(lclStg.getSize());
                break;
            case 2:
                if (lclStg.getCleanOnRecycle()) {
                    result = Messages.lclStgClean;
                } else {
                    result = Messages.lclStgPsv;
                }
                break;
            case 3:
                try {
                    result = lclStg.getPathEnv();
                } catch (WindowsAzureInvalidProjectOperationException e) {
                    errorTitle = Messages.lclStgSetErrTtl;
                    errorMessage = Messages.lclStgSetErrMsg;
                    MessageUtil.displayErrorDialog(getShell(),
                            errorTitle, errorMessage);
                    Activator.getDefault().log(errorMessage, e);
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

    /**
     * Cell modifier class for local storage table,
     * which implements the in-place editing for cells.
     *
     */
    private class LclStgCellModifier implements ICellModifier {
        @SuppressWarnings("unchecked")
		@Override
        public void modify(Object waLclStg, String columnName,
                Object modifiedVal) {
            TableItem tblItem = (TableItem) waLclStg;
            Entry<String, WindowsAzureLocalStorage> lclStgEntry =
                    (Entry<String, WindowsAzureLocalStorage>) tblItem.getData();
            WindowsAzureLocalStorage resStg = lclStgEntry.getValue();
            try {
                if (columnName.equals(Messages.lclStgRcl)) {
                    modifyRecycle(resStg, modifiedVal);
                } else if (columnName.equals(Messages.lclStgRname)) {
                    modifyName(resStg, modifiedVal);
                } else if (columnName.equals(Messages.lclStgSize)) {
                    modifySize(resStg, modifiedVal);
                } else if (columnName.equals(Messages.lclStgPath)) {
                    modifyPath(resStg, modifiedVal);
                }
            } catch (Exception e) {
                errorTitle = Messages.lclStgSetErrTtl;
                errorMessage = Messages.lclStgSetErrMsg;
                MessageUtil.displayErrorDialog(getShell(),
                        errorTitle, errorMessage);
                Activator.getDefault().log(errorMessage, e);
            }
            tblViewer.refresh();
        }

        /**
         * Handles the modification of on recycle column.
         *
         * @param loclRes : the local storage resource being modified.
         * @param modifiedVal : new value for local resource on recycle.
         * @throws WindowsAzureInvalidProjectOperationException .
         */
         private void modifyRecycle(WindowsAzureLocalStorage loclRes,
                 Object modifiedVal)
                         throws WindowsAzureInvalidProjectOperationException {
             if (modifiedVal.toString().equals("0")) {
                 loclRes.setCleanOnRecycle(true);
             } else {
                 loclRes.setCleanOnRecycle(false);
             }
         }


         /**
          * Handles the modification of local storage resource name.
          *
          * @param loclRes : the local storage resource being modified.
          * @param modifiedVal : new value for resource name.
          * @throws WindowsAzureInvalidProjectOperationException .
          */
         private void modifyName(WindowsAzureLocalStorage loclRes,
                 Object modifiedVal)
                         throws WindowsAzureInvalidProjectOperationException {
             // Validate resource name
             if (modifiedVal.toString().isEmpty()) {
                 errorTitle = Messages.lclStgNameErrTtl;
                 errorMessage = Messages.lclStgNameEmpMsg;
                 MessageUtil.displayErrorDialog(getShell(),
                         errorTitle, errorMessage);
             } else {
                     StringBuffer strBfr =
                    		 new StringBuffer(modifiedVal.toString());
                     boolean isValidName = true;
                     for (Iterator<String> iterator =
                    		 mapLclStg.keySet().iterator();
                             iterator.hasNext();) {
                         String key = iterator.next();
                         if (key.equalsIgnoreCase(strBfr.toString())) {
                             isValidName = false;
                             break;
                         }
                     }
                     if (isValidName || modifiedVal.toString().equalsIgnoreCase(
                             loclRes.getName())) {
                         loclRes.setName(modifiedVal.toString());
                     } else {
                         errorTitle = Messages.lclStgNameErrTtl;
                         errorMessage = Messages.lclStgNameErrMsg;
                         MessageUtil.displayErrorDialog(getShell(),
                                 errorTitle, errorMessage);
                     }
             }
         }

         /**
          * Handles the modification of size of local storage resource.
          *
          * @param loclRes : the local resource being modified.
          * @param modifiedVal : new value for size.
          * @throws WindowsAzureInvalidProjectOperationException .
          */
         private void modifySize(WindowsAzureLocalStorage loclRes,
                 Object modifiedVal)
                         throws WindowsAzureInvalidProjectOperationException {
             if (modifiedVal.toString().isEmpty()) {
                 errorTitle = Messages.lclStgSizeErrTtl;
                 errorMessage = Messages.lclStgSizeEmpMsg;
                 MessageUtil.displayErrorDialog(getShell(),
                         errorTitle, errorMessage);
             } else {

             // Validate size
             int maxSize = WindowsAzureProjectManager.
            		 getMaxLocalStorageSize(windowsAzureRole.getVMSize());
             try {
                 int value = Integer.parseInt(modifiedVal.toString());
                 if (value <= 0) {
                     errorTitle = Messages.lclStgSizeErrTtl;
                     errorMessage = Messages.lclStgSizeErrMsg;
                     MessageUtil.displayErrorDialog(getShell(),
                             errorTitle, errorMessage);
                 } else if (value > maxSize) {
                     boolean choice = MessageDialog.openQuestion(new Shell(),
                             Messages.lclStgMxSizeTtl, String.format("%s%s%s",
                                     Messages.lclStgMxSizeMsg1 , maxSize ,
                                     Messages.lclStgMxSizeMsg2));
                     if (choice) {
                         loclRes.setSize(value);
                     }
                 }
                 else {
                     loclRes.setSize(value);
                 }

             } catch (NumberFormatException e) {
                 errorTitle = Messages.lclStgSizeErrTtl;
                 errorMessage = Messages.lclStgSizeErrMsg;
                 MessageUtil.displayErrorDialog(getShell(),
                         errorTitle, errorMessage);
                 Activator.getDefault().log(errorMessage, e);
             } catch (WindowsAzureInvalidProjectOperationException e) {
                 errorTitle = Messages.lclStgSetErrTtl;
                 errorMessage = Messages.lclStgSetErrMsg;
                 MessageUtil.displayErrorDialog(getShell(),
                         errorTitle, errorMessage);
                 Activator.getDefault().log(errorMessage, e);
             }
             }
         }

         /**
          * Handles the in-place modification of local storage resource.
          *
          * @param loclRes : the local storage resource being modified.
          * @param modifiedVal : new value for path.
          * @throws WindowsAzureInvalidProjectOperationException .
          */
         private void modifyPath(WindowsAzureLocalStorage loclRes,
                 Object modifiedVal)
                         throws WindowsAzureInvalidProjectOperationException {
             StringBuffer strBfr = new StringBuffer(modifiedVal.toString());
             if (modifiedVal.toString().isEmpty()) {
                 loclRes.setPathEnv("");
             }
             else if (!modifiedVal.toString().
            		 equalsIgnoreCase(loclRes.getPathEnv())) {
                 try {
                     boolean isPathValid = true;
                     for (Iterator<WindowsAzureLocalStorage> iterator =
                             mapLclStg.values().iterator();
                    		 iterator.hasNext();) {
                         WindowsAzureLocalStorage type =
                        		 (WindowsAzureLocalStorage) iterator.next();
                         if (type.getPathEnv().
                        		 equalsIgnoreCase(strBfr.toString())) {
                             isPathValid = false;
                             break;
                         }
                     }
                     if (windowsAzureRole.getRuntimeEnv().
                    		 containsKey(strBfr.toString())) {
                         isPathValid = false;
                         errorTitle = Messages.lclStgPathErrTtl;
                         errorMessage = Messages.lclStgEnvVarMsg;
                         MessageUtil.displayErrorDialog(getShell(),
                                 errorTitle, errorMessage);
                     }
                     else if (isPathValid
                    		 || modifiedVal.toString().equalsIgnoreCase(
                             loclRes.getPathEnv())) {
                         loclRes.setPathEnv(modifiedVal.toString());
                     } else {
                         errorTitle = Messages.lclStgPathErrTtl;
                         errorMessage = Messages.lclStgPathErrMsg;
                         MessageUtil.displayErrorDialog(getShell(),
                                 errorTitle, errorMessage);
                     }
                 }
                 catch (Exception e) {
                     errorTitle = Messages.lclStgSetErrTtl;
                     errorMessage = Messages.lclStgSetErrMsg;
                     MessageUtil.displayErrorDialog(getShell(),
                             errorTitle, errorMessage);
                     Activator.getDefault().log(errorMessage, e);
                 }
             }
         }

         @SuppressWarnings("unchecked")
		public Object getValue(Object element, String property) {
             Object result = null;
             Entry<String, WindowsAzureLocalStorage> lclStgEntry =
            		 (Entry<String, WindowsAzureLocalStorage>) element;
             WindowsAzureLocalStorage lclStg = lclStgEntry.getValue();
             if (property.equals(Messages.lclStgRcl)) {
                 if (lclStg.getCleanOnRecycle()) {
                     result = 0;
                 } else {
                     result = 1;
                 }
             } else if (property.equals(Messages.lclStgRname)) {
                 result = lclStg.getName();
             } else if (property.equals(Messages.lclStgSize)) {
                 result = String.valueOf(lclStg.getSize());
             } else if (property.equals(Messages.lclStgPath)) {
                 try {
                     result = lclStg.getPathEnv();
                 } catch (WindowsAzureInvalidProjectOperationException e) {
                     errorTitle = Messages.lclStgSetErrTtl;
                     errorMessage = Messages.lclStgSetErrMsg;
                     MessageUtil.displayErrorDialog(getShell(),
                             errorTitle, errorMessage);
                     Activator.getDefault().log(errorMessage, e);
                 }
             }
             return result;
         }

         /**
          * Determines whether a particular cell can be modified or not.
          */
         @Override
         public boolean canModify(Object element, String property) {
        	 boolean retVal = true;
        	 @SuppressWarnings("unchecked")
        	 Entry<String, WindowsAzureLocalStorage> entry =
        	 (Entry<String, WindowsAzureLocalStorage>) element;
        	 /*
        	  * If local storage selected for in place editing
        	  * is related to caching then don't allow.
        	  */
        	 if (entry.getValue().getName().
        			 equals(Messages.cachLclStrNm)
        			 && (property.equals(Messages.lclStgRname)
        					 || property.equals(Messages.lclStgSize)
        					 || property.equals(Messages.lclStgRcl)
        					 || property.equals(Messages.lclStgPath))) {
        		 retVal = false;
        	 }
        	 return retVal;
         }
    }

    /**
     * Listener method for remove button which
     * deletes the selected local storage resource.
     */
    @SuppressWarnings("unchecked")
    protected void removeBtnListener() {
    	int selIndex = tblViewer.getTable().getSelectionIndex();
    	if (selIndex > -1) {
    		try {
    			Entry<String, WindowsAzureLocalStorage> mapEntry =
    					(Entry<String, WindowsAzureLocalStorage>)
    					tblViewer.getTable().getItem(selIndex).getData();
    			WindowsAzureLocalStorage delRes = mapEntry.getValue();
    			/*
    			 * Check local storage selected for removal
    			 * is associated with caching then give error
    			 * and does not allow to remove.
    			 */
    			if (delRes.isCachingLocalStorage()) {
    				errorTitle = Messages.cachDsblErTtl;
    				errorMessage = Messages.lclStrRmvErMsg;
    				MessageUtil.displayErrorDialog(getShell(),
    						errorTitle, errorMessage);
    			} else {
    				boolean choice = MessageDialog.openQuestion(new Shell(),
    						Messages.lclStgRmvTtl, Messages.lclStgRmvMsg);
    				if (choice) {
    					delRes.delete();
    					tblViewer.refresh();
    				}
    			}
    		} catch (WindowsAzureInvalidProjectOperationException e) {
    			errorTitle = Messages.lclStgSetErrTtl;
    			errorMessage = Messages.lclStgSetErrMsg;
    			MessageUtil.displayErrorDialog(getShell(),
    					errorTitle, errorMessage);
    			Activator.getDefault().log(errorMessage, e);
    		}
    	}
    }

    /**
     * Listener method for add button which opens a dialog
     * to add a local storage resource.
     */
    protected void addBtnListener() {
        LocalStorageResourceDialog dialog =
        		new LocalStorageResourceDialog(this.getShell(),
        				mapLclStg);
        dialog.open();
        tblViewer.refresh(true);
    }

    /**
     * Listener for edit button, which launches a dialog to edit
     * a local storage resource.
     */
    @SuppressWarnings("unchecked")
    protected void editBtnListener() {
    	int selIndex = tblViewer.getTable().getSelectionIndex();
    	if (selIndex > -1) {
    		try {
    			Entry<String, WindowsAzureLocalStorage> mapEntry =
    					(Entry<String, WindowsAzureLocalStorage>)
    					tblViewer.getTable().getItem(selIndex).getData();
    			/*
    			 * Check local storage selected for modification
    			 * is associated with caching then give error
    			 * and does not allow to edit.
    			 */
    			if (mapEntry.getValue().isCachingLocalStorage()) {
    				errorTitle = Messages.cachDsblErTtl;
    				errorMessage = Messages.lclStrEdtErMsg;
    				MessageUtil.displayErrorDialog(getShell(),
    						errorTitle, errorMessage);		
    			} else {
    				LocalStorageResourceDialog dialog =
    						new LocalStorageResourceDialog(getShell(),
    								mapLclStg , windowsAzureRole, mapEntry.getKey());
    				dialog.open();
    				tblViewer.refresh();
    			}
    		} catch (Exception e) {
    			errorTitle = Messages.lclStgSetErrTtl;
    			errorMessage = Messages.lclStgSetErrMsg;
    			MessageUtil.displayErrorDialog(getShell(),
    					errorTitle, errorMessage);
    			Activator.getDefault().log(errorMessage, e);
    		}
    	}
    }


    /**
     * Create TableViewer for local storage table.,
     * which a) sets the input data for table
     *       b) enables in-place modification.
     *
     */
    private void createTableViewer() {
        tblViewer = new TableViewer(tblResources);

        tblViewer.setUseHashlookup(true);
        tblViewer.setColumnProperties(new String[] {
                Messages.lclStgRname,
                Messages.lclStgSize,
                Messages.lclStgRcl,
                Messages.lclStgPath });

        CellEditor[] editors = new CellEditor[4];

        editors[0] = new TextCellEditor(tblResources);
        editors[1] = new TextCellEditor(tblResources);
        editors[2] = new ComboBoxCellEditor(tblResources, arrType,
                SWT.READ_ONLY);
        editors[3] = new TextCellEditor(tblResources);
        tblViewer.setCellEditors(editors);
        tblViewer.setContentProvider(new LclStgTableContentProvider());
        tblViewer.setLabelProvider(new LclStgTableLabelProvider());
        tblViewer.setCellModifier(new LclStgCellModifier());

        try {
            mapLclStg = windowsAzureRole.getLocalStorage();
            tblViewer.setInput(mapLclStg.entrySet().toArray());
        } catch (Exception e) {
            errorTitle = Messages.lclStgSetErrTtl;
            errorMessage = Messages.lclStgSetErrMsg;
            MessageUtil.displayErrorDialog(getShell(),
                    errorTitle, errorMessage);
            Activator.getDefault().log(errorMessage, e);
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
