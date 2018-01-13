/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.util.Optional;

/**
 * This is to help searching for an available value by checking small ranges at a time.
 *
 * Features:
 * <ul>
 * <li>Keeps track of the last found value to use it as the next lower range</li>
 * <li>Will loop once if the end is reached</li>
 * </ul>
 */
public final class SearchingAvailabilityIntTools extends AbstractBasics {

    public interface CheckAvailabilityInt {
        /**
         * Give the next available value in the specified range.
         *
         * @param from
         *            the lower range (inclusive)
         * @param to
         *            the upper range (inclusive)
         * @return the next value if any
         */
        Optional<Integer> nextAvailable(int from, int to);
    }

    // Properties
    private int min;
    private int max;
    private int range;
    private CheckAvailabilityInt checkAvailability;

    // Internal
    private int cursor;

    public SearchingAvailabilityIntTools(int min, int max, int range, CheckAvailabilityInt checkAvailability) {
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
    public synchronized Optional<Integer> getNext() {
        int initialCursor = cursor;
        boolean didLoop = false;

        while (!didLoop || cursor < initialCursor) {

            // Search
            Optional<Integer> next = checkAvailability.nextAvailable(cursor, Math.min(cursor + range, max));

            // Found one
            if (next.isPresent()) {
                cursor = next.get() + 1;
                if (cursor > max) {
                    cursor = min;
                }
                return next;
            }

            // Move on
            cursor += range + 1;
            if (cursor > max) {
                cursor = min;
                didLoop = true;
            }

        }

        return Optional.empty();

    }

}
