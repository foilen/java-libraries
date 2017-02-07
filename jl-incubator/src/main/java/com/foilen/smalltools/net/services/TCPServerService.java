/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.net.services;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.tools.CloseableTools;

/**
 * This is a TCP server. All the logic to handle new connections is taken care of and all you need to have is a {@link SocketCallback}. Upon instantiation, a new thread is started right away.
 *
 * Usage:
 *
 * <pre>
 * // An example of an Hello Server
 * public class HelloSocketCallback implements SocketCallback {
 *
 *     private String text;
 *
 *     public HelloSocketCallback(String text) {
 *         this.text = text;
 *     }
 *
 *     &#064;Override
 *     public void newClient(Socket socket) {
 *         try {
 *             socket.getOutputStream().write(text.getBytes());
 *         } catch (IOException e) {
 *         }
 *     }
 * }
 *
 * // Create a TCP server on port 9090
 * TCPServerService tcpServerService = new TCPServerService(9090, new HelloSocketCallback(&quot;Hello World&quot;));
 *
 * </pre>
 */
public class TCPServerService implements Closeable, Runnable {

    private static final Logger logger = LoggerFactory.getLogger(TCPServerService.class);

    private Thread thread;
    private ServerSocket serverSocket;

    private SocketCallback socketCallback;

    /**
     * Create a new TCP server on a specific port.
     *
     * @param port
     *            the desired port
     * @param socketCallback
     *            when someone connects to this new server, this handler will be called
     */
    public TCPServerService(int port, SocketCallback socketCallback) {
        this.socketCallback = socketCallback;

        initServer(port);
        initThread();
    }

    /**
     * Create a new TCP server on any available port.
     *
     * @param socketCallback
     *            when someone connects to this new server, this handler will be called
     */
    public TCPServerService(SocketCallback socketCallback) {
        this.socketCallback = socketCallback;

        initServer(null);
        initThread();
    }

    /**
     * Use an already created TCP server.
     *
     * @param socketCallback
     *            when someone connects to this new server, this handler will be called
     * @param serverSocket
     *            the already created TCP server
     */
    public TCPServerService(SocketCallback socketCallback, ServerSocket serverSocket) {

        this.socketCallback = socketCallback;
        this.serverSocket = serverSocket;

        initThread();
    }

    @Override
    public void close() {
        ServerSocket old = serverSocket;
        serverSocket = null;
        CloseableTools.close(old);

    }

    /**
     * Get the listening port.
     *
     * @return the port
     */
    public int getPort() {
        return serverSocket.getLocalPort();
    }

    private void initServer(Integer port) {
        // Create the server
        try {
            serverSocket = new ServerSocket();
            if (port == null) {
                serverSocket.bind(null);
            } else {
                serverSocket.bind(new InetSocketAddress(port));
            }
        } catch (Exception e) {
            logger.error("Error binding server", e);
            throw new SmallToolsException(e);
        }
    }

    private void initThread() {
        // Start the thread
        thread = new Thread(this);
        thread.start();
        thread.setName("TCPServerService-" + getPort());
        logger.info("TCP Server listening on port {}", getPort());
    }

    @Override
    public void run() {
        while (serverSocket != null) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
                InetSocketAddress remoteAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
                logger.info("Client is connecting from {}:{}", new Object[] { remoteAddress.getHostName(), remoteAddress.getPort() });
                socketCallback.newClient(socket);
            } catch (Exception e) {
                CloseableTools.close(socket);
                logger.error("Problem while accepting connection", e);
            }
        }
    }

}
