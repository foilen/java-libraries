package com.foilen.smalltools.upgrader.p2;

import com.foilen.smalltools.upgrader.UpgraderToolsTest;
import com.foilen.smalltools.upgrader.tasks.UpgradeTask;
import org.junit.Assert;

public class Stub2UpgradeTask implements UpgradeTask {

    private String useTracker = UpgradeTask.DEFAULT_TRACKER;
    private Integer calledOrder = null;

    public void assertCalled(Integer expectedOrder) {
        Assert.assertNotNull("Was not called", calledOrder);
        Assert.assertEquals(expectedOrder, calledOrder);
    }

    @Override
    public void execute() {
        Assert.assertNull("Was already called", calledOrder);
        calledOrder = UpgraderToolsTest.taskCalledOrder.incrementAndGet();
    }

    @Override
    public String useTracker() {
        return useTracker;
    }

}
