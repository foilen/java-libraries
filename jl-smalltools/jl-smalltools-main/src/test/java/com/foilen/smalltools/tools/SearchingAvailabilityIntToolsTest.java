package com.foilen.smalltools.tools;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SearchingAvailabilityIntToolsTest {

    @Test
    public void test() {

        // Initial values
        Set<Integer> used = new HashSet<>();

        // The searching
        SearchingAvailabilityIntTools searchingAvailability = new SearchingAvailabilityIntTools(0, 9, 3, (from, to) -> {
            for (int i = from; i <= to; ++i) {
                if (used.add(i)) {
                    return Optional.of(i);
                }
            }
            return Optional.empty();
        });

        // Get them all
        for (int i = 0; i < 10; ++i) {
            Assertions.assertEquals(i, (int) searchingAvailability.getNext().get());
        }
        Assertions.assertFalse(searchingAvailability.getNext().isPresent());

        // Release 6th
        used.remove(6);
        Assertions.assertEquals(6, (int) searchingAvailability.getNext().get());

        // Release 1, 5
        used.remove(1);
        used.remove(5);
        Assertions.assertEquals(1, (int) searchingAvailability.getNext().get());
        Assertions.assertEquals(5, (int) searchingAvailability.getNext().get());
        Assertions.assertFalse(searchingAvailability.getNext().isPresent());

    }

}
