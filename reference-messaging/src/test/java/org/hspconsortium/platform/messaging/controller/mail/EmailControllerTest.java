package org.hspconsortium.platform.messaging.controller.mail;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.hspconsortium.platform.messaging.AppConfig;
import org.hspconsortium.platform.messaging.Application;
import org.hspconsortium.platform.messaging.model.mail.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;

@ContextConfiguration(locations = {"classpath*:org/hspconsortium/platform/messaging/controller/mail/EmailControllerTest-context.xml"})
// default context name is <ClassName>-context.xml
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class, AppConfig.class})
@WebAppConfiguration
@IntegrationTest({"server.port=8080"})
public class EmailControllerTest {
    public static final String PNG_MIME = "image/png";
    public static final String ATTACHMENT_FILE_FILE = "templates\\images\\background.png";
    public static final String TEMPLATE_FILE = "templates\\email-editable.html";
    public static final String EMAIL_SUBJECT = "This is a test Email Subject";
    private static final String BACKGROUND_IMAGE = "templates\\images\\background.png";
    private static final String LOGO_BACKGROUND_IMAGE = "templates\\images\\background.png";
    private static final String THYMELEAF_BANNER_IMAGE = "templates\\images\\company-logo-main-web-top.png";
    private static final String THYMELEAF_LOGO_IMAGE = "templates\\images\\company-logo.png";


    @Autowired
    QueueChannel testChannel;

    @Autowired
    EmailController gateway;

    private static Logger logger = Logger.getLogger(EmailControllerTest.class);

    private String senderEmail = "sandbox@hspconsortium.org";
    private String senderName = "HSPC Email Sender Name";

    private String recipientEmail = "sandbox@hspconsortium.org";
    private String recipientName = "Email Recipient Name";


    @Test
    public void testHealth() throws Exception {
        String returnValue = gateway.health(":" + System.currentTimeMillis());
        assertNotNull("Expected a message", returnValue);
    }

    @Test
    public void testSendEmailWithAttachment() throws Exception {
        boolean multipart = true;
        boolean messageFormatHtml = false;
        org.hspconsortium.platform.messaging.model.mail.Message message = createMessage(multipart, messageFormatHtml);
        message.setTemplateName("email-text");
        message.setTemplateFormat(Message.TemplateFormat.TEXT);
        message.addAttachment("some_attachment.png", PNG_MIME, getImageFile(ATTACHMENT_FILE_FILE, "png"));
        //uncomment next two lines to send emails.
//        Map auditInformation = gateway.sendEmail(message);
//        assertNotNull("Expected a message", auditInformation);
    }

    @Test
    public void testSendExternalTemplateEmail() throws Exception {
        boolean multipart = true;
        boolean messageFormatHtml = true;
        org.hspconsortium.platform.messaging.model.mail.Message message = createMessage(multipart, messageFormatHtml);
        message.setTemplate(getFile(TEMPLATE_FILE));
        message.setTemplateFormat(Message.TemplateFormat.HTML);

        // Add the inline images, referenced from the HTML code as "cid:image-name"
        message.addResource("background", PNG_MIME, getImageFile(BACKGROUND_IMAGE, "png"));
        message.addResource("logo-background", PNG_MIME, getImageFile(LOGO_BACKGROUND_IMAGE, "png"));
        message.addResource("hspc-banner", PNG_MIME, getImageFile(THYMELEAF_BANNER_IMAGE, "png"));
        message.addResource("hspc-logo", PNG_MIME, getImageFile(THYMELEAF_LOGO_IMAGE, "png"));
        //uncomment next two lines to send emails.
//        Map auditInformation = gateway.sendEmail(message);
//        assertNotNull("Expected a message", auditInformation);
    }

    @Test
    public void testSendSimpleHtmlEmail() throws Exception {
        boolean multipart = true;
        boolean messageFormatHtml = true;
        org.hspconsortium.platform.messaging.model.mail.Message message = createMessage(multipart, messageFormatHtml);
        message.setTemplateName("email-simple");
        message.setTemplateFormat(Message.TemplateFormat.HTML);
        //uncomment next two lines to send emails.
//        Map auditInformation = gateway.sendEmail(message);
//        assertNotNull("Expected a message", auditInformation);
    }

    @Test
    public void testSendHtmlEmailWithInlineImage() throws Exception {
        boolean multipart = true;
        boolean messageFormatHtml = true;
        org.hspconsortium.platform.messaging.model.mail.Message message = createMessage(multipart, messageFormatHtml);
        message.setTemplateName("email-inlineimage");
        message.setTemplateFormat(Message.TemplateFormat.HTML);

        message.addVariable("imageResourceName", "image_of_a_background");
        message.addResource("image_of_a_background", PNG_MIME, getImageFile(BACKGROUND_IMAGE, "png"));

        //uncomment next two lines to send emails.
//        Map auditInformation = gateway.sendEmail(message);
//       assertNotNull("Expected a message", auditInformation);
    }


    @Test
    public void testSendTextEmail() throws Exception {
        boolean multipart = false;
        boolean messageFormatHtml = false;
        org.hspconsortium.platform.messaging.model.mail.Message message = createMessage(multipart, messageFormatHtml);
        message.setTemplateName("email-text");
        message.setTemplateFormat(org.hspconsortium.platform.messaging.model.mail.Message.TemplateFormat.TEXT);
        //uncomment next two lines to send emails.
//        Map auditInformation = gateway.sendEmail(message);
//        assertNotNull("Expected a message", auditInformation);
    }

    private org.hspconsortium.platform.messaging.model.mail.Message createMessage(boolean multipart, boolean messageFormatHtml) {
        org.hspconsortium.platform.messaging.model.mail.Message message
                = new org.hspconsortium.platform.messaging.model.mail.Message(multipart, org.hspconsortium.platform.messaging.model.mail.Message.ENCODING);

        message.setSubject(EMAIL_SUBJECT);
        message.setAcceptHtmlMessage(messageFormatHtml);

        message.setSenderName(this.senderName);
        message.setSenderEmail(senderEmail);
        message.addRecipient(recipientName, recipientEmail);

        message.addVariable("name", message.getSenderName());
        message.addVariable("subscriptionDate", new Date());
        message.addVariable("hobbies", Arrays.asList("Cinema", "Sports", "Music"));
        return message;
    }

    private byte[] getImageFile(String pathName, String imageType) {
        BufferedImage img;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            ClassPathResource cpr = new ClassPathResource(pathName);
            final File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".tmp");
            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                IOUtils.copy(cpr.getInputStream(), out);
            }
            img = ImageIO.read(tempFile);
            ImageIO.write(img, imageType, baos);
            baos.flush();
            byte[] imageInByte = baos.toByteArray();
            baos.close();
            return imageInByte;
        } catch (IOException e) {
        }
        return null;
    }

    private byte[] getFile(String pathName) {
        FileInputStream fileInputStream = null;
        byte[] bFile = new byte[0];
        try {
            ClassPathResource cpr = new ClassPathResource(pathName);
            final File file = File.createTempFile(UUID.randomUUID().toString(), ".tmp");
            try (FileOutputStream out = new FileOutputStream(file)) {
                IOUtils.copy(cpr.getInputStream(), out);
            }
            bFile = new byte[(int) file.length()];
            //convert file into array of bytes
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bFile);
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bFile;
    }

    private HttpHeaders getHttpHeadersWithUserCredentials(HttpHeaders headers) {
        String username = "user";
        String password = "password";
        String combinedUsernamePassword = username + ":" + password;
        byte[] base64Token = Base64.encode(combinedUsernamePassword.getBytes());
        String base64EncodedToken = new String(base64Token);
        //adding Authorization header for HTTP Basic authentication
        headers.add("Authorization", "Basic  " + base64EncodedToken);
        return headers;
    }

    private String toJson(Message message) {
        Gson gson = new Gson();
        Type type = new TypeToken<Message>() {}.getType();
        String json = gson.toJson(message, type);
        System.out.println(json);
        Message fromJson = gson.fromJson(json, type);
        return json;
    }
}