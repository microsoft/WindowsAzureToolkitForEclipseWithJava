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
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.List;

import org.osgi.service.prefs.Preferences;

import waeclipseplugin.Activator;

import com.microsoftopentechnologies.azurecommons.deploy.util.PublishData;
import com.microsoftopentechnologies.azuremanagementutil.model.Subscription;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
/* Class needs to be removed after 2.5.1 release.
 * Class is added just to take care of project upgrade scenario.
 */
public class PreferenceUtilForProjectUpgrade {
	private static final PreferenceUtilForProjectUpgrade INSTANCE = new PreferenceUtilForProjectUpgrade();
	private static final String PREF_KEY = waeclipseplugin.Activator.PLUGIN_ID + ".settings";

	public static PublishData[] loadOldPreferences() {
		return INSTANCE.loadOldPrefs();
	}

	private PublishData[] loadOldPrefs() {
		Preferences prefs = PluginUtil.getPrefs(waeclipseplugin.Activator.PLUGIN_ID);
		PublishData[] publishDatas = null;
		try {
			byte[] data = prefs.getByteArray(PREF_KEY, null);
			if (data != null) {
				ByteArrayInputStream buffer = new ByteArrayInputStream(data);
				ObjectInput input = new ObjectInputStream(buffer);
				publishDatas = (PublishData[]) input.readObject();
			}
		} catch (IOException e) {
			e.printStackTrace();
			Activator.getDefault().log(Messages.error,e);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			Activator.getDefault().log(Messages.error,e);
		}
		return publishDatas;
	}

	public static String extractSubIdFromOldPublishData(String subscriptionName) {
		String subId = "";
		PublishData[] publishDatas = loadOldPreferences();
		if (publishDatas != null) {
			Subscription sub = findSubscriptionByNameForOldPublishData(
					subscriptionName, Arrays.asList(publishDatas));
			if (sub != null) {
				subId = sub.getSubscriptionID();
			}
		}
		return subId;
	}

	public static Subscription findSubscriptionByNameForOldPublishData(String subscriptionName,
			List<PublishData> PUBLISHS) {
		for (PublishData pd : PUBLISHS) {
			List<Subscription> subscriptions = pd.getPublishProfile().getSubscriptions();
			for (Subscription sub : subscriptions) {
				if (sub.getName().equals(subscriptionName))
					return sub;
			}
		}
		return null;
	}
}
