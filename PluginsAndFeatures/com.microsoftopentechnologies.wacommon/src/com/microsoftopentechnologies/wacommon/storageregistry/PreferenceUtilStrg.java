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
package com.microsoftopentechnologies.wacommon.storageregistry;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.List;

import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import com.microsoftopentechnologies.azurecommons.storageregistry.StorageAccount;
import com.microsoftopentechnologies.azurecommons.storageregistry.StorageAccountRegistry;
import com.microsoftopentechnologies.wacommon.Activator;
import com.microsoftopentechnologies.wacommon.utils.Messages;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;

/**
 * Preference utility class to save and load
 * preferences of centralized storage account registry. 
 */
public class PreferenceUtilStrg {
	private static final String PREF_KEY = "WAEclipsePlugin" + ".storage";
	private static final PreferenceUtilStrg INSTANCE = new PreferenceUtilStrg();

	public synchronized static void save() {
		INSTANCE.savePreferences();
	}

	/**
	 * Stores storage account list
	 * in preference file in the form of byte array. 
	 */
	private void savePreferences() {
		try {
			Preferences prefs = PluginUtil.getPrefs(
					com.microsoftopentechnologies.wacommon.utils.Messages.prefFileName);
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			ObjectOutput output = new ObjectOutputStream(buffer);
			List<StorageAccount> data = StorageAccountRegistry.getStrgList();
			/*
			 * Sort list according to storage account name.
			 */
			Collections.sort(data);
			StorageAccount[] dataArray = new StorageAccount[data.size()];
			int i = 0;
			for (StorageAccount pd1 : data) {
				dataArray[i] = pd1;
				i++;
			}
			try {
				output.writeObject(dataArray);
			} finally {
				output.close();
			}
			prefs.putByteArray(PREF_KEY, buffer.toByteArray());
			prefs.flush();
		} catch (BackingStoreException e) {
			Activator.getDefault().log(Messages.err, e);
		} catch (IOException e) {
			Activator.getDefault().log(Messages.err, e);
		}
	}

	public static void load(){
		INSTANCE.loadPreferences();
	}

	/**
	 * Read and load preference file data.
	 * Converts byte array format data to list of storage accounts.
	 */
	private void loadPreferences() {
		Preferences prefs = PluginUtil.getPrefs(
				com.microsoftopentechnologies.wacommon.utils.Messages.prefFileName);
		try {
			byte[] data = prefs.getByteArray(PREF_KEY, null);
			if (data != null) {
				ByteArrayInputStream buffer = new ByteArrayInputStream(data);
				ObjectInput input = new ObjectInputStream(buffer);
				try {
					StorageAccount[] storageAccs = (StorageAccount[]) input.readObject();
					for (StorageAccount str : storageAccs) {
						try {
							if (!StorageAccountRegistry.getStrgList().contains(str)) {
								StorageAccountRegistry.getStrgList().add(str);
							}
						} catch (Exception e) {
							Activator.getDefault().log(Messages.err, e);
						}
					}
				} finally {
					input.close();
				}
			}
		} catch (IOException e) {
			Activator.getDefault().log(Messages.err, e);
		} catch (ClassNotFoundException e) {
			Activator.getDefault().log(Messages.err, e);
		}
	}
}
