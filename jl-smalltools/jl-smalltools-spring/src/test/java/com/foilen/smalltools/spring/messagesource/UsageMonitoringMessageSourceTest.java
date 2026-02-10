package com.foilen.smalltools.spring.messagesource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UsageMonitoringMessageSourceTest {

    @Test
    public void testFormat() {
        Assertions.assertEquals("This is a test", UsageMonitoringMessageSource.format("This is a test", null));
        Assertions.assertEquals("This is a test", UsageMonitoringMessageSource.format("This is a test", new Object[]{}));
        Assertions.assertEquals("This is a test", UsageMonitoringMessageSource.format("This is a test", new Object[]{"param1"}));
        Assertions.assertEquals("This is a test: param1, param2, param3", UsageMonitoringMessageSource.format("This is a test: {0}, {1}, {2}", new Object[]{"param1", "param2", "param3"}));
        Assertions.assertEquals("This is a test: param1, null, param3", UsageMonitoringMessageSource.format("This is a test: {0}, {1}, {2}", new Object[]{"param1", null, "param3"}));
    }

}
