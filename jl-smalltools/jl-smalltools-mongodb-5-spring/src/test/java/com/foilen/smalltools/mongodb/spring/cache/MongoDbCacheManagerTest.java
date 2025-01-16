/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2025 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.mongodb.spring.cache;

import com.foilen.smalltools.mongodb.AbstractEmbeddedMongoDbTest;
import com.foilen.smalltools.mongodb.distributed.MongoDbReentrantLock;
import com.foilen.smalltools.test.asserts.AssertTools;
import com.foilen.smalltools.tools.SecureRandomTools;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MongoDbCacheManagerTest extends AbstractEmbeddedMongoDbTest {

    @Test
    public void testSingleThreadAllMethods_noLock() throws Exception {
        String databaseName = SecureRandomTools.randomHexString(10);
        String collectionNamePrefix = "cache_";

        MongoDbCacheManager cacheManager = new MongoDbCacheManager(mongoClient, databaseName, collectionNamePrefix,
                null, 60);
        testSingleThreadAllMethods(cacheManager);
    }

    @Test
    public void testSingleThreadAllMethods_withLock() throws Exception {
        String databaseName = SecureRandomTools.randomHexString(10);
        String collectionNamePrefix = "cache_";

        MongoCollection<Document> lockMongoCollection = mongoClient.getDatabase(databaseName).getCollection("lock");
        MongoDbReentrantLock lock = new MongoDbReentrantLock(mongoClient, lockMongoCollection);

        MongoDbCacheManager cacheManager = new MongoDbCacheManager(mongoClient, databaseName, collectionNamePrefix,
                lock, 60);
        testSingleThreadAllMethods(cacheManager);
    }


    private void testSingleThreadAllMethods(MongoDbCacheManager cacheManager) throws Exception {
        // No current caches
        AssertTools.assertJsonComparison(List.of(), cacheManager.getCacheNames());

        // Create caches
        Cache intCache = cacheManager.getCache("int");
        Cache stringCache = cacheManager.getCache("string");

        AssertTools.assertJsonComparison(List.of("int", "string"), cacheManager.getCacheNames());

        // Basics
        Assertions.assertEquals("int", intCache.getName());
        Assertions.assertEquals("string", stringCache.getName());

        // Add some values
        intCache.put("a", 1);
        intCache.put("b", 2);
        intCache.put("c", 3);
        intCache.put("z", null);
        stringCache.put("a", "A");
        stringCache.put("d", "D");
        stringCache.put("e", "E");
        stringCache.put("zz", null);

        // Get
        Assertions.assertEquals(1, intCache.get("a").get());
        Assertions.assertEquals(2, intCache.get("b").get());
        Assertions.assertEquals(3, intCache.get("c").get());
        Assertions.assertNull(intCache.get("z").get());
        Assertions.assertNull(intCache.get("missing"));

        Assertions.assertEquals("A", stringCache.get("a").get());
        Assertions.assertEquals("D", stringCache.get("d").get());
        Assertions.assertEquals("E", stringCache.get("e").get());
        Assertions.assertNull(stringCache.get("zz").get());
        Assertions.assertNull(stringCache.get("missing"));
        Assertions.assertNull(stringCache.get("b"));

        // Get with type
        Assertions.assertEquals(1, intCache.get("a", Integer.class));
        Assertions.assertEquals(2, intCache.get("b", Integer.class));
        Assertions.assertNull(intCache.get("missing", Integer.class));

        Assertions.assertEquals("A", stringCache.get("a", String.class));
        Assertions.assertEquals("D", stringCache.get("d", String.class));
        Assertions.assertNull(stringCache.get("missing", String.class));

        // Get with loader
        Assertions.assertEquals(4, intCache.get("g", () -> 4));
        Assertions.assertEquals(4, intCache.get("g", () -> 44)); // Previous value
        Assertions.assertEquals(4, intCache.get("g", () -> 44)); // Previous value
        Assertions.assertEquals("G", stringCache.get("g", () -> "G"));
        Assertions.assertEquals("G", stringCache.get("g", () -> "GG")); // Previous value
        Assertions.assertEquals("G", stringCache.get("g", () -> "GG")); // Previous value

        // Evict
        intCache.evict("a");
        stringCache.evict("a");
        Assertions.assertNull(intCache.get("a"));
        Assertions.assertNull(stringCache.get("a"));

        // Retrieve
        var completableFuture = intCache.retrieve("b");
        Assertions.assertEquals(new SimpleValueWrapper(2), completableFuture.get());
        completableFuture = stringCache.retrieve("d");
        Assertions.assertEquals(new SimpleValueWrapper("D"), completableFuture.get());
        completableFuture = intCache.retrieve("missing");
        Assertions.assertNull(completableFuture.get());
        completableFuture = stringCache.retrieve("missing");
        Assertions.assertNull(completableFuture.get());

        // Retrieve with loader
        completableFuture = intCache.retrieve("retrieveWithLoader", () -> {
            var cf = new CompletableFuture<>();
            cf.complete(42);
            return cf;
        });
        Assertions.assertEquals(42, completableFuture.get());
        completableFuture = intCache.retrieve("retrieveWithLoader", () -> {
            var cf = new CompletableFuture<>();
            cf.complete(666);
            return cf;
        });
        Assertions.assertEquals(42, completableFuture.get());

        // Put if absent
        Assertions.assertEquals(new SimpleValueWrapper(99), intCache.putIfAbsent("putIfAbsent", 99));
        Assertions.assertEquals(new SimpleValueWrapper(99), intCache.putIfAbsent("putIfAbsent", 100));

        // Evict if present
        Assertions.assertTrue(intCache.evictIfPresent("putIfAbsent"));
        Assertions.assertFalse(intCache.evictIfPresent("putIfAbsent"));

        // Clear
        intCache.clear();
        Assertions.assertTrue(stringCache.invalidate());
        Assertions.assertFalse(stringCache.invalidate());
        Assertions.assertNull(intCache.get("b"));
        Assertions.assertNull(stringCache.get("d"));
    }

}
