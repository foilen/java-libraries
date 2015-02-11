/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.communication;

import java.util.Map;

import com.foilen.smalltools.net.connections.ConnectionFactory;

/**
 * The part that sends messages.
 */
public interface CommunicationMessageSender {

    /**
     * Send a message to everyone connected.
     * 
     * @param message
     *            the message
     */
    void sendMessage(Map<String, Object> message);

    /**
     * Send a message to the specific id.
     * 
     * @param id
     *            the id that the {@link ConnectionFactory} understands
     * @param message
     *            the message
     * @return true if could send it to it
     */
    boolean sendMessage(String id, Map<String, Object> message);

    /**
     * Send a message to everyone connected.
     * 
     * @param object
     *            the message (will be wrapped in a map)
     */
    void sendObject(Object object);

    /**
     * Send a message to the specific id.
     * 
     * @param id
     *            the id that the {@link ConnectionFactory} understands
     * @param object
     *            the message (will be wrapped in a map)
     * @return true if could send it to it
     */
    boolean sendObject(String id, Object object);
}
