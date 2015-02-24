/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.communication;

import java.util.Map;

import com.foilen.smalltools.net.connections.ConnectionMessageConstants;

/**
 * The part that receives new messages.
 */
public interface CommunicationMessageReceiver {

    /**
     * Wait and get the next available message on the queue. The connection that received the message is on {@link ConnectionMessageConstants#CONNECTION}.
     * 
     * @return the next message
     */
    Map<String, Object> getNextMessage();

}
