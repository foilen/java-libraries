/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.communication;

import java.util.Map;

import com.foilen.smalltools.net.connections.Connection;

/**
 * To have the messages automatically executed by {@link CommunicationCommandExecutor}, they need to implement this interface.
 */
public interface CommunicationCommand {

    /**
     * Execute the command on the remote end.
     * 
     * @param connection
     *            the connection that sent the message
     * @param message
     *            the initial message
     */
    void execute(Connection connection, Map<String, Object> message);

}
