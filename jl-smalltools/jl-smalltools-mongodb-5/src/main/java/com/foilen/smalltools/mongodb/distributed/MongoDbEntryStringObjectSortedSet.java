/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.mongodb.distributed;

import com.foilen.smalltools.collection.ImmutableMapEntry;
import com.foilen.smalltools.hash.HashSha1;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tools.StringTools;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;

/**
 * A distributed Set using MongoDB of Map Entries with keys that are strings and value that will be any json serializable type.
 */
public class MongoDbEntryStringObjectSortedSet<V> implements SortedSet<Map.Entry<String, V>> {

    private final Class<V> valueType;
    private final MongoCollection<Document> mongoCollection;
    private final String fromId;
    private final String toId;

    public MongoDbEntryStringObjectSortedSet(Class<V> valueType, MongoCollection<Document> mongoCollection) {
        this(valueType, mongoCollection, null, null);
    }

    public MongoDbEntryStringObjectSortedSet(Class<V> valueType, MongoCollection<Document> mongoCollection, String fromId, String toId) {
        this.valueType = valueType;
        this.mongoCollection = mongoCollection;
        this.fromId = fromId;
        this.toId = toId;
    }

    @Override
    public Comparator<? super Map.Entry<String, V>> comparator() {
        return null;
    }

    @Override
    public Map.Entry<String, V> first() {

        var document = mongoCollection.find(getFilter())
                .sort(new Document(MongoDbDistributedConstants.FIELD_ID, 1))
                .limit(1)
                .first();
        if (document == null) {
            return null;
        }

        return new ImmutableMapEntry<>(
                document.getString(MongoDbDistributedConstants.FIELD_ID),
                JsonTools.readFromString(document.getString(MongoDbDistributedConstants.FIELD_JSON_VALUE), valueType)
        );

    }

    @Override
    public Map.Entry<String, V> last() {

        var document = mongoCollection.find(getFilter())
                .sort(new Document(MongoDbDistributedConstants.FIELD_ID, -1))
                .limit(1)
                .first();
        if (document == null) {
            return null;
        }

        return new ImmutableMapEntry<>(
                document.getString(MongoDbDistributedConstants.FIELD_ID),
                JsonTools.readFromString(document.getString(MongoDbDistributedConstants.FIELD_JSON_VALUE), valueType)
        );

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
        if (o instanceof Map.Entry value) {
            Object key = value.getKey();
            if (key instanceof String keyString) {
                if (fromId != null && keyString.compareTo(fromId) < 0)
                    return false;
                if (toId != null && keyString.compareTo(toId) >= 0)
                    return false;
            }

            return mongoCollection.find(Filters.and(
                    Filters.eq(MongoDbDistributedConstants.FIELD_ID, key),
                    Filters.eq(MongoDbDistributedConstants.FIELD_HASH_JSON_VALUE, HashSha1.hashString(JsonTools.compactPrintWithoutNulls(value.getValue())))
            )).first() != null;
        }
        return false;
    }

    @Override
    public boolean add(Map.Entry<String, V> s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends Map.Entry<String, V>> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof Map.Entry value) {
            Object key = value.getKey();
            if (key instanceof String keyString) {
                if (fromId != null && keyString.compareTo(fromId) < 0)
                    return false;
                if (toId != null && keyString.compareTo(toId) >= 0)
                    return false;
            }

            return mongoCollection.deleteOne(Filters.and(
                    Filters.eq(MongoDbDistributedConstants.FIELD_ID, key),
                    Filters.eq(MongoDbDistributedConstants.FIELD_HASH_JSON_VALUE, HashSha1.hashString(JsonTools.compactPrintWithoutNulls(value.getValue())))
            )).getDeletedCount() > 0;
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        if (c.isEmpty()) {
            return true;
        }

        return c.stream().allMatch(this::contains);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        if (c.isEmpty()) {
            return false;
        }

        AtomicBoolean removed = new AtomicBoolean(false);
        c.stream()
                .map(this::remove)
                .forEach(r -> {
                    if (r) removed.set(true);
                });

        return removed.get();
    }

    @Override
    public void clear() {
        mongoCollection.deleteMany(Filters.empty());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MongoDbEntryStringObjectSortedSet other) {
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
                .map(document -> new ImmutableMapEntry<>(
                        document.getString(MongoDbDistributedConstants.FIELD_ID),
                        JsonTools.readFromString(document.getString(MongoDbDistributedConstants.FIELD_JSON_VALUE), valueType)
                ))
                .forEach(entry -> sum.addAndGet(entry.hashCode()));
        return sum.get();
    }

    @Override
    public SortedSet<Map.Entry<String, V>> subSet(Map.Entry<String, V> fromElement, Map.Entry<String, V> toElement) {
        String from = this.fromId;
        String to = this.toId;
        if (fromElement != null) {
            // If from is null or fromElement is greater
            if (from == null || fromElement.getKey().compareTo(from) > 0) {
                from = fromElement.getKey();
            }
        }
        if (toElement != null) {
            // If to is null or toElement is smaller
            if (to == null || toElement.getKey().compareTo(to) < 0) {
                to = toElement.getKey();
            }
        }
        return new MongoDbEntryStringObjectSortedSet(valueType, mongoCollection, from, to);
    }

    @Override
    public SortedSet<Map.Entry<String, V>> headSet(Map.Entry<String, V> toElement) {
        return subSet(null, toElement);
    }

    @Override
    public SortedSet<Map.Entry<String, V>> tailSet(Map.Entry<String, V> fromElement) {
        return subSet(fromElement, null);
    }

    @Override
    public Iterator<Map.Entry<String, V>> iterator() {
        return new MongoDbEntryStringObjectSortedSetIterator(valueType, mongoCollection, fromId, toId);
    }

    @Override
    public Object[] toArray() {
        return StreamSupport.stream(mongoCollection.find()
                        .sort(Sorts.ascending(MongoDbDistributedConstants.FIELD_ID))
                        .spliterator(), false)
                .map(document -> new ImmutableMapEntry<>(
                        document.getString(MongoDbDistributedConstants.FIELD_ID),
                        JsonTools.readFromString(document.getString(MongoDbDistributedConstants.FIELD_JSON_VALUE), valueType)
                ))
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
        return "MongoDbEntryStringObjectSortedSet{" +
                "valueType=" + valueType +
                ", mongoCollection=" + mongoCollection.getNamespace() +
                ", fromId='" + fromId + '\'' +
                ", toId='" + toId + '\'' +
                '}';
    }
}
