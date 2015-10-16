package org.hspconsortium.platform.messaging.service;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.resource.Subscription;

public interface SubscriptionManagerService {

    String health();

    String asString();

    void registerSubscription(Subscription subscription);

    String submitResource(IResource resource);

    String reset();

}
