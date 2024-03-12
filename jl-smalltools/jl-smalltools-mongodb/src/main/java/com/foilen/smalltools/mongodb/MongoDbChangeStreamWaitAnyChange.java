/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.mongodb;

import com.foilen.smalltools.DelayedEvent;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.SecureRandomTools;
import com.mongodb.client.ChangeStreamIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.changestream.FullDocument;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Uses MongoDB Change Streams to wait for any changes instead of polling. The threads can call {@link #waitForChange(long)} and will be woken up when a requested change type happens.
 * <p>
 * If no thread is waiting, the change stream will stop after a specific time.
 */
public class MongoDbChangeStreamWaitAnyChange extends AbstractBasics {

    private final MongoCollection<Document> mongoCollection;
    private final long stopAfterNoThreadWaitedInMs;
    private final List<String> changeTypes;

    private final Semaphore semaphore = new Semaphore(0);

    private long stopAfter;
    private Thread thread;
    private ChangeStreamIterable<Document> changeStream;

    public MongoDbChangeStreamWaitAnyChange(MongoCollection<Document> mongoCollection, long stopAfterNoThreadWaitedInMs, String firstChangeType, String... changeTypes) {
        this.mongoCollection = mongoCollection;
        this.stopAfterNoThreadWaitedInMs = stopAfterNoThreadWaitedInMs;
        this.changeTypes = new ArrayList<>();
        this.changeTypes.add(firstChangeType);
        this.changeTypes.addAll(Arrays.asList(changeTypes));
    }

    public void waitForChange(long timeInMs) throws InterruptedException {
        startIfNeeded();
        stopAfter = System.currentTimeMillis() + stopAfterNoThreadWaitedInMs;
        semaphore.tryAcquire(timeInMs, TimeUnit.MILLISECONDS);
        stopAfter = System.currentTimeMillis() + stopAfterNoThreadWaitedInMs;
    }

    private void startIfNeeded() {
        var poller = this;
        synchronized (poller) {
            if (thread == null) {
                thread = new Thread(() -> {
                    try {
                        new DelayedEvent(2000, () -> {
                            semaphore.drainPermits();
                            semaphore.release((int) mongoCollection.estimatedDocumentCount());
                        });

                        // Start the stream for add and delete without the document details
                        logger.info("Starting change stream");
                        changeStream = mongoCollection.watch(List.of(
                                        Aggregates.match(new Document("operationType", new Document("$in", changeTypes))),
                                        Aggregates.project(new Document("fullDocument", 0))
                                ))
                                .fullDocument(FullDocument.DEFAULT);

                        changeStream.forEach(change -> semaphore.release());

                    } catch (Exception e) {
                        logger.error("Problem with change stream", e);
                    } finally {
                        synchronized (poller) {
                            logger.info("Change stream stopped");
                            thread = null;
                        }

                    }
                }, "Change stream for " + mongoCollection.getNamespace() + "-" + SecureRandomTools.randomHexString(5));
                thread.start();

                checkLaterIfStop();
            }
        }

    }

    private void checkLaterIfStop() {
        long delay = stopAfter - System.currentTimeMillis();
        if (delay <= 0) {
            delay = 100;
        }

        new DelayedEvent(delay, () -> {
            if (System.currentTimeMillis() > stopAfter) {
                if (semaphore.hasQueuedThreads()) {
                    logger.info("Some threads are waiting. Will wait more");
                    stopAfter = System.currentTimeMillis() + stopAfterNoThreadWaitedInMs;
                } else {
                    logger.info("Stopping change stream");
                    changeStream.cursor().close();
                    return;
                }
            }

            checkLaterIfStop();
        });
    }

}
