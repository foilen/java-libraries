/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.smalltools.exception.SmallToolsException;

public class PriceFormatToolsTest {

    @Test
    public void testToDigit() {
        Assert.assertEquals("0.00", PriceFormatTools.toDigit(0));
        Assert.assertEquals("0.10", PriceFormatTools.toDigit(10));
        Assert.assertEquals("1.20", PriceFormatTools.toDigit(120));
        Assert.assertEquals("12.34", PriceFormatTools.toDigit(1234));
        Assert.assertEquals("1234.56", PriceFormatTools.toDigit(123456));
    }

    @Test
    public void testToLong() {
        Assert.assertEquals(0, PriceFormatTools.toLong("0.00"));
        Assert.assertEquals(0, PriceFormatTools.toLong("0.0"));
        Assert.assertEquals(0, PriceFormatTools.toLong("0."));
        Assert.assertEquals(0, PriceFormatTools.toLong("."));
        Assert.assertEquals(0, PriceFormatTools.toLong("0"));
        Assert.assertEquals(0, PriceFormatTools.toLong(""));
        Assert.assertEquals(0, PriceFormatTools.toLong(null));
        Assert.assertEquals(10, PriceFormatTools.toLong("0.10"));
        Assert.assertEquals(10, PriceFormatTools.toLong("0.1"));
        Assert.assertEquals(120, PriceFormatTools.toLong("1.20"));
        Assert.assertEquals(1234, PriceFormatTools.toLong("12.34"));
        Assert.assertEquals(123456, PriceFormatTools.toLong("1234.56"));
        Assert.assertEquals(123456, PriceFormatTools.toLong("1234,56"));
        Assert.assertEquals(123450, PriceFormatTools.toLong("1234.5"));
        Assert.assertEquals(123400, PriceFormatTools.toLong("1234"));
        Assert.assertEquals(123400, PriceFormatTools.toLong("1234."));
        Assert.assertEquals(123400, PriceFormatTools.toLong("1234,"));
    }

    @Test
    public void testToLongInvalid() {
        try {
            PriceFormatTools.toLong("0.00.0");
            Assert.fail("Expecting failure");
        } catch (SmallToolsException e) {
        }
        try {
            PriceFormatTools.toLong("0.00,0");
            Assert.fail("Expecting failure");
        } catch (SmallToolsException e) {
        }
        try {
            PriceFormatTools.toLong("0-00");
            Assert.fail("Expecting failure");
        } catch (SmallToolsException e) {
        }
        try {
            PriceFormatTools.toLong("1.234");
            Assert.fail("Expecting failure");
        } catch (SmallToolsException e) {
        }
        try {
            PriceFormatTools.toLong("1.c4");
            Assert.fail("Expecting failure");
        } catch (SmallToolsException e) {
        }
        try {
            PriceFormatTools.toLong("1carotte4");
            Assert.fail("Expecting failure");
        } catch (SmallToolsException e) {
        }
    }

}
