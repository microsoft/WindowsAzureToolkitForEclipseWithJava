/**
 * Copyright 2011 Persistent Systems Ltd.
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
package com.persistent.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import waeclipseplugin.Activator;

import com.interopbridges.tools.windowsazure.WindowsAzureConstants;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;

/**
 * Listens to changes to resources in the workspace.
 *
 */

public class WAResourceChangeListener implements IResourceChangeListener {

	private String  errorTitle;
	private String  errorMessage;
	private boolean installSDK;
	/**
	 * Gets called when resource change takes place.
	 */
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta resourcedelta = event.getDelta();
		IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {
			@Override
			public boolean visit(IResourceDelta delta) throws CoreException {
				IResource resource = delta.getResource();
				IProject project = resource.getProject();
				boolean isNature = false;
				//Check if project is of required nature
				if (project != null && project.isOpen()) {
					isNature = project.hasNature(
							Messages.stUpProjNature);
				}
				if (isNature) {
					handleResourceChange(delta);
				}
				return true;
			}
		};
		try {
			resourcedelta.accept(visitor);
			WorkspaceJob job = new WorkspaceJob(
					Messages.resCLJobName) {

				@Override
				public IStatus runInWorkspace(IProgressMonitor monitor)
						throws CoreException {
					refreshWorkspace();
					return Status.OK_STATUS;
				}
			};
			job.schedule();
		} catch (CoreException e) {
			//This is not a user initiated task
			//So user should not get any exception prompt.
			Activator.getDefault().log(
					Messages.resCLExInVisit, e);
		}
	}

	/**
	 * Handles project rename and folder rename/delete.
	 *
	 * @param delta .
	 */
	private void handleResourceChange(IResourceDelta delta) {
		IResource resource = delta.getResource();
		IProject project = resource.getProject();

		//If folder(role) is renamed
		if (resource.getType() == IResource.FOLDER
				&& (delta.getFlags()
						& IResourceDelta.MOVED_TO) != 0) {
			handleFolderRename(delta);
		} else if (resource.getType() == IResource.PROJECT
				&& (delta.getFlags()
						& IResourceDelta.MOVED_FROM) != 0) {
			//If project is renamed
			handleProjectRename(delta);
		} else if (delta.getKind() == IResourceDelta.REMOVED
				&& resource.getType() == IResource.FOLDER
				&& resource.getProject().isOpen()) {
			//If folder is deleted
			handleFolderDelete(resource);
		}
		//If project gets opened
		if (resource.getType() == IResource.PROJECT
				&& (delta.getFlags() & IResourceDelta.OPEN) != 0) {
			handleProjectOpen(project);
		}
	}

	/**
	 * Removes the role when a folder corresponding to it is deleted.
	 *
	 * @param resource .
	 */
	private void handleFolderDelete(IResource resource) {
		try {
			File rolePath = resource.getLocation().toFile();
			File projDirPath = rolePath.getParentFile();
			String packageFileLoc = projDirPath
					+ File.separator + Messages.resCLPkgXML;
			File packageFile = new File(packageFileLoc);
			if (packageFile.exists()) {
				WindowsAzureProjectManager projMngr =
						WindowsAzureProjectManager.load(
								projDirPath);
				WindowsAzureRole role =
						projMngr.roleFromPath(rolePath);
				if (role != null) {
					try {
						role.delete();
					} catch (Exception e) {
						//As folder delete has been occurred already
						//user should not get any exception prompt.
						Activator.getDefault().log(
								Messages.resCLExFolderRem, e);
					}
					projMngr.save();
				}
			}
		} catch (WindowsAzureInvalidProjectOperationException e) {
			//As folder delete has been occurred already
			//user should not get any exception prompt.
			Activator.getDefault().log(
					Messages.resCLExFolderRem, e);
		}
	}

	/**
	 * Makes changes in launch file so that project can be built
	 * properly.
	 *
	 * @param delta .
	 */
	private void handleProjectRename(IResourceDelta delta) {
		try {
			IResource resource = delta.getResource();
			IWorkspaceRoot root = resource.getWorkspace().getRoot();
			IProject proj = root.getProject(resource.getName());
			String strPath = proj.getLocation().toOSString();
			String launchFile =	strPath + File.separator
					+ Messages.resCLExtToolBldr
					+ File.separator
					+ Messages.resCLLaunchFile;
			ParseXML.setProjectNameinLaunch(launchFile,
					delta.getMovedFromPath().lastSegment(),
					resource.getName());
			WindowsAzureProjectManager projMngr =
					WindowsAzureProjectManager.load(
							proj.getLocation().toFile());
			projMngr.setProjectName(resource.getName());
			projMngr.save();
		} catch (Exception e) {
			//As project rename has been occurred already
			//user should not get any exception prompt.
			Activator.getDefault().log(
					Messages.resCLExProjRename, e);
		}
	}

	/**
	 * Makes changes in xml files by calling setName() on role,
	 * to which the folder being renamed corresponds.
	 *
	 * @param delta
	 */
	private void handleFolderRename(IResourceDelta delta) {
		try {
			IResource resource = delta.getResource();
			File rolePath = resource.getLocation().toFile();
			File projDirPath =
					rolePath.getParentFile();
			String packageFileLoc = projDirPath
					+ File.separator + Messages.resCLPkgXML;
			File packageFile = new File(packageFileLoc);
			if (packageFile.exists()) {
				WindowsAzureProjectManager projMngr =
						WindowsAzureProjectManager.load(
								projDirPath);
				WindowsAzureRole role =
						projMngr.roleFromPath(rolePath);
				if (role != null) {
					role.setName(
							delta.getMovedToPath().lastSegment());
					projMngr.save();
				}
			}
		} catch (WindowsAzureInvalidProjectOperationException e) {
			//As user has already renamed the folder in project explorer,
			//it is expected to get an exception that folder cannot be renamed.
			//So user should not get any exception prompt.
			Activator.getDefault().log(
					Messages.resCLExFoldRename, null);
		}
	}

	/**
	 * Upgrades the opened project if required.
	 *
	 * @param project to be upgraded
	 */
	private void handleProjectOpen(final IProject project) {
		final WindowsAzureProjectManager projMngr;
		try {
			projMngr = WindowsAzureProjectManager.
					load(project.getLocation().toFile());
			if (!projMngr.isCurrVersion()) {

				//step1:check if latest SDK is available or not
				String sdkPath = null;
				try {
					sdkPath = WindowsAzureProjectManager.getLatestAzureSdkDir();
				} catch (IOException e) {
					sdkPath = null;
					Activator.getDefault().log(errorMessage, e);
				}
				if (sdkPath == null) {
					try {
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								installSDK = MessageDialog.openQuestion(null,
											 	Messages.sdkInsTtl, Messages.sdkInsMsg);
							}
						});
						if (installSDK) {
							PlatformUI.getWorkbench().getBrowserSupport().
							getExternalBrowser()
							.openURL(new URL(Messages.sdkInsUrl));
						}
					} catch (final Exception e) {
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								PluginUtil.displayErrorDialogAndLog(null,
										Messages.resCLExSDKIns,
										Messages.resCLExSDKIns, e);
							}
						});
					}
				}

				// close project if version is below V1.7	
				if (!WindowsAzureConstants.V17_VERSION.
						equals(projMngr.getVersion())) {
					errorTitle = Messages.resChgOldPrjOpenTtl;
					errorMessage = Messages.resChgOldPrjOpenMsg;
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							MessageDialog.
							openWarning(null, errorTitle, errorMessage);
						}
					});
				}
			}
			// correct project name if its invalid.
			if (!project.getName().
					equalsIgnoreCase(projMngr.getProjectName())) {
				WAEclipseHelper.
				correctProjectName(project, projMngr);
			}
		} catch (Exception e) {
			//As project open has been occurred already
			//user should not get any exception prompt.
			Activator.getDefault().log(Messages.resCLExProjUpgrd, e);
		}
}

	/**
	 * Refreshes the workspace.
	 */
	private static void refreshWorkspace() {
		try {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceRoot root = workspace.getRoot();
			root.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			//This is just a try to refresh workspace.
			//User can also refresh the workspace manually.
			//So user should not get any exception prompt.
			Activator.getDefault().log(
					Messages.resCLExWkspRfrsh,
					null);
		}
	}
}
