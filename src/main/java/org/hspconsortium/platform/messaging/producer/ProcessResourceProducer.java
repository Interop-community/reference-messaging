package org.hspconsortium.platform.messaging.producer;

import org.springframework.integration.annotation.Publisher;
import org.springframework.messaging.handler.annotation.Payload;

/**
 * This producer is available for those that want to publish a resource to the ProcessResource queue
 */
public class ProcessResourceProducer {

    @Publisher(channel="testChannel")
    @Payload
    public String hello() {
        return "hello";
    }

}
