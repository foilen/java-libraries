/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.streamwrapper;

import java.io.InputStream;

/**
 * A stream wrapper that does nothing with the stream.
 */
public class NoneInputStreamWrapper extends AbstractInputStreamWrapper {

    public NoneInputStreamWrapper(InputStream wrappedInputStream) {
        super(wrappedInputStream);
    }
}
