package com.foilen.smalltools.mongodb.distributed;

import com.foilen.smalltools.mongodb.AbstractEmbeddedMongoDbTest;
import com.foilen.smalltools.tools.SecureRandomTools;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.SortedSet;

public class MongoDbIdStringSortedSetTest extends AbstractEmbeddedMongoDbTest {

    @Test
    public void testSingleThreadAllMethods() {

        String collectionName = SecureRandomTools.randomHexString(10);
        MongoCollection<Document> mongoCollection = mongoClient.getDatabase("test").getCollection(collectionName);
        var mainSet = new MongoDbIdStringSortedSet(mongoCollection);

        // isEmpty
        Assertions.assertTrue(mainSet.isEmpty());

        // Add
        Assertions.assertTrue(mainSet.add("a"));
        Assertions.assertTrue(mainSet.add("z"));
        Assertions.assertTrue(mainSet.add("b"));
        Assertions.assertTrue(mainSet.add("y"));

        // Re-add
        Assertions.assertFalse(mainSet.add("a"));
        Assertions.assertFalse(mainSet.add("b"));

        // List in order
        Assertions.assertEquals(List.of("a", "b", "y", "z"), mainSet.stream().toList());

        // Contains
        Assertions.assertTrue(mainSet.contains("a"));
        Assertions.assertTrue(mainSet.contains("b"));
        Assertions.assertTrue(mainSet.contains("y"));
        Assertions.assertTrue(mainSet.contains("z"));
        Assertions.assertFalse(mainSet.contains("c"));

        // Size
        Assertions.assertEquals(4, mainSet.size());

        // isEmpty
        Assertions.assertFalse(mainSet.isEmpty());

        // Remove
        Assertions.assertTrue(mainSet.remove("a"));
        Assertions.assertTrue(mainSet.remove("z"));
        Assertions.assertFalse(mainSet.remove("z"));
        Assertions.assertFalse(mainSet.remove("c"));
        Assertions.assertEquals(List.of("b", "y"), mainSet.stream().toList());

        // Contains all
        Assertions.assertTrue(mainSet.containsAll(List.of("b", "y")));
        Assertions.assertFalse(mainSet.containsAll(List.of("b", "y", "z")));

        // Add all
        Assertions.assertTrue(mainSet.addAll(List.of("z", "a")));
        Assertions.assertFalse(mainSet.addAll(List.of("z", "a")));
        Assertions.assertEquals(List.of("a", "b", "y", "z"), mainSet.stream().toList());

        // Retain all
        Assertions.assertTrue(mainSet.retainAll(List.of("a", "b")));
        Assertions.assertFalse(mainSet.retainAll(List.of("a", "b")));
        Assertions.assertEquals(List.of("a", "b"), mainSet.stream().toList());

        // Remove all
        Assertions.assertTrue(mainSet.addAll(List.of("a", "b", "y", "z")));
        Assertions.assertTrue(mainSet.removeAll(List.of("a", "b")));
        Assertions.assertFalse(mainSet.removeAll(List.of("a", "b")));
        Assertions.assertEquals(List.of("y", "z"), mainSet.stream().toList());

        // Clear
        mainSet.clear();
        Assertions.assertTrue(mainSet.isEmpty());
        Assertions.assertEquals(List.of(), mainSet.stream().toList());

        // SUBSET
        SortedSet<String> subSet = mainSet.subSet("a", "k");
        Assertions.assertTrue(subSet.isEmpty());

        // Put some in the main
        Assertions.assertTrue(mainSet.addAll(List.of("a", "b", "y", "z")));
        Assertions.assertEquals(List.of("a", "b"), subSet.stream().toList());

        // Add in the subset
        Assertions.assertTrue(subSet.add("c"));
        Assertions.assertTrue(subSet.add("d"));
        Assertions.assertTrue(subSet.add("e"));
        Assertions.assertFalse(subSet.add("e"));
        Assertions.assertEquals(List.of("a", "b", "c", "d", "e"), subSet.stream().toList());
        Assertions.assertEquals(List.of("a", "b", "c", "d", "e", "y", "z"), mainSet.stream().toList());

        // Out of range operations
        Assertions.assertThrows(IllegalArgumentException.class, () -> subSet.add("t"));
        Assertions.assertFalse(subSet.contains("z"));
        Assertions.assertFalse(subSet.remove("z"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> subSet.addAll(List.of("t")));
        Assertions.assertTrue(subSet.removeAll(List.of("a", "z")));
        Assertions.assertFalse(subSet.removeAll(List.of("a", "z")));
        Assertions.assertTrue(subSet.retainAll(List.of("z")));
        Assertions.assertEquals(List.of(), subSet.stream().toList());
        Assertions.assertEquals(List.of("y", "z"), mainSet.stream().toList());
        Assertions.assertFalse(subSet.containsAll(List.of("z")));

    }

}
