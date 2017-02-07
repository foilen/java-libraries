/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.net.commander.channel;

import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.net.commander.CommanderClient;
import com.foilen.smalltools.net.commander.command.CommandImplementation;
import com.foilen.smalltools.net.commander.command.CommandImplementationConnectionAware;
import com.foilen.smalltools.net.commander.connectionpool.CommanderConnection;
import com.foilen.smalltools.net.netty.NettyClient;
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

    private ExecutorService executorService;

    private boolean configureSpring;

    private CommanderClient commanderClient;

    private CommanderConnection cachedCommanderConnection;

    /**
     * Create the channel handler that executes a {@link CommandImplementation}.
     *
     * @param configureSpring
     *            true to configure the {@link CommandImplementation} (e.g: fill the @Autowired)
     * @param commanderClient
     *            (optional) the commander client to be able to reconnect
     * @param executorService
     *            an executor service
     */
    public CommanderExecutionChannel(boolean configureSpring, CommanderClient commanderClient, ExecutorService executorService) {
        this.setConfigureSpring(configureSpring);
        this.setCommanderClient(commanderClient);
        this.setExecutorService(executorService);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        logger.debug("Got one {}", msg.getClass().getSimpleName());
        CommandImplementation commandImplementation = (CommandImplementation) msg;

        try {
            // Configure the CommandImplementationChannelAware
            if (commandImplementation instanceof CommandImplementationConnectionAware) {
                CommandImplementationConnectionAware commandImplementationChannelAware = (CommandImplementationConnectionAware) commandImplementation;
                commandImplementationChannelAware.setCommanderConnection(getCommanderConnection(ctx));
            }

            // Configure Spring if needed
            if (configureSpring) {
                SpringTools.configure(commandImplementation);
            }

            // Execute
            executorService.execute(commandImplementation);
        } catch (Exception e) {
            logger.error("Problem configuring the command", e);
        }
    }

    public CommanderClient getCommanderClient() {
        return commanderClient;
    }

    private CommanderConnection getCommanderConnection(ChannelHandlerContext channelHandlerContext) {
        if (cachedCommanderConnection == null) {
            cachedCommanderConnection = new CommanderConnection(new NettyClient(channelHandlerContext.channel()));
            cachedCommanderConnection.setCommanderClient(commanderClient);
        }
        return cachedCommanderConnection;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public boolean isConfigureSpring() {
        return configureSpring;
    }

    public void setCommanderClient(CommanderClient commanderClient) {
        this.commanderClient = commanderClient;
    }

    public void setConfigureSpring(boolean configureSpring) {
        this.configureSpring = configureSpring;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

}
