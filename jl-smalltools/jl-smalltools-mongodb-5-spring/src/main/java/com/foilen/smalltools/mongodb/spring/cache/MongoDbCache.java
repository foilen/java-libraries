/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2025 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.mongodb.spring.cache;

import com.foilen.smalltools.mongodb.distributed.MongoDbReentrantLock;
import com.foilen.smalltools.mongodb.distributed.MongoDbSortedMapStringObject;
import com.foilen.smalltools.mongodb.spring.cache.internal.ValueAndType;
import com.foilen.smalltools.tools.ExecutorsTools;
import com.foilen.smalltools.tools.JsonTools;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class MongoDbCache implements Cache {

    private final String cacheName;
    private final MongoDbSortedMapStringObject<ValueAndType> sortedMap;
    private final MongoDbReentrantLock lock;

    /**
     * Create a cache.
     *
     * @param cacheName        The name of the cache
     * @param mongoClient      The mongo client
     * @param mongoCollection  The mongo collection
     * @param lock             (optional) The lock to use for atomic operations
     * @param maxDurationInSec The maximum duration of an element in the cache (MongoDB can take up to 60 seconds to clean up after the expiration)
     */
    public MongoDbCache(String cacheName, MongoClient mongoClient, MongoCollection<Document> mongoCollection,
                        MongoDbReentrantLock lock, long maxDurationInSec
    ) {
        this.cacheName = cacheName;
        this.lock = lock;
        this.sortedMap = new MongoDbSortedMapStringObject<>(ValueAndType.class, mongoClient, mongoCollection, maxDurationInSec);
    }

    @Override
    public String getName() {
        return cacheName;
    }

    @Override
    public Object getNativeCache() {
        return sortedMap;
    }

    @Override
    public ValueWrapper get(Object key) {
        ValueAndType valueAndType = sortedMap.get(key.toString());
        if (valueAndType == null) {
            return null;
        }
        return new SimpleValueWrapper(valueAndType.toValue());
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        ValueAndType valueAndType = sortedMap.get(key.toString());
        if (valueAndType == null) {
            return null;
        }
        return JsonTools.readFromString(valueAndType.getJsonValue(), type);
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        ValueAndType valueAndType = sortedMap.get(key.toString());
        if (valueAndType == null) {

            // Use the lock if available
            if (lock == null) {
                // Get the value
                T value = null;
                try {
                    value = valueLoader.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                // Persist it
                put(key, value);

                // Return it
                return value;
            } else {

                String lockName = cacheName + "-" + key;
                try {
                    // Lock
                    lock.lock(lockName);

                    // Check if the value is still not there
                    valueAndType = sortedMap.get(key.toString());
                    if (valueAndType == null) {
                        // Get the value
                        T value = valueLoader.call();

                        // Persist it
                        put(key, value);

                        // Return it
                        return value;
                    } else {
                        return (T) valueAndType.toValue();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    lock.unlock(lockName);
                }
            }
        }
        return (T) valueAndType.toValue();
    }

    @Override
    public void put(Object key, Object value) {
        if (value == null) {
            sortedMap.put(key.toString(), new ValueAndType()
                    .setJsonValue(JsonTools.compactPrint(value))
            );
        } else {
            sortedMap.put(key.toString(), new ValueAndType()
                    .setJsonValue(JsonTools.compactPrint(value))
                    .setType(value.getClass().getName())
            );
        }
    }

    @Override
    public void evict(Object key) {
        sortedMap.remove(key.toString());
    }

    @Override
    public void clear() {
        sortedMap.clear();
    }

    @Override
    public CompletableFuture<?> retrieve(Object key) {
        CompletableFuture<ValueWrapper> completableFuture = new CompletableFuture<>();
        ExecutorsTools.getCachedDaemonThreadPool().execute(() -> {
            ValueWrapper valueWrapper = get(key);
            completableFuture.complete(valueWrapper);
        });
        return completableFuture;
    }

    @Override
    public <T> CompletableFuture<T> retrieve(Object key, Supplier<CompletableFuture<T>> valueLoader) {
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        ExecutorsTools.getCachedDaemonThreadPool().execute(() -> {
            T value = get(key, () -> valueLoader.get().join());
            completableFuture.complete(value);
        });
        return completableFuture;
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        ValueAndType previous = sortedMap.putIfAbsent(key.toString(), new ValueAndType()
                .setJsonValue(JsonTools.compactPrint(value))
                .setType(value.getClass().getName())
        );
        if (previous == null) {
            return new SimpleValueWrapper(value);
        }
        return new SimpleValueWrapper(previous.toValue());
    }

    @Override
    public boolean evictIfPresent(Object key) {
        return sortedMap.remove(key.toString()) != null;
    }

    @Override
    public boolean invalidate() {
        return sortedMap.clearAndTellIfWasEmpty();
    }

}
