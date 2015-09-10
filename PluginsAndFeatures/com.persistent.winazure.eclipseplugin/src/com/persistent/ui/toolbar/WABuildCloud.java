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
import java.io.IOException;

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

import com.interopbridges.tools.windowsazure.WindowsAzurePackageType;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
import com.microsoftopentechnologies.wacommon.utils.WACommonException;
import com.microsoftopentechnologies.azurecommons.wacommonutil.PreferenceSetUtil;
import com.persistent.util.WAEclipseHelper;

/**
 * This class opens deploy folder in windows explorer
 * where the files were built.
 */
public class WABuildCloud extends AbstractHandler {

	private String errorTitle;
	private String errorMessage;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Get selected WA project
		IProject selProject = WAEclipseHelper.getSelectedProject();
		try {
			WindowsAzureProjectManager waProjManager = WindowsAzureProjectManager.
					load(new File(selProject.getLocation().toOSString()));
			
			if (waProjManager.getPackageType().equals(WindowsAzurePackageType.LOCAL)) {
				waProjManager.setPackageType(WindowsAzurePackageType.CLOUD);
			}
			try {
				String path = PluginUtil.getPrefFilePath();
				String prefSetUrl = PreferenceSetUtil.getSelectedPortalURL(
						PreferenceSetUtil.getSelectedPreferenceSetName(path), path);
				/*
				 * Don't check if URL is empty or null.
				 * As if it is then we remove "portalurl" attribute
				 * from package.xml. 
				 */
				waProjManager.setPortalURL(prefSetUrl);
			} catch (WACommonException e1) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						MessageDialog.openError(null,
								Messages.errTtl,
								Messages.getPrefUrlErMsg);
					}
				});
			}
			waProjManager.save();

			waProjManager = WindowsAzureProjectManager.
					load(new File(selProject.getLocation().toOSString()));

			final IProject selProj = selProject;
            Job job = new Job(Messages.cldJobTtl) {
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    monitor.beginTask(Messages.cldJobTtl, IProgressMonitor.UNKNOWN);
                    String dplyFolderPath = "";
					try {
						selProj.build(IncrementalProjectBuilder.FULL_BUILD, null);

						WindowsAzureProjectManager waProjMngr = WindowsAzureProjectManager.
								load(new File(selProj.getLocation().toOSString()));

						dplyFolderPath = WAEclipseHelper.
								getDeployFolderPath(waProjMngr, selProj);
						String bldFlFilePath = String.format("%s%s%s",
								dplyFolderPath,
								File.separator,
								com.persistent.util.Messages.bldErFileName);
						File buildFailFile = new File(bldFlFilePath);
						File deployFile = new File(dplyFolderPath);

						if (deployFile.exists() && deployFile.isDirectory() 
								&& deployFile.listFiles().length > 0
								&& !buildFailFile.exists()) {
							String[] cmd = {"explorer.exe", "\""+dplyFolderPath+"\""};
							new ProcessBuilder(cmd).start();
						} else {
							return Status.CANCEL_STATUS;
						}
						waProjMngr.save();
					} catch (IOException e) {
						errorMessage = String.format("%s %s",
								Messages.dplyFldErrMsg,
								dplyFolderPath);
						Activator.getDefault().log(errorMessage, e);
						return Status.CANCEL_STATUS;
					} catch (Exception e) {
						errorTitle = Messages.bldErrTtl;
						errorMessage = Messages.bldErrMsg;
						Activator.getDefault().log(errorMessage, e);
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
		} catch (Exception e) {
			errorTitle = Messages.bldCldErrTtl;
			errorMessage = String.format("%s %s", Messages.bldCldErrMsg, selProject.getName());
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
