package org.hspconsortium.platform.messaging.controller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.parser.IParser;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.hspconsortium.platform.messaging.model.ObservationContainer;
import org.hspconsortium.platform.messaging.model.ResourceContainer;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.io.IOException;

@RestController
@RequestMapping(value = "resource")
public class ResourceController {

    private static final Logger logger = LoggerFactory.getLogger(ResourceController.class);

    private IParser jsonParser = FhirContext.forDstu2().newJsonParser();

    @Inject
    KnowledgeBase knowledgeBase;

    @RequestMapping(method = RequestMethod.GET)
    public String hello() {
        return "Hello from " + this.getClass().getSimpleName();
    }

    @RequestMapping(method = RequestMethod.POST)
    public String resource(@RequestBody String jsonResource) {
        Validate.notNull(jsonResource);

        // create a drl based on the subscription
        logger.info("Received subscription for registration: " + jsonResource);

        IResource resource = (IResource) jsonParser.parseResource(jsonResource);
        Validate.notNull(resource);

        StatefulKnowledgeSession ksession = knowledgeBase.newStatefulKnowledgeSession();

        ResourceContainer resourceContainer;
        if (resource instanceof Observation) {
            resourceContainer = new ObservationContainer((Observation) resource);
            ksession.insert(resourceContainer);
        } else {
            return "Nothing to do";
        }

        ksession.fireAllRules();

        // todo need to process if the resource matched a rule and was assigned a route
        // process by posting the idPart to the route output
        System.out.println("Resource: " + resource.getId()
                + " Processing Message: " + resourceContainer.getProcessingMessage()
                + " Route: " + resourceContainer.getRouteChannel());

        // if the container has been assigned a route, send the message now
        if (resourceContainer.getRouteChannel() != null) {
            sendSubscriptionMessage(resourceContainer);
        }

        return "Success";
    }

    private void sendSubscriptionMessage(ResourceContainer resourceContainer) {
        try {
            HttpPost postRequest = new HttpPost(resourceContainer.getRouteChannel());
            StringEntity resourceIdEntity = new StringEntity(resourceContainer.getResource().getId().toString());
            postRequest.setEntity(resourceIdEntity);

            CloseableHttpClient httpClient = HttpClients.custom().build();
            CloseableHttpResponse closeableHttpResponse = httpClient.execute(postRequest);
            if (closeableHttpResponse.getStatusLine().getStatusCode() != 200) {
                HttpEntity rEntity = closeableHttpResponse.getEntity();
                String responseString = EntityUtils.toString(rEntity, "UTF-8");
                throw new RuntimeException(String.format("There was a problem with the registration the Launch Context.\n" +
                        "Response Status : %s .\nResponse Detail :%s."
                        , closeableHttpResponse.getStatusLine()
                        , responseString));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
