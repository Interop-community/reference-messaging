package org.hspconsortium.platform.messaging.model;


import org.hl7.fhir.dstu3.model.CarePlan;
import org.hl7.fhir.instance.model.api.IDomainResource;

import java.io.Serializable;

public class CarePlanRoutingContainer extends ResourceRoutingContainer implements Serializable {

    private CarePlan carePlan;

    public CarePlanRoutingContainer(CarePlan carePlan) {
        super();
        this.carePlan = carePlan;
    }

    @Override
    public IDomainResource getResource() {
        return getCarePlan();
    }

    public CarePlan getCarePlan() {
        return carePlan;
    }

}
