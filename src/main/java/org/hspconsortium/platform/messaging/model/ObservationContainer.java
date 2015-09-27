package org.hspconsortium.platform.messaging.model;

import ca.uhn.fhir.model.dstu2.resource.Observation;

import java.io.Serializable;

public class ObservationContainer extends Container implements Serializable {

    private Observation observation;

    public ObservationContainer(Observation observation) {
        super();
        this.observation = observation;
        observation.getCode();
    }

    public Observation getObservation() {
        return observation;
    }

}
