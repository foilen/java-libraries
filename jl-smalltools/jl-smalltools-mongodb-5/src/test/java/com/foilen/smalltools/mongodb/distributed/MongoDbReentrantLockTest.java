/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.mongodb.distributed;

import com.foilen.smalltools.mongodb.AbstractEmbeddedMongoDbTest;
import com.foilen.smalltools.tools.ExecutorsTools;
import com.foilen.smalltools.tools.SecureRandomTools;
import com.foilen.smalltools.tools.ThreadTools;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class MongoDbReentrantLockTest extends AbstractEmbeddedMongoDbTest {

    @Test
    public void testTwoThreads() {

        String collectionName = SecureRandomTools.randomHexString(10);
        MongoCollection<Document> mongoCollection = mongoClient.getDatabase("test").getCollection(collectionName);
        var lock = new MongoDbReentrantLock(mongoClient, mongoCollection);

        Semaphore barrier1 = new Semaphore(0);
        Semaphore barrier2 = new Semaphore(0);
        CyclicBarrier waitBarrier = new CyclicBarrier(3);

        List<String> actual = new ArrayList<>();

        // Thread 1
        ExecutorsTools.getCachedDaemonThreadPool().execute(() -> {
            try {
                // Get the lock
                lock.lock("lock1");
                actual.add("1 Got lock1");

                // Let the thread 2 wait for the lock
                barrier2.release();
                barrier1.acquire();
                ThreadTools.sleep(100);
                actual.add("1 Still has lock1");

                // Release the lock
                lock.unlock("lock1");
                actual.add("1 Released lock1");

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            try {
                waitBarrier.await();
            } catch (Exception e) {
            }
        });

        // Thread 2
        ExecutorsTools.getCachedDaemonThreadPool().execute(() -> {
            try {
                // For the thread 1 to get the lock
                barrier2.acquire();

                // Wait with time
                actual.add("2 Wait lock1 with time");
                boolean gotLock = lock.tryLock("lock1", 1000);
                actual.add("2 Got lock1 with time: " + gotLock);

                // Wait to get the lock while the thread 1 has it
                actual.add("2 Wait lock1");
                barrier1.release();
                lock.lock("lock1");
                actual.add("2 Got lock1");

                // Release the lock
                lock.unlock("lock1");
                actual.add("2 Released lock1");

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            try {
                waitBarrier.await();
            } catch (Exception e) {
            }
        });

        try {
            waitBarrier.await(5, TimeUnit.SECONDS);
        } catch (Exception e) {
        }

        List<String> expected = List.of(
                "1 Got lock1",
                "2 Wait lock1 with time",
                "2 Got lock1 with time: false",
                "2 Wait lock1",
                "1 Still has lock1",
                "1 Released lock1",
                "2 Got lock1",
                "2 Released lock1"
        );
        Assertions.assertEquals(String.join("\n", expected), String.join("\n", actual));

    }

    @Test
    public void testLotOfThreads() throws Exception {

        final int amountOfThreads = 100;

        String collectionName = SecureRandomTools.randomHexString(10);
        MongoCollection<Document> mongoCollection = mongoClient.getDatabase("test").getCollection(collectionName);
        var lock1 = new MongoDbReentrantLock(mongoClient, mongoCollection);
        var lock2 = new MongoDbReentrantLock(mongoClient, mongoCollection);

        CyclicBarrier waitStart = new CyclicBarrier(amountOfThreads * 2);
        CountDownLatch waitStop = new CountDownLatch(amountOfThreads * 2);
        List<Integer> actual1 = new ArrayList<>();
        List<Integer> actual2 = new ArrayList<>();

        for (int i = 0; i < amountOfThreads; ++i) {
            int finalI = i;
            ExecutorsTools.getCachedDaemonThreadPool().execute(() -> {
                try {
                    waitStart.await();

                    var lockToUse = finalI % 2 == 0 ? lock1 : lock2;
                    lockToUse.waitLockAndExecute("actual1", () -> {
                        actual1.add(finalI);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                waitStop.countDown();
            });
        }
        for (int i = 0; i < amountOfThreads; ++i) {
            int finalI = i;
            ExecutorsTools.getCachedDaemonThreadPool().execute(() -> {
                try {
                    waitStart.await();

                    var lockToUse = finalI % 2 == 0 ? lock1 : lock2;
                    lockToUse.waitLockAndExecute("actual2", () -> {
                        actual2.add(finalI);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                waitStop.countDown();
            });
        }

        waitStop.await(30, TimeUnit.SECONDS);

        // Assert
        Collections.sort(actual1);
        Collections.sort(actual2);

        List<Integer> expected = new ArrayList<>();
        for (int i = 0; i < amountOfThreads; ++i) {
            expected.add(i);
        }
        Assertions.assertEquals(expected, actual1);
        Assertions.assertEquals(expected, actual2);

    }

}