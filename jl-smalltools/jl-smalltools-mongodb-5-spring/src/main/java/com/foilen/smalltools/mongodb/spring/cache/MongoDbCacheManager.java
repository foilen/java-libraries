/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.mongodb.spring.cache;

import com.foilen.smalltools.mongodb.distributed.MongoDbReentrantLock;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.*;

/**
 * A distributed cache using MongoDB that creates {@link com.foilen.smalltools.mongodb.distributed.MongoDbSortedMapStringObject} using the prefix and the cache name.
 */
public class MongoDbCacheManager implements CacheManager {

    private final MongoClient mongoClient;
    private final String databaseName;
    private final String collectionNamePrefix;
    private final MongoDbReentrantLock lock;
    private final long defaultMaxDurationInSec;
    private final Map<String, Long> maxDurationInSecByCacheName = new HashMap<>();

    /**
     * Create a cache manager.
     *
     * @param mongoClient             the mongo client
     * @param databaseName            the database name
     * @param collectionNamePrefix    the collection name prefix
     * @param lock                    (optional) the lock to use for atomic operations
     * @param defaultMaxDurationInSec the default maximum duration of an element in the cache (MongoDB can take up to 60 seconds to clean up after the expiration)
     */
    public MongoDbCacheManager(MongoClient mongoClient, String databaseName, String collectionNamePrefix,
                               MongoDbReentrantLock lock, long defaultMaxDurationInSec
    ) {
        this(mongoClient, databaseName, collectionNamePrefix, lock, defaultMaxDurationInSec, Map.of());
    }

    /**
     * Create a cache manager.
     *
     * @param mongoClient                 the mongo client
     * @param databaseName                the database name
     * @param collectionNamePrefix        the collection name prefix
     * @param lock                        (optional) the lock to use for atomic operations
     * @param defaultMaxDurationInSec     the default maximum duration of an element in the cache (MongoDB can take up to 60 seconds to clean up after the expiration)
     * @param maxDurationInSecByCacheName the specific maximum duration of an element in the cache by cache name
     */
    public MongoDbCacheManager(MongoClient mongoClient, String databaseName, String collectionNamePrefix,
                               MongoDbReentrantLock lock, long defaultMaxDurationInSec, Map<String, Long> maxDurationInSecByCacheName
    ) {
        this.mongoClient = mongoClient;
        this.databaseName = databaseName;
        this.collectionNamePrefix = collectionNamePrefix;
        this.lock = lock;
        this.defaultMaxDurationInSec = defaultMaxDurationInSec;
        this.maxDurationInSecByCacheName.putAll(maxDurationInSecByCacheName);
    }

    @Override
    public Cache getCache(String cacheName) {
        Long durationInSec = maxDurationInSecByCacheName.getOrDefault(cacheName, defaultMaxDurationInSec);
        MongoCollection<Document> mongoCollection = mongoClient.getDatabase(databaseName).getCollection(collectionNamePrefix + cacheName);
        return new MongoDbCache(cacheName, mongoClient, mongoCollection, lock, durationInSec);
    }

    @Override
    public Collection<String> getCacheNames() {
        // Return all the collection names that start with the collectionNamePrefix
        SortedSet<String> cacheNames = new TreeSet<>();
        mongoClient.getDatabase(databaseName).listCollectionNames().forEach((String collectionName) -> {
            if (collectionName.startsWith(collectionNamePrefix)) {
                cacheNames.add(collectionName.substring(collectionNamePrefix.length()));
            }
        });
        return cacheNames;
    }

}
