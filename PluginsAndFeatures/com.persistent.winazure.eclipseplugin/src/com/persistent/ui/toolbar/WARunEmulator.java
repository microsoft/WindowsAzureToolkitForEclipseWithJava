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
package com.persistent.ui.toolbar;

import java.io.File;

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

import waeclipseplugin.Activator;

import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzurePackageType;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.persistent.util.WAEclipseHelper;
/**
 * This class runs selected Windows Azure project.
 * in the Windows Azure Emulator
 */
public class WARunEmulator extends AbstractHandler {

	private String errorTitle;
	private String errorMessage;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Get selected WA project
		IProject selProject = WAEclipseHelper.getSelectedProject();

		try {
			 WindowsAzureProjectManager waProjManager = WindowsAzureProjectManager.
					load(new File(selProject.getLocation().toOSString()));
			if (waProjManager.getPackageType().equals(WindowsAzurePackageType.CLOUD)) {
				waProjManager.setPackageType(WindowsAzurePackageType.LOCAL);
				waProjManager.save();
			}
			final IProject selProj = selProject;
			Job job = new Job(Messages.runJobTtl) {
			    @Override
			    protected IStatus run(IProgressMonitor monitor) {
			        monitor.beginTask(Messages.runJobTtl, IProgressMonitor.UNKNOWN);
			        try {
			        	WindowsAzureProjectManager waProjMgr = WindowsAzureProjectManager.
			        			load(new File(selProj.getLocation().toOSString()));
						selProj.build(IncrementalProjectBuilder.FULL_BUILD, null);
						waProjMgr.deployToEmulator();
					} catch (WindowsAzureInvalidProjectOperationException e) {
						errorTitle = String.format("%s%s%s", Messages.waEmulator,
								" ", Messages.runEmltrErrTtl);
						errorMessage = String.format("%s%s%s%s", Messages.runEmltrErrMsg,
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
						errorTitle = Messages.bldErrTtl;
						errorMessage = Messages.bldErrMsg;
						Activator.getDefault().log(errorMessage, ex);
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								MessageDialog.openError(null,
										errorTitle, errorMessage);
							}
						});
						return Status.CANCEL_STATUS;
					}				
			        monitor.done();
			        return Status.OK_STATUS;
			    }
			};
			job.schedule();
		} catch (WindowsAzureInvalidProjectOperationException e) {
			errorTitle = String.format("%s%s%s", Messages.waEmulator,
					" ", Messages.runEmltrErrTtl);
			errorMessage = String.format("%s%s%s%s", Messages.runEmltrErrMsg,
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
}
