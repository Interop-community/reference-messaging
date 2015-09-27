//package org.hspconsortium.platform.messaging.service;
//
//import org.hspconsortium.platform.messaging.example.SubscriptionContainer;
//import org.kie.api.io.ResourceType;
//import org.kie.internal.KnowledgeBase;
//import org.kie.internal.KnowledgeBaseFactory;
//import org.kie.internal.builder.KnowledgeBuilder;
//import org.kie.internal.builder.KnowledgeBuilderError;
//import org.kie.internal.builder.KnowledgeBuilderErrors;
//import org.kie.internal.builder.KnowledgeBuilderFactory;
//import org.kie.internal.io.ResourceFactory;
//import org.kie.internal.runtime.StatefulKnowledgeSession;
//import org.springframework.core.io.ClassPathResource;
//
//import java.io.IOException;
//
//public class SubscriptionExample {
//
//    public static final void main(String[] args) {
//
//        KnowledgeBase kbase = loadKnowledgeBase();
//
//        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
//
//        SubscriptionContainer subscriptionContainer = new SubscriptionContainer("hello");
//        ksession.insert(subscriptionContainer);
//
//        SubscriptionContainer subscriptionContainer2 = new SubscriptionContainer("good bye");
//        ksession.insert(subscriptionContainer2);
//
//        ksession.fireAllRules();
//
//        System.out.println("In: " + subscriptionContainer.getIn()
//                + " Out: " + subscriptionContainer.getOut());
//
//        System.out.println("In: " + subscriptionContainer2.getIn()
//                + " Out: " + subscriptionContainer2.getOut());
//
//        ksession.dispose();
//    }
//
//    private static KnowledgeBase loadKnowledgeBase() {
//        ClassPathResource classPathResource = new ClassPathResource("/org/hspconsortium/platform/messaging/example/SubscriptionTest.drl");
//
//        KnowledgeBuilder kbuilder = null;
//        try {
//            kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
//            kbuilder.add(
//                    ResourceFactory.newInputStreamResource(classPathResource.getInputStream()),
//                    ResourceType.DRL);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        KnowledgeBuilderErrors errors = kbuilder.getErrors();
//
//        if (errors.size() > 0) {
//            for (KnowledgeBuilderError error : errors) {
//                System.err.println(error);
//            }
//            throw new IllegalArgumentException("Could not parse knowledge.");
//        }
//
//        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
//        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
//
//        return kbase;
//    }
//}
