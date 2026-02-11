package com.foilen.smalltools.tools.thread;

import java.util.concurrent.Semaphore;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

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

    @Test
    @Timeout(20)
    public void test() throws InterruptedException {

        Semaphore sa = new Semaphore(0);
        Semaphore sb = new Semaphore(0);
        Semaphore sc = new Semaphore(0);

        ThreadList threadList = new ThreadList(
                new ThreadListTestRunnable(sa),
                new ThreadListTestRunnable(sb),
                new ThreadListTestRunnable(sc)
        );

        Assertions.assertFalse(threadList.areAllAlive());
        Assertions.assertFalse(threadList.isAnyAlive());
        Assertions.assertEquals(0, threadList.countActive());

        // 3 alive
        threadList.start();
        Assertions.assertTrue(threadList.areAllAlive());
        Assertions.assertTrue(threadList.isAnyAlive());
        Assertions.assertEquals(3, threadList.countActive());

        // 1 down and join
        sa.release();
        threadList.join(100);
        ThreadTools.sleep(500);
        Assertions.assertFalse(threadList.areAllAlive());
        Assertions.assertTrue(threadList.isAnyAlive());
        Assertions.assertEquals(2, threadList.countActive());

        // 2 down and join
        sb.release();
        sc.release();
        threadList.join();
        Assertions.assertFalse(threadList.areAllAlive());
        Assertions.assertFalse(threadList.isAnyAlive());
        Assertions.assertEquals(0, threadList.countActive());
    }

}
