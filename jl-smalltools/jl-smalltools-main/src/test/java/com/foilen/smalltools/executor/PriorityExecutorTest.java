/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.executor;

import com.foilen.smalltools.test.asserts.AssertTools;
import com.foilen.smalltools.tools.ThreadTools;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PriorityExecutorTest {

    @Test
    public void testFifoOrder() throws InterruptedException {
        PriorityExecutor priorityExecutor = new PriorityExecutor(1);
        List<String> actual = new LinkedList<>();

        priorityExecutor.execute(() -> ThreadTools.sleep(1000)); // To give time to fill the queue

        priorityExecutor.execute(() -> actual.add("1- execute"));
        priorityExecutor.execute(() -> actual.add("2- execute"));
        priorityExecutor.submit(() -> actual.add("3- submit callable"));
        priorityExecutor.submit((Runnable) () -> actual.add("4- submit runnable"));
        priorityExecutor.submit(() -> actual.add("5- submit runnable with result"), Void.class);

        // Wait completion
        priorityExecutor.shutdown();
        priorityExecutor.awaitTermination(5, TimeUnit.SECONDS);

        List<String> expected = List.of(
                "1- execute",
                "2- execute",
                "3- submit callable",
                "4- submit runnable",
                "5- submit runnable with result"
        );
        AssertTools.assertJsonComparison(expected, actual);
    }


    @Test
    public void testPriorityOrder() throws InterruptedException {
        PriorityExecutor priorityExecutor = new PriorityExecutor(1);
        List<String> actual = new LinkedList<>();

        priorityExecutor.execute(() -> ThreadTools.sleep(1000)); // To give time to fill the queue

        priorityExecutor.submit(3, () -> actual.add("3- submit callable"));
        priorityExecutor.execute(1, () -> actual.add("1- execute"));
        priorityExecutor.submit(5, () -> actual.add("5- submit runnable with result"), Void.class);
        priorityExecutor.submit(4, (Runnable) () -> actual.add("4- submit runnable"));
        priorityExecutor.execute(2, () -> actual.add("2- execute"));

        // Wait completion
        priorityExecutor.shutdown();
        priorityExecutor.awaitTermination(5, TimeUnit.SECONDS);

        List<String> expected = List.of(
                "1- execute",
                "2- execute",
                "3- submit callable",
                "4- submit runnable",
                "5- submit runnable with result"
        );
        AssertTools.assertJsonComparison(expected, actual);
    }

    @Test
    public void testMixedOrder() throws InterruptedException {
        PriorityExecutor priorityExecutor = new PriorityExecutor(1);
        List<String> actual = new LinkedList<>();

        priorityExecutor.execute(() -> ThreadTools.sleep(1000)); // To give time to fill the queue

        priorityExecutor.submit(3, () -> actual.add("3- submit callable"));
        priorityExecutor.submit(() -> actual.add("no 1- submit callable"));
        priorityExecutor.execute(1, () -> actual.add("1- execute"));
        priorityExecutor.execute(Long.MAX_VALUE, () -> actual.add("last- execute"));
        priorityExecutor.submit(5, () -> actual.add("5- submit runnable with result"), Void.class);
        priorityExecutor.submit(4, (Runnable) () -> actual.add("4- submit runnable"));
        priorityExecutor.submit((Runnable) () -> actual.add("no 2- submit runnable"));
        priorityExecutor.execute(2, () -> actual.add("2- execute"));
        priorityExecutor.execute(Long.MIN_VALUE, () -> actual.add("first- execute"));

        // Wait completion
        priorityExecutor.shutdown();
        priorityExecutor.awaitTermination(5, TimeUnit.SECONDS);

        List<String> expected = List.of(
                "first- execute",
                "1- execute",
                "2- execute",
                "3- submit callable",
                "4- submit runnable",
                "5- submit runnable with result",
                "no 1- submit callable",
                "no 2- submit runnable",
                "last- execute"
        );
        AssertTools.assertJsonComparison(expected, actual);
    }
}