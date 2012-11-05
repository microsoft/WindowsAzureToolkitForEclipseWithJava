/***************************"****************************************************
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.gigaspaces.azure.wizards;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import waeclipseplugin.Activator;

import com.gigaspaces.azure.model.RemoteDesktopDescriptor;
import com.gigaspaces.azure.util.FileSearch;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.persistent.util.MessageUtil;

import com.microsoftopentechnologies.wacommon.commoncontrols.NewCertificateDialog;
import com.microsoftopentechnologies.wacommon.commoncontrols.NewCertificateDialogData;

public class RemoteDesktopPage extends WindowsAzurePage {

	class ModificationValidator implements ModifyListener {

		@Override
		public void modifyText(ModifyEvent modifyevent) {
			setPageComplete(validatePageComplete());
		}
	}

	private Label userNameLabel;
	private Label passwordLabel;
	private Label confirmPasswordLbl;
	private Label expiryDateLabel;
	private Label pathCertLabel;
	private Label pathPfxLabel;

	private Text txtUserName;
	private Text txtPassword;
	private Text txtConfirmPassword;
	private Text txtExpiryDate;
	private Text txtCertPath;
	private Text txtPfxPath;

	private Button remoteChkBtn;
	private Button newButton;
	private Button workspaceCertButton;
	private Button fileSystemCertButton;
	private Button cal;
	private Button startRemoteChkBtn;

	private String errorTitle;
	private String errorMessage;

	private boolean isInconsistent;
	private boolean isPwdChanged;

	private static final int BUFF_SIZE = 1024;
	private static final String DATE_SEP = "/"; //$NON-NLS-1$

	private WindowsAzureProjectManager waProjManager;
	private Button workspacePfxButton;
	private Button fileSystemPfxButton;
	private Label pfxPasswordLabel;
	private Text txtPfxPassword;

	private IProject selectedProject;
	private boolean rdpSelection;

	protected RemoteDesktopPage() {
		super(Messages.rdpTitle);
		setTitle(Messages.rdpTitle);
	}

	public void setSelectedProject(IProject project) {
		this.selectedProject = project;
	}

	@Override
	public void createControl(Composite parent) {

		loadProject();

		Composite composite = new Composite(parent, SWT.NULL);

		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);

		createRemoteDesktopCheckBtn(composite);

		final Composite container = new Composite(composite, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 3;
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.widthHint = 550;
		gridData.horizontalAlignment = SWT.FILL;
		container.setLayout(layout);
		container.setLayoutData(gridData);

		// user name text box and its grid data.
		createUsernameComponent(container);
		// password text box and its grid data.
		createPasswordComponent(container);
		// confirm password text box and its grid data
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
		expiryDateLabel.setText("Expiration date :"); //$NON-NLS-1$		
		txtExpiryDate = new Text(container, SWT.LEFT | SWT.CALENDAR | SWT.BORDER);
		txtExpiryDate.setLayoutData(dateGridData);
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					Messages.remAccDateFormat, Locale.getDefault());
			Date date = waProjManager.getRemoteAccessAccountExpiration();
			if (date == null) {
				GregorianCalendar currentCal = new GregorianCalendar();
				currentCal.add(Calendar.YEAR, 1);
				Date today = currentCal.getTime();
				if (txtExpiryDate.isEnabled()) {
					txtExpiryDate.setText(dateFormat.format(today));
				}
			} else {
				String newDate = dateFormat.format(date);
				txtExpiryDate.setText(newDate);
			}
		} catch (WindowsAzureInvalidProjectOperationException e1) {
			txtExpiryDate.setText(""); //$NON-NLS-1$
			remoteChkBtn.setSelection(false);
			isInconsistent = true;
			Activator.getDefault().log(Messages.remAccErExpDate, e1);
		}

		txtExpiryDate.addModifyListener(new ModificationValidator());

		// button for calendar picker
		cal = new Button(container, SWT.PUSH | SWT.CENTER);
		cal.setText("..."); //$NON-NLS-1$
		cal.addSelectionListener(new CalenderListener());

		// group for the path and other variables
		createGroupCertPath(composite);

		createStartRemoteDesktopOnDeployBtn(composite);

		if (remoteChkBtn.getSelection()) {
			getDefaultValues();
		} else {
			makeAllTextBlank();
			setComponentStatus(false);
		}

		if (isInconsistent) {
			errorTitle = "Remote Access Properties Not Valid"; //$NON-NLS-1$
			errorMessage = "The details of the current remote access configuration for this project cannot be shown because they are not consistent for all the roles."; //$NON-NLS-1$
			MessageUtil.displayErrorDialog(this.getShell(), errorTitle,
					errorMessage);
		}

		setControl(composite);
	}

	private void createRemoteDesktopCheckBtn(Composite parent) {
		remoteChkBtn = new Button(parent, SWT.CHECK);
		boolean isEnabled = false;
		try {
			isEnabled = waProjManager.getRemoteAccessAllRoles();
		} catch (WindowsAzureInvalidProjectOperationException e2) {
			errorTitle = "Error"; //$NON-NLS-1$
			errorMessage = "Error occurred while getting remote access check box status."; //$NON-NLS-1$
			MessageUtil.displayErrorDialog(getShell(), errorTitle, errorMessage);
			Activator.getDefault().log("Error occurred while getting remote access check box status.", e2); //$NON-NLS-1$
		}
		// grid data for remote check box.
		GridData gridData = new GridData();
		gridData.horizontalSpan = 2;
		remoteChkBtn.setLayoutData(gridData);
		remoteChkBtn.setText("Enable all roles to accept Remote Desktop Connections with these login credentials:"); //$NON-NLS-1$
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
				setPageComplete(validatePageComplete());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
			}
		});

	}

	protected void getDefaultValues() {
		try {
			txtUserName.setText(waProjManager.getRemoteAccessUsername());
		} catch (WindowsAzureInvalidProjectOperationException e) {
			txtUserName.setText(""); //$NON-NLS-1$
			remoteChkBtn.setSelection(false);
			isInconsistent = true;
			Activator.getDefault().log(Messages.remAccErUserName, e);
		}

		try {
			txtPassword.setText(waProjManager.getRemoteAccessEncryptedPassword());
		} catch (WindowsAzureInvalidProjectOperationException e) {
			txtPassword.setText(""); //$NON-NLS-1$
			remoteChkBtn.setSelection(false);

			isInconsistent = true;
			Activator.getDefault().log(Messages.remAccErPwd, e);
		}

		try {
			txtConfirmPassword.setText(waProjManager.getRemoteAccessEncryptedPassword());
		} catch (WindowsAzureInvalidProjectOperationException e) {
			txtConfirmPassword.setText(""); //$NON-NLS-1$
			remoteChkBtn.setSelection(false);

			isInconsistent = true;

			Activator.getDefault().log(Messages.remAccErPwd, e);
		}

		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					Messages.remAccDateFormat, Locale.getDefault());
			Date date = waProjManager.getRemoteAccessAccountExpiration();
			if (date == null) {
				GregorianCalendar currentCal = new GregorianCalendar();
				currentCal.add(Calendar.YEAR, 1);
				Date today = currentCal.getTime();
				if (txtExpiryDate.isEnabled()) {
					txtExpiryDate.setText(dateFormat.format(today));
				}
			} else {
				String newDate = dateFormat.format(date);
				txtExpiryDate.setText(newDate);
			}
		} catch (WindowsAzureInvalidProjectOperationException e1) {
			txtExpiryDate.setText(""); //$NON-NLS-1$
			remoteChkBtn.setSelection(false);
			isInconsistent = true;
			Activator.getDefault().log(Messages.remAccErExpDate, e1);
		}

		try {
			populateCertTextBox();
		} catch (WindowsAzureInvalidProjectOperationException e) {
			txtCertPath.setText(""); //$NON-NLS-1$
			remoteChkBtn.setSelection(false);
			isInconsistent = true;
			Activator.getDefault().log(Messages.remAccErCertPath, e);
		}

		try {
			populatePfxTextBox();
		} catch (WindowsAzureInvalidProjectOperationException e) {
			txtCertPath.setText(""); //$NON-NLS-1$
			remoteChkBtn.setSelection(false);
			isInconsistent = true;
			Activator.getDefault().log(Messages.remAccErPfxPath, e);
		}
	}

	private void makeAllTextBlank(){
		txtUserName.setText("");
		txtPassword.setText("");
		txtConfirmPassword.setText("");
		txtExpiryDate.setText("");
		txtCertPath.setText("");
		txtPfxPath.setText("");
	}


	private void createStartRemoteDesktopOnDeployBtn(Composite parent) {
		startRemoteChkBtn = new Button(parent, SWT.CHECK);

		startRemoteChkBtn.setText("Start Remote Desktop on deploy"); //$NON-NLS-1$		
		startRemoteChkBtn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setPageComplete(validatePageComplete());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		startRemoteChkBtn.setSelection(false);

	}

	private void createUsernameComponent(Composite container) {
		userNameLabel = new Label(container, SWT.LEFT);
		userNameLabel.setText("User name :"); //$NON-NLS-1$
		txtUserName = new Text(container, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
		GridData gridData = new GridData();
		gridData.widthHint = 150;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 2;
		txtUserName.setLayoutData(gridData);

		try {
			txtUserName.setText(waProjManager.getRemoteAccessUsername());
		} catch (WindowsAzureInvalidProjectOperationException e) {
			txtUserName.setText(""); //$NON-NLS-1$
			remoteChkBtn.setSelection(false);
			isInconsistent = true;
			Activator.getDefault().log(Messages.remAccErUserName, e);
		}

		txtUserName.addModifyListener(new ModificationValidator());
	}

	private void createPasswordComponent(Composite container) {
		passwordLabel = new Label(container, SWT.LEFT);
		passwordLabel.setText("Password :"); //$NON-NLS-1$
		txtPassword = new Text(container, SWT.LEFT | SWT.PASSWORD | SWT.BORDER);
		GridData gridData = new GridData();
		gridData.widthHint = 150;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 2;
		txtPassword.setLayoutData(gridData);

		try {
			txtPassword.setText(waProjManager
					.getRemoteAccessEncryptedPassword());
		} catch (WindowsAzureInvalidProjectOperationException e) {
			txtPassword.setText(""); //$NON-NLS-1$
			remoteChkBtn.setSelection(false);

			isInconsistent = true;
			Activator.getDefault().log(Messages.remAccErPwd, e);
		}

		// Listener for key event when user click on password text box
		// it will set flag for entering the new values.
		txtPassword.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent event) {
			}

			@Override
			public void keyPressed(KeyEvent event) {
				isPwdChanged = true;
			}
		});

		txtPassword.addFocusListener(new PasswordFocusListener());
	}

	private void createConfPwdComponent(Composite container) {
		confirmPasswordLbl = new Label(container, SWT.LEFT);
		confirmPasswordLbl.setText("Confirm password :"); //$NON-NLS-1$
		txtConfirmPassword = new Text(container, SWT.LEFT | SWT.PASSWORD
				| SWT.BORDER);
		GridData gridData = new GridData();
		gridData.widthHint = 150;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 2;
		txtConfirmPassword.setLayoutData(gridData);

		try {
			txtConfirmPassword.setText(waProjManager
					.getRemoteAccessEncryptedPassword());
		} catch (WindowsAzureInvalidProjectOperationException e) {
			txtConfirmPassword.setText(""); //$NON-NLS-1$
			remoteChkBtn.setSelection(false);

			isInconsistent = true;

			Activator.getDefault().log(Messages.remAccErPwd, e);
		}

		txtConfirmPassword.addModifyListener(new ModificationValidator());
		txtConfirmPassword.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void focusGained(FocusEvent arg0) {
				txtConfirmPassword.setText("");

			}
		});
	}

	private void createGroupCertPath(Composite parent) {
		Group group = new Group(parent, SWT.SHADOW_ETCHED_IN | SWT.FILL);
		GridLayout groupGridLayout = new GridLayout();
		GridData groupGridData = new GridData();
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridLayout.numColumns = 1;
		group.setText("Certificate to encrypt user credentials"); //$NON-NLS-1$
		group.setLayout(groupGridLayout);
		group.setLayoutData(groupGridData);

		Composite certContainer = new Composite(group, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		certContainer.setLayout(layout);

		pathCertLabel = new Label(certContainer, SWT.LEFT);
		groupGridData = new GridData();
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridData.horizontalSpan = 3;
		groupGridData.widthHint = 350;
		groupGridData.verticalIndent = 5;
		pathCertLabel.setText(Messages.rdpPublicKey);
		pathCertLabel.setLayoutData(groupGridData);

		txtCertPath = new Text(certContainer, SWT.BORDER);
		groupGridData = new GridData();
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridData.widthHint = 350;
		txtCertPath.setLayoutData(groupGridData);

		try {

			populateCertTextBox();

		} catch (WindowsAzureInvalidProjectOperationException e) {
			txtCertPath.setText(""); //$NON-NLS-1$
			remoteChkBtn.setSelection(false);
			isInconsistent = true;
			Activator.getDefault().log(Messages.remAccErCertPath, e);
		}

		txtCertPath.addModifyListener(new ModificationValidator());

		groupGridData = new GridData();
		groupGridData.widthHint = 100;
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.horizontalAlignment = SWT.FILL;
		workspaceCertButton = new Button(certContainer, SWT.PUSH | SWT.CENTER);
		workspaceCertButton.setText("Workspace..."); //$NON-NLS-1$
		workspaceCertButton.setLayoutData(groupGridData);
		workspaceCertButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				workspaceCertBtnListener();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
			}
		});

		groupGridData = new GridData();
		groupGridData.widthHint = 100;
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.horizontalAlignment = SWT.FILL;
		fileSystemCertButton = new Button(certContainer, SWT.PUSH | SWT.CENTER);
		fileSystemCertButton.setText("File System..."); //$NON-NLS-1$
		fileSystemCertButton.setLayoutData(groupGridData);
		fileSystemCertButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				browseCertBtnListener();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
			}
		});

		Composite pfxContainer = new Composite(group, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 3;
		pfxContainer.setLayout(layout);

		pathPfxLabel = new Label(pfxContainer, SWT.LEFT);
		groupGridData = new GridData();
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridData.horizontalSpan = 3;
		groupGridData.widthHint = 350;
		groupGridData.verticalIndent = 5;
		pathPfxLabel.setText(Messages.rdpPrivateKey);
		pathPfxLabel.setLayoutData(groupGridData);
		txtPfxPath = new Text(pfxContainer, SWT.BORDER);
		groupGridData = new GridData();
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridData.widthHint = 350;
		txtPfxPath.setLayoutData(groupGridData);

		boolean defaultPfx = false;
		try {

			defaultPfx = populatePfxTextBox();			

		} catch (WindowsAzureInvalidProjectOperationException e) {
			txtCertPath.setText(""); //$NON-NLS-1$
			remoteChkBtn.setSelection(false);
			isInconsistent = true;
			Activator.getDefault().log(Messages.remAccErPfxPath, e);
		}
		txtPfxPath.addModifyListener(new ModificationValidator());

		groupGridData = new GridData();
		groupGridData.widthHint = 100;
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.horizontalAlignment = SWT.FILL;
		workspacePfxButton = new Button(pfxContainer, SWT.PUSH | SWT.CENTER);
		workspacePfxButton.setText("Workspace..."); //$NON-NLS-1$
		workspacePfxButton.setLayoutData(groupGridData);
		workspacePfxButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				workspacePfxBtnListener();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
			}
		});

		groupGridData = new GridData();
		groupGridData.widthHint = 100;
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.horizontalAlignment = SWT.FILL;
		fileSystemPfxButton = new Button(pfxContainer, SWT.PUSH | SWT.CENTER);
		fileSystemPfxButton.setText("File System..."); //$NON-NLS-1$
		fileSystemPfxButton.setLayoutData(groupGridData);
		fileSystemPfxButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				browsePfxBtnListener();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
			}
		});


		Composite pfxPassContainer = new Composite(pfxContainer, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		pfxPassContainer.setLayout(layout);
		pfxPasswordLabel = new Label(pfxPassContainer, SWT.LEFT);
		pfxPasswordLabel.setText(Messages.rdpPrivateKeyPassword);
		txtPfxPassword = new Text(pfxPassContainer, SWT.LEFT | SWT.PASSWORD
				| SWT.BORDER);
		GridData gridData = new GridData();
		gridData.widthHint = 150;
		gridData.grabExcessHorizontalSpace = true;

		txtPfxPassword.setLayoutData(gridData);

		if (defaultPfx) {

			try {
				KeyStore ks = KeyStore.getInstance(Messages.keyStore);
				FileInputStream input = new FileInputStream(new File(txtPfxPath.getText()));
				ks.load(input, "Password1".toCharArray());
			} catch (Exception e) {
				txtPfxPassword.setText(""); //$NON-NLS-1$
			}			

			txtPfxPassword.setText("Password1"); //$NON-NLS-1$
		}
		else {
			txtPfxPassword.setText(""); //$NON-NLS-1$
		}
		txtPfxPassword.addModifyListener(new ModificationValidator());

		groupGridData = new GridData();
		groupGridData.horizontalIndent = 400;
		groupGridData.horizontalSpan = 2;
		groupGridData.widthHint = 100;
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.horizontalAlignment = SWT.FILL;
		newButton = new Button(group, SWT.PUSH | SWT.CENTER);
		newButton.setText("New..."); //$NON-NLS-1$
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

	}

	private void populateCertTextBox() throws WindowsAzureInvalidProjectOperationException {

		String path = waProjManager.getRemoteAccessCertificatePath();

		if (path.isEmpty()) {
			txtCertPath.setText("");
			return;
		}
		File defaultCertFile = new File(path);

		if (defaultCertFile.exists()) { // in case the file does not reside under the project folder
			txtCertPath.setText(defaultCertFile.getPath());
		}
		else { // this means the file is inside the project dir (path starts with ${basedir})
			String fileName = defaultCertFile.getName();
			String dirName = defaultCertFile.getParentFile().getName();

			String pathInWorkspace = getPath(fileName, dirName);
			if (pathInWorkspace != null) {
				txtCertPath.setText(pathInWorkspace);								
			}
			else {
				txtCertPath.setText("");
			}
		}

	}

	private boolean populatePfxTextBox() throws WindowsAzureInvalidProjectOperationException {

		String path = waProjManager.getRemoteAccessCertificatePath();

		File defaultCer = new File(path); // this is the certificate file from the Remote Access Property page

		String fileName = defaultCer.getName().replace("Public.cer", "Private.pfx"); // a guess as to what the pfx file name would be on the File System
		if (!fileName.contains(".pfx")) {
			txtPfxPath.setText("");
			return false;
		}
		String dirName = defaultCer.getParentFile().getName(); // a guess as to where the pfx file is placed 

		File pfx = new File(defaultCer.getParent(), fileName);
		if (pfx.exists()) { // if the guess was correct
			txtPfxPath.setText(pfx.getAbsolutePath());
		}
		else { // the guess was not correct
			String pathInWorkspace = getPath(fileName, dirName);
			if (pathInWorkspace != null) {
				File defaultPfxFile = new File(pathInWorkspace); // try looking for the pfx file in the project folder
				if (defaultPfxFile.exists()) { // if it exists
					txtPfxPath.setText(defaultPfxFile.getAbsolutePath());
					return true;
				}
			}
			else { // if not, i dont know where it is. leave blank and have the user populate this text box.
				txtPfxPath.setText("");
			}				
		}
		return false;


	}

	private String getPath(final String fileName, final String dirName) {
		String p1 = selectedProject.getLocation().toPortableString();

		FileSearch s = new FileSearch();

		PathListener listener = new PathListener(fileName, dirName);

		s.addListener(listener);
		try {
			s.traverse(new File(p1));
		} catch (IOException e) {
		}

		return listener.getResult();
	}

	private void setComponentStatus(boolean status) {
		userNameLabel.setEnabled(status);
		passwordLabel.setEnabled(status);
		confirmPasswordLbl.setEnabled(status);
		expiryDateLabel.setEnabled(status);
		txtUserName.setEnabled(status);
		txtPassword.setEnabled(status);
		txtConfirmPassword.setEnabled(status);
		txtExpiryDate.setEnabled(status);
		txtCertPath.setEnabled(status);
		txtPfxPath.setEnabled(status);
		pathCertLabel.setEnabled(status);
		pathPfxLabel.setEnabled(status);
		newButton.setEnabled(status);
		workspaceCertButton.setEnabled(status);
		fileSystemCertButton.setEnabled(status);
		workspacePfxButton.setEnabled(status);
		fileSystemPfxButton.setEnabled(status);
		pfxPasswordLabel.setEnabled(status);
		txtPfxPassword.setEnabled(status);
		cal.setEnabled(status);
		startRemoteChkBtn.setEnabled(status);
	}

	private void loadProject() {
		try {
			String projectPath = selectedProject.getLocation().toPortableString();
			File projectDir = new File(projectPath);
			waProjManager = WindowsAzureProjectManager.load(projectDir);
		} catch (Exception e) {
			errorTitle = "Syntax or File Error"; //$NON-NLS-1$
			errorMessage = "Project configuration files are not valid." //$NON-NLS-1$
					+ "Check your *.cscfg, *.csdef and *.xml files for syntax errors or other technical issues."; //$NON-NLS-1$
			MessageUtil.displayErrorDialog(this.getShell(), errorTitle,
					errorMessage);
			Activator
			.getDefault()
			.log("Error occurred while loading project. Try the operation after re-selecting the project.", e); //$NON-NLS-1$
		}
	}

	public void writeFile(InputStream inStream, OutputStream outStream)
			throws IOException {

		try {
			byte[] buf = new byte[BUFF_SIZE];
			int len = inStream.read(buf);
			while (len > 0) {
				outStream.write(buf, 0, len);
				len = inStream.read(buf);
			}
		} finally {
			if (inStream != null) {
				inStream.close();
			}
			if (outStream != null) {
				outStream.close();
			}
		}
	}

	private void setExpiryDate(String edate) {
		txtExpiryDate.setText(edate);
	}

	protected void newBtnListener() {
		NewCertificateDialogData data = new NewCertificateDialogData();
        NewCertificateDialog dialog = new NewCertificateDialog(getShell(),data);
        
		int returnCode = dialog.open();
		if (returnCode == Window.OK) {
			txtCertPath.setText(data.getCerFilePath());
			txtPfxPath.setText(data.getPfxFilePath());
			txtPfxPassword.setText(data.getPassword());
			setPageComplete(validatePageComplete());
		}
	}

	protected void workspaceCertBtnListener() {

		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
				.getProjects();
		workspaceBtnListener(new CustomViewerFilter(Arrays.asList(projects),
				".cer"), txtCertPath); //$NON-NLS-1$
	}

	protected void workspacePfxBtnListener() {

		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
				.getProjects();

		workspaceBtnListener(new CustomViewerFilter(Arrays.asList(projects),
				".pfx"), txtPfxPath); //$NON-NLS-1$

		// workspaceBtnListener(new ViewerFilter() {
		//
		// @Override
		// public boolean select(Viewer arg0, Object arg1, Object arg2) {
		// if (arg2 instanceof IProject) {
		// return ((IProject) arg2).isOpen();
		// } else if (arg2 instanceof IFile) {
		//					boolean ext = ((IFile) arg2).getName().endsWith(".pfx"); //$NON-NLS-1$
		//
		// if (!ext) {
		// return false;
		// }
		// }
		// return true;
		// }
		// }, txtPfxPath);
	}

	protected void workspaceBtnListener(ViewerFilter filter, Text target) {
		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(
				getShell(), new WorkbenchLabelProvider(),
				new WorkbenchContentProvider());
		dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
		dialog.setTitle("Select from workspace"); //$NON-NLS-1$

		dialog.addFilter(filter);

		dialog.setAllowMultiple(false);
		dialog.open();
		Object obj[] = dialog.getResult();
		if (obj != null && obj.length > 0) {
			if (obj[0] instanceof IFile) {
				IFile file = (IFile) obj[0];
				String exactPath = file.getLocation().toOSString();
				// if (exactPath.contains(selProject.getLocation().toOSString()
				// + File.separator)) {
				// String workspacePath = selProject.getLocation()
				// .toOSString();
				// String replaceString = exactPath;
				// String subString = exactPath.substring(
				// exactPath.indexOf(workspacePath),
				// workspacePath.length());
				// replaceString = replaceString.replace(subString, BASE_PATH);
				// target.setText(replaceString);
				// } else {
				target.setText(exactPath);
				// }
			} else {
				errorTitle = "Not a valid certificate file"; //$NON-NLS-1$
				errorMessage = "The file you have selected is not a certificate file.Please select a valid certificate file (*.cer)."; //$NON-NLS-1$
				MessageUtil.displayErrorDialog(getShell(), errorTitle,
						errorMessage);
				target.setText(""); //$NON-NLS-1$
			}
		}
	}

	protected void browseCertBtnListener() {
		browseBtnListener(new String[] { "*.cer" }, txtCertPath); //$NON-NLS-1$

	}

	protected void browsePfxBtnListener() {

		browseBtnListener(new String[] { "*.pfx" }, txtPfxPath); //$NON-NLS-1$
	}

	protected void browseBtnListener(String[] extensions, Text target) {
		FileDialog dialog = new FileDialog(this.getShell());
		dialog.setText("Browse"); //$NON-NLS-1$
		dialog.setFilterExtensions(extensions);
		String directory = dialog.open();
		if (directory != null) {
			if (directory.contains(selectedProject.getLocation().toOSString() + File.separator)) {
				String replaceString = directory;
				target.setText(replaceString);
			} else {
				target.setText(directory);
			}
		}
	}

	private class CalenderListener extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent event) {
			final Shell shell = new Shell(getShell(), SWT.DIALOG_TRIM);
			shell.setText("Select Expiration Date"); //$NON-NLS-1$
			shell.setLayout(new GridLayout(1, false));
			final DateTime calPick = new DateTime(shell, SWT.CALENDAR
					| SWT.BORDER);
			new Label(shell, SWT.NONE);
			new Label(shell, SWT.NONE);
			Button done = new Button(shell, SWT.PUSH);
			done.setText(Messages.rdpOk);
			GridData gridData = new GridData();
			gridData.horizontalAlignment = SWT.CENTER;
			gridData.widthHint = 100;
			done.setLayoutData(gridData);
			done.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					int calMonth, calYear, calDay;
					calDay = calPick.getDay();
					calMonth = (calPick.getMonth() + 1);
					calYear = calPick.getYear();
					String expiryDate = Integer.toString(calMonth) + DATE_SEP
							+ Integer.toString(calDay) + DATE_SEP
							+ Integer.toString(calYear);
					Date userSelected;
					long todaySeconds, userDateSeconds;
					DateFormat formatter = new SimpleDateFormat(
							"MM/dd/yyyy", Locale.getDefault()); //$NON-NLS-1$
					try {
						userSelected = formatter.parse(expiryDate);
						userDateSeconds = userSelected.getTime();
						GregorianCalendar todayCal = new GregorianCalendar();
						todaySeconds = todayCal.getTimeInMillis();
						if ((userDateSeconds - todaySeconds) < 0) {
							errorTitle = "Remote Access Properties Not Valid"; //$NON-NLS-1$
							errorMessage = "Expiration date should be greater than today's date."; //$NON-NLS-1$
							MessageUtil.displayErrorDialog(shell, errorTitle,
									errorMessage);
						} else {
							setExpiryDate(expiryDate);
							shell.close();
						}
					} catch (ParseException e1) {
						errorTitle = "Error"; //$NON-NLS-1$
						errorMessage = "Error occurred while parsing expiration date."; //$NON-NLS-1$
						MessageUtil.displayErrorDialog(getShell(), errorTitle,
								errorMessage);
						Activator
						.getDefault()
						.log("Error occurred while parsing expiration date.", //$NON-NLS-1$
								e1);
					}
				}
			});
			shell.setDefaultButton(done);
			shell.open();
			shell.pack();
		}
	}

	@Override
	protected boolean validatePageComplete() {

		String userName = null;
		String pwd = null;
		String confirm;
		String expiryDate;
		Date ed = null;
		String certPath;
		String pfxPath;
		String pfxPassword = null;
		if (remoteChkBtn.getSelection() == true) {
			userName = txtUserName.getText();

			if (userName == null || userName.isEmpty()) {
				setErrorMessage(Messages.rdpUserNameEmpty);
				return false;
			}

			pwd = txtPassword.getText();

			if (pwd == null || pwd.isEmpty()) {
				setErrorMessage(Messages.rdpPasswordEmpty);
				return false;
			}

			confirm = txtConfirmPassword.getText();

			if (confirm == null || confirm.isEmpty()) {
				setErrorMessage(Messages.rdpConfirmPasswordEmpty);
				return false;
			}

			if (!pwd.equals(confirm)) {
				setErrorMessage(Messages.rdpPasswordsDontMatch);
				return false;
			}

			expiryDate = txtExpiryDate.getText();

			if (expiryDate == null || expiryDate.isEmpty()) {
				setErrorMessage(Messages.rdpExpDateNull);
				return false;
			}

			try {
				ed = new SimpleDateFormat("MM/dd/yyyy").parse(expiryDate); //$NON-NLS-1$
			} catch (ParseException e) {
				setErrorMessage(Messages.rdpinvalidxpDate);
				return false;
			}

			certPath = txtCertPath.getText();

			if (certPath == null || certPath.isEmpty()) {
				setErrorMessage(Messages.rdpCertIsEmpty);
				return false;
			}

			if ((new File(certPath)).exists() == false) {
				setErrorMessage(Messages.rdpCertIsExists);
				return false;
			}

			pfxPath = txtPfxPath.getText();

			if (pfxPath == null || pfxPath.isEmpty()) {
				setErrorMessage(Messages.rdpPrivateKeyEmpty);
				return false;
			}

			if ((new File(pfxPath)).exists() == false) {
				setErrorMessage(Messages.rdpPrivateKeyIsExists);
				return false;
			}

			pfxPassword = txtPfxPassword.getText();

			if (pfxPassword == null || pfxPassword.isEmpty()) {
				setErrorMessage(Messages.rdpPrivateKeyPasswordEmpty);
				return false;
			}

			try {
				KeyStore ks = KeyStore.getInstance(Messages.keyStore);
				FileInputStream input = new FileInputStream(new File(pfxPath));
				ks.load(input, pfxPassword.toCharArray());
			} catch (Exception e) {
				setErrorMessage(Messages.invalidPfxPwdMsg);
				return false;
			}			
		}

		boolean startRemoteRdp = startRemoteChkBtn.getSelection();
		rdpSelection = remoteChkBtn.getSelection();

		fireConfigurationEvent(new ConfigurationEventArgs(this,ConfigurationEventArgs.REMOTE_DESKTOP,
				new RemoteDesktopDescriptor(userName, pwd, ed,txtCertPath.getText(), txtPfxPath.getText(), 
						pfxPassword, startRemoteRdp, remoteChkBtn.getSelection())));

		setErrorMessage(null);
		return true;
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
						txtConfirmPassword.setText("");
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

	public boolean getRdpSelection() {
		return rdpSelection;
	}

}