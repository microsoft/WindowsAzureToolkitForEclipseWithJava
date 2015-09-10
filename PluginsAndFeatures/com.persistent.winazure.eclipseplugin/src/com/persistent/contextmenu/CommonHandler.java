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
					Messages.cachPgId, "");
		} else if (commandName.equals(Messages.certCmdId)) {
			// Certificates
			WAEclipseHelper.openRolePropertyDialog(wARole,
					Messages.certPgId, "");
		} else if (commandName.equals(Messages.cmpntCmdId)) {
			// Components
			WAEclipseHelper.openRolePropertyDialog(wARole,
					Messages.cmpntPgId, "");
		} else if (commandName.equals(Messages.dbgCmdId)) {
			// Debugging
			WAEclipseHelper.openRolePropertyDialog(wARole,
					Messages.dbgPgId, "");
		} else if (commandName.equals(Messages.endPtCmdId)) {
			// Endpoints
			WAEclipseHelper.openRolePropertyDialog(wARole,
					Messages.endPtPgId, "");
		} else if (commandName.equals(Messages.envVarCmdId)) {
			// Environment Variables
			WAEclipseHelper.openRolePropertyDialog(wARole,
					Messages.envVarPgId, "");
		} else if (commandName.equals(Messages.ldBalCmdId)) {
			// Load Balancing
			WAEclipseHelper.openRolePropertyDialog(wARole,
					Messages.ldBalPgId, "");
		} else if (commandName.equals(Messages.lclStrCmdId)) {
			// Local Storage
			WAEclipseHelper.openRolePropertyDialog(wARole,
					Messages.lclStrPgId, "");
		} else if (commandName.equals(Messages.srvConfCmdId)) {
			// Server Configuration
			WAEclipseHelper.openRolePropertyDialog(wARole,
					Messages.srvConfPgId, "JDK");
		} else if (commandName.equals(Messages.sslCmdId)) {
			// Server Configuration
			WAEclipseHelper.openRolePropertyDialog(wARole,
					Messages.sslPgId, "");
		} else {
			// General
			WAEclipseHelper.openRolePropertyDialog(wARole,
					Messages.genPgId, "");
		}
		return null;
	}
}
