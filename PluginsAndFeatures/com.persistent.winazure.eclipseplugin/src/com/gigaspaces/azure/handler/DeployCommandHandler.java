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

package com.gigaspaces.azure.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.internal.Workbench;

import com.gigaspaces.azure.wizards.DeployWizard;
import com.gigaspaces.azure.wizards.DeployWizardDialog;

@SuppressWarnings("restriction")
public class DeployCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		DeployWizard wizard = new DeployWizard();
		if (wizard.getSelectedProject() == null) {
			return null;
		}
		wizard.setNeedsProgressMonitor(true);
		DeployWizardDialog dialog = new DeployWizardDialog(Workbench.getInstance().getActiveWorkbenchWindow()
				.getShell(), wizard,Messages.publish);
		dialog.create();
		dialog.open();

		return null;

	}

}