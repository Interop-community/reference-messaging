package org.hspconsortium.platform.messaging.model;

import org.hl7.fhir.instance.model.api.IDomainResource;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public abstract class ResourceRoutingContainer {

    protected Date now;

    private List<String> destinationChannels = new LinkedList<>();

    public ResourceRoutingContainer() {
        now = new Date();
    }

    public Date getNow() {
        return now;
    }

    public List<String> getDestinationChannels() {
        return destinationChannels;
    }

    public void addDestinationChannel(String destinationChannel) {
        destinationChannels.add(destinationChannel);
    }

    public abstract IDomainResource getResource();
}
