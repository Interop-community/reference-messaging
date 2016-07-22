package org.hspconsortium.platform.messaging.model.mail;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Message {
    public static final String ENCODING = "UTF-8";
    public static final String TEMPLATE_FORMAT_HTML = "HTML";
    public static final String TEMPLATE_FORMAT_TEXT = "TEXT";
    private final boolean multipart;
    private final String encoding;
    private String senderEmail;
    private String senderName;

    private List<Recipient> recipients;
    private byte[] template;
    private List<Resource> resources;
    private List<Resource> attachments;
    private Map<String, Object> variable;
    private String subject;
    private String templateName;
    private boolean acceptHtmlMessage = false;
    private TemplateFormat templateFormat = TemplateFormat.HTML;
    private boolean auditEnabled = false;

    public Message(boolean multipart, String encoding) {
        this.multipart = multipart;
        this.encoding = encoding;
    }

    public Message() {
        multipart = false;
        encoding = ENCODING;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        if (!Recipient.isValid(senderEmail)) {
            throw new RuntimeException(String.format("%s email format not valid.", senderEmail));
        }
        this.senderEmail = senderEmail;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public List<Recipient> getRecipients() {
        return recipients;
    }

    public byte[] getTemplate() {
        return template;
    }

    public void setTemplate(byte[] template) {
        this.template = template;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public List<Resource> getAttachments() {
        return attachments;
    }

    public Map<String, Object> getVariable() {
        return variable;
    }

    public void addVariable(String key, Object value) {
        if (variable == null) {
            variable = new HashMap<>();
        }
        variable.put(key, value);
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public boolean isAcceptHtmlMessage() {
        return acceptHtmlMessage;
    }

    public void setAcceptHtmlMessage(boolean acceptHtmlMessage) {
        this.acceptHtmlMessage = acceptHtmlMessage;
    }

    public void addResource(String contentName, String contentType, byte[] content) {
        if (this.resources == null) {
            this.resources = new ArrayList();
        }
        this.resources.add(new Resource(contentName, contentType, content));
    }

    public void addAttachment(String contentName, String contentType, byte[] content) {
        if (this.attachments == null) {
            this.attachments = new ArrayList();
        }
        this.attachments.add(new Resource(contentName, contentType, content));
    }

    public TemplateFormat getTemplateFormat() {
        return templateFormat;
    }

    public void setTemplateFormat(TemplateFormat templateFormat) {
        this.templateFormat = templateFormat;
    }

    public boolean isMultipart() {
        return multipart;
    }

    public String getEncoding() {
        return encoding;
    }

    public boolean isAuditEnabled() {
        return auditEnabled;
    }

    public void setAuditEnabled(boolean auditEnabled) {
        this.auditEnabled = auditEnabled;
    }

    public void addRecipient(String name, String email) {
        if (this.recipients == null) {
            this.recipients = new ArrayList();
        }

        this.recipients.add(new Recipient(name, email));
    }

    public void addRecipient(String name, String email, String replyTo, Locale locale) {
        if (this.recipients == null) {
            this.recipients = new ArrayList();
        }

        this.recipients.add(new Recipient(name, email, replyTo, locale));
    }

    public void addRecipient(String name, String email, String replyTo) {
        if (this.recipients == null) {
            this.recipients = new ArrayList();
        }

        this.recipients.add(new Recipient(name, email, replyTo));
    }

    public static class Recipient {
        public static final String INVALID_EMAIL_FORMAT_MESSAGE = "'%s' email format not valid.";
        private static final String EMAIL_PATTERN =
                "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        private static Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        private static Matcher matcher;
        private final String email;
        private final String name;
        private String replyTo;
        private Locale locale = Locale.US;


        public Recipient(String name, String email, String replyTo, Locale locale) {
            if (!isValid(email)) {
                throw new RuntimeException(String.format("%s email format not valid.", email));
            }
            this.email = email;
            this.name = name;
            this.replyTo = replyTo;
            this.locale = locale;
        }

        public Recipient(String name, String email, String replyTo) {
            if (!isValid(email)) {
                throw new RuntimeException(String.format("%s email format not valid.", email));
            }
            this.email = email;
            this.name = name;
            this.replyTo = replyTo;
        }

        public Recipient(String name, String email) {
            if (!isValid(email)) {
                throw new RuntimeException(String.format("%s email format not valid.", email));
            }
            this.email = email;
            this.name = name;
        }

        public Recipient(String email, String name, Locale locale) {
            if (!isValid(email)) {
                throw new RuntimeException(String.format(INVALID_EMAIL_FORMAT_MESSAGE, email));
            }
            this.email = email;
            this.name = name;
            this.locale = locale;
        }

        public static boolean isValid(final String hex) {
            matcher = pattern.matcher(hex);
            return matcher.matches();
        }

        public String getReplyTo() {
            return replyTo;
        }

        public void setReplyTo(String replyTo) {
            if (!isValid(replyTo)) {
                throw new RuntimeException(String.format(Recipient.INVALID_EMAIL_FORMAT_MESSAGE, email));
            }
            this.replyTo = replyTo;
        }

        public String getEmail() {
            return email;
        }

        public String getName() {
            return name;
        }

        public Locale getLocale() {
            return locale;
        }

        public void setLocale(Locale locale) {
            this.locale = locale;
        }

    }

    public static class Resource {
        private String contentName;
        private String contentType;
        private byte[] content;

        public Resource(String contentName, String contentType, byte[] content) {
            this.contentName = contentName;
            this.contentType = contentType;
            this.content = content;
        }

        public String getContentName() {
            return contentName;
        }

        public void setContentName(String contentName) {
            this.contentName = contentName;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public byte[] getContent() {
            return content;
        }

        public void setContent(byte[] content) {
            this.content = content;
        }
    }

    public static enum TemplateFormat {
        HTML("HTML"),
        TEXT("TEXT"),
        STRING("STRING");
        private final String templateType;

        TemplateFormat(String templateType) {
            this.templateType = templateType;
        }
    }
}