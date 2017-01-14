package org.hspconsortium.platform.messaging.model;


import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.resource.CarePlan;

import java.io.Serializable;

public class CarePlanRoutingContainer extends ResourceRoutingContainer implements Serializable {

    private CarePlan carePlan;

    public CarePlanRoutingContainer(CarePlan carePlan) {
        super();
        this.carePlan = carePlan;
    }

    @Override
    public IResource getResource() {
        return getCarePlan();
    }

    public CarePlan getCarePlan() {
        return carePlan;
    }

}
