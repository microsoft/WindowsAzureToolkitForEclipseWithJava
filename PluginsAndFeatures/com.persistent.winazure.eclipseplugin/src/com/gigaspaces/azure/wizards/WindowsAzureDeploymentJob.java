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

package com.gigaspaces.azure.wizards;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import waeclipseplugin.Activator;

import com.gigaspaces.azure.deploy.DeploymentManager;
import com.microsoftopentechnologies.azurecommons.deploy.DeploymentEventArgs;
import com.microsoftopentechnologies.azurecommons.deploy.DeploymentEventListener;
import com.microsoftopentechnologies.azurecommons.deploy.UploadProgressEventArgs;
import com.microsoftopentechnologies.azurecommons.deploy.UploadProgressEventListener;
import com.microsoftopentechnologies.azurecommons.exception.DeploymentException;


public class WindowsAzureDeploymentJob extends Job {

	private final IProject selectedProject;
	private final AtomicBoolean wait = new AtomicBoolean(true);
	private String deploymentId;
	private String name;

	public WindowsAzureDeploymentJob(String name, IProject selectedProject) {
		super(name);
		this.name = name;
		this.selectedProject = selectedProject;
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		MessageConsole myConsole = Activator.findConsole(Activator.CONSOLE_NAME);

		myConsole.clearConsole();

		final MessageConsoleStream out = myConsole.newMessageStream();

		monitor.beginTask(name, 100);
		out.println(Messages.deployingToAzureMsg);
		Activator.removeUnNecessaryListener();
		DeploymentEventListener deployListnr = new DeploymentEventListener() {

			@Override
			public void onDeploymentStep(DeploymentEventArgs args) {
				deploymentId = args.getId();
				monitor.subTask(args.toString());
				monitor.worked(args.getDeployCompleteness());
				out.println(args.toString());
			}
		};
		Activator.getDefault().addDeploymentEventListener(deployListnr);
		Activator.depEveList.add(deployListnr);


		Activator.getDefault().addUploadProgressEventListener(new UploadProgressEventListener() {

			@Override
			public void onUploadProgress(UploadProgressEventArgs args) {
				synchronized (monitor) {
					String message = com.gigaspaces.azure.deploy.Messages.uploadingServicePackage + " - " + args.getPercentage() + "% Completed";			
					monitor.subTask(message);
					out.println(message);
				}	
			}
		});

		Thread thread = doAsync();

		while (wait.get() == true) {
			if (monitor.isCanceled()) {
				DeploymentEventArgs canceled = createDeploymentCanceledEventArgs(deploymentId);
				Activator.getDefault().fireDeploymentEvent(canceled);
				thread.interrupt();
				super.setName("");
				monitor.done();
				super.done(Status.CANCEL_STATUS);
				return Status.CANCEL_STATUS;
			} 
		}
		monitor.done();
		super.setName("");
		super.done(Status.OK_STATUS);
		return Status.OK_STATUS;
	}

	private DeploymentEventArgs createDeploymentCanceledEventArgs(String id) {

		DeploymentEventArgs canceledArgs = new DeploymentEventArgs(this);
		canceledArgs.setDeployMessage(Messages.deploymentCanceled);
		canceledArgs.setId(id);
		canceledArgs.setDeployCompleteness(100);
		return canceledArgs;

	}

	private Thread doAsync() {
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

	private void doTask() {
		try {
			DeploymentManager.getInstance().deploy(selectedProject);
		} 
		catch (InterruptedException e) {
		} 
		catch (DeploymentException e) {
			Activator.getDefault().log(Messages.error,e);
		} 
	}

}
