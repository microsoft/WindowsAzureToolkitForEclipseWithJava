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
import java.util.Collection;
import java.util.List;

import com.microsoftopentechnologies.azurecommons.deploy.util.PublishData;
import com.microsoftopentechnologies.azuremanagementutil.model.Subscription;


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
