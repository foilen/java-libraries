/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.upgrader.trackers;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bson.Document;

import com.foilen.smalltools.tools.DateTools;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.Filters;

/**
 * A tracker that stores the successfully executed tasks in a MongoDB Database.
 *
 * <pre>
 * Dependencies:
 * compile 'org.mongodb:mongodb-driver-sync:4.1.1'
 * </pre>
 */
public class MongoDbUpgraderTracker implements UpgraderTracker {

    private MongoClient mongoClient;
    private String collectionName = "upgraderTools";
    private String databaseName;

    public MongoDbUpgraderTracker(MongoClient mongoClient, String databaseName) {
        this.mongoClient = mongoClient;
        this.databaseName = databaseName;
    }

    public MongoDbUpgraderTracker(String uri, String databaseName) {
        mongoClient = MongoClients.create(uri);
        this.databaseName = databaseName;
    }

    @Override
    public void executionBegin(String taskSimpleName) {
    }

    @Override
    public void executionEnd(String taskSimpleName, boolean isSuccessful) {
        if (isSuccessful) {
            Date appliedDate = new Date();

            Map<String, Object> document = new HashMap<String, Object>();
            document.put("_id", taskSimpleName);
            document.put("appliedDate", appliedDate);
            document.put("appliedDateText", DateTools.formatFull(appliedDate));

            mongoClient.getDatabase(databaseName) //
                    .getCollection(collectionName) //
                    .insertOne(new Document(document));
        }
    }

    public String getCollectionName() {
        return collectionName;
    }

    public MongoDbUpgraderTracker setCollectionName(String collectionName) {
        this.collectionName = collectionName;
        return this;
    }

    @Override
    public void trackerBegin() {
    }

    @Override
    public void trackerEnd() {
    }

    @Override
    public boolean wasExecutedSuccessfully(String taskSimpleName) {
        return mongoClient.getDatabase(databaseName) //
                .getCollection(collectionName) //
                .countDocuments(Filters.eq("_id", taskSimpleName)) > 0;
    }

}
