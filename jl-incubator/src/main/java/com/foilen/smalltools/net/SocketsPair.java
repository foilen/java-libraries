/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.foilen.smalltools.exception.SmallToolsException;

/**
 * This gives a pair of already connected sockets that talks between them. If you use it in a test, do not forget to set a timeout on the test to make sure it is not blocked.
 *
 * <pre>
 * &#064;Test(timeout = 5000)
 * public void test() throws IOException {
 *     SocketsPair socketsPair = new SocketsPair();
 *
 *     Socket client = socketsPair.getClient();
 *     Socket server = socketsPair.getServer();
 *
 *     // Use the sockets ...
 * }
 * </pre>
 */
public class SocketsPair {
    private Socket client;
    private Socket server;

    public SocketsPair() {
        try {
            // Create the server
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.bind(null);
            int port = serverSocket.getLocalPort();

            // Connect a client to it
            client = new Socket("127.0.0.1", port);

            // Get the socket of the server
            server = serverSocket.accept();

            // Close the server
            serverSocket.close();
        } catch (IOException e) {
            throw new SmallToolsException("Problem creating the pair", e);
        }
    }

    /**
     * @return the client
     */
    public Socket getClient() {
        return client;
    }

    /**
     * @return the server
     */
    public Socket getServer() {
        return server;
    }
}
