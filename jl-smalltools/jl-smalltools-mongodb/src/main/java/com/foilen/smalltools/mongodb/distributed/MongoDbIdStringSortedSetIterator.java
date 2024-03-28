/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.mongodb.distributed;

import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class MongoDbIdStringSortedSetIterator implements Iterator<String> {

    private final MongoCollection<Document> mongoCollection;
    private final String fromId;
    private final String toId;

    private String lastId;
    private String next;
    private boolean completed;

    public MongoDbIdStringSortedSetIterator(MongoCollection<Document> mongoCollection, String fromId, String toId) {
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
    public String next() {

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
        next = lastId;
    }

    @Override
    public void remove() {
        if (lastId == null || completed) {
            throw new IllegalStateException();
        }
        mongoCollection.deleteOne(new Document().append(MongoDbDistributedConstants.FIELD_ID, lastId));
    }

}
