/**
 * Copyright 2015 Microsoft Open Technologies, Inc.
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import waeclipseplugin.Activator;

import com.gigaspaces.azure.wizards.DeployWizard;
import com.gigaspaces.azure.wizards.DeployWizardDialog;
import com.interopbridges.tools.windowsazure.WARoleComponentCloudUploadMode;
import com.interopbridges.tools.windowsazure.WindowsAzureEndpoint;
import com.interopbridges.tools.windowsazure.WindowsAzureEndpointType;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.interopbridges.tools.windowsazure.WindowsAzureRoleComponent;
import com.interopbridges.tools.windowsazure.WindowsAzureRoleComponentImportMethod;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
import com.persistent.builder.WADependencyBuilder;
import com.persistent.ui.propertypage.WAProjectNature;
import com.persistent.util.ParseXML;
import com.persistent.util.WAEclipseHelper;

public class SingleClickPublishUtils {
	static File cmpntFile = new File(WAEclipseHelper.getTemplateFile(Messages.cmpntFileName));
	static String auto = "auto";
	static String httpEp = "http";
	static String HTTP_PORT = "80";
	static final String LAUNCH_FILE_PATH = File.separator
			+ com.persistent.ui.projwizard.Messages.pWizToolBuilder
			+ File.separator
			+ com.persistent.ui.projwizard.Messages.pWizLaunchFile;
	static final String BASE_PATH = "${basedir}" + File.separator + ".." + File.separator;

	public static void exceute() {
		try {
			IProject dynamicWebProj = PluginUtil.getSelectedProject();
			String dynamicWebProjName = dynamicWebProj.getName();
			String dynamicWebProjLoc = dynamicWebProj.getLocation().toString();
			List<IProject> projList = findAzureProjectHavingDynamicProjectReference(
					dynamicWebProjName, dynamicWebProjLoc);
			int size = projList.size();
			if (size == 0) {
				// If no azure deployment project contains WAR project as a project reference
				IProject project = handleAzureDeploymentProjectCreation(dynamicWebProjName);
				if (project != null) {
					openPublishWizard(project);
				}
			} else if (size == 1) {
				// If single azure deployment project contains WAR project as a project reference
				openPublishWizard(projList.get(0));
			} else {
				// If more than one azure deployment project contains WAR project as a project reference
				MessageDialog.openInformation(new Shell(), Messages.title, Messages.twoProjMsg);
			}
		} catch(Exception ex) {
			Activator.getDefault().log(Messages.cntxtMenuErr, ex);
		}
	}

	private static List<IProject> findAzureProjectHavingDynamicProjectReference(
			String dynamicWebProjName, String dynamicWebProjLoc) throws Exception {
		List<IProject> projList = new ArrayList<IProject>();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		String appPath = BASE_PATH + dynamicWebProjName;
		IProject[] projects = root.getProjects();
		for (IProject iProject : projects) {
			if (iProject.isOpen()
					&& iProject.hasNature(com.persistent.util.Messages.stUpProjNature)) {
				boolean isAppPresent = false;
				WindowsAzureProjectManager projMngr = WindowsAzureProjectManager.load(
						iProject.getLocation().toFile());
				List<WindowsAzureRole> roleList = projMngr.getRoles();
				for (int i = 0; i < roleList.size(); i++) {
					List<WindowsAzureRoleComponent> apps = roleList.get(i).getServerApplications();
					for (WindowsAzureRoleComponent app : apps) {
						String srcPath = app.getImportPath();
						if (srcPath.equalsIgnoreCase(appPath)
								|| srcPath.equalsIgnoreCase(dynamicWebProjLoc)) {
							isAppPresent = true;
						}
					}
				}
				if (isAppPresent) {
					projList.add(iProject);
				}
			}
		}
		return projList;
	}

	private static IProject handleAzureDeploymentProjectCreation(
			String dynamicWebProjName) throws Exception {
		IProject deploymentProj = null;
		try {
			boolean isCloudServerJdkPresent = true;
			String thirdPartyServer = WindowsAzureProjectManager.getFirstDefaultThirdPartySrvName(cmpntFile);
			if (thirdPartyServer.isEmpty()) {
				// if default server not specified then get first one
				String [] thrdPrtSrvArr = WindowsAzureProjectManager.getAllThirdPartySrvNames(cmpntFile, "");
				if (thrdPrtSrvArr.length >= 1) {
					thirdPartyServer = thrdPrtSrvArr[0];
				} else {
					isCloudServerJdkPresent = false;
				}
			}

			String[] thrdPrtJdkArr = WindowsAzureProjectManager.getThirdPartyJdkNames(cmpntFile, "");
			String jdkName = "";
			if (thrdPrtJdkArr.length >= 1) {
				jdkName = thrdPrtJdkArr[0];
			} else {
				isCloudServerJdkPresent = false;
			}

			if (isCloudServerJdkPresent) {
				String jdkLicense = WindowsAzureProjectManager.getLicenseUrl(jdkName, cmpntFile);
				String serverLicense = WindowsAzureProjectManager.
						getThirdPartyServerLicenseUrl(thirdPartyServer, cmpntFile);
				NewAzureProjectPromptDlg dlg = new NewAzureProjectPromptDlg(new Shell(),
						jdkName, jdkLicense, thirdPartyServer, serverLicense);
				int btnId = dlg.open();
				if (btnId == Window.OK) {
					deploymentProj = createAzureDeploymentProject(
							dynamicWebProjName, jdkName, thirdPartyServer);
					if (deploymentProj == null) {
						MessageDialog.openError(new Shell(), Messages.title, Messages.projErr);
					}
				}
			} else {
				MessageDialog.openError(new Shell(), Messages.title, Messages.noJdkSrvMsg);
			}
		} catch(Exception ex) {
			deploymentProj = null;
		}
		return deploymentProj;
	}

	private static IProject createAzureDeploymentProject(
			String dynamicWebProjName,
			String jdkName, String thirdPartyServer) {
		IProject deploymentProj = null;
		try {
			// initialize deployment project
			String zipFile  = String.format("%s%s%s%s%s", PluginUtil.pluginFolder, File.separator,
					com.persistent.ui.projwizard.Messages.pluginId,
					File.separator, com.persistent.ui.projwizard.Messages.starterKitFileName);
			WindowsAzureProjectManager waProjMgr = WindowsAzureProjectManager.create(zipFile);
			waProjMgr.setRemoteAccessAllRoles(false);
			waProjMgr.setClassPathInPackage("azure.lib.dir", PluginUtil.getAzureLibLocation());
			WindowsAzureRole role = waProjMgr.getRoles().get(0);
			WindowsAzureEndpoint endpoint = role.getEndpoint(com.persistent.ui.projwizard.Messages.httpEp);
			if (endpoint != null) {
				endpoint.delete();
			}

			// Prepare project basic properties like name, location
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceRoot root = workspace.getRoot();
			String workspaceLocation = root.getLocation().toString();
			boolean isDefault = true;
			StringBuffer projName = new StringBuffer(dynamicWebProjName + "_onAzure");
			int originalLength = projName.length();
			int index = 1;
			while(root.getProject(projName.toString()).exists()) {
				// Make sure deployment project name is unique
				projName.delete(originalLength, projName.length());
				projName.append(index++);
			}

			// configure third party JDK
			role.setJDKSourcePath("", cmpntFile, jdkName);
			role.setJDKCloudName(jdkName);
			role.setJDKCloudUploadMode(WARoleComponentCloudUploadMode.auto);
			role.setJDKCloudURL(auto);
			role.setJDKCloudHome(WindowsAzureProjectManager.getCloudValue(jdkName,cmpntFile));

			// configure third party server
			String serverName = WindowsAzureProjectManager.
					getServerNameUsingThirdPartyServerName(thirdPartyServer, cmpntFile);
			if (!serverName.isEmpty()) {
				String srvPriPort = WindowsAzureProjectManager.getHttpPort(serverName, cmpntFile);
				if (role.isValidEndpoint(httpEp,
						WindowsAzureEndpointType.Input, srvPriPort, HTTP_PORT)) {
					role.addEndpoint(httpEp,
							WindowsAzureEndpointType.Input,srvPriPort, HTTP_PORT);
				}
				role.setServer(serverName, "", cmpntFile);
				role.setServerCldAltSrc(WindowsAzureProjectManager.
						getThirdPartyServerCloudAltSrc(thirdPartyServer, cmpntFile));
				role.setServerCloudName(thirdPartyServer);
				role.setServerCloudUploadMode(WARoleComponentCloudUploadMode.auto);
				role.setServerCloudURL(auto);
				role.setServerCloudHome(WindowsAzureProjectManager.getThirdPartyServerHome(
						thirdPartyServer, cmpntFile));
			}

			List<WindowsAzureRoleComponent> waCompList = role.getServerApplications();
			for (WindowsAzureRoleComponent waComp : waCompList) {
				if (waComp.getDeployName().
						equalsIgnoreCase(com.persistent.ui.projwizard.Messages.helloWorld)
						&& waComp.getImportPath().isEmpty()) {
					waComp.delete();
				}
			}

			// configure server appliaction
			role.addServerApplication(BASE_PATH + dynamicWebProjName,
					dynamicWebProjName + ".war",
					WindowsAzureRoleComponentImportMethod.auto.name(),
					cmpntFile, true);

			waProjMgr.save();

			String projNameStr = projName.toString();
			WindowsAzureProjectManager.moveProjFromTemp(projNameStr, workspaceLocation);
			String launchFilePath = workspaceLocation + File.separator
					+ projNameStr + LAUNCH_FILE_PATH;
			ParseXML.setProjectNameinLaunch(launchFilePath,
					com.persistent.ui.projwizard.Messages.pWizWinAzureProj, projNameStr);
			root.touch(null);

			deploymentProj = root.getProject(projNameStr);
			IProjectDescription projDescription =
					workspace.newProjectDescription(projNameStr);
			Path path = new Path(workspaceLocation + File.separator + projNameStr);
			projDescription.setLocation(path);
			projDescription.setNatureIds(new String [] {WAProjectNature.NATURE_ID});
			if (!deploymentProj.exists()) {
				if (isDefault) {
					deploymentProj.create(null);
				} else {
					deploymentProj.create(projDescription, null);
				}
			}
			deploymentProj.open(null);
			projDescription = deploymentProj.getDescription();
			projDescription.setName(projNameStr);
			projDescription.setNatureIds(new String [] {WAProjectNature.NATURE_ID});

			deploymentProj.move(projDescription, IResource.FORCE, null);

			root.touch(null);
			if (deploymentProj != null) {
				WADependencyBuilder builder = new WADependencyBuilder();
				builder.addBuilder(deploymentProj,
						"com.persistent.winazure.eclipseplugin.Builder");
			}
		} catch(Exception ex) {
			deploymentProj = null;
		}
		return deploymentProj;
	}

	private static void openPublishWizard(IProject projectToPublish) {
		DeployWizard wizard = new DeployWizard(projectToPublish);
		if (wizard.getSelectedProject() != null) {
			wizard.setNeedsProgressMonitor(true);
			DeployWizardDialog dialog = new DeployWizardDialog(new Shell(), wizard,
					com.gigaspaces.azure.handler.Messages.publish);
			dialog.create();
			dialog.open();
		}
	}

}
