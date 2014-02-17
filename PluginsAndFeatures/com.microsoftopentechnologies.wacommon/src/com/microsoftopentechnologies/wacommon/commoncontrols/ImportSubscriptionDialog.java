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
package com.microsoftopentechnologies.wacommon.commoncontrols;

import java.io.File;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
/**
 * Class constructs dialog which has facility
 * to download and import publish settings file.
 */
public class ImportSubscriptionDialog extends Dialog {
	private Button dwnlPubSetFile;
	private Label path;
	private Text pathTxt;
	private Button browseBtn;
	private Button okButton;
	public static String pubSetFilePath;

	public static String getPubSetFilePath() {
		return pubSetFilePath;
	}

	/**
	 * Constructor.
	 * @param parentShell
	 */
	public ImportSubscriptionDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.impSubDlgTtl);
	}
	
	@Override
	protected Control createButtonBar(Composite parent) {
		GridData gridData = new GridData();
		gridData.verticalAlignment = SWT.FILL;
		gridData.horizontalAlignment = SWT.FILL;
		parent.setLayoutData(gridData);
		Control ctrl = super.createButtonBar(parent);
		okButton = getButton(IDialogConstants.OK_ID);
		okButton.setEnabled(false);
		return ctrl;
	}

	/**
	 * Method creates contents of dialog.
	 */
	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(3, false);
		container.setLayout(gridLayout);
		GridData gridData = new GridData();
		gridData.widthHint = 500;
		container.setLayoutData(gridData);
		// create download publish settings file button
		createDownloadPubSetFileButton(container);
		// create separator
		createHorizontalSeparator(container);
		// create Path components
		createPathComponents(container);
		return super.createContents(parent);
	}

	/**
	 * Method creates path UI components and respective listeners.
	 * @param container
	 */
	private void createPathComponents(Composite container) {
		path = new Label(container, SWT.LEFT);
		GridData gridData = new GridData();
		gridData.horizontalIndent = 10;
		gridData.verticalIndent = 10;
		path.setLayoutData(gridData);
		path.setText(Messages.pathLbl);

		pathTxt =  new Text(container, SWT.SINGLE | SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.END;
		gridData.verticalIndent = 10;
		gridData.grabExcessHorizontalSpace = true;
		gridData.widthHint = 350;
		pathTxt.setLayoutData(gridData);
		pathTxt.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent event) {
				if (okButton != null) {
					String path = pathTxt.getText().trim();
					if (path.isEmpty()) {
						okButton.setEnabled(false);
					} else {
						File file = new File(path);
						if (file.exists() && file.isFile()) {
							okButton.setEnabled(true);
						} else {
							okButton.setEnabled(false);
						}
					}
				}
			}
		});

		browseBtn = new Button(container, SWT.PUSH | SWT.CENTER);
		gridData = new GridData();
		gridData.widthHint = 100;
		gridData.verticalIndent = 10;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		browseBtn.setText(Messages.newCertDlgBrwsBtn);
		browseBtn.setLayoutData(gridData);
		browseBtn.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				browseBtnListener();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {

			}
		});
	}

	/**
	 * Method creates horizontal line as separator.
	 * @param parent
	 */
	public static void createHorizontalSeparator(Composite parent) {
		Label seperator =
				new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.verticalIndent = 10;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		seperator.setLayoutData(gridData);
	}
	
	/**
	 * Method creates download button and listener.
	 * @param parent
	 */
	private void createDownloadPubSetFileButton(Composite parent) {
		dwnlPubSetFile = new Button(parent, SWT.PUSH | SWT.CENTER);
		GridData gridData = new GridData();
		gridData.horizontalIndent = 10;
		gridData.verticalIndent = 10;
		gridData.horizontalSpan = 3;
		gridData.widthHint = 260;
		dwnlPubSetFile.setText(Messages.dwnlPubSetFile);
		dwnlPubSetFile.setLayoutData(gridData);
		dwnlPubSetFile.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				downloadBtnListener();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {

			}
		});
	}

	@Override
	protected void okPressed() {
		pubSetFilePath = pathTxt.getText().trim();
		super.okPressed();
	}
	
	@Override
	protected void cancelPressed() {
		pubSetFilePath = "";
		super.cancelPressed();
	}

	/**
	 * Browse button for publish settings file
	 * selection using file system.
	 */
	private void browseBtnListener() {
		FileDialog dialog = new FileDialog(this.getShell());
		String[] extensions = new String [2];
		extensions[0] = "*.publishsettings";
		extensions[1] = "*.*";
		dialog.setFilterExtensions(extensions);
		String file = dialog.open();
		if (file != null) {
			pathTxt.setText(file);
		}
	}

	/**
	 * Download button to download
	 * publish settings file selection using file system.
	 */
	private void downloadBtnListener() {
		PublishSettingsDialog dialog =
				new PublishSettingsDialog(getShell());
		dialog.open();
	}
}
