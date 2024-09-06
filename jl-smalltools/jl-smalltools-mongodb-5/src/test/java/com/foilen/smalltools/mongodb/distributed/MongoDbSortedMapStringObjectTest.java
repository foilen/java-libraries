/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.mongodb.distributed;

import com.foilen.smalltools.collection.ImmutableMapEntry;
import com.foilen.smalltools.mongodb.AbstractEmbeddedMongoDbTest;
import com.foilen.smalltools.test.asserts.AssertTools;
import com.foilen.smalltools.tools.ExecutorsTools;
import com.foilen.smalltools.tools.SecureRandomTools;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class MongoDbSortedMapStringObjectTest extends AbstractEmbeddedMongoDbTest {

    @Test
    public void testSingleThreadAllMethods() {

        String collectionName = SecureRandomTools.randomHexString(10);
        MongoCollection<Document> mongoCollection = mongoClient.getDatabase("test").getCollection(collectionName);
        var map = new MongoDbSortedMapStringObject<>(String.class, mongoClient, mongoCollection);

        // Put some values
        SortedMap<String, String> expected = new TreeMap<String, String>();
        for (int i = 0; i < 100; ++i) {
            map.put("key" + i, "value" + i);
            expected.put("key" + i, "value" + i);
        }
        AssertTools.assertJsonComparison(expected, map);

        // Remove some keys
        for (int i = 0; i < 50; ++i) {
            map.remove("key" + i);
            expected.remove("key" + i);
        }
        AssertTools.assertJsonComparison(expected, map);

        // Assert contains
        for (int i = 0; i < 100; ++i) {
            AssertTools.assertJsonComparison(expected.containsKey("key" + i), map.containsKey("key" + i));
        }

        // Update some values
        for (int i = 0; i < 100; ++i) {
            map.put("key" + i, "value" + i + "updated");
            expected.put("key" + i, "value" + i + "updated");
        }
        AssertTools.assertJsonComparison(expected, map);

        // Contains value
        Assertions.assertTrue(map.containsValue("value50updated"));
        Assertions.assertFalse(map.containsValue("value50"));
        Assertions.assertFalse(map.containsValue(null));

        // Contains key
        Assertions.assertTrue(map.containsKey("key50"));
        Assertions.assertFalse(map.containsKey("key50not"));

        // Get
        Assertions.assertEquals("value50updated", map.get("key50"));
        Assertions.assertNull(map.get("bob"));

        // Put all
        SortedMap<String, String> toAdd = new TreeMap<>();
        toAdd.put("key100", "value100");
        toAdd.put("key101", "value101");
        map.putAll(toAdd);
        expected.putAll(toAdd);
        AssertTools.assertJsonComparison(expected, map);

        // Remove
        Assertions.assertEquals("value100", map.remove("key100"));
        Assertions.assertNull(map.remove("key100"));

        // Size
        Assertions.assertEquals(101, map.size());

        // First and last key
        Assertions.assertEquals("key0", map.firstKey());
        Assertions.assertEquals("key99", map.lastKey());

        // Clear
        map.clear();
        expected.clear();
        AssertTools.assertJsonComparison(expected, map);
        Assertions.assertEquals(0, map.size());
        Assertions.assertNull(map.firstKey());
        Assertions.assertNull(map.lastKey());

        // Add some
        map.put("A", "Z1");
        map.put("B", "Y2");
        map.put("C", "X3");
        map.put("D", "W4");

        // Key set
        var keySet = map.keySet();
        AssertTools.assertJsonComparison(new TreeSet<>(Set.of("A", "B", "C", "D")), keySet);
        Assertions.assertEquals("A", keySet.first());
        Assertions.assertEquals("D", keySet.last());
        Assertions.assertEquals(4, keySet.size());
        Assertions.assertTrue(keySet.contains("A"));
        Assertions.assertFalse(keySet.contains("E"));
        Assertions.assertTrue(keySet.remove("A"));
        Assertions.assertFalse(keySet.remove("A"));
        map.put("A", "Z1");
        Assertions.assertTrue(keySet.containsAll(List.of()));
        Assertions.assertTrue(keySet.containsAll(List.of("B", "C", "D")));
        Assertions.assertTrue(keySet.containsAll(List.of("B", "C", "D", "B")));
        Assertions.assertFalse(keySet.containsAll(List.of("B", "C", "E")));
        Assertions.assertFalse(keySet.containsAll(List.of("B", "C", "E", "B")));
        Assertions.assertTrue(keySet.retainAll(List.of("B", "C", "D", "E", "B")));
        AssertTools.assertJsonComparison(new TreeSet<>(Set.of("B", "C", "D")), keySet);
        map.put("A", "Z1");
        map.put("B", "Y2");
        map.put("C", "X3");
        map.put("D", "W4");
        Assertions.assertTrue(keySet.removeAll(List.of("B", "C", "D", "E", "B")));
        AssertTools.assertJsonComparison(new TreeSet<>(Set.of("A")), keySet);
        map.put("A", "Z1");
        map.put("B", "Y2");
        map.put("C", "X3");
        map.put("D", "W4");

        var keySet2 = map.keySet();
        Assertions.assertTrue(keySet.equals(keySet2));
        Assertions.assertTrue(keySet.equals(Set.of("A", "B", "C", "D")));
        Assertions.assertFalse(keySet.equals(Set.of("A", "B", "C")));
        Assertions.assertFalse(keySet.equals(Set.of("A", "B", "C", "D", "E")));
        Assertions.assertFalse(keySet.equals("YAY"));
        Assertions.assertEquals(266, keySet.hashCode());

        var keySetIt = keySet.iterator();
        Assertions.assertThrows(IllegalStateException.class, () -> keySetIt.remove());
        Assertions.assertTrue(keySetIt.hasNext());
        Assertions.assertTrue(keySetIt.hasNext());
        Assertions.assertEquals("A", keySetIt.next());
        Assertions.assertTrue(keySetIt.hasNext());
        Assertions.assertEquals("B", keySetIt.next());
        keySetIt.remove();
        Assertions.assertTrue(keySetIt.hasNext());
        Assertions.assertEquals("C", keySetIt.next());
        Assertions.assertEquals("D", keySetIt.next());
        Assertions.assertFalse(keySetIt.hasNext());
        Assertions.assertThrows(NoSuchElementException.class, () -> keySetIt.next());
        Assertions.assertThrows(IllegalStateException.class, () -> keySetIt.remove());
        AssertTools.assertJsonComparison(new TreeSet<>(Set.of("A", "C", "D")), keySet);
        map.put("B", "Y2");
        AssertTools.assertJsonComparison(new String[]{"A", "B", "C", "D"}, keySet.toArray());

        // A sub one
        var keySetSub = keySet.subSet("B", "D");
        AssertTools.assertJsonComparison(new TreeSet<>(Set.of("B", "C")), keySetSub);
        Assertions.assertEquals("B", keySetSub.first());
        Assertions.assertEquals("C", keySetSub.last());
        Assertions.assertEquals(2, keySetSub.size());
        Assertions.assertTrue(keySetSub.contains("B"));
        Assertions.assertFalse(keySetSub.contains("A"));
        Assertions.assertFalse(keySetSub.contains("D"));
        Assertions.assertTrue(keySetSub.remove("B"));
        Assertions.assertFalse(keySetSub.remove("B"));
        Assertions.assertFalse(keySetSub.remove("D"));
        Assertions.assertFalse(keySetSub.remove("A"));
        map.put("B", "Y2");
        Assertions.assertTrue(keySetSub.containsAll(List.of()));
        Assertions.assertTrue(keySetSub.containsAll(List.of("C")));
        Assertions.assertTrue(keySetSub.containsAll(List.of("C", "C")));
        Assertions.assertFalse(keySetSub.containsAll(List.of("C", "E")));
        Assertions.assertFalse(keySetSub.containsAll(List.of("C", "E", "C")));
        Assertions.assertTrue(keySetSub.retainAll(List.of("C", "D", "E", "C")));
        AssertTools.assertJsonComparison(new TreeSet<>(Set.of("C")), keySetSub);
        AssertTools.assertJsonComparison(new TreeSet<>(Set.of("A", "C", "D")), keySet);
        map.put("B", "Y2");
        Assertions.assertTrue(keySetSub.removeAll(List.of("C", "D", "E", "C")));
        AssertTools.assertJsonComparison(new TreeSet<>(Set.of("B")), keySetSub);
        AssertTools.assertJsonComparison(new TreeSet<>(Set.of("A", "B", "D")), keySet);
        map.put("C", "X3");
        Assertions.assertEquals(keySetSub.headSet("D"), keySetSub);
        Assertions.assertEquals(keySetSub.headSet("Z"), keySetSub);
        Assertions.assertEquals(keySetSub.tailSet("B"), keySetSub);
        Assertions.assertEquals(keySetSub.tailSet("A"), keySetSub);
        AssertTools.assertJsonComparison(new TreeSet<>(Set.of("A", "B", "C")), keySet.headSet("D"));
        AssertTools.assertJsonComparison(new TreeSet<>(Set.of("A", "B", "C", "D")), keySet.headSet("Z"));
        AssertTools.assertJsonComparison(new TreeSet<>(Set.of("B", "C", "D")), keySet.tailSet("B"));
        AssertTools.assertJsonComparison(new TreeSet<>(Set.of("A", "B", "C", "D")), keySet.tailSet("A"));

        // Values
        var values = map.values();
        AssertTools.assertJsonComparison(new TreeSet<>(Set.of("Z1", "Y2", "X3", "W4")), new TreeSet<>(values));
        Assertions.assertFalse(values.isEmpty());
        Assertions.assertEquals(4, values.size());
        Assertions.assertTrue(values.contains("Z1"));
        Assertions.assertFalse(values.contains("Z2"));
        Assertions.assertFalse(values.contains(null));
        AssertTools.assertJsonComparison(new String[]{"Z1", "Y2", "X3", "W4"}, values.toArray());
        Assertions.assertTrue(values.remove("Z1"));
        Assertions.assertFalse(values.remove("Z1"));
        Assertions.assertFalse(values.remove(null));
        Assertions.assertEquals(3, map.size());
        Assertions.assertEquals(3, values.size());
        map.put("A", "Z1");
        Assertions.assertTrue(values.containsAll(List.of()));
        Assertions.assertTrue(values.containsAll(List.of("Y2", "X3", "W4")));
        Assertions.assertTrue(values.containsAll(List.of("Y2", "X3", "W4", "Y2")));
        Assertions.assertFalse(values.containsAll(List.of("Y2", "X3", "Z2")));
        Assertions.assertFalse(values.containsAll(List.of("Y2", "X3", "Z2", "Y2")));
        Assertions.assertTrue(values.retainAll(List.of("Y2", "X3", "W4", "Z2", "Y2")));
        AssertTools.assertJsonComparison(new TreeSet<>(Set.of("Y2", "X3", "W4")), new TreeSet<>(values));
        map.put("A", "Z1");
        Assertions.assertTrue(values.removeAll(List.of("Y2", "X3", "W4", "Z2", "Y2")));
        Assertions.assertFalse(values.removeAll(List.of()));
        AssertTools.assertJsonComparison(new TreeSet<>(Set.of("Z1")), new TreeSet<>(values));
        Assertions.assertTrue(values.retainAll(List.of()));
        AssertTools.assertJsonComparison(new TreeSet<>(Set.of()), new TreeSet<>(values));
        map.put("A", "Z1");
        map.put("B", "Y2");
        map.put("C", "X3");
        map.put("D", "W4");

        var valuesIt = values.iterator();
        Assertions.assertThrows(IllegalStateException.class, () -> valuesIt.remove());
        Assertions.assertTrue(valuesIt.hasNext());
        Assertions.assertTrue(valuesIt.hasNext());
        Assertions.assertEquals("Z1", valuesIt.next());
        Assertions.assertTrue(valuesIt.hasNext());
        Assertions.assertEquals("Y2", valuesIt.next());
        valuesIt.remove();
        Assertions.assertTrue(valuesIt.hasNext());
        Assertions.assertEquals("X3", valuesIt.next());
        Assertions.assertEquals("W4", valuesIt.next());
        Assertions.assertFalse(valuesIt.hasNext());
        Assertions.assertThrows(NoSuchElementException.class, () -> valuesIt.next());
        Assertions.assertThrows(IllegalStateException.class, () -> valuesIt.remove());
        AssertTools.assertJsonComparison(new TreeSet<>(Set.of("Z1", "X3", "W4")), new TreeSet<>(values));
        map.put("B", "Y2");

        // Entry set
        var entrySet = map.entrySet();
        var expectedEntrySet = new TreeSet<Map.Entry<String, String>>(Map.Entry.comparingByKey());
        expectedEntrySet.add(new ImmutableMapEntry<>("A", "Z1"));
        expectedEntrySet.add(new ImmutableMapEntry<>("B", "Y2"));
        expectedEntrySet.add(new ImmutableMapEntry<>("C", "X3"));
        expectedEntrySet.add(new ImmutableMapEntry<>("D", "W4"));
        AssertTools.assertJsonComparison(expectedEntrySet, entrySet);
        Assertions.assertEquals(new ImmutableMapEntry<>("A", "Z1"), entrySet.first());
        Assertions.assertEquals(new ImmutableMapEntry<>("D", "W4"), entrySet.last());
        Assertions.assertEquals(4, entrySet.size());
        Assertions.assertTrue(entrySet.contains(new ImmutableMapEntry<>("A", "Z1")));
        Assertions.assertFalse(entrySet.contains(new ImmutableMapEntry<>("A", "Z2")));
        Assertions.assertFalse(entrySet.contains(null));
        Assertions.assertTrue(entrySet.remove(new ImmutableMapEntry<>("A", "Z1")));
        Assertions.assertFalse(entrySet.remove(new ImmutableMapEntry<>("A", "Z1")));
        Assertions.assertFalse(entrySet.remove(null));
        Assertions.assertEquals(3, map.size());
        Assertions.assertEquals(3, entrySet.size());
        map.put("A", "Z1");
        Assertions.assertTrue(entrySet.containsAll(List.of()));
        Assertions.assertTrue(entrySet.containsAll(List.of(new ImmutableMapEntry<>("B", "Y2"), new ImmutableMapEntry<>("C", "X3"), new ImmutableMapEntry<>("D", "W4"))));
        Assertions.assertTrue(entrySet.containsAll(List.of(new ImmutableMapEntry<>("B", "Y2"), new ImmutableMapEntry<>("C", "X3"), new ImmutableMapEntry<>("D", "W4"), new ImmutableMapEntry<>("B", "Y2"))));
        Assertions.assertFalse(entrySet.containsAll(List.of(new ImmutableMapEntry<>("B", "Y2"), new ImmutableMapEntry<>("C", "X3"), new ImmutableMapEntry<>("E", "Z2"))));
        Assertions.assertFalse(entrySet.containsAll(List.of(new ImmutableMapEntry<>("B", "Y2"), new ImmutableMapEntry<>("C", "X3"), new ImmutableMapEntry<>("E", "Z2"), new ImmutableMapEntry<>("B", "Y2"))));
        Assertions.assertTrue(entrySet.removeAll(List.of(new ImmutableMapEntry<>("B", "Y2"), new ImmutableMapEntry<>("C", "X3"), new ImmutableMapEntry<>("D", "W4"), new ImmutableMapEntry<>("E", "Z2"), new ImmutableMapEntry<>("B", "Y2"))));
        AssertTools.assertJsonComparison(new TreeSet<>(Set.of(new ImmutableMapEntry<>("A", "Z1"))), entrySet);
        Assertions.assertFalse(entrySet.removeAll(List.of()));
        AssertTools.assertJsonComparison(new TreeSet<>(Set.of(new ImmutableMapEntry<>("A", "Z1"))), entrySet);
        var entrySet2 = map.entrySet();
        Assertions.assertTrue(entrySet.equals(entrySet2));
        map.put("B", "Y2");
        map.put("C", "X3");
        map.put("D", "W4");
        Assertions.assertTrue(entrySet.equals(expectedEntrySet));
        Assertions.assertFalse(entrySet.equals("bob"));
        Assertions.assertEquals(23266, entrySet.hashCode());
        AssertTools.assertJsonComparison(new ImmutableMapEntry[]{new ImmutableMapEntry<>("A", "Z1"), new ImmutableMapEntry<>("B", "Y2"), new ImmutableMapEntry<>("C", "X3"), new ImmutableMapEntry<>("D", "W4")}, entrySet.toArray());
        Assertions.assertFalse(entrySet.isEmpty());

        // A sub one
        var entrySetSub = entrySet.subSet(new ImmutableMapEntry<>("B", "Y2"), new ImmutableMapEntry<>("D", "W4"));
        var expectedEntrySetSub = new TreeSet<Map.Entry<String, String>>(Map.Entry.comparingByKey());
        expectedEntrySetSub.add(new ImmutableMapEntry<>("B", "Y2"));
        expectedEntrySetSub.add(new ImmutableMapEntry<>("C", "X3"));
        AssertTools.assertJsonComparison(expectedEntrySetSub, entrySetSub);
        Assertions.assertEquals(new ImmutableMapEntry<>("B", "Y2"), entrySetSub.first());
        Assertions.assertEquals(new ImmutableMapEntry<>("C", "X3"), entrySetSub.last());
        Assertions.assertEquals(2, entrySetSub.size());
        Assertions.assertTrue(entrySetSub.contains(new ImmutableMapEntry<>("B", "Y2")));
        Assertions.assertFalse(entrySetSub.contains(new ImmutableMapEntry<>("A", "Z1")));
        Assertions.assertFalse(entrySetSub.contains(new ImmutableMapEntry<>("D", "W4")));
        Assertions.assertTrue(entrySetSub.remove(new ImmutableMapEntry<>("B", "Y2")));
        Assertions.assertFalse(entrySetSub.remove(new ImmutableMapEntry<>("B", "Y2")));
        Assertions.assertFalse(entrySetSub.remove(new ImmutableMapEntry<>("D", "W4")));
        Assertions.assertFalse(entrySetSub.remove(new ImmutableMapEntry<>("A", "Z1")));
        map.put("B", "Y2");
        Assertions.assertTrue(entrySetSub.containsAll(List.of()));
        Assertions.assertTrue(entrySetSub.containsAll(List.of(new ImmutableMapEntry<>("C", "X3"))));
        Assertions.assertTrue(entrySetSub.containsAll(List.of(new ImmutableMapEntry<>("C", "X3"), new ImmutableMapEntry<>("C", "X3"))));
        Assertions.assertFalse(entrySetSub.containsAll(List.of(new ImmutableMapEntry<>("C", "X3"), new ImmutableMapEntry<>("D", "W4"))));

        var entrySetSubIt = entrySetSub.iterator();
        Assertions.assertThrows(IllegalStateException.class, () -> entrySetSubIt.remove());
        Assertions.assertTrue(entrySetSubIt.hasNext());
        Assertions.assertTrue(entrySetSubIt.hasNext());
        Assertions.assertEquals(new ImmutableMapEntry<>("B", "Y2"), entrySetSubIt.next());
        Assertions.assertTrue(entrySetSubIt.hasNext());
        Assertions.assertEquals(new ImmutableMapEntry<>("C", "X3"), entrySetSubIt.next());
        entrySetSubIt.remove();
        Assertions.assertFalse(entrySetSubIt.hasNext());
        Assertions.assertThrows(NoSuchElementException.class, () -> entrySetSubIt.next());
        Assertions.assertThrows(IllegalStateException.class, () -> entrySetSubIt.remove());
        map.put("C", "X3");

        var entrySetSubIt2 = entrySetSub.iterator();
        Assertions.assertEquals(new ImmutableMapEntry<>("B", "Y2"), entrySetSubIt2.next());
        Assertions.assertEquals(new ImmutableMapEntry<>("C", "X3"), entrySetSubIt2.next());

        // Clear
        map.clear();
        Assertions.assertNull(keySet.first());
        Assertions.assertNull(keySet.last());
        Assertions.assertNull(entrySet.first());
        Assertions.assertNull(entrySet.last());

    }

    @Test
    public void testMultiThreads() {

        final int amountOfThreads = 100;
        int quad = amountOfThreads / 4;

        String collectionName = SecureRandomTools.randomHexString(10);
        MongoCollection<Document> mongoCollection = mongoClient.getDatabase("test").getCollection(collectionName);
        var map1 = new MongoDbSortedMapStringObject<>(String.class, mongoClient, mongoCollection);
        var map2 = new MongoDbSortedMapStringObject<>(String.class, mongoClient, mongoCollection);

        CyclicBarrier waitStart = new CyclicBarrier(amountOfThreads);
        CountDownLatch waitStop = new CountDownLatch(amountOfThreads);

        // Quad1
        for (int i = 0; i < quad; ++i) {
            int finalI = i;
            ExecutorsTools.getCachedDaemonThreadPool().submit(() -> {
                try {
                    waitStart.await();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                map1.put("key" + finalI, "quad1-" + finalI);

                waitStop.countDown();
            });
        }

        // Quad2
        for (int i = 0; i < quad; ++i) {
            int finalI = i;
            ExecutorsTools.getCachedDaemonThreadPool().submit(() -> {
                try {
                    waitStart.await();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                map2.put("key" + (finalI + quad), "quad2-" + finalI);

                waitStop.countDown();
            });
        }

        // Quad3
        for (int i = 0; i < quad; ++i) {
            int finalI = i;
            ExecutorsTools.getCachedDaemonThreadPool().submit(() -> {
                try {
                    waitStart.await();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                map1.put("key" + (finalI + quad * 2), "quad3-" + finalI);

                waitStop.countDown();
            });
        }

        // Quad4
        for (int i = 0; i < quad; ++i) {
            int finalI = i;
            ExecutorsTools.getCachedDaemonThreadPool().submit(() -> {
                try {
                    waitStart.await();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                map2.put("key" + (finalI + quad * 3), "quad4-" + finalI);

                waitStop.countDown();
            });
        }

        try {
            waitStop.await();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Check
        SortedMap<String, String> expected = new TreeMap<String, String>();
        for (int i = 0; i < amountOfThreads; ++i) {
            expected.put("key" + i, "quad" + (i / quad + 1) + "-" + (i % quad));
        }
        AssertTools.assertJsonComparison(expected, map1);
        AssertTools.assertJsonComparison(expected, map2);

    }

    @Test
    public void testMultiThreadsEditAllTheSame() {

        final int amountOfThreads = 100;

        String collectionName = SecureRandomTools.randomHexString(10);
        MongoCollection<Document> mongoCollection = mongoClient.getDatabase("test").getCollection(collectionName);
        var map1 = new MongoDbSortedMapStringObject<>(String.class, mongoClient, mongoCollection);
        var map2 = new MongoDbSortedMapStringObject<>(String.class, mongoClient, mongoCollection);

        CyclicBarrier waitStart = new CyclicBarrier(amountOfThreads);
        CountDownLatch waitStop = new CountDownLatch(amountOfThreads);

        for (int i = 0; i < amountOfThreads; ++i) {
            int finalI = i;
            ExecutorsTools.getCachedDaemonThreadPool().submit(() -> {
                try {
                    waitStart.await();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                map1.put("key", "thread-" + finalI);

                waitStop.countDown();
            });
        }

        try {
            waitStop.await();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Check
        Assertions.assertEquals(1, map1.size());
        Assertions.assertEquals(1, map2.size());
        Assertions.assertTrue(map1.containsKey("key"));
        Assertions.assertTrue(map2.containsKey("key"));
        Assertions.assertEquals(map1.get("key"), map2.get("key"));

    }
}
