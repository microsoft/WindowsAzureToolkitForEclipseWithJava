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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;


public class FileDialogDelegator {
	 
	private final Shell shell;
	private final FileDialog dialog;
 
	public FileDialogDelegator(Shell parent) {
		this.shell = parent;
		this.dialog = new FileDialog(parent);
	}
 
	public void setFilterNames(String[] names) {
		dialog.setFilterNames(names);
	}
 
	public void setFilterExtensions(String[] extensions) {
		dialog.setFilterExtensions(extensions);
	}
 
	public String open() {
		if (isInTestMode()) {
			InputDialog dialog = new InputDialog(shell, Messages.inputDialog, Messages.enterPath, "", null);  //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$
			if (dialog.open() == IStatus.OK) { // open dialog and wait for return status code.
				return dialog.getValue();
			} else {
				return null;
			}
 
		} else {
			return dialog.open();
		}
	}
 
	private boolean isInTestMode() {
		return System.getProperty("com.gigaspaces.azure.isTest", "false").equals("true"); //$NON-NLS-1$  
	}
}
