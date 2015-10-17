/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.commander.connectionpool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;

/**
 * This is a queue that manages the messages to send to make sure that it is done in a thread-safe way.
 * 
 * <pre>
* Dependencies:
* compile 'io.netty:netty-all:5.0.0.Alpha2'
 * </pre>
 */
public class ChannelMessagingQueue extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(ChannelMessagingQueue.class);

    private Channel channel;
    private BlockingQueue<Object> msgQueue = new ArrayBlockingQueue<>(1000);
    private AtomicBoolean closeRequested = new AtomicBoolean();

    public ChannelMessagingQueue(Channel channel) {
        super("Messaging-Queue");
        setDaemon(true);
        this.channel = channel;
        start();
    }

    /**
     * Request the channel to be closed and this thread to be stopped.
     */
    public void close() {
        closeRequested.set(true);
        this.interrupt();
    }

    /**
     * Get the channel.
     * 
     * @return the channel
     */
    public Channel getChannel() {
        return channel;
    }

    @Override
    public void run() {
        while (!closeRequested.get()) {
            try {
                Object msg = msgQueue.take();
                channel.writeAndFlush(msg).sync();
            } catch (InterruptedException e) {
                logger.debug("Interrupted");
                break;
            } catch (Exception e) {
                logger.error("Got an exception while sending a message", e);
            }
        }

        if (closeRequested.get()) {
            channel.close();
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

}
