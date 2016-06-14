package com.foilen.smalltools.tools;

import org.junit.Assert;
import org.junit.Test;

public class StringToolsTest {

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
