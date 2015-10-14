package org.hspconsortium.platform.messaging.model;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import org.apache.commons.lang3.Validate;

import java.io.Serializable;
import java.util.Date;

public class PatientRoutingContainer extends ResourceRoutingContainer implements Serializable {

    private Patient patient;

    private long ageInMillis;

    public PatientRoutingContainer(Patient patient) {
        super();
        this.patient = patient;

        Validate.notNull(patient.getBirthDate());
        ageInMillis =  new Date().getTime() - patient.getBirthDate().getTime();

    }

    @Override
    public IResource getResource() {
        return getPatient();
    }

    public Patient getPatient() {
        return patient;
    }

    public long getAgeInMillis() {
        return ageInMillis;
    }
}
