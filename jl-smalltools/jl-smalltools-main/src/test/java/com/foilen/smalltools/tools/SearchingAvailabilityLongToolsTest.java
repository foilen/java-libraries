package com.foilen.smalltools.tools;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class SearchingAvailabilityLongToolsTest {

    @Test
    public void test() {

        // Initial values
        Set<Long> used = new HashSet<>();

        // The searching
        SearchingAvailabilityLongTools searchingAvailability = new SearchingAvailabilityLongTools(0, 9, 3, (from, to) -> {
            for (long i = from; i <= to; ++i) {
                if (used.add(i)) {
                    return Optional.of(i);
                }
            }
            return Optional.empty();
        });

        // Get them all
        for (long i = 0; i < 10; ++i) {
            Assert.assertEquals(i, (long) searchingAvailability.getNext().get());
        }
        Assert.assertFalse(searchingAvailability.getNext().isPresent());

        // Release 6th
        used.remove(6L);
        Assert.assertEquals(6, (long) searchingAvailability.getNext().get());

        // Release 1, 5
        used.remove(1L);
        used.remove(5L);
        Assert.assertEquals(1, (long) searchingAvailability.getNext().get());
        Assert.assertEquals(5, (long) searchingAvailability.getNext().get());
        Assert.assertFalse(searchingAvailability.getNext().isPresent());

    }

}
