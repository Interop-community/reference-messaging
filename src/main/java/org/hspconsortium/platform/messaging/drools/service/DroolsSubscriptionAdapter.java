package org.hspconsortium.platform.messaging.drools.service;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IDomainResource;
import org.hspconsortium.platform.messaging.service.SubscriptionManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class DroolsSubscriptionAdapter {

    private static final Logger logger = LoggerFactory.getLogger(DroolsSubscriptionAdapter.class);

    @Inject
    SubscriptionManagerService subscriptionManagerService;

    public String submitResource(String resourceJson) {
        IBaseResource resource = FhirContext.forDstu3().newJsonParser().parseResource(resourceJson);
        if (resource instanceof IDomainResource) {
            subscriptionManagerService.submitResource((IDomainResource) resource);
        } else {
            logger.warn("Attempt to submit invalid resource: " + resourceJson);
        }
        return "Ok";
    }
}
