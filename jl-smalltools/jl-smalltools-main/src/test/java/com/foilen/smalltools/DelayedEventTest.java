/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.smalltools.tools.ThreadTools;

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

}
