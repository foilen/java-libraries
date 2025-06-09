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
