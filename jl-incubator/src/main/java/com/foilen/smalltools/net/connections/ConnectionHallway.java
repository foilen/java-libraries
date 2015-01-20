/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.connections;

import com.foilen.smalltools.net.connections.actions.ConnectionAssemblyLine;

/**
 * When there is a new connection (incoming or outgoing), it goes through the hallway to execute some actions.
 */
public class ConnectionHallway {

    private ConnectionAssemblyLine incomingAssemblyLine = new ConnectionAssemblyLine();
    private ConnectionAssemblyLine outgoingAssemblyLine = new ConnectionAssemblyLine();

    public ConnectionAssemblyLine getIncomingAssemblyLine() {
        return incomingAssemblyLine;
    }

    public ConnectionAssemblyLine getOutgoingAssemblyLine() {
        return outgoingAssemblyLine;
    }

    /**
     * Execute all the actions for the incoming connection.
     * 
     * @param connection
     *            the connection
     * @return the final connection or null if it was dropped
     */
    public Connection processIncomingConnection(Connection connection) {
        return incomingAssemblyLine.process(connection);
    }

    /**
     * Execute all the actions for the outgoing connection.
     * 
     * @param connection
     *            the connection
     * @return the final connection or null if it was dropped
     */
    public Connection processOutgoingConnection(Connection connection) {
        return outgoingAssemblyLine.process(connection);
    }

    public void setIncomingAssemblyLine(ConnectionAssemblyLine incomingAssemblyLine) {
        this.incomingAssemblyLine = incomingAssemblyLine;
    }

    public void setOutgoingAssemblyLine(ConnectionAssemblyLine outgoingAssemblyLine) {
        this.outgoingAssemblyLine = outgoingAssemblyLine;
    }

}
