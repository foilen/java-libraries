/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.commander.connectionpool;

import com.foilen.smalltools.net.commander.CommanderClient;
import com.foilen.smalltools.net.commander.command.AbstractCommandWithResponse;

/**
 * Manages the connections that can be reused.
 */
public interface ConnectionPool {

    /**
     * If the connection pool keeps the connections open, close all of them.
     */
    void closeAllConnections();

    /**
     * If the connection pool keeps a connection open, it can close it.
     * 
     * @param host
     *            the host name
     * @param port
     *            the port
     */
    void closeConnection(String host, int port);

    /**
     * If the connection pool keeps the connections open, tells how many are opened.
     * 
     * @return the number of connections
     */
    int getConnectionsCount();

    /**
     * Send a command to a server or connected client.
     * 
     * @param commanderClient
     *            your {@link CommanderClient} that will create the connections to the server
     * @param host
     *            the host name
     * @param port
     *            the port
     * @param command
     *            the command to run
     */
    void sendCommand(CommanderClient commanderClient, String host, int port, Runnable command);

    /**
     * Send a command to a server or connected client and wait for the response.
     * 
     * @param commanderClient
     *            your {@link CommanderClient} that will create the connections to the server
     * @param host
     *            the host name
     * @param port
     *            the port
     * @param commandWithReply
     *            the command to run
     * @param <R>
     *            the response type
     * @return the response
     */
    <R> R sendCommandAndWaitResponse(CommanderClient commanderClient, String host, int port, AbstractCommandWithResponse<R> commandWithReply);

}
