package com.foilen.smalltools.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SpaceConverterToolsTest {

    @Test
    public void testConvertBytes_allNumbers() {
        Assertions.assertEquals(1023456789000000L, SpaceConverterTools.convertToBytes("1023456789 m"));
    }

    @Test
    public void testConvertBytes_bytes() {
        Assertions.assertEquals(85L, SpaceConverterTools.convertToBytes("85"));
        Assertions.assertEquals(85L, SpaceConverterTools.convertToBytes("85b"));
        Assertions.assertEquals(85L, SpaceConverterTools.convertToBytes("85B"));
        Assertions.assertEquals(85L, SpaceConverterTools.convertToBytes("85 B"));
        Assertions.assertEquals(85L, SpaceConverterTools.convertToBytes("85 b"));
    }

    @Test
    public void testConvertBytes_gibibytes() {
        Assertions.assertEquals(91268055040L, SpaceConverterTools.convertToBytes("85gib"));
        Assertions.assertEquals(91268055040L, SpaceConverterTools.convertToBytes("85 giB"));
    }

    @Test
    public void testConvertBytes_gigabytes() {
        Assertions.assertEquals(85000000000L, SpaceConverterTools.convertToBytes("85g"));
        Assertions.assertEquals(85000000000L, SpaceConverterTools.convertToBytes("85 g"));
        Assertions.assertEquals(85000000000L, SpaceConverterTools.convertToBytes("85 gB"));
    }

    @Test
    public void testConvertBytes_invalid_char() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            SpaceConverterTools.convertToBytes("%");
        });
        assertTrue(exception.getMessage().contains("% is an invalid space"));
    }

    @Test
    public void testConvertBytes_invalid_number() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            SpaceConverterTools.convertToBytes("kb");
        });
        assertTrue(exception.getMessage().contains("kb is an invalid space"));
    }

    @Test
    public void testConvertBytes_invalid_unit() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            SpaceConverterTools.convertToBytes("75J");
        });
        assertTrue(exception.getMessage().contains("75J is an invalid space"));
    }

    @Test
    public void testConvertBytes_kibibytes() {
        Assertions.assertEquals(87040L, SpaceConverterTools.convertToBytes("85kib"));
        Assertions.assertEquals(87040L, SpaceConverterTools.convertToBytes("85 kiB"));
    }

    @Test
    public void testConvertBytes_kilobytes() {
        Assertions.assertEquals(85000L, SpaceConverterTools.convertToBytes("85k"));
        Assertions.assertEquals(85660L, SpaceConverterTools.convertToBytes("85.66k"));
        Assertions.assertEquals(85000L, SpaceConverterTools.convertToBytes("85 k"));
        Assertions.assertEquals(85000L, SpaceConverterTools.convertToBytes("85\t k"));
        Assertions.assertEquals(85000L, SpaceConverterTools.convertToBytes("85 kB"));
    }

    @Test
    public void testConvertBytes_mebibytes() {
        Assertions.assertEquals(89128960L, SpaceConverterTools.convertToBytes("85mib"));
        Assertions.assertEquals(89128960L, SpaceConverterTools.convertToBytes("85 miB"));
        Assertions.assertEquals(89128960L, SpaceConverterTools.convertToBytes("85    miB   "));
    }

    @Test
    public void testConvertBytes_megabytes() {
        Assertions.assertEquals(85000000L, SpaceConverterTools.convertToBytes("85m"));
        Assertions.assertEquals(85000000L, SpaceConverterTools.convertToBytes("85 m"));
        Assertions.assertEquals(85000000L, SpaceConverterTools.convertToBytes("85 mB"));
    }

    @Test
    public void testConvertBytes_tebibytes() {
        Assertions.assertEquals(93458488360960L, SpaceConverterTools.convertToBytes("85tib"));
        Assertions.assertEquals(93458488360960L, SpaceConverterTools.convertToBytes("85 tiB"));
    }

    @Test
    public void testConvertBytes_terabytes() {
        Assertions.assertEquals(85000000000000L, SpaceConverterTools.convertToBytes("85t"));
        Assertions.assertEquals(85000000000000L, SpaceConverterTools.convertToBytes("85 t"));
        Assertions.assertEquals(85000000000000L, SpaceConverterTools.convertToBytes("85 tB"));
    }

    @Test
    public void testConvertToBiggestBUnit_Exact() {
        Assertions.assertEquals("0B", SpaceConverterTools.convertToBiggestBUnit(0L));
        Assertions.assertEquals("1B", SpaceConverterTools.convertToBiggestBUnit(1L));
        Assertions.assertEquals("1K", SpaceConverterTools.convertToBiggestBUnit(1000L));
        Assertions.assertEquals("1M", SpaceConverterTools.convertToBiggestBUnit(1000000L));
        Assertions.assertEquals("1G", SpaceConverterTools.convertToBiggestBUnit(1000000000L));
        Assertions.assertEquals("1T", SpaceConverterTools.convertToBiggestBUnit(1000000000000L));
    }

    @Test
    public void testConvertToBiggestBUnit_NoDecimal() {
        Assertions.assertEquals("85B", SpaceConverterTools.convertToBiggestBUnit(85L));
        Assertions.assertEquals("85K", SpaceConverterTools.convertToBiggestBUnit(85000L));
        Assertions.assertEquals("85G", SpaceConverterTools.convertToBiggestBUnit(85000000000L));
        Assertions.assertEquals("85T", SpaceConverterTools.convertToBiggestBUnit(85000000000000L));
    }

    @Test
    public void testConvertToBiggestBUnit_OneDecimal() {
        Assertions.assertEquals("8.5K", SpaceConverterTools.convertToBiggestBUnit(8500L));
        Assertions.assertEquals("8.5G", SpaceConverterTools.convertToBiggestBUnit(8500000000L));
        Assertions.assertEquals("8.5T", SpaceConverterTools.convertToBiggestBUnit(8500000000000L));
    }

    @Test
    public void testConvertToBiggestBUnit_TwoDecimal() {
        Assertions.assertEquals("8.51K", SpaceConverterTools.convertToBiggestBUnit(8510L));
        Assertions.assertEquals("8.51G", SpaceConverterTools.convertToBiggestBUnit(8510000000L));
        Assertions.assertEquals("8.51T", SpaceConverterTools.convertToBiggestBUnit(8510000000000L));
    }

    @Test
    public void testConvertToBiggestBUnit_TwoDecimal_Bigger() {
        Assertions.assertEquals("8.51K", SpaceConverterTools.convertToBiggestBUnit(8512L));
        Assertions.assertEquals("8.51G", SpaceConverterTools.convertToBiggestBUnit(8512000000L));
        Assertions.assertEquals("8.51T", SpaceConverterTools.convertToBiggestBUnit(8512000000000L));
    }

    @Test
    public void testConvertToBiggestBUnit_TwoDecimal_Bigger_Higher() {
        Assertions.assertEquals("8.52K", SpaceConverterTools.convertToBiggestBUnit(8517L));
        Assertions.assertEquals("8.52G", SpaceConverterTools.convertToBiggestBUnit(8517000000L));
        Assertions.assertEquals("8.52T", SpaceConverterTools.convertToBiggestBUnit(8517000000000L));
    }

    @Test
    public void testConvertToBiggestBUnit_TwoDecimal_With_0() {
        Assertions.assertEquals("8.01K", SpaceConverterTools.convertToBiggestBUnit(8010L));
        Assertions.assertEquals("8.01G", SpaceConverterTools.convertToBiggestBUnit(8010000000L));
        Assertions.assertEquals("8.01T", SpaceConverterTools.convertToBiggestBUnit(8010000000000L));
    }

}
