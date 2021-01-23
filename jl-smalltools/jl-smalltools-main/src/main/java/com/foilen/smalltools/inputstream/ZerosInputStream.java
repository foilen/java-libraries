/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.inputstream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * An {@link InputStream} that provides only zeros.
 */
public class ZerosInputStream extends InputStream {

    private static final int EOF = -1;

    private long bytesLeft;

    /**
     * The constructor.
     *
     * @param totalBytes
     *            the amount of bytes to send before the end of file
     */
    public ZerosInputStream(long totalBytes) {
        this.bytesLeft = totalBytes;
    }

    @Override
    public int available() throws IOException {
        if (bytesLeft > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }

        return (int) bytesLeft;
    }

    @Override
    public int read() throws IOException {
        if (bytesLeft == 0) {
            return EOF;
        }
        --bytesLeft;
        return 0;
    }

    @Override
    public int read(byte[] bytes) throws IOException {
        return read(bytes, 0, bytes.length);
    }

    @Override
    public int read(byte[] bytes, int off, int len) throws IOException {
        if (bytes == null) {
            throw new NullPointerException();
        }
        if ((off < 0) || (len < 0) || (len > bytes.length - off)) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return 0;
        }

        // Completed
        if (bytesLeft == 0) {
            return EOF;
        }

        // Bigger
        if (len > bytesLeft) {
            len = (int) bytesLeft;
            bytesLeft = 0;
        } else {
            bytesLeft -= len;
        }

        Arrays.fill(bytes, off, off + len, (byte) 0);

        return len;
    }

    @Override
    public long skip(long len) throws IOException {

        // Negative
        if (len <= 0) {
            return 0;
        }

        // Bigger
        if (len > bytesLeft) {
            len = bytesLeft;
            bytesLeft = 0;
            return len;
        }

        // Skip
        bytesLeft -= len;
        return len;

    }

}
