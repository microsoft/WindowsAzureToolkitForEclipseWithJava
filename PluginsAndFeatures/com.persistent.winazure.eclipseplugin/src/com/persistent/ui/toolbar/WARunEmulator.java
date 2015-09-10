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

import java.io.File;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import waeclipseplugin.Activator;

import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzurePackageType;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.persistent.util.WAEclipseHelper;
/**
 * This class runs selected Azure project.
 * in the Azure Emulator
 */
public class WARunEmulator extends AbstractHandler {

	private String errorTitle;
	private String errorMessage;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Get selected WA project 
		IProject selProject = WAEclipseHelper.getSelectedProject();
		errorTitle = String.format("%s%s%s", Messages.waEmulator,
				" ", Messages.runEmltrErrTtl);
		try {
			WindowsAzureProjectManager waProjManager = WindowsAzureProjectManager.
					load(new File(selProject.getLocation().toOSString()));
			/*
			 * always set WindowsAzureProjectManager object
			 * as we don't update on project selection.
			 */
			Activator.getDefault().setWaProjMgr(waProjManager);
			List<WindowsAzureRole> roleList = waProjManager.getRoles();
			if (roleList.size() > 0) {
				Activator.getDefault().setWaRole(roleList.get(0));
			}
			WindowsAzureRole roleWithoutLocalJdk = performJDKCheck(roleList);
			if (roleWithoutLocalJdk == null) {
				WindowsAzureRole roleWithoutLocalServer = performServerCheck(roleList);
				if (roleWithoutLocalServer == null) {
					if (waProjManager.getPackageType().equals(WindowsAzurePackageType.CLOUD)) {
						waProjManager.setPackageType(WindowsAzurePackageType.LOCAL);	
					}
					waProjManager.save();
					final IProject selProj = selProject;
					Job job = new Job(Messages.runJobTtl) {
						@Override
						protected IStatus run(IProgressMonitor monitor) {
							monitor.beginTask(Messages.runJobTtl, IProgressMonitor.UNKNOWN);
							try {
								WindowsAzureProjectManager waProjMgr = WindowsAzureProjectManager.
										load(new File(selProj.getLocation().toOSString()));
								selProj.build(IncrementalProjectBuilder.FULL_BUILD, null);
								waProjMgr = WindowsAzureProjectManager.
										load(new File(selProj.getLocation().toOSString()));
								if (WAEclipseHelper.isBuildSuccessful(waProjMgr, selProj)) {
									waProjMgr.deployToEmulator();
								} else {
									return Status.CANCEL_STATUS;
								}
							} catch (WindowsAzureInvalidProjectOperationException e) {
								errorMessage = String.format("%s %s%s%s", Messages.runEmltrErrMsg,
										selProj.getName(), " in ", Messages.waEmulator);
								Activator.getDefault().log(errorMessage, e);
								Display.getDefault().syncExec(new Runnable() {
									public void run() {
										MessageDialog.openError(null,
												errorTitle, errorMessage);
									}
								});
								return Status.CANCEL_STATUS;
							} catch (Exception ex) {
								errorMessage = Messages.bldErrMsg;
								Activator.getDefault().log(errorMessage, ex);
								Display.getDefault().syncExec(new Runnable() {
									public void run() {
										MessageDialog.openError(null,
												Messages.bldErrTtl, errorMessage);
									}
								});
								return Status.CANCEL_STATUS;
							}
							monitor.done();
							return Status.OK_STATUS;
						}
					};
					job.schedule();
				} else {
					// show server message dialog
					boolean choice = MessageDialog.openConfirm(new Shell(),
							errorTitle,
							String.format(Messages.noLocalServerMsg, roleWithoutLocalServer.getName()));
					if (choice) {
						Activator.getDefault().setWaRole(roleWithoutLocalServer);
						WAEclipseHelper.openRolePropertyDialog(roleWithoutLocalServer,
								com.persistent.contextmenu.Messages.srvConfPgId, "Server");
					}
				}
			} else {
				// show JDK message dialog
				boolean choice = MessageDialog.openConfirm(new Shell(),
						errorTitle,
						String.format(Messages.noLocalJDKMsg, roleWithoutLocalJdk.getName()));
				if (choice) {
					Activator.getDefault().setWaRole(roleWithoutLocalJdk);
					WAEclipseHelper.openRolePropertyDialog(roleWithoutLocalJdk,
							com.persistent.contextmenu.Messages.srvConfPgId, "JDK");
				}
			}
		} catch (Exception e) {
			errorMessage = String.format("%s %s%s%s", Messages.runEmltrErrMsg,
					selProject.getName(), " in ", Messages.waEmulator);
			Activator.getDefault().log(errorMessage, e);
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					MessageDialog.openError(null,
							errorTitle, errorMessage);
				}
			});
		}
		return null;
	}

	private WindowsAzureRole performJDKCheck(List<WindowsAzureRole> roleList)
			throws WindowsAzureInvalidProjectOperationException {
		for (int i = 0; i < roleList.size(); i++) {
			WindowsAzureRole role = roleList.get(i);
			if (role.getJDKSourcePath() != null
					&& role.getJDKSourcePath().isEmpty()
					&& role.getJDKCloudURL() != null
					&& !role.getJDKCloudURL().isEmpty()) {
				// cloud JDK present but local absent
				return role;
			}
		}
		return null;
	}

	private WindowsAzureRole performServerCheck(List<WindowsAzureRole> roleList)
			throws WindowsAzureInvalidProjectOperationException {
		for (int i = 0; i < roleList.size(); i++) {
			WindowsAzureRole role = roleList.get(i);
			if (role.getServerName() != null
					&& role.getServerSourcePath() != null
					&& role.getServerSourcePath().isEmpty()
					&& role.getServerCloudURL() != null
					&& !role.getServerCloudURL().isEmpty()) {
				// cloud JDK present but local absent
				return role;
			}
		}
		return null;
	}
}
