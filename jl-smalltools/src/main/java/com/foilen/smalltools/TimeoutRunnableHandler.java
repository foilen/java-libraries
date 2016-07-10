/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools;

import com.foilen.smalltools.exception.SmallToolsException;

/**
 * When you need to execute something and wrap it in a timeout, you can simply put that in a {@link Runnable} and give it to this object. It will send an interrupt to the Thread. It will retrow any
 * exception that occurs.
 */
public class TimeoutRunnableHandler {

    private long timeoutInMilliseconds;
    private Runnable runnable;

    private RuntimeException thrownException;

    public TimeoutRunnableHandler(long timeoutInMilliseconds, Runnable runnable) {
        this.timeoutInMilliseconds = timeoutInMilliseconds;
        this.runnable = runnable;
    }

    /**
     * Call this method to execute the runnable. This call is waiting for the end of the execution or the timeout to occur.
     * 
     * @throws SmallToolsException
     *             if the timeout occurs
     */
    public void run() {

        // Start the thread
        Thread callThread = new Thread(() -> {
            try {
                runnable.run();
            } catch (RuntimeException e) {
                thrownException = e;
            }
        } , "TimeoutHandler");
        callThread.start();

        // Wait for the thread to finish or timeout
        try {
            callThread.join(timeoutInMilliseconds);
        } catch (InterruptedException e) {
        }
        if (callThread.isAlive()) {
            callThread.interrupt();
            throw new SmallToolsException("The call is still running and the timeout passed");
        }

        // Throw any exception that was thrown
        if (thrownException != null) {
            throw thrownException;
        }

    }

}
