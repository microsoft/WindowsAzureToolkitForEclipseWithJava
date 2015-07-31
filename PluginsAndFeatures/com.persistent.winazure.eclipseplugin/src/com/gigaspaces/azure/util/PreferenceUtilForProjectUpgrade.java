/**
* Copyright Microsoft Corp.
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
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.List;

import org.osgi.service.prefs.Preferences;

import waeclipseplugin.Activator;

import com.microsoftopentechnologies.deploy.util.PublishData;
import com.microsoftopentechnologies.model.Subscription;
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
