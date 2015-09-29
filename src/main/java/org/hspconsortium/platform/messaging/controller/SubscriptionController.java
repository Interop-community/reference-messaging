package org.hspconsortium.platform.messaging.controller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Subscription;
import ca.uhn.fhir.parser.IParser;
import org.apache.commons.lang3.Validate;
import org.kie.api.io.ResourceType;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderError;
import org.kie.internal.builder.KnowledgeBuilderErrors;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;

@RestController
@RequestMapping(value = "subscription")
public class SubscriptionController {
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionController.class);

    private IParser jsonParser = FhirContext.forDstu2().newJsonParser();

    @Inject
    KnowledgeBase knowledgeBase;

    @RequestMapping(method = RequestMethod.POST)
    public String subscription(@RequestBody String jsonSubscription) {
        Validate.notNull(jsonSubscription);

        logger.info("Received subscription for registration: " + jsonSubscription);

        Subscription subscription = (Subscription) jsonParser.parseResource(jsonSubscription);

        String strDrl = createDroolsRule(subscription);

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

    private String createDroolsRule(Subscription subscription) {
        // extract the resource from the criteria
        String resource = getResourceFromCriteria(subscription.getCriteria());

        switch (resource) {
            case "Patient":
                return createPatientDroolsRule(subscription);
            case "Observation":
                return createObservationDroolsRule(subscription);
            default:
                throw new RuntimeException("Unsupported resource for criteria: " + subscription.getCriteria());
        }
    }

    private String getResourceFromCriteria(String criteria) {
        String[] split = criteria.split("\\?");

        return split[0];
    }

    private String[] getCriteriaOptions(String criteria) {
        String[] split = criteria.split("\\?");

        if (split.length > 1) {
            String options = split[1];
            return options.split("&");
        }
        return null;
    }

    private String createPatientDroolsRule(Subscription subscription) {
        // create a drl based on the subscription
        // todo add support for actual criteria
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("package org.hspconsortium.platform.messaging\n");
        stringBuffer.append("dialect \"mvel\"\n");
        stringBuffer.append("import org.hspconsortium.platform.messaging.model.ResourceContainer\n");
        stringBuffer.append("import org.hspconsortium.platform.messaging.model.PatientContainer\n");
        stringBuffer.append("rule \"Subscription rule: " + subscription.getId().getIdPart() + "\"\n");
        stringBuffer.append("    when\n");
        stringBuffer.append("        $c: PatientContainer()");
        stringBuffer.append("    then\n");
        stringBuffer.append("        $c.setProcessingMessage(\"Matched!\");\n");
        stringBuffer.append("        $c.setRouteChannel(\"" + subscription.getChannel().getEndpoint() + "\");\n");
        stringBuffer.append("end\n");
        return stringBuffer.toString();
    }

    private String createObservationDroolsRule(Subscription subscription) {
        // create a drl based on the subscription
        String codeOption = getCriteria("code", getCriteriaOptions(subscription.getCriteria()));

        // todo add support for actual criteria
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("package org.hspconsortium.platform.messaging\n");
        stringBuffer.append("dialect \"mvel\"\n");
        stringBuffer.append("import org.hspconsortium.platform.messaging.model.ResourceContainer\n");
        stringBuffer.append("import org.hspconsortium.platform.messaging.model.ObservationContainer\n");
        stringBuffer.append("rule \"Subscription rule: " + subscription.getId().getIdPart() + "\"\n");
        stringBuffer.append("    when\n");
        stringBuffer.append("        $c: ObservationContainer(\n");
        // todo this hard-coding is just for the demo
        if (codeOption != null) {
            stringBuffer.append("              getObservation().getCode() != null\n");
            stringBuffer.append("              && getObservation().getCode().getCodingFirstRep() != null\n");
            stringBuffer.append("              && getObservation().getCode().getCodingFirstRep().getCode() == \"" + codeOption + "\"\n");
        }
        stringBuffer.append("            )\n");
        stringBuffer.append("    then\n");
        stringBuffer.append("        $c.setProcessingMessage(\"Matched!\");\n");
        stringBuffer.append("        $c.setRouteChannel(\"" + subscription.getChannel().getEndpoint() + "\");\n");
        stringBuffer.append("end\n");
        return stringBuffer.toString();
    }

    private String getCriteria(String name, String[] criteriaOptions) {
        if (criteriaOptions != null) {
            for (String option : criteriaOptions) {
                if (option.equals(name)) {
                    return option;
                }
            }
        }
        return null;
    }

}
