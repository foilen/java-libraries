/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2025 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.mongodb.distributed;

import com.foilen.smalltools.hash.HashSha1;
import com.foilen.smalltools.tools.JsonTools;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MongoDbValueCollection<V> implements Collection<V> {

    private final Class<V> valueType;
    private final MongoCollection<Document> mongoCollection;

    public MongoDbValueCollection(Class<V> valueType, MongoCollection<Document> mongoCollection) {
        this.valueType = valueType;
        this.mongoCollection = mongoCollection;
    }

    @Override
    public int size() {
        long count = mongoCollection.estimatedDocumentCount();
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
    public boolean contains(Object value) {
        if (value == null) {
            return false;
        }

        var hashJsonValue = HashSha1.hashString(JsonTools.compactPrintWithoutNulls(value));
        return mongoCollection.find(new Document().append(MongoDbDistributedConstants.FIELD_HASH_JSON_VALUE, hashJsonValue)).first() != null;
    }

    @Override
    public Iterator<V> iterator() {
        return new MongoDbValueCollectionIterator<>(valueType, mongoCollection);
    }

    @Override
    public Object[] toArray() {
        return StreamSupport.stream(mongoCollection.find()
                        .sort(Sorts.ascending(MongoDbDistributedConstants.FIELD_ID))
                        .spliterator(), false)
                .map(document -> JsonTools.readFromString(document.getString(MongoDbDistributedConstants.FIELD_JSON_VALUE), valueType))
                .toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(V v) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object value) {
        if (value == null) {
            return false;
        }

        var hashJsonValue = HashSha1.hashString(JsonTools.compactPrintWithoutNulls(value));
        return mongoCollection.deleteOne(new Document().append(MongoDbDistributedConstants.FIELD_HASH_JSON_VALUE, hashJsonValue)).getDeletedCount() > 0;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        if (c.isEmpty()) {
            return true;
        }

        var hashJsonValues = c.stream()
                .map(JsonTools::compactPrintWithoutNulls)
                .map(HashSha1::hashString)
                .collect(Collectors.toSet());
        return mongoCollection.countDocuments(
                Filters.in(MongoDbDistributedConstants.FIELD_HASH_JSON_VALUE, hashJsonValues)
        ) == hashJsonValues.size();
    }

    @Override
    public boolean addAll(Collection<? extends V> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        if (c.isEmpty()) {
            return false;
        }

        var hashJsonValues = c.stream()
                .map(JsonTools::compactPrintWithoutNulls)
                .map(HashSha1::hashString)
                .sorted().distinct()
                .toList();
        return mongoCollection.deleteMany(
                Filters.in(MongoDbDistributedConstants.FIELD_HASH_JSON_VALUE, hashJsonValues)
        ).getDeletedCount() > 0;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        if (c.isEmpty()) {
            return mongoCollection.deleteMany(Filters.empty()).getDeletedCount() > 0;
        }

        var hashJsonValues = c.stream()
                .map(JsonTools::compactPrintWithoutNulls)
                .map(HashSha1::hashString)
                .sorted().distinct()
                .toList();
        return mongoCollection.deleteMany(
                Filters.nin(MongoDbDistributedConstants.FIELD_HASH_JSON_VALUE, hashJsonValues)
        ).getDeletedCount() > 0;
    }

    @Override
    public void clear() {
        mongoCollection.deleteMany(Filters.empty());
    }
}
