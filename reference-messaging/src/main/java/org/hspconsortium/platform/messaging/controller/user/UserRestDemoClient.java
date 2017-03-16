package org.hspconsortium.platform.messaging.controller.user;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.hspconsortium.platform.messaging.model.user.SandboxUserInfo;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.UUID;

public class UserRestDemoClient {
    private static Logger logger = Logger.getLogger(UserRestDemoClient.class);
    public static void main(String[] args) {
        try {
            createOrUpdateUser(createUser());
        } catch (IOException | AuthenticationException e) {
            e.printStackTrace();
        }
    }

    private static void createOrUpdateUser(SandboxUserInfo userInfo) throws IOException, AuthenticationException {
        String url = "http://lpv-hdsvnev02.co.ihc.com:8080/sandboxuser";
//        String url = "http://localhost:8080/hspc-reference-messaging/sandboxuser";

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPut http = new HttpPut(url);
        http.setEntity(new StringEntity(toJson(userInfo)));
        http.setHeader("Accept", "application/json");
        http.setHeader("Content-type", "application/json");

        CloseableHttpResponse response = client.execute(http);
        logger.info(response.getStatusLine());
        logger.info(response.getEntity());
        client.close();
    }

    private static SandboxUserInfo createUser() {
        SandboxUserInfo userInfo = new SandboxUserInfo();
        userInfo.setCn(UUID.randomUUID().toString());
        userInfo.setLastName(UUID.randomUUID().toString());
        userInfo.setFirstName(UUID.randomUUID().toString());
        userInfo.setOrganization("Intermountain Healthcare");
        userInfo.setOrganizationName("Intermountain Healthcare Name");
        userInfo.setDisplayName("Tintin");
        userInfo.setEmployeeNumber("896512");
        userInfo.setEmail(UUID.randomUUID().toString() + "@imail.org");
        userInfo.setLdapHost("ldap://lpv-hdsvnev02.co.ihc.com:10389");
        userInfo.setProfileUrl("https://sandbox.hspconsortium.org/dstu2/hspc-reference-api/data/Practitioner/16824");
        userInfo.setUserId(userInfo.getEmail());
        userInfo.setUserPassword("modification");
        userInfo.setDistinctName("cn=" + userInfo.getCn());
        return userInfo;
    }

    private static String toJson(SandboxUserInfo userInfo) {
        Gson gson = new Gson();
        Type type = new TypeToken<SandboxUserInfo>() {
        }.getType();
        return gson.toJson(userInfo, type);
    }
}
