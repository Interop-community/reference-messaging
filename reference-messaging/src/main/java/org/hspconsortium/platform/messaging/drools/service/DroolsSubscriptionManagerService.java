package org.hspconsortium.platform.messaging.drools.service;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.hl7.fhir.dstu3.model.CarePlan;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.Subscription;
import org.hl7.fhir.instance.model.api.IDomainResource;
import org.hspconsortium.platform.messaging.controller.mail.EmailController;
import org.hspconsortium.platform.messaging.converter.ResourceStringConverter;
import org.hspconsortium.platform.messaging.drools.factory.RuleFromSubscriptionFactory;
import org.hspconsortium.platform.messaging.model.CarePlanRoutingContainer;
import org.hspconsortium.platform.messaging.model.ObservationRoutingContainer;
import org.hspconsortium.platform.messaging.model.PatientRoutingContainer;
import org.hspconsortium.platform.messaging.model.ResourceRoutingContainer;
import org.hspconsortium.platform.messaging.model.mail.Message;
import org.hspconsortium.platform.messaging.service.SubscriptionManagerService;
import org.kie.api.definition.rule.Rule;
import org.kie.api.io.ResourceType;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderError;
import org.kie.internal.builder.KnowledgeBuilderErrors;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.definition.KnowledgePackage;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import static org.hspconsortium.platform.messaging.controller.mail.EmailRestDemoClient.PNG_MIME;

@Service
public class DroolsSubscriptionManagerService implements SubscriptionManagerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DroolsSubscriptionManagerService.class);

    private static final String HSPC_LOGO_IMAGE = "templates\\images\\Meld-favicon-16.png";

    @Inject
    private RuleFromSubscriptionFactory ruleFromSubscriptionFactory;

    @Inject
    private EmailController gateway;

    @Inject
    private KnowledgeBase knowledgeBase;

    @Value("${mail.server.sender.address}")
    private String defaultSenderAddress;

    private ResourceStringConverter resourceStringConverter = new ResourceStringConverter();

    @Override
    public String health() {
        return knowledgeBase != null ? "OK" : "Not Initialized";
    }

    @Override
    public String asString() {
        StringBuilder packageBuffer = new StringBuilder("Packages: \n");
        for (KnowledgePackage knowledgePackage : knowledgeBase.getKnowledgePackages()) {
            packageBuffer.append(" - ");
            packageBuffer.append(knowledgePackage.getName());
            packageBuffer.append("\n");

            if (!knowledgePackage.getRules().isEmpty()) {
                packageBuffer.append("    Rules: \n");
                for (Rule rule : knowledgePackage.getRules()) {
                    packageBuffer.append("      - ");
                    packageBuffer.append(rule.getName());
                    packageBuffer.append(" \n");
                }
            }
        }
        return packageBuffer.toString();
    }

    @Override
    public String registerSubscription(String subscriptionStr) {
        Subscription subscription = (Subscription) resourceStringConverter.toResource(subscriptionStr);
        String strDrl = ruleFromSubscriptionFactory.create(subscription);

        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(
                ResourceFactory.newInputStreamResource(
                        new ByteArrayInputStream(strDrl.getBytes()),
                        StandardCharsets.UTF_8.name()),
                ResourceType.DRL
        );

        KnowledgeBuilderErrors errors = kbuilder.getErrors();

        if (errors.size() > 0) {
            for (KnowledgeBuilderError error : errors) {
                LOGGER.error("Error in DRL: " + error.toString());
            }
            throw new IllegalArgumentException("Could not parse knowledge.");
        }

        // add this rule to the commonly shared knowledge base
        knowledgeBase.addKnowledgePackages(kbuilder.getKnowledgePackages());

        LOGGER.info("Subscription registration successful");

        return "Ok";
    }

    @Override
    public String submitResource(IDomainResource resource) {
        ResourceRoutingContainer resourceRoutingContainer;
        if (resource instanceof Observation) {
            resourceRoutingContainer = new ObservationRoutingContainer((Observation) resource);
        } else if (resource instanceof CarePlan) {
            resourceRoutingContainer = new CarePlanRoutingContainer((CarePlan) resource);
        } else if (resource instanceof Patient) {
            resourceRoutingContainer = new PatientRoutingContainer((Patient) resource);
        } else {
            return "Nothing to do";
        }

        StatefulKnowledgeSession knowledgeSession = knowledgeBase.newStatefulKnowledgeSession();
        knowledgeSession.insert(resourceRoutingContainer);
        knowledgeSession.fireAllRules();

        // todo need to process if the resource matched a rule and was assigned a route
        // process by posting the idPart to the route output
        System.out.println("Resource: " + resource.getId()
                + " Route: " + resourceRoutingContainer.getDestinationChannels());

        // if the container has been assigned a route, send the message now
        if (resourceRoutingContainer.getDestinationChannels() != null) {
            sendSubscriptionMessage(resourceRoutingContainer);
        }

        return "Success";
    }

    @Override
    public String reset() {
        for (KnowledgePackage knowledgePackage : knowledgeBase.getKnowledgePackages()) {
            knowledgeBase.removeKnowledgePackage(knowledgePackage.getName());
        }
        return "OK";
    }

    // replace this with camel route
    private void sendSubscriptionMessage(ResourceRoutingContainer resourceRoutingContainer) {
        for (Subscription.SubscriptionChannelComponent destinationChannel : resourceRoutingContainer.getDestinationChannels()) {
            LOGGER.info("Sending subscription message for: " + destinationChannel.getEndpoint());
            switch (destinationChannel.getType()) {
                case EMAIL:
                    sendEmailChannelMessage(destinationChannel, resourceRoutingContainer);
                    break;
                case SMS:
                    sendSmsChannelMessage(destinationChannel, resourceRoutingContainer);
                    break;
                case RESTHOOK:
                    sendRestHookChannelMessage(destinationChannel, resourceRoutingContainer);
                    break;
                case WEBSOCKET:
                    break;
                case MESSAGE:
                    break;
                default:
                    break;
            }
        }
    }

    private void sendEmailChannelMessage(Subscription.SubscriptionChannelComponent destinationChannel, ResourceRoutingContainer resourceRoutingContainer) {
        if (destinationChannel.getEndpoint() != null) {
            try {
                // endpoint is in the form: "mailto:someone@example.com"
                String[] endpointParts = destinationChannel.getEndpoint().split(":");

                Message message = new Message(true);
                message.setTemplateFormat(Message.TemplateFormat.HTML);
                message.setAcceptHtmlMessage(true);
                message.setTemplateName("email-subscriptionmessage");
                message.addRecipient(endpointParts[1]);
                message.setSenderEmail(defaultSenderAddress);
                String subject="";
                for(StringType s : destinationChannel.getHeader())
                {
                    subject=subject + s.getValueNotNull() + ",";
                }
                message.setSubject(destinationChannel.hasHeader() ? subject  : "Resource Matching Subscription" );
                message.addResource("company-logo", PNG_MIME, getImageFile(HSPC_LOGO_IMAGE, "png"));
                String resourceType = resourceRoutingContainer.getResource().getClass().getSimpleName();
                message.addVariable("resourceType", resourceType);
                message.addVariable("resourceTypeStatement", resourceType + " has been added or updated");
                message.addVariable("sandboxLink", "https://sandbox.hspconsortium.org");

                LOGGER.info("Sending email...");
                Map auditInformation = gateway.sendEmail(message);
                LOGGER.info("Done sending email");
            } catch (RuntimeException e) {
                LOGGER.warn("Error sending email on error channel: " + e.getMessage());
            }
        }
    }

    
    private void sendSmsChannelMessage(Subscription.SubscriptionChannelComponent destinationChannel, ResourceRoutingContainer resourceRoutingContainer) {
        if (destinationChannel.getEndpoint() != null) {
            try {

                SnsClient snsClient = SnsClient.builder()
                .region(Region.US_EAST_1)
                .build();
                IDomainResource resourceType = resourceRoutingContainer.getResource(); //.getClass().getSimpleName();

                String message = "Broward Notification: " +
                                 resourceType.getClass().getSimpleName() +
                                 " resource of the patient that you are subscribed to has been added or updated.";  

                // endpoint is in the form: "tel:+1555-345-5555"
                String[] endpointParts = destinationChannel.getEndpoint().split(":");

                PublishRequest request = PublishRequest.builder()
                .message(message)
                .phoneNumber(endpointParts[1])
                .build();

                LOGGER.info("Sending sms...");
                PublishResponse result = snsClient.publish(request);
                LOGGER.info(result.messageId() + " Message sent. Status was " + result.sdkHttpResponse().statusCode());

                snsClient.close();

            } catch (SnsException  e) {
                LOGGER.warn("Error sending sms via sns: " + e.awsErrorDetails().errorMessage());
            }
            catch (RuntimeException e) {
                LOGGER.warn("Error sending sms on error channel: " + e.getMessage());
            }
        }
    }

    private void sendRestHookChannelMessage(Subscription.SubscriptionChannelComponent destinationChannel, ResourceRoutingContainer resourceRoutingContainer) {
        try {
            HttpPost postRequest = new HttpPost(destinationChannel.getEndpoint());
            if (resourceRoutingContainer != null
                    && resourceRoutingContainer.getResource() != null
                    && resourceRoutingContainer.getResource().getId() != null) {
                StringEntity resourceIdEntity = new StringEntity(resourceRoutingContainer.getResource().getId());
                postRequest.setEntity(resourceIdEntity);

                CloseableHttpClient httpClient = HttpClients.custom().build();
                CloseableHttpResponse closeableHttpResponse = httpClient.execute(postRequest);
                if (closeableHttpResponse.getStatusLine().getStatusCode() != 200) {
                    HttpEntity rEntity = closeableHttpResponse.getEntity();
                    String responseString = EntityUtils.toString(rEntity, StandardCharsets.UTF_8);
                    throw new RuntimeException(
                            "Error sending the subscription message to: " + resourceRoutingContainer.getDestinationChannels()
                                    + " Response Status : " + closeableHttpResponse.getStatusLine()
                                    + " Response Detail: " + responseString);
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Error sending hooks channel: " + e.getMessage());
        }
    }

    private byte[] getImageFile(String pathName, String imageType) {
        BufferedImage img;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            ClassPathResource cpr = new ClassPathResource(pathName);
            final File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".tmp");
            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                IOUtils.copy(cpr.getInputStream(), out);
            }
            img = ImageIO.read(tempFile);
            ImageIO.write(img, imageType, baos);
            baos.flush();
            byte[] imageInByte = baos.toByteArray();
            baos.close();
            return imageInByte;
        } catch (IOException e) {
        }
        return null;
    }

}
