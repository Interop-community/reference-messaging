package org.hspconsortium.platform.messaging.controller.mail;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.hspconsortium.platform.messaging.model.mail.Message;
import org.springframework.core.io.ClassPathResource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

public class EmailRestDemoClient {
    private static Logger logger = Logger.getLogger(EmailRestDemoClient.class);
    public static final String PNG_MIME = "image/png";
    public static final String ATTACHMENT_FILE_FILE = "templates\\images\\background.png";
    public static final String TEMPLATE_FILE = "templates\\email-editable.html";
    public static final String EMAIL_SUBJECT = "This is a test Email Subject";
    private static final String BACKGROUND_IMAGE = "templates\\images\\background.png";
    private static final String LOGO_BACKGROUND_IMAGE = "templates\\images\\background.png";
    private static final String THYMELEAF_BANNER_IMAGE = "templates\\images\\company-logo-main-web-top.png";
    private static final String THYMELEAF_LOGO_IMAGE = "templates\\images\\company-logo.png";

    public static void main(String[] args) {
        try {
            sendMessage(createMessage());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
    }

    private static void sendMessage(Message message) throws IOException, AuthenticationException {
        String url = "http://lpv-hdsvnev02.co.ihc.com:8080/mailsender";
//        String url = "http://localhost:8091/mailsender";



        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new StringEntity(toJson(message)));
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");

        CloseableHttpResponse response = client.execute(httpPost);
        logger.info(response.getStatusLine());
        logger.info(response.getEntity());
        client.close();
    }

    private static org.hspconsortium.platform.messaging.model.mail.Message createMessage() {
        org.hspconsortium.platform.messaging.model.mail.Message message
                = new org.hspconsortium.platform.messaging.model.mail.Message(true, org.hspconsortium.platform.messaging.model.mail.Message.ENCODING);

        message.setSubject("Subject of email...");

        message.setTemplateFormat(Message.TemplateFormat.HTML);
        message.setAcceptHtmlMessage(true);

        message.setSenderName("Noman Rahman");
        message.setSenderEmail("sandbox@hspconsortium.org");
        message.addRecipient("Noman Rahman", "sandbox@hspconsortium.org");

        message.addVariable("name", message.getSenderName());
        message.addVariable("subscriptionDate", new Date());
        message.addVariable("hobbies", Arrays.asList("Cinema", "Sports", "Music"));

        message.setTemplate(getFile(TEMPLATE_FILE));
        message.setTemplateFormat(Message.TemplateFormat.HTML);

        // Add the inline images, referenced from the HTML code as "cid:image-name"
        message.addResource("background", PNG_MIME, getImageFile(BACKGROUND_IMAGE, "png"));
        message.addResource("logo-background", PNG_MIME, getImageFile(LOGO_BACKGROUND_IMAGE, "png"));
        message.addResource("hspc-banner", PNG_MIME, getImageFile(THYMELEAF_BANNER_IMAGE, "png"));
        message.addResource("hspc-logo", PNG_MIME, getImageFile(THYMELEAF_LOGO_IMAGE, "png"));
        return message;
    }

    private static String toJson(Message message) {
        Gson gson = new Gson();
        Type type = new TypeToken<Message>() {
        }.getType();
        String json = gson.toJson(message, type);
        return json;
    }

    private static byte[] getImageFile(String pathName, String imageType) {
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

    private static byte[] getFile(String pathName) {
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
}
