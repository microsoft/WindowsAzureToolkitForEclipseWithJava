/*******************************************************************************
 * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.gigaspaces.azure.wizards;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;

import waeclipseplugin.Activator;

import com.gigaspaces.azure.model.AutoUpldCmpnts;
import com.gigaspaces.azure.model.CreateStorageServiceInput;
import com.gigaspaces.azure.model.RemoteDesktopDescriptor;
import com.gigaspaces.azure.model.StorageService;
import com.interopbridges.tools.windowsazure.OSFamilyType;
import com.interopbridges.tools.windowsazure.WARoleComponentCloudUploadMode;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzurePackageType;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.interopbridges.tools.windowsazure.WindowsAzureRoleComponent;
import com.microsoftopentechnologies.wacommon.storageregistry.PreferenceUtilStrg;
import com.microsoftopentechnologies.wacommon.storageregistry.StorageAccount;
import com.microsoftopentechnologies.wacommon.storageregistry.StorageAccountRegistry;
import com.microsoftopentechnologies.wacommon.utils.EncUtilHelper;
import com.microsoftopentechnologies.wacommon.utils.PreferenceSetUtil;
import com.microsoftopentechnologies.wacommon.utils.WACommonException;
import com.persistent.util.JdkSrvConfig;
import com.persistent.util.MessageUtil;
import com.persistent.util.WAEclipseHelper;

/**
 * Class responsible for deploying windows azure project
 * to cloud.
 */
public class DeployWizard extends Wizard {
	private SignInPage signInPage;
	private IProject selectedProject;
	private WindowsAzurePackageType deployMode = WindowsAzurePackageType.CLOUD;
	private static final String BASE_PATH = "${basedir}";
	private final String auto = "auto";
	private final String dashAuto = "-auto";
	/**
	 * To store components which got modified before build.
	 */
	List<AutoUpldCmpnts> mdfdCmpntList =
			new ArrayList<AutoUpldCmpnts>();
	/**
	 * To store roles of whom cache property
	 * got modified before build.
	 */
	List<String> roleMdfdCache = new ArrayList<String>();

	/**
	 * Constructor.
	 */
	public DeployWizard() {
		super();
		selectedProject = WizardCacheManager.
				getCurrentSelectedProject();
		try {
			setWindowTitle(Messages.publishWiz);
			setDefaultPageImageDescriptor(Activator.
					getImageDescriptor(Activator.DEPLOY_IMAGE));
		} catch (IOException e) {
			Activator.getDefault().log(Messages.error, e);
		}
	}

	/**
	 * Returns currently selected project.
	 * @return
	 */
	public IProject getSelectedProject() {
		return selectedProject;
	}

	@Override
	public void addPages() {
		if (selectedProject != null) {
			signInPage = new SignInPage();
			signInPage.setSelectedProject(selectedProject);
			addPage(signInPage);
		}
	}

	@Override
	public boolean needsPreviousAndNextButtons() {
		return false;
	}

	@Override
	public boolean performFinish() {
		try {
			WindowsAzureProjectManager waProjManager =
					WindowsAzureProjectManager.load(
							new File(selectedProject.getLocation().toOSString()));

			// Configure or remove remote access settings
			boolean status = handleRDPSettings(waProjManager);
			if (!status) {
				return false;
			}
			// clear new service array
			signInPage.getNewServices().clear();

			// set target OS
			String wizTargetOS = signInPage.getTargetOSName();
			if (!waProjManager.getOSFamily().getName().
					equalsIgnoreCase(wizTargetOS)) {
				if (wizTargetOS.equals(OSFamilyType.
						WINDOWS_SERVER_2008_R2.getName())) {
					waProjManager.setOSFamily(OSFamilyType.WINDOWS_SERVER_2008_R2);
				} else {
					waProjManager.setOSFamily(OSFamilyType.WINDOWS_SERVER_2012);
				}
			}
			// WORKITEM: China Support customizable portal URL in the plugin
			try {
				String prefSetUrl = PreferenceSetUtil.getSelectedPortalURL(
						PreferenceSetUtil.getSelectedPreferenceSetName());
				/*
				 * Don't check if URL is empty or null.
				 * As if it is then we remove "portalurl" attribute
				 * from package.xml.
				 */
				waProjManager.setPortalURL(prefSetUrl);
			} catch (WACommonException e1) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						MessageDialog.openError(null,
								Messages.error,
								Messages.getPrefUrlErMsg);
					}
				});
			}
			waProjManager.save();

			waProjManager = WindowsAzureProjectManager.
					load(new File(selectedProject.getLocation().toOSString()));
			
			WAAutoStorageConfJob autoStorageConfJob = new WAAutoStorageConfJob(Messages.confStorageAccount);
			autoStorageConfJob.setManager(waProjManager);
			autoStorageConfJob.schedule();
			autoStorageConfJob.addJobChangeListener(new JobChangeAdapter() {
				public void done(IJobChangeEvent event) {
					if (event.getResult().isOK()) {
						try {
							WindowsAzureProjectManager waProjManager = WindowsAzureProjectManager.
									load(new File(selectedProject.getLocation().toOSString()));
							
							WindowsAzureBuildProjectJob buildProjectJob = new
									WindowsAzureBuildProjectJob(Messages.buildProj);
							buildProjectJob.setManager(waProjManager);
							buildProjectJob.schedule();

							buildProjectJob.addJobChangeListener(new JobChangeAdapter() {

								public void done(IJobChangeEvent event) {
									if (event.getResult().isOK()) {
										try {
											WindowsAzureProjectManager waProjManager = WindowsAzureProjectManager.
													load(new File(selectedProject.getLocation().toOSString()));
											/*
											 * Build job is completed.
											 * If component's url settings done before build then,
											 * replace with auto.
											 */
											if (mdfdCmpntList.size() > 0) {
												waProjManager = addAutoCloudUrl(waProjManager);
												waProjManager.save();
											}
											/*
											 * If cache's storage account name and key,
											 * is changed before build then replace with auto.
											 */
											if (roleMdfdCache.size() > 0) {
												waProjManager = addAutoSettingsForCache(waProjManager);
												waProjManager.save();
											}
											WAEclipseHelper.refreshWorkspace(
													com.persistent.winazureroles.Messages.rfrshErrTtl,
													com.persistent.winazureroles.Messages.rfrshErrMsg);
										} catch (WindowsAzureInvalidProjectOperationException e) {
											Activator.getDefault().log(Messages.error, e);
										}
										Job job = new WindowsAzureDeploymentJob(
												Messages.deployingToAzure,selectedProject);
										job.addJobChangeListener(new JobChangeAdapter() {
											public void done(IJobChangeEvent event) {
												if (!event.getResult().isOK()) {
													Display.getDefault().asyncExec(new Runnable() {
														@Override
														public void run() {
															MessageDialog.openInformation(
																	getShell(),
																	Messages.interrupt,
																	Messages.deploymentInterrupted);
														}
													});
												}
												WAEclipseHelper.refreshWorkspace(
														com.persistent.winazureroles.Messages.rfrshErrTtl,
														com.persistent.winazureroles.Messages.rfrshErrMsg);
											}
										});
										job.schedule();
									}
								}
							});
						} catch (WindowsAzureInvalidProjectOperationException e) {
							Activator.getDefault().log(Messages.error, e);
						}
					}
				}
			});
		} catch (WindowsAzureInvalidProjectOperationException e) {
			MessageUtil.displayErrorDialog(getShell(),
					Messages.buildFail,
					Messages.projLoadErr);
			return false;
		}
		return true;
	}

	@Override
	public boolean needsProgressMonitor() {
		return true;
	}

	private class WindowsAzureBuildProjectJob extends Job {

		private WindowsAzureProjectManager waProjManager;

		public WindowsAzureBuildProjectJob(String name) {
			super(name);
		}

		public void setManager(WindowsAzureProjectManager manager) {
			this.waProjManager = manager;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask(String.format(
					Messages.buildingProjTask,
					selectedProject.getName()),
					IProgressMonitor.UNKNOWN);
			try {
				waProjManager.setPackageType(deployMode);
				selectedProject.build(IncrementalProjectBuilder.
						CLEAN_BUILD, monitor);
				waProjManager.save();
				selectedProject.build(IncrementalProjectBuilder.
						INCREMENTAL_BUILD, null);
				selectedProject.refreshLocal(IResource.
						DEPTH_INFINITE, monitor);
				super.setName("");
				monitor.done();
				if (WAEclipseHelper.isBuildSuccessful(
						waProjManager, selectedProject)) {
					return Status.OK_STATUS;
				} else {
					return Status.CANCEL_STATUS;
				}
			} catch (Exception e) {
				Activator.getDefault().log(Messages.error, e);
				super.setName("");
				monitor.done();
				return Status.CANCEL_STATUS;
			}
		}
	}

	private class WAAutoStorageConfJob extends Job {

		private WindowsAzureProjectManager waProjManager;

		public WAAutoStorageConfJob(String name) {
			super(name);
		}

		public void setManager(WindowsAzureProjectManager manager) {
			this.waProjManager = manager;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask(Messages.confStorageAccount,				
					IProgressMonitor.UNKNOWN);
			try {
				// Create storage account if it does not exists
				createStorageAccountIfNotExists();

				/*
				 * Check components having upload method "AUTO"
				 * and cloudurl set to auto, update them with
				 * appropriate blob url and key
				 * as per storage account selected on wizard.
				 */
				waProjManager = removeAutoCloudUrl(waProjManager);
				waProjManager.save();
			} catch (Exception e) {
				Activator.getDefault().log(Messages.error, e);
				super.setName("");
				monitor.done();
				return Status.CANCEL_STATUS;
			}
			super.setName("");
			monitor.done();
			return Status.OK_STATUS;
		}
	}

	private void createStorageAccountIfNotExists() throws InterruptedException, Exception {
		StorageService storageAccount = WizardCacheManager.getCurrentStorageAcount();
		
		if (storageAccount.getUrl() == null || storageAccount.getUrl().isEmpty()) {
			CreateStorageServiceInput body = new CreateStorageServiceInput(storageAccount.getServiceName(),
			storageAccount.getServiceName(),
			storageAccount.getStorageServiceProperties().getLocation());
			body.setDescription(storageAccount.getStorageServiceProperties().getDescription());
			StorageService storageService = WizardCacheManager.createStorageAccount(body);
			/*
			 * Add newly created storage account
			 * in centralized storage account registry.
			 */
			StorageAccount storageAccountPref = new StorageAccount(storageService.getServiceName(),
					storageService.getStorageServiceKeys().getPrimary(),
					storageService.
					getStorageServiceProperties().getEndpoints().
					getEndpoints().get(0));
			StorageAccountRegistry.addAccount(storageAccountPref);
			PreferenceUtilStrg.save();
		}
	}

	/**
	 * Configure or remove remote access settings
	 * according to user name provided by user.
	 * @param waProjManager
	 */
	private boolean handleRDPSettings(
			WindowsAzureProjectManager waProjManager) {
		try {
			/*
			 * Check if service is new.
			 */
			boolean isNewService = false;
			if (signInPage.getNewServices().
					contains(signInPage.getCurrentService())) {
				isNewService = true;
			}
			String userName = null;
			String pwd = null;
			Date expDate = null;
			String certPath = "";
			String pfxPath = "";
			String pfxPassword = null;
			boolean remoteEnabled = false;
			if (signInPage.getRdpUname().isEmpty()) {
				// disable remote access
				waProjManager.setRemoteAccessAllRoles(false);
			} else {
				remoteEnabled = true;
				userName = signInPage.getRdpUname();
				pwd = signInPage.getRdpPwd();
				String defaultCertPath = String.format("%s%s",
						selectedProject.getLocation().toOSString(),
						Messages.cerPath);
				String defaultPfxPath = String.format("%s%s",
						selectedProject.getLocation().toOSString(),
						Messages.pfxPath);
				String defaultThumbprint =
						EncUtilHelper.getThumbPrint(defaultCertPath);
				// already enabled
				if (waProjManager.getRemoteAccessAllRoles()) {
					/*
					 * Check if sample certificate is used or
					 * custom one.
					 */
					certPath = waProjManager.getRemoteAccessCertificatePath();
					if (certPath.startsWith(BASE_PATH)) {
						certPath = certPath.substring(certPath.indexOf("}") + 1
								, certPath.length());
						certPath = String.format("%s%s",
								selectedProject.getLocation().toOSString(),
								certPath);
					}
					String thumbprint = EncUtilHelper.getThumbPrint(certPath);
					if (thumbprint.equals(defaultThumbprint)) {
						// sample certificate
						pfxPath = defaultPfxPath;
						pfxPassword = Messages.defPfxPwd;
					} else {
						// custom certificate
						// open dialog to accept pfx password
						PfxPwdDialog inputDlg = new PfxPwdDialog(
								getShell(), certPath, isNewService);
						int btnId = inputDlg.open();
						if (btnId == Window.OK) {
							pfxPath = inputDlg.getPfxPath();
							pfxPassword = inputDlg.getPwd();
						} else {
							/*
							 * Just return to publish wizard.
							 * No need to save any information.
							 */
							return false;
						}
					}
					// if user name has been changed by user
					if (!userName.
							equals(waProjManager.getRemoteAccessUsername())) {
						waProjManager.
						setRemoteAccessUsername(userName);
					}

					// if password has been changed by user
					if (!pwd.
							equals(waProjManager.getRemoteAccessEncryptedPassword())) {
						String encryptedPwd =
								EncUtilHelper.encryptPassword(pwd,
										certPath);
						waProjManager.
						setRemoteAccessEncryptedPassword(encryptedPwd);
					}
					expDate = waProjManager.getRemoteAccessAccountExpiration();
				} else {
					// enabling for the first time, so use all default entries
					waProjManager.setRemoteAccessAllRoles(true);
					waProjManager.setRemoteAccessUsername(
							userName);
					// save certificate path
					certPath = defaultCertPath;
					waProjManager.setRemoteAccessCertificatePath(certPath);
					// save thumb print
					waProjManager.
					setRemoteAccessCertificateFingerprint(defaultThumbprint);
					// save password, encrypt always as storing for the first time
					String encryptedPwd =
							EncUtilHelper.encryptPassword(pwd,
									certPath);
					waProjManager.
					setRemoteAccessEncryptedPassword(encryptedPwd);
					// save expiration date
					GregorianCalendar currentCal = new GregorianCalendar();
					currentCal.add(Calendar.YEAR, 1);
					expDate = currentCal.getTime();
					waProjManager.setRemoteAccessAccountExpiration(expDate);
					// save pfx path
					pfxPath = defaultPfxPath;
					// save pfx password
					pfxPassword = Messages.defPfxPwd;
				}
			}
			signInPage.fireConfigurationEvent(new ConfigurationEventArgs(
					this,ConfigurationEventArgs.REMOTE_DESKTOP,
					new RemoteDesktopDescriptor(userName,
							pwd,
							expDate,
							certPath,
							pfxPath,
							pfxPassword,
							signInPage.getConToDplyChkStatus(),
							remoteEnabled)));
		} catch (Exception e) {
			Activator.getDefault().log(Messages.rdpError, e);
			return false;
		}
		return true;
	}

	/**
	 * Check each role and its respective
	 * components having upload method "AUTO"
	 * and cloudurl set to auto, update them with
	 * appropriate blob url and key
	 * as per storage account selected on wizard.
	 * Also method remembers which components has been updated
	 * so that after project build
	 * they can be restored to original state.
	 * @param projMngr
	 * @return
	 */
	private WindowsAzureProjectManager removeAutoCloudUrl(
			WindowsAzureProjectManager projMngr) {
		mdfdCmpntList.clear();
		roleMdfdCache.clear();
		try {
			// get number of roles in one project
			List<WindowsAzureRole> roleList = projMngr.getRoles();
			StorageService curAcc = WizardCacheManager.getCurrentStorageAcount();
			String curKey = curAcc.getStorageServiceKeys().getPrimary();
			String accUrl = curAcc.
					getStorageServiceProperties().getEndpoints().
					getEndpoints().get(0);
			for (int i = 0; i < roleList.size(); i++) {
				WindowsAzureRole role = roleList.get(i);
				/*
				 * check for caching storage account name given
				 * and its "auto"
				 * then update cache name and key.
				 */
				String name = role.getCacheStorageAccountName();
				if (name != null
						&& !name.isEmpty()
						&& name.equals(dashAuto)) {
					roleMdfdCache.add(role.getName());
					role.setCacheStorageAccountName(curAcc.getServiceName());
					role.setCacheStorageAccountKey(curKey);
					role.setCacheStorageAccountUrl(accUrl);
				}
				// get list of components in one role.
				List<WindowsAzureRoleComponent> cmpnntsList =
						role.getComponents();
				for (int j = 0; j < cmpnntsList.size(); j++) {
					WindowsAzureRoleComponent component =
							cmpnntsList.get(j);
					/*
					 * Check component is of JDK or server
					 * and auto upload is enabled.
					 */
					if ((component.getType().equals(
							com.persistent.winazureroles.Messages.typeJdkDply)
							|| component.getType().equals(
									com.persistent.winazureroles.Messages.typeSrvDply))
									&& component.getCloudUploadMode() != null
									&& component.getCloudUploadMode().
									equals(WARoleComponentCloudUploadMode.AUTO)) {
						/*
						 * Check storage account is not specified,
						 * i.e URL is auto
						 */
						if (component.getCloudDownloadURL().equalsIgnoreCase(auto)) {
							// update cloudurl and cloudkey
							/*
							 * If component is JDK, then check if its
							 * third party JDK.
							 */
							if (component.getType().equals(
									com.persistent.winazureroles.Messages.typeJdkDply)) {
								String jdkName = role.getJDKCloudName();
								if (jdkName == null || jdkName.isEmpty()) {
									component.setCloudDownloadURL(JdkSrvConfig.
											prepareCloudBlobURL(
													component.getImportPath(),
													accUrl));
								} else {
									component.setCloudDownloadURL(
											JdkSrvConfig.prepareUrlForThirdPartyJdk(
													jdkName,
													accUrl));
								}
							} else {
								component.setCloudDownloadURL(JdkSrvConfig.
										prepareCloudBlobURL(
												component.getImportPath(),
												accUrl));
							}
							component.setCloudKey(curKey);
							// Save components that are modified
							AutoUpldCmpnts obj = new AutoUpldCmpnts(role.getName());
							/*
							 * Check list contains
							 * entry with this role name,
							 * if yes then just add index of entry to list
							 * else create new object.
							 */
							if (mdfdCmpntList.contains(obj)) {
								int index = mdfdCmpntList.indexOf(obj);
								AutoUpldCmpnts presentObj =
										mdfdCmpntList.get(index);
								if (!presentObj.getCmpntIndices().contains(j)) {
									presentObj.getCmpntIndices().add(j);
								}
							} else {
								mdfdCmpntList.add(obj);
								obj.getCmpntIndices().add(j);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			Activator.getDefault().log(Messages.autoUploadEr, e);
		}
		return projMngr;
	}

	/**
	 * Method restores components which are updated before build
	 * to original state i.e. again updates cloudurl to "auto"
	 * and removes cloudkey attribute.
	 * @param projMngr
	 * @return
	 */
	private WindowsAzureProjectManager addAutoCloudUrl(
			WindowsAzureProjectManager projMngr) {
		try {
			// get number of roles in one project
			List<WindowsAzureRole> roleList = projMngr.getRoles();
			for (int i = 0; i < roleList.size(); i++) {
				WindowsAzureRole role = roleList.get(i);
				AutoUpldCmpnts obj = new AutoUpldCmpnts(role.getName());
				// check list has entry with this role name
				if (mdfdCmpntList.contains(obj)) {
					// get list of components
					List<WindowsAzureRoleComponent> cmpnntsList =
							role.getComponents();
					// get indices of components which needs to be updated.
					int index = mdfdCmpntList.indexOf(obj);
					AutoUpldCmpnts presentObj =
							mdfdCmpntList.get(index);
					List<Integer> indices = presentObj.getCmpntIndices();
					// iterate over indices and update respective components.
					for (int j = 0; j < indices.size(); j++) {
						WindowsAzureRoleComponent cmpnt =
								cmpnntsList.get(indices.get(j));
						cmpnt.setCloudDownloadURL(auto);
						cmpnt.setCloudKey("");
					}
				}
			}
		} catch (Exception e) {
			Activator.getDefault().log(Messages.autoUploadEr, e);
		}
		return projMngr;
	}

	/**
	 * Method restores caching properties which are updated before build
	 * to original state i.e. again updates storage account name to "auto"
	 * and removes key property.
	 * @param projMngr
	 * @return
	 */
	private WindowsAzureProjectManager addAutoSettingsForCache(
			WindowsAzureProjectManager projMngr) {
		try {
			// get number of roles in one project
			List<WindowsAzureRole> roleList = projMngr.getRoles();
			for (int i = 0; i < roleList.size(); i++) {
				WindowsAzureRole role = roleList.get(i);
				if (roleMdfdCache.contains(role.getName())) {
					role.setCacheStorageAccountName(dashAuto);
					role.setCacheStorageAccountKey("");
					role.setCacheStorageAccountUrl("");
				}
			}
		} catch (Exception e) {
			Activator.getDefault().log(Messages.autoUploadEr, e);
		}
		return projMngr;
	}
}