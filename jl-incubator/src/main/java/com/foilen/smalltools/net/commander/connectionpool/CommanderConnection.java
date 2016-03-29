/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.commander.connectionpool;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.net.commander.CommanderClient;
import com.foilen.smalltools.net.commander.CommanderServer;
import com.foilen.smalltools.net.commander.command.AbstractCommandRequestWithResponse;
import com.foilen.smalltools.net.commander.command.CommandRequest;
import com.foilen.smalltools.net.netty.NettyClient;
import com.foilen.smalltools.tools.AssertTools;

/**
 * A connection provided by {@link CommanderClient} or {@link CommanderServer}. When trying to send a message, if the channel is closed, it will be reopened automatically if possible.
 */
public class CommanderConnection implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(CommanderConnection.class);

    private String host;
    private Integer port;
    private CommanderClient commanderClient;

    // Internals
    private NettyClientMessagingQueue nettyClientMessagingQueue;

    /**
     * Close the connection and clear the sending queue.
     */
    @Override
    public void close() {
        if (nettyClientMessagingQueue != null) {
            nettyClientMessagingQueue.close();
            nettyClientMessagingQueue = null;
        }
    }

    public CommanderConnection() {
    }

    /**
     * Create a commander connection using an existing Netty Client.
     * 
     * @param nettyClient
     *            the Netty Client
     */
    public CommanderConnection(NettyClient nettyClient) {
        SocketAddress socketAddress = nettyClient.getRemoteAddress();
        if (socketAddress instanceof InetSocketAddress) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
            setHost(inetSocketAddress.getHostString());
            setPort(inetSocketAddress.getPort());
        }

        nettyClientMessagingQueue = new NettyClientMessagingQueue(nettyClient);
    }

    /**
     * (Optional) Forces to connect if not already connected. You can send messages without calling this function.
     * 
     * @throws SmallToolsException
     *             if cannot connect
     */
    public synchronized void connect() {
        // Already connected
        if (isConnected()) {
            return;
        }

        AssertTools.assertNotNull(commanderClient, "The connection is not connected and there is no known client to create the connection");
        AssertTools.assertNotNull(host, "The connection is not connected and there is no host set");
        AssertTools.assertNotNull(port, "The connection is not connected and there is no port set");

        NettyClient nettyClient = commanderClient.createNettyClient(host, port);
        if (nettyClientMessagingQueue == null) {
            nettyClientMessagingQueue = new NettyClientMessagingQueue(nettyClient);
        } else {
            nettyClientMessagingQueue.setNettyClient(nettyClient);
        }
    }

    public CommanderClient getCommanderClient() {
        return commanderClient;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    /**
     * Tells if it is currently connected.
     * 
     * @return true if connected
     */
    public synchronized boolean isConnected() {
        if (nettyClientMessagingQueue == null) {
            return false;
        }

        return nettyClientMessagingQueue.isConnected();
    }

    /**
     * Send a command.
     * 
     * @param command
     *            the command to run
     */
    public void sendCommand(CommandRequest command) {

        connect();

        // Send
        logger.debug("Sending command {} to {}:{}", command.getClass().getName(), host, port);
        nettyClientMessagingQueue.send(command);
    }

    /**
     * Send a command and wait for the response.
     * 
     * @param commandWithReply
     *            the command to run
     * @param <R>
     *            the response type
     * @return the response
     */
    @SuppressWarnings("unchecked")
    public <R> R sendCommandAndWaitResponse(AbstractCommandRequestWithResponse<R> commandWithReply) {

        connect();

        // Fill the request
        String requestId = GlobalCommanderResponseManager.createRequest(nettyClientMessagingQueue.getNettyClient());
        commandWithReply.setRequestId(requestId);

        // Send
        logger.debug("Sending command with reply {} to {}:{} . Request id is {}", commandWithReply.getClass().getName(), host, port, requestId);
        nettyClientMessagingQueue.send(commandWithReply);

        // Wait for the result
        return (R) GlobalCommanderResponseManager.waitAndGetResponse(requestId);

    }

    public void setCommanderClient(CommanderClient commanderClient) {
        this.commanderClient = commanderClient;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

}
