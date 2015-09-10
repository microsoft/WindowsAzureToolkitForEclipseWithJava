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

package com.microsoftopentechnologies.azurecommons.deploy.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.microsoft.windowsazure.Configuration;
import com.microsoftopentechnologies.azurecommons.deploy.util.PublishData;
import com.microsoftopentechnologies.azurecommons.messagehandler.PropUtil;
import com.microsoftopentechnologies.azuremanagementutil.model.Subscription;
import com.microsoftopentechnologies.azuremanagementutil.rest.WindowsAzureServiceManagement;

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
			loadSubscription.setConfiguration(data.getConfiguration(s.getId()));
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

		private Configuration configuration;
		private String subcriptionId;

		public void setSubscriptionId(String id) {
			this.subcriptionId = id;
		}

		public void setConfiguration(Configuration configuration) {
			this.configuration = configuration;
		}

		@Override
		public void run() {
			try {
				if (errorHappened.get() == false) {
					Subscription subs = service.getSubscription(configuration);
					addSubscription(subs);
				}
			} catch (Exception e) {
				AccountCachingExceptionEvent event = new AccountCachingExceptionEvent(this);
				event.setException(e);
				if (PropUtil.getValueFromFile("bouncyCastleMsg").equals(e.getMessage())
						&& e.getCause() instanceof ClassNotFoundException) {
					event.setMessage(PropUtil.getValueFromFile("importDlgMsgJavaVersion"));
				} else if ((e.getMessage() == null) || (e.getMessage().isEmpty())) {
					event.setMessage(PropUtil.getValueFromFile("genericErrorWhileLoadingCred"));					
				}
				else {
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
