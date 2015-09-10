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

import com.interopbridges.tools.windowsazure.DeploymentSlot;
import com.interopbridges.tools.windowsazure.OSFamilyType;
import com.interopbridges.tools.windowsazure.WARoleComponentCloudUploadMode;
import com.interopbridges.tools.windowsazure.WindowsAzureCertificate;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzurePackageType;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.interopbridges.tools.windowsazure.WindowsAzureRoleComponent;
import com.microsoft.windowsazure.management.compute.models.HostedServiceListResponse.HostedService;
import com.microsoft.windowsazure.management.compute.models.ServiceCertificateListResponse.Certificate;
import com.microsoft.windowsazure.management.storage.models.StorageAccountCreateParameters;
import com.microsoftopentechnologies.azurecommons.deploy.model.AutoUpldCmpnts;
import com.microsoftopentechnologies.azurecommons.deploy.model.CertificateUpload;
import com.microsoftopentechnologies.azurecommons.deploy.model.CertificateUploadList;
import com.microsoftopentechnologies.azurecommons.deploy.model.RemoteDesktopDescriptor;
import com.microsoftopentechnologies.azurecommons.deploy.wizard.ConfigurationEventArgs;
import com.microsoftopentechnologies.azurecommons.deploy.wizard.DeployWizardUtilMethods;
import com.microsoftopentechnologies.azurecommons.roleoperations.JdkSrvConfigUtilMethods;
import com.microsoftopentechnologies.azurecommons.storageregistry.StorageAccount;
import com.microsoftopentechnologies.azurecommons.storageregistry.StorageAccountRegistry;
import com.microsoftopentechnologies.azurecommons.wacommonutil.CerPfxUtil;
import com.microsoftopentechnologies.azurecommons.wacommonutil.EncUtilHelper;
import com.microsoftopentechnologies.azurecommons.wacommonutil.PreferenceSetUtil;
import com.microsoftopentechnologies.azuremanagementutil.model.StorageService;
import com.microsoftopentechnologies.wacommon.storageregistry.PreferenceUtilStrg;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
import com.microsoftopentechnologies.wacommon.utils.WACommonException;
import com.persistent.util.JdkSrvConfig;
import com.persistent.util.MessageUtil;
import com.persistent.util.WAEclipseHelper;

import waeclipseplugin.Activator;

/**
 * Class responsible for deploying Azure project
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
	
	public DeployWizard(IProject projectToPublish) {
		super();
		selectedProject = projectToPublish;
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
			PluginUtil.showBusy(true);
			WindowsAzureProjectManager waProjManager =
					WindowsAzureProjectManager.load(
							new File(selectedProject.getLocation().toOSString()));
			
			// Update global properties in package.xml
			updateGlobalPropertiesinPackage(waProjManager);
			
			// Configure or remove remote access settings
			boolean status = handleRDPSettings(waProjManager);
			if (!status) {
				PluginUtil.showBusy(false);
				return false;
			}
			// certificate upload configuration
			List<WindowsAzureCertificate> certToUpload = handleCertUpload(waProjManager);
			PluginUtil.showBusy(false);
			if (certToUpload != null && certToUpload.size() > 0) {
				List<CertificateUpload> certUploadList = new ArrayList<CertificateUpload>();
				for (int i = 0; i < certToUpload.size(); i++) {
					WindowsAzureCertificate cert = certToUpload.get(i);
					String name = cert.getName();
					Boolean invokePfxDlg = true;
					/*
					 * If Remote access is enabled and
					 * using sample certificate, then don't ask PFX file path
					 * and password. Just assign default values.
					 */
					if (name.equalsIgnoreCase(
							com.gigaspaces.azure.deploy.Messages.remoteAccessPasswordEncryption)
							&& checkRDPUsesSampleCert(waProjManager)) {
						invokePfxDlg = false;
						String defaultPfxPath = String.format("%s%s",
								selectedProject.getLocation().toOSString(),
								Messages.pfxPath);
						CertificateUpload object = new CertificateUpload(
								name,
								cert.getFingerPrint(),
								defaultPfxPath,
								Messages.defPfxPwd);
						certUploadList.add(object);
					}
					if (invokePfxDlg) {
						// open dialog to accept pfx password
						PfxPwdDialog inputDlg = new PfxPwdDialog(
								getShell(), cert);
						int btnId = inputDlg.open();
						if (btnId == Window.OK) {
							CertificateUpload object = new CertificateUpload(
									name,
									cert.getFingerPrint(),
									inputDlg.getPfxPath(),
									inputDlg.getPwd());
							certUploadList.add(object);
						} else {
							/*
							 * Just return to publish wizard.
							 * No need to save any information.
							 */
							return false;
						}
					}
				}
				signInPage.fireConfigurationEvent(new ConfigurationEventArgs(
						this,
						ConfigurationEventArgs.CERTIFICATES,
						new CertificateUploadList(certUploadList)));
			}
			PluginUtil.showBusy(true);
			// clear new service array
			signInPage.getNewServices().clear();

			// set target OS
			String wizTargetOS = signInPage.getTargetOSName();
			if (!waProjManager.getOSFamily().getName().
					equalsIgnoreCase(wizTargetOS)) {

				for (OSFamilyType osType : OSFamilyType.values()) {
	            	if (osType.getName().equalsIgnoreCase(wizTargetOS)) {
	            		waProjManager.setOSFamily(osType);
	            	}
				}
			}
			// WORKITEM: China Support customizable portal URL in the plugin
			try {
				String path = PluginUtil.getPrefFilePath();
				String prefSetUrl = PreferenceSetUtil.getSelectedPortalURL(
						PreferenceSetUtil.getSelectedPreferenceSetName(path), path);
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
											Display.getDefault().syncExec(new Runnable() {
												public void run() {
													MessageDialog.openError(null,
															Messages.error,
															Messages.autoUploadEr);
												}
											});
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
							Display.getDefault().syncExec(new Runnable() {
								public void run() {
									MessageDialog.openError(null,
											Messages.error,
											Messages.deployErr);
								}
							});
						}
					}
				}
			});
		} catch (Exception e) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					MessageDialog.openError(null,
							Messages.buildFail,
							Messages.projLoadErr);
				}
			});
			return false;
		}
		return true;
	}

	private void updateGlobalPropertiesinPackage(WindowsAzureProjectManager waProjManager) throws WindowsAzureInvalidProjectOperationException {
		String currentSubscriptionID = WizardCacheManager.getCurrentPublishData().getCurrentSubscription().getSubscriptionID();
		waProjManager.setPublishSubscriptionId(currentSubscriptionID);
		waProjManager.setPublishSettingsPath(WizardCacheManager.getPublishSettingsPath(currentSubscriptionID));		
		waProjManager.setPublishCloudServiceName(WizardCacheManager.getCurentHostedService().getServiceName());
		waProjManager.setPublishRegion(WizardCacheManager.getCurentHostedService().getProperties().getLocation());
		waProjManager.setPublishStorageAccountName(WizardCacheManager.getCurrentStorageAcount().getServiceName());
		waProjManager.setPublishDeploymentSlot(DeploymentSlot.valueOf(WizardCacheManager.getCurrentDeplyState()));
		waProjManager.setPublishOverwritePreviousDeployment(Boolean.parseBoolean(WizardCacheManager.getUnpublish()));		
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
				PluginUtil.showBusy(false);
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

	private void createStorageAccountIfNotExists() throws Exception {
		StorageService storageAccount = WizardCacheManager.getCurrentStorageAcount();

		if (storageAccount.getUrl() == null || storageAccount.getUrl().isEmpty()) {
			StorageAccountCreateParameters accountParameters = new StorageAccountCreateParameters();
            accountParameters.setName(storageAccount.getServiceName());
			accountParameters.setLabel(storageAccount.getServiceName());
			accountParameters.setLocation(storageAccount.getStorageAccountProperties().getLocation());
			accountParameters.setDescription(storageAccount.getStorageAccountProperties().getDescription());
			StorageService storageService = WizardCacheManager.createStorageAccount(accountParameters);
			/*
			 * Add newly created storage account
			 * in centralized storage account registry.
			 */
			StorageAccount storageAccountPref = new StorageAccount(storageService.getServiceName(),
					storageService.getPrimaryKey(),
					storageService.
                            getStorageAccountProperties().getEndpoints().get(0).toString());
			StorageAccountRegistry.addAccount(storageAccountPref);
			PreferenceUtilStrg.save();
		}
	}

	/**
	 * Prepares list of certificates which needs to be
	 * uploaded to cloud service by comparing
	 * certificates present in particular cloud service
	 * with the certificates configured in selected project.
	 * @param projMngr
	 * @return
	 */
	private List<WindowsAzureCertificate> handleCertUpload(WindowsAzureProjectManager projMngr) {
		List<WindowsAzureCertificate> certToUpload =
				new ArrayList<WindowsAzureCertificate>();
		List<Certificate> cloudCertList = null;
		try {
			HostedService service = WizardCacheManager.getCurentHostedService();
			if (service.getUri() != null) {
				cloudCertList = WizardCacheManager.fetchUploadedCertificates();
			}
			certToUpload = DeployWizardUtilMethods.handleCertUpload(projMngr, cloudCertList);
		} catch (Exception e) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					MessageDialog.openError(null,
							Messages.error,
							Messages.certUploadEr);
				}
			});
			Activator.getDefault().log(Messages.certUploadEr, e);
		}
		return certToUpload;
	}

	/**
	 * If remote desktop is enabled
	 * then method checks whether
	 * its using sample certificate or not.
	 * @param waProjManager
	 * @return
	 */
	private boolean checkRDPUsesSampleCert(WindowsAzureProjectManager waProjManager) {
		Boolean usesSampleCert = false;
		try {
			usesSampleCert = DeployWizardUtilMethods.checkRDPUsesSampleCert(waProjManager,
					selectedProject.getLocation().toOSString());
		} catch (Exception e) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					MessageDialog.openError(null,
							Messages.error,
							Messages.certUploadEr);
				}
			});
			Activator.getDefault().log(Messages.certUploadEr, e);
		}
		return usesSampleCert;
	}

	/**
	 * Configure or remove remote access settings
	 * according to user name provided by user.
	 * @param waProjManager
	 */
	private boolean handleRDPSettings(
			WindowsAzureProjectManager waProjManager) {
		try {
			String userName = null;
			String pwd = null;
			Date expDate = null;
			String certPath = "";
			boolean remoteEnabled = false;
			if (signInPage.getRdpUname().isEmpty()) {
				// disable remote access
				waProjManager.setRemoteAccessAllRoles(false);
			} else {
				remoteEnabled = true;
				userName = signInPage.getRdpUname();
				pwd = signInPage.getRdpPwd();
				// already enabled
				if (waProjManager.getRemoteAccessAllRoles()) {
					certPath = waProjManager.getRemoteAccessCertificatePath();
					if (certPath.startsWith(BASE_PATH)) {
						certPath = certPath.substring(certPath.indexOf("}") + 1
								, certPath.length());
						certPath = String.format("%s%s",
								selectedProject.getLocation().toOSString(),
								certPath);
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
										certPath, PluginUtil.getEncPath());
						waProjManager.
						setRemoteAccessEncryptedPassword(encryptedPwd);
					}
					expDate = waProjManager.getRemoteAccessAccountExpiration();
				} else {
					// enabling for the first time, so use all default entries
					String defaultCertPath = String.format("%s%s",
							selectedProject.getLocation().toOSString(),
							Messages.cerPath);
					File certFile = new File(defaultCertPath);
					if (certFile.exists()) {
						String defaultThumbprint =
								CerPfxUtil.getThumbPrint(defaultCertPath);
						if (defaultThumbprint.equals(Messages.dfltThmbprnt)) {
							waProjManager.setRemoteAccessAllRoles(true);
							waProjManager.setRemoteAccessUsername(
									userName);
							certPath = defaultCertPath;
							// save certificate path in package.xml in the format of
							// ${basedir}\cert\SampleRemoteAccessPublic.cer
							waProjManager.setRemoteAccessCertificatePath(
									String.format("%s%s", BASE_PATH, Messages.cerPath));
							// save thumb print
							try {
								if (waProjManager.
										isRemoteAccessTryingToUseSSLCert(defaultThumbprint)) {
									MessageUtil.displayErrorDialog(getShell(),
											com.persistent.ui.propertypage.Messages.remAccSyntaxErr,
											com.persistent.ui.propertypage.Messages.usedBySSL);
									return false;
								} else {
									waProjManager.
									setRemoteAccessCertificateFingerprint(defaultThumbprint);
								}
							} catch (Exception e) {
								MessageUtil.displayErrorDialog(getShell(),
										Messages.error,
										Messages.dfltImprted);
								return false;
							}
							// save password, encrypt always as storing for the first time
							String encryptedPwd =
									EncUtilHelper.encryptPassword(pwd,
											certPath, PluginUtil.getEncPath());
							waProjManager.
							setRemoteAccessEncryptedPassword(encryptedPwd);
							// save expiration date
							GregorianCalendar currentCal = new GregorianCalendar();
							currentCal.add(Calendar.YEAR, 1);
							expDate = currentCal.getTime();
							waProjManager.setRemoteAccessAccountExpiration(expDate);
						} else {
							MessageUtil.displayErrorDialog(getShell(),
									Messages.error,
									Messages.dfltErrThumb);
							return false;
						}
					} else {
						MessageUtil.displayErrorDialog(getShell(),
								Messages.error,
								Messages.dfltErr);
						return false;
					}
				}
			}
			signInPage.fireConfigurationEvent(new ConfigurationEventArgs(
					this,ConfigurationEventArgs.REMOTE_DESKTOP,
					new RemoteDesktopDescriptor(userName,
							pwd,
							expDate,
							certPath,
							signInPage.getConToDplyChkStatus(),
							remoteEnabled)));
		} catch (Exception e) {
			MessageUtil.displayErrorDialogAndLog(
					getShell(),
					Messages.error,
					Messages.rdpError,
					e);
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
			String curKey = curAcc.getPrimaryKey();
			String accUrl = curAcc.getStorageAccountProperties().getEndpoints().get(0).toString();
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
					String cmpntType = component.getType();
					WARoleComponentCloudUploadMode mode =
							component.getCloudUploadMode();
					/*
					 * Check component is of JDK or server
					 * and auto upload is enabled.
					 */
					if (((cmpntType.equals(
							com.persistent.winazureroles.Messages.typeJdkDply)
							|| cmpntType.equals(
									com.persistent.winazureroles.Messages.typeSrvDply))
									&& mode != null
									&& mode.
									equals(WARoleComponentCloudUploadMode.auto))
									|| (cmpntType.equals(
											com.persistent.winazureroles.Messages.typeSrvApp)
											&& mode != null
											&& mode.equals(WARoleComponentCloudUploadMode.always))) {
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
							if (cmpntType.equals(
									com.persistent.winazureroles.Messages.typeJdkDply)) {
								String jdkName = role.getJDKCloudName();
								if (jdkName == null || jdkName.isEmpty()) {
									component.setCloudDownloadURL(JdkSrvConfigUtilMethods.
											prepareCloudBlobURL(
													component.getImportPath(),
													accUrl));
								} else {
									component.setCloudDownloadURL(
											JdkSrvConfig.prepareUrlForThirdPartyJdk(
													jdkName,
													accUrl));
								}
							} else if (cmpntType.equals(
									com.persistent.winazureroles.Messages.typeSrvDply)){
								String srvName = role.getServerCloudName();
								if (srvName == null || srvName.isEmpty()) {
									component.setCloudDownloadURL(JdkSrvConfigUtilMethods.
											prepareCloudBlobURL(
													component.getImportPath(),
													accUrl));
								} else {
									component.setCloudDownloadURL(
											JdkSrvConfig.prepareUrlForThirdPartySrv(
													srvName,
													accUrl));
								}

							} else {
								component.setCloudDownloadURL(JdkSrvConfigUtilMethods.
										prepareUrlForApp(
												component.getDeployName(),
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
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					MessageDialog.openError(null,
							Messages.error,
							Messages.autoUploadEr);
				}
			});
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
			projMngr = DeployWizardUtilMethods.
					addAutoCloudUrl(projMngr, mdfdCmpntList);
		} catch (Exception e) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					MessageDialog.openError(null,
							Messages.error,
							Messages.autoUploadEr);
				}
			});
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
			projMngr = DeployWizardUtilMethods.
					addAutoSettingsForCache(projMngr, roleMdfdCache);
		} catch (Exception e) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					MessageDialog.openError(null,
							Messages.error,
							Messages.autoUploadEr);
				}
			});
			Activator.getDefault().log(Messages.autoUploadEr, e);
		}
		return projMngr;
	}
}