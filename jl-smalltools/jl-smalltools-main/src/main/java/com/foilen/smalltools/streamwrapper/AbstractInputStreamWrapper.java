package com.foilen.smalltools.streamwrapper;

import java.io.IOException;
import java.io.InputStream;

/**
 * Wrap an input stream to modify its behavior.
 */
public abstract class AbstractInputStreamWrapper extends InputStream {

    /**
     * The wrapped input stream.
     */
    protected InputStream wrappedInputStream;

    /**
     * Wrap an input stream to modify its behavior.
     *
     * @param wrappedInputStream the wrapped input stream
     */
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
