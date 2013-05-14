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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import waeclipseplugin.Activator;

import com.gigaspaces.azure.model.RemoteDesktopDescriptor;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzurePackageType;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.microsoftopentechnologies.wacommon.utils.EncUtilHelper;
import com.microsoftopentechnologies.wacommon.utils.PreferenceSetUtil;
import com.microsoftopentechnologies.wacommon.utils.WACommonException;
import com.persistent.util.MessageUtil;

/**
 * Class responsible for deploying windows azure project
 * to cloud.
 */
public class DeployWizard extends Wizard {
	private SignInPage signInPage;
	private IProject selectedProject;
	private WindowsAzurePackageType deployMode = WindowsAzurePackageType.CLOUD;
	private static Map<String, Boolean> rememberMydecisions = new HashMap<String, Boolean>();
	private static Map<String, Integer> decisions = new HashMap<String, Integer>();
	private static final String BASE_PATH = "${basedir}";

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

			// WORKITEM: China Support customizable portal URL in the plugin
			try {
				String prefSetUrl = PreferenceSetUtil.getSelectedPortalURL();
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

			WindowsAzureBuildProjectJob buildProjectJob = new
					WindowsAzureBuildProjectJob(Messages.buildProj);
			buildProjectJob.setManager(waProjManager);
			buildProjectJob.setShell(getShell());
			buildProjectJob.schedule();

			buildProjectJob.addJobChangeListener(new JobChangeAdapter() {
				public void done(IJobChangeEvent event) {
					if (event.getResult().isOK()) {
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
							}
						});
						job.schedule();
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

		private Shell shell;
		private WindowsAzureProjectManager waProjManager;

		public WindowsAzureBuildProjectJob(String name) {
			super(name);
		}

		public void setShell(Shell shell) {
			this.shell = shell;
		}

		public void setManager(WindowsAzureProjectManager manager) {
			this.waProjManager = manager;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {

			MessageDialogWithToggle dialog = null;

			final AtomicReference<MessageDialogWithToggle> dialogRef = new
					AtomicReference<MessageDialogWithToggle>();

			String projectName = selectedProject.getName();

			Boolean rememberMyDecisionForSelectedProject =
					rememberMydecisions.get(projectName);

			if (rememberMyDecisionForSelectedProject == null
					|| rememberMyDecisionForSelectedProject == false) {

				Display.getDefault().syncExec(new Runnable() {

					@Override
					public void run() {
						MessageDialogWithToggle dialog = MessageDialogWithToggle.open(
								MessageDialog.QUESTION, shell,
								Messages.deplConfirmConfigChangeMsg,
								Messages.deplFullProjBuildConfirmMsg,
								Messages.deplRememberMyDecisionMsg,
								false, null, null,
								SWT.SHEET);	
						dialogRef.set(dialog);
					}
				});
			}

			dialog = dialogRef.get();

			if (dialog != null) {
				/*
				 * user did not want to remember his
				 * decision for this project. dialog was opened.
				 */
				boolean toogleState = dialog.getToggleState();
				if (toogleState) {
					rememberMydecisions.put(projectName, true);
				} else {
					rememberMydecisions.put(projectName, false);
				}
				decisions.put(projectName, dialog.getReturnCode());
			}
			if  (decisions.get(projectName) == IDialogConstants.YES_ID) {
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
			return Status.OK_STATUS;
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
}