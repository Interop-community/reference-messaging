package org.hspconsortium.platform.messaging.model;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.resource.Observation;

import java.io.Serializable;

public class ObservationRoutingContainer extends ResourceRoutingContainer implements Serializable {

    private Observation observation;

    public ObservationRoutingContainer(Observation observation) {
        super();
        this.observation = observation;
    }

    @Override
    public IResource getResource() {
        return getObservation();
    }

    public Observation getObservation() {
        return observation;
    }

}
