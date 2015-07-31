/**
* Copyright Microsoft Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.gigaspaces.azure.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.internal.Workbench;

import waeclipseplugin.Activator;

import com.gigaspaces.azure.wizards.DeployWizard;
import com.gigaspaces.azure.wizards.DeployWizardDialog;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
import com.persistent.contextmenu.SingleClickPublishUtils;
import com.persistent.ui.propertypage.WAProjectNature;

@SuppressWarnings("restriction")
public class DeployCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IProject selectedProject = PluginUtil.getSelectedProject();
		try {
			if (selectedProject.hasNature(WAProjectNature.NATURE_ID)) {
				DeployWizard wizard = new DeployWizard();
				if (wizard.getSelectedProject() == null) {
					return null;
				}
				wizard.setNeedsProgressMonitor(true);
				DeployWizardDialog dialog = new DeployWizardDialog(
						Workbench.getInstance().getActiveWorkbenchWindow().getShell(),
						wizard, Messages.publish);
				dialog.create();
				dialog.open();
			} else {
				SingleClickPublishUtils.exceute();
			}
		} catch (CoreException e) {
			Activator.getDefault().log("Error", e);
		}
		return null;
	}
}