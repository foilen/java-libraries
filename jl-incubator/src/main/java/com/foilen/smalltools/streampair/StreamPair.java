/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.streampair;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.foilen.smalltools.exception.SmallToolsException;

/**
 * This is used to keep a pair of input and output. The main goal is to be able to wrap these with input and output stream wrapper and simply set the wrapper here.
 */
public class StreamPair implements Closeable {

    private InputStream inputStream;
    private OutputStream outputStream;

    public StreamPair() {
    }

    public StreamPair(InputStream inputStream, OutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    public StreamPair(Socket socket) {
        try {
            inputStream = socket.getInputStream();
        } catch (IOException e) {
            throw new SmallToolsException("Problem getting the input stream", e);
        }
        try {
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            throw new SmallToolsException("Problem getting the output stream", e);
        }
    }

    /**
     * Close both streams.
     */
    @Override
    public void close() {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
            }
        }
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
            }
        }
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

}
