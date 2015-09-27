package org.hspconsortium.platform.messaging;

import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@PropertySource("classpath:config/config.properties")
@EnableWebMvc
public class AppConfig {

    @Autowired
    Environment env;

    @Bean
    public KnowledgeBase knowledgeBase() {
        // load known rules
//        ClassPathResource classPathResource =
//                new ClassPathResource("/org/hspconsortium/platform/messaging/BilirubinObservation.drl");
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

        KnowledgeBase knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase();
//        knowledgeBase.addKnowledgePackages(kbuilder.getKnowledgePackages());

        return knowledgeBase;
    }

}
