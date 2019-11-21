/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@Deprecated
public class SpaceConverterToolTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testConvertBytes_allNumbers() {
        Assert.assertEquals(1023456789000000L, SpaceConverterTool.convertToBytes("1023456789 m"));
    }

    @Test
    public void testConvertBytes_bytes() {
        Assert.assertEquals(85L, SpaceConverterTool.convertToBytes("85"));
        Assert.assertEquals(85L, SpaceConverterTool.convertToBytes("85b"));
        Assert.assertEquals(85L, SpaceConverterTool.convertToBytes("85B"));
        Assert.assertEquals(85L, SpaceConverterTool.convertToBytes("85 B"));
        Assert.assertEquals(85L, SpaceConverterTool.convertToBytes("85 b"));
    }

    @Test
    public void testConvertBytes_gibibytes() {
        Assert.assertEquals(91268055040L, SpaceConverterTool.convertToBytes("85gib"));
        Assert.assertEquals(91268055040L, SpaceConverterTool.convertToBytes("85 giB"));
    }

    @Test
    public void testConvertBytes_gigabytes() {
        Assert.assertEquals(85000000000L, SpaceConverterTool.convertToBytes("85g"));
        Assert.assertEquals(85000000000L, SpaceConverterTool.convertToBytes("85 g"));
        Assert.assertEquals(85000000000L, SpaceConverterTool.convertToBytes("85 gB"));
    }

    @Test
    public void testConvertBytes_invalid_char() {
        thrown.expectMessage("% is an invalid space");
        SpaceConverterTool.convertToBytes("%");
    }

    @Test
    public void testConvertBytes_invalid_number() {
        thrown.expectMessage("kb is an invalid space");
        SpaceConverterTool.convertToBytes("kb");
    }

    @Test
    public void testConvertBytes_invalid_unit() {
        thrown.expectMessage("75J is an invalid space");
        SpaceConverterTool.convertToBytes("75J");
    }

    @Test
    public void testConvertBytes_kibibytes() {
        Assert.assertEquals(87040L, SpaceConverterTool.convertToBytes("85kib"));
        Assert.assertEquals(87040L, SpaceConverterTool.convertToBytes("85 kiB"));
    }

    @Test
    public void testConvertBytes_kilobytes() {
        Assert.assertEquals(85000L, SpaceConverterTool.convertToBytes("85k"));
        Assert.assertEquals(85660L, SpaceConverterTool.convertToBytes("85.66k"));
        Assert.assertEquals(85000L, SpaceConverterTool.convertToBytes("85 k"));
        Assert.assertEquals(85000L, SpaceConverterTool.convertToBytes("85\t k"));
        Assert.assertEquals(85000L, SpaceConverterTool.convertToBytes("85 kB"));
    }

    @Test
    public void testConvertBytes_mebibytes() {
        Assert.assertEquals(89128960L, SpaceConverterTool.convertToBytes("85mib"));
        Assert.assertEquals(89128960L, SpaceConverterTool.convertToBytes("85 miB"));
        Assert.assertEquals(89128960L, SpaceConverterTool.convertToBytes("85    miB   "));
    }

    @Test
    public void testConvertBytes_megabytes() {
        Assert.assertEquals(85000000L, SpaceConverterTool.convertToBytes("85m"));
        Assert.assertEquals(85000000L, SpaceConverterTool.convertToBytes("85 m"));
        Assert.assertEquals(85000000L, SpaceConverterTool.convertToBytes("85 mB"));
    }

    @Test
    public void testConvertBytes_tebibytes() {
        Assert.assertEquals(93458488360960L, SpaceConverterTool.convertToBytes("85tib"));
        Assert.assertEquals(93458488360960L, SpaceConverterTool.convertToBytes("85 tiB"));
    }

    @Test
    public void testConvertBytes_terabytes() {
        Assert.assertEquals(85000000000000L, SpaceConverterTool.convertToBytes("85t"));
        Assert.assertEquals(85000000000000L, SpaceConverterTool.convertToBytes("85 t"));
        Assert.assertEquals(85000000000000L, SpaceConverterTool.convertToBytes("85 tB"));
    }

    @Test
    public void testConvertToBiggestBUnit_Exact() {
        Assert.assertEquals("0B", SpaceConverterTool.convertToBiggestBUnit(0L));
        Assert.assertEquals("1B", SpaceConverterTool.convertToBiggestBUnit(1L));
        Assert.assertEquals("1K", SpaceConverterTool.convertToBiggestBUnit(1000L));
        Assert.assertEquals("1M", SpaceConverterTool.convertToBiggestBUnit(1000000L));
        Assert.assertEquals("1G", SpaceConverterTool.convertToBiggestBUnit(1000000000L));
        Assert.assertEquals("1T", SpaceConverterTool.convertToBiggestBUnit(1000000000000L));
    }

    @Test
    public void testConvertToBiggestBUnit_NoDecimal() {
        Assert.assertEquals("85B", SpaceConverterTool.convertToBiggestBUnit(85L));
        Assert.assertEquals("85K", SpaceConverterTool.convertToBiggestBUnit(85000L));
        Assert.assertEquals("85G", SpaceConverterTool.convertToBiggestBUnit(85000000000L));
        Assert.assertEquals("85T", SpaceConverterTool.convertToBiggestBUnit(85000000000000L));
    }

    @Test
    public void testConvertToBiggestBUnit_OneDecimal() {
        Assert.assertEquals("8.5K", SpaceConverterTool.convertToBiggestBUnit(8500L));
        Assert.assertEquals("8.5G", SpaceConverterTool.convertToBiggestBUnit(8500000000L));
        Assert.assertEquals("8.5T", SpaceConverterTool.convertToBiggestBUnit(8500000000000L));
    }

    @Test
    public void testConvertToBiggestBUnit_TwoDecimal() {
        Assert.assertEquals("8.51K", SpaceConverterTool.convertToBiggestBUnit(8510L));
        Assert.assertEquals("8.51G", SpaceConverterTool.convertToBiggestBUnit(8510000000L));
        Assert.assertEquals("8.51T", SpaceConverterTool.convertToBiggestBUnit(8510000000000L));
    }

    @Test
    public void testConvertToBiggestBUnit_TwoDecimal_Bigger() {
        Assert.assertEquals("8.51K", SpaceConverterTool.convertToBiggestBUnit(8512L));
        Assert.assertEquals("8.51G", SpaceConverterTool.convertToBiggestBUnit(8512000000L));
        Assert.assertEquals("8.51T", SpaceConverterTool.convertToBiggestBUnit(8512000000000L));
    }

    @Test
    public void testConvertToBiggestBUnit_TwoDecimal_Bigger_Higher() {
        Assert.assertEquals("8.52K", SpaceConverterTool.convertToBiggestBUnit(8517L));
        Assert.assertEquals("8.52G", SpaceConverterTool.convertToBiggestBUnit(8517000000L));
        Assert.assertEquals("8.52T", SpaceConverterTool.convertToBiggestBUnit(8517000000000L));
    }

    @Test
    public void testConvertToBiggestBUnit_TwoDecimal_With_0() {
        Assert.assertEquals("8.01K", SpaceConverterTool.convertToBiggestBUnit(8010L));
        Assert.assertEquals("8.01G", SpaceConverterTool.convertToBiggestBUnit(8010000000L));
        Assert.assertEquals("8.01T", SpaceConverterTool.convertToBiggestBUnit(8010000000000L));
    }

}
