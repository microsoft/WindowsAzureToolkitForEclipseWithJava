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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;

import waeclipseplugin.Activator;

import com.gigaspaces.azure.util.CommandLineException;
import com.gigaspaces.azure.wizards.Messages;
import com.gigaspaces.azure.wizards.WizardCacheManager;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.management.storage.models.StorageAccountCreateParameters;
import com.microsoftopentechnologies.azurecommons.deploy.tasks.AccountCachingExceptionEvent;
import com.microsoftopentechnologies.azurecommons.deploy.util.PublishData;
import com.microsoftopentechnologies.azurecommons.exception.RestAPIException;
import com.microsoftopentechnologies.azuremanagementutil.model.StorageService;
import com.persistent.util.MessageUtil;

public class NewStorageAccountWithProgressWindow
extends AccountActionRunnable implements Runnable {

	private StorageAccountCreateParameters body;
	static StorageService storageService;
	private final static int TASKS = 200;

	public NewStorageAccountWithProgressWindow(PublishData data, Shell shell) {
		super(data, shell);
	}

	public void setCreateStorageAccount(StorageAccountCreateParameters body) {
		this.body = body;
	}

	@Override
	public void run() {
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
		try {
			dialog.run(true, true, this);
			dialog.close();
		} catch (InvocationTargetException e) {
			MessageUtil.displayErrorDialog(shell,
					com.gigaspaces.azure.wizards.Messages.createStorageAccountFailedTitle,
					e.getMessage());
			Activator.getDefault().log(Messages.error, e);
		} catch (InterruptedException e) {
			MessageDialog.openWarning(shell,
					Messages.interrupt,
					Messages.newStorageInterrupted);
			Activator.getDefault().log(Messages.error, e);
		}
	}

	@Override
	public void run(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {

		monitor.beginTask(Messages.crtStrgAcc
				+ body.getName()
				+ Messages.takeMinLbl,
				TASKS);

		Thread thread = doAsync();

		while (wait.get()) {
			if (monitor.isCanceled()) {
				thread.interrupt();
				throw new InterruptedException();
			}
			Thread.sleep(1000);
			monitor.worked(1);
		}
		if (error.get()) {
			monitor.worked(TASKS);
			monitor.done();
			throw new InvocationTargetException(exception, errorMessage);
		}
		monitor.worked(TASKS);
		monitor.done();
		thread.join();
	}

	public static StorageService getStorageService() {
		return storageService;
	}

	@Override
	public void doTask() {
		try {
			storageService = WizardCacheManager.createStorageAccount(body);
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
		} catch (ServiceException e) {
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
}
