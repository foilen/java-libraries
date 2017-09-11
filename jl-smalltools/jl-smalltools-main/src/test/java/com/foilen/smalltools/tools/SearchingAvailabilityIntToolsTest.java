/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

public class SearchingAvailabilityIntToolsTest {

    @Test
    public void test() {

        // Initial values
        boolean[] availables = new boolean[10];
        for (int i = 0; i < 10; ++i) {
            availables[i] = true;
        }

        // The searching
        SearchingAvailabilityIntTools searchingAvailability = new SearchingAvailabilityIntTools(0, 9, 3, (from, to) -> {
            for (int i = from; i <= to; ++i) {
                if (availables[i]) {
                    availables[i] = false;
                    return Optional.of(i);
                }
            }
            return Optional.empty();
        });

        // Get them all
        for (int i = 0; i < 10; ++i) {
            Assert.assertEquals(i, (int) searchingAvailability.getNext().get());
        }
        Assert.assertFalse(searchingAvailability.getNext().isPresent());

        // Release 6th
        availables[6] = true;
        Assert.assertEquals(6, (int) searchingAvailability.getNext().get());

        // Release 1, 5
        availables[1] = true;
        availables[5] = true;
        Assert.assertEquals(1, (int) searchingAvailability.getNext().get());
        Assert.assertEquals(5, (int) searchingAvailability.getNext().get());
        Assert.assertFalse(searchingAvailability.getNext().isPresent());

    }

}
