/**
 * Copyright 2013 Persistent Systems Ltd.
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
 * of windows azure deployment project
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
							Messages.genPgId);
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
