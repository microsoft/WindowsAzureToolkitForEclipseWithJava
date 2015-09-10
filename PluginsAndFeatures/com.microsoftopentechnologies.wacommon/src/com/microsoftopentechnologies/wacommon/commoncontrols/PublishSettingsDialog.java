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

package com.microsoftopentechnologies.wacommon.commoncontrols;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
import com.microsoftopentechnologies.azurecommons.wacommonutil.PreferenceSetUtil;
/**
 * Class configure publish settings download dialog.
 * Opens browser to download publish settings file.
 */
public class PublishSettingsDialog extends Dialog {
	
	/**
	 * Constructor.
	 * @param parentShell
	 */
	protected PublishSettingsDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.mgmtPortalShell);
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		return null;
	}

	@Override
	protected Button createButton(Composite parent,
			int id, String label,
			boolean defaultButton) {
		return null;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Browser browser = new Browser(parent, SWT.BORDER | SWT.FILL);
		FillLayout layout = new FillLayout();
		browser.setLayout(layout);
		browser.setSize(900, 600);
		try {
			String filePath = PluginUtil.getPrefFilePath();
			browser.setUrl(PreferenceSetUtil.
					getSelectedPublishSettingsURL(
							PreferenceSetUtil.
							getSelectedPreferenceSetName(filePath), filePath));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return parent;
	}
}
