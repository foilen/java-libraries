package com.foilen.smalltools.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StringToolsTest {

    @Test
    public void testSafeComparisonNullFirst() {
        Assertions.assertTrue(StringTools.safeComparisonNullFirst("a", "a") == 0);
        Assertions.assertTrue(StringTools.safeComparisonNullFirst("a", "b") < 0);
        Assertions.assertTrue(StringTools.safeComparisonNullFirst("b", "a") > 0);
        Assertions.assertTrue(StringTools.safeComparisonNullFirst(null, "b") < 0);
        Assertions.assertTrue(StringTools.safeComparisonNullFirst("b", null) > 0);
        Assertions.assertTrue(StringTools.safeComparisonNullFirst(null, null) == 0);
    }

    @Test
    public void testSafeComparisonNullLast() {
        Assertions.assertTrue(StringTools.safeComparisonNullLast("a", "a") == 0);
        Assertions.assertTrue(StringTools.safeComparisonNullLast("a", "b") < 0);
        Assertions.assertTrue(StringTools.safeComparisonNullLast("b", "a") > 0);
        Assertions.assertTrue(StringTools.safeComparisonNullLast(null, "b") > 0);
        Assertions.assertTrue(StringTools.safeComparisonNullLast("b", null) < 0);
        Assertions.assertTrue(StringTools.safeComparisonNullLast(null, null) == 0);
    }

    @Test
    public void testSafeEquals() {
        Assertions.assertTrue(StringTools.safeEquals(null, null));
        Assertions.assertFalse(StringTools.safeEquals("a", null));
        Assertions.assertFalse(StringTools.safeEquals(null, "a"));

        Assertions.assertTrue(StringTools.safeEquals("a", "a"));
        Assertions.assertTrue(StringTools.safeEquals("", ""));
        Assertions.assertTrue(StringTools.safeEquals("hello", "hello"));

        Assertions.assertFalse(StringTools.safeEquals("a", "b"));
        Assertions.assertFalse(StringTools.safeEquals("a", ""));
    }

}
