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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;

import waeclipseplugin.Activator;

import com.gigaspaces.azure.wizards.Messages;
import com.gigaspaces.azure.wizards.WizardCacheManager;
import com.microsoft.windowsazure.management.compute.models.HostedServiceGetDetailedResponse;
import com.microsoftopentechnologies.azurecommons.deploy.tasks.AccountCachingExceptionEvent;
import com.microsoftopentechnologies.azurecommons.deploy.util.PublishData;
import com.microsoftopentechnologies.azuremanagementutil.exception.InvalidThumbprintException;
import com.persistent.util.MessageUtil;

public class FetchDeploymentsForHostedServiceWithProgressWindow extends AccountActionRunnable implements Runnable {

	private String hostedServiceName;
	private HostedServiceGetDetailedResponse hostedService;
	
	private final static int TASKS = 100;
	
	public FetchDeploymentsForHostedServiceWithProgressWindow(PublishData data, Shell shell) {
		super(data, shell);
	}

	@Override
	public void run() {
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
		try {
			dialog.run(true, true, this);
			dialog.close();
		} catch (InvocationTargetException e) {
			MessageUtil.displayErrorDialog(shell, com.gigaspaces.azure.wizards.Messages.fetchingDeploymentsTitle, e.getMessage());
			Activator.getDefault().log(Messages.error, e);
		} catch (InterruptedException e) {
		}			
	}

	@Override
	public void doTask() {
		
		try {
			this.hostedService = WizardCacheManager.getHostedServiceWithDeployments(hostedServiceName);
		} catch (InvalidThumbprintException e) {
			AccountCachingExceptionEvent event = new AccountCachingExceptionEvent(this);
			event.setException(e);
			event.setMessage(e.getMessage());
			onRestAPIError(event);
			Activator.getDefault().log(Messages.error, e);
		} catch(Exception e) {
			Activator.getDefault().log(Messages.error, e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		 
		monitor.beginTask("Fetching Deployments For " + hostedServiceName, TASKS);
		
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


	public String getHostedServiceName() {
		return hostedServiceName;
	}

	public void setHostedServiceName(String hostedServiceName) {
		this.hostedServiceName = hostedServiceName;
	}

	public HostedServiceGetDetailedResponse getHostedServiceDetailed() {
		return hostedService;
	}

	public void setHostedService(HostedServiceGetDetailedResponse hostedService) {
		this.hostedService = hostedService;
	}
	
	

}
