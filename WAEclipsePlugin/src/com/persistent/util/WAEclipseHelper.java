/**
* Copyright 2012 Persistent Systems Ltd.
*
* Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*	http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/
package com.persistent.util;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.persistent.ui.projwizard.Messages;

import waeclipseplugin.Activator;

/**
 * This class contains common utility methods.
 *
 */
public class WAEclipseHelper {
	/**
	 * @return Template(componentssets.xml)
	 */
	public static String getTemplateFile() {
		String file = String.format("%s%s%s%s%s%s%s", 
				Platform.getInstallLocation().getURL().getPath().toString(),
				File.separator, Messages.pluginFolder, 
				File.separator, Messages.pluginId, File.separator, 
				Messages.cmpntFile);
		return file;
	}
	
	/**
	 * To delete directory having contents within it.
	 * @param dir
	 */
	public static void deleteDirectory(File dir) {
		if (dir.isDirectory()) {
			//directory is empty, then delete it
			if (dir.list().length == 0) {
				dir.delete();
			} else {
				//list all the directory contents
				String[] subFiles = dir.list();
				for (int i = 0; i < subFiles.length; i++) {
					//construct the file structure
					File fileDelete = new File(dir, subFiles[i]);
					//recursive delete
					deleteDirectory(fileDelete);
				}
				//check the directory again, if empty then delete it
				if (dir.list().length == 0) {
					dir.delete();
				}
			}
		} else {
			dir.delete();
		}
	}

	/**
	 * Converts Windows path into regex including wildcards
	 * @param windowsPath
	 * @return
	 */
	private static String windowsPathToRegex(String windowsPath)
	{
		if(windowsPath == null)
			return null;
		
		// Escape special characters
		String regex = windowsPath.replaceAll("([\\\"\\+\\(\\)\\^\\$\\.\\{\\}\\[\\]\\|\\\\])", "\\\\$1");
		
		// Replace wildcards
		return regex.replace("*", ".*").replace("?", ".");
	}
		
	/**
	 * Returns the server name whose detection patterns is matched under path
	 * @param serverDetectors
	 * @param path
	 * @return
	 */
	public static String detectServer(File path)
	{
		Map<String, String> serverDetectors;
		
		// Get the templates files
		String templateFilePath = WAEclipseHelper.getTemplateFile();
		if(templateFilePath == null || path == null || !path.isDirectory() || !path.exists())
			return null;

		// Get the server detectors from the templates
		try {
			if(null == (serverDetectors = WindowsAzureProjectManager.getServerTemplateDetectors(new File(templateFilePath)))) {
				return null;
			}
		} catch (WindowsAzureInvalidProjectOperationException e) {
			return null;
		}
		
		// Check each pattern
		for(Map.Entry<String, String> entry : serverDetectors.entrySet()) {
			String serverName = entry.getKey();
			String patternText = entry.getValue();
			if(patternText == null || patternText.isEmpty())
				continue;

			// Fast path: Check the pattern directly (like, if it has no wild cards)
			File basePathFile = new File(path, patternText);
			if(basePathFile.exists())
				return serverName;
			
			// Split pattern path into parts and check for existence of each part
			basePathFile = path;
			String[] pathParts = patternText.split("\\\\");
			boolean foundSoFar = false;
			for(int i=0; i<pathParts.length; i++) {
				String pathPart = pathParts[i];
				// Try direct match first
				File pathPartFile = new File(basePathFile, pathPart);
				if(pathPartFile.exists()) {
					foundSoFar = true;
				// Check for wildcards
				} else if(!pathPart.contains("*") && !pathPart.contains("?")) {
					foundSoFar = false;
				// Wildcards present, so check pattern
				} else {
					String[] fileNames = basePathFile.list();
					String patternRegex = windowsPathToRegex(pathPart) + "$";
					Pattern pattern = Pattern.compile(patternRegex);
					Matcher matcher = pattern.matcher("");
					foundSoFar = false;
					for(String fileName : fileNames) {
						matcher.reset(fileName);
						if(matcher.find()) {
							foundSoFar = true;
							break;
						}
					}
				}
				
				if(foundSoFar) {
					basePathFile = new File(basePathFile, pathPart);
				} else {
					break;
				}
			}
			
			if(foundSoFar)
				return serverName;
		}
		
		// No matches found
		return null;		
	}
	
		
	/**
	 * This method will refresh the workspace.If any changes are made in any
	 * configuration files through UI in that case it will refresh the
	 * workspace so that user can see the correct/modified files.
	 * @param errorTitle
	 * @param errorMessage
	 */
	public static void refreshWorkspace(String errorTitle, String errorMessage) {
		try {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceRoot root = workspace.getRoot();
			root.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			Activator.getDefault().log(errorMessage, e);
			MessageUtil.displayErrorDialog(new Shell(),
					errorTitle, errorMessage);
		}
	}

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
		if (element instanceof IResource) {
			resource = (IResource) element;
			selProject = resource.getProject();
		} else {
			IWorkbenchPage page = window.getActivePage();
			IFile file = (IFile) page.getActiveEditor().
					getEditorInput().getAdapter(IFile.class);
			selProject = file.getProject();
		}
		return selProject;
	}
	
    /**
     * Returns default JDK path
     * @return
     */
    public static String jdkDefaultDirectory(String currentlySelectedDir) {
    	File file;
    	
    	// Try currently selected JDK path
    	String path = currentlySelectedDir;
    	if(path != null && !path.isEmpty()) {
    		file= new File(path);
    		if(file.isDirectory() && file.exists()) {
    			return path;
    		}
    	}
    	
    	// Try JAVA_HOME
        path = System.getenv("JAVA_HOME");
        if(path != null && !path.isEmpty())
        {
        	file = new File(path);
	    	if(file.exists() && file.isDirectory()) {
	    		// Verify presence of javac.exe
	    		File javacFile = new File(file, "bin\\javac.exe");
	    		if(javacFile.exists()) {
	    			return path;
	    		}
	    	}
        }
    		
    	// Try %ProgramFiles%\Java
        path = String.format("%s%s%s", System.getenv("ProgramFiles"), File.separator, "Java", File.separator);
    	file = new File(path);
    	if(!file.exists() || !file.isDirectory()) {
    		return System.getenv("ProgramFiles");
    	} 
    	
    	// Find the first entry under Java that contains jdk
    	File[] files = file.listFiles();
    	for(File subFile : files) {
    		if(!subFile.isDirectory()) {
    			continue;
    		} else if(subFile.getName().contains("jdk")) {
    			return subFile.getAbsolutePath();
    		}
    	}
    	
    	return path;
    			
    }
    
    /*
     * This API compares if two files content is identical. It ignores extra spaces and new lines
     * while comparing
     */
	public static boolean isFilesIdentical(File sourceFile,File destFile) throws Exception {
		try {
			Scanner sourceFileScanner = new Scanner(sourceFile);
			Scanner destFileScanner   = new Scanner(destFile);
			
			while(sourceFileScanner.hasNext() ) {
				// If source file is having next token then destination file also should have next token , 
				// else they are not identical.
				if(!destFileScanner.hasNext())
					return false;
				
				if(!sourceFileScanner.next().equals(destFileScanner.next()))
					return false;
			}
			
			// Handling the case where source file is empty and destination file is having text
			if(destFileScanner.hasNext())
				return false;
			else
				return true;
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/*
	 * This returns the resource has a file.
	 * @returns File pointing to resource. null if file doesnot exists
	 */
	
	public static File getResourceAsFile(String fileEntry) {
		File file = null;
    	try {
    		URL url 	= Activator.getDefault().getBundle().getEntry(fileEntry);
    		URL fileURL = FileLocator.toFileURL(url);
    		URL resolve = FileLocator.resolve(fileURL);
    		file 		= new File(resolve.getFile());
    	} catch(Exception e){
                Activator.getDefault().log(e.getMessage(), e);
    	}
    	return file;
		
	}
}
