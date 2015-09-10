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
package com.persistent.contextmenu;

import org.eclipse.jface.window.Window;
import java.io.File;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;

import waeclipseplugin.Activator;

import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.persistent.util.WAEclipseHelper;
/**
 * Add Worker Role class will be invoked
 * when sub menu "Add Role..." of fly out menu
 * of azure deployment project
 * is clicked.
 * Dialog to add worker role will be opened.
 */
public class AddWorkerRole extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent arg0)
			throws ExecutionException {
		try {
			IProject selProject = WAEclipseHelper.getSelectedProject();
			String path = selProject.getLocation().toPortableString();
			WindowsAzureProjectManager waProjManager = WindowsAzureProjectManager.
					load(new File(path));
			List<WindowsAzureRole> listRoles = waProjManager.getRoles();
			WindowsAzureRole windowsAzureRole = WAEclipseHelper.
					prepareRoleToAdd(waProjManager);
			int btnID = WAEclipseHelper.
					openRolePropertyDialog(windowsAzureRole,
							Messages.genPgId, "");
			if (btnID == Window.CANCEL) {
				listRoles.remove(windowsAzureRole);
			}
			WAEclipseHelper.refreshWorkspace(
					com.persistent.winazureroles.Messages.rolsRefTitle,
					com.persistent.winazureroles.Messages.rolsRefMsg);
		} catch (Exception e) {
			Activator.getDefault().log(e.getMessage());
		}
		return null;
	}
}
