package org.hspconsortium.platform.messaging.drools.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IResource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hspconsortium.platform.messaging.service.SubscriptionManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class DroolsSubscriptionAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DroolsSubscriptionAdapter.class);

    @Inject
    SubscriptionManagerService subscriptionManagerService;

    public void submitResource(String resourceJson) {
        LOGGER.info("**************************************************");
        LOGGER.info("Received message for processing: \n" + resourceJson);
        LOGGER.info("**************************************************");
        IBaseResource resource = FhirContext.forDstu2().newJsonParser().parseResource(resourceJson);
        LOGGER.info("Submitting resource: " + resource.getIdElement());
        subscriptionManagerService.submitResource((IResource)resource);
    }
}
