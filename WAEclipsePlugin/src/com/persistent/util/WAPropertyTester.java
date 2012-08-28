/**
* Copyright 2011 Persistent Systems Ltd.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.persistent.util;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import waeclipseplugin.Activator;

import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.persistent.ui.propertypage.WAProjectNature;
import com.persistent.util.ProjectNatureHelper.ProjExportType;

/**
 * Provides implementation for following tests :
 *
 * 1) isRoleFolder : to determine if a folder is role folder or not.
 *
 * 2) isRolePrefNode : to determine if the node is a particular
 *                     role related node.
 *
 */
public class WAPropertyTester extends PropertyTester {

    @Override
    public boolean test(Object object, String property, Object[] args, Object value) {
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
        IProject project = (IProject)object;
        if (project.isOpen()) {
        ProjExportType type = ProjectNatureHelper.getProjectNature(project);
        if (type!=null && type.equals(ProjExportType.WAR)){
            retVal = true;
        }
        }
        return retVal;
    }

    private boolean isProjFile(Object obj) throws CoreException {
        boolean isProjFile = false;
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IFile editFile = (IFile)page.getActiveEditor().getEditorInput().getAdapter(IFile.class);
        if (editFile != null) {
        if (editFile.getProject().hasNature(WAProjectNature.NATURE_ID)) {
            isProjFile = true;
        }
        }
        return isProjFile;
    }

}
