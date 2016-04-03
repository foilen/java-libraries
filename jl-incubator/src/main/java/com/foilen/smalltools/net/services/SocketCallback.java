/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.services;

import java.net.Socket;

/**
 * A server callback when a new connection is made.
 */
public interface SocketCallback {

    /**
     * Called when there is a new connection made.
     * 
     * @param socket
     *            the socket of the client
     */
    void newClient(Socket socket);

}
