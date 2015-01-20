/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.connections;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * All the incoming and outgoing connections cache.
 */
public class ConnectionsRoom {

    private Map<String, Connection> connectionById = new ConcurrentHashMap<>();
    private ConcurrentLinkedDeque<Connection> anonymousConnections = new ConcurrentLinkedDeque<>();

    /**
     * Register a connection using its {@link Connection#getId()} (if not null). Warning, it will replace an already registered one.
     * 
     * @param connection
     *            the connection
     */
    public void addConnection(Connection connection) {
        String id = connection.getId();
        if (id == null) {
            anonymousConnections.add(connection);
        } else {
            connectionById.put(id, connection);
        }
    }

    /**
     * Retrieve a saved connection if it is still open.
     * 
     * @param id
     *            the id of the connection (as used by your {@link ConnectionFactory})
     * @return the connection or null if not present or disconnected
     */
    public Connection getConnectionById(String id) {
        Connection connection = connectionById.get(id);
        if (connection != null) {
            if (!connection.isConnected()) {
                connectionById.remove(id);
                connection = null;
            }
        }
        return connection;
    }

    /**
     * Send an object to all connected connections.
     * 
     * @param message
     *            the message to send
     */
    public void sendMessageAll(Map<String, Object> message) {
        Iterator<Entry<String, Connection>> namedIt = connectionById.entrySet().iterator();
        while (namedIt.hasNext()) {
            Connection connection = namedIt.next().getValue();
            if (!connection.isConnected()) {
                namedIt.remove();
                continue;
            }

            connection.sendMessage(message);
        }

        Iterator<Connection> anonIt = anonymousConnections.iterator();
        while (anonIt.hasNext()) {
            Connection connection = anonIt.next();
            if (!connection.isConnected()) {
                anonIt.remove();
                continue;
            }

            connection.sendMessage(message);
        }

    }

    /**
     * Send an object to all connected connections. It will be wrapped in a Map with the "object" key.
     * 
     * @param object
     *            the object to send
     */
    public void sendObjectAll(Object object) {
        Map<String, Object> message = new HashMap<>();
        message.put("object", object);
        sendMessageAll(message);
    }
}
