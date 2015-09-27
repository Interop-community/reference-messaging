package org.hspconsortium.platform.messaging.model;

import ca.uhn.fhir.model.api.IResource;

import java.util.Date;

public abstract class ResourceContainer {
    protected Date now;
    private String routeChannel;
    private String processingMessage;

    public ResourceContainer() {
        now = new Date();
    }

    public Date getNow() {
        return now;
    }

    public String getRouteChannel() {
        return routeChannel;
    }

    public void setRouteChannel(String routeChannel) {
        this.routeChannel = routeChannel;
    }

    public String getProcessingMessage() {
        return processingMessage;
    }

    public void setProcessingMessage(String processingMessage) {
        this.processingMessage = processingMessage;
    }

    public abstract IResource getResource();
}
