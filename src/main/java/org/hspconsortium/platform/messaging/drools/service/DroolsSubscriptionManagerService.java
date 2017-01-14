package org.hspconsortium.platform.messaging.drools.service;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.resource.CarePlan;
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
import org.hspconsortium.platform.messaging.model.CarePlanRoutingContainer;
import org.hspconsortium.platform.messaging.model.ObservationRoutingContainer;
import org.hspconsortium.platform.messaging.model.PatientRoutingContainer;
import org.hspconsortium.platform.messaging.model.ResourceRoutingContainer;
import org.hspconsortium.platform.messaging.service.SubscriptionManagerService;
import org.kie.api.definition.rule.Rule;
import org.kie.api.io.ResourceType;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderError;
import org.kie.internal.builder.KnowledgeBuilderErrors;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.definition.KnowledgePackage;
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
    public String health() {
        return knowledgeBase != null ? "OK" : "Not Initialized";
    }

    @Override
    public String asString() {
        StringBuffer packageBuffer = new StringBuffer("Packages: \n");
        for (KnowledgePackage knowledgePackage : knowledgeBase.getKnowledgePackages()) {
            packageBuffer.append(" - " + knowledgePackage.getName() + "\n");

            if (!knowledgePackage.getRules().isEmpty()) {
                packageBuffer.append("    Rules: \n");
                for (Rule rule : knowledgePackage.getRules()) {
                    packageBuffer.append("      - " + rule.getName() + " \n");
                }
            }
        }
        return packageBuffer.toString();
    }

    @Override
    public void registerSubscription(Subscription subscription) {
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
    }

    @Override
    public String submitResource(IResource resource) {
        ResourceRoutingContainer resourceRoutingContainer;
        if (resource instanceof Observation) {
            resourceRoutingContainer = new ObservationRoutingContainer((Observation) resource);
        } else if (resource instanceof CarePlan) {
                resourceRoutingContainer = new CarePlanRoutingContainer((CarePlan) resource);
        } else if (resource instanceof Patient) {
            resourceRoutingContainer = new PatientRoutingContainer((Patient) resource);
        } else {
            return "Nothing to do";
        }

        StatefulKnowledgeSession knowledgeSession = knowledgeBase.newStatefulKnowledgeSession();
        knowledgeSession.insert(resourceRoutingContainer);
        knowledgeSession.fireAllRules();

        // todo need to process if the resource matched a rule and was assigned a route
        // process by posting the idPart to the route output
        System.out.println("Resource: " + resource.getId()
                + " Route: " + resourceRoutingContainer.getDestinationChannels());

        // if the container has been assigned a route, send the message now
        if (resourceRoutingContainer.getDestinationChannels() != null) {
            sendSubscriptionMessage(resourceRoutingContainer);
        }

        return "Success";
    }

    @Override
    public String reset() {
        for (KnowledgePackage knowledgePackage : knowledgeBase.getKnowledgePackages()) {
            knowledgeBase.removeKnowledgePackage(knowledgePackage.getName());
        }
        return "OK";
    }

    // replace this with camel route
    private void sendSubscriptionMessage(ResourceRoutingContainer resourceRoutingContainer) {
        for (String destinationChannel : resourceRoutingContainer.getDestinationChannels()) {
            try {
                HttpPost postRequest = new HttpPost(destinationChannel);
                StringEntity resourceIdEntity = new StringEntity(resourceRoutingContainer.getResource().getId().toString());
                postRequest.setEntity(resourceIdEntity);

                CloseableHttpClient httpClient = HttpClients.custom().build();
                CloseableHttpResponse closeableHttpResponse = httpClient.execute(postRequest);
                if (closeableHttpResponse.getStatusLine().getStatusCode() != 200) {
                    HttpEntity rEntity = closeableHttpResponse.getEntity();
                    String responseString = EntityUtils.toString(rEntity, "UTF-8");
                    throw new RuntimeException(
                            "Error sending the subscription message to: " + resourceRoutingContainer.getDestinationChannels()
                                    + " Response Status : " + closeableHttpResponse.getStatusLine()
                                    + " Response Detail: " + responseString);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
