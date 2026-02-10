package com.foilen.smalltools.upgrader.trackers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public abstract class AbstractUpgraderTrackerTest {

    private UpgraderTracker upgraderTracker;

    protected void init(UpgraderTracker upgraderTracker) {
        this.upgraderTracker = upgraderTracker;
    }

    @Test
    public void test() {

        upgraderTracker.trackerBegin();

        // Success on first time
        Assertions.assertFalse(upgraderTracker.wasExecutedSuccessfully("task1"));
        upgraderTracker.executionBegin("task1");
        Assertions.assertFalse(upgraderTracker.wasExecutedSuccessfully("task1"));
        upgraderTracker.executionEnd("task1", true);
        Assertions.assertTrue(upgraderTracker.wasExecutedSuccessfully("task1"));

        // Success on second time
        Assertions.assertFalse(upgraderTracker.wasExecutedSuccessfully("task2"));
        upgraderTracker.executionBegin("task2");
        Assertions.assertFalse(upgraderTracker.wasExecutedSuccessfully("task2"));
        upgraderTracker.executionBegin("task2");
        Assertions.assertFalse(upgraderTracker.wasExecutedSuccessfully("task2"));
        upgraderTracker.executionEnd("task2", false);
        Assertions.assertFalse(upgraderTracker.wasExecutedSuccessfully("task2"));
        upgraderTracker.executionEnd("task2", true);
        Assertions.assertTrue(upgraderTracker.wasExecutedSuccessfully("task2"));

        upgraderTracker.trackerEnd();

    }

}
