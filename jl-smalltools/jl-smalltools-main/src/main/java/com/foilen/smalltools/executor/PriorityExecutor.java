/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2025 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.executor;

import com.foilen.smalltools.tools.AbstractBasics;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A priority executor that executes tasks based on their priority.
 * <ul>
 *     <li>Smaller number is higher priority
 *     <ul>
 *         <li>Long.MIN_VALUE is the highest priority</li>
 *         <li>Long.MAX_VALUE is the lowest priority</li>
 *         <li>Example:
 *         <ul>
 *             <li>If putting the current time as the priority, the tasks will be executed in time order</li>
 *         </ul>
 *     </ul>
 *     </li>
 * </ul>
 */
public class PriorityExecutor extends AbstractBasics implements ExecutorService {

    private final ThreadPoolExecutor executor;
    private final PriorityRunnableBlockingQueue queue;

    private final AtomicLong nextNoPriority = new AtomicLong(System.currentTimeMillis());

    public PriorityExecutor(int poolSize) {
        queue = new PriorityRunnableBlockingQueue();
        this.executor = new ThreadPoolExecutor(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS, queue);
    }

    public void execute(long priority, Runnable task) {
        queue.setPriorityOnThread(priority);
        executor.execute(task);
    }

    public <T> Future<T> submit(long priority, Callable<T> task) {
        queue.setPriorityOnThread(priority);
        return executor.submit(task);
    }

    public <T> Future<T> submit(long priority, Runnable task, T result) {
        queue.setPriorityOnThread(priority);
        return executor.submit(task, result);
    }

    /**
     * Submit a task with a priority.
     *
     * @param priority The priority. Smaller number is higher priority. Long.MIN_VALUE is the highest priority. Long.MAX_VALUE is the lowest priority.
     * @param task     The task to execute
     */
    public Future<?> submit(long priority, Runnable task) {
        queue.setPriorityOnThread(priority);
        return executor.submit(task);
    }

    // ---== Wrappers ==---

    @Override
    public void shutdown() {
        executor.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return executor.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return executor.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return executor.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return executor.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        queue.setPriorityOnThread(nextNoPriority.getAndIncrement());
        return executor.submit(task);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        queue.setPriorityOnThread(nextNoPriority.getAndIncrement());
        return executor.submit(task, result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        queue.setPriorityOnThread(nextNoPriority.getAndIncrement());
        return executor.submit(task);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        queue.setPriorityOnThread(nextNoPriority.getAndIncrement());
        return executor.invokeAll(tasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        queue.setPriorityOnThread(nextNoPriority.getAndIncrement());
        return executor.invokeAll(tasks, timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        queue.setPriorityOnThread(nextNoPriority.getAndIncrement());
        return executor.invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        queue.setPriorityOnThread(nextNoPriority.getAndIncrement());
        return executor.invokeAny(tasks, timeout, unit);
    }

    @Override
    public void execute(Runnable task) {
        queue.setPriorityOnThread(nextNoPriority.getAndIncrement());
        executor.execute(task);
    }

}
