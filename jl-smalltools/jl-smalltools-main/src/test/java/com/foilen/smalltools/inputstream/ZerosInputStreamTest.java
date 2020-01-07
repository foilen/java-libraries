/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.inputstream;

import org.junit.Assert;
import org.junit.Test;

public class ZerosInputStreamTest {

    private void assertAllZeros(byte[] bytes, int start, int end) {
        for (int i = start; i < end; ++i) {
            Assert.assertEquals(0, bytes[i]);
        }
    }

    private void assertNotZeros(byte[] bytes, int start, int end) {
        for (int i = start; i < end; ++i) {
            Assert.assertEquals(1, bytes[i]);
        }
    }

    private void fillWithJunk(byte[] bytes) {
        for (int i = 0; i < bytes.length; ++i) {
            bytes[i] = 1;
        }
    }

    @Test
    public void testRead() throws Exception {
        long expected = 1024000;

        ZerosInputStream zerosInputStream = new ZerosInputStream(expected);
        long actual = 0;
        int next;
        while ((next = zerosInputStream.read()) != -1) {
            ++actual;
            Assert.assertEquals(0, next);
        }

        Assert.assertEquals(expected, actual);
        zerosInputStream.close();
    }

    @Test
    public void testReadByteArray() throws Exception {
        ZerosInputStream zerosInputStream = new ZerosInputStream(500);
        Assert.assertEquals(500, zerosInputStream.available());
        byte[] bytes = new byte[321];

        fillWithJunk(bytes);
        Assert.assertEquals(321, zerosInputStream.read(bytes));
        assertAllZeros(bytes, 0, 321);
        Assert.assertEquals(179, zerosInputStream.available());

        fillWithJunk(bytes);
        Assert.assertEquals(179, zerosInputStream.read(bytes));
        assertAllZeros(bytes, 0, 179);
        assertNotZeros(bytes, 179, 321);

        Assert.assertEquals(0, zerosInputStream.available());

        zerosInputStream.close();
    }

    @Test
    public void testReadByteArrayIntInt() throws Exception {
        ZerosInputStream zerosInputStream = new ZerosInputStream(500);
        Assert.assertEquals(500, zerosInputStream.available());
        byte[] bytes = new byte[321];

        fillWithJunk(bytes);
        Assert.assertEquals(290, zerosInputStream.read(bytes, 10, 290));
        assertNotZeros(bytes, 0, 10);
        assertAllZeros(bytes, 10, 300);
        assertNotZeros(bytes, 300, 321);
        Assert.assertEquals(210, zerosInputStream.available());

        fillWithJunk(bytes);
        Assert.assertEquals(210, zerosInputStream.read(bytes, 20, 300));
        assertNotZeros(bytes, 0, 20);
        assertAllZeros(bytes, 20, 230);
        assertNotZeros(bytes, 230, 321);
        Assert.assertEquals(0, zerosInputStream.available());

        zerosInputStream.close();
    }

    @Test
    public void testSkip() throws Exception {
        ZerosInputStream zerosInputStream = new ZerosInputStream(500);
        Assert.assertEquals(500, zerosInputStream.available());
        Assert.assertEquals(0, zerosInputStream.skip(-10));
        Assert.assertEquals(0, zerosInputStream.skip(0));
        Assert.assertEquals(500, zerosInputStream.available());
        Assert.assertEquals(300, zerosInputStream.skip(300));
        Assert.assertEquals(200, zerosInputStream.available());
        Assert.assertEquals(200, zerosInputStream.skip(250));
        Assert.assertEquals(0, zerosInputStream.available());
        Assert.assertEquals(0, zerosInputStream.skip(250));
        Assert.assertEquals(0, zerosInputStream.available());
        zerosInputStream.close();
    }

}
