/**
 * Copyright 2012 Persistent Systems Ltd.
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Shell;

import waeclipseplugin.Activator;

import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.persistent.util.MessageUtil;
/**
 * This class resets the Windows Azure Emulator.
 */
public class WAResetEmulator extends AbstractHandler {
	private static final int BUFF_SIZE = 1024;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			String strKitLoc = String.format("%s%s%s%s%s%s",
					Platform.getInstallLocation().getURL().getPath().toString(),
					File.separator, Messages.pluginFolder,
					File.separator, Messages.pluginId,
					Messages.pWizStarterKit);
			StringBuilder output = new StringBuilder();
			ZipFile zipFile = new ZipFile(strKitLoc);
			@SuppressWarnings("rawtypes")
			Enumeration entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				if (entry.getName().toLowerCase().
						indexOf(Messages.rstEmCmd) != -1) {
					InputStream in = zipFile.getInputStream(entry);
					Reader reader = new InputStreamReader(in);
					char[] buf = new char[BUFF_SIZE];
					int length = reader.read(buf, 0, buf.length);
					while (length > 0) {
						output.append(buf,0,length);
						length = reader.read(buf, 0, buf.length);
					}
					break;
				}
			}
			WindowsAzureProjectManager.resetEmulator(output.toString());
		} catch (WindowsAzureInvalidProjectOperationException e) {
			String errorTitle = Messages.rstEmltrErrTtl;
			String errorMessage = Messages.rstEmuErrMsg;
			Activator.getDefault().log(errorMessage, e);
			MessageUtil.displayErrorDialog(new Shell(), errorTitle, errorMessage);
		} catch (IOException e1) {
			Activator.getDefault().log(Messages.ioErrMsg, e1);
		}
		return null;
	}
}
