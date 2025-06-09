package com.foilen.smalltools.spring.messagesource;

import org.junit.Assert;
import org.junit.Test;

public class UsageMonitoringMessageSourceTest {

    @Test
    public void testFormat() {
        Assert.assertEquals("This is a test", UsageMonitoringMessageSource.format("This is a test", null));
        Assert.assertEquals("This is a test", UsageMonitoringMessageSource.format("This is a test", new Object[]{}));
        Assert.assertEquals("This is a test", UsageMonitoringMessageSource.format("This is a test", new Object[]{"param1"}));
        Assert.assertEquals("This is a test: param1, param2, param3", UsageMonitoringMessageSource.format("This is a test: {0}, {1}, {2}", new Object[]{"param1", "param2", "param3"}));
        Assert.assertEquals("This is a test: param1, null, param3", UsageMonitoringMessageSource.format("This is a test: {0}, {1}, {2}", new Object[]{"param1", null, "param3"}));
    }

}
