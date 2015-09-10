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
package com.microsoftopentechnologies.wacommon.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;
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

	public static final String pluginFolder = getPluginFolderPathUsingBundle();

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
	public static Preferences getPrefs(String qualifier) {
		Preferences prefs = null;
		if (isHelios()) {
			prefs = new InstanceScope().getNode(qualifier);
		} else {
			prefs = InstanceScope.INSTANCE.getNode(qualifier);
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

	public static String getPrefFilePath() {
		String prefFilePath = String.format("%s%s%s%s%s",
				pluginFolder,
				File.separator,
				Messages.waCommonFolderID,
				File.separator,
				"preferencesets.xml");
		return prefFilePath;
	}

	public static String getEncPath() {
		String encPath = String.format("%s%s%s",
				pluginFolder, File.separator,
				Messages.waCommonFolderID);
		return encPath;
	}

	public static String getPluginFolderPathUsingBundle() {
		Bundle bundle = Activator.getDefault().getBundle();
		URL url = bundle.getEntry("/");
		String pluginFolderPath = "";
		try {
			@SuppressWarnings("deprecation")
			URL resolvedURL = Platform.resolve (url);
			File file = new File (resolvedURL.getFile());
			String path = file.getParentFile().getAbsolutePath();

			// Default values for Linux
			String fileTxt = "file:";
			int index = 5;
			if (path.contains(fileTxt)) {
				if (Activator.IS_WINDOWS) {
					fileTxt = fileTxt + File.separator;
					index = 6;
				}
				pluginFolderPath = path.substring(path.indexOf(fileTxt) + index);
			} else {
				// scenario when we run source code
				pluginFolderPath = String.format("%s%s%s",
						Platform.getInstallLocation().getURL().getPath().toString(),
						File.separator, Messages.pluginFolder);
				if (Activator.IS_WINDOWS) {
					pluginFolderPath = pluginFolderPath.substring(1);
				}
			}
			Activator.getDefault().log("Plugin folder path:" + pluginFolderPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pluginFolderPath;
	}

	/**
	 * Refreshes the workspace.
	 */
	public static void refreshWorkspace() {
		try {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceRoot root = workspace.getRoot();
			root.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			//This is just a try to refresh workspace.
			//User can also refresh the workspace manually.
			//So user should not get any exception prompt.
			Activator.getDefault().log(Messages.resCLExWkspRfrsh, null);
		}
	}

	/**
	 * Method opens property dialog with only desired property page.
	 * 
	 * @param nodeId
	 *            : Node ID of property page
	 * @param nodeLbl
	 *            : Property page name
	 * @param classObj
	 *            : Class object of property page
	 * @return
	 */
	public static int openPropertyPageDialog(String nodeId, String nodeLbl,
			Object classObj) {
		int retVal = Window.CANCEL; // value corresponding to cancel
		// Node creation
		try {
			PreferenceNode nodePropPg = new PreferenceNode(nodeId, nodeLbl,
					null, classObj.getClass().toString());
			nodePropPg.setPage((IPreferencePage) classObj);
			nodePropPg.getPage().setTitle(nodeLbl);

			PreferenceManager mgr = new PreferenceManager();
			mgr.addToRoot(nodePropPg);
			// Dialog creation
			PreferenceDialog dialog = new PreferenceDialog(PlatformUI
					.getWorkbench().getDisplay().getActiveShell(), mgr);
			// make desired property page active.
			dialog.setSelectedNode(nodeLbl);
			dialog.create();
			/*
			 * If showing storage accounts preference page, don't show
			 * properties for title as its common repository.
			 */
			String dlgTitle = "";
			if (nodeLbl.equals(Messages.cmhLblStrgAcc)
					|| nodeLbl.equals(Messages.aiTxt)) {
				dlgTitle = nodeLbl;
			} else {
				dlgTitle = String.format(Messages.cmhPropFor,
						getSelectedProject().getName());
			}
			dialog.getShell().setText(dlgTitle);
			dialog.open();
			// return whether user has pressed OK or Cancel button
			retVal = dialog.getReturnCode();
		} catch (Exception e) {
			PluginUtil.displayErrorDialogAndLog(new Shell(),
					Messages.rolsDlgErr, Messages.projDlgErrMsg, e);
		}
		return retVal;
	}

	/**
	 * Method will change cursor type whenever required.
	 * @param busy
	 * true : Wait cursor
	 * false : Normal arrow cursor
	 */
	public static void showBusy(final boolean busy) {
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				Shell shell = Display.getDefault().getActiveShell();
				if (busy) { //show Busy Cursor
					Cursor cursor = Display.getDefault().getSystemCursor(SWT.CURSOR_WAIT);
					shell.setCursor(cursor);
				} else {
					shell.setCursor(null);
				}
			}
		});
	}

	/**
	 * Method will change cursor type of particular shell whenever required.
	 * @param busy
	 * @param shell
	 */
	public static void showBusy(final boolean busy, final Shell shell) {
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				if (busy) { //show Busy Cursor
					Cursor cursor = Display.getDefault().getSystemCursor(SWT.CURSOR_WAIT);
					shell.setCursor(cursor);
				} else {
					shell.setCursor(null);
				}
			}
		});
	}
}
