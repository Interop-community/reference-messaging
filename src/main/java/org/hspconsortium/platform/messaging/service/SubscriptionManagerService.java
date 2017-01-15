package org.hspconsortium.platform.messaging.service;

import org.hl7.fhir.instance.model.api.IDomainResource;

public interface SubscriptionManagerService {

    String health();

    String asString();

    String registerSubscription(String subscriptionStr);

    String submitResource(IDomainResource resource);

    String reset();

}
