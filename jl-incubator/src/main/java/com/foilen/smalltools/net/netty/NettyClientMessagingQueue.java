/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.net.netty;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.tools.AssertTools;
import com.foilen.smalltools.tools.CloseableTools;
import com.foilen.smalltools.tools.ThreadTools;

import io.netty.channel.Channel;
import io.netty.channel.ChannelException;

/**
 * This is a queue that manages the messages to send to make sure that it is done in a thread-safe way.
 *
 * <pre>
* Dependencies:
* compile 'io.netty:netty-all:5.0.0.Alpha2'
 * </pre>
 */
public class NettyClientMessagingQueue extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(NettyClientMessagingQueue.class);

    static private final ConcurrentHashMap<Channel, NettyClientMessagingQueue> messagingQueueByChannel = new ConcurrentHashMap<>();
    static private boolean cleanupTaskStarted = false;

    /**
     * Get the {@link NettyClientMessagingQueue} for the channel. Will be the same instance for the same channel.
     *
     * @param channel
     *            the channel
     * @return the {@link NettyClientMessagingQueue} or null if the channel is null
     */
    static public NettyClientMessagingQueue getInstance(Channel channel) {
        if (channel == null) {
            return null;
        }
        NettyClientMessagingQueue nettyClientMessagingQueue = messagingQueueByChannel.get(channel);
        if (nettyClientMessagingQueue == null) {
            nettyClientMessagingQueue = getNewInstance(channel);
        }
        return nettyClientMessagingQueue;
    }

    /**
     * Get the {@link NettyClientMessagingQueue} for the channel in the {@link NettyClient}. Will be the same instance for the same channel.
     *
     * @param nettyClient
     *            the netty client
     * @return the {@link NettyClientMessagingQueue} or null if the channel or the nettyClient is null
     */
    static public NettyClientMessagingQueue getInstance(NettyClient nettyClient) {
        if (nettyClient == null) {
            return null;
        }
        return getInstance(nettyClient.channel);
    }

    static private synchronized NettyClientMessagingQueue getNewInstance(Channel channel) {
        NettyClientMessagingQueue nettyClientMessagingQueue = messagingQueueByChannel.get(channel);
        if (nettyClientMessagingQueue == null) {
            NettyClient nettyClient = new NettyClient(channel);
            nettyClientMessagingQueue = new NettyClientMessagingQueue(nettyClient);
            logger.info("[{}] New messaging queue", nettyClient.getPeerIp());
            messagingQueueByChannel.put(channel, nettyClientMessagingQueue);

            // Start the cleanup task if not yet started
            if (!cleanupTaskStarted) {
                cleanupTaskStarted = true;
                Thread cleanupThread = new Thread((Runnable) () -> {
                    for (;;) {
                        // Wait 5 minutes
                        ThreadTools.sleep(5 * 60000);
                        logger.debug("Checking for stopped messaging queues");

                        // Check for those closed
                        Iterator<NettyClientMessagingQueue> it = messagingQueueByChannel.values().iterator();
                        while (it.hasNext()) {
                            NettyClientMessagingQueue next = it.next();
                            if (!next.isConnected()) {
                                next.close();
                                it.remove();
                            }
                        }
                    }
                }, "NettyClientMessagingQueue - Cleanup");
                cleanupThread.setDaemon(true);
                cleanupThread.start();
            }
        }
        return nettyClientMessagingQueue;
    }

    private NettyClient nettyClient;

    private LinkedBlockingDeque<Object> msgQueue = new LinkedBlockingDeque<>(1000);

    private AtomicBoolean closeRequested = new AtomicBoolean();

    private NettyClientMessagingQueue(NettyClient nettyClient) {
        super("Messaging-Queue " + nettyClient.getPeerIp());
        AssertTools.assertTrue(nettyClient.isConnected(), "The netty client is not connected");
        setDaemon(true);
        this.nettyClient = nettyClient;
        start();
    }

    /**
     * Request the channel to be closed and this thread to be stopped.
     */
    public void close() {
        closeRequested.set(true);
        this.interrupt();
    }

    public NettyClient getNettyClient() {
        return nettyClient;
    }

    /**
     * Tells if it is currently connected.
     *
     * @return true if connected
     */
    public boolean isConnected() {
        return nettyClient.isConnected();
    }

    @Override
    public void run() {
        while (!closeRequested.get() && nettyClient.isConnected()) {
            Object msg = null;
            try {
                msg = msgQueue.take();
                nettyClient.writeFlushWait(msg);
            } catch (InterruptedException e) {
                logger.debug("Interrupted");
                break;
            } catch (ChannelException e) {
                logger.warn("Got an io exception while sending a message. Will retry", e);
                if (msg != null) {
                    msgQueue.addFirst(msg);
                }
                ThreadTools.sleep(5000);
            } catch (Exception e) {
                logger.error("Got an exception while sending a message", e);
            }
        }

        CloseableTools.close(nettyClient);
    }

    /**
     * Place a message to send on the sending queue.
     *
     * @param msg
     *            the message
     */
    public void send(Object msg) {
        try {
            msgQueue.put(msg);
        } catch (InterruptedException e) {
            logger.error("Was interrupted while placing a message on the queue");
        }
    }

    public void setNettyClient(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
    }

}
