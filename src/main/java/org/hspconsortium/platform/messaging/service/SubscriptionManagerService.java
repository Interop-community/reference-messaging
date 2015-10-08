package org.hspconsortium.platform.messaging.service;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.resource.Subscription;

public interface SubscriptionManagerService {

    String registerSubscription(Subscription subscription);

    String submitResource(IResource resource);

}
