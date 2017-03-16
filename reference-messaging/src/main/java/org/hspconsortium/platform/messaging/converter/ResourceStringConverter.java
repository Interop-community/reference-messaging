package org.hspconsortium.platform.messaging.converter;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.instance.model.api.IBaseResource;

public class ResourceStringConverter {
    private static final FhirContext FHIR_CONTEXT = FhirContext.forDstu3();

    public String toString(IBaseResource resource) {
        return FHIR_CONTEXT.newJsonParser().encodeResourceToString(resource);
    }

    public IBaseResource toResource(String json) {
        return (IBaseResource) FHIR_CONTEXT.newJsonParser().parseResource(json);

    }
}
