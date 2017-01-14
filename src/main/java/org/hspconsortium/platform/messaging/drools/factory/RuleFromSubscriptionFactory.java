package org.hspconsortium.platform.messaging.drools.factory;

import org.hl7.fhir.dstu3.model.Subscription;
import org.springframework.stereotype.Component;

@Component
public class RuleFromSubscriptionFactory {

    public String create(Subscription subscription) {
        // extract the resource from the criteria
        String resource = getResourceFromCriteria(subscription.getCriteria());

        switch (resource) {
            case "Patient":
                return createPatientDroolsRule(subscription);
            case "Observation":
                return createObservationDroolsRule(subscription);
            case "CarePlan":
                return createCarePlanDroolsRule(subscription);
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
        stringBuffer.append("import org.hspconsortium.platform.messaging.model.ResourceRoutingContainer\n");
        stringBuffer.append("import org.hspconsortium.platform.messaging.model.PatientRoutingContainer\n");
        stringBuffer.append("rule \"Subscription rule: " + subscription.getId() + "\"\n");
        stringBuffer.append("    when\n");
        stringBuffer.append("        $c: PatientRoutingContainer()");
        stringBuffer.append("    then\n");
        stringBuffer.append("        $c.addDestinationChannel(\"" + subscription.getChannel().getEndpoint() + "\");\n");
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
        stringBuffer.append("import org.hspconsortium.platform.messaging.model.ResourceRoutingContainer\n");
        stringBuffer.append("import org.hspconsortium.platform.messaging.model.ObservationRoutingContainer\n");
        stringBuffer.append("rule \"Subscription rule: " + subscription.getId() + "\"\n");
        stringBuffer.append("    when\n");
        stringBuffer.append("        $c: ObservationRoutingContainer(\n");
        // todo this hard-coding is just for the demo
        if (codeOption != null) {
            stringBuffer.append("              getObservation().getCode() != null\n");
            stringBuffer.append("              && getObservation().getCode().getCodingFirstRep() != null\n");
            stringBuffer.append("              && getObservation().getCode().getCodingFirstRep().getCode() == \"" + codeOption + "\"\n");
        }
        stringBuffer.append("            )\n");
        stringBuffer.append("    then\n");
        stringBuffer.append("        $c.addDestinationChannel(\"" + subscription.getChannel().getEndpoint() + "\");\n");
        stringBuffer.append("end\n");
        return stringBuffer.toString();
    }

    private String createCarePlanDroolsRule(Subscription subscription) {
        // create a drl based on the subscription
        String codeOption = getCriteria("code", getCriteriaOptions(subscription.getCriteria()));

        // todo add support for actual criteria
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("package org.hspconsortium.platform.messaging\n");
        stringBuffer.append("dialect \"mvel\"\n");
        stringBuffer.append("import org.hspconsortium.platform.messaging.model.ResourceRoutingContainer\n");
        stringBuffer.append("import org.hspconsortium.platform.messaging.model.CarePlanRoutingContainer\n");
        stringBuffer.append("rule \"Subscription rule: " + subscription.getId() + "\"\n");
        stringBuffer.append("    when\n");
        stringBuffer.append("        $c: CarePlanRoutingContainer(\n");
        stringBuffer.append("            )\n");
        stringBuffer.append("    then\n");
        stringBuffer.append("        $c.addDestinationChannel(\"" + subscription.getChannel().getEndpoint() + "\");\n");
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
