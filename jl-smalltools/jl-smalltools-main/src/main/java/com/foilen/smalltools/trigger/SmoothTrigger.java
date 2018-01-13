/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.trigger;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.tools.AssertTools;

/**
 * The goal of a {@link SmoothTrigger} is to easily manipulate the frequency of running a requested action when it can be requested multiple times.
 *
 * Options are:
 * <ul>
 * <li>cancellable: when cancelled, it goes in idle state.</li>
 * <li>delayAfterLastTriggerMs: when a request comes in, it goes in warmup for that duration. If another one comes in, it will reset the warmup timer and wait for that duration. When that time is
 * passed without a new request, the action is triggered and it goes back to idling.</li>
 * <li>maxDelayAfterFirstRequestMs: when a request comes in, it will wait max for this delay before triggering. It will bypass the delayAfterLastTriggerMs to make sure that it won't wait forever.</li>
 * <li>isFirstPassThrough: when a request comes in, if nothing is pending, the action is triggered and it goes in cooldown state for delayAfterLastTriggerMs time.</li>
 * </ul>
 *
 * The states are:
 * <ul>
 * <li>Idle: No request is done</li>
 * <li>Cooldown: When isFirstPassThrough is used and an event is requested, this is the state that waits for delayAfterLastTriggerMs. If a request comes in, it goes in warmup ; else, it goes back to
 * idling.</li>
 * <li>Warmup: When a request is made, it is in pending state. After the action is triggered, it goes in cooldown. The max amount of time in this state is managed by maxDelayAfterFirstRequest.</li>
 * </ul>
 *
 */
public class SmoothTrigger {

    // Properties
    private long delayAfterLastTriggerMs;
    private long maxDelayAfterFirstRequestMs;
    private boolean isFirstPassThrough;
    private Runnable action;

    // Internal
    private Object internalUpdateLock = new Object();
    private SmoothTriggerRunnable smoothTriggerRunnable;
    private Thread smoothTriggerThread;

    /**
     * Create a {@link SmoothTrigger}.
     *
     * @param delayAfterLastTriggerMs
     *            how long to wait between a request and the action executions when multiple requests are done quickly.
     * @param maxDelayAfterFirstRequestMs
     *            the max amount of time to wait before executing the action when the warmup is always reseting (due to too many quick requests) . To disable, set it to {@link Long#MAX_VALUE}.
     * @param isFirstPassThrough
     *            true to trigger the event right away and go in cooldown state
     * @param action
     *            the action to execute
     */
    public SmoothTrigger(long delayAfterLastTriggerMs, long maxDelayAfterFirstRequestMs, boolean isFirstPassThrough, Runnable action) {
        this.delayAfterLastTriggerMs = delayAfterLastTriggerMs;
        this.maxDelayAfterFirstRequestMs = maxDelayAfterFirstRequestMs;
        this.isFirstPassThrough = isFirstPassThrough;
        this.action = action;
    }

    /**
     * Create a {@link SmoothTrigger} with a warmup time of 1s and a max wait of 10s.
     *
     * @param action
     *            the action to execute
     */
    public SmoothTrigger(Runnable action) {
        this.delayAfterLastTriggerMs = 1000;
        this.maxDelayAfterFirstRequestMs = 10000;
        this.isFirstPassThrough = false;
        this.action = action;
    }

    public void cancelPending() {
        AssertTools.assertNotNull(smoothTriggerRunnable, "Not running");
        smoothTriggerRunnable.cancelPending();
        smoothTriggerThread.interrupt();
    }

    public Runnable getAction() {
        return action;
    }

    public long getDelayAfterLastTriggerMs() {
        return delayAfterLastTriggerMs;
    }

    public long getMaxDelayAfterFirstRequestMs() {
        return maxDelayAfterFirstRequestMs;
    }

    public boolean isFirstPassThrough() {
        return isFirstPassThrough;
    }

    /**
     * Request the execution of the action. Depending on the state and the parameters, it might execute now or in the future. It returns right away since the execution of the action is done in a
     * separate thread.
     */
    public void request() {
        AssertTools.assertNotNull(smoothTriggerRunnable, "Not running");
        smoothTriggerRunnable.request();
        smoothTriggerThread.interrupt();
    }

    public SmoothTrigger setAction(Runnable action) {
        AssertTools.assertNull(smoothTriggerRunnable, "Cannot change while running");
        this.action = action;
        return this;
    }

    public SmoothTrigger setDelayAfterLastTriggerMs(long delayAfterLastTriggerMs) {
        AssertTools.assertNull(smoothTriggerRunnable, "Cannot change while running");
        this.delayAfterLastTriggerMs = delayAfterLastTriggerMs;
        return this;
    }

    public SmoothTrigger setFirstPassThrough(boolean isFirstPassThrough) {
        AssertTools.assertNull(smoothTriggerRunnable, "Cannot change while running");
        this.isFirstPassThrough = isFirstPassThrough;
        return this;
    }

    public SmoothTrigger setMaxDelayAfterFirstRequestMs(long maxDelayAfterFirstRequestMs) {
        AssertTools.assertNull(smoothTriggerRunnable, "Cannot change while running");
        this.maxDelayAfterFirstRequestMs = maxDelayAfterFirstRequestMs;
        return this;
    }

    public SmoothTrigger start() {

        // Start thread
        synchronized (internalUpdateLock) {
            AssertTools.assertNotNull(action, "No action given");
            AssertTools.assertTrue(delayAfterLastTriggerMs >= 0, "delayAfterLastTriggerMs must be 0 or bigger");
            AssertTools.assertTrue(maxDelayAfterFirstRequestMs >= delayAfterLastTriggerMs, "maxDelayAfterFirstRequestMs must be greater or equals to delayAfterLastTriggerMs");

            AssertTools.assertNull(smoothTriggerRunnable, "Already started");
            smoothTriggerRunnable = new SmoothTriggerRunnable(this);
            smoothTriggerThread = new Thread(smoothTriggerRunnable, "SmoothTrigger");
            smoothTriggerThread.setDaemon(true);
            smoothTriggerThread.start();
        }
        return this;

    }

    /**
     * Stop the smooth trigger system. It returns when stopped and the action execution completed (if needed).
     *
     * @param executeActionIfPending
     *            if true and in the warmup state, will trigger the action
     */
    public void stop(boolean executeActionIfPending) {

        synchronized (internalUpdateLock) {
            AssertTools.assertNotNull(smoothTriggerRunnable, "Not running");

            smoothTriggerRunnable.requestStop();
            smoothTriggerThread.interrupt();
            try {
                smoothTriggerThread.join();
            } catch (InterruptedException e) {
                throw new SmallToolsException("Interrupted while waiting for the Thread to stop", e);
            }

            if (executeActionIfPending) {
                smoothTriggerRunnable.executeActionIfPending(true);
            }

            smoothTriggerRunnable = null;
            smoothTriggerThread = null;
        }

    }

}
