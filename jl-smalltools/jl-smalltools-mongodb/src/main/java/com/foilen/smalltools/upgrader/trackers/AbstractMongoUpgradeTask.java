/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.upgrader.trackers;

import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tuple.Tuple2;
import com.foilen.smalltools.upgrader.tasks.UpgradeTask;
import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoOperations;

import java.util.List;

public abstract class AbstractMongoUpgradeTask extends AbstractBasics implements UpgradeTask {

    @Autowired
    protected MongoClient mongoClient;
    @Autowired
    protected MongoOperations mongoOperations;

    @Value("${spring.data.mongodb.database}")
    protected String databaseName;

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

    protected void dropCollection(String collectionName) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);

        logger.info("Drop collection {}", collectionName);
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        collection.drop();
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    @Override
    public String useTracker() {
        return "mongodb";
    }

}
