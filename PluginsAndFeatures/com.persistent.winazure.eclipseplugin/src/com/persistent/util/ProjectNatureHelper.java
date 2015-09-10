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
import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Shell;

import waeclipseplugin.Activator;

import com.interopbridges.tools.windowsazure.WindowsAzureRoleComponentImportMethod;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
/**
 * Class has various method which
 * helps to find project nature,
 * projects from workspace,
 * deployment name of component &
 * for path conversion.
 */
public class ProjectNatureHelper {
    public enum ProjExportType { WAR, EAR, JAR };
    private static final String BASE_PATH = "${basedir}" + File.separator + "..";

    /**
     * Method returns nature of project.
     * @param proj
     * @return
     */
    public static ProjExportType getProjectNature(IProject proj) {
        ProjExportType type = null;
        try {
            if (proj.hasNature(Messages.natJavaEMF)
                    && proj.hasNature(Messages.natMdCore)
                    && proj.hasNature(Messages.natFctCore)
                    && proj.hasNature(Messages.natJava)
                    && proj.hasNature(Messages.natJs)) {
                    type = ProjExportType.WAR;
            } else if (proj.hasNature(Messages.natFctCore)
                    && proj.hasNature(Messages.natMdCore)) {
                if (proj.hasNature(Messages.natJs)
                        || proj.hasNature(Messages.natJava)
                        || proj.hasNature(Messages.natJavaEMF)) {

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
    					getProjectNature(findProjectFromWorkSpace(
    							convertPath(path)));
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
                       && !wRoot.hasNature(Messages.stUpProjNature)) {
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
           PluginUtil.displayErrorDialogAndLog(
        		   new Shell(),
        		   Messages.prjSelErr,
        		   Messages.prjSelMsg, e);
       }
       return project;
   }
}
