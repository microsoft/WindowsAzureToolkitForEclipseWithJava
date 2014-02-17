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
			Preferences prefs = PluginUtil.getPrefs();
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
		Preferences prefs = PluginUtil.getPrefs();
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
