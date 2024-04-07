/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.upgrader.trackers.tasks;

import com.foilen.smalltools.upgrader.trackers.AbstractMongoUpgradeTask;
import org.bson.Document;

public class V_1_InsertTask extends AbstractMongoUpgradeTask {

    @Override
    public void execute() {
        String collectionName = "col";
        String id = "id-1";
        logger.info("Insert in {} : {}", collectionName, id);
        mongoClient.getDatabase(databaseName)
                .getCollection(collectionName)
                .insertOne(new Document("_id", id));
    }
}
