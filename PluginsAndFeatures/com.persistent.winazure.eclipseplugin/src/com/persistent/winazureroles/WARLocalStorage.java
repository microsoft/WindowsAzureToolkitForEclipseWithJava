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

import java.util.HashMap;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureLocalStorage;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.microsoftopentechnologies.azurecommons.exception.AzureCommonsException;
import com.microsoftopentechnologies.azurecommons.roleoperations.WARLocalStorageUtilMethods;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
import com.persistent.util.WAEclipseHelper;

import waeclipseplugin.Activator;

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
                	PluginUtil.displayErrorDialogAndLog(
                			getShell(),
                			Messages.lclStgSetErrTtl,
                			Messages.lclStgSetErrMsg, e);
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
            	PluginUtil.displayErrorDialogAndLog(
            			getShell(),
            			Messages.lclStgSetErrTtl,
            			Messages.lclStgSetErrMsg, e);
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
        	 try {
        		 loclRes = WARLocalStorageUtilMethods.
        				 modifyName(loclRes, modifiedVal, mapLclStg);
        	 } catch (AzureCommonsException e) {
        		 PluginUtil.displayErrorDialogAndLog(getShell(), Messages.lclStgNameErrTtl, e.getMessage(), e);
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
            	 PluginUtil.displayErrorDialog(
            			 getShell(),
            			 Messages.lclStgSizeErrTtl,
            			 Messages.lclStgSizeEmpMsg);
             } else {

             // Validate size
             int maxSize = WindowsAzureProjectManager.
            		 getMaxLocalStorageSize(windowsAzureRole.getVMSize());
             try {
                 int value = Integer.parseInt(modifiedVal.toString());
                 if (value <= 0) {
                	 PluginUtil.displayErrorDialog(getShell(),
                			 Messages.lclStgSizeErrTtl,
                			 Messages.lclStgSizeErrMsg);
                 } else if (value > maxSize) {
                     boolean choice = MessageDialog.openQuestion(getShell(),
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
            	 PluginUtil.displayErrorDialogAndLog(getShell(),
            			 Messages.lclStgSizeErrTtl,
            			 Messages.lclStgSizeErrMsg, e);
             } catch (WindowsAzureInvalidProjectOperationException e) {
            	 PluginUtil.displayErrorDialogAndLog(
            			 getShell(),
            			 Messages.lclStgSetErrTtl,
            			 Messages.lclStgSetErrMsg, e);
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
        	 try {
        		 WARLocalStorageUtilMethods.modifyPath(
        				 loclRes, modifiedVal, mapLclStg, windowsAzureRole);
        	 } catch (AzureCommonsException e1) {
        		 PluginUtil.displayErrorDialogAndLog(getShell(), Messages.lclStgPathErrTtl, e1.getMessage(), e1);
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
                	 PluginUtil.displayErrorDialogAndLog(
                			 getShell(),
                			 Messages.lclStgSetErrTtl,
                			 Messages.lclStgSetErrMsg, e);
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
    				PluginUtil.displayErrorDialog(getShell(),
    						Messages.cachDsblErTtl,
    						Messages.lclStrRmvErMsg);
    			} else {
    				boolean choice = MessageDialog.openQuestion(getShell(),
    						Messages.lclStgRmvTtl, Messages.lclStgRmvMsg);
    				if (choice) {
    					delRes.delete();
    					tblViewer.refresh();
    					if (tblResources.getItemCount() == 0) {
    						// table is empty i.e. number of rows = 0
    						btnRemove.setEnabled(false);
    						btnEdit.setEnabled(false);
    					}
    				}
    			}
    		} catch (WindowsAzureInvalidProjectOperationException e) {
    			PluginUtil.displayErrorDialogAndLog(
    					getShell(),
    					Messages.lclStgSetErrTtl,
    					Messages.lclStgSetErrMsg, e);
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
    				PluginUtil.displayErrorDialog(getShell(),
    						Messages.cachDsblErTtl,
    						Messages.lclStrEdtErMsg);
    			} else {
    				LocalStorageResourceDialog dialog =
    						new LocalStorageResourceDialog(getShell(),
    								mapLclStg,
    								windowsAzureRole,
    								mapEntry.getKey());
    				dialog.open();
    				tblViewer.refresh();
    			}
    		} catch (Exception e) {
    			PluginUtil.displayErrorDialogAndLog(getShell(),
    					Messages.lclStgSetErrTtl,
    					Messages.lclStgSetErrMsg, e);
    		}
    	}
    }

    /**
     * Create TableViewer for local storage table.,
     * which a) sets the input data for table
     *       b) enables in-place modification.
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
        	PluginUtil.displayErrorDialogAndLog(getShell(),
        			Messages.lclStgSetErrTtl,
        			Messages.lclStgSetErrMsg, e);
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
