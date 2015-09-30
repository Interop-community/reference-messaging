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
        return KnowledgeBaseFactory.newKnowledgeBase();
    }

}
