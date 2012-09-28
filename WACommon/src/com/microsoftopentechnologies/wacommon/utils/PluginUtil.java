/**
 * Copyright 2012 Microsoft Open Technologies Inc.
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
package com.microsoftopentechnologies.wacommon.utils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.microsoftopentechnologies.wacommon.Activator;


public class PluginUtil {
	
	/**
     * This method returns currently selected project in workspace.
     * @return IProject
     */
    public static IProject getSelectedProject() {
        IWorkbench workbench = PlatformUI.getWorkbench();
        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        ISelectionService service = window.getSelectionService();
        ISelection selection = service.getSelection();
        Object element = null;
        IResource resource;
        IProject selProject = null;
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSel =
            		(IStructuredSelection) selection;
            element = structuredSel.getFirstElement();
        }
        if (element instanceof IProject) {
            resource = (IResource) element;
            selProject = resource.getProject();
        } else if (element instanceof IJavaProject) {
            IJavaProject proj = ((IJavaElement) element).getJavaProject();
            selProject = proj.getProject();
        }
        return selProject;
    }
    
    /**
     * This method will display the error message box when any error occurs.It takes two parameters
     *
     * @param shell       parent shell
     * @param title       the text or title of the window.
     * @param message     the message which is to be displayed
     */
    public static void displayErrorDialog (Shell shell , String title , String message ){
         MessageDialog.openError(shell, title, message);
    }
    
    public static void displayErrorDialogAndLog(Shell shell, String title, String message, Exception e) { 
    	Activator.getDefault().log(message, e); 
    	displayErrorDialog(shell, title, message);	
    }
}
