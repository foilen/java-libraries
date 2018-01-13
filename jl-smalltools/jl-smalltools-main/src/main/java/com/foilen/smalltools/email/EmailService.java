/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.email;

/**
 * An interface for an email sender service. See {@link EmailServiceSpring} for the implementation to use.
 */
public interface EmailService {

    /**
     * Send an advanced email defined with the builder.
     *
     * @param emailBuilder
     *            the builder
     */
    void sendEmail(EmailBuilder emailBuilder);

    /**
     * Send an html email.
     *
     * @param from
     *            the email that sends it
     * @param to
     *            the email destination
     * @param subject
     *            the email's subject
     * @param html
     *            the body of the email in html format
     */
    void sendHtmlEmail(String from, String to, String subject, String html);

    /**
     * Send a text email.
     *
     * @param from
     *            the email that sends it
     * @param to
     *            the email destination
     * @param subject
     *            the email's subject
     * @param text
     *            the body of the email in text format
     */
    void sendTextEmail(String from, String to, String subject, String text);

}
