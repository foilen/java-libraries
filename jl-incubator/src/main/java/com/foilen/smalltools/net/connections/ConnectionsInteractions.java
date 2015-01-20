/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.connections;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.foilen.smalltools.event.EventList;

/**
 * Lists all the incoming messages and can dispatch events about the connections. You must have at least one thread that reads the messages on {@link #getMessagesQueue()}.
 */
public class ConnectionsInteractions {

    private EventList<Connection> connectionConnectEvents = new EventList<>();
    private EventList<Connection> connectionDisconnectEvents = new EventList<>();
    private EventList<Connection> connectionReceiveMessageEvents = new EventList<>();

    private BlockingQueue<Map<String, Object>> messagesQueue = new ArrayBlockingQueue<>(1000);

    public void addReceivedMessage(Connection connection, Map<String, Object> message) {
        messagesQueue.add(message);
        connectionReceiveMessageEvents.dispatch(connection);
    }

    public void dispatchConnected(Connection connection) {
        connectionConnectEvents.dispatch(connection);
    }

    public void dispatchDisconnected(Connection connection) {
        connectionDisconnectEvents.dispatch(connection);
    }

    public EventList<Connection> getConnectionConnectEvents() {
        return connectionConnectEvents;
    }

    public EventList<Connection> getConnectionDisconnectEvents() {
        return connectionDisconnectEvents;
    }

    public EventList<Connection> getConnectionReceiveMessageEvents() {
        return connectionReceiveMessageEvents;
    }

    /**
     * Get the messages queue to retrieve the messages.
     * 
     * @return the messages queue
     */
    public BlockingQueue<Map<String, Object>> getMessagesQueue() {
        return messagesQueue;
    }

    /**
     * Set the messages queue if you do not want to use the default one.
     * 
     * @param messagesQueue
     *            the messages queue
     */
    public void setMessagesQueue(BlockingQueue<Map<String, Object>> messagesQueue) {
        this.messagesQueue = messagesQueue;
    }

}
