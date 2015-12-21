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
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.naming.ldap.LdapName;
import javax.servlet.http.HttpServletResponse;

public interface SandboxUserRegistrationService {
    String health();

    void registerSandboxUserOrganization(byte[] userInfoRequest);

    String addResourceLink();

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
            Iterable<User> allMembers = userService.findAll("ou=users");
            for (User u : allMembers) {
                buffer.append(u.getUserName()).append(":");
                buffer.append(addPractitionerUriAttributeToLdap(u, ldapHostUri)).append(".\n");
            }
            return buffer.toString();
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

            userService.updateUser(ldapUser.getId().toString(), ldapUser);

            logger.info(String.format("ldap attribute for %s (%s) updated with newly created practitioner resource id: %s"
                    , ldapUser.getId().toString()
                    , ldapUser.getUserName()
                    , savedPractitionerOutcome.getId()));
            return savedPractitionerOutcome.getId().getValue();
        }

        @Override
        public void registerSandboxUserOrganization(byte[] userInfoRequest) {
            ObjectMapper mapper = new ObjectMapper();
            final String s = new String(userInfoRequest);

            try {
                SandboxUserInfo sandboxUserInfo = mapper.readValue(s, SandboxUserInfo.class);
                //sandbox user info comes with the full path of dn so we have to remove the
                //base path since service already has base path setup
                LdapName dn = LdapUtils.newLdapName(sandboxUserInfo.getDistinctName());
                LdapName basePath = userService.getBaseLdapPath();
                LdapName userDn = LdapUtils.removeFirst(dn, basePath);

                User ldapUser = userService.findUser(userDn);

                addPractitionerUriAttributeToLdap(ldapUser, sandboxUserInfo.getLdapHost());

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private Practitioner populateUserAsPractitioner(User ldapUserInfo, Organization managingOrganization) {
            Practitioner practitioner = new Practitioner();
            practitioner.addIdentifier().setSystem(ldapUserInfo.getId().toString()).setValue(ldapUserInfo.getDisplayName());
            practitioner.setActive(true);
            practitioner.setName(new HumanNameDt().setText(ldapUserInfo.getDisplayName()));
            practitioner.addPractitionerRole().setRole(PractitionerRoleEnum.DOCTOR).setManagingOrganization(managingOrganization.getPartOf())
                    .addSpecialty()
                    .setValueAsEnum(PractitionerSpecialtyEnum.CARDIOLOGIST);
            return practitioner;
        }

        private Organization populateUserOrganization(User ldapUserInfo, String ldapHostUri) {
            Organization organization = new Organization();
            organization.addIdentifier().setSystem(ldapHostUri + "/" + ldapUserInfo.getId().toString()).setValue(ldapUserInfo.getUserName());
            organization.setType(OrganizationTypeEnum.HEALTHCARE_PROVIDER);
            organization.setName(ldapUserInfo.getOrganizationName());
            organization.setActive(true);
            return organization;
        }

    }
}
