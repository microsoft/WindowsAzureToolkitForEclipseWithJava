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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import waeclipseplugin.Activator;

import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzurePackageType;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.persistent.util.MessageUtil;

public class DeployWizard extends Wizard {
	private SignInPage signInPage;
	private RemoteDesktopPage rdpPage;
	private IProject selectedProject;
	
	private WindowsAzurePackageType deployMode = WindowsAzurePackageType.CLOUD;
	private static Map<String, Boolean> rememberMydecisions = new HashMap<String, Boolean>();
	private static Map<String, Integer> decisions = new HashMap<String, Integer>();


	public DeployWizard() {
		super();
		selectedProject = WizardCacheManager.getCurrentSelectedProject();
		try {
			setWindowTitle(Messages.publishWiz);
			setDefaultPageImageDescriptor(Activator.getImageDescriptor(Activator.DEPLOY_IMAGE));
		} catch (IOException e) {
			Activator.getDefault().log(Messages.error,e);
		}
	}
	
	public IProject getSelectedProject() {
		return selectedProject;
	}

	@Override
	public void addPages() {
		if (selectedProject != null) {
			signInPage = new SignInPage();
			signInPage.setSelectedProject(selectedProject);
			addPage(signInPage);			
			rdpPage = new RemoteDesktopPage();
			rdpPage.setSelectedProject(selectedProject);
			addPage(rdpPage);
		}
	}

	
	@Override
	public boolean needsPreviousAndNextButtons() {
		return true;
	}

	@Override
	public boolean performFinish() {
				
		try {
			final WindowsAzureProjectManager waProjManager = WindowsAzureProjectManager.load(new File(selectedProject.getLocation().toOSString()));

			if (rdpPage.getRdpSelection()) {
				waProjManager.setRemoteAccessAllRoles(true);
			}
			else {
				waProjManager.setRemoteAccessAllRoles(false);
			}
			
			WindowsAzureBuildProjectJob buildProjectJob = new WindowsAzureBuildProjectJob("Building Project");
			buildProjectJob.setManager(waProjManager);
			buildProjectJob.setShell(getShell());
			buildProjectJob.schedule();
			
			buildProjectJob.addJobChangeListener(new JobChangeAdapter() {
				public void done(IJobChangeEvent event) {
					if (event.getResult().isOK()) {
						Job job = new WindowsAzureDeploymentJob(Messages.deployingToAzure,selectedProject);
						job.addJobChangeListener(new JobChangeAdapter() {
							
							public void done(IJobChangeEvent event) {
								if (!event.getResult().isOK()) {
									
									Display.getDefault().asyncExec(new Runnable() {
										
										@Override
										public void run() {
											MessageDialog.openInformation(getShell(), Messages.interrupt, Messages.deploymentInterrupted);
										}
									});
								}
							}
							
						});
						job.schedule();
					}
				}
			});
			
		} catch (WindowsAzureInvalidProjectOperationException e) {
			MessageUtil.displayErrorDialog(getShell(), "Build Failure", "An error occured while loading project, please try again");
			return false;
		}

		return true;
	}
		
	@Override
	public boolean needsProgressMonitor() {		
		return true;
	}
		
	private class WindowsAzureBuildProjectJob extends Job {
		
		private Shell shell;
		private WindowsAzureProjectManager waProjManager;

		public WindowsAzureBuildProjectJob(String name) {
			super(name);
		}
		
		public void setShell(Shell shell) {
			this.shell = shell;
		}
		
		public void setManager(WindowsAzureProjectManager manager) {
			this.waProjManager = manager;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			
			MessageDialogWithToggle dialog = null;
			
			final AtomicReference<MessageDialogWithToggle> dialogRef = new AtomicReference<MessageDialogWithToggle>();
			
			String projectName = selectedProject.getName();
			
			Boolean rememberMyDecisionForSelectedProject = rememberMydecisions.get(projectName);
			
			if (rememberMyDecisionForSelectedProject == null || rememberMyDecisionForSelectedProject == false) {
				
				Display.getDefault().syncExec(new Runnable() {
					
					@Override
					public void run() {
						MessageDialogWithToggle dialog = MessageDialogWithToggle.open(
								MessageDialog.QUESTION, shell,
								Messages.deplConfirmConfigChangeMsg,
								Messages.deplFullProjBuildConfirmMsg,
								Messages.deplRememberMyDecisionMsg, false, null, null,
								SWT.SHEET);	
						dialogRef.set(dialog);
					}
				});				
			}
			
			dialog = dialogRef.get();

			if (dialog != null) { // user did not want to remember his decision for this project. dialog was opened.
				
				boolean toogleState = dialog.getToggleState();
				if (toogleState == true) {
					rememberMydecisions.put(projectName, true);
				}
				else {
					rememberMydecisions.put(projectName, false);
				}
				decisions.put(projectName, dialog.getReturnCode());
			}
			if  (decisions.get(projectName) == IDialogConstants.YES_ID) {				
				monitor.beginTask(String.format(Messages.buildingProjTask,selectedProject.getName()), IProgressMonitor.UNKNOWN);
				try {
					waProjManager.setPackageType(deployMode);
					selectedProject.build(IncrementalProjectBuilder.CLEAN_BUILD,monitor);
					waProjManager.save();
					selectedProject.build(IncrementalProjectBuilder.INCREMENTAL_BUILD,null);
					selectedProject.refreshLocal(IResource.DEPTH_INFINITE, monitor);
				} catch (Exception e) {
					Activator.getDefault().log(Messages.error, e);
					super.setName("");
					monitor.done();
					return Status.CANCEL_STATUS;
				}
				super.setName("");
				monitor.done();
				return Status.OK_STATUS;
			}
			return Status.OK_STATUS;
		}
	}


}