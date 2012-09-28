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

package com.gigaspaces.azure.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.framework.Version;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import waeclipseplugin.Activator;
import com.gigaspaces.azure.rest.RestAPIException;
import com.gigaspaces.azure.tasks.LoadingAccoutListener;
import com.gigaspaces.azure.wizards.WizardCacheManager;

public class PreferenceUtil {

	private static final String PREF_KEY = waeclipseplugin.Activator.PLUGIN_ID + ".settings";
	private static final PreferenceUtil INSTANCE = new PreferenceUtil();
	private static boolean loaded;

	public synchronized static void save() {
		INSTANCE.savePreferences();
	}

	private void savePreferences() {
		
		try {
			Preferences prefs = getPrefs();

			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			ObjectOutput output = new ObjectOutputStream(buffer);

			Collection<PublishData> data = WizardCacheManager.getPublishDatas();

			PublishData[] dataArray = new PublishData[data.size()];

			int i = 0;
			for (PublishData pd1 : data) {
				dataArray[i] = new PublishData();
				dataArray[i++].setPublishProfile(pd1.getPublishProfile());
			}

			try {
				output.writeObject(dataArray);
			} finally {
				output.close();
			}

			prefs.putByteArray(PREF_KEY, buffer.toByteArray());

			prefs.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
		Preferences prefs = getPrefs();

		try {
			byte[] data = prefs.getByteArray(PREF_KEY, null);

			if (data != null) {
				ByteArrayInputStream buffer = new ByteArrayInputStream(data);

				ObjectInput input = new ObjectInputStream(buffer);

				try {
					PublishData[] publishDatas = (PublishData[]) input.readObject();
					listener.setNumberOfAccounts(publishDatas.length);
					for (PublishData pd : publishDatas) {
						try {
							WizardCacheManager.cachePublishData(pd, listener);
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
	
	@SuppressWarnings("deprecation")
	private Preferences getPrefs() {
		
		Preferences prefs = null;
		if (isHelios()) {
			prefs = new InstanceScope().getNode(waeclipseplugin.Activator.PLUGIN_ID);
		} else {
			prefs = InstanceScope.INSTANCE.getNode(waeclipseplugin.Activator.PLUGIN_ID);
		}
		return prefs;
		
	}
	
	private boolean isHelios() {
		
		Version version = Platform.getBundle("org.eclipse.core.runtime").getVersion();
		int majorVersion = version.getMajor();
		if (majorVersion == 3) { // indigo and helios
			int minorVersion = version.getMinor();
			if (minorVersion < 7) { // helios 3.6 and lower versions
				return true;
			}
		}
		return false;
	}

}
