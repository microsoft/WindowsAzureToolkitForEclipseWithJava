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
