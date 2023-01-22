/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Some shared executors.
 */
public final class ExecutorsTools {

    private static ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
    private static ExecutorService cachedDaemonThreadPool = Executors.newCachedThreadPool(ThreadTools.daemonThreadFactory());

    /**
     * Get a shared executor that executes everything right now and keep the idling Threads around for 1 minute. The threads are set as being daemon threads.
     *
     * @return the {@link ExecutorService}
     */
    public static ExecutorService getCachedDaemonThreadPool() {
        return cachedDaemonThreadPool;
    }

    /**
     * Get a shared executor that executes everything right now and keep the idling Threads around for 1 minute.
     *
     * @return the {@link ExecutorService}
     */
    public static ExecutorService getCachedThreadPool() {
        return cachedThreadPool;
    }

    private ExecutorsTools() {
    }
}
