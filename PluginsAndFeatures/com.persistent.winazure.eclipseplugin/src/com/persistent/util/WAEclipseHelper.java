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
import java.io.IOException;
import java.net.URL;
import java.util.List;
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

import com.gigaspaces.azure.propertypage.SubscriptionPropertyPage;
import com.gigaspaces.azure.util.PreferenceUtilForProjectUpgrade;
import com.gigaspaces.azure.util.PreferenceUtilPubWizard;
import com.gigaspaces.azure.util.WizardCache;
import com.interopbridges.tools.windowsazure.WindowsAzureConstants;
import com.interopbridges.tools.windowsazure.WindowsAzureEndpoint;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.microsoftopentechnologies.azurecommons.util.WAEclipseHelperMethods;
import com.microsoftopentechnologies.azurecommons.wacommonutil.FileUtil;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
import com.persistent.ui.propertypage.WARemoteAccessPropertyPage;
import com.persistent.ui.propertypage.WARolesPropertyPage;
import com.persistent.ui.propertypage.WAWinAzurePropertyPage;
import com.persistent.winazureroles.WARCaching;
import com.persistent.winazureroles.WARCertificates;
import com.persistent.winazureroles.WARComponents;
import com.persistent.winazureroles.WARDebugging;
import com.persistent.winazureroles.WAREndpoints;
import com.persistent.winazureroles.WAREnvVars;
import com.persistent.winazureroles.WARGeneral;
import com.persistent.winazureroles.WARLoadBalance;
import com.persistent.winazureroles.WARLocalStorage;
import com.persistent.winazureroles.WASSLOffloading;
import com.persistent.winazureroles.WAServerConfiguration;

import waeclipseplugin.Activator;

/**
 * This class contains common utility methods.
 * 
 */
public class WAEclipseHelper {
	/**
	 * @return Template(componentssets.xml)
	 */
	public static String getTemplateFile(String fileName) {
		String file = String.format("%s%s%s%s%s", PluginUtil.pluginFolder,
				File.separator, Messages.pluginId, File.separator, fileName);
		return file;
	}

	/**
	 * This method will refresh the workspace.If any changes are made in any
	 * configuration files through UI in that case it will refresh the workspace
	 * so that user can see the correct/modified files.
	 * 
	 * @param errorTitle
	 * @param errorMessage
	 */
	public static void refreshWorkspace(String errorTitle, String errorMessage) {
		try {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceRoot root = workspace.getRoot();
			root.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			PluginUtil.displayErrorDialogAndLog(new Shell(), errorTitle,
					errorMessage, e);
		}
	}

	/**
	 * This method returns currently selected project in workspace.
	 * 
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
			IStructuredSelection structuredSel = (IStructuredSelection) selection;
			element = structuredSel.getFirstElement();
		}
		if (element instanceof IResource) {
			resource = (IResource) element;
			selProject = resource.getProject();
		} else {
			IWorkbenchPage page = window.getActivePage();
			IFile file = (IFile) page.getActiveEditor().getEditorInput()
					.getAdapter(IFile.class);
			selProject = file.getProject();
		}
		return selProject;
	}

	/**
	 * This returns the resource has a file.
	 * 
	 * @param fileEntry
	 *            : File pointing to resource. null if file doesn't exists
	 * @return
	 */
	public static File getResourceAsFile(String fileEntry) {
		File file = null;
		try {
			URL url = Activator.getDefault().getBundle().getEntry(fileEntry);
			URL fileURL = FileLocator.toFileURL(url);
			URL resolve = FileLocator.resolve(fileURL);
			file = new File(resolve.getFile());
		} catch (Exception e) {
			Activator.getDefault().log(e.getMessage(), e);
		}
		return file;
	}

	/**
	 * Method creates tree structure of azure property pages. and opens property
	 * dialog with desired property page selected & active.
	 * 
	 * @param windowsAzureRole
	 *            : worker role
	 * @param pageToDisplay
	 *            : property page Id which should be active after opening dialog
	 * @param tabToSelect : In case pageToDisplay is Server Configuration page then
	 * 						provide tab which should be selected.
	 * @return integer
	 */
	public static int openRolePropertyDialog(WindowsAzureRole windowsAzureRole,
			String pageToDisplay, String tabToSelect) {
		int retVal = Window.CANCEL; // value corresponding to cancel
		try {
			// Node creation
			PreferenceNode nodeGeneral = new PreferenceNode(
					Messages.cmhIdGeneral, Messages.cmhLblGeneral, null,
					WARGeneral.class.toString());
			nodeGeneral.setPage(new WARGeneral());
			nodeGeneral.getPage().setTitle(Messages.cmhLblGeneral);

			PreferenceNode nodeCache = new PreferenceNode(Messages.cmhIdCach,
					Messages.cmhLblCach, null, WARCaching.class.toString());
			nodeCache.setPage(new WARCaching());
			nodeCache.getPage().setTitle(Messages.cmhLblCach);

			PreferenceNode nodeCert = new PreferenceNode(Messages.cmhIdCert,
					Messages.cmhLblCert, null, WARCertificates.class.toString());
			nodeCert.setPage(new WARCertificates());
			nodeCert.getPage().setTitle(Messages.cmhLblCert);

			PreferenceNode nodeCmpnts = new PreferenceNode(
					Messages.cmhIdCmpnts, Messages.cmhLblCmpnts, null,
					WARComponents.class.toString());
			nodeCmpnts.setPage(new WARComponents());
			nodeCmpnts.getPage().setTitle(Messages.cmhLblCmpnts);

			PreferenceNode nodeDebugging = new PreferenceNode(
					Messages.cmhIdDbg, Messages.cmhLblDbg, null,
					WARDebugging.class.toString());
			nodeDebugging.setPage(new WARDebugging());
			nodeDebugging.getPage().setTitle(Messages.cmhLblDbg);

			PreferenceNode nodeEndPts = new PreferenceNode(
					Messages.cmhIdEndPts, Messages.cmhLblEndPts, null,
					WAREndpoints.class.toString());
			nodeEndPts.setPage(new WAREndpoints());
			nodeEndPts.getPage().setTitle(Messages.cmhLblEndPts);

			PreferenceNode nodeEnvVars = new PreferenceNode(
					Messages.cmhIdEnvVars, Messages.cmhLblEnvVars, null,
					WAREnvVars.class.toString());
			nodeEnvVars.setPage(new WAREnvVars());
			nodeEnvVars.getPage().setTitle(Messages.cmhLblEnvVars);

			PreferenceNode nodeLdBlnc = new PreferenceNode(
					Messages.cmhIdLdBlnc, Messages.cmhLblLdBlnc, null,
					WARLoadBalance.class.toString());
			nodeLdBlnc.setPage(new WARLoadBalance());
			nodeLdBlnc.getPage().setTitle(Messages.cmhLblLdBlnc);

			PreferenceNode nodeLclStg = new PreferenceNode(
					Messages.cmhIdLclStg, Messages.cmhLblLclStg, null,
					WARLocalStorage.class.toString());
			nodeLclStg.setPage(new WARLocalStorage());
			nodeLclStg.getPage().setTitle(Messages.cmhLblLclStg);

			PreferenceNode nodeSrvCnfg = new PreferenceNode(
					Messages.cmhIdSrvCnfg, Messages.cmhLblSrvCnfg, null,
					WAServerConfiguration.class.toString());
			nodeSrvCnfg.setPage(new WAServerConfiguration(tabToSelect));
			nodeSrvCnfg.getPage().setTitle(Messages.cmhLblSrvCnfg);

			PreferenceNode nodeSslOff = new PreferenceNode(Messages.cmhIdSsl,
					Messages.cmhLblSsl, null, WASSLOffloading.class.toString());
			nodeSslOff.setPage(new WASSLOffloading());
			nodeSslOff.getPage().setTitle(Messages.cmhLblSsl);

			/*
			 * Tree structure creation. Don't change order while adding nodes.
			 * Its the default alphabetical order given by eclipse.
			 */
			nodeGeneral.add(nodeCache);
			nodeGeneral.add(nodeCert);
			nodeGeneral.add(nodeCmpnts);
			nodeGeneral.add(nodeDebugging);
			nodeGeneral.add(nodeEndPts);
			nodeGeneral.add(nodeEnvVars);
			nodeGeneral.add(nodeLdBlnc);
			nodeGeneral.add(nodeLclStg);
			nodeGeneral.add(nodeSrvCnfg);
			nodeGeneral.add(nodeSslOff);

			PreferenceManager mgr = new PreferenceManager();
			mgr.addToRoot(nodeGeneral);
			// Dialog creation
			PreferenceDialog dialog = new PreferenceDialog(PlatformUI
					.getWorkbench().getDisplay().getActiveShell(), mgr);
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
					Messages.rolsDlgErr, Messages.rolsDlgErrMsg, ex);
		}
		return retVal;
	}

	/**
	 * Method creates tree structure of azure deployment project property pages.
	 * and opens property dialog with desired property page selected & active.
	 * 
	 * @param pageToDisplay
	 *            : property page Id which should be active after opening dialog
	 * @return integer
	 */
	public static int openWAProjectPropertyDialog(String pageToDisplay) {
		int retVal = Window.CANCEL; // value corresponding to cancel
		// Node creation
		try {
			PreferenceNode nodeWindowsAzure = new PreferenceNode(
					Messages.cmhIdWinAz, Messages.cmhLblWinAz, null,
					WAWinAzurePropertyPage.class.toString());
			nodeWindowsAzure.setPage(new WAWinAzurePropertyPage());
			nodeWindowsAzure.getPage().setTitle(Messages.cmhLblWinAz);

			PreferenceNode nodeRemoteAcess = new PreferenceNode(
					Messages.cmhIdRmtAces, Messages.cmhLblRmtAces, null,
					WARemoteAccessPropertyPage.class.toString());
			nodeRemoteAcess.setPage(new WARemoteAccessPropertyPage());
			nodeRemoteAcess.getPage().setTitle(Messages.cmhLblRmtAces);

			PreferenceNode nodeRoles = new PreferenceNode(Messages.cmhIdRoles,
					Messages.cmhLblRoles, null,
					WARolesPropertyPage.class.toString());
			nodeRoles.setPage(new WARolesPropertyPage());
			nodeRoles.getPage().setTitle(Messages.cmhLblRoles);

			PreferenceNode nodeSubscriptions = new PreferenceNode(
					Messages.cmhIdCrdntls, Messages.cmhLblSubscrpt, null,
					SubscriptionPropertyPage.class.toString());
			nodeSubscriptions.setPage(new SubscriptionPropertyPage());
			nodeSubscriptions.getPage().setTitle(Messages.cmhLblSubscrpt);

			/*
			 * Tree structure creation. Don't change order while adding nodes.
			 * Its the default alphabetical order given by eclipse.
			 */
			nodeWindowsAzure.add(nodeRemoteAcess);
			nodeWindowsAzure.add(nodeRoles);
			nodeWindowsAzure.add(nodeSubscriptions);

			PreferenceManager mgr = new PreferenceManager();
			mgr.addToRoot(nodeWindowsAzure);
			// Dialog creation
			PreferenceDialog dialog = new PreferenceDialog(PlatformUI
					.getWorkbench().getDisplay().getActiveShell(), mgr);
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
					Messages.rolsDlgErr, Messages.projDlgErrMsg, e);
		}
		return retVal;
	}

	/**
	 * If project name present in package.xml and
	 * WindowsAzureProjectBuilder.launch does not match with the actual one then
	 * correct it accordingly.
	 * 
	 * @param project
	 * @param mngr
	 *            : WindowsAzureProjectManager
	 */
	public static void correctProjectName(IProject project,
			WindowsAzureProjectManager mngr) {
		String strPath = project.getLocation().toOSString();
		String launchFile = strPath + File.separator
				+ Messages.resCLExtToolBldr + File.separator
				+ Messages.resCLLaunchFile;
		try {
			ParseXML.setProjectNameinLaunch(launchFile, mngr.getProjectName(),
					project.getName());
			mngr.setProjectName(project.getName());
			mngr.save();
		} catch (Exception e) {
			Activator.getDefault().log(e.getMessage());
		}
	}

	public static void handleProjectUpgrade(IProject iProject,
			WindowsAzureProjectManager projMngr) throws IOException,
			WindowsAzureInvalidProjectOperationException, CoreException {

		URL url = Activator.getDefault().getBundle()
				.getEntry(Messages.starterKitEntry);
		URL resolve = FileLocator.resolve(FileLocator.toFileURL(url));
		File zipFile = new File(resolve.getFile());

		boolean result = FileUtil.copyFileFromZip(zipFile, "%proj%/"
				+ Messages.skJarName, new File(iProject.getLocation().toFile(),
				Messages.skJarName));

		if (result) {
			upgradeWAPFiles(iProject, zipFile, projMngr);
		}

		// If not able to copy just log the error and close the project.
		if (!result) {
			Activator.getDefault().log(
					iProject.getName() + ".cspack.jar file is not updated",
					null);
			iProject.close(null);
		} else {
			// this method is mainly for upgrading the publish tasks in package.xml and may need to remove after release 2.7.0
			projMngr.upgradePackageDoc();
			
			// update version in package.xml
			projMngr.setVersion(WindowsAzureConstants.VERSION);
			/*
			 * Transfer the publish properties from prefs file to package.xml
			 * Note : below changes will only work when, we have respective key in prefs file
			 * i.e. same previous workspace is used even after plugin upgrade.
			 */
			String key = String.format("%s%s%s", Activator.PLUGIN_ID,
					com.persistent.util.Messages.proj, iProject.getName());
			if (PreferenceUtilPubWizard.getProjKeyList().contains(key)) {
				WizardCache cacheObj = PreferenceUtilPubWizard.load(key);
				if (cacheObj != null) {
					String subscriptionName = cacheObj.getSubName();
					if (!subscriptionName.isEmpty()) {
						String subId = PreferenceUtilForProjectUpgrade.extractSubIdFromOldPublishData(subscriptionName);
						if (!subId.isEmpty()) {
							if (!cacheObj.getStorageName().isEmpty()) {
								projMngr.setPublishStorageAccountName(cacheObj.getStorageName());
							}
							if (!cacheObj.getServiceName().isEmpty()) {
								projMngr.setPublishCloudServiceName(cacheObj.getServiceName());
							}
							projMngr.setPublishSubscriptionId(subId);
						}
					}
				}
			}
			projMngr.save();
		}
	}
	
	private static void upgradeWAPFiles(IProject iProject, File starterKitZip,
			WindowsAzureProjectManager projMngr) throws IOException,
			WindowsAzureInvalidProjectOperationException {
		List<WindowsAzureRole> rolesList = projMngr.getRoles();
		File projectLocation = iProject.getLocation().toFile();

		for (WindowsAzureRole role : rolesList) {
			// Copy session affinity files if SA is enabled
			WindowsAzureEndpoint saEndPoint = null;
			if ((saEndPoint = role.getSessionAffinityInputEndpoint()) != null) {
				// Remove old files and disable first
				projMngr.removeOldSAResources(role.getName());
				role.setSessionAffinityInputEndpoint(null);

				// Get latest definition of endpoint and enable again
				saEndPoint = role.getEndpoint(saEndPoint.getName());
				role.setSessionAffinityInputEndpoint(saEndPoint);
			}

			// Copy or rewrite .wash script
			FileUtil.copyFileFromZip(starterKitZip,
					"%proj%/WorkerRole1/approot/util/" + Messages.washFileName,
					new File(projectLocation, role.getName()
							+ "/approot/util/" + Messages.washFileName));
		}

		// upgradePackageFileDoc
		try {
			projMngr.upgradePackageFileDoc(PluginUtil.getAzureLibLocation());
		} catch (Exception e){
			// just log and silently ignore
			e.printStackTrace();
		}

		// Copy or rewrite .templates/startup/.startup.cmd
		FileUtil.copyFileFromZip(starterKitZip,
				"%proj%/.templates/startup/.startup.cmd", new File(projectLocation,
						".templates/startup/.startup.cmd"));

		// Copy or rewrite .templates/startup/.startup.cmd
		FileUtil.copyFileFromZip(starterKitZip,
				"%proj%/.templates/emulatorTools/ResetEmulator.cmd", new File(
						projectLocation, ".templates/emulatorTools/ResetEmulator.cmd"));

		FileUtil.copyFileFromZip(starterKitZip,
				"%proj%/.templates/emulatorTools/RunInEmulator.cmd", new File(projectLocation,
						".templates/emulatorTools/RunInEmulator.cmd"));
		
		// Copy cloud tools - start - may need to remove after 2.7.0 release
		FileUtil.copyFileFromZip(starterKitZip,	"%proj%/.templates/cloudTools/buildAndPublish.cmd",
				new File(projectLocation, ".templates/cloudTools/buildAndPublish.cmd"));
		
		FileUtil.copyFileFromZip(starterKitZip,	"%proj%/.templates/cloudTools/buildAndPublish.sh",
				new File(projectLocation, ".templates/cloudTools/buildAndPublish.sh"));
		
		FileUtil.copyFileFromZip(starterKitZip,	"%proj%/.templates/cloudTools/unpublish.cmd",
				new File(projectLocation, ".templates/cloudTools/unpublish.cmd"));
		
		FileUtil.copyFileFromZip(starterKitZip,	"%proj%/.templates/cloudTools/unpublish.sh",
				new File(projectLocation, ".templates/cloudTools/unpublish.sh"));

		FileUtil.copyFileFromZip(starterKitZip,	"%proj%/.templates/cloudTools/publish.cmd",
				new File(projectLocation, ".templates/cloudTools/publish.cmd"));

		FileUtil.copyFileFromZip(starterKitZip,	"%proj%/.templates/cloudTools/publish.sh",
				new File(projectLocation, ".templates/cloudTools/publish.sh"));
		// Copy cloud tools - end.
	}

	/**
	 * Method validates remote access password.
	 * 
	 * @param isPwdChanged
	 *            : flag to monitor whether password is changed or not
	 * @param txtPassword
	 *            : Object of password text box
	 * @param waProjManager
	 *            : WindowsAzureProjectManager object
	 * @param isRAPropPage
	 *            : flag to monitor who has called this method Encryption link
	 *            or normal property page call.
	 * @param txtConfirmPassword
	 *            : Object of confirm password text box
	 */
	public static void checkRdpPwd(boolean isPwdChanged, Text txtPassword,
			WindowsAzureProjectManager waProjManager, boolean isRAPropPage,
			Text txtConfirmPassword) {
		Pattern pattern = Pattern
				.compile("(?=^.{6,}$)(?=.*\\d)(?=.*[A-Z])(?!.*\\s)(?=.*[a-z]).*$|"
						+ "(?=^.{6,}$)(?=.*\\d)(?!.*\\s)(?=.*[a-z])(?=.*\\p{Punct}).*$|"
						+ "(?=^.{6,}$)(?=.*\\d)(?!.*\\s)(?=.*[A-Z])(?=.*\\p{Punct}).*$|"
						+ "(?=^.{6,}$)(?=.*[A-Z])(?=.*[a-z])(?!.*\\s)(?=.*\\p{Punct}).*$");
		Matcher match = pattern.matcher(txtPassword.getText());
		try {
			/*
			 * checking if user has changed the password and that field is not
			 * blank then check for strong password else set the old password.
			 */
			if (isPwdChanged) {
				if (!txtPassword.getText().isEmpty() && !match.find()) {
					PluginUtil.displayErrorDialog(new Shell(),
							Messages.remAccErPwdNtStrg,
							Messages.remAccPwdNotStrg);
					txtConfirmPassword.setText("");
					txtPassword.setFocus();
				}
			} else {
				String pwd = waProjManager.getRemoteAccessEncryptedPassword();
				/*
				 * Remote access property page accessed via context menu
				 */
				if (isRAPropPage) {
					txtPassword.setText(pwd);
				} else {
					/*
					 * Remote access property page accessed via encryption link
					 */
					if (!pwd.equals(Messages.remAccDummyPwd)) {
						txtPassword.setText(pwd);
					}
				}
			}
		} catch (WindowsAzureInvalidProjectOperationException e1) {
			PluginUtil.displayErrorDialogAndLog(new Shell(),
					Messages.remAccErrTitle, Messages.remAccErPwd, e1);
		}
	}

	/**
	 * Returns ant build is successful or not on the basis of existence of files
	 * under deploy folder.
	 * 
	 * @param waProjMngr
	 * @param selProj
	 * @return
	 */
	public static boolean isBuildSuccessful(
			WindowsAzureProjectManager waProjMngr, IProject selProj) {
		Boolean isSuccessful = false;
		try {
			String dplyFolderPath = getDeployFolderPath(waProjMngr, selProj);
			String bldFlFilePath = String.format("%s%s%s", dplyFolderPath,
					File.separator, Messages.bldErFileName);
			File deployFile = new File(dplyFolderPath);
			File buildFailFile = new File(bldFlFilePath);

			if (deployFile.exists() && deployFile.isDirectory()
					&& deployFile.listFiles().length > 0
					&& !buildFailFile.exists()) {
				isSuccessful = true;
			}
		} catch (Exception ex) {
			Activator.getDefault().log(ex.getMessage());
		}
		return isSuccessful;
	}

	/**
	 * Returns path of deploy folder.
	 * 
	 * @param waProjMngr
	 * @param selProj
	 * @return
	 */
	public static String getDeployFolderPath(
			WindowsAzureProjectManager waProjMngr, IProject selProj) {
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
			while (!waProjManager.isAvailableRoleName(strBfr.toString())) {
				strBfr.delete(10, strBfr.length());
				strBfr.append(roleNo++);
			}
			String strKitLoc = String.format("%s%s%s%s",
					PluginUtil.pluginFolder, File.separator,
					Messages.pluginId, com.persistent.winazureroles.Messages.pWizStarterKit);
			windowsAzureRole = waProjManager.addRole(strBfr.toString(),
					strKitLoc);
			windowsAzureRole
					.setInstances(com.persistent.winazureroles.Messages.rolsNoOfInst);
			windowsAzureRole
					.setVMSize(com.persistent.winazureroles.Messages.rolsVMSmall);
			Activator.getDefault().setWaProjMgr(waProjManager);
			Activator.getDefault().setWaRole(windowsAzureRole);
			Activator.getDefault().setEdit(false);
		} catch (Exception e) {
			Activator.getDefault().log(e.getMessage());
		}
		return windowsAzureRole;
	}

	public static String detectServer(File path) {
		return WAEclipseHelperMethods.detectServer(path, getTemplateFile(Messages.cmpntFileName));
	}
}