/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.upgrader.trackers;

import com.foilen.smalltools.tools.StreamTools;
import com.foilen.smalltools.upgrader.UpgraderTools;
import com.foilen.smalltools.upgrader.tasks.UpgradeTask;
import com.foilen.smalltools.upgrader.trackers.tasks.V_1_InsertTask;
import com.foilen.smalltools.upgrader.trackers.tasks.V_2_InsertTask;
import com.mongodb.client.model.Sorts;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.List;

public class MongoDbUpgraderTrackerTest extends AbstractEmbeddedMongoDbTest {

    private String mongoDatabaseName = getClass().getSimpleName();

    @Test
    public void test() throws Exception {

        var mongoDatabase = mongoClient.getDatabase(mongoDatabaseName);
        var mongoCollection = mongoDatabase.getCollection("col");

        var tracker = new MongoDbUpgraderTracker(mongoClient, mongoDatabaseName);
        var upgraderTools = new UpgraderTools();
        upgraderTools.setDefaultUpgraderTracker(tracker);
        upgraderTools.setSortByClassName(false);
        upgraderTools.addUpgraderTracker("mongodb", tracker);

        // No tasks
        upgraderTools.setTasks(List.of());
        upgraderTools.execute();
        Assert.assertEquals(List.of(),
                StreamTools.toStream(mongoCollection.find().sort(Sorts.ascending("_id")).spliterator())
                        .map(doc -> doc.getString("_id"))
                        .toList()
        );

        // Execute one
        upgraderTools.setTasks(List.of(
                createTask(new V_1_InsertTask())
        ));
        upgraderTools.execute();
        Assert.assertEquals(List.of("id-1"),
                StreamTools.toStream(mongoCollection.find().sort(Sorts.ascending("_id")).spliterator())
                        .map(doc -> doc.getString("_id"))
                        .toList()
        );

        // Reexecute (must not do anything)
        upgraderTools.execute();
        Assert.assertEquals(List.of("id-1"),
                StreamTools.toStream(mongoCollection.find().sort(Sorts.ascending("_id")).spliterator())
                        .map(doc -> doc.getString("_id"))
                        .toList()
        );

        // Execute with another task
        upgraderTools.setTasks(List.of(
                createTask(new V_1_InsertTask()),
                createTask(new V_2_InsertTask())
        ));
        upgraderTools.execute();
        Assert.assertEquals(List.of("id-1", "id-2"),
                StreamTools.toStream(mongoCollection.find().sort(Sorts.ascending("_id")).spliterator())
                        .map(doc -> doc.getString("_id"))
                        .toList()
        );

        // Reeexecute (must not do anything)
        upgraderTools.execute();
        Assert.assertEquals(List.of("id-1", "id-2"),
                StreamTools.toStream(mongoCollection.find().sort(Sorts.ascending("_id")).spliterator())
                        .map(doc -> doc.getString("_id"))
                        .toList()
        );

    }

    private UpgradeTask createTask(AbstractMongoUpgradeTask upgradeTask) {
        upgradeTask.setMongoClient(mongoClient);
        upgradeTask.setDatabaseName(mongoDatabaseName);
        return upgradeTask;
    }

}