/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

/**
 * This is to help searching for an available value by checking small ranges at a time.
 *
 * Features:
 * <ul>
 * <li>Keeps track of the last found value to use it as the next lower range</li>
 * <li>Will loop once if the end is reached</li>
 * </ul>
 */
public final class SearchingAvailabilityLongTools extends SearchingAvailabilityTools<Long> {

    public interface CheckAvailabilityLong extends CheckAvailability<Long> {
        @Override
        default Long increment(Long from, long increment) {
            return from + increment;
        }
    }

    public SearchingAvailabilityLongTools(long min, long max, long range, CheckAvailabilityLong checkAvailability) {
        super(min, max, range, checkAvailability);
    }

}
