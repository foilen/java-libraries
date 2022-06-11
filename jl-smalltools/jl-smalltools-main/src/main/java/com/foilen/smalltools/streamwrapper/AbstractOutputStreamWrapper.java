/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.streamwrapper;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Wrap an output stream to modify its behavior.
 */
public abstract class AbstractOutputStreamWrapper extends OutputStream {

    protected OutputStream wrappedOutputStream;

    public AbstractOutputStreamWrapper(OutputStream wrappedOutputStream) {
        this.wrappedOutputStream = wrappedOutputStream;
    }

    @Override
    public void close() throws IOException {
        flush();
        wrappedOutputStream.close();
    }

    @Override
    public void flush() throws IOException {
        wrappedOutputStream.flush();
    }

    @Override
    public void write(byte b[]) throws IOException {
        wrappedOutputStream.write(b);
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
        wrappedOutputStream.write(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
        wrappedOutputStream.write(b);
    }

}
