/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SpaceConverterToolsTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testConvertBytes_allNumbers() {
        Assert.assertEquals(1023456789000000L, SpaceConverterTools.convertToBytes("1023456789 m"));
    }

    @Test
    public void testConvertBytes_bytes() {
        Assert.assertEquals(85L, SpaceConverterTools.convertToBytes("85"));
        Assert.assertEquals(85L, SpaceConverterTools.convertToBytes("85b"));
        Assert.assertEquals(85L, SpaceConverterTools.convertToBytes("85B"));
        Assert.assertEquals(85L, SpaceConverterTools.convertToBytes("85 B"));
        Assert.assertEquals(85L, SpaceConverterTools.convertToBytes("85 b"));
    }

    @Test
    public void testConvertBytes_gibibytes() {
        Assert.assertEquals(91268055040L, SpaceConverterTools.convertToBytes("85gib"));
        Assert.assertEquals(91268055040L, SpaceConverterTools.convertToBytes("85 giB"));
    }

    @Test
    public void testConvertBytes_gigabytes() {
        Assert.assertEquals(85000000000L, SpaceConverterTools.convertToBytes("85g"));
        Assert.assertEquals(85000000000L, SpaceConverterTools.convertToBytes("85 g"));
        Assert.assertEquals(85000000000L, SpaceConverterTools.convertToBytes("85 gB"));
    }

    @Test
    public void testConvertBytes_invalid_char() {
        thrown.expectMessage("% is an invalid space");
        SpaceConverterTools.convertToBytes("%");
    }

    @Test
    public void testConvertBytes_invalid_number() {
        thrown.expectMessage("kb is an invalid space");
        SpaceConverterTools.convertToBytes("kb");
    }

    @Test
    public void testConvertBytes_invalid_unit() {
        thrown.expectMessage("75J is an invalid space");
        SpaceConverterTools.convertToBytes("75J");
    }

    @Test
    public void testConvertBytes_kibibytes() {
        Assert.assertEquals(87040L, SpaceConverterTools.convertToBytes("85kib"));
        Assert.assertEquals(87040L, SpaceConverterTools.convertToBytes("85 kiB"));
    }

    @Test
    public void testConvertBytes_kilobytes() {
        Assert.assertEquals(85000L, SpaceConverterTools.convertToBytes("85k"));
        Assert.assertEquals(85660L, SpaceConverterTools.convertToBytes("85.66k"));
        Assert.assertEquals(85000L, SpaceConverterTools.convertToBytes("85 k"));
        Assert.assertEquals(85000L, SpaceConverterTools.convertToBytes("85\t k"));
        Assert.assertEquals(85000L, SpaceConverterTools.convertToBytes("85 kB"));
    }

    @Test
    public void testConvertBytes_mebibytes() {
        Assert.assertEquals(89128960L, SpaceConverterTools.convertToBytes("85mib"));
        Assert.assertEquals(89128960L, SpaceConverterTools.convertToBytes("85 miB"));
        Assert.assertEquals(89128960L, SpaceConverterTools.convertToBytes("85    miB   "));
    }

    @Test
    public void testConvertBytes_megabytes() {
        Assert.assertEquals(85000000L, SpaceConverterTools.convertToBytes("85m"));
        Assert.assertEquals(85000000L, SpaceConverterTools.convertToBytes("85 m"));
        Assert.assertEquals(85000000L, SpaceConverterTools.convertToBytes("85 mB"));
    }

    @Test
    public void testConvertBytes_tebibytes() {
        Assert.assertEquals(93458488360960L, SpaceConverterTools.convertToBytes("85tib"));
        Assert.assertEquals(93458488360960L, SpaceConverterTools.convertToBytes("85 tiB"));
    }

    @Test
    public void testConvertBytes_terabytes() {
        Assert.assertEquals(85000000000000L, SpaceConverterTools.convertToBytes("85t"));
        Assert.assertEquals(85000000000000L, SpaceConverterTools.convertToBytes("85 t"));
        Assert.assertEquals(85000000000000L, SpaceConverterTools.convertToBytes("85 tB"));
    }

    @Test
    public void testConvertToBiggestBUnit_Exact() {
        Assert.assertEquals("0B", SpaceConverterTools.convertToBiggestBUnit(0L));
        Assert.assertEquals("1B", SpaceConverterTools.convertToBiggestBUnit(1L));
        Assert.assertEquals("1K", SpaceConverterTools.convertToBiggestBUnit(1000L));
        Assert.assertEquals("1M", SpaceConverterTools.convertToBiggestBUnit(1000000L));
        Assert.assertEquals("1G", SpaceConverterTools.convertToBiggestBUnit(1000000000L));
        Assert.assertEquals("1T", SpaceConverterTools.convertToBiggestBUnit(1000000000000L));
    }

    @Test
    public void testConvertToBiggestBUnit_NoDecimal() {
        Assert.assertEquals("85B", SpaceConverterTools.convertToBiggestBUnit(85L));
        Assert.assertEquals("85K", SpaceConverterTools.convertToBiggestBUnit(85000L));
        Assert.assertEquals("85G", SpaceConverterTools.convertToBiggestBUnit(85000000000L));
        Assert.assertEquals("85T", SpaceConverterTools.convertToBiggestBUnit(85000000000000L));
    }

    @Test
    public void testConvertToBiggestBUnit_OneDecimal() {
        Assert.assertEquals("8.5K", SpaceConverterTools.convertToBiggestBUnit(8500L));
        Assert.assertEquals("8.5G", SpaceConverterTools.convertToBiggestBUnit(8500000000L));
        Assert.assertEquals("8.5T", SpaceConverterTools.convertToBiggestBUnit(8500000000000L));
    }

    @Test
    public void testConvertToBiggestBUnit_TwoDecimal() {
        Assert.assertEquals("8.51K", SpaceConverterTools.convertToBiggestBUnit(8510L));
        Assert.assertEquals("8.51G", SpaceConverterTools.convertToBiggestBUnit(8510000000L));
        Assert.assertEquals("8.51T", SpaceConverterTools.convertToBiggestBUnit(8510000000000L));
    }

    @Test
    public void testConvertToBiggestBUnit_TwoDecimal_Bigger() {
        Assert.assertEquals("8.51K", SpaceConverterTools.convertToBiggestBUnit(8512L));
        Assert.assertEquals("8.51G", SpaceConverterTools.convertToBiggestBUnit(8512000000L));
        Assert.assertEquals("8.51T", SpaceConverterTools.convertToBiggestBUnit(8512000000000L));
    }

    @Test
    public void testConvertToBiggestBUnit_TwoDecimal_Bigger_Higher() {
        Assert.assertEquals("8.52K", SpaceConverterTools.convertToBiggestBUnit(8517L));
        Assert.assertEquals("8.52G", SpaceConverterTools.convertToBiggestBUnit(8517000000L));
        Assert.assertEquals("8.52T", SpaceConverterTools.convertToBiggestBUnit(8517000000000L));
    }

    @Test
    public void testConvertToBiggestBUnit_TwoDecimal_With_0() {
        Assert.assertEquals("8.01K", SpaceConverterTools.convertToBiggestBUnit(8010L));
        Assert.assertEquals("8.01G", SpaceConverterTools.convertToBiggestBUnit(8010000000L));
        Assert.assertEquals("8.01T", SpaceConverterTools.convertToBiggestBUnit(8010000000000L));
    }

}
