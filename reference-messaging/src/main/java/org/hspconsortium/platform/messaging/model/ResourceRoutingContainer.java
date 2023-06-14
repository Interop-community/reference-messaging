package org.hspconsortium.platform.messaging.model;

import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.Subscription;
import org.hl7.fhir.instance.model.api.IDomainResource;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.IGenericClient;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class ResourceRoutingContainer {

    protected Date now;

    // public FhirContext ctx = FhirContext.forR4();

    private List<Subscription.SubscriptionChannelComponent> destinationChannels = new ArrayList<>();

    public ResourceRoutingContainer() {
        now = new Date();
    }

    public Date getNow() {
        return now;
    }

    public List<Subscription.SubscriptionChannelComponent> getDestinationChannels() {
        return destinationChannels;
    }

    public void addDestinationChannel(String type, String endpoint, String payload, String header) {
        Subscription.SubscriptionChannelComponent subscriptionChannelComponent = new Subscription.SubscriptionChannelComponent();
        subscriptionChannelComponent.setType(Subscription.SubscriptionChannelType.valueOf(type));
        subscriptionChannelComponent.setEndpoint(endpoint);
        subscriptionChannelComponent.setPayload(payload);
        subscriptionChannelComponent.getHeader().add(new StringType(header));
        destinationChannels.add(subscriptionChannelComponent);
    }

    public abstract IDomainResource getResource();

    // public IGenericClient getFhirClient(String baseUrl){

    //     return ctx.newRestfulGenericClient(baseUrl);

    // }
}
