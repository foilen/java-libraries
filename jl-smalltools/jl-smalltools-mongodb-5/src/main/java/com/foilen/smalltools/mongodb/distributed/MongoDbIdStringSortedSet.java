/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.mongodb.distributed;

import com.foilen.smalltools.tools.StringTools;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;

/**
 * A distributed Set of the "_id" field using MongoDB. The "_id" field is a string.
 */
public class MongoDbIdStringSortedSet implements SortedSet<String> {

    private final MongoCollection<Document> mongoCollection;
    private final String fromId;
    private final String toId;

    public MongoDbIdStringSortedSet(MongoCollection<Document> mongoCollection) {
        this(mongoCollection, null, null);
    }

    public MongoDbIdStringSortedSet(MongoCollection<Document> mongoCollection, String fromId, String toId) {
        this.mongoCollection = mongoCollection;
        this.fromId = fromId;
        this.toId = toId;
    }

    @Override
    public Comparator<? super String> comparator() {
        return null;
    }

    @Override
    public String first() {

        var document = mongoCollection.find(getFilter())
                .sort(new Document(MongoDbDistributedConstants.FIELD_ID, 1))
                .projection(new Document(MongoDbDistributedConstants.FIELD_ID, 1))
                .limit(1)
                .first();
        if (document == null) {
            return null;
        }

        return document.getString(MongoDbDistributedConstants.FIELD_ID);

    }

    @Override
    public String last() {

        var document = mongoCollection.find(getFilter())
                .sort(new Document(MongoDbDistributedConstants.FIELD_ID, -1))
                .projection(new Document(MongoDbDistributedConstants.FIELD_ID, 1))
                .limit(1)
                .first();
        if (document == null) {
            return null;
        }

        return document.getString(MongoDbDistributedConstants.FIELD_ID);

    }

    @Override
    public int size() {

        long count;

        if (fromId != null || toId != null) {
            // Subset
            count = mongoCollection.countDocuments(getFilter());
        } else {
            // All
            count = mongoCollection.estimatedDocumentCount();
        }

        if (count > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) count;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof String value) {
            if (fromId != null && value.compareTo(fromId) < 0)
                return false;
            if (toId != null && value.compareTo(toId) >= 0)
                return false;

            return mongoCollection.find(Filters.eq(MongoDbDistributedConstants.FIELD_ID, o)).first() != null;
        }
        return false;
    }

    @Override
    public boolean add(String value) {
        if (fromId != null && value.compareTo(fromId) < 0)
            throw new IllegalArgumentException("Value is smaller than the fromId");

        if (toId != null && value.compareTo(toId) >= 0)
            throw new IllegalArgumentException("Value is greater or equal to the toId");

        var result = mongoCollection.updateOne(
                Filters.eq(MongoDbDistributedConstants.FIELD_ID, value),
                new Document("$setOnInsert", new Document(MongoDbDistributedConstants.FIELD_ID, value)),
                new UpdateOptions().upsert(true)
        );
        return result.getMatchedCount() == 0;
    }

    @Override
    public boolean addAll(Collection<? extends String> value) {
        boolean anyAdded = false;

        for (String v : value) {
            if (add(v)) {
                anyAdded = true;
            }
        }
        return anyAdded;
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof String value) {
            if (fromId != null && value.compareTo(fromId) < 0)
                return false;
            if (toId != null && value.compareTo(toId) >= 0)
                return false;

            return mongoCollection.deleteOne(Filters.eq(MongoDbDistributedConstants.FIELD_ID, o)).getDeletedCount() > 0;
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        if (c.isEmpty()) {
            return true;
        }

        // Get as unique
        Set<?> uniqueIds = new HashSet<>(c);

        return mongoCollection.countDocuments(
                Filters.and(
                        getFilter(),
                        Filters.in(MongoDbDistributedConstants.FIELD_ID, uniqueIds)
                )
        ) == uniqueIds.size();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return mongoCollection.deleteMany(
                Filters.and(
                        getFilter(),
                        Filters.nin(MongoDbDistributedConstants.FIELD_ID, c)
                )
        ).getDeletedCount() > 0;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return mongoCollection.deleteMany(
                Filters.and(
                        getFilter(),
                        Filters.in(MongoDbDistributedConstants.FIELD_ID, c)
                )
        ).getDeletedCount() > 0;
    }

    @Override
    public void clear() {
        mongoCollection.deleteMany(Filters.empty());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MongoDbIdStringSortedSet other) {
            if (mongoCollection.getNamespace().equals(other.mongoCollection.getNamespace())
                    && StringTools.safeEquals(fromId, other.fromId)
                    && StringTools.safeEquals(toId, other.toId)) {
                return true;
            }
        }

        if (o instanceof Set<?> other) {
            return size() == other.size() && containsAll(other);
        }

        return false;
    }

    @Override
    public int hashCode() {
        // Sum of all hash codes
        AtomicInteger sum = new AtomicInteger();
        mongoCollection.find(getFilter())
                .sort(new Document(MongoDbDistributedConstants.FIELD_ID, 1))
                .projection(new Document(MongoDbDistributedConstants.FIELD_ID, 1))
                .forEach(document -> sum.addAndGet(document.getString(MongoDbDistributedConstants.FIELD_ID).hashCode()));
        return sum.get();
    }

    @Override
    public SortedSet<String> subSet(String fromElement, String toElement) {
        String from = this.fromId;
        String to = this.toId;
        if (fromElement != null) {
            // If from is null or fromElement is greater
            if (from == null || fromElement.compareTo(from) > 0) {
                from = fromElement;
            }
        }
        if (toElement != null) {
            // If to is null or toElement is smaller
            if (to == null || toElement.compareTo(to) < 0) {
                to = toElement;
            }
        }
        return new MongoDbIdStringSortedSet(mongoCollection, from, to);
    }

    @Override
    public SortedSet<String> headSet(String toElement) {
        return subSet(null, toElement);
    }

    @Override
    public SortedSet<String> tailSet(String fromElement) {
        return subSet(fromElement, null);
    }

    @Override
    public Iterator<String> iterator() {
        return new MongoDbIdStringSortedSetIterator(mongoCollection, fromId, toId);
    }

    @Override
    public Object[] toArray() {
        return StreamSupport.stream(mongoCollection.find()
                        .sort(Sorts.ascending(MongoDbDistributedConstants.FIELD_ID))
                        .projection(new Document(MongoDbDistributedConstants.FIELD_ID, 1))
                        .spliterator(), false)
                .map(document -> document.getString(MongoDbDistributedConstants.FIELD_ID))
                .toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }

    private Bson getFilter() {
        if (fromId != null && toId != null) {
            // Subset
            return Filters.and(
                    Filters.gte(MongoDbDistributedConstants.FIELD_ID, fromId),
                    Filters.lt(MongoDbDistributedConstants.FIELD_ID, toId)
            );
        } else if (fromId != null) {
            // From
            return Filters.gte(MongoDbDistributedConstants.FIELD_ID, fromId);
        } else if (toId != null) {
            // To
            return Filters.lt(MongoDbDistributedConstants.FIELD_ID, toId);
        } else {
            // All
            return new Document();
        }
    }

    @Override
    public String toString() {
        return "MongoDbIdStringSortedSet{" +
                "mongoCollection=" + mongoCollection.getNamespace() +
                ", fromId='" + fromId + '\'' +
                ", toId='" + toId + '\'' +
                '}';
    }
}
