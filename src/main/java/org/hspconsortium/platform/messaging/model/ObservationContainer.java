package org.hspconsortium.platform.messaging.model;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.resource.Observation;

import java.io.Serializable;

public class ObservationContainer extends ResourceContainer implements Serializable {

    private Observation observation;

    public ObservationContainer(Observation observation) {
        super();
        this.observation = observation;
        observation.getCode();
    }

    @Override
    public IResource getResource() {
        return getObservation();
    }

    public Observation getObservation() {
        return observation;
    }

}
