package com.foilen.smalltools.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.foilen.smalltools.exception.SmallToolsException;

public class PriceFormatToolsTest {

    @Test
    public void testToDigit() {
        Assertions.assertEquals("0.00", PriceFormatTools.toDigit(0));
        Assertions.assertEquals("0.10", PriceFormatTools.toDigit(10));
        Assertions.assertEquals("1.20", PriceFormatTools.toDigit(120));
        Assertions.assertEquals("12.34", PriceFormatTools.toDigit(1234));
        Assertions.assertEquals("1234.56", PriceFormatTools.toDigit(123456));
    }

    @Test
    public void testToLong() {
        Assertions.assertEquals(0, PriceFormatTools.toLong("0.00"));
        Assertions.assertEquals(0, PriceFormatTools.toLong("0.0"));
        Assertions.assertEquals(0, PriceFormatTools.toLong("0."));
        Assertions.assertEquals(0, PriceFormatTools.toLong("."));
        Assertions.assertEquals(0, PriceFormatTools.toLong("0"));
        Assertions.assertEquals(0, PriceFormatTools.toLong(""));
        Assertions.assertEquals(0, PriceFormatTools.toLong(null));
        Assertions.assertEquals(10, PriceFormatTools.toLong("0.10"));
        Assertions.assertEquals(10, PriceFormatTools.toLong("0.1"));
        Assertions.assertEquals(120, PriceFormatTools.toLong("1.20"));
        Assertions.assertEquals(1234, PriceFormatTools.toLong("12.34"));
        Assertions.assertEquals(123456, PriceFormatTools.toLong("1234.56"));
        Assertions.assertEquals(123456, PriceFormatTools.toLong("1234,56"));
        Assertions.assertEquals(123450, PriceFormatTools.toLong("1234.5"));
        Assertions.assertEquals(123400, PriceFormatTools.toLong("1234"));
        Assertions.assertEquals(123400, PriceFormatTools.toLong("1234."));
        Assertions.assertEquals(123400, PriceFormatTools.toLong("1234,"));
    }

    @Test
    public void testToLongInvalid() {
        try {
            PriceFormatTools.toLong("0.00.0");
            Assertions.fail("Expecting failure");
        } catch (SmallToolsException e) {
        }
        try {
            PriceFormatTools.toLong("0.00,0");
            Assertions.fail("Expecting failure");
        } catch (SmallToolsException e) {
        }
        try {
            PriceFormatTools.toLong("0-00");
            Assertions.fail("Expecting failure");
        } catch (SmallToolsException e) {
        }
        try {
            PriceFormatTools.toLong("1.234");
            Assertions.fail("Expecting failure");
        } catch (SmallToolsException e) {
        }
        try {
            PriceFormatTools.toLong("1.c4");
            Assertions.fail("Expecting failure");
        } catch (SmallToolsException e) {
        }
        try {
            PriceFormatTools.toLong("1carotte4");
            Assertions.fail("Expecting failure");
        } catch (SmallToolsException e) {
        }
    }

}
