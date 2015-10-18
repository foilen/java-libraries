/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.commander.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;

/**
 * Extend this class to create a command that sends back a result.
 * 
 * @param <R>
 *            the response type
 * 
 * 
 *            <pre>
 * Dependencies:
 * compile 'io.netty:netty-all:5.0.0.Alpha2'
 *            </pre>
 */
public abstract class AbstractCommandImplementationWithResponse<R> implements CommandImplementation {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ChannelHandlerContext channelHandlerContext;

    private String requestId;

    public String getRequestId() {
        return requestId;
    }

    @Override
    public void run() {

        logger.debug("Running command with requestId {}", requestId);

        R response = runWithResponse();
        CommandResponse<R> msg = new CommandResponse<>(requestId, response);
        channelHandlerContext.channel().writeAndFlush(msg);

        logger.debug("Giving back the response of requestId {}", requestId);
    }

    /**
     * Overwrite with the method that will return a reply.
     * 
     * @return the reply
     */
    protected abstract R runWithResponse();

    public void setChannelHandlerContext(ChannelHandlerContext channelHandlerContext) {
        this.channelHandlerContext = channelHandlerContext;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

}
