package org.hspconsortium.platform.messaging;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.IRestfulClientFactory;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.apache.commons.io.IOUtils;
import org.hspconsortium.client.auth.Scopes;
import org.hspconsortium.client.auth.SimpleScope;
import org.hspconsortium.client.auth.access.AccessTokenProvider;
import org.hspconsortium.client.auth.access.JsonAccessTokenProvider;
import org.hspconsortium.client.auth.credentials.ClientSecretCredentials;
import org.hspconsortium.client.auth.credentials.Credentials;
import org.hspconsortium.client.auth.credentials.JWTCredentials;
import org.hspconsortium.client.controller.FhirEndpointsProvider;
import org.hspconsortium.client.session.clientcredentials.ClientCredentialsSessionFactory;
import org.hspconsortium.platform.messaging.drools.service.DroolsSubscriptionManagerService;
import org.hspconsortium.platform.messaging.service.EmailSenderService;
import org.hspconsortium.platform.messaging.service.SandboxUserRegistrationService;
import org.hspconsortium.platform.messaging.service.SubscriptionManagerService;
import org.hspconsortium.platform.messaging.service.ldap.UserService;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.NamingException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.UUID;

@Configuration
@PropertySource("classpath:application.properties")
@ImportResource("classpath*:/META-INF/spring/spring-integration-config.xml")
public class AppConfig {
    public static final String ENCODING = "UTF-8";

    @Autowired
    Environment env;

    @Bean
    public SubscriptionManagerService subscriptionManagerService() {
        return new DroolsSubscriptionManagerService();
    }

    @Bean
    public SandboxUserRegistrationService sandboxUserRegistrationService() {
        return new SandboxUserRegistrationService.Impl();
    }

    @Bean
    public KnowledgeBase knowledgeBase() {
        return KnowledgeBaseFactory.newKnowledgeBase();
    }

    @Bean
    public String fhirServicesUrl() {
        return env.getProperty("hspc.reference.api.url");
    }

    @Bean
    public String clientId() {
        return env.getProperty("hspc.reference.messaging.app.clientId");
    }

    @Bean
    public String scope() {
        return env.getProperty("hspc.reference.messaging.app.scopes");
    }

    @Bean
    public String clientSecret() {
        return env.getProperty("sandbox.messaging.clientSecret");
    }

    @Bean
    public String jsonWebKeySetLocation() {
        return env.getProperty("hspc.reference.messaging.app.jsonWebKeySetLocation");
    }

    @Bean
    public Integer httpConnectionTimeOut() {
        return Integer.parseInt(env.getProperty("sandbox.messaging.httpConnectionTimeoutMilliSeconds"
                , IRestfulClientFactory.DEFAULT_CONNECT_TIMEOUT + ""));
    }

    @Bean
    public Integer httpReadTimeOut() {
        return Integer.parseInt(env.getProperty("sandbox.messaging.httpReadTimeoutMilliSeconds"
                , IRestfulClientFactory.DEFAULT_CONNECTION_REQUEST_TIMEOUT + ""));
    }

    @Bean
    public String proxyPassword() {
        return System.getProperty("http.proxyPassword", System.getProperty("https.proxyPassword"));
    }

    @Bean
    public String proxyUser() {
        return System.getProperty("http.proxyUser", System.getProperty("https.proxyUser"));
    }

    @Bean
    public Integer proxyPort() {
        return Integer.parseInt(System.getProperty("http.proxyPort", System.getProperty("https.proxyPort", "8080")));
    }

    @Bean
    public String proxyHost() {
        //-Dhttp.proxyHost=proxy.host.com -Dhttp.proxyPort=8080  -Dhttp.proxyUser=username -Dhttp.proxyPassword=password -Dhttp.nonProxyHosts=*.nonproxyrepos.com|localhost
        return System.getProperty("http.proxyHost", System.getProperty("https.proxyHost"));
    }

    @Bean
    public Integer jsonWebKeySetSizeLimitBytes() {
        return Integer.parseInt(env.getProperty("sandbox.messaging.jsonWebKeySetSizeLimitBytes", "10000"));
    }

    @Bean
    public Long jsonTokenDuration() {
        return Long.parseLong(env.getProperty("sandbox.messaging.tokenDuration", "900"));
    }

    @Bean
    public String mailServerUserName() {
        return env.getProperty("mail.server.username");
    }

    @Bean
    public String mailServerPassword() {
        return env.getProperty("mail.server.password");
    }

    /**
     * If set to false, the QUIT command is sent and the connection is immediately closed. If set to true (the default),
     * causes the transport to wait for the response to the QUIT command.
     *
     * @return quie wait
     */
    @Bean
    public boolean quitWait() {
        String quitWait = env.getProperty("mail.smtp.quitwait");
        return quitWait != null && Boolean.parseBoolean(quitWait);
    }

    /**
     * TLS refers to extensions in plain text communication protocols, which offer a way to upgrade a plain text
     * connection to an encrypted (TLS or SSL) connection instead of using a separate port for encrypted communication
     *
     * @return startTls
     */
    @Bean
    public boolean startTls() {
        String starttls = env.getProperty("mail.smtp.starttls.enable");
        return starttls != null && Boolean.parseBoolean(starttls);
    }

    @Bean
    public String mailServerProtocol() {
        return env.getProperty("mail.server.protocol");
    }

    @Bean
    public String mailServerHost() {
        return env.getProperty("mail.server.host");
    }

    @Bean
    public Integer mailServerPort() {
        String port = env.getProperty("mail.server.port");
        if (port != null) {
            return Integer.parseInt(port);
        }
        return null;
    }

    /**
     * If true, attempt to authenticate the user using the AUTH command. Defaults to false.
     *
     * @return if email server needs authentication
     */
    @Bean
    public boolean mailServerAuthentication() {
        String authentication = env.getProperty("mail.smtp.auth");
        return authentication != null && Boolean.parseBoolean(authentication);
    }

    @Bean
    @Inject
    public ClientSecretCredentials clientSecretCredentials(String clientSecret) {
        return new ClientSecretCredentials(clientSecret);
    }

    @Bean
    public AccessTokenProvider tokenProvider() {
        return new JsonAccessTokenProvider();
    }

    @Bean
    public FhirEndpointsProvider fhirEndpointsProvider(FhirContext fhirContext) {
        return new FhirEndpointsProvider.Impl(fhirContext);
    }

    @Bean
    @Inject
    public Credentials credentials(String clientSecret, String jsonWebKeySetLocation) {
        if (clientSecret != null) {
            return clientSecretCredentials(clientSecret);
        } else if (jsonWebKeySetLocation != null) {
            return jwtCredentials(
                    jwkSet(jsonWebKeySetLocation(), httpConnectionTimeOut(), httpReadTimeOut(), jsonWebKeySetSizeLimitBytes()),
                    clientId(),
                    null,
                    jsonTokenDuration());
        } else {
            throw new RuntimeException("Credentials not specified");
        }
    }

    @Bean
    public FhirContext fhirContext(Integer httpConnectionTimeOut, Integer httpReadTimeOut
            , String proxyHost, Integer proxyPort
            , String proxyUser, String proxyPassword) {
        FhirContext hapiFhirContext = FhirContext.forDstu3();
        // Set how long to try and establish the initial TCP connection (in ms)
        hapiFhirContext.getRestfulClientFactory().setConnectTimeout(httpConnectionTimeOut);

        // Set how long to block for individual read/write operations (in ms)
        hapiFhirContext.getRestfulClientFactory().setSocketTimeout(httpReadTimeOut);

        if (proxyHost != null) {
            hapiFhirContext.getRestfulClientFactory().setProxy(proxyHost, proxyPort);

            hapiFhirContext.getRestfulClientFactory().setProxyCredentials(proxyUser
                    , proxyPassword);
        }
        return hapiFhirContext;
    }

    @Bean
    @Inject
    // simulate two EHR by having two instances of session factory
    public ClientCredentialsSessionFactory<? extends Credentials> ehrSessionFactory(
            FhirContext fhirContext, AccessTokenProvider tokenProvider, FhirEndpointsProvider fhirEndpointsProvider, String fhirServicesUrl,
            String clientId, Credentials credentials, String scope) {
        Scopes scopes = new Scopes();
        scopes.add(new SimpleScope(scope));
        return new ClientCredentialsSessionFactory<>(fhirContext, tokenProvider, fhirEndpointsProvider, fhirServicesUrl, clientId,
                credentials, scopes);
    }

    private JWKSet jwkSet(String jsonWebKeySetLocation, Integer httpConnectionTimeOut
            , Integer httpReadTimeOut
            , Integer jsonWebKeySetSizeLimitBytes) {
        JWKSet jwks;
        try {
            if (isUrl(jsonWebKeySetLocation)) {
                URL url = new URL(jsonWebKeySetLocation);
                jwks = JWKSet.load(url, httpConnectionTimeOut, httpReadTimeOut, jsonWebKeySetSizeLimitBytes);
            } else {
//                Class currentClass = new Object() {
//                }.getClass().getEnclosingClass();
//                String fileName = currentClass.getClassLoader().getResource(jsonWebKeySetLocation).getFile();
//                jwks = JWKSet.load(new File(fileName));
                ClassPathResource cpr = new ClassPathResource(jsonWebKeySetLocation);
                final File tempFile = File.createTempFile("jwkSet", ".tmp");
                tempFile.deleteOnExit();
                try (FileOutputStream out = new FileOutputStream(tempFile)) {
                    IOUtils.copy(cpr.getInputStream(), out);
                }
                jwks = JWKSet.load(tempFile);
            }
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
        return jwks;
    }

    private JWTCredentials jwtCredentials(JWKSet jwkSet, String clientId, String audience, Long jsonTokenDuration) {
        try {
            RSAKey rsaKey = (RSAKey) jwkSet.getKeys().get(0);
            JWTCredentials credentials = new JWTCredentials(rsaKey.toRSAPrivateKey());
            credentials.setIssuer(clientId);
            credentials.setSubject(clientId);
            credentials.setAudience(audience);
            credentials.setTokenReference(UUID.randomUUID().toString());
            credentials.setDuration(jsonTokenDuration);
            return credentials;
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }

    @Bean
    public UserService ldapUserService() throws NamingException {
        // Set up the environment for creating the initial context
        Hashtable<String, Object> contextEnv = new Hashtable<>(5);
        contextEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        contextEnv.put(Context.PROVIDER_URL, env.getProperty("ldap.server"));
        contextEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
        contextEnv.put(Context.SECURITY_PRINCIPAL, env.getProperty("ldap.userDn"));
        contextEnv.put(Context.SECURITY_CREDENTIALS, env.getProperty("ldap.password"));

        return new UserService(contextEnv);
    }

    /**
     * THYMELEAF: Template Engine (Spring4-specific version) for HTML email templates.
     */
    @Bean
    public TemplateEngine htmlTemplateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(htmlTemplateResolver());
        return templateEngine;
    }

    /**
     * THYMELEAF: Template Engine (Spring4-specific version) for TEXT email templates.
     */
    @Bean
    public TemplateEngine textTemplateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(textTemplateResolver());
        return templateEngine;
    }

    /**
     * THYMELEAF: Template Engine (Spring4-specific version) for in-memory HTML email templates.
     */
    @Bean
    public TemplateEngine stringTemplateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.addTemplateResolver(stringTemplateResolver());
        return templateEngine;
    }

    /**
     * THYMELEAF: Template Resolver for HTML email templates.
     */
    private ITemplateResolver textTemplateResolver() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/templates/");
        templateResolver.setSuffix(".txt");
        templateResolver.setTemplateMode(TemplateMode.TEXT);
        templateResolver.setCharacterEncoding(ENCODING);
        templateResolver.setCacheable(false);
        return templateResolver;
    }

    /**
     * THYMELEAF: Template Resolver for HTML email templates.
     */
    private ITemplateResolver htmlTemplateResolver() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding(ENCODING);
        templateResolver.setCacheable(false);
        return templateResolver;
    }

    /**
     * THYMELEAF: Template Resolver for String templates. (template will be a passed String)
     */
    private ITemplateResolver stringTemplateResolver() {
        StringTemplateResolver templateResolver = new StringTemplateResolver();
        templateResolver.setTemplateMode("HTML5");
        templateResolver.setCacheable(false);
        return templateResolver;
    }

    @Bean
    public EmailSenderService mailerService() {
        return new EmailSenderService.Impl();
    }

    @Bean
    public JavaMailSender mailSender() throws IOException {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailServerHost());
        if (mailServerPort() != null)
            mailSender.setPort(mailServerPort());
        mailSender.setProtocol(mailServerProtocol());

        if (mailServerAuthentication()) {
            mailSender.setUsername(mailServerUserName());
            mailSender.setPassword(mailServerPassword());
        }

        Properties p = new Properties();
        p.put("mail.smtp.auth", mailServerAuthentication());
        p.put("mail.smtp.starttls.enable", startTls());
        p.put("mail.smtp.quitwait", quitWait());

        mailSender.setJavaMailProperties(p);
        return mailSender;
    }

    private boolean isUrl(String location) {
        String[] schemes = {"http", "https"};
        org.apache.commons.validator.UrlValidator urlValidator = new org.apache.commons.validator.UrlValidator(schemes);
        return urlValidator.isValid(location);
    }
}
