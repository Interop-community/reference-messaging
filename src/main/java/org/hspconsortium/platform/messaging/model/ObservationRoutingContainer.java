package org.hspconsortium.platform.messaging.model;


import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.instance.model.api.IDomainResource;

import java.io.Serializable;

public class ObservationRoutingContainer extends ResourceRoutingContainer implements Serializable {

    private Observation observation;

    public ObservationRoutingContainer(Observation observation) {
        super();
        this.observation = observation;
    }

    @Override
    public IDomainResource getResource() {
        return getObservation();
    }

    public Observation getObservation() {
        return observation;
    }

}
