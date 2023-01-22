/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.math.BigInteger;

/**
 * This is to help searching for an available value by checking small ranges at a time.
 *
 * Features:
 * <ul>
 * <li>Keeps track of the last found value to use it as the next lower range</li>
 * <li>Will loop once if the end is reached</li>
 * </ul>
 */
public final class SearchingAvailabilityBigIntegerTools extends SearchingAvailabilityTools<BigInteger> {

    public interface CheckAvailabilityBigInteger extends CheckAvailability<BigInteger> {
        @Override
        default BigInteger increment(BigInteger from, long increment) {
            return from.add(new BigInteger(String.valueOf(increment)));
        }
    }

    public SearchingAvailabilityBigIntegerTools(BigInteger min, BigInteger max, long range, CheckAvailabilityBigInteger checkAvailability) {
        super(min, max, range, checkAvailability);
    }

}
