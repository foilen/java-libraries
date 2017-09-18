/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import org.junit.Assert;
import org.junit.Test;

public class InternetToolsTest {

    @Test
    public void testIsIpLocalLoop() {
        Assert.assertTrue(InternetTools.isIpLocalLoop("127.0.0.1"));
        Assert.assertTrue(InternetTools.isIpLocalLoop("127.0.0.2"));
        Assert.assertTrue(InternetTools.isIpLocalLoop("0:0:0:0:0:0:0:1"));
        Assert.assertTrue(InternetTools.isIpLocalLoop("fe80:6:8:3:2:1:7:8:8"));
        Assert.assertTrue(InternetTools.isIpLocalLoop("FE80:6:8:3:2:1:7:8:8"));
        Assert.assertFalse(InternetTools.isIpLocalLoop("192.168.0.2"));
        Assert.assertFalse(InternetTools.isIpLocalLoop("10.0.0.1"));
        Assert.assertFalse(InternetTools.isIpLocalLoop("2607:f8b0:400b:80c:0:200e"));
    }

}
