/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.listscomparator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.smalltools.listscomparator.ListsComparator;
import com.foilen.smalltools.listscomparator.ListsComparatorDifference;

/**
 * Tests for {@link ListsComparator}.
 */
public class ListsComparatorTest {

    private void assertComparison(ListsComparatorDifference<String> diffComparison, String object, int side) {
        Assert.assertEquals(object, diffComparison.getObject());
        Assert.assertEquals(side, diffComparison.getSide());
    }

    @Test
    public void testCompareLists() {
        // Empty lists
        List<String> left = Arrays.asList("aaa", "bbb", "ddd", "fff", "ggg", "hhh", "kkk");
        List<String> right = Arrays.asList("aaa", "ccc", "eee", "hhh", "iii", "jjj", "kkk", "lll");

        List<ListsComparatorDifference<String>> diffComparisons = ListsComparator.compareLists(left, right);

        Assert.assertEquals(9, diffComparisons.size());

        assertComparison(diffComparisons.get(0), "bbb", -1);
        assertComparison(diffComparisons.get(1), "ccc", 1);
        assertComparison(diffComparisons.get(2), "ddd", -1);
        assertComparison(diffComparisons.get(3), "eee", 1);
        assertComparison(diffComparisons.get(4), "fff", -1);
        assertComparison(diffComparisons.get(5), "ggg", -1);
        assertComparison(diffComparisons.get(6), "iii", 1);
        assertComparison(diffComparisons.get(7), "jjj", 1);
        assertComparison(diffComparisons.get(8), "lll", 1);
    }

    @Test
    public void testCompareListsEmpty() {
        // Empty lists
        List<String> left = new ArrayList<String>();
        List<String> right = new ArrayList<String>();

        List<ListsComparatorDifference<String>> diffComparisons = ListsComparator.compareLists(left, right);

        Assert.assertEquals(0, diffComparisons.size());
    }

    @Test
    public void testCompareListsOnlyLeft() {
        // Empty lists
        List<String> left = Arrays.asList("aaa", "bbb");
        List<String> right = new ArrayList<String>();

        List<ListsComparatorDifference<String>> diffComparisons = ListsComparator.compareLists(left, right);

        Assert.assertEquals(2, diffComparisons.size());

        assertComparison(diffComparisons.get(0), "aaa", -1);
        assertComparison(diffComparisons.get(1), "bbb", -1);
    }

    @Test
    public void testCompareListsOnlyRight() {
        // Empty lists
        List<String> left = new ArrayList<String>();
        List<String> right = Arrays.asList("aaa", "bbb");

        List<ListsComparatorDifference<String>> diffComparisons = ListsComparator.compareLists(left, right);

        Assert.assertEquals(2, diffComparisons.size());

        assertComparison(diffComparisons.get(0), "aaa", 1);
        assertComparison(diffComparisons.get(1), "bbb", 1);
    }
}
