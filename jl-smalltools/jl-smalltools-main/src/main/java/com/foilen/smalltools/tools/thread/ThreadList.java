package com.foilen.smalltools.tools.thread;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Helper to manage multiple threads as a group.
 */
public class ThreadList {

    private List<Thread> threads = new ArrayList<>();

    /**
     * Create a new instance.
     */
    public ThreadList() {
    }

    /**
     * Create a new instance.
     *
     * @param threads the threads to manage
     */
    public ThreadList(List<Thread> threads) {
        this.threads = threads;
    }

    /**
     * Create a new instance.
     *
     * @param runnables the runnables to manage
     */
    public ThreadList(Runnable... runnables) {
        addAll(runnables);
    }

    /**
     * Create a new instance.
     *
     * @param threads the threads to manage
     */
    public ThreadList(Thread... threads) {
        addAll(threads);
    }

    /**
     * Add a new thread.
     *
     * @param runnable the runnable to execute
     */
    public void add(Runnable runnable) {
        threads.add(new Thread(runnable));
    }

    /**
     * Add a new thread.
     *
     * @param thread the thread to manage
     */
    public void add(Thread thread) {
        threads.add(thread);
    }

    /**
     * Add all the threads.
     *
     * @param runnables the runnables to execute
     */
    public void addAll(Collection<Runnable> runnables) {
        for (Runnable runnable : runnables) {
            add(runnable);
        }
    }

    /**
     * Add all the threads.
     *
     * @param runnables the runnables to execute
     */
    public void addAll(Runnable... runnables) {
        for (Runnable runnable : runnables) {
            add(runnable);
        }
    }

    /**
     * Add all the threads.
     *
     * @param threads the threads to manage
     */
    public void addAll(Thread... threads) {
        for (Thread thread : threads) {
            add(thread);
        }
    }

    /**
     * Tells if all the threads are alive.
     *
     * @return true only if all the threads are alive
     */
    public boolean areAllAlive() {
        for (Thread thread : threads) {
            if (!thread.isAlive()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tells how many threads are active.
     *
     * @return the amount of active threads
     */
    public int countActive() {
        int count = 0;
        for (Thread thread : threads) {
            if (thread.isAlive()) {
                ++count;
            }
        }
        return count;
    }

    /**
     * Get the threads.
     *
     * @return the threads
     */
    public List<Thread> getThreads() {
        return threads;
    }

    /**
     * Interrupt all the threads.
     */
    public void interrupt() {
        threads.forEach(it -> {
            it.interrupt();
        });

    }

    /**
     * Tells if any of the threads is alive.
     *
     * @return true if any thread is alive
     */
    public boolean isAnyAlive() {
        for (Thread thread : threads) {
            if (thread.isAlive()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Join all threads.
     *
     * @throws InterruptedException if interrupted
     */
    public void join() throws InterruptedException {
        for (Thread thread : threads) {
            thread.join();
        }
    }

    /**
     * Join all threads.
     *
     * @param millis the time to wait in milliseconds for all of them to be completed
     * @throws InterruptedException if interrupted
     */
    public void join(long millis) throws InterruptedException {
        long timeoutAt = System.currentTimeMillis() + millis;
        for (Thread thread : threads) {
            long now = System.currentTimeMillis();
            if (now >= timeoutAt) {
                return;
            }
            thread.join(timeoutAt - now);
        }
    }

    /**
     * Set all threads as daemon or not.
     *
     * @param isDaemon true if must be daemon
     */
    public void setDaemon(boolean isDaemon) {
        threads.forEach(it -> {
            it.setDaemon(isDaemon);
        });
    }

    /**
     * Set the threads to manage.
     *
     * @param threads the threads to manage
     */
    public void setThreads(List<Thread> threads) {
        this.threads = threads;
    }

    /**
     * Start all the threads.
     */
    public void start() {
        threads.forEach(it -> {
            it.start();
        });
    }

}
