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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;

import waeclipseplugin.Activator;
import com.gigaspaces.azure.model.CreateHostedService;
import com.gigaspaces.azure.rest.RestAPIConflictException;
import com.gigaspaces.azure.rest.RestAPIException;
import com.gigaspaces.azure.tasks.AccountCachingExceptionEvent;
import com.gigaspaces.azure.util.CommandLineException;
import com.gigaspaces.azure.util.PublishData;
import com.gigaspaces.azure.wizards.Messages;
import com.gigaspaces.azure.wizards.WizardCacheManager;
import com.persistent.util.MessageUtil;

public class NewHostedServiceWithProgressWindow extends AccountActionRunnable implements Runnable {

	private CreateHostedService body;
	
	private final static int TASKS = 100;
	
	public NewHostedServiceWithProgressWindow(PublishData data, Shell shell) {
		super(data, shell);
	}
	
	public void setCreateHostedService(CreateHostedService body) {
		this.body = body;
	}
	

	@Override
	public void run() {
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
		try {
			dialog.run(true, true, this);
			dialog.close();
		} catch (InvocationTargetException e) {
			MessageUtil.displayErrorDialog(shell, com.gigaspaces.azure.wizards.Messages.createHostedServiceFailedMsg, e.getMessage());
			Activator.getDefault().log(Messages.error, e);
		} catch (InterruptedException e) {
			MessageDialog.openWarning(shell, Messages.interrupt, Messages.newServiceInterrupted);
			Activator.getDefault().log(Messages.error, e);
		}			
	}
	
	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		 
		monitor.beginTask("Creating " + body.getServiceName(), TASKS);
		
		Thread thread = doAsync();

		while (wait.get() == true) {
			if (monitor.isCanceled()) {
				thread.interrupt();
				throw new InterruptedException();
			}
			Thread.sleep(1000);
			monitor.worked(1);
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
	public void doTask() {
		try {
			WizardCacheManager.createHostedService(body);
		}
		catch (RestAPIConflictException e) {
			AccountCachingExceptionEvent event = new AccountCachingExceptionEvent(this);
			event.setException(e);
			event.setMessage(Messages.hostedServiceConflictError);
			onRestAPIError(event);
			Activator.getDefault().log(Messages.error, e);			
		}
		catch (RestAPIException e) {
			AccountCachingExceptionEvent event = new AccountCachingExceptionEvent(this);
			event.setException(e);
			event.setMessage(e.getMessage());
			onRestAPIError(event);
			Activator.getDefault().log(Messages.error, e);
		}
		catch (InterruptedException e) {
		} 
		catch (CommandLineException e) {
			AccountCachingExceptionEvent event = new AccountCachingExceptionEvent(this);
			event.setException(e);
			event.setMessage(e.getMessage());
			onRestAPIError(event);
			Activator.getDefault().log(Messages.error, e);
		}
	}
	
	

}
