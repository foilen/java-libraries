/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.upgrader.trackers;

import org.junit.Assert;
import org.junit.Test;

public abstract class AbstractUpgraderTrackerTest {

    private UpgraderTracker upgraderTracker;

    protected void init(UpgraderTracker upgraderTracker) {
        this.upgraderTracker = upgraderTracker;
    }

    @Test
    public void test() {

        upgraderTracker.trackerBegin();

        // Success on first time
        Assert.assertFalse(upgraderTracker.wasExecutedSuccessfully("task1"));
        upgraderTracker.executionBegin("task1");
        Assert.assertFalse(upgraderTracker.wasExecutedSuccessfully("task1"));
        upgraderTracker.executionEnd("task1", true);
        Assert.assertTrue(upgraderTracker.wasExecutedSuccessfully("task1"));

        // Success on second time
        Assert.assertFalse(upgraderTracker.wasExecutedSuccessfully("task2"));
        upgraderTracker.executionBegin("task2");
        Assert.assertFalse(upgraderTracker.wasExecutedSuccessfully("task2"));
        upgraderTracker.executionBegin("task2");
        Assert.assertFalse(upgraderTracker.wasExecutedSuccessfully("task2"));
        upgraderTracker.executionEnd("task2", false);
        Assert.assertFalse(upgraderTracker.wasExecutedSuccessfully("task2"));
        upgraderTracker.executionEnd("task2", true);
        Assert.assertTrue(upgraderTracker.wasExecutedSuccessfully("task2"));

        upgraderTracker.trackerEnd();

    }

}
