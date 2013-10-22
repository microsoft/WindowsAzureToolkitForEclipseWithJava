/**
* Copyright 2013 Persistent Systems Ltd.
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import waeclipseplugin.Activator;

import com.gigaspaces.azure.propertypage.SubscriptionPropertyPage;
import com.interopbridges.tools.windowsazure.WindowsAzureConstants;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.microsoftopentechnologies.wacommon.utils.FileUtil;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
import com.persistent.ui.propertypage.WARemoteAccessPropertyPage;
import com.persistent.ui.propertypage.WARolesPropertyPage;
import com.persistent.ui.propertypage.WAWinAzurePropertyPage;
import com.persistent.winazureroles.WARCaching;
import com.persistent.winazureroles.WARComponents;
import com.persistent.winazureroles.WARDebugging;
import com.persistent.winazureroles.WAREndpoints;
import com.persistent.winazureroles.WAREnvVars;
import com.persistent.winazureroles.WARGeneral;
import com.persistent.winazureroles.WARLoadBalance;
import com.persistent.winazureroles.WARLocalStorage;
import com.persistent.winazureroles.WAServerConfiguration;

/**
 * This class contains common utility methods.
 *
 */
public class WAEclipseHelper {
	/**
	 * @return Template(componentssets.xml)
	 */
	public static String getTemplateFile(String fileName) {
		String file = String.format("%s%s%s%s%s%s%s", 
				Platform.getInstallLocation().getURL().getPath().toString(),
				File.separator, Messages.pluginFolder, 
				File.separator, Messages.pluginId, File.separator, 
				fileName);
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
	 * Converts Windows path into regex including wildcards.
	 * @param windowsPath
	 * @return
	 */
	private static String windowsPathToRegex(String windowsPath) {
		if (windowsPath == null) {
			return null;
		}

		// Escape special characters
		String regex = windowsPath.
				replaceAll("([\\\"\\+\\(\\)\\^\\$\\.\\{\\}\\[\\]\\|\\\\])", "\\\\$1");

		// Replace wildcards
		return regex.replace("*", ".*").replace("?", ".");
	}

	/**
	 * Looks for a pattern in a text file
	 * @param file
	 * @param pattern
	 * @return True if a pattern is found, else false
	 * @throws FileNotFoundException 
	 */
	private static boolean isPatternInFile(File file, String patternText) {
		Scanner fileScanner = null;
		
		if (file.isDirectory()) {
			return false;
		}
		try {
			fileScanner = new Scanner(file);
			Pattern pattern =  Pattern.compile(patternText, Pattern.CASE_INSENSITIVE);  
			Matcher matcher = null;
			while (fileScanner.hasNextLine()) {
				String line = fileScanner.nextLine(); 
				matcher = pattern.matcher(line);
				if (matcher.find()) {
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			return false;
		} finally {
			if(fileScanner != null) {
				fileScanner.close();
			}
		}
	}
	
	
	/**
	 * Returns the server name whose detection patterns is matched under path.
	 * @param serverDetectors
	 * @param path
	 * @return
	 */
	public static String detectServer(File path) {
		final Map<String, String> serverDetectors;
		final Map<String, String> serverPatterns;

		// Get the templates files
		final String templateFilePath = WAEclipseHelper.getTemplateFile(Messages.cmpntFileName);
		if (templateFilePath == null || path == null || !path.isDirectory() || !path.exists()) {
			return null;
		}

		// Get the server detectors from the templates
		final File templateFile = new File(templateFilePath);
		try {
			if (null == (serverDetectors = WindowsAzureProjectManager.getServerTemplateDetectors(templateFile))) {
				return null;
			} else if (null == (serverPatterns = WindowsAzureProjectManager.getServerTemplatePatterns(templateFile))) {
				return null;
			}
		} catch (WindowsAzureInvalidProjectOperationException e) {
			return null;
		}

		// Check each path
		boolean foundSoFar = false;
		String serverName = null;
		File basePathFile= null;
		for (Map.Entry<String, String> entry : serverDetectors.entrySet()) {
			serverName = entry.getKey();
			String pathPatternText = entry.getValue();
			String textPatternText = serverPatterns.get(serverName);
			if (pathPatternText == null || pathPatternText.isEmpty()) {
				continue;
			}

			// Fast path: Check the pattern directly (like, if it has no wild cards)
			basePathFile = new File(path, pathPatternText);
			if (basePathFile.exists()) {
				// Check for the required pattern inside (if any)
				if (basePathFile.isDirectory() || textPatternText == null || isPatternInFile(basePathFile, textPatternText)) {
					return serverName;
				} else {
					continue;
				}
			}

			//Split pattern path into parts and check for existence of each part
			basePathFile = path;
			String[] pathParts = pathPatternText.split("\\\\");
			foundSoFar = false;
			for (int i = 0; i < pathParts.length; i++) {
				String pathPart = pathParts[i];
				
				// Try direct match first
				File pathPartFile = new File(basePathFile, pathPart);
				if (pathPartFile.exists()) {
					foundSoFar = true;
					
				// Check for wildcards
				} else if (!pathPart.contains("*") && !pathPart.contains("?")) {
					foundSoFar = false;
					
				// Wildcards present, so check pattern
				} else {
					String[] fileNames = basePathFile.list();
					String pathPatternRegex = windowsPathToRegex(pathPart) + "$";
					Pattern pathPattern = Pattern.compile(pathPatternRegex);
					Matcher matcher = pathPattern.matcher("");
					foundSoFar = false;
					for (String fileName : fileNames) {
						matcher.reset(fileName);
						if (matcher.find()) {
							File file;
							foundSoFar = true;

							if (textPatternText == null) {
								// No text pattern to look for inside, so allow for the match to proceed
								break;
							} else if(i != pathParts.length-1) {
								// Path part not terminal, so allow the match to proceed
								break;
							} else if(!(file = new File(basePathFile, fileName)).isFile()) {
								// Terminal path not a file so don't proceed with this file
								continue;
							} else if(isPatternInFile(file, textPatternText)) {
								// Internal text pattern matched, so success
								return serverName;
							}
						}
					}
				}

				// If this path part worked so far, expand the base path and dig deeper
				if (foundSoFar) {
					basePathFile = new File(basePathFile, pathPart);
				} else {
					break;
				}
			}

			// If matched a full path, then success
			if (foundSoFar) {
				return serverName;
			}
		}
		
		return null;
	}

	/**
	 * This method will refresh the workspace.If any changes are made in any
	 * configuration files through UI in that case it will refresh the
	 * workspace so that user can see the correct/modified files.
	 * @param errorTitle
	 * @param errorMessage
	 */
	public static void refreshWorkspace(
			String errorTitle, String errorMessage) {
		try {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceRoot root = workspace.getRoot();
			root.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			PluginUtil.displayErrorDialogAndLog(
					new Shell(),
					errorTitle,
					errorMessage, e);
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
	 * Returns default JDK path.
	 * @param currentlySelectedDir
	 * @return
	 */
	public static String jdkDefaultDirectory(
			String currentlySelectedDir) {
		File file;

		// Try currently selected JDK path
		String path = currentlySelectedDir;
		if (path != null && !path.isEmpty()) {
			file = new File(path);
			if (file.isDirectory() && file.exists()) {
				return path;
			}
		}

		// Try JAVA_HOME
		path = System.getenv("JAVA_HOME");
		if (path != null && !path.isEmpty()) {
			file = new File(path);
			if (file.exists() && file.isDirectory()) {
				// Verify presence of javac.exe
				File javacFile = new File(file,
						"bin\\javac.exe");
				if (javacFile.exists()) {
					return path;
				}
			}
		}

		// Try under %ProgramFiles%\Java
		path = String.format("%s%s%s",
				System.getenv("ProgramFiles"),
				File.separator, "Java", File.separator);
		file = new File(path);
		if (!file.exists() || !file.isDirectory()) {
			return "";
		}

		// Find the first entry under Java that contains jdk
		File[] files = file.listFiles();
		for (File subFile : files) {
			if (!subFile.isDirectory()) {
				continue;
			} else if (subFile.getName().contains("jdk")) {
				return subFile.getAbsolutePath();
			}
		}
		return "";
	}

    /**
     * This API compares if two files content is identical.
     * It ignores extra spaces and new lines while comparing
     * @param sourceFile
     * @param destFile
     * @return
     * @throws Exception
     */
	public static boolean isFilesIdentical(File sourceFile,
			File destFile)
			throws Exception {
		try {
			Scanner sourceFileScanner = new Scanner(sourceFile);
			Scanner destFileScanner   = new Scanner(destFile);

			while (sourceFileScanner.hasNext()) {
				/*
				 * If source file is having next token
				 * then destination file also should have next token,
				 *	else they are not identical.
				 */
				if (!destFileScanner.hasNext()) {
					destFileScanner.close();
					sourceFileScanner.close();
					return false;
				}
				if (!sourceFileScanner.next().
						equals(destFileScanner.next())) {
					sourceFileScanner.close();
					destFileScanner.close();
					return false;
				}
			}
			/*
			 * Handling the case where source file is empty
			 * and destination file is having text
			 */
			if (destFileScanner.hasNext()) {
				destFileScanner.close();
				sourceFileScanner.close();
				return false;
			} else {
				destFileScanner.close();
				sourceFileScanner.close();
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * This returns the resource has a file.
	 * @param fileEntry : File pointing to resource.
	 * 					 null if file doesn't exists
	 * @return
	 */
	public static File getResourceAsFile(String fileEntry) {
		File file = null;
		try {
			URL url = Activator.getDefault().
					getBundle().getEntry(fileEntry);
			URL fileURL = FileLocator.toFileURL(url);
			URL resolve = FileLocator.resolve(fileURL);
			file 		= new File(resolve.getFile());
		} catch (Exception e) {
			Activator.getDefault().log(e.getMessage(), e);
		}
		return file;
	}

	/**
	 * Method checks if text contains
	 * alphanumeric characters, underscore only
	 * and is starting with alphanumeric or underscore.
	 * \p{L} any kind of letter from any language.
	 * \p{Nd} a digit zero through nine
	 * in any script except ideographic scripts.
	 * @param text
	 * @return Boolean
	 */
	public static Boolean isAlphaNumericUnderscore(String text) {
		Pattern alphaNumUndscor = Pattern.
				compile("^[\\p{L}_]+[\\p{L}\\p{Nd}_]*$");
		Matcher m = alphaNumUndscor.matcher(text);
		return m.matches();
	}

	/**
	 * Method checks if text contains
	 * alphanumeric lower case characters and integers only.
	 * @param text
	 * @return
	 */
	public static Boolean isLowerCaseAndInteger(String text) {
		Pattern lowerCaseInteger = Pattern.
				compile("^[a-z0-9]+$");
		Matcher m = lowerCaseInteger.matcher(text);
		return m.matches();
	}

	/**
	 * Method creates tree structure of windows azure
	 * property pages.
	 * and opens property dialog
	 * with desired property page selected & active.
	 * @param windowsAzureRole : worker role
	 * @param pageToDisplay : property page Id which should be active
	 *                        after opening dialog
	 * @return integer
	 */
	public static int openRolePropertyDialog(
			WindowsAzureRole windowsAzureRole,
			String pageToDisplay) {
		int retVal = Window.CANCEL; //value corresponding to cancel
		try {
			// Node creation
			PreferenceNode nodeGeneral = new PreferenceNode(
					Messages.cmhIdGeneral,
					Messages.cmhLblGeneral,
					null, WARGeneral.class.toString());
			nodeGeneral.setPage(new WARGeneral());
			nodeGeneral.getPage().setTitle(Messages.cmhLblGeneral);

			PreferenceNode nodeCache = new PreferenceNode(
					Messages.cmhIdCach,
					Messages.cmhLblCach,
					null, WARCaching.class.toString());
			nodeCache.setPage(new WARCaching());
			nodeCache.getPage().setTitle(Messages.cmhLblCach);

			PreferenceNode nodeCmpnts = new PreferenceNode(
					Messages.cmhIdCmpnts,
					Messages.cmhLblCmpnts,
					null, WARComponents.class.toString());
			nodeCmpnts.setPage(new WARComponents());
			nodeCmpnts.getPage().setTitle(Messages.cmhLblCmpnts);

			PreferenceNode nodeDebugging = new PreferenceNode(
					Messages.cmhIdDbg, Messages.cmhLblDbg,
					null, WARDebugging.class.toString());
			nodeDebugging.setPage(new WARDebugging());
			nodeDebugging.getPage().setTitle(Messages.cmhLblDbg);

			PreferenceNode nodeEndPts = new PreferenceNode(
					Messages.cmhIdEndPts,
					Messages.cmhLblEndPts,
					null, WAREndpoints.class.toString());
			nodeEndPts.setPage(new WAREndpoints());
			nodeEndPts.getPage().setTitle(Messages.cmhLblEndPts);

			PreferenceNode nodeEnvVars = new PreferenceNode(
					Messages.cmhIdEnvVars,
					Messages.cmhLblEnvVars,
					null, WAREnvVars.class.toString());
			nodeEnvVars.setPage(new WAREnvVars());
			nodeEnvVars.getPage().setTitle(Messages.cmhLblEnvVars);

			PreferenceNode nodeLdBlnc = new PreferenceNode(
					Messages.cmhIdLdBlnc,
					Messages.cmhLblLdBlnc,
					null, WARLoadBalance.class.toString());
			nodeLdBlnc.setPage(new WARLoadBalance());
			nodeLdBlnc.getPage().setTitle(Messages.cmhLblLdBlnc);

			PreferenceNode nodeLclStg = new PreferenceNode(
					Messages.cmhIdLclStg,
					Messages.cmhLblLclStg,
					null, WARLocalStorage.class.toString());
			nodeLclStg.setPage(new WARLocalStorage());
			nodeLclStg.getPage().setTitle(Messages.cmhLblLclStg);

			PreferenceNode nodeSrvCnfg = new PreferenceNode(
					Messages.cmhIdSrvCnfg,
					Messages.cmhLblSrvCnfg,
					null,
					WAServerConfiguration.class.toString());
			nodeSrvCnfg.setPage(new WAServerConfiguration());
			nodeSrvCnfg.getPage().setTitle(Messages.cmhLblSrvCnfg);

			/*
			 * Tree structure creation.
			 * Don't change order while adding nodes.
			 * Its the default alphabetical order given by eclipse.
			 */
			nodeGeneral.add(nodeCache);
			nodeGeneral.add(nodeCmpnts);
			nodeGeneral.add(nodeDebugging);
			nodeGeneral.add(nodeEndPts);
			nodeGeneral.add(nodeEnvVars);
			nodeGeneral.add(nodeLdBlnc);
			nodeGeneral.add(nodeLclStg);
			nodeGeneral.add(nodeSrvCnfg);

			PreferenceManager mgr = new PreferenceManager();
			mgr.addToRoot(nodeGeneral);
			// Dialog creation
			PreferenceDialog dialog = new
					PreferenceDialog(PlatformUI.getWorkbench()
							.getDisplay().getActiveShell(),
							mgr);
			// make desired property page active.
			dialog.setSelectedNode(pageToDisplay);
			dialog.create();
			String dlgTitle = String.format(Messages.cmhPropFor,
					windowsAzureRole.getName());
			dialog.getShell().setText(dlgTitle);
			dialog.open();
			// return whether user has pressed OK or Cancel button
			retVal = dialog.getReturnCode();
		} catch (Exception ex) {
			PluginUtil.displayErrorDialogAndLog(new Shell(),
					Messages.rolsDlgErr,
					Messages.rolsDlgErrMsg, ex);
		}
		return retVal;
	}

	/**
	 * Method creates tree structure of windows azure deployment
	 * project property pages.
	 * and opens property dialog
	 * with desired property page selected & active.
	 * @param pageToDisplay : property page Id which should be active
	 *                        after opening dialog
	 * @return integer
	 */
	public static int openWAProjectPropertyDialog(String pageToDisplay) {
		int retVal = Window.CANCEL; //value corresponding to cancel
		// Node creation
		try {
			PreferenceNode nodeWindowsAzure = new PreferenceNode(
					Messages.cmhIdWinAz,
					Messages.cmhLblWinAz,
					null, WAWinAzurePropertyPage.class.toString());
			nodeWindowsAzure.setPage(new WAWinAzurePropertyPage());
			nodeWindowsAzure.getPage().setTitle(Messages.cmhLblWinAz);

			PreferenceNode nodeRemoteAcess = new PreferenceNode(
					Messages.cmhIdRmtAces,
					Messages.cmhLblRmtAces,
					null, WARemoteAccessPropertyPage.class.toString());
			nodeRemoteAcess.setPage(new WARemoteAccessPropertyPage());
			nodeRemoteAcess.getPage().setTitle(Messages.cmhLblRmtAces);

			PreferenceNode nodeRoles = new PreferenceNode(
					Messages.cmhIdRoles,
					Messages.cmhLblRoles,
					null, WARolesPropertyPage.class.toString());
			nodeRoles.setPage(new WARolesPropertyPage());
			nodeRoles.getPage().setTitle(Messages.cmhLblRoles);

			PreferenceNode nodeSubscriptions = new PreferenceNode(
					Messages.cmhIdCrdntls,
					Messages.cmhLblSubscrpt,
					null, SubscriptionPropertyPage.class.toString());
			nodeSubscriptions.setPage(new SubscriptionPropertyPage());
			nodeSubscriptions.getPage().setTitle(Messages.cmhLblSubscrpt);

			/*
			 * Tree structure creation.
			 * Don't change order while adding nodes.
			 * Its the default alphabetical
			 * order given by eclipse.
			 */
			nodeWindowsAzure.add(nodeRemoteAcess);
			nodeWindowsAzure.add(nodeRoles);
			nodeWindowsAzure.add(nodeSubscriptions);

			PreferenceManager mgr = new PreferenceManager();
			mgr.addToRoot(nodeWindowsAzure);
			// Dialog creation
			PreferenceDialog dialog = new
					PreferenceDialog(PlatformUI.getWorkbench()
							.getDisplay().getActiveShell(),
							mgr);
			// make desired property page active.
			dialog.setSelectedNode(pageToDisplay);
			dialog.create();
			String dlgTitle = String.format(Messages.cmhPropFor,
					getSelectedProject().getName());
			dialog.getShell().setText(dlgTitle);
			dialog.open();
			// return whether user has pressed OK or Cancel button
			retVal = dialog.getReturnCode();
		} catch (Exception e) {
			PluginUtil.displayErrorDialogAndLog(new Shell(),
					Messages.rolsDlgErr,
					Messages.projDlgErrMsg, e);
		}
		return retVal;
	}

	/**
	 * Method opens property dialog
	 * with only desired property page.
	 * @param nodeId : Node ID of property page
	 * @param nodeLbl : Property page name
	 * @param classObj : Class object of property page
	 * @return
	 */
	public static int openPropertyPageDialog(String nodeId,
			String nodeLbl, Object classObj) {
		int retVal = Window.CANCEL; //value corresponding to cancel
		// Node creation
		try {
			PreferenceNode nodePropPg = new PreferenceNode(
					nodeId,
					nodeLbl,
					null, classObj.getClass().toString());
			nodePropPg.setPage((IPreferencePage) classObj);
			nodePropPg.getPage().setTitle(nodeLbl);

			PreferenceManager mgr = new PreferenceManager();
			mgr.addToRoot(nodePropPg);
			// Dialog creation
			PreferenceDialog dialog = new
					PreferenceDialog(PlatformUI.getWorkbench()
							.getDisplay().getActiveShell(),
							mgr);
			// make desired property page active.
			dialog.setSelectedNode(nodeLbl);
			dialog.create();
			/*
			 * If showing storage accounts preference page,
			 * don't show properties for title
			 * as its common repository.
			 */
			String dlgTitle = "";
			if (nodeLbl.equals(Messages.cmhLblStrgAcc)) {
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
					Messages.rolsDlgErr,
					Messages.projDlgErrMsg, e);
		}
		return retVal;
	}

	/**
	 * If project name present in package.xml and
	 * WindowsAzureProjectBuilder.launch does not match
	 * with the actual one then correct it accordingly.
	 * @param project
	 * @param mngr : WindowsAzureProjectManager
	 */
	public static void correctProjectName(IProject project,
			WindowsAzureProjectManager mngr) {
		String strPath = project.getLocation().toOSString();
		String launchFile =  strPath + File.separator
				+ Messages.resCLExtToolBldr
				+ File.separator
				+ Messages.resCLLaunchFile;
		try {
			ParseXML.setProjectNameinLaunch(launchFile,
					mngr.getProjectName(),
					project.getName());
			mngr.setProjectName(project.getName());
			mngr.save();
		} catch (Exception e) {
			Activator.getDefault().log(e.getMessage());
		}
	}

	/**
	 * Copy file from source to destination.
	 * @param source
	 * @param destination
	 * @throws Exception
	 */
	public static void copyFile(String source,
			String destination) throws Exception {
		try {
			File f1 = new File(source);
			File f2 = new File(destination);
			InputStream in = new FileInputStream(f1);
			OutputStream out = new FileOutputStream(f2);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}
	
	public static void handleProjectUpgrade(IProject iProject, WindowsAzureProjectManager projMngr) 
	throws IOException, WindowsAzureInvalidProjectOperationException, CoreException {
	
		URL 	url 	= Activator.getDefault().getBundle().getEntry(Messages.starterKitEntry);
    	URL 	resolve = FileLocator.resolve(FileLocator.toFileURL(url));
        File 	zipFile = new File(resolve.getFile());
    	
    	boolean result = FileUtil.copyFileFromZip(zipFile, "%proj%/"+Messages.skJarName, 
    			new File(iProject.getLocation().toFile(), Messages.skJarName));
    	
    	if (result) {
    		upgradeWAPFiles(iProject, zipFile, projMngr);
    	}

    	// If not able to copy just log the error and close the project.
    	if (!result) {
    		Activator.getDefault().log(iProject.getName() + ".cspack.jar file is not updated", null);
    		iProject.close(null);
    	} else {
    		projMngr.setVersion(WindowsAzureConstants.VERSION);
    		projMngr.save();
    	}
		
	}
	
	private static void upgradeWAPFiles(IProject iProject, File starterKitZip, WindowsAzureProjectManager projMngr) 
			throws IOException, WindowsAzureInvalidProjectOperationException {
    	List<WindowsAzureRole> rolesList = projMngr.getRoles();
    	
    	for (WindowsAzureRole role: rolesList) {
    		// Copy session affinity files if SA is enabled
    		if (role.getSessionAffinityInputEndpoint() != null) {
    			projMngr.copySAResources(role.getName());
    		}
    		
    		//Copy or rewrite .wash script
    		FileUtil.copyFileFromZip(starterKitZip, "%proj%/WorkerRole1/approot/util/"+Messages.washFileName,
        			new File(iProject.getLocation().toFile(), role.getName()+"/approot/util/"+Messages.washFileName));
    	}
    	
    	// Copy or rewrite .templates/startup/.startup.cmd
    	FileUtil.copyFileFromZip(starterKitZip, "%proj%/.templates/startup/.startup.cmd",
    			new File(iProject.getLocation().toFile(), ".templates/startup/.startup.cmd"));
	}
	
	/**
	 * Method validates remote access password.
	 * @param isPwdChanged : flag to monitor whether password is changed or not
	 * @param txtPassword : Object of password text box
	 * @param waProjManager : WindowsAzureProjectManager object
	 * @param isRAPropPage : flag to monitor who has called this method
	 * 						 Encryption link or normal property page call.
	 * @param txtConfirmPassword : Object of confirm password text box
	 */
	public static void checkRdpPwd(
			boolean isPwdChanged,
			Text txtPassword,
			WindowsAzureProjectManager waProjManager,
			boolean isRAPropPage,
			Text txtConfirmPassword) {
		Pattern pattern = Pattern.compile("(?=^.{6,}$)(?=.*\\d)(?=.*[A-Z])(?!.*\\s)(?=.*[a-z]).*$|"
				+ "(?=^.{6,}$)(?=.*\\d)(?!.*\\s)(?=.*[a-z])(?=.*\\p{Punct}).*$|"
				+ "(?=^.{6,}$)(?=.*\\d)(?!.*\\s)(?=.*[A-Z])(?=.*\\p{Punct}).*$|"
				+ "(?=^.{6,}$)(?=.*[A-Z])(?=.*[a-z])(?!.*\\s)(?=.*\\p{Punct}).*$");
		Matcher match = pattern.matcher(txtPassword.getText());
		try {
			/*
			 * checking if user has changed the password
			 * and that field is not blank
			 * then check for strong password else set the old password.
			 */
			if (isPwdChanged) {
				if (!txtPassword.getText().isEmpty()
						&& !match.find()) {
					PluginUtil.displayErrorDialog(new Shell(),
							Messages.remAccErPwdNtStrg,
							Messages.remAccPwdNotStrg);
					txtConfirmPassword.setText("");
					txtPassword.setFocus();
				}
			} else {
				String pwd = waProjManager.
						getRemoteAccessEncryptedPassword();
				/*
				 * Remote access property page
				 * accessed via context menu
				 */
				if (isRAPropPage) {
					txtPassword.setText(pwd);
				} else {
					/*
					 * Remote access property page accessed
					 * via encryption link
					 */
					if (!pwd.equals(Messages.remAccDummyPwd)) {
						txtPassword.setText(pwd);
					}
				}
			}
		} catch (WindowsAzureInvalidProjectOperationException e1) {
			PluginUtil.displayErrorDialogAndLog(new Shell(),
					Messages.remAccErrTitle,
					Messages.remAccErPwd, e1);
		}
	}

	/**
	 * Method checks whether URL is blob URL or not.
	 * It should satisfy pattern
	 * http[s]://<storage-account-name>
	 * 	-->(only lower case letters and numbers allowed)
	 * . -->(exactly one dot is required)
	 * <blob-service-endpoint>
	 * 	-->(only lower case letters, numbers and period allowed)
	 * / -->(exactly one forward slash is required)
	 * <container-name>
	 * --> (only lower case letters, numbers and '-' allowed)
	 * 		must start with letter or number,
	 * 		no consecutive dashes allowed
	 * 		must be of 3 through 63 characters long
	 * / -->(exactly one forward slash is required)
	 * <blob-name>
	 * 	-->(may contain upper lower case characters,
	 * 		numbers and punctuation marks)
	 * 		must be of 1 through 1024 characters long
	 *
	 * @param text
	 * @return
	 */
	public static Boolean isBlobStorageUrl(String text) {
		Pattern blob = Pattern.compile(
				"^https?://[a-z0-9]+\\.{1}[a-z0-9.]+/{1}([a-z]|\\d){1}([a-z]|-|\\d){1,61}([a-z]|\\d){1}/{1}[\\w\\p{Punct}]+$");
		Matcher m = blob.matcher(text);
		return m.matches();
	}
	
	/**
	 * API to find the hostname. This API first checks OS type. If Windows then tries to get 
	 * hostname from computername environment variable else uses environment variable hostname.
	 * 
	 * In case if hostname is not found from environment variable then uses java networking apis
	 * 
	 */
	public static String getHostName() {		
		String hostOS   = System.getProperty("os.name");
		String hostName = null;
		
		// Check host Operating System and get value of hostname.   
		if (hostOS != null && hostOS.indexOf("Win") >= 0) {
			hostName = System.getenv("COMPUTERNAME");
		} else { // non-windows platforms
			hostName = System.getenv("HOSTNAME");
		}
		
		// If hostname is still null , use java network apis
		try {
			if (hostName == null || hostName.isEmpty()) {
				hostName = InetAddress.getLocalHost().getHostName();
			}
		} catch (Exception ex) { // catches UnknownHostException
			// just ignore this exception
		}
		
		if (hostName == null || hostName.isEmpty()) { // most probabily this case won't happen 
			hostName = "localhost";
		}
		
		return hostName;
	} 

	/**
	 * Returns ant build is successful or not
	 * on the basis of existence of files under deploy folder.
	 * @param waProjMngr
	 * @param selProj
	 * @return
	 */
	public static boolean isBuildSuccessful(
			WindowsAzureProjectManager waProjMngr,
			IProject selProj) {
		Boolean isSuccessful = false;
		try {
			String dplyFolderPath =
					getDeployFolderPath(waProjMngr, selProj);
			String bldFlFilePath = String.format("%s%s%s",
					dplyFolderPath, "\\", Messages.bldErFileName);
			File deployFile = new File(dplyFolderPath);
			File buildFailFile = new File(bldFlFilePath);

			if (deployFile.exists() && deployFile.isDirectory()
					&& deployFile.listFiles().length > 0
					&& !buildFailFile.exists()) {
				isSuccessful =  true;
			}
		} catch (Exception ex) {
			Activator.getDefault().log(ex.getMessage());
		}
		return isSuccessful;
	}

	/**
	 * Returns path of deploy folder.
	 * @param waProjMngr
	 * @param selProj
	 * @return
	 */
	public static String getDeployFolderPath(
			WindowsAzureProjectManager waProjMngr,
			IProject selProj) {
		String dplyFolderPath = "";
		try {
			String dplyFldrName = waProjMngr.getPackageDir();
			String projPath = selProj.getLocation().toOSString();

			if (dplyFldrName.startsWith(".")) {
				dplyFldrName = dplyFldrName.substring(1);
			}
			dplyFolderPath = String.format("%s%s", projPath, dplyFldrName);
		} catch (Exception e) {
			Activator.getDefault().log(e.getMessage());
		}
		return dplyFolderPath;
	}

	public static WindowsAzureRole prepareRoleToAdd(
			WindowsAzureProjectManager waProjManager) {
		WindowsAzureRole windowsAzureRole = null;
		try {
			StringBuffer strBfr = new StringBuffer(
					com.persistent.winazureroles.Messages.dlgWorkerRole1);
			int roleNo = 2;
			while (!waProjManager
					.isAvailableRoleName(strBfr.toString())) {
				strBfr.delete(10, strBfr.length());
				strBfr.append(roleNo++);
			}
			String strKitLoc = String.format("%s%s%s%s%s%s",
					Platform.getInstallLocation().
					getURL().getPath().toString(),
					File.separator, Messages.pluginFolder,
					File.separator, Messages.pluginId,
					com.persistent.winazureroles.Messages.pWizStarterKit);
			windowsAzureRole = waProjManager
					.addRole(strBfr.toString(), strKitLoc);
			windowsAzureRole.setInstances(com.persistent.winazureroles.Messages.rolsNoOfInst);
			windowsAzureRole.setVMSize(com.persistent.winazureroles.Messages.rolsVMSmall);
			Activator.getDefault().setWaProjMgr(waProjManager);
			Activator.getDefault().setWaRole(windowsAzureRole);
			Activator.getDefault().setEdit(false);
		} catch (Exception e) {
			Activator.getDefault().log(e.getMessage());
		}
		return windowsAzureRole;
	}
}