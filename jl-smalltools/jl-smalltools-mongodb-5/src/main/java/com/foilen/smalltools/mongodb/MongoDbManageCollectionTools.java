/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.mongodb;

import com.foilen.smalltools.tools.SpaceConverterTools;
import com.foilen.smalltools.tuple.Tuple2;
import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MongoDbManageCollectionTools {

    private final static Logger logger = LoggerFactory.getLogger(MongoDbManageCollectionTools.class);

    /**
     * Create the collection if it does not exist.
     *
     * @param mongoClient the mongo client
     * @param namespace   the namespace
     */
    public static void addCollectionIfMissing(MongoClient mongoClient, MongoNamespace namespace) {
        var mongoDatabase = mongoClient.getDatabase(namespace.getDatabaseName());

        if (!mongoDatabase.listCollectionNames().into(new ArrayList<>()).contains(namespace.getCollectionName())) {
            logger.info("Creating collection {}", namespace.getCollectionName());
            mongoDatabase.createCollection(namespace.getCollectionName());
        }
    }

    /**
     * Create the collection if it does not exist or the capped size is incorrect.
     *
     * @param mongoClient              the mongo client
     * @param namespace                the namespace
     * @param maxCollectionSizeInBytes the max collection size in bytes
     */
    public static void addCollectionIfMissing(MongoClient mongoClient, MongoNamespace namespace, long maxCollectionSizeInBytes) {
        var mongoDatabase = mongoClient.getDatabase(namespace.getDatabaseName());

        // Check the capped size
        Document collectionInfo = mongoDatabase.runCommand(new Document("collStats", namespace.getCollectionName()));
        if (collectionInfo.containsKey("maxSize")) {
            long currentMaxCollectionSizeInBytes = collectionInfo.getLong("maxSize");
            if (currentMaxCollectionSizeInBytes != maxCollectionSizeInBytes) {
                logger.info("Dropping collection {} to recreate it with a max size of {}", namespace.getCollectionName(), maxCollectionSizeInBytes);
                mongoDatabase.getCollection(namespace.getCollectionName()).drop();
            }
        }

        // Create collection if missing
        if (!mongoDatabase.listCollectionNames().into(new ArrayList<>()).contains(namespace.getCollectionName())) {
            logger.info("Creating collection {} with max size {} ({})", namespace.getCollectionName(), maxCollectionSizeInBytes, SpaceConverterTools.convertToBiggestBUnit(maxCollectionSizeInBytes));
            mongoDatabase.createCollection(namespace.getCollectionName(), new CreateCollectionOptions()
                    .capped(true)
                    .sizeInBytes(maxCollectionSizeInBytes)
            );
        }
    }

    /**
     * Create the indexes if they do not exist and delete any extra. It does not update any indexes with same name, but different options.
     *
     * @param mongoCollection                the collection
     * @param indexKeysAndOptionsByIndexName the index name and the keys and options
     */
    public static void manageIndexes(MongoCollection<Document> mongoCollection, Map<String, Tuple2<Document, IndexOptions>> indexKeysAndOptionsByIndexName) {

        // Get the current indexes
        Set<String> currentIndexNames = mongoCollection.listIndexes()
                .into(new ArrayList<>()).stream()
                .map(index -> index.getString("name"))
                .filter(indexName -> !indexName.equals("_id_"))
                .collect(Collectors.toSet());

        // Delete any extra
        for (String indexName : currentIndexNames) {
            if (!indexKeysAndOptionsByIndexName.containsKey(indexName)) {
                logger.info("Dropping extra index {}", indexName);
                mongoCollection.dropIndex(indexName);
            }
        }

        // Create the indexes if they do not exist
        for (Map.Entry<String, Tuple2<Document, IndexOptions>> entry : indexKeysAndOptionsByIndexName.entrySet()) {
            String indexName = entry.getKey();
            Tuple2<Document, IndexOptions> keysAndOptions = entry.getValue();

            // If the index does not exist, create it
            if (!currentIndexNames.contains(indexName)) {
                Document keys = keysAndOptions.getA();
                IndexOptions options = keysAndOptions.getB();
                options.name(indexName);
                logger.info("Creating index {} with keys {} and options {}", indexName, keys, options);
                mongoCollection.createIndex(keys, options);
            }
        }

    }
}
