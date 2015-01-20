/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.connections;

import java.util.Map;

/**
 * This is a way for two connections to discuss in a way that they both understand.
 */
public interface ConnectionLanguage {

    /**
     * Receive the next message.
     * 
     * @param connection
     *            the connection to listen on
     * @return the message
     */
    Map<String, Object> receiveMessage(Connection connection);

    /**
     * Send a message. Warning: the method could hang if the remote party does not read fast enough.
     * 
     * @param connection
     *            the connection to send to
     * @param message
     *            the message to send
     */
    void sendMessage(Connection connection, Map<String, Object> message);

}
