/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

public class TimeExecutionTools extends AbstractBasics {

    /**
     * Measure the execution time in ms
     *
     * @param runnable
     *            what to execute
     * @return the execution time in ms
     */
    public static long measureInMs(Runnable runnable) {
        long start = System.currentTimeMillis();
        runnable.run();
        return System.currentTimeMillis() - start;

    }

}
