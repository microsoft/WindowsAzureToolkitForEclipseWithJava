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
package com.persistent.ui.propertypage;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import waeclipseplugin.Activator;

import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.microsoftopentechnologies.azurecommons.exception.AzureCommonsException;
import com.microsoftopentechnologies.azurecommons.propertypage.RemoteAccess;
import com.microsoftopentechnologies.azurecommons.util.WAEclipseHelperMethods;
import com.microsoftopentechnologies.wacommon.commoncontrols.NewCertificateDialog;
import com.microsoftopentechnologies.wacommon.commoncontrols.NewCertificateDialogData;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
import com.microsoftopentechnologies.azurecommons.wacommonutil.CerPfxUtil;
import com.microsoftopentechnologies.azurecommons.wacommonutil.EncUtilHelper;
import com.persistent.util.WAEclipseHelper;


public class WARemoteAccessPropertyPage extends PropertyPage {

    private Label userNameLabel;
    private Label passwordLabel;
    private Label confirmPwdLbl;
    private Label expiryDateLabel;
    private Text txtUserName;
    private Text txtPassword;
    private Text txtConfirmPwd;
    private Text txtExpiryDate;
    private Text txtPath;
    private Label pathLabel;
    private Label noteLabel;
    private String errorTitle;
    private String errorMessage;
    private WindowsAzureProjectManager waProjManager;
    private IProject selProject;
    private Button remoteChkBtn;
    private Button newButton;
    private Button workspaceButton;
    private Button fileSystemButton;
    private Button cal;
    private boolean isInconsistent;
    private static final String BASE_PATH = "${basedir}";
    private static final String DATE_SEP = "/";
    private boolean isPwdChanged;
    private boolean isPageDisplayed = false;
    /**
     * Variable to track, if came to remote access page from
     * Publish wizard's Encryption link.
     */
    private boolean isFrmEncLink = false;

    /**
     * This method creates the control or contents for the remote access page.
     * @param parent : composite type
     * @return :- control object
     */
    @Override
    protected Control createContents(Composite parent) {
        noDefaultAndApplyButton();
        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
                "com.persistent.winazure.eclipseplugin."
                + "windows_azure_project_remote_access_property");
        loadProject();
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        parent.setLayout(gridLayout);
        //create remote desktop check button
        createRemoteDesktopCheckBtn(parent);

        final Composite container = new Composite(parent, SWT.NONE);
        gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.widthHint = 550;
        gridData.horizontalAlignment = SWT.FILL;
        container.setLayout(gridLayout);
        container.setLayoutData(gridData);

        //user name text box and its grid data.
        createUsernameComponent(container);
        //password text box and its grid data.
        createPasswordComponent(container);
        //confirm password text box and its grid data
        createConfPwdComponent(container);
        // expiration date and its grid data
        final Composite dateContainer = new Composite(parent, SWT.NONE);
        GridLayout dateGridLayout = new GridLayout();
        dateGridLayout.numColumns = 3;
        GridData dateGridData = new GridData();
        dateGridData.widthHint = 200;
        dateGridData.horizontalSpan = 2;
        dateGridData.grabExcessHorizontalSpace = true;
        dateContainer.setLayout(dateGridLayout);
        dateContainer.setLayoutData(dateGridData);
        dateGridLayout = new GridLayout();
        dateGridLayout.numColumns = 3;
        dateGridData = new GridData();
        dateGridData.widthHint = 150;
        expiryDateLabel = new Label(container, SWT.LEFT);
        expiryDateLabel.setText(Messages.remAccExpDate);
        txtExpiryDate = new Text(container, SWT.LEFT
                | SWT.CALENDAR | SWT.BORDER);
        txtExpiryDate.setLayoutData(dateGridData);
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    Messages.remAccDateFormat, Locale.getDefault());
            Date date = waProjManager.
            getRemoteAccessAccountExpiration();
            if (date == null) {
                GregorianCalendar currentCal = new GregorianCalendar();
                currentCal.add(Calendar.YEAR, 1);
                Date today = currentCal.getTime();
                if (txtExpiryDate.isEnabled()) {
                    txtExpiryDate.setText(dateFormat.format(today).toString());
                }
            } else {
                String newDate = dateFormat.format(date);
                txtExpiryDate.setText(newDate);
            }
        } catch (WindowsAzureInvalidProjectOperationException e1) {
            txtExpiryDate.setText("");
            remoteChkBtn.setSelection(false);
            /*
             * When user data is not consistent we are making
             * isInconsistent as true and later on we are checking the status
             * of this variable and throwing the error message to user.
             */
            isInconsistent = true;
            Activator.getDefault().log(Messages.remAccErExpDate,
                    e1);
        }
        //button for calendar picker
        cal = new Button(container, SWT.PUSH | SWT.CENTER);
        cal.setText("...");
        cal.addSelectionListener(new CalenderListener());

        // group for the path and other variables
        createGroupCertPath(parent);

        /*
         * Check if we are coming from Publish wizard link,
         */
        if (Activator.getDefault().getIsFromEncLink()) {
        	String uname = Activator.getDefault().getPubUname();
        	if (uname.isEmpty()) {
        		// disable remote access
        		remoteChkBtn.setSelection(false);
        		makeAllTextBlank();
        	} else {
        		String pwd = Activator.getDefault().getPubPwd();
        		String cnfPwd = Activator.getDefault().getPubCnfPwd();
        		/*
        		 * enable remote access and
        		 * show values given on publish wizard
        		 */
        		remoteChkBtn.setSelection(true);
        		txtUserName.setText(uname);
        		txtPassword.setText(pwd);
        		txtConfirmPwd.setText(cnfPwd);
        		try {
        			if (!waProjManager.
        					getRemoteAccessEncryptedPassword().
        					equals(pwd)) {
        				isPwdChanged = true;
        			}
        		} catch (WindowsAzureInvalidProjectOperationException e) {
        			Activator.getDefault().log(Messages.remAccErPwd, e);
        		}
        		isFrmEncLink =  true;
        	}
        	Activator.getDefault().setIsFromEncLink(false);
        } else {
        	if (remoteChkBtn.getSelection()) {
        		getDefaultValues();
        	} else {
        		makeAllTextBlank();
        	}
        }
        setComponentStatus(remoteChkBtn.getSelection());

        /*
         * Here we are checking the isInconsistent value
         * and showing the error message to user on UI.
         */
        if (isInconsistent) {
            PluginUtil.displayErrorDialog(this.getShell(),
            		Messages.remAccErTxtTitle,
            		Messages.remAccDataInc);
        }
        /*
         * Non windows OS then disable components,
         * but keep values as it is
         */
        if (!Activator.IS_WINDOWS) {
        	setComponentStatus(false);
        	if (!remoteChkBtn.getSelection()) {
        		remoteChkBtn.setEnabled(false);
        	}
        }
        isPageDisplayed = true;
        return parent;
    }

    /**
     * Creates remote desktop check button.
     *
     * @param parent
     */
    private void createRemoteDesktopCheckBtn(Composite parent) {
        remoteChkBtn = new Button(parent, SWT.CHECK);
        boolean isEnabled = false;
        try {
            isEnabled = waProjManager.getRemoteAccessAllRoles();
        } catch (WindowsAzureInvalidProjectOperationException e2) {
            PluginUtil.displayErrorDialogAndLog(getShell(),
            		Messages.remAccErrTitle,
            		Messages.remAccErAllRoles, e2);
        }
        //grid data for remote check box.
        GridData gridData = new GridData();
        gridData.horizontalSpan = 2;
        remoteChkBtn.setLayoutData(gridData);
        remoteChkBtn.setText(Messages.remAccChkBoxTxt);
        remoteChkBtn.setSelection(isEnabled);

        remoteChkBtn.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                setComponentStatus(remoteChkBtn.getSelection());
                if (remoteChkBtn.getSelection()) {
                    getDefaultValues();
                } else {
                    makeAllTextBlank();
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
            } });
    }

    /**
     * Creates label and text box for username.
     *
     * @param container
     */
    private void createUsernameComponent(Composite container) {
        userNameLabel = new Label(container, SWT.LEFT);
        userNameLabel.setText(Messages.remAccUserName);
        txtUserName = new Text(container, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
        GridData gridData = new GridData();
        gridData.widthHint = 150;
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalSpan = 2;
        txtUserName.setLayoutData(gridData);
        try {
            txtUserName.setText(waProjManager.
                    getRemoteAccessUsername());
        } catch (WindowsAzureInvalidProjectOperationException e1) {
            txtUserName.setText("");
            remoteChkBtn.setSelection(false);
            /*
             * When user data is not consistent we are making
             * isInconsistent as true and later on we are checking the status
             * of this variable and throwing the error message to user.
             */
            isInconsistent = true;
            Activator.getDefault().log(Messages.remAccErUserName, e1);
        }
    }

    /**
     * Creates label and text box for password.
     *
     * @param container
     */
    private void createPasswordComponent(Composite container) {
        passwordLabel = new Label(container, SWT.LEFT);
        passwordLabel.setText(Messages.remAccPassword);
        txtPassword = new Text(container, SWT.LEFT
                | SWT.PASSWORD | SWT.BORDER);
        GridData gridData = new GridData();
        gridData.widthHint = 150;
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalSpan = 2;
        txtPassword.setLayoutData(gridData);
        /*
         * Listener for key event when user click on password text box
         * it will set flag for entering the new values.
         */
        txtPassword.addKeyListener(new KeyListener() {
            @Override
            public void keyReleased(KeyEvent event) {
            }

            @Override
            public void keyPressed(KeyEvent event) {
                isPwdChanged = true;
            }
        });

        /*
         * Listener for handling focus event on password text box on focus gain
         * text box will blank.on focus lost we will be checking for strong
         * password. If password has not changed then we will display old
         * password only.
         */
        txtPassword.addFocusListener(new PasswordFocusListener());

        try {
            txtPassword.setText(waProjManager.
                    getRemoteAccessEncryptedPassword());
        } catch (WindowsAzureInvalidProjectOperationException e1) {
            txtPassword.setText("");
            remoteChkBtn.setSelection(false);
            /*
             * When user data is not consistent we are making
             * isInconsistent as true and later on we are checking the status
             * of this variable and throwing the error message to user.
             */
            isInconsistent = true;
            Activator.getDefault().log(Messages.remAccErPwd, e1);
        }
    }

    /**
     * Creates label and text box for confirm password.
     *
     * @param container
     */
    private void createConfPwdComponent(Composite container) {
        confirmPwdLbl = new Label(container, SWT.LEFT);
        confirmPwdLbl.setText(Messages.remAccConfirmPwd);
        txtConfirmPwd = new Text(container, SWT.LEFT
                | SWT.PASSWORD | SWT.BORDER);
        GridData gridData = new GridData();
        gridData.widthHint = 150;
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalSpan = 2;
        txtConfirmPwd.setLayoutData(gridData);
        txtConfirmPwd.addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent event) {
                try {
                    if (!isPwdChanged) {
                        if (txtPassword.getText().isEmpty()) {
                            txtConfirmPwd.setText("");
                        } else {
                            txtConfirmPwd.setText(waProjManager.
                                    getRemoteAccessEncryptedPassword());
                        }
                    }
                } catch (WindowsAzureInvalidProjectOperationException e1) {
                    PluginUtil.displayErrorDialogAndLog(getShell(),
                    		Messages.remAccErrTitle,
                    		Messages.remAccErPwd, e1);
                }
            }

            @Override
            public void focusGained(FocusEvent event) {
                txtConfirmPwd.setText("");
            }
        });

        try {
            txtConfirmPwd.setText(waProjManager.
                    getRemoteAccessEncryptedPassword());
        } catch (WindowsAzureInvalidProjectOperationException e1) {
            txtConfirmPwd.setText("");
            remoteChkBtn.setSelection(false);
            /*
             * When user data is not consistent we are making
             * isInconsistent as true and later on we are checking the status
             * of this variable and throwing the error message to user.
             */
            isInconsistent = true;
            Activator.getDefault().log(Messages.remAccErPwd, e1);
        }
    }

    /**
     * Creates group for certificate path and related components.
     *
     * @param parent
     */
    private void createGroupCertPath(Composite parent) {
        Group group =  new Group(parent, SWT.SHADOW_ETCHED_IN);
        GridLayout groupGridLayout = new GridLayout();
        GridData groupGridData = new GridData();
        groupGridData.grabExcessHorizontalSpace = true;
        groupGridLayout.numColumns = 5;
        group.setText(Messages.remAccGrpTxt);
        group.setLayout(groupGridLayout);
        group.setLayoutData(groupGridData);
        pathLabel = new Label(group, SWT.LEFT);
        groupGridData = new GridData();
        pathLabel.setText(Messages.remAccPath);
        pathLabel.setLayoutData(groupGridData);
        txtPath = new Text(group, SWT.BORDER);
        groupGridData = new GridData();
        groupGridData.widthHint = 400;
        groupGridData.horizontalIndent = 70;
        groupGridData.horizontalSpan = 4;
        groupGridData.grabExcessHorizontalSpace = true;
        groupGridData.horizontalAlignment = SWT.FILL;
        txtPath.setLayoutData(groupGridData);
        try {
            txtPath.setText(waProjManager.
                    getRemoteAccessCertificatePath());
        } catch (WindowsAzureInvalidProjectOperationException e1) {
            txtPath.setText("");
            remoteChkBtn.setSelection(false);
            /*
             * When user data is not consistent we are making
             * isInconsistent as true and later on we are checking the status
             * of this variable and throwing the error message to user.
             */
            isInconsistent = true;
            Activator.getDefault().log(Messages.remAccErCertPath, e1);
        }

        groupGridData = new GridData();
        groupGridData.horizontalIndent = 210;
        groupGridData.horizontalSpan = 2;
        groupGridData.widthHint = 100;
        groupGridData.grabExcessHorizontalSpace = true;
        groupGridData.horizontalAlignment = SWT.FILL;
        newButton = new Button(group, SWT.PUSH | SWT.CENTER);
        newButton.setText(Messages
                .remAccNewBtn);
        newButton.setLayoutData(groupGridData);
        newButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                newBtnListener();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });

        groupGridData = new GridData();
        groupGridData.widthHint = 100;
        groupGridData.grabExcessHorizontalSpace = true;
        groupGridData.horizontalAlignment = SWT.FILL;
        workspaceButton = new Button(group, SWT.PUSH | SWT.CENTER);
        workspaceButton.setText(Messages.remAccWkspcBtn);
        workspaceButton.setLayoutData(groupGridData);
        workspaceButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                workspaceBtnListener();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });

        groupGridData = new GridData();
        groupGridData.widthHint = 100;
        groupGridData.grabExcessHorizontalSpace = true;
        groupGridData.horizontalAlignment = SWT.FILL;
        fileSystemButton = new Button(group, SWT.PUSH | SWT.CENTER);
        fileSystemButton.setText(Messages.remAccFileSysBtn);
        fileSystemButton.setLayoutData(groupGridData);
        fileSystemButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                browseBtnListener();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });

        noteLabel = new Label(group, SWT.NONE);
        groupGridData = new GridData();
        groupGridData.horizontalSpan = 5;
        noteLabel.setText(Messages.remAccNote);
        noteLabel.setLayoutData(groupGridData);

    }

    /**
     * Listener for new button.
     */
    protected void newBtnListener() {
    	NewCertificateDialogData data = new NewCertificateDialogData();
    	
        NewCertificateDialog dialog =
        		new NewCertificateDialog(getShell(), data,
        				WAEclipseHelperMethods.findJdkPathFromRole(waProjManager));

        int returnCode = dialog.open();
        if (returnCode == Window.OK) {
        		String certPath = data.getCerFilePath();
        		if (certPath != null
        				&& certPath.contains(selProject.getLocation()
                        .toOSString() + File.separator)) {
                    String workspacePath = selProject.getLocation()
                    .toOSString();
                    String replaceString = certPath;
                    String subString = certPath .
                    substring(certPath.indexOf(workspacePath),
                            workspacePath.length());
                    replaceString = replaceString.replace(subString, BASE_PATH);
                    txtPath.setText(replaceString);
                } else {
                    txtPath.setText(certPath);
                }
        }
    }
    
    /**
     * Listener for workspace button.
     */
    protected void workspaceBtnListener() {
        ElementTreeSelectionDialog dialog =
                new ElementTreeSelectionDialog(
                        getShell(), new WorkbenchLabelProvider()
                        , new WorkbenchContentProvider());
        dialog.setInput(ResourcesPlugin.
                getWorkspace().getRoot());
        dialog.setTitle(Messages.remAccWrkspcTitle);
        dialog.addFilter(new ViewerFilter() {

            @Override
            public boolean select(Viewer arg0,
            		Object arg1, Object arg2) {
                if (arg2 instanceof IProject) {
                    return ((IProject) arg2).isOpen();
                } else if (arg2 instanceof IFile) {
                    boolean ext = ((IFile) arg2).getName().endsWith(".cer");
                     if (!ext) {
                         return false;
                     }
                }
                return true;
            }
        });

        dialog.setAllowMultiple(false);
        dialog.open();
        Object[] obj = dialog.getResult();
        if (obj != null && obj.length > 0) {
            if (obj[0] instanceof IFile) {
                IFile file = (IFile) obj[0];
                String exactPath = file.getLocation().toOSString();
                if (exactPath.contains(selProject.getLocation().toOSString()
                        + File.separator)) {
                    String workspacePath =
                    		selProject.getLocation().toOSString();
                    String replaceString = exactPath;
                    String subString = exactPath .
                    substring(exactPath.indexOf(workspacePath),
                            workspacePath.length());
                    replaceString = replaceString.replace(subString,
                            BASE_PATH);
                    txtPath.setText(replaceString);
                } else {
                    txtPath.setText(exactPath);
                }
            } else {
            	PluginUtil.displayErrorDialog(getShell(),
            			Messages.certDlgWrongTitle,
            			Messages.remAccWkspWrngSel);
            	txtPath.setText("");
            }
        }
    }

    /**
     * This method is used for setting certificate expiration date.
     *
     * @param edate Expiration date
     */
    private void setExpiryDate(String edate) {
        txtExpiryDate.setText(edate);
    }

    /**
     * Listener for browse button it is used in file system button.
     * It will open the file system location for storing the certificate file.
     */
    protected void browseBtnListener() {
        FileDialog dialog = new FileDialog(this.getShell());
        String [] extensions = {"*.cer", "*.CER"};
        dialog.setText(Messages.certDlgBrowFldr);
        dialog.setFilterExtensions(extensions);
        String path 		= selProject.getLocation().toPortableString();
        // Default directory should be the cert directory in the project, and if it 
        // doesn't exist, then it should be the project directory
        String certPath		= path + File.separator + "cert";
        if(new File(certPath).exists())
        	dialog.setFilterPath(certPath);
        else
        	dialog.setFilterPath(path);
        /*
         * When we use tab to traverse through controls,
         * focus goes to last selected control i.e password fields.
         * To avoid that explicitly setting focus on cert path text box.
         */
        txtPath.setFocus();
        String directory = dialog.open();
        if (directory != null) {
            if (directory.contains(selProject.getLocation().toOSString()
            		+ File.separator)) {
                String workspacePath = selProject.getLocation().toOSString();
                String replaceString = directory;
                String subString = directory .
                substring(directory.indexOf(workspacePath),
                        workspacePath.length());
                replaceString = replaceString.replace(subString, BASE_PATH);
                txtPath.setText(replaceString);
            } else {
                txtPath.setText(directory);
            }
        }
    }

    /**
     * This method loads the projects available in workspace.
     * selProject variable will contain value of current selected project.
     */
    private void loadProject() {
    	selProject = WAEclipseHelper.getSelectedProject();
    	String path = selProject.getLocation().toPortableString();
    	File projDirPath = new File(path);
        try {
            waProjManager = WindowsAzureProjectManager.
            load(projDirPath);
        } catch (Exception e) {
            errorTitle = Messages.remAccSyntaxErr;
            errorMessage = Messages.proPageErrMsgBox1
            + Messages.proPageErrMsgBox2;
            PluginUtil.displayErrorDialog(this.getShell(), errorTitle,
                    errorMessage);
            Activator.getDefault().log(Messages.remAccErProjLoad, e);
        }
    }

    @Override
    public boolean okToLeave() {
        boolean okToProceed = true;
        String userName = txtUserName.getText();
        String newPath = txtPath.getText();
        String expDate = txtExpiryDate.getText();
        if (newPath.startsWith(BASE_PATH)) {
            newPath = newPath.substring(newPath.indexOf("}") + 1
                    , newPath.length());
            newPath = String.format("%s%s",
            		selProject.getLocation().toOSString(), newPath);
        }
        try {
			RemoteAccess.okToLeave(newPath, remoteChkBtn.getSelection(), userName, expDate);
			setErrorMessage(null);
		} catch (AzureCommonsException e) {
			setErrorMessage(e.getMessage());
            okToProceed = false;
		}

        boolean retVal = false;
        if (okToProceed) {
            retVal = super.okToLeave();
        }
        return retVal;
    }

    /**
     * Method specifies action to be executed when OK button is pressed.
     */
    public boolean performOk() {
    	if (!isPageDisplayed) {
    		return super.performOk();
    	}
        try {
            loadProject();
            if (remoteChkBtn.getSelection()) {
                waProjManager.setRemoteAccessAllRoles(true);
                String userName = txtUserName.getText();
                String pwd = txtPassword.getText();
                String cnfPwd = txtConfirmPwd.getText();
                String newPath = txtPath.getText();
                String expDate = txtExpiryDate.getText();
                String tempPath = newPath;
                boolean isPathChanged = false;
                DateFormat formatter = new SimpleDateFormat(
                        Messages.remAccDateFormat, Locale.getDefault());
                if (userName == null || userName.equalsIgnoreCase("")) {
                    PluginUtil.displayErrorDialog(this.getShell(),
                    		Messages.remAccErTxtTitle,
                    		Messages.remAccNameNull);
                    return false;
                } else {
                    waProjManager.setRemoteAccessUsername(userName);
                }
                if (!newPath.equals(waProjManager.
                        getRemoteAccessCertificatePath())
                        && !newPath.isEmpty()) {
                    isPathChanged = true;
                    /*
                     * check If certificate file path has changed,
                     * If yes then prompt user
                     * for changing the password as well,
                     * if that is not changed.
                     * Because we have to encrypt the new password
                     * and then we will generate certificate
                     * based on that.
                     * Case 1 :- If user has changed the path
                     * and password is old then it
                     * will prompt for new password or re-enter the password.
                     * If user changes the password
                     * then it will generate certificate based
                     * on that new password.
                     * Case 2 :- If user set the blank password
                     * even after displaying that
                     * password change prompt, in that case
                     * we will display warning messages
                     * to user that whether he want to continue
                     * with empty password, If yes
                     * then we will consider that blank password
                     * else use will have to enter
                     * new password.
                     */
                    if (pwd.equals(waProjManager.
                    		getRemoteAccessEncryptedPassword())
                            && !pwd.isEmpty()) {
                        txtPassword.setText("");
                        txtConfirmPwd.setText("");
                        PluginUtil.displayErrorDialog(this.getShell(),
                        		Messages.remAccErTxtTitle,
                        		Messages.remAccPwdMstChng);
                        return false;
                    }
                }
                if (pwd.isEmpty()) {
                     boolean choice = MessageDialog.openQuestion(getShell(),
                             Messages.remAccErTxtTitle, Messages.remAccWarnPwd);
                    if (!choice) {
                        return false;
                    }
                }
                if (expDate == null || expDate.equalsIgnoreCase("")) {
                    PluginUtil.displayErrorDialog(this.getShell(),
                    		Messages.remAccErTxtTitle,
                    		Messages.remAccExpDateNull);
                    return false;
                } else {
                    boolean status =  validateExpDate(expDate, formatter);
                    if (!status) {
                        return false;
                    }
                }
                if (newPath == null || newPath.equalsIgnoreCase("")) {
                    PluginUtil.displayErrorDialog(this.getShell(),
                    		Messages.remAccErTxtTitle,
                    		Messages.remAccPathNull);
                    return false;
                }
                /*
                 * Check for displaying the relative path
                 * in case when user select the certificate file path
                 * as workspace or of current project.
                 * We will be showing relative path in that case on UI.
                 */
                if (tempPath.startsWith(BASE_PATH)) {
                    tempPath = tempPath.substring(tempPath.indexOf("}") + 1
                            , tempPath.length());
                    tempPath = String.format("%s%s",
                    		selProject.getLocation().toOSString(),
                    		tempPath);
                }
                File file = new File(tempPath);
                //if path is not correct.display error message for that.
                if (file.exists() && tempPath.endsWith(".cer")) {
                	waProjManager.setRemoteAccessCertificatePath(newPath);
                } else {
                    PluginUtil.displayErrorDialog(this.getShell(),
                    		Messages.remAccErTxtTitle,
                    		Messages.remAccInvldPath);
                    return false;
                }
                try {
                    if (isPathChanged) {
                    	String thumbprint =
                    			CerPfxUtil.getThumbPrint(tempPath);
                    	if (waProjManager.isRemoteAccessTryingToUseSSLCert(thumbprint)) {
                    		PluginUtil.displayErrorDialog(getShell(),
                    				Messages.remAccSyntaxErr,
                    				Messages.usedBySSL);
                    		return false;
                    	} else {
                    		waProjManager.
                    		setRemoteAccessCertificateFingerprint(thumbprint);
                    	}
                    }
                } catch (Exception e) {
                    PluginUtil.displayErrorDialogAndLog(this.getShell(),
                    		Messages.remAccSyntaxErr,
                    		Messages.remAccErTmbPrint, e);
                    return false;
                }
                if (cnfPwd.equals(pwd)) {
                    try {
                        /*
                         * Encrypting the password
                         * if it is not dummy & blank from xml
                         * and isPwdChanged is true that means
                         * user has changes the password.
                         */
                        String modifiedPwd = Messages.remAccDummyPwd;
                        if (!pwd.equals(modifiedPwd)
                        		&& !pwd.isEmpty()
                        		&& isPwdChanged) {
                            String encryptedPwd =
                            		EncUtilHelper.encryptPassword(pwd,
                            				tempPath, PluginUtil.getEncPath());
                            waProjManager.
                            setRemoteAccessEncryptedPassword(encryptedPwd);
                        } else {
                            waProjManager.setRemoteAccessEncryptedPassword(pwd);
                        }
                    } catch (Exception e) {
                        PluginUtil.displayErrorDialogAndLog(getShell(),
                        		Messages.remAccSyntaxErr,
                        		Messages.remAccErPwd, e);
                        return false;
                    }
                } else {
                    PluginUtil.displayErrorDialog(this.getShell(),
                    		Messages.remAccErTxtTitle,
                    		Messages.remAccPwdNotMatch);
                    return false;
                }
            } else {
                waProjManager.setRemoteAccessAllRoles(false);
            }
            waProjManager.save();
        } catch (WindowsAzureInvalidProjectOperationException e) {
            errorTitle = Messages.remAccSyntaxErr;
            errorMessage = Messages.proPageErrMsgBox1
            + Messages.proPageErrMsgBox2;
            PluginUtil.displayErrorDialog(this.getShell(), errorTitle,
                    errorMessage);
            Activator.getDefault().log(Messages.remAccErConfigErr, e);
        } catch (ParseException e) {
            PluginUtil.displayErrorDialogAndLog(getShell(),
            		Messages.remAccErrTitle,
            		Messages.remAccErDateParse, e);
            return false;
        }
        WAEclipseHelper.refreshWorkspace(
        		Messages.remAccWarning, Messages.remAccWarnMsg);
        isFrmEncLink = false;
        return super.performOk();
    }

    /**
     * This method will disable/enable all fields based on
     * the remote check button status.
     *
     * @param status : status of the remote check check box.
     */
    private void setComponentStatus(boolean status) {
        userNameLabel.setEnabled(status);
        passwordLabel.setEnabled(status);
        confirmPwdLbl.setEnabled(status);
        expiryDateLabel.setEnabled(status);
        txtUserName.setEnabled(status);
        txtPassword.setEnabled(status);
        txtConfirmPwd.setEnabled(status);
        txtExpiryDate.setEnabled(status);
        txtPath.setEnabled(status);
        pathLabel.setEnabled(status);
        noteLabel.setEnabled(status);
        newButton.setEnabled(status);
        workspaceButton.setEnabled(status);
        fileSystemButton.setEnabled(status);
        cal.setEnabled(status);
    }

    /**
     * This method will set all fields to blank,
     * if remote check button is disabled.
     */
    private void makeAllTextBlank() {
        txtUserName.setText("");
        txtPassword.setText("");
        txtConfirmPwd.setText("");
        txtExpiryDate.setText("");
        txtPath.setText("");
    }

    /**
     *  This method will set default values to all fields.
     */
    private void getDefaultValues() {
        try {
            txtUserName.setText(waProjManager.
                    getRemoteAccessUsername());
        } catch (WindowsAzureInvalidProjectOperationException e) {
            PluginUtil.displayErrorDialogAndLog(getShell(),
            		Messages.remAccErrTitle,
            		Messages.remAccErUserName, e);
        }
        try {
            txtPassword.setText(waProjManager.
                    getRemoteAccessEncryptedPassword());
        } catch (WindowsAzureInvalidProjectOperationException e) {

        }
        try {
            txtConfirmPwd.setText(waProjManager.
                    getRemoteAccessEncryptedPassword());
        } catch (WindowsAzureInvalidProjectOperationException e) {
        	PluginUtil.displayErrorDialogAndLog(getShell(),
            		Messages.remAccErrTitle,
            		Messages.remAccErPwd, e);
        }
        try {
            txtPath.setText(waProjManager.
                    getRemoteAccessCertificatePath());
        } catch (WindowsAzureInvalidProjectOperationException e) {
            PluginUtil.displayErrorDialogAndLog(getShell(),
            		Messages.remAccErrTitle,
            		Messages.remAccErCertPath, e);
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                Messages.remAccDateFormat, Locale.getDefault());
        Date date;
        try {
            date = waProjManager.
            getRemoteAccessAccountExpiration();
            if (date == null) {
                GregorianCalendar currentCal = new GregorianCalendar();
                currentCal.add(Calendar.YEAR, 1);
                Date today = currentCal.getTime();
                if (txtExpiryDate.isEnabled()) {
                    txtExpiryDate.setText(dateFormat.format(today).toString());
                }
            } else {
                String newDate = dateFormat.format(date);
                txtExpiryDate.setText(newDate);
            }
        } catch (WindowsAzureInvalidProjectOperationException e) {
            PluginUtil.displayErrorDialogAndLog(getShell(),
            		Messages.remAccErrTitle,
            		Messages.remAccErExpDate, e);
        }
    }

    /**
     * Validates the expiry date.
     * Expiry date should be greater than current date.
     *
     * @param expDate
     * @param formatter
     * @return
     * @throws ParseException
     * @throws WindowsAzureInvalidProjectOperationException
     */
    private boolean validateExpDate(String expDate, DateFormat formatter)
    		throws ParseException,
    		WindowsAzureInvalidProjectOperationException {
    	boolean isValid = RemoteAccess.validateExpDate(expDate, formatter);
    	if (isValid) {
    		waProjManager
    		.setRemoteAccessAccountExpiration(formatter
    				.parse(expDate));
    	} else {
    		PluginUtil.displayErrorDialog(getShell(),
    				Messages.remAccErTxtTitle,
    				Messages.remAccDateWrong);
    	}
    	return isValid;
    }

    /**
     * Focus listener for password text box.
     *
     */
    private class PasswordFocusListener implements FocusListener {

        @Override
        public void focusLost(FocusEvent event) {
        	if (isFrmEncLink) {
        		WAEclipseHelper.checkRdpPwd(isPwdChanged, txtPassword,
        				waProjManager, false, txtConfirmPwd);
        	} else {
        		WAEclipseHelper.checkRdpPwd(isPwdChanged, txtPassword,
        				waProjManager, true, txtConfirmPwd);
        	}
        }

        @Override
        //making text box blank on focus gained.
        public void focusGained(FocusEvent event) {
            txtPassword.setText("");
        }
    }

    /**
     * Selection listener for date chooser button.
     *
     */
    private class CalenderListener extends SelectionAdapter {
        public void widgetSelected(SelectionEvent event) {
            final Shell shell = new
            Shell(getShell(), SWT.DIALOG_TRIM);
            shell.setText(Messages.remAccSelExpDate);
            shell.setLayout(new GridLayout(1, false));
            final DateTime calPick = new
            DateTime(shell, SWT.CALENDAR | SWT.BORDER);
            new Label(shell, SWT.NONE);
            new Label(shell, SWT.NONE);
            Button done = new Button(shell, SWT.PUSH);
            done.setText("OK");
            GridData gridData = new GridData();
            gridData.horizontalAlignment = SWT.CENTER;
            gridData.widthHint = 100;
            done.setLayoutData(gridData);
            done.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent event) {
                    int calMonth, calYear, calDay;
                    calDay = calPick.getDay();
                    calMonth = (calPick.getMonth() + 1);
                    calYear = calPick.getYear();
                    String expiryDate = Integer.toString(calMonth)
                    + DATE_SEP + Integer.toString(calDay)
                    + DATE_SEP + Integer.toString(calYear);
                    Date userSelected;
                    long todaySeconds, userDateSeconds;
                    DateFormat formatter = new SimpleDateFormat(
                            Messages.remAccDateFormat, Locale.getDefault());
                    try {
                        userSelected = formatter.parse(expiryDate);
                        userDateSeconds = userSelected.getTime();
                        GregorianCalendar todayCal = new GregorianCalendar();
                        todaySeconds = todayCal.getTimeInMillis();
                        if ((userDateSeconds - todaySeconds) < 0) {
                        	PluginUtil.displayErrorDialog(shell,
                        			Messages.remAccErTxtTitle,
                        			Messages.remAccDateWrong);
                        }
                        else {
                            setExpiryDate(expiryDate);
                            shell.close();
                        }
                    } catch (ParseException e1) {
                        PluginUtil.displayErrorDialogAndLog(getShell(),
                        		Messages.remAccErrTitle,
                        		Messages.remAccErDateParse, e1);
                    }
                }
            });
            shell.setDefaultButton(done);
            shell.open();
            shell.pack();
        }
    }
}
