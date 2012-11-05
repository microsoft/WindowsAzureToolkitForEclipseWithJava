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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import waeclipseplugin.Activator;
import com.gigaspaces.azure.deploy.DeploymentEventArgs;
import com.gigaspaces.azure.deploy.DeploymentEventListener;
import com.gigaspaces.azure.deploy.DeploymentManager;
import com.gigaspaces.azure.rest.RestAPIException;
import com.gigaspaces.azure.util.CommandLineException;
import com.microsoftopentechnologies.wacommon.utils.WACommonException;

public class WindowsAzureUndeploymentJob extends Job {

	private String serviceName;
	private String deploymentName;
	private String deploymentState;
	private String name;

	public WindowsAzureUndeploymentJob(String name, String serviceName,
			String deploymentName, String deploymentLabel, String deploymentState) {
		super(name);
		this.name = name;
		this.serviceName = serviceName;
		this.deploymentName = deploymentName;
		this.deploymentState = deploymentState;
	}
	
	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		MessageConsole console = Activator.findConsole(Activator.CONSOLE_NAME);

		console.clearConsole();

		final MessageConsoleStream out = console.newMessageStream();

		monitor.beginTask(name, 100);

		Activator.getDefault().addDeploymentEventListener(
				new DeploymentEventListener() {

					@Override
					public void onDeploymentStep(DeploymentEventArgs args) {
						monitor.subTask(args.toString());
						monitor.worked(args.getDeployCompleteness());
						out.println(args.toString());
					}
				});
		
		try {
			DeploymentManager.getInstance().undeploy(serviceName, deploymentName,deploymentState);
		} 
		catch (RestAPIException e) {
			Activator.getDefault().log(Messages.error,e);
		} 
		catch (InterruptedException e) {
			Activator.getDefault().log(Messages.error,e);
		} 
		catch (CommandLineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (WACommonException e) {
			Activator.getDefault().log(Messages.error,e);
			e.printStackTrace();
		}
		
		super.setName("");
		monitor.done();
		super.done(Status.OK_STATUS);

		return Status.OK_STATUS;
	}

}
