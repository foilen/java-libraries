/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.trigger;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.test.asserts.AssertTools;
import com.foilen.smalltools.tools.ThreadTools;

public class SmoothTriggerTest {

    private static final Logger logger = LoggerFactory.getLogger(SmoothTriggerTest.class);

    private static final long QUICK = 100;
    private static final long DELAY = 500;
    private static final long HALF_DELAY = DELAY / 2; // 0.5D
    private static final long MAX_DELAY = DELAY * 3; // 3D
    private static final long HALF_MAX_DELAY = MAX_DELAY / 2; // 1.5D

    private long startTime;
    private List<Long> triggers;
    private Runnable action;

    @Before
    public void before() {
        triggers = new ArrayList<>();
        action = () -> {
            long delta = System.currentTimeMillis() - startTime;
            logger.info("Action triggered at {}", delta);
            triggers.add(delta);
        };
        startTime = System.currentTimeMillis();
    }

    @Test(timeout = 30000)
    public void testPassthru_Warmup_Max_Cancel() {
        SmoothTrigger smoothTrigger = new SmoothTrigger(action) //
                .setDelayAfterLastTriggerMs(DELAY) //
                .setMaxDelayAfterFirstRequestMs(MAX_DELAY) //
                .setFirstPassThrough(true) //
                .start();

        // Trigger first and at max
        smoothTrigger.request(); // [TRIGGERED] At 0D ; next min delta 1D ; next max 3D
        ThreadTools.sleep(HALF_DELAY);
        smoothTrigger.request(); // [NO] At 0.5D ; next min delta 1.5D ; next max 3D
        ThreadTools.sleep(HALF_DELAY);
        smoothTrigger.request(); // [NO] At 1D ; next min delta 2D ; next max 3D

        ThreadTools.sleep(HALF_DELAY);
        smoothTrigger.request(); // [NO] At 1.5D ; next min delta 2.5D ; next max 3D
        ThreadTools.sleep(HALF_DELAY);
        smoothTrigger.request(); // [NO] At 2D ; next min delta 3D ; next max 3D

        ThreadTools.sleep(DELAY); // [TRIGGERED] At 3D by max while waiting . End of cooldown at 4D

        // During cooldown, don't do right away
        smoothTrigger.request(); // [NO] At 3D ; next min delta 4D ; next max 6D
        ThreadTools.sleep(HALF_DELAY);
        smoothTrigger.request(); // [NO] At 3.5D ; next min delta 4.5D ; next max 6D
        ThreadTools.sleep(HALF_DELAY);
        smoothTrigger.request(); // [NO] At 4D ; next min delta 5D ; next max 6D
        ThreadTools.sleep(HALF_DELAY);
        smoothTrigger.request(); // [NO] At 4.5D ; next min delta 5.5D ; next max 6D
        ThreadTools.sleep(HALF_DELAY);
        smoothTrigger.request(); // [NO] At 5D ; next min delta 6D ; next max 6D

        ThreadTools.sleep(2 * DELAY + HALF_DELAY); // [TRIGGERED] At 6D by max while waiting . End of cooldown at 7D

        // Wait end of cooldown, then 2 quickly -> Trigger first, wait delay for second
        smoothTrigger.request(); // [TRIGGERED] At 7.5D ; next min delta 8.5D ; next max 10.5D
        ThreadTools.sleep(QUICK);
        smoothTrigger.request(); // [NO] At 7.5D ; next min delta 8.5D ; next max 10.5D

        ThreadTools.sleep(2 * DELAY + HALF_DELAY); // [TRIGGERED] At 8.5D by delay while waiting . End of cooldown at 9.5D

        // Wait end of cooldown, 1 [TRIGGERED], cancel, 1 [NO] at 11D
        smoothTrigger.request(); // [TRIGGERED] At 10D ; next min delta 11D ; next max 13D
        ThreadTools.sleep(QUICK);
        smoothTrigger.cancelPending();
        ThreadTools.sleep(QUICK);
        smoothTrigger.request(); // [NO] At 10D ; next min delta 11D ; next max 13D

        ThreadTools.sleep(2 * DELAY); // [TRIGGERED] At 11D by delay while waiting . End of cooldown at 12D

        smoothTrigger.stop(true);

        AssertTools.assertEqualsDelta(0, triggers.get(0), HALF_DELAY);
        AssertTools.assertEqualsDelta(3 * DELAY, triggers.get(1), HALF_DELAY);
        AssertTools.assertEqualsDelta(6 * DELAY, triggers.get(2), HALF_DELAY);
        AssertTools.assertEqualsDelta(7 * DELAY + HALF_DELAY, triggers.get(3), HALF_DELAY);
        AssertTools.assertEqualsDelta(8 * DELAY + HALF_DELAY, triggers.get(4), HALF_DELAY);
        AssertTools.assertEqualsDelta(10 * DELAY, triggers.get(5), HALF_DELAY);
        AssertTools.assertEqualsDelta(11 * DELAY, triggers.get(6), DELAY);
        Assert.assertEquals(7, triggers.size());
    }

    @Test(timeout = 30000)
    public void testWarmup() {
        SmoothTrigger smoothTrigger = new SmoothTrigger(action) //
                .setDelayAfterLastTriggerMs(DELAY) //
                .setMaxDelayAfterFirstRequestMs(Long.MAX_VALUE) //
                .setFirstPassThrough(false) //
                .start();

        // Send too many requests to let it execute
        for (int i = 0; i < 4; ++i) {
            ThreadTools.sleep(HALF_DELAY);
            smoothTrigger.request();
        } // At 2D
        smoothTrigger.stop(false);
        Assert.assertEquals(0, triggers.size());

        // Send multiple events in one shot (2 times) and get 2 triggers at the end
        startTime = System.currentTimeMillis(); // At 0ms
        smoothTrigger.start();
        for (int i = 0; i < 2; ++i) {
            smoothTrigger.request();
            ThreadTools.sleep(QUICK);
            smoothTrigger.request();
            ThreadTools.sleep(HALF_MAX_DELAY);
        } // At 3D
        smoothTrigger.stop(false);
        Assert.assertEquals(2, triggers.size());
        AssertTools.assertEqualsDelta(DELAY, triggers.get(0), HALF_DELAY);
        AssertTools.assertEqualsDelta(2 * DELAY + HALF_DELAY, triggers.get(1), HALF_DELAY);

    }

    @Test(timeout = 30000)
    public void testWarmup_Max() {
        SmoothTrigger smoothTrigger = new SmoothTrigger(action) //
                .setDelayAfterLastTriggerMs(DELAY) //
                .setMaxDelayAfterFirstRequestMs(MAX_DELAY) //
                .setFirstPassThrough(false) //
                .start();

        // Send too many requests. Catched by max
        for (int i = 0; i < 14; ++i) {
            smoothTrigger.request();
            ThreadTools.sleep(HALF_DELAY);
        } // At 7D
        smoothTrigger.stop(false);
        Assert.assertEquals(2, triggers.size());
        AssertTools.assertEqualsDelta(MAX_DELAY, triggers.get(0), HALF_DELAY);
        AssertTools.assertEqualsDelta(2 * MAX_DELAY, triggers.get(1), DELAY);

    }

    @Test(timeout = 30000)
    public void testWarmup_Max_Cancel() {
        SmoothTrigger smoothTrigger = new SmoothTrigger(action) //
                .setDelayAfterLastTriggerMs(DELAY) //
                .setMaxDelayAfterFirstRequestMs(MAX_DELAY) //
                .setFirstPassThrough(false) //
                .start();

        // Send too many requests. Catched by max
        for (int i = 0; i < 8; ++i) {
            smoothTrigger.request();
            ThreadTools.sleep(HALF_DELAY);
        } // At 4D

        // Cancel most recent and send one that will use the normal delda
        smoothTrigger.cancelPending();
        smoothTrigger.request();
        ThreadTools.sleep(DELAY + HALF_DELAY);

        smoothTrigger.stop(false);
        Assert.assertEquals(2, triggers.size());
        AssertTools.assertEqualsDelta(MAX_DELAY, triggers.get(0), HALF_DELAY);
        AssertTools.assertEqualsDelta(5 * DELAY, triggers.get(1), HALF_DELAY);

    }

}
