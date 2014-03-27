/**
 * Copyright 2013 Persistent Systems Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gigaspaces.azure.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import waeclipseplugin.Activator;

import com.microsoftopentechnologies.wacommon.utils.Messages;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;

public class PreferenceUtilPubWizard {

	private static final PreferenceUtilPubWizard INSTANCE = new PreferenceUtilPubWizard();

	public synchronized static void save(String prefKey, WizardCache cacheObj) {
		INSTANCE.savePreferences(prefKey, cacheObj);
	}

	public static WizardCache load(String prefKey){
		WizardCache cacheObj = INSTANCE.loadPreferences(prefKey);
		return cacheObj;
	}

	private void savePreferences(String prefKey, WizardCache cacheObj) {
		try {
			Preferences prefs = PluginUtil.getPrefs();
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			ObjectOutput output = new ObjectOutputStream(buffer);
			try {
				output.writeObject(cacheObj);
			} catch (Exception e) {
				output.close();
			}
			prefs.putByteArray(prefKey, buffer.toByteArray());
			prefs.flush();
		} catch (Exception e) {
			Activator.getDefault().log(Messages.err, e);
		}
	}

	private WizardCache loadPreferences(String prefKey) {
		WizardCache cacheObj = null;
		Preferences prefs = PluginUtil.getPrefs();
		try {
			if (Arrays.asList(prefs.keys()).contains(prefKey)) {
				byte[] data = prefs.getByteArray(prefKey, null);
				if (data != null) {
					ByteArrayInputStream buffer = new ByteArrayInputStream(data);
					ObjectInput input = new ObjectInputStream(buffer);
					try {
						cacheObj = (WizardCache) input.readObject();
					} catch (Exception e) {
						input.close();
					}
				}
			}
		} catch (Exception e) {
			Activator.getDefault().log(Messages.err, e);
		}
		return cacheObj;
	}

	/**
	 * Method will be used in case of
	 * project delete.
	 * @param prefKey
	 */
	public static void removePreference(String prefKey) {
		Preferences prefs = PluginUtil.getPrefs();
		try {
			if (Arrays.asList(prefs.keys()).contains(prefKey)) {
				prefs.remove(prefKey);
				prefs.flush();
			}
		} catch (BackingStoreException e) {
			Activator.getDefault().log(Messages.err, e);
		}
	}

	/**
	 * Method will be used in case of
	 * project rename.
	 * It will check for entry in preference file with old key,
	 * If its present then will cache value
	 * and create new entry with new key and cached value
	 * and remove old key value pair from preference file.
	 * @param oldKey
	 * @param newKey
	 */
	public static void renamePreference(String oldKey, String newKey) {
		WizardCache oldCacheObj = load(oldKey);
		/*
		 * Check project which is getting renamed
		 * has entry in preference set file
		 * i.e. entry with old name should be present.
		 */
		if (oldCacheObj != null) {
			save(newKey, oldCacheObj);
			removePreference(oldKey);
		}
	}

	public static List<String> getProjKeyList() {
		List<String> keyList = new ArrayList<String>();
		Preferences prefs = PluginUtil.getPrefs();
		try {
			List<String> list =  Arrays.asList(prefs.keys());
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).startsWith(
						String.format("%s%s",
								Activator.PLUGIN_ID,
								com.persistent.util.Messages.proj))) {
					keyList.add(list.get(i));
				}
			}
		} catch (BackingStoreException e) {
			Activator.getDefault().log(Messages.err, e);
		}
		return keyList;
	}
}
