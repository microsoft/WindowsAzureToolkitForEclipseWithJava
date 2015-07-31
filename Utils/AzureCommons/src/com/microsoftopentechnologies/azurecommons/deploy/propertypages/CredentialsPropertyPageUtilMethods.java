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
