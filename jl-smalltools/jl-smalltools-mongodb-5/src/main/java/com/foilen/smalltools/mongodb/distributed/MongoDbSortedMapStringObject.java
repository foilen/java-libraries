package com.foilen.smalltools.mongodb.distributed;

import com.foilen.smalltools.hash.HashSha1;
import com.foilen.smalltools.mongodb.MongoDbManageCollectionTools;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.BufferBatchesTools;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tuple.Tuple2;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * A distributed Map using MongoDB with keys that are strings and value that will be any json serializable type.
 * You can also use it as a cache by providing a maxDurationInSec.
 *
 * @param <V> the value type
 */
public class MongoDbSortedMapStringObject<V> extends AbstractBasics implements SortedMap<String, V> {

    private final Class<V> valueType;
    private final MongoCollection<Document> mongoCollection;

    /**
     * Create a new instance of the map.
     *
     * @param valueType       the value type
     * @param mongoClient     the mongo client
     * @param mongoCollection the mongo collection
     */
    public MongoDbSortedMapStringObject(Class<V> valueType, MongoClient mongoClient, MongoCollection<Document> mongoCollection) {
        this(valueType, mongoClient, mongoCollection, null);
    }

    /**
     * Create a new instance of the map.
     *
     * @param valueType        the value type
     * @param mongoClient      the mongo client
     * @param mongoCollection  the mongo collection
     * @param maxDurationInSec the maximum duration of an element in the cache (MongoDB can take up to 60 seconds to clean up after the expiration)
     */
    public MongoDbSortedMapStringObject(Class<V> valueType, MongoClient mongoClient, MongoCollection<Document> mongoCollection, Long maxDurationInSec) {
        this.valueType = valueType;
        this.mongoCollection = mongoCollection;

        // Collection
        MongoDbManageCollectionTools.addCollectionIfMissing(mongoClient, mongoCollection.getNamespace());

        // Indexes
        Map<String, Tuple2<Document, IndexOptions>> indexes = new HashMap<>();
        indexes.put("hashJsonValue_id", new Tuple2<>(
                new Document().append(MongoDbDistributedConstants.FIELD_HASH_JSON_VALUE, 1).append(MongoDbDistributedConstants.FIELD_ID, 1),
                new IndexOptions()
        ));
        if (maxDurationInSec != null) {
            indexes.put("createdAt_" + maxDurationInSec, new Tuple2<>(
                    new Document().append(MongoDbDistributedConstants.FIELD_CREATED_AT, 1),
                    new IndexOptions().expireAfter(maxDurationInSec, TimeUnit.SECONDS)
            ));
        }
        MongoDbManageCollectionTools.manageIndexes(mongoCollection, indexes);

    }

    @Override
    public boolean containsKey(Object key) {
        return mongoCollection.find(Filters.eq(MongoDbDistributedConstants.FIELD_ID, key)).first() != null;
    }

    @Override
    public boolean containsValue(Object value) {
        if (value == null) {
            return false;
        }

        var hashJsonValue = HashSha1.hashString(JsonTools.compactPrintWithoutNulls(value));
        return mongoCollection.find(new Document().append(MongoDbDistributedConstants.FIELD_HASH_JSON_VALUE, hashJsonValue)).first() != null;
    }

    @Override
    public V get(Object key) {
        Document document = mongoCollection.find(Filters.eq(MongoDbDistributedConstants.FIELD_ID, key)).first();
        if (document == null) {
            return null;
        }
        return JsonTools.readFromString(document.getString(MongoDbDistributedConstants.FIELD_JSON_VALUE), valueType);
    }

    @Override
    public V put(String key, V value) {
        // Prepare the document
        String jsonValue = JsonTools.compactPrintWithoutNulls(value);
        var hashJsonValue = HashSha1.hashString(jsonValue);
        var document = new Document()
                .append(MongoDbDistributedConstants.FIELD_ID, key)
                .append(MongoDbDistributedConstants.FIELD_JSON_VALUE, jsonValue)
                .append(MongoDbDistributedConstants.FIELD_HASH_JSON_VALUE, hashJsonValue)
                .append(MongoDbDistributedConstants.FIELD_CREATED_AT, new Date());

        // Save the document
        var previousDocument = mongoCollection.findOneAndReplace(Filters.eq(MongoDbDistributedConstants.FIELD_ID, key),
                document,
                new FindOneAndReplaceOptions().upsert(true)
        );

        // Return the previous value
        if (previousDocument == null) {
            return null;
        }
        return JsonTools.readFromString(previousDocument.getString(MongoDbDistributedConstants.FIELD_JSON_VALUE), valueType);
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> m) {

        BufferBatchesTools.<Entry<String, V>>autoClose(10, items -> {
            mongoCollection.insertMany(items.stream()
                    .map(entry -> {
                        String jsonValue = JsonTools.compactPrintWithoutNulls(entry.getValue());
                        var hashJsonValue = HashSha1.hashString(jsonValue);
                        return new Document()
                                .append(MongoDbDistributedConstants.FIELD_ID, entry.getKey())
                                .append(MongoDbDistributedConstants.FIELD_JSON_VALUE, jsonValue)
                                .append(MongoDbDistributedConstants.FIELD_HASH_JSON_VALUE, hashJsonValue)
                                .append(MongoDbDistributedConstants.FIELD_CREATED_AT, new Date());
                    })
                    .toList());
        }, bufferBatchesTools -> {
            m.entrySet().forEach(entry -> {
                bufferBatchesTools.add((Entry<String, V>) entry);
            });
        });

    }

    @Override
    public V remove(Object key) {
        Document document = mongoCollection.findOneAndDelete(Filters.eq(MongoDbDistributedConstants.FIELD_ID, key));
        if (document == null) {
            return null;
        }
        return JsonTools.readFromString(document.getString(MongoDbDistributedConstants.FIELD_JSON_VALUE), valueType);
    }

    @Override
    public void clear() {
        mongoCollection.deleteMany(Filters.empty());
    }

    /**
     * Like {@link #clear()} but returns true if the map was already empty.
     *
     * @return true if the map was already empty
     */
    public boolean clearAndTellIfWasEmpty() {
        var result = mongoCollection.deleteMany(Filters.empty());
        return result.getDeletedCount() != 0;
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
    public Comparator<? super String> comparator() {
        return null;
    }

    @Override
    public SortedMap<String, V> subMap(String fromKey, String toKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SortedMap<String, V> headMap(String toKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SortedMap<String, V> tailMap(String fromKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String firstKey() {
        Document first = mongoCollection.find()
                .sort(new Document().append(MongoDbDistributedConstants.FIELD_ID, 1))
                .first();
        if (first == null) {
            return null;
        }
        return first.getString(MongoDbDistributedConstants.FIELD_ID);
    }

    @Override
    public String lastKey() {
        Document last = mongoCollection.find()
                .sort(new Document().append(MongoDbDistributedConstants.FIELD_ID, -1))
                .first();
        if (last == null) {
            return null;
        }
        return last.getString(MongoDbDistributedConstants.FIELD_ID);
    }

    @Override
    public SortedSet<String> keySet() {
        return new MongoDbIdStringSortedSet(mongoCollection);
    }

    @Override
    public Collection<V> values() {
        return new MongoDbValueCollection(valueType, mongoCollection);
    }

    @Override
    public SortedSet<Entry<String, V>> entrySet() {
        return new MongoDbEntryStringObjectSortedSet<>(valueType, mongoCollection);
    }

}
