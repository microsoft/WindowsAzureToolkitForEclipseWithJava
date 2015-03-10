/**
 * Copyright 2015 Microsoft Open Technologies, Inc.
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

package com.gigaspaces.azure.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Map;

import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import waeclipseplugin.Activator;

import com.gigaspaces.azure.wizards.WizardCacheManager;
import com.microsoftopentechnologies.azurecommons.deploy.tasks.LoadingAccoutListener;
import com.microsoftopentechnologies.azurecommons.deploy.util.PublishData;
import com.microsoftopentechnologies.azurecommons.exception.RestAPIException;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;

public class PreferenceUtil {

	private static final String PREF_KEY_PS_PER_SUB = waeclipseplugin.Activator.PLUGIN_ID
			+ "_PS_PER_SUB" + ".settings";
	private static final PreferenceUtil INSTANCE = new PreferenceUtil();
	private static boolean loaded;

	public synchronized static void save() {
		INSTANCE.savePreferences();
	}

	private void savePreferences() {
		ByteArrayOutputStream buffer = null;
		try {
			Preferences prefs = PluginUtil.getPrefs();
			buffer = new ByteArrayOutputStream();
			ObjectOutput output = new ObjectOutputStream(buffer);
			Map<String, String> publishSettingsPerSubscriptionMap = WizardCacheManager.getPublishSettingsPerSubscription();
			try {
				output.writeObject(publishSettingsPerSubscriptionMap);
			} finally {
				output.close();
			}
			prefs.putByteArray(PREF_KEY_PS_PER_SUB, buffer.toByteArray());
			prefs.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				buffer.reset();
				buffer.close();
			} catch (IOException ioException) {
				//just ignore
			}
		}
	}

	public static boolean isLoaded() {
		return loaded;
	}

	public static void setLoaded(boolean bool) {
		loaded = bool;
	}

	public static void load(LoadingAccoutListener listener) throws RestAPIException {
		INSTANCE.loadPreferences(listener);
	}

	private void loadPreferences(LoadingAccoutListener listener) {
		Preferences prefs = PluginUtil.getPrefs();
		// load publishsettingspersubscription data
		try {
			byte[] data = prefs.getByteArray(PREF_KEY_PS_PER_SUB, null);
			if (data != null) {
				ByteArrayInputStream buffer = new ByteArrayInputStream(data);
				ObjectInput input = new ObjectInputStream(buffer);

				try {
					@SuppressWarnings("unchecked")
					Map<String, String> publishSettingsPerSubscription = (Map<String, String>) input.readObject();
					WizardCacheManager.addPublishSettingsPerSubscription(publishSettingsPerSubscription);
					// To do - Need to test below function call.
					// Test case - Publish wizard reload data while coming via Subscription property page
					WizardCacheManager.getPublishDataList().clear();
					WizardCacheManager.preparePubDataPerFileMap();
					Map<String, PublishData> map = WizardCacheManager.getPubDataPerFileMap();
					listener.setNumberOfAccounts(map.size());
					for (Map.Entry<String, PublishData> entry : map.entrySet()) {
						// Key - pubFilePath and Value - PublishData
						String pubFilePath = entry.getKey();
						PublishData pubData = entry.getValue();
						try {
							if (pubFilePath.startsWith("empty")) {
								WizardCacheManager.cachePublishData(null, pubData, listener);
							} else {
								WizardCacheManager.cachePublishData(
										new File(pubFilePath), pubData, listener);
							}
						} catch (RestAPIException e) {
							Activator.getDefault().log(Messages.error, e);
						}
					}
				} finally {
					input.close();
				}
			}
		} catch (IOException e) {
			Activator.getDefault().log(Messages.error,e);
		} catch (ClassNotFoundException e) {
			Activator.getDefault().log(Messages.error,e);
		}
	}
}
