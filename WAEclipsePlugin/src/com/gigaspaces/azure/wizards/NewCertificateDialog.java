/*******************************************************************************
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import waeclipseplugin.Activator;
import com.persistent.util.EncUtilHelper;
import com.persistent.util.MessageUtil;

public class NewCertificateDialog extends TitleAreaDialog {

	private Text txtPwd;
	private Text txtConfirmPwd;
	private Text txtCertFile;
	private Text txtPFXFile;
	private String errorTitle;
	private String errorMessage;
	private IProject selProject;
	private String pfxPassword;
	private String pfxPath;
	private String certPath;

	public String getPfxPassword() {
		return pfxPassword;
	}

	public String getPfxPath() {
		return pfxPath;
	}

	public String getCertPath() {
		return certPath;
	}

	public NewCertificateDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.certDlgNewCertTxt);
		newShell.setLocation(200, 200);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(Messages.certDlgNewCertTxt);
		setMessage(Messages.certDlgNewCertMsg);

		Composite container = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		GridData gridData = new GridData();
		gridLayout.numColumns = 2;
		gridData.verticalIndent = 10;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		container.setLayout(gridLayout);
		container.setLayoutData(gridData);

		Label pwdLabel = new Label(container, SWT.LEFT);
		pwdLabel.setText(Messages.certDlgPwdLbl);
		gridData = new GridData();
		gridData.horizontalIndent = 10;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.widthHint = 350;

		txtPwd = new Text(container, SWT.BORDER | SWT.PASSWORD);
		txtPwd.setLayoutData(gridData);
		Label confirmPwdLbl = new Label(container, SWT.LEFT);
		confirmPwdLbl.setText(Messages.certDlgConfPwdLbl);
		txtConfirmPwd = new Text(container, SWT.BORDER | SWT.PASSWORD);
		txtConfirmPwd.setLayoutData(gridData);

		Group group = new Group(container, SWT.SHADOW_ETCHED_IN);
		GridLayout groupGridLayout = new GridLayout();
		GridData groupGridData = new GridData();
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridLayout.numColumns = 3;
		groupGridData.horizontalSpan = 2;
		groupGridData.verticalIndent = 10;
		groupGridData.widthHint = 450;
		group.setText(Messages.certDlgGrpLbl);
		group.setLayout(groupGridLayout);
		group.setLayoutData(groupGridData);

		createCertFileComp(group);

		createPfxFileComp(group);

		return super.createDialogArea(parent);
	}

	private void createPfxFileComp(Group group) {
		Label pfxFileLabel = new Label(group, SWT.LEFT);
		pfxFileLabel.setText(Messages.certDlgPFXLbl);
		GridData groupGridData = new GridData();
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridData.horizontalSpan = 3;
		groupGridData.widthHint = 350;
		groupGridData.verticalIndent = 5;
		pfxFileLabel.setLayoutData(groupGridData);

		txtPFXFile = new Text(group, SWT.BORDER | SWT.LEFT);
		groupGridData = new GridData();
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridData.widthHint = 350;
		txtPFXFile.setLayoutData(groupGridData);

		Button pfxBrowseButton = new Button(group, SWT.PUSH | SWT.CENTER
				| SWT.END);
		pfxBrowseButton.setText(Messages.pfxDlgBrowseBtn);
		groupGridData = new GridData();
		groupGridData.widthHint = 100;
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.horizontalAlignment = SWT.FILL;
		pfxBrowseButton.setLayoutData(groupGridData);
		pfxBrowseButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				String location = browseBtnListener(Messages.pfxFilePattern);
				if (location != null) {
					if (location.endsWith(Messages.pxSuffix)) {
						txtPFXFile.setText(location);
					} else {
						StringBuilder stringBuilder = new StringBuilder(
								location);
						stringBuilder.append(Messages.pfxSuffix);
						txtPFXFile.setText(stringBuilder.toString());
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
			}
		});
	}

	private void createCertFileComp(Group group) {
		Label certFileLabel = new Label(group, SWT.LEFT);
		certFileLabel.setText(Messages.certDlgCertLbl);
		GridData groupGridData = new GridData();
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.horizontalSpan = 3;
		groupGridData.widthHint = 350;
		groupGridData.verticalIndent = 5;
		certFileLabel.setLayoutData(groupGridData);

		txtCertFile = new Text(group, SWT.BORDER | SWT.LEFT);
		groupGridData = new GridData();
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.horizontalAlignment = SWT.FILL;
		groupGridData.widthHint = 350;
		txtCertFile.setLayoutData(groupGridData);

		Button certBrowseButton = new Button(group, SWT.PUSH | SWT.CENTER
				| SWT.END);
		certBrowseButton.setText(Messages.certDlgBrowseBtn);
		groupGridData = new GridData();
		groupGridData.widthHint = 100;
		groupGridData.grabExcessHorizontalSpace = true;
		groupGridData.horizontalAlignment = SWT.FILL;
		certBrowseButton.setLayoutData(groupGridData);
		certBrowseButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				String location = browseBtnListener(Messages.cerFilePattern);
				if (location != null) {
					if (location.endsWith(Messages.cerSuffix)) {
						txtCertFile.setText(location);
					} else {
						StringBuilder stringBuilder = new StringBuilder(
								location);
						stringBuilder.append(Messages.cerSuffix);
						txtCertFile.setText(stringBuilder.toString());
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
			}
		});
	}

	/*
	 * Listener for Browse Button
	 */
	protected String browseBtnListener(String ext) {
		FileDialog dialog = new FileDialog(this.getShell(), SWT.SAVE);
		String[] extensions = new String[1];
		extensions[0] = ext;
		dialog.setOverwrite(true);
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		ISelectionService service = window.getSelectionService();
		ISelection selection = service.getSelection();
		Object element = null;
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSel = (IStructuredSelection) selection;
			element = structuredSel.getFirstElement();
		}
		IResource resource;
		String path = ""; //$NON-NLS-1$
		if (element instanceof IResource) {
			resource = (IResource) element;
			selProject = resource.getProject();
			path = selProject.getLocation().toPortableString();
		} else {
			IWorkbenchPage page = window.getActivePage();
			IFile file = (IFile) page.getActiveEditor().getEditorInput()
					.getAdapter(IFile.class);
			path = file.getProject().getLocation().toPortableString();
		}
		dialog.setFilterPath(path);
		dialog.setText(Messages.certDlgBrowFldr);
		dialog.setFilterExtensions(extensions);
		return dialog.open();
	}

	@Override
	protected void okPressed() {
		if (txtPwd.getText() == null || txtPwd.getText().isEmpty()) {
			errorTitle = Messages.remAccErTxtTitle;
			errorMessage = Messages.certDlgPwNull;
			MessageUtil.displayErrorDialog(this.getShell(), errorTitle,
					errorMessage);
			return;
		} else if (txtConfirmPwd.getText() == null
				|| txtConfirmPwd.getText().isEmpty()) {
			errorTitle = Messages.remAccErTxtTitle;
			errorMessage = Messages.certDlgCfPwdNull;
			MessageUtil.displayErrorDialog(this.getShell(), errorTitle,
					errorMessage);
			return;
		} else if (!(txtPwd.getText() == null) && !(txtPwd.getText().isEmpty())) {
			Pattern pattern = Pattern.compile("^\\S+$"); //$NON-NLS-1$
			Matcher match = pattern.matcher(txtPwd.getText());
			if (!match.find()) {
				errorTitle = Messages.certDlgPwdWrong;
				errorMessage = Messages.certDlgPwdNtCorr;
				MessageUtil.displayErrorDialog(new Shell(), errorTitle,
						errorMessage);
				return;
			}
		}
		if (!(txtConfirmPwd.getText() == null)
				&& !(txtConfirmPwd.getText().isEmpty())) {
			Pattern pattern = Pattern.compile("^\\S+$"); //$NON-NLS-1$
			Matcher match = pattern.matcher(txtConfirmPwd.getText());
			if (!match.find()) {
				errorTitle = Messages.certDlgPwdWrong;
				errorMessage = Messages.certDlgPwdNtCorr;
				MessageUtil.displayErrorDialog(new Shell(), errorTitle,
						errorMessage);
				return;
			}
		}
		if (!(txtPwd.getText()).equals(txtConfirmPwd.getText())) {
			errorTitle = Messages.certDlgPwNtMatch;
			errorMessage = Messages.certDlgPwdNtMtch;
			MessageUtil.displayErrorDialog(new Shell(), errorTitle,
					errorMessage);
			return;
		}
		if (txtCertFile.getText() == null || txtCertFile.getText().isEmpty()) {
			errorTitle = Messages.remAccErTxtTitle;
			errorMessage = Messages.certDlgCerNull;
			MessageUtil.displayErrorDialog(this.getShell(), errorTitle,
					errorMessage);
			return;
		} else if (txtPFXFile.getText() == null
				|| txtPFXFile.getText().isEmpty()) {
			errorTitle = Messages.remAccErTxtTitle;
			errorMessage = Messages.certDlgPFXNull;
			MessageUtil.displayErrorDialog(this.getShell(), errorTitle,
					errorMessage);
			return;
		}
		String certFilePath = txtCertFile.getText();
		String pfxFilePath = txtPFXFile.getText();
		if (certFilePath.lastIndexOf(File.separator) == -1
				|| pfxFilePath.lastIndexOf(File.separator) == -1) {
			errorTitle = Messages.remAccErTxtTitle;
			errorMessage = Messages.remAccInvldPath;
			MessageUtil.displayErrorDialog(this.getShell(), errorTitle,
					errorMessage);
			return;
		}
		if ((!certFilePath.endsWith(Messages.cerSuffix))
				|| (!pfxFilePath.endsWith(Messages.pfxSuffix))) {
			errorTitle = Messages.remAccErTxtTitle;
			errorMessage = Messages.remAccInvdFilExt;
			MessageUtil.displayErrorDialog(this.getShell(), errorTitle,
					errorMessage);
			return;
		}
		String certFolder = certFilePath.substring(0,
				certFilePath.lastIndexOf(File.separator));
		String pfxFolder = pfxFilePath.substring(0,
				pfxFilePath.lastIndexOf(File.separator));
		File certFile = new File(certFolder);
		File pfxFile = new File(pfxFolder);
		if (!(certFile.exists() && pfxFile.exists())) {
			errorTitle = Messages.remAccErTxtTitle;
			errorMessage = Messages.remAccInvldPath;
			MessageUtil.displayErrorDialog(this.getShell(), errorTitle,
					errorMessage);
			return;
		} else {
			try {
				String alias = Messages.certDlgAlias;
				EncUtilHelper.createCertificate(txtCertFile.getText(),
						txtPFXFile.getText(), alias, txtPwd.getText());

				pfxPassword = txtPwd.getText();
				pfxPath = txtPFXFile.getText();
				certPath = txtCertFile.getText();
				
			} catch (Exception e) {
				Activator.getDefault().log(e.getMessage(), e);
				errorTitle = Messages.remAccErTxtTitle;
				errorMessage = Messages.remAccErCreateCer;
				MessageUtil.displayErrorDialog(this.getShell(), errorTitle,
						errorMessage);
				return;
			}
		}
		super.okPressed();
	}
}