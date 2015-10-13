package org.hspconsortium.platform.messaging.controller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.parser.IParser;
import org.apache.commons.lang3.Validate;
import org.hspconsortium.platform.messaging.service.SubscriptionManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

@RestController
@RequestMapping(value = "/resource")
public class ResourceController {

    private static final Logger logger = LoggerFactory.getLogger(ResourceController.class);

    private IParser jsonParser = FhirContext.forDstu2().newJsonParser();

    @Inject
    SubscriptionManagerService subscriptionManagerService;

    @RequestMapping(value = "/health", method = RequestMethod.GET)
    public String health() {
        return "Ok";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String resource(@RequestBody String jsonResource) {
        Validate.notNull(jsonResource);

        // create a drl based on the subscription
        logger.info("Received resource for processing: " + jsonResource);

        IResource resource = (IResource) jsonParser.parseResource(jsonResource);
        Validate.notNull(resource);

        return subscriptionManagerService.submitResource(resource);
    }
}
