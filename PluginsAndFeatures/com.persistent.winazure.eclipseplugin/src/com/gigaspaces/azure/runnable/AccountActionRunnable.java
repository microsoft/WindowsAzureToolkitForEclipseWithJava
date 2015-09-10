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
package com.gigaspaces.azure.runnable;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;

import com.microsoftopentechnologies.azurecommons.deploy.tasks.AccountCachingExceptionEvent;
import com.microsoftopentechnologies.azurecommons.deploy.tasks.LoadingAccoutListener;
import com.microsoftopentechnologies.azurecommons.deploy.util.PublishData;

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
		progressMonitor.subTask("Storage Services, Cloud Services and Locations");
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
