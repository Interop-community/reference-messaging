package org.hspconsortium.platform.messaging.drools.service;

import ca.uhn.fhir.model.dstu2.resource.Subscription;
import org.hspconsortium.platform.messaging.drools.factory.RuleFromSubscriptionFactory;
import org.hspconsortium.platform.messaging.service.SubscriptionManagerService;
import org.kie.api.io.ResourceType;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderError;
import org.kie.internal.builder.KnowledgeBuilderErrors;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;

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
}
