package com.foilen.smalltools;

import java.util.concurrent.CountDownLatch;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.foilen.smalltools.tools.ThreadTools;

public class TimeoutRunnableHandlerTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test(timeout = 10000)
    public void testRun_FailedException() {

        thrown.expectMessage("Got an exception");

        new TimeoutRunnableHandler(5000, () -> {
            throw new RuntimeException("Got an exception");
        }).run();
    }

    @Test(timeout = 10000)
    public void testRun_FailedTimedOut() throws Exception {

        thrown.expectMessage("The call is still running and the timeout passed");

        new TimeoutRunnableHandler(500, () -> {
            ThreadTools.sleep(20000);
        }).run();
    }

    @Test(timeout = 10000)
    public void testRun_Success() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        new TimeoutRunnableHandler(5000, () -> {
            latch.countDown();
        }).run();
        latch.await();
    }

}
