/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.io.Closeable;

/**
 * Some simple methods to play with {@link Closeable}.
 */
public final class CloseableTools {

    /**
     * Close quietly.
     * 
     * @param closeable
     *            the {@link Closeable} to close.
     */
    public static void close(Closeable closeable) {
        try {
            closeable.close();
        } catch (Exception e) {
        }
    }

    private CloseableTools() {
    }
}
