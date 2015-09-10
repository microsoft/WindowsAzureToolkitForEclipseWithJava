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
package com.persistent.ui.toolbar;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;

import waeclipseplugin.Activator;

import com.microsoftopentechnologies.wacommon.commoncontrols.NewCertificateDialog;
import com.microsoftopentechnologies.wacommon.commoncontrols.NewCertificateDialogData;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
/**
 * This class creates new self signed certificates.
 */
public class WANewCertificate extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			NewCertificateDialogData data = new NewCertificateDialogData();
			/*
			 * third parameter is jdkPath
			 * as its toolbar button, do not refer any project for JDK path
			 * just pass empty string.
			 */
	        NewCertificateDialog dialog =	new NewCertificateDialog(new Shell(), data, "");
	        // Open the dialog
	        dialog.open();
		} catch (Exception e) {
			PluginUtil.displayErrorDialogAndLog(new Shell(), Messages.newCertErrTtl,
												Messages.newCertMsg, e);
			Activator.getDefault().log(Messages.newCertMsg, e);
		}
		return null;
	}
}
