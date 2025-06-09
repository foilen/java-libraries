package com.foilen.smalltools.mongodb.distributed;

import com.foilen.smalltools.hash.HashSha1;
import com.foilen.smalltools.mongodb.MongoDbChangeStreamWaitAnyChange;
import com.foilen.smalltools.mongodb.MongoDbManageCollectionTools;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.BufferBatchesTools;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tools.RetryTools;
import com.foilen.smalltools.tuple.Tuple2;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Sorts;
import org.bson.Document;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

/**
 * A distributed queue using MongoDB. When processing or removing an element, it is not removed from MongoDB, but only marked as processed for this specific instance. If you create a new instance, it will replay all the elements (unless you {@link #movePointerToEnd()}).
 * This is perfect for having multiple processes processing global actions that each node must process once. In other words: to broadcast actions to all nodes.
 *
 * @param <E> the type of elements in this queue
 */
public class MongoDbReplayableQueue<E> extends AbstractBasics implements BlockingQueue<E> {

    private final Class<E> entityType;
    private final MongoClient mongoClient;
    private final MongoCollection<Document> mongoCollection;
    private final long stopChangeStreamAfterNoThreadWaitedInMs;

    private MongoDbChangeStreamWaitAnyChange mongoDbChangeStreamWaitAnyChange;

    private long pointer = -1;

    /**
     * Create a new instance of the queue.
     * Default is to stop the change stream after 10 minutes of no thread waiting and to cleanup old entries after 1 hour.
     *
     * @param entityType      the type of elements in this queue
     * @param mongoClient     the mongo client
     * @param mongoCollection the mongo collection
     */
    public MongoDbReplayableQueue(Class<E> entityType, MongoClient mongoClient, MongoCollection<Document> mongoCollection) {
        this(entityType, mongoClient, mongoCollection, 10 * 60000, 3600);
    }

    /**
     * Create a new instance of the queue.
     *
     * @param entityType                              the type of elements in this queue
     * @param mongoClient                             the mongo client
     * @param mongoCollection                         the mongo collection
     * @param stopChangeStreamAfterNoThreadWaitedInMs the maximum time to wait for a change in the change stream
     * @param maxDurationInSec                        the maximum duration of an element in the queue (MongoDB can take up to 60 seconds to clean up after the expiration)
     */
    public MongoDbReplayableQueue(Class<E> entityType, MongoClient mongoClient, MongoCollection<Document> mongoCollection, long stopChangeStreamAfterNoThreadWaitedInMs, long maxDurationInSec) {
        this.entityType = entityType;
        this.mongoClient = mongoClient;
        this.mongoCollection = mongoCollection;
        this.stopChangeStreamAfterNoThreadWaitedInMs = stopChangeStreamAfterNoThreadWaitedInMs;

        MongoDbManageCollectionTools.addCollectionIfMissing(mongoClient, mongoCollection.getNamespace());
        MongoDbManageCollectionTools.manageIndexes(mongoCollection, Map.of(
                "hashJsonValue_id", new Tuple2<>(
                        new Document().append(MongoDbDistributedConstants.FIELD_HASH_JSON_VALUE, 1).append(MongoDbDistributedConstants.FIELD_ID, 1),
                        new IndexOptions()
                ),
                "createdAt_" + maxDurationInSec, new Tuple2<>(
                        new Document().append(MongoDbDistributedConstants.FIELD_CREATED_AT, 1),
                        new IndexOptions().expireAfter(maxDurationInSec, TimeUnit.SECONDS)
                )
        ));
    }

    public MongoDbReplayableQueue<E> movePointerToEnd() {
        var lastEntry = mongoCollection.find()
                .projection(new Document().append(MongoDbDistributedConstants.FIELD_ID, 1))
                .sort(Sorts.descending(MongoDbDistributedConstants.FIELD_ID))
                .first();
        pointer = lastEntry == null ? -1 : lastEntry.getLong(MongoDbDistributedConstants.FIELD_ID);

        return this;
    }

    @Override
    public boolean offer(E e) {

        if (e == null) {
            throw new NullPointerException();
        }

        // Insert a new document with the order as the maximum + 1
        RetryTools.retryBetween(3, 200, () -> {
            mongoClient.startSession().withTransaction(() -> {
                var nextEntry = mongoCollection.find()
                        .projection(new Document().append(MongoDbDistributedConstants.FIELD_ID, 1))
                        .sort(Sorts.descending(MongoDbDistributedConstants.FIELD_ID))
                        .first();
                long order = nextEntry == null ? 0 : nextEntry.getLong(MongoDbDistributedConstants.FIELD_ID) + 1;

                String jsonValue = JsonTools.compactPrintWithoutNulls(e);
                mongoCollection.insertOne(new Document()
                        .append(MongoDbDistributedConstants.FIELD_ID, order)
                        .append(MongoDbDistributedConstants.FIELD_JSON_VALUE, jsonValue)
                        .append(MongoDbDistributedConstants.FIELD_HASH_JSON_VALUE, HashSha1.hashString(jsonValue))
                        .append(MongoDbDistributedConstants.FIELD_CREATED_AT, new Date())
                );
                return null;
            });
        });

        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {

        BufferBatchesTools.<E>autoClose(10, items -> {

            // Insert a new document with the order as the maximum + 1
            RetryTools.retryBetween(3, 200, () ->
                    mongoClient.startSession().withTransaction(() -> {
                        var nextEntry = mongoCollection.find()
                                .projection(new Document().append(MongoDbDistributedConstants.FIELD_ID, 1))
                                .sort(Sorts.descending(MongoDbDistributedConstants.FIELD_ID))
                                .first();
                        long order = nextEntry == null ? 0 : nextEntry.getLong(MongoDbDistributedConstants.FIELD_ID) + 1;

                        List<Document> documents = new ArrayList<>();
                        for (var e : items) {
                            if (e == null) {
                                throw new NullPointerException();
                            }
                            String jsonValue = JsonTools.compactPrintWithoutNulls(e);
                            documents.add(new Document()
                                    .append(MongoDbDistributedConstants.FIELD_ID, order++)
                                    .append(MongoDbDistributedConstants.FIELD_JSON_VALUE, jsonValue)
                                    .append(MongoDbDistributedConstants.FIELD_HASH_JSON_VALUE, HashSha1.hashString(jsonValue))
                                    .append(MongoDbDistributedConstants.FIELD_CREATED_AT, new Date())
                            );
                        }

                        mongoCollection.insertMany(documents);
                        return null;
                    })
            );

        }, bufferBatchesTools -> {
            bufferBatchesTools.add((List<E>) c);
        });

        return true;
    }

    @Override
    public E peek() {
        var result = mongoCollection.find(
                        new Document()
                                .append(MongoDbDistributedConstants.FIELD_ID, new Document().append("$gt", pointer))
                )
                .sort(Sorts.ascending(MongoDbDistributedConstants.FIELD_ID))
                .first();
        if (result == null) {
            return null;
        } else {
            return JsonTools.readFromString(result.getString(MongoDbDistributedConstants.FIELD_JSON_VALUE), entityType);
        }
    }

    @Override
    public boolean contains(Object o) {
        if (o == null) {
            return false;
        }

        var hashJsonValue = HashSha1.hashString(JsonTools.compactPrintWithoutNulls(o));
        return mongoCollection.find(new Document()
                .append(MongoDbDistributedConstants.FIELD_ID, new Document().append("$gt", pointer))
                .append(MongoDbDistributedConstants.FIELD_HASH_JSON_VALUE, hashJsonValue)
        ).first() != null;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        if (c == null) {
            throw new NullPointerException();
        }

        // Get all the hashJsonValues
        Set<String> allHashJsonValues = new HashSet<>();
        c.forEach(item -> {
            if (item == null) {
                throw new NullPointerException();
            }
            allHashJsonValues.add(HashSha1.hashString(JsonTools.compactPrintWithoutNulls(item)));
        });

        // Get the count of unique hashJsonValues in that list
        long count = StreamSupport.stream(mongoCollection.find(new Document()
                                .append(MongoDbDistributedConstants.FIELD_ID, new Document().append("$gt", pointer))
                                .append(MongoDbDistributedConstants.FIELD_HASH_JSON_VALUE, new Document().append("$in", allHashJsonValues))
                        )
                        .projection(new Document().append(MongoDbDistributedConstants.FIELD_HASH_JSON_VALUE, 1))
                        .spliterator(), false)
                .map(document -> document.getString(MongoDbDistributedConstants.FIELD_HASH_JSON_VALUE))
                .sorted().distinct()
                .count();

        return count == allHashJsonValues.size();
    }

    @Override
    public Iterator<E> iterator() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Object[] toArray() {
        return StreamSupport.stream(mongoCollection.find(
                                new Document()
                                        .append(MongoDbDistributedConstants.FIELD_ID, new Document().append("$gt", pointer))
                        )
                        .sort(Sorts.ascending(MongoDbDistributedConstants.FIELD_ID))
                        .projection(new Document().append(MongoDbDistributedConstants.FIELD_JSON_VALUE, 1))
                        .spliterator(), false)
                .map(document -> JsonTools.readFromString(document.getString(MongoDbDistributedConstants.FIELD_JSON_VALUE), entityType))
                .toArray();
    }

    @Override
    public synchronized E poll() {
        var entry = mongoCollection.find(
                        new Document()
                                .append(MongoDbDistributedConstants.FIELD_ID, new Document().append("$gt", pointer))
                )
                .first();
        if (entry == null) {
            return null;
        }
        pointer = entry.getLong(MongoDbDistributedConstants.FIELD_ID);

        return JsonTools.readFromString(entry.getString(MongoDbDistributedConstants.FIELD_JSON_VALUE), entityType);
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {

        E value = poll();
        if (value != null) {
            return value;
        }

        long waitUntil = System.currentTimeMillis() + unit.toMillis(timeout);
        if (waitUntil < 0) {
            waitUntil = Long.MAX_VALUE;
        }
        while (value == null && System.currentTimeMillis() < waitUntil) {
            waitForChange(waitUntil - System.currentTimeMillis());
            value = poll();
        }
        return value;
    }

    private void waitForChange(long timeInMs) throws InterruptedException {
        synchronized (this) {
            if (mongoDbChangeStreamWaitAnyChange == null) {
                mongoDbChangeStreamWaitAnyChange = new MongoDbChangeStreamWaitAnyChange(mongoCollection, stopChangeStreamAfterNoThreadWaitedInMs, "insert");
            }
        }
        mongoDbChangeStreamWaitAnyChange.waitForChange(timeInMs);
    }

    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        throw new UnsupportedOperationException("Cannot delete elements");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Cannot delete elements");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Cannot delete elements");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Cannot delete elements");
    }

    @Override
    public int size() {
        long count = mongoCollection.countDocuments(new Document()
                .append(MongoDbDistributedConstants.FIELD_ID, new Document().append("$gt", pointer))
        );
        if (count > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) count;
    }

    // ---== Methods reusing the others at the top  ==---

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) {
        return offer(e);
    }

    @Override
    public boolean add(E e) {
        return offer(e);
    }

    @Override
    public void put(E e) {
        offer(e);
    }

    @Override
    public E remove() {
        E e = poll();
        if (e == null) {
            throw new NoSuchElementException();
        }
        return e;
    }

    @Override
    public E take() throws InterruptedException {
        return poll(Long.MAX_VALUE, TimeUnit.DAYS);
    }

    @Override
    public int remainingCapacity() {
        return Integer.MAX_VALUE;
    }

    @Override
    public E element() {
        E e = peek();
        if (e == null) {
            throw new NoSuchElementException();
        }
        return e;
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Cannot delete elements");
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        throw new UnsupportedOperationException("Cannot delete elements");
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }

}
