/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.email;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.tools.FreemarkerTools;
import com.foilen.smalltools.tools.ResourceTools;
import com.foilen.smalltools.tools.StreamsTools;

/**
 * Use this builder to create an email with most of the features: list of TO, CC, BCC, attachments, inline attachments, etc. The text or html body can come from a String, a resource, a file or a
 * Freemarker template.
 *
 * <pre>
 * Usage:
 *
 * EmailBuilder emailBuilder = new EmailBuilder();
 * emailBuilder.setFrom(emailFrom);
 * emailBuilder.addTo(to);
 * emailBuilder.setSubject("Welcome");
 * emailBuilder.setBodyHtmlFromFreemarker("/emails/intro.ftl", model);
 * emailBuilder.addInlineAttachmentFromResource(&quot;logo&quot;, &quot;/emails/logo.png&quot;);
 * emailService.sendEmail(emailBuilder);
 * </pre>
 *
 * <pre>
 * Dependencies:
 * implementation 'org.springframework:spring-context-support:5.3.23'
 * implementation 'org.freemarker:freemarker:2.3.31' (optional)
 * </pre>
 */
public class EmailBuilder {

    private String from;
    private List<String> tos = new ArrayList<String>();
    private List<String> ccs = new ArrayList<String>();
    private List<String> bccs = new ArrayList<String>();
    private String subject;

    private boolean isHtml;
    private String body;

    private List<EmailAttachment> inlineAttachments = new ArrayList<EmailAttachment>();
    private List<EmailAttachment> attachments = new ArrayList<EmailAttachment>();

    /**
     * Include an attachment.
     *
     * @param attachmentFilename
     *            the filename in the email
     * @param fileName
     *            the path of the file
     * @return this
     */
    public EmailBuilder addAttachmentFromFile(String attachmentFilename, String fileName) {
        attachments.add(new EmailAttachment(attachmentFilename, new FileSystemResource(fileName)));
        return this;
    }

    /**
     * Include an attachment.
     *
     * @param attachmentFilename
     *            the filename in the email
     * @param resource
     *            the path of the resource
     * @return this
     */
    public EmailBuilder addAttachmentFromResource(String attachmentFilename, String resource) {
        attachments.add(new EmailAttachment(attachmentFilename, new ClassPathResource(resource)));
        return this;
    }

    /**
     * Include an attachment. Warning, since JavaMail needs a replayable resource, the content of the input stream is first loaded into memory.
     *
     * @param attachmentFilename
     *            the filename in the email
     * @param inputStream
     *            the input stream
     * @return this
     */
    public EmailBuilder addAttachmentFromStream(String attachmentFilename, InputStream inputStream) {
        attachments.add(new EmailAttachment(attachmentFilename, new ByteArrayResource(StreamsTools.consumeAsBytes(inputStream))));
        return this;
    }

    /**
     * Add an invisible recipient.
     *
     * @param bcc
     *            the recipient
     * @return this
     */
    public EmailBuilder addBcc(String bcc) {
        bccs.add(bcc);
        return this;
    }

    /**
     * Add a recipient in the copy section.
     *
     * @param cc
     *            the recipient
     * @return this
     */
    public EmailBuilder addCc(String cc) {
        ccs.add(cc);
        return this;
    }

    /**
     * Include an inline attachment. Used with images.
     *
     * @param contentId
     *            the content id to use. This is the "Content-ID" header in the body part. Can be used in HTML source with src="cid:theId"
     * @param fileName
     *            the path of the file
     * @return this
     */
    public EmailBuilder addInlineAttachmentFromFile(String contentId, String fileName) {
        inlineAttachments.add(new EmailAttachment(contentId, new FileSystemResource(fileName)));
        return this;
    }

    /**
     * Include an inline attachment. Used with images.
     *
     * @param contentId
     *            the content id to use. This is the "Content-ID" header in the body part. Can be used in HTML source with src="cid:theId"
     * @param resource
     *            the path of the resource
     * @return this
     */
    public EmailBuilder addInlineAttachmentFromResource(String contentId, String resource) {
        inlineAttachments.add(new EmailAttachment(contentId, new ClassPathResource(resource)));
        return this;
    }

    /**
     * Include an inline attachment. Used with images. Warning: the content of the input stream is first loaded into memory since JavaMail needs to be able to replay it.
     *
     * @param contentId
     *            the content id to use. This is the "Content-ID" header in the body part. Can be used in HTML source with src="cid:theId"
     * @param inputStream
     *            the input stream
     * @return this
     */
    public EmailBuilder addInlineAttachmentFromStream(String contentId, InputStream inputStream) {
        inlineAttachments.add(new EmailAttachment(contentId, new ByteArrayResource(StreamsTools.consumeAsBytes(inputStream))));
        return this;
    }

    /**
     * Add a recipient in the TO section.
     *
     * @param to
     *            the recipient
     * @return this
     */
    public EmailBuilder addTo(String to) {
        tos.add(to);
        return this;
    }

    /**
     * Get the list of attachments.
     *
     * @return the attachments
     */
    public List<EmailAttachment> getAttachments() {
        return attachments;
    }

    /**
     * Get the list of invisible recipients.
     *
     * @return the recipients
     */
    public List<String> getBccs() {
        return bccs;
    }

    /**
     * Get the body of the email.
     *
     * @return the body
     */
    public String getBody() {
        return body;
    }

    /**
     * Get the list of copied recipients.
     *
     * @return the recipients
     */
    public List<String> getCcs() {
        return ccs;
    }

    /**
     * Get the email of the sender.
     *
     * @return the sender
     */
    public String getFrom() {
        return from;
    }

    /**
     * Get the list of inline attachments.
     *
     * @return the attachments
     */
    public List<EmailAttachment> getInlineAttachments() {
        return inlineAttachments;
    }

    /**
     * Get the subject of the email.
     *
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Get the list of TO recipients.
     *
     * @return the recipients
     */
    public List<String> getTos() {
        return tos;
    }

    /**
     * Tells if the email is an html one.
     *
     * @return true if html ; false if text
     */
    public boolean isHtml() {
        return isHtml;
    }

    /**
     * Take an html file and use it as the body of the email.
     *
     * @param filename
     *            the full path of the file
     * @return this
     */
    public EmailBuilder setBodyHtmlFromFile(String filename) {
        try {
            return setBodyHtmlFromString(StreamsTools.consumeAsString(new FileInputStream(filename)));
        } catch (FileNotFoundException e) {
            throw new SmallToolsException(e);
        }
    }

    /**
     * Take an html Freemarker template and use it as the body of the email.
     *
     * @param resource
     *            the absolute resource to open
     * @param model
     *            the variables available in the templates
     * @return this
     */
    public EmailBuilder setBodyHtmlFromFreemarker(String resource, Map<String, ?> model) {
        return setBodyHtmlFromString(FreemarkerTools.processTemplate(resource, model));
    }

    /**
     * Take an html resource file and use it as the body of the email.
     *
     * @param resource
     *            the absolute resource to open
     * @return this
     */
    public EmailBuilder setBodyHtmlFromResource(String resource) {
        return setBodyHtmlFromString(ResourceTools.getResourceAsString(resource, getClass()));
    }

    /**
     * Take an html resource file and use it as the body of the email.
     *
     * @param resource
     *            the resource to open
     * @param context
     *            the context class to use relative path
     * @return this
     */
    public EmailBuilder setBodyHtmlFromResource(String resource, Class<?> context) {
        return setBodyHtmlFromString(ResourceTools.getResourceAsString(resource, context));
    }

    /**
     * Take an html String and use it as the body of the email.
     *
     * @param body
     *            the html text
     * @return this
     */
    public EmailBuilder setBodyHtmlFromString(String body) {
        isHtml = true;
        this.body = body;
        return this;
    }

    /**
     * Take a text file and use it as the body of the email.
     *
     * @param filename
     *            the full path of the file
     * @return this
     */
    public EmailBuilder setBodyTextFromFile(String filename) {
        try {
            return setBodyTextFromString(StreamsTools.consumeAsString(new FileInputStream(filename)));
        } catch (FileNotFoundException e) {
            throw new SmallToolsException(e);
        }
    }

    /**
     * Take a Freemarker template and use it as the body of the email.
     *
     * @param resource
     *            the path of the resource
     * @param model
     *            the variables available in the templates
     * @return this
     */
    public EmailBuilder setBodyTextFromFreemarker(String resource, Map<String, ?> model) {
        return setBodyTextFromString(FreemarkerTools.processTemplate(resource, model));
    }

    /**
     * Take a text resource file and use it as the body of the email.
     *
     * @param resource
     *            the absolute resource to open
     * @return this
     */
    public EmailBuilder setBodyTextFromResource(String resource) {
        return setBodyTextFromString(ResourceTools.getResourceAsString(resource, getClass()));
    }

    /**
     * Take a text resource file and use it as the body of the email.
     *
     * @param resource
     *            the resource to open
     * @param context
     *            the context class to use relative path
     * @return this
     */
    public EmailBuilder setBodyTextFromResource(String resource, Class<?> context) {
        return setBodyTextFromString(ResourceTools.getResourceAsString(resource, context));
    }

    /**
     * Take a text String and use it as the body of the email.
     *
     * @param body
     *            the text
     * @return this
     */
    public EmailBuilder setBodyTextFromString(String body) {
        isHtml = false;
        this.body = body;
        return this;
    }

    /**
     * Set the email sender.
     *
     * @param from
     *            the sender
     * @return this
     */
    public EmailBuilder setFrom(String from) {
        this.from = from;
        return this;
    }

    /**
     * Set the subject of the email.
     *
     * @param subject
     *            the subject
     * @return this
     */
    public EmailBuilder setSubject(String subject) {
        this.subject = subject;
        return this;
    }
}
