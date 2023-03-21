/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.tools.thread.ThreadList;

import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

/**
 * Some common methods for threads.
 */
public final class ThreadTools {

    /**
     * Create a thread factory that is configured as daemon threads.
     *
     * @return the thread factory
     */
    public static ThreadFactory daemonThreadFactory() {
        return new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                return thread;
            }
        };
    }

    /**
     * Create a thread factory that is configured as daemon threads.
     *
     * @param consumer the consumer to configure more the thread
     * @return the thread factory
     */
    public static ThreadFactory daemonThreadFactory(Consumer<Thread> consumer) {
        return new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                consumer.accept(thread);
                return thread;
            }
        };
    }

    /**
     * Check the current stack and return the name of the highest method in the stack from the desired class.
     *
     * @param onClass the class to check.
     * @return the method's name
     */
    public static Optional<String> getStackMethodName(Class<?> onClass) {

        String onClassName = onClass.getName();

        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (element.getClassName().equals(onClassName)) {
                return Optional.of(element.getMethodName());
            }
        }

        return Optional.empty();
    }

    /**
     * To help change the name of a thread and revert back later.
     *
     * <pre>
     * ThreadNameStateTool threadNameStateTool = ThreadTools.nameThread() //
     *         .clear() //
     *         .appendText(threadName) //
     *         .appendText("-") //
     *         .appendObjectText(executionCount) //
     *         .change();
     *
     * threadNameStateTool.revert();
     * </pre>
     *
     * @return the thread changer
     */
    public static ThreadNameStateTool nameThread() {
        return new ThreadNameStateTool();
    }

    /**
     * Write the stack trace of all threads to STDOUT.
     */
    public static void printAllStackTraces() {
        for (Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
            System.out.println(entry.getKey().getName());
            printStackTrace(entry.getValue());
            System.out.println();
        }
    }

    /**
     * Write the stack trace to STDOUT.
     *
     * @param stackTraceElements the stach
     */
    public static void printStackTrace(StackTraceElement[] stackTraceElements) {
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            System.out.println(stackTraceElement);
        }
    }

    /**
     * Write the stack trace of the requested thread to STDOUT.
     *
     * @param thread the thread to dump
     */
    public static void printStackTrace(Thread thread) {
        printStackTrace(thread.getStackTrace());
    }

    /**
     * Wait for the specified amount of time.
     *
     * @param millis time in milliseconds
     */
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new SmallToolsException("Sleeping interupted", e);
        }
    }

    /**
     * Start multiple threads.
     *
     * @param isDaemon  if the threads should be daemon
     * @param runnables the runnables to start
     * @return the list of threads
     */
    public static ThreadList startMultipleThreads(boolean isDaemon, Runnable... runnables) {
        ThreadList threadList = new ThreadList();
        threadList.addAll(runnables);
        threadList.setDaemon(isDaemon);
        threadList.start();
        return threadList;
    }

    private ThreadTools() {
    }

}
