/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.commander.connectionpool;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.net.netty.NettyClient;
import com.foilen.smalltools.tools.CloseableTools;
import com.foilen.smalltools.tools.ThreadTools;

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

    private NettyClient nettyClient;
    private LinkedBlockingDeque<Object> msgQueue = new LinkedBlockingDeque<>(1000);
    private AtomicBoolean closeRequested = new AtomicBoolean();

    public NettyClientMessagingQueue(NettyClient nettyClient) {
        super("Messaging-Queue");
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
        while (!closeRequested.get()) {
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

        if (closeRequested.get()) {
            CloseableTools.close(nettyClient);
        }
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
