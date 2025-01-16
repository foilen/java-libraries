/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2025 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.util.Optional;

/**
 * This is to help searching for an available value by checking small ranges at a time.
 * <p>
 * Features:
 * <ul>
 * <li>Keeps track of the last found value to use it as the next lower range</li>
 * <li>Will loop once if the end is reached</li>
 * </ul>
 *
 * @param <T> The type of value to search for
 */
public class SearchingAvailabilityTools<T extends Comparable<T>> extends AbstractBasics {

    /**
     * Implement this to check if a value is available.
     *
     * @param <T> the type of value
     */
    public interface CheckAvailability<T> {
        /**
         * Calculate the next value.
         *
         * @param from      the value to increment
         * @param increment the amount to increment
         * @return the incremented value
         */
        T increment(T from, long increment);

        /**
         * Give the next available value in the specified range.
         *
         * @param from the lower range (inclusive)
         * @param to   the upper range (inclusive)
         * @return the next value if any
         */
        Optional<T> nextAvailable(T from, T to);
    }

    // Properties
    private T min;
    private T max;
    private long range;
    private CheckAvailability<T> checkAvailability;

    // Internal
    private T cursor;

    /**
     * Create a new instance.
     *
     * @param min               the minimum value to check
     * @param max               the maximum value to check
     * @param range             the range to check at a time
     * @param checkAvailability the interface to check if a value is available
     */
    public SearchingAvailabilityTools(T min, T max, long range, CheckAvailability<T> checkAvailability) {
        this.min = min;
        this.max = max;
        this.range = range;
        this.checkAvailability = checkAvailability;

        this.cursor = min;
    }

    /**
     * Get the next available value if any.
     *
     * @return the next value
     */
    public synchronized Optional<T> getNext() {

        logger.debug("Search: min={}, cursor={}, max={}, range={}", min, cursor, max, range);

        T initialCursor = cursor;
        boolean didLoop = false;

        while (!didLoop || cursor.compareTo(initialCursor) < 0) {

            // Search
            T to = checkAvailability.increment(cursor, range);
            T stepTo = min(to, max);
            logger.debug("Next Available: cursor={}, stepTo={}", cursor, stepTo);
            Optional<T> next = checkAvailability.nextAvailable(cursor, stepTo);
            logger.debug("Found: {}", next);

            // Found one
            if (next.isPresent()) {
                cursor = checkAvailability.increment(next.get(), 1);
                if (cursor.compareTo(max) > 0) {
                    cursor = min;
                }
                return next;
            }

            // Move on
            cursor = checkAvailability.increment(stepTo, 1);
            if (cursor.compareTo(max) > 0) {
                cursor = min;
                didLoop = true;
            }

        }

        return Optional.empty();

    }

    private T min(T a, T b) {
        if (a.compareTo(b) < 0) {
            return a;
        }
        return b;
    }

}
