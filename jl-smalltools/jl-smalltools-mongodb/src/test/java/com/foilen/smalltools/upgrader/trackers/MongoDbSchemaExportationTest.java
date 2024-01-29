/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.upgrader.trackers;

import com.foilen.smalltools.test.asserts.AssertTools;
import com.foilen.smalltools.tools.ResourceTools;
import com.foilen.smalltools.upgrader.UpgraderTools;
import com.foilen.smalltools.upgrader.tasks.UpgradeTask;
import com.foilen.smalltools.upgrader.trackers.tasks.V_Create_Schema;
import com.foilen.smalltools.upgrader.trackers.tasks.V_Export_Schema;
import org.junit.jupiter.api.Test;

import java.util.List;

public class MongoDbSchemaExportationTest extends AbstractEmbeddedMongoDbTest {

    private final String mongoDatabaseName = getClass().getSimpleName();

    @Test
    public void test() throws Exception {

        var mongoDatabase = mongoClient.getDatabase(mongoDatabaseName);

        var tracker = new MongoDbUpgraderTracker(mongoClient, mongoDatabaseName);
        var upgraderTools = new UpgraderTools();
        upgraderTools.setDefaultUpgraderTracker(tracker);
        upgraderTools.setSortByClassName(false);
        upgraderTools.addUpgraderTracker("mongodb", tracker);

        // Execute
        V_Export_Schema exportSchema = new V_Export_Schema();
        upgraderTools.setTasks(List.of(
                createTask(new V_Create_Schema()),
                createTask(exportSchema)
        ));
        upgraderTools.execute();

        // Assert
        String expected = ResourceTools.getResourceAsString("MongoDbSchemaExportationTest-test-expected.txt", getClass());
        AssertTools.assertIgnoreLineFeed(expected, exportSchema.getExportedSchema());

    }

    private UpgradeTask createTask(AbstractMongoUpgradeTask upgradeTask) {
        upgradeTask.setMongoClient(mongoClient);
        upgradeTask.setDatabaseName(mongoDatabaseName);
        return upgradeTask;
    }

}