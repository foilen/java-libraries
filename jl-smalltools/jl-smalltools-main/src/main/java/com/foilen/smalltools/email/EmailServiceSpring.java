/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.email;

import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.foilen.smalltools.exception.SmallToolsException;

/**
 * A service to help sending all kinds of email. You can quickly send an html or text email or take the time to use the {@link EmailBuilder} that gives you access to all advanced functionnalities
 * (like attachments).
 *
 * <pre>
 * Dependencies:
 * compile 'com.sun.mail:javax.mail:1.6.0'
 * compile 'org.springframework:spring-context-support:4.3.11.RELEASE'
 * </pre>
 */
public class EmailServiceSpring implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void sendEmail(EmailBuilder emailBuilder) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(emailBuilder.getFrom());
            for (String to : emailBuilder.getTos()) {
                helper.addTo(to);
            }
            for (String cc : emailBuilder.getCcs()) {
                helper.addCc(cc);
            }
            for (String bcc : emailBuilder.getBccs()) {
                helper.addBcc(bcc);
            }
            helper.setSubject(emailBuilder.getSubject());
            helper.setText(emailBuilder.getBody(), emailBuilder.isHtml());

            // Inline
            for (EmailAttachment emailAttachment : emailBuilder.getInlineAttachments()) {
                helper.addInline(emailAttachment.getId(), emailAttachment.getResource());
            }

            // Attachment
            for (EmailAttachment emailAttachment : emailBuilder.getAttachments()) {
                helper.addAttachment(emailAttachment.getId(), emailAttachment.getResource());
            }

            mailSender.send(message);
        } catch (Exception e) {
            throw new SmallToolsException("Could not send email", e);
        }
    }

    @Override
    public void sendHtmlEmail(String from, String to, String subject, String html) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);
        } catch (Exception e) {
            throw new SmallToolsException("Could not send email", e);
        }
    }

    @Override
    public void sendTextEmail(String from, String to, String subject, String text) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, false);

            mailSender.send(message);
        } catch (Exception e) {
            throw new SmallToolsException("Could not send email", e);
        }
    }

    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

}
