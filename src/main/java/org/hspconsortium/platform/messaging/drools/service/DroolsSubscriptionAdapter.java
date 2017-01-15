package org.hspconsortium.platform.messaging.drools.service;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IDomainResource;
import org.hspconsortium.platform.messaging.service.SubscriptionManagerService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class DroolsSubscriptionAdapter {

    @Inject
    SubscriptionManagerService subscriptionManagerService;

    public String submitResource(String resourceJson) {
        IBaseResource resource = FhirContext.forDstu3().newJsonParser().parseResource(resourceJson);
        subscriptionManagerService.submitResource((IDomainResource)resource);
        return "Ok";
    }
}
