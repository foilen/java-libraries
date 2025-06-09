package com.foilen.smalltools.mongodb.distributed;

import com.foilen.smalltools.tools.JsonTools;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class MongoDbDequeIterator<E> implements Iterator<E> {

    private final Class<E> entityType;
    private final MongoCollection<Document> mongoCollection;
    private final boolean ascending;

    private Long lastId = null;
    private E next;
    private boolean completed;

    public MongoDbDequeIterator(Class<E> entityType, MongoCollection<Document> mongoCollection, boolean ascending) {
        this.entityType = entityType;
        this.mongoCollection = mongoCollection;
        this.ascending = ascending;
    }

    @Override
    public boolean hasNext() {
        loadNext();
        return !completed;
    }

    @Override
    public E next() {

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
        if (lastId != null) {
            query.append(MongoDbDistributedConstants.FIELD_ID, new Document().append(ascending ? "$gt" : "$lt", lastId));
        }

        var sort = new Document().append(MongoDbDistributedConstants.FIELD_ID, ascending ? 1 : -1);
        var result = mongoCollection.find(query)
                .sort(sort)
                .first();
        if (result == null) {
            completed = true;
            return;
        }

        lastId = result.getLong(MongoDbDistributedConstants.FIELD_ID);
        next = JsonTools.readFromString(result.getString(MongoDbDistributedConstants.FIELD_JSON_VALUE), entityType);
    }

    @Override
    public void remove() {
        if (lastId == null) {
            throw new IllegalStateException();
        }
        mongoCollection.deleteOne(new Document().append(MongoDbDistributedConstants.FIELD_ID, lastId));
    }
}
