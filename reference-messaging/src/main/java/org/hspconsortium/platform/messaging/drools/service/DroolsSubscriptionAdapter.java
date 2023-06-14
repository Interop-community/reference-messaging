package org.hspconsortium.platform.messaging.drools.service;

import ca.uhn.fhir.context.FhirContext;
import javassist.bytecode.stackmap.BasicBlock.Catch;

import org.apache.coyote.Request;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IDomainResource;
import org.hspconsortium.platform.messaging.service.SubscriptionManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.ServletRequestAttributes;
import java.io.ByteArrayOutputStream;



@Service
public class DroolsSubscriptionAdapter {
    

    private static final Logger logger = LoggerFactory.getLogger(DroolsSubscriptionAdapter.class);

    @Inject
    SubscriptionManagerService subscriptionManagerService;

    private String srcParam;


    public String submitResource(String resourceJson, @Header(value="source", required=false) String source) {

        IBaseResource resource = FhirContext.forDstu3().newJsonParser().parseResource(resourceJson);
        if (resource instanceof IDomainResource) {
            subscriptionManagerService.submitResource((IDomainResource) resource, source);
        } else {
            logger.warn("Attempt to submit invalid resource: " + resourceJson);
        }
        return "Ok";
    }


}
