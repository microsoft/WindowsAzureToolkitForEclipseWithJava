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

package com.gigaspaces.azure.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import waeclipseplugin.Activator;

import com.gigaspaces.azure.model.HostedServices;
import com.gigaspaces.azure.model.Subscription;
import com.gigaspaces.azure.rest.WindowsAzureServiceManagement;
import com.gigaspaces.azure.util.CommandLineException;
import com.gigaspaces.azure.util.PublishData;

public class LoadingHostedServicesTask extends LoadingTask<Map<String,HostedServices>> {

	public LoadingHostedServicesTask(PublishData data) {
		super(data);
	}

	private static final int OPERATION_TIMEOUT = 120;

	private final WindowsAzureServiceManagement service = getServiceInstance();
	private final Map<String, HostedServices> hostedServicesMap = new ConcurrentHashMap<String, HostedServices>();
	private List<Future<?>> futures = new ArrayList<Future<?>>();
	private ScheduledExecutorService threadPool;


	@Override
	public Map<String , HostedServices> call() throws Exception {

		List<Subscription> subscriptions = data.getPublishProfile().getSubscriptions();
		if (!subscriptions.isEmpty()) {
			int numberOfSubscriptions = subscriptions.size();
			threadPool = Executors.newScheduledThreadPool(numberOfSubscriptions);
			for (Subscription sub : subscriptions) {
				LoadHostetServicesPerSubscription task = new LoadHostetServicesPerSubscription();
				task.setSubscriptionId(sub.getId());
				Future<?> submit = threadPool.submit(task);
				futures.add(submit);
			}
			try {
				for (Future<?> future : futures) {
					future.get(OPERATION_TIMEOUT, TimeUnit.SECONDS);
				}
			}
			catch (TimeoutException e) {
				AccountCachingExceptionEvent event = new AccountCachingExceptionEvent(this);
				event.setException(e);
				event.setMessage("Timed out while waiting for hosted services, please try again");
				threadPool.shutdownNow();
				fireRestAPIErrorEvent(event);
				return new ConcurrentHashMap<String, HostedServices>();
			}
			catch (InterruptedException e) {
				return new ConcurrentHashMap<String, HostedServices>();
			}
		}

		return hostedServicesMap;

	}

	@Override
	protected void setDataResult(Map<String, HostedServices> data) {
		this.data.setServicesPerSubscription(data);
		if (!data.keySet().isEmpty()) {
			fireOnLoadedHostesServicesEvent();
		}
	}

	class LoadHostetServicesPerSubscription implements Runnable {

		private String subcriptionId;

		public void setSubscriptionId(String id) {
			this.subcriptionId = id;
		}

		@Override
		public void run() {

			HostedServices hostedServicesForSubscription;
			try {
				hostedServicesForSubscription = service.listHostedServices(subcriptionId);
				hostedServicesMap.put(subcriptionId, hostedServicesForSubscription);
			} 
			catch (InterruptedException e) {
				Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, e);
			} 
			catch (CommandLineException e) {
				Activator.getDefault().log(com.gigaspaces.azure.rest.Messages.error, e);
			}
		}	
	}

	private void fireOnLoadedHostesServicesEvent() {
		Object[] list = listeners.getListenerList();
		for (int i = 0; i < list.length; i += 2) {
			if (list[i] == LoadingAccoutListener.class) {
				((LoadingAccoutListener) list[i + 1]).onLoadedHostedServices();
			}
		}	
	}


}
