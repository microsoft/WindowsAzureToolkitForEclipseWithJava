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
package com.microsoftopentechnologies.wacommon.startup;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.w3c.dom.Document;

import com.microsoftopentechnologies.azurecommons.xmlhandling.DataOperations;
import com.microsoftopentechnologies.azurecommons.xmlhandling.ParseXMLUtilMethods;
import com.microsoftopentechnologies.wacommon.Activator;
import com.microsoftopentechnologies.wacommon.telemetry.AppInsightsCustomEvent;
import com.microsoftopentechnologies.wacommon.utils.FileUtil;
import com.microsoftopentechnologies.wacommon.utils.Messages;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;

/**
 * This class gets executed after the Workbench initializes.
 */
public class WACPStartUp implements IStartup {

	public void earlyStartup() {
		//this code is for copying encutil.exe in plugins folder
		copyPluginComponents();
		initialize();
	}

	private void copyPluginComponents() {
		try {
			String pluginInstLoc = String.format("%s%s%s",
					PluginUtil.pluginFolder,
					File.separator, Messages.waCommonFolderID);
			if (!new File(pluginInstLoc).exists()) {
				new File(pluginInstLoc).mkdir();
			}
			String enctFile = String.format("%s%s%s", pluginInstLoc,
					File.separator, Messages.encFileName);

			// Check for encutil.exe
			if (new File(enctFile).exists()) {
				new File(enctFile).delete();
			}
			FileUtil.copyResourceFile(Messages.encFileEntry,enctFile);
		} catch (Exception e) {
			Activator.getDefault().log(e.getMessage(), e);
		}

	}

	/**
	 * Method verifies presence of com.microsoftopentechnologies.wacommon folder and data.xml file.
	 * It updates or creates property elements in data.xml as per scenarios.
	 */
	private void initialize() {
		try {
			String pluginInstLoc = String.format("%s%s%s",
					PluginUtil.pluginFolder, File.separator, Messages.commonPluginID);
			final String dataFile = String.format("%s%s%s", pluginInstLoc,
					File.separator, Messages.dataFileName);
			if (new File(pluginInstLoc).exists()) {
				if (new File(dataFile).exists()) {
					String version = DataOperations.getProperty(dataFile, Messages.version);
					if (version == null || version.isEmpty()) {
						// proceed with setValues method as no version specified
						setValues(dataFile);
					} else {
						String curVersion = Activator.getDefault().getBundle().getVersion().toString();
						// compare version
						if (curVersion.equalsIgnoreCase(version)) {
							// Case of normal eclipse restart
							// check preference-value & installation-id exists or not else copy values
							String prefValue = DataOperations.getProperty(dataFile, Messages.prefVal);
							String instID = DataOperations.getProperty(dataFile, Messages.instID);
							if (prefValue == null || prefValue.isEmpty()) {
								setValues(dataFile);
							} else if (instID == null || instID.isEmpty()) {
								Document doc = ParseXMLUtilMethods.parseFile(dataFile);
								DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
								DataOperations.updatePropertyValue(doc, Messages.instID, dateFormat.format(new Date()));
								ParseXMLUtilMethods.saveXMLDocument(dataFile, doc);
							}
						} else {
							// proceed with setValues method. Case of new plugin installation
							setValues(dataFile);
						}
					}
				} else {
					// copy file and proceed with setValues method
					FileUtil.copyResourceFile(Messages.dataFileEntry, dataFile);
					setValues(dataFile);
				}
			} else {
				new File(pluginInstLoc).mkdir();
				FileUtil.copyResourceFile(Messages.dataFileEntry, dataFile);
				setValues(dataFile);
			}
		} catch(Exception ex) {
			Activator.getDefault().log(ex.getMessage(), ex);
		}
	}

	/**
	 * Method updates or creates property elements in data.xml
	 * @param dataFile
	 * @throws Exception
	 */
	private void setValues(final String dataFile) throws Exception {
		final Document doc = ParseXMLUtilMethods.parseFile(dataFile);
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				boolean accepted = false;
				AcceptLicenseDlg dlg = new AcceptLicenseDlg(Display.getDefault().getActiveShell());
				if (dlg.open() == Window.OK) {
					accepted = true;
				}
				DataOperations.updatePropertyValue(doc, Messages.prefVal, String.valueOf(accepted));
			}});
		DataOperations.updatePropertyValue(doc, Messages.version,
				Activator.getDefault().getBundle().getVersion().toString());
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		DataOperations.updatePropertyValue(doc, Messages.instID, dateFormat.format(new Date()));
		ParseXMLUtilMethods.saveXMLDocument(dataFile, doc);
		String prefVal = DataOperations.getProperty(dataFile, Messages.prefVal);
		if (prefVal != null && !prefVal.isEmpty() && prefVal.equals("true")) {
			AppInsightsCustomEvent.create(Messages.telAgrEvtName, "");
		}
	}
}
