/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.communication.simple;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.net.connections.Connection;
import com.foilen.smalltools.tools.SocketTools;

/**
 * Send messages to a connection.
 */
public class SimpleMessageSender extends Thread {

    private final static Logger log = LoggerFactory.getLogger(SimpleMessageSender.class);

    public final static Map<String, Object> DISCONNECT_MESSAGE = new HashMap<>();

    private SimpleCommunicationSystem simpleCommunicationSystem;
    private Connection connection;

    private BlockingQueue<Map<String, Object>> queue = new ArrayBlockingQueue<Map<String, Object>>(100);
    private boolean stopRequested;

    /**
     * Initialize and start the sending thread.
     * 
     * @param simpleCommunicationSystem
     *            the communication system that is calling
     * @param connection
     *            the connection
     */
    public SimpleMessageSender(SimpleCommunicationSystem simpleCommunicationSystem, Connection connection) {
        super("Messages Sender - " + connection.getId());
        this.simpleCommunicationSystem = simpleCommunicationSystem;
        this.connection = connection;

        start();
    }

    /**
     * Add one message to send to the queue.
     * 
     * @param message
     *            the message
     */
    public void addMessageToQueue(Map<String, Object> message) {
        queue.add(message);
    }

    /**
     * Request for this thread to be stopped when possible.
     */
    public void requestStop() {
        stopRequested = true;
    }

    @Override
    public void run() {

        log.debug("Starting sending messages for connection {}", connection);

        try {

            while (!stopRequested) {
                Map<String, Object> message = queue.poll(1, TimeUnit.MINUTES);
                if (message == null) {
                    continue;
                }
                if (message == DISCONNECT_MESSAGE) {
                    log.debug("The connection {} got the local disconnect message", connection);
                    stopRequested = true;
                    simpleCommunicationSystem.disconnect(connection);
                    continue;
                }

                // Send the message
                log.debug("Sending message to the connection {}: {}", connection, message);
                connection.sendMessage(message);
                log.debug("Message sent to the connection {}", connection);
            }

        } catch (Exception e) {
            if (SocketTools.isADisconnectionException(e)) {
                log.debug("Connection {} disconnected", connection);
            } else {
                log.warn("Problem sending message", e);
            }
            simpleCommunicationSystem.disconnect(connection);
        }

        log.debug("Ending sending messages for connection {}", connection);
    }
}
