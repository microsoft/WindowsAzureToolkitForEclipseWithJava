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
package com.persistent.ui.decorator;

import java.net.URL;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

import waeclipseplugin.Activator;

import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.persistent.ui.propertypage.WAProjectNature;

/**
 * This class sets the icon for project
 * and icon for role folder.
 */
public class LightWeightDecorator implements ILightweightLabelDecorator {

    @Override
    public void addListener(ILabelProviderListener listener) {

    }

    @Override
    public void dispose() {

    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {

    }

    @Override
    public void decorate(Object element, IDecoration decoration) {
        if (element instanceof IProject) {
            try {
                IProject project = (IProject) element;
                if (!project.isOpen() || !project.hasNature(
                		WAProjectNature.NATURE_ID)) {
                    return;
                }
                URL url = null;
                URL resolve = null;
                url = Activator.getDefault().getBundle().getEntry(
                		Messages.projFolder);
                URL fileURL = FileLocator.toFileURL(url);
                resolve = FileLocator.resolve(fileURL);
                ImageDescriptor overlay = ImageDescriptor.createFromURL(
                		resolve);
                decoration.addOverlay(overlay, IDecoration.TOP_LEFT);
            } catch (Exception e) {
            	//This class sets the icons for folders, which is not an user
            	//initiated process so only logging the exception.
                Activator.getDefault().log(Messages.errProjIcon,
                        e);
            }
        } else if (element instanceof IFolder) {
            IFolder folder = (IFolder) element;
            IProject project = folder.getProject();
            try {
                if (!project.isOpen() || !project.hasNature(
                		WAProjectNature.NATURE_ID)) {
                    return;
                }
                WindowsAzureProjectManager projMngr =
                    WindowsAzureProjectManager.load(project.getLocation().toFile());
                WindowsAzureRole role = projMngr.roleFromPath(
                		folder.getLocation().toFile());
                if (role == null) {
                    return;
                }
                URL url = null;
                URL resolve = null;
                url = Activator.getDefault().getBundle().getEntry(
                		Messages.roleFolder);
                URL fileURL = FileLocator.toFileURL(url);
                resolve = FileLocator.resolve(fileURL);
                ImageDescriptor overlay = ImageDescriptor.createFromURL(
                		resolve);
                decoration.addOverlay(overlay, IDecoration.TOP_RIGHT);
            } catch (Exception e) {
            	//This class sets the icons for folders, which is not an user
            	//initiated process so only logging the exception.
                Activator.getDefault().log(Messages.errRoleIcon,
                        e);
            }
        }
    }

}
