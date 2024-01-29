/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import com.foilen.smalltools.systemusage.MemoryUsage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class displays in the logs some information about the application usage of the resources.
 * <p>
 * Usage:
 *
 * <pre>
 * new ApplicationResourceUsageTools() //
 *         .setDelayBetweenOutputInMs(60000) // 1 minute
 *         .setShowJvmMemory(true) //
 *         .setShowSystemMemory(true) //
 *         .setShowThreadCount(true) //
 *         .setShowThreadStackstrace(true) //
 *         .start();
 * </pre>
 */
public class ApplicationResourceUsageTools extends AbstractBasics implements Runnable {

    /**
     * To test.
     *
     * @param args ignored
     */
    public static void main(String[] args) {

        new ApplicationResourceUsageTools() //
                .setDelayBetweenOutputInMs(1000) // 1 second
                .setShowJvmMemory(true) //
                .setShowSystemMemory(true) //
                .setShowThreadCount(true) //
                .setShowThreadStackstrace(true) //
                .start();

        // Use more and more memory
        List<String> keep = new ArrayList<>();
        for (; ; ) {
            keep.add(SecureRandomTools.randomHexString(100));
        }

    }

    private long delayBetweenOutputInMs = 60000; // Every minute

    private boolean showJvmMemory = true;
    private boolean showSystemMemory = true;
    private boolean showThreadCount = true;
    private boolean showThreadStackstrace = true;

    /**
     * Get the delay between each output.
     *
     * @return the delay in ms
     */
    public long getDelayBetweenOutputInMs() {
        return delayBetweenOutputInMs;
    }

    /**
     * Tell if it will show the JVM memory.
     *
     * @return true if showing
     */
    public boolean isShowJvmMemory() {
        return showJvmMemory;
    }

    /**
     * Tell if it will show the system memory.
     *
     * @return true if showing
     */
    public boolean isShowSystemMemory() {
        return showSystemMemory;
    }

    /**
     * Tell if it will show the thread count.
     *
     * @return true if showing
     */
    public boolean isShowThreadCount() {
        return showThreadCount;
    }

    /**
     * Tell if it will show the thread stackstrace.
     *
     * @return true if showing
     */
    public boolean isShowThreadStackstrace() {
        return showThreadStackstrace;
    }

    @Override
    public void run() {

        long lastCheckedTime = 0;

        for (; ; ) {
            try {

                // Wait for the next time to execute
                long nextExecutionTime = lastCheckedTime + delayBetweenOutputInMs;
                long waitTimeInMs = nextExecutionTime - System.currentTimeMillis();
                if (waitTimeInMs > 0) {
                    ThreadTools.sleep(waitTimeInMs);
                }

                // Get the details
                lastCheckedTime = System.currentTimeMillis();

                // JVM Memory
                if (showJvmMemory) {
                    long free = Runtime.getRuntime().freeMemory();
                    long total = Runtime.getRuntime().totalMemory();
                    long max = Runtime.getRuntime().maxMemory();
                    long used = total - free;
                    logger.info("JVM Memory: used: {} ; (free: {} ; total: {}) ; max: {}", used, free, total, max);
                    logger.info("JVM Memory (human): used: {} ; (free: {} ; total: {}) ; max: {}", //
                            SpaceConverterTools.convertToBiggestBUnit(used), //
                            SpaceConverterTools.convertToBiggestBUnit(free), //
                            SpaceConverterTools.convertToBiggestBUnit(total), //
                            SpaceConverterTools.convertToBiggestBUnit(max) //
                    );
                }

                // System Memory
                if (showSystemMemory) {
                    long free = MemoryUsage.getSystemFreeMemory();
                    long used = MemoryUsage.getSystemUsedMemory();
                    long total = MemoryUsage.getSystemTotalMemory();
                    logger.info("System Memory: used: {} ; free: {} ; total: {}", used, free, total);
                    logger.info("System Memory (human): used: {} ; free: {} ; total: {}", //
                            SpaceConverterTools.convertToBiggestBUnit(used), //
                            SpaceConverterTools.convertToBiggestBUnit(free), //
                            SpaceConverterTools.convertToBiggestBUnit(total) //
                    );
                }

                // Thread counts
                Map<Thread, StackTraceElement[]> threads = null;
                if (showThreadCount || showThreadStackstrace) {
                    threads = Thread.getAllStackTraces();
                }

                if (showThreadCount) {
                    logger.info("Threads count: {}", threads.size());
                }
                if (showThreadStackstrace) {
                    long threadId = 1;
                    long threadsTotal = threads.size();
                    for (Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
                        String threadName = entry.getKey().getName();
                        logger.info("Thread {}/{} name: {}", threadId++, threadsTotal, threadName);
                        StackTraceElement[] threadStacktraceElements = entry.getValue();
                        long stackId = 1;
                        long stackTotal = threadStacktraceElements.length;
                        for (StackTraceElement stackTraceElement : threadStacktraceElements) {
                            logger.info("\tStacktrace {}/{}: {}", stackId++, stackTotal, stackTraceElement);
                        }
                    }
                }

            } catch (Exception e) {
                logger.error("Problem outputting the resource usage", e);
            }
        }
    }

    /**
     * Set the delay between each output.
     *
     * @param delayBetweenOutputInMs the delay in ms
     * @return this
     */
    public ApplicationResourceUsageTools setDelayBetweenOutputInMs(long delayBetweenOutputInMs) {
        this.delayBetweenOutputInMs = delayBetweenOutputInMs;
        return this;
    }

    /**
     * Set if it will show the JVM memory.
     *
     * @param showJvmMemory true to show
     * @return this
     */
    public ApplicationResourceUsageTools setShowJvmMemory(boolean showJvmMemory) {
        this.showJvmMemory = showJvmMemory;
        return this;
    }

    /**
     * Set if it will show the system memory.
     *
     * @param showSystemMemory true to show
     * @return this
     */
    public ApplicationResourceUsageTools setShowSystemMemory(boolean showSystemMemory) {
        this.showSystemMemory = showSystemMemory;
        return this;
    }

    /**
     * Set if it will show the thread count.
     *
     * @param showThreadCount true to show
     * @return this
     */
    public ApplicationResourceUsageTools setShowThreadCount(boolean showThreadCount) {
        this.showThreadCount = showThreadCount;
        return this;
    }

    /**
     * Set if it will show the thread stackstrace.
     *
     * @param showThreadStackstrace true to show
     * @return this
     */
    public ApplicationResourceUsageTools setShowThreadStackstrace(boolean showThreadStackstrace) {
        this.showThreadStackstrace = showThreadStackstrace;
        return this;
    }

    /**
     * Start logging at the fixed rate.
     *
     * @return this
     */
    public ApplicationResourceUsageTools start() {
        ExecutorsTools.getCachedDaemonThreadPool().submit(this);
        return this;
    }
}
