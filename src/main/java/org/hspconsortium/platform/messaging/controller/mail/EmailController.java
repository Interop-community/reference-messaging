package org.hspconsortium.platform.messaging.controller.mail;

import org.hspconsortium.platform.messaging.model.mail.Message;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Map;

public interface EmailController {

    String health(String request);

    Map<String, String> sendEmail(Message message);

    Map<String, String> sendEmail(String senderName, String senderEmail
            , String recipientName, String recipientEmail
            , String subject
            , String templateName, String templateFormat
            , Map templateVariables
            , final MultipartFile[] attachment
            , final MultipartFile[] resources
            , final Locale locale);
}
