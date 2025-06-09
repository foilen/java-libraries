package com.foilen.smalltools.tools;

/**
 * Tools to measure the execution time.
 */
public class TimeExecutionTools extends AbstractBasics {

    /**
     * Measure the execution time in ms
     *
     * @param runnable what to execute
     * @return the execution time in ms
     */
    public static long measureInMs(Runnable runnable) {
        long start = System.currentTimeMillis();
        runnable.run();
        return System.currentTimeMillis() - start;

    }

}
