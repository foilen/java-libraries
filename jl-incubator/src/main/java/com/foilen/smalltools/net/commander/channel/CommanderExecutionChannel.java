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
import com.foilen.smalltools.net.commander.command.CommandImplementation;
import com.foilen.smalltools.tools.SpringTools;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * A channel that takes a {@link CommandImplementation} and executes it in an executor.
 * 
 * <pre>
 * Dependencies:
 * compile 'io.netty:netty-all:5.0.0.Alpha2'
 * compile 'org.springframework:spring-beans:4.1.6.RELEASE' (optional)
 * </pre>
 */
public class CommanderExecutionChannel extends ChannelHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(CommanderExecutionChannel.class);

    private ExecutorService executorService = Executors.newCachedThreadPool();

    private boolean configureSpring;

    /**
     * Create the channel handler that executes a {@link CommandImplementation}.
     * 
     * @param configureSpring
     *            true to configure the {@link CommandImplementation} (e.g: fill the @Autowired)
     */
    public CommanderExecutionChannel(boolean configureSpring) {
        this.setConfigureSpring(configureSpring);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        logger.debug("Got one {}", msg.getClass().getSimpleName());
        CommandImplementation commandImplementation = (CommandImplementation) msg;

        // Configure the AbstractCommandImplementationWithResponse
        if (commandImplementation instanceof AbstractCommandImplementationWithResponse) {
            AbstractCommandImplementationWithResponse commandWithResponse = (AbstractCommandImplementationWithResponse) commandImplementation;
            commandWithResponse.setChannelHandlerContext(ctx);
        }

        // Configure Spring if needed
        if (configureSpring) {
            SpringTools.configure(commandImplementation);
        }

        // Execute
        executorService.execute(commandImplementation);
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public boolean isConfigureSpring() {
        return configureSpring;
    }

    public void setConfigureSpring(boolean configureSpring) {
        this.configureSpring = configureSpring;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

}
