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
import java.io.ObjectInput;
import java.io.ObjectInputStream;
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

	public static WizardCache load(String prefKey){
		WizardCache cacheObj = INSTANCE.loadPreferences(prefKey);
		return cacheObj;
	}

	private WizardCache loadPreferences(String prefKey) {
		WizardCache cacheObj = null;
		Preferences prefs = PluginUtil.getPrefs(waeclipseplugin.Activator.PLUGIN_ID);
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

	public static List<String> getProjKeyList() {
		List<String> keyList = new ArrayList<String>();
		Preferences prefs = PluginUtil.getPrefs(waeclipseplugin.Activator.PLUGIN_ID);
		try {
			List<String> list =  Arrays.asList(prefs.keys());
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).startsWith(
						String.format("%s%s", Activator.PLUGIN_ID, com.persistent.util.Messages.proj))) {
					keyList.add(list.get(i));
				}
			}
		} catch (BackingStoreException e) {
			Activator.getDefault().log(Messages.err, e);
		}
		return keyList;
	}
}
