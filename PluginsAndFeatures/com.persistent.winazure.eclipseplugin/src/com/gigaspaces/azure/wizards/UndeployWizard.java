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

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.wizard.Wizard;

public class UndeployWizard extends Wizard {

	private UndeploymentPage page;
	private String serviceName;
	private String deploymentName;
	private String deploymentState;

	public UndeployWizard() {
		super();
		setWindowTitle(Messages.undeployWizTitle);
	}

	@Override
	public void addPages() {
		page = new UndeploymentPage(Messages.unpubplishAzureProjPage);
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		Job job = new WindowsAzureUndeploymentJob(Messages.deletingAzureDeployment, serviceName, deploymentName,deploymentState,deploymentState);
		job.schedule();
		return true;
	}

	public void setSettings(String serviceName, String deploymentName,String deploymentLabel, String deploymentState) {
		this.serviceName = serviceName;		
		this.deploymentName = deploymentName;
		this.deploymentState = deploymentState;
	}
}