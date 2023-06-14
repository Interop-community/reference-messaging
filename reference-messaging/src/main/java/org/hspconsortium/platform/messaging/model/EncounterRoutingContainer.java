package org.hspconsortium.platform.messaging.model;



import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.instance.model.api.IDomainResource;
import ca.uhn.fhir.rest.client.IGenericClient;
// import org.hl7.fhir.r4.model.Patient;

import java.io.Serializable;


public class EncounterRoutingContainer extends ResourceRoutingContainer implements Serializable {

    private Encounter encounter;

    private String source;

    public EncounterRoutingContainer(Encounter encounter) {
        super();
        this.encounter = encounter;
    }
    public EncounterRoutingContainer(Encounter encounter, String src) {
        super();
        this.encounter = encounter;
        this.source = src;
    }

    @Override
    public IDomainResource getResource() {
        return getEncounter();
    }

    public Encounter getEncounter() {
        return encounter;
    }

    public String getSubjectRef() {
       return encounter.getSubject().getReference();
    }
    
    public String getSource(){
        return source;
    }
    // public String getPatient(){

    //     System.out.print(source+"/open/"+getSubjectRef());

    //     IGenericClient client = super.getFhirClient(source+"/open");
        
    //     return ctx.newJsonParser().encodeResourceToString(client.read(Patient.class, getSubjectRef().split("\\/")[1]));
    // }




}
