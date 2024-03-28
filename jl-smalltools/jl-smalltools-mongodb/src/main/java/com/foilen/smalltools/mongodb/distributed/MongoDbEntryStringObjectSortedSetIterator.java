/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.mongodb.distributed;

import com.foilen.smalltools.collection.ImmutableMapEntry;
import com.foilen.smalltools.tools.JsonTools;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public class MongoDbEntryStringObjectSortedSetIterator<V> implements Iterator<Map.Entry<String, V>> {

    private final Class<V> valueType;
    private final MongoCollection<Document> mongoCollection;
    private final String fromId;
    private final String toId;

    private String lastId;
    private Map.Entry<String, V> next;
    private boolean completed;

    public MongoDbEntryStringObjectSortedSetIterator(Class<V> valueType, MongoCollection<Document> mongoCollection, String fromId, String toId) {
        this.valueType = valueType;
        this.mongoCollection = mongoCollection;
        this.fromId = fromId;
        this.toId = toId;
    }

    @Override
    public boolean hasNext() {
        if (next != null) {
            return true;
        }
        loadNext();
        return !completed;
    }

    @Override
    public Map.Entry<String, V> next() {

        if (next != null) {
            var toReturn = next;
            next = null;
            return toReturn;
        }

        loadNext();
        if (completed) {
            throw new NoSuchElementException();
        }
        var toReturn = next;
        next = null;
        return toReturn;
    }

    private void loadNext() {
        next = null;

        if (completed) {
            return;
        }

        var query = new Document();
        if (fromId != null) {
            query.append(MongoDbDistributedConstants.FIELD_ID, new Document().append("$gte", fromId));
        }
        if (lastId != null) {
            query.append(MongoDbDistributedConstants.FIELD_ID, new Document().append("$gt", lastId));
        }

        var sort = new Document().append(MongoDbDistributedConstants.FIELD_ID, 1);
        var result = mongoCollection.find(query)
                .sort(sort)
                .first();
        if (result == null) {
            completed = true;
            return;
        }

        lastId = result.getString(MongoDbDistributedConstants.FIELD_ID);
        if (toId != null && toId.compareTo(lastId) <= 0) {
            lastId = null;
            completed = true;
            return;
        }
        var value = JsonTools.readFromString(result.getString(MongoDbDistributedConstants.FIELD_JSON_VALUE), valueType);
        next = new ImmutableMapEntry<>(lastId, value);
    }

    @Override
    public void remove() {
        if (lastId == null || completed) {
            throw new IllegalStateException();
        }
        mongoCollection.deleteOne(new Document().append(MongoDbDistributedConstants.FIELD_ID, lastId));
    }

}
