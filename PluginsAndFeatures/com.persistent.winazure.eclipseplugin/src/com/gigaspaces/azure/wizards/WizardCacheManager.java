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
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import waeclipseplugin.Activator;

import com.gigaspaces.azure.runnable.CacheAccountWithProgressWindow;
import com.interopbridges.tools.windowsazure.WindowsAzurePackageType;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.management.compute.models.HostedServiceCreateParameters;
import com.microsoft.windowsazure.management.compute.models.HostedServiceGetDetailedResponse;
import com.microsoft.windowsazure.management.compute.models.HostedServiceListResponse.HostedService;
import com.microsoft.windowsazure.management.compute.models.ServiceCertificateListResponse.Certificate;
import com.microsoft.windowsazure.management.models.LocationsListResponse.Location;
import com.microsoft.windowsazure.management.storage.models.StorageAccountCreateParameters;
import com.microsoftopentechnologies.azurecommons.deploy.model.CertificateUpload;
import com.microsoftopentechnologies.azurecommons.deploy.model.CertificateUploadList;
import com.microsoftopentechnologies.azurecommons.deploy.model.DeployDescriptor;
import com.microsoftopentechnologies.azurecommons.deploy.model.RemoteDesktopDescriptor;
import com.microsoftopentechnologies.azurecommons.deploy.propertypages.CredentialsPropertyPageUtilMethods;
import com.microsoftopentechnologies.azurecommons.deploy.tasks.LoadingAccoutListener;
import com.microsoftopentechnologies.azurecommons.deploy.tasks.LoadingHostedServicesTask;
import com.microsoftopentechnologies.azurecommons.deploy.tasks.LoadingLocationsTask;
import com.microsoftopentechnologies.azurecommons.deploy.tasks.LoadingStorageAccountTask;
import com.microsoftopentechnologies.azurecommons.deploy.tasks.LoadingSubscriptionTask;
import com.microsoftopentechnologies.azurecommons.deploy.tasks.LoadingTaskRunner;
import com.microsoftopentechnologies.azurecommons.deploy.util.PublishData;
import com.microsoftopentechnologies.azurecommons.deploy.wizard.ConfigurationEventArgs;
import com.microsoftopentechnologies.azurecommons.deploy.wizard.ConfigurationEventListener;
import com.microsoftopentechnologies.azurecommons.deploy.wizard.WizardCacheManagerUtilMethods;
import com.microsoftopentechnologies.azurecommons.exception.RestAPIException;
import com.microsoftopentechnologies.azurecommons.util.WAEclipseHelperMethods;
import com.microsoftopentechnologies.azurecommons.wacommonutil.PreferenceSetUtil;
import com.microsoftopentechnologies.azuremanagementutil.model.KeyName;
import com.microsoftopentechnologies.azuremanagementutil.model.StorageService;
import com.microsoftopentechnologies.azuremanagementutil.model.StorageServices;
import com.microsoftopentechnologies.azuremanagementutil.model.Subscription;
import com.microsoftopentechnologies.azuremanagementutil.rest.WindowsAzureRestUtils;
import com.microsoftopentechnologies.azuremanagementutil.rest.WindowsAzureServiceManagement;
import com.microsoftopentechnologies.azuremanagementutil.rest.WindowsAzureStorageServices;
import com.persistent.util.MessageUtil;
import com.persistent.util.WAEclipseHelper;

public final class WizardCacheManager {

	private static final WizardCacheManager INSTANCE = new WizardCacheManager();

	private static final List<PublishData> PUBLISHS = new ArrayList<PublishData>();

	private static PublishData currentPublishData;
	private static KeyName currentAccessKey;
	private static String currentStorageService;
	private static String currentHostedService;
	private static String deployFile;
	private static String deployConfigFile;
	private static String deployState;
	private static WindowsAzurePackageType deployMode;
	private static String unpublish;
	private static RemoteDesktopDescriptor remoteDesktopDescriptor;
	private static CertificateUploadList certList;
	private static boolean displayHttpsLink = false;
	private static Map<String, String> publishSettingsPerSubscriptionMap = new HashMap<String, String>();
	private static Map<String, PublishData> pubDataPerFileMap = new HashMap<String, PublishData>();

	public static WizardCacheManager getInstrance() {
		return INSTANCE;
	}

	public static List<CertificateUpload> getList() {
		return certList.getList();
	}

	private WizardCacheManager() {

		WindowsAzurePage.addConfigurationEventListener(new ConfigurationEventListener() {

			@Override
			public void onConfigurationChanged(ConfigurationEventArgs config) {
				try {
					notifyConfiguration(config);
				} catch (RestAPIException e) {
					Activator.getDefault().log(Messages.error, e);
				}
			}
		});
	}

	public static DeployDescriptor collectConfiguration() {

		DeployDescriptor deployDescriptor = new DeployDescriptor(deployMode,
				currentPublishData.getCurrentSubscription().getId(),
				getCurrentStorageAcount(), currentAccessKey,
				getCurentHostedService(), deployFile, deployConfigFile,
				deployState, remoteDesktopDescriptor,
				checkSchemaVersionAndReturnUrl(),
				unpublish, certList, displayHttpsLink, currentPublishData.getCurrentConfiguration());

		remoteDesktopDescriptor = null;

		return deployDescriptor;
	}

	public static WindowsAzureStorageServices createStorageServiceHelper() {
		return WizardCacheManagerUtilMethods.createStorageServiceHelper(currentPublishData,
				currentStorageService, currentAccessKey);
	}

	public static WindowsAzureServiceManagement createServiceManagementHelper() {
		return WizardCacheManagerUtilMethods.createServiceManagementHelper(currentPublishData);
	}

	public static List<Location> getLocation() {
		return WizardCacheManagerUtilMethods.getLocation(currentPublishData);
	}

	public static String getCurrentDeplyFile() {
		return deployFile;
	}

	public static boolean getDisplayHttpsLink() {
		return displayHttpsLink;
	}


	public static String getCurrentDeployConfigFile() {
		return deployConfigFile;
	}

	public static String getCurrentDeplyState() {
		return deployState;
	}

	public static String getUnpublish() {
		return unpublish;
	}

	public static RemoteDesktopDescriptor getCurrentRemoteDesktopDescriptor() {
		return remoteDesktopDescriptor;
	}

	public static PublishData getCurrentPublishData() {
		return currentPublishData;
	}

	public static Collection<PublishData> getPublishDatas() {
		return PUBLISHS;
	}

	public static List<PublishData> getPublishDataList() {
		return PUBLISHS;
	}

	public static Subscription findSubscriptionByName(String subscriptionName) {
		return WizardCacheManagerUtilMethods.
				findSubscriptionByName(subscriptionName, PUBLISHS);
	}

	public static PublishData findPublishDataBySubscriptionId(String subscriptionId) {
		return WizardCacheManagerUtilMethods.
				findPublishDataBySubscriptionId(subscriptionId, PUBLISHS);
	}

	public static String findSubscriptionNameBySubscriptionId(String subscriptionId) {
		return WizardCacheManagerUtilMethods.
				findSubscriptionNameBySubscriptionId(subscriptionId, PUBLISHS);
	}

	public static void removeSubscription(String subscriptionId) {

		if (subscriptionId == null) {
			return;
		}

		PublishData publishData = findPublishDataBySubscriptionId(subscriptionId);

		if (publishData == null) {
			return;
		}

		List<Subscription> subs = publishData.getPublishProfile().getSubscriptions();
		int index = WizardCacheManagerUtilMethods.
				getIndexOfPublishData(subscriptionId, PUBLISHS);

		for (int i = 0; i < subs.size(); i++) {
			Subscription s = subs.get(i);

			if (s.getSubscriptionID().equals(subscriptionId)) {
				publishData.getPublishProfile().getSubscriptions().remove(i);
				PUBLISHS.set(index, publishData);
				if (publishData.getPublishProfile().getSubscriptions().size() == 0) {
					PUBLISHS.remove(publishData);
					/*
					 * If all subscriptions are removed
					 * set current subscription to null.
					 */
					setCurrentPublishData(null);
				}
				break;
			}
		}
	}

	public static void changeCurrentSubscription(PublishData publishData, String subscriptionId) {
		WizardCacheManagerUtilMethods.
		changeCurrentSubscription(publishData, subscriptionId);
	}

	public static StorageService getCurrentStorageAcount() {
		return WizardCacheManagerUtilMethods.
				getCurrentStorageAcount(currentPublishData, currentStorageService);
	}

	public static HostedService getCurentHostedService() {
		return WizardCacheManagerUtilMethods.
				getCurentHostedService(currentPublishData, currentHostedService);
	}

	public static HostedService getHostedServiceFromCurrentPublishData(final String hostedServiceName) {
		return WizardCacheManagerUtilMethods.
				getHostedServiceFromCurrentPublishData(hostedServiceName,
						currentPublishData);
	}

	/**
	 * Method uses REST API and returns already uploaded certificates
	 * from currently selected cloud service on wizard.
	 * @return
	 */
	public static List<Certificate> fetchUploadedCertificates() {
		return WizardCacheManagerUtilMethods.
				fetchUploadedCertificates(currentPublishData, currentHostedService);
	}

	public static HostedService createHostedService(HostedServiceCreateParameters createHostedService)
			throws Exception {
		HostedService hostedService = WizardCacheManagerUtilMethods.
				createHostedService(createHostedService, currentPublishData);
		currentPublishData.getServicesPerSubscription().get(
				currentPublishData.getCurrentSubscription().getId()).add(hostedService);
		return hostedService;
	}

	public static StorageService createStorageAccount(StorageAccountCreateParameters accountParameters)
			throws Exception {
		Subscription subscription = currentPublishData.getCurrentSubscription();
		String prefFilePath = WAEclipseHelper.getTemplateFile(com.persistent.ui.preference.Messages.prefFileName);
		StorageService storageAccount = WizardCacheManagerUtilMethods.
				createStorageAccount(accountParameters, currentPublishData, prefFilePath);
		// remove previous mock if existed
		currentPublishData.getStoragesPerSubscription().get(subscription.getId()).remove(accountParameters.getName());
		currentPublishData.getStoragesPerSubscription().get(subscription.getId()).add(storageAccount);
		return storageAccount;
	}

	public static boolean isHostedServiceNameAvailable(final String hostedServiceName)
			throws Exception {
		return WizardCacheManagerUtilMethods.
				isHostedServiceNameAvailable(hostedServiceName, currentPublishData);
	}

	public static boolean isStorageAccountNameAvailable(final String storageAccountName)
			throws Exception {
		return WizardCacheManagerUtilMethods.
				isStorageAccountNameAvailable(storageAccountName, currentPublishData);
	}

	public static StorageService createStorageServiceMock(String storageAccountNameToCreate,
			String storageAccountLocation, String description) {
		StorageService storageService = WizardCacheManagerUtilMethods.createStorageServiceMock(
				storageAccountNameToCreate, storageAccountLocation, description);

		currentPublishData.getStoragesPerSubscription().get(
				currentPublishData.getCurrentSubscription().getId()).
				add(storageService);
		return storageService;
	}

	public static HostedService createHostedServiceMock(String hostedServiceNameToCreate,
			String hostedServiceLocation, String description) {
		Subscription subscription = currentPublishData.getCurrentSubscription();
		HostedService hostedService = WizardCacheManagerUtilMethods.
				createHostedServiceMock(hostedServiceNameToCreate,
						hostedServiceLocation, description);
		currentPublishData.getServicesPerSubscription().get(subscription.getId()).add(hostedService);
		return hostedService;
	}

	public static List<HostedService> getHostedServices() {
		return WizardCacheManagerUtilMethods.
				getHostedServices(currentPublishData);
	}

	public static IProject getCurrentSelectedProject() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		ISelectionService service = window.getSelectionService();
		ISelection selection = service.getSelection();

		Object element = null;
		IProject selProject = null;

		try {

			if (selection instanceof IStructuredSelection) {
				IStructuredSelection structuredSel = (IStructuredSelection) selection;
				element = structuredSel.getFirstElement();
			}
			IResource resource;

			if (element instanceof IResource) {
				resource = (IResource) element;
				selProject = resource.getProject();
			} else {
				IWorkbenchPage page = window.getActivePage();
				IFile file = (IFile) page.getActiveEditor().getEditorInput()
						.getAdapter(IFile.class);
				selProject = file.getProject();
			}
		}
		catch (Exception ex) {
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					String errorTitle = "Error while retrieving project"; //$NON-NLS-1$
					String errorMessage = "Error occurred while loading project. Try the operation after re-selecting the project."; //$NON-NLS-1$
					MessageUtil.displayErrorDialog(new Shell(), errorTitle,
							errorMessage);					
				}
			});
			return null;
		}

		return selProject;
	}

	private void notifyConfiguration(ConfigurationEventArgs config) throws RestAPIException {
		if (ConfigurationEventArgs.DEPLOY_FILE.equals(config.getKey())) {
			deployFile = config.getValue().toString();
		}
		else if (ConfigurationEventArgs.DEPLOY_CONFIG_FILE.equals(config.getKey())) {
			deployConfigFile = config.getValue().toString();
		}
		else if (ConfigurationEventArgs.DEPLOY_STATE.equals(config.getKey())) {
			deployState = config.getValue().toString();
		}
		else if (ConfigurationEventArgs.SUBSCRIPTION.equals(config.getKey())) {
			PublishData publishData = (PublishData) config.getValue();
			if (publishData.isInitialized() == false && publishData.isInitializing().compareAndSet(false, true)) {
				CacheAccountWithProgressWindow settings = new CacheAccountWithProgressWindow(null, publishData, Display.getDefault().getActiveShell(), null);
				Display.getDefault().syncExec(settings);				
			}
		}
		else if (ConfigurationEventArgs.HOSTED_SERVICE.equals(config.getKey())) {
			HostedService hostedService = (HostedService) config.getValue();
			if (hostedService != null)
				currentHostedService = hostedService.getServiceName();
		}
		else if (ConfigurationEventArgs.STORAGE_ACCOUNT.equals(config.getKey())) {

			StorageService storageService = (StorageService) config.getValue();

			if (storageService != null) {
				currentStorageService = storageService.getServiceName();
			}
		}
		else if (ConfigurationEventArgs.REMOTE_DESKTOP .equals(config.getKey())) {
			remoteDesktopDescriptor = (RemoteDesktopDescriptor) config.getValue();
		}
		else if (ConfigurationEventArgs.CERTIFICATES.equals(config.getKey())) {
			certList = (CertificateUploadList) config.getValue();
		}
		else if (ConfigurationEventArgs.DEPLOY_MODE.equals(config.getKey())) {
			deployMode = (WindowsAzurePackageType) config.getValue();
		}
		else if (ConfigurationEventArgs.UN_PUBLISH.equals(config.getKey())) {
			unpublish = config.getValue().toString();
		}
		else if (ConfigurationEventArgs.STORAGE_ACCESS_KEY.equals(config.getKey())) {
			String value = config.getValue().toString();

			if (value != null && !value.isEmpty()) {
				currentAccessKey = KeyName.valueOf(value);
			} else {
				currentAccessKey = KeyName.Primary;
			}
		}
		else if (ConfigurationEventArgs.CONFIG_HTTPS_LINK.equals(config.getKey())) {
			String value = config.getValue().toString();

			if (value != null && !value.isEmpty()) {
				displayHttpsLink = Boolean.parseBoolean(value.trim());
			}
		}
	}

	public static HostedServiceGetDetailedResponse getHostedServiceWithDeployments(String hostedService)
			throws Exception {
		return WizardCacheManagerUtilMethods.
				getHostedServiceWithDeployments(hostedService, currentPublishData);
	}

	public static void setCurrentPublishData(PublishData currentSubscription2) {
		currentPublishData = currentSubscription2;
	}

	public static void cachePublishData(File publishSettingsFile, PublishData publishData, LoadingAccoutListener listener)
			throws RestAPIException, IOException {

		boolean canceled = false;
		List<Subscription> subscriptions = null;
		int OPERATIONS_TIMEOUT = 60 * 5;

		if (publishData == null) {
			return;
		} else {
			subscriptions = publishData.getPublishProfile().getSubscriptions();
		}

		if (subscriptions == null) {
			return;
		}
		String schemaVer = publishData.getPublishProfile().getSchemaVersion();
		boolean isNewSchema = schemaVer != null && !schemaVer.isEmpty() && schemaVer.equalsIgnoreCase("2.0");
		// URL if schema version is 1.0
		String url = publishData.getPublishProfile().getUrl();
		String prefFilePath = WAEclipseHelper.getTemplateFile(
				com.persistent.ui.preference.Messages.prefFileName);
		Map<String, Configuration> configurationPerSubscription = new HashMap<String, Configuration>();
		for (Subscription subscription : subscriptions) {
			if (isNewSchema) {
				// publish setting file is of schema version 2.0
				url = subscription.getServiceManagementUrl();
			}
			if (url == null || url.isEmpty()) {
				try {
					url = PreferenceSetUtil.getManagementURL(
							PreferenceSetUtil.getSelectedPreferenceSetName(prefFilePath),
							prefFilePath);
					url = url.substring(0, url.lastIndexOf("/"));
				} catch (Exception e) {
					Activator.getDefault().log(e.getMessage());
				}
			}
			Configuration configuration = (publishSettingsFile == null) ?
					WindowsAzureRestUtils.loadConfiguration(subscription.getId(), url) :
						WindowsAzureRestUtils.getConfiguration(publishSettingsFile, subscription.getId());
					configurationPerSubscription.put(subscription.getId(), configuration);

					if (publishSettingsFile != null) {
						//copy file to user home
						String outFile = System.getProperty("user.home") + File.separator + ".azure" + File.separator + publishSettingsFile.getName();
						try {
							/*
							 * copy file to user home only if
							 * source and destination file path are not same
							 * otherwise it results into invalid file of 0 KB
							 */
							if (!publishSettingsFile.getPath().equals(outFile)) {
								WAEclipseHelperMethods.copyFile(publishSettingsFile, new File(outFile));
							}
							// put an entry into global cache - subId, File path
							publishSettingsPerSubscriptionMap.put(subscription.getId(), outFile);
						} catch (Exception e) {
							// Ignore error
							e.printStackTrace();
						}
					} else {
						// subscription added via Add functionality
						publishSettingsPerSubscriptionMap.put(subscription.getId(), "");
					}
		}
		publishData.setConfigurationPerSubscription(configurationPerSubscription);

		if (publishData.isInitialized() == false) {
//				&& publishData.isInitializing().compareAndSet(false, true)) {

			List<Future<?>> loadServicesFutures = null;
			Future<?> loadSubscriptionsFuture = null;
			try {
				List<Subscription> subBackup = publishData.getPublishProfile().getSubscriptions();

				// thread pool size is number of subscriptions
				ScheduledExecutorService subscriptionThreadPool = Executors.newScheduledThreadPool(subscriptions.size());

				LoadingSubscriptionTask loadingSubscriptionTask = new LoadingSubscriptionTask(publishData);
				loadingSubscriptionTask.setSubscriptionIds(subscriptions);
				if (listener != null) {
					loadingSubscriptionTask.addLoadingAccountListener(listener);
				}

				loadSubscriptionsFuture = subscriptionThreadPool.submit(new LoadingTaskRunner(loadingSubscriptionTask));				
				loadSubscriptionsFuture.get(OPERATIONS_TIMEOUT, TimeUnit.SECONDS);

				/*
				 * add explicitly management URL and certificate which was removed
				 * Changes are did to support both publish setting schema versions.
				 */
				if (isNewSchema) {
					for (int i = 0; i < subBackup.size(); i++) {
						publishData.getPublishProfile().getSubscriptions().get(i).
						setServiceManagementUrl(subBackup.get(i).getServiceManagementUrl());
						publishData.getPublishProfile().getSubscriptions().get(i).
						setManagementCertificate(subBackup.get(i).getManagementCertificate());
					}
				}

				if (publishData.getCurrentSubscription() == null && publishData.getPublishProfile().getSubscriptions().size() > 0) {
					publishData.setCurrentSubscription(publishData.getPublishProfile().getSubscriptions().get(0));
				}

				// thread pool size is 3 to load hosted services, locations and storage accounts.
				ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(3);
				loadServicesFutures = new ArrayList<Future<?>>();

				// Hosted services
				LoadingHostedServicesTask loadingHostedServicesTask = new LoadingHostedServicesTask(publishData);
				if (listener != null) {
					loadingHostedServicesTask.addLoadingAccountListener(listener);
				}
				Future<?> submitHostedServices = threadPool.submit(new LoadingTaskRunner(loadingHostedServicesTask));
				loadServicesFutures.add(submitHostedServices);

				// locations
				LoadingLocationsTask loadingLocationsTask = new LoadingLocationsTask(publishData);
				if (listener != null) {
					loadingLocationsTask.addLoadingAccountListener(listener);
				}
				Future<?> submitLocations = threadPool.submit(new LoadingTaskRunner(loadingLocationsTask));
				loadServicesFutures.add(submitLocations);

				// storage accounts
				LoadingStorageAccountTask loadingStorageAccountTask = new LoadingStorageAccountTask(publishData);
				if (listener != null) {
					loadingStorageAccountTask.addLoadingAccountListener(listener);
				}
				Future<?> submitStorageAccounts = threadPool.submit(new LoadingTaskRunner(loadingStorageAccountTask));
				loadServicesFutures.add(submitStorageAccounts);

				for (Future<?> future : loadServicesFutures) {
					future.get(OPERATIONS_TIMEOUT, TimeUnit.SECONDS);
				}

				try {
					String chinaMngmntUrl = PreferenceSetUtil.getManagementURL("windowsazure.cn (China)", prefFilePath);
					chinaMngmntUrl = chinaMngmntUrl.substring(0, chinaMngmntUrl.lastIndexOf("/"));
					if (url.equals(chinaMngmntUrl)) {
						for (Subscription sub : publishData.getPublishProfile().getSubscriptions()) {
							/*
							 * Get collection of storage services in each subscription.
							 */
							StorageServices services = publishData.getStoragesPerSubscription().get(sub.getId());
							for (StorageService strgService : services) {
								List<URI> endpoints = strgService.getStorageAccountProperties().getEndpoints();
								for (int i = 0; i < endpoints.size(); i++) {
									String uri = endpoints.get(i).toString();
									if (uri.startsWith("https://")) {
										endpoints.set(i, URI.create(uri.replaceFirst("https://", "http://")));
									}
								}
							}
						}
					}
				} catch (Exception e) {
					Activator.getDefault().log(e.getMessage());
				}
			}
			catch (InterruptedException e) {
				if (loadSubscriptionsFuture != null) {
					loadSubscriptionsFuture.cancel(true);
				}
				if (loadServicesFutures != null) {
					for (Future<?> future : loadServicesFutures) {
						future.cancel(true);
					}
				}
				canceled = true;
			}
			catch (ExecutionException e) {
			}
			catch (TimeoutException e) {
			}
		}


		if (publishData.getPublishProfile().getSubscriptions().size() > 0) {
			if (!empty(publishData) && !canceled) {
				removeDuplicateSubscriptions(publishData);
				PUBLISHS.add(publishData);
				publishData.isInitializing().compareAndSet(true, false);
				currentPublishData = publishData;
			}
		}
	}

	private static void removeDuplicateSubscriptions(PublishData publishData) {

		Set<String> subscriptionIdsToRemove = new HashSet<String>();

		List<Subscription> subscriptionsOfPublishDataToCache = publishData.getPublishProfile().getSubscriptions();
		for (Subscription subscriptionOfPublishDataToCache : subscriptionsOfPublishDataToCache) {
			for (PublishData pd : PUBLISHS) {
				for (Subscription existingSubscription : pd.getPublishProfile().getSubscriptions()) {
					if (existingSubscription.getId().equals(subscriptionOfPublishDataToCache.getId())) {
						subscriptionIdsToRemove.add(existingSubscription.getId());
					}
				}
			}
		}

		for (String subscriptionId : subscriptionIdsToRemove) {
			removeSubscription(subscriptionId);
		}

		List<PublishData> emptyPublishDatas = new ArrayList<PublishData>();
		for (PublishData pd : PUBLISHS) {
			if (pd.getPublishProfile().getSubscriptions().isEmpty()) {
				emptyPublishDatas.add(pd);
			}
		}

		for (PublishData emptyData : emptyPublishDatas) {
			PUBLISHS.remove(emptyData);
		}
	}

	private static boolean empty(PublishData data) {
		return WizardCacheManagerUtilMethods.empty(data);
	}

	public static StorageService getStorageAccountFromCurrentPublishData(String storageAccountName) {
		return WizardCacheManagerUtilMethods.
				getStorageAccountFromCurrentPublishData(storageAccountName, currentPublishData);
	}

	private static String checkSchemaVersionAndReturnUrl() {
		return WizardCacheManagerUtilMethods.checkSchemaVersionAndReturnUrl(currentPublishData);
	}

	public static String getPublishSettingsPath(String subscriptionID) {
		return publishSettingsPerSubscriptionMap.get(subscriptionID);
	}

	public static Map<String, String> getPublishSettingsPerSubscription() {
		return publishSettingsPerSubscriptionMap;
	}

	public static void addPublishSettingsPerSubscription(Map<String, String> publishSettingsPerSubscription) {
		publishSettingsPerSubscriptionMap.putAll(publishSettingsPerSubscription);
	}

	public static void preparePubDataPerFileMap() {
		String emptyKeyStr = "empty";
		int cnt = 1;
		for (Map.Entry<String, String> entry : getPublishSettingsPerSubscription().entrySet()) {
			// Key - SubId and Value - pubFile
			String pubFilePath = entry.getValue();
			String subId = entry.getKey();
			if (pubFilePath.isEmpty() || !pubDataPerFileMap.containsKey(pubFilePath)) {
				if (pubFilePath.isEmpty()) {
					pubFilePath = emptyKeyStr + cnt;
					cnt++;
				}
				pubDataPerFileMap.put(pubFilePath,
						CredentialsPropertyPageUtilMethods.createPublishData(subId));
			} else {
				// Prepare subscription and put into existing publish data
				Subscription s = new Subscription();
				s.setSubscriptionID(subId);
				PublishData data = pubDataPerFileMap.get(pubFilePath);
				if (!data.getSubscriptionIds().contains(subId)) {
					data.getPublishProfile().getSubscriptions().add(s);
				}
			}
		}
	}

	public static Map<String, PublishData> getPubDataPerFileMap() {
		return pubDataPerFileMap;
	}
}
