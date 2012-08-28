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
package com.persistent.ui.propertypage;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.persistent.util.WAEclipseHelper;
import com.persistent.util.EncUtilHelper;
import com.persistent.util.MessageUtil;


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
    private static String pathFrmEncUtil;
    private Button newButton;
    private Button workspaceButton;
    private Button fileSystemButton;
    private Button cal;
    private boolean isInconsistent;
    private static final String BASE_PATH = "${basedir}";
    private static final String DATE_SEP = "/";
    private boolean isPwdChanged;


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
            //When user data is not consistent we are making
            //isInconsistent as true and later on we are checking the status
            //of this variable and throwing the error message to user.
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

        setComponentStatus(remoteChkBtn.getSelection());
        if (remoteChkBtn.getSelection()) {
            getDefaultValues();
        } else {
            makeAllTextBlank();
        }

        //Here we are checking the isInconsistent value and showing the
        //error message to user on UI.
        if (isInconsistent) {
            errorTitle = Messages.remAccErTxtTitle;
            errorMessage = Messages.remAccDataInc;
            MessageUtil.displayErrorDialog(this.getShell(),
                    errorTitle, errorMessage);
        }
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
            errorTitle = Messages.remAccErrTitle;
            errorMessage = Messages.remAccErAllRoles;
            MessageUtil.displayErrorDialog(getShell(),
                    errorTitle, errorMessage);
            Activator.getDefault().log(Messages.remAccErAllRoles,
                    e2);
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
            //When user data is not consistent we are making
            //isInconsistent as true and later on we are checking the status
            //of this variable and throwing the error message to user.
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
        //Listener for key event when user click on password text box
        //it will set flag for entering the new values.
        txtPassword.addKeyListener(new KeyListener() {
            @Override
            public void keyReleased(KeyEvent event) {
            }

            @Override
            public void keyPressed(KeyEvent event) {
                isPwdChanged = true;
            }
        });

        //Listener for handling focus event on password text box on focus gain
        //text box will blank.on focus lost we will be checking for strong
        //password.if password has not changed then we will display old
        //password only.
        txtPassword.addFocusListener(new PasswordFocusListener());

        try {
            txtPassword.setText(waProjManager.
                    getRemoteAccessEncryptedPassword());
        } catch (WindowsAzureInvalidProjectOperationException e1) {
            txtPassword.setText("");
            remoteChkBtn.setSelection(false);
            //When user data is not consistent we are making
            //isInconsistent as true and later on we are checking the status
            //of this variable and throwing the error message to user.
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
                    errorTitle = Messages.remAccErrTitle;
                    errorMessage = Messages.remAccErPwd;
                    MessageUtil.displayErrorDialog(getShell(),
                            errorTitle, errorMessage);
                    Activator.getDefault().log(Messages.remAccErPwd, e1);
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
            //When user data is not consistent we are making
            //isInconsistent as true and later on we are checking the status
            //of this variable and throwing the error message to user.
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
            //When user data is not consistent we are making
            //isInconsistent as true and later on we are checking the status
            //of this variable and throwing the error message to user.
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
        NewCertificateDialog dialog = new
                NewCertificateDialog(getShell());
        int returnCode = dialog.open();
        if (returnCode == Window.OK) {
            if (pathFrmEncUtil.contains(selProject.getLocation()
                    .toOSString() + File.separator)) {
                String workspacePath = selProject.getLocation()
                .toOSString();
                String replaceString = pathFrmEncUtil;
                String subString = pathFrmEncUtil .
                substring(pathFrmEncUtil.indexOf(workspacePath),
                        workspacePath.length());
                replaceString = replaceString.replace(subString, BASE_PATH);
                txtPath.setText(replaceString);
            } else {
                txtPath.setText(pathFrmEncUtil);
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
            public boolean select(Viewer arg0, Object arg1, Object arg2) {
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
                errorTitle = Messages.certDlgWrongTitle;
                errorMessage = Messages.remAccWkspWrngSel;
                MessageUtil.displayErrorDialog(getShell(),
                        errorTitle, errorMessage);
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
        String [] extensions = {"*.cer"};
        dialog.setText(Messages.certDlgBrowFldr);
        dialog.setFilterExtensions(extensions);
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
            MessageUtil.displayErrorDialog(this.getShell(), errorTitle,
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
        File cerFile = new File(newPath);
        boolean isRemoteEnabled = remoteChkBtn.getSelection();
        if (isRemoteEnabled && userName.isEmpty()) {
            setErrorMessage(Messages.remAccNameNull);
            okToProceed = false;
        } else if (isRemoteEnabled && expDate.isEmpty()) {
            setErrorMessage(Messages.remAccExpDateNull);
            okToProceed = false;
        } else if (isRemoteEnabled
                && (!cerFile.exists() || (!newPath.endsWith(".cer")))) {
            setErrorMessage(Messages.remAccInvldPath);
            okToProceed = false;
        } else {
            setErrorMessage(null);
        }

        boolean retVal = false;
        if (okToProceed) {
            retVal = super.okToLeave();
        }
        return retVal;
    }

    public boolean performOk() {
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
                    errorTitle = Messages.remAccErTxtTitle;
                    errorMessage = Messages.remAccNameNull;
                    MessageUtil.displayErrorDialog(this.getShell(),
                            errorTitle, errorMessage);
                    return false;
                } else {
                    waProjManager.setRemoteAccessUsername(userName);
                }
                if (!newPath.equals(waProjManager.
                        getRemoteAccessCertificatePath()) && !newPath.isEmpty()) {
                    isPathChanged = true;
                    //check for if certificate file path has changed.if yes then prompt user
                    //for changing the password as well if that is not changed.becuase we
                    //have to encrypt the new password and then we will generate certificate
                    //based on that.
                    //Case 1 :- if user has changed the path and password is old then it
                    //will prompt for new password or re-enter the password.
                    //if user changes the password then it will generate certificate based
                    //on that new password.
                    //Case 2 :- if user set the blank password even after displaying that
                    //password change prompt in that case we will display warning messages
                    //to use that whether he want to continue with empty password if yes
                    //then we will consider that blank password else use will have to enter
                    //new password.
                    if (pwd.equals(waProjManager.getRemoteAccessEncryptedPassword())
                            && !pwd.isEmpty()) {
                        txtPassword.setText("");
                        txtConfirmPwd.setText("");
                        errorTitle = Messages.remAccErTxtTitle;
                        errorMessage = Messages.remAccPwdMstChng;
                        MessageUtil.displayErrorDialog(this.getShell(),
                                errorTitle, errorMessage);
                        return false;
                    }
                }
                if (pwd.isEmpty() && !waProjManager.
                        getRemoteAccessEncryptedPassword().isEmpty()) {
                     boolean choice = MessageDialog.openQuestion(new Shell(),
                             Messages.remAccErTxtTitle, Messages.remAccWarnPwd);
                    if (!choice) {
                        return false;
                    }
                }
                if (expDate == null || expDate.equalsIgnoreCase("")) {
                    errorTitle = Messages.remAccErTxtTitle;
                    errorMessage = Messages.remAccExpDateNull;
                    MessageUtil.displayErrorDialog(this.getShell(),
                            errorTitle, errorMessage);
                    return false;
                } else {
                    boolean status =  validateExpDate(expDate, formatter);
                    if (!status) {
                        return false;
                    }
                }
                if (newPath == null || newPath.equalsIgnoreCase("")) {
                    errorTitle = Messages.remAccErTxtTitle;
                    errorMessage = Messages.remAccPathNull;
                    MessageUtil.displayErrorDialog(this.getShell(),
                            errorTitle, errorMessage);
                    return false;
                }
                //Check for displaying the relative path in case when user select the
                // certificate file path as workspace or of current project.We will be
                //showing relative path in that case on UI.
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
                    errorTitle = Messages.remAccErTxtTitle;
                    errorMessage = Messages.remAccInvldPath;
                    MessageUtil.displayErrorDialog(this.getShell(),
                            errorTitle, errorMessage);
                    return false;
                }
                try {
                    if (isPathChanged) {
                        String thumbprint =
                        		EncUtilHelper.getThumbPrint(tempPath);
                        waProjManager.
                        setRemoteAccessCertificateFingerprint(thumbprint);
                    }
                } catch (Exception e) {
                    Activator.getDefault().log(Messages.remAccErTmbPrint, e);
                    errorTitle = Messages.remAccSyntaxErr;
                    errorMessage = Messages.remAccErTmbPrint;
                    MessageUtil.displayErrorDialog(this.getShell(), errorTitle,
                            errorMessage);
                    return false;
                }
                if (cnfPwd.equals(pwd)) {
                    try {
                        //Encrypting the password if it is not dummy from xml and it is not
                        //blank and isPwdChanged is true that means user has changes the
                        //password.
                        String modifiedPwd = Messages.remAccDummyPwd;
                        if (!pwd.equals(modifiedPwd)
                        		&& !pwd.isEmpty()
                        		&& isPwdChanged) {
                            String encryptedPwd =
                            		EncUtilHelper.encryptPassword(pwd,
                            				tempPath);
                            waProjManager.
                            setRemoteAccessEncryptedPassword(encryptedPwd);
                        } else {
                            waProjManager.setRemoteAccessEncryptedPassword(pwd);
                        }
                    } catch (Exception e) {
                        Activator.getDefault().log(Messages.
                                remAccErPwd, e);
                        errorTitle = Messages.remAccSyntaxErr;
                        errorMessage = Messages.remAccErPwd;
                        MessageUtil.displayErrorDialog(this.getShell(),
                        		errorTitle, errorMessage);
                        return false;
                    }
                } else {
                    errorTitle = Messages.remAccErTxtTitle;
                    errorMessage = Messages.remAccPwdNotMatch;
                    MessageUtil.displayErrorDialog(this.getShell(),
                            errorTitle, errorMessage);
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
            MessageUtil.displayErrorDialog(this.getShell(), errorTitle,
                    errorMessage);
            Activator.getDefault().log(Messages.remAccErConfigErr, e);
        } catch (ParseException e) {
            errorTitle = Messages.remAccErrTitle;
            errorMessage = Messages.remAccErDateParse;
            MessageUtil.displayErrorDialog(getShell(), errorTitle,
                    errorMessage);
            Activator.getDefault().log(Messages.remAccErDateParse,
                    e);
            return false;
        }
        WAEclipseHelper.refreshWorkspace(
        		Messages.remAccWarning, Messages.remAccWarnMsg);
        return super.performOk();
    }

    /**
     * This method sets the user selected certificate file path from
     * new certificate dialog box to path text field on remote access page.
     *
     * @param cpath : path of certificate file
     */
    protected static void setNewPathfromDialog(String cpath) {
        pathFrmEncUtil = cpath;
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
            errorTitle = Messages.remAccErrTitle;
            errorMessage = Messages.remAccErUserName;
            MessageUtil.displayErrorDialog(getShell(),
                    errorTitle, errorMessage);
            Activator.getDefault().log(Messages.remAccErUserName, e);
        }
        try {
            txtPassword.setText(waProjManager.
                    getRemoteAccessEncryptedPassword());
        } catch (WindowsAzureInvalidProjectOperationException e) {
            errorTitle = Messages.remAccErrTitle;
            errorMessage = Messages.remAccErPwd;
            MessageUtil.displayErrorDialog(getShell(),
                    errorTitle, errorMessage);
            Activator.getDefault().log(Messages.remAccErPwd, e);
        }
        try {
            txtConfirmPwd.setText(waProjManager.
                    getRemoteAccessEncryptedPassword());
        } catch (WindowsAzureInvalidProjectOperationException e) {
            errorTitle = Messages.remAccErrTitle;
            errorMessage = Messages.remAccErPwd;
            MessageUtil.displayErrorDialog(getShell(),
                    errorTitle, errorMessage);
            Activator.getDefault().log(Messages.remAccErPwd, e);
        }
        try {
            txtPath.setText(waProjManager.
                    getRemoteAccessCertificatePath());
        } catch (WindowsAzureInvalidProjectOperationException e) {
            errorTitle = Messages.remAccErrTitle;
            errorMessage = Messages.remAccErCertPath;
            MessageUtil.displayErrorDialog(getShell(),
                    errorTitle, errorMessage);
            Activator.getDefault().log(Messages.remAccErCertPath, e);
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
            errorTitle = Messages.remAccErrTitle;
            errorMessage = Messages.remAccErExpDate;
            MessageUtil.displayErrorDialog(getShell(),
                    errorTitle, errorMessage);
            Activator.getDefault().log(Messages.remAccErExpDate, e);
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
        Date userSelected;
        boolean isValid = true;
        long todaySeconds, userDateSeconds;
            userSelected = formatter.parse(expDate);
            userDateSeconds = userSelected.getTime();
            GregorianCalendar todayCal = new GregorianCalendar();
            todaySeconds = todayCal.getTimeInMillis();
            if ((userDateSeconds - todaySeconds) < 0) {
                errorTitle = Messages.remAccErTxtTitle;
                errorMessage = Messages.remAccDateWrong;
                MessageUtil.displayErrorDialog(new Shell(),
                        errorTitle, errorMessage);
                isValid = false;
            } else {
                waProjManager
                        .setRemoteAccessAccountExpiration(formatter
                                .parse(expDate));
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
            Pattern pattern = Pattern.compile("(?=^.{6,}$)(?=.*\\d)(?=.*[A-Z])(?!.*\\s)(?=.*[a-z]).*$|"
                + "(?=^.{6,}$)(?=.*\\d)(?!.*\\s)(?=.*[a-z])(?=.*\\p{Punct}).*$|"
                + "(?=^.{6,}$)(?=.*\\d)(?!.*\\s)(?=.*[A-Z])(?=.*\\p{Punct}).*$|"
                + "(?=^.{6,}$)(?=.*[A-Z])(?=.*[a-z])(?!.*\\s)(?=.*\\p{Punct}).*$");
            Matcher match = pattern.matcher(txtPassword.getText());
            try {
                //checking if user has changed the password and that field is not
                //blank then check for strong password thing else set the old password.
                if (isPwdChanged) {
                    if (!txtPassword.getText().isEmpty() && !match.find()) {
                        errorTitle = Messages.remAccErPwdNtStrg;
                        errorMessage = Messages.remAccPwdNotStrg;
                        MessageUtil.displayErrorDialog(new Shell(),
                                errorTitle, errorMessage);
                        txtPassword.setFocus();
                    }
                } else {
                    txtPassword.setText(waProjManager.
                            getRemoteAccessEncryptedPassword());
                }
            } catch (WindowsAzureInvalidProjectOperationException e1) {
                errorTitle = Messages.remAccErrTitle;
                errorMessage = Messages.remAccErPwd;
                MessageUtil.displayErrorDialog(getShell(),
                        errorTitle, errorMessage);
                Activator.getDefault().log(Messages.remAccErPwd, e1);
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
                            errorTitle = Messages.remAccErTxtTitle;
                            errorMessage = Messages.remAccDateWrong;
                            MessageUtil.displayErrorDialog(shell,
                                    errorTitle, errorMessage);
                        }
                        else {
                            setExpiryDate(expiryDate);
                            shell.close();
                        }
                    } catch (ParseException e1) {
                        errorTitle = Messages.remAccErrTitle;
                        errorMessage = Messages.remAccErDateParse;
                        MessageUtil.displayErrorDialog(getShell(),
                                errorTitle, errorMessage);
                        Activator.getDefault().log(Messages.remAccErDateParse,
                                e1);
                    }
                }
            });
            shell.setDefaultButton(done);
            shell.open();
            shell.pack();
        }
    }
}
