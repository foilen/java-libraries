package com.foilen.smalltools.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        Assertions.assertEquals(Arrays.asList("itemA"), actual);

        actual = CollectionsTools.getOrCreateEmptyArrayList(map, "first", String.class);
        actual.add("itemB");
        Assertions.assertEquals(actual, Arrays.asList("itemA", "itemB"));
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
        Assertions.assertEquals(expected, actual);

        actual = CollectionsTools.getOrCreateEmptyHashSet(map, "first", String.class);
        actual.add("itemC");
        expected.add("itemC");
        Assertions.assertEquals(expected, actual);
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
        Assertions.assertEquals(expected, actual);

        actual = CollectionsTools.getOrCreateEmptyTreeSet(map, "first", String.class);
        actual.add("itemC");
        expected.add("itemC");
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testIsAllItemNotNullArray() {
        Assertions.assertTrue(CollectionsTools.isAllItemNotNull());
        Assertions.assertTrue(CollectionsTools.isAllItemNotNull("a", "b", "c", ""));
        Assertions.assertFalse(CollectionsTools.isAllItemNotNull((String) null));
        Assertions.assertFalse(CollectionsTools.isAllItemNotNull("a", null, "c"));
    }

    @Test
    public void testIsAllItemNotNullCollection() {
        Assertions.assertTrue(CollectionsTools.isAllItemNotNull(Arrays.asList()));
        Assertions.assertTrue(CollectionsTools.isAllItemNotNull(Arrays.asList("a", "b", "c", "")));
        Assertions.assertFalse(CollectionsTools.isAllItemNotNull(Arrays.asList((String) null)));
        Assertions.assertFalse(CollectionsTools.isAllItemNotNull(Arrays.asList("a", null, "c")));
    }

    @Test
    public void testIsAllItemNotNullOrEmptyArray() {
        Assertions.assertTrue(CollectionsTools.isAllItemNotNullOrEmpty());
        Assertions.assertTrue(CollectionsTools.isAllItemNotNullOrEmpty("a", "b", "c"));
        Assertions.assertFalse(CollectionsTools.isAllItemNotNullOrEmpty(""));
        Assertions.assertFalse(CollectionsTools.isAllItemNotNullOrEmpty((String) null));
        Assertions.assertFalse(CollectionsTools.isAllItemNotNullOrEmpty("a", "", "c"));
        Assertions.assertFalse(CollectionsTools.isAllItemNotNullOrEmpty("a", null, "c"));
    }

    @Test
    public void testIsAllItemNotNullOrEmptyCollection() {
        Assertions.assertTrue(CollectionsTools.isAllItemNotNullOrEmpty(Arrays.asList()));
        Assertions.assertTrue(CollectionsTools.isAllItemNotNullOrEmpty(Arrays.asList("a", "b", "c")));
        Assertions.assertFalse(CollectionsTools.isAllItemNotNullOrEmpty(Arrays.asList("")));
        Assertions.assertFalse(CollectionsTools.isAllItemNotNullOrEmpty(Arrays.asList((String) null)));
        Assertions.assertFalse(CollectionsTools.isAllItemNotNullOrEmpty(Arrays.asList("a", "", "c")));
        Assertions.assertFalse(CollectionsTools.isAllItemNotNullOrEmpty(Arrays.asList("a", null, "c")));
    }

    @Test
    public void testIsAnyItemNotNullArray() {
        Assertions.assertFalse(CollectionsTools.isAnyItemNotNull());
        Assertions.assertTrue(CollectionsTools.isAnyItemNotNull("a", "b", "c", ""));
        Assertions.assertFalse(CollectionsTools.isAnyItemNotNull((String) null));
        Assertions.assertFalse(CollectionsTools.isAnyItemNotNull((String) null, (String) null));
        Assertions.assertTrue(CollectionsTools.isAnyItemNotNull("a", null, "c"));
    }

    @Test
    public void testIsAnyItemNotNullCollection() {
        Assertions.assertFalse(CollectionsTools.isAnyItemNotNull(Arrays.asList()));
        Assertions.assertTrue(CollectionsTools.isAnyItemNotNull(Arrays.asList("a", "b", "c", "")));
        Assertions.assertFalse(CollectionsTools.isAnyItemNotNull(Arrays.asList((String) null)));
        Assertions.assertFalse(CollectionsTools.isAnyItemNotNull(Arrays.asList((String) null, (String) null)));
        Assertions.assertTrue(CollectionsTools.isAnyItemNotNull(Arrays.asList("a", null, "c")));
    }

    @Test
    public void testIsAnyItemNotNullOrEmptyArray() {
        Assertions.assertFalse(CollectionsTools.isAnyItemNotNullOrEmpty());
        Assertions.assertTrue(CollectionsTools.isAnyItemNotNullOrEmpty("a", "b", "c"));
        Assertions.assertFalse(CollectionsTools.isAnyItemNotNullOrEmpty(""));
        Assertions.assertFalse(CollectionsTools.isAnyItemNotNullOrEmpty("", ""));
        Assertions.assertTrue(CollectionsTools.isAnyItemNotNullOrEmpty("", "a", ""));
        Assertions.assertFalse(CollectionsTools.isAnyItemNotNullOrEmpty((String) null));
        Assertions.assertFalse(CollectionsTools.isAnyItemNotNullOrEmpty((String) null, (String) null));
        Assertions.assertTrue(CollectionsTools.isAnyItemNotNullOrEmpty("a", "", "c"));
        Assertions.assertTrue(CollectionsTools.isAnyItemNotNullOrEmpty("a", null, "c"));
    }

    @Test
    public void testIsAnyItemNotNullOrEmptyCollection() {
        Assertions.assertFalse(CollectionsTools.isAnyItemNotNullOrEmpty(Arrays.asList()));
        Assertions.assertTrue(CollectionsTools.isAnyItemNotNullOrEmpty(Arrays.asList("a", "b", "c")));
        Assertions.assertFalse(CollectionsTools.isAnyItemNotNullOrEmpty(Arrays.asList("")));
        Assertions.assertFalse(CollectionsTools.isAnyItemNotNullOrEmpty(Arrays.asList("", "")));
        Assertions.assertTrue(CollectionsTools.isAnyItemNotNullOrEmpty(Arrays.asList("", "a", "")));
        Assertions.assertFalse(CollectionsTools.isAnyItemNotNullOrEmpty(Arrays.asList((String) null)));
        Assertions.assertFalse(CollectionsTools.isAnyItemNotNullOrEmpty(Arrays.asList((String) null, (String) null)));
        Assertions.assertTrue(CollectionsTools.isAnyItemNotNullOrEmpty(Arrays.asList("a", "", "c")));
        Assertions.assertTrue(CollectionsTools.isAnyItemNotNullOrEmpty(Arrays.asList("a", null, "c")));
    }

    @Test
    public void testIsNullOrEmpty() {
        List<String> actual = null;
        Assertions.assertTrue(CollectionsTools.isNullOrEmpty(actual));

        actual = new ArrayList<>();
        Assertions.assertTrue(CollectionsTools.isNullOrEmpty(actual));

        actual.add("a");
        Assertions.assertFalse(CollectionsTools.isNullOrEmpty(actual));
    }

    @Test
    public void testRemoveValue() {
        Map<String, Integer> map = new HashMap<>();
        map.put("key1", 1);
        map.put("key2", 5);
        map.put("key3", 5);
        map.put("key4", 1);

        Assertions.assertEquals(4, map.size());

        Assertions.assertEquals("key2", CollectionsTools.removeValue(map, 5));
        Assertions.assertEquals(3, map.size());
        Assertions.assertEquals("key3", CollectionsTools.removeValue(map, 5));
        Assertions.assertEquals(2, map.size());
        Assertions.assertNull(CollectionsTools.removeValue(map, 5));
        Assertions.assertEquals(2, map.size());
        Assertions.assertEquals(Integer.valueOf(1), map.get("key1"));
        Assertions.assertEquals(Integer.valueOf(1), map.get("key4"));
    }

    @Test
    public void testRemoveValues() {
        Map<String, Integer> map = new HashMap<>();
        map.put("key1", 1);
        map.put("key2", 5);
        map.put("key3", 5);
        map.put("key4", 1);

        Assertions.assertEquals(4, map.size());

        CollectionsTools.removeValues(map, 5);
        Assertions.assertEquals(2, map.size());
        Assertions.assertEquals(Integer.valueOf(1), map.get("key1"));
        Assertions.assertEquals(Integer.valueOf(1), map.get("key4"));
    }

}
