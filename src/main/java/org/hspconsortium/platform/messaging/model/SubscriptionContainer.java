package org.hspconsortium.platform.messaging.model;

public class SubscriptionContainer {

    private String in;

    private String out;

    public SubscriptionContainer(String in) {
        this.in = in;
    }

    public String getIn() {
        return in;
    }

    public void setIn(String in) {
        this.in = in;
    }

    public String getOut() {
        return out;
    }

    public void setOut(String out) {
        this.out = out;
    }
}
