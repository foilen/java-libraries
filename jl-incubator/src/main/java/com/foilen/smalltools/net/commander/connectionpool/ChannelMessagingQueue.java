/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.commander.connectionpool;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class ChannelMessagingQueue extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(ChannelMessagingQueue.class);

    private Channel channel;
    private LinkedBlockingDeque<Object> msgQueue = new LinkedBlockingDeque<>(1000);
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
            Object msg = null;
            try {
                msg = msgQueue.take();
                channel.writeAndFlush(msg).sync();
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

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

}
