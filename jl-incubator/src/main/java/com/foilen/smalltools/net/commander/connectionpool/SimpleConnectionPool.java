/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.commander.connectionpool;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.net.commander.CommanderClient;
import com.foilen.smalltools.tools.CloseableTools;
import com.foilen.smalltools.tools.ThreadTools;

/**
 * Simply keeps 1 connection open per host and sends all the messages in order.
 * 
 * <pre>
 * Dependencies:
 * compile 'io.netty:netty-all:5.0.0.Alpha2'
 * </pre>
 */
public class SimpleConnectionPool implements ConnectionPool {

    private static final Logger logger = LoggerFactory.getLogger(SimpleConnectionPool.class);

    private Map<String, CommanderConnection> cachedConnections = new HashMap<>();
    private Thread cleanupThread;

    public SimpleConnectionPool() {
        cleanupThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        ThreadTools.sleep(2 * 60000);
                        logger.debug("Cleaning up the cached connections");
                        synchronized (cachedConnections) {
                            Iterator<Entry<String, CommanderConnection>> it = cachedConnections.entrySet().iterator();
                            while (it.hasNext()) {
                                Entry<String, CommanderConnection> next = it.next();
                                if (!next.getValue().isConnected()) {
                                    logger.debug("Removing connection {}", next.getKey());
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
    public void closeAllConnections() {
        synchronized (cachedConnections) {
            for (CommanderConnection commanderConnection : cachedConnections.values()) {
                commanderConnection.close();
            }

            cachedConnections.clear();
        }
    }

    @Override
    public void closeConnection(String host, int port) {
        logger.debug("Closing connection to {}:{}...", host, port);
        String key = host + ":" + port;
        synchronized (cachedConnections) {
            CommanderConnection commanderConnection = cachedConnections.remove(key);
            CloseableTools.close(commanderConnection);
        }
    }

    @Override
    public CommanderConnection getConnection(CommanderClient commanderClient, String host, int port) {
        String key = host + ":" + port;

        synchronized (cachedConnections) {
            CommanderConnection commanderConnection = cachedConnections.get(key);

            if (commanderConnection == null) {
                commanderConnection = new CommanderConnection();
                commanderConnection.setHost(host);
                commanderConnection.setPort(port);
                commanderConnection.setCommanderClient(commanderClient);
                commanderConnection.connect();

                cachedConnections.put(key, commanderConnection);
            }

            return commanderConnection;
        }

    }

    @Override
    public int getConnectionsCount() {
        synchronized (cachedConnections) {
            return cachedConnections.size();
        }
    }

}
