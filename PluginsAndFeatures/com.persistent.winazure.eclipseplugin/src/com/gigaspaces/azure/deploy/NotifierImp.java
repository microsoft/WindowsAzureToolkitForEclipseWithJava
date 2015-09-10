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
 */package com.gigaspaces.azure.deploy;

import java.util.concurrent.atomic.AtomicInteger;

import com.microsoftopentechnologies.azurecommons.deploy.UploadProgressEventArgs;
import com.microsoftopentechnologies.azuremanagementutil.model.Notifier;

import waeclipseplugin.Activator;

class NotifierImp implements Notifier {
	
	private final AtomicInteger percent = new AtomicInteger(0);

	@Override
	public void notifyProgress(int step) {
		UploadProgressEventArgs event = new UploadProgressEventArgs(this);
		int currentPercentage = percent.get();
		percent.set(currentPercentage + step);
		event.setPercentage(percent.get());
		Activator.getDefault().fireUploadProgressEvent(event);
	}

	public int getPercent() {
		return percent.get();
	}
}
