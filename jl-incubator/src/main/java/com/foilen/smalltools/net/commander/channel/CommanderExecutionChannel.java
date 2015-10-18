/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.commander.channel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.net.commander.command.AbstractCommandImplementationWithResponse;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * A channel that takes a {@link Runnable} and executes it in an executor.
 * 
 * <pre>
 * Dependencies:
 * compile 'io.netty:netty-all:5.0.0.Alpha2'
 * </pre>
 */
public class CommanderExecutionChannel extends ChannelHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(CommanderExecutionChannel.class);

    private ExecutorService executorService = Executors.newCachedThreadPool();

    @SuppressWarnings("rawtypes")
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        logger.debug("Got one {}", msg.getClass().getSimpleName());
        Runnable runnable = (Runnable) msg;

        // Configure the AbstractCommandImplementationWithResponse
        if (runnable instanceof AbstractCommandImplementationWithResponse) {
            AbstractCommandImplementationWithResponse commandWithResponse = (AbstractCommandImplementationWithResponse) runnable;
            commandWithResponse.setChannelHandlerContext(ctx);
        }

        // Execute
        executorService.execute(runnable);
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

}
