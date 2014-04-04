/**
* Copyright 2014 Microsoft Open Technologies, Inc.
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
package com.persistent.ui.projwizard;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WorkingSetGroup;

import waeclipseplugin.Activator;

import com.interopbridges.tools.windowsazure.WARoleComponentCloudUploadMode;
import com.interopbridges.tools.windowsazure.WindowsAzureEndpoint;
import com.interopbridges.tools.windowsazure.WindowsAzureEndpointType;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.interopbridges.tools.windowsazure.WindowsAzureRoleComponent;
import com.interopbridges.tools.windowsazure.WindowsAzureRoleComponentImportMethod;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
import com.persistent.builder.WADependencyBuilder;
import com.persistent.ui.propertypage.WAProjectNature;
import com.persistent.util.AppCmpntParam;
import com.persistent.util.JdkSrvConfig;
import com.persistent.util.ParseXML;
import com.persistent.util.WAEclipseHelper;

/**
 * This class creates a wizard for new Azure Cloud Project.
 */
public class WAProjectWizard extends Wizard
implements INewWizard, IPageChangedListener {

	private String errorTitle;
	private String errorMessage;
	private WAProjectWizardPage waProjWizPage;
	private WATabPage tabPg;
	private WAKeyFeaturesPage waKeyPage;
	private WindowsAzureProjectManager waProjMgr;
	private WindowsAzureRole waRole;
	private final int CACH_DFLTVAL = 30;
	private final String DEBUG_PORT = "8090";
	private final String HTTP_PRV_PORT = "8080";
	private final String HTTP_PORT = "80";
	private final String auto = "auto";
	private final String dashAuto = "-auto";
    private static final String LAUNCH_FILE_PATH = File.separator
    		+ Messages.pWizToolBuilder
    		+ File.separator
            + Messages.pWizLaunchFile;

    /**
     * Default constructor.
     */
    public WAProjectWizard() {
        setWindowTitle(Messages.pWizWindowTitle);
        		
        String zipFile = "";
        try {
        	zipFile  = String.format("%s%s%s%s%s%s%s",
        			Platform.getInstallLocation().getURL()
        			.getPath().toString(),
        			File.separator, Messages.pluginFolder,
        			File.separator, Messages.pluginId,
        			File.separator,
        			Messages.starterKitFileName);

        	//Extract the WAStarterKitForJava.zip to temp dir
        	waProjMgr = WindowsAzureProjectManager.create(zipFile);
        	//	By deafult - disabling remote access
        	//  when creating new project
        	waProjMgr.setRemoteAccessAllRoles(false);
        	waProjMgr.setClassPathInPackage("azure.lib.dir", PluginUtil.getAzureLibLocation());
        	waRole = waProjMgr.getRoles().get(0);
        	// remove http endpoint
        	waRole.getEndpoint(Messages.httpEp).delete();
        } catch (IOException e) {
        	PluginUtil.displayErrorDialogAndLog(this.getShell(),
        			Messages.pWizErrTitle, Messages.pWizErrMsg, e);
        } catch (Exception e) {
        	errorTitle = Messages.adRolErrTitle;
        	errorMessage = Messages.pWizErrMsgBox1
        			+ Messages.pWizErrMsgBox2;
        	PluginUtil.displayErrorDialogAndLog(this.getShell(),
        			errorTitle,
        			errorMessage, e);
        } 
    }

    /**
     * Init method.
     */
    @Override
    public void init(IWorkbench arg0,
    		IStructuredSelection arg1) {

    }

    @Override
    public void setContainer(IWizardContainer wizardContainer) {
    	super.setContainer(wizardContainer);
    	if (wizardContainer != null
    			&& wizardContainer instanceof WizardDialog) {
			((WizardDialog) getContainer()).
			addPageChangedListener((IPageChangedListener) this);
		}
    }
    

    /**
     * This method gets called when wizard's finish button is clicked.
     *
     * @return True, if project gets created successfully; else false.
     */
    @Override
    public boolean performFinish() {
        final String projName = waProjWizPage.getTextProjName();
        final String projLocation = waProjWizPage.getTextLocation();
        final boolean isDefault = waProjWizPage.isDefaultLocation();
        final WorkingSetGroup workingSetGroup =
        		waProjWizPage.getWorkingSetGroup();
        final IWorkingSet[] selWorkingSets =
        		workingSetGroup.getSelectedWorkingSets();
        final IWorkingSetManager workingSetManager =
        		PlatformUI.getWorkbench().getWorkingSetManager();
        final Map<String, String> depParams =
        		getDeployPageValues();
        final Map<String, Boolean> keyFtr =
        		getKeyFtrPageValues();
        final IProject proj = getSelectProject();
        boolean retVal = true;
        IRunnableWithProgress runnable = new IRunnableWithProgress() {
        	public void run(IProgressMonitor monitor)
        			throws InvocationTargetException {
        		try {
        			doFinish(projName, projLocation, isDefault,
        					selWorkingSets,
        					workingSetManager,
        					depParams, keyFtr, proj);
        		} finally {
        			monitor.done();
        		}
            }
        };
        try {
        	/*
        	 * Check if third party JDK is selected
        	 * then license is accepted or not.
        	 */
        	boolean tempAccepted = true;
        	if (WATabPage.isThirdPartyJdkChecked()
        			&& !WATabPage.isAccepted()) {
        		tempAccepted = JdkSrvConfig.createAccLicenseAggDlg();
        	}
        	if (tempAccepted) {
        		getContainer().run(true, false, runnable);
        	} else {
        		return false;
        	}
        } catch (InterruptedException e) {
        	PluginUtil.displayErrorDialog(this.getShell(),
        			Messages.pWizErrTitle,
        			Messages.pWizErrMsg);
        	retVal = false;
        } catch (InvocationTargetException e) {
        	PluginUtil.displayErrorDialogAndLog(this.getShell(),
        			Messages.pWizErrTitle,
        			Messages.pWizErrMsg, e);
        	retVal = false;
        }
        //re-initializing context menu to default option : false
        Activator.getDefault().setContextMenu(false);
        return retVal;
    }

    /**
     * Move the project structure to the location provided by user.
     * Also configure JDK, server, server application
     * and key features like session affinity, caching, debugging
     * if user wants to do so.
     * @param projName : Name of the project
     * @param projLocation : Location of the project
     * @param isDefault : whether location of project is default
     * @param selWorkingSets
     * @param workingSetManager
     * @param depMap : stores configurations done on WATagPage
     * @param ftrMap : stores configurations done on WAKeyFeaturesPage
     * @param selProj
     */
    private void doFinish(String projName, String projLocation,
    		boolean isDefault, IWorkingSet[] selWorkingSets,
    		IWorkingSetManager workingSetManager,
    		Map<String, String> depMap,
    		Map<String, Boolean> ftrMap,
    		IProject selProj) {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        IProject project = null;
        try {
            WindowsAzureRole role = waProjMgr.getRoles().get(0);
            //logic for handling deploy page components and their values.
            if (!depMap.isEmpty()) {
            	File templateFile = new File(depMap.get("tempFile"));
            	// JDK
            	if (depMap.get("jdkChecked").equalsIgnoreCase("true")
            			&& !depMap.get("jdkLoc").isEmpty()) {
            		// Third party JDK selected
            		if (depMap.get("jdkThrdPartyChecked").equalsIgnoreCase("true")) {
            			String jdkName = depMap.get("jdkName");
            			role.setJDKSourcePath(depMap.get("jdkLoc"),
            					templateFile,
                				jdkName);
            			role.setJDKCloudName(jdkName);
            		} else {
            			role.setJDKSourcePath(depMap.get("jdkLoc"),
            					templateFile, "");
            		}

            		// JDK download group
            		// By default auto upload will be selected.
            		String jdkTabUrl = depMap.get("jdkUrl");
            		if (depMap.get("jdkAutoDwnldChecked").
            				equalsIgnoreCase("true")
            				|| depMap.get("jdkThrdPartyChecked").equalsIgnoreCase("true")) {
            			if (jdkTabUrl.
            					equalsIgnoreCase(JdkSrvConfig.AUTO_TXT)) {
            				jdkTabUrl = auto;
            			}
            			role.setJDKCloudUploadMode(WARoleComponentCloudUploadMode.auto);
            		}
            		role.setJDKCloudURL(jdkTabUrl);
            		role.setJDKCloudKey(depMap.get("jdkKey"));
            		/*
            		 * By default package type is local,
            		 * hence store JAVA_HOME for cloud.
            		 */
            		role.setJDKCloudHome(depMap.get("javaHome"));
            	}

                // Server
                if (depMap.get("serChecked").equalsIgnoreCase("true")
                		&& !depMap.get("serLoc").isEmpty()
                		&& !depMap.get("servername").isEmpty()) {
                	String srvName = depMap.get("servername");
                	String srvPriPort = WindowsAzureProjectManager.
                			getHttpPort(srvName, templateFile);
                	if (role.isValidEndpoint(Messages.httpEp,
                			WindowsAzureEndpointType.Input,
                			srvPriPort, HTTP_PORT)) {
                		role.addEndpoint(Messages.httpEp,
                				WindowsAzureEndpointType.Input,
                				srvPriPort, HTTP_PORT);
                	}
                	role.setServer(srvName,
                			depMap.get("serLoc"),
                			templateFile);

                	// Server download group
                	// Add only if Server component added
                	if (depMap.get("srvDwnldChecked").equalsIgnoreCase("true")
                		|| depMap.get("srvAutoDwnldChecked").equalsIgnoreCase("true")) {
                		String srvTabUrl = depMap.get("srvUrl");
                		if (depMap.get("srvAutoDwnldChecked").
                				equalsIgnoreCase("true")
                				&& srvTabUrl.
                				equalsIgnoreCase(JdkSrvConfig.AUTO_TXT)) {
                			srvTabUrl = auto;
                		}
                		role.setServerCloudURL(srvTabUrl);
                		role.setServerCloudKey(depMap.get("srvKey"));
                		/*
                		 * By default package type is local,
                		 * hence store server home directory for cloud.
                		 */
                		role.setServerCloudHome(depMap.get("srvHome"));
                		if (depMap.get("srvAutoDwnldChecked").equalsIgnoreCase("true")) {
                			role.setServerCloudUploadMode(WARoleComponentCloudUploadMode.auto);
                		}
                	}
                }

                /*
                 * Handling adding server application
                 * without configuring server/JDK.
                 * Always add cloud attributes as
                 * approot directory is not created yet
                 * hence all applications are
                 * imported from outside of the Azure project
                 */
                if (!tabPg.getAppsAsNames().isEmpty()) {
                    for (int i = 0; i < tabPg.getAppsList().size(); i++) {
                        AppCmpntParam app = tabPg.getAppsList().get(i);
                        if (!app.getImpAs().
                        		equalsIgnoreCase(Messages.helloWorld)) {
                            role.addServerApplication(app.getImpSrc(),
                            		app.getImpAs(), app.getImpMethod(),
                            		templateFile, true);
                        }
                    }
                }
            }

            /**
             * Handling for HelloWorld application in plug-in
             */
            if (tabPg != null) {
                if (!tabPg.getAppsAsNames().contains(Messages.helloWorld)) {
                    List<WindowsAzureRoleComponent> waCompList =
                    		waProjMgr.getRoles().
                    		get(0).getServerApplications();
                    for (WindowsAzureRoleComponent waComp : waCompList) {
                        if (waComp.getDeployName().
                        		equalsIgnoreCase(Messages.helloWorld)
                        		&& waComp.getImportPath().isEmpty()) {
                            waComp.delete();
                        }
                    }
                }
            }

            // Enable Key features
            // Session Affinity
            if (ftrMap.get("ssnAffChecked")) {
            	WindowsAzureEndpoint httpEndPt =
            			role.getEndpoint(Messages.httpEp);
            	if (httpEndPt == null) {
            		/*
            		 * server is not enabled.
            		 * hence create new endpoint
            		 * for session affinity.
            		 */
            		if (role.isValidEndpoint(Messages.httpEp,
            				WindowsAzureEndpointType.Input,
            				HTTP_PRV_PORT, HTTP_PORT)) {
            			httpEndPt = role.addEndpoint(Messages.httpEp,
            					WindowsAzureEndpointType.Input,
            					HTTP_PRV_PORT, HTTP_PORT);
            		}
            	}
            	if (httpEndPt != null) {
            		role.
            		setSessionAffinityInputEndpoint(httpEndPt);
            	}
            }

            // Caching
            if (ftrMap.get("cacheChecked")) {
            	role.setCacheMemoryPercent(CACH_DFLTVAL);
            	role.setCacheStorageAccountName(dashAuto);
            }

            // Remote Debugging
            if (ftrMap.get("debugChecked")) {
            	if (role.isValidEndpoint(Messages.dbgEp,
            			WindowsAzureEndpointType.Input,
            			DEBUG_PORT, DEBUG_PORT)) {
            		WindowsAzureEndpoint dbgEndPt =
            				role.addEndpoint(Messages.dbgEp,
            						WindowsAzureEndpointType.Input,
            						DEBUG_PORT, DEBUG_PORT);
            		if (dbgEndPt != null) {
            			role.setDebuggingEndpoint(dbgEndPt);
            			role.setStartSuspended(false);
            		}
            	}
            }

            waProjMgr.save();
            WindowsAzureProjectManager.moveProjFromTemp(projName, projLocation);
            String launchFilePath = projLocation + File.separator
            		+ projName + LAUNCH_FILE_PATH;
            ParseXML.setProjectNameinLaunch(launchFilePath,
                    Messages.pWizWinAzureProj, projName);

            root.touch(null);
            project =  root.getProject(projName);
            IProjectDescription projDescription =
            		workspace.newProjectDescription(projName);

            Path path = new Path(projLocation + File.separator + projName);
            projDescription.setLocation(path);
            projDescription.setNatureIds(
            		new String [] {WAProjectNature.NATURE_ID});

            if (!project.exists()) {
            	if (isDefault) {
            		project.create(null);
            	} else {
            		project.create(projDescription, null);
            	}
            }
            project.open(null);

            projDescription = project.getDescription();
            projDescription.setName(projName);
            projDescription.setNatureIds(
            		new String [] {WAProjectNature.NATURE_ID});

            project.move(projDescription, IResource.FORCE, null);

            workingSetManager.addToWorkingSets(project, selWorkingSets);

            root.touch(null);
            if (project != null) {
            	WADependencyBuilder builder = new WADependencyBuilder();
            	builder.addBuilder(project,
            			"com.persistent.winazure.eclipseplugin.Builder");
            }
        } catch (Exception e) {
        	Display.getDefault().syncExec(new Runnable() {
        		public void run() {
        			MessageDialog.openError(null,
        					Messages.pWizErrTitle,
        					Messages.pWizErrMsg);
        		}
        	});
        	Activator.getDefault().log(Messages.pWizErrMsg, e);
        }
    }

    /**
     * Add page to the wizard.
     */
    @Override
    public void addPages() {
        String sdkPath = null;
        try {
            sdkPath = WindowsAzureProjectManager.
            		getLatestAzureSdkDir();
        } catch (IOException e) {
            sdkPath = null;
            Activator.getDefault().log(errorMessage, e);
        }

        try {
            if (sdkPath == null) {
                errorTitle = Messages.sdkInsErrTtl;
                errorMessage = Messages.sdkInsErrMsg;
                boolean choice = MessageDialog.openQuestion(new Shell(),
                        errorTitle, errorMessage);
                if (choice) {
                    PlatformUI.getWorkbench().getBrowserSupport()
                    .getExternalBrowser()
                    .openURL(new URL(Messages.sdkInsUrl));
                }
                addPage(null);
            } else {
                waProjWizPage = new WAProjectWizardPage(Messages.projWizPg);
                tabPg = new WATabPage(Messages.tbPg, waRole, this, true);
                /*
                 * If wizard activated through right click on
                 * Dynamic web project then
                 * Add that as an WAR application.
                 */
                if (Activator.getDefault().isContextMenu()) {
                	IProject selProj = getSelectProject();
                	tabPg.addToAppList(selProj.getLocation().
                    		toOSString(), selProj.getName()
                    		+ ".war", WindowsAzureRoleComponentImportMethod.
                    		auto.name());
                }

                waKeyPage = new WAKeyFeaturesPage(Messages.keyPg);
                addPage(waProjWizPage);
                addPage(tabPg);
                addPage(waKeyPage);
            }
        } catch (Exception ex) {
            // only logging the error in log file not showing anything to
            // end user
            Activator.getDefault().log(errorMessage, ex);
        }
    }

    /**
     * The first page for wizard.
     *
     * @return Wizard Page
     */
    @Override
    public IWizardPage getStartingPage() {
        return waProjWizPage;
    }

    /**
     * If wizard can be finished or not.
     *
     * @return boolean
     */
    @Override
    public boolean canFinish() {
        boolean validPage = false;
        // check starting page is valid
        if (waProjWizPage.canFlipToNextPage()) {
            validPage = waProjWizPage.isPageComplete();
        }
        if (validPage) {
            validPage = waProjWizPage.isPageComplete()
            		&& tabPg.isPageComplete();
        }
        return validPage;
    }

    /**
     * To enable/disable cancel operation.
     *
     * @return boolean
     */
    @Override
    public boolean performCancel() {
        //re-initialising context menu to default option : false
        Activator.getDefault().setContextMenu(false);
        return true;
    }

    /**
     * Whether Previous And Next Buttons are needed/not.
     *
     * @return boolean
     */
    @Override
    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    /**
     * Returns configurations done on WATagPage.
     * @return Map<String, String>
     */
    @SuppressWarnings("static-access")
    private Map<String, String> getDeployPageValues() {
    	Map <String, String> values = new HashMap<String, String>();
    	// JDK
    	values.put("jdkChecked" , Boolean.valueOf(
    			tabPg.isJdkChecked()).toString());
    	values.put("jdkLoc" , tabPg.getJdkLoc());
    	// JDK download group
    	values.put("jdkDwnldChecked" , Boolean.valueOf(
    			tabPg.isJdkDownloadChecked()).toString());
    	values.put("jdkAutoDwnldChecked" , Boolean.valueOf(
    			tabPg.isJdkAutoUploadChecked()).toString());
    	values.put("jdkThrdPartyChecked" , Boolean.valueOf(
    			tabPg.isThirdPartyJdkChecked()).toString());
    	values.put("jdkName" , tabPg.getJdkName());
    	values.put("jdkUrl" , tabPg.getJdkUrl());
    	values.put("jdkKey" , tabPg.getJdkKey());
    	values.put("javaHome", tabPg.getJavaHome());
    	// Server
    	values.put("serChecked" , Boolean.valueOf(
    			tabPg.isSrvChecked()).toString());
    	values.put("servername", tabPg.getServerName());
    	values.put("serLoc", tabPg.getServerLoc());
    	values.put("tempFile", WAEclipseHelper.
    			getTemplateFile(Messages.cmpntFile));
    	// Server download group
    	values.put("srvDwnldChecked" , Boolean.valueOf(
    			JdkSrvConfig.isSrvDownloadChecked()).toString());
    	values.put("srvAutoDwnldChecked" , Boolean.valueOf(
    			JdkSrvConfig.isSrvAutoUploadChecked()).toString());
    	values.put("srvUrl" , tabPg.getSrvUrl());
    	values.put("srvKey" , tabPg.getSrvKey());
    	values.put("srvHome", tabPg.getSrvHomeDir());
    	return values;
    }

    /**
     * Returns configurations done on WAKeyFeaturesPage.
     * @return Map<String, Boolean>
     */
    private Map<String, Boolean> getKeyFtrPageValues() {
    	Map <String, Boolean> ftrPgValues = new
    			HashMap<String, Boolean>();
    	ftrPgValues.put("ssnAffChecked", waKeyPage.isSsnAffChecked());
    	ftrPgValues.put("cacheChecked", waKeyPage.isCacheChecked());
    	ftrPgValues.put("debugChecked", waKeyPage.isDebugChecked());
    	return ftrPgValues;
    }

    /**
     * This method returns currently selected project in workspace.
     * Do not use WAEclipseHelper Utility method instead of this method
     * @return IProject
     */
    private IProject getSelectProject() {
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
        }
        return selProject;
    }

    /**
     * Capture page changed event and if current page
     * is WATabPage, then by default select JDK tab.
     */
    @Override
    public void pageChanged(PageChangedEvent event) {
    	IWizardPage page = (IWizardPage) event.getSelectedPage();
    	if (page.getName().equals(Messages.tbPg)) {
    		WATabPage.getFolder().
    		setSelection(WATabPage.getJdkTab());
    	}
    }
}
