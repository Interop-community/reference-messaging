package org.hspconsortium.platform.messaging.controller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import org.apache.commons.lang3.Validate;
import org.hspconsortium.platform.messaging.model.Container;
import org.hspconsortium.platform.messaging.model.ObservationContainer;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

@RestController
@RequestMapping(value = "resource")
public class ResourceController {

    @Inject
    KnowledgeBase knowledgeBase;

    @RequestMapping(method = RequestMethod.GET)
    public String hello() {
        return "Hello from " + this.getClass().getSimpleName();
    }

    @RequestMapping(method = RequestMethod.POST)
    public String resource(@RequestBody String jsonResource) {
        Validate.notNull(jsonResource);

        IResource resource = (IResource) FhirContext.forDstu2().newJsonParser().parseResource(jsonResource);
        Validate.notNull(resource);

        StatefulKnowledgeSession ksession = knowledgeBase.newStatefulKnowledgeSession();

        Container container;
        if (resource instanceof Observation) {
            container = new ObservationContainer((Observation) resource);
            ksession.insert(container);
        } else {
            return "Nothing to do";
        }

        ksession.fireAllRules();

        // todo need to process if the resource matched a rule and was assigned a route
        // process by posting the idPart to the route output
        System.out.println("Resource: " + resource.getId()
                + " Processing Message: " + container.getProcessingMessage()
                + " Route: " + container.getRouteChannel());

        return "Success";
    }

}
