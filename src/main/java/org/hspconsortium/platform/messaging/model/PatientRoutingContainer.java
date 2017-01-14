package org.hspconsortium.platform.messaging.model;

import org.apache.commons.lang3.Validate;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.instance.model.api.IDomainResource;

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
    public IDomainResource getResource() {
        return getPatient();
    }

    public Patient getPatient() {
        return patient;
    }

    public long getAgeInMillis() {
        return ageInMillis;
    }
}
