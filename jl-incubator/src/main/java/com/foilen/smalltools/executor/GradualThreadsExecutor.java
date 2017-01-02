/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.executor;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import com.foilen.smalltools.exception.SmallToolsException;

/**
 * When new tasks are submitted, it will reuse free threads or start new ones up to the max. It will throw an exception if the max is reached.
 */
public class GradualThreadsExecutor implements Executor {

    private AtomicInteger threadCount = new AtomicInteger();
    private AtomicInteger threadId = new AtomicInteger();
    private Queue<ExpirableTaskThread> freeThreads = new ConcurrentLinkedQueue<>();

    private int maxThreads;
    private long timeoutThreadMs;

    public GradualThreadsExecutor(int maxThreads, long timeoutThreadMs) {
        this.maxThreads = maxThreads;
        this.timeoutThreadMs = timeoutThreadMs;
    }

    @Override
    public void execute(Runnable command) {

        // Get a free thread
        ExpirableTaskThread thread = freeThreads.poll();
        if (thread == null) {
            // Create a new thread if possible
            if (threadCount.incrementAndGet() > maxThreads) {
                threadCount.decrementAndGet();
                throw new SmallToolsException("The maximum amount of threads [" + maxThreads + "] has been reached");
            }

            thread = new ExpirableTaskThread(threadId.getAndIncrement(), timeoutThreadMs, this, command);
            thread.start();
        } else {
            // Request execution
            if (thread.setTask(command)) {
                thread.interrupt();
            } else {
                // Got an expired thread, ask for another
                execute(command);
            }
        }
    }

    protected void nowFree(ExpirableTaskThread expirableTaskThread) {
        freeThreads.add(expirableTaskThread);
    }

    protected void expired(ExpirableTaskThread expirableTaskThread) {
        freeThreads.remove(expirableTaskThread);
        threadCount.decrementAndGet();
    }
}
