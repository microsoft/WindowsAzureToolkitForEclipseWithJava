/*******************************************************************************
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
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

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

import com.gigaspaces.azure.deploy.RequestStatus;
import com.gigaspaces.azure.model.CreateHostedService;
import com.gigaspaces.azure.model.CreateStorageServiceInput;
import com.gigaspaces.azure.model.DeployDescriptor;
import com.gigaspaces.azure.model.HostedService;
import com.gigaspaces.azure.model.HostedServices;
import com.gigaspaces.azure.model.KeyName;
import com.gigaspaces.azure.model.Locations;
import com.gigaspaces.azure.model.Operation;
import com.gigaspaces.azure.model.RemoteDesktopDescriptor;
import com.gigaspaces.azure.model.StorageService;
import com.gigaspaces.azure.model.StorageServices;
import com.gigaspaces.azure.model.Subscription;
import com.gigaspaces.azure.rest.InvalidThumbprintException;
import com.gigaspaces.azure.rest.RestAPIException;
import com.gigaspaces.azure.rest.WindowsAzureServiceManagement;
import com.gigaspaces.azure.rest.WindowsAzureStorageServices;
import com.gigaspaces.azure.runnable.CacheAccountWithProgressWindow;
import com.gigaspaces.azure.tasks.LoadingAccoutListener;
import com.gigaspaces.azure.tasks.LoadingHostedServicesTask;
import com.gigaspaces.azure.tasks.LoadingLocationsTask;
import com.gigaspaces.azure.tasks.LoadingStorageAccountTask;
import com.gigaspaces.azure.tasks.LoadingSubscriptionTask;
import com.gigaspaces.azure.tasks.LoadingTaskRunner;
import com.gigaspaces.azure.util.CommandLineException;
import com.gigaspaces.azure.util.PublishData;
import com.interopbridges.tools.windowsazure.WindowsAzurePackageType;
import com.persistent.util.MessageUtil;

public class WizardCacheManager {

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

	private static RemoteDesktopDescriptor remoteDesktopDescriptor;

	public static WizardCacheManager getInstrance() {
		return INSTANCE;
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
				deployState, remoteDesktopDescriptor);

		remoteDesktopDescriptor = null;

		return deployDescriptor;
	}

	public static WindowsAzureStorageServices createStorageServiceHelper() {

		if (currentPublishData != null) {
			StorageService storageService = getCurrentStorageAcount();

			try {
				String key = ""; //$NON-NLS-1$
				if (currentAccessKey == KeyName.Primary)
					key = storageService.getStorageServiceKeys().getPrimary();
				else
					key = storageService.getStorageServiceKeys().getSecondary();

				return new WindowsAzureStorageServices(
						storageService.getServiceName(), key);
			} catch (InvalidKeyException e) {
				Activator.getDefault().log(Messages.error, e);
			} catch (NoSuchAlgorithmException e) {
				Activator.getDefault().log(Messages.error, e);
			}
		}
		return null;
	}

	public static WindowsAzureServiceManagement createServiceManagementHelper() {
		if (currentPublishData != null) {
			try {
				return new WindowsAzureServiceManagement(
						currentPublishData.getThumbprint());
			} catch (InvalidThumbprintException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return null;
	}

	public static Locations getLocation() {

		if (currentPublishData != null)
			return currentPublishData.getLocationsPerSubscription().get(
					currentPublishData.getCurrentSubscription().getId());

		return null;
	}

	public static String getCurrentDeplyFile() {
		return deployFile;
	}

	public static String getCurrentDeployConfigFile() {
		return deployConfigFile;
	}

	public static String getCurrentDeplyState() {
		return deployState;
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

	public static Subscription findSubscriptionByName(String subscriptionName) {

		for (PublishData pd : PUBLISHS) {
			List<Subscription> subscriptions = pd.getPublishProfile()
					.getSubscriptions();
			for (Subscription sub : subscriptions) {
				if (sub.getName().equals(subscriptionName))
					return sub;
			}
		}

		return null;
	}

	public static PublishData findPublishDataByThumbprint(String thumbprint) {

		for (PublishData pd : PUBLISHS) {

			if (thumbprint.equalsIgnoreCase(pd.getThumbprint()))
				return pd;

		}
		return null;
	}

	public static void removeSubscription(String subscriptionId, String thumbprint) {

		if (subscriptionId == null || thumbprint == null) {
			return;
		}

		PublishData publishData = findPublishDataByThumbprint(thumbprint);

		if (publishData == null) {
			return;
		}

		List<Subscription> subs = publishData.getPublishProfile().getSubscriptions();

		for (int i = 0; i < subs.size(); i++) {
			Subscription s = subs.get(i);

			if (s.getSubscriptionID().equals(subscriptionId)) {

				publishData.getPublishProfile().getSubscriptions().remove(i);
				if (publishData.getPublishProfile().getSubscriptions().size() == 0) {
					PUBLISHS.remove(publishData);
				}

				break;
			}
		}
	}

	public static void changeCurrentSubscription(PublishData publishData, String subscriptionId) {
		if (publishData == null || subscriptionId == null) {
			return;
		}

		List<Subscription> subs = publishData.getPublishProfile().getSubscriptions();

		for (int i = 0; i < subs.size(); i++) {
			Subscription s = subs.get(i);

			if (s.getSubscriptionID().equals(subscriptionId)) {
				publishData.setCurrentSubscription(s);
				break;
			}
		}
	}

	public static StorageService getCurrentStorageAcount() {

		if (currentPublishData != null && (currentStorageService != null && !currentStorageService.isEmpty())) {

			for (StorageService storageService : currentPublishData
					.getStoragesPerSubscription()
					.get(currentPublishData.getCurrentSubscription().getId())) {
				if (storageService.getServiceName().equalsIgnoreCase(
						currentStorageService))
					return storageService;
			}

		}

		return null;

	}

	public static HostedService getCurentHostedService() {
		if (currentPublishData != null
				&& (currentHostedService != null && !currentHostedService
				.isEmpty())) {
			String subsId = currentPublishData.getCurrentSubscription().getId();

			for (HostedService hostedService : currentPublishData
					.getServicesPerSubscription().get(subsId)) {
				if (hostedService.getServiceName().equalsIgnoreCase(
						currentHostedService))
					return hostedService;
			}
		}

		return null;
	}

	public static void createHostedService(CreateHostedService body)
			throws RestAPIException, InterruptedException, CommandLineException {

		WindowsAzureServiceManagement service;
		Subscription subscription = currentPublishData.getCurrentSubscription();

		try {
			service = new WindowsAzureServiceManagement(currentPublishData.getThumbprint());

			String subscriptionId = subscription.getId();
			service.createHostedService(subscriptionId, body);
			HostedService hostedServiceProperties = service.getHostedServiceWithProperties(subscriptionId, body.getServiceName());
			currentPublishData.getServicesPerSubscription().get(subscriptionId).add(hostedServiceProperties);
		} 
		catch (InvalidThumbprintException e) {
		}
	}
	
	public static void createStorageAccount(CreateStorageServiceInput body) throws RestAPIException, InterruptedException, CommandLineException {

		WindowsAzureServiceManagement service;
		Subscription subscription = currentPublishData.getCurrentSubscription();

		try {
			service = new WindowsAzureServiceManagement(currentPublishData.getThumbprint());
			
			String requestId = service.createStorageAccount(subscription.getId(), body);
			
			waitForStatus(subscription.getId(), service, requestId);
			
			StorageService storageAccount = service.getStorageAccount(subscription.getId(), body.getServiceName());
			currentPublishData.getStoragesPerSubscription().get(subscription.getId()).add(storageAccount);	
		} 
		catch (InvalidThumbprintException e) {
		}
	}
	
	private static RequestStatus waitForStatus(String subscriptionId,WindowsAzureServiceManagement service, String requestId) throws InterruptedException, RestAPIException, CommandLineException {
		Operation op;
		RequestStatus status = null;
		do {
			op = service.getOperationStatus(subscriptionId, requestId);
			status = RequestStatus.valueOf(op.getStatus());

			if (op.getError() != null) {
				throw new RestAPIException(op.getError().getCode().toString(), op.getError().getMessage());
			}

			Thread.sleep(5000);

		} while (status == RequestStatus.InProgress);
		return status;
	}

	public static HostedServices getHostedServices() {

		if (currentPublishData == null)
			return null;

		String subbscriptionId = currentPublishData.getCurrentSubscription().getId();

		return currentPublishData.getServicesPerSubscription().get(subbscriptionId);
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
		else if (ConfigurationEventArgs.SUPSCRIPTION.equals(config.getKey())) {
			PublishData publishData = (PublishData) config.getValue();
			if (publishData.isInitialized() == false && publishData.isInitializing().compareAndSet(false, true)) {
				CacheAccountWithProgressWindow settings = new CacheAccountWithProgressWindow(publishData, Display.getDefault().getActiveShell(), null);
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
		else if (ConfigurationEventArgs.DEPLOY_MODE.equals(config.getKey())) {
			deployMode = (WindowsAzurePackageType) config.getValue();
		} 
		else if (ConfigurationEventArgs.STORAGE_ACCESS_KEY.equals(config.getKey())) {
			String value = config.getValue().toString();

			if (value != null && !value.isEmpty()) {
				currentAccessKey = KeyName.valueOf(value);
			} else {
				currentAccessKey = KeyName.Primary;
			}
		}
	}

	public static HostedService getHostedServiceWithDeployments(String hostedService)
			throws RestAPIException, InterruptedException, CommandLineException, InvalidThumbprintException {

		String subscriptionId = currentPublishData.getCurrentSubscription()
				.getId();

		WindowsAzureServiceManagement service = null;
		service = new WindowsAzureServiceManagement(currentPublishData.getThumbprint());	
		return service.getHostedServiceWithProperties(subscriptionId, hostedService);			
	}

	public static void setCurrentPublishData(PublishData currentSubscription2) {
		currentPublishData = currentSubscription2;
	}

	public static void cachePublishData(PublishData publishData, LoadingAccoutListener listener) throws RestAPIException {

		boolean canceled = false;

		int OPERATIONS_TIMEOUT = 60 * 5;

		List<Subscription> subscriptions = publishData.getPublishProfile().getSubscriptions();

		if (subscriptions == null) {
			return;
		}

		if (publishData.isInitialized() == false && publishData.isInitializing().compareAndSet(false, true)) {

			List<Future<?>> loadServicesFutures = null;
			Future<?> loadSubscriptionsFuture = null;
			try {				
				ScheduledExecutorService subscriptionThreadPool = Executors.newScheduledThreadPool(subscriptions.size());
				LoadingSubscriptionTask loadingSubscriptionTask = new LoadingSubscriptionTask(publishData);
				loadingSubscriptionTask.setSubscriptionIds(subscriptions);
				if (listener != null) {
					loadingSubscriptionTask.addLoadingAccountListener(listener);
				}

				loadSubscriptionsFuture = subscriptionThreadPool.submit(new LoadingTaskRunner(loadingSubscriptionTask));				
				loadSubscriptionsFuture.get(OPERATIONS_TIMEOUT, TimeUnit.SECONDS);

				if (publishData.getCurrentSubscription() == null && publishData.getPublishProfile().getSubscriptions().size() > 0) {
					publishData.setCurrentSubscription(publishData.getPublishProfile().getSubscriptions().get(0));
				}

				ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(3);
				loadServicesFutures = new ArrayList<Future<?>>();


				LoadingHostedServicesTask loadingHostedServicesTask = new LoadingHostedServicesTask(publishData);
				if (listener != null) {
					loadingHostedServicesTask.addLoadingAccountListener(listener);
				}
				Future<?> submitHostedServices = threadPool.submit(new LoadingTaskRunner(loadingHostedServicesTask));
				loadServicesFutures.add(submitHostedServices);

				LoadingLocationsTask loadingLocationsTask = new LoadingLocationsTask(publishData);
				if (listener != null) {
					loadingLocationsTask.addLoadingAccountListener(listener);
				}
				Future<?> submitLocations = threadPool.submit(new LoadingTaskRunner(loadingLocationsTask));
				loadServicesFutures.add(submitLocations);

				LoadingStorageAccountTask loadingStorageAccountTask = new LoadingStorageAccountTask(publishData);
				if (listener != null) {
					loadingStorageAccountTask.addLoadingAccountListener(listener);
				}
				Future<?> submitStorageAccounts = threadPool.submit(new LoadingTaskRunner(loadingStorageAccountTask));
				loadServicesFutures.add(submitStorageAccounts);
			
				for (Future<?> future : loadServicesFutures) {
					future.get(OPERATIONS_TIMEOUT, TimeUnit.SECONDS);
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
		
		Map<String, String> subscriptionIdsToRemove = new HashMap<String, String>();
		
		List<Subscription> subscriptionsOfPublishDataToCache = publishData.getPublishProfile().getSubscriptions();
		for (Subscription subscriptionOfPublishDataToCache : subscriptionsOfPublishDataToCache) {
			for (PublishData pd : PUBLISHS) {
				for (Subscription existingSubscription : pd.getPublishProfile().getSubscriptions()) {
					if (existingSubscription.getId().equals(subscriptionOfPublishDataToCache.getId())) {
						subscriptionIdsToRemove.put(existingSubscription.getId(), pd.getThumbprint());
					}
				}
			}
		}
		
		for (Entry<String, String> entry : subscriptionIdsToRemove.entrySet()) {
			removeSubscription(entry.getKey(), entry.getValue());
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
		
		Map<String, HostedServices> hostedServices = data.getServicesPerSubscription();
		if (hostedServices == null || hostedServices.keySet().isEmpty()) {
			return true;
		}
		Map<String, StorageServices> storageServices = data.getStoragesPerSubscription();
		if (storageServices == null || storageServices.keySet().isEmpty()) {
			return true;
		}
		Map<String , Locations> locations = data.getLocationsPerSubscription();
		if (locations == null || locations.keySet().isEmpty()) {
			return true;
		}
		return false;
		
	}
}
