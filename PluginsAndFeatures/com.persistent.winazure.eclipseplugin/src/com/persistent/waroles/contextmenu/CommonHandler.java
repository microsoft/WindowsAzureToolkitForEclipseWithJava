/**
 * Copyright 2012 Persistent Systems Ltd.
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
package com.persistent.waroles.contextmenu;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import waeclipseplugin.Activator;

import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.persistent.util.WAEclipseHelper;
/**
 * Common handler class will be invoked
 * when sub menu of fly out menu of worker role is clicked.
 * According to Command Id of selected sub menu,
 * property dialog of worker role will be opened
 * with selected & desired property page active.
 */
public class CommonHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event)
			throws ExecutionException {
		WindowsAzureRole wARole = Activator.getDefault().getWaRole();
		String commandName = "";
		commandName = event.getCommand().getId();
		if (commandName.equals(Messages.cachCmdId)) {
			// Caching
			WAEclipseHelper.openRolePropertyDialog(wARole,
					Messages.cachPgId);
		} else if (commandName.equals(Messages.cmpntCmdId)) {
			// Components
			WAEclipseHelper.openRolePropertyDialog(wARole,
					Messages.cmpntPgId);
		} else if (commandName.equals(Messages.dbgCmdId)) {
			// Debugging
			WAEclipseHelper.openRolePropertyDialog(wARole,
					Messages.dbgPgId);
		} else if (commandName.equals(Messages.endPtCmdId)) {
			// Endpoints
			WAEclipseHelper.openRolePropertyDialog(wARole,
					Messages.endPtPgId);
		} else if (commandName.equals(Messages.envVarCmdId)) {
			// Environment Variables
			WAEclipseHelper.openRolePropertyDialog(wARole,
					Messages.envVarPgId);
		} else if (commandName.equals(Messages.ldBalCmdId)) {
			// Load Balancing
			WAEclipseHelper.openRolePropertyDialog(wARole,
					Messages.ldBalPgId);
		} else if (commandName.equals(Messages.lclStrCmdId)) {
			// Local Storage
			WAEclipseHelper.openRolePropertyDialog(wARole,
					Messages.lclStrPgId);
		} else if (commandName.equals(Messages.srvConfCmdId)) {
			// Server Configuration
			WAEclipseHelper.openRolePropertyDialog(wARole,
					Messages.srvConfPgId);
		} else {
			// General
			WAEclipseHelper.openRolePropertyDialog(wARole,
					Messages.genPgId);
		}
		return null;
	}
}
