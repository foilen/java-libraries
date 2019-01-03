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

}
