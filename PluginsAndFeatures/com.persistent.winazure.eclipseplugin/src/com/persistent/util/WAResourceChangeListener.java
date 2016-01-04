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
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;

import waeclipseplugin.Activator;

/**
 * Listens to changes to resources in the workspace.
 *
 */

public class WAResourceChangeListener implements IResourceChangeListener {

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
					PluginUtil.refreshWorkspace();
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
			String newName = resource.getName();
			String oldName = delta.getMovedFromPath().lastSegment();
			IProject proj = root.getProject(newName);
			String strPath = proj.getLocation().toOSString();
			String launchFile =	strPath + File.separator
					+ Messages.resCLExtToolBldr
					+ File.separator
					+ Messages.resCLLaunchFile;
			ParseXML.setProjectNameinLaunch(launchFile,
					oldName,
					newName);
			WindowsAzureProjectManager projMngr =
					WindowsAzureProjectManager.load(
							proj.getLocation().toFile());
			projMngr.setProjectName(newName);
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
		PluginUtil.refreshWorkspace();
		WindowsAzureProjectManager projMngr;
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
				if (sdkPath == null && Activator.IS_WINDOWS) {
					try {
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								SDKInstallationDlg dlg = new SDKInstallationDlg(null);
								int btnId = dlg.open();
								if (btnId == Window.OK) {
									installSDK = true;
								} else {
									installSDK = false;
								}
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
			}

	        if (!projMngr.isCurrVersion()) {
              	WAEclipseHelper.handleProjectUpgrade(project,projMngr);
            }
			// correct project name if its invalid.
			if (!project.getName().
					equalsIgnoreCase(projMngr.getProjectName())) {
				WAEclipseHelper.
				correctProjectName(project, projMngr);
			}
			projMngr = WAStartUp.initializeStorageAccountRegistry(projMngr);
			projMngr = WAStartUp.changeLocalToAuto(projMngr, project.getName());
			// save object so that access key will get saved in PML.
			projMngr.save();
		} catch (Exception e) {
			//As project open has been occurred already
			//user should not get any exception prompt.
			Activator.getDefault().log(Messages.resCLExProjUpgrd, e);
		}
	}
}
