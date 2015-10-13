package org.hspconsortium.platform.messaging;

import org.apache.activemq.command.ActiveMQQueue;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.jms.Queue;

@Configuration
@EnableWebMvc
@PropertySource("classpath:application.properties")
@ImportResource("classpath*:/META-INF/spring/spring-integration-config.xml")
public class AppConfig {

    public static final String PROCESS_RESOURCE_QUEUE = "hspc.messaging.processresource.queue";

    @Autowired
    Environment env;

    @Bean
    public Queue helloJMSQueue() {
        return new ActiveMQQueue(PROCESS_RESOURCE_QUEUE);
    }

    @Bean
    public KnowledgeBase knowledgeBase() {
        return KnowledgeBaseFactory.newKnowledgeBase();
    }

}
