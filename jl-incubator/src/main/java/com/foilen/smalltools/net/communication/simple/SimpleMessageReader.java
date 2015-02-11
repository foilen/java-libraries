package com.foilen.smalltools.net.communication.simple;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.SocketTools;
import com.foilen.smalltools.net.connections.Connection;
import com.foilen.smalltools.net.connections.ConnectionMessageConstants;

/**
 * Reading to a connection for incoming messages.
 */
public class SimpleMessageReader extends Thread {

    private final static Logger log = LoggerFactory.getLogger(SimpleMessageReader.class);

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
    public SimpleMessageReader(SimpleCommunicationSystem simpleCommunicationSystem, Connection connection) {
        this.simpleCommunicationSystem = simpleCommunicationSystem;
        this.connection = connection;

        start();
    }

    @Override
    public void run() {

        log.debug("Starting reading messages for connection {}", connection);

        try {

            while (true) {
                Map<String, Object> message = connection.getConnectionLanguage().receiveMessage(connection);
                log.debug("Got a new message from the connection {}: {}", connection, message);
                message.put(ConnectionMessageConstants.CONNECTION, connection);
                simpleCommunicationSystem.addNewMessage(message);
            }

        } catch (Exception e) {
            if (SocketTools.isADisconnectionException(e)) {
                log.debug("Connection {} disconnected", connection);
            } else {
                log.warn("Problem reading message", e);
            }
            simpleCommunicationSystem.disconnect(connection);
        }

        log.debug("Ending reading message for connection {}", connection);

    }

}
