/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import com.foilen.smalltools.tools.ThreadTools;

/**
 * To run a command after a certain amount of time unless it is cancelled.
 */
public class DelayedEvent extends Thread {

    private long delayInMilliseconds;
    private Runnable event;
    private AtomicBoolean canceled = new AtomicBoolean(false);

    /**
     * Create the delayed event. It starts counting right now.
     *
     * @param delayInMilliseconds
     *            the time to wait before executing
     * @param event
     *            the runnable to execute if not cancelled
     */
    public DelayedEvent(long delayInMilliseconds, Runnable event) {
        this.delayInMilliseconds = delayInMilliseconds;
        this.event = event;

        start();
    }

    /**
     * Request cancellation of the task.
     */
    public void cancel() {
        canceled.set(true);
    }

    @Override
    public void run() {
        long endTime = new Date().getTime() + delayInMilliseconds;
        // Wait the delay
        long delta = endTime - new Date().getTime();
        while (!canceled.get() && delta > 0) {

            // Wait a bit
            long wait = Math.min(1000, delta);
            ThreadTools.sleep(wait);

            // Recalculate
            delta = endTime - new Date().getTime();
        }

        // Execute if not cancelled
        if (!canceled.get()) {
            event.run();
        }
    }
}
