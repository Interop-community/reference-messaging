package org.hspconsortium.platform.messaging.model.user;

import java.util.HashMap;
import java.util.Map;

public class SandboxUserInfo {
    private String userId;
    private String userPassword;
    private String email;
    private String organization;
    private String organizationName;
    private String distinctName;
    private String displayName;
    private String ldapHost;
    private String profileUrl;
    private String cn;
    private String sn;
    private String employeeNumber;

    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private String firstName;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getDistinctName() {
        return distinctName;
    }

    public void setDistinctName(String distinctName) {
        this.distinctName = distinctName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getLdapHost() {
        return ldapHost;
    }

    public void setLdapHost(String ldapHost) {
        this.ldapHost = ldapHost;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public String getCn() {
        return cn;
    }

    public void setCn(String cn) {
        this.cn = cn;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.sn;
    }

    public void setLastName(String lastName) {
        this.sn = lastName;
    }
}