package com.foilen.smalltools.streamwrapper;

import java.io.OutputStream;

/**
 * A stream wrapper that does nothing with the stream.
 */
public class NoneOutputStreamWrapper extends AbstractOutputStreamWrapper {

    /**
     * Create a new wrapper.
     *
     * @param wrappedOutputStream the stream to wrap
     */
    public NoneOutputStreamWrapper(OutputStream wrappedOutputStream) {
        super(wrappedOutputStream);
    }

}
