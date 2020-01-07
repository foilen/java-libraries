/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.streamwrapper;

import java.io.OutputStream;

/**
 * A stream wrapper that does nothing with the stream.
 */
public class NoneOutputStreamWrapper extends AbstractOutputStreamWrapper {

    public NoneOutputStreamWrapper(OutputStream wrappedOutputStream) {
        super(wrappedOutputStream);
    }

}
