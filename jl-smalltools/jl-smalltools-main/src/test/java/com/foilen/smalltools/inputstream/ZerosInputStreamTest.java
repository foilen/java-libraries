package com.foilen.smalltools.inputstream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ZerosInputStreamTest {

    private void assertAllZeros(byte[] bytes, int start, int end) {
        for (int i = start; i < end; ++i) {
            Assertions.assertEquals(0, bytes[i]);
        }
    }

    private void assertNotZeros(byte[] bytes, int start, int end) {
        for (int i = start; i < end; ++i) {
            Assertions.assertEquals(1, bytes[i]);
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
            Assertions.assertEquals(0, next);
        }

        Assertions.assertEquals(expected, actual);
        zerosInputStream.close();
    }

    @Test
    public void testReadByteArray() throws Exception {
        ZerosInputStream zerosInputStream = new ZerosInputStream(500);
        Assertions.assertEquals(500, zerosInputStream.available());
        byte[] bytes = new byte[321];

        fillWithJunk(bytes);
        Assertions.assertEquals(321, zerosInputStream.read(bytes));
        assertAllZeros(bytes, 0, 321);
        Assertions.assertEquals(179, zerosInputStream.available());

        fillWithJunk(bytes);
        Assertions.assertEquals(179, zerosInputStream.read(bytes));
        assertAllZeros(bytes, 0, 179);
        assertNotZeros(bytes, 179, 321);

        Assertions.assertEquals(0, zerosInputStream.available());

        zerosInputStream.close();
    }

    @Test
    public void testReadByteArrayIntInt() throws Exception {
        ZerosInputStream zerosInputStream = new ZerosInputStream(500);
        Assertions.assertEquals(500, zerosInputStream.available());
        byte[] bytes = new byte[321];

        fillWithJunk(bytes);
        Assertions.assertEquals(290, zerosInputStream.read(bytes, 10, 290));
        assertNotZeros(bytes, 0, 10);
        assertAllZeros(bytes, 10, 300);
        assertNotZeros(bytes, 300, 321);
        Assertions.assertEquals(210, zerosInputStream.available());

        fillWithJunk(bytes);
        Assertions.assertEquals(210, zerosInputStream.read(bytes, 20, 300));
        assertNotZeros(bytes, 0, 20);
        assertAllZeros(bytes, 20, 230);
        assertNotZeros(bytes, 230, 321);
        Assertions.assertEquals(0, zerosInputStream.available());

        zerosInputStream.close();
    }

    @Test
    public void testSkip() throws Exception {
        ZerosInputStream zerosInputStream = new ZerosInputStream(500);
        Assertions.assertEquals(500, zerosInputStream.available());
        Assertions.assertEquals(0, zerosInputStream.skip(-10));
        Assertions.assertEquals(0, zerosInputStream.skip(0));
        Assertions.assertEquals(500, zerosInputStream.available());
        Assertions.assertEquals(300, zerosInputStream.skip(300));
        Assertions.assertEquals(200, zerosInputStream.available());
        Assertions.assertEquals(200, zerosInputStream.skip(250));
        Assertions.assertEquals(0, zerosInputStream.available());
        Assertions.assertEquals(0, zerosInputStream.skip(250));
        Assertions.assertEquals(0, zerosInputStream.available());
        zerosInputStream.close();
    }

}
