/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.smalltools.SecureRandomTools;

public class SecureRandomToolsTest {

    @Test
    public void testRandomBase64String() {
        testRandomBase64String(1);
        testRandomBase64String(10);
        testRandomBase64String(100);
        testRandomBase64String(1000);
    }

    private void testRandomBase64String(int length) {
        String actual = SecureRandomTools.randomBase64String(length);
        Assert.assertEquals(length, actual.length());
    }

}
