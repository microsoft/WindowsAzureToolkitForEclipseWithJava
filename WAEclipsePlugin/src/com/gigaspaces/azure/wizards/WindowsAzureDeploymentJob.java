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

import com.gigaspaces.azure.deploy.DeploymentEventArgs;
import com.gigaspaces.azure.deploy.DeploymentEventListener;
import com.gigaspaces.azure.deploy.DeploymentException;
import com.gigaspaces.azure.deploy.DeploymentManager;
import com.gigaspaces.azure.deploy.UploadProgressEventArgs;
import com.gigaspaces.azure.deploy.UploadProgressEventListener;

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

		Activator.getDefault().addDeploymentEventListener(
				new DeploymentEventListener() {

					@Override
					public void onDeploymentStep(DeploymentEventArgs args) {
						deploymentId = args.getId();
						monitor.subTask(args.toString());
						monitor.worked(args.getDeployCompleteness());
						out.println(args.toString());
					}
				});


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
