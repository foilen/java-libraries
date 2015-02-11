package com.foilen.smalltools.net.communication;

import com.foilen.smalltools.event.EventCallback;
import com.foilen.smalltools.net.connections.Connection;

/**
 * Manages the events that can happen on the communication system.
 */
public interface CommunicationEventsManager {

    /**
     * Add an event callback for when a new connections is made (incoming or outgoing).
     * 
     * @param callback
     *            the callback
     */
    void addEventConnectedCallback(EventCallback<Connection> callback);

    /**
     * Add an event callback for when a connections is disconnected.
     * 
     * @param callback
     *            the callback
     */
    void addEventDisconnectedCallback(EventCallback<Connection> callback);

    /**
     * Add an event callback for when a new message is in the queue.
     * 
     * @param callback
     *            the callback
     */
    void addEventMessageReceivedCallback(EventCallback<Connection> callback);

}
