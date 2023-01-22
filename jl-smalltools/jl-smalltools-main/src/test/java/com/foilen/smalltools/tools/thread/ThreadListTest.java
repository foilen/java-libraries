/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools.thread;

import java.util.concurrent.Semaphore;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.smalltools.tools.ThreadTools;

public class ThreadListTest {

    public static class ThreadListTestRunnable implements Runnable {

        private Semaphore semaphore;

        public ThreadListTestRunnable(Semaphore semaphore) {
            this.semaphore = semaphore;
        }

        @Override
        public void run() {
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test(timeout = 20000)
    public void test() throws InterruptedException {

        Semaphore sa = new Semaphore(0);
        Semaphore sb = new Semaphore(0);
        Semaphore sc = new Semaphore(0);

        ThreadList threadList = new ThreadList( //
                new ThreadListTestRunnable(sa), //
                new ThreadListTestRunnable(sb), //
                new ThreadListTestRunnable(sc) //
        );

        Assert.assertFalse(threadList.areAllAlive());
        Assert.assertFalse(threadList.isAnyAlive());
        Assert.assertEquals(0, threadList.countActive());

        // 3 alive
        threadList.start();
        Assert.assertTrue(threadList.areAllAlive());
        Assert.assertTrue(threadList.isAnyAlive());
        Assert.assertEquals(3, threadList.countActive());

        // 1 down and join
        sa.release();
        threadList.join(100);
        ThreadTools.sleep(500);
        Assert.assertFalse(threadList.areAllAlive());
        Assert.assertTrue(threadList.isAnyAlive());
        Assert.assertEquals(2, threadList.countActive());

        // 2 down and join
        sb.release();
        sc.release();
        threadList.join();
        Assert.assertFalse(threadList.areAllAlive());
        Assert.assertFalse(threadList.isAnyAlive());
        Assert.assertEquals(0, threadList.countActive());
    }

}
