package com.foilen.smalltools.net.communication;

import com.foilen.smalltools.net.connections.ConnectionAction;

/**
 * Manages the actions that are done on every new incoming and outgoing connections.
 */
public interface CommunicationInitialiser {

    /**
     * Add a connection action to incoming and outgoing connections.
     * 
     * @param connectionAction
     *            the action
     */
    void addConnectionAction(ConnectionAction connectionAction);

    /**
     * Add a connection action to incoming connections.
     * 
     * @param connectionAction
     *            the action
     */
    void addConnectionActionToIncoming(ConnectionAction connectionAction);

    /**
     * Add a connection action to outgoing connections.
     * 
     * @param connectionAction
     *            the action
     */
    void addConnectionActionToOutgoing(ConnectionAction connectionAction);

}
