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
package com.gigaspaces.azure.deploy;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import waeclipseplugin.Activator;

import com.gigaspaces.azure.rest.PluginConstants;
import com.gigaspaces.azure.util.CommandLineException;
import com.gigaspaces.azure.views.WindowsAzureActivityLogView;
import com.gigaspaces.azure.wizards.WizardCacheManager;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzurePackageType;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.interopbridges.tools.windowsazure.WindowsAzureRoleComponent;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.core.OperationStatus;
import com.microsoft.windowsazure.core.OperationStatusResponse;
import com.microsoft.windowsazure.management.compute.models.DeploymentGetResponse;
import com.microsoft.windowsazure.management.compute.models.DeploymentSlot;
import com.microsoft.windowsazure.management.compute.models.DeploymentStatus;
import com.microsoft.windowsazure.management.compute.models.HostedServiceCreateParameters;
import com.microsoft.windowsazure.management.compute.models.HostedServiceListResponse.HostedService;
import com.microsoft.windowsazure.management.compute.models.RoleInstance;
import com.microsoft.windowsazure.management.storage.models.StorageAccountCreateParameters;
import com.microsoftopentechnologies.azurecommons.deploy.DeploymentEventArgs;
import com.microsoftopentechnologies.azurecommons.deploy.DeploymentManagerUtilMethods;
import com.microsoftopentechnologies.azurecommons.deploy.model.CertificateUpload;
import com.microsoftopentechnologies.azurecommons.deploy.model.DeployDescriptor;
import com.microsoftopentechnologies.azurecommons.exception.DeploymentException;
import com.microsoftopentechnologies.azurecommons.exception.RestAPIException;
import com.microsoftopentechnologies.azuremanagementutil.model.InstanceStatus;
import com.microsoftopentechnologies.azuremanagementutil.model.Notifier;
import com.microsoftopentechnologies.azuremanagementutil.model.StorageService;
import com.microsoftopentechnologies.azuremanagementutil.rest.WindowsAzureRestUtils;
import com.microsoftopentechnologies.azuremanagementutil.rest.WindowsAzureServiceManagement;
import com.microsoftopentechnologies.azuremanagementutil.rest.WindowsAzureStorageServices;
import com.microsoftopentechnologies.azurecommons.storageregistry.StorageAccount;
import com.microsoftopentechnologies.azurecommons.storageregistry.StorageAccountRegistry;
import com.microsoftopentechnologies.wacommon.storageregistry.PreferenceUtilStrg;
import com.microsoftopentechnologies.wacommon.telemetry.AppInsightsCustomEvent;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
import com.microsoftopentechnologies.wacommon.utils.WACommonException;

public final class DeploymentManager {

	private final HashMap<String, DeployDescriptor> deployments = new HashMap<String, DeployDescriptor>();

	private static final DeploymentManager DEFAULT_MANAGER = new DeploymentManager();

	public static DeploymentManager getInstance() {
		return DEFAULT_MANAGER;
	}

	private DeploymentManager() {

	}

	public void addDeployment(String name, DeployDescriptor deployment) {
		deployments.put(name, deployment);
	}

	public void removeDeployment(String name) {
		deployments.remove(name);
	}

	public HashMap<String, DeployDescriptor> getDeployments() {
		return deployments;
	}

	public void deploy(IProject selectedProject) throws InterruptedException,
			DeploymentException {

		DeployDescriptor deploymentDesc = WizardCacheManager
				.collectConfiguration();

		String deployState = deploymentDesc.getDeployState();
		try {

			int conditionalProgress = 20;

			HostedService hostedService = deploymentDesc.getHostedService();
			addDeployment(hostedService.getServiceName(), deploymentDesc);

			StorageService storageAccount = deploymentDesc.getStorageAccount();

			WindowsAzureServiceManagement service = WizardCacheManager
					.createServiceManagementHelper();

			openWindowsAzureActivityLogView(deploymentDesc);

			if (deploymentDesc.getDeployMode() == WindowsAzurePackageType.LOCAL) {
				deployToLocalEmulator(selectedProject);
				notifyProgress(deploymentDesc.getDeploymentId(), null, 100,
						OperationStatus.Succeeded, Messages.deplCompleted);
				return;
			}
			// Publish start event
			AppInsightsCustomEvent.create(Messages.startEvent, "");

			// need to improve this check (maybe hostedSerivce.isExisting())?
			if (hostedService.getUri() == null
					|| hostedService.getUri().toString().isEmpty()) { 
				notifyProgress(deploymentDesc.getDeploymentId(), null, 5,
						OperationStatus.InProgress, String.format("%s - %s",
								Messages.createHostedService,
								hostedService.getServiceName()));
				createHostedService(hostedService.getServiceName(),
						hostedService.getServiceName(), hostedService
								.getProperties().getLocation(), hostedService
								.getProperties().getDescription());
				conditionalProgress -= 5;
			}

			// same goes here
			if (storageAccount.getUrl() == null
					|| storageAccount.getUrl().isEmpty()) { 
				notifyProgress(deploymentDesc.getDeploymentId(), null, 10,
						OperationStatus.InProgress, String.format("%s - %s",
								Messages.createStorageAccount,
								storageAccount.getServiceName()));
				createStorageAccount(storageAccount.getServiceName(),
						storageAccount.getServiceName(), storageAccount
								.getStorageAccountProperties().getLocation(),
						storageAccount.getStorageAccountProperties()
								.getDescription());
				conditionalProgress -= 10;
			}

			checkContainerExistance();

			// upload certificates
			if (deploymentDesc.getCertList() != null) {
				List<CertificateUpload> certList = deploymentDesc.getCertList()
						.getList();
				if (certList != null && certList.size() > 0) {
					for (int i = 0; i < certList.size(); i++) {
						CertificateUpload cert = certList.get(i);
						DeploymentManagerUtilMethods
								.uploadCertificateIfNeededGeneric(service,
										deploymentDesc, cert.getPfxPath(),
										cert.getPfxPwd());
						notifyProgress(deploymentDesc.getDeploymentId(), null,
								0, OperationStatus.InProgress, String.format(
										"%s%s", Messages.deplUploadCert,
										cert.getName()));
					}
				}
			}

			if (deploymentDesc.getRemoteDesktopDescriptor().isEnabled()) {

				notifyProgress(deploymentDesc.getDeploymentId(), null,
						conditionalProgress, OperationStatus.InProgress,
						Messages.deplConfigRdp);
				DeploymentManagerUtilMethods.configureRemoteDesktop(
						deploymentDesc,
						WizardCacheManager.getCurrentDeployConfigFile(),
						PluginUtil.getEncPath());
			} else {
				notifyProgress(deploymentDesc.getDeploymentId(), null,
						conditionalProgress, OperationStatus.InProgress,
						Messages.deplConfigRdp);
			}

			Notifier notifier = new NotifierImp();

			String targetCspckgName = createCspckTargetName(deploymentDesc);

			notifyProgress(deploymentDesc.getDeploymentId(), null, 20,
					OperationStatus.InProgress,
					Messages.uploadingServicePackage);

			DeploymentManagerUtilMethods.uploadPackageService(
					WizardCacheManager.createStorageServiceHelper(),
					deploymentDesc.getCspkgFile(), targetCspckgName,
					Messages.eclipseDeployContainer.toLowerCase(),
					deploymentDesc, notifier);

			notifyProgress(deploymentDesc.getDeploymentId(), null, 20,
					OperationStatus.InProgress, Messages.creatingDeployment);

			String storageAccountURL = deploymentDesc.getStorageAccount()
					.getStorageAccountProperties().getEndpoints().get(0)
					.toString();

			String cspkgUrl = String.format("%s%s/%s", storageAccountURL,
					Messages.eclipseDeployContainer.toLowerCase(),
					targetCspckgName);
			/*
			 * To make deployment name unique attach time stamp to the
			 * deployment name.
			 */
			DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
			String deploymentName = String.format("%s%s%s",
					hostedService.getServiceName(), deployState,
					dateFormat.format(new Date()));
			String requestId = DeploymentManagerUtilMethods.createDeployment(
					deploymentDesc, service, cspkgUrl, deploymentName);
			OperationStatus status = waitForStatus(
					deploymentDesc.getConfiguration(), service, requestId);

			DeploymentManagerUtilMethods.deletePackage(
					WizardCacheManager.createStorageServiceHelper(),
					Messages.eclipseDeployContainer.toLowerCase(),
					targetCspckgName, notifier);
			notifyProgress(deploymentDesc.getDeploymentId(), null, 0,
					OperationStatus.InProgress, Messages.deletePackage);

			notifyProgress(deploymentDesc.getDeploymentId(), null, 20,
					OperationStatus.InProgress, Messages.waitingForDeployment);

			DeploymentGetResponse deployment = waitForDeployment(
					deploymentDesc.getConfiguration(),
					hostedService.getServiceName(), deployState);

			boolean displayHttpsLink = deploymentDesc.getDisplayHttpsLink();
			WindowsAzureProjectManager waProjManager = WindowsAzureProjectManager
					.load(new File(selectedProject.getLocation().toOSString()));

			String serverAppName = null;
			for (WindowsAzureRole role : waProjManager.getRoles()) {
				if (role.getJDKSourcePath() != null
						&& role.getServerCloudName() != null) {
					List<WindowsAzureRoleComponent> serverAppComponents = role
							.getServerApplications();
					// Get first server app component
					if (serverAppComponents != null
							&& serverAppComponents.size() > 0) {
						String deployName = serverAppComponents.get(0)
								.getDeployName();
						serverAppName = deployName.substring(0,
								deployName.lastIndexOf("."));
						break;
					}
				}
			}

			String deploymentURL = displayHttpsLink ? deployment.getUri()
					.toString().replaceAll("http://", "https://") : deployment
					.getUri().toString();
		
			if (serverAppName != null) {
				if (!deploymentURL.endsWith("/")) {
					deploymentURL += "/";
				}
				deploymentURL += serverAppName + "/";
			}
			
			notifyProgress(deploymentDesc.getDeploymentId(), deploymentURL, 20,
					status, deployment.getStatus().toString());
			// publish success event
			AppInsightsCustomEvent.create(Messages.successEvent, "");

			// RDP prompt will come only on windows
			if (deploymentDesc.isStartRdpOnDeploy() && Activator.IS_WINDOWS) {
				// plugin folder
				String eclipseInstallation = Platform.getInstallLocation()
						.getURL().getPath();
				if (eclipseInstallation.charAt(0) == '/'
						|| eclipseInstallation.charAt(0) == '\\') {
					eclipseInstallation = eclipseInstallation.substring(1);
				}
				eclipseInstallation = eclipseInstallation.replace("/",
						File.separator);
				String pluginFolder = String.format("%s%s%s%s%s",
						eclipseInstallation, File.separator,
						com.persistent.util.Messages.pluginFolder,
						File.separator, com.persistent.util.Messages.pluginId);
				WindowsAzureRestUtils.getInstance().launchRDP(
						deployment,
						deploymentDesc.getRemoteDesktopDescriptor()
								.getUserName(), pluginFolder);
			}
		} catch (Throwable t) {
			boolean deploymentCancelled = false;
			String msg = (t != null ? t.getMessage() : "");
			if (msg.equalsIgnoreCase("sleep interrupted")
					|| msg.equalsIgnoreCase("java.lang.InterruptedException: sleep interrupted")
					|| msg.equalsIgnoreCase("java.lang.InterruptedException")
					|| msg.equalsIgnoreCase("Exception when create deployment")) {
				msg = "Deployment cancelled";
				deploymentCancelled = true;
			}
			if (!msg.startsWith(OperationStatus.Failed.toString())) {
				msg = OperationStatus.Failed.toString() + " : " + msg;
			}
			if (!deploymentCancelled) {
				// Publish failure event
				AppInsightsCustomEvent.create(Messages.failureEvent, "");
			}
			notifyProgress(deploymentDesc.getDeploymentId(), null, 100,
					OperationStatus.Failed, msg,
					deploymentDesc.getDeploymentId(), deployState);
			if (t instanceof DeploymentException) {
				throw (DeploymentException) t;
			}
			throw new DeploymentException(msg, t);
		}
	}

	private void createStorageAccount(final String storageServiceName,
			final String label, final String location, final String description)
			throws Exception {

		StorageAccountCreateParameters accountParameters = new StorageAccountCreateParameters();
		accountParameters.setName(storageServiceName);
		accountParameters.setLabel(label);
		accountParameters.setLocation(location);
		accountParameters.setDescription(description);

		StorageService storageService = WizardCacheManager
				.createStorageAccount(accountParameters);
		/*
		 * Add newly created storage account in centralized storage account
		 * registry.
		 */
		StorageAccount storageAccount = new StorageAccount(
				storageService.getServiceName(),
				storageService.getPrimaryKey(), storageService
						.getStorageAccountProperties().getEndpoints().get(0)
						.toString());
		StorageAccountRegistry.addAccount(storageAccount);
		PreferenceUtilStrg.save();
	}

	private void createHostedService(final String hostedServiceName,
			final String label, final String location, final String description)
			throws Exception {
		HostedServiceCreateParameters createHostedService = new HostedServiceCreateParameters();
		createHostedService.setServiceName(hostedServiceName);
		createHostedService.setLabel(label);
		createHostedService.setLocation(location);
		createHostedService.setDescription(description);

		WizardCacheManager.createHostedService(createHostedService);
	}

	private void checkContainerExistance() throws Exception {
		WindowsAzureStorageServices storageServices = WizardCacheManager
				.createStorageServiceHelper();
		storageServices.createContainer(Messages.eclipseDeployContainer
				.toLowerCase());
	}

	private DeploymentGetResponse waitForDeployment(
			Configuration configuration, String serviceName,
			String deployState)
					throws Exception {
		DeploymentGetResponse deployment = null;
		String status = null;
		DeploymentSlot deploymentSlot;
		if (DeploymentSlot.Staging.toString().equalsIgnoreCase(deployState)) {
			deploymentSlot = DeploymentSlot.Staging;
		} else if (DeploymentSlot.Production.toString().equalsIgnoreCase(deployState)) {
			deploymentSlot = DeploymentSlot.Production;
		} else {
			throw new Exception("Invalid deployment slot name");
		}
		// check role status
		do {
			Thread.sleep(5000);
			deployment = WindowsAzureRestUtils.getDeploymentBySlot(configuration, serviceName, deploymentSlot);

			for (RoleInstance instance : deployment.getRoleInstances()) {
				status = instance.getInstanceStatus();
				if (InstanceStatus.ReadyRole.getInstanceStatus().equals(status)
						|| InstanceStatus.CyclingRole.getInstanceStatus().equals(status)
						|| InstanceStatus.FailedStartingVM.getInstanceStatus().equals(status)
						|| InstanceStatus.UnresponsiveRole.getInstanceStatus().equals(status)) {
					break;
				}
			}
		} while (status != null
				&& !(InstanceStatus.ReadyRole.getInstanceStatus().equals(status)
						|| InstanceStatus.CyclingRole.getInstanceStatus().equals(status)
						|| InstanceStatus.FailedStartingVM.getInstanceStatus().equals(status)
						|| InstanceStatus.UnresponsiveRole.getInstanceStatus().equals(status)));

		if (!InstanceStatus.ReadyRole.getInstanceStatus().equals(status)) {
			throw new DeploymentException(status);
		}
		// check deployment status. And let Transitioning phase to finish
		DeploymentStatus deploymentStatus = null;
		do {
			Thread.sleep(10000);
			deployment = WindowsAzureRestUtils.getDeploymentBySlot(configuration, serviceName, deploymentSlot);
			deploymentStatus = deployment.getStatus();
		} while(deploymentStatus != null
				&& (deploymentStatus.equals(DeploymentStatus.RunningTransitioning)
						|| deploymentStatus.equals(DeploymentStatus.SuspendedTransitioning)));
		return deployment;
	}

	private OperationStatus waitForStatus(Configuration configuration,
			WindowsAzureServiceManagement service, String requestId)
			throws Exception {
		OperationStatusResponse op;
		OperationStatus status = null;
		do {
			op = service.getOperationStatus(configuration, requestId);
			status = op.getStatus();

			Activator.getDefault().log(Messages.deplId + op.getId());
			Activator.getDefault().log(Messages.deplStatus + op.getStatus());
			Activator.getDefault().log(
					Messages.deplHttpStatus + op.getHttpStatusCode());
			if (op.getError() != null) {
				Activator.getDefault().log(
						Messages.deplErrorMessage + op.getError().getMessage());
				throw new RestAPIException(op.getError().getMessage());
			}

			Thread.sleep(5000);

		} while (status == OperationStatus.InProgress);

		return status;
	}

	private String createCspckTargetName(DeployDescriptor deploymentDesc) {
		String cspkgName = String.format(Messages.cspkgName, deploymentDesc
				.getHostedService().getServiceName(), deploymentDesc
				.getDeployState());
		return cspkgName;
	}

	private void deployToLocalEmulator(IProject selectedProject) throws DeploymentException {

		WindowsAzureProjectManager waProjManager;
		try {
			waProjManager = WindowsAzureProjectManager.load(new File(
					selectedProject.getLocation().toOSString()));
			waProjManager.deployToEmulator();
		} catch (WindowsAzureInvalidProjectOperationException e) {
			throw new DeploymentException(e);
		}
	}

	public void notifyUploadProgress() {

	}

	public void notifyProgress(String deploymentId, String deploymentURL,
			int progress, OperationStatus inprogress, String message,
			Object... args) {

		DeploymentEventArgs arg = new DeploymentEventArgs(this);
		arg.setId(deploymentId);
		arg.setDeploymentURL(deploymentURL);
		arg.setDeployMessage(String.format(message, args));
		arg.setDeployCompleteness(progress);
		arg.setStartTime(new Date());
		arg.setStatus(inprogress);
		Activator.getDefault().fireDeploymentEvent(arg);
	}

	private void openWindowsAzureActivityLogView(
			final DeployDescriptor descriptor) {

		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				try {
					WindowsAzureActivityLogView waView = (WindowsAzureActivityLogView) PlatformUI
							.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage().showView(Messages.activityView);

					String desc = String.format(Messages.deplDesc, descriptor
							.getHostedService().getServiceName(), descriptor
							.getDeployState());

					waView.addDeployment(descriptor.getDeploymentId(), desc,
							descriptor.getStartTime());

				} catch (PartInitException e) {
					Activator.getDefault().log(Messages.deplCantOpenView, e);
				}

			}
		});
	}

	public void undeploy(final String serviceName, final String deplymentName,
			final String deploymentState) throws WACommonException,
			RestAPIException, InterruptedException, CommandLineException {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				try {
					WindowsAzureActivityLogView waView = (WindowsAzureActivityLogView) PlatformUI
							.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage().showView(Messages.activityView);

					String desc = String.format(Messages.undeployMsg,
							serviceName, deploymentState);

					waView.addDeployment(deplymentName, desc, new Date());

				} catch (PartInitException e) {
					Activator.getDefault().log(Messages.deplCantOpenView, e);
				}
			}
		});

		Configuration configuration = WizardCacheManager
				.getCurrentPublishData().getCurrentConfiguration();
		int[] progressArr = new int[] { 50, 50 };
		unPublish(configuration, serviceName, deplymentName, progressArr);
	}

	/**
	 * Unpublish deployment without notifying user.
	 * 
	 * @param configuration
	 * @param serviceName
	 * @param deplymentName
	 */
	public void unPublish(Configuration configuration, String serviceName,
			String deplymentName, int[] progressArr) {
		String requestId = null;

		int retryCount = 0;
		boolean successfull = false;
		while (!successfull) {
			try {
				retryCount++;
				WindowsAzureServiceManagement service = WizardCacheManager
						.createServiceManagementHelper();
				// Commenting suspend deployment call since it is giving issues
				// in china cloud.
				// notifyProgress(deplymentName, null, progressArr[0],
				// OperationStatus.InProgress,
				// Messages.stoppingMsg, serviceName);
				// requestId = service.updateDeploymentStatus(configuration,
				// serviceName,
				// deplymentName,
				// UpdatedDeploymentStatus.Suspended
				// );
				// waitForStatus(configuration, service, requestId);
				notifyProgress(deplymentName, null, progressArr[0],
						OperationStatus.InProgress,
						Messages.undeployProgressMsg, deplymentName);
				requestId = service.deleteDeployment(configuration,
						serviceName, deplymentName);
				waitForStatus(configuration, service, requestId);
				notifyProgress(deplymentName, null, progressArr[1],
						OperationStatus.Succeeded,
						Messages.undeployCompletedMsg, serviceName);
				successfull = true;
			} catch (Exception e) {
				// Retry 5 times
				if (retryCount > PluginConstants.REST_SERVICE_MAX_RETRY_COUNT) {
					Activator.getDefault().log(Messages.deplError, e);
					notifyProgress(deplymentName, null, 100,
							OperationStatus.Failed, e.getMessage(), serviceName);
				}
				notifyProgress(deplymentName, null, -progressArr[0],
						OperationStatus.InProgress,
						Messages.undeployProgressMsg, deplymentName);
			}
		}
	}
}
