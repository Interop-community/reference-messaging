package org.hspconsortium.platform.messaging.drools.service;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.resource.Subscription;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.hspconsortium.platform.messaging.drools.factory.RuleFromSubscriptionFactory;
import org.hspconsortium.platform.messaging.model.ObservationContainer;
import org.hspconsortium.platform.messaging.model.PatientContainer;
import org.hspconsortium.platform.messaging.model.ResourceContainer;
import org.hspconsortium.platform.messaging.service.SubscriptionManagerService;
import org.kie.api.io.ResourceType;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderError;
import org.kie.internal.builder.KnowledgeBuilderErrors;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Service
public class DroolsSubscriptionManagerService implements SubscriptionManagerService {

    private static final Logger logger = LoggerFactory.getLogger(DroolsSubscriptionManagerService.class);

    @Inject
    RuleFromSubscriptionFactory ruleFromSubscriptionFactory;

    @Inject
    KnowledgeBase knowledgeBase;

    @Override
    public String registerSubscription(Subscription subscription) {
        String strDrl = ruleFromSubscriptionFactory.create(subscription);

        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(
                ResourceFactory.newInputStreamResource(
                        new ByteArrayInputStream(strDrl.getBytes()),
                        "UTF-8"),
                ResourceType.DRL
        );

        KnowledgeBuilderErrors errors = kbuilder.getErrors();

        if (errors.size() > 0) {
            for (KnowledgeBuilderError error : errors) {
                System.err.println(error);
            }
            throw new IllegalArgumentException("Could not parse knowledge.");
        }

        // add this rule to the commonly shared knowledge base
        knowledgeBase.addKnowledgePackages(kbuilder.getKnowledgePackages());

        logger.info("Subscription registration successful");

        // submit the subscription into the knowledge session
        return "Success";

    }

    @Override
    public String submitResource(IResource resource) {
        ResourceContainer resourceContainer;
        if (resource instanceof Observation) {
            resourceContainer = new ObservationContainer((Observation) resource);
        } else if (resource instanceof Patient) {
            resourceContainer = new PatientContainer((Patient) resource);
        } else {
            return "Nothing to do";
        }

        StatefulKnowledgeSession knowledgeSession = knowledgeBase.newStatefulKnowledgeSession();
        knowledgeSession.insert(resourceContainer);
        knowledgeSession.fireAllRules();

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

    // replace this with camel route
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
                throw new RuntimeException(
                        "Error sending the subscription message to: " + resourceContainer.getRouteChannel()
                                + " Response Status : " + closeableHttpResponse.getStatusLine()
                                + " Response Detail: " + responseString);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
