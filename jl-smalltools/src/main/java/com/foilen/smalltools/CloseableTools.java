package com.foilen.smalltools;

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
