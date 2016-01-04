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
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.wizards.IWizardDescriptor;

import com.microsoftopentechnologies.wacommon.utils.PluginUtil;

/**
 * This class handles the click event on custom context menu of role folder.
 *
 */
public class WANewProject extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent arg0)
			throws ExecutionException {
		try {
			IWizardDescriptor des = PlatformUI.getWorkbench().
					getNewWizardRegistry().
					findWizard(Messages.waWizardId);
			if (des != null) {
				IWizard wizard = des.createWizard();
				WizardDialog wizDialog = new
						WizardDialog(PlatformUI.getWorkbench()
								.getDisplay().getActiveShell(),
								wizard);
				wizDialog.setTitle(wizard.getWindowTitle());
				wizDialog.open();
			}
		} catch (NullPointerException ex) {
			PluginUtil.displayErrorDialogAndLog(new Shell(),
					Messages.errTtl,
					Messages.wzrdCrtErMsg + Messages.instSDK, ex);
		} catch (Exception ex) {
			PluginUtil.displayErrorDialogAndLog(new Shell(),
					Messages.errTtl,
					Messages.wzrdCrtErMsg, ex);
		}
		return null;
	}
}

