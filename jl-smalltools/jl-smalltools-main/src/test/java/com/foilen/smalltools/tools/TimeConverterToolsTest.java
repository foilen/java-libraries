/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import org.junit.Assert;
import org.junit.Test;

public class TimeConverterToolsTest {

    @SuppressWarnings("deprecation")
    @Test
    public void testConvertToText() {
        Assert.assertEquals(null, TimeConverterTools.convertToText(null));
        Assert.assertEquals("0ms", TimeConverterTools.convertToText(0L));
        Assert.assertEquals("10ms", TimeConverterTools.convertToText(10L));
        Assert.assertEquals("250ms", TimeConverterTools.convertToText(250L));
        Assert.assertEquals("24s 0ms", TimeConverterTools.convertToText(24000L));
        Assert.assertEquals("24s 34ms", TimeConverterTools.convertToText(24034L));
        Assert.assertEquals("1m 15s 856ms", TimeConverterTools.convertToText(75856L));
        Assert.assertEquals("34w 5d 16h 1m 15s 856ms", TimeConverterTools.convertToText(21052875856L));
    }

    @Test
    public void testConvertToTextFromMin() {
        Assert.assertEquals(null, TimeConverterTools.convertToTextFromMin(null));
        Assert.assertEquals("0m", TimeConverterTools.convertToTextFromMin(0L));
        Assert.assertEquals("24m", TimeConverterTools.convertToTextFromMin(24L));
        Assert.assertEquals("1h 15m", TimeConverterTools.convertToTextFromMin(75L));
        Assert.assertEquals("34w 5d 16h 1m", TimeConverterTools.convertToTextFromMin(350881L));
    }

    @Test
    public void testConvertToTextFromMs() {
        Assert.assertEquals(null, TimeConverterTools.convertToTextFromMs(null));
        Assert.assertEquals("0ms", TimeConverterTools.convertToTextFromMs(0L));
        Assert.assertEquals("10ms", TimeConverterTools.convertToTextFromMs(10L));
        Assert.assertEquals("250ms", TimeConverterTools.convertToTextFromMs(250L));
        Assert.assertEquals("24s 0ms", TimeConverterTools.convertToTextFromMs(24000L));
        Assert.assertEquals("24s 34ms", TimeConverterTools.convertToTextFromMs(24034L));
        Assert.assertEquals("1m 15s 856ms", TimeConverterTools.convertToTextFromMs(75856L));
        Assert.assertEquals("34w 5d 16h 1m 15s 856ms", TimeConverterTools.convertToTextFromMs(21052875856L));
    }

    @Test
    public void testConvertToTextFromSec() {
        Assert.assertEquals(null, TimeConverterTools.convertToTextFromSec(null));
        Assert.assertEquals("0s", TimeConverterTools.convertToTextFromSec(0L));
        Assert.assertEquals("24s", TimeConverterTools.convertToTextFromSec(24L));
        Assert.assertEquals("1m 15s", TimeConverterTools.convertToTextFromSec(75L));
        Assert.assertEquals("34w 5d 16h 1m 15s", TimeConverterTools.convertToTextFromSec(21052875L));
    }

}
