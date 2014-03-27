/**
* Copyright 2014 Microsoft Open Technologies, Inc.
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
package com.persistent.winazureroles;

import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.interopbridges.tools.windowsazure.WindowsAzureCertificate;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.microsoftopentechnologies.wacommon.commoncontrols.NewCertificateDialog;
import com.microsoftopentechnologies.wacommon.commoncontrols.NewCertificateDialogData;
import com.microsoftopentechnologies.wacommon.utils.CerPfxUtil;
import com.microsoftopentechnologies.wacommon.utils.EncUtilHelper;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
import com.persistent.util.WAEclipseHelper;

public class CertificateDialog extends Dialog {
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
		newShell.setText(Messages.certAddTtl);
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		GridData gridData = new GridData();
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 5;
		gridData.verticalAlignment = SWT.FILL;
		gridData.horizontalAlignment = SWT.LEFT_TO_RIGHT;
		gridData.horizontalIndent = 0;
		gridData.verticalIndent = 10;
		parent.setLayout(gridLayout);
		parent.setLayoutData(gridData);

		Button btnImport = new Button(parent, SWT.PUSH | SWT.CENTER);
		btnImport.setText(Messages.importBtn);
		gridData = new GridData();
		gridData.widthHint = 75;
		gridData.horizontalAlignment = SWT.BEGINNING;
		btnImport.setLayoutData(gridData);
		btnImport.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				importBtnListner();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		Button btnCreate = new Button(parent, SWT.PUSH | SWT.CENTER);
		btnCreate.setText(Messages.newBtn);
		btnCreate.setLayoutData(gridData);
		btnCreate.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				newBtnListener();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
			}
		});

		Button btnNo = new Button(parent, SWT.PUSH | SWT.CENTER);
		btnNo.setVisible(false);
		gridData = new GridData();
		gridData.widthHint = 25;
		btnNo.setLayoutData(gridData);

		createButton(parent, IDialogConstants.OK_ID,
				IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
		okButton = getButton(IDialogConstants.OK_ID);
		okButton.setEnabled(false);
		return parent;
	}

	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		container.setLayout(gridLayout);
		GridData gridData = new GridData();
		gridData.widthHint = 400;
		container.setLayoutData(gridData);

		createNameComponent(container);
		createThumbprintComponent(container);

		return super.createContents(parent);
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
		gridData.horizontalAlignment = GridData.END;
		gridData.verticalIndent = 10;
		gridData.grabExcessHorizontalSpace = true;
		gridData.widthHint = 300;
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
		gridData.verticalIndent = 5;
		lblValue.setLayoutData(gridData);
		lblValue.setText(String.format("%s%s", Messages.colThumb, ":"));

		txtThumb = new Text(container, SWT.SINGLE | SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.END;
		gridData.verticalIndent = 5;
		gridData.grabExcessHorizontalSpace = true;
		gridData.widthHint = 300;
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
		if (WAEclipseHelper.isAlphaNumericUnderscore(name)) {
			for (Iterator<Entry<String, WindowsAzureCertificate>> iterator =
					mapCert.entrySet().iterator();
					iterator.hasNext();) {
				WindowsAzureCertificate cert = iterator.next().getValue();
				if (cert.getName().trim().equalsIgnoreCase(name)
						|| cert.getFingerPrint().trim().equalsIgnoreCase(thumb)) {
					retVal = false;
					PluginUtil.displayErrorDialog(
							this.getShell(),
							Messages.certErrTtl,
							Messages.certAddErrMsg);
					break;
				}
			}
		} else {
			retVal = false;
			PluginUtil.displayErrorDialog(
					this.getShell(),
					Messages.certErrTtl,
					Messages.certRegMsg);
		}
		return retVal;
	}

	private boolean isNameAlreadyPresent(String name) {
		boolean isPresent = false;
		for (Iterator<Entry<String, WindowsAzureCertificate>> iterator =
				mapCert.entrySet().iterator();
				iterator.hasNext();) {
			WindowsAzureCertificate cert = iterator.next().getValue();
			if (cert.getName().trim().equalsIgnoreCase(name)) {
				isPresent = true;
				break;
			}
		}
		return isPresent;
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
		String path = CerPfxUtil.importCerPfx(this.getShell(),
				WAEclipseHelper.getSelectedProject().getLocation().toPortableString());
		String password = null;
		boolean proceed = true;
		if (path != null && path.endsWith(".pfx")) {
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
					populateCertName(removeSpaceFromCN(cert.getSubjectDN().getName()));
				}
				String thumbprint = "";
				try {
					if (path.endsWith(".cer")) {
						thumbprint = EncUtilHelper.getThumbPrint(path);
					} else {
						thumbprint = CerPfxUtil.getThumbPrint(cert);
					}
				} catch (Exception e) {
					PluginUtil.displayErrorDialog(
							this.getShell(),
							Messages.certErrTtl,
							Messages.certImpEr);
				}
				txtThumb.setText(thumbprint);
			}
		}
	}

	private String removeSpaceFromCN(String nameParam) {
		String name = nameParam;
		name = name.replaceAll("\\s+", "");
		return name.substring(name.indexOf("=") + 1);
	}
	/**
	 * Method checks if certificate name is already
	 * used then make it unique by concatenating current date.
	 * @param certName
	 */
	private void populateCertName(String certNameParam) {
		String certName = certNameParam;
		if (isNameAlreadyPresent(certName)) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
			Date now = new Date();
			certName = certName + dateFormat.format(now);
		}
		txtName.setText(certName);
	}

	private void newBtnListener() {
		NewCertificateDialogData data = new NewCertificateDialogData();
		NewCertificateDialog dialog =
				new NewCertificateDialog(this.getShell(), data);
		if (dialog.open() == Window.OK) {
			if (txtName.getText().isEmpty()) {
				populateCertName(removeSpaceFromCN(data.getCnName()));
			}
			try {
				txtThumb.setText(EncUtilHelper.
						getThumbPrint(data.getCerFilePath()));
			} catch (Exception e) {
				PluginUtil.displayErrorDialog(
						this.getShell(),
						Messages.certErrTtl,
						Messages.certImpEr);
			}
		}
	}
}
