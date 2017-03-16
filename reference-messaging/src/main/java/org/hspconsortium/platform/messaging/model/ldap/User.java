package org.hspconsortium.platform.messaging.model.ldap;

import org.hspconsortium.platform.messaging.model.ldap.annotations.Attribute;

public final class User {
    @Attribute(name = "cn")
    private String cn;

    @Attribute(name = "uid")
    private String userName;

    @Attribute(name = "userPassword")
    private String userPassword;

    @Attribute(name = "employeeNumber")
    private String employeeNumber;

    @Attribute(name = "ou")
    private String organization;

    @Attribute(name = "organizationName")
    private String organizationName;

    @Attribute(name = "givenName")
    private String firstName;

    @Attribute(name = "displayName")
    private String displayName;

    @Attribute(name = "sn")
    private String lastName;

    @Attribute(name = "title")
    private String title;

    @Attribute(name = "mail")
    private String email;

    @Attribute(name = "telephoneNumber")
    private String phone;

    @Attribute(name = "labeledURI")
    private String profileUri;

    @Attribute(name = "givenName")
    private String givenName;

    @Attribute(name = "familyName")
    private String familyName;

    @Attribute(name = "middleName")
    private String middleName;

    @Attribute(name = "profile")
    private String profile;

    @Attribute(name = "website")
    private String website;

    private final String ldapEntityName;

    public User(String ldapEntityName) {
        this.ldapEntityName = ldapEntityName;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getCn() {
        return cn;
    }

    public void setCn(String cn) {
        this.cn = cn;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setProfileUri(String profileUri) {
        this.profileUri = profileUri;
    }

    public String getProfileUri() {
        return profileUri;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getProfile() {
        return profile;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getWebsite() {
        return website;
    }

    public String getLdapEntityName() {
        return ldapEntityName;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getUserPassword() {
        return userPassword;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (ldapEntityName != null ? !ldapEntityName.equals(user.ldapEntityName) : user.ldapEntityName != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return ldapEntityName != null ? ldapEntityName.hashCode() : 0;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getOrganization() {
        return organization;
    }
}