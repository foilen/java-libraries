/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.tools;

import com.foilen.smalltools.exception.SmallToolsException;

/**
 * Some common methods for threads.
 */
public final class ThreadTools {

    /**
     * Write the stack trace of the requested thread to STDOUT.
     * 
     * @param thread
     *            the thread to dump
     */
    public static void printStackTrace(Thread thread) {
        StackTraceElement[] stackTraceElements = thread.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            System.out.println(stackTraceElement);
        }
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
