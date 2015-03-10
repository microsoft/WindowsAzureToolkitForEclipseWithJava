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
package com.microsoftopentechnologies.azuremanagementutil.rest;

import com.microsoft.windowsazure.management.models.SubscriptionGetResponse;
import com.microsoftopentechnologies.azuremanagementutil.model.Subscription;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public class SubscriptionTransformer {
    public static Subscription transform(SubscriptionGetResponse response) {
        Subscription subscription = new Subscription();
        subscription.setSubscriptionID(response.getSubscriptionID());
        subscription.setSubscriptionName(response.getSubscriptionName());
        subscription.setSubscriptionStatus(response.getSubscriptionStatus().name());
        subscription.setAccountAdminLiveEmailId(response.getAccountAdminLiveEmailId());
        subscription.setServiceAdminLiveEmailId(response.getServiceAdminLiveEmailId());
        subscription.setMaxCoreCount(response.getMaximumCoreCount());
        subscription.setMaxStorageAccounts(response.getMaximumStorageAccounts());
        subscription.setMaxHostedServices(response.getMaximumHostedServices());
        subscription.setCurrentCoreCount(response.getCurrentCoreCount());
        subscription.setCurrentHostedServices(response.getCurrentHostedServices());
        subscription.setCurrentStorageAccounts(response.getCurrentStorageAccounts());
        printSubscription(subscription);
        return subscription;
    }

    private static void printSubscription(Subscription subscription) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Subscription.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            jaxbMarshaller.marshal(subscription, System.out);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
}
