/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.net.commander.connectionpool;

import com.foilen.smalltools.net.commander.CommanderClient;

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
     * Call this to get a connection where to send messages.
     *
     * @param commanderClient
     *            your {@link CommanderClient} that will create the connections to the server
     * @param host
     *            the host name
     * @param port
     *            the port
     * @return the connection
     */
    CommanderConnection getConnection(CommanderClient commanderClient, String host, int port);

    /**
     * If the connection pool keeps the connections open, tells how many are opened.
     *
     * @return the number of connections
     */
    int getConnectionsCount();

}
