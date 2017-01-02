/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.util.Map.Entry;

import com.foilen.smalltools.exception.SmallToolsException;

/**
 * Some common methods for threads.
 */
public final class ThreadTools {

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
     * @param stackTraceElements
     *            the stach
     */
    public static void printStackTrace(StackTraceElement[] stackTraceElements) {
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            System.out.println(stackTraceElement);
        }
    }

    /**
     * Write the stack trace of the requested thread to STDOUT.
     * 
     * @param thread
     *            the thread to dump
     */
    public static void printStackTrace(Thread thread) {
        printStackTrace(thread);
    }

    /**
     * Wait for the specified amount of time.
     * 
     * @param millis
     *            time in milliseconds
     */
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new SmallToolsException("Sleeping interupted", e);
        }
    }

    private ThreadTools() {
    }

}
