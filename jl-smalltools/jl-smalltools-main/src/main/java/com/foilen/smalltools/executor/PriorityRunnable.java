/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.executor;

public class PriorityRunnable implements Runnable, Comparable<PriorityRunnable> {

    private final long priority;
    private final Runnable runnable;

    public PriorityRunnable(long priority, Runnable task) {
        this.priority = priority;
        this.runnable = task;
    }

    @Override
    public void run() {
        runnable.run();
    }

    @Override
    public int compareTo(PriorityRunnable o) {
        return Long.compare(this.priority, o.priority);
    }

    public Runnable getRunnable() {
        return runnable;
    }

}
