package org.hspconsortium.platform.messaging.model;

import java.util.Date;

public class Container {
    protected Date now;
    private String routeChannel;
    private String processingMessage;

    public Container() {
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
}
