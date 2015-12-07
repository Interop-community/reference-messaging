package org.hspconsortium.platform.messaging.model;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
        "user_id",
        "user_password",
        "email",
        "organization",
        "organization_name",
        "distinct_name",
        "display_name"
})
public class SandboxUserInfo {

    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("user_password")
    private String userPassword;
    @JsonProperty("email")
    private String email;
    @JsonProperty("organization")
    private String organization;
    @JsonProperty("organization_name")
    private String organizationName;
    @JsonProperty("distinct_name")
    private String distinctName;
    @JsonProperty("display_name")
    private String displayName;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * @return The userId
     */
    @JsonProperty("user_id")
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId The user_id
     */
    @JsonProperty("user_id")
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return The userPassword
     */
    @JsonProperty("user_password")
    public String getUserPassword() {
        return userPassword;
    }

    /**
     * @param userPassword The user_password
     */
    @JsonProperty("user_password")
    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    /**
     * @return The email
     */
    @JsonProperty("email")
    public String getEmail() {
        return email;
    }

    /**
     * @param email The email
     */
    @JsonProperty("email")
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return The organization
     */
    @JsonProperty("organization")
    public String getOrganization() {
        return organization;
    }

    /**
     * @param organization The organization
     */
    @JsonProperty("organization")
    public void setOrganization(String organization) {
        this.organization = organization;
    }

    /**
     * @return The organizationName
     */
    @JsonProperty("organization_name")
    public String getOrganizationName() {
        return organizationName;
    }

    /**
     * @param organizationName The organization_name
     */
    @JsonProperty("organization_name")
    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    /**
     * @return The distinctName
     */
    @JsonProperty("distinct_name")
    public String getDistinctName() {
        return distinctName;
    }

    /**
     * @param distinctName The distinct_name
     */
    @JsonProperty("distinct_name")
    public void setDistinctName(String distinctName) {
        this.distinctName = distinctName;
    }

    /**
     * @return The displayName
     */
    @JsonProperty("display_name")
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param displayName The display_name
     */
    @JsonProperty("display_name")
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}