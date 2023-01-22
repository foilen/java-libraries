/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.trigger;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.tools.ExecutorsTools;

class SmoothTriggerRunnable implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SmoothTriggerRunnable.class);

    private SmoothTrigger smoothTrigger;

    private SmoothTriggerState state = SmoothTriggerState.IDLE;
    private volatile long requestMade = -1;

    private Queue<Future<?>> futures = new ConcurrentLinkedQueue<>();
    private volatile boolean requestStop = false;

    public SmoothTriggerRunnable(SmoothTrigger smoothTrigger) {
        this.smoothTrigger = smoothTrigger;
    }

    public void cancelPending() {

        logger.debug("Cancelling pending. Current state [{}]", state);

        switch (state) {
        case COOLDOWN:
            break;
        case IDLE:
            break;
        case WARMUP:
            requestMade = 0;
            state = SmoothTriggerState.COOLDOWN;
            break;
        }

        logger.debug("Ending state [{}]", state);
    }

    public void executeActionIfPending(boolean wait) {

        logger.debug("executeActionIfPending. Current state [{}]", state);

        if (state == SmoothTriggerState.WARMUP) {
            state = SmoothTriggerState.COOLDOWN;

            // Submit
            logger.debug("Executing action");
            Future<?> future = ExecutorsTools.getCachedThreadPool().submit(smoothTrigger.getAction());
            futures.add(future);
        }

        logger.debug("executeActionIfPending. Ending state [{}]", state);

        // Wait for all to finish if needed
        if (wait) {
            logger.debug("Waiting for all the actions to finish executing");
            Iterator<Future<?>> it = futures.iterator();
            while (it.hasNext()) {
                Future<?> future = it.next();
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    continue;
                }
                it.remove();
            }
            logger.debug("All the actions are finished to execute");
        }
    }

    public void request() {
        requestMade = System.currentTimeMillis();

    }

    public void requestStop() {
        logger.debug("Requesting stop");
        requestStop = true;
    }

    @Override
    public void run() {

        logger.info("Starting");

        long nextWarmupFinished = -1;
        long nextMaxFinished = -1;
        while (!requestStop) {

            long now = System.currentTimeMillis();
            logger.debug("Current state [{}] ; now [{}]", state, now);

            // Check if should go back to idling
            if (state == SmoothTriggerState.COOLDOWN && nextWarmupFinished <= now) {
                state = SmoothTriggerState.IDLE;
                logger.debug("Change state [{}]", state, now);
            }

            // Check if new request made
            if (requestMade >= 0) {

                logger.debug("A request was made at [{}]", requestMade);

                SmoothTriggerState previousState = state;

                state = SmoothTriggerState.WARMUP;
                nextWarmupFinished = requestMade + smoothTrigger.getDelayAfterLastTriggerMs();
                if (nextWarmupFinished < 0) {
                    nextWarmupFinished = Long.MAX_VALUE;
                }
                if (nextMaxFinished == -1) {
                    nextMaxFinished = requestMade + smoothTrigger.getMaxDelayAfterFirstRequestMs();
                    if (nextMaxFinished < 0) {
                        nextMaxFinished = Long.MAX_VALUE;
                    }
                }
                requestMade = -1;

                if (smoothTrigger.isFirstPassThrough() && previousState == SmoothTriggerState.IDLE) {
                    logger.debug("Execute since pass through and was idling");
                    executeActionIfPending(false);
                    nextWarmupFinished += smoothTrigger.getDelayAfterLastTriggerMs();
                }

            }

            // Check what action to do
            logger.debug("State [{}] ; now [{}] ; nextWarmupFinished [{}] ; nextMaxFinished [{}]", state, now, nextWarmupFinished, nextMaxFinished);
            switch (state) {
            case COOLDOWN:
                break;
            case IDLE:
                break;
            case WARMUP:
                if (nextWarmupFinished <= now || nextMaxFinished <= now) {
                    executeActionIfPending(false);
                    nextWarmupFinished += smoothTrigger.getDelayAfterLastTriggerMs();
                    nextMaxFinished = -1;
                }

                break;

            }

            // Cleanup completed actions until hitting one not completed (will continue the cleanup later
            Future<?> future;
            while ((future = futures.peek()) != null) {
                if (!future.isDone()) {
                    break;
                }
                futures.poll();
            }

            // Go to sleep
            switch (state) {
            case COOLDOWN:
            case IDLE:
                try {
                    Thread.sleep(120000); // 2 mins (will be interrupted if needed)
                } catch (InterruptedException e) {
                }
                break;
            case WARMUP:
                long sooner = Math.min(nextMaxFinished, nextWarmupFinished);
                long delta = sooner - now;
                if (delta > 0) {
                    try {
                        Thread.sleep(delta);
                    } catch (InterruptedException e) {
                    }
                }
                break;

            }

        }

        logger.info("Ended");

    }

}
