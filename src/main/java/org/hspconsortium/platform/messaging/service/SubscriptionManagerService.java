package org.hspconsortium.platform.messaging.service;

import ca.uhn.fhir.model.dstu2.resource.Subscription;

public interface SubscriptionManagerService {

    String registerSubscription(Subscription subscription);

}
