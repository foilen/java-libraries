/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.commander.channel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.net.commander.CommanderClient;
import com.foilen.smalltools.net.commander.command.CommandImplementation;
import com.foilen.smalltools.net.commander.command.CommandImplementationConnectionAware;
import com.foilen.smalltools.net.commander.connectionpool.CommanderConnection;
import com.foilen.smalltools.tools.SpringTools;
import com.foilen.smalltools.tools.ThreadTools;

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

    private Map<ChannelHandlerContext, CommanderConnection> cachedConnections = new HashMap<>();

    private ExecutorService executorService = Executors.newCachedThreadPool();

    private boolean configureSpring;

    private CommanderClient commanderClient;

    private Thread cleanupThread;

    /**
     * Create the channel handler that executes a {@link CommandImplementation}.
     * 
     * @param configureSpring
     *            true to configure the {@link CommandImplementation} (e.g: fill the @Autowired)
     * @param commanderClient
     *            (optional) the commander client to be able to reconnect
     */
    public CommanderExecutionChannel(boolean configureSpring, CommanderClient commanderClient) {
        this.setConfigureSpring(configureSpring);
        this.setCommanderClient(commanderClient);

        cleanupThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        ThreadTools.sleep(2 * 60000);
                        logger.debug("Cleaning up the cached connections");
                        synchronized (cachedConnections) {
                            Iterator<Entry<ChannelHandlerContext, CommanderConnection>> it = cachedConnections.entrySet().iterator();
                            while (it.hasNext()) {
                                Entry<ChannelHandlerContext, CommanderConnection> next = it.next();
                                if (!next.getValue().isConnected()) {
                                    logger.debug("Removing connection {}:{}", next.getValue().getHost(), next.getValue().getPort());
                                    it.remove();
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Got an exception in the cleanup thread", e);
                    }
                }
            }
        });
        cleanupThread.setName("SimpleConnectionPool - Cleanup");
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        logger.debug("Got one {}", msg.getClass().getSimpleName());
        CommandImplementation commandImplementation = (CommandImplementation) msg;

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
    }

    public CommanderClient getCommanderClient() {
        return commanderClient;
    }

    private CommanderConnection getCommanderConnection(ChannelHandlerContext channelHandlerContext) {

        CommanderConnection commanderConnection;

        synchronized (cachedConnections) {
            commanderConnection = cachedConnections.get(channelHandlerContext);
            if (commanderConnection == null) {
                commanderConnection = new CommanderConnection(channelHandlerContext.channel());
                commanderConnection.setCommanderClient(commanderClient);
                cachedConnections.put(channelHandlerContext, commanderConnection);
            }
        }

        return commanderConnection;
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
