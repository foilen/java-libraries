/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.commander.connectionpool;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.net.commander.CommanderClient;
import com.foilen.smalltools.net.commander.command.AbstractCommandRequestWithResponse;
import com.foilen.smalltools.net.commander.command.CommandRequest;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * Simply keeps 1 connection open per host and sends all the messages in order.
 * 
 * <pre>
 * Dependencies:
 * compile 'io.netty:netty-all:5.0.0.Alpha2'
 * </pre>
 */
public class SimpleConnectionPool implements ConnectionPool, GenericFutureListener<ChannelFuture> {

    private static final Logger logger = LoggerFactory.getLogger(SimpleConnectionPool.class);

    private Map<String, ChannelMessagingQueue> cachedConnections = new HashMap<>();

    @Override
    public void closeAllConnections() {
        synchronized (cachedConnections) {
            for (ChannelMessagingQueue channelMessagingQueue : cachedConnections.values()) {
                channelMessagingQueue.close();
            }

            cachedConnections.clear();
        }
    }

    @Override
    public void closeConnection(String host, int port) {
        logger.debug("Closing connection to {}:{}...", host, port);
        String key = host + ":" + port;
        synchronized (cachedConnections) {
            ChannelMessagingQueue channelMessagingQueue = cachedConnections.remove(key);
            channelMessagingQueue.close();
        }
    }

    private ChannelMessagingQueue getChannel(CommanderClient commanderClient, String host, int port) {

        String key = host + ":" + port;

        synchronized (cachedConnections) {
            ChannelMessagingQueue channelMessagingQueue = cachedConnections.get(key);
            if (channelMessagingQueue == null) {
                ChannelFuture channelFuture = commanderClient.createChannelFuture(host, port);
                if (channelFuture == null) {
                    throw new SmallToolsException("Could not connect to " + host + ":" + port);
                }
                Channel channel = channelFuture.channel();
                channel.closeFuture().addListener(this);
                channelMessagingQueue = new ChannelMessagingQueue(channel);
                cachedConnections.put(key, channelMessagingQueue);
            }

            return channelMessagingQueue;
        }
    }

    @Override
    public int getConnectionsCount() {
        synchronized (cachedConnections) {
            return cachedConnections.size();
        }
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        // Remove from the cache
        synchronized (cachedConnections) {
            Channel channel = future.channel();
            cachedConnections.values().remove(channel);
        }
    }

    @Override
    public void sendCommand(CommanderClient commanderClient, String host, int port, CommandRequest command) {

        ChannelMessagingQueue channelMessagingQueue = getChannel(commanderClient, host, port);

        // Send
        logger.debug("Sending command {} to {}:{}", command.getClass().getName(), host, port);
        channelMessagingQueue.send(command);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> R sendCommandAndWaitResponse(CommanderClient commanderClient, String host, int port, AbstractCommandRequestWithResponse<R> commandWithReply) {

        ChannelMessagingQueue channelMessagingQueue = getChannel(commanderClient, host, port);

        // Fill the request
        String requestId = GlobalCommanderResponseManager.createRequest(channelMessagingQueue.getChannel());
        commandWithReply.setRequestId(requestId);

        // Send
        logger.debug("Sending command with reply {} to {}:{} . Request id is {}", commandWithReply.getClass().getName(), host, port, requestId);
        channelMessagingQueue.send(commandWithReply);

        // Wait for the result
        return (R) GlobalCommanderResponseManager.waitAndGetResponse(requestId);

    }
}
