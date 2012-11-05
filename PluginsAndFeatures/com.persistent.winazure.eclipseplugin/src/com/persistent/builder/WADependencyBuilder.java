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
package com.persistent.builder;


import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.internal.ui.actions.OpenRunConfigurations;

import waeclipseplugin.Activator;

import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.interopbridges.tools.windowsazure.WindowsAzureRoleComponent;
import com.interopbridges.tools.windowsazure.WindowsAzureRoleComponentImportMethod;
import com.persistent.util.WAEclipseHelper;
import com.persistent.util.ProjectNatureHelper;
import com.persistent.util.ProjectNatureHelper.ProjExportType;
/**
 * This class add a custom Windows Azure project's dependency builder.
 * Calls export methods (JAR, WAR, EAR) according to nature of project.
 */
@SuppressWarnings("restriction")
public class WADependencyBuilder extends IncrementalProjectBuilder {
    private String errorMessage;
    private static List<WindowsAzureRole> waRoles;
    private static List<WindowsAzureRoleComponent> listComponents;

    @SuppressWarnings("rawtypes")
	@Override
    public IProject[] build(int arg0, Map arg1,
            IProgressMonitor arg2) throws CoreException {
        try {
            OpenRunConfigurations con = new OpenRunConfigurations();
            con.run();
        } catch (Exception e) {
            //Invalid thread exception is expected here.
            //No need to show/log any error message to user as this
            //is only for initialising the console for logging.
        }
        WindowsAzureProjectManager waProjManager;
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        IProject[] iProjArr = root.getProjects();
        try {
            waProjManager = WindowsAzureProjectManager.
                    load(new File(getProject().getLocation().toOSString()));

            // Get existing Windows Azure roles from WA project
            waRoles  = waProjManager.getRoles();
            for (Iterator<WindowsAzureRole> iterator =
            		waRoles.iterator(); iterator.hasNext();) {
                WindowsAzureRole windowsAzureRole =
                		(WindowsAzureRole) iterator.next();

                String approotPath = String.format("%s%s%s%s",
                        root.getProject(waProjManager.getProjectName()).getLocation(),
                        "\\", windowsAzureRole.getName(), "\\approot");

                // Get existing components from Windows Azure role if any
                listComponents = windowsAzureRole.getComponents();
                if (listComponents != null && !listComponents.isEmpty()) {
                    for (Iterator<WindowsAzureRoleComponent> it =
                            listComponents.iterator(); it.hasNext();) {
                        WindowsAzureRoleComponent waRoleCmpnt =
                                (WindowsAzureRoleComponent) it.next();

                        String impDestPath = null;
                        // Check if import method is auto
                        if (waRoleCmpnt.getImportMethod() != null
                        && waRoleCmpnt.getImportMethod().
                        equals(WindowsAzureRoleComponentImportMethod.auto)) {

                            String frmPath = waRoleCmpnt.getImportPath();
                            String asName = waRoleCmpnt.getDeployName();

                            if (frmPath.isEmpty()) {
                                errorMessage = Messages.pathErrMsg;
                                Activator.getDefault().log(errorMessage,
                                		new Exception());
                            } else if (asName.isEmpty()) {
                                asName = new File(frmPath).getName();
                            }

                            String projName = frmPath.substring(
                            		frmPath.lastIndexOf('\\') + 1);
                            IProject iProj = root.getProject(projName);
                            ProjExportType type = ProjectNatureHelper.
                            		getProjectNature(iProj);

                            // Calculate destination path for exporting project
                            if (asName.endsWith(type.name().toLowerCase())) {
                                impDestPath = String.format("%s%s%s",
                                		approotPath, "\\", asName);
                            }
                            else {
                                impDestPath = String.format("%s%s%s%s%s",
                                        approotPath, "\\", asName, ".",
                                        type.name().toLowerCase());
                            }
                            try {
                                switch(type) {
                                case WAR :
                                    WAExportWarEar.exportWarComponent(projName,
                                    		impDestPath);
                                    break;

                                case EAR :
                                    WAExportWarEar.exportWarComponent(projName,
                                    		impDestPath);
                                    break;

                                case JAR :
                                    if (frmPath.startsWith(Messages.basePath)) {
                                        frmPath = frmPath.substring(frmPath.indexOf('}') + 4,
                                                frmPath.length());
                                        frmPath = String.format("%s%s", root.getLocation().toOSString(),
                                                frmPath);
                                }
                                File[] tobeJared = new File[1];
                                tobeJared[0] = new File(frmPath);
                                WAExportJar.createJarArchive(new File(impDestPath),
                                		tobeJared);
                                break;

                                default:
                                    errorMessage = Messages.impErrMsg;
                                    Activator.getDefault().log(errorMessage,
                                    		new Exception());
                                    break;
                                }
                            } catch (ExecutionException e) {
                                errorMessage = String.format("%s%s%s%s%s",
                                        Messages.crtErrMsg, " ", type,
                                        " of project: ", projName);
                                Activator.getDefault().log(errorMessage, e);
                            }
                            WAEclipseHelper.refreshWorkspace(
                            		Messages.rfrshErrTtl, Messages.rfrshErrMsg);
                        }
                    }
                }
            }
        } catch (WindowsAzureInvalidProjectOperationException e) {
            errorMessage = Messages.bldErrMsg;
            Activator.getDefault().log(errorMessage, e);
        }
        return iProjArr;
    }

    /**
     * This method adds an entry of WADependency builder
     * to Windows Azure Projects.
     * @param project
     * @param builderId
     */
    public void addBuilder(IProject project, String builderId) {
        IProjectDescription desc = null;
        try {
            desc = project.getDescription();
            ICommand[] commands = desc.getBuildSpec();
            for (int i = 0; i < commands.length; ++i) {
                if (commands[i].getBuilderName().equals(builderId)) {
                    return;
                }
            }
            //add builder to project
            ICommand command = desc.newCommand();
            command.setBuilderName(builderId);
            command.setBuilding(AUTO_BUILD, true);
            ICommand[] commandArr = new ICommand[commands.length + 1];
            // Add it before other builders.
            System.arraycopy(commands, 0, commandArr, 1, commands.length);
            commandArr[0] = command;
            desc.setBuildSpec(commandArr);
            project.setDescription(desc, null);
        } catch (CoreException e) {
            errorMessage = Messages.addBldErrMsg;
            Activator.getDefault().log(errorMessage, e);
        }
    }
}
