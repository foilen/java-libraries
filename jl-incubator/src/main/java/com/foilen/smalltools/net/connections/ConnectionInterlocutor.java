/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.connections;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read all the messages that comes in a connection and send them to the {@link ConnectionsInteractions}.
 */
public class ConnectionInterlocutor extends Thread {

    protected final static Logger log = LoggerFactory.getLogger(ConnectionInterlocutor.class);

    private Connection connection;
    private ConnectionsInteractions connectionsInteractions;

    public ConnectionInterlocutor(Connection connection, ConnectionsInteractions connectionsInteractions) {
        this.connection = connection;
        this.connectionsInteractions = connectionsInteractions;
    }

    @Override
    public void run() {

        connectionsInteractions.dispatchConnected(connection);

        log.info("Starting reading messages on connection {}", connection.getId());

        try {
            while (true) {
                Map<String, Object> message = connection.getConnectionLanguage().receiveMessage(connection);
                log.debug("Got message: {}", message);
                connectionsInteractions.addReceivedMessage(connection, message);
            }
        } catch (Exception e) {
            log.error("Problem while reading a message", e);
        } finally {
            log.info("Closing pair");
            connection.close();
        }

        log.info("Stop listening on connection {}", connection.getId());
        connectionsInteractions.dispatchDisconnected(connection);
    }

}
