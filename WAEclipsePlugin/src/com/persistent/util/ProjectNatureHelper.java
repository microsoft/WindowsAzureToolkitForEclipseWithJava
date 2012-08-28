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
package com.persistent.util;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Shell;

import com.interopbridges.tools.windowsazure.WindowsAzureRoleComponentImportMethod;
import com.persistent.winazureroles.Messages;

import waeclipseplugin.Activator;

public class ProjectNatureHelper {
    public enum ProjExportType {WAR,EAR,JAR};
    private static final String BASE_PATH = "${basedir}\\..";
    public static String errorTitle;
    private static String errorMessage;

    public static ProjExportType getProjectNature(IProject proj) {
        ProjExportType type = null;
        try {
            if (proj.hasNature("org.eclipse.jem.workbench.JavaEMFNature")
                    && proj.hasNature("org.eclipse.wst.common.modulecore.ModuleCoreNature")
                    && proj.hasNature("org.eclipse.wst.common.project.facet.core.nature")
                    && proj.hasNature("org.eclipse.jdt.core.javanature")
                    && proj.hasNature("org.eclipse.wst.jsdt.core.jsNature")) {
                    type = ProjExportType.WAR;
            } else if (proj.hasNature("org.eclipse.wst.common.project.facet.core.nature")
                    && proj.hasNature("org.eclipse.wst.common.modulecore.ModuleCoreNature")) {
                if (proj.hasNature("org.eclipse.wst.jsdt.core.jsNature")
                        || proj.hasNature("org.eclipse.jdt.core.javanature")
                        || proj.hasNature("org.eclipse.jem.workbench.JavaEMFNature")) {

                    type = ProjExportType.JAR;
                } else {
                    type = ProjExportType.EAR;
                }
            } else {
                type = ProjExportType.JAR;
            }
        } catch (CoreException e) {
            Activator.getDefault().log(e.getMessage(), e);
        }
        return type;

    }
    /**
     * This method find the absolute path from
     * relative path.
     * @param path : relative path
     * @return absolute path
     */
    public static String convertPath(String path) {
        String newPath = "";
        if (path.startsWith(BASE_PATH)) {
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            IWorkspaceRoot root = workspace.getRoot();
            String rplStr = path.substring(path.indexOf('}')
            		+ 4, path.length());
            newPath = String.format("%s%s",
            		root.getLocation().toOSString(), rplStr);
        } else {
            newPath = path;
        }
        return newPath;
    }

    /**
     * This method returns the deployment name of any component
     * it also prepares the name if name is not specified.
     * @param path
     * @param method
     * @param asName
     * @return
     */
    public static String getAsName(String path,
    		WindowsAzureRoleComponentImportMethod method,
    		String asName) {
    	String name;
    	if (asName.isEmpty()) {
    		name = new File(path).getName();
    		if (method == WindowsAzureRoleComponentImportMethod.auto) {
    			ProjExportType type = ProjectNatureHelper.
    					getProjectNature(findProjectFromWorkSpace(convertPath(path)));
    			name = String.format("%s%s%s", name, ".",
    					type.name().toLowerCase());
    		} else if (method == WindowsAzureRoleComponentImportMethod.zip) {
    			name = String.format("%s%s", name, ".zip");
    		}
    	} else {
    		name = asName;
    	}
    	return name;
    }

    /**
     * This method finds the project in workspace.
     * @param path : import path
     * @return : matched project
     */
    public static IProject findProjectFromWorkSpace(String path) {
       IWorkspace workspace = ResourcesPlugin.getWorkspace();
       IWorkspaceRoot root = workspace.getRoot();
       IProject project = null;
       ArrayList<IProject> projList = new ArrayList<IProject>();
       try {
           for (IProject wRoot : root.getProjects()) {
               if (wRoot.isOpen()
                       && !wRoot.hasNature("com.persistent.ui.projectnature")) {
                   projList.add(wRoot);
               }
           }
           IProject[] arr = new IProject[projList.size()];
           arr = projList.toArray(arr);
           for (int i = 0; i < arr.length; i++) {
               if (arr[i].getLocation().toOSString().equalsIgnoreCase(path)) {
                   project = arr[i];
               }
           }
       } catch (Exception e) {
    	   errorTitle = Messages.prjSelErr;
           errorMessage = Messages.prjSelMsg;
           Activator.getDefault().log(errorMessage, e);
           MessageUtil.displayErrorDialog(new Shell(), errorTitle,
                   errorMessage);
       }
       return project;
   }

}
