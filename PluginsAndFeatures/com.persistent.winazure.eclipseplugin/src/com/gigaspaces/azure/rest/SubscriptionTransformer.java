package com.gigaspaces.azure.rest;

import com.gigaspaces.azure.model.Subscription;
import com.microsoft.windowsazure.management.models.SubscriptionGetResponse;

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
