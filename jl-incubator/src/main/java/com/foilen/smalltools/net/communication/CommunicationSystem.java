/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.communication;

import com.foilen.smalltools.net.connections.Connection;
import com.foilen.smalltools.net.connections.ConnectionFactory;

/**
 * A full communication system that can create and accept connections and that can send and receive messages.
 */
public interface CommunicationSystem extends CommunicationInitialiser, CommunicationMessageSender, CommunicationEventsManager, CommunicationMessageReceiver {

    /**
     * Get or create a connection.
     * 
     * @param id
     *            the id that the {@link ConnectionFactory} understands
     * @return the connection or null if could not connect
     */
    Connection connectTo(String id);

    /**
     * Remove the connection.
     * 
     * @param id
     *            the id that the {@link ConnectionFactory} understands
     */
    void disconnect(String id);

    /**
     * Wait for the sending queues to be empty and remove all the connections. This is waiting for the completion.
     */
    void disconnectAfterMessagesSent();

    /**
     * Wait for the sending queue to be empty and remove the connection. This is waiting for the completion.
     * 
     * @param id
     *            the id that the {@link ConnectionFactory} understands
     */
    void disconnectAfterMessagesSent(String id);

}
