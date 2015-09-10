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

import java.util.concurrent.Callable;

import javax.swing.event.EventListenerList;

import com.microsoftopentechnologies.azurecommons.deploy.util.PublishData;
import com.microsoftopentechnologies.azuremanagementutil.exception.InvalidThumbprintException;
import com.microsoftopentechnologies.azuremanagementutil.rest.WindowsAzureServiceManagement;


public abstract class LoadingTask<T> implements Callable<T>, Runnable {
	
	protected PublishData data;
	private T dataResult;
	
	protected EventListenerList listeners = new EventListenerList();
	
	public void addLoadingAccountListener(LoadingAccoutListener listener) {
		listeners.add(LoadingAccoutListener.class, listener);
	}
	
	public void removeLoadingAccountListener(LoadingAccoutListener listener) {
		listeners.remove(LoadingAccoutListener.class, listener);
	}

	public LoadingTask(PublishData data) {
		this.data = data;
	}

	public T getDataResult() {
		return dataResult;
	}

	protected abstract void setDataResult(T data);

	@Override
	public abstract T call() throws Exception;

	private void doTask() throws Exception {
		dataResult = call();
		synchronized (data) {
			setDataResult(dataResult);
		}

	}

	@Override
	public void run() {

		try {
			doTask();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected synchronized WindowsAzureServiceManagement getServiceInstance() {
		WindowsAzureServiceManagement instance = null;
		try {
			instance = new WindowsAzureServiceManagement();
		} catch (InvalidThumbprintException e) {
			e.printStackTrace();
		}
		return instance;
	}
	
	protected void fireRestAPIErrorEvent(AccountCachingExceptionEvent e) {
		Object[] list = listeners.getListenerList();
		for (int i = 0; i < list.length; i += 2) {
			if (list[i] == LoadingAccoutListener.class) {
				((LoadingAccoutListener) list[i + 1]).onRestAPIError(e);
			}
		}	
	}
}
