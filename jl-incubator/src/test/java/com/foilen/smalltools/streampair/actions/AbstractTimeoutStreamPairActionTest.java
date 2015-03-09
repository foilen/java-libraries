/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.streampair.actions;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.foilen.smalltools.streampair.StreamPair;
import com.foilen.smalltools.streampair.actions.AbstractTimeoutStreamPairAction;

public abstract class AbstractTimeoutStreamPairActionTest {

    private ExecutorService executor = new ThreadPoolExecutor(5, 100, 1L, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());

    protected Future<StreamPair> executeAction(final StreamPair streamPair, final AbstractTimeoutStreamPairAction action) {
        return executor.submit(new Callable<StreamPair>() {

            @Override
            public StreamPair call() throws Exception {
                return action.executeAction(streamPair);
            }
        });
    }

}