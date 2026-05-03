package com.foilen.smalltools.executor;

import com.foilen.smalltools.tools.ApplicationResourceUsageTools;
import com.foilen.smalltools.tools.SpaceConverterTools;
import com.foilen.smalltools.tools.ThreadTools;

import java.util.concurrent.TimeUnit;

public class GradualExecutorServiceTestApp {

    public static void main(String[] args) throws InterruptedException {

        new ApplicationResourceUsageTools()
                .setDelayBetweenOutputInMs(10000) // 10 seconds
                .setShowJvmMemory(true)
                .setShowSystemMemory(true)
                .setShowThreadCount(true)
                .setShowThreadStackstrace(false)
                .start();

        var executor = new GradualExecutorService(5, 500, 80, 500);

        // 5 seconds tasks
        for (int i = 1; i <= 50; i++) {
            final int taskId = i;
            executor.execute(() -> {
                task(taskId, 5000, 50_000_000);
            });
        }
        while (executor.queueSize() > 10) {
            ThreadTools.sleep(1000);
        }

        // Steady
        for (int i = 101; i <= 220; i++) { // during 2 minutes
            final int taskId = i;
            ThreadTools.sleep(1000);
            executor.execute(() -> {
                task(taskId, 500, 50_000_000);
            });
            executor.execute(() -> {
                task(taskId, 500, 50_000_000);
            });
        }

        // Max resources
        for (int i = 1001; i <= 2000; i++) {
            final int taskId = i;
            executor.execute(() -> {
                task(taskId, 500, 50_000_000);
            });
        }

        // Steady priority
        for (int i = 3001; i <= 3120; i++) { // during 2 minutes
            final int taskId = i;
            ThreadTools.sleep(1000);
            executor.executeFast(() -> {
                task(taskId, 500, 50_000_000);
            });
            executor.executeFast(() -> {
                task(taskId, 500, 50_000_000);
            });
        }

        // End
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.MINUTES);

    }

    private static void task(int taskId, int timeToSleep, int memorySizeInBytes) {
        System.out.println("Executing task " + taskId + " for " + timeToSleep + "ms " + memorySizeInBytes + " bytes (" + SpaceConverterTools.convertToBiggestBUnit((long) memorySizeInBytes) + ")");
        byte[] memory = new byte[memorySizeInBytes];
        ThreadTools.sleep(timeToSleep);
        System.out.println("Done executing task " + taskId);
    }

}