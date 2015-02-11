/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.connections;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.net.connections.language.YamlConnectionLanguage;
import com.foilen.smalltools.streampair.StreamPair;

/**
 * This is a tcp network connection that can send serialized messages.
 */
public class Connection {

    private String id;
    private Socket socket;
    private StreamPair streamPair;
    private ConnectionLanguage connectionLanguage = new YamlConnectionLanguage();

    /**
     * Create a connection that uses Yaml to communicate.
     * 
     * @param socket
     *            an opened socket
     */
    public Connection(Socket socket) {
        this.socket = socket;
        this.streamPair = new StreamPair(socket);
    }

    /**
     * Create a connection.
     * 
     * @param socket
     *            an opened socket
     * @param connectionLanguage
     *            the language for the communication
     */
    public Connection(Socket socket, ConnectionLanguage connectionLanguage) {
        this.socket = socket;
        this.streamPair = new StreamPair(socket);
        this.connectionLanguage = connectionLanguage;
    }

    /**
     * Create a connection that uses Yaml to communicate.
     * 
     * @param socket
     *            an opened socket
     * @param id
     *            the id of this connection. The one that your {@link ConnectionFactory} understands.
     */
    public Connection(Socket socket, String id) {
        this.socket = socket;
        this.streamPair = new StreamPair(socket);
        this.id = id;
    }

    /**
     * Create a new socket that connects there.
     * 
     * @param host
     *            the host or ip
     * @param port
     *            the port
     */
    public Connection(String host, int port) {
        try {
            this.socket = new Socket(host, port);
            this.streamPair = new StreamPair(socket);
        } catch (Exception e) {
            throw new SmallToolsException("Could not connect to remote server", e);
        }
    }

    /**
     * Close the connection.
     */
    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
        }
        streamPair.close();
    }

    public ConnectionLanguage getConnectionLanguage() {
        return connectionLanguage;
    }

    /**
     * Get the id of this connection.
     * 
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Get the socket's inet address.
     * 
     * @return the inet address
     */
    public InetAddress getInetAddress() {
        return socket.getInetAddress();
    }

    /**
     * Get the input stream (that can have been modified).
     * 
     * @return the input stream
     */
    public InputStream getInputStream() {
        return streamPair.getInputStream();
    }

    /**
     * Get the output stream (that can have been modified).
     * 
     * @return the output stream
     */
    public OutputStream getOutputStream() {
        return streamPair.getOutputStream();
    }

    /**
     * Get the socket's port.
     * 
     * @return the port
     */
    public int getPort() {
        return socket.getPort();
    }

    /**
     * Get the stream pair to modify the streams on it.
     * 
     * @return the stream pair
     */
    public StreamPair getStreamPair() {
        return streamPair;
    }

    /**
     * Tells if the connection is in a connected state.
     * 
     * @return true if connected
     */
    public boolean isConnected() {
        return socket.isConnected() && !socket.isClosed();
    }

    /**
     * Send a message to this connection. Warning: make sure that no other threads are using the {@link StreamPair} or the Streams.
     * 
     * @param message
     *            the message to send
     */
    public synchronized void sendMessage(Map<String, Object> message) {
        connectionLanguage.sendMessage(this, message);
    }

    /**
     * Send an object to this connection. It will be wrapped in a Map with the {@link ConnectionMessageConstants#OBJECT} key. Warning: make sure that no other threads are using the {@link StreamPair}
     * or the Streams.
     * 
     * @param object
     *            the object to send
     */
    public synchronized void sendObject(Object object) {
        Map<String, Object> message = new HashMap<>();
        message.put(ConnectionMessageConstants.OBJECT, object);
        connectionLanguage.sendMessage(this, message);
    }

    /**
     * Set the id of this connection.
     * 
     * @param id
     *            the id
     */
    public void setId(String id) {
        this.id = id;
    }

}
