/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.connections.actions;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.foilen.smalltools.net.connections.Connection;
import com.foilen.smalltools.net.connections.actions.AbstractTimeoutConnectionAction;

public abstract class AbstractTimeoutConnectionActionTest {

    private ExecutorService executor = new ThreadPoolExecutor(5, 100, 1L, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());

    protected Future<Connection> executeAction(final Connection connection, final AbstractTimeoutConnectionAction action) {
        return executor.submit(new Callable<Connection>() {

            @Override
            public Connection call() throws Exception {
                return action.executeAction(connection);
            }
        });
    }

}
