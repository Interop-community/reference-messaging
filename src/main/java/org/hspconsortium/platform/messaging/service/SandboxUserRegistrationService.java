package org.hspconsortium.platform.messaging.service;

import ca.uhn.fhir.model.dstu2.resource.Organization;
import ca.uhn.fhir.model.dstu2.valueset.OrganizationTypeEnum;
import ca.uhn.fhir.rest.api.MethodOutcome;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hspconsortium.client.auth.credentials.Credentials;
import org.hspconsortium.client.session.clientcredentials.ClientCredentialsSessionFactory;
import org.hspconsortium.platform.messaging.model.SandboxUserInfo;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

public interface SandboxUserRegistrationService {
    String health();

    void registerSandboxUserOrganization(byte[] userInfoRequest);

    @Component
    class Impl implements SandboxUserRegistrationService {
        @Inject
        ClientCredentialsSessionFactory<? extends Credentials> ehrSessionFactory;

        @Override
        public String health() {
            return "http_servlet_response:" + HttpServletResponse.SC_OK;
        }

        @Override
        public void registerSandboxUserOrganization(byte[] userInfoRequest) {
            ObjectMapper mapper = new ObjectMapper();
            final String s = new String(userInfoRequest);

            try {
                SandboxUserInfo sandboxUserInfo = mapper.readValue(s, SandboxUserInfo.class);
                System.out.println("received : " + s);

                Organization organization = populateUserOrganization(sandboxUserInfo);
                MethodOutcome savedResourceMethodOutcome = ehrSessionFactory
                        .createSession().create().resource(organization).execute();
                System.out.println("newly created resource id: " + savedResourceMethodOutcome.getId());

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private Organization populateUserOrganization(SandboxUserInfo sandboxUserInfo) {
            Organization organization = new Organization();
            organization.addIdentifier().setSystem(sandboxUserInfo.getDistinctName()).setValue(sandboxUserInfo.getOrganization());
            organization.setType(OrganizationTypeEnum.HEALTHCARE_PROVIDER);
            organization.setName(sandboxUserInfo.getOrganizationName());
            organization.setActive(true);
            return organization;
        }

    }
}
