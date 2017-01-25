package org.hspconsortium.platform.messaging.service;

import org.hspconsortium.platform.messaging.model.mail.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public interface EmailSenderService {
    String ENCODING = StandardCharsets.UTF_8.name();

    String health(String request);

    Map sendEmail(Message message) throws MessagingException;

    Map sendEmailTest(String body) throws MessagingException;

    @Component
    public static class Impl implements EmailSenderService {

        private static final Logger logger = LoggerFactory.getLogger(EmailSenderService.Impl.class);

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
            Map<String, String> auditMap = new HashMap<>();

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
                try {
                    this.mailSender.send(mimeMessage);
                } catch (Exception e) {
                    logger.error("Error sending email message", e);
                }
                if (emailMessage.isAuditEnabled())
                    auditMap.put(mimeMessage.getRecipients(javax.mail.Message.RecipientType.TO)[0].toString()
                            , mimeMessage.getReplyTo()[0].toString());
                else
                    auditMap.put(mimeMessage.getRecipients(javax.mail.Message.RecipientType.TO)[0].toString()
                            , emailMessage.getSenderEmail());
            }
            return auditMap;
        }

        /*
         * Send HTML mail (simple)
         */
        @Override
        public Map sendEmailTest(String body)
                throws MessagingException {
            Map<String, String> auditMap = new HashMap<>();

            Message.Recipient recipient = new Message.Recipient("Patricia Primary, MD", "travis@isalussolutions.com");

            // Prepare messageHelper using a Spring helper
            final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            final MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, ENCODING);

            messageHelper.setSubject("CarePlan Has Been Updated");
            messageHelper.setFrom("no-reply@hspconsortium.org");
            messageHelper.setTo(recipient.getEmail());
            messageHelper.setReplyTo("no-reply@hspconsortium.org");
            messageHelper.setText("<img src='cid:myLogo' width='325px' height='55px'><br/>HSPConsortium.org Email Notification System<br/><br/><h1>CarePlan has been update.</h1><br/><br/>Click <a href='https://sandbox.hspconsortium.org'>here</a> to access the HSPC Sandbox.", true);
            messageHelper.addInline("myLogo", new ClassPathResource("templates/images/company-logo-main-web-top.png"));

            // Send email
            try {
                this.mailSender.send(mimeMessage);
            } catch (Exception e) {
                logger.error("Error sending email message", e);
            }

            auditMap.put(mimeMessage.getRecipients(javax.mail.Message.RecipientType.TO)[0].toString(), recipient.getEmail());
            return auditMap;
        }
    }
}
