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

package com.gigaspaces.azure.runnable;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;

import com.gigaspaces.azure.tasks.AccountCachingExceptionEvent;
import com.gigaspaces.azure.tasks.LoadingAccoutListener;
import com.gigaspaces.azure.util.PublishData;

public abstract class AccountActionRunnable implements IRunnableWithProgress , LoadingAccoutListener {

	protected PublishData data;
	protected Shell shell;
	
	private IProgressMonitor progressMonitor;
	
	private static final int TASKS = 200;
	
	protected final AtomicBoolean wait = new AtomicBoolean(true);
	protected final AtomicBoolean error = new AtomicBoolean(false);
	
	private int numberOfAccounts = 1;
	protected Exception exception;
	protected String errorMessage;

	public abstract void doTask();

	public AccountActionRunnable(PublishData data, Shell shell) {
		this.data = data;
		this.shell = shell;
	}
	
	public void setNumberOfAccounts(int num) {
		this.numberOfAccounts = num;
	}

	public Thread doAsync() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
					doTask();
					wait.set(false);
			}
		});

		thread.start();
		
		return thread;
	}

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

		 
		monitor.beginTask("Loading Account Settings...", TASKS);
		monitor.subTask("Subscriptions");
		
		progressMonitor = monitor;
		
		Thread thread = doAsync();

		while (wait.get() == true) {
			if (monitor.isCanceled()) {
				thread.interrupt();
				throw new InterruptedException();
			}
			Thread.sleep(1000);
		}
		if (error.get() == true) {
			monitor.worked(TASKS);
			monitor.done();
			throw new InvocationTargetException(exception, errorMessage);
		}
		monitor.worked(TASKS);
		monitor.done();
		thread.join();
	}

	@Override
	public synchronized void onLoadedSubscriptions() {
		setWorked(TASKS / (5 * numberOfAccounts) );
		progressMonitor.subTask("Storage Services, Hosted Services and Locations");
	}

	@Override
	public void onLoadedStorageServices() {
		setWorked(TASKS / (5 * numberOfAccounts));
	}

	@Override
	public void onLoadedHostedServices() {
		setWorked(TASKS / (5 * numberOfAccounts));
	}

	@Override
	public void onLoadedLocations() {
		setWorked(TASKS / (5 * numberOfAccounts));
	}
	
	@Override
	public void onRestAPIError(AccountCachingExceptionEvent e) {
		wait.set(false);
		error.set(true);
		exception = e.getException();
		errorMessage = e.getMessage();
	}
	
	private synchronized void setWorked(int work) {
		progressMonitor.worked(work);
	}
}
