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
