package org.hspconsortium.platform.messaging.controller.user;

import org.apache.log4j.Logger;
import org.hspconsortium.platform.messaging.model.user.SandboxUserInfo;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.UUID;

public class UserClient {
    private static Logger logger = Logger.getLogger(UserClient.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        UserController requestGateway = context.getBean("userRestGateway", UserController.class);
        String reply = requestGateway.health(":" + System.currentTimeMillis());
        logger.info("\n\n++++++++++++ Replied with: " + reply + " ++++++++++++\n");

        String reply2 = requestGateway.createUser(getSandboxUser()) + "";
        logger.info("\n\n++++++++++++ Replied with: " + reply2 + " ++++++++++++\n");
    }

    private static SandboxUserInfo getSandboxUser() {
        SandboxUserInfo userInfo = new SandboxUserInfo();
        userInfo.setCn(UUID.randomUUID().toString());
        userInfo.setSn(UUID.randomUUID().toString());
        userInfo.setOrganization("Intermountain Healthcare");
        userInfo.setDisplayName("Tintin");
        userInfo.setEmployeeNumber("896512");
        userInfo.setEmail("noman.rahman@imail.org");
        userInfo.setProfileUrl("http://localhost:8080/hspc-reference-api/data/Practitioner/332/_history/1");
        userInfo.setUserId("noman.rahman@imail.org");
        userInfo.setUserPassword("password");
        userInfo.setDistinctName("cn=" + userInfo.getCn());
        return userInfo;
    }
}
