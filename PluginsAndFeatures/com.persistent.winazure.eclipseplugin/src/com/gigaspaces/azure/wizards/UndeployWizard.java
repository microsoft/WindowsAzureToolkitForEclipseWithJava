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