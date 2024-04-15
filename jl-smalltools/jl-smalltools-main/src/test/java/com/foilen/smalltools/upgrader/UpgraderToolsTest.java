/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.upgrader;

import com.foilen.smalltools.upgrader.p1.Stub1UpgradeTask;
import com.foilen.smalltools.upgrader.p1.Stub3UpgradeTask;
import com.foilen.smalltools.upgrader.p2.Stub2UpgradeTask;
import com.foilen.smalltools.upgrader.trackers.JsonFileUpgraderTracker;
import com.foilen.smalltools.upgrader.trackers.UpgraderTracker;
import org.junit.Test;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

public class UpgraderToolsTest {

    public static AtomicInteger taskCalledOrder = new AtomicInteger(0);

    @Test
    public void testExecuteRightOrder() throws Exception {

        taskCalledOrder.set(0);

        // Prepare tracker
        File file = File.createTempFile("upgrader", null);
        file.delete();
        UpgraderTracker upgraderTracker = new JsonFileUpgraderTracker(file.getAbsolutePath());

        UpgraderTools upgraderTools = new UpgraderTools();
        upgraderTools.setDefaultUpgraderTracker(upgraderTracker);

        // All tasks
        var task1 = new Stub1UpgradeTask();
        var task3 = new Stub3UpgradeTask();
        var task2 = new Stub2UpgradeTask();
        upgraderTools.getTasks().add(task1);
        upgraderTools.getTasks().add(task3);
        upgraderTools.getTasks().add(task2);
        upgraderTools.execute();

        task1.assertCalled(1);
        task2.assertCalled(2);
        task3.assertCalled(3);

    }

    @Test
    public void testExecuteDifferentRuns() throws Exception {

        taskCalledOrder.set(0);

        // Prepare tracker
        File file = File.createTempFile("upgrader", null);
        file.delete();
        UpgraderTracker upgraderTracker = new JsonFileUpgraderTracker(file.getAbsolutePath());

        UpgraderTools upgraderTools = new UpgraderTools();
        upgraderTools.setDefaultUpgraderTracker(upgraderTracker);

        // One task
        Stub1UpgradeTask taskOne = new Stub1UpgradeTask();
        upgraderTools.getTasks().add(taskOne);
        upgraderTools.execute();
        upgraderTools.execute();

        // 2 tasks
        Stub3UpgradeTask taskTwo = new Stub3UpgradeTask();
        upgraderTools.getTasks().add(taskTwo);
        upgraderTools.execute();
        upgraderTools.execute();

        taskOne.assertCalled(1);
        taskTwo.assertCalled(2);

    }

}
