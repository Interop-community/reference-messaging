package org.hspconsortium.platform.messaging.service;

import org.hl7.fhir.dstu3.model.Subscription;
import org.hl7.fhir.instance.model.api.IDomainResource;

public interface SubscriptionManagerService {

    String health();

    String asString();

    void registerSubscription(Subscription subscription);

    String submitResource(IDomainResource resource);

    String reset();

}
