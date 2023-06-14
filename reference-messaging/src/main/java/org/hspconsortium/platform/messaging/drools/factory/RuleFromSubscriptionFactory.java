package org.hspconsortium.platform.messaging.drools.factory;

import org.hl7.fhir.dstu3.model.Subscription;
import org.springframework.stereotype.Component;

@Component
public class RuleFromSubscriptionFactory {

    private String fhirbaseUrl;

    public String create(Subscription subscription, String src) {
        // extract the resource from the criteria
        String resource = getResourceFromCriteria(subscription.getCriteria());
        this.fhirbaseUrl = src;

        switch (resource) {
            case "Patient":
                return createPatientDroolsRule(subscription);
            case "Observation":
                return createObservationDroolsRule(subscription);
            case "CarePlan":
                return createCarePlanDroolsRule(subscription);
            case "Encounter":
                return createEncounterDroolsRule(subscription);
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
        String firstHeader = null;
        if (subscription.getChannel() != null && subscription.getChannel().getHeader().size() > 0) {
            firstHeader = subscription.getChannel().getHeader().get(0).getValue();
        }
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
        stringBuffer.append("        $c.addDestinationChannel(\n");
        stringBuffer.append("            \"" + subscription.getChannel().getType().toString() + "\",\n");
        stringBuffer.append("            \"" + subscription.getChannel().getEndpoint() + "\",\n");
        stringBuffer.append("            \"" + subscription.getChannel().getPayload() + "\",\n");
        stringBuffer.append("            " + (firstHeader != null ? "\"" + firstHeader  + "\"" : "null") + ")\n");
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
        stringBuffer.append("rule \"Subscription rule: " + fhirbaseUrl + "/" + subscription.getId()  + "\"\n");
        stringBuffer.append("    when\n");
        stringBuffer.append("        $c: ObservationRoutingContainer(\n");
        stringBuffer.append("              getSource() == \"" + fhirbaseUrl + "\"\n");

        // todo this hard-coding is just for the demo
        if (codeOption != null) {
            stringBuffer.append("              getObservation().getCode() != null\n");
            stringBuffer.append("              && getObservation().getCode().getCodingFirstRep() != null\n");
            stringBuffer.append("              && getObservation().getCode().getCodingFirstRep().getCode() == \"" + codeOption + "\"\n");
        }
        stringBuffer.append("            )\n");
        stringBuffer.append("    then\n");
        stringBuffer.append("        $c.addDestinationChannel(\n");
        stringBuffer.append("            \"" + subscription.getChannel().getType().toString() + "\",\n");
        stringBuffer.append("            \"" + subscription.getChannel().getEndpoint() + "\",\n");
        stringBuffer.append("            \"" + subscription.getChannel().getPayload() + "\",\n");
        stringBuffer.append("            \"" + subscription.getChannel().getHeader() + "\")\n");
        stringBuffer.append("end\n");
        System.out.println(stringBuffer.toString());

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
        stringBuffer.append("        $c.addDestinationChannel(\n");
        stringBuffer.append("            \"" + subscription.getChannel().getType().toString() + "\",\n");
        stringBuffer.append("            \"" + subscription.getChannel().getEndpoint() + "\",\n");
        stringBuffer.append("            \"" + subscription.getChannel().getPayload() + "\",\n");
        stringBuffer.append("            \"" + subscription.getChannel().getHeader() + "\")\n");
        stringBuffer.append("end\n");

        System.out.println(stringBuffer.toString());

        return stringBuffer.toString();
    }


    private String createEncounterDroolsRule(Subscription subscription) {
        // create a drl based on the subscription
        String codeOption = getCriteria("code", getCriteriaOptions(subscription.getCriteria()));

        // todo add support for actual criteria
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("package org.hspconsortium.platform.messaging\n");
        stringBuffer.append("dialect \"mvel\"\n");
        stringBuffer.append("import org.hspconsortium.platform.messaging.model.ResourceRoutingContainer\n");
        stringBuffer.append("import org.hspconsortium.platform.messaging.model.EncounterRoutingContainer\n");
        stringBuffer.append("rule \"Subscription rule: " + fhirbaseUrl + "/" + subscription.getId() + "\"\n");
        stringBuffer.append("    when\n");
        stringBuffer.append("        $c: EncounterRoutingContainer(\n");
        stringBuffer.append("              getSource() == \"" + fhirbaseUrl + "\"\n");
        stringBuffer.append("            )\n");
        stringBuffer.append("    then\n");
        stringBuffer.append("        $c.addDestinationChannel(\n");
        stringBuffer.append("            \"" + subscription.getChannel().getType().toString() + "\",\n");
        stringBuffer.append("            \"" + subscription.getChannel().getEndpoint() + "\",\n");
        stringBuffer.append("            \"" + subscription.getChannel().getPayload() + "\",\n");
        stringBuffer.append("            \"" + subscription.getChannel().getHeader() + "\")\n");
        stringBuffer.append("end\n");
        System.out.println(stringBuffer.toString());
        
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
