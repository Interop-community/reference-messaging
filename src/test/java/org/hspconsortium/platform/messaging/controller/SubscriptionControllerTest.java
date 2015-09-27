package org.hspconsortium.platform.messaging.controller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Subscription;
import org.hspconsortium.platform.messaging.controller.SubscriptionController;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MockServletContext.class)
@WebAppConfiguration
public class SubscriptionControllerTest {

    private MockMvc mvc;

    @Before
    public void setUp() throws Exception {
        mvc = MockMvcBuilders.standaloneSetup(new SubscriptionController()).build();
    }

    @Test
    public void helloTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/subscription").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Hello from " + SubscriptionController.class.getSimpleName())));
    }

    @Test
    @Ignore
    public void subscriptionTest() throws Exception {
        Subscription subscription = new Subscription();
        Subscription.Channel channel = new Subscription.Channel()
                .setEndpoint("Endpoint");
        subscription.setChannel(channel);
        String json = FhirContext.forDstu2().newJsonParser().encodeResourceToString(subscription);
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.post("/subscription").content(json));
        mvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/subscription")
                                .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        equalTo("Success")));
    }
}
