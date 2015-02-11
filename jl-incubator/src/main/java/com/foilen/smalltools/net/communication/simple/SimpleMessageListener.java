package com.foilen.smalltools.net.communication.simple;

import java.util.Map;

import com.foilen.smalltools.net.connections.Connection;
import com.foilen.smalltools.net.connections.ConnectionMessageConstants;

/**
 * Listen to a connection for incoming messages.
 */
public class SimpleMessageListener extends Thread {

    private SimpleCommunicationSystem simpleCommunicationSystem;
    private Connection connection;

    /**
     * Initialize and start the listening thread.
     * 
     * @param simpleCommunicationSystem
     *            the communication system that is calling
     * @param connection
     *            the connection
     */
    public SimpleMessageListener(SimpleCommunicationSystem simpleCommunicationSystem, Connection connection) {
        this.simpleCommunicationSystem = simpleCommunicationSystem;
        this.connection = connection;

        start();
    }

    @Override
    public void run() {

        try {

            while (true) {
                Map<String, Object> message = connection.getConnectionLanguage().receiveMessage(connection);
                message.put(ConnectionMessageConstants.CONNECTION, connection);
                simpleCommunicationSystem.addNewMessage(message);
            }

        } catch (Exception e) {
            simpleCommunicationSystem.disconnect(connection);
        }

    }

}
