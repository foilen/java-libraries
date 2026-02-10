package com.foilen.smalltools.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InternetToolsTest {

    @Test
    public void testIsIpLocalLoop() {
        Assertions.assertTrue(InternetTools.isIpLocalLoop("127.0.0.1"));
        Assertions.assertTrue(InternetTools.isIpLocalLoop("127.0.0.2"));
        Assertions.assertTrue(InternetTools.isIpLocalLoop("0:0:0:0:0:0:0:1"));
        Assertions.assertTrue(InternetTools.isIpLocalLoop("fe80:6:8:3:2:1:7:8:8"));
        Assertions.assertTrue(InternetTools.isIpLocalLoop("FE80:6:8:3:2:1:7:8:8"));
        Assertions.assertFalse(InternetTools.isIpLocalLoop("192.168.0.2"));
        Assertions.assertFalse(InternetTools.isIpLocalLoop("10.0.0.1"));
        Assertions.assertFalse(InternetTools.isIpLocalLoop("2607:f8b0:400b:80c:0:200e"));
    }

}
