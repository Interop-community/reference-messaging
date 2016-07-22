package org.hspconsortium.platform.messaging.service;

import org.hspconsortium.platform.messaging.model.mail.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public interface EmailSenderService {
    public static final String ENCODING = "UTF-8";

    public String health(String request);

    public Map sendEmail(Message message) throws MessagingException;


    @Component
    public static class Impl implements EmailSenderService {

        @Autowired
        private JavaMailSender mailSender;

        @Autowired
        private TemplateEngine htmlTemplateEngine;

        @Autowired
        private TemplateEngine textTemplateEngine;

        @Autowired
        private TemplateEngine stringTemplateEngine;

        @Override
        public String health(String request) {
            return String.format("http_servlet_response from %s with %s @ %s.", this.getClass().getName(), HttpServletResponse.SC_OK, request);
        }

        /*
         * Send HTML mail (simple)
         */
        @Override
        public Map sendEmail(Message emailMessage)
                throws MessagingException {
            Map auditMap = new HashMap();

            for (Message.Recipient recipient : emailMessage.getRecipients()) {
                final Context ctx = new Context(recipient.getLocale());
                ctx.setVariables(emailMessage.getVariable());

                // Prepare messageHelper using a Spring helper
                final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
                final MimeMessageHelper messageHelper
                        = new MimeMessageHelper(mimeMessage, emailMessage.isMultipart(), emailMessage.getEncoding());

                messageHelper.setSubject(emailMessage.getSubject());
                messageHelper.setFrom(emailMessage.getSenderEmail());
                messageHelper.setTo(recipient.getEmail());
                if (emailMessage.isAuditEnabled() && (recipient.getReplyTo() == null)) {
                    messageHelper.setReplyTo(UUID.randomUUID().toString().toLowerCase() + "@" + emailMessage.getSenderEmail().split("@")[1]);
                } else if (recipient.getReplyTo() != null) {
                    messageHelper.setReplyTo(recipient.getReplyTo());
                }

                if (emailMessage.getTemplate() != null) {
                    try {
                        final String output = this.stringTemplateEngine.process(new String(emailMessage.getTemplate()), ctx);
                        messageHelper.setText(output, emailMessage.isAcceptHtmlMessage());
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                } else if ((emailMessage.getTemplateName() != null) && (emailMessage.getTemplate() == null)) {
                    if (Message.TemplateFormat.TEXT.equals(emailMessage.getTemplateFormat())) {
                        final String output = this.textTemplateEngine.process(emailMessage.getTemplateName(), ctx);
                        messageHelper.setText(output, emailMessage.isAcceptHtmlMessage());
                    } else {
                        final String output = this.htmlTemplateEngine.process(emailMessage.getTemplateName(), ctx);
                        messageHelper.setText(output, emailMessage.isAcceptHtmlMessage());
                    }
                }

                if (emailMessage.getAttachments() != null) {
                    for (Message.Resource attachment : emailMessage.getAttachments()) {
                        // Add the attachment
                        final InputStreamSource attachmentSource = new ByteArrayResource(attachment.getContent());
                        messageHelper.addAttachment(
                                attachment.getContentName(), attachmentSource, attachment.getContentType());
                    }
                }

                if (emailMessage.getResources() != null) {
                    for (final Message.Resource resource : emailMessage.getResources()) {
                        final InputStreamSource imageSource = new ByteArrayResource(resource.getContent());
                        messageHelper.addInline(resource.getContentName(), imageSource, resource.getContentType());
                    }
                }
                // Send email
                this.mailSender.send(mimeMessage);
                if (emailMessage.isAuditEnabled())
                    auditMap.put(mimeMessage.getRecipients(javax.mail.Message.RecipientType.TO)[0].toString()
                            , mimeMessage.getReplyTo()[0].toString());
                else
                    auditMap.put(mimeMessage.getRecipients(javax.mail.Message.RecipientType.TO)[0].toString()
                            , emailMessage.getSenderEmail());
            }
            return auditMap;
        }
    }
}
