/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2025 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

/**
 * Tools to help with retrying.
 */
public class RetryTools {

    /**
     * This method is used to retry a task in case of an exception.
     * It will keep retrying the task until it is successful.
     * The retry delay is a random value between minRetryMs and maxRetryMs.
     *
     * @param minRetryMs The minimum delay (in milliseconds) before retrying the task after an exception.
     * @param maxRetryMs The maximum delay (in milliseconds) before retrying the task after an exception.
     * @param runnable   The task that needs to be executed and retried in case of an exception.
     */
    public static void retryBetween(int minRetryMs, int maxRetryMs, Runnable runnable) {
        while (true) {
            try {
                runnable.run();
                return;
            } catch (Exception e) {
                int sleepMs = minRetryMs + (int) (Math.random() * (maxRetryMs - minRetryMs));
                ThreadTools.sleep(sleepMs);
            }
        }
    }
}
