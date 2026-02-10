package com.foilen.smalltools;

import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.foilen.smalltools.tools.ThreadTools;

public class TimeoutRunnableHandlerTest {

    @Test
    @Timeout(10)
    public void testRun_FailedException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            new TimeoutRunnableHandler(5000, () -> {
                throw new RuntimeException("Got an exception");
            }).run();
        });
        assertTrue(exception.getMessage().contains("Got an exception"));
    }

    @Test
    @Timeout(10)
    public void testRun_FailedTimedOut() throws Exception {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            new TimeoutRunnableHandler(500, () -> {
                ThreadTools.sleep(20000);
            }).run();
        });
        assertTrue(exception.getMessage().contains("The call is still running and the timeout passed"));
    }

    @Test
    @Timeout(10)
    public void testRun_Success() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        new TimeoutRunnableHandler(5000, () -> {
            latch.countDown();
        }).run();
        latch.await();
    }

}
