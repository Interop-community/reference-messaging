package org.hspconsortium.platform.messaging.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hspconsortium.platform.messaging.model.SandboxUserInfo;
import org.hspconsortium.platform.messaging.model.ldap.User;
import org.hspconsortium.platform.messaging.service.ldap.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface SandboxUserRegistrationService {
    String health();

    /**
     * create a Practitioner resource url and update LDAP user account. Escape if the profile url url exists.
     *
     * @return
     */
    String addResourceLink();

    /**
     * @param userInfoRequest
     * @return {
     * "user_id": "ldap userid",
     * "profile_url" : "http://docs.spring.io/spring-integration/reference/html/http.html"
     * }
     */
    int updateSandboxUserProfile(byte[] userInfoRequest);

    @Component
    class Impl implements SandboxUserRegistrationService {

        private static final Logger logger = LoggerFactory.getLogger(SandboxUserRegistrationService.class);

        @Inject
        UserService userService;

        @Override
        public String health() {
            return "http_servlet_response:" + HttpServletResponse.SC_OK;
        }

        @Override
        public String addResourceLink() {
            String ldapHostUri = "ldap://sandbox.hspconsortium.org";
            StringBuffer buffer = new StringBuffer("");
            Iterable<User> allMembers = userService.findAll("");
            for (User u : allMembers) {
                if (u.getProfileUri() != null) {
                    buffer.append(u.getUserName()).append(":");
                    buffer.append(u.getProfileUri()).append(".\n");
                    continue;
                } else {
                    buffer.append(u.getUserName()).append(":");
                    //buffer.append(addPractitionerUriAttributeToLdap(u, ldapHostUri)).append(".\n");
                    buffer.append("\n");
                }
            }
            return buffer.toString();
        }

        @Override
        public int updateSandboxUserProfile(byte[] userInfoRequest) {
            ObjectMapper mapper = new ObjectMapper();
            final String s = new String(userInfoRequest);
            logger.info(String.format("User detail:\t %s\n", s));
            try {
                SandboxUserInfo sandboxUserInfo = mapper.readValue(s, SandboxUserInfo.class);
                Attributes matchAttributes = new BasicAttributes(true); // ignore case
                matchAttributes.put(new BasicAttribute("uid", sandboxUserInfo.getUserId()));
//                matchAttributes.put(new BasicAttribute("cn"));
                User ldapUser = userService.findUser(matchAttributes, "");
                if (ldapUser != null) {
                    ldapUser.setProfileUri(sandboxUserInfo.getProfileUrl());
                    userService.updateUser(ldapUser);

                    logger.info(String.format("ldap attribute for %s (%s) updated with resource uri: %s"
                            , ldapUser.getLdapEntityName()
                            , ldapUser.getUserName()
                            , sandboxUserInfo.getProfileUrl()));
                    return HttpServletResponse.SC_OK;
                }
                else {
                    throw new RuntimeException("Sandbox User not found: " + sandboxUserInfo);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
