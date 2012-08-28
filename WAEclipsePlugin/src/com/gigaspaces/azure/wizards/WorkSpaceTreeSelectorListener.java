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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class WorkSpaceTreeSelectorListener implements SelectionListener {

	private final Shell shell;
	private final String filter;
	private final IProject selProject;
	private final Text target;

	public WorkSpaceTreeSelectorListener(Shell shell, String filter, Text target, IProject project) {

		if (shell == null || target == null)
			throw new RuntimeException();
		// type
		this.shell = shell;
		this.filter = filter;
		this.selProject = project;
		this.target = target;
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent arg0) {
	}

	@Override
	public void widgetSelected(SelectionEvent arg0) {
		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(
				shell, new WorkbenchLabelProvider(),
				new WorkbenchContentProvider());
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		dialog.setInput(root);

		// assign default tree element selection
		if (target.getText() != null) {
			IPath location = Path.fromOSString(target.getText());
			IFile file = root.getFileForLocation(location);
			dialog.setInitialSelection(file);
		}

		dialog.setTitle(Messages.remAccWrkspcTitle);
		dialog.addFilter(new CustomViewerFilter(selProject, filter));

		dialog.setAllowMultiple(false);

		dialog.open();
		Object obj[] = dialog.getResult();
		if (obj != null && obj.length > 0) {
			if (obj[0] instanceof IFile) {
				IFile file = (IFile) obj[0];
				String exactPath = file.getLocation().toOSString();
				if (exactPath.contains(selProject.getLocation().toOSString()
						+ File.separator)) {

					String replaceString = exactPath;

					target.setText(replaceString);
				} else {
					target.setText(exactPath);
				}
			} else {
				target.setText(""); //$NON-NLS-1$
			}
		}

	}

}
