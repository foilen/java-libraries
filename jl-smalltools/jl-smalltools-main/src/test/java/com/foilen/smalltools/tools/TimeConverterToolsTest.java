/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import org.junit.Assert;
import org.junit.Test;

public class TimeConverterToolsTest {

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

}
