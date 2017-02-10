package org.hspconsortium.platform.messaging.model;

import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.instance.model.api.IDomainResource;

import java.io.Serializable;
import java.util.Date;

public class PatientRoutingContainer extends ResourceRoutingContainer implements Serializable {

    private Patient patient;

    private long ageInMillis = Long.MAX_VALUE;

    public PatientRoutingContainer(Patient patient) {
        super();
        this.patient = patient;

        if (patient.getBirthDate() != null) {
            ageInMillis = new Date().getTime() - patient.getBirthDate().getTime();
        }

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
