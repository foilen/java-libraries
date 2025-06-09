package com.foilen.smalltools.tools;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FrequencyConverterToolsTest {

    @Test
    public void testConvertToBiggestHzUnit_NullInput_ReturnsNull() {
        Long input = null;
        String result = FrequencyConverterTools.convertToBiggestHzUnit(input);
        assertNull(result);
    }

    @Test
    public void testConvertToBiggestHzUnit_HzInput_ReturnsHz() {
        Long input = 500L;
        String result = FrequencyConverterTools.convertToBiggestHzUnit(input);
        assertEquals("500Hz", result);
    }

    @Test
    public void testConvertToBiggestHzUnit_KhzInput_ReturnsKhz() {
        Long input = 1500L;
        String result = FrequencyConverterTools.convertToBiggestHzUnit(input);
        assertEquals("1.5Khz", result);
    }

    @Test
    public void testConvertToBiggestHzUnit_MhzInput_ReturnsMhz() {
        Long input = 1500000L;
        String result = FrequencyConverterTools.convertToBiggestHzUnit(input);
        assertEquals("1.5Mhz", result);
    }

    @Test
    public void testConvertToBiggestHzUnit_GhzInput_ReturnsGhz() {
        Long input = 1500000000L;
        String result = FrequencyConverterTools.convertToBiggestHzUnit(input);
        assertEquals("1.5Ghz", result);
    }

    @Test
    public void testConvertToBiggestHzUnit_ThzInput_ReturnsThz() {
        Long input = 1500000000000L;
        String result = FrequencyConverterTools.convertToBiggestHzUnit(input);
        assertEquals("1.5Thz", result);
    }

}