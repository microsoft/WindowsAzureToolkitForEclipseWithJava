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
import java.io.PrintStream;
import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
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
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import waeclipseplugin.Activator;

import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.persistent.builder.WADependencyBuilder;
import com.persistent.ui.propertypage.WAProjectNature;

/**
 * Listens to changes to resources in the workspace.
 *
 */

public class WAResourceChangeListener implements IResourceChangeListener {

	private String  errorTitle;
	private String  errorMessage;
	private boolean upgradeProject;
	private boolean installSDK;
	private String  upgradedProjectPath;
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
				errorTitle = Messages.resChgPrjUpgTtl;
				errorMessage = Messages.resChgPrjUpgMsg;
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						upgradeProject = MessageDialog.openQuestion(
								null, errorTitle, errorMessage);
					}
				});
				if (upgradeProject) {
					//step1:check if latest SDK is available or not
					String sdkPath = null;
				try {
					sdkPath = WindowsAzureProjectManager.
							getLatestAzureSdkDir();
				} catch (IOException e) {
					sdkPath = null;
					Activator.getDefault().log(errorMessage, e);
				}
				if (sdkPath == null) {
					try {
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								installSDK = MessageDialog.
										openQuestion(null,
												Messages.sdkInsTtl, Messages.sdkInsMsg);
							}
						});
						if (installSDK) {
							PlatformUI.getWorkbench().getBrowserSupport().
							getExternalBrowser()
							.openURL(new URL(Messages.sdkInsUrl));
						}
					} catch (Exception e) {
						Activator.getDefault().log(Messages.resCLExSDKIns, e);
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								MessageUtil.displayErrorDialog(null,
										Messages.resCLExSDKIns,
										Messages.resCLExSDKIns);
							}
						});
					}
				}

				if (sdkPath != null) {
					//step2:Try to upgrade project
					Job job = new Job(Messages.upgrdJobTtl) {
					protected IStatus run(IProgressMonitor monitor) {
						monitor.beginTask(Messages.upgrdJobTtl, IProgressMonitor.UNKNOWN);
						try {
							String zipFile = String.format("%s%s%s%s%s%s%s",
									Platform.getInstallLocation().getURL().getPath().toString(),
									File.separator, Messages.pluginFolder,
									File.separator, Messages.pluginId, File.separator,
									Messages.starterKitFileName);

							PrintStream ss = System.out;
							MessageConsole waConsole =
									new MessageConsole(Messages.cnslName, null);
							ConsolePlugin.getDefault().getConsoleManager().
							addConsoles(new IConsole[]{waConsole });
							ConsolePlugin.getDefault().getConsoleManager().
							showConsoleView(waConsole);
							MessageConsoleStream stream =
									waConsole.newMessageStream();
							System.setOut(new PrintStream(stream));
							stream.setActivateOnWrite(true);

							upgradedProjectPath = projMngr.upgradeProject(
									project.getLocation().toFile(), zipFile);
							//Resetting System out
							System.setOut(ss);

							//Now load project into workspace
							IWorkspace workspace = ResourcesPlugin.getWorkspace();
							IWorkspaceRoot root = workspace.getRoot();
							IProjectDescription projDescription;

							final File upgradedProjectName = new File(upgradedProjectPath);
							//change the externalToolBuilders/WindowsAzureProjectBuilder.launch
							String launchFilePath = upgradedProjectPath
									+ File.separator
									+ Messages.pWizToolBuilder
									+ File.separator
									+ Messages.pWizLaunchFile;
							ParseXML.setProjectNameinLaunch(launchFilePath,
									Messages.pWizWinAzureProj,
									upgradedProjectName.getName());

							root.touch(null);
							projDescription =
									workspace.newProjectDescription(upgradedProjectPath);
							Path path = new Path(upgradedProjectPath);
							projDescription.setLocation(path);
							projDescription.setNatureIds(new String [] {WAProjectNature.NATURE_ID});

							//To add new project name in .project file
							projDescription.setName(upgradedProjectName.getName());
							IProject iProject = root.getProject(upgradedProjectName.getName());

							if (!iProject.exists()) {
								File workspaceDirectory = workspace.getRoot().getLocation().toFile();
								if (workspaceDirectory.equals(upgradedProjectName.getParentFile())) {
									iProject.create(null);
								}
								else {
									iProject.create(projDescription, null);
								}
							}
							iProject.open(null);

							projDescription = iProject.getDescription();
							projDescription.setName(upgradedProjectName.getName());

							//Associating new builder with new project
							WADependencyBuilder builder = new WADependencyBuilder();
							builder.addBuilder(iProject,
									"com.persistent.winazure.eclipseplugin.Builder");

							projDescription.setNatureIds(new String [] {WAProjectNature.NATURE_ID});
							iProject.move(projDescription, IResource.FORCE, null);

							//Associating WA nature to new project
							Display.getDefault().syncExec(new Runnable() {
								public void run() {
									MessageDialog.openInformation(null,
											Messages.resChgPrjUpgTtl,
											Messages.resChgPrjUpgMsgConfirm
											+ upgradedProjectName.getName());
								}
							});
							root.touch(null);
						} catch (CoreException e) {
							Activator.getDefault().log(Messages.resCLExProjUpgrd, e);
							return Status.CANCEL_STATUS;
						} catch (WindowsAzureInvalidProjectOperationException e) {
							Activator.getDefault().log(Messages.resCLExProjUpgrd, e);
							Display.getDefault().syncExec(new Runnable() {
								public void run() {
									MessageUtil.displayErrorDialog(null,
											Messages.resCLExProjUpgrd,
											Messages.resCLExProjUpgrd);
								}
							});
							return Status.CANCEL_STATUS;
						} catch (IOException e) {
							Activator.getDefault().log(errorMessage, e);
							Display.getDefault().syncExec(new Runnable() {
								public void run() {
									MessageUtil.displayErrorDialog(null,
											Messages.resCLExProjUpgrd,
											Messages.resCLExProjUpgrd);
								}
							});
							return Status.CANCEL_STATUS;
						} catch (Exception e) {
							Activator.getDefault().log(errorMessage, e);
							return Status.CANCEL_STATUS;
						}
						monitor.done();
						return Status.OK_STATUS;
					}
					};
					job.schedule();
				}
				}
				//If project is older version always close it
				Job closeProject = new Job(Messages.closeProj) {
					protected IStatus run(IProgressMonitor monitor) {
						try {
							project.close(null);
						} catch (CoreException e) {
							Activator.getDefault().log(Messages.resCLExProjUpgrd, e);
							return Status.CANCEL_STATUS;
						} catch (Exception e) {
							Activator.getDefault().log(errorMessage, e);
							return Status.CANCEL_STATUS;
						}
						return Status.OK_STATUS;
					}
				};
				closeProject.schedule();
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
