package org.hspconsortium.platform.messaging.controller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Subscription;
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
@RequestMapping(value = "/subscription")
public class SubscriptionController {
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionController.class);

    private IParser jsonParser = FhirContext.forDstu2().newJsonParser();

    @Inject
    SubscriptionManagerService subscriptionManagerService;

    @RequestMapping(value = "/health", method = RequestMethod.GET)
    public String health() {
        return "Ok";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String subscription(@RequestBody String jsonSubscription) {
        Validate.notNull(jsonSubscription);

        logger.info("Received subscription for registration: " + jsonSubscription);

        Subscription subscription = (Subscription) jsonParser.parseResource(jsonSubscription);

        return subscriptionManagerService.registerSubscription(subscription);
    }
}
