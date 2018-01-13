/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.streamwrapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.smalltools.crypt.symmetric.AESCrypt;
import com.foilen.smalltools.crypt.symmetric.AbstractSymmetricCrypt;
import com.foilen.smalltools.crypt.symmetric.SymmetricKey;
import com.foilen.smalltools.tools.CharsetTools;

/**
 * Tests for {@link SymmetricDecryptInputStreamWrapper} and {@link SymmetricCryptOutputStreamWrapper} using AES.
 */
public class SymmetricCryptStreamWrapperTest extends AbstractStreamWrapperTest {

    private static final int AES_BLOCK_SIZE = 16;

    AbstractSymmetricCrypt<?> crypt;
    private SymmetricKey key;
    private byte[] iv;

    public SymmetricCryptStreamWrapperTest() {
        crypt = new AESCrypt();
        key = crypt.generateKey(256);
        iv = crypt.generateIV();
    }

    @Test
    public void testMultiFlush() throws IOException {

        for (int i = 0; i < 30; ++i) {
            // Write
            byte[] b = "Hello World".getBytes(CharsetTools.UTF_8);
            byte[] actual = new byte[11];
            outputStream.write(b);
            outputStream.flush();

            // Read
            Assert.assertEquals(17, inputStream.available());
            int len = inputStream.read(actual);
            Assert.assertEquals(11, len);
            Assert.assertArrayEquals(b, Arrays.copyOf(actual, len));
        }
        outputStream.flush();
        outputStream.close();
        Assert.assertEquals(0, inputStream.available()); // EOF
        Assert.assertEquals(-1, inputStream.read()); // EOF
    }

    /**
     * Test correct use of IV in the stream cipher (compare encrypting in multiple parts with buffer and in full directly).
     *
     * @throws IOException
     */
    @Test
    public void testValidStreamCipherImplementation() throws IOException {
        // Encrypt in parts
        for (int i = 0; i < 30; ++i) {
            byte[] b = "Hello World".getBytes(CharsetTools.UTF_8);
            outputStream.write(b);
        }
        outputStream.flush();
        outputStream.close();
        int expectedBytes = 1 + 21 * AES_BLOCK_SIZE;
        byte[] actualCompleteFrame = new byte[expectedBytes];
        Assert.assertEquals(expectedBytes, inputStreamInitial.available());
        int len = inputStreamInitial.read(actualCompleteFrame);
        Assert.assertEquals(expectedBytes, len); // Read everything
        Assert.assertEquals(0, inputStreamInitial.available()); // EOF
        Assert.assertEquals(-1, inputStreamInitial.read()); // EOF
        Assert.assertEquals(21, actualCompleteFrame[0]); // Numbers of blocks

        byte[] actual = new byte[expectedBytes - 1];
        System.arraycopy(actualCompleteFrame, 1, actual, 0, actual.length);

        // Encrypt in one shoot
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 30; ++i) {
            sb.append("Hello World");
        }
        byte[] data = sb.toString().getBytes(CharsetTools.UTF_8);
        byte[] expected = crypt.encryptWithIV(key, iv, data);
        expectedBytes = 21 * AES_BLOCK_SIZE;
        Assert.assertEquals(expectedBytes, expected.length);

        // Compare
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void testWriteAndReadFullArrayCrypted() throws IOException {
        byte[] b = "Hello World".getBytes(CharsetTools.UTF_8);
        byte[] actual = new byte[1024];
        outputStream.write(b);
        outputStream.flush();
        int len = inputStreamInitial.read(actual);
        Assert.assertEquals(AES_BLOCK_SIZE + 1, len);
        for (int i = 0; i < b.length; ++i) {
            Assert.assertNotEquals(b[i], actual[i]);
        }
    }

    @Test
    public void testWriteAndReadPartialCrypted() throws IOException {
        byte[] b = "Hello World".getBytes(CharsetTools.UTF_8);
        byte[] actual = new byte[1024];
        outputStream.write(b, 6, 3);
        outputStream.flush();
        int len = inputStreamInitial.read(actual);
        Assert.assertEquals(AES_BLOCK_SIZE + 1, len);
        for (int i = 0; i < 3; ++i) {
            Assert.assertNotEquals(b[i + 6], actual[i]);
        }
    }

    @Override
    protected InputStream wrapInputStream(InputStream inputStream) {
        return new SymmetricDecryptInputStreamWrapper(inputStream, crypt, key, iv);
    }

    @Override
    protected OutputStream wrapOutputStream(OutputStream outputStream) {
        return new SymmetricCryptOutputStreamWrapper(outputStream, crypt, key, iv);
    }

}
