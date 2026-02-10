package com.foilen.smalltools.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TimeConverterToolsTest {

    @Test
    public void testConvertToTextFromMin() {
        Assertions.assertEquals(null, TimeConverterTools.convertToTextFromMin(null));
        Assertions.assertEquals("0m", TimeConverterTools.convertToTextFromMin(0L));
        Assertions.assertEquals("24m", TimeConverterTools.convertToTextFromMin(24L));
        Assertions.assertEquals("1h 15m", TimeConverterTools.convertToTextFromMin(75L));
        Assertions.assertEquals("34w 5d 16h 1m", TimeConverterTools.convertToTextFromMin(350881L));
    }

    @Test
    public void testConvertToTextFromMs() {
        Assertions.assertEquals(null, TimeConverterTools.convertToTextFromMs(null));
        Assertions.assertEquals("0ms", TimeConverterTools.convertToTextFromMs(0L));
        Assertions.assertEquals("10ms", TimeConverterTools.convertToTextFromMs(10L));
        Assertions.assertEquals("250ms", TimeConverterTools.convertToTextFromMs(250L));
        Assertions.assertEquals("24s 0ms", TimeConverterTools.convertToTextFromMs(24000L));
        Assertions.assertEquals("24s 34ms", TimeConverterTools.convertToTextFromMs(24034L));
        Assertions.assertEquals("1m 15s 856ms", TimeConverterTools.convertToTextFromMs(75856L));
        Assertions.assertEquals("34w 5d 16h 1m 15s 856ms", TimeConverterTools.convertToTextFromMs(21052875856L));
    }

    @Test
    public void testConvertToTextFromSec() {
        Assertions.assertEquals(null, TimeConverterTools.convertToTextFromSec(null));
        Assertions.assertEquals("0s", TimeConverterTools.convertToTextFromSec(0L));
        Assertions.assertEquals("24s", TimeConverterTools.convertToTextFromSec(24L));
        Assertions.assertEquals("1m 15s", TimeConverterTools.convertToTextFromSec(75L));
        Assertions.assertEquals("34w 5d 16h 1m 15s", TimeConverterTools.convertToTextFromSec(21052875L));
    }

}
