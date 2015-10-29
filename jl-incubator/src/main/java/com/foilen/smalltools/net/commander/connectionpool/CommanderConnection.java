/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.commander.connectionpool;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.net.commander.CommanderClient;
import com.foilen.smalltools.net.commander.CommanderServer;
import com.foilen.smalltools.net.commander.command.AbstractCommandRequestWithResponse;
import com.foilen.smalltools.net.commander.command.CommandRequest;
import com.foilen.smalltools.tools.AssertTools;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

/**
 * A connection provided by {@link CommanderClient} or {@link CommanderServer}. When trying to send a message, if the channel is closed, it will be reopened automatically if possible.
 */
public class CommanderConnection {

    private static final Logger logger = LoggerFactory.getLogger(CommanderConnection.class);

    private String host;
    private Integer port;
    private CommanderClient commanderClient;

    // Internals
    private ChannelMessagingQueue channelMessagingQueue;

    /**
     * Close the connection and clear the sending queue.
     */
    public void close() {
        if (channelMessagingQueue != null) {
            channelMessagingQueue.close();
            channelMessagingQueue = null;
        }
    }

    public CommanderConnection() {
    }

    /**
     * Create a commander connection using an existing channel.
     * 
     * @param channel
     *            the channel
     */
    public CommanderConnection(Channel channel) {
        SocketAddress socketAddress = channel.remoteAddress();
        if (socketAddress instanceof InetSocketAddress) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
            setHost(inetSocketAddress.getHostString());
            setPort(inetSocketAddress.getPort());
        }

        channelMessagingQueue = new ChannelMessagingQueue(channel);
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

        ChannelFuture channelFuture = commanderClient.createChannelFuture(host, port);
        if (channelFuture == null) {
            throw new SmallToolsException("Could not connect to " + host + ":" + port);
        }
        Channel channel = channelFuture.channel();

        if (channelMessagingQueue == null) {
            channelMessagingQueue = new ChannelMessagingQueue(channel);
        } else {
            channelMessagingQueue.setChannel(channel);
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
        if (channelMessagingQueue == null) {
            return false;
        }

        return channelMessagingQueue.getChannel().isOpen();
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
        channelMessagingQueue.send(command);
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
        String requestId = GlobalCommanderResponseManager.createRequest(channelMessagingQueue.getChannel());
        commandWithReply.setRequestId(requestId);

        // Send
        logger.debug("Sending command with reply {} to {}:{} . Request id is {}", commandWithReply.getClass().getName(), host, port, requestId);
        channelMessagingQueue.send(commandWithReply);

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
