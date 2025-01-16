/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2025 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

/**
 * This is to help searching for an available value by checking small ranges at a time.
 * <p>
 * Features:
 * <ul>
 * <li>Keeps track of the last found value to use it as the next lower range</li>
 * <li>Will loop once if the end is reached</li>
 * </ul>
 */
public final class SearchingAvailabilityLongTools extends SearchingAvailabilityTools<Long> {

    /**
     * The interface to check if a value is available.
     */
    public interface CheckAvailabilityLong extends CheckAvailability<Long> {
        @Override
        default Long increment(Long from, long increment) {
            return from + increment;
        }
    }

    /**
     * Create a new instance.
     *
     * @param min               the minimum value to check
     * @param max               the maximum value to check
     * @param range             the range to check at a time
     * @param checkAvailability the interface to check if a value is available
     */
    public SearchingAvailabilityLongTools(long min, long max, long range, CheckAvailabilityLong checkAvailability) {
        super(min, max, range, checkAvailability);
    }

}
