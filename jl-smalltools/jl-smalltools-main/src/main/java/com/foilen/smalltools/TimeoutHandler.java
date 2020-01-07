/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools;

/**
 * When you need to execute something and wrap it in a timeout, you can simply put that in a {@link Runnable} and give it to this object. Warning: it won't stop the execution of the thread, so your
 * runnable needs to check handle the stop call.
 *
 * @param <T>
 *            the returned type of the call
 */
public class TimeoutHandler<T> {

    /**
     * This is like a runnable, but adds a way to asks for stopping.
     *
     * @param <T>
     *            the returned type of the call
     */
    public static interface TimeoutHandlerRunnable<T> extends Runnable {

        /**
         * Give the result of the successful call.
         *
         * @return the result
         */
        T result();

        /**
         * Do something to stop the call that timedout.
         */
        void stopRequested();

    }

    private long timeoutInMilliseconds;
    private TimeoutHandlerRunnable<T> runnable;

    public TimeoutHandler(long timeoutInMilliseconds, TimeoutHandlerRunnable<T> runnable) {
        this.timeoutInMilliseconds = timeoutInMilliseconds;
        this.runnable = runnable;
    }

    /**
     * Call this method to execute the runnable. This call is waiting for the end of the execution or the timeout to occur.
     *
     * @return the result
     *
     * @throws InterruptedException
     *             if the timeout occurs
     */
    public T call() throws InterruptedException {

        Thread callThread = new Thread(runnable, "TimeoutHandler");
        callThread.start();

        callThread.join(timeoutInMilliseconds);
        if (callThread.isAlive()) {
            runnable.stopRequested();
            callThread.interrupt();
            throw new InterruptedException("The call is still running and the timeout passed");
        }

        return runnable.result();

    }

}
