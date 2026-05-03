package com.foilen.smalltools.executor;

import com.foilen.smalltools.systemusage.MemoryUsage;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.SecureRandomTools;
import com.foilen.smalltools.tools.ThreadTools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is an unbounded executor service that allows a certain number of submissions to be handled right away and then allow more, but with a delay.
 * It auto-adjusts the amount to be handled right away to 90% of the maximum.
 * It can also automatically be blocked when the machine's resources are too much used.
 * The goal is to find what is the necessary amount vs the system resource availability.
 */
public class GradualExecutorService extends AbstractBasics implements ExecutorService {

    private final ExecutorService executorService = createExecutorService();

    private ExecutorService createExecutorService() {
        try {
            // Use virtual threads if available (Java 21+)
            var method = Executors.class.getMethod("newVirtualThreadPerTaskExecutor");
            logger.debug("Using newVirtualThreadPerTaskExecutor");
            return (ExecutorService) method.invoke(null);
        } catch (Exception e) {
            logger.debug("Using newCachedThreadPool");
            return Executors.newCachedThreadPool(ThreadTools.daemonThreadFactory());
        }
    }

    // Settings
    private volatile int noWaitTickets;
    private volatile int extraTicketWaitInMs;
    private final int maxMemoryUsagePercent;
    private final long memoryUsageCheckEveryInMs;

    // State
    private final Queue<Runnable> fastLine = new ConcurrentLinkedDeque<>();
    private final Queue<Runnable> normalLine = new ConcurrentLinkedDeque<>();
    private final AtomicInteger currentTickets = new AtomicInteger();
    private volatile boolean oneToExchange;
    private final Thread dispatcherThread;

    private volatile boolean isBlocked;
    private final Thread memoryUsageThread;
    private volatile long canCallGcAfterTime;

    private volatile boolean isShutdown = false;
    private final CountDownLatch terminationLatch = new CountDownLatch(1);

    public GradualExecutorService(int noWaitTickets, int extraTicketWaitInMs, int maxMemoryUsagePercent, long memoryUsageCheckEveryInMs) {
        this.noWaitTickets = noWaitTickets;
        this.extraTicketWaitInMs = extraTicketWaitInMs;
        this.maxMemoryUsagePercent = maxMemoryUsagePercent;
        this.memoryUsageCheckEveryInMs = memoryUsageCheckEveryInMs;

        dispatcherThread = new Thread(this::dispatch);
        dispatcherThread.setDaemon(true);
        dispatcherThread.setName("GradualExecutorService-dispatcher-" + SecureRandomTools.randomHexString(5));
        dispatcherThread.start();

        memoryUsageThread = new Thread(this::memoryUsage);
        memoryUsageThread.setDaemon(true);
        memoryUsageThread.setName("GradualExecutorService-memoryUsage-" + SecureRandomTools.randomHexString(5));
        memoryUsageThread.start();
    }

    private void dispatch() {

        while (true) {
            try {
                if (fastLine.isEmpty() && normalLine.isEmpty()) {
                    if (isShutdown && currentTickets.get() == 0) {
                        logger.debug("Shutdown requested and all tasks completed, shutting down inner executor");
                        executorService.shutdown();
                        terminationLatch.countDown();
                        return;
                    }
                    ThreadTools.sleepNoException(Long.MAX_VALUE);
                    continue;
                }

                // Check if we need to block
                if (isBlocked) {
                    logger.debug("Blocked, sleeping...");
                    ThreadTools.sleepNoException(Long.MAX_VALUE);
                    continue;
                }

                // Take a ticket and check if it is a no wait or not
                String type = "NO WAIT";
                int ticket = currentTickets.incrementAndGet();
                if (ticket <= noWaitTickets) {
                    Runnable task = fastLine.poll();
                    if (task != null) {
                        logger.debug("{} - Execute from fastline", type);
                        executorService.execute(wrapWithTicketDecrement(task));
                    } else {
                        task = normalLine.poll();
                        if (task != null) {
                            logger.debug("{} - Execute from normal line", type);
                            executorService.execute(wrapWithTicketDecrement(task));
                        }
                    }

                    continue;
                }

                // Wait Extra ticket
                oneToExchange = false;
                long waitUntil = System.currentTimeMillis() + extraTicketWaitInMs;
                type = "EXTRA";
                while (System.currentTimeMillis() < waitUntil) {
                    ThreadTools.sleepNoException(waitUntil - System.currentTimeMillis());
                    if (isBlocked) {
                        logger.debug("Now Blocked...");
                        currentTickets.decrementAndGet();
                        throw new InterruptedException(); // To restart the loop
                    }

                    // If interrupted because another task finished, break (to exchange)
                    if (oneToExchange) {
                        oneToExchange = false;
                        type = "EXCHANGE";
                        break;
                    }

                }

                // Execute
                Runnable task = fastLine.poll();
                if (task != null) {
                    logger.debug("{} - Execute from fastline", type);
                    executorService.execute(wrapWithTicketDecrement(task));
                } else {
                    task = normalLine.poll();
                    if (task != null) {
                        logger.debug("{} - Execute from normal line", type);
                        executorService.execute(wrapWithTicketDecrement(task));
                    }
                }

                // Increment the noWaitTickets if needed
                int nextNoWaitTickets = (int) (ticket * 0.9);
                if (nextNoWaitTickets > noWaitTickets) {
                    logger.debug("Incrementing noWaitTickets from {} to {}", noWaitTickets, nextNoWaitTickets);
                    noWaitTickets = nextNoWaitTickets;
                }

            } catch (Exception ignored) {
            }
        }
    }

    private Runnable wrapWithTicketDecrement(Runnable task) {
        return () -> {
            try {
                task.run();
            } finally {
                currentTickets.decrementAndGet();
                oneToExchange = true;
                dispatcherThread.interrupt();
            }
        };
    }

    private void memoryUsage() {
        int minFreeMemoryPercent = 100 - maxMemoryUsagePercent;
        while (true) {
            try {
                ThreadTools.sleepNoException(memoryUsageCheckEveryInMs);

                Double systemFreePercentDouble = MemoryUsage.getSystemFreeMemoryPercent();
                int systemFreePercent = systemFreePercentDouble == null ? -1 : systemFreePercentDouble.intValue();
                long jvmMaxMemory = MemoryUsage.getJvmMaxMemory();
                int jvmFreePercent = (int) ((jvmMaxMemory - MemoryUsage.getJvmUsedMemory()) * 100 / jvmMaxMemory);

                boolean systemTooLow = systemFreePercentDouble != null && systemFreePercent < minFreeMemoryPercent;
                boolean jvmTooLow = jvmFreePercent < minFreeMemoryPercent;
                boolean shouldBlock = systemTooLow || jvmTooLow;

                if (shouldBlock) {
                    if (!isBlocked) {
                        logger.debug("Memory usage too high (system free: {}%, jvm free: {}%), blocking dispatcher", systemFreePercent, jvmFreePercent);
                        isBlocked = true;

                        // Lower the no wait
                        int nextNoWaitTickets = (int) (currentTickets.get() * 0.7);
                        if (nextNoWaitTickets < noWaitTickets) {
                            logger.debug("Decrementing noWaitTickets from {} to {}", noWaitTickets, nextNoWaitTickets);
                            noWaitTickets = nextNoWaitTickets;
                        }
                    }

                    // If jvmTooLow, call System.gc() every 10 seconds
                    if (jvmTooLow && canCallGcAfterTime < System.currentTimeMillis()) {
                        canCallGcAfterTime = System.currentTimeMillis() + 10_000;
                        logger.debug("Calling System.gc() to try to free memory");
                        System.gc();
                    }
                } else {
                    if (isBlocked) {
                        logger.debug("Memory usage back to normal (system free: {}%, jvm free: {}%), unblocking dispatcher", systemFreePercent, jvmFreePercent);
                        isBlocked = false;
                        dispatcherThread.interrupt();
                    }
                }

            } catch (Exception ignored) {

            }
        }
    }

    public int queueSize() {
        return fastLine.size() + normalLine.size();
    }

    // -- ExecutorService methods

    @Override
    public void shutdown() {
        isShutdown = true;
        dispatcherThread.interrupt();
    }

    @Override
    public List<Runnable> shutdownNow() {
        isShutdown = true;
        dispatcherThread.interrupt();
        List<Runnable> remaining = new ArrayList<>();
        Runnable task;
        while ((task = fastLine.poll()) != null) remaining.add(task);
        while ((task = normalLine.poll()) != null) remaining.add(task);
        executorService.shutdownNow();
        terminationLatch.countDown();
        return remaining;
    }

    @Override
    public boolean isShutdown() {
        return isShutdown;
    }

    @Override
    public boolean isTerminated() {
        return isShutdown && currentTickets.get() == 0 && fastLine.isEmpty() && normalLine.isEmpty() && executorService.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long deadline = System.currentTimeMillis() + unit.toMillis(timeout);
        long remaining = deadline - System.currentTimeMillis();
        if (remaining <= 0) return isTerminated();
        terminationLatch.await(remaining, TimeUnit.MILLISECONDS);
        remaining = deadline - System.currentTimeMillis();
        if (remaining <= 0) return isTerminated();
        return executorService.awaitTermination(remaining, TimeUnit.MILLISECONDS);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        FutureTask<T> future = new FutureTask<>(task);
        execute(future);
        return future;
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        FutureTask<T> future = new FutureTask<>(task, result);
        execute(future);
        return future;
    }

    @Override
    public Future<?> submit(Runnable task) {
        FutureTask<?> future = new FutureTask<>(task, null);
        execute(future);
        return future;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        List<Future<T>> futures = new ArrayList<>();
        for (Callable<T> task : tasks) {
            futures.add(submit(task));
        }
        for (Future<T> future : futures) {
            try {
                future.get();
            } catch (ExecutionException ignored) {
            }
        }
        return futures;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        List<Future<T>> futures = new ArrayList<>();
        for (Callable<T> task : tasks) {
            futures.add(submit(task));
        }
        long deadline = System.nanoTime() + unit.toNanos(timeout);
        for (Future<T> future : futures) {
            long remaining = deadline - System.nanoTime();
            if (remaining <= 0) {
                future.cancel(true);
            } else {
                try {
                    future.get(remaining, TimeUnit.NANOSECONDS);
                } catch (ExecutionException | TimeoutException ignored) {
                    future.cancel(true);
                }
            }
        }
        return futures;
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        try {
            return invokeAny(tasks, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw new ExecutionException(e);
        }
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        List<Future<T>> futures = new ArrayList<>();
        long deadline = System.nanoTime() + unit.toNanos(timeout);
        try {
            for (Callable<T> task : tasks) {
                futures.add(submit(task));
            }
            for (Future<T> future : futures) {
                long remaining = deadline - System.nanoTime();
                if (remaining <= 0) throw new TimeoutException();
                try {
                    return future.get(remaining, TimeUnit.NANOSECONDS);
                } catch (ExecutionException ignored) {
                }
            }
            throw new ExecutionException(new RuntimeException("No task completed successfully"));
        } finally {
            for (Future<T> future : futures) {
                future.cancel(true);
            }
        }
    }

    @Override
    public void close() {
        ExecutorService.super.close();
    }

    @Override
    public void execute(Runnable command) {
        if (isShutdown) {
            throw new RejectedExecutionException("Executor is shut down");
        }
        normalLine.add(command);
        dispatcherThread.interrupt();
    }

    public void executeFast(Runnable command) {
        if (isShutdown) {
            throw new RejectedExecutionException("Executor is shut down");
        }
        fastLine.add(command);
        dispatcherThread.interrupt();
    }

    public Future<?> submitFast(Runnable task) {
        FutureTask<?> future = new FutureTask<>(task, null);
        executeFast(future);
        return future;
    }

    public <T> Future<T> submitFast(Runnable task, T result) {
        FutureTask<T> future = new FutureTask<>(task, result);
        executeFast(future);
        return future;
    }

    public <T> Future<T> submitFast(Callable<T> task) {
        FutureTask<T> future = new FutureTask<>(task);
        executeFast(future);
        return future;
    }

}
