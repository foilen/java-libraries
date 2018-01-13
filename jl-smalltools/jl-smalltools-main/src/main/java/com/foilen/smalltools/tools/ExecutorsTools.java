/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2018 Foilen (http://foilen.com)

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
