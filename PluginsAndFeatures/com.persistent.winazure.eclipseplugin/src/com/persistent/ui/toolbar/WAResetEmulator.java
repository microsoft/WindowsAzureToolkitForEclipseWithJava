/**
 * Copyright 2014 Microsoft Open Technologies, Inc.
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
package com.persistent.ui.toolbar;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Shell;

import waeclipseplugin.Activator;

import com.microsoftopentechnologies.wacommon.utils.PluginUtil;

/**
 * This class resets the Azure Emulator.
 */
public class WAResetEmulator extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			String strKitLoc = String.format("%s%s%s%s%s%s", Platform
					.getInstallLocation().getURL().getPath().toString(),
					File.separator, Messages.pluginFolder, File.separator,
					Messages.pluginId, Messages.pWizStarterKit);
			// copy elevate.vbs to temp location
			String tmpPath = System.getProperty("java.io.tmpdir");
			com.microsoftopentechnologies.roleoperations.WAResetEmulator.
			resetEmulator(strKitLoc,
					"%proj%/.templates/emulatorTools/.elevate.vbs",
					new File(String.format("%s%s%s", tmpPath, File.separator,
							".elevate.vbs")));
		} catch (IOException e1) {
			Activator.getDefault().log(Messages.ioErrMsg, e1);
		} catch (Exception e) {
			PluginUtil.displayErrorDialogAndLog(new Shell(),
					Messages.rstEmltrErrTtl, Messages.rstEmuErrMsg, e);
		}
		return null;
	}
}
