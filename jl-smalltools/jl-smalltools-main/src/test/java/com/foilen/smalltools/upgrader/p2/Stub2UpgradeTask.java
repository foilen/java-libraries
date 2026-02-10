package com.foilen.smalltools.upgrader.p2;

import com.foilen.smalltools.upgrader.UpgraderToolsTest;
import com.foilen.smalltools.upgrader.tasks.UpgradeTask;
import org.junit.jupiter.api.Assertions;

public class Stub2UpgradeTask implements UpgradeTask {

    private String useTracker = UpgradeTask.DEFAULT_TRACKER;
    private Integer calledOrder = null;

    public void assertCalled(Integer expectedOrder) {
        Assertions.assertNotNull(calledOrder, "Was not called");
        Assertions.assertEquals(expectedOrder, calledOrder);
    }

    @Override
    public void execute() {
        Assertions.assertNull(calledOrder, "Was already called");
        calledOrder = UpgraderToolsTest.taskCalledOrder.incrementAndGet();
    }

    @Override
    public String useTracker() {
        return useTracker;
    }

}
