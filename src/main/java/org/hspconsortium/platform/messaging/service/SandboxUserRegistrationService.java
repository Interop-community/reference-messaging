package org.hspconsortium.platform.messaging.service;

import ca.uhn.fhir.model.dstu2.composite.HumanNameDt;
import ca.uhn.fhir.model.dstu2.resource.Organization;
import ca.uhn.fhir.model.dstu2.resource.Practitioner;
import ca.uhn.fhir.model.dstu2.valueset.OrganizationTypeEnum;
import ca.uhn.fhir.model.dstu2.valueset.PractitionerRoleEnum;
import ca.uhn.fhir.model.dstu2.valueset.PractitionerSpecialtyEnum;
import ca.uhn.fhir.rest.api.MethodOutcome;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hspconsortium.client.auth.credentials.Credentials;
import org.hspconsortium.client.session.clientcredentials.ClientCredentialsSessionFactory;
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
import javax.naming.ldap.LdapName;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface SandboxUserRegistrationService {
    String health();

    /**
     * @param userInfoRequest {
     *                        "user_id": "ldap userid",
     *                        "email": "user@imail.org",
     *                        "organization": "",
     *                        "organization_name": "",
     *                        "distinct_name": "uid=C3YRLOH75KMO5NZ1,ou=users,dc=hspconsortium,dc=org",
     *                        "ldap_host": "ldap://localhost/",
     *                        "display_name": "",
     *                        "profile_url" : "http://docs.spring.io/spring-integration/reference/html/http.html"
     *                        }
     */
    int registerSandboxUserOrganization(byte[] userInfoRequest);

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

        @Inject
        ClientCredentialsSessionFactory<? extends Credentials> ehrSessionFactory;

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
                ldapUser.setProfileUri(sandboxUserInfo.getProfileUrl());
                userService.updateUser(ldapUser);

                logger.info(String.format("ldap attribute for %s (%s) updated with resource uri: %s"
                        , ldapUser.getLdapEntityName()
                        , ldapUser.getUserName()
                        , sandboxUserInfo.getProfileUrl()));
                return HttpServletResponse.SC_OK;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        private String addPractitionerUriAttributeToLdap(User ldapUser, String ldapHostUri) {
            Organization organization = populateUserOrganization(ldapUser, ldapHostUri);
            MethodOutcome savedOrganizationOutcome = ehrSessionFactory
                    .createSession().create().resource(organization).execute();
            logger.info("newly created organization resource id: " + savedOrganizationOutcome.getId());

            Practitioner practitioner = populateUserAsPractitioner(ldapUser, organization);
            MethodOutcome savedPractitionerOutcome = ehrSessionFactory
                    .createSession().create().resource(practitioner).execute();

            logger.info("newly created practitioner resource id: " + savedPractitionerOutcome.getId());

            ldapUser.setProfileUri(savedPractitionerOutcome.getId().getValue());

            userService.updateUser(ldapUser);

            logger.info(String.format("ldap attribute for %s (%s) updated with newly created practitioner resource id: %s"
                    , ldapUser.getLdapEntityName().toString()
                    , ldapUser.getUserName()
                    , savedPractitionerOutcome.getId()));
            return savedPractitionerOutcome.getId().getValue();
        }

        @Override
        public int registerSandboxUserOrganization(byte[] userInfoRequest) {
            ObjectMapper mapper = new ObjectMapper();
            final String s = new String(userInfoRequest);
            logger.info(String.format("User detail:\t %s\n", s));
            try {
                SandboxUserInfo sandboxUserInfo = mapper.readValue(s, SandboxUserInfo.class);
                //sandbox user info comes with the full path of dn so we have to remove the
                //base path since service already has base path setup
                LdapName dn = new LdapName(sandboxUserInfo.getDistinctName());
                Iterable<User> ldapUser = userService.searchByDistinctName(dn);
                if (ldapUser != null && ldapUser.iterator().hasNext())
                    addPractitionerUriAttributeToLdap(ldapUser.iterator().next(), sandboxUserInfo.getLdapHost());

                return HttpServletResponse.SC_OK;

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private Practitioner populateUserAsPractitioner(User ldapUserInfo, Organization managingOrganization) {
            Practitioner practitioner = new Practitioner();
            practitioner.addIdentifier().setSystem(ldapUserInfo.getLdapEntityName()).setValue(ldapUserInfo.getDisplayName());
            practitioner.setActive(true);
            practitioner.setName(new HumanNameDt().setText(ldapUserInfo.getDisplayName()));
            practitioner.addPractitionerRole().setRole(PractitionerRoleEnum.DOCTOR).setManagingOrganization(managingOrganization.getPartOf())
                    .addSpecialty()
                    .setValueAsEnum(PractitionerSpecialtyEnum.CARDIOLOGIST);
            return practitioner;
        }

        private Organization populateUserOrganization(User ldapUserInfo, String ldapHostUri) {
            Organization organization = new Organization();
            organization.addIdentifier().setSystem(ldapHostUri + "/" + ldapUserInfo.getLdapEntityName()).setValue(ldapUserInfo.getUserName());
            organization.setType(OrganizationTypeEnum.HEALTHCARE_PROVIDER);
            organization.setName(ldapUserInfo.getOrganizationName());
            organization.setActive(true);
            return organization;
        }

    }
}
