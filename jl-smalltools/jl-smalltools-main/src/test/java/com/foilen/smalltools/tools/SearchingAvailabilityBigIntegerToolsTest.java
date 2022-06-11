/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class SearchingAvailabilityBigIntegerToolsTest {

    @Test
    public void test() {

        // Initial values
        Set<BigInteger> used = new HashSet<>();

        // The searching
        SearchingAvailabilityBigIntegerTools searchingAvailability = new SearchingAvailabilityBigIntegerTools(new BigInteger("0"), new BigInteger("9"), 3, (from, to) -> {
            for (BigInteger i = from; i.compareTo(to) <= 0; i = i.add(BigInteger.ONE)) {
                if (used.add(i)) {
                    return Optional.of(i);
                }
            }
            return Optional.empty();
        });

        // Get them all
        for (BigInteger i = BigInteger.ZERO; i.compareTo(new BigInteger("10")) < 0; i = i.add(BigInteger.ONE)) {
            Assert.assertEquals(i, searchingAvailability.getNext().get());
        }
        Assert.assertFalse(searchingAvailability.getNext().isPresent());

        // Release 6th
        used.remove(new BigInteger("6"));
        Assert.assertEquals(new BigInteger("6"), searchingAvailability.getNext().get());

        // Release 1, 5
        used.remove(new BigInteger("1"));
        used.remove(new BigInteger("5"));
        Assert.assertEquals(new BigInteger("1"), searchingAvailability.getNext().get());
        Assert.assertEquals(new BigInteger("5"), searchingAvailability.getNext().get());
        Assert.assertFalse(searchingAvailability.getNext().isPresent());

    }

}
