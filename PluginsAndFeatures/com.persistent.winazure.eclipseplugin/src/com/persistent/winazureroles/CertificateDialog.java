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
import java.security.cert.X509Certificate;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import waeclipseplugin.Activator;

import com.interopbridges.tools.windowsazure.WindowsAzureCertificate;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.microsoftopentechnologies.azurecommons.exception.AzureCommonsException;
import com.microsoftopentechnologies.azurecommons.roleoperations.CertificateDialogUtilMethods;
import com.microsoftopentechnologies.wacommon.commoncontrols.NewCertificateDialog;
import com.microsoftopentechnologies.wacommon.commoncontrols.NewCertificateDialogData;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
import com.microsoftopentechnologies.azurecommons.wacommonutil.CerPfxUtil;
import com.persistent.util.WAEclipseHelper;

public class CertificateDialog extends TitleAreaDialog {
	private Text txtName;
	private Text txtThumb;
	private Map<String, WindowsAzureCertificate> mapCert;
	private WindowsAzureRole waRole;
	private Button okButton;
	public static String certNameAdded = "";

	protected CertificateDialog(Shell parentShell,
			Map<String, WindowsAzureCertificate> mapCert,
			WindowsAzureRole waRole) {
		super(parentShell);
		this.mapCert = mapCert;
		this.waRole = waRole;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.certTtl);
		Image image;
		try {
			URL imgUrl = Activator.getDefault().
					getBundle().getEntry(Messages.certDlgImg);
			URL imgFileURL = FileLocator.toFileURL(imgUrl);
			URL path = FileLocator.resolve(imgFileURL);
			String imgpath = path.getFile();
			image = new Image(null, new FileInputStream(imgpath));
			setTitleImage(image);
		} catch (Exception e) {
			PluginUtil.displayErrorDialogAndLog(getShell(),
					Messages.genErrTitle,
					Messages.lclDlgImgErr, e);
		}
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button btnImport = createButton(parent, 2,
				Messages.importBtn, false);
		btnImport.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				importBtnListner();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		Button btnCreate = createButton(parent, 3,
				Messages.newBtn, false);
		btnCreate.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				newBtnListener();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
			}
		});

		Button btnNo = createButton(parent, 4,
				Messages.importBtn, false);
		btnNo.setVisible(false);

		createButton(parent, IDialogConstants.OK_ID,
				IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
		okButton = getButton(IDialogConstants.OK_ID);
		okButton.setEnabled(false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(Messages.certAddTtl);
		setMessage(Messages.certMsg);
		// Display help contents
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				"com.persistent.winazure.eclipseplugin."
						+ "windows_azure_certificates_page");
		Activator.getDefault().setSaved(false);

		Composite container = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginBottom = 25;
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
		container.setLayout(gridLayout);
		container.setLayoutData(gridData);

		createNameComponent(container);
		createThumbprintComponent(container);

		return super.createDialogArea(parent);
	}

	/**
	 * Creates label and text box for variable's name.
	 *
	 * @param container
	 */
	private void createNameComponent(Composite container) {
		Label lblName = new Label(container, SWT.LEFT);
		GridData gridData = new GridData();
		gridData.horizontalIndent = 5;
		gridData.verticalIndent = 10;
		lblName.setLayoutData(gridData);
		lblName.setText(Messages.adRolName);

		txtName = new Text(container, SWT.SINGLE | SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalIndent = 10;
		gridData.widthHint = 350;
		txtName.setLayoutData(gridData);
		txtName.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent event) {
				enableDisableOkBtn();
			}
		});
	}

	private void createThumbprintComponent(Composite container) {
		Label lblValue = new Label(container, SWT.LEFT);
		GridData gridData = new GridData();
		gridData.horizontalIndent = 5;
		gridData.verticalIndent = 10;
		lblValue.setLayoutData(gridData);
		lblValue.setText(String.format("%s%s", Messages.colThumb, ":"));

		txtThumb = new Text(container, SWT.SINGLE | SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalIndent = 10;
		gridData.widthHint = 350;
		txtThumb.setLayoutData(gridData);

		txtThumb.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent event) {
				enableDisableOkBtn();
			}
		});
	}

	private void enableDisableOkBtn() {
		if (okButton != null) {
			if (txtThumb.getText().trim().isEmpty()
					|| txtName.getText().trim().isEmpty()) {
				okButton.setEnabled(false);
			} else {
				okButton.setEnabled(true);
			}
		}
	}

	private boolean validateNameAndThumbprint(String name, String thumb) {
		boolean retVal = true;
		try {
			retVal = CertificateDialogUtilMethods.validateNameAndThumbprint(name, thumb, mapCert);
		} catch (AzureCommonsException e) {
			PluginUtil.displayErrorDialogAndLog(getShell(), Messages.genErrTitle, e.getMessage(), e);
		}
		return retVal;
	}

	@Override
	protected void okPressed() {
		boolean retVal = true;
		try {
			String name = txtName.getText().trim();
			String thumb = txtThumb.getText().trim();
			retVal = validateNameAndThumbprint(name, thumb);
			if (retVal) {
				waRole.addCertificate(name, thumb.toUpperCase());
				certNameAdded = name;
			}
		} catch (Exception ex) {
			PluginUtil.displayErrorDialogAndLog(
					this.getShell(),
					Messages.rolsErr,
					Messages.adRolErrMsgBox1
					+ Messages.adRolErrMsgBox2, ex);
			retVal = false;
		}
		if (retVal) {
			super.okPressed();
		}
	}
	/**
	 * Method to remember which certificate got added recently.
	 * @return
	 */
	public static String getNewlyAddedCert() {
		return certNameAdded;
	}

	private void importBtnListner() {
		String path = com.microsoftopentechnologies.wacommon.utils.CerPfxUtil.importCerPfx(this.getShell(),
				WAEclipseHelper.getSelectedProject().getLocation().toPortableString());
		String password = null;
		boolean proceed = true;
		if (path != null && (path.endsWith(".pfx") || path.endsWith(".PFX"))) {
			SimplePfxPwdDlg dlg = new SimplePfxPwdDlg(this.getShell(), path);
			if (dlg.open() == Window.OK) {
				password = dlg.getPwd();
			} else {
				proceed = false;
			}
		}
		if (proceed) {
			X509Certificate cert = CerPfxUtil.getCert(path, password);
			if (cert != null) {
				if (txtName.getText().isEmpty()) {
					populateCertName(
							CertificateDialogUtilMethods.
							removeSpaceFromCN(cert.getSubjectDN().getName()));
				}
				String thumbprint = "";
				try {
					thumbprint = CerPfxUtil.getThumbPrint(cert);					
				} catch (Exception e) {
					PluginUtil.displayErrorDialogAndLog(
							this.getShell(),
							Messages.certErrTtl,
							Messages.certImpEr, e);
				}
				txtThumb.setText(thumbprint);
			}
		}
	}

	/**
	 * Method checks if certificate name is already
	 * used then make it unique by concatenating current date.
	 * @param certName
	 */
	private void populateCertName(String certNameParam) {
		txtName.setText(CertificateDialogUtilMethods.populateCertName(certNameParam, mapCert));
	}

	private void newBtnListener() {
		NewCertificateDialogData data = new NewCertificateDialogData();
		String jdkPath = "";
		try {
			jdkPath = waRole.getJDKSourcePath();
		} catch (Exception e) {
			jdkPath = "";
		}
		NewCertificateDialog dialog =
				new NewCertificateDialog(this.getShell(), data, jdkPath);
		if (dialog.open() == Window.OK) {
			if (txtName.getText().isEmpty()) {
				populateCertName(
						CertificateDialogUtilMethods.
						removeSpaceFromCN(data.getCnName()));
			}
			try {
				txtThumb.setText(CerPfxUtil.
						getThumbPrint(data.getCerFilePath()));
			} catch (Exception e) {
				PluginUtil.displayErrorDialogAndLog(
						this.getShell(),
						Messages.certErrTtl,
						Messages.certImpEr, e);
			}
		}
	}
}
