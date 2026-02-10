package com.foilen.smalltools.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        Assertions.assertEquals(length, actual.length());
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
        Assertions.assertEquals(length, actual.length());
    }

}
