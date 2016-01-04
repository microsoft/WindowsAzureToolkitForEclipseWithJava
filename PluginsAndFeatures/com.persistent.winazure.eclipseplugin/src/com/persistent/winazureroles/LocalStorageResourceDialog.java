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
import java.io.FileInputStream;
import java.net.URL;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureLocalStorage;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.microsoftopentechnologies.azurecommons.exception.AzureCommonsException;
import com.microsoftopentechnologies.azurecommons.roleoperations.LocalStrgResDialogUtilMethods;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;

import waeclipseplugin.Activator;
/**
 * Class creates UI controls and respective listeners
 * for add or edit local storage dialog.
 */
public class LocalStorageResourceDialog extends TitleAreaDialog {
    private Text txtResource;
    private Text txtSize;
    private Text txtVar;
    private Button btnClean;
    private boolean isResEdit;
    private Button okButton;
    private WindowsAzureRole windowsAzureRole;
    private int maxSize;
    private Map<String,WindowsAzureLocalStorage> lclStgMap;
    private String resName;

    /**
     * Constructor to be called for add local storage resources.
     * @param parentShell
     * @param mapLclStg : map containing all local storage resources.
     */
    public LocalStorageResourceDialog(Shell parentShell,
    	    Map<String,
    		WindowsAzureLocalStorage> mapLclStg) {
        super(parentShell);
        windowsAzureRole = Activator.getDefault().getWaRole();
        lclStgMap = mapLclStg;
    }

    /**
     * Constructor to be called for editing an local storage resource.
     *
     * @param parentShell
     * @param mapLclStg : map containing all local storage resources.
     * @param windowsAzureRole
     * @param key
     */
    public LocalStorageResourceDialog(Shell parentShell,
    		Map<String,
    		WindowsAzureLocalStorage> mapLclStg,
            WindowsAzureRole windowsAzureRole,
            String key) {
        super(parentShell);
        this.lclStgMap = mapLclStg;
        this.windowsAzureRole = windowsAzureRole;
        this.isResEdit = true;
        this.resName = key;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.lclStrTtl);
        newShell.setLocation(250, 250);
        Image image;
        try {
            URL imgUrl = Activator.getDefault().
            		getBundle().getEntry(Messages.lclDlgImg);
            URL imgFileURL = FileLocator.toFileURL(imgUrl);
            URL path = FileLocator.resolve(imgFileURL);
            String imgpath = path.getFile();
            image = new Image(null, new FileInputStream(imgpath));
            setTitleImage(image);
        } catch (Exception e) {
            PluginUtil.displayErrorDialogAndLog(
            		getShell(),
            		Messages.genErrTitle,
            		Messages.lclDlgImgErr, e);
        }
    }


    @Override
    protected Control createDialogArea(Composite parent) {
        setTitle(Messages.lclStrTxt);
        setMessage(Messages.lclStrMsg);
        //display help contents
        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
                "com.persistent.winazure.eclipseplugin."
                        + "windows_azure_localstorage_page");
        maxSize =  WindowsAzureProjectManager.
        		getMaxLocalStorageSize(windowsAzureRole.getVMSize());
        Activator.getDefault().setSaved(false);
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        GridData gridData = new GridData();
        gridLayout.numColumns = 2;
        gridLayout.marginBottom = 8;
        gridData.verticalIndent = 10;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        container.setLayout(gridLayout);
        container.setLayoutData(gridData);
        createResName(container);
        createSizeComponent(container);
        createNote(container);
        createCleanBtn(container);
        createPathComponent(container);
        if (isResEdit) {
            populateData();
        } else {
        	StringBuffer strBfr = LocalStrgResDialogUtilMethods.formName(lclStgMap);
            txtResource.setText(strBfr.toString());
            txtSize.setText("1");
            txtVar.setText(String.format("%s%s",
            		strBfr, Messages.lclStgPathStr));
        }
        return super.createDialogArea(parent);
    }

    @Override
    protected Control createButtonBar(Composite parent) {
        Control ctrl = super.createButtonBar(parent);
        okButton = getButton(IDialogConstants.OK_ID);
        okButton.setEnabled(true);
        return ctrl;
    }

    @Override
    protected void okPressed() {
        boolean retVal = true;
        try {
            if (isResEdit && txtVar.getText().
                    equalsIgnoreCase(lclStgMap.get(resName).getPathEnv())) {
                retVal =  isValidName(txtResource.getText())
                		&& isValidSize(txtSize.getText(),
                				txtResource.getText());
            } else {
                retVal = isValidName(txtResource.getText())
                		&& isValidSize(txtSize.getText(),
                				txtResource.getText())
                        && isValidPath(txtVar.getText());
            }
            if (!isResEdit && retVal) {
                windowsAzureRole.addLocalStorage(txtResource.getText(),
                		Integer.parseInt(txtSize.getText()),
                		btnClean.getSelection() , txtVar.getText());
            } else if (isResEdit && retVal) {
                lclStgMap.get(resName).setName(txtResource.getText());
                if (!resName.equalsIgnoreCase(txtResource.getText())) {
                    resName = txtResource.getText();
                }

                lclStgMap.get(resName).setSize(Integer.
                		parseInt(txtSize.getText()));
                lclStgMap.get(resName).setPathEnv(txtVar.getText());
                lclStgMap.get(resName).
                setCleanOnRecycle(btnClean.getSelection());
            }
        } catch (NumberFormatException e) {
            PluginUtil.displayErrorDialogAndLog(
            		getShell(),
            		Messages.lclStgSizeErrTtl,
            		Messages.lclStgSizeErrMsg, e);
        } catch (WindowsAzureInvalidProjectOperationException e) {
            PluginUtil.displayErrorDialogAndLog(
            		getShell(),
            		Messages.lclStgSetErrTtl,
            		Messages.lclStgSetErrMsg, e);
        }
        if (retVal) {
            super.okPressed();
        }
    }


    /**
     * Populates the resource name and value text fields with the corresponding
     * attributes of local storage resource selected for editing.
     *
     */
     private void populateData() {
         try {
             WindowsAzureLocalStorage stg = lclStgMap.get(resName);
             txtResource.setText(stg.getName());
             txtSize.setText(String.valueOf(stg.getSize()));
             btnClean.setSelection(stg.getCleanOnRecycle());
             txtVar.setText(stg.getPathEnv());
         } catch (Exception e) {
             PluginUtil.displayErrorDialogAndLog(
            		 getShell(),
            		 Messages.lclStgSetErrTtl,
            		 Messages.lclStgSetErrMsg, e);
         }
     }

     /**
      * Validates the resource name of local storage.
      *
      * @param name : name to be validated.
      * @return retVal : true if name is valid else false
      */
     private boolean isValidName(String name) {
    	 boolean retVal = true;
    	 try {
    		 retVal = LocalStrgResDialogUtilMethods.
    				 isValidName(name, lclStgMap, isResEdit, resName);
    	 } catch (AzureCommonsException e) {
    		 retVal = false;
    		 PluginUtil.displayErrorDialogAndLog(getShell(),
    				 Messages.genErrTitle, e.getMessage(), e);
    	 }
    	 return retVal;
     }

     /**
      * Validates the size of VM.
      *
      * @param size : user entered size
      * @param resName : name of the resource
      * @return isValidSize : true if size is valid else false.
      */
     private boolean isValidSize(String size, String resName) {
         boolean isValidSize = false;
         try {
             int value = Integer.parseInt(size);
             if (value <= 0) {
                 PluginUtil.displayErrorDialog(
                		 getShell(),
                		 Messages.lclStgSizeErrTtl,
                		 Messages.lclStgSizeErrMsg);
                 isValidSize = false;
             } else if (value > maxSize) {
                 boolean choice = MessageDialog.openQuestion(getShell(),
                         Messages.lclStgMxSizeTtl, String.format("%s%s%s",
                                 Messages.lclStgMxSizeMsg1 , maxSize ,
                                 Messages.lclStgMxSizeMsg2));
                 if (choice) {
                	 isValidSize = true;
                 } else {
                	 /*
                	  * If user selects No
                	  * then keep dialog open.
                	  */
                	 isValidSize = false;
                 }
             } else {
                 isValidSize = true;
             }
         } catch (NumberFormatException e) {
        	 PluginUtil.displayErrorDialogAndLog(
        			 getShell(),
        			 Messages.lclStgSizeErrTtl,
        			 Messages.lclStgSizeErrMsg, e);
        	 isValidSize = false;
         }
         return isValidSize;
     }

     /**
      * Validates the environment path.
      *
      * @param path : user given path
      * @return retVal : true if valid path else false
      */
     private boolean isValidPath(String path) {
    	 boolean retVal = true;
    	 try {
    		 retVal = LocalStrgResDialogUtilMethods.
    				 isValidPath(path, lclStgMap, windowsAzureRole);
    	 } catch (AzureCommonsException e) {
    		 retVal = false;
    		 PluginUtil.displayErrorDialogAndLog(
    				 getShell(), Messages.genErrTitle,
    				 e.getMessage(), e);
    	 }
    	 return retVal;
     }

     /**
      * Creates the resource name component.
      *
      * @param container : parent container
      */
     private void createResName(Composite container) {
         Label lblResource = new Label(container, SWT.LEFT);
         GridData gridData = new GridData();
         gridData.horizontalIndent = 10;
         lblResource.setText(Messages.lclStrResLbl);
         lblResource.setLayoutData(gridData);
         txtResource = new Text(container, SWT.LEFT | SWT.BORDER);
         gridData = new GridData();
         gridData.horizontalAlignment = SWT.END;
         gridData.horizontalIndent = 130;
         gridData.horizontalAlignment = SWT.FILL;
         gridData.widthHint = 180;
         gridData.grabExcessHorizontalSpace = true;
         txtResource.setLayoutData(gridData);
         txtResource.addModifyListener(new ModifyListener() {
             @Override
             public void modifyText(ModifyEvent arg0) {
                 if (txtResource.getText().isEmpty()
                		 || txtSize.getText().isEmpty()) {
                     if (okButton != null) {
                     okButton.setEnabled(false);
                     }
                 } else {
                     if (!isResEdit) {
                         String path = txtResource.getText().
                        		 trim().replaceAll("[\\s]+", "_");
                         txtVar.setText(String.format("%s%s",
                        		 path, Messages.lclStgPathStr));
                     }
                     if (okButton != null) {
                         okButton.setEnabled(true);
                     }
                 }
             }
         });
     }

     /**
      * Creates the size component.
      *
      * @param container : parent container
      */
     private void createSizeComponent(Composite container) {
         Label lblSize = new Label(container, SWT.LEFT);
         GridData gridData = new GridData();
         gridData.horizontalIndent = 10;
         gridData.verticalIndent = 10;
         lblSize.setText(Messages.lclStrSizeLbl);
         lblSize.setLayoutData(gridData);
         txtSize = new Text(container, SWT.LEFT | SWT.BORDER);
         gridData = new GridData();
         gridData.horizontalAlignment = SWT.END;
         gridData.horizontalIndent = 130;
         gridData.widthHint = 180;
         gridData.horizontalAlignment = SWT.FILL;
         gridData.grabExcessHorizontalSpace = true;
         txtSize.setLayoutData(gridData);
         txtSize.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent arg0) {
                if (txtSize.getText().isEmpty()
                		|| txtResource.getText().isEmpty()) {
                    if (okButton != null) {
                        okButton.setEnabled(false);
                        }
                } else {
                    if (okButton != null) {
                    okButton.setEnabled(true);
                    }
                }
            }
        });
     }

     /**
      * Create note component.
      *
      * @param container : parent container
      */
     private void createNote(Composite container) {
         Label lblNote = new Label(container, SWT.NONE);
         GridData gridData = new GridData();
         gridData.horizontalSpan = 2;
         gridData.horizontalAlignment = SWT.END;
         gridData.grabExcessHorizontalSpace = true;
         lblNote.setText(String.format("%s%s%s%s",
        		 Messages.rangeNote1, " ",
        		 maxSize, Messages.rangeNote2));
         lblNote.setLayoutData(gridData);

     }

     /**
      * Create clean on recycle component.
      *
      * @param container : parent container
      */
     private void createCleanBtn(Composite container) {
         btnClean = new Button(container, SWT.CHECK);
         GridData gridData = new GridData();
         gridData.horizontalSpan = 2;
         gridData.verticalIndent = 10;
         gridData.horizontalIndent = 10;
         gridData.horizontalAlignment = SWT.BEGINNING;
         gridData.grabExcessHorizontalSpace = true;
         btnClean.setText(Messages.lclStrCleanLbl);
         btnClean.setLayoutData(gridData);
     }

     /**
      * Create environment path component.
      *
      * @param container : parent container
      */
     private void createPathComponent(Composite container) {
         Label lblVar = new Label(container, SWT.LEFT);
         GridData gridData = new GridData();
         gridData.horizontalSpan = 2;
         gridData.verticalIndent = 10;
         gridData.horizontalIndent = 10;
         gridData.horizontalAlignment = SWT.BEGINNING;
         gridData.grabExcessHorizontalSpace = true;
         lblVar.setText(Messages.lclStrVarlbl);
         lblVar.setLayoutData(gridData);

         txtVar = new Text(container, SWT.LEFT | SWT.BORDER);
         gridData = new GridData();
         gridData.horizontalSpan = 2;
         gridData.horizontalIndent = 10;
         gridData.widthHint = 180;
         gridData.horizontalAlignment = SWT.FILL;
         gridData.grabExcessHorizontalSpace = true;
         txtVar.setLayoutData(gridData);
     }
}
