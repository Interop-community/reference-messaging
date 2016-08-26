package org.hspconsortium.platform.messaging.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hspconsortium.platform.messaging.model.ldap.User;
import org.hspconsortium.platform.messaging.model.user.SandboxUserInfo;
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
import java.util.ArrayList;
import java.util.List;

public interface SandboxUserRegistrationService {
    String health();

    /**
     * create a Practitioner resource url and update LDAP user account. Escape if the searchStatement url url exists.
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

    int createSandboxUser(SandboxUserInfo sbUser);

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
                matchAttributes.put(new BasicAttribute("cn"));
                User ldapUser = userService.findUser(matchAttributes, "");
                if (ldapUser != null) {
                    ldapUser.setProfileUri(sandboxUserInfo.getProfileUrl());
                    userService.updateUser(ldapUser);

                    logger.info(String.format("ldap attribute for %s (%s) updated with resource uri: %s"
                            , ldapUser.getLdapEntityName()
                            , ldapUser.getUserName()
                            , sandboxUserInfo.getProfileUrl()));
                    return HttpServletResponse.SC_OK;
                } else {
                    throw new RuntimeException("Sandbox User not found: " + sandboxUserInfo);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         *
         * @param sbUser
         * {
         * "userId":"noman_rahman@imail.org",
         * "userPassword":"modification",
         * "email":"noman_rahman@imail.org",
         * "organization":"Intermountain Healthcare",
         * "organizationName":"Intermountain Healthcare Name",
         * "distinctName":"cn\u003dbbc72236-25e6-4d8f-bdf9-4790c1e6f44e",
         * "displayName":"Tintin",
         * "ldapHost":"ldap://lpv-hdsvnev02.co.ihc.com:10389",
         * "profileUrl":"https://sandbox.hspconsortium.org/dstu2/hspc-reference-api/data/Practitioner/16824",
         * "cn":"bbc72236-25e6-4d8f-bdf9-4790c1e6f44e",
         * "sn":"e5f77cf5-fc56-4fae-9e9a-d9eaaffa86d3",
         * "employeeNumber":"896512",
         * "additionalProperties":{
         *
         * },
         * "firstName":"18361e77-228d-4192-82f0-002f4ec58c17"
         * }
         * @return
         */
        public int createSandboxUser(SandboxUserInfo sbUser) {

            Attributes matchAttributes = new BasicAttributes(true); // ignore case
            matchAttributes.put(new BasicAttribute("uid", sbUser.getUserId()));
            matchAttributes.put(new BasicAttribute("cn"));
            User user = userService.findUser(matchAttributes, "");
            if (user != null) {
                populateUser(sbUser, user);
                userService.updateUser(user);

                logger.info(String.format("ldap attribute for %s (%s) updated with resource uri: %s"
                        , user.getLdapEntityName()
                        , user.getUserName()
                        , sbUser.getProfileUrl()));
            } else {
                user = new User(sbUser.getDistinctName());
                populateUser(sbUser, user);
                userService.createUser(user);
            }

            return HttpServletResponse.SC_OK;
        }

        /**
         *
         * @param searchFilter
         * @return
         *  String filter = "(&(objectClass=inetOrgPerson)(labeledURI=*sandbox.hspconsortium.org/*))";
         *  String filter = "(%26(objectClass=inetOrgPerson)(labeledURI=*sandbox.hspconsortium.org/*))";
        *   searchSandboxUser(filter, 100);
         */
        public List<SandboxUserInfo> searchSandboxUserByProfile(String searchFilter) {
            List<SandboxUserInfo> userInfoList = new ArrayList();
            User[] users = userService.findUser("", String.format("(&(objectClass=inetOrgPerson)(labeledURI=*%s*))", searchFilter), 100);

            for (User user : users) {
                userInfoList.add(toSandBoxUser(user));
            }
            return userInfoList;
        }

        private SandboxUserInfo toSandBoxUser(User user) {
            SandboxUserInfo userInfo = new SandboxUserInfo();
            userInfo.setDistinctName(user.getLdapEntityName());
            userInfo.setCn(user.getCn());
            userInfo.setLastName(user.getLastName());
            userInfo.setFirstName(user.getFirstName());
            userInfo.setDisplayName(user.getDisplayName());
            userInfo.setEmail(user.getEmail());
            userInfo.setEmployeeNumber(user.getEmployeeNumber());
            userInfo.setOrganization(user.getOrganization());
            userInfo.setOrganizationName(user.getOrganizationName());
            userInfo.setProfileUrl(user.getProfileUri());
            userInfo.setUserId(user.getUserName());
            return userInfo;
        }

        private void populateUser(SandboxUserInfo sbUser, User user) {
            if ((user.getCn() == null) && (sbUser.getCn() != null))
                user.setCn(sbUser.getCn());
            if (sbUser.getSn() != null)
                user.setLastName(sbUser.getSn());
            if (sbUser.getFirstName() != null)
                user.setFirstName(sbUser.getFirstName());
            if (sbUser.getDisplayName() != null)
                user.setDisplayName(sbUser.getDisplayName());
            if (sbUser.getEmail() != null) {
                user.setEmail(sbUser.getEmail());
                user.setUserName(sbUser.getEmail());
            }
            if (sbUser.getUserId() != null) {
                user.setEmail(sbUser.getUserId());
                user.setUserName(sbUser.getUserId());
            }
            if (sbUser.getEmployeeNumber() != null)
                user.setEmployeeNumber(sbUser.getEmployeeNumber());
            if (sbUser.getProfileUrl() != null)
                user.setProfileUri(sbUser.getProfileUrl());
            if (sbUser.getUserPassword() != null)
                user.setUserPassword(sbUser.getUserPassword());
            if (sbUser.getOrganization() != null)
                user.setOrganization(sbUser.getOrganization());
            if (sbUser.getOrganizationName() != null)
                user.setOrganizationName(sbUser.getOrganizationName());
        }
    }
}
