package org.hspconsortium.platform.messaging.controller.mail;

import org.apache.log4j.Logger;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class EmailClient {
    private static Logger logger = Logger.getLogger(EmailClient.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        EmailController requestGateway = context.getBean("emailRestGateway", EmailController.class);
        String reply = requestGateway.health(":" + System.currentTimeMillis());
        logger.info("\n\n++++++++++++ Replied with: " + reply + " ++++++++++++\n");
    }
}
