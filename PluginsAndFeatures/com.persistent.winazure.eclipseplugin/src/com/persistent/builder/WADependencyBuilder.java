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
package com.persistent.builder;


import java.io.File;
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

import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.interopbridges.tools.windowsazure.WindowsAzureRoleComponent;
import com.interopbridges.tools.windowsazure.WindowsAzureRoleComponentImportMethod;
import com.microsoftopentechnologies.azurecommons.builder.WAExportJar;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
import com.persistent.util.WAEclipseHelper;
import com.persistent.util.ProjectNatureHelper;
import com.persistent.util.ProjectNatureHelper.ProjExportType;
/**
 * This class add a custom Azure project's dependency builder.
 * Calls export methods (JAR, WAR, EAR) according to nature of project.
 */
@SuppressWarnings("restriction")
public class WADependencyBuilder extends IncrementalProjectBuilder {
    private String errorMessage;

    @SuppressWarnings("rawtypes")
	@Override
    public IProject[] build(int arg0, Map arg1, IProgressMonitor arg2) throws CoreException {
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
            waProjManager = WindowsAzureProjectManager.load(new File(getProject().getLocation().toOSString()));
            waProjManager.setClassPathInPackage("azure.lib.dir", PluginUtil.getAzureLibLocation());
            waProjManager.save();
            

            // Get existing Azure roles from WA project
            for (WindowsAzureRole windowsAzureRole : waProjManager.getRoles()) {
                String approotPath = String.format("%s%s%s%s%s",
                        root.getProject(waProjManager.getProjectName()).getLocation(),
                        File.separator, windowsAzureRole.getName(),
                        File.separator, "approot");

                // Get existing components from Azure role if any
                List<WindowsAzureRoleComponent> listComponents = windowsAzureRole.getComponents();
                if (listComponents != null && !listComponents.isEmpty()) {
                	for (WindowsAzureRoleComponent waRoleCmpnt : listComponents) {
                        String impDestPath = null;
                        // Check if import method is auto
                        if (waRoleCmpnt.getImportMethod() == WindowsAzureRoleComponentImportMethod.auto) {

                            String frmPath = waRoleCmpnt.getImportPath();
                            String asName = waRoleCmpnt.getDeployName();

                            if (frmPath.isEmpty()) {
                                errorMessage = Messages.pathErrMsg;
                                Activator.getDefault().log(errorMessage, new Exception());
                            } else if (asName.isEmpty()) {
                                asName = new File(frmPath).getName();
                            }

                            String projName = frmPath.substring(frmPath.lastIndexOf(File.separator) + 1);
                            IProject iProj = root.getProject(projName);
                            ProjExportType type = ProjectNatureHelper.getProjectNature(iProj);
                            String basePath = Messages.basePath + File.separator + "..";

                            // Calculate destination path for exporting project
                            if (asName.endsWith(type.name().toLowerCase())) {
                                impDestPath = String.format("%s%s%s", approotPath, File.separator, asName);
                            }
                            else {
                                impDestPath = String.format("%s%s%s%s%s",
                                        approotPath, File.separator, asName, ".",
                                        type.name().toLowerCase());
                            }
                            try {
                                switch(type) {
                                case WAR :
                                    WAExportWarEar.exportWarComponent(projName, impDestPath);
                                    break;

                                case EAR :
                                    WAExportWarEar.exportWarComponent(projName, impDestPath);
                                    break;

                                case JAR :
                                    if (frmPath.startsWith(basePath)) {
                                        frmPath = frmPath.substring(frmPath.indexOf('}') + 4, frmPath.length());
                                        frmPath = String.format("%s%s", root.getLocation().toOSString(), frmPath);
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
                            WAEclipseHelper.refreshWorkspace(Messages.rfrshErrTtl, Messages.rfrshErrMsg);
                        }
                    }
                }
            }
        } catch (Exception e) {
            errorMessage = Messages.bldErrMsg;
            Activator.getDefault().log(errorMessage, e);
        }
        return iProjArr;
    }

    /**
     * This method adds an entry of WADependency builder
     * to Azure Projects.
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
            command.setBuilding(AUTO_BUILD, false);
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
