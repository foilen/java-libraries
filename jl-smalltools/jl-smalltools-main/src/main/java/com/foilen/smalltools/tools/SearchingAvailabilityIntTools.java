/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2021 Foilen (https://foilen.com)

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
public final class SearchingAvailabilityIntTools extends SearchingAvailabilityTools<Integer> {

    public interface CheckAvailabilityInt extends CheckAvailability<Integer> {
        @Override
        default Integer increment(Integer from, long increment) {
            return (int) (from + increment);
        }
    }

    public SearchingAvailabilityIntTools(int min, int max, long range, CheckAvailabilityInt checkAvailability) {
        super(min, max, range, checkAvailability);
    }

}
