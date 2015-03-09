/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.communication.simple;

import java.net.Socket;

import com.foilen.smalltools.net.connections.Connection;
import com.foilen.smalltools.net.services.SocketCallback;

/**
 * Accepts new connections using the system's executor.
 */
public class SimpleIncomingConnection implements SocketCallback {

    private SimpleCommunicationSystem simpleCommunicationSystem;

    public SimpleIncomingConnection(SimpleCommunicationSystem simpleCommunicationSystem) {
        this.simpleCommunicationSystem = simpleCommunicationSystem;
    }

    @Override
    public void newClient(Socket socket) {
        Connection connection = new Connection(socket);
        simpleCommunicationSystem.getExecutorService().execute(new SimpleIncomingConnectionProcess(simpleCommunicationSystem, connection));
    }

}