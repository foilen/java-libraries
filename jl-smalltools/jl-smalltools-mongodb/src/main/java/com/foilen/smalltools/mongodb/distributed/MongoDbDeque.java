/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
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
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndDeleteOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Sorts;
import org.bson.Document;

import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;

/**
 * A distributed deque/queue using MongoDB.
 *
 * @param <E> the type of elements in this queue
 */
public class MongoDbDeque<E> extends AbstractBasics implements BlockingDeque<E> {

    private final Class<E> entityType;
    private final MongoClient mongoClient;
    private final MongoCollection<Document> mongoCollection;
    private final long stopChangeStreamAfterNoThreadWaitedInMs;

    private MongoDbChangeStreamWaitAnyChange mongoDbChangeStreamWaitAnyChange;

    public MongoDbDeque(Class<E> entityType, MongoClient mongoClient, MongoCollection<Document> mongoCollection) {
        this(entityType, mongoClient, mongoCollection, 10 * 60000);
    }

    public MongoDbDeque(Class<E> entityType, MongoClient mongoClient, MongoCollection<Document> mongoCollection, long stopChangeStreamAfterNoThreadWaitedInMs) {
        this.entityType = entityType;
        this.mongoClient = mongoClient;
        this.mongoCollection = mongoCollection;
        this.stopChangeStreamAfterNoThreadWaitedInMs = stopChangeStreamAfterNoThreadWaitedInMs;

        MongoDbManageCollectionTools.addCollectionIfMissing(mongoClient, mongoCollection.getNamespace());
        MongoDbManageCollectionTools.manageIndexes(mongoCollection, Map.of(
                "hashJsonValue_id", new Tuple2<>(
                        new Document().append(MongoDbDistributedConstants.FIELD_HASH_JSON_VALUE, 1).append(MongoDbDistributedConstants.FIELD_ID, 1),
                        new IndexOptions()
                )
        ));
    }

    @Override
    public boolean offerFirst(E e) {

        if (e == null) {
            throw new NullPointerException();
        }

        // Insert a new document with the order as the minimum - 1
        RetryTools.retryBetween(3, 200, () ->
                mongoClient.startSession().withTransaction(() -> {
                    var nextEntry = mongoCollection.find()
                            .sort(Sorts.ascending(MongoDbDistributedConstants.FIELD_ID))
                            .projection(new Document().append(MongoDbDistributedConstants.FIELD_ID, 1))
                            .first();
                    long order = nextEntry == null ? 0 : nextEntry.getLong(MongoDbDistributedConstants.FIELD_ID) - 1;

                    String jsonValue = JsonTools.compactPrintWithoutNulls(e);
                    mongoCollection.insertOne(new Document()
                            .append(MongoDbDistributedConstants.FIELD_ID, order)
                            .append(MongoDbDistributedConstants.FIELD_JSON_VALUE, jsonValue)
                            .append(MongoDbDistributedConstants.FIELD_HASH_JSON_VALUE, HashSha1.hashString(jsonValue))
                    );
                    return null;
                })
        );

        return true;
    }

    @Override
    public boolean offerLast(E e) {

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
    public E peekFirst() {
        var result = mongoCollection.find()
                .sort(Sorts.ascending(MongoDbDistributedConstants.FIELD_ID))
                .first();
        if (result == null) {
            return null;
        } else {
            return JsonTools.readFromString(result.getString(MongoDbDistributedConstants.FIELD_JSON_VALUE), entityType);
        }
    }

    @Override
    public E peekLast() {
        var result = mongoCollection.find()
                .sort(Sorts.descending(MongoDbDistributedConstants.FIELD_ID))
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
        return mongoCollection.find(new Document().append(MongoDbDistributedConstants.FIELD_HASH_JSON_VALUE, hashJsonValue)).first() != null;
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
                                .append(MongoDbDistributedConstants.FIELD_HASH_JSON_VALUE, new Document().append("$in", allHashJsonValues)))
                        .projection(new Document().append(MongoDbDistributedConstants.FIELD_HASH_JSON_VALUE, 1))
                        .spliterator(), false)
                .map(document -> document.getString(MongoDbDistributedConstants.FIELD_HASH_JSON_VALUE))
                .sorted().distinct()
                .count();

        return count == allHashJsonValues.size();
    }

    @Override
    public Iterator<E> iterator() {
        return new MongoDbDequeIterator<>(entityType, mongoCollection, true);
    }

    @Override
    public Iterator<E> descendingIterator() {
        return new MongoDbDequeIterator<>(entityType, mongoCollection, false);
    }

    @Override
    public Object[] toArray() {
        return StreamSupport.stream(mongoCollection.find()
                        .sort(Sorts.ascending(MongoDbDistributedConstants.FIELD_ID))
                        .projection(new Document().append(MongoDbDistributedConstants.FIELD_JSON_VALUE, 1))
                        .spliterator(), false)
                .map(document -> JsonTools.readFromString(document.getString(MongoDbDistributedConstants.FIELD_JSON_VALUE), entityType))
                .toArray();
    }

    @Override
    public E pollFirst() {
        var entry = mongoCollection.findOneAndDelete(
                new Document(),
                new FindOneAndDeleteOptions().sort(Sorts.ascending(MongoDbDistributedConstants.FIELD_ID))
        );
        if (entry == null) {
            return null;
        }

        return JsonTools.readFromString(entry.getString(MongoDbDistributedConstants.FIELD_JSON_VALUE), entityType);
    }

    @Override
    public E pollFirst(long timeout, TimeUnit unit) throws InterruptedException {

        E value = pollFirst();
        if (value != null) {
            return value;
        }

        long waitUntil = System.currentTimeMillis() + unit.toMillis(timeout);
        if (waitUntil < 0) {
            waitUntil = Long.MAX_VALUE;
        }
        while (value == null && System.currentTimeMillis() < waitUntil) {
            waitForChange(waitUntil - System.currentTimeMillis());
            value = pollFirst();
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
    public E pollLast() {
        var entry = mongoCollection.findOneAndDelete(
                new Document(),
                new FindOneAndDeleteOptions().sort(Sorts.descending(MongoDbDistributedConstants.FIELD_ID))
        );
        if (entry == null) {
            return null;
        }

        return JsonTools.readFromString(entry.getString(MongoDbDistributedConstants.FIELD_JSON_VALUE), entityType);
    }

    @Override
    public E pollLast(long timeout, TimeUnit unit) throws InterruptedException {

        E value = pollLast();
        if (value != null) {
            return value;
        }

        long waitUntil = System.currentTimeMillis() + unit.toMillis(timeout);
        if (waitUntil < 0) {
            waitUntil = Long.MAX_VALUE;
        }
        while (value == null && System.currentTimeMillis() < waitUntil) {
            waitForChange(waitUntil - System.currentTimeMillis());
            value = pollLast();
        }
        return value;
    }

    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        if (c == null)
            throw new NullPointerException();
        if (c == this)
            throw new IllegalArgumentException("Cannot drain to itself");
        if (maxElements < 0)
            throw new IllegalArgumentException("maxElements should be >= 0");

        AtomicInteger count = new AtomicInteger(0);
        AtomicInteger left = new AtomicInteger(maxElements);
        while (left.get() > 0) {
            mongoClient.startSession().withTransaction(() -> {
                int toGet = Math.min(left.get(), 10);
                List<Long> idsToDelete = new ArrayList<>(10);
                mongoCollection.find()
                        .sort(Sorts.ascending(MongoDbDistributedConstants.FIELD_ID))
                        .limit(toGet)
                        .forEach(document -> {
                            c.add(JsonTools.readFromString(document.getString(MongoDbDistributedConstants.FIELD_JSON_VALUE), entityType));
                            idsToDelete.add(document.getLong(MongoDbDistributedConstants.FIELD_ID));
                        });
                if (idsToDelete.isEmpty()) {
                    left.set(0);
                    return null;
                }
                mongoCollection.deleteMany(new Document().append(MongoDbDistributedConstants.FIELD_ID, new Document().append("$in", idsToDelete)));

                count.addAndGet(idsToDelete.size());
                left.addAndGet(-idsToDelete.size());

                return null;
            });
        }

        return count.get();
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        if (o == null) {
            return false;
        }

        var hashJsonValue = HashSha1.hashString(JsonTools.compactPrintWithoutNulls(o));
        return mongoClient.startSession().withTransaction(() -> {
            var entry = mongoCollection.findOneAndDelete(
                    new Document().append(MongoDbDistributedConstants.FIELD_HASH_JSON_VALUE, hashJsonValue),
                    new FindOneAndDeleteOptions().sort(Sorts.ascending(MongoDbDistributedConstants.FIELD_ID))
            );
            return entry != null;
        });
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        if (o == null) {
            return false;
        }

        var hashJsonValue = HashSha1.hashString(JsonTools.compactPrintWithoutNulls(o));
        return mongoClient.startSession().withTransaction(() -> {
            var entry = mongoCollection.findOneAndDelete(
                    new Document().append(MongoDbDistributedConstants.FIELD_HASH_JSON_VALUE, hashJsonValue),
                    new FindOneAndDeleteOptions().sort(Sorts.descending(MongoDbDistributedConstants.FIELD_ID))
            );
            return entry != null;
        });
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        if (c == null) {
            throw new NullPointerException();
        }

        AtomicBoolean found = new AtomicBoolean(false);
        BufferBatchesTools.<String>autoClose(10, hashJsonValues -> {
            // Find all the entries with the hashJsonValue
            var entries = mongoCollection.find(new Document().append(MongoDbDistributedConstants.FIELD_HASH_JSON_VALUE, new Document().append("$in", hashJsonValues)));

            // List their ids
            var idsToDelete = new ArrayList<Long>();
            entries.forEach(document -> {
                idsToDelete.add(document.getLong(MongoDbDistributedConstants.FIELD_ID));
                found.set(true);
            });

            // Delete them
            if (!idsToDelete.isEmpty()) {
                mongoCollection.deleteMany(new Document().append(MongoDbDistributedConstants.FIELD_ID, new Document().append("$in", idsToDelete)));
                found.set(true);
            }
        }, bufferBatchesTools -> {
            c.forEach(item -> {
                var hashJsonValue = HashSha1.hashString(JsonTools.compactPrintWithoutNulls(item));
                bufferBatchesTools.add(hashJsonValue);
            });
        });

        return found.get();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
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

        // Delete all the entries that are not in the list
        var result = mongoCollection.deleteMany(new Document().append(MongoDbDistributedConstants.FIELD_HASH_JSON_VALUE, new Document().append("$nin", allHashJsonValues)));

        // Return true if there were deleted entries
        return result.getDeletedCount() > 0;
    }

    @Override
    public void clear() {
        mongoCollection.deleteMany(Filters.empty());
    }

    @Override
    public int size() {
        long count = mongoCollection.estimatedDocumentCount();
        if (count > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) count;
    }

    // ---== Methods reusing the others at the top  ==---

    @Override
    public void addFirst(E e) {
        offerFirst(e);
    }

    @Override
    public void addLast(E e) {
        offerLast(e);
    }


    @Override
    public void putFirst(E e) {
        offerFirst(e);
    }

    @Override
    public void putLast(E e) {
        offerLast(e);
    }

    @Override
    public boolean offerFirst(E e, long timeout, TimeUnit unit) {
        return offerFirst(e);
    }

    @Override
    public boolean offerLast(E e, long timeout, TimeUnit unit) {
        return offerLast(e);
    }

    @Override
    public E takeFirst() throws InterruptedException {
        return pollFirst(Long.MAX_VALUE, TimeUnit.DAYS);
    }

    @Override
    public E takeLast() throws InterruptedException {
        return pollLast(Long.MAX_VALUE, TimeUnit.DAYS);
    }


    @Override
    public E removeFirst() {
        var entry = pollFirst();
        if (entry == null) {
            throw new NoSuchElementException();
        }
        return entry;
    }

    @Override
    public E removeLast() {
        var entry = pollLast();
        if (entry == null) {
            throw new NoSuchElementException();
        }
        return entry;
    }


    @Override
    public E getFirst() {
        E value = peekFirst();
        if (value == null) {
            throw new NoSuchElementException();
        }

        return value;
    }

    @Override
    public E getLast() {
        E value = peekLast();
        if (value == null) {
            throw new NoSuchElementException();
        }

        return value;
    }

    @Override
    public boolean add(E e) {
        return offerLast(e);
    }

    @Override
    public boolean offer(E e) {
        return offerLast(e);
    }

    @Override
    public void put(E e) {
        offerLast(e);
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) {
        return offerLast(e);
    }

    @Override
    public E remove() {
        return removeFirst();
    }

    @Override
    public E poll() {
        return pollFirst();
    }

    @Override
    public E take() throws InterruptedException {
        return takeFirst();
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        return pollFirst(timeout, unit);
    }

    @Override
    public int remainingCapacity() {
        return Integer.MAX_VALUE;
    }

    @Override
    public E element() {
        return getFirst();
    }

    @Override
    public E peek() {
        return peekFirst();
    }

    @Override
    public void push(E e) {
        offerFirst(e);
    }

    @Override
    public E pop() {
        return removeFirst();
    }

    @Override
    public boolean remove(Object o) {
        return removeFirstOccurrence(o);
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        return drainTo(c, Integer.MAX_VALUE);
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
