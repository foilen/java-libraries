/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.smalltools.test.asserts.AssertTools;

public class CollectionsToolsTest {

    @Test
    public void testGetOrCreateEmpty() {
        Map<String, AtomicInteger> expected = new HashMap<>();
        Map<String, AtomicInteger> actual = new HashMap<>();

        // Create first
        expected.put("first", new AtomicInteger(1));
        AtomicInteger item = CollectionsTools.getOrCreateEmpty(actual, "first", AtomicInteger.class);
        item.set(1);
        AssertTools.assertJsonComparison(expected, actual);

        // Create second
        expected.put("second", new AtomicInteger(2));
        item = CollectionsTools.getOrCreateEmpty(actual, "second", AtomicInteger.class);
        item.set(2);
        AssertTools.assertJsonComparison(expected, actual);

        // Get existing
        CollectionsTools.getOrCreateEmpty(actual, "first", AtomicInteger.class);
        AssertTools.assertJsonComparison(expected, actual);
    }

    @Test
    public void testGetOrCreateEmptyArrayList() {
        Map<String, List<String>> map = new HashMap<>();
        List<String> actual = CollectionsTools.getOrCreateEmptyArrayList(map, "first", String.class);
        actual.add("itemA");
        Assert.assertEquals(Arrays.asList("itemA"), actual);

        actual = CollectionsTools.getOrCreateEmptyArrayList(map, "first", String.class);
        actual.add("itemB");
        Assert.assertEquals(Arrays.asList("itemA", "itemB"), actual);
    }

    @Test
    public void testGetOrCreateEmptyHashSet() {
        Map<String, Set<String>> map = new HashMap<>();
        Set<String> actual = CollectionsTools.getOrCreateEmptyHashSet(map, "first", String.class);
        actual.add("itemA");
        actual.add("itemA");
        actual.add("itemB");
        Set<String> expected = new HashSet<>();
        expected.add("itemA");
        expected.add("itemB");
        Assert.assertEquals(expected, actual);

        actual = CollectionsTools.getOrCreateEmptyHashSet(map, "first", String.class);
        actual.add("itemC");
        expected.add("itemC");
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetOrCreateEmptyTreeSet() {
        Map<String, Set<String>> map = new HashMap<>();
        Set<String> actual = CollectionsTools.getOrCreateEmptyTreeSet(map, "first", String.class);
        actual.add("itemA");
        actual.add("itemA");
        actual.add("itemB");
        Set<String> expected = new HashSet<>();
        expected.add("itemA");
        expected.add("itemB");
        Assert.assertEquals(expected, actual);

        actual = CollectionsTools.getOrCreateEmptyTreeSet(map, "first", String.class);
        actual.add("itemC");
        expected.add("itemC");
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testIsAllItemNotNullArray() {
        Assert.assertTrue(CollectionsTools.isAllItemNotNull());
        Assert.assertTrue(CollectionsTools.isAllItemNotNull("a", "b", "c", ""));
        Assert.assertFalse(CollectionsTools.isAllItemNotNull((String) null));
        Assert.assertFalse(CollectionsTools.isAllItemNotNull("a", null, "c"));
    }

    @Test
    public void testIsAllItemNotNullCollection() {
        Assert.assertTrue(CollectionsTools.isAllItemNotNull(Arrays.asList()));
        Assert.assertTrue(CollectionsTools.isAllItemNotNull(Arrays.asList("a", "b", "c", "")));
        Assert.assertFalse(CollectionsTools.isAllItemNotNull(Arrays.asList((String) null)));
        Assert.assertFalse(CollectionsTools.isAllItemNotNull(Arrays.asList("a", null, "c")));
    }

    @Test
    public void testIsAllItemNotNullOrEmptyArray() {
        Assert.assertTrue(CollectionsTools.isAllItemNotNullOrEmpty());
        Assert.assertTrue(CollectionsTools.isAllItemNotNullOrEmpty("a", "b", "c"));
        Assert.assertFalse(CollectionsTools.isAllItemNotNullOrEmpty(""));
        Assert.assertFalse(CollectionsTools.isAllItemNotNullOrEmpty((String) null));
        Assert.assertFalse(CollectionsTools.isAllItemNotNullOrEmpty("a", "", "c"));
        Assert.assertFalse(CollectionsTools.isAllItemNotNullOrEmpty("a", null, "c"));
    }

    @Test
    public void testIsAllItemNotNullOrEmptyCollection() {
        Assert.assertTrue(CollectionsTools.isAllItemNotNullOrEmpty(Arrays.asList()));
        Assert.assertTrue(CollectionsTools.isAllItemNotNullOrEmpty(Arrays.asList("a", "b", "c")));
        Assert.assertFalse(CollectionsTools.isAllItemNotNullOrEmpty(Arrays.asList("")));
        Assert.assertFalse(CollectionsTools.isAllItemNotNullOrEmpty(Arrays.asList((String) null)));
        Assert.assertFalse(CollectionsTools.isAllItemNotNullOrEmpty(Arrays.asList("a", "", "c")));
        Assert.assertFalse(CollectionsTools.isAllItemNotNullOrEmpty(Arrays.asList("a", null, "c")));
    }

    @Test
    public void testIsAnyItemNotNullArray() {
        Assert.assertFalse(CollectionsTools.isAnyItemNotNull());
        Assert.assertTrue(CollectionsTools.isAnyItemNotNull("a", "b", "c", ""));
        Assert.assertFalse(CollectionsTools.isAnyItemNotNull((String) null));
        Assert.assertFalse(CollectionsTools.isAnyItemNotNull((String) null, (String) null));
        Assert.assertTrue(CollectionsTools.isAnyItemNotNull("a", null, "c"));
    }

    @Test
    public void testIsAnyItemNotNullCollection() {
        Assert.assertFalse(CollectionsTools.isAnyItemNotNull(Arrays.asList()));
        Assert.assertTrue(CollectionsTools.isAnyItemNotNull(Arrays.asList("a", "b", "c", "")));
        Assert.assertFalse(CollectionsTools.isAnyItemNotNull(Arrays.asList((String) null)));
        Assert.assertFalse(CollectionsTools.isAnyItemNotNull(Arrays.asList((String) null, (String) null)));
        Assert.assertTrue(CollectionsTools.isAnyItemNotNull(Arrays.asList("a", null, "c")));
    }

    @Test
    public void testIsAnyItemNotNullOrEmptyArray() {
        Assert.assertFalse(CollectionsTools.isAnyItemNotNullOrEmpty());
        Assert.assertTrue(CollectionsTools.isAnyItemNotNullOrEmpty("a", "b", "c"));
        Assert.assertFalse(CollectionsTools.isAnyItemNotNullOrEmpty(""));
        Assert.assertFalse(CollectionsTools.isAnyItemNotNullOrEmpty("", ""));
        Assert.assertTrue(CollectionsTools.isAnyItemNotNullOrEmpty("", "a", ""));
        Assert.assertFalse(CollectionsTools.isAnyItemNotNullOrEmpty((String) null));
        Assert.assertFalse(CollectionsTools.isAnyItemNotNullOrEmpty((String) null, (String) null));
        Assert.assertTrue(CollectionsTools.isAnyItemNotNullOrEmpty("a", "", "c"));
        Assert.assertTrue(CollectionsTools.isAnyItemNotNullOrEmpty("a", null, "c"));
    }

    @Test
    public void testIsAnyItemNotNullOrEmptyCollection() {
        Assert.assertFalse(CollectionsTools.isAnyItemNotNullOrEmpty(Arrays.asList()));
        Assert.assertTrue(CollectionsTools.isAnyItemNotNullOrEmpty(Arrays.asList("a", "b", "c")));
        Assert.assertFalse(CollectionsTools.isAnyItemNotNullOrEmpty(Arrays.asList("")));
        Assert.assertFalse(CollectionsTools.isAnyItemNotNullOrEmpty(Arrays.asList("", "")));
        Assert.assertTrue(CollectionsTools.isAnyItemNotNullOrEmpty(Arrays.asList("", "a", "")));
        Assert.assertFalse(CollectionsTools.isAnyItemNotNullOrEmpty(Arrays.asList((String) null)));
        Assert.assertFalse(CollectionsTools.isAnyItemNotNullOrEmpty(Arrays.asList((String) null, (String) null)));
        Assert.assertTrue(CollectionsTools.isAnyItemNotNullOrEmpty(Arrays.asList("a", "", "c")));
        Assert.assertTrue(CollectionsTools.isAnyItemNotNullOrEmpty(Arrays.asList("a", null, "c")));
    }

    @Test
    public void testIsNullOrEmpty() {
        List<String> actual = null;
        Assert.assertTrue(CollectionsTools.isNullOrEmpty(actual));

        actual = new ArrayList<>();
        Assert.assertTrue(CollectionsTools.isNullOrEmpty(actual));

        actual.add("a");
        Assert.assertFalse(CollectionsTools.isNullOrEmpty(actual));
    }

    @Test
    public void testRemoveValue() {
        Map<String, Integer> map = new HashMap<>();
        map.put("key1", 1);
        map.put("key2", 5);
        map.put("key3", 5);
        map.put("key4", 1);

        Assert.assertEquals(4, map.size());

        Assert.assertEquals("key2", CollectionsTools.removeValue(map, 5));
        Assert.assertEquals(3, map.size());
        Assert.assertEquals("key3", CollectionsTools.removeValue(map, 5));
        Assert.assertEquals(2, map.size());
        Assert.assertNull(CollectionsTools.removeValue(map, 5));
        Assert.assertEquals(2, map.size());
        Assert.assertEquals(Integer.valueOf(1), map.get("key1"));
        Assert.assertEquals(Integer.valueOf(1), map.get("key4"));
    }

    @Test
    public void testRemoveValues() {
        Map<String, Integer> map = new HashMap<>();
        map.put("key1", 1);
        map.put("key2", 5);
        map.put("key3", 5);
        map.put("key4", 1);

        Assert.assertEquals(4, map.size());

        CollectionsTools.removeValues(map, 5);
        Assert.assertEquals(2, map.size());
        Assert.assertEquals(Integer.valueOf(1), map.get("key1"));
        Assert.assertEquals(Integer.valueOf(1), map.get("key4"));
    }

}
