/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.upgrader.trackers;

import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.BufferBatchesTools;
import com.foilen.smalltools.tools.ResourceTools;
import com.foilen.smalltools.tuple.Tuple2;
import com.foilen.smalltools.upgrader.tasks.UpgradeTask;
import com.mongodb.MongoCommandException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoOperations;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

/**
 * Tasks with helpers to manage a mongodb database.
 */
public abstract class AbstractMongoUpgradeTask extends AbstractBasics implements UpgradeTask {

    @Autowired
    protected MongoClient mongoClient;
    @Autowired
    protected MongoOperations mongoOperations;

    @Value("${spring.data.mongodb.database}")
    protected String databaseName;

    /**
     * Add a collection if it does not exist.
     *
     * @param collectionName the name of the collection
     */
    protected void addCollection(String collectionName) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);

        logger.info("Create collection {}", collectionName);
        try {
            mongoDatabase.createCollection(collectionName);
        } catch (MongoCommandException e) {
            if (e.getErrorCode() != 48) { // Already exists
                throw e;
            }
        }
    }

    /**
     * Add an index if it does not exist.
     *
     * @param collectionName the name of the collection
     * @param indexOptions   the options for the index
     * @param keys           the keys to index
     */
    @SafeVarargs
    protected final void addIndex(String collectionName, IndexOptions indexOptions, Tuple2<String, Object>... keys) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);

        logger.info("Create index for collection {} , with keys {}", collectionName, keys);
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        Document keysDocument = new Document();
        for (Tuple2<String, Object> key : keys) {
            keysDocument.put(key.getA(), key.getB());
        }
        collection.createIndex(keysDocument, indexOptions);
    }

    /**
     * Add an index if it does not exist.
     *
     * @param collectionName the name of the collection
     * @param keys           the keys to index
     */
    @SafeVarargs
    protected final void addIndex(String collectionName, Tuple2<String, Object>... keys) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);

        logger.info("Create index for collection {} , with keys {}", collectionName, keys);
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        Document keysDocument = new Document();
        for (Tuple2<String, Object> key : keys) {
            keysDocument.put(key.getA(), key.getB());
        }
        collection.createIndex(keysDocument);
    }

    /**
     * Add a view if it does not exist.
     *
     * @param viewName the name of the view
     * @param viewOn   the collection to view
     * @param pipeline the pipeline to apply
     */
    protected void addView(String viewName, String viewOn, List<? extends Bson> pipeline) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);

        logger.info("Create view {}", viewName);
        try {
            mongoDatabase.createView(viewName, viewOn, pipeline);
        } catch (MongoCommandException e) {
            if (e.getErrorCode() != 48) { // Already exists
                throw e;
            }
        }
    }

    protected void insertInCollection(String collectionName, String resourceName, Class<?> resourceClass) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);

        logger.info("Insert in collection {}", collectionName);
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);

        BufferBatchesTools.<Document>autoClose(100, collection::insertMany, documents -> {
            ResourceTools.readResourceLinesIteration(resourceName, resourceClass).forEach(line -> {
                logger.info("Batching line {}", line);
                Document document = Document.parse(line);
                documents.add(document);
            });
        });

    }

    protected void insertOrUpdateInCollection(String collectionName, String resourceName, Class<?> resourceClass) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);

        logger.info("Insert or update in collection {}", collectionName);
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);

        ResourceTools.readResourceLinesIteration(resourceName, resourceClass).forEach(line -> {
            logger.info("Line {}", line);
            Document document = Document.parse(line);

            // Check if exists
            if (collection.find(Filters.eq("_id", document.get("_id"))).first() == null) {
                logger.info("Inserting {}", line);
                collection.insertOne(document);
            } else {
                logger.info("Updating {}", line);
                collection.replaceOne(Filters.eq("_id", document.get("_id")), document);
            }

        });

    }

    protected void exportFromCollection(String fileName, String collectionName, Bson filter, Consumer<FindIterable<Document>> findConsumer) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);

        logger.info("Export from collection {} to file {}", collectionName, fileName);
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);

        var findIterator = collection.find(filter);
        findConsumer.accept(findIterator);

        try (PrintWriter out = new PrintWriter(fileName, StandardCharsets.UTF_8)) {
            findIterator.forEach(document -> {
                String line = document.toJson();
                logger.info("Exporting {}", line);
                out.println(line);
            });
        } catch (Exception e) {
            throw new RuntimeException("Problem exporting", e);
        }

    }

    /**
     * Drop a collection if it exists.
     *
     * @param collectionName the name of the collection
     */
    protected void dropCollection(String collectionName) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);

        logger.info("Drop collection {}", collectionName);
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        collection.drop();
    }

    /**
     * Get the database name.
     *
     * @return the database name
     */
    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * Get the mongo client.
     *
     * @return the mongo client
     */
    public MongoClient getMongoClient() {
        return mongoClient;
    }

    /**
     * Set the database name.
     *
     * @param databaseName the database name
     */
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    /**
     * Set the mongo client.
     *
     * @param mongoClient the mongo client
     */
    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    @Override
    public String useTracker() {
        return "mongodb";
    }

}
