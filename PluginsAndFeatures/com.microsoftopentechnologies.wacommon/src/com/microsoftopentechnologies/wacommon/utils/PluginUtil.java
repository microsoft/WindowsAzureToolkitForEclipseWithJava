/**
 * Copyright 2014 Microsoft Open Technologies Inc.
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

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.osgi.service.prefs.Preferences;

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
        } else if (element instanceof IResource) {
        	resource = (IResource) element;
        	selProject = resource.getProject();
        } else {
        	IWorkbenchPage page = window.getActivePage();
        	IEditorPart editorPart = page.getActiveEditor();
        	if (editorPart != null) {
        		IFile file = (IFile) editorPart.getEditorInput().getAdapter(IFile.class);
        		selProject = file.getProject();
        	}
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

    /**
     * Gets preferences object according to node name.
     * @return
     */
    @SuppressWarnings("deprecation")
    public static Preferences getPrefs() {
    	Preferences prefs = null;
    	if (isHelios()) {
    		prefs = new InstanceScope().getNode(Messages.prefFileName);
    	} else {
    		prefs = InstanceScope.INSTANCE.getNode(Messages.prefFileName);
    	}
    	return prefs;
    }

    /**
     * Method checks version of the eclipse.
     * If its helios then returns true.
     * @return
     */
    private static boolean isHelios() {
    	Version version = Platform.getBundle(Messages.bundleName).getVersion();
    	int majorVersion = version.getMajor();
    	if (majorVersion == 3) { // indigo and helios
    		int minorVersion = version.getMinor();
    		if (minorVersion < 7) { // helios 3.6 and lower versions
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
	 * Gets location of Azure Libraries
     * @throws WACommonException 
	 */
	public static String getAzureLibLocation() throws WACommonException {
		String libLocation = null;
		
		try {
			//get bundle for the sdk
	        Bundle bundle = Platform.getBundle(Messages.sdkLibBundleName);
	        
	        if (bundle == null) {
	        	throw new WACommonException(Messages.SDKLocErrMsg);
	        } else {
	            //locate sdk jar in bundle
	            URL url = FileLocator.find(bundle,new Path(Messages.sdkLibBaseJar), null);
	            if (url == null) {
	            	throw new WACommonException(Messages.SDKLocErrMsg);
	            } else {
	                //if jar is found then resolve url and get the location
	                url = FileLocator.resolve(url);
	                File loc = new File(url.getPath());
	                libLocation = loc.getParentFile().getAbsolutePath();
	            }
	        }
		} catch (WACommonException e) {
	    	e.printStackTrace(); 
			throw e;	    	 
	     } catch (IOException e) {
	    	 e.printStackTrace();
	    	 throw new WACommonException(Messages.SDKLocErrMsg);
		}
       
        return libLocation;
	}
}
