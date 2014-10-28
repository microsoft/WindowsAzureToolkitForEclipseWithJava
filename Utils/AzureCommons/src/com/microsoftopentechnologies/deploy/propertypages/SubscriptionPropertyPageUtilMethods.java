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
package com.microsoftopentechnologies.deploy.propertypages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.microsoftopentechnologies.deploy.util.PublishData;
import com.microsoftopentechnologies.model.Subscription;


public class SubscriptionPropertyPageUtilMethods {

	public static Object[] getTableContent(Collection<PublishData> publishDatas) {
		List<SubscriptionPropertyPageTableElement> tableRowElements =
				new ArrayList<SubscriptionPropertyPageTableElement>();
		for (PublishData pd : publishDatas) {
			for (Subscription sub : pd.getPublishProfile().getSubscriptions()) {
				SubscriptionPropertyPageTableElement el = new SubscriptionPropertyPageTableElement();
				el.setSubscriptionId(sub.getId());
				el.setSubscriptionName(sub.getName());
				if (!tableRowElements.contains(el)) {
					tableRowElements.add(el);
				}
			}
		}
		SubscriptionPropertyPageTableElements elements = new SubscriptionPropertyPageTableElements();
		elements.setElements(tableRowElements);

		return elements.getElements().toArray();
	}
}
