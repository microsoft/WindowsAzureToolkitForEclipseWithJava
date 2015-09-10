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
			Preferences prefs = PluginUtil.getPrefs(waeclipseplugin.Activator.PLUGIN_ID);
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
		Preferences prefs = PluginUtil.getPrefs(waeclipseplugin.Activator.PLUGIN_ID);
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
