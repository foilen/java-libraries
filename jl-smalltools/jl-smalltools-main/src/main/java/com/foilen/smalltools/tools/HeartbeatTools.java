/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * To be able to execute some commands periodically while a main action is running.
 */
public class HeartbeatTools extends AbstractBasics {

    /**
     * Execute the main action and execute in another thread the ping action continuously.
     *
     * @param timeBetweenPingsInMs
     *            the time before the first ping and between them
     * @param pingAction
     *            the action to execute frequently while the mainAction is running
     * @param mainAction
     *            the main action to execute
     */
    public static void execute(long timeBetweenPingsInMs, Runnable pingAction, Runnable mainAction) {

        // Start the pinging thread
        AtomicBoolean keepPinging = new AtomicBoolean(true);
        Thread thread = new Thread(() -> {

            while (keepPinging.get()) {
                ThreadTools.sleep(timeBetweenPingsInMs);
                if (keepPinging.get()) {
                    pingAction.run();
                }
            }

        }, HeartbeatTools.class.getSimpleName());
        thread.setDaemon(true);
        thread.start();

        // Execute the main
        try {
            mainAction.run();
        } finally {
            // Stop
            keepPinging.set(false);
        }

    }

}
