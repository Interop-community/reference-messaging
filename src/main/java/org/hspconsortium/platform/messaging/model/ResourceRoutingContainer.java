package org.hspconsortium.platform.messaging.model;

import ca.uhn.fhir.model.api.IResource;

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

    public abstract IResource getResource();
}
