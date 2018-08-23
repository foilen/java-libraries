/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import org.junit.Assert;
import org.junit.Test;

public class StringToolsTest {

    @Test
    public void testSafeComparisonNullFirst() {
        Assert.assertTrue(StringTools.safeComparisonNullFirst("a", "a") == 0);
        Assert.assertTrue(StringTools.safeComparisonNullFirst("a", "b") < 0);
        Assert.assertTrue(StringTools.safeComparisonNullFirst("b", "a") > 0);
        Assert.assertTrue(StringTools.safeComparisonNullFirst(null, "b") < 0);
        Assert.assertTrue(StringTools.safeComparisonNullFirst("b", null) > 0);
        Assert.assertTrue(StringTools.safeComparisonNullFirst(null, null) == 0);
    }

    @Test
    public void testSafeComparisonNullLast() {
        Assert.assertTrue(StringTools.safeComparisonNullLast("a", "a") == 0);
        Assert.assertTrue(StringTools.safeComparisonNullLast("a", "b") < 0);
        Assert.assertTrue(StringTools.safeComparisonNullLast("b", "a") > 0);
        Assert.assertTrue(StringTools.safeComparisonNullLast(null, "b") > 0);
        Assert.assertTrue(StringTools.safeComparisonNullLast("b", null) < 0);
        Assert.assertTrue(StringTools.safeComparisonNullLast(null, null) == 0);
    }

    @Test
    public void testSafeEquals() {
        Assert.assertTrue(StringTools.safeEquals(null, null));
        Assert.assertFalse(StringTools.safeEquals("a", null));
        Assert.assertFalse(StringTools.safeEquals(null, "a"));

        Assert.assertTrue(StringTools.safeEquals("a", "a"));
        Assert.assertTrue(StringTools.safeEquals("", ""));
        Assert.assertTrue(StringTools.safeEquals("hello", "hello"));

        Assert.assertFalse(StringTools.safeEquals("a", "b"));
        Assert.assertFalse(StringTools.safeEquals("a", ""));
    }

}
