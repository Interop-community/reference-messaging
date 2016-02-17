# README #

Welcome to the HSPC Reference Messaging!  The HSPC Reference Messaging server contains a Spring Integration deployment to easily configure enterprise integration patterns.  The Reference Messaging server is optional if 1) the Reference API server is configured to not enable subscription support and 2) the sandbox user management features are not needed. The HSPC Reference Messaging server also provides these specific features:

* Endpoint for registering Subscription FHIR Resources
* Endpoint for submitting FHIR Resources for processing by the subscription engine
* Endpoint for configuring sandbox user information

## How do I get set up? ##

### Preconditions ###
    For secured configuration, the reference-messaging server must register a client with the reference-authorization server.
    From MySQL
    mysql> use oic;
    mysql> source {install path}/reference-messaging/src/main/resources/db/openidconnect/mysql/messaging-client.sql;
    * note this script is included with the complete installation of the reference-impl (optional)

### Build and Run ###
    mvn clean install
    deploy target/hspc-reference-messaging.war to Tomcat

### Verify ###
* http://localhost:8080/hspc-reference-messaging/subscription/health
