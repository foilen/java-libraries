/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.test.asserts;

import org.junit.Assert;
import org.junit.Test;

public class AssertToolsTest {

    private void testAssertEqualsDeltaFail(int expected, int actual, int delta, String errorMessage) {
        try {
            AssertTools.assertEqualsDelta(expected, actual, delta);
        } catch (Throwable t) {
            Assert.assertEquals(errorMessage, t.getMessage());
            return;
        }

        Assert.fail("Did not fail");
    }

    private void testAssertEqualsDeltaFail(long expected, long actual, long delta, String errorMessage) {
        try {
            AssertTools.assertEqualsDelta(expected, actual, delta);
        } catch (Throwable t) {
            Assert.assertEquals(errorMessage, t.getMessage());
            return;
        }

        Assert.fail("Did not fail");
    }

    @Test
    public void testAssertEqualsDeltaInt() {

        // Not failing
        for (long actual = 10; actual <= 30; ++actual) {
            AssertTools.assertEqualsDelta(20, actual, 10);
        }

        // Borders
        testAssertEqualsDeltaFail(20, 9, 10, "Expecting value between 10 and 30, but got 9");
        testAssertEqualsDeltaFail(20, 31, 10, "Expecting value between 10 and 30, but got 31");

        // Outside
        testAssertEqualsDeltaFail(20, 60, 10, "Expecting value between 10 and 30, but got 60");
        testAssertEqualsDeltaFail(20, -7, 10, "Expecting value between 10 and 30, but got -7");
    }

    @Test
    public void testAssertEqualsDeltaLong() {

        // Not failing
        for (long actual = 10L; actual <= 30L; ++actual) {
            AssertTools.assertEqualsDelta(20L, actual, 10L);
        }

        // Borders
        testAssertEqualsDeltaFail(20L, 9L, 10L, "Expecting value between 10 and 30, but got 9");
        testAssertEqualsDeltaFail(20L, 31L, 10L, "Expecting value between 10 and 30, but got 31");

        // Outside
        testAssertEqualsDeltaFail(20L, 60L, 10L, "Expecting value between 10 and 30, but got 60");
        testAssertEqualsDeltaFail(20L, -7L, 10L, "Expecting value between 10 and 30, but got -7");
    }

}
