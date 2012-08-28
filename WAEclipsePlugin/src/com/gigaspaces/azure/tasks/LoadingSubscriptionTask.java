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
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.gigaspaces.azure.model.Subscription;
import com.gigaspaces.azure.rest.WindowsAzureServiceManagement;
import com.gigaspaces.azure.util.PublishData;

public class LoadingSubscriptionTask extends LoadingTask<List<Subscription>> {

	private static final int OPERATION_TIMEOUT = 120;

	private final WindowsAzureServiceManagement service = getServiceInstance();
	private final List<Subscription> subscriptions = new ArrayList<Subscription>();
	private List<Future<?>> futures = new ArrayList<Future<?>>();
	private ScheduledExecutorService threadPool;

	private AtomicBoolean errorHappened = new AtomicBoolean(false);

	private List<Subscription> subs;	

	public LoadingSubscriptionTask(PublishData data) {
		super(data);
	}

	public void setSubscriptionIds(List<Subscription> subs) {
		this.subs = subs;
	}

	@Override
	public List<Subscription> call() throws Exception {
		int numberOfSubscriptions = subs.size();
		threadPool = Executors.newScheduledThreadPool(numberOfSubscriptions);
		for (Subscription s : subs) {
			LoadSubscription loadSubscription = new LoadSubscription();
			loadSubscription.setSubscriptionId(s.getId());
			Future<?> submit = threadPool.submit(loadSubscription);
			futures.add(submit);
		}

		try {
			for (Future<?> future : futures) {
				future.get(OPERATION_TIMEOUT, TimeUnit.SECONDS);
			}
			threadPool.shutdownNow();
			threadPool.awaitTermination(OPERATION_TIMEOUT * numberOfSubscriptions, TimeUnit.SECONDS);
		}
		catch (TimeoutException e) {
			AccountCachingExceptionEvent event = new AccountCachingExceptionEvent(this);
			event.setException(e);
			event.setMessage("Timed out while waiting for subscriptions, please try again");
			threadPool.shutdownNow();
			fireRestAPIErrorEvent(event);
			return new ArrayList<Subscription>();

		}
		catch (InterruptedException e) {
			return new ArrayList<Subscription>();
		}
		if (errorHappened.get() == true) {
			return new ArrayList<Subscription>(); 
		}
		return subscriptions;
	}

	@Override
	protected void setDataResult(List<Subscription> data) {
		this.data.getPublishProfile().setSubscriptions(data);
		if (!data.isEmpty()) {
			fireOnLoadedSubscriptionsEvent();
		}

	}

	class LoadSubscription implements Runnable {

		private String subcriptionId;

		public void setSubscriptionId(String id) {
			this.subcriptionId = id;
		}

		@Override
		public void run() {
			try {
				if (errorHappened.get() == false) {
					Subscription subs = service.getSubscription(subcriptionId);
					addSubscription(subs);
				}
			} catch (Exception e) {
				AccountCachingExceptionEvent event = new AccountCachingExceptionEvent(this);
				event.setException(e);
				if ((e.getMessage() != null) && (!e.getMessage().isEmpty())) {
					event.setMessage(com.gigaspaces.azure.wizards.Messages.genericErrorWhileLoadingCred);					
				}
				else{ 
					event.setMessage(e.getMessage());
				}
				errorHappened.set(true);
				fireRestAPIErrorEvent(event);
			}
		}	
	}


	private synchronized void addSubscription(Subscription sub) {
		subscriptions.add(sub);
	}

	private void fireOnLoadedSubscriptionsEvent() {
		Object[] list = listeners.getListenerList();
		for (int i = 0; i < list.length; i += 2) {
			if (list[i] == LoadingAccoutListener.class) {
				((LoadingAccoutListener) list[i + 1]).onLoadedSubscriptions();
			}
		}	
	}
}
