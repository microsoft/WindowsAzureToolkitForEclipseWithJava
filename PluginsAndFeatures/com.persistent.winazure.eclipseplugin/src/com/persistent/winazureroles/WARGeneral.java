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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

import waeclipseplugin.Activator;

import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureNamedCache;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
import com.persistent.util.WAEclipseHelper;

/**
 * Property page for windows azure role.
 */
public class WARGeneral extends PropertyPage {

    private Text txtRoleName;
    private Combo comboVMSize;
    private Text txtNoOfInstances;
    private String[] arrVMSize = {"ExtraLarge", "Large",
    		"Medium", "Small", "ExtraSmall"};
    private boolean isValidRoleName = false;
    private boolean isValidinstances = true;
    private WindowsAzureProjectManager waProjManager;
    private WindowsAzureRole windowsAzureRole;
    private boolean isPageDisplayed = false;

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
            	handleSmallVMCacheConf();
            	
            	// Set VM Size in role
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
        txtNoOfInstances.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				instancesModifyListener();
			}
		});
    }

    /**
     * Modify listener for number of instances textbox.
     * Validate number of instances field.
     */
    protected void instancesModifyListener() {
    	if (isValidRoleName) {
    		setValid(true);
    	} else  {
    		setValid(false);
    	}
        String noOfInstances = txtNoOfInstances.getText();
        /*
         * If text box is not empty
         * then only get integer value using casting.
         */
        if (!noOfInstances.isEmpty()) {
        	try {
        		int instances = Integer.parseInt(noOfInstances);
        		if (instances < 1) {
        			isValidinstances = false;
        		} else {
        			isValidinstances = true;
        		}
        	} catch (NumberFormatException ex) {
        		isValidinstances = false;
        	}
        }
        try {
        	/*
        	 * If text box is empty then do not show error
        	 * as user may be giving input.
        	 * Just disable OK button.
        	 */
        	if (noOfInstances.isEmpty()) {
        		setValid(false);
        	} else if (!isValidinstances) {
                setValid(false);
                PluginUtil.displayErrorDialog(getShell(),
                		Messages.dlgInvldInst1,
                		Messages.dlgInvldInst2);
        	} else {
        		windowsAzureRole.setInstances(txtNoOfInstances.getText());
        	}
        } catch (Exception ex) {
        	PluginUtil.displayErrorDialogAndLog(
        			getShell(),
        			Messages.adRolErrTitle,
        			Messages.adRolErrMsgBox1
        			+ Messages.adRolErrMsgBox2, ex);
        }
    }

    /**
     * Listener for VM size combo box.
     */
    private void vmSizeSelectionListener() {
        try {
            windowsAzureRole.setVMSize(comboVMSize.getText());
        } catch (WindowsAzureInvalidProjectOperationException e) {
        	PluginUtil.displayErrorDialogAndLog(
        			getShell(),
        			Messages.adRolErrTitle,
        			Messages.adRolErrMsgBox1
        			+ Messages.adRolErrMsgBox2, e);
        }
    }


    /**
     * Modify listener for role name textbox.
     *
     * @param event : ModifyEvent
     */
    private void roleNameModifyListener(ModifyEvent event) {
    	if (isValidinstances) {
    		setValid(true);
    	} else {
    		setValid(false);
    	}
        String roleName = ((Text) event.widget).getText().trim();
        try {
            if (roleName.equalsIgnoreCase(
                    windowsAzureRole.getName())) {
                isValidRoleName = true;
            } else {
                isValidRoleName = waProjManager
                        .isAvailableRoleName(roleName);
            }
            /*
        	 * If text box is empty then do not show error
        	 * as user may be giving input.
        	 * Just disable OK button.
        	 */
            if (txtRoleName.getText().isEmpty()) {
            	setValid(false);
            } else if (isValidRoleName) {
                windowsAzureRole.setName(txtRoleName.getText().trim());
            } else {
            	setValid(false);
            	PluginUtil.displayErrorDialog(getShell(),
            			Messages.dlgInvldRoleName1,
            			Messages.dlgInvldRoleName2);
            }
        } catch (WindowsAzureInvalidProjectOperationException e) {
        	PluginUtil.displayErrorDialogAndLog(
        			getShell(),
        			Messages.adRolErrTitle,
        			Messages.adRolErrMsgBox1
        			+ Messages.adRolErrMsgBox2, e);
        }
    }

    /**
     * Populates the data in case of edit operation of role.
     */
    private void showContents() {
        txtRoleName.setText(windowsAzureRole.getName());
        comboVMSize.setText(arrVMSize[getVMSizeIndex()]);

        try {
            txtNoOfInstances.setText(windowsAzureRole.getInstances());
        } catch (WindowsAzureInvalidProjectOperationException e) {
        	PluginUtil.displayErrorDialogAndLog(
        			this.getShell(),
        			Messages.adRolErrTitle,
        			Messages.adRolErrMsgBox1
        			+ Messages.adRolErrMsgBox2, e);
        }
    }
    
    private int getVMSizeIndex() {
    	String vmSize = "";
    	
    	 vmSize = windowsAzureRole.getVMSize();
         int index = 3;
         for (int i = 0; i < arrVMSize.length; i++) {
             if (vmSize.equalsIgnoreCase(arrVMSize[i])) {
                 index = i;
                 break;
             }
         }
         return index;
    	
    }

    @Override
    public boolean okToLeave() {
    	boolean okToProceed = true;
    	okToProceed = handleHighAvailabilityFeature(okToProceed);
    	boolean retVal = false;
    	if (okToProceed) {
    		retVal = super.okToLeave();
    	}
    	return retVal;
    }

    @Override
    public boolean performOk() {
    	if (!isPageDisplayed) {
    		return super.performOk();
    	}
    	boolean okToProceed = true;
    	try {
    		okToProceed = handleHighAvailabilityFeature(okToProceed);
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
    
    private void handleSmallVMCacheConf() {
    	try {
    		if(Messages.txtExtraSmallVM.equals(comboVMSize.getText()) && windowsAzureRole.getCacheMemoryPercent() > 0 ) { 
    			// If extra small VM and cache is enabled
    			MessageDialog dialog = new MessageDialog(new Shell(),Messages.cacheConfTitle,null,
    													 Messages.cacheConfMsg,
    													 MessageDialog.WARNING,
    													 new String[] { "Yes", "No"},2);
    			
    			 int choice = dialog.open();
    			 switch(choice) {
    			 	case 0: // Yes - Disable cache
    			 		windowsAzureRole.setCacheMemoryPercent(0);
    			 		break;
    			 		
    			 	case  1:	 // No or if dialog is closed directly then reset VM size back to original
    			 	case -1:     
    			 		comboVMSize.setText(arrVMSize[getVMSizeIndex()]);
    			 		break;
    			 }
    		}
    	} catch (WindowsAzureInvalidProjectOperationException e) {
        		PluginUtil.displayErrorDialogAndLog(getShell(),Messages.cachErrTtl,Messages.cachGetErMsg, e);
       	}    	
    }

    /**
     * Method checks if number of instances are equal to 1
     * and caching is enabled as well as high availability
     * feature is on then ask input from user,
     * whether to turn off high availability feature
     * or he wants to edit instances.
     * @param okToProceed
     * @return boolean
     */
    private boolean handleHighAvailabilityFeature(
    		boolean okToProceed) {
    	boolean isBackupSet = false;
    	try {
    		/*
    		 * checks if number of instances are equal to 1
    		 * and caching is enabled
    		 */
    		if (txtNoOfInstances.getText().trim().equalsIgnoreCase("1")
    				&& windowsAzureRole.getCacheMemoryPercent() > 0) {
    			/*
    			 * Check high availability feature of any of the cache is on
    			 */
    			Map<String, WindowsAzureNamedCache> mapCache =
    					windowsAzureRole.getNamedCaches();
    			for (Iterator<WindowsAzureNamedCache> iterator =
    					mapCache.values().iterator();
    					iterator.hasNext();) {
    				WindowsAzureNamedCache cache =
    						(WindowsAzureNamedCache) iterator.next();
    				if (cache.getBackups()) {
    					isBackupSet = true;
    				}
    			}
    			/*
    			 * High availability feature of any of the cache is on.
    			 */
    			if (isBackupSet) {
    				boolean choice = MessageDialog.openConfirm(
    						new Shell(),
    						Messages.highAvailTtl,
    						Messages.highAvailMsg);
    				/*
    				 * Set High availability feature to No.
    				 */
    				if (choice) {
    					for (Iterator<WindowsAzureNamedCache> iterator =
    							mapCache.values().iterator();
    							iterator.hasNext();) {
    						WindowsAzureNamedCache cache =
    								(WindowsAzureNamedCache) iterator.next();
    						if (cache.getBackups()) {
    							cache.setBackups(false);
    						}
    					}
    					okToProceed = true;
    					waProjManager.save();
    				} else {
    					/*
    					 * Stay on Role properties page.
    					 */
    					okToProceed = false;
    					txtNoOfInstances.setFocus();
    				}
    			}
    		}
    	} catch (WindowsAzureInvalidProjectOperationException e) {
    		PluginUtil.displayErrorDialogAndLog(
    				getShell(),
    				Messages.cachErrTtl,
    				Messages.cachGetErMsg, e);
    		okToProceed = false;
    	}
    	return okToProceed;
    }
}
