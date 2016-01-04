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

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.microsoftopentechnologies.azurecommons.util.WAEclipseHelperMethods;
import com.persistent.ui.propertypage.WAProjectNature;
import com.persistent.util.ProjectNatureHelper.ProjExportType;

import waeclipseplugin.Activator;

/**
 * Provides implementation for following tests :
 *
 * 1) isRoleFolder : to determine if a folder is role folder or not.
 *
 * 2) isRolePrefNode : to determine if the node is a particular
 *                     role related node.
 *                    
 * 3) isProjFile : to determine if file opened in editor is
 * 					is of  Azure Deployment project.
 */
public class WAPropertyTester extends PropertyTester {

    @Override
    public boolean test(Object object,
    		String property, Object[] args,
    		Object value) {
        boolean retVal = false;
        try {
            if (property.equalsIgnoreCase(Messages.propRoleFolder)
                    && object instanceof IFolder) {
                //to determine if a folder is role folder or not.
                retVal = isRoleFolder(object);
            } else if (property.equalsIgnoreCase(Messages.propRolePrefNode)
                    && object instanceof PreferenceNode) {
                //to determine if the node is a particular role related node.
                PreferenceNode node = (PreferenceNode) object;
                if (node.getId().equalsIgnoreCase(Messages.propIdGeneral)
                        || node.getId().equals(Messages.propIdEndPts)
                        || node.getId().equals(Messages.propIdDbg)) {
                    retVal = true;
                }
            } else if (property.equalsIgnoreCase(Messages.propWebProj)
                    && object instanceof IProject) {
                  retVal = isWebProj(object);
            } else if (property.equalsIgnoreCase("isProjFile")
                    && object instanceof IEditorPart) {
                retVal = isProjFile(object);
            } else if (property.equalsIgnoreCase("isWindows")) {
                retVal = isWindows();
            } else if(property.equalsIgnoreCase("isWebOrAzureProj")
            		&& object instanceof IProject) {
            	retVal = isWebOrAzureProj(object);
            } else if (property.equalsIgnoreCase("isFirstPackageWithAuto")
            		&& object instanceof IProject) {
            	retVal = isFirstPackageWithAuto(object);
            } else if (property.equalsIgnoreCase("isProjFileAndisFirstPackageWithAuto")
            		&& object instanceof IEditorPart) {
            	retVal = isProjFileAndisFirstPackageWithAuto(object);
            }
        } catch (Exception ex) {
            //As this is not an user initiated method,
            //only logging the exception and not showing an error dialog.
            Activator.getDefault().log(Messages.propErr, ex);
        }
        return retVal;
    }

    /**
     * Determines whether a folder is a role folder or not.
     *
     * @param object : variable from the calling test method.
     * @return true if the folder is a role folder else false.
     * @throws CoreException
     * @throws WindowsAzureInvalidProjectOperationException
     */
    private boolean isRoleFolder(Object object)
    throws CoreException, WindowsAzureInvalidProjectOperationException {
        boolean retVal = false;
        IFolder folder = (IFolder) object;
        IProject project = folder.getProject();

        if (project.isOpen() && project.hasNature(
                WAProjectNature.NATURE_ID)) {
            WindowsAzureProjectManager projMngr =
                WindowsAzureProjectManager.load(
                        project.getLocation().toFile());
            WindowsAzureRole role = projMngr.roleFromPath(
                    folder.getLocation().toFile());
            if (role != null) {
                //if role is not null then it's a role folder
                Activator.getDefault().setEdit(true);
                Activator.getDefault().setWaProjMgr(projMngr);
                Activator.getDefault().setWaRole(role);
                retVal = true;
            }
        }
        return retVal;
    }


    /**
     * Determines whether a project is a dynamic web project or not.
     *
     * @param object : variable from the calling test method.
     * @return true if the project is a dynamic web project else false.
     * @throws CoreException
     * @throws WindowsAzureInvalidProjectOperationException
     */
    private boolean isWebProj(Object object)
    throws CoreException, WindowsAzureInvalidProjectOperationException {
        boolean retVal = false;
        IProject project = (IProject) object;
        if (project.isOpen()) {
        	ProjExportType type = ProjectNatureHelper.getProjectNature(project);
        	if (type != null && type.equals(ProjExportType.WAR)){
        		retVal = true;
        	}
        }
        return retVal;
    }
    
    private boolean isWebOrAzureProj(Object object)
    		throws CoreException, WindowsAzureInvalidProjectOperationException {
    	boolean retVal = false;
    	IProject project = (IProject) object;
    	if (project.isOpen()) {
    		ProjExportType type = ProjectNatureHelper.getProjectNature(project);
    		if ((type != null && type.equals(ProjExportType.WAR))
    				|| project.hasNature(WAProjectNature.NATURE_ID)){
    			retVal = true;
    		}
    	}
    	return retVal;
    }

    /**
     * Method checks if auto is present as storage account then
     * valid publish information is present.
     * @param object
     * @return
     * @throws CoreException
     * @throws WindowsAzureInvalidProjectOperationException
     */
    private boolean isFirstPackageWithAuto(Object object)
    		throws CoreException, WindowsAzureInvalidProjectOperationException {
    	boolean retVal = false;
    	IProject project = (IProject) object;
    	if (project.isOpen() && project.hasNature(WAProjectNature.NATURE_ID)) {
    		WindowsAzureProjectManager projMngr = WindowsAzureProjectManager.
    				load(new File(project.getLocation().toOSString()));
    		retVal = WAEclipseHelperMethods.isFirstPackageWithAuto(projMngr);
    	}
    	return retVal;
    }

    /**
     * Method checks and returns true,
     * if the file which is opened in editor
     * is of Azure Deployment project.
     * @param obj
     * @return
     * @throws CoreException
     */
    private boolean isProjFile(Object obj) throws CoreException {
    	boolean isProjFile = false;
    	IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    	if (window != null) {
    		IWorkbenchPage page = window.getActivePage();
    		/*
    		 * To avoid null pointer exception when we close any file
    		 * and as a result of which there is no active editor.
    		 */
    		if (page.getActiveEditor() != null) {
    			IFile editFile = (IFile) page.getActiveEditor().
    					getEditorInput().getAdapter(IFile.class);
    			if (editFile != null) {
    				if (editFile.getProject().
    						hasNature(WAProjectNature.NATURE_ID)) {
    					isProjFile = true;
    				}
    			}
    		}
    	}
    	return isProjFile;
    }
    
    /**
     * Method checks and returns true,
     * if the file which is opened in editor
     * is of Azure Deployment project and contains publish information
     * if auto is present as storage account.
     * @param obj
     * @return
     * @throws CoreException
     * @throws WindowsAzureInvalidProjectOperationException
     */
    private boolean isProjFileAndisFirstPackageWithAuto(Object obj)
    		throws CoreException, WindowsAzureInvalidProjectOperationException {
    	boolean isProjFile = false;
    	IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    	if (window != null) {
    		IWorkbenchPage page = window.getActivePage();
    		/*
    		 * To avoid null pointer exception when we close any file
    		 * and as a result of which there is no active editor.
    		 */
    		if (page.getActiveEditor() != null) {
    			IFile editFile = (IFile) page.getActiveEditor().
    					getEditorInput().getAdapter(IFile.class);
    			if (editFile != null) {
    				isProjFile = isFirstPackageWithAuto(editFile.getProject());
    			}
    		}
    	}
    	return isProjFile;
    }

    private boolean isWindows() {
        return Activator.IS_WINDOWS;
    }
}
