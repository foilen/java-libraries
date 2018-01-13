/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.upgrader;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.smalltools.upgrader.tasks.UpgradeTask;
import com.foilen.smalltools.upgrader.trackers.JsonFileUpgraderTracker;
import com.foilen.smalltools.upgrader.trackers.UpgraderTracker;

public class UpgraderToolsTest {

    private static class StubOneUpgradeTask implements UpgradeTask {

        private String useTracker = UpgradeTask.DEFAULT_TRACKER;
        private boolean called = false;

        private void assertCalled() {
            Assert.assertTrue("Was not called", called);
        }

        @Override
        public void execute() {
            Assert.assertFalse("Was already called", called);
            called = true;
        }

        @Override
        public String useTracker() {
            return useTracker;
        }

    }

    private static class StubTwoUpgradeTask implements UpgradeTask {

        private String useTracker = UpgradeTask.DEFAULT_TRACKER;
        private boolean called = false;

        private void assertCalled() {
            Assert.assertTrue("Was not called", called);
        }

        @Override
        public void execute() {
            Assert.assertFalse("Was already called", called);
            called = true;
        }

        @Override
        public String useTracker() {
            return useTracker;
        }

    }

    @Test
    public void testExecute() throws Exception {
        // Prepare tracker
        File file = File.createTempFile("upgrader", null);
        file.delete();
        UpgraderTracker upgraderTracker = new JsonFileUpgraderTracker(file.getAbsolutePath());

        UpgraderTools upgraderTools = new UpgraderTools();
        upgraderTools.setDefaultUpgraderTracker(upgraderTracker);

        // One task
        StubOneUpgradeTask taskOne = new StubOneUpgradeTask();
        upgraderTools.getTasks().add(taskOne);
        upgraderTools.execute();
        upgraderTools.execute();

        // 2 tasks
        StubTwoUpgradeTask taskTwo = new StubTwoUpgradeTask();
        upgraderTools.getTasks().add(taskTwo);
        upgraderTools.execute();
        upgraderTools.execute();

        taskOne.assertCalled();
        taskTwo.assertCalled();

    }

}
