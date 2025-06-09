package com.foilen.smalltools;

import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.DateTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * To run a command after a certain amount of time unless it is cancelled.
 */
public class DelayedEvent extends AbstractBasics {

    private static final Logger logger = LoggerFactory.getLogger(DelayedEvent.class);

    private static final SortedMap<Long, List<DelayedEvent>> delayedEventsByEndTime = new TreeMap<>();
    private static Thread thread = null;
    private static final ReentrantLock reentrantLock = new ReentrantLock();

    private final long endTime;
    private final Runnable event;
    private final AtomicBoolean canceled = new AtomicBoolean(false);

    /**
     * Create the delayed event. It starts counting right now.
     *
     * @param delayInMilliseconds the time to wait before executing
     * @param event               the runnable to execute if not cancelled
     */
    public DelayedEvent(long delayInMilliseconds, Runnable event) {
        this.endTime = System.currentTimeMillis() + delayInMilliseconds;
        this.event = event;

        reentrantLock.lock();
        try {
            logger.info("Adding a delayed event for at {} : {}", DateTools.formatFull(new Date(endTime)), event);
            var previousNextEndTime = Long.MAX_VALUE;
            try {
                previousNextEndTime = delayedEventsByEndTime.firstKey();
            } catch (NoSuchElementException e) {
                // Ignore
            }
            delayedEventsByEndTime.computeIfAbsent(endTime, k -> new ArrayList<>()).add(this);

            startIfNotStarted();
            if (endTime < previousNextEndTime && thread != null) {
                thread.interrupt();
            }
        } finally {
            reentrantLock.unlock();
        }
    }

    /**
     * Request cancellation of the task.
     */
    public void cancel() {
        canceled.set(true);
        reentrantLock.lock();
        try {
            logger.info("Cancelling a delayed event for at {} : {}", DateTools.formatFull(new Date(endTime)), event);
            List<DelayedEvent> delayedEvents = delayedEventsByEndTime.get(endTime);
            if (delayedEvents != null) {
                delayedEvents.remove(this);
                if (delayedEvents.isEmpty()) {
                    delayedEventsByEndTime.remove(endTime);
                }
            }
        } finally {
            reentrantLock.unlock();
        }
    }

    private static void startIfNotStarted() {

        if (thread != null) {
            return;
        }

        thread = new Thread("DelayedEvent") {
            @Override
            public void run() {
                logger.info("Thread started");
                while (!delayedEventsByEndTime.isEmpty()) {

                    // Get the next in line
                    long nextEndTime = delayedEventsByEndTime.firstKey();
                    logger.debug("NextEndTime: {}", DateTools.formatFull(new Date(nextEndTime)));

                    // Wait the delay
                    long delta = nextEndTime - System.currentTimeMillis();
                    long wait = Math.min(60000, delta);
                    if (wait > 0) {
                        try {
                            Thread.sleep(wait);
                        } catch (InterruptedException e) {
                            logger.debug("Interrupted");
                        }
                    }

                    // Execute all not cancelled
                    reentrantLock.lock();
                    try {
                        while (!delayedEventsByEndTime.isEmpty() && delayedEventsByEndTime.firstKey() <= System.currentTimeMillis()) {
                            List<DelayedEvent> delayedEvents = delayedEventsByEndTime.remove(delayedEventsByEndTime.firstKey());
                            for (DelayedEvent delayedEvent : delayedEvents) {
                                if (!delayedEvent.canceled.get()) {
                                    logger.info("Executing {}", delayedEvent);
                                    delayedEvent.event.run();
                                }
                            }
                        }
                    } finally {
                        reentrantLock.unlock();
                    }
                }
                logger.info("DelayedEvent thread stopped");
                thread = null;
            }
        };
        thread.setDaemon(true);
        thread.start();

    }

}
