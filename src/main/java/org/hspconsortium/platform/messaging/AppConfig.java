package org.hspconsortium.platform.messaging;

import org.hspconsortium.platform.messaging.drools.service.DroolsSubscriptionManagerService;
import org.hspconsortium.platform.messaging.service.SubscriptionManagerService;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("classpath:application.properties")
@ImportResource("classpath*:/META-INF/spring/spring-integration-config.xml")
public class AppConfig {

    @Autowired
    Environment env;

    @Bean
    public SubscriptionManagerService subscriptionManagerService() {
        return new DroolsSubscriptionManagerService();
    }

    @Bean
    public KnowledgeBase knowledgeBase() {
        return KnowledgeBaseFactory.newKnowledgeBase();
    }

}
