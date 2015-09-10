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

package com.microsoftopentechnologies.azurecommons.deploy.propertypages;

import java.util.ArrayList;
import java.util.List;

import com.microsoftopentechnologies.azurecommons.deploy.util.PublishData;
import com.microsoftopentechnologies.azurecommons.deploy.util.PublishProfile;
import com.microsoftopentechnologies.azurecommons.deploy.wizard.WizardCacheManagerUtilMethods;
import com.microsoftopentechnologies.azuremanagementutil.model.Subscription;

public class CredentialsPropertyPageUtilMethods {

	public static PublishData createPublishData(String subsciptionId) {
		PublishData pd = new PublishData();
		pd.setPublishProfile(new PublishProfile());

		List<Subscription> subs = new ArrayList<Subscription>();

		Subscription s = new Subscription();
		s.setSubscriptionID(subsciptionId);
		subs.add(s);
		pd.getPublishProfile().setSubscriptions(subs);
		return pd;
	}

	public static boolean doesSubscriptionExist(PublishData publishData, String subsciptionId) {
		List<Subscription> subs = publishData.getPublishProfile().getSubscriptions();

		for (int i = 0; i < subs.size(); i++) {
			Subscription s = subs.get(i);

			if (s.getSubscriptionID().equals(subsciptionId)) {
				return true;
			}
		}
		return false;
	}
	
	public static PublishData handleAddAndEdit(String subsciptionId, List<PublishData> PUBLISHS) {
		PublishData pd = WizardCacheManagerUtilMethods.findPublishDataBySubscriptionId(subsciptionId, PUBLISHS);
		if (pd == null) {
			pd = createPublishData(subsciptionId);
			return pd;
		}
		if (!doesSubscriptionExist(pd, subsciptionId)) {
			Subscription s = new Subscription();
			s.setSubscriptionID(subsciptionId);
			pd.getPublishProfile().getSubscriptions().add(s);
		} 
		else {
			return null; // TODO - bad practice to return null as a valid return value.
		}
		return pd;
	}
}
