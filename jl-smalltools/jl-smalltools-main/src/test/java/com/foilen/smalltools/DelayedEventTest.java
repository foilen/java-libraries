/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2025 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools;

import com.foilen.smalltools.tools.ThreadTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

public class DelayedEventTest {

    @Test(timeout = 5000)
    public void testCancel() throws Exception {

        AtomicLong doneTime = new AtomicLong();

        // Start the event
        DelayedEvent delayedEvent = new DelayedEvent(500, () -> {
            doneTime.set(System.currentTimeMillis());
        });
        delayedEvent.cancel();

        // Wait
        ThreadTools.sleep(1000);

        Assert.assertEquals(0, doneTime.get());
    }

    @Test(timeout = 5000)
    public void testExecute() throws Exception {

        AtomicLong doneTime = new AtomicLong();
        Semaphore completed = new Semaphore(0);

        // Start the event
        long startTime = System.currentTimeMillis();
        new DelayedEvent(1000, () -> {
            doneTime.set(System.currentTimeMillis());
            completed.release();
        });

        // Wait
        completed.acquire();

        // Check delay
        long delta = doneTime.get() - startTime;
        long deltaFromExpected = delta - 1000;
        Assert.assertTrue(Math.abs(deltaFromExpected) <= 200);
    }

    @Test(timeout = 20000)
    public void testExecuteMultipleInMixedOrder() throws InterruptedException {

        long startTime = System.currentTimeMillis();
        var doneTimes = new ConcurrentLinkedQueue<Long>();
        var texts = new ConcurrentLinkedQueue<String>();

        // Start the events
        CountDownLatch completed = new CountDownLatch(5);
        new DelayedEvent(4000, () -> {
            doneTimes.add(System.currentTimeMillis());
            texts.add("4");
            completed.countDown();
        });
        new DelayedEvent(5000, () -> {
            doneTimes.add(System.currentTimeMillis());
            texts.add("5");
            completed.countDown();
        });
        new DelayedEvent(2500, () -> {
            doneTimes.add(System.currentTimeMillis());
            texts.add("X");
            completed.countDown();
        }).cancel();
        new DelayedEvent(2000, () -> {
            doneTimes.add(System.currentTimeMillis());
            texts.add("2");
            completed.countDown();
        });
        new DelayedEvent(3000, () -> {
            doneTimes.add(System.currentTimeMillis());
            texts.add("3");
            completed.countDown();
        });
        new DelayedEvent(3000, () -> {
            doneTimes.add(System.currentTimeMillis());
            texts.add("X");
            completed.countDown();
        }).cancel();
        new DelayedEvent(1000, () -> {
            doneTimes.add(System.currentTimeMillis());
            texts.add("1");
            completed.countDown();
        });

        // Wait
        completed.await();

        // Check text in order
        Assert.assertEquals("12345", String.join("", texts));

        // Check delay
        var deltas = doneTimes.stream()
                .sorted()
                .map(doneTime -> doneTime - startTime)
                .toList();
        for (int i = 1000; i <= 5000; i += 1000) {
            var delta = deltas.get(i / 1000 - 1);
            var deltaFromExpected = delta - i;
            Assert.assertTrue("Delta " + delta + " is not close to " + i, Math.abs(deltaFromExpected) <= 200);
        }

    }

}
