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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import waeclipseplugin.Activator;
import com.gigaspaces.azure.deploy.DeploymentManager;
import com.gigaspaces.azure.util.CommandLineException;
import com.microsoftopentechnologies.azurecommons.deploy.DeploymentEventArgs;
import com.microsoftopentechnologies.azurecommons.deploy.DeploymentEventListener;
import com.microsoftopentechnologies.azurecommons.exception.RestAPIException;
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
		Activator.removeUnNecessaryListener();
		DeploymentEventListener undeployListnr = new DeploymentEventListener() {

			@Override
			public void onDeploymentStep(DeploymentEventArgs args) {
				monitor.subTask(args.toString());
				monitor.worked(args.getDeployCompleteness());
				out.println(args.toString());
			}
		};
		Activator.getDefault().addDeploymentEventListener(undeployListnr);
		Activator.depEveList.add(undeployListnr);

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
