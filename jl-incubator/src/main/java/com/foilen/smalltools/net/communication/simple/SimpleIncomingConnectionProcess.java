/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.communication.simple;

import com.foilen.smalltools.net.connections.Connection;

/**
 * JAVADOC
 */
public class SimpleIncomingConnectionProcess implements Runnable {

    private SimpleCommunicationSystem simpleCommunicationSystem;

    private Connection connection;

    public SimpleIncomingConnectionProcess(SimpleCommunicationSystem simpleCommunicationSystem, Connection connection) {
        this.simpleCommunicationSystem = simpleCommunicationSystem;
        this.connection = connection;
    }

    @Override
    public void run() {
        simpleCommunicationSystem.registerIncomingConnection(connection);
    }

}
