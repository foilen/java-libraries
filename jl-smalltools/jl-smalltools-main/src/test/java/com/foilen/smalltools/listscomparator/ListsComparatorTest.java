/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.listscomparator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

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
        // Lists
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

        // Lists
        List<String> left = Arrays.asList("aaa", "bbb");
        List<String> right = new ArrayList<String>();

        List<ListsComparatorDifference<String>> diffComparisons = ListsComparator.compareLists(left, right);

        Assert.assertEquals(2, diffComparisons.size());

        assertComparison(diffComparisons.get(0), "aaa", -1);
        assertComparison(diffComparisons.get(1), "bbb", -1);
    }

    @Test
    public void testCompareListsOnlyRight() {

        // Lists
        List<String> left = new ArrayList<String>();
        List<String> right = Arrays.asList("aaa", "bbb");

        List<ListsComparatorDifference<String>> diffComparisons = ListsComparator.compareLists(left, right);

        Assert.assertEquals(2, diffComparisons.size());

        assertComparison(diffComparisons.get(0), "aaa", 1);
        assertComparison(diffComparisons.get(1), "bbb", 1);
    }

    @Test
    public void testCompareStreams() {

        // Lists
        List<String> left = Arrays.asList("aaa", "bbb", "ddd", "fff", "ggg", "hhh", "kkk");
        List<String> right = Arrays.asList("aaa", "ccc", "eee", "hhh", "iii", "jjj", "kkk", "lll");

        List<ListsComparatorDifference<String>> diffComparisons = ListsComparator.compareStreams(left.stream(), right.stream()).collect(Collectors.toList());

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
    public void testCompareStreamsEmpty() {

        // Empty lists
        List<String> left = new ArrayList<String>();
        List<String> right = new ArrayList<String>();

        List<ListsComparatorDifference<String>> diffComparisons = ListsComparator.compareStreams(left.stream(), right.stream()).collect(Collectors.toList());

        Assert.assertEquals(0, diffComparisons.size());
    }

    @Test
    public void testCompareStreamsOnlyLeft() {

        // Lists
        List<String> left = Arrays.asList("aaa", "bbb");
        List<String> right = new ArrayList<String>();

        List<ListsComparatorDifference<String>> diffComparisons = ListsComparator.compareStreams(left.stream(), right.stream()).collect(Collectors.toList());

        Assert.assertEquals(2, diffComparisons.size());

        assertComparison(diffComparisons.get(0), "aaa", -1);
        assertComparison(diffComparisons.get(1), "bbb", -1);
    }

    @Test
    public void testCompareStreamsOnlyRight() {

        // Lists
        List<String> left = new ArrayList<String>();
        List<String> right = Arrays.asList("aaa", "bbb");

        List<ListsComparatorDifference<String>> diffComparisons = ListsComparator.compareStreams(left.stream(), right.stream()).collect(Collectors.toList());

        Assert.assertEquals(2, diffComparisons.size());

        assertComparison(diffComparisons.get(0), "aaa", 1);
        assertComparison(diffComparisons.get(1), "bbb", 1);
    }
}
