/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import org.junit.Assert;
import org.junit.Test;

public class SecureRandomToolsTest {

    @Test
    public void testRandomBase64String() {
        for (int i = 1; i < 30; ++i) {
            testRandomBase64String(i);
        }
        testRandomBase64String(100);
        testRandomBase64String(555);
        testRandomBase64String(1000);
    }

    private void testRandomBase64String(int length) {
        String actual = SecureRandomTools.randomBase64String(length);
        Assert.assertEquals(length, actual.length());
    }

    @Test
    public void testRandomHexString() {
        for (int i = 1; i < 30; ++i) {
            testRandomHexString(i);
        }
        testRandomHexString(100);
        testRandomHexString(555);
        testRandomHexString(1000);
    }

    private void testRandomHexString(int length) {
        String actual = SecureRandomTools.randomHexString(length);
        Assert.assertEquals(length, actual.length());
    }

}
