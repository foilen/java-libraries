/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.streamwrapper;

import java.io.IOException;
import java.io.InputStream;

/**
 * Wrap an input stream to modify its behavior.
 */
public abstract class AbstractInputStreamWrapper extends InputStream {

    protected InputStream wrappedInputStream;

    public AbstractInputStreamWrapper(InputStream wrappedInputStream) {
        this.wrappedInputStream = wrappedInputStream;
    }

    @Override
    public int available() throws IOException {
        return wrappedInputStream.available();
    }

    @Override
    public void close() throws IOException {
        wrappedInputStream.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        wrappedInputStream.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return wrappedInputStream.markSupported();
    }

    @Override
    public int read() throws IOException {
        return wrappedInputStream.read();
    }

    @Override
    public int read(byte b[]) throws IOException {
        return wrappedInputStream.read(b);
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        return wrappedInputStream.read(b, off, len);
    }

    @Override
    public synchronized void reset() throws IOException {
        wrappedInputStream.reset();
    }

    @Override
    public long skip(long n) throws IOException {
        return wrappedInputStream.skip(n);
    }
}
